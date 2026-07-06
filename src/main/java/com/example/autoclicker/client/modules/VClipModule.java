package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.network.play.client.C03PacketPlayer;

public class VClipModule extends Module {

    public static volatile boolean active = false;

    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "VClip", "HClip"));
    private final Setting.Mode dir = (Setting.Mode) add(new Setting.Mode("Direction", 0, "Up", "Down", "Forward", "Back", "Left", "Right"));
    private final Setting.Number dist = (Setting.Number) add(new Setting.Number("Distance", 3, 1, 10, 1, true));
    private final Setting.Bool packet = (Setting.Bool) add(new Setting.Bool("Packet", true));

    public VClipModule() { super("Clip", Category.MOVEMENT); }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;

        String m = mode.current();
        String d = dir.current();
        double distance = dist.value;

        if (mc.gameSettings.keyBindJump.isKeyDown() && m.equals("VClip") && d.equals("Up")) {
            if (packet.value) {
                for (int i = 0; i < (int) distance; i++) {
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(
                        mc.thePlayer.posX, mc.thePlayer.posY + 1, mc.thePlayer.posZ, false));
                }
            }
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + distance, mc.thePlayer.posZ);
        }

        if (mc.gameSettings.keyBindSneak.isKeyDown() && m.equals("VClip") && d.equals("Down")) {
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - distance, mc.thePlayer.posZ);
        }
    }
}
