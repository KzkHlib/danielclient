package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;

import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

/**
 * Anti debuff : retire les effets de potion négatifs (poison, lenteur, etc.)
 * côté client à chaque tick. NB : effet visuel/prédiction local — le serveur
 * garde l'effet réel selon sa config.
 */
public class AntiDebuffModule extends Module {

    public AntiDebuffModule() {
        super("AntiDebuff", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;
        List<Integer> toRemove = new ArrayList<Integer>();
        for (Object o : mc.thePlayer.getActivePotionEffects()) {
            PotionEffect pe = (PotionEffect) o;
            Potion p = Potion.potionTypes[pe.getPotionID()];
            if (p != null && p.isBadEffect()) toRemove.add(pe.getPotionID());
        }
        for (Integer id : toRemove) {
            mc.thePlayer.removePotionEffectClient(id);
        }
    }
}
