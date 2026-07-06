package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;

import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Blink : retient tes paquets de mouvement tant que c'est actif (tu sembles figé
 * côté serveur), puis les relâche d'un coup à la désactivation (téléport).
 * Lu par MixinNetworkManager.
 */
public class BlinkModule extends Module {

    public static volatile boolean active = false;
    private static final List<Packet> held = new ArrayList<Packet>();

    public BlinkModule() {
        super("Blink", Category.MOVEMENT);
    }

    public static boolean shouldHold(Packet p) {
        return active && p instanceof C03PacketPlayer;
    }

    public static void hold(Packet p) {
        synchronized (held) { held.add(p); }
    }

    @Override public void onEnable() { active = true; }

    @Override
    public void onDisable() {
        active = false;   // les prochains paquets passent normalement
        flush();
    }

    @Override
    public String arrayListSuffix() {
        synchronized (held) { return held.isEmpty() ? null : String.valueOf(held.size()); }
    }

    private void flush() {
        net.minecraft.network.NetworkManager nm =
                Minecraft.getMinecraft().getNetHandler() != null
                        ? Minecraft.getMinecraft().getNetHandler().getNetworkManager() : null;
        synchronized (held) {
            if (nm != null) {
                for (Packet p : held) nm.sendPacket(p);
            }
            held.clear();
        }
    }
}
