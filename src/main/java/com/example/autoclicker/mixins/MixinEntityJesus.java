package com.example.autoclicker.mixins;

import com.example.autoclicker.client.modules.JesusModule;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityLivingBase.class)
public class MixinEntityJesus {

    @Inject(method = "isInWater", at = @At("RETURN"), cancellable = true)
    private void jesus$inWater(CallbackInfoReturnable<Boolean> cir) {
        if (JesusModule.active) {
            Entity self = (Entity) (Object) this;
            Entity player = net.minecraft.client.Minecraft.getMinecraft().thePlayer;
            if (self.equals(player) && !self.isSneaking()) {
                cir.setReturnValue(false);
            }
        }
    }
}
