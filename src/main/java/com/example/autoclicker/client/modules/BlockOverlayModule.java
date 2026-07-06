package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;
import com.example.autoclicker.client.Theme;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import org.lwjgl.opengl.GL11;

/**
 * BlockOverlay : remplace la sélection vanilla par un contour (et remplissage)
 * coloré sur le bloc visé. Couleur d'accent du client.
 */
public class BlockOverlayModule extends Module {

    private final Setting.Number thickness =
            (Setting.Number) add(new Setting.Number("Epaisseur", 2, 1, 5, 0.5, false));
    private final Setting.Bool fill =
            (Setting.Bool) add(new Setting.Bool("Remplir", true));

    public BlockOverlayModule() {
        super("BlockOverlay", Category.VISUAL);
    }

    @Override
    public void onDrawBlockHighlight(DrawBlockHighlightEvent e) {
        MovingObjectPosition target = e.target;
        if (target == null || target.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;

        BlockPos pos = target.getBlockPos();
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        block.setBlockBoundsBasedOnState(mc.theWorld, pos);
        AxisAlignedBB raw = block.getSelectedBoundingBox(mc.theWorld, pos);
        if (raw == null) return;

        float pt = e.partialTicks;
        double px = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * pt;
        double py = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * pt;
        double pz = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * pt;
        AxisAlignedBB bb = new AxisAlignedBB(
                raw.minX - px, raw.minY - py, raw.minZ - pz,
                raw.maxX - px, raw.maxY - py, raw.maxZ - pz).expand(0.002, 0.002, 0.002);

        int c = Theme.ACCENT;
        float r = ((c >> 16) & 0xFF) / 255f;
        float g = ((c >> 8) & 0xFF) / 255f;
        float b = (c & 0xFF) / 255f;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);

        if (fill.value) {
            GlStateManager.color(r, g, b, 0.22f);
            drawFilled(bb);
        }
        GlStateManager.color(r, g, b, 0.85f);
        GL11.glLineWidth((float) thickness.value);
        drawOutline(bb);

        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        e.setCanceled(true); // remplace la sélection vanilla
    }

    private void line(WorldRenderer wr, double x1, double y1, double z1, double x2, double y2, double z2) {
        wr.pos(x1, y1, z1).endVertex();
        wr.pos(x2, y2, z2).endVertex();
    }

    private void drawOutline(AxisAlignedBB b) {
        Tessellator t = Tessellator.getInstance();
        WorldRenderer wr = t.getWorldRenderer();
        wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        line(wr, b.minX, b.minY, b.minZ, b.maxX, b.minY, b.minZ);
        line(wr, b.maxX, b.minY, b.minZ, b.maxX, b.minY, b.maxZ);
        line(wr, b.maxX, b.minY, b.maxZ, b.minX, b.minY, b.maxZ);
        line(wr, b.minX, b.minY, b.maxZ, b.minX, b.minY, b.minZ);
        line(wr, b.minX, b.maxY, b.minZ, b.maxX, b.maxY, b.minZ);
        line(wr, b.maxX, b.maxY, b.minZ, b.maxX, b.maxY, b.maxZ);
        line(wr, b.maxX, b.maxY, b.maxZ, b.minX, b.maxY, b.maxZ);
        line(wr, b.minX, b.maxY, b.maxZ, b.minX, b.maxY, b.minZ);
        line(wr, b.minX, b.minY, b.minZ, b.minX, b.maxY, b.minZ);
        line(wr, b.maxX, b.minY, b.minZ, b.maxX, b.maxY, b.minZ);
        line(wr, b.maxX, b.minY, b.maxZ, b.maxX, b.maxY, b.maxZ);
        line(wr, b.minX, b.minY, b.maxZ, b.minX, b.maxY, b.maxZ);
        t.draw();
    }

    private void drawFilled(AxisAlignedBB b) {
        Tessellator t = Tessellator.getInstance();
        WorldRenderer wr = t.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        wr.pos(b.minX, b.minY, b.minZ).endVertex(); wr.pos(b.maxX, b.minY, b.minZ).endVertex();
        wr.pos(b.maxX, b.minY, b.maxZ).endVertex(); wr.pos(b.minX, b.minY, b.maxZ).endVertex();
        wr.pos(b.minX, b.maxY, b.minZ).endVertex(); wr.pos(b.minX, b.maxY, b.maxZ).endVertex();
        wr.pos(b.maxX, b.maxY, b.maxZ).endVertex(); wr.pos(b.maxX, b.maxY, b.minZ).endVertex();
        wr.pos(b.minX, b.minY, b.minZ).endVertex(); wr.pos(b.minX, b.maxY, b.minZ).endVertex();
        wr.pos(b.maxX, b.maxY, b.minZ).endVertex(); wr.pos(b.maxX, b.minY, b.minZ).endVertex();
        wr.pos(b.minX, b.minY, b.maxZ).endVertex(); wr.pos(b.maxX, b.minY, b.maxZ).endVertex();
        wr.pos(b.maxX, b.maxY, b.maxZ).endVertex(); wr.pos(b.minX, b.maxY, b.maxZ).endVertex();
        wr.pos(b.minX, b.minY, b.minZ).endVertex(); wr.pos(b.minX, b.minY, b.maxZ).endVertex();
        wr.pos(b.minX, b.maxY, b.maxZ).endVertex(); wr.pos(b.minX, b.maxY, b.minZ).endVertex();
        wr.pos(b.maxX, b.minY, b.minZ).endVertex(); wr.pos(b.maxX, b.maxY, b.minZ).endVertex();
        wr.pos(b.maxX, b.maxY, b.maxZ).endVertex(); wr.pos(b.maxX, b.minY, b.maxZ).endVertex();
        t.draw();
    }
}
