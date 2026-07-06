package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

public class AntiHungerModule extends Module {

    public static volatile boolean active = false;
    public static volatile boolean packetMode = false;

    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "Normal", "Packet", "NCP", "Spoof"));
    private final Setting.Bool lessHunger = (Setting.Bool) add(new Setting.Bool("Moins de faim", true));

    public AntiHungerModule() { super("AntiHunger", Category.MOVEMENT); }

    @Override public void onEnable() { active = true; push(); }
    @Override public void onDisable() { active = false; }

    @Override public void onTick() { push(); }

    private void push() {
        String m = mode.current();
        active = true;
        packetMode = m.equals("Packet") || m.equals("NCP");
    }
}
