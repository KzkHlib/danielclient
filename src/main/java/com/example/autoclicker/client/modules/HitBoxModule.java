package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

public class HitBoxModule extends Module {

    public static volatile boolean active = false;
    public static volatile float expandX = 0.3f;
    public static volatile float expandY = 0.1f;
    public static volatile boolean smartMode = false;
    public static volatile boolean alwaysMode = false;

    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "Normal", "Smart", "Always"));
    private final Setting.Number sizeX = (Setting.Number) add(new Setting.Number("Horizontal", 0.3, 0, 1, 0.05, false));
    private final Setting.Number sizeY = (Setting.Number) add(new Setting.Number("Vertical", 0.1, 0, 0.5, 0.05, false));

    public HitBoxModule() { super("HitBox", Category.COMBAT); }

    @Override public void onEnable() { active = true; push(); }
    @Override public void onDisable() { active = false; }

    @Override public void onTick() { push(); }

    private void push() {
        expandX = (float) sizeX.value;
        expandY = (float) sizeY.value;
        String m = mode.current();
        smartMode = m.equals("Smart");
        alwaysMode = m.equals("Always");
    }
}
