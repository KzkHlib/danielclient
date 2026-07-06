package com.example.autoclicker.client.combat;

import com.example.autoclicker.client.modules.AntiBotModule;
import com.example.autoclicker.client.render.Targets;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;

/** Selection de cible partagee par les modules combat premium. */
public final class TargetSelector {
    private TargetSelector() {}

    public enum Filter { MOBS, JOUEURS, TOUS }
    public enum Priority { DISTANCE, VIE, ANGLE, ARMURE, MENACE }

    public static EntityLivingBase best(Filter filter, Priority priority, double range, double fov,
                                        boolean throughWalls, EntityLivingBase lock) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return null;
        if (valid(lock, filter, range, fov, throughWalls)) return lock;
        EntityLivingBase best = null;
        double bestScore = Double.MAX_VALUE;
        for (Object o : mc.theWorld.loadedEntityList) {
            if (!(o instanceof EntityLivingBase)) continue;
            EntityLivingBase e = (EntityLivingBase) o;
            if (!valid(e, filter, range, fov, throughWalls)) continue;
            double score = score(e, priority);
            if (score < bestScore) {
                bestScore = score;
                best = e;
            }
        }
        return best;
    }

    public static boolean valid(EntityLivingBase e, Filter filter, double range, double fov, boolean throughWalls) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return false;
        if (e == null || e == mc.thePlayer || e.isDead || !e.isEntityAlive()) return false;
        if (mc.thePlayer.getDistanceToEntity(e) > range) return false;
        if (Targets.isFriend(e) || AntiBotModule.isBot(e)) return false;
        if (!matches(e, filter)) return false;
        if (!inFov(e, fov)) return false;
        return throughWalls || mc.thePlayer.canEntityBeSeen(e);
    }

    public static Filter filter(String raw) {
        if ("Joueurs".equalsIgnoreCase(raw)) return Filter.JOUEURS;
        if ("Tous".equalsIgnoreCase(raw)) return Filter.TOUS;
        return Filter.MOBS;
    }

    public static Priority priority(String raw) {
        if ("Vie".equalsIgnoreCase(raw)) return Priority.VIE;
        if ("Angle".equalsIgnoreCase(raw)) return Priority.ANGLE;
        if ("Armure".equalsIgnoreCase(raw)) return Priority.ARMURE;
        if ("Menace".equalsIgnoreCase(raw)) return Priority.MENACE;
        return Priority.DISTANCE;
    }

    public static double angleTo(EntityLivingBase e) {
        Minecraft mc = Minecraft.getMinecraft();
        double dx = e.posX - mc.thePlayer.posX;
        double dz = e.posZ - mc.thePlayer.posZ;
        double dy = (e.posY + e.getEyeHeight() * 0.85) - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double distXZ = Math.sqrt(dx * dx + dz * dz);
        float wantYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float wantPitch = (float) -Math.toDegrees(Math.atan2(dy, distXZ));
        float dyaw = MathHelper.wrapAngleTo180_float(wantYaw - mc.thePlayer.rotationYaw);
        float dpitch = MathHelper.wrapAngleTo180_float(wantPitch - mc.thePlayer.rotationPitch);
        return Math.sqrt(dyaw * dyaw + dpitch * dpitch);
    }

    private static boolean matches(EntityLivingBase e, Filter filter) {
        if (filter == Filter.MOBS) return e instanceof EntityLiving;
        if (filter == Filter.JOUEURS) return e instanceof EntityPlayer;
        return e instanceof EntityLiving || e instanceof EntityPlayer;
    }

    private static boolean inFov(EntityLivingBase e, double fov) {
        if (fov >= 360) return true;
        Minecraft mc = Minecraft.getMinecraft();
        double dx = e.posX - mc.thePlayer.posX;
        double dz = e.posZ - mc.thePlayer.posZ;
        float yawTo = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float diff = Math.abs(MathHelper.wrapAngleTo180_float(yawTo - mc.thePlayer.rotationYaw));
        return diff <= fov / 2.0;
    }

    private static double score(EntityLivingBase e, Priority priority) {
        Minecraft mc = Minecraft.getMinecraft();
        if (priority == Priority.VIE) return e.getHealth();
        if (priority == Priority.ANGLE) return angleTo(e);
        if (priority == Priority.ARMURE) return armorScore(e);
        if (priority == Priority.MENACE) return threatScore(e);
        return mc.thePlayer.getDistanceToEntity(e);
    }

    private static double armorScore(EntityLivingBase e) {
        double score = 0;
        for (int i = 1; i <= 4; i++) if (e.getEquipmentInSlot(i) != null) score += 1.0;
        return score;
    }

    private static double threatScore(EntityLivingBase e) {
        Minecraft mc = Minecraft.getMinecraft();
        double dist = mc.thePlayer.getDistanceToEntity(e);
        double hp = Math.max(1.0, e.getHealth());
        double armor = armorScore(e);
        return dist * 1.5 + hp * 0.15 - armor * 0.8 + angleTo(e) * 0.02;
    }
}
