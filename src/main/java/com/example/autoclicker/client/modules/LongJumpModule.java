package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

public class LongJumpModule extends Module {

    public static volatile boolean active = false;

    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "NCP", "AAC", "Verus", "Hypixel", "CubeCraft", "OldNCP", "Boost"));
    private final Setting.Number boost = (Setting.Number) add(new Setting.Number("Boost", 2.0, 1.0, 5.0, 0.5, false));
    private final Setting.Number height = (Setting.Number) add(new Setting.Number("Hauteur", 0.42, 0.3, 1.0, 0.05, false));
    private boolean jumped = false;
    private int jumpTicks = 0;

    public LongJumpModule() { super("LongJump", Category.MOVEMENT); }

    @Override public void onEnable() { active = true; jumped = false; jumpTicks = 0; }
    @Override public void onDisable() { active = false; }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;

        if (!jumped && mc.thePlayer.onGround) {
            if (mc.thePlayer.moveForward > 0) {
                mc.thePlayer.jump();
                mc.thePlayer.motionY = height.value;
                jumped = true;
                jumpTicks = 0;
            }
            return;
        }

        if (!jumped) return;
        jumpTicks++;

        if (mc.thePlayer.onGround || jumpTicks > 40) {
            jumped = false;
            return;
        }

        double b = boost.value;
        double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
        double forward = mc.thePlayer.moveForward;
        double strafe = mc.thePlayer.moveStrafing;
        if (forward == 0 && strafe == 0) return;

        double len = Math.max(0.01, Math.sqrt(forward * forward + strafe * strafe));
        double mx = forward / len * b * Math.cos(yaw) - strafe / len * b * Math.sin(yaw);
        double mz = forward / len * b * Math.sin(yaw) + strafe / len * b * Math.cos(yaw);

        String sm = mode.current();

        if (sm.equals("NCP")) {
            mc.thePlayer.motionX = mx * 0.85;
            mc.thePlayer.motionZ = mz * 0.85;
            if (jumpTicks < 5) mc.thePlayer.motionY = height.value;
        } else if (sm.equals("AAC")) {
            mc.thePlayer.motionX = mx * 0.6;
            mc.thePlayer.motionZ = mz * 0.6;
            if (jumpTicks == 1) mc.thePlayer.motionY = 0.6;
        } else if (sm.equals("Verus")) {
            mc.thePlayer.motionX = mx * 0.75;
            mc.thePlayer.motionZ = mz * 0.75;
            if (jumpTicks > 2 && jumpTicks < 6) mc.thePlayer.motionY = 0.1;
        } else if (sm.equals("Hypixel")) {
            if (jumpTicks < 3) { mc.thePlayer.motionX = mx * 0.5; mc.thePlayer.motionZ = mz * 0.5; }
            else { mc.thePlayer.motionX = mx * 0.3; mc.thePlayer.motionZ = mz * 0.3; }
        } else if (sm.equals("CubeCraft")) {
            mc.thePlayer.motionX = mx * 0.5;
            mc.thePlayer.motionZ = mz * 0.5;
            if (jumpTicks == 2) mc.thePlayer.motionY = 0.3;
        } else if (sm.equals("OldNCP")) {
            mc.thePlayer.motionX = mx * 0.9;
            mc.thePlayer.motionZ = mz * 0.9;
            mc.thePlayer.motionY = 0.3;
        } else if (sm.equals("Boost")) {
            double accel = Math.min(1.0, jumpTicks / 10.0);
            mc.thePlayer.motionX = mx * (0.5 + accel * 0.5);
            mc.thePlayer.motionZ = mz * (0.5 + accel * 0.5);
            mc.thePlayer.motionY = height.value * (1.0 - jumpTicks / 20.0);
        }
    }
}
