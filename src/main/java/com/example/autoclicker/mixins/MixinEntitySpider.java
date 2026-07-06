package com.example.autoclicker.mixins;

import com.example.autoclicker.client.modules.SpiderModule;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityLivingBase.class)
public class MixinEntitySpider {

    @Inject(method = "isOnLadder", at = @At("RETURN"), cancellable = true)
    private void spider$onLadder(CallbackInfoReturnable<Boolean> cir) {
        if (SpiderModule.active) {
            Entity self = (Entity) (Object) this;
            Entity player = net.minecraft.client.Minecraft.getMinecraft().thePlayer;
            if (self.equals(player) && self.isCollidedHorizontally) {
                cir.setReturnValue(true);
            }
        }
    }
}
