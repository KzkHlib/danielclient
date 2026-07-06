package com.example.autoclicker.client.render;

import net.minecraft.client.renderer.GlStateManager;

import org.lwjgl.opengl.GL11;

/** Primitives 2D propres : rectangles à coins arrondis. */
public final class Render2D {

    private Render2D() {}

    private static void color(int argb) {
        float a = (argb >>> 24 & 0xFF) / 255f;
        float r = (argb >>> 16 & 0xFF) / 255f;
        float g = (argb >>> 8 & 0xFF) / 255f;
        float b = (argb & 0xFF) / 255f;
        GlStateManager.color(r, g, b, a);
    }

    /** Rectangle à coins arrondis plein. */
    public static void roundedRect(float x, float y, float x2, float y2, float radius, int argb) {
        if (radius < 0) radius = 0;
        float w = x2 - x, h = y2 - y;
        if (radius > w / 2f) radius = w / 2f;
        if (radius > h / 2f) radius = h / 2f;

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();
        GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
        color(argb);

        // corps central (croix)
        rect(x + radius, y, x2 - radius, y2);
        rect(x, y + radius, x + radius, y2 - radius);
        rect(x2 - radius, y + radius, x2, y2 - radius);

        // 4 coins en éventail
        corner(x + radius, y + radius, radius, 180);
        corner(x2 - radius, y + radius, radius, 270);
        corner(x2 - radius, y2 - radius, radius, 0);
        corner(x + radius, y2 - radius, radius, 90);

        GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    /** Contour arrondi (épaisseur 1, simple : on dessine un rond plus grand puis plus petit non dispo ici -> trait). */
    public static void roundedOutline(float x, float y, float x2, float y2, float radius, int argb, float thick) {
        roundedRect(x, y, x2, y2, radius, argb);                 // plein
        // (laissé simple : pour un vrai contour, dessiner un fond plein par-dessus côté appelant)
    }

    private static void rect(float x, float y, float x2, float y2) {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y2);
        GL11.glVertex2f(x2, y2);
        GL11.glVertex2f(x2, y);
        GL11.glVertex2f(x, y);
        GL11.glEnd();
    }

    private static void corner(float cx, float cy, float r, float startDeg) {
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(cx, cy);
        for (int i = 0; i <= 90; i += 10) {
            double a = Math.toRadians(startDeg + i);
            GL11.glVertex2f(cx + (float) Math.cos(a) * r, cy + (float) Math.sin(a) * r);
        }
        GL11.glEnd();
    }
}
