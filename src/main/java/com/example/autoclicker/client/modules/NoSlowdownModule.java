package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.MinecraftAccess;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemSword;

public class NoSlowdownModule extends Module {

    public static volatile boolean active = false;
    public static volatile boolean cancelSword = true;
    public static volatile boolean cancelBow = true;
    public static volatile boolean cancelEat = true;
    private static volatile double chanceVal = 100;
    private static volatile boolean timerMode = false;

    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "Vanilla", "NCP", "AAC", "Hypixel", "Grim"));
    private final Setting.Bool sword = (Setting.Bool) add(new Setting.Bool("Sword", true));
    private final Setting.Bool bow = (Setting.Bool) add(new Setting.Bool("Bow", true));
    private final Setting.Bool eat = (Setting.Bool) add(new Setting.Bool("Eat", true));
    private final Setting.Number chance = (Setting.Number) add(new Setting.Number("Chance %", 100, 10, 100, 5, true));
    private final Setting.Bool timerBoost = (Setting.Bool) add(new Setting.Bool("Timer", false));

    public NoSlowdownModule() { super("NoSlowdown", Category.COMBAT); }

    @Override public void onEnable() { active = true; push(); }
    @Override public void onDisable() { active = false; MinecraftAccess.setTimerSpeed(1.0F); }

    @Override public void onTick() { push(); }

    private void push() {
        cancelSword = sword.value;
        cancelBow = bow.value;
        cancelEat = eat.value;
        chanceVal = chance.value;
        timerMode = timerBoost.value;

        if (timerMode && mode.is("Timer") && mc.thePlayer != null && mc.thePlayer.isUsingItem()) {
            MinecraftAccess.setTimerSpeed(1.08F);
        } else {
            MinecraftAccess.setTimerSpeed(1.0F);
        }
    }

    public static boolean shouldCancel(EntityPlayer self) {
        if (!active) return false;
        if (Math.random() > chanceVal / 100.0) return false;
        if (self.isUsingItem() && self.getHeldItem() != null) {
            if (self.getHeldItem().getItem() instanceof ItemSword && cancelSword) return true;
            if (self.getHeldItem().getItem() instanceof ItemBow && cancelBow) return true;
            if (cancelEat) return true;
        }
        return false;
    }
}
