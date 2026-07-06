package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;

import net.minecraft.client.settings.KeyBinding;

/** ToggleSneak : maintient l'accroupissement sans garder la touche enfoncée. */
public class ToggleSneakModule extends Module {

    public ToggleSneakModule() {
        super("ToggleSneak", Category.MOVEMENT);
    }

    private int sneakKey() {
        return mc.gameSettings.keyBindSneak.getKeyCode();
    }

    @Override
    public void onTick() {
        // réaffirme l'état "enfoncé" du bind sneak à chaque tick
        KeyBinding.setKeyBindState(sneakKey(), true);
    }

    @Override
    public void onDisable() {
        KeyBinding.setKeyBindState(sneakKey(), false);
    }
}
