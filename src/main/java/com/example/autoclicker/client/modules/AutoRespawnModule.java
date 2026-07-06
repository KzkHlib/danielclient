package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.client.gui.GuiGameOver;

public class AutoRespawnModule extends Module {

    private final Setting.Number delay = (Setting.Number) add(new Setting.Number("Delay", 0, 0, 5, 1, true));
    private long deathTime = 0;

    public AutoRespawnModule() {
        super("AutoRespawn", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;
        if (!(mc.currentScreen instanceof GuiGameOver)) { deathTime = 0; return; }

        if (deathTime == 0) {
            deathTime = System.currentTimeMillis();
        }
        if (System.currentTimeMillis() - deathTime >= (long) delay.value * 1000L) {
            mc.thePlayer.respawnPlayer();
            mc.displayGuiScreen(null);
        }
    }
}
