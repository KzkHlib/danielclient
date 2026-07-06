package com.example.autoclicker.client.gui;

import com.example.autoclicker.client.Theme;

import net.minecraft.client.gui.Gui;

import java.util.Random;

/**
 * Fond animé partagé par les écrans du client : dégradé sombre + particules
 * lumineuses qui flottent vers le haut (effet doux, ambiance "ghost client").
 * Tout est dessiné avec drawRect (aucune texture requise).
 */
public final class GuiFx {

    private GuiFx() {}

    private static final int COUNT = 45;
    private static final Random R = new Random();

    private static float[] px, py, spd, size, phase;
    private static int lastW = -1, lastH = -1;
    private static long lastNanos = 0L;

    private static void init(int w, int h) {
        px = new float[COUNT];
        py = new float[COUNT];
        spd = new float[COUNT];
        size = new float[COUNT];
        phase = new float[COUNT];
        for (int i = 0; i < COUNT; i++) {
            px[i] = R.nextFloat() * w;
            py[i] = R.nextFloat() * h;
            spd[i] = 6f + R.nextFloat() * 16f;     // vitesse de montée (px/s)
            size[i] = 1f + R.nextFloat() * 2.4f;
            phase[i] = R.nextFloat() * 6.28f;
        }
        lastW = w;
        lastH = h;
    }

    public static void render(int w, int h) {
        if (px == null || lastW != w || lastH != h) init(w, h);

        long now = System.nanoTime();
        float dt = lastNanos == 0 ? 0.016f : (now - lastNanos) / 1.0e9f;
        lastNanos = now;
        if (dt > 0.1f) dt = 0.1f;
        float time = now / 1.0e9f;

        // --- dégradé de fond (bandes horizontales) ---
        int bands = 48;
        for (int b = 0; b < bands; b++) {
            float t = bands == 1 ? 0f : (float) b / (bands - 1);
            int c = Theme.lerp(0xF0101020, 0xF0060608, t); // haut bleuté -> bas quasi noir
            int y0 = h * b / bands;
            int y1 = h * (b + 1) / bands;
            Gui.drawRect(0, y0, w, y1, c);
        }

        // --- aurora : blobs lumineux qui dérivent (effet ghost client premium) ---
        int aRgb = Theme.ACCENT & 0xFFFFFF;
        int bRgb = Theme.ACCENT2 & 0xFFFFFF;
        int pRgb = 0xD4537E; // touche rose
        float rad = Math.min(w, h) * 0.55f;
        softBlob(w * 0.50f + (float) Math.sin(time * 0.13f) * w * 0.22f,
                 h * 0.42f + (float) Math.cos(time * 0.11f) * h * 0.18f, rad, aRgb, 1.0f);
        softBlob(w * 0.35f + (float) Math.sin(time * 0.17f + 2f) * w * 0.26f,
                 h * 0.60f + (float) Math.cos(time * 0.15f + 1f) * h * 0.20f, rad * 0.85f, bRgb, 0.9f);
        softBlob(w * 0.65f + (float) Math.sin(time * 0.09f + 4f) * w * 0.20f,
                 h * 0.55f + (float) Math.cos(time * 0.12f + 3f) * h * 0.22f, rad * 0.7f, pRgb, 0.7f);

        // --- particules ---
        int rgb = Theme.ACCENT & 0xFFFFFF;
        for (int i = 0; i < COUNT; i++) {
            py[i] -= spd[i] * dt;
            px[i] += (float) Math.sin(time * 0.6f + phase[i]) * 0.4f;
            if (py[i] < -4) { py[i] = h + 4; px[i] = R.nextFloat() * w; }
            if (px[i] < -4) px[i] = w + 4;
            if (px[i] > w + 4) px[i] = -4;

            int x = (int) px[i];
            int y = (int) py[i];
            int s = (int) size[i];

            float a = 0.35f + 0.30f * (float) Math.sin(time * 1.4f + phase[i]);
            int alpha = (int) (a * 200f);
            if (alpha < 0) alpha = 0;
            if (alpha > 255) alpha = 255;

            int halo = ((alpha / 3) << 24) | rgb;
            int glow = (alpha << 24) | rgb;
            int core = (Math.min(255, alpha + 60) << 24) | 0xFFFFFF;

            Gui.drawRect(x - s - 1, y - s - 1, x + s + 1, y + s + 1, halo);
            Gui.drawRect(x - s, y - s, x + s, y + s, glow);
            Gui.drawRect(x, y, x + 1, y + 1, core);
        }

        // --- léger vignettage (assombrit les bords) ---
        int v = 0x44000000;
        Gui.drawRect(0, 0, w, 22, v);
        Gui.drawRect(0, h - 22, w, h, v);
    }

    /** Halo doux (carrés concentriques translucides) — fake glow radial. */
    private static void softBlob(float cx, float cy, float radius, int rgb, float intensity) {
        int layers = 10;
        for (int i = layers; i >= 1; i--) {
            float t = i / (float) layers;
            float r = radius * t;
            int a = (int) (intensity * 26f * (1f - t));   // centre lumineux, bords fondus
            if (a <= 0) continue;
            int col = (a << 24) | rgb;
            Gui.drawRect((int) (cx - r), (int) (cy - r), (int) (cx + r), (int) (cy + r), col);
        }
    }
}
