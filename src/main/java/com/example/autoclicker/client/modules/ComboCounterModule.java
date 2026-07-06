package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Theme;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

/** ComboCounter : compte tes coups d'affilée, remis à zéro si tu prends un coup. */
public class ComboCounterModule extends Module {

    private int combo = 0;
    private long lastHit = 0L;
    private float lastHealth = 20f;

    public ComboCounterModule() {
        super("ComboCounter", Category.COMBAT);
        showInArrayList = false;
    }

    @Override
    public void onAttackEntity(AttackEntityEvent e) {
        if (e.target instanceof EntityLivingBase) {
            combo++;
            lastHit = System.currentTimeMillis();
        }
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;
        float hp = mc.thePlayer.getHealth();
        if (hp < lastHealth) combo = 0;        // touché -> reset
        lastHealth = hp;
        if (System.currentTimeMillis() - lastHit > 4000) combo = 0; // timeout
    }

    private String text() { return combo + "x combo"; }

    @Override public int hudW(ScaledResolution sr) {
        return combo > 0 ? mc.fontRendererObj.getStringWidth(text()) + 6 : 0;
    }
    @Override public int hudH(ScaledResolution sr) { return 12; }
    @Override protected int hudDefX(ScaledResolution sr) {
        return sr.getScaledWidth() / 2 - mc.fontRendererObj.getStringWidth(text()) / 2;
    }
    @Override protected int hudDefY(ScaledResolution sr) { return sr.getScaledHeight() / 2 - 34; }

    @Override
    public void onRenderHud(ScaledResolution sr) {
        if (combo <= 0) return;
        FontRenderer fr = mc.fontRendererObj;
        String t = text();
        int x = getHudX(sr), y = getHudY(sr);
        Gui.drawRect(x - 3, y - 2, x + fr.getStringWidth(t) + 3, y + 10, 0x80000000);
        fr.drawStringWithShadow(t, x, y, Theme.ACCENT);
    }
}
