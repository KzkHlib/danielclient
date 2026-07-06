package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;

/**
 * Reach avancé : étend la portée d'attaque avec hitbox prediction,
 * reach dynamique (varie entre min et max) et randomisation.
 * Appliqué par MixinEntityRenderer dans getMouseOver.
 *
 * Hitbox prediction : augmente temporairement la portée si la cible
 * se déplace vers le joueur (simule l'anticipation de hitbox).
 */
public class ReachModule extends Module {

    public static volatile boolean active = false;
    public static volatile double reach = 3.5;

    // Portée prédite (augmentée si la cible avance vers nous)
    private static volatile double predictedReach = 3.5;
    private static volatile boolean usePrediction = false;

    private final Setting.Number rangeMin =
            (Setting.Number) add(new Setting.Number("Reach min", 3.0, 3, 6, 0.1, false));
    private final Setting.Number rangeMax =
            (Setting.Number) add(new Setting.Number("Reach max", 3.5, 3, 6, 0.1, false));
    private final Setting.Bool prediction =
            (Setting.Bool) add(new Setting.Bool("Prediction hitbox", true));
    private final Setting.Number predictionBonus =
            (Setting.Number) add(new Setting.Number("Bonus prediction", 0.3, 0, 1, 0.1, false));
    private final Setting.Bool dynamicReach =
            (Setting.Bool) add(new Setting.Bool("Reach dynamique", true));
    private final Setting.Number randomReach =
            (Setting.Number) add(new Setting.Number("Randomisation", 10, 0, 40, 5, true));

    // Cible actuelle pour la prédiction
    private EntityLivingBase currentTarget;

    public ReachModule() {
        super("Reach", Category.COMBAT);
    }

    @Override
    public String arrayListSuffix() {
        return String.format("%.1f-%.1f", rangeMin.value, rangeMax.value);
    }

    @Override public void onEnable() { active = true; push(); }
    @Override public void onDisable() { active = false; currentTarget = null; }

    @Override
    public void onTick() {
        push();
        updateTarget();
    }

    private void push() {
        double min = Math.min(rangeMin.value, rangeMax.value);
        double max = Math.max(rangeMin.value, rangeMax.value);

        if (dynamicReach.value) {
            // Reach dynamique : valeur aléatoire entre min et max
            double variance = (max - min) * (randomReach.value / 100.0) * (Math.random() - 0.5) * 2.0;
            double base = min + (max - min) * Math.random();
            reach = Math.max(3.0, Math.min(6.0, base + variance));
        } else {
            reach = max;
        }

        usePrediction = prediction.value;
        // Si prédiction active, on ajoute le bonus prédit
        if (usePrediction && currentTarget != null) {
            predictedReach = reach + computePredictionBonus();
        } else {
            predictedReach = reach;
        }
    }

    /**
     * Calcule un bonus de portée basé sur le mouvement de la cible.
     * Si la cible se déplace vers nous, on anticipe sa hitbox.
     */
    private double computePredictionBonus() {
        if (mc.thePlayer == null || currentTarget == null) return 0.0;
        double bonus = predictionBonus.value;

        // Vélocité relative de la cible par rapport au joueur
        double vx = currentTarget.posX - currentTarget.prevPosX;
        double vz = currentTarget.posZ - currentTarget.prevPosZ;

        // Vecteur joueur -> cible
        double dx = currentTarget.posX - mc.thePlayer.posX;
        double dz = currentTarget.posZ - mc.thePlayer.posZ;
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < 0.1) return 0.0;

        // Produit scalaire : positif = cible s'approche, négatif = s'éloigne
        double dot = (vx * dx + vz * dz) / dist;
        if (dot > 0) {
            // S'approche : bonus proportionnel à la vélocité relative
            double speed = Math.sqrt(vx * vx + vz * vz);
            return Math.min(bonus, speed * 2.0);
        }
        return 0.0;
    }

    /** Trouve la cible potentielle pour la prédiction. */
    private void updateTarget() {
        if (mc.theWorld == null || mc.thePlayer == null) {
            currentTarget = null;
            return;
        }
        if (currentTarget != null && (currentTarget.isDead || !currentTarget.isEntityAlive()
                || mc.thePlayer.getDistanceToEntity(currentTarget) > rangeMax.value + 2.0)) {
            currentTarget = null;
        }
        if (currentTarget == null) {
            // Prend la première entité valide à portée (pour la prédiction)
            for (Object o : mc.theWorld.loadedEntityList) {
                if (!(o instanceof EntityLivingBase)) continue;
                EntityLivingBase e = (EntityLivingBase) o;
                if (e == mc.thePlayer || e.isDead || !e.isEntityAlive()) continue;
                if (mc.thePlayer.getDistanceToEntity(e) <= rangeMax.value + 2.0) {
                    currentTarget = e;
                    break;
                }
            }
        }
    }

    /**
     * Retourne la portée actuelle (incluant la prédiction si active).
     * Appelé par MixinEntityRenderer via les champs statiques.
     */
    public static double getEffectiveReach() {
        if (!active) return 3.0;
        return usePrediction ? Math.max(reach, predictedReach) : reach;
    }
}
