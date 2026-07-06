package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.item.ItemFishingRod;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

public class AutoRodModule extends Module {

    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "Normal", "Insta", "Smart", "Delay"));
    private final Setting.Number delay = (Setting.Number) add(new Setting.Number("Delay", 200, 50, 500, 50, true));
    private long lastRod = 0L;
    private boolean rodding = false;
    private int rodTicks = 0;
    private boolean hitPending = false;

    public AutoRodModule() { super("AutoRod", Category.COMBAT); }

    @Override
    public void onAttackEntity(AttackEntityEvent e) {
        if (mode.is("Smart")) hitPending = true;
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;

        if (rodding) {
            rodTicks--;
            if (rodTicks <= 0) {
                if (mode.is("Insta")) {
                    mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(
                            C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                }
                rodding = false;
            }
            return;
        }

        if (System.currentTimeMillis() - lastRod < (long) delay.value) return;

        boolean shouldRod = false;
        if (mode.is("Normal") || mode.is("Delay")) {
            shouldRod = true;
        } else if (mode.is("Smart")) {
            shouldRod = hitPending;
            hitPending = false;
        } else if (mode.is("Insta")) {
            shouldRod = mc.thePlayer.getHeldItem() != null
                && mc.thePlayer.getHeldItem().getItem() instanceof ItemFishingRod;
        }

        if (shouldRod) doRod();
    }

    private void doRod() {
        if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemFishingRod) {
            mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
            lastRod = System.currentTimeMillis();
            rodding = true;
            rodTicks = mode.is("Insta") ? 1 : 2;
        } else {
            switchToRod();
        }
    }

    private void switchToRod() {
        for (int i = 0; i < 9; i++) {
            if (mc.thePlayer.inventory.mainInventory[i] != null
                    && mc.thePlayer.inventory.mainInventory[i].getItem() instanceof ItemFishingRod) {
                mc.thePlayer.inventory.currentItem = i;
                mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(i));
                doRod();
                break;
            }
        }
    }
}
