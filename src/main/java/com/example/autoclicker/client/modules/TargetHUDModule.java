package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;
import com.example.autoclicker.client.Theme;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

/**
 * TargetHUD : panneau compact et transparent, finitions soignées —
 * mini modèle 3D (skin + armure), nom, vie chiffrée colorée, distance, barre de
 * vie animée avec "ghost" de dégâts, et rangée de stuff + durabilité optionnelle.
 */
public class TargetHUDModule extends Module {

    private static final int W = 120;               // étroit

    private final Setting.Bool showModel =
            (Setting.Bool) add(new Setting.Bool("Modele 3D", true));
    private final Setting.Bool showStuff =
            (Setting.Bool) add(new Setting.Bool("Stuff + dura", true));
    private final Setting.Number opacity =
            (Setting.Number) add(new Setting.Number("Opacite fond", 60, 0, 100, 5, true));

    private static final long LINGER = 2500L;

    private EntityLivingBase target;
    private long lastSeen = 0L;

    private float dispRatio = 1f, ghostRatio = 1f;
    private long lastFrame = System.currentTimeMillis();
    private float intro = 0f;

    public TargetHUDModule() {
        super("TargetHUD", Category.RENDER);
        showInArrayList = false;
    }

    @Override
    public void onTick() {
        if (mc.objectMouseOver != null
                && mc.objectMouseOver.entityHit instanceof EntityLivingBase) {
            target = (EntityLivingBase) mc.objectMouseOver.entityHit;
            lastSeen = System.currentTimeMillis();
        }
    }

    private boolean visible() {
        return target != null && target.isEntityAlive()
                && System.currentTimeMillis() - lastSeen < LINGER;
    }

    private int height() {
        return 38 + (showStuff.value ? 18 : 0);
    }

    @Override public int hudW(ScaledResolution sr) { return visible() ? W : 0; }
    @Override public int hudH(ScaledResolution sr) { return height(); }
    @Override protected int hudDefX(ScaledResolution sr) { return sr.getScaledWidth() / 2 + 50; }
    @Override protected int hudDefY(ScaledResolution sr) { return sr.getScaledHeight() / 2 - 50; }

    @Override
    public void onRenderHud(ScaledResolution sr) {
        long now = System.currentTimeMillis();
        float dt = Math.min(0.1f, (now - lastFrame) / 1000f);
        lastFrame = now;
        intro += ((visible() ? 1f : 0f) - intro) * Math.min(1f, dt * 9f);
        if (intro < 0.02f || target == null) return;

        FontRenderer fr = mc.fontRendererObj;
        int x = getHudX(sr);
        int y = getHudY(sr);
        int H = height();

        float max = target.getMaxHealth();
        float ratio = max <= 0 ? 0 : Math.max(0f, Math.min(1f, target.getHealth() / max));
        dispRatio += (ratio - dispRatio) * Math.min(1f, dt * 10f);
        if (ratio < ghostRatio) ghostRatio += (ratio - ghostRatio) * Math.min(1f, dt * 3f);
        else ghostRatio = dispRatio;

        // apparition : léger fondu + scale
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + W / 2f, y + H / 2f, 0);
        float sc = 0.94f + 0.06f * intro;
        GlStateManager.scale(sc, sc, 1f);
        GlStateManager.translate(-(x + W / 2f), -(y + H / 2f), 0);

        // ---- fond transparent (dégradé) ----
        int a = (int) (opacity.value * 2.55) & 0xFF;
        int bgTop = (a << 24) | 0x141420;
        int bgBot = ((int) (a * 0.85) << 24) | 0x0C0C14;
        drawVGradient(x, y, x + W, y + H, bgTop, bgBot);
        // liserés d'accent
        Gui.drawRect(x, y, x + W, y + 1, Theme.ACCENT);
        Gui.drawRect(x, y, x + 1, y + H, Theme.ACCENT);
        Gui.drawRect(x, y + H - 1, x + W, y + H, Theme.lerp(Theme.ACCENT, Theme.ACCENT2, dispRatio));

        int rx = 6;
        if (showModel.value) {
            drawModel(target, x + 20, y + 33, 18);
            rx = 40;
        }

        // nom + distance
        String name = target.getName();
        int maxNameW = W - rx - 30;
        if (fr.getStringWidth(name) > maxNameW) {
            while (name.length() > 2 && fr.getStringWidth(name + "..") > maxNameW)
                name = name.substring(0, name.length() - 1);
            name += "..";
        }
        fr.drawStringWithShadow(name, x + rx, y + 5, Theme.TEXT);
        String dist = String.format("%.0fm", mc.thePlayer == null ? 0 : mc.thePlayer.getDistanceToEntity(target));
        fr.drawStringWithShadow(dist, x + W - 5 - fr.getStringWidth(dist), y + 5, Theme.ACCENT2);

        // vie chiffrée colorée
        int hpColor = Theme.lerp(0xFFE74C3C, 0xFF2ECC71, dispRatio);
        String hpTxt = ((int) Math.ceil(target.getHealth())) + " HP";
        fr.drawStringWithShadow(hpTxt, x + rx, y + 16, hpColor);

        // barre de vie animée + ghost (pleine largeur sous les infos)
        int barX = x + rx, barY = y + 27, barW = W - rx - 6, barH = 4;
        Gui.drawRect(barX, barY, barX + barW, barY + barH, 0x80202028);
        Gui.drawRect(barX, barY, barX + (int) (barW * ghostRatio), barY + barH, 0x66FFFFFF);
        Gui.drawRect(barX, barY, barX + (int) (barW * dispRatio), barY + barH, hpColor);

        // stuff + durabilité (optionnel, en bas)
        if (showStuff.value) drawGear(x + 5, y + 38);

        GlStateManager.popMatrix();
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    private void drawModel(EntityLivingBase e, int px, int py, int scale) {
        try {
            GlStateManager.enableColorMaterial();
            GuiInventory.drawEntityOnScreen(px, py, scale, (float) px - 35, (float) py - 55, e);
            GlStateManager.disableColorMaterial();
            RenderHelper.disableStandardItemLighting();
        } catch (Throwable ignored) {
        }
    }

    private void drawGear(int startX, int startY) {
        FontRenderer fr = mc.fontRendererObj;
        RenderItem ri = mc.getRenderItem();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableDepth();
        int ix = startX;
        for (int slot = 4; slot >= 0; slot--) {       // 4=casque..1=bottes, 0=main
            ItemStack st = (slot == 0) ? target.getHeldItem() : target.getEquipmentInSlot(slot);
            if (st == null) { ix += 16; continue; }
            ri.renderItemAndEffectIntoGUI(st, ix, startY);
            ri.renderItemOverlayIntoGUI(fr, st, ix, startY, null);
            if (st.isItemStackDamageable()) {
                float d = 1f - (float) st.getItemDamage() / (float) st.getMaxDamage();
                int dc = Theme.lerp(0xFFE74C3C, 0xFF2ECC71, d);
                Gui.drawRect(ix, startY + 15, ix + 16, startY + 16, 0xFF000000);
                Gui.drawRect(ix, startY + 15, ix + (int) (16 * d), startY + 16, dc);
            }
            ix += 16;
        }
        RenderHelper.disableStandardItemLighting();
    }

    /** Rectangle à dégradé vertical (ARGB). */
    private static void drawVGradient(int left, int top, int right, int bottom, int cTop, int cBot) {
        // approximation par bandes (pas de tessellator coloré = simple et net)
        int h = bottom - top;
        for (int i = 0; i < h; i++) {
            float r = i / (float) Math.max(1, h - 1);
            Gui.drawRect(left, top + i, right, top + i + 1, Theme.lerp(cTop, cBot, r));
        }
    }
}
