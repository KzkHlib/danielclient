package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.util.BlockPos;

public class JesusModule extends Module {

    public static volatile boolean active = false;

    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "NCP", "Vanilla", "AAC", "Grim", "Smart", "Dolphin"));
    private final Setting.Number height = (Setting.Number) add(new Setting.Number("Hauteur", 0.15, 0.05, 0.5, 0.05, false));

    public JesusModule() { super("Jesus", Category.MOVEMENT); }

    @Override public void onEnable() { active = true; }
    @Override public void onDisable() { active = false; }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;
        if (!mc.thePlayer.isInWater() && !mc.thePlayer.isInLava()) return;

        String m = mode.current();

        if (m.equals("NCP")) {
            mc.thePlayer.motionY = height.value;
            mc.thePlayer.setSprinting(true);
        } else if (m.equals("Vanilla")) {
            if (mc.thePlayer.isSneaking()) {
                mc.thePlayer.motionY = 0.02;
            } else {
                mc.thePlayer.motionY = 0.0;
                mc.thePlayer.onGround = true;
            }
        } else if (m.equals("AAC")) {
            if (mc.thePlayer.ticksExisted % 3 == 0) mc.thePlayer.motionY = 0.08;
        } else if (m.equals("Grim")) {
            mc.thePlayer.motionY = 0.01;
            if (mc.thePlayer.ticksExisted % 5 == 0) mc.thePlayer.motionY = 0.1;
        } else if (m.equals("Smart")) {
            if (!shouldFloat()) mc.thePlayer.motionY = height.value;
        } else if (m.equals("Dolphin")) {
            if (mc.thePlayer.isInWater()) {
                mc.thePlayer.motionY = 0.1;
                if (mc.thePlayer.ticksExisted % 10 == 0) mc.thePlayer.motionY = 0.5;
            }
        }
    }

    private boolean shouldFloat() {
        BlockPos pos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + 0.3, mc.thePlayer.posZ);
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        return !(block instanceof BlockLiquid);
    }
}
