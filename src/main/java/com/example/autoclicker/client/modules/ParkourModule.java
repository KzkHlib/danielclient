package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

public class ParkourModule extends Module {

    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "Normal", "Smart", "NCP", "AAC", "Sprint"));
    private final Setting.Number delay = (Setting.Number) add(new Setting.Number("Delai ticks", 0, 0, 10, 1, true));

    public ParkourModule() { super("Parkour", Category.MOVEMENT); }

    @Override
    public void onTick() {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!mc.thePlayer.onGround) return;
        if (mc.thePlayer.isOnLadder() || mc.thePlayer.isInWater() || mc.thePlayer.isInLava()) return;
        if (mc.thePlayer.isSneaking()) return;

        if (!isAtEdge()) return;

        String m = mode.current();
        if (m.equals("Sprint") && !mc.thePlayer.isSprinting()) return;

        if (delay.value > 0 && mc.thePlayer.ticksExisted % (delay.value + 1) != 0) return;

        boolean shouldJump = true;
        if (m.equals("Smart")) shouldJump = Math.random() > 0.3;
        if (m.equals("NCP")) shouldJump = mc.thePlayer.ticksExisted % 3 == 0;
        if (m.equals("AAC")) shouldJump = mc.thePlayer.ticksExisted % 5 == 0;

        if (shouldJump) mc.thePlayer.jump();
    }

    private boolean isAtEdge() {
        double x = mc.thePlayer.posX;
        double z = mc.thePlayer.posZ;
        return mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer,
                mc.thePlayer.getEntityBoundingBox().offset(0, -0.5, 0)).isEmpty();
    }
}
