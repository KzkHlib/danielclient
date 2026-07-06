package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

public class PhaseModule extends Module {

    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "NCP", "AAC", "Verus"));
    private final Setting.Number speed = (Setting.Number) add(new Setting.Number("Vitesse", 0.3, 0.1, 1.0, 0.1, false));

    public PhaseModule() { super("Phase", Category.MOVEMENT); }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;
        if (!mc.thePlayer.isCollidedHorizontally) return;

        double spd = speed.value;
        String m = mode.current();
        float yaw = mc.thePlayer.rotationYaw;

        if (m.equals("NCP")) {
            double mx = -Math.sin(Math.toRadians(yaw)) * spd;
            double mz = Math.cos(Math.toRadians(yaw)) * spd;
            mc.thePlayer.motionX = mx;
            mc.thePlayer.motionZ = mz;
            mc.thePlayer.noClip = true;
        } else if (m.equals("AAC")) {
            mc.thePlayer.motionX *= 1.05;
            mc.thePlayer.motionZ *= 1.05;
            mc.thePlayer.noClip = true;
        } else if (m.equals("Verus")) {
            if (mc.thePlayer.ticksExisted % 3 == 0) {
                mc.thePlayer.setPosition(mc.thePlayer.posX + mc.thePlayer.motionX * 2,
                    mc.thePlayer.posY, mc.thePlayer.posZ + mc.thePlayer.motionZ * 2);
            }
        }
    }

    @Override public void onDisable() {
        if (mc.thePlayer != null) mc.thePlayer.noClip = false;
    }
}
