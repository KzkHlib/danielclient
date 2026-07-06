package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.BlockPos;

public class AntiVoidModule extends Module {

    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "Fallback", "Motion", "Packet", "TP", "Grim", "VClip"));
    private final Setting.Number threshold = (Setting.Number) add(new Setting.Number("Seuil Y", 5, 2, 30, 1, true));
    private double lastSafeX, lastSafeY, lastSafeZ;
    private boolean hasSafePos = false;

    public AntiVoidModule() { super("AntiVoid", Category.MOVEMENT); }

    @Override
    public void onTick() {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (mc.thePlayer.onGround) {
            lastSafeX = mc.thePlayer.posX;
            lastSafeY = mc.thePlayer.posY;
            lastSafeZ = mc.thePlayer.posZ;
            hasSafePos = true;
            return;
        }

        if (mc.thePlayer.posY > -threshold.value) return;

        String m = mode.current();

        if (m.equals("Fallback") && hasSafePos) {
            mc.thePlayer.setPosition(lastSafeX, lastSafeY, lastSafeZ);
            mc.thePlayer.motionY = 0;
            mc.thePlayer.fallDistance = 0;
        } else if (m.equals("Motion")) {
            mc.thePlayer.motionY = 0.5;
            mc.thePlayer.fallDistance = 0;
        } else if (m.equals("Packet")) {
            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(
                mc.thePlayer.posX, mc.thePlayer.posY + 5, mc.thePlayer.posZ, true));
        } else if (m.equals("TP") && hasSafePos) {
            mc.thePlayer.setPosition(lastSafeX, lastSafeY + 2, lastSafeZ);
            mc.thePlayer.motionY = 0;
        } else if (m.equals("Grim")) {
            mc.thePlayer.motionY = 0.1;
            mc.thePlayer.onGround = true;
            mc.thePlayer.fallDistance = 0;
        } else if (m.equals("VClip") && hasSafePos) {
            mc.thePlayer.setPosition(mc.thePlayer.posX, lastSafeY, mc.thePlayer.posZ);
            mc.thePlayer.motionY = 0;
        }
    }
}
