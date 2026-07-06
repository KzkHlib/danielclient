package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import org.lwjgl.input.Keyboard;

/**
 * FreeLook : regarde autour de toi sans bouger ton personnage (tête + corps
 * lockés), en vue 3e personne (tu te vois). FOV réglable. Maintien de la touche
 * (V par défaut). Caméra mise à jour par frame = fluide.
 */
public class FreeLookModule extends Module {

    private final Setting.Number fov =
            (Setting.Number) add(new Setting.Number("FOV", 90, 30, 120, 1, true));

    private boolean engaged = false;
    private float camYaw, camPitch;   // rotation de la caméra (libre)
    private float lockYaw, lockPitch; // rotation figée du perso
    private float savedFov;
    private int savedPerspective;

    public FreeLookModule() {
        super("FreeLook", Category.VISUAL);
        holdKey = true;
        key = Keyboard.KEY_V;
    }

    @Override
    public void onTick() {
        boolean down = key != 0 && Keyboard.isKeyDown(key)
                && mc.currentScreen == null && mc.thePlayer != null;

        if (down && !engaged) engage();
        else if (!down && engaged) disengage();

        if (engaged) {
            mc.gameSettings.fovSetting = (float) fov.value; // appliqué avant le rendu
        }
    }

    private void engage() {
        engaged = true;
        lockYaw = mc.thePlayer.rotationYaw;
        lockPitch = mc.thePlayer.rotationPitch;
        camYaw = lockYaw;
        camPitch = lockPitch;
        savedPerspective = mc.gameSettings.thirdPersonView;
        savedFov = mc.gameSettings.fovSetting;
        mc.gameSettings.thirdPersonView = 1; // F5 : se voir de dos
    }

    private void disengage() {
        engaged = false;
        if (mc.thePlayer != null) {
            mc.thePlayer.rotationYaw = lockYaw;
            mc.thePlayer.rotationPitch = lockPitch;
        }
        mc.gameSettings.thirdPersonView = savedPerspective;
        mc.gameSettings.fovSetting = savedFov;
    }

    @Override
    public void onCameraSetup(EntityViewRenderEvent.CameraSetup e) {
        if (!engaged || mc.thePlayer == null) return;
        EntityPlayerSP p = mc.thePlayer;

        // la souris a bougé la rotation depuis le dernier frame -> on la donne à la caméra
        camYaw += p.rotationYaw - lockYaw;
        camPitch += p.rotationPitch - lockPitch;
        if (camPitch > 90f) camPitch = 90f;
        if (camPitch < -90f) camPitch = -90f;

        // lock complet : corps + tête figés
        p.rotationYaw = lockYaw;
        p.prevRotationYaw = lockYaw;
        p.rotationPitch = lockPitch;
        p.prevRotationPitch = lockPitch;
        p.rotationYawHead = lockYaw;
        p.prevRotationYawHead = lockYaw;
        p.renderYawOffset = lockYaw;
        p.prevRenderYawOffset = lockYaw;

        // applique la rotation de la caméra (par frame = fluide)
        e.yaw = camYaw;
        e.pitch = camPitch;
    }

    @Override
    public void onDisable() {
        if (engaged) disengage();
    }
}
