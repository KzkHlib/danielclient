package com.example.autoclicker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/** Réglages persistants de l'autoclicker. */
public class Config {

    public enum Mode { TOGGLE, HOLD }

    // Valeurs par défaut
    public static double cpsMin = 10.0;
    public static double cpsMax = 14.0;
    public static boolean rightClick = false;   // false = clic gauche, true = clic droit
    public static Mode mode = Mode.HOLD;          // HOLD = actif tant que le bouton est maintenu
    public static boolean enabled = false;        // état ON/OFF (utilisé en mode TOGGLE)
    public static boolean blockInGui = true;      // ne pas cliquer quand un menu est ouvert
    public static boolean showCps = true;         // afficher le compteur de CPS en jeu

    private static File file;

    public static void init(File configDir) {
        file = new File(configDir, "autoclicker.cfg");
        load();
    }

    public static void load() {
        if (file == null || !file.exists()) { save(); return; }
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(file)) {
            p.load(in);
            cpsMin = Double.parseDouble(p.getProperty("cpsMin", "10.0"));
            cpsMax = Double.parseDouble(p.getProperty("cpsMax", "14.0"));
            rightClick = Boolean.parseBoolean(p.getProperty("rightClick", "false"));
            mode = Mode.valueOf(p.getProperty("mode", "HOLD"));
            blockInGui = Boolean.parseBoolean(p.getProperty("blockInGui", "true"));
            showCps = Boolean.parseBoolean(p.getProperty("showCps", "true"));
        } catch (Exception e) {
            System.err.println("[AutoClicker] Erreur lecture config: " + e.getMessage());
        }
    }

    public static void save() {
        if (file == null) return;
        Properties p = new Properties();
        p.setProperty("cpsMin", Double.toString(cpsMin));
        p.setProperty("cpsMax", Double.toString(cpsMax));
        p.setProperty("rightClick", Boolean.toString(rightClick));
        p.setProperty("mode", mode.name());
        p.setProperty("blockInGui", Boolean.toString(blockInGui));
        p.setProperty("showCps", Boolean.toString(showCps));
        try (FileOutputStream out = new FileOutputStream(file)) {
            p.store(out, "AutoClicker config");
        } catch (Exception e) {
            System.err.println("[AutoClicker] Erreur ecriture config: " + e.getMessage());
        }
    }

    public static void clampCps() {
        if (cpsMin < 1) cpsMin = 1;
        if (cpsMax < cpsMin) cpsMax = cpsMin;
        if (cpsMax > 100) cpsMax = 100;
    }
}
