package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

/**
 * AutoPot : lance une potion splash de soin vers le sol quand tes PV passent
 * sous le seuil. Visée silencieuse (packet), sans bouger ta caméra.
 */
public class AutoPotModule extends Module {

    private final Setting.Number threshold =
            (Setting.Number) add(new Setting.Number("Seuil PV", 8, 1, 19, 1, true));
    private final Setting.Bool healOnly =
            (Setting.Bool) add(new Setting.Bool("Soin uniquement", true));
    private final Setting.Number cooldownTicks =
            (Setting.Number) add(new Setting.Number("Cooldown", 25, 5, 60, 5, true));

    private int cooldown = 0;

    public AutoPotModule() {
        super("AutoPot", Category.COMBAT);
    }

    @Override
    public void onTick() {
        EntityPlayerSP p = mc.thePlayer;
        if (p == null || mc.theWorld == null || mc.currentScreen != null) return;
        if (cooldown > 0) { cooldown--; return; }
        if (p.getHealth() > threshold.value) return;

        int slot = findPotSlot();
        if (slot < 0) return;

        int prev = p.inventory.currentItem;
        // sélection + visée vers le bas (packet, silencieux) + jet
        p.sendQueue.addToSendQueue(new C09PacketHeldItemChange(slot));
        p.inventory.currentItem = slot;
        p.sendQueue.addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(p.rotationYaw, 80f, p.onGround));
        ItemStack st = p.inventory.getCurrentItem();
        if (st != null) mc.playerController.sendUseItem(p, mc.theWorld, st);
        // restauration du slot (la caméra n'a pas bougé)
        p.sendQueue.addToSendQueue(new C09PacketHeldItemChange(prev));
        p.inventory.currentItem = prev;

        cooldown = (int) cooldownTicks.value;
    }

    private int findPotSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.thePlayer.inventory.getStackInSlot(i);
            if (s == null || !(s.getItem() instanceof ItemPotion)) continue;
            if (!ItemPotion.isSplash(s.getMetadata())) continue;
            if (healOnly.value && !isHeal((ItemPotion) s.getItem(), s)) continue;
            return i;
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    private boolean isHeal(ItemPotion item, ItemStack st) {
        java.util.List<PotionEffect> fx = item.getEffects(st);
        if (fx == null) return false;
        for (PotionEffect pe : fx) {
            if (pe.getPotionID() == Potion.heal.id || pe.getPotionID() == Potion.regeneration.id) return true;
        }
        return false;
    }
}
