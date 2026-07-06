package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraftforge.event.entity.player.AttackEntityEvent;

public class WTapModule extends Module {

    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "W-Tap", "S-Tap", "D-Tap", "Auto"));
    private final Setting.Mode subMode = (Setting.Mode) add(new Setting.Mode("Sous-mode", 0, "Normal", "Legit", "Instant", "AAC", "NCP", "Smart"));

    public WTapModule() { super("WTap", Category.COMBAT); }

    @Override
    public void onAttackEntity(AttackEntityEvent e) {
        if (mc.thePlayer == null) return;

        String m = mode.current();
        String sm = subMode.current();

        if (m.equals("W-Tap")) {
            if (mc.thePlayer.isSprinting()) {
                mc.thePlayer.setSprinting(false);
                if (sm.equals("Instant")) mc.thePlayer.setSprinting(true);
            }
        } else if (m.equals("S-Tap")) {
            mc.thePlayer.setSprinting(false);
            mc.thePlayer.motionX *= sm.equals("NCP") ? 0.4 : 0.6;
            mc.thePlayer.motionZ *= sm.equals("NCP") ? 0.4 : 0.6;
            if (sm.equals("Instant")) mc.thePlayer.setSprinting(true);
        } else if (m.equals("D-Tap")) {
            double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
            if (sm.equals("Smart")) {
                double dist = mc.thePlayer.getDistanceToEntity(e.target);
                mc.thePlayer.motionX = dist > 3 ? -Math.sin(yaw) * 0.3 : Math.sin(yaw) * 0.3;
                mc.thePlayer.motionZ = dist > 3 ? Math.cos(yaw) * 0.3 : -Math.cos(yaw) * 0.3;
            } else {
                mc.thePlayer.motionX = Math.sin(yaw) * 0.3;
                mc.thePlayer.motionZ = -Math.cos(yaw) * 0.3;
            }
            mc.thePlayer.setSprinting(false);
        } else if (m.equals("Auto")) {
            double dist = mc.thePlayer.getDistanceToEntity(e.target);
            if (dist > 4) {
                mc.thePlayer.setSprinting(false);
            } else if (dist > 2.5) {
                mc.thePlayer.motionX *= 0.6;
                mc.thePlayer.motionZ *= 0.6;
                mc.thePlayer.setSprinting(false);
            } else {
                double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
                mc.thePlayer.motionX = Math.sin(yaw) * 0.2;
                mc.thePlayer.motionZ = -Math.cos(yaw) * 0.2;
            }
        }
    }
}
