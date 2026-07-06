package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

public class SpiderModule extends Module {

    public static volatile boolean active = false;

    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "Normal", "NCP", "Verus", "AAC", "Grim"));
    private final Setting.Number speed = (Setting.Number) add(new Setting.Number("Vitesse", 0.2, 0.1, 0.6, 0.1, false));

    public SpiderModule() { super("Spider", Category.MOVEMENT); }

    @Override public void onEnable() { active = true; }
    @Override public void onDisable() { active = false; }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;
        if (!mc.thePlayer.isCollidedHorizontally) return;

        double spd = speed.value;
        String m = mode.current();

        if (m.equals("Normal")) {
            mc.thePlayer.motionY = spd;
        } else if (m.equals("NCP")) {
            if (mc.thePlayer.ticksExisted % 3 == 0) {
                mc.thePlayer.motionY = spd * 0.9;
            } else {
                mc.thePlayer.motionY = -0.1;
            }
        } else if (m.equals("Verus")) {
            if (mc.thePlayer.ticksExisted % 2 == 0) mc.thePlayer.motionY = spd * 0.6;
            else mc.thePlayer.motionY = 0.02;
        } else if (m.equals("AAC")) {
            mc.thePlayer.motionY = spd * 0.4;
            mc.thePlayer.motionX *= 0.8;
            mc.thePlayer.motionZ *= 0.8;
        } else if (m.equals("Grim")) {
            if (mc.thePlayer.ticksExisted % 4 == 0) mc.thePlayer.motionY = spd * 0.5;
            else mc.thePlayer.motionY = 0.0;
        }
    }
}
