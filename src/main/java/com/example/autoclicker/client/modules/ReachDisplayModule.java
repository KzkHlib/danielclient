package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Theme;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

/** ReachDisplay : affiche la distance de ton dernier coup (info, n'augmente rien). */
public class ReachDisplayModule extends Module {

    private double lastReach = 0;
    private long lastTime = 0L;

    public ReachDisplayModule() {
        super("ReachDisplay", Category.COMBAT);
        showInArrayList = false;
    }

    @Override
    public void onAttackEntity(AttackEntityEvent e) {
        if (mc.thePlayer == null || e.target == null) return;
        lastReach = mc.thePlayer.getDistanceToEntity(e.target);
        lastTime = System.currentTimeMillis();
    }

    private boolean visible() { return System.currentTimeMillis() - lastTime < 3000; }
    private String text() { return String.format("Reach : %.2f m", lastReach); }

    @Override public int hudW(ScaledResolution sr) {
        return visible() ? mc.fontRendererObj.getStringWidth(text()) + 6 : 0;
    }
    @Override public int hudH(ScaledResolution sr) { return 12; }
    @Override protected int hudDefX(ScaledResolution sr) {
        return sr.getScaledWidth() / 2 - mc.fontRendererObj.getStringWidth(text()) / 2;
    }
    @Override protected int hudDefY(ScaledResolution sr) { return sr.getScaledHeight() / 2 - 48; }

    @Override
    public void onRenderHud(ScaledResolution sr) {
        if (!visible()) return;
        FontRenderer fr = mc.fontRendererObj;
        String t = text();
        int x = getHudX(sr), y = getHudY(sr);
        Gui.drawRect(x - 3, y - 2, x + fr.getStringWidth(t) + 3, y + 10, 0x80000000);
        fr.drawStringWithShadow(t, x, y, Theme.TEXT);
    }
}
