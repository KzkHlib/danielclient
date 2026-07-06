package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import java.util.Random;

public class AntiAFKModule extends Module {

    private final Setting.Mode mode = (Setting.Mode) add(new Setting.Mode("Mode", 0, "Rotation", "Saut", "Marche", "Mix"));
    private final Random rand = new Random();
    private long nextAction = 0L;

    public AntiAFKModule() {
        super("AntiAFK", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;
        long now = System.currentTimeMillis();
        if (now < nextAction) return;
        nextAction = now + (3000 + rand.nextInt(4000));

        switch (mode.current()) {
            case "Rotation":
                mc.thePlayer.rotationYaw += rand.nextFloat() * 360f - 180f;
                break;
            case "Saut":
                if (mc.thePlayer.onGround) mc.thePlayer.jump();
                break;
            case "Marche":
                mc.thePlayer.setSprinting(true);
                break;
            case "Mix": {
                int r = rand.nextInt(3);
                if (r == 0) mc.thePlayer.rotationYaw += 90f;
                else if (r == 1 && mc.thePlayer.onGround) mc.thePlayer.jump();
                else mc.thePlayer.setSprinting(true);
                break;
            }
        }
    }
}
