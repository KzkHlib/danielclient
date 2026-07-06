package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Client;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.input.Mouse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * AutoClicker premium : moteur de timing avec drift, variance gaussienne,
 * micro-pauses, bursts et profils rapides. Fonctionne pour clic gauche/droit.
 */
public class AutoClickerModule extends Module {

    private final boolean fixedRight;

    private final Setting.Mode profile =
            (Setting.Mode) add(new Setting.Mode("Profil", 5, "Premium", "Stable", "Jitter", "Butterfly", "Drag", "Polar"));
    private final Setting.Number cpsMin =
            (Setting.Number) add(new Setting.Number("CPS min", 12, 1, 35, 1, true));
    private final Setting.Number cpsMax =
            (Setting.Number) add(new Setting.Number("CPS max", 16, 1, 35, 1, true));
    private final Setting.Mode mode =
            (Setting.Mode) add(new Setting.Mode("Mode", 0, "Maintien", "Toggle", "Hold touche"));
    private final Setting.Bool inInventory =
            (Setting.Bool) add(new Setting.Bool("Clic inventaire", false));
    private final Setting.Bool weaponOnly =
            (Setting.Bool) add(new Setting.Bool("Arme seulement", false));
    private final Setting.Number randomization =
            (Setting.Number) add(new Setting.Number("Randomisation", 18, 0, 60, 1, true));
    private final Setting.Number drift =
            (Setting.Number) add(new Setting.Number("Drift CPS", 12, 0, 45, 1, true));
    private final Setting.Bool fatigue =
            (Setting.Bool) add(new Setting.Bool("Fatigue", true));
    private final Setting.Number missChance =
            (Setting.Number) add(new Setting.Number("Miss %", 1, 0, 25, 1, true));
    private final Setting.Bool pauses =
            (Setting.Bool) add(new Setting.Bool("Micro-pauses", true));
    private final Setting.Number pauseChance =
            (Setting.Number) add(new Setting.Number("Pause chance", 5, 0, 30, 1, true));
    private final Setting.Number pauseMin =
            (Setting.Number) add(new Setting.Number("Pause min ms", 70, 20, 400, 10, true));
    private final Setting.Number pauseMax =
            (Setting.Number) add(new Setting.Number("Pause max ms", 180, 20, 800, 10, true));
    private final Setting.Number doubleClick =
            (Setting.Number) add(new Setting.Number("Double clic %", 4, 0, 60, 1, true));
    private final Setting.Number burstChance =
            (Setting.Number) add(new Setting.Number("Burst %", 2, 0, 40, 1, true));
    private final Setting.Number burstClicks =
            (Setting.Number) add(new Setting.Number("Burst clics", 3, 2, 6, 1, true));
    private final Setting.Mode clickSource;
    private final Setting.Number replayJitter;
    private final Setting.Bool breakBlock;
    private final Setting.Number missStreak;
    private final Setting.Number cpsCurve;
    private final Setting.Number polarCPS;

    private long nextClickTime = 0L;
    private long pauseUntil = 0L;
    private long nextDriftAt = 0L;
    private long holdStartedAt = 0L;
    private double driftOffset = 0.0;
    private boolean butterflyCloseClick = false;
    private int replayIndex = 0;
    private final List<Long> recordedIntervals = new ArrayList<Long>();
    private int missStreakCounter = 0;
    private long startTime = 0L;
    private int totalClicksInSession = 0;

    /** @param right false = clic gauche, true = clic droit. */
    public AutoClickerModule(String name, boolean right) {
        super(name, Category.COMBAT);
        this.fixedRight = right;
        if (!right) {
            clickSource = (Setting.Mode) add(new Setting.Mode("Source clics", 0, "Premium", "Recording"));
            replayJitter = (Setting.Number) add(new Setting.Number("Replay jitter", 6, 0, 40, 1, true));
            breakBlock = (Setting.Bool) add(new Setting.Bool("Break block", true));
        } else {
            clickSource = null;
            replayJitter = null;
            breakBlock = null;
        }
        missStreak = (Setting.Number) add(new Setting.Number("Miss streak", 0, 0, 8, 1, true));
        cpsCurve = (Setting.Number) add(new Setting.Number("Courbe CPS", 30, 0, 100, 5, true));
        polarCPS = (Setting.Number) add(new Setting.Number("CPS Polar", 12, 4, 20, 1, true));
    }

    private boolean right() { return fixedRight; }

    @Override
    public String arrayListSuffix() {
        if (usesRecording()) return "Recording " + recordedIntervals.size();
        return ((int) cpsMin.value) + "-" + ((int) cpsMax.value) + " " + profile.current();
    }

    @Override
    public void onEnable() {
        applyProfile();
        resetTiming();
        startTime = System.currentTimeMillis();
        totalClicksInSession = 0;
        missStreakCounter = 0;
    }

    @Override
    public void onDisable() {
        resetTiming();
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;
        holdKey = mode.is("Hold touche");

        if (usesRecording() && recordedIntervals.isEmpty()) loadLatestRecording();

        if (!shouldClick()) {
            resetTiming();
            return;
        }

        long now = System.currentTimeMillis();
        if (holdStartedAt == 0L) holdStartedAt = now;
        updateDrift(now);

        if (pauses.value && now < pauseUntil) return;
        if (nextClickTime == 0L) {
            nextClickTime = now + nextInterval();
            return;
        }

        int safety = 0;
        while (now >= nextClickTime && safety++ < 18) {
            // Miss streak : saute un clic tous les N clics pour simuler l'imprécision
            boolean shouldMiss = missStreak.value > 0
                    && missStreakCounter >= (int) missStreak.value;
            if (!shouldMiss && !roll(missChance.value)) {
                click();
                clickExtras();
                missStreakCounter = 0;
            } else {
                missStreakCounter++;
            }

            if (maybePause(now)) break;
            nextClickTime += nextInterval();
        }
    }

    private boolean shouldClick() {
        if (mc.currentScreen != null) {
            if (!inInventory.value || !(mc.currentScreen instanceof GuiContainer)) return false;
            int btn = right() ? 1 : 0;
            return Mouse.isButtonDown(btn);
        }

        if (!mc.inGameHasFocus) return false;
        if (weaponOnly.value && !right() && !holdingWeapon()) return false;
        if (!right() && !canBreakBlock()) return false;
        if (mode.is("Toggle")) return true;
        if (mode.is("Hold touche")) return key != 0 && org.lwjgl.input.Keyboard.isKeyDown(key);
        return Mouse.isButtonDown(right() ? 1 : 0);
    }

    private boolean usesRecording() {
        return clickSource != null && clickSource.is("Recording");
    }

    private boolean canBreakBlock() {
        if (breakBlock == null || breakBlock.value) return true;
        if (mc.objectMouseOver == null) return true;
        return mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK;
    }

    private void clickExtras() {
        if (roll(doubleClick.value)) {
            click();
        }

        if (roll(burstChance.value)) {
            int extra = Math.max(1, (int) burstClicks.value - 1);
            for (int i = 0; i < extra; i++) click();
        }
    }

    private boolean maybePause(long now) {
        if (!pauses.value || pauseChance.value <= 0) return false;
        double scaledChance = pauseChance.value / 100.0;
        if (profile.is("Drag")) scaledChance *= 0.35;
        if (Math.random() >= scaledChance) return false;

        double min = Math.min(pauseMin.value, pauseMax.value);
        double max = Math.max(pauseMin.value, pauseMax.value);
        pauseUntil = now + Math.round(min + Math.random() * (max - min));
        nextClickTime = pauseUntil + nextInterval();
        return true;
    }

    private long nextInterval() {
        clamp();

        if (usesRecording() && !recordedIntervals.isEmpty()) {
            long interval = recordedIntervals.get(replayIndex++ % recordedIntervals.size());
            if (replayJitter != null && replayJitter.value > 0) {
                interval = Math.round(interval * (1.0 + gaussian() * (replayJitter.value / 100.0)));
            }
            return Math.max(1L, interval);
        }

        double cps = cpsMin.value + Math.random() * (cpsMax.value - cpsMin.value);
        cps += driftOffset;

        // CPS curve : monte progressivement en CPS (simule l'accélération humaine)
        if (cpsCurve.value > 0 && holdStartedAt > 0L) {
            long held = System.currentTimeMillis() - holdStartedAt;
            double curveProgress = Math.min(1.0, held / 2000.0); // 2s pour atteindre le plein régime
            double curveFactor = 1.0 - (1.0 - curveProgress) * (cpsCurve.value / 100.0);
            cps *= curveFactor;
        }

        if (fatigue.value && holdStartedAt > 0L) {
            long held = System.currentTimeMillis() - holdStartedAt;
            double fatigueCps = Math.min(2.2, held / 12000.0);
            cps -= fatigueCps * (0.35 + Math.random() * 0.65);
        }

        if (profile.is("Stable")) {
            cps = (cpsMin.value + cpsMax.value) * 0.5 + driftOffset * 0.35;
        } else if (profile.is("Jitter")) {
            cps += gaussian() * 1.8;
        } else if (profile.is("Butterfly")) {
            butterflyCloseClick = !butterflyCloseClick;
            if (butterflyCloseClick) return 24L + (long) (Math.random() * 18.0);
            cps *= 0.58 + Math.random() * 0.18;
        } else if (profile.is("Drag")) {
            cps = Math.max(cps, cpsMax.value - 2.0 + Math.random() * 3.0);
        } else if (profile.is("Polar")) {
            // Polar : distribution de timing plus réaliste (gamma-like)
            double polarBase = polarCPS.value - 2.0 + Math.random() * 4.0;
            cps = polarBase + driftOffset * 0.5;
            // Ajoute une micro-variation sinusoïdale pour casser les patterns
            double phase = (System.currentTimeMillis() % 3000) / 3000.0 * Math.PI * 2;
            cps += Math.sin(phase) * 0.7;
        }

        cps = clampCps(cps);
        double interval = 1000.0 / cps;

        if (randomization.value > 0) {
            // Distribution de timing asymétrique (plus réaliste)
            double spread = randomization.value / 100.0;
            double noise = (Math.random() + Math.random() + Math.random() - 1.5) / 1.5;
            interval *= 1.0 + noise * spread;
        }

        if (profile.is("Jitter")) interval *= 0.72 + Math.random() * 0.62;
        if (profile.is("Drag")) interval *= 0.82 + Math.random() * 0.22;
        if (profile.is("Polar")) {
            // Polar : intervalle minimum plus haut (pas de 0ms)
            interval = Math.max(35.0, interval);
            // Ajoute un micro-jitter temporel
            interval += (Math.random() - 0.5) * 8.0;
        }

        return Math.max(1L, Math.round(interval));
    }

    private void updateDrift(long now) {
        if (now < nextDriftAt) return;
        double max = (cpsMax.value - cpsMin.value) * (drift.value / 100.0);
        driftOffset = gaussian() * Math.max(0.0, max);
        nextDriftAt = now + 550L + (long) (Math.random() * 1500.0);
    }

    private void applyProfile() {
        if (profile.is("Premium")) {
            if (randomization.value < 18) randomization.value = 18;
            if (drift.value < 12) drift.value = 12;
            pauses.value = true;
            fatigue.value = true;
        } else if (profile.is("Stable")) {
            randomization.value = Math.min(randomization.value, 8);
            drift.value = Math.min(drift.value, 6);
            pauseChance.value = Math.min(pauseChance.value, 2);
        } else if (profile.is("Jitter")) {
            if (randomization.value < 28) randomization.value = 28;
            if (drift.value < 20) drift.value = 20;
        } else if (profile.is("Butterfly")) {
            if (doubleClick.value < 18) doubleClick.value = 18;
            if (randomization.value < 14) randomization.value = 14;
        } else if (profile.is("Drag")) {
            if (cpsMin.value < 18) cpsMin.value = 18;
            if (cpsMax.value < 24) cpsMax.value = 24;
            if (burstChance.value < 10) burstChance.value = 10;
            pauses.value = false;
        } else if (profile.is("Polar")) {
            // Profil optimisé Polar : CPS modéré, randomisation élevée
            double pcps = polarCPS.value;
            cpsMin.value = Math.max(6, pcps - 3);
            cpsMax.value = Math.min(20, pcps + 3);
            if (randomization.value < 25) randomization.value = 25;
            if (drift.value < 15) drift.value = 15;
            pauses.value = true;
            fatigue.value = true;
            missChance.value = Math.max(missChance.value, 3);
            if (cpsCurve.value < 20) cpsCurve.value = 20;
        }
        clamp();
    }

    private void clamp() {
        if (cpsMax.value < cpsMin.value) cpsMax.value = cpsMin.value;
        if (pauseMax.value < pauseMin.value) pauseMax.value = pauseMin.value;
    }

    private double clampCps(double cps) {
        double min = Math.max(0.5, cpsMin.value - 4.0);
        double max = Math.max(min, cpsMax.value + 5.0);
        if (cps < min) return min;
        if (cps > max) return max;
        return cps;
    }

    private boolean holdingWeapon() {
        ItemStack held = mc.thePlayer.getHeldItem();
        return held != null && (held.getItem() instanceof ItemSword || held.getItem() instanceof ItemAxe);
    }

    private boolean roll(double percent) {
        return percent > 0 && Math.random() * 100.0 < percent;
    }

    private double gaussian() {
        double u = Math.max(0.000001, Math.random());
        double v = Math.max(0.000001, Math.random());
        return Math.sqrt(-2.0 * Math.log(u)) * Math.cos(2.0 * Math.PI * v) * 0.45;
    }

    private void resetTiming() {
        nextClickTime = 0L;
        pauseUntil = 0L;
        nextDriftAt = 0L;
        holdStartedAt = 0L;
        driftOffset = 0.0;
        butterflyCloseClick = false;
        replayIndex = 0;
    }

    private File recordsDir() {
        return new File(mc.mcDataDir, "DanielClient/click-records");
    }

    private void loadLatestRecording() {
        File latest = findNewestRecording();
        if (latest == null || !latest.exists()) return;

        recordedIntervals.clear();
        replayIndex = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(latest));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0 || line.startsWith("#")) continue;
                long interval = Long.parseLong(line);
                if (interval >= 1L && interval <= 1000L) recordedIntervals.add(interval);
            }
            br.close();
            if (Client.INSTANCE != null && !recordedIntervals.isEmpty()) {
                Client.INSTANCE.notifications.add(new Client.Notif("Macro chargee: " + latest.getName()));
            }
        } catch (Exception e) {
            recordedIntervals.clear();
        }
    }

    private File findNewestRecording() {
        File dir = recordsDir();
        if (!dir.exists()) dir.mkdirs();

        File latest = new File(dir, "autoclick-left-latest.clicks");
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                return name.toLowerCase().endsWith(".clicks");
            }
        });

        File best = latest.exists() ? latest : null;
        if (files != null) {
            for (File file : files) {
                if (best == null || file.lastModified() > best.lastModified()) best = file;
            }
        }
        return best;
    }

    private void click() {
        if (mc.currentScreen instanceof GuiContainer) {
            guiClick(mc.currentScreen);
        } else {
            resetVanillaDelay();
            KeyBinding kb = right() ? mc.gameSettings.keyBindUseItem
                                    : mc.gameSettings.keyBindAttack;
            KeyBinding.onTick(kb.getKeyCode());
        }
        Client.INSTANCE.recordClick(right());
    }

    private static Method mouseClickedMethod;

    private void guiClick(GuiScreen screen) {
        try {
            if (mouseClickedMethod == null) {
                mouseClickedMethod = ReflectionHelper.findMethod(
                        GuiScreen.class, screen,
                        new String[]{"mouseClicked", "func_73864_a"},
                        int.class, int.class, int.class);
            }
            ScaledResolution sr = new ScaledResolution(mc);
            int mx = Mouse.getX() * sr.getScaledWidth() / mc.displayWidth;
            int my = sr.getScaledHeight() - Mouse.getY() * sr.getScaledHeight() / mc.displayHeight - 1;
            mouseClickedMethod.invoke(screen, mx, my, right() ? 1 : 0);
        } catch (Exception ignored) {
        }
    }

    private static Field leftClickCounterField;
    private static Field rightClickDelayField;

    private void resetVanillaDelay() {
        try {
            if (right()) {
                if (rightClickDelayField == null) {
                    rightClickDelayField = ReflectionHelper.findField(
                            Minecraft.class, "rightClickDelayTimer", "field_71467_ac");
                }
                rightClickDelayField.setInt(mc, 0);
            } else {
                if (leftClickCounterField == null) {
                    leftClickCounterField = ReflectionHelper.findField(
                            Minecraft.class, "leftClickCounter", "field_71429_W");
                }
                leftClickCounterField.setInt(mc, 0);
            }
        } catch (Exception ignored) {
        }
    }
}
