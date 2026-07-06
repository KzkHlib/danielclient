package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

public class ChamsModule extends Module {

    public static volatile boolean active = false;
    public static volatile boolean visible = true;
    public static volatile boolean throughWalls = true;
    public static volatile float r = 0, g = 1, b = 0, a = 0.3F;

    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "Texture", "Flat", "Both"));
    private final Setting.Bool vis = (Setting.Bool) add(new Setting.Bool("Visible", true));
    private final Setting.Bool walls = (Setting.Bool) add(new Setting.Bool("À travers les murs", true));
    private final Setting.Number red = (Setting.Number) add(new Setting.Number("Rouge", 0, 0, 1, 0.05, false));
    private final Setting.Number green = (Setting.Number) add(new Setting.Number("Vert", 1, 0, 1, 0.05, false));
    private final Setting.Number blue = (Setting.Number) add(new Setting.Number("Bleu", 0, 0, 1, 0.05, false));
    private final Setting.Number alpha = (Setting.Number) add(new Setting.Number("Alpha", 0.3, 0, 1, 0.05, false));

    public ChamsModule() { super("Chams", Category.RENDER); }

    @Override public void onEnable() { active = true; push(); }
    @Override public void onDisable() { active = false; }

    @Override
    public void onTick() { push(); }

    private void push() {
        visible = vis.value;
        throughWalls = walls.value;
        r = (float) red.value;
        g = (float) green.value;
        b = (float) blue.value;
        a = (float) alpha.value;
    }
}
