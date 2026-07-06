package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;

/** Fullbright : pousse la luminosité au maximum (gamma). */
public class FullbrightModule extends Module {

    private float savedGamma = 1.0f;
    private boolean saved = false;

    public FullbrightModule() {
        super("Fullbright", Category.VISUAL);
    }

    @Override
    public void onEnable() {
        if (mc.gameSettings == null) return;
        savedGamma = mc.gameSettings.gammaSetting;
        saved = true;
    }

    @Override
    public void onTick() {
        if (mc.gameSettings != null) {
            mc.gameSettings.gammaSetting = 100.0f; // gamma "fullbright"
        }
    }

    @Override
    public void onDisable() {
        if (mc.gameSettings != null && saved) {
            mc.gameSettings.gammaSetting = savedGamma;
        }
    }
}
