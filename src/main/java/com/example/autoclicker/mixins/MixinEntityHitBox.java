package com.example.autoclicker.mixins;

import com.example.autoclicker.client.modules.HitBoxModule;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityLivingBase.class)
public class MixinEntityHitBox {

    @Inject(method = "getCollisionBorderSize", at = @At("RETURN"), cancellable = true)
    private void hitbox$expand(CallbackInfoReturnable<Float> cir) {
        if (!HitBoxModule.active) return;
        Entity self = (Entity) (Object) this;
        Entity player = net.minecraft.client.Minecraft.getMinecraft().thePlayer;
        if (self.equals(player)) return;

        if (HitBoxModule.smartMode) {
            net.minecraft.util.MovingObjectPosition mo = net.minecraft.client.Minecraft.getMinecraft().objectMouseOver;
            if (mo == null || mo.entityHit != self) return;
        }

        cir.setReturnValue(cir.getReturnValue() + HitBoxModule.expandX);
    }

    @Inject(method = "getEyeHeight", at = @At("RETURN"), cancellable = true)
    private void hitbox$eyeHeight(CallbackInfoReturnable<Float> cir) {
        if (!HitBoxModule.active) return;
        Entity self = (Entity) (Object) this;
        Entity player = net.minecraft.client.Minecraft.getMinecraft().thePlayer;
        if (self.equals(player)) return;

        if (HitBoxModule.smartMode) {
            net.minecraft.util.MovingObjectPosition mo = net.minecraft.client.Minecraft.getMinecraft().objectMouseOver;
            if (mo == null || mo.entityHit != self) return;
        }

        cir.setReturnValue(cir.getReturnValue() + HitBoxModule.expandY);
    }
}
