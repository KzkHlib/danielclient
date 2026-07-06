package com.example.autoclicker;

import com.example.autoclicker.client.Client;
import com.example.autoclicker.client.AutoUpdater;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Point d'entrée du mod. Initialise le client (modules, ClickGUI, HUD) et
 * l'enregistre sur le bus d'évènements Forge. Aucune licence requise.
 *
 * Touche par défaut pour ouvrir le menu : Maj droit (RSHIFT).
 */
@Mod(modid = AutoClickerMod.MODID, name = "Daniel Client", version = "1.0", clientSideOnly = true)
public class AutoClickerMod {

    public static final String MODID = "fullbright";

    private Client client;
    private java.io.File configDir;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        configDir = event.getModConfigurationDirectory();
        client = new Client(configDir);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(client);
        AutoUpdater.checkAsync(configDir);
    }
}
