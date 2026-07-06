package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;

/**
 * AutoWater : quand le joueur prend feu (dégâts de feu/lave), pose un bucket
 * d'eau sous ses pieds pour s'éteindre, puis reprend la source.
 *
 * Comme pour la lave, le bucket fait son raytrace au clic droit : on vise le
 * sol sous les pieds, puis on étale pose/reprise sur plusieurs ticks pour que
 * la rotation parte avant les packets d'usage.
 */
public class AutoWaterModule extends Module {

    private static final int IDLE = 0, AIM = 1, PLACE = 2, WAIT = 3, PICKUP = 4;

    private final Setting.Bool pickup =
            (Setting.Bool) add(new Setting.Bool("Reprendre eau", true));
    private final Setting.Number pickupDelay =
            (Setting.Number) add(new Setting.Number("Delai reprise", 4, 1, 20, 1, true));
    private final Setting.Bool silent =
            (Setting.Bool) add(new Setting.Bool("Rotation silencieuse", true));

    private int state = IDLE;
    private int waitTicks = 0;
    private int cooldown = 0;
    private int prevSlot = -1;
    private int waterSlot = -1;
    private BlockPos targetBlock;
    private float savedYaw, savedPitch;

    public AutoWaterModule() {
        super("AutoWater", Category.PLAYER);
    }

    @Override
    public void onDisable() { reset(); cooldown = 0; }

    @Override
    public void onTick() {
        EntityPlayerSP p = mc.thePlayer;
        if (p == null || mc.theWorld == null) { reset(); return; }
        if (cooldown > 0) cooldown--;

        switch (state) {
            case IDLE:   tryStart(p);   break;
            case AIM:    tickAim(p);    break;
            case PLACE:  tickPlace(p);  break;
            case WAIT:   tickWait(p);   break;
            case PICKUP: tickPickup(p); break;
            default: break;
        }
    }

    /** Déclenche un cycle si on brûle et qu'on a un bucket d'eau. */
    private void tryStart(EntityPlayerSP p) {
        if (cooldown > 0) return;
        if (!p.isBurning()) return;                 // prend des dégâts de feu
        waterSlot = findHotbar(Items.water_bucket);
        if (waterSlot < 0) return;                  // pas de bucket d'eau : on ignore

        targetBlock = new BlockPos(
                Math.floor(p.posX),
                Math.floor(p.posY) - 1,             // bloc sous les pieds
                Math.floor(p.posZ));
        prevSlot = p.inventory.currentItem;
        savedYaw = p.rotationYaw;
        savedPitch = p.rotationPitch;
        state = AIM;
    }

    private void tickAim(EntityPlayerSP p) {
        aimAtBlock(p, targetBlock);
        state = PLACE;
    }

    private void tickPlace(EntityPlayerSP p) {
        aimAtBlock(p, targetBlock);
        selectSlot(waterSlot);
        ItemStack stack = p.inventory.getCurrentItem();
        if (stack != null) mc.playerController.sendUseItem(p, mc.theWorld, stack);

        if (pickup.value) {
            waitTicks = (int) pickupDelay.value;
            state = WAIT;
        } else {
            finish(p);
        }
    }

    private void tickWait(EntityPlayerSP p) {
        if (silent.value) { p.rotationYaw = savedYaw; p.rotationPitch = savedPitch; }
        if (--waitTicks <= 0) state = PICKUP;
    }

    private void tickPickup(EntityPlayerSP p) {
        aimAtBlock(p, targetBlock);
        ItemStack stack = p.inventory.getCurrentItem(); // bucket vide après la pose
        if (stack != null) mc.playerController.sendUseItem(p, mc.theWorld, stack);
        finish(p);
    }

    private void finish(EntityPlayerSP p) {
        selectSlot(prevSlot);
        if (silent.value) { p.rotationYaw = savedYaw; p.rotationPitch = savedPitch; }
        reset();
        cooldown = 10;                              // anti-spam (~0.5 s)
    }

    // ====================== utilitaires ======================

    private int findHotbar(Item item) {
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.thePlayer.inventory.getStackInSlot(i);
            if (s != null && s.getItem() == item) return i;
        }
        return -1;
    }

    private void selectSlot(int slot) {
        if (slot < 0) return;
        if (mc.thePlayer.inventory.currentItem != slot) {
            mc.thePlayer.inventory.currentItem = slot;
            if (mc.getNetHandler() != null) {
                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(slot));
            }
        }
    }

    private void aimAtBlock(EntityPlayerSP p, BlockPos b) {
        double dx = (b.getX() + 0.5) - p.posX;
        double dy = (b.getY() + 1.0) - (p.posY + p.getEyeHeight());
        double dz = (b.getZ() + 0.5) - p.posZ;
        double distXZ = Math.sqrt(dx * dx + dz * dz);
        p.rotationYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        p.rotationPitch = (float) -Math.toDegrees(Math.atan2(dy, distXZ));
    }

    private void reset() {
        state = IDLE;
        targetBlock = null;
        waterSlot = -1;
        prevSlot = -1;
        waitTicks = 0;
    }
}
