package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ChestStealer : recupere automatiquement le contenu d'un coffre ouvert.
 */
public class ChestStealerModule extends Module {

    private final Setting.Mode profile =
            (Setting.Mode) add(new Setting.Mode("Profil", 0, "Instant", "Rapide", "Legit"));
    private final Setting.Number startDelay =
            (Setting.Number) add(new Setting.Number("Delai ouverture", 0, 0, 1000, 25, true));
    private final Setting.Number clickDelay =
            (Setting.Number) add(new Setting.Number("Recuperation ms", 0, 0, 500, 5, true));
    private final Setting.Number itemsPerTick =
            (Setting.Number) add(new Setting.Number("Items/tick", 54, 1, 54, 1, true));
    private final Setting.Number randomization =
            (Setting.Number) add(new Setting.Number("Randomisation", 0, 0, 80, 5, true));
    private final Setting.Bool randomSlots =
            (Setting.Bool) add(new Setting.Bool("Slots aleatoires", false));
    private final Setting.Bool closeEmpty =
            (Setting.Bool) add(new Setting.Bool("Fermer vide", true));
    private final Setting.Bool diamondChestplate =
            (Setting.Bool) add(new Setting.Bool("Plastron dia Thorns1", true));
    private final Setting.Bool diamondLeggings =
            (Setting.Bool) add(new Setting.Bool("Pantalon dia Thorns1", true));
    private final Setting.Bool diamondHelmet =
            (Setting.Bool) add(new Setting.Bool("Casque dia Thorns1", true));
    private final Setting.Bool diamondSword =
            (Setting.Bool) add(new Setting.Bool("Sword dia Sharp1", true));
    private final Setting.Bool goldIngots =
            (Setting.Bool) add(new Setting.Bool("Lingots or", true));
    private final Setting.Bool xpBottles =
            (Setting.Bool) add(new Setting.Bool("Bouteilles XP", true));

    private int lastWindowId = -1;
    private long openedAt = 0L;
    private long nextClick = 0L;

    public ChestStealerModule() {
        super("ChestStealer", Category.PLAYER);
    }

    @Override
    public String arrayListSuffix() {
        return profile.current() + " " + ((int) clickDelay.value) + "ms";
    }

    @Override
    public void onDisable() {
        resetChest();
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!(mc.currentScreen instanceof GuiChest)) {
            resetChest();
            return;
        }
        if (!(mc.thePlayer.openContainer instanceof ContainerChest)) return;

        ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
        long now = System.currentTimeMillis();

        if (chest.windowId != lastWindowId) {
            lastWindowId = chest.windowId;
            openedAt = now;
            applyProfile();
            nextClick = now + delay(startDelay.value);
            return;
        }

        if (now - openedAt < startDelay.value || now < nextClick) return;

        List<Integer> slots = filledSlots(chest, chest.getLowerChestInventory().getSizeInventory());
        if (slots.isEmpty()) {
            if (closeEmpty.value) mc.thePlayer.closeScreen();
            return;
        }

        int maxClicks = profile.is("Instant")
                ? slots.size()
                : Math.min(slots.size(), (int) itemsPerTick.value);
        for (int i = 0; i < maxClicks; i++) {
            mc.playerController.windowClick(chest.windowId, slots.get(i), 0, 1, mc.thePlayer);
        }

        nextClick = now + delay(clickDelay.value);
    }

    private void resetChest() {
        lastWindowId = -1;
        openedAt = 0L;
        nextClick = 0L;
    }

    private List<Integer> filledSlots(ContainerChest chest, int chestSize) {
        List<Integer> slots = new ArrayList<Integer>();
        for (int i = 0; i < chestSize && i < chest.inventorySlots.size(); i++) {
            Slot slot = (Slot) chest.inventorySlots.get(i);
            ItemStack stack = slot.getStack();
            if (stack != null && shouldSteal(stack)) slots.add(i);
        }
        if (randomSlots.value && slots.size() > 1) Collections.shuffle(slots);
        return slots;
    }

    private boolean shouldSteal(ItemStack stack) {
        if (goldIngots.value && stack.getItem() == Items.gold_ingot) return true;
        if (xpBottles.value && stack.getItem() == Items.experience_bottle) return true;

        if (diamondChestplate.value && stack.getItem() == Items.diamond_chestplate && hasThornsOne(stack)) return true;
        if (diamondLeggings.value && stack.getItem() == Items.diamond_leggings && hasThornsOne(stack)) return true;
        if (diamondHelmet.value && stack.getItem() == Items.diamond_helmet && hasThornsOne(stack)) return true;
        if (diamondSword.value && stack.getItem() == Items.diamond_sword && hasSharpnessOne(stack)) return true;

        return false;
    }

    private boolean hasThornsOne(ItemStack stack) {
        return EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack) == 1;
    }

    private boolean hasSharpnessOne(ItemStack stack) {
        return EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack) == 1;
    }

    private long delay(double base) {
        if (randomization.value <= 0 || base <= 0) return Math.max(0L, Math.round(base));
        double jitter = (Math.random() + Math.random() + Math.random()) / 3.0 - 0.5;
        double factor = 1.0 + jitter * (randomization.value / 100.0) * 2.0;
        return Math.max(0L, Math.round(base * factor));
    }

    private void applyProfile() {
        if (profile.is("Instant")) {
            startDelay.value = 0;
            clickDelay.value = 0;
            itemsPerTick.value = 54;
            randomization.value = 0;
        } else if (profile.is("Rapide")) {
            if (startDelay.value < 50) startDelay.value = 50;
            if (clickDelay.value < 20) clickDelay.value = 20;
            if (itemsPerTick.value < 12) itemsPerTick.value = 12;
        } else if (profile.is("Legit")) {
            if (startDelay.value < 150) startDelay.value = 150;
            if (clickDelay.value < 70) clickDelay.value = 70;
            if (itemsPerTick.value > 4) itemsPerTick.value = 4;
            if (randomization.value < 15) randomization.value = 15;
        }
    }
}
