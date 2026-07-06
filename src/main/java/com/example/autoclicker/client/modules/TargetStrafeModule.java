package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Client;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;

/**
 * TargetStrafe : tourne automatiquement autour de la cible de l'AimAssist (ou
 * de l'entité la plus proche) à un rayon réglable. Combiné à l'AimAssist smooth
 * = combats cinématiques.
 */
public class TargetStrafeModule extends Module {

    private final Setting.Number radius =
            (Setting.Number) add(new Setting.Number("Rayon", 2.5, 1, 5, 0.5, false));
    private final Setting.Number speed =
            (Setting.Number) add(new Setting.Number("Vitesse", 0.28, 0.1, 0.5, 0.01, false));
    private final Setting.Bool onlyGround =
            (Setting.Bool) add(new Setting.Bool("Sol uniquement", true));

    private int dir = 1; // sens de rotation (inversé par A/D)

    public TargetStrafeModule() {
        super("TargetStrafe", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        EntityPlayerSP p = mc.thePlayer;
        if (p == null || mc.theWorld == null) return;
        if (onlyGround.value && !p.onGround) return;

        EntityLivingBase t = resolveTarget();
        if (t == null) return;

        // inverse le sens selon le strafe joueur (A / D)
        if (p.moveStrafing < 0) dir = -1;
        else if (p.moveStrafing > 0) dir = 1;

        double dx = t.posX - p.posX;
        double dz = t.posZ - p.posZ;
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < 0.1) return;

        double angTo = Math.atan2(dz, dx);
        double tang = angTo + dir * Math.PI / 2.0;     // direction tangentielle
        double radial = (dist - radius.value);          // correction de rayon

        double s = speed.value;
        p.motionX = Math.cos(tang) * s + Math.cos(angTo) * radial * 0.25;
        p.motionZ = Math.sin(tang) * s + Math.sin(angTo) * radial * 0.25;
    }

    /** Cible de l'AimAssist si dispo, sinon l'entité vivante la plus proche. */
    private EntityLivingBase resolveTarget() {
        Module m = Client.INSTANCE == null ? null : Client.INSTANCE.getModule(AimAssistModule.class);
        if (m instanceof AimAssistModule) {
            EntityLivingBase t = ((AimAssistModule) m).getTarget();
            if (t != null) return t;
        }
        EntityLivingBase best = null;
        double bestDist = radius.value * 2.0 + 2.0;
        for (Object o : mc.theWorld.loadedEntityList) {
            if (!(o instanceof EntityLivingBase)) continue;
            EntityLivingBase e = (EntityLivingBase) o;
            if (e == mc.thePlayer || !e.isEntityAlive()) continue;
            double d = mc.thePlayer.getDistanceToEntity(e);
            if (d < bestDist) { bestDist = d; best = e; }
        }
        return best;
    }
}
