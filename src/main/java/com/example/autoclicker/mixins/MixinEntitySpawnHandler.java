package com.example.autoclicker.mixins;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.internal.EntitySpawnHandler;
import net.minecraftforge.fml.common.network.internal.FMLMessage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Forge 1.8.9 EntitySpawnHandler n'a PAS de null-check sur getWorldClient().
 * Quand un EntityAdjustMessage arrive via le pipeline netty AVANT que le
 * WorldClient soit assigné (race condition au join), FMLClientHandler
 * .instance().getWorldClient() retourne null → NPE.
 *
 * Ce mixin ajoute un guard : si le monde n'est pas prêt, on skip
 * poliment l'ajustement.
 */
@Mixin(EntitySpawnHandler.class)
public class MixinEntitySpawnHandler {

    @Inject(method = "adjustEntity", at = @At("HEAD"), cancellable = true, remap = false)
    private void guardAdjustEntity(FMLMessage.EntityAdjustMessage msg, CallbackInfo ci) {
        WorldClient wc = FMLClientHandler.instance().getWorldClient();
        if (wc == null) {
            ci.cancel();
        }
    }
}
