package com.example.autoclicker.mixins;

import com.example.autoclicker.client.modules.BlinkModule;

import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepte les paquets sortants pour Blink : retient les C03 quand actif.
 */
@Mixin(NetworkManager.class)
public class MixinNetworkManager {

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet packet, CallbackInfo ci) {
        if (BlinkModule.shouldHold(packet)) {
            BlinkModule.hold(packet);
            ci.cancel();
        }
    }
}
