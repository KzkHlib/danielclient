package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class BreadcrumbsModule extends Module {

    private final Setting.Number thickness = (Setting.Number) add(new Setting.Number("Epaisseur", 2, 1, 6, 1, true));
    private final Setting.Number fadeTicks = (Setting.Number) add(new Setting.Number("Durée ticks", 200, 20, 600, 20, true));
    private final Setting.Bool rainbow = (Setting.Bool) add(new Setting.Bool("Arc-en-ciel", true));

    private static class Point {
        final double x, y, z;
        final int tick;
        Point(double x, double y, double z, int t) { this.x = x; this.y = y; this.z = z; this.tick = t; }
    }

    private final List<Point> trail = new ArrayList<Point>();
    private int tickCounter = 0;

    public BreadcrumbsModule() {
        super("Breadcrumbs", Category.RENDER);
    }

    @Override
    public void onEnable() { trail.clear(); tickCounter = 0; }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;
        tickCounter++;
        if (tickCounter % 3 != 0) return;
        trail.add(new Point(mc.thePlayer.posX, mc.thePlayer.posY + 0.1, mc.thePlayer.posZ, tickCounter));
    }

    @Override
    public void onRenderWorld(float partialTicks) {
        if (mc.thePlayer == null || trail.size() < 2) return;
        int now = tickCounter;
        int maxAge = (int) fadeTicks.value;

        while (!trail.isEmpty() && now - trail.get(0).tick > maxAge) trail.remove(0);
        if (trail.size() < 2) return;

        double vx = mc.getRenderManager().viewerPosX;
        double vy = mc.getRenderManager().viewerPosY;
        double vz = mc.getRenderManager().viewerPosZ;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth((float) thickness.value);

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();

        for (int i = 1; i < trail.size(); i++) {
            Point a = trail.get(i - 1);
            Point b = trail.get(i);
            float age = 1f - (float) (now - b.tick) / maxAge;
            if (age < 0.05f) continue;

            int alpha = (int) (age * 200);
            int color;
            if (rainbow.value) {
                float hue = (System.currentTimeMillis() % 3600L) / 3600f + i * 0.02f;
                if (hue > 1) hue -= 1;
                color = java.awt.Color.HSBtoRGB(hue, 0.8f, 1f);
            } else {
                color = 0x55FFFF;
            }
            int argb = (alpha << 24) | (color & 0xFFFFFF);

            float r = (argb >> 16 & 0xFF) / 255f;
            float g = (argb >> 8 & 0xFF) / 255f;
            float b_ = (argb & 0xFF) / 255f;
            float a_ = (argb >> 24 & 0xFF) / 255f;

            wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            wr.pos(a.x - vx, a.y - vy, a.z - vz).color(r, g, b_, a_).endVertex();
            wr.pos(b.x - vx, b.y - vy, b.z - vz).color(r, g, b_, a_).endVertex();
            tess.draw();
        }

        GL11.glLineWidth(1f);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        // trim
        while (trail.size() > 2000) trail.remove(0);
    }
}
