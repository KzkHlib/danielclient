package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.client.entity.EntityPlayerSP;

/**
 * Velocity avancée : réduit/modifie le knockback reçu avec plusieurs modes
 * spécifiques pour bypasser Polar Anticheat. Modes : Cancel (0% total),
 * Push (inverse le KB), Humanize (variance aléatoire légère),
 * Legit (réduction douce), Décalé (appliqué plus tard).
 *
 * Le packet de vélocité est intercepté par MixinNetHandlerPlayClient,
 * qui lit les champs statiques ci-dessous.
 */
public class VelocityModule extends Module {

    public static volatile boolean active = false;
    public static volatile int horizontal = 0;
    public static volatile int vertical = 0;
    public static volatile boolean delayed = false;
    public static volatile int delayTicks = 2;

    // Modes spéciaux
    public static volatile boolean pushMode = false;     // inverse le KB
    public static volatile boolean humanizeMode = false; // variance aléatoire
    public static volatile double humanStrength = 0.3;   // force de la variance
    public static volatile double kbChance = 1.0;        // probabilité d'appliquer la réduction
    public static volatile double jitterH = 0.0;         // jitter horizontal
    public static volatile double jitterV = 0.0;         // jitter vertical

    // knockback en attente (mode Décalé)
    private static volatile double pX, pY, pZ;
    private static volatile int pCountdown = -1;

    private final Setting.Number h =
            (Setting.Number) add(new Setting.Number("Horizontal", 0, 0, 100, 5, true));
    private final Setting.Number v =
            (Setting.Number) add(new Setting.Number("Vertical", 0, 0, 100, 5, true));
    private final Setting.Mode mode =
            (Setting.Mode) add(new Setting.Mode("Mode", 0, "Cancel", "Push", "Humanize", "Legit", "Décalé"));
    private final Setting.Number delay =
            (Setting.Number) add(new Setting.Number("Delai", 2, 1, 10, 1, true));
    private final Setting.Number chance =
            (Setting.Number) add(new Setting.Number("Chance %", 100, 10, 100, 5, true));
    private final Setting.Number randomH =
            (Setting.Number) add(new Setting.Number("Jitter H", 0, 0, 30, 1, true));
    private final Setting.Number randomV =
            (Setting.Number) add(new Setting.Number("Jitter V", 0, 0, 30, 1, true));

    public VelocityModule() {
        super("Velocity", Category.COMBAT);
    }

    @Override public void onEnable()  { active = true; push(); }
    @Override public void onDisable() { active = false; pCountdown = -1; }

    @Override
    public void onTick() {
        push();
        // applique le knockback décalé arrivé à échéance
        if (pCountdown > 0) {
            pCountdown--;
            if (pCountdown == 0 && mc.thePlayer != null) {
                mc.thePlayer.motionX = pX;
                mc.thePlayer.motionY = pY;
                mc.thePlayer.motionZ = pZ;
            }
        }
    }

    private void push() {
        String m = mode.current();
        delayed = m.equals("Décalé");
        pushMode = m.equals("Push");
        humanizeMode = m.equals("Humanize");

        if (m.equals("Cancel")) {
            horizontal = 0;
            vertical = 0;
        } else if (m.equals("Push")) {
            horizontal = (int) h.value;
            vertical = (int) v.value;
        } else if (m.equals("Humanize")) {
            horizontal = 0;
            vertical = 0;
            humanStrength = (h.value + v.value) / 200.0; // 0.0 ~ 1.0
        } else if (m.equals("Legit")) {
            horizontal = Math.max(20, (int) h.value);
            vertical = Math.max(20, (int) v.value);
        } else {
            // Décalé / Normal
            horizontal = (int) h.value;
            vertical = (int) v.value;
        }

        delayTicks = (int) delay.value;
        kbChance = chance.value / 100.0;
        jitterH = randomH.value / 100.0;
        jitterV = randomV.value / 100.0;
    }

    /**
     * Applique la vélocité modifiée selon le mode actif.
     * Appelé par MixinNetHandlerPlayClient.
     */
    public static double[] apply(double mx, double my, double mz) {
        if (!active) return new double[]{mx, my, mz};

        // Le packet a déjà été annulé dans le mixin; on retourne ce qu'on veut
        double h = horizontal / 100.0;
        double v = vertical / 100.0;

        // Chance de ne PAS appliquer la réduction (simule des hits non-réduits)
        if (Math.random() > kbChance) {
            return new double[]{mx, my, mz};
        }

        // Jitter horizontal / vertical
        double jh = 1.0 + (Math.random() - 0.5) * 2.0 * jitterH;
        double jv = 1.0 + (Math.random() - 0.5) * 2.0 * jitterV;

        if (pushMode) {
            // Push : inverse légèrement le KB pour attirer vers le joueur
            return new double[]{-mx * h * jh, my * v * 0.3, -mz * h * jh};
        }

        if (humanizeMode) {
            // Humanize : KB presque normal mais avec variance douce
            double var = 1.0 - humanStrength * (0.2 + Math.random() * 0.8);
            return new double[]{mx * var * jh, my * (0.8 + Math.random() * 0.2) * jv, mz * var * jh};
        }

        // Normal / Legit / Cancel
        return new double[]{mx * h * jh, my * v * jv, mz * h * jh};
    }

    /** Programme un knockback à appliquer dans {@code ticks} ticks. */
    public static void schedule(double x, double y, double z, int ticks) {
        pX = x; pY = y; pZ = z;
        pCountdown = Math.max(1, ticks);
    }
}
