package com.example.autoclicker.client;

import com.example.autoclicker.client.gui.KeyEntryGui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Affiche la fenêtre de saisie de clé au premier lancement (au menu principal)
 * tant qu'aucune licence valide n'a été enregistrée. Une fois validée, le client
 * est activé et cette fenêtre ne réapparaît plus.
 */
public class LicenseGate {

    private final Minecraft mc = Minecraft.getMinecraft();
    private final LicenseManager license;
    private final Runnable onLicensed;

    private boolean prompted = false;
    private boolean done = false;

    public LicenseGate(LicenseManager license, Runnable onLicensed) {
        this.license = license;
        this.onLicensed = onLicensed;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (done) return;

        // on attend le menu principal pour proposer la saisie, une seule fois
        if (!prompted && mc.currentScreen instanceof GuiMainMenu) {
            prompted = true;
            mc.displayGuiScreen(new KeyEntryGui(license, new Runnable() {
                @Override public void run() { activate(); }
            }));
        }
    }

    private void activate() {
        if (done) return;
        done = true;
        if (onLicensed != null) onLicensed.run();
    }
}
