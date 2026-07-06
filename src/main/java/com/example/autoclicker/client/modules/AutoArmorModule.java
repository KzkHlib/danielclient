package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

/**
 * AutoArmor : équipe automatiquement la meilleure armure trouvée dans
 * l'inventaire (valeur de protection + enchantements). Une pièce à la fois,
 * via des clics d'inventaire ; ne touche rien si un coffre/menu est ouvert.
 */
public class AutoArmorModule extends Module {

    private final Setting.Number delay =
            (Setting.Number) add(new Setting.Number("Delai", 4, 1, 20, 1, true));

    private int cooldown = 0;

    public AutoArmorModule() {
        super("AutoArmor", Category.PLAYER);
    }

    @Override
    public void onTick() {
        EntityPlayer p = mc.thePlayer;
        if (p == null) return;
        if (mc.currentScreen != null) return;          // pas pendant un coffre ouvert
        if (cooldown > 0) { cooldown--; return; }

        for (int type = 0; type < 4; type++) {
            int armorSlot = 5 + type;                   // 5=casque .. 8=bottes
            ItemStack equipped = p.inventoryContainer.getSlot(armorSlot).getStack();
            int bestVal = score(equipped, type);
            int bestSlot = -1;

            for (Object o : p.inventoryContainer.inventorySlots) {
                Slot s = (Slot) o;
                if (s.slotNumber < 9 || s.slotNumber > 44) continue;   // inventaire + hotbar
                ItemStack st = s.getStack();
                if (st == null || !(st.getItem() instanceof ItemArmor)) continue;
                if (((ItemArmor) st.getItem()).armorType != type) continue;
                int v = score(st, type);
                if (v > bestVal) { bestVal = v; bestSlot = s.slotNumber; }
            }

            if (bestSlot >= 0) {
                equip(p, bestSlot, armorSlot);
                cooldown = (int) delay.value;
                return;                                 // une pièce par cycle
            }
        }
    }

    /** Valeur d'une armure : protection de base + bonus d'enchantement. */
    private int score(ItemStack st, int type) {
        if (st == null || !(st.getItem() instanceof ItemArmor)) return -1;
        ItemArmor a = (ItemArmor) st.getItem();
        if (a.armorType != type) return -1;
        int v = a.damageReduceAmount * 4;
        v += EnchantmentHelper.getEnchantmentLevel(0, st) * 2;   // Protection
        return v;
    }

    private void equip(EntityPlayer p, int invSlot, int armorSlot) {
        mc.playerController.windowClick(p.inventoryContainer.windowId, invSlot, 0, 0, p);   // prend
        mc.playerController.windowClick(p.inventoryContainer.windowId, armorSlot, 0, 0, p); // pose dans l'armure
        mc.playerController.windowClick(p.inventoryContainer.windowId, invSlot, 0, 0, p);   // repose l'ancienne
    }
}
