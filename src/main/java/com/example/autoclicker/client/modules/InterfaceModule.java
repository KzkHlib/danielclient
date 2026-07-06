package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;
import com.example.autoclicker.client.Theme;

import java.awt.Color;

/**
 * Interface : personnalise la couleur d'accent du client (ClickGUI, HUD…) via
 * des sliders Teinte / Saturation / Luminosité. Désactivé = couleurs par défaut.
 */
public class InterfaceModule extends Module {

    private final Setting.Number hue =
            (Setting.Number) add(new Setting.Number("Teinte", 265, 0, 360, 1, true));
    private final Setting.Number sat =
            (Setting.Number) add(new Setting.Number("Saturation", 64, 0, 100, 1, true));
    private final Setting.Number bri =
            (Setting.Number) add(new Setting.Number("Luminosite", 100, 0, 100, 1, true));
    private final Setting.Bool rainbow =
            (Setting.Bool) add(new Setting.Bool("Rainbow", false));

    public InterfaceModule() {
        super("Interface", Category.VISUAL);
    }

    @Override public void onEnable() { apply(); }

    @Override
    public void onDisable() {
        Theme.ACCENT = Theme.ACCENT_DEFAULT;
        Theme.ACCENT2 = Theme.ACCENT2_DEFAULT;
    }

    @Override public void onTick() { apply(); }

    private void apply() {
        float h;
        if (rainbow.value) {
            h = (System.currentTimeMillis() % 4000L) / 4000f;     // cycle 4 s
        } else {
            h = (float) (hue.value / 360.0);
        }
        float s = (float) (sat.value / 100.0);
        float b = (float) (bri.value / 100.0);
        Theme.ACCENT = Color.HSBtoRGB(h, s, b) | 0xFF000000;
        Theme.ACCENT2 = Color.HSBtoRGB((h + 0.08f) % 1f, s * 0.9f, b) | 0xFF000000;
    }
}
