package com.example.autoclicker.mixins;

import com.example.autoclicker.client.modules.ReachModule;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.renderer.EntityRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Reach : getMouseOver limite la visée des entités à 3 blocs en survie.
 * On remplace ce plafond par la portée du module (incluant prédiction) et
 * on étend la distance de recherche si la portée dépasse la portée bloc
 * vanilla (4.5).
 *
 * Utilise ReachModule.getEffectiveReach() pour intégrer la prédiction de
 * hitbox (bonus si la cible se déplace vers le joueur).
 */
@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Redirect(method = "getMouseOver",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;getBlockReachDistance()F"))
    private float reach$blockReachDistance(PlayerControllerMP controller) {
        float vanilla = controller.getBlockReachDistance();
        if (!ReachModule.active) return vanilla;
        double eff = ReachModule.getEffectiveReach();
        return Math.max(vanilla, (float) eff);
    }

    @ModifyConstant(method = "getMouseOver", constant = @Constant(doubleValue = 3.0D))
    private double reach$entityCap(double constant) {
        return ReachModule.active ? ReachModule.getEffectiveReach() : constant;
    }
}
