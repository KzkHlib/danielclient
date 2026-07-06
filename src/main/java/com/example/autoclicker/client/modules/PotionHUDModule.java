package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Theme;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.StatCollector;

import java.util.ArrayList;
import java.util.List;

/** PotionHUD : liste des effets actifs avec niveau et timer. */
public class PotionHUDModule extends Module {

    private static final int ROW = 11;

    public PotionHUDModule() {
        super("PotionHUD", Category.RENDER);
        showInArrayList = false;
    }

    private List<PotionEffect> effects() {
        List<PotionEffect> list = new ArrayList<PotionEffect>();
        if (mc.thePlayer != null) {
            list.addAll(mc.thePlayer.getActivePotionEffects());
        }
        return list;
    }

    private String label(PotionEffect e) {
        Potion p = Potion.potionTypes[e.getPotionID()];
        String name = StatCollector.translateToLocal(p.getName());
        int lvl = e.getAmplifier() + 1;
        if (lvl > 1) name += " " + lvl;
        return name + " " + Potion.getDurationString(e);
    }

    @Override
    public int hudW(ScaledResolution sr) {
        FontRenderer fr = mc.fontRendererObj;
        int max = 0;
        for (PotionEffect e : effects()) max = Math.max(max, fr.getStringWidth(label(e)) + 8);
        return max;
    }
    @Override public int hudH(ScaledResolution sr) { return effects().size() * ROW; }
    @Override protected int hudDefX(ScaledResolution sr) { return 3; }
    @Override protected int hudDefY(ScaledResolution sr) { return sr.getScaledHeight() / 2 - 20; }

    @Override
    public void onRenderHud(ScaledResolution sr) {
        List<PotionEffect> list = effects();
        if (list.isEmpty()) return;
        FontRenderer fr = mc.fontRendererObj;
        int x = getHudX(sr);
        int y = getHudY(sr);
        int w = hudW(sr);

        Gui.drawRect(x - 2, y - 2, x + w, y + list.size() * ROW + 1, 0x90000000);
        for (int i = 0; i < list.size(); i++) {
            PotionEffect e = list.get(i);
            int color = Potion.potionTypes[e.getPotionID()].getLiquidColor() | 0xFF000000;
            Gui.drawRect(x - 2, y + i * ROW, x - 1, y + i * ROW + ROW - 1, color);
            fr.drawStringWithShadow(label(e), x + 2, y + i * ROW + 1, Theme.TEXT);
        }
    }
}
