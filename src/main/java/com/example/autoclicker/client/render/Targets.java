package com.example.autoclicker.client.render;

import com.example.autoclicker.client.FriendManager;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;

/** Filtrage commun des entités ciblées (ESP, Tracers, Nametags, AimAssist). */
public final class Targets {

    private Targets() {}

    /** Le joueur est-il dans ta liste d'amis ? */
    public static boolean isFriend(EntityLivingBase e) {
        return e instanceof EntityPlayer && FriendManager.isFriend(e.getName());
    }

    /** Filtre simple ("Mobs", "Joueurs", "Tous") — amis inclus. */
    public static boolean matches(EntityLivingBase e, String mode) {
        return matches(e, mode, false);
    }

    /** Filtre + option d'exclusion des amis (pour l'AimAssist). */
    public static boolean matches(EntityLivingBase e, String mode, boolean excludeFriends) {
        Minecraft mc = Minecraft.getMinecraft();
        if (e == mc.thePlayer) return false;
        if (e.isDead || !e.isEntityAlive()) return false;
        if (excludeFriends && isFriend(e)) return false;
        if (com.example.autoclicker.client.modules.AntiBotModule.isBot(e)) return false;
        if ("Joueurs".equalsIgnoreCase(mode)) {
            return e instanceof EntityPlayer;
        }
        if ("Mobs".equalsIgnoreCase(mode)) {
            return isMob(e);
        }
        // "Tous"
        return e instanceof EntityPlayer || isMob(e);
    }

    private static boolean isMob(EntityLivingBase e) {
        return e instanceof IMob || e instanceof IAnimals
                || e instanceof EntityAnimal || e instanceof EntityWither
                || e instanceof EntityDragon;
    }

    /** Couleur ARGB selon le type (bleu ami, rouge hostile, vert passif, blanc joueur). */
    public static int color(EntityLivingBase e) {
        if (isFriend(e)) return 0xFF5CB8FF; // ami = accent bleu
        if (e instanceof EntityPlayer) return 0xFFFFFFFF;
        if (e instanceof IMob)         return 0xFFE74C3C; // hostile
        return 0xFF2ECC71;                                // passif
    }
}
