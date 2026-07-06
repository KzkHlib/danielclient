package com.example.autoclicker.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.AxisAlignedBB;

import org.lwjgl.opengl.GL11;

/**
 * Primitives de dessin pour les modules de rendu monde (ESP, Tracers).
 * Tout est tracé en coordonnées MONDE ABSOLUES : on translate d'abord par
 * -viewerPos pour se replacer dans le repère caméra du RenderWorldLastEvent.
 */
public final class RenderUtil {

    private RenderUtil() {}

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static void color(int argb) {
        float a = (argb >>> 24 & 0xFF) / 255f;
        float r = (argb >>> 16 & 0xFF) / 255f;
        float g = (argb >>> 8 & 0xFF) / 255f;
        float b = (argb & 0xFF) / 255f;
        GlStateManager.color(r, g, b, a);
    }

    private static void pre() {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        RenderManager rm = mc.getRenderManager();
        GlStateManager.translate(-rm.viewerPosX, -rm.viewerPosY, -rm.viewerPosZ);
    }

    private static void post() {
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.popMatrix();
    }

    /** Boîte 3D filaire autour d'une AABB monde. */
    public static void box3D(AxisAlignedBB bb, int argb, float lineWidth) {
        pre();
        GL11.glLineWidth(lineWidth);
        color(argb);
        drawBoxLines(bb);
        GL11.glLineWidth(1f);
        post();
    }

    /** Boîte 2D : rectangle plein écran-aligné (billboard) autour de l'entité. */
    public static void box2D(double x, double y, double z, float width, float height,
                             int outline, int fill, float lineWidth) {
        pre();
        // billboard : annule la rotation de la vue pour faire face à la caméra
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0f, 1f, 0f);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1f, 0f, 0f);
        float hw = width / 2f;

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();

        // remplissage
        color(fill);
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        wr.pos(-hw, 0, 0).endVertex();
        wr.pos(hw, 0, 0).endVertex();
        wr.pos(hw, height, 0).endVertex();
        wr.pos(-hw, height, 0).endVertex();
        tess.draw();

        // contour
        GL11.glLineWidth(lineWidth);
        color(outline);
        wr.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        wr.pos(-hw, 0, 0).endVertex();
        wr.pos(hw, 0, 0).endVertex();
        wr.pos(hw, height, 0).endVertex();
        wr.pos(-hw, height, 0).endVertex();
        tess.draw();
        GL11.glLineWidth(1f);
        post();
    }

    /** Ligne monde A->B (tracer). */
    public static void line(double x1, double y1, double z1,
                            double x2, double y2, double z2, int argb, float lineWidth) {
        pre();
        GL11.glLineWidth(lineWidth);
        color(argb);
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        wr.pos(x1, y1, z1).endVertex();
        wr.pos(x2, y2, z2).endVertex();
        tess.draw();
        GL11.glLineWidth(1f);
        post();
    }

    private static void drawBoxLines(AxisAlignedBB b) {
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        // 12 arêtes du cube
        edge(wr, b.minX, b.minY, b.minZ, b.maxX, b.minY, b.minZ);
        edge(wr, b.maxX, b.minY, b.minZ, b.maxX, b.minY, b.maxZ);
        edge(wr, b.maxX, b.minY, b.maxZ, b.minX, b.minY, b.maxZ);
        edge(wr, b.minX, b.minY, b.maxZ, b.minX, b.minY, b.minZ);
        edge(wr, b.minX, b.maxY, b.minZ, b.maxX, b.maxY, b.minZ);
        edge(wr, b.maxX, b.maxY, b.minZ, b.maxX, b.maxY, b.maxZ);
        edge(wr, b.maxX, b.maxY, b.maxZ, b.minX, b.maxY, b.maxZ);
        edge(wr, b.minX, b.maxY, b.maxZ, b.minX, b.maxY, b.minZ);
        edge(wr, b.minX, b.minY, b.minZ, b.minX, b.maxY, b.minZ);
        edge(wr, b.maxX, b.minY, b.minZ, b.maxX, b.maxY, b.minZ);
        edge(wr, b.maxX, b.minY, b.maxZ, b.maxX, b.maxY, b.maxZ);
        edge(wr, b.minX, b.minY, b.maxZ, b.minX, b.maxY, b.maxZ);
        tess.draw();
    }

    private static void edge(WorldRenderer wr,
                             double x1, double y1, double z1,
                             double x2, double y2, double z2) {
        wr.pos(x1, y1, z1).endVertex();
        wr.pos(x2, y2, z2).endVertex();
    }

    /** AABB interpolée d'une entité au frame courant (anti-tremblement). */
    public static AxisAlignedBB interpolatedBox(net.minecraft.entity.Entity e, float pt) {
        double x = e.lastTickPosX + (e.posX - e.lastTickPosX) * pt;
        double y = e.lastTickPosY + (e.posY - e.lastTickPosY) * pt;
        double z = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * pt;
        AxisAlignedBB bb = e.getEntityBoundingBox();
        double w = (bb.maxX - bb.minX) / 2.0;
        return new AxisAlignedBB(
                x - w, y, z - w,
                x + w, y + (bb.maxY - bb.minY), z + w);
    }
}
