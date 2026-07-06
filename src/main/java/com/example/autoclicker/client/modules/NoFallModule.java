package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.network.play.client.C03PacketPlayer;

public class NoFallModule extends Module {

    public static volatile boolean active = false;

    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "Packet", "Ground", "NCP", "AAC", "Water", "Distance"));
    private final Setting.Number distance = (Setting.Number) add(new Setting.Number("Distance max", 3, 1, 10, 1, true));

    public NoFallModule() { super("NoFall", Category.MOVEMENT); }

    @Override public void onEnable() { active = true; }
    @Override public void onDisable() { active = false; }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;
        if (mc.thePlayer.onGround) return;
        if (mc.thePlayer.fallDistance < 2.5F) return;

        String m = mode.current();

        if (m.equals("Distance") && mc.thePlayer.fallDistance > distance.value) {
            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
            mc.thePlayer.fallDistance = 0;
            return;
        }

        if (m.equals("Water") && !mc.thePlayer.isInWater()) return;

        if (m.equals("Packet")) {
            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
            mc.thePlayer.fallDistance = 0;
        } else if (m.equals("Ground")) {
            mc.thePlayer.onGround = true;
        } else if (m.equals("NCP")) {
            if (mc.thePlayer.ticksExisted % 3 == 0) {
                mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
            }
        } else if (m.equals("AAC")) {
            if (mc.thePlayer.fallDistance > 3.5F) {
                mc.thePlayer.motionY = 0.2;
                mc.thePlayer.fallDistance = 0;
            }
        }
    }
}
