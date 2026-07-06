package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;

/**
 * AimAssist amélioré : verrouillage + lissage avec GCD correction,
 * micro-ajustements humains et courbe d'accélération/décélération.
 * Chaque rotation est snapée au GCD natif de la souris (0.5 / sens
 * / 0.15) pour paraître 100% humaine aux yeux de Polar Anticheat.
 */
public class AimAssistModule extends Module {

    private final Setting.Number range =
            (Setting.Number) add(new Setting.Number("Portee", 4.5, 1, 8, 0.5, false));
    private final Setting.Number fov =
            (Setting.Number) add(new Setting.Number("FOV", 360, 0, 360, 5, true));
    private final Setting.Number smoothness =
            (Setting.Number) add(new Setting.Number("Smoothness", 55, 1, 100, 1, true));
    private final Setting.Number maxH =
            (Setting.Number) add(new Setting.Number("Vitesse H", 40, 1, 90, 1, true));
    private final Setting.Number maxV =
            (Setting.Number) add(new Setting.Number("Vitesse V", 40, 1, 90, 1, true));
    private final Setting.Number randomization =
            (Setting.Number) add(new Setting.Number("Randomisation", 2, 0, 10, 1, true));
    private final Setting.Number stick =
            (Setting.Number) add(new Setting.Number("Stick", 2, 0, 10, 1, true));
    private final Setting.Bool prediction =
            (Setting.Bool) add(new Setting.Bool("Prediction", true));
    private final Setting.Bool legitPitch =
            (Setting.Bool) add(new Setting.Bool("Pitch doux", true));
    private final Setting.Mode targets =
            (Setting.Mode) add(new Setting.Mode("Cibles", 0, "Mobs", "Joueurs", "Tous"));
    private final Setting.Mode priority =
            (Setting.Mode) add(new Setting.Mode("Priorité", 0, "Distance", "Vie", "Angle"));
    private final Setting.Number switchDelay =
            (Setting.Number) add(new Setting.Number("Délai switch", 0, 0, 500, 50, true));
    private final Setting.Bool requireClick =
            (Setting.Bool) add(new Setting.Bool("Seulement en cliquant", false));
    private final Setting.Bool keepTarget =
            (Setting.Bool) add(new Setting.Bool("Garder cible", true));
    private final Setting.Bool throughWalls =
            (Setting.Bool) add(new Setting.Bool("A travers murs", false));
    private final Setting.Bool gcdCorrection =
            (Setting.Bool) add(new Setting.Bool("GCD correction", true));
    private final Setting.Number microAdjust =
            (Setting.Number) add(new Setting.Number("Micro-ajust", 10, 0, 50, 5, true));

    private EntityLivingBase target;
    private long lastSwitch = 0L;

    public AimAssistModule() {
        super("AimAssist", Category.COMBAT);
    }

    public EntityLivingBase getTarget() {
        return isEnabled() ? target : null;
    }

    @Override
    public String arrayListSuffix() {
        return target != null ? target.getName() : null;
    }

    @Override
    public void onDisable() {
        target = null;
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null || mc.theWorld == null) { target = null; return; }
        if (mc.currentScreen != null) return;
        if (requireClick.value && !org.lwjgl.input.Mouse.isButtonDown(0)) { target = null; return; }

        if (!keepTarget.value || !isValid(target)) {
            EntityLivingBase next = findBest();
            if (next != target) lastSwitch = System.currentTimeMillis();
            target = next;
        }
    }

    private boolean isValid(EntityLivingBase t) {
        if (t == null || t.isDead || !t.isEntityAlive()) return false;
        if (mc.thePlayer.getDistanceToEntity(t) > range.value) return false;
        if (!matchesFilter(t)) return false;
        if (!throughWalls.value && !mc.thePlayer.canEntityBeSeen(t)) return false;
        return true;
    }

    private boolean matchesFilter(EntityLivingBase t) {
        if (t == mc.thePlayer) return false;
        if (com.example.autoclicker.client.render.Targets.isFriend(t)) return false;
        if (AntiBotModule.isBot(t)) return false;
        if (targets.is("Mobs"))    return t instanceof EntityLiving;
        if (targets.is("Joueurs")) return t instanceof EntityPlayer;
        return t instanceof EntityLiving || t instanceof EntityPlayer;
    }

    private EntityLivingBase findBest() {
        EntityLivingBase best = null;
        double bestScore = Double.MAX_VALUE;
        for (Object o : mc.theWorld.loadedEntityList) {
            if (!(o instanceof EntityLivingBase)) continue;
            EntityLivingBase e = (EntityLivingBase) o;
            if (!isValidCandidate(e)) continue;
            double score;
            if (priority.is("Vie")) score = e.getHealth();
            else if (priority.is("Angle")) score = angleTo(e);
            else score = mc.thePlayer.getDistanceToEntity(e);
            if (score < bestScore) {
                bestScore = score;
                best = e;
            }
        }
        return best;
    }

    private boolean isValidCandidate(EntityLivingBase e) {
        if (e.isDead || !e.isEntityAlive()) return false;
        boolean inRange = mc.thePlayer.getDistanceToEntity(e) <= range.value;
        if (!inRange) return false;
        if (!matchesFilter(e)) return false;
        if (!inFov(e)) return false;
        if (!throughWalls.value && !mc.thePlayer.canEntityBeSeen(e)) return false;
        return true;
    }

    private double angleTo(EntityLivingBase e) {
        EntityPlayer p = mc.thePlayer;
        double dx = e.posX - p.posX;
        double dz = e.posZ - p.posZ;
        double dy = (e.posY + e.getEyeHeight() * 0.9) - (p.posY + p.getEyeHeight());
        double distXZ = Math.sqrt(dx * dx + dz * dz);
        float wantYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float wantPitch = (float) -Math.toDegrees(Math.atan2(dy, distXZ));
        float dYaw = MathHelper.wrapAngleTo180_float(wantYaw - p.rotationYaw);
        float dPitch = MathHelper.wrapAngleTo180_float(wantPitch - p.rotationPitch);
        return Math.sqrt(dYaw * dYaw + dPitch * dPitch);
    }

    private boolean inFov(Entity e) {
        if (fov.value >= 360) return true;
        EntityPlayer p = mc.thePlayer;
        double dx = e.posX - p.posX;
        double dz = e.posZ - p.posZ;
        float yawTo = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float diff = Math.abs(MathHelper.wrapAngleTo180_float(yawTo - p.rotationYaw));
        return diff <= fov.value / 2.0;
    }

    /**
     * Calcule le GCD de la souris à partir de la sensibilité Minecraft.
     * Les rotations légitimes sont toujours des multiples de cette valeur.
     * gcd = (yaw * 0.15) * sensitivity * 0.15  →  simplifié : 0.0225 * sens
     * En pratique : le pas minimum = 0.5 / (sens * 0.15)
     */
    private float getGCD() {
        float sens = mc.gameSettings.mouseSensitivity;
        if (sens < 0.01f) sens = 0.5f;
        return 0.5f / (sens * 0.15f);
    }

    /** Snap une valeur au multiple le plus proche du GCD. */
    private float applyGCD(float value) {
        if (!gcdCorrection.value) return value;
        float gcd = getGCD();
        if (gcd < 0.001f) return value;
        return Math.round(value / gcd) * gcd;
    }

    @Override
    public void onRender(float dt) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (mc.currentScreen != null) return;
        if (requireClick.value && !org.lwjgl.input.Mouse.isButtonDown(0)) return;
        EntityLivingBase t = target;
        if (t == null || !t.isEntityAlive()) return;
        if (switchDelay.value > 0 && System.currentTimeMillis() - lastSwitch < switchDelay.value) return;
        EntityPlayer p = mc.thePlayer;

        double tx = t.posX, ty = t.posY, tz = t.posZ;
        if (prediction.value) {
            double vx = t.posX - t.prevPosX;
            double vz = t.posZ - t.prevPosZ;
            if (vx * vx + vz * vz > 1e-5) { tx += vx * 2.0; tz += vz * 2.0; }
        }

        double dx = tx - p.posX;
        double dz = tz - p.posZ;
        double dy = (ty + t.getEyeHeight() * 0.9) - (p.posY + p.getEyeHeight());
        double distXZ = Math.sqrt(dx * dx + dz * dz);
        double dist = Math.sqrt(distXZ * distXZ + dy * dy);

        float wantYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float wantPitch = (float) -Math.toDegrees(Math.atan2(dy, distXZ));

        // Sway organique lent (sinus/cosinus)
        if (randomization.value > 0) {
            double tt = System.currentTimeMillis() / 1000.0;
            wantYaw += (float) (Math.sin(tt * 2.1) * randomization.value * 0.06);
            wantPitch += (float) (Math.cos(tt * 1.6) * randomization.value * 0.04);
        }

        float dYaw = MathHelper.wrapAngleTo180_float(wantYaw - p.rotationYaw);
        float dPitch = MathHelper.wrapAngleTo180_float(wantPitch - p.rotationPitch);

        // Lissage expo : f = 1 - e^(-k*dt)
        double k = (105.0 - smoothness.value) * 0.25;
        if (stick.value > 0 && dist < 4.0) {
            k += stick.value * (1.0 - dist / 4.0) * 1.5;
        }
        float f = (float) (1.0 - Math.exp(-k * dt));
        if (f > 1f) f = 1f;

        float stepYaw = dYaw * f;
        float stepPitch = dPitch * (legitPitch.value ? f * 0.85f : f);

        // Clamp en deg/frame
        float maxYaw = (float) (maxH.value * dt * 20.0);
        float maxPitch = (float) (maxV.value * dt * 20.0);
        stepYaw = clamp(stepYaw, maxYaw);
        stepPitch = clamp(stepPitch, maxPitch);

        // Micro-ajustements : petits dépassements puis retour (simule un humain qui corrige)
        if (microAdjust.value > 0 && dist < 5.0) {
            double adjust = microAdjust.value / 100.0;
            if (Math.abs(dYaw) < 2.0f && Math.random() < 0.3) {
                stepYaw += (float) ((Math.random() - 0.5) * adjust * 2.0);
            }
            if (Math.abs(dPitch) < 2.0f && Math.random() < 0.2) {
                stepPitch += (float) ((Math.random() - 0.5) * adjust * 1.5);
            }
        }

        // GCD correction : snap au pas natif de la souris
        stepYaw = applyGCD(stepYaw);
        stepPitch = applyGCD(stepPitch);

        p.rotationYaw += stepYaw;
        p.rotationPitch = MathHelper.clamp_float(p.rotationPitch + stepPitch, -90f, 90f);

        // Prévient l'interpolation parasite
        p.prevRotationYaw = p.rotationYaw;
        p.prevRotationPitch = p.rotationPitch;
    }

    private static float clamp(float v, float max) {
        if (v > max) return max;
        if (v < -max) return -max;
        return v;
    }
}
