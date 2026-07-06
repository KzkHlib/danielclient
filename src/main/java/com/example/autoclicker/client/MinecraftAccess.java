package com.example.autoclicker.client;

import com.example.autoclicker.mixins.MinecraftAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;

public final class MinecraftAccess {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static Timer getTimer() {
        return ((MinecraftAccessor) mc).getTimer();
    }

    public static int getRightClickDelay() {
        return ((MinecraftAccessor) mc).getRightClickDelayTimer();
    }

    public static void setRightClickDelay(int value) {
        ((MinecraftAccessor) mc).setRightClickDelayTimer(value);
    }

    public static void setTimerSpeed(float speed) {
        getTimer().timerSpeed = speed;
    }

    public static float getTimerSpeed() {
        return getTimer().timerSpeed;
    }
}
