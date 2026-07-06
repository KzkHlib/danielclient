package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.client.settings.KeyBinding;

public class SafeWalkModule extends Module {

    public static volatile boolean active = false;

    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "Normal", "NCP", "Eagle", "AAC", "Sneak"));
    private boolean sneaking = false;

    public SafeWalkModule() { super("SafeWalk", Category.MOVEMENT); }

    @Override public void onEnable() { active = true; }
    @Override public void onDisable() {
        active = false;
        if (sneaking) { KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false); sneaking = false; }
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        String m = mode.current();

        if (m.equals("Sneak")) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
            sneaking = true;
            return;
        }

        if (!mc.thePlayer.onGround) return;
        boolean atEdge = isAtEdge();

        if (m.equals("Eagle")) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), atEdge);
            sneaking = atEdge;
        } else if (m.equals("Normal") && atEdge) {
            mc.thePlayer.motionX *= 0.3;
            mc.thePlayer.motionZ *= 0.3;
        } else if (m.equals("NCP") && atEdge) {
            mc.thePlayer.motionX *= 0.0;
            mc.thePlayer.motionZ *= 0.0;
        } else if (m.equals("AAC") && atEdge) {
            mc.thePlayer.motionX *= 0.1;
            mc.thePlayer.motionZ *= 0.1;
            mc.thePlayer.setSneaking(true);
        } else if (m.equals("Eagle") && !atEdge && sneaking) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
            sneaking = false;
        }
    }

    private boolean isAtEdge() {
        double x = mc.thePlayer.posX;
        double z = mc.thePlayer.posZ;
        double y = mc.thePlayer.posY - 0.5;
        for (double ix = -0.3; ix <= 0.3; ix += 0.3) {
            for (double iz = -0.3; iz <= 0.3; iz += 0.3) {
                if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer,
                        mc.thePlayer.getEntityBoundingBox().offset(ix, -0.5, iz)).isEmpty()) {
                    return false;
                }
            }
        }
        return mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer,
                mc.thePlayer.getEntityBoundingBox().offset(0, -0.5, 0)).isEmpty();
    }
}
