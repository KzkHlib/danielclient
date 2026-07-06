package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.MinecraftAccess;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

public class CriticalsModule extends Module {

    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "Packet", "Motion", "Jump", "Hybrid"));
    private final Setting.Mode ac = (Setting.Mode) add(new Setting.Mode("AC", 0, "Normal", "NCP", "AAC", "Grim"));

    public CriticalsModule() { super("Criticals", Category.COMBAT); }

    @Override
    public void onAttackEntity(AttackEntityEvent e) {
        EntityPlayerSP p = mc.thePlayer;
        if (p == null) return;
        if (!p.onGround) return;
        if (p.isInWater() || p.isInLava() || p.isOnLadder() || p.isRiding()) return;
        if (p.isPotionActive(net.minecraft.potion.Potion.jump)) return;

        String m = mode.current();
        String acs = ac.current();

        if (m.equals("Packet")) {
            if (acs.equals("NCP")) {
                p.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(p.posX, p.posY + 0.0625, p.posZ, false));
                p.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(p.posX, p.posY, p.posZ, false));
                p.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(p.posX, p.posY + 1.1E-5, p.posZ, false));
                p.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(p.posX, p.posY, p.posZ, false));
            } else if (acs.equals("AAC")) {
                p.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(p.posX, p.posY + 0.1, p.posZ, false));
                p.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(p.posX, p.posY + 0.0, p.posZ, false));
            } else if (acs.equals("Grim")) {
                p.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(p.posX, p.posY + 0.03, p.posZ, false));
                p.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(p.posX, p.posY, p.posZ, false));
                p.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(p.posX, p.posY + 0.01, p.posZ, false));
                p.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(p.posX, p.posY, p.posZ, false));
            } else {
                p.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(p.posX, p.posY + 0.0625, p.posZ, false));
                p.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(p.posX, p.posY, p.posZ, false));
                p.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(p.posX, p.posY + 1.1E-5, p.posZ, false));
                p.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(p.posX, p.posY, p.posZ, false));
            }
        } else if (m.equals("Motion")) {
            double y = 0.42;
            if (acs.equals("Hypixel")) y = 0.4;
            else if (acs.equals("Vulcan")) y = 0.38;
            p.motionY = y;
            p.fallDistance = 0.1F;
            p.onGround = false;
        } else if (m.equals("Jump")) {
            p.jump();
            if (acs.equals("MiniJump")) p.motionY *= 0.5;
            if (acs.equals("Timer")) MinecraftAccess.setTimerSpeed(0.3F);
        } else if (m.equals("Hybrid")) {
            p.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(p.posX, p.posY + 0.0625, p.posZ, false));
            p.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(p.posX, p.posY, p.posZ, false));
            p.motionY = 0.15;
            p.fallDistance = 0.1F;
        }
    }

    @Override public void onDisable() {
        MinecraftAccess.setTimerSpeed(1.0F);
    }
}
