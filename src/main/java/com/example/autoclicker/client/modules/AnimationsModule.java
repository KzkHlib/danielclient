package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

public class AnimationsModule extends Module {

    public static final int VANILLA = 0, OLD17 = 1, SLIDE = 2, SWIRL = 3, SPIN = 4,
            HELICO = 5, DRUNK = 6, SCREW = 7, RX = 8, FLUX = 9, AVATAR = 10, INTERIA = 11;

    public static volatile boolean active = false;
    public static volatile int style = OLD17;
    public static volatile float spinSpeed = 240f;

    private final Setting.Mode mode =
            (Setting.Mode) add(new Setting.Mode("Style", 1,
                    "Vanilla", "1.7", "Slide", "Swirl", "Spin",
                    "Helico", "Drunk", "Screw", "Rx", "Flux", "Avatar", "Interia"));
    private final Setting.Number spin =
            (Setting.Number) add(new Setting.Number("Speed", 240, 30, 1080, 10, true));

    public AnimationsModule() {
        super("Animations", Category.VISUAL);
    }

    @Override
    public String arrayListSuffix() { return mode.current(); }

    @Override public void onEnable()  { active = true; push(); }
    @Override public void onDisable() { active = false; }
    @Override public void onTick()    { push(); }

    private void push() {
        style = mode.index;
        spinSpeed = (float) spin.value;
    }
}
