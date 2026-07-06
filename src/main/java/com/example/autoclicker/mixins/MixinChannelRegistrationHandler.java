package com.example.autoclicker.mixins;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.fml.common.network.handshake.ChannelRegistrationHandler;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.netty.channel.ChannelHandlerContext;

/**
 * Forge 1.8.9 émet une ClassCastException dans
 * CustomPacketRegistrationEvent quand un packet REGISTER arrive sur le
 * pipeline pendant la phase LOGIN (NetHandlerLoginClient au lieu de
 * NetHandlerPlayClient). Cela arrive quand le serveur ne répond pas au
 * handshake moddé (serveur vanilla ou anti-forge).
 *
 * Ce mixin détecte le cas et skip proprement l'event.
 */
@Mixin(value = ChannelRegistrationHandler.class, remap = false)
public class MixinChannelRegistrationHandler {

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true, remap = false)
    private void skipIfNotPlay(ChannelHandlerContext ctx, FMLProxyPacket msg, CallbackInfo ci) {
        NetworkManager mgr = msg.getOrigin();
        if (mgr == null) return;
        Object handler = mgr.getNetHandler();
        if (handler != null && !(handler instanceof NetHandlerPlayClient)) {
            ci.cancel();
        }
    }
}
