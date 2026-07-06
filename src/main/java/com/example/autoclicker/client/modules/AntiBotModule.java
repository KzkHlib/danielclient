package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

/**
 * AntiBot : détecte les faux joueurs (bots PvP / NPC) pour que KillAura, ESP,
 * Nametags et TargetHUD les ignorent. Heuristiques simples et fiables.
 */
public class AntiBotModule extends Module {

    public static volatile boolean active = false;

    private final Setting.Bool tabCheck =
            (Setting.Bool) add(new Setting.Bool("Absent du tab", true));
    private final Setting.Bool nameCheck =
            (Setting.Bool) add(new Setting.Bool("Nom invalide", true));

    private static AntiBotModule INSTANCE;

    public AntiBotModule() {
        super("AntiBot", Category.COMBAT);
        INSTANCE = this;
    }

    @Override public void onEnable()  { active = true; }
    @Override public void onDisable() { active = false; }

    /** Vrai si l'entité est probablement un bot (à ignorer). */
    public static boolean isBot(Entity e) {
        if (!active || INSTANCE == null) return false;
        if (!(e instanceof EntityPlayer)) return false;
        EntityPlayer p = (EntityPlayer) e;

        if (INSTANCE.nameCheck.value) {
            String n = p.getName();
            if (n == null || n.isEmpty() || n.length() > 16 || !n.matches("[A-Za-z0-9_]{1,16}")) return true;
        }
        if (INSTANCE.tabCheck.value) {
            NetHandlerPlayClient nh = Minecraft.getMinecraft().getNetHandler();
            if (nh != null && nh.getPlayerInfo(p.getUniqueID()) == null) return true; // pas dans la tablist
        }
        return false;
    }
}
