package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;
import com.example.autoclicker.client.render.Targets;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.MathHelper;

/**
 * KillAura amélioré avec GCD correction pour rotations humaines,
 * smoothing exponentiel et timing d'attaque randomisé.
 */
public class KillAuraModule extends Module {

    private final Setting.Number range =
            (Setting.Number) add(new Setting.Number("Portee", 3.6, 3, 6, 0.1, false));
    private final Setting.Number cpsMin =
            (Setting.Number) add(new Setting.Number("CPS min", 8, 1, 20, 1, true));
    private final Setting.Number cpsMax =
            (Setting.Number) add(new Setting.Number("CPS max", 12, 1, 20, 1, true));
    private final Setting.Number fov =
            (Setting.Number) add(new Setting.Number("FOV", 120, 0, 360, 5, true));
    private final Setting.Mode targetMode =
            (Setting.Mode) add(new Setting.Mode("Cibles", 0, "Mobs", "Joueurs", "Tous"));
    private final Setting.Mode priority =
            (Setting.Mode) add(new Setting.Mode("Priorité", 0, "Distance", "Vie", "Angle"));
    private final Setting.Mode rotation =
            (Setting.Mode) add(new Setting.Mode("Rotation", 0, "Caméra", "Silencieuse", "Aucune"));
    private final Setting.Number smooth =
            (Setting.Number) add(new Setting.Number("Smooth", 30, 1, 90, 1, true));
    private final Setting.Bool throughWalls =
            (Setting.Bool) add(new Setting.Bool("A travers murs", false));
    private final Setting.Bool autoBlock =
            (Setting.Bool) add(new Setting.Bool("Auto-block", false));
    private final Setting.Mode cibleMode =
            (Setting.Mode) add(new Setting.Mode("Mode cible", 1, "Single", "Switch"));
    private final Setting.Number switchDelay =
            (Setting.Number) add(new Setting.Number("Délai switch", 0, 0, 500, 50, true));
    private final Setting.Number attackRandom =
            (Setting.Number) add(new Setting.Number("Randomisation", 10, 0, 40, 1, true));
    private final Setting.Bool gcdCorrection =
            (Setting.Bool) add(new Setting.Bool("GCD correction", true));
    private final Setting.Bool hitTimingHumanize =
            (Setting.Bool) add(new Setting.Bool("Humaniser timing", true));

    private EntityLivingBase current;
    private long nextAttack = 0L;
    private long lastSwitch = 0L;
    private float smoothYaw = 0f;
    private float smoothPitch = 0f;

    public KillAuraModule() {
        super("KillAura", Category.COMBAT);
    }

    public EntityLivingBase getTarget() { return isEnabled() ? current : null; }

    @Override
    public String arrayListSuffix() {
        return current != null ? current.getName() : null;
    }

    @Override
    public void onDisable() {
        current = null;
        nextAttack = 0L;
        setBlocking(false);
    }

    /** Calcule le pas GCD natif de la souris. */
    private float getGCD() {
        float sens = mc.gameSettings.mouseSensitivity;
        if (sens < 0.01f) sens = 0.5f;
        return 0.5f / (sens * 0.15f);
    }

    private float applyGCD(float value) {
        if (!gcdCorrection.value) return value;
        float gcd = getGCD();
        if (gcd < 0.001f) return value;
        return Math.round(value / gcd) * gcd;
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null || mc.theWorld == null || mc.currentScreen != null) {
            current = null; return;
        }
        EntityLivingBase next = (cibleMode.is("Single") && current != null && valid(current))
                ? current : findTarget();
        if (next != current) lastSwitch = System.currentTimeMillis();
        current = next;
        if (current == null) { setBlocking(false); return; }

        float[] rot = rotationTo(current);

        if (rotation.is("Caméra")) {
            EntityPlayer p = mc.thePlayer;
            float maxS = (float) smooth.value;
            float dYaw = MathHelper.wrapAngleTo180_float(rot[0] - p.rotationYaw);
            float dPitch = MathHelper.wrapAngleTo180_float(rot[1] - p.rotationPitch);
            // Smooth avec GCD correction
            smoothYaw = applyGCD(clamp(dYaw, maxS));
            smoothPitch = applyGCD(clamp(dPitch, maxS * 0.85f));
            p.rotationYaw += smoothYaw;
            p.rotationPitch = MathHelper.clamp_float(p.rotationPitch + smoothPitch, -90f, 90f);
        }

        long now = System.currentTimeMillis();
        if (switchDelay.value > 0 && now - lastSwitch < switchDelay.value) return;
        if (nextAttack == 0L) { nextAttack = now + interval(); return; }
        if (now < nextAttack) return;

        if (!inFov(current)) return;
        if (!throughWalls.value && !mc.thePlayer.canEntityBeSeen(current)) return;

        if (rotation.is("Silencieuse")) {
            // Envoie le packet look avec GCD correction
            float[] correctedRot = new float[]{
                    mc.thePlayer.rotationYaw + applyGCD(rot[0] - mc.thePlayer.rotationYaw),
                    MathHelper.clamp_float(mc.thePlayer.rotationPitch + applyGCD(rot[1] - mc.thePlayer.rotationPitch), -90f, 90f)
            };
            mc.thePlayer.sendQueue.addToSendQueue(
                    new C03PacketPlayer.C05PacketPlayerLook(correctedRot[0], correctedRot[1], mc.thePlayer.onGround));
        }

        boolean wasBlocking = blocking;
        if (autoBlock.value) setBlocking(false);
        attack(current);
        if (autoBlock.value) setBlocking(true);
        nextAttack = now + interval();
    }

    // ====================== cible ======================

    private EntityLivingBase findTarget() {
        EntityLivingBase best = null;
        double bestScore = Double.MAX_VALUE;
        for (Object o : mc.theWorld.loadedEntityList) {
            if (!(o instanceof EntityLivingBase)) continue;
            EntityLivingBase e = (EntityLivingBase) o;
            if (!valid(e)) continue;
            double score;
            if (priority.is("Vie")) score = e.getHealth();
            else if (priority.is("Angle")) score = angleTo(e);
            else score = mc.thePlayer.getDistanceToEntity(e);
            if (score < bestScore) { bestScore = score; best = e; }
        }
        return best;
    }

    private boolean valid(EntityLivingBase e) {
        if (e == mc.thePlayer || e.isDead || !e.isEntityAlive()) return false;
        if (mc.thePlayer.getDistanceToEntity(e) > range.value) return false;
        if (Targets.isFriend(e)) return false;
        if (AntiBotModule.isBot(e)) return false;
        if (targetMode.is("Mobs")) { if (!(e instanceof EntityLiving)) return false; }
        else if (targetMode.is("Joueurs")) { if (!(e instanceof EntityPlayer)) return false; }
        else if (!(e instanceof EntityLiving || e instanceof EntityPlayer)) return false;
        if (!inFov(e)) return false;
        return true;
    }

    private boolean inFov(Entity e) {
        if (fov.value >= 360) return true;
        double dx = e.posX - mc.thePlayer.posX;
        double dz = e.posZ - mc.thePlayer.posZ;
        float yawTo = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float diff = Math.abs(MathHelper.wrapAngleTo180_float(yawTo - mc.thePlayer.rotationYaw));
        return diff <= fov.value / 2.0;
    }

    private float[] rotationTo(EntityLivingBase t) {
        EntityPlayer p = mc.thePlayer;
        double dx = t.posX - p.posX;
        double dz = t.posZ - p.posZ;
        double dy = (t.posY + t.getEyeHeight() * 0.85) - (p.posY + p.getEyeHeight());
        double distXZ = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, distXZ));
        return new float[]{yaw, MathHelper.clamp_float(pitch, -90f, 90f)};
    }

    private double angleTo(EntityLivingBase e) {
        float[] r = rotationTo(e);
        float dy = MathHelper.wrapAngleTo180_float(r[0] - mc.thePlayer.rotationYaw);
        float dp = MathHelper.wrapAngleTo180_float(r[1] - mc.thePlayer.rotationPitch);
        return Math.sqrt(dy * dy + dp * dp);
    }

    // ====================== attaque / block ======================

    private void attack(Entity t) {
        mc.thePlayer.swingItem();
        mc.playerController.attackEntity(mc.thePlayer, t);
    }

    private boolean blocking = false;

    private void setBlocking(boolean b) {
        if (b == blocking) return;
        if (b && !(mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword)) return;
        blocking = b;
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), b);
    }

    private long interval() {
        double lo = Math.min(cpsMin.value, cpsMax.value);
        double hi = Math.max(cpsMin.value, cpsMax.value);
        double cps;

        if (hitTimingHumanize.value) {
            // Distribution plus réaliste : tendance centrale avec fatigue
            cps = (lo + hi) / 2.0 + (Math.random() - 0.5) * (hi - lo) * 0.7;
            // Fatigue douce sur la durée
            if (nextAttack > 0) {
                long elapsed = System.currentTimeMillis() - (nextAttack - 2000);
                if (elapsed > 5000) cps -= Math.min(2.0, elapsed / 15000.0);
            }
        } else {
            cps = lo + Math.random() * (hi - lo);
        }

        if (cps < 0.5) cps = 0.5;
        double ms = 1000.0 / cps;
        if (attackRandom.value > 0) {
            double g = (Math.random() + Math.random() + Math.random() - 1.5) / 1.5;
            ms *= 1.0 + g * (attackRandom.value / 100.0);
        }
        return Math.max(50L, Math.round(ms)); // min 50ms pour éviter les 0-delay flags
    }

    private static float clamp(float v, float max) {
        if (v > max) return max;
        if (v < -max) return -max;
        return v;
    }
}
