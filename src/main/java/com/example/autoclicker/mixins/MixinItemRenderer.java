package com.example.autoclicker.mixins;

import com.example.autoclicker.client.render.ItemTransform;

import net.minecraft.client.renderer.ItemRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class MixinItemRenderer {

    @Inject(method = "transformFirstPersonItem", at = @At("TAIL"))
    private void onTransformTail(float equipProgress, float swingProgress, CallbackInfo ci) {
        ItemTransform.applyFirstPerson(swingProgress);
    }
}
