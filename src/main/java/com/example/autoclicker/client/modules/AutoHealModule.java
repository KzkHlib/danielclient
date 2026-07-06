package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemSoup;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class AutoHealModule extends Module {

    private final Setting.Number health = (Setting.Number) add(new Setting.Number("PV min", 8, 2, 20, 1, true));
    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "Pot", "Soup", "Gapp", "Smart"));
    private final Setting.Mode potMode = (Setting.Mode) add(new Setting.Mode("Pot mode", 0, "Normal", "Jump", "NCP", "AAC"));
    private final Setting.Mode soupMode = (Setting.Mode) add(new Setting.Mode("Soup mode", 0, "Normal", "Instant", "AAC"));
    private final Setting.Mode gappMode = (Setting.Mode) add(new Setting.Mode("Gapp mode", 0, "Normal", "Packet"));
    private final Setting.Bool pauseBow = (Setting.Bool) add(new Setting.Bool("Pause bow", true));
    private long lastHeal = 0L;

    public AutoHealModule() { super("AutoHeal", Category.COMBAT); }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;
        if (System.currentTimeMillis() - lastHeal < 500L) return;
        float hp = mc.thePlayer.getHealth();
        float maxHp = mc.thePlayer.getMaxHealth();
        if (hp > (float) health.value || hp >= maxHp) return;
        if (pauseBow.value && mc.thePlayer.getHeldItem() != null
                && mc.thePlayer.getHeldItem().getItem() instanceof ItemBow) return;

        String m = mode.current();
        if (m.equals("Smart")) {
            if (hasItem(ItemPotion.class)) m = "Pot";
            else if (hasItem(ItemSoup.class)) m = "Soup";
            else if (hasItem(ItemAppleGold.class)) m = "Gapp";
            else return;
        }

        if (m.equals("Pot")) {
            String pm = potMode.current();
            if (pm.equals("Jump")) { mc.thePlayer.jump(); }
            else if (pm.equals("NCP")) { mc.thePlayer.motionY = 0.3; }
            useBest(ItemPotion.class, stack -> isHealPotion(stack));
        } else if (m.equals("Soup")) {
            useBest(ItemSoup.class, null);
            if (soupMode.is("Instant") || soupMode.is("AAC")) {
                dropBowl();
            }
        } else if (m.equals("Gapp")) {
            useBest(ItemAppleGold.class, null);
        }
    }

    private void dropBowl() {
        for (int i = 0; i < 36; i++) {
            net.minecraft.item.ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack != null && stack.getItem() == net.minecraft.init.Items.bowl) {
                mc.playerController.windowClick(0, i, 1, 4, mc.thePlayer);
                break;
            }
        }
    }

    private boolean isHealPotion(net.minecraft.item.ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof ItemPotion)) return false;
        ItemPotion pot = (ItemPotion) stack.getItem();
        java.util.List<PotionEffect> effects = pot.getEffects(stack);
        if (effects == null) return false;
        for (PotionEffect e : effects) {
            if (e.getPotionID() == Potion.heal.id || e.getPotionID() == Potion.regeneration.id) return true;
        }
        return false;
    }

    private boolean hasItem(Class<?> clazz) {
        for (int i = 0; i < 36; i++) {
            if (mc.thePlayer.inventory.mainInventory[i] != null
                    && clazz.isInstance(mc.thePlayer.inventory.mainInventory[i].getItem())) return true;
        }
        return false;
    }

    private interface StackCheck { boolean check(net.minecraft.item.ItemStack stack); }

    private void useBest(Class<?> clazz, StackCheck check) {
        int bestSlot = -1;
        int bestPriority = -1;
        for (int i = 0; i < 36; i++) {
            net.minecraft.item.ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack == null || !clazz.isInstance(stack.getItem())) continue;
            if (check != null && !check.check(stack)) continue;
            int priority = stack.stackSize;
            if (stack.getItem() instanceof ItemPotion) {
                ItemPotion pot = (ItemPotion) stack.getItem();
                java.util.List<PotionEffect> effects = pot.getEffects(stack);
                if (effects != null) {
                    for (PotionEffect e : effects) {
                        if (e.getPotionID() == Potion.heal.id) priority += 100;
                        if (e.getPotionID() == Potion.regeneration.id) priority += 50;
                    }
                }
            }
            if (priority > bestPriority) { bestPriority = priority; bestSlot = i; }
        }
        if (bestSlot < 0) return;

        int prevSlot = mc.thePlayer.inventory.currentItem;
        if (bestSlot < 9) {
            mc.thePlayer.inventory.currentItem = bestSlot;
            mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(bestSlot));
        } else {
            mc.playerController.windowClick(0, bestSlot, 0, 0, mc.thePlayer);
            mc.playerController.windowClick(0, bestSlot, 1, 0, mc.thePlayer);
        }

        mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
        lastHeal = System.currentTimeMillis();

        if (bestSlot >= 9) {
            mc.thePlayer.inventory.currentItem = prevSlot;
            mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(prevSlot));
        }
    }
}
