package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

public class StepModule extends Module {

    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "Normal", "NCP", "AAC", "Spike", "Reverse"));
    private final Setting.Number height = (Setting.Number) add(new Setting.Number("Hauteur", 1.0, 0.5, 2.5, 0.5, false));

    public StepModule() { super("Step", Category.MOVEMENT); }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;

        String m = mode.current();

        if (m.equals("Reverse") && !mc.thePlayer.onGround) {
            mc.thePlayer.motionY = -height.value * 0.5;
            return;
        }

        if (!mc.thePlayer.onGround) return;
        if (!mc.thePlayer.isCollidedHorizontally) return;

        if (m.equals("Normal")) {
            mc.thePlayer.stepHeight = (float) height.value;
        } else if (m.equals("NCP")) {
            mc.thePlayer.stepHeight = 1.0F;
            if (height.value > 1.0) mc.thePlayer.motionY = 0.42;
        } else if (m.equals("AAC")) {
            mc.thePlayer.stepHeight = 1.5F;
        } else if (m.equals("Spike")) {
            mc.thePlayer.motionY = 0.6;
            mc.thePlayer.stepHeight = 0.5F;
        }
    }

    @Override public void onDisable() {
        if (mc.thePlayer != null) mc.thePlayer.stepHeight = 0.5F;
    }
}
