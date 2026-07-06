package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Client;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Theme;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

/** Compteur de CPS affiché en jeu (déplaçable via le ClickGUI). */
public class CpsModule extends Module {

    public CpsModule() {
        super("CPS", Category.RENDER);
        showInArrayList = false; // pas besoin de le lister, il est déjà visible
        setEnabled(true);        // visible par défaut
    }

    private int textWidth() {
        return mc.fontRendererObj.getStringWidth(text());
    }

    @Override public int hudW(ScaledResolution sr) { return textWidth() + 6; }
    @Override public int hudH(ScaledResolution sr) { return 12; }
    @Override protected int hudDefX(ScaledResolution sr) { return sr.getScaledWidth() / 2 - textWidth() / 2; }
    @Override protected int hudDefY(ScaledResolution sr) { return sr.getScaledHeight() / 2 + 16; }

    private String text() {
        Client c = Client.INSTANCE;
        int l = c == null ? 0 : c.leftCps();
        int r = c == null ? 0 : c.rightCps();
        return "[" + l + " | " + r + "] CPS";
    }

    @Override
    public void onRenderHud(ScaledResolution sr) {
        FontRenderer fr = mc.fontRendererObj;
        String t = text();
        int x = getHudX(sr);
        int y = getHudY(sr);
        Gui.drawRect(x - 3, y - 2, x + fr.getStringWidth(t) + 3, y + 10, 0x80000000);
        Gui.drawRect(x - 3, y - 2, x - 2, y + 10, Theme.ACCENT);
        fr.drawStringWithShadow(t, x, y, Theme.TEXT);
    }
}
