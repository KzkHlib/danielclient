package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;
import com.example.autoclicker.client.render.Targets;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

/**
 * BowAimbot (légit) : pendant que tu BANDES l'arc, vise automatiquement la cible
 * la plus proche en compensant la gravité de la flèche et en anticipant son
 * déplacement. Rotation lissée, pas de tir auto (tu décides quand lâcher).
 */
public class BowAimbotModule extends Module {

    private final Setting.Number range =
            (Setting.Number) add(new Setting.Number("Portee", 60, 10, 120, 5, true));
    private final Setting.Number smooth =
            (Setting.Number) add(new Setting.Number("Smooth", 35, 1, 90, 1, true));
    private final Setting.Number fov =
            (Setting.Number) add(new Setting.Number("FOV", 360, 0, 360, 5, true));
    private final Setting.Bool prediction =
            (Setting.Bool) add(new Setting.Bool("Prédiction", true));
    private final Setting.Mode targetMode =
            (Setting.Mode) add(new Setting.Mode("Cibles", 1, "Mobs", "Joueurs", "Tous"));
    private final Setting.Bool throughWalls =
            (Setting.Bool) add(new Setting.Bool("A travers murs", false));

    public BowAimbotModule() {
        super("BowAimbot", Category.COMBAT);
    }

    @Override
    public void onTick() {
        EntityPlayer p = mc.thePlayer;
        if (p == null || mc.theWorld == null) return;
        ItemStack held = p.getHeldItem();
        if (held == null || !(held.getItem() instanceof ItemBow)) return;
        if (!p.isUsingItem()) return;   // seulement pendant la charge (légit)

        // puissance de tir actuelle (vitesse de la flèche)
        int useTicks = held.getMaxItemUseDuration() - p.getItemInUseCount();
        float f = useTicks / 20.0f;
        f = (f * f + f * 2.0f) / 3.0f;
        if (f > 1.0f) f = 1.0f;
        if (f < 0.1f) return;           // pas assez chargé
        double v = f * 3.0;

        EntityLivingBase t = nearest();
        if (t == null) return;

        double dx = t.posX - p.posX;
        double dz = t.posZ - p.posZ;
        if (prediction.value) {
            dx += (t.posX - t.prevPosX) * 4.0;   // anticipe légèrement
            dz += (t.posZ - t.prevPosZ) * 4.0;
        }
        double horiz = Math.sqrt(dx * dx + dz * dz);
        double dy = (t.posY + t.getEyeHeight() * 0.6) - (p.posY + p.getEyeHeight());

        float wantYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);

        // balistique : pitch pour atteindre (horiz, dy) avec gravité 0.05/tick
        double g = 0.05;
        double v2 = v * v;
        double root = v2 * v2 - g * (g * horiz * horiz + 2.0 * dy * v2);
        float wantPitch;
        if (root < 0) {
            wantPitch = -45f;           // hors de portée : tir tendu vers le haut
        } else {
            double angle = Math.atan((v2 - Math.sqrt(root)) / (g * horiz)); // trajectoire basse
            wantPitch = (float) -Math.toDegrees(angle);
        }
        wantPitch = MathHelper.clamp_float(wantPitch, -90f, 90f);

        // rotation lissée (légit)
        float maxS = (float) smooth.value;
        float dYaw = MathHelper.wrapAngleTo180_float(wantYaw - p.rotationYaw);
        float dPitch = MathHelper.wrapAngleTo180_float(wantPitch - p.rotationPitch);
        p.rotationYaw += clamp(dYaw, maxS);
        p.rotationPitch = MathHelper.clamp_float(p.rotationPitch + clamp(dPitch, maxS), -90f, 90f);
    }

    private EntityLivingBase nearest() {
        EntityLivingBase best = null;
        double bestDist = range.value;
        for (Object o : mc.theWorld.loadedEntityList) {
            if (!(o instanceof EntityLivingBase)) continue;
            EntityLivingBase e = (EntityLivingBase) o;
            if (e == mc.thePlayer || !e.isEntityAlive()) continue;
            if (Targets.isFriend(e) || AntiBotModule.isBot(e)) continue;
            if (!Targets.matches(e, targetMode.current())) continue;
            if (!inFov(e)) continue;
            if (!throughWalls.value && !mc.thePlayer.canEntityBeSeen(e)) continue;
            double d = mc.thePlayer.getDistanceToEntity(e);
            if (d <= bestDist) { bestDist = d; best = e; }
        }
        return best;
    }

    private boolean inFov(EntityLivingBase e) {
        if (fov.value >= 360) return true;
        double dx = e.posX - mc.thePlayer.posX;
        double dz = e.posZ - mc.thePlayer.posZ;
        float yawTo = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float diff = Math.abs(MathHelper.wrapAngleTo180_float(yawTo - mc.thePlayer.rotationYaw));
        return diff <= fov.value / 2.0;
    }

    private static float clamp(float v, float max) {
        if (v > max) return max;
        if (v < -max) return -max;
        return v;
    }
}
