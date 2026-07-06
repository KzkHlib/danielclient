package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;
import com.example.autoclicker.client.Theme;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

/** CustomCrosshair : viseur personnalisé au centre de l'écran. */
public class CustomCrosshairModule extends Module {

    private final Setting.Mode style =
            (Setting.Mode) add(new Setting.Mode("Style", 0, "Croix", "Point", "T", "Cercle"));
    private final Setting.Number size =
            (Setting.Number) add(new Setting.Number("Taille", 5, 1, 12, 1, true));
    private final Setting.Number gap =
            (Setting.Number) add(new Setting.Number("Ecart", 2, 0, 8, 1, true));
    private final Setting.Number thickness =
            (Setting.Number) add(new Setting.Number("Epaisseur", 1, 1, 4, 1, true));
    private final Setting.Bool accentColor =
            (Setting.Bool) add(new Setting.Bool("Couleur accent", true));

    public CustomCrosshairModule() {
        super("CustomCrosshair", Category.RENDER);
        showInArrayList = false;
    }

    @Override
    public void onRenderHud(ScaledResolution sr) {
        if (mc.thePlayer == null || mc.currentScreen != null) return;
        int cx = sr.getScaledWidth() / 2;
        int cy = sr.getScaledHeight() / 2;
        int s = (int) size.value;
        int g = (int) gap.value;
        int th = (int) thickness.value;
        int color = accentColor.value ? Theme.ACCENT : 0xFFFFFFFF;

        if (style.is("Point")) {
            Gui.drawRect(cx - th, cy - th, cx + th, cy + th, color);
            return;
        }
        if (style.is("Cercle")) {
            ring(cx, cy, s, color);
            return;
        }
        // Croix / T : branches horizontales + verticales (sauf le haut pour T)
        Gui.drawRect(cx + g, cy - th / 2 - (th == 1 ? 0 : 0), cx + g + s, cy + (th + 1) / 2, color); // droite
        Gui.drawRect(cx - g - s, cy - th / 2, cx - g, cy + (th + 1) / 2, color);                      // gauche
        Gui.drawRect(cx - th / 2, cy + g, cx + (th + 1) / 2, cy + g + s, color);                      // bas
        if (!style.is("T")) {
            Gui.drawRect(cx - th / 2, cy - g - s, cx + (th + 1) / 2, cy - g, color);                  // haut
        }
    }

    /** Anneau approximé par petits points. */
    private void ring(int cx, int cy, int r, int color) {
        int pts = 28;
        for (int i = 0; i < pts; i++) {
            double a = (i / (double) pts) * Math.PI * 2;
            int x = cx + (int) Math.round(Math.cos(a) * r);
            int y = cy + (int) Math.round(Math.sin(a) * r);
            Gui.drawRect(x, y, x + 1, y + 1, color);
        }
    }
}
