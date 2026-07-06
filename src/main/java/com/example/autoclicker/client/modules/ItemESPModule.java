package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

public class ItemESPModule extends Module {

    private final Setting.Bool labels = (Setting.Bool) add(new Setting.Bool("Labels", true));
    private final Setting.Bool boxes = (Setting.Bool) add(new Setting.Bool("Boîtes", false));
    private final Setting.Number range = (Setting.Number) add(new Setting.Number("Portée", 32, 5, 64, 4, true));

    public ItemESPModule() {
        super("ItemESP", Category.RENDER);
    }

    @Override
    public void onRenderWorld(float partialTicks) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        double vx = mc.getRenderManager().viewerPosX;
        double vy = mc.getRenderManager().viewerPosY;
        double vz = mc.getRenderManager().viewerPosZ;

        for (Object o : mc.theWorld.loadedEntityList) {
            if (!(o instanceof EntityItem)) continue;
            EntityItem item = (EntityItem) o;
            if (mc.thePlayer.getDistanceToEntity(item) > (double) range.value) continue;

            ItemStack stack = item.getEntityItem();
            if (stack == null) continue;
            String name = stack.getDisplayName();
            int color = getRarityColor(stack);

            double x = item.posX - vx;
            double y = item.posY - vy;
            double z = item.posZ - vz;

            if (boxes.value) {
                drawBox(x, y, z, 0.3, 0.3, color);
            }
            if (labels.value) {
                drawLabel(name, x, y + 0.5, z, color);
            }
        }
    }

    private int getRarityColor(ItemStack stack) {
        switch (stack.getRarity()) {
            case UNCOMMON: return 0xFFFF55;
            case RARE: return 0x55FFFF;
            case EPIC: return 0xFF55FF;
            default: return 0xFFFFFF;
        }
    }

    private void drawBox(double x, double y, double z, double w, double h, int rgb) {
        float r = (rgb >> 16 & 0xFF) / 255f;
        float g = (rgb >> 8 & 0xFF) / 255f;
        float b = (rgb & 0xFF) / 255f;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();

        GlStateManager.color(r, g, b, 0.15f);
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        wr.pos(x - w, y, z - w).endVertex();
        wr.pos(x + w, y, z - w).endVertex();
        wr.pos(x + w, y, z + w).endVertex();
        wr.pos(x - w, y, z + w).endVertex();
        wr.pos(x - w, y + h, z - w).endVertex();
        wr.pos(x + w, y + h, z - w).endVertex();
        wr.pos(x + w, y + h, z + w).endVertex();
        wr.pos(x - w, y + h, z + w).endVertex();
        tess.draw();

        GlStateManager.color(r, g, b, 0.6f);
        GL11.glLineWidth(1.5f);
        wr.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        wr.pos(x - w, y, z - w).endVertex();
        wr.pos(x + w, y, z - w).endVertex();
        wr.pos(x + w, y + h, z - w).endVertex();
        wr.pos(x - w, y + h, z - w).endVertex();
        tess.draw();
        wr.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        wr.pos(x - w, y, z + w).endVertex();
        wr.pos(x + w, y, z + w).endVertex();
        wr.pos(x + w, y + h, z + w).endVertex();
        wr.pos(x - w, y + h, z + w).endVertex();
        tess.draw();
        wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        wr.pos(x - w, y, z - w).endVertex();
        wr.pos(x - w, y, z + w).endVertex();
        wr.pos(x + w, y, z - w).endVertex();
        wr.pos(x + w, y, z + w).endVertex();
        wr.pos(x - w, y + h, z - w).endVertex();
        wr.pos(x - w, y + h, z + w).endVertex();
        wr.pos(x + w, y + h, z - w).endVertex();
        wr.pos(x + w, y + h, z + w).endVertex();
        tess.draw();

        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void drawLabel(String text, double x, double y, double z, int rgb) {
        GlStateManager.pushMatrix();
        RenderManager rm = mc.getRenderManager();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(-rm.playerViewY, 0, 1, 0);
        GlStateManager.rotate(rm.playerViewX, 1, 0, 0);
        float scale = 0.025f;
        GlStateManager.scale(-scale, -scale, scale);
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();

        int w = mc.fontRendererObj.getStringWidth(text) / 2;
        mc.fontRendererObj.drawStringWithShadow(text, -w, 0, rgb | 0xFF000000);

        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
