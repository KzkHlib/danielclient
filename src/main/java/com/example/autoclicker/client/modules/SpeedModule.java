package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.MinecraftAccess;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.client.entity.EntityPlayerSP;

public class SpeedModule extends Module {

    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "Bhop", "YPort", "Strafe", "Timer", "SlowHop", "LowHop", "Ground"));
    private final Setting.Mode ac = (Setting.Mode) add(new Setting.Mode("AC", 0, "NCP", "Hypixel", "Verus", "Grim", "Mineplex", "OldNCP", "AAC"));
    private final Setting.Number speed = (Setting.Number) add(new Setting.Number("Vitesse", 0.4, 0.2, 1.5, 0.05, false));
    private final Setting.Bool autoJump = (Setting.Bool) add(new Setting.Bool("Saut auto", true));
    private int bhopTicks = 0;

    public SpeedModule() { super("Speed", Category.MOVEMENT); }

    @Override
    public void onTick() {
        EntityPlayerSP p = mc.thePlayer;
        if (p == null) return;
        boolean moving = p.moveForward != 0 || p.moveStrafing != 0;
        if (!moving) return;

        String m = mode.current();
        String acs = ac.current();

        if (m.equals("Ground") && !p.onGround) return;

        double rad = Math.toRadians(p.rotationYaw);
        double sin = Math.sin(rad), cos = Math.cos(rad);
        double fwd = p.moveForward, str = p.moveStrafing;
        double len = Math.sqrt(fwd * fwd + str * str);
        if (len < 1e-4) return;
        fwd /= len; str /= len;

        double s = speed.value;
        double mx = (-sin * fwd + cos * str) * s;
        double mz = (cos * fwd + sin * str) * s;

        if (m.equals("Bhop")) {
            if (p.onGround && autoJump.value && !mc.gameSettings.keyBindJump.isKeyDown()) {
                p.motionY = acs.equals("Grim") ? 0.38 : acs.equals("Hypixel") ? 0.4 : 0.42;
            }
            if (acs.equals("NCP")) {
                p.motionX = mx * (p.onGround ? 1.0 : 0.95);
                p.motionZ = mz * (p.onGround ? 1.0 : 0.95);
            } else if (acs.equals("Hypixel")) {
                if (p.onGround) { p.motionX = mx * 0.6; p.motionZ = mz * 0.6; }
                else { p.motionX = mx * 0.7; p.motionZ = mz * 0.7; }
            } else if (acs.equals("Verus")) {
                if (p.ticksExisted % 3 == 0) { p.motionX = mx * 0.5; p.motionZ = mz * 0.5; }
            } else {
                p.motionX = mx; p.motionZ = mz;
            }
        } else if (m.equals("YPort")) {
            if (p.onGround && autoJump.value) p.motionY = acs.equals("OldNCP") ? 0.4 : 0.42;
            if (p.onGround) { p.motionX = mx; p.motionZ = mz; }
            else { p.motionX = mx * 0.5; p.motionZ = mz * 0.5; }
        } else if (m.equals("Strafe")) {
            boolean air = !p.onGround;
            if (acs.equals("NCP")) { p.motionX = mx * (air ? 0.7 : 0.8); p.motionZ = mz * (air ? 0.7 : 0.8); }
            else { p.motionX = mx * 0.85; p.motionZ = mz * 0.85; }
        } else if (m.equals("Timer")) {
            MinecraftAccess.setTimerSpeed(1.0F + (float) s * 0.3F);
            if (p.onGround) { p.motionX = mx * 0.4; p.motionZ = mz * 0.4; }
        } else if (m.equals("SlowHop")) {
            if (p.onGround && autoJump.value) p.motionY = 0.35;
            p.motionX = mx * 0.6; p.motionZ = mz * 0.6;
        } else if (m.equals("LowHop")) {
            if (p.onGround && autoJump.value) p.motionY = 0.28;
            p.motionX = mx * 0.7; p.motionZ = mz * 0.7;
        } else if (m.equals("Ground")) {
            p.motionX = mx; p.motionZ = mz;
        }
    }

    @Override public void onDisable() {
        MinecraftAccess.setTimerSpeed(1.0F);
    }
}
