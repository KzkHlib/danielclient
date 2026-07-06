package com.example.autoclicker.client;

/** Palette de couleurs du client (ARGB). Style sombre + accent violet/bleu. */
public final class Theme {

    private Theme() {}

    /** Couleurs d'accent modifiables en live par le module Interface. */
    public static int ACCENT      = 0xFF8A5CFF; // violet
    public static int ACCENT2     = 0xFF5CB8FF; // bleu (pour dégradés)

    public static final int ACCENT_DEFAULT  = 0xFF8A5CFF;
    public static final int ACCENT2_DEFAULT = 0xFF5CB8FF;

    public static final int BACKDROP    = 0x66000000; // voile derrière le ClickGUI
    public static final int PANEL_BG    = 0xF014151F; // fond de panneau
    public static final int HEADER_BG   = 0xFF1B1B27;
    public static final int ROW_BG      = 0xF0181820;
    public static final int ROW_HOVER   = 0xFF242431;
    public static final int SETTING_BG  = 0xF0121219;

    public static final int TEXT         = 0xFFFFFFFF;
    public static final int TEXT_DIM     = 0xFFA0A0AE;
    public static final int TEXT_ENABLED = ACCENT;

    public static final int SLIDER_TRACK = 0xFF2C2C3A;

    /** Interpole deux couleurs ARGB (ratio 0..1). */
    public static int lerp(int a, int b, float t) {
        if (t < 0) t = 0;
        if (t > 1) t = 1;
        int aa = (a >> 24) & 0xFF, ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int ba = (b >> 24) & 0xFF, br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int oa = (int) (aa + (ba - aa) * t);
        int or = (int) (ar + (br - ar) * t);
        int og = (int) (ag + (bg - ag) * t);
        int ob = (int) (ab + (bb - ab) * t);
        return (oa << 24) | (or << 16) | (og << 8) | ob;
    }
}
