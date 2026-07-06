package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.client.entity.EntityPlayerSP;

/** Sprint : sprint automatique (option omni-directionnel : aussi en arrière/côté). */
public class SprintModule extends Module {

    private final Setting.Bool omni =
            (Setting.Bool) add(new Setting.Bool("Omni-directionnel", false));

    public SprintModule() {
        super("Sprint", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        EntityPlayerSP p = mc.thePlayer;
        if (p == null) return;
        if (p.isSneaking() || p.isUsingItem()) return;
        if (p.getFoodStats().getFoodLevel() <= 6) return;
        if (p.isCollidedHorizontally) return;

        boolean moving = omni.value
                ? (p.moveForward != 0 || p.moveStrafing != 0)
                : p.moveForward > 0.1f;
        if (moving) p.setSprinting(true);
    }
}
