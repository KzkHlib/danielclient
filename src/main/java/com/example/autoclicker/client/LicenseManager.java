package com.example.autoclicker.client;

import net.minecraft.client.Minecraft;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

/**
 * Gestion de licence côté mod.
 * - clé enregistrée dans config/daniel-license.txt (saisie une seule fois)
 * - validée auprès de l'API avec l'UUID du compte
 * - expiration renvoyée par le serveur (0 = à vie / Lifetime)
 * - tolérance hors-ligne 24 h via cache local
 *
 * Mets SERVER_URL sur ton serveur (localhost en test, IP du VPS en prod).
 */
public class LicenseManager {

    // URL par défaut (bake ici l'adresse de ton serveur public pour la distribution).
    private static final String SERVER_URL = "http://localhost:8080/validate";
    private static final long OFFLINE_GRACE_MS = 24L * 60L * 60L * 1000L;

    private final File keyFile;
    private final File cacheFile;
    /** URL effective : config/daniel-server.txt si présent, sinon SERVER_URL. */
    private final String serverUrl;

    private boolean licensed = false;
    /** Fin de validité en ms (epoch). 0 = à vie. */
    private long expiresAt = 0L;

    public LicenseManager(File configDir) {
        this.keyFile = new File(configDir, "daniel-license.txt");
        this.cacheFile = new File(configDir, "daniel-license-cache.properties");
        this.serverUrl = resolveUrl(configDir);
        System.out.println("[Daniel Client] Serveur de licence : " + serverUrl);
    }

    /** Lit l'URL du serveur depuis config/daniel-server.txt, sinon défaut. */
    private String resolveUrl(File configDir) {
        try {
            File f = new File(configDir, "daniel-server.txt");
            if (f.exists()) {
                BufferedReader r = new BufferedReader(new FileReader(f));
                String line = r.readLine();
                r.close();
                if (line != null && line.trim().length() > 0) return line.trim();
            }
        } catch (Exception ignored) {
        }
        return SERVER_URL;
    }

    public boolean isLicensed() { return licensed; }
    public long getExpiresAt() { return expiresAt; }

    /** Une clé est-elle déjà enregistrée ? (sert à n'ouvrir la GUI qu'au 1er lancement) */
    public boolean hasSavedKey() {
        String k = readKey();
        return k != null && !k.isEmpty();
    }

    /** Validation silencieuse à partir de la clé enregistrée (au lancement). */
    public boolean validate() {
        String key = readKey();
        if (key == null || key.isEmpty()) {
            licensed = false;
            return false;
        }
        try {
            boolean ok = callServer(key, uuid());
            if (ok) {
                saveCache();
                licensed = true;
                return true;
            }
            licensed = false;
            return false;
        } catch (Exception e) {
            if (withinGrace()) {
                System.out.println("[Daniel Client] Serveur injoignable, validation hors-ligne (cache 24 h).");
                licensed = true;
                return true;
            }
            licensed = false;
            return false;
        }
    }

    /** Validation d'une clé saisie dans la GUI ; l'enregistre si elle est bonne. */
    public boolean validateKey(String key) {
        if (key == null || key.trim().isEmpty()) return false;
        key = key.trim();
        try {
            boolean ok = callServer(key, uuid());
            if (ok) {
                writeKey(key);
                saveCache();
                licensed = true;
                return true;
            }
            return false;
        } catch (Exception e) {
            return false; // une clé neuve ne peut pas passer hors-ligne
        }
    }

    private String uuid() {
        try {
            return Minecraft.getMinecraft().getSession().getPlayerID();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private String name() {
        try {
            return Minecraft.getMinecraft().getSession().getUsername();
        } catch (Exception e) {
            return "unknown";
        }
    }

    /** Empreinte machine non-invasive (hash des MAC + OS + utilisateur). */
    private String hwid() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(System.getProperty("os.name", "")).append('|')
              .append(System.getProperty("user.name", ""));
            java.util.Enumeration<java.net.NetworkInterface> nis = java.net.NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                byte[] mac = nis.nextElement().getHardwareAddress();
                if (mac != null) {
                    for (byte x : mac) sb.append(String.format("%02X", x));
                }
            }
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] h = md.digest(sb.toString().getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 8 && i < h.length; i++) hex.append(String.format("%02x", h[i]));
            return hex.toString().toUpperCase();
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    private String readKey() {
        try {
            if (!keyFile.exists()) return null;
            BufferedReader r = new BufferedReader(new FileReader(keyFile));
            String line = r.readLine();
            r.close();
            return line == null ? null : line.trim();
        } catch (Exception e) {
            return null;
        }
    }

    private void writeKey(String key) {
        try {
            FileWriter w = new FileWriter(keyFile);
            w.write(key);
            w.close();
        } catch (Exception ignored) {
        }
    }

    /** Appel HTTP ; remplit expiresAt et renvoie true si valide. */
    private boolean callServer(String key, String uuid) throws IOException {
        URL url = new URL(serverUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        String body = "{\"key\":\"" + esc(key) + "\",\"uuid\":\"" + esc(uuid)
                + "\",\"name\":\"" + esc(name()) + "\",\"hwid\":\"" + esc(hwid()) + "\"}";
        OutputStream os = con.getOutputStream();
        os.write(body.getBytes("UTF-8"));
        os.close();

        int code = con.getResponseCode();
        InputStream is = code >= 400 ? con.getErrorStream() : con.getInputStream();
        StringBuilder sb = new StringBuilder();
        if (is != null) {
            BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String l;
            while ((l = r.readLine()) != null) sb.append(l);
            r.close();
        }
        String resp = sb.toString();
        boolean valid = resp.contains("\"valid\":true");
        if (valid) expiresAt = parseExpires(resp);
        return valid;
    }

    /** Extrait expiresAt du JSON ("expiresAt":null -> 0, sinon le nombre). */
    private long parseExpires(String json) {
        try {
            int i = json.indexOf("\"expiresAt\"");
            if (i < 0) return 0L;
            int colon = json.indexOf(':', i);
            if (colon < 0) return 0L;
            String rest = json.substring(colon + 1).trim();
            if (rest.startsWith("null")) return 0L;
            StringBuilder num = new StringBuilder();
            for (int j = 0; j < rest.length(); j++) {
                char c = rest.charAt(j);
                if (Character.isDigit(c)) num.append(c);
                else break;
            }
            return num.length() == 0 ? 0L : Long.parseLong(num.toString());
        } catch (Exception e) {
            return 0L;
        }
    }

    private String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void saveCache() {
        try {
            Properties p = new Properties();
            p.setProperty("lastValid", Long.toString(System.currentTimeMillis()));
            p.setProperty("expiresAt", Long.toString(expiresAt));
            FileOutputStream o = new FileOutputStream(cacheFile);
            p.store(o, "Daniel Client license cache");
            o.close();
        } catch (Exception ignored) {
        }
    }

    private boolean withinGrace() {
        try {
            if (!cacheFile.exists()) return false;
            Properties p = new Properties();
            FileInputStream i = new FileInputStream(cacheFile);
            p.load(i);
            i.close();
            long last = Long.parseLong(p.getProperty("lastValid", "0"));
            long exp = Long.parseLong(p.getProperty("expiresAt", "0"));
            // si la licence a une fin connue et qu'elle est dépassée, pas de grâce
            if (exp != 0 && System.currentTimeMillis() > exp) return false;
            expiresAt = exp;
            return System.currentTimeMillis() - last < OFFLINE_GRACE_MS;
        } catch (Exception e) {
            return false;
        }
    }
}
