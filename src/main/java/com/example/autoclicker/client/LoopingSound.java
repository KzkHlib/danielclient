package com.example.autoclicker.client;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.util.ResourceLocation;

/** Son en boucle non spatialisé (musique de fond du menu). */
public class LoopingSound extends PositionedSound {

    public LoopingSound(ResourceLocation loc, float vol) {
        super(loc);
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = vol;
        this.pitch = 1.0F;
        this.attenuationType = ISound.AttenuationType.NONE;
    }
}
