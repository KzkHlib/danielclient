package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.MinecraftAccess;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.block.Block;
import net.minecraft.block.BlockIce;
import net.minecraft.block.BlockPackedIce;
import net.minecraft.util.BlockPos;

public class IceSpeedModule extends Module {

    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "Normal", "NCP", "AAC", "Timer"));
    private final Setting.Number speed = (Setting.Number) add(new Setting.Number("Vitesse", 0.5, 0.1, 1.5, 0.1, false));

    public IceSpeedModule() { super("IceSpeed", Category.MOVEMENT); }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;
        if (!isOnIce()) return;

        String m = mode.current();
        double spd = speed.value;
        float yaw = mc.thePlayer.rotationYaw;

        double mx = -Math.sin(Math.toRadians(yaw)) * spd;
        double mz = Math.cos(Math.toRadians(yaw)) * spd;

        if (m.equals("Normal")) { mc.thePlayer.motionX = mx; mc.thePlayer.motionZ = mz; }
        else if (m.equals("NCP")) { mc.thePlayer.motionX = mx * 0.6; mc.thePlayer.motionZ = mz * 0.6; }
        else if (m.equals("AAC")) { mc.thePlayer.motionX = mx * 0.3; mc.thePlayer.motionZ = mz * 0.3; }
        else if (m.equals("Timer")) {
            mc.thePlayer.motionX = mx; mc.thePlayer.motionZ = mz;
            MinecraftAccess.setTimerSpeed(1.15F);
        }
    }

    @Override public void onDisable() {
        MinecraftAccess.setTimerSpeed(1.0F);
    }

    private boolean isOnIce() {
        BlockPos pos = new BlockPos(mc.thePlayer).down();
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        return block instanceof BlockIce || block instanceof BlockPackedIce;
    }
}
