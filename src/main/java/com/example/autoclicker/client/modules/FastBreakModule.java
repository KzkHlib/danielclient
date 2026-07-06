package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

/**
 * FastBreak : supprime le délai vanilla entre deux interactions de minage,
 * ce qui enchaîne les blocs plus vite.
 */
public class FastBreakModule extends Module {

    private static Field delayField;

    public FastBreakModule() {
        super("FastBreak", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.playerController == null) return;
        try {
            if (delayField == null) {
                delayField = ReflectionHelper.findField(
                        PlayerControllerMP.class, "blockHitDelay", "field_78781_i");
            }
            delayField.setInt(mc.playerController, 0);
        } catch (Exception ignored) {
        }
    }
}
