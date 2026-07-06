package com.example.autoclicker.mixins;

import com.example.autoclicker.client.modules.VelocityModule;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {

    @Inject(method = "handleEntityVelocity", at = @At("HEAD"), cancellable = true)
    private void velocity$handle(S12PacketEntityVelocity packet, CallbackInfo ci) {
        if (!VelocityModule.active) return;
        EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
        if (p == null || packet.getEntityID() != p.getEntityId()) return;

        double mx = packet.getMotionX() / 8000.0;
        double my = packet.getMotionY() / 8000.0;
        double mz = packet.getMotionZ() / 8000.0;

        double[] result = VelocityModule.apply(mx, my, mz);
        mx = result[0];
        my = result[1];
        mz = result[2];

        if (VelocityModule.delayed) {
            VelocityModule.schedule(mx, my, mz, VelocityModule.delayTicks);
        } else {
            p.motionX = mx;
            p.motionY = my;
            p.motionZ = mz;
        }
        ci.cancel();
    }
}
