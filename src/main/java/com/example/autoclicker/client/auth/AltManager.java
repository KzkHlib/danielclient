package com.example.autoclicker.client.auth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/** Stockage des comptes Microsoft enregistrés (dashboard). Persisté en JSON. */
public final class AltManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<Account>>() {}.getType();

    private static File file;
    private static final List<Account> accounts = new ArrayList<Account>();

    private AltManager() {}

    public static void init(File configDir) {
        file = new File(configDir, "alts.json");
        load();
    }

    public static List<Account> accounts() { return accounts; }

    public static void add(Account a) {
        if (a == null) return;
        // remplace un compte existant du même nom
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).name != null && accounts.get(i).name.equalsIgnoreCase(a.name)) {
                accounts.set(i, a);
                save();
                return;
            }
        }
        accounts.add(a);
        save();
    }

    public static void remove(Account a) {
        accounts.remove(a);
        save();
    }

    // ---- persistance ----
    @SuppressWarnings("unchecked")
    public static void load() {
        accounts.clear();
        if (file == null || !file.exists()) return;
        try {
            FileReader r = new FileReader(file);
            try {
                List<Account> loaded = GSON.fromJson(r, LIST_TYPE);
                if (loaded != null) accounts.addAll(loaded);
            } finally { r.close(); }
        } catch (Exception e) {
            System.err.println("[Alts] Erreur lecture: " + e.getMessage());
        }
    }

    public static void save() {
        if (file == null) return;
        try {
            FileWriter w = new FileWriter(file);
            try { GSON.toJson(accounts, LIST_TYPE, w); } finally { w.close(); }
        } catch (Exception e) {
            System.err.println("[Alts] Erreur ecriture: " + e.getMessage());
        }
    }
}
