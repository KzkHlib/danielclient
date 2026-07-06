package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

public class FastLadderModule extends Module {

    public static volatile boolean active = false;
    public static volatile double speedVal = 0.4;

    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "Normal", "NCP", "AAC", "Verus", "Instant"));
    private final Setting.Number spd = (Setting.Number) add(new Setting.Number("Vitesse", 0.4, 0.1, 1.0, 0.1, false));

    public FastLadderModule() { super("FastLadder", Category.MOVEMENT); }

    @Override public void onEnable() { active = true; }
    @Override public void onDisable() { active = false; }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;
        speedVal = spd.value;
        if (!mc.thePlayer.isOnLadder()) return;

        String m = mode.current();

        if (m.equals("Normal")) { mc.thePlayer.motionY = speedVal; }
        else if (m.equals("NCP")) { mc.thePlayer.motionY = Math.min(speedVal, 0.56); }
        else if (m.equals("AAC")) { mc.thePlayer.motionY = mc.thePlayer.ticksExisted % 2 == 0 ? speedVal * 0.8 : 0.42; }
        else if (m.equals("Verus")) {
            if (mc.thePlayer.ticksExisted % 3 == 0) mc.thePlayer.motionY = speedVal * 0.5;
        }
        else if (m.equals("Instant")) {
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 2.0, mc.thePlayer.posZ);
        }
    }
}
