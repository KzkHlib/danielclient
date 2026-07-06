package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Client;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Theme;
import com.example.autoclicker.client.render.Render2D;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

import java.util.ArrayList;
import java.util.List;

/** Notifications : toasts arrondis animés (slide + barre de progression). */
public class NotificationsModule extends Module {

    public static final long DURATION = 3000L;
    private static final int H = 18;

    public NotificationsModule() {
        super("Notifications", Category.RENDER);
        showInArrayList = false;
        setEnabled(true);
    }

    // non déplaçable : toujours en bas à droite
    @Override public int hudW(ScaledResolution sr) { return 0; }

    @Override
    public void onRenderHud(ScaledResolution sr) {
        Client c = Client.INSTANCE;
        if (c == null) return;
        FontRenderer fr = mc.fontRendererObj;
        long now = System.currentTimeMillis();
        int sw = sr.getScaledWidth();
        int sh = sr.getScaledHeight();

        // purge + collecte des notifs actives
        List<Client.Notif> alive = new ArrayList<Client.Notif>();
        for (Client.Notif n : c.notifications) {
            if (now - n.time < DURATION) alive.add(n);
        }
        c.notifications.clear();
        c.notifications.addAll(alive);

        for (int i = 0; i < alive.size(); i++) {
            Client.Notif n = alive.get(i);
            long age = now - n.time;
            String text = n.generic ? n.text : n.text + (n.on ? "  activé" : "  désactivé");
            int w = fr.getStringWidth(text) + 16;

            // animation entrée/sortie (glisse depuis la droite, easing)
            float in = ease(Math.min(1f, age / 220f));
            float out = age > DURATION - 280 ? ease((DURATION - age) / 280f) : 1f;
            float f = Math.max(0f, Math.min(in, out));
            int slide = (int) ((1f - f) * (w + 10));

            int x = sw - w - 5 + slide;
            int y = sh - 24 - i * (H + 4);

            int accent = n.generic ? Theme.ACCENT : (n.on ? 0xFF2ECC71 : 0xFFE74C3C);

            // fond arrondi
            Render2D.roundedRect(x, y, x + w, y + H, 4f, 0xE0121219);
            // pastille d'accent à gauche
            Render2D.roundedRect(x + 3, y + 4, x + 6, y + H - 4, 1.5f, accent);
            // texte
            fr.drawStringWithShadow(text, x + 11, y + 5, Theme.TEXT);
            // barre de progression (temps restant)
            float prog = 1f - (float) age / DURATION;
            Gui.drawRect(x + 4, y + H - 2, x + 4 + (int) ((w - 8) * prog), y + H - 1, accent);
        }
    }

    private static float ease(float x) {
        if (x < 0) x = 0;
        if (x > 1) x = 1;
        return x * x * (3 - 2 * x);
    }
}
