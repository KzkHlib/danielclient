package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.MinecraftAccess;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

public class TimerModule extends Module {

    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "Normal", "NCP", "Switch", "Blink"));
    private final Setting.Number speed = (Setting.Number) add(new Setting.Number("Vitesse", 1.5, 0.5, 5.0, 0.5, false));
    private long lastToggle = 0;

    public TimerModule() { super("Timer", Category.MOVEMENT); }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;

        String m = mode.current();

        if (m.equals("Normal")) {
            MinecraftAccess.setTimerSpeed((float) speed.value);
        } else if (m.equals("NCP")) {
            MinecraftAccess.setTimerSpeed(Math.min((float) speed.value, 1.2F));
        } else if (m.equals("Switch")) {
            long now = System.currentTimeMillis();
            if (now - lastToggle > 1000) {
                float cur = MinecraftAccess.getTimerSpeed();
                MinecraftAccess.setTimerSpeed(cur == 1.0F ? (float) speed.value : 1.0F);
                lastToggle = now;
            }
        } else if (m.equals("Blink")) {
            MinecraftAccess.setTimerSpeed((float) speed.value);
        }
    }

    @Override public void onDisable() {
        MinecraftAccess.setTimerSpeed(1.0F);
    }
}
