package com.example.autoclicker.mixins;

import com.example.autoclicker.client.modules.XrayModule;

import net.minecraft.block.Block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Xray : rend les blocs hors whitelist non opaques pour que les faces des
 * minerais adjacents soient dessinées (on voit les minerais embarqués).
 */
@Mixin(Block.class)
public class MixinBlock {

    @Inject(method = "isOpaqueCube", at = @At("HEAD"), cancellable = true)
    private void xray$isOpaqueCube(CallbackInfoReturnable<Boolean> cir) {
        if (XrayModule.active && !XrayModule.isXrayBlock((Block) (Object) this)) {
            cir.setReturnValue(Boolean.FALSE);
        }
    }
}
