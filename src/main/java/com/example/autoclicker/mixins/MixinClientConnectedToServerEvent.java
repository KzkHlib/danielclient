package com.example.autoclicker.mixins;

import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Forge 1.8.9 lance une ClassCastException dans
 * ClientConnectedToServerEvent.<init> quand le NetHandler est encore
 * NetHandlerLoginClient (serveur vanilla/anti-forge). Ce mixin neutralise
 * l'appel à getNetHandler() : si le handler n'est pas un
 * INetHandlerPlayClient, il retourne null (le CHECKCAST suivant accepte
 * les références null) → pas de crash, l'event est posté avec un handler
 * null, ce qui est sûr car aucun listener de base n'utilise handler sans
 * vérification.
 */
@Mixin(value = FMLNetworkEvent.ClientConnectedToServerEvent.class, remap = false)
public class MixinClientConnectedToServerEvent {

    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/NetworkManager;getNetHandler()Lnet/minecraft/network/INetHandler;"),
            remap = false, expect = 1)
    private INetHandler safeGetNetHandler(NetworkManager manager) {
        INetHandler handler = manager.getNetHandler();
        if (handler instanceof INetHandlerPlayClient) {
            return handler;
        }
        return null;
    }
}
