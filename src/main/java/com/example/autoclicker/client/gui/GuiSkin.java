package com.example.autoclicker.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.DefaultPlayerSkin;

/** Dessine une tête de skin (skin Steve par défaut) façon Alt Manager. */
public final class GuiSkin {

    private GuiSkin() {}

    public static void drawHead(int x, int y, int size) {
        try {
            Minecraft.getMinecraft().getTextureManager().bindTexture(
                    DefaultPlayerSkin.getDefaultSkinLegacy());
            GlStateManager.color(1f, 1f, 1f, 1f);
            // face (couche de base) : u=8 v=8, 8x8 sur une texture 64x64
            Gui.drawScaledCustomSizeModalRect(x, y, 8, 8, 8, 8, size, size, 64, 64);
            // chapeau (overlay) : u=40 v=8
            Gui.drawScaledCustomSizeModalRect(x, y, 40, 8, 8, 8, size, size, 64, 64);
        } catch (Throwable ignored) {
        }
    }
}
