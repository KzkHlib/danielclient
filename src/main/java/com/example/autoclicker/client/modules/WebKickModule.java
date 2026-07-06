package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.block.BlockWeb;
import net.minecraft.util.BlockPos;

public class WebKickModule extends Module {

    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "Normal", "NCP", "AAC", "Fast", "Jump"));
    private final Setting.Number speed = (Setting.Number) add(new Setting.Number("Vitesse", 0.5, 0.1, 2.0, 0.1, false));

    public WebKickModule() { super("WebKick", Category.MOVEMENT); }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;
        if (!isInWeb()) return;

        String m = mode.current();
        double spd = speed.value;

        if (m.equals("Normal")) {
            mc.thePlayer.motionY = spd;
            mc.thePlayer.stepHeight = 1.0F;
        } else if (m.equals("NCP")) {
            if (mc.thePlayer.ticksExisted % 3 == 0) {
                mc.thePlayer.motionY = spd * 0.7;
            }
        } else if (m.equals("AAC")) {
            mc.thePlayer.motionY = 0.2;
            mc.thePlayer.motionX *= 5.0;
            mc.thePlayer.motionZ *= 5.0;
        } else if (m.equals("Fast")) {
            for (int i = 0; i < 4; i++) {
                mc.thePlayer.motionY = spd;
            }
        } else if (m.equals("Jump")) {
            mc.thePlayer.motionY = 0.6;
            double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
            mc.thePlayer.motionX = -Math.sin(yaw) * spd;
            mc.thePlayer.motionZ = Math.cos(yaw) * spd;
        }
    }

    private boolean isInWeb() {
        BlockPos pos = new BlockPos(mc.thePlayer);
        return mc.theWorld.getBlockState(pos).getBlock() instanceof BlockWeb
            || mc.theWorld.getBlockState(pos.add(0, 1, 0)).getBlock() instanceof BlockWeb;
    }
}
