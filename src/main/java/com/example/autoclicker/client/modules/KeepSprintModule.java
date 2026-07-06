package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;

import net.minecraft.client.entity.EntityPlayerSP;

/**
 * KeepSprint : réapplique le sprint chaque tick (vanilla le coupe après un coup
 * ou en touchant un mur) pour garder un knockback constant.
 */
public class KeepSprintModule extends Module {

    public KeepSprintModule() {
        super("KeepSprint", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        EntityPlayerSP p = mc.thePlayer;
        if (p == null) return;
        if (p.moveForward <= 0.8f) return;          // avance franchement
        if (p.isSneaking() || p.isCollidedHorizontally) return;
        if (p.isUsingItem()) return;
        if (p.getFoodStats().getFoodLevel() <= 6) return;
        p.setSprinting(true);
    }
}
