package com.example.autoclicker.mixins;

import com.example.autoclicker.client.gui.CustomMainMenu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MusicTicker;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Coupe la musique vanilla du menu quand notre menu custom est affiché (on
 * garde notre propre musique de fond). En jeu, la musique vanilla reste normale.
 */
@Mixin(MusicTicker.class)
public class MixinMusicTicker {

    @Shadow private ISound currentMusic;

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void fb$noMenuMusic(CallbackInfo ci) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen instanceof CustomMainMenu) {
            if (currentMusic != null) {
                mc.getSoundHandler().stopSound(currentMusic);
                currentMusic = null;
            }
            ci.cancel();   // empêche vanilla de relancer une musique de menu
        }
    }
}
