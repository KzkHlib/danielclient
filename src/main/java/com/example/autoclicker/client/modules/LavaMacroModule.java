package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import org.lwjgl.input.Keyboard;

/**
 * Lava macro : à l'appui de la touche, vise le bloc sous l'entité la plus
 * proche, vide un bucket de lave dessus puis reprend instantanément la source.
 *
 * Le bucket de lave fait son propre raytrace au clic droit : on tourne donc la
 * caméra vers le sol sous la cible AVANT d'utiliser l'item, et on étale les
 * étapes sur plusieurs ticks pour que la rotation soit envoyée au serveur avant
 * les packets d'usage (sinon le serveur place la lave au mauvais endroit).
 */
public class LavaMacroModule extends Module {

    private static final int IDLE = 0, AIM = 1, PLACE = 2, PICKUP = 3;

    private final Setting.Number range =
            (Setting.Number) add(new Setting.Number("Portee", 4.5, 1, 6, 0.5, false));
    private final Setting.Bool silent =
            (Setting.Bool) add(new Setting.Bool("Rotation silencieuse", false));

    private int state = IDLE;
    private boolean prevDown = false;
    private int prevSlot = -1;
    private int lavaSlot = -1;
    private BlockPos targetBlock;
    private float savedYaw, savedPitch;

    public LavaMacroModule() {
        super("LavaMacro", Category.COMBAT);
        holdKey = true;            // on gère la touche nous-mêmes (déclencheur)
        key = Keyboard.KEY_G;
    }

    @Override
    public void onDisable() { reset(); }

    @Override
    public void onTick() {
        EntityPlayerSP p = mc.thePlayer;
        if (p == null || mc.theWorld == null) { reset(); return; }

        boolean down = key != 0 && Keyboard.isKeyDown(key) && mc.currentScreen == null;
        if (down && !prevDown && state == IDLE) start();
        prevDown = down;

        switch (state) {
            case AIM:    tickAim(p);    break;
            case PLACE:  tickPlace(p);  break;
            case PICKUP: tickPickup(p); break;
            default: break;
        }
    }

    /** Démarre un cycle : trouve la cible et le bucket de lave. */
    private void start() {
        EntityLivingBase target = nearest();
        if (target == null) { notify("Aucune cible"); return; }
        lavaSlot = findHotbar(Items.lava_bucket);
        if (lavaSlot < 0) { notify("Pas de bucket de lave"); return; }

        targetBlock = new BlockPos(
                Math.floor(target.posX),
                Math.floor(target.posY) - 1,   // bloc sous les pieds
                Math.floor(target.posZ));
        prevSlot = mc.thePlayer.inventory.currentItem;
        savedYaw = mc.thePlayer.rotationYaw;
        savedPitch = mc.thePlayer.rotationPitch;
        state = AIM;
    }

    /** Tick 1 : oriente la caméra vers le sol sous la cible (envoyé ce tick). */
    private void tickAim(EntityPlayerSP p) {
        aimAtBlock(p, targetBlock);
        state = PLACE;
    }

    /** Tick 2 : sélectionne le bucket de lave et le vide (rotation déjà reçue). */
    private void tickPlace(EntityPlayerSP p) {
        aimAtBlock(p, targetBlock);             // maintient la visée
        selectSlot(lavaSlot);
        ItemStack stack = p.inventory.getCurrentItem();
        if (stack != null) {
            mc.playerController.sendUseItem(p, mc.theWorld, stack);
        }
        state = PICKUP;
    }

    /** Tick 3 : reprend la source de lave (même slot, désormais bucket vide). */
    private void tickPickup(EntityPlayerSP p) {
        aimAtBlock(p, targetBlock);
        ItemStack stack = p.inventory.getCurrentItem(); // bucket vide après la pose
        if (stack != null) {
            mc.playerController.sendUseItem(p, mc.theWorld, stack);
        }
        selectSlot(prevSlot);
        if (silent.value) {                     // restaure la vue d'origine
            p.rotationYaw = savedYaw;
            p.rotationPitch = savedPitch;
        }
        reset();
    }

    // ====================== utilitaires ======================

    private EntityLivingBase nearest() {
        EntityLivingBase best = null;
        double bestDist = range.value;
        for (Object o : mc.theWorld.loadedEntityList) {
            if (!(o instanceof EntityLivingBase)) continue;
            EntityLivingBase e = (EntityLivingBase) o;
            if (e == mc.thePlayer || !e.isEntityAlive()) continue;
            double d = mc.thePlayer.getDistanceToEntity(e);
            if (d <= bestDist) { bestDist = d; best = e; }
        }
        return best;
    }

    private int findHotbar(net.minecraft.item.Item item) {
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

    /** Oriente le joueur vers le dessus du bloc cible. */
    private void aimAtBlock(EntityPlayerSP p, BlockPos b) {
        double dx = (b.getX() + 0.5) - p.posX;
        double dy = (b.getY() + 1.0) - (p.posY + p.getEyeHeight());
        double dz = (b.getZ() + 0.5) - p.posZ;
        double distXZ = Math.sqrt(dx * dx + dz * dz);
        p.rotationYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        p.rotationPitch = (float) -Math.toDegrees(Math.atan2(dy, distXZ));
    }

    private void notify(String msg) {
        if (com.example.autoclicker.client.Client.INSTANCE != null) {
            com.example.autoclicker.client.Client.INSTANCE.notifications.add(
                    new com.example.autoclicker.client.Client.Notif("LavaMacro: " + msg, false));
        }
    }

    private void reset() {
        state = IDLE;
        targetBlock = null;
        lavaSlot = -1;
        prevSlot = -1;
    }
}
