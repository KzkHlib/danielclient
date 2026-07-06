package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

public class StrafeModule extends Module {

    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "Normal", "NCP", "AAC"));
    private final Setting.Number strength = (Setting.Number) add(new Setting.Number("Force", 0.5, 0.1, 1.0, 0.1, false));
    private final Setting.Bool onlyAir = (Setting.Bool) add(new Setting.Bool("Air seulement", true));

    public StrafeModule() { super("Strafe", Category.MOVEMENT); }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;
        if (mc.thePlayer.onGround && onlyAir.value) return;

        boolean moving = mc.thePlayer.moveForward != 0 || mc.thePlayer.moveStrafing != 0;
        if (!moving) return;

        double fwd = mc.thePlayer.moveForward;
        double str = mc.thePlayer.moveStrafing;
        double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
        double len = Math.max(0.01, Math.sqrt(fwd * fwd + str * str));
        fwd /= len; str /= len;

        double spd = strength.value;
        double mx = (-Math.sin(yaw) * fwd + Math.cos(yaw) * str) * spd;
        double mz = (Math.cos(yaw) * fwd + Math.sin(yaw) * str) * spd;

        String m = mode.current();

        if (m.equals("Normal")) {
            mc.thePlayer.motionX = mx;
            mc.thePlayer.motionZ = mz;
        } else if (m.equals("NCP")) {
            double speed = Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ);
            if (speed > 0.1) {
                mc.thePlayer.motionX = mx * speed;
                mc.thePlayer.motionZ = mz * speed;
            }
        } else if (m.equals("AAC")) {
            mc.thePlayer.motionX = mx * 0.8;
            mc.thePlayer.motionZ = mz * 0.8;
        }
    }
}
