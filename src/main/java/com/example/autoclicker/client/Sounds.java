package com.example.autoclicker.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

/** Sons d'UI : toggle de module, clics, + musique de fond du menu. */
public final class Sounds {

    private Sounds() {}

    public static volatile boolean enabled = true;
    private static ISound menuMusic;

    /** Lance la musique du menu en boucle (assets/fullbright/sounds/menu.ogg). */
    public static void playMenuMusic() {
        if (!enabled) return;
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.getSoundHandler() == null) return;
            if (menuMusic != null && mc.getSoundHandler().isSoundPlaying(menuMusic)) return;
            menuMusic = new LoopingSound(new ResourceLocation("fullbright", "menu_music"), 0.45f);
            mc.getSoundHandler().playSound(menuMusic);
        } catch (Throwable ignored) {
        }
    }

    /** Coupe la musique du menu (à l'entrée en jeu). */
    public static void stopMenuMusic() {
        try {
            if (menuMusic != null) {
                Minecraft.getMinecraft().getSoundHandler().stopSound(menuMusic);
                menuMusic = null;
            }
        } catch (Throwable ignored) {
        }
    }

    /** Activation/désactivation d'un module (pitch plus haut = on). */
    public static void toggle(boolean on) {
        play("gui.button.press", on ? 1.35f : 0.85f);
    }

    /** Petit clic d'interface. */
    public static void click() {
        play("gui.button.press", 1.1f);
    }

    private static void play(String name, float pitch) {
        if (!enabled) return;
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.getSoundHandler() != null) {
                mc.getSoundHandler().playSound(
                        PositionedSoundRecord.create(new ResourceLocation(name), pitch));
            }
        } catch (Throwable ignored) {
        }
    }
}
