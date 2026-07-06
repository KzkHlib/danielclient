package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class ScaffoldModule extends Module {

    public static volatile boolean active = false;
    public static volatile float silentYaw = 0;
    public static volatile float silentPitch = 90;
    public static volatile boolean useSilent = false;

    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "Normal", "TellyBridge", "GodBridge", "Expand"));
    private final Setting.Mode ac = (Setting.Mode) add(new Setting.Mode("Bypass", 0, "NCP", "AAC", "Grim"));
    private final Setting.Bool silentRot = (Setting.Bool) add(new Setting.Bool("Rotation silencieuse", true));
    private final Setting.Bool tower = (Setting.Bool) add(new Setting.Bool("Tower", false));
    private final Setting.Number eagle = (Setting.Number) add(new Setting.Number("Eagle", 0, 0, 2, 1, true));
    private final Setting.Number speed = (Setting.Number) add(new Setting.Number("Speed", 1.0, 0.1, 2.0, 0.1, false));
    private int slot = -1;

    public ScaffoldModule() { super("Scaffold", Category.MOVEMENT); }

    @Override public void onEnable() { active = true; slot = -1; }
    @Override public void onDisable() { active = false; if (mc.thePlayer != null) mc.thePlayer.setSneaking(false); }

    @Override
    public void onTick() {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        useSilent = silentRot.value;

        slot = findBlockSlot();
        if (slot == -1) return;

        BlockPos pos = findPlacePos();
        if (pos == null) return;

        int oldSlot = mc.thePlayer.inventory.currentItem;
        if (mc.thePlayer.inventory.currentItem != slot) {
            mc.thePlayer.inventory.currentItem = slot;
        }

        calculateRotation(pos);

        if (mc.playerController.onPlayerRightClick(
                mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(),
                pos, EnumFacing.UP, new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5))) {
            mc.thePlayer.swingItem();
        }

        mc.thePlayer.inventory.currentItem = oldSlot;

        String sm = mode.current();
        int eagleMode = (int) eagle.value;

        if (sm.equals("TellyBridge") || sm.equals("GodBridge")) {
            mc.thePlayer.setSneaking(sm.equals("GodBridge"));
        } else if (eagleMode == 1) {
            mc.thePlayer.setSneaking(mc.thePlayer.onGround && mc.thePlayer.moveForward > 0 && isNearEdge());
        } else if (eagleMode == 2) {
            mc.thePlayer.setSneaking(true);
        } else {
            mc.thePlayer.setSneaking(false);
        }

        if (tower.value && mc.gameSettings.keyBindJump.isKeyDown() && mc.thePlayer.onGround) {
            mc.thePlayer.motionY = 0.42;
        }

        double spd = speed.value;
        if (spd != 1.0 && mc.thePlayer.onGround) {
            float yaw = mc.thePlayer.rotationYaw;
            double fwd = mc.thePlayer.moveForward;
            double str = mc.thePlayer.moveStrafing;
            double len = Math.max(0.01, Math.sqrt(fwd * fwd + str * str));
            fwd /= len; str /= len;
            double rad = Math.toRadians(yaw);
            mc.thePlayer.motionX = (-Math.sin(rad) * fwd + Math.cos(rad) * str) * spd * 0.3;
            mc.thePlayer.motionZ = (Math.cos(rad) * fwd + Math.sin(rad) * str) * spd * 0.3;
        }
    }

    private boolean isNearEdge() {
        BlockPos pos = new BlockPos(mc.thePlayer).down();
        return mc.theWorld.getBlockState(pos).getBlock() instanceof BlockAir;
    }

    private int findBlockSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemBlock) {
                Block block = ((ItemBlock) stack.getItem()).getBlock();
                if (block != null && block != Blocks.air) return i;
            }
        }
        return -1;
    }

    private BlockPos findPlacePos() {
        BlockPos playerPos = new BlockPos(mc.thePlayer).down();
        if (isAir(playerPos)) {
            BlockPos[] offsets = {
                playerPos.north(), playerPos.south(), playerPos.east(), playerPos.west(),
                playerPos.add(1, 0, 0), playerPos.add(-1, 0, 0),
                playerPos.add(0, 0, 1), playerPos.add(0, 0, -1)
            };
            for (BlockPos offset : offsets) { if (!isAir(offset)) return offset; }

            if (mode.is("Expand")) {
                BlockPos[] expand = {
                    playerPos.add(1, 0, 1), playerPos.add(-1, 0, 1),
                    playerPos.add(1, 0, -1), playerPos.add(-1, 0, -1)
                };
                for (BlockPos offset : expand) { if (!isAir(offset)) return offset; }
            }
            return null;
        }
        BlockPos below = playerPos.down();
        if (!isAir(below)) return below;
        return playerPos;
    }

    private boolean isAir(BlockPos pos) {
        return mc.theWorld.getBlockState(pos).getBlock() instanceof BlockAir
            || mc.theWorld.getBlockState(pos).getBlock() == Blocks.air;
    }

    private void calculateRotation(BlockPos target) {
        double dx = target.getX() + 0.5 - mc.thePlayer.posX;
        double dy = target.getY() + 0.5 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double dz = target.getZ() + 0.5 - mc.thePlayer.posZ;
        double dist = MathHelper.sqrt_double(dx * dx + dz * dz);
        silentYaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
        silentPitch = (float) (-(Math.atan2(dy, dist) * 180.0 / Math.PI));
        if (!useSilent) { mc.thePlayer.rotationYaw = silentYaw; mc.thePlayer.rotationPitch = silentPitch; }
    }
}
