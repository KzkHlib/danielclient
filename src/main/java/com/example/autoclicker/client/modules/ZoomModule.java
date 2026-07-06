package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import org.lwjgl.input.Keyboard;

/**
 * Zoom : réduit le FOV tant que la touche liée est MAINTENUE.
 * Touche par défaut : C (remappable dans le ClickGUI).
 */
public class ZoomModule extends Module {

    private final Setting.Number fov =
            (Setting.Number) add(new Setting.Number("FOV", 30, 10, 60, 1, true));

    private boolean zooming = false;
    private float savedFov = 70.0f;

    public ZoomModule() {
        super("Zoom", Category.VISUAL);
        holdKey = true;          // la touche se MAINTIENT, ne toggle pas
        key = Keyboard.KEY_C;    // défaut
        setEnabled(true);        // fonctionnel par défaut
    }

    @Override
    public void onTick() {
        boolean down = key != 0 && Keyboard.isKeyDown(key) && mc.currentScreen == null;
        if (down && !zooming) {
            savedFov = mc.gameSettings.fovSetting;
            zooming = true;
        } else if (!down && zooming) {
            mc.gameSettings.fovSetting = savedFov;
            zooming = false;
        }
        if (zooming) mc.gameSettings.fovSetting = (float) fov.value;
    }

    @Override
    public void onDisable() {
        if (zooming) {
            mc.gameSettings.fovSetting = savedFov;
            zooming = false;
        }
    }
}
