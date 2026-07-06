package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;

/** ToggleSprint : sprint automatique tant qu'on avance. */
public class ToggleSprintModule extends Module {

    public ToggleSprintModule() {
        super("ToggleSprint", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;
        // on ne sprinte que si on avance réellement
        if (mc.thePlayer.moveForward > 0.0f && !mc.thePlayer.isSneaking()) {
            mc.thePlayer.setSprinting(true);
        }
    }
}
