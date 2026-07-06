package com.example.autoclicker.mixins;

import com.example.autoclicker.client.modules.ChamsModule;

import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RendererLivingEntity.class)
public class MixinRenderChams {

    @Inject(method = "doRender", at = @At("HEAD"))
    private void chams$preRender(EntityLivingBase entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        if (!ChamsModule.active) return;
        if (ChamsModule.throughWalls) {
            GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
            GL11.glPolygonOffset(1.0F, -1100000.0F);
        }
    }

    @Inject(method = "doRender", at = @At("RETURN"))
    private void chams$postRender(EntityLivingBase entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        if (!ChamsModule.active) return;
        if (ChamsModule.throughWalls) {
            GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
            GL11.glPolygonOffset(1.0F, 1100000.0F);
        }
    }
}
