package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.*;

import java.util.HashSet;
import java.util.Set;

public class InvCleanerModule extends Module {

    private final Setting.Bool tools = (Setting.Bool) add(new Setting.Bool("Outils", true));
    private final Setting.Bool swords = (Setting.Bool) add(new Setting.Bool("Epées", false));
    private final Setting.Bool armor = (Setting.Bool) add(new Setting.Bool("Armure", false));
    private final Setting.Bool bows = (Setting.Bool) add(new Setting.Bool("Arcs", false));
    private final Setting.Bool rods = (Setting.Bool) add(new Setting.Bool("Canes", false));
    private final Setting.Bool pots = (Setting.Bool) add(new Setting.Bool("Potions", false));
    private final Setting.Bool food = (Setting.Bool) add(new Setting.Bool("Nourriture", false));
    private final Setting.Bool blocks = (Setting.Bool) add(new Setting.Bool("Blocs", false));
    private final Setting.Number delay = (Setting.Number) add(new Setting.Number("Delay", 100, 20, 500, 20, true));
    private final Setting.Number invulnerable = (Setting.Number) add(new Setting.Number("Invulnérable", 0, 0, 2000, 100, true));

    private long lastClean = 0L;
    private int cleanSlot = 9;

    public InvCleanerModule() {
        super("InvCleaner", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        cleanSlot = 9;
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;
        if (mc.currentScreen != null) return;
        if (System.currentTimeMillis() - lastClean < (long) delay.value) return;
        if (mc.thePlayer.ticksExisted < (int) invulnerable.value / 50) return;

        if (cleanSlot >= 45) cleanSlot = 9;

        ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(cleanSlot).getStack();
        if (stack != null && shouldDrop(stack)) {
            mc.playerController.windowClick(0, cleanSlot, 1, 4, mc.thePlayer);
            lastClean = System.currentTimeMillis();
        }
        cleanSlot++;
    }

    private boolean shouldDrop(ItemStack stack) {
        if (stack.getItem() instanceof ItemSword && !swords.value) return false;
        if (stack.getItem() instanceof ItemArmor && !armor.value) return false;
        if (stack.getItem() instanceof ItemTool && !tools.value) return false;
        if (stack.getItem() instanceof ItemBow && !bows.value) return false;
        if (stack.getItem() instanceof ItemFishingRod && !rods.value) return false;
        if (stack.getItem() instanceof ItemPotion && !pots.value) return false;
        if (stack.getItem() instanceof ItemFood && !food.value) return false;
        if (stack.getItem() instanceof ItemBlock && !blocks.value) return false;
        return true;
    }
}
