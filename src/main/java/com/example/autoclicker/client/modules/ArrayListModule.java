package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Client;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Theme;
import com.example.autoclicker.client.render.Render2D;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * ArrayList : modules actifs en haut à droite, triés par largeur,
 * nom en blanc + suffixe en gris, barre d'accent dégradée à droite. Pas de
 * titre (le nom du client est géré par le Watermark en haut à gauche).
 */
public class ArrayListModule extends Module {

    private static final int ROW_H = 12;

    public ArrayListModule() {
        super("ArrayList", Category.RENDER);
        showInArrayList = false;
        setEnabled(true);
    }

    /** Partie "suffixe" (mode/cps/touche) affichée en gris après le nom. */
    private String suffix(Module m) {
        String s = "";
        String suf = m.arrayListSuffix();
        if (suf != null && suf.length() > 0) s += " " + suf;
        String kn = m.keyName();
        if (kn != null) s += " [" + kn + "]";
        return s;
    }

    private int lineWidth(Module m) {
        FontRenderer fr = mc.fontRendererObj;
        return fr.getStringWidth(m.name + suffix(m));
    }

    private List<Module> active() {
        List<Module> list = new ArrayList<Module>();
        Client c = Client.INSTANCE;
        if (c == null) return list;
        for (Module m : c.modules) {
            if (m.isEnabled() && m.showInArrayList) list.add(m);
        }
        Collections.sort(list, new Comparator<Module>() {
            @Override public int compare(Module a, Module b) {
                return lineWidth(b) - lineWidth(a);
            }
        });
        return list;
    }

    public int width() {
        int max = 0;
        for (Module m : active()) max = Math.max(max, lineWidth(m) + 8);
        return Math.max(max, 1);
    }
    public int height() { return active().size() * ROW_H; }

    @Override public int hudW(ScaledResolution sr) { return width(); }
    @Override public int hudH(ScaledResolution sr) { return height(); }
    @Override protected int hudDefX(ScaledResolution sr) { return sr.getScaledWidth() - width() - 2; }
    @Override protected int hudDefY(ScaledResolution sr) { return 2; }

    // slide-in animé par module
    private final java.util.Map<Module, Float> slide = new java.util.HashMap<Module, Float>();
    private long lastF = System.currentTimeMillis();

    @Override
    public void onRenderHud(ScaledResolution sr) {
        FontRenderer fr = mc.fontRendererObj;
        int rightEdge = getHudX(sr) + width();
        int blockTop = getHudY(sr);

        long now = System.currentTimeMillis();
        float dt = Math.min(0.1f, (now - lastF) / 1000f);
        lastF = now;

        List<Module> list = active();
        for (int i = 0; i < list.size(); i++) {
            Module m = list.get(i);
            String name = m.name;
            String suf = suffix(m);
            int w = fr.getStringWidth(name + suf);

            // animation d'entrée (glisse depuis la droite, easing)
            Float a = slide.get(m);
            float anim = (a == null) ? 0f : a;
            anim = Math.min(1f, anim + dt * 7f);
            slide.put(m, anim);
            float e = anim * anim * (3 - 2 * anim);
            int off = (int) ((1f - e) * (w + 14));

            int rowRight = rightEdge + off;
            int rowLeft = rowRight - w - 8;
            int top = blockTop + i * ROW_H;

            float ratio = list.size() == 1 ? 0f : (float) i / (list.size() - 1);
            int accent = Theme.lerp(Theme.ACCENT, Theme.ACCENT2, ratio);

            // fond arrondi + barre d'accent
            Render2D.roundedRect(rowLeft, top, rowRight, top + ROW_H - 1, 3f, 0xC80A0A12);
            Render2D.roundedRect(rowRight - 2, top, rowRight, top + ROW_H - 1, 1f, accent);

            fr.drawStringWithShadow(name, rowLeft + 4, top + 2, Theme.TEXT);
            if (!suf.isEmpty())
                fr.drawStringWithShadow(suf, rowLeft + 4 + fr.getStringWidth(name), top + 2, 0xFF9A9AAA);
        }

        // purge des modules désactivés (pour rejouer l'anim au ré-activage)
        slide.keySet().retainAll(list);
    }
}
