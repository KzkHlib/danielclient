package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.C03PacketPlayer;

public class FlightModule extends Module {

    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "Vanilla", "NCP", "Jetpack", "Damage", "AirJump", "CubeCraft", "Mineplex", "Hypixel"));
    private final Setting.Number horizontal = (Setting.Number) add(new Setting.Number("Horizontale", 1.0, 0.1, 5.0, 0.1, false));
    private final Setting.Number vertical = (Setting.Number) add(new Setting.Number("Verticale", 1.0, 0.1, 5.0, 0.1, false));
    private final Setting.Bool glide = (Setting.Bool) add(new Setting.Bool("Hover", true));
    private int damageTicks = 0;

    public FlightModule() { super("Flight", Category.MOVEMENT); }

    @Override
    public void onTick() {
        EntityPlayerSP p = mc.thePlayer;
        if (p == null) return;

        double h = horizontal.value;
        double fwd = p.moveForward;
        double str = p.moveStrafing;
        double rad = Math.toRadians(p.rotationYaw);
        double sin = Math.sin(rad), cos = Math.cos(rad);

        String m = mode.current();

        if (m.equals("Vanilla")) {
            move(p, h, sin, cos, fwd, str);
            handleVertical(p);
            p.fallDistance = 0;
            p.onGround = false;
        } else if (m.equals("NCP")) {
            p.motionY = -0.05;
            if (mc.gameSettings.keyBindJump.isKeyDown()) p.motionY = 0.3;
            if (mc.gameSettings.keyBindSneak.isKeyDown()) p.motionY = -0.3;
            move(p, h * 0.3, sin, cos, fwd, str);
            p.fallDistance = 0;
        } else if (m.equals("Jetpack")) {
            move(p, h * 0.5, sin, cos, fwd, str);
            if (mc.gameSettings.keyBindJump.isKeyDown() || !p.onGround) {
                p.motionY = vertical.value * 0.5;
            }
            p.fallDistance = 0;
        } else if (m.equals("Damage")) {
            if (damageTicks <= 0 && p.onGround && fwd == 0 && str == 0) {
                for (int i = 0; i < 3; i++) {
                    p.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(p.posX, p.posY + 0.1, p.posZ, false));
                }
                p.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(p.posX, p.posY, p.posZ, true));
                p.motionY = 0.5;
                damageTicks = 20;
            }
            if (damageTicks > 0) damageTicks--;
            move(p, h * 0.3, sin, cos, fwd, str);
            if (glide.value && fwd == 0 && str == 0) p.motionY = 0;
            p.fallDistance = 0;
        } else if (m.equals("AirJump")) {
            if (!p.onGround) {
                p.onGround = true;
                p.fallDistance = 0;
            }
            move(p, h, sin, cos, fwd, str);
            if (mc.gameSettings.keyBindJump.isKeyDown()) p.motionY = 0.42;
        } else if (m.equals("CubeCraft")) {
            p.motionY = -0.1;
            if (mc.gameSettings.keyBindJump.isKeyDown()) p.motionY = 0.2;
            move(p, h * 0.5, sin, cos, fwd, str);
            p.fallDistance = 0;
        } else if (m.equals("Mineplex")) {
            if (p.ticksExisted % 4 == 0) p.motionY = 0.1;
            move(p, h * 0.4, sin, cos, fwd, str);
            p.fallDistance = 0;
        } else if (m.equals("Hypixel")) {
            if (p.ticksExisted % 5 == 0) {
                p.motionY = 0.04;
                move(p, h * 0.2, sin, cos, fwd, str);
            }
            p.fallDistance = 0;
        }
        p.onGround = false;
    }

    private void move(EntityPlayerSP p, double h, double sin, double cos, double fwd, double str) {
        if (fwd == 0 && str == 0) {
            p.motionX = 0;
            p.motionZ = 0;
        } else {
            p.motionX = (-sin * fwd + cos * str) * h;
            p.motionZ = (cos * fwd + sin * str) * h;
        }
    }

    private void handleVertical(EntityPlayerSP p) {
        boolean up = mc.gameSettings.keyBindJump.isKeyDown();
        boolean down = mc.gameSettings.keyBindSneak.isKeyDown();
        double v = vertical.value;
        if (up && !down) p.motionY = v;
        else if (down && !up) p.motionY = -v;
        else p.motionY = glide.value ? 0 : p.motionY;
    }

    @Override public void onDisable() {
        if (mc.thePlayer != null) mc.thePlayer.fallDistance = 0;
    }
}
