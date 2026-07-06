package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** ArmorHUD : pièces d'armure + objet en main avec durabilité. */
public class ArmorHUDModule extends Module {

    private static final int SLOT = 18;

    public ArmorHUDModule() {
        super("ArmorHUD", Category.RENDER);
        showInArrayList = false;
    }

    /** Casque -> bottes, puis objet en main (ignore les emplacements vides). */
    private List<ItemStack> stacks() {
        List<ItemStack> out = new ArrayList<ItemStack>();
        if (mc.thePlayer == null) return out;
        ItemStack[] armor = mc.thePlayer.inventory.armorInventory;
        for (int i = armor.length - 1; i >= 0; i--) {
            if (armor[i] != null) out.add(armor[i]);
        }
        ItemStack held = mc.thePlayer.inventory.getCurrentItem();
        if (held != null) out.add(held);
        return out;
    }

    @Override public int hudW(ScaledResolution sr) { return stacks().size() * SLOT; }
    @Override public int hudH(ScaledResolution sr) { return SLOT; }
    @Override protected int hudDefX(ScaledResolution sr) {
        return sr.getScaledWidth() / 2 - hudW(sr) / 2;
    }
    @Override protected int hudDefY(ScaledResolution sr) {
        return sr.getScaledHeight() - 55; // au-dessus de la hotbar
    }

    @Override
    public void onRenderHud(ScaledResolution sr) {
        List<ItemStack> list = stacks();
        if (list.isEmpty()) return;
        int x = getHudX(sr);
        int y = getHudY(sr);

        RenderItem ri = mc.getRenderItem();
        RenderHelper.enableGUIStandardItemLighting();
        for (int i = 0; i < list.size(); i++) {
            int sx = x + i * SLOT;
            ItemStack s = list.get(i);
            ri.renderItemAndEffectIntoGUI(s, sx, y);
            ri.renderItemOverlayIntoGUI(mc.fontRendererObj, s, sx, y, null);
        }
        RenderHelper.disableStandardItemLighting();
    }
}
