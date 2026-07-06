package com.example.autoclicker.client.render;

import com.example.autoclicker.client.modules.AnimationsModule;
import com.example.autoclicker.client.modules.ViewModelModule;

import net.minecraft.client.renderer.GlStateManager;

/**
 * Animations d'item fluides et stylées, appliquées en additif
 * sur la transform vanilla (TAIL du mixin).
 *
 * Chaque style combine :
 *  - un idle continu (léger mouvement permanent)
 *  - un effet de swing (déclenché par swingProgress > 0)
 *  - des animations continues optionnelles (spin, helico...)
 */
public final class ItemTransform {

    private ItemTransform() {}

    public static void applyFirstPerson(float swingProgress) {
        if (AnimationsModule.active && AnimationsModule.style != AnimationsModule.VANILLA) {
            animation(swingProgress);
        }
        if (ViewModelModule.active) viewModel();
    }

    // ======================== ViewModel ========================

    private static void viewModel() {
        GlStateManager.translate(ViewModelModule.posX, ViewModelModule.posY, ViewModelModule.posZ);
        if (ViewModelModule.scale != 1f) {
            GlStateManager.scale(ViewModelModule.scale, ViewModelModule.scale, ViewModelModule.scale);
        }
        if (ViewModelModule.rotX != 0f) GlStateManager.rotate(ViewModelModule.rotX, 1f, 0f, 0f);
        if (ViewModelModule.rotY != 0f) GlStateManager.rotate(ViewModelModule.rotY, 0f, 1f, 0f);
        if (ViewModelModule.rotZ != 0f) GlStateManager.rotate(ViewModelModule.rotZ, 0f, 0f, 1f);
    }

    // ======================== Animations ========================

    private static float t = 0f;
    private static long lastNanos = 0L;

    private static void animation(float swing) {
        // delta time
        long now = System.nanoTime();
        if (lastNanos == 0L) lastNanos = now;
        float dt = (now - lastNanos) / 1e9f;
        lastNanos = now;
        if (dt > 0.05f) dt = 0.05f;
        t += dt;

        // courbe de swing : 0→1→0 en cloche lissée
        float s = (float) Math.sin(swing * Math.PI);
        // ease out quad pour l'attaque : 0→1 en douceur
        float e = swing * (2 - swing);

        switch (AnimationsModule.style) {
            case AnimationsModule.OLD17:
                old17(s, e);
                break;
            case AnimationsModule.SLIDE:
                slide(s, e);
                break;
            case AnimationsModule.SWIRL:
                swirl(s, e);
                break;
            case AnimationsModule.SPIN:
                spin(s, e);
                break;
            case AnimationsModule.HELICO:
                helico(s, e);
                break;
            case AnimationsModule.DRUNK:
                drunk(s, e);
                break;
            case AnimationsModule.SCREW:
                screw(s, e);
                break;
            case AnimationsModule.RX:
                rx(s, e);
                break;
            case AnimationsModule.FLUX:
                flux(s, e);
                break;
            case AnimationsModule.AVATAR:
                avatar(s, e);
                break;
            case AnimationsModule.INTERIA:
                interia(s, e);
                break;
        }
    }

    // -- utils --

    /** Fait tourner autour du centre de l'item (0.5, 0.5, 0.5). */
    private static void spinCentered(float ang, float ax, float ay, float az) {
        GlStateManager.translate(0.5f, 0.5f, 0.5f);
        GlStateManager.rotate(ang, ax, ay, az);
        GlStateManager.translate(-0.5f, -0.5f, -0.5f);
    }

    /** Oscillation sinusoïdale (amplitude, fréquence, phase). */
    private static float osc(float amp, float freq, float phase) {
        return (float) Math.sin(t * freq + phase) * amp;
    }

    // ======================== STYLES ========================

    // ---- 1.7 : recul + tilt, classique ----
    private static void old17(float s, float e) {
        idleBob(0.012f);
        GlStateManager.translate(0f, 0f, s * 0.12f);
        GlStateManager.rotate(s * -18f, 1f, 0f, 0f);
    }

    // ---- Slide : glisse latérale fluide ----
    private static void slide(float s, float e) {
        idleSway(0.015f, 0.01f);
        GlStateManager.translate(s * -0.18f, s * 0.04f, s * 0.10f);
        GlStateManager.rotate(s * 14f, 0f, 1f, 0f);
        GlStateManager.rotate(s * 4f, 0f, 0f, 1f);
    }

    // ---- Swirl : rotation élégante ----
    private static void swirl(float s, float e) {
        idleSway(0.008f, 0.006f);
        float ang = s * 45f;
        spinCentered(ang, 0f, 0f, 1f);
        GlStateManager.translate(0f, s * 0.10f, s * 0.06f);
    }

    // ---- Spin : rotation continue sur Z ----
    private static void spin(float s, float e) {
        float sp = (float) ((t * AnimationsModule.spinSpeed) % 360.0);
        // pulsation au rythme du swing
        float pulse = 1f + s * 0.5f;
        spinCentered(sp * pulse, 0f, 0f, 1f);
        // léger recul sur le swing
        GlStateManager.translate(0f, 0f, s * 0.06f);
    }

    // ---- Helico : tourne à plat + inclinaison ----
    private static void helico(float s, float e) {
        float sp = (float) ((t * AnimationsModule.spinSpeed) % 360.0);
        spinCentered(sp, 0f, 1f, 0f);
        GlStateManager.rotate(75f, 1f, 0f, 0f);
        GlStateManager.translate(0f, 0.15f + s * 0.06f, 0f);
    }

    // ---- Drunk : wobble smooth ----
    private static void drunk(float s, float e) {
        float wobble = osc(18f, 7f, 0f) + s * 12f;
        float tilt = osc(10f, 5f, 1.5f) + s * 8f;
        float drift = osc(0.035f, 3f, 0.8f) + s * 0.04f;
        GlStateManager.rotate(wobble, 0f, 0f, 1f);
        GlStateManager.rotate(tilt, 1f, 0f, 0f);
        GlStateManager.translate(drift, osc(0.02f, 2.5f, 0f), 0f);
    }

    // ---- Screw : tire-bouchon multi-axes ----
    private static void screw(float s, float e) {
        float sp = (float) ((t * AnimationsModule.spinSpeed) % 360.0);
        spinCentered(sp, 0f, 0f, 1f);
        spinCentered(sp * 0.7f, 0f, 1f, 0f);
        float tilt = osc(8f, 4f, 0f);
        spinCentered(tilt, 1f, 0f, 0f);
        GlStateManager.translate(0f, 0f, s * 0.08f);
    }

    // ---- Rx : précis, sec, estoc + rotation ----
    private static void rx(float s, float e) {
        idleBob(0.008f);
        GlStateManager.translate(0f, 0f, e * 0.32f);
        spinCentered(e * 360f, 0f, 0f, 1f);
    }

    // ---- Flux : ondulation fluide continue ----
    private static void flux(float s, float e) {
        float waveX = osc(0.04f, 2.8f, 0f) + s * 0.08f;
        float waveY = osc(0.03f, 3.5f, 1.2f) + s * 0.05f;
        float waveZ = osc(0.06f, 2.2f, 2.5f) + s * 0.12f;
        float rotZ = osc(8f, 3f, 0.5f) + s * 20f;
        float rotX = osc(4f, 4f, 1.8f) + s * 10f;
        GlStateManager.translate(waveX, waveY, waveZ);
        GlStateManager.rotate(rotZ, 0f, 0f, 1f);
        GlStateManager.rotate(rotX, 1f, 0f, 0f);
    }

    // ---- Avatar : équilibré, élégant, tout-en-un ----
    private static void avatar(float s, float e) {
        idleSway(0.01f, 0.008f);
        float sp = (float) ((t * AnimationsModule.spinSpeed * 0.3f) % 360.0);
        spinCentered(sp * s, 0f, 0f, 1f);
        GlStateManager.translate(0f, s * 0.06f, e * 0.14f);
        GlStateManager.rotate(s * -12f, 1f, 0f, 0f);
    }

    // ---- Interia : élan / inertie ----
    private static void interia(float s, float e) {
        float overshoot = s * 1.3f;
        if (overshoot > 1f) overshoot = 2f - overshoot;
        if (overshoot < 0f) overshoot = 0f;
        GlStateManager.translate(0f, 0f, e * 0.25f);
        GlStateManager.translate(0f, 0f, -overshoot * 0.08f);
        GlStateManager.rotate(e * -25f + overshoot * 10f, 1f, 0f, 0f);
        GlStateManager.rotate(overshoot * 6f, 0f, 0f, 1f);
    }

    // ======================== Idle motions ========================

    /** Petit mouvement de respiration vertical. */
    private static void idleBob(float amp) {
        GlStateManager.translate(0f, osc(amp, 2.5f, 0f), 0f);
    }

    /** Balancement latéral + vertical doux. */
    private static void idleSway(float latAmp, float vertAmp) {
        GlStateManager.translate(osc(latAmp, 2f, 0.5f), osc(vertAmp, 2.8f, 0f), 0f);
    }
}
