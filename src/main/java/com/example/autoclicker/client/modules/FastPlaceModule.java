package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.MinecraftAccess;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

public class FastPlaceModule extends Module {

    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "Normal", "NCP", "AAC", "Switch"));
    private final Setting.Number delay = (Setting.Number) add(new Setting.Number("Delai ticks", 1, 0, 5, 1, true));
    private int tick = 0;

    public FastPlaceModule() { super("FastPlace", Category.MOVEMENT); }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;
        String m = mode.current();
        if (m.equals("Normal")) {
            MinecraftAccess.setRightClickDelay(Math.min(MinecraftAccess.getRightClickDelay(), Math.max(0, (int) delay.value)));
        } else if (m.equals("NCP")) {
            if (tick % 2 == 0) MinecraftAccess.setRightClickDelay(0);
        } else if (m.equals("AAC")) {
            if (tick % 3 == 0) MinecraftAccess.setRightClickDelay(0);
        } else if (m.equals("Switch")) {
            MinecraftAccess.setRightClickDelay(tick % 2 == 0 ? 0 : Math.max(1, (int) delay.value));
        }
        tick++;
    }
}
