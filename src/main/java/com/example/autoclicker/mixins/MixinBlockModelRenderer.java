package com.example.autoclicker.mixins;

import com.example.autoclicker.client.modules.XrayModule;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Xray : opacité 0 = blocs non-minerai invisibles (see-through pur), sinon ils
 * sont rendus mais assombris selon le slider ; les minerais restent full bright.
 * Aucun paramètre tableau ici (évite le bug Mixin 0.7 "[I").
 */
@Mixin(BlockModelRenderer.class)
public class MixinBlockModelRenderer {

    @Inject(method = "renderModel(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/resources/model/IBakedModel;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/BlockPos;Lnet/minecraft/client/renderer/WorldRenderer;Z)Z",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void xray$renderModel(IBlockAccess world, IBakedModel model, IBlockState state,
                                  BlockPos pos, WorldRenderer wr, boolean checkSides,
                                  CallbackInfoReturnable<Boolean> cir) {
        if (!XrayModule.active) {
            XrayModule.CURRENT_ORE.set(Boolean.TRUE);
            return;
        }
        boolean ore = XrayModule.isXrayBlock(state.getBlock());
        XrayModule.CURRENT_ORE.set(ore);
        if (!ore && XrayModule.opacityFactor <= 0f) {
            cir.setReturnValue(Boolean.FALSE); // opacité 0 : bloc invisible
        }
    }

    @Redirect(method = {"renderModelStandardQuads", "renderModelAmbientOcclusionQuads"},
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/WorldRenderer;putBrightness4(IIII)V"),
            require = 0)
    private void xray$putBrightness4(WorldRenderer wr, int b1, int b2, int b3, int b4) {
        if (!XrayModule.active) {
            wr.putBrightness4(b1, b2, b3, b4);
            return;
        }
        if (XrayModule.CURRENT_ORE.get()) {
            int full = 0x00F000F0; // minerai = lightmap max
            wr.putBrightness4(full, full, full, full);
        } else {
            float f = XrayModule.opacityFactor;
            wr.putBrightness4(dim(b1, f), dim(b2, f), dim(b3, f), dim(b4, f));
        }
    }

    /** Atténue un lightmap packé (sky<<16 | block) par un facteur 0..1. */
    private static int dim(int b, float f) {
        int sky = (b >> 16) & 0xFFFF;
        int block = b & 0xFFFF;
        sky = (int) (sky * f);
        block = (int) (block * f);
        return (sky << 16) | block;
    }
}
