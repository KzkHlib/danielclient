package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;

/**
 * NoHurtCam : supprime la secousse de caméra reçue lors des dégâts en
 * remettant hurtTime à 0 chaque tick.
 */
public class NoHurtCamModule extends Module {

    public NoHurtCamModule() {
        super("NoHurtCam", Category.VISUAL);
    }

    @Override
    public void onTick() {
        if (mc.thePlayer != null) {
            mc.thePlayer.hurtTime = 0;
        }
    }
}
