package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Theme;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

/** Watermark : nom du client en grand en haut à gauche. */
public class WatermarkModule extends Module {

    private static final String NAME = "Daniel";
    private static final String VERSION = "1.0";
    private static final float SCALE = 2.4f;

    public WatermarkModule() {
        super("Watermark", Category.RENDER);
        showInArrayList = false;
        setEnabled(true);
    }

    @Override public int hudW(ScaledResolution sr) {
        return (int) (mc.fontRendererObj.getStringWidth(NAME) * SCALE) + 18;
    }
    @Override public int hudH(ScaledResolution sr) { return (int) (mc.fontRendererObj.FONT_HEIGHT * SCALE); }
    @Override protected int hudDefX(ScaledResolution sr) { return 6; }
    @Override protected int hudDefY(ScaledResolution sr) { return 6; }

    @Override
    public void onRenderHud(ScaledResolution sr) {
        FontRenderer fr = mc.fontRendererObj;
        int x = getHudX(sr);
        int y = getHudY(sr);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(SCALE, SCALE, 1f);
        // nom (couleur d'accent, suit l'Interface si custom)
        fr.drawStringWithShadow(NAME, 0, 0, Theme.ACCENT);
        GlStateManager.popMatrix();

        // version en petit, en exposant après le nom
        int nameW = (int) (fr.getStringWidth(NAME) * SCALE);
        fr.drawStringWithShadow(VERSION, x + nameW + 3, y, 0xFFB0B0C0);
    }
}
