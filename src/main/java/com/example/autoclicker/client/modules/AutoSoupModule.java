package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C09PacketHeldItemChange;

/**
 * AutoSoup : mange une soupe (ou nourriture) quand tes PV passent sous le seuil
 * (serveurs soup PvP). Cooldown réglable, et drop automatique du bol vide.
 */
public class AutoSoupModule extends Module {

    private final Setting.Number threshold =
            (Setting.Number) add(new Setting.Number("Seuil PV", 8, 1, 19, 1, true));
    private final Setting.Mode type =
            (Setting.Mode) add(new Setting.Mode("Type", 0, "Soupe", "Nourriture", "Les deux"));
    private final Setting.Number cooldownTicks =
            (Setting.Number) add(new Setting.Number("Cooldown", 6, 1, 40, 1, true));
    private final Setting.Bool autoDrop =
            (Setting.Bool) add(new Setting.Bool("Drop bol auto", true));
    private final Setting.Bool keepOne =
            (Setting.Bool) add(new Setting.Bool("Garde 1 slot", false));

    private int cooldown = 0;

    public AutoSoupModule() {
        super("AutoSoup", Category.PLAYER);
    }

    @Override
    public void onTick() {
        EntityPlayerSP p = mc.thePlayer;
        if (p == null || mc.theWorld == null || mc.currentScreen != null) return;

        if (autoDrop.value) dropBowls(p);

        if (cooldown > 0) { cooldown--; return; }
        if (p.getHealth() > threshold.value) return;

        int slot = findSoupSlot(p);
        if (slot < 0) return;

        int prev = p.inventory.currentItem;
        p.sendQueue.addToSendQueue(new C09PacketHeldItemChange(slot));
        p.inventory.currentItem = slot;
        ItemStack st = p.inventory.getCurrentItem();
        if (st != null) mc.playerController.sendUseItem(p, mc.theWorld, st);  // mange/soupe
        p.sendQueue.addToSendQueue(new C09PacketHeldItemChange(prev));
        p.inventory.currentItem = prev;

        cooldown = (int) cooldownTicks.value;
    }

    private int findSoupSlot(EntityPlayerSP p) {
        int count = 0;
        int found = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.inventory.getStackInSlot(i);
            if (s == null) continue;
            boolean soup = s.getItem() == Items.mushroom_stew;
            boolean food = s.getItem() instanceof ItemFood;
            boolean ok = (type.is("Soupe") && soup)
                    || (type.is("Nourriture") && food)
                    || (type.is("Les deux") && (soup || food));
            if (ok) { count++; if (found < 0) found = i; }
        }
        // option : garder au moins un slot (ne pas vider la dernière soupe)
        if (keepOne.value && count <= 1) return -1;
        return found;
    }

    /** Jette les bols vides de la hotbar (ctrl+Q sur le slot). */
    private void dropBowls(EntityPlayerSP p) {
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.inventory.getStackInSlot(i);
            if (s != null && s.getItem() == Items.bowl) {
                int slotNumber = 36 + i;   // hotbar dans le container joueur
                mc.playerController.windowClick(p.inventoryContainer.windowId, slotNumber, 1, 4, p); // throw stack
            }
        }
    }
}
