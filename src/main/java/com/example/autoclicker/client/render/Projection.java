package com.example.autoclicker.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Capture les matrices GL pendant le rendu du monde et projette une position
 * monde (absolue) vers des coordonnées écran (ScaledResolution). Sert aux
 * Nametags et au point d'ancrage des Tracers.
 */
public final class Projection {

    private Projection() {}

    private static final FloatBuffer MODELVIEW  = BufferUtils.createFloatBuffer(16);
    private static final FloatBuffer PROJECTION = BufferUtils.createFloatBuffer(16);
    private static final IntBuffer   VIEWPORT   = BufferUtils.createIntBuffer(16);
    private static final FloatBuffer OUT        = BufferUtils.createFloatBuffer(3);

    private static float partialTicks = 1f;

    /** PartialTicks du frame courant (pour interpoler les positions d'entités). */
    public static float partialTicks() { return partialTicks; }

    public static void capture(float pt) {
        partialTicks = pt;
        capture();
    }

    /** À appeler au début de RenderWorldLastEvent (état GL = caméra appliquée). */
    public static void capture() {
        MODELVIEW.clear();
        PROJECTION.clear();
        VIEWPORT.clear();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, MODELVIEW);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, PROJECTION);
        GL11.glGetInteger(GL11.GL_VIEWPORT, VIEWPORT);
    }

    /** Résultat de projection : coords écran mises à l'échelle GUI + profondeur. */
    public static final class Screen {
        public final float x, y;
        public final boolean onScreen;
        Screen(float x, float y, boolean onScreen) {
            this.x = x; this.y = y; this.onScreen = onScreen;
        }
    }

    /**
     * Projette une position monde absolue (x,y,z) vers l'écran GUI.
     * @return null si derrière la caméra, sinon coords (onScreen indique si dans le cadre).
     */
    public static Screen project(double x, double y, double z) {
        RenderManager rm = Minecraft.getMinecraft().getRenderManager();
        // coords relatives à la caméra : à RenderWorldLastEvent l'origine modelview = caméra
        float rx = (float) (x - rm.viewerPosX);
        float ry = (float) (y - rm.viewerPosY);
        float rz = (float) (z - rm.viewerPosZ);

        OUT.clear();
        if (!GLU.gluProject(rx, ry, rz, MODELVIEW, PROJECTION, VIEWPORT, OUT)) return null;
        float winX = OUT.get(0);
        float winY = OUT.get(1);
        float winZ = OUT.get(2);
        if (winZ < 0f || winZ > 1f) return null; // derrière la caméra / au-delà

        Minecraft mc = Minecraft.getMinecraft();
        net.minecraft.client.gui.ScaledResolution sr = new net.minecraft.client.gui.ScaledResolution(mc);
        double scale = sr.getScaleFactor();
        float sx = (float) (winX / scale);
        float sy = (float) ((mc.displayHeight - winY) / scale);
        boolean on = sx >= 0 && sy >= 0 && sx <= sr.getScaledWidth() && sy <= sr.getScaledHeight();
        return new Screen(sx, sy, on);
    }
}
