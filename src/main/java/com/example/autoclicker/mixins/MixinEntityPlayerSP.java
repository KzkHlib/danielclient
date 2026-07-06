package com.example.autoclicker.mixins;

import com.example.autoclicker.client.modules.NoSlowdownModule;
import com.example.autoclicker.client.modules.ScaffoldModule;

import net.minecraft.client.entity.EntityPlayerSP;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {

    @Unique private float scaffold$savedYaw;
    @Unique private float scaffold$savedPitch;

    @Redirect(method = "onLivingUpdate",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/entity/EntityPlayerSP;isUsingItem()Z"))
    private boolean noslow$isUsingItem(EntityPlayerSP self) {
        if (NoSlowdownModule.shouldCancel(self)) return false;
        return self.isUsingItem();
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"))
    private void scaffold$preUpdate(CallbackInfo ci) {
        if (ScaffoldModule.active && ScaffoldModule.useSilent) {
            EntityPlayerSP self = (EntityPlayerSP) (Object) this;
            scaffold$savedYaw = self.rotationYaw;
            scaffold$savedPitch = self.rotationPitch;
            self.rotationYaw = ScaffoldModule.silentYaw;
            self.rotationPitch = ScaffoldModule.silentPitch;
        }
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("RETURN"))
    private void scaffold$postUpdate(CallbackInfo ci) {
        if (ScaffoldModule.active && ScaffoldModule.useSilent) {
            EntityPlayerSP self = (EntityPlayerSP) (Object) this;
            self.rotationYaw = scaffold$savedYaw;
            self.rotationPitch = scaffold$savedPitch;
        }
    }
}
