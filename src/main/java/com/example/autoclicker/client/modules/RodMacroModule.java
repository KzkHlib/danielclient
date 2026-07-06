package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Client;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

/**
 * RodMacro : envoie une rod juste apres un hit puis revient a l'epee.
 */
public class RodMacroModule extends Module {

    private static final int IDLE = 0, WAIT_ROD = 1, THROW_ROD = 2, WAIT_BACK = 3, BACK = 4;

    private final Setting.Mode targets =
            (Setting.Mode) add(new Setting.Mode("Cibles", 0, "Joueurs", "Tous"));
    private final Setting.Number range =
            (Setting.Number) add(new Setting.Number("Portee", 5.0, 2.0, 8.0, 0.5, false));
    private final Setting.Number fov =
            (Setting.Number) add(new Setting.Number("FOV", 130, 10, 360, 5, true));
    private final Setting.Number delayRod =
            (Setting.Number) add(new Setting.Number("Delai rod ticks", 1, 0, 6, 1, true));
    private final Setting.Number delayBack =
            (Setting.Number) add(new Setting.Number("Retour ticks", 1, 0, 8, 1, true));
    private final Setting.Number cooldown =
            (Setting.Number) add(new Setting.Number("Cooldown ms", 230, 0, 1000, 10, true));
    private final Setting.Number rodSlot =
            (Setting.Number) add(new Setting.Number("Slot rod", 0, 0, 9, 1, true));
    private final Setting.Number swordSlot =
            (Setting.Number) add(new Setting.Number("Slot sword", 0, 0, 9, 1, true));
    private final Setting.Bool requireSword =
            (Setting.Bool) add(new Setting.Bool("Epee requise", true));
    private final Setting.Bool switchBack =
            (Setting.Bool) add(new Setting.Bool("Retour sword", true));
    private final Setting.Bool swing =
            (Setting.Bool) add(new Setting.Bool("Swing rod", true));
    private final Setting.Bool cancelIfBusy =
            (Setting.Bool) add(new Setting.Bool("Annuler si occupe", true));
    private final Setting.Bool notifyFail =
            (Setting.Bool) add(new Setting.Bool("Notif manque rod", false));

    private int state = IDLE;
    private int ticks = 0;
    private int rod = -1;
    private int sword = -1;
    private int previous = -1;
    private long lastThrow = 0L;

    public RodMacroModule() {
        super("RodMacro", Category.COMBAT);
    }

    @Override
    public String arrayListSuffix() {
        return ((int) delayRod.value) + "/" + ((int) delayBack.value) + "t";
    }

    @Override
    public void onDisable() {
        reset();
    }

    @Override
    public void onAttackEntity(AttackEntityEvent e) {
        if (mc.thePlayer == null || mc.theWorld == null || mc.currentScreen != null) return;
        if (cancelIfBusy.value && state != IDLE) return;
        if (!(e.target instanceof EntityLivingBase)) return;
        if (!validTarget(e.target)) return;
        if (requireSword.value && !holdingSword()) return;
        long now = System.currentTimeMillis();
        if (now - lastThrow < cooldown.value) return;

        rod = findRodSlot();
        if (rod < 0) {
            if (notifyFail.value) notify("pas de rod");
            return;
        }

        previous = mc.thePlayer.inventory.currentItem;
        sword = findSwordSlot();
        ticks = (int) delayRod.value;
        state = ticks <= 0 ? THROW_ROD : WAIT_ROD;
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null || mc.theWorld == null || mc.currentScreen != null) {
            reset();
            return;
        }

        switch (state) {
            case WAIT_ROD:
                if (--ticks <= 0) state = THROW_ROD;
                break;
            case THROW_ROD:
                throwRod();
                ticks = (int) delayBack.value;
                state = ticks <= 0 ? BACK : WAIT_BACK;
                break;
            case WAIT_BACK:
                if (--ticks <= 0) state = BACK;
                break;
            case BACK:
                if (switchBack.value) selectSlot(backSlot());
                reset();
                break;
            default:
                break;
        }
    }

    private void throwRod() {
        selectSlot(rod);
        ItemStack stack = mc.thePlayer.inventory.getCurrentItem();
        if (stack != null && stack.getItem() == Items.fishing_rod) {
            mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, stack);
            if (swing.value) mc.thePlayer.swingItem();
            lastThrow = System.currentTimeMillis();
        }
    }

    private boolean validTarget(Entity e) {
        if (targets.is("Joueurs") && !(e instanceof EntityPlayer)) return false;
        if (mc.thePlayer.getDistanceToEntity(e) > range.value) return false;
        if (fov.value >= 360) return true;
        float diff = Math.abs(wrapAngle(yawTo(e) - mc.thePlayer.rotationYaw));
        return diff <= fov.value / 2.0;
    }

    private boolean holdingSword() {
        ItemStack held = mc.thePlayer.getHeldItem();
        return held != null && held.getItem() instanceof ItemSword;
    }

    private int findRodSlot() {
        int forced = (int) rodSlot.value;
        if (forced > 0) {
            int slot = forced - 1;
            ItemStack s = mc.thePlayer.inventory.getStackInSlot(slot);
            return s != null && s.getItem() == Items.fishing_rod ? slot : -1;
        }
        return findHotbar(Items.fishing_rod);
    }

    private int findSwordSlot() {
        int forced = (int) swordSlot.value;
        if (forced > 0) {
            int slot = forced - 1;
            ItemStack s = mc.thePlayer.inventory.getStackInSlot(slot);
            return s != null && s.getItem() instanceof ItemSword ? slot : -1;
        }
        if (previous >= 0) {
            ItemStack s = mc.thePlayer.inventory.getStackInSlot(previous);
            if (s != null && s.getItem() instanceof ItemSword) return previous;
        }
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.thePlayer.inventory.getStackInSlot(i);
            if (s != null && s.getItem() instanceof ItemSword) return i;
        }
        return previous;
    }

    private int findHotbar(Item item) {
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.thePlayer.inventory.getStackInSlot(i);
            if (s != null && s.getItem() == item) return i;
        }
        return -1;
    }

    private int backSlot() {
        return sword >= 0 ? sword : previous;
    }

    private void selectSlot(int slot) {
        if (slot < 0 || slot > 8 || mc.thePlayer.inventory.currentItem == slot) return;
        mc.thePlayer.inventory.currentItem = slot;
        if (mc.getNetHandler() != null) {
            mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(slot));
        }
    }

    private float yawTo(Entity e) {
        double dx = e.posX - mc.thePlayer.posX;
        double dz = e.posZ - mc.thePlayer.posZ;
        return (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
    }

    private float wrapAngle(float a) {
        while (a <= -180f) a += 360f;
        while (a > 180f) a -= 360f;
        return a;
    }

    private void notify(String msg) {
        if (Client.INSTANCE != null) Client.INSTANCE.notifications.add(new Client.Notif("RodMacro: " + msg, false));
    }

    private void reset() {
        state = IDLE;
        ticks = 0;
        rod = -1;
        sword = -1;
        previous = -1;
    }
}
