package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.lwjgl.input.Mouse;

public class AutoBlockModule extends Module {

    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "Normal", "Interact", "Packet", "Smart"));
    private final Setting.Mode activation = (Setting.Mode) add(new Setting.Mode("Activation", 0, "En cliquant", "Toujours"));
    private boolean blocking = false;

    public AutoBlockModule() { super("AutoBlock", Category.COMBAT); }

    @Override public void onDisable() { stopBlock(); }

    @Override
    public void onTick() {
        if (mc.thePlayer == null || mc.currentScreen != null) { stopBlock(); return; }
        boolean sword = mc.thePlayer.getHeldItem() != null
                && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword;
        if (!sword) { stopBlock(); return; }

        boolean want = activation.is("Toujours") || (activation.is("En cliquant") && Mouse.isButtonDown(0));
        String m = mode.current();

        if (m.equals("Normal")) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), want);
            blocking = want;
        } else if (m.equals("Interact")) {
            if (want && !blocking) {
                mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, mc.thePlayer.getHeldItem(), 0, 0, 0));
                blocking = true;
            } else if (!want && blocking) {
                mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                blocking = false;
            }
        } else if (m.equals("Packet")) {
            if (want && !blocking) {
                mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                blocking = true;
            } else if (!want && blocking) {
                mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                blocking = false;
            }
        } else if (m.equals("Smart")) {
            boolean canHit = mc.objectMouseOver != null && mc.objectMouseOver.entityHit != null;
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), want && canHit);
            blocking = want && canHit;
        }
    }

    private void stopBlock() {
        if (blocking) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
            blocking = false;
        }
    }
}
