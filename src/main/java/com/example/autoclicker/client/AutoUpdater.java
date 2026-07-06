package com.example.autoclicker.client;

import com.example.autoclicker.AutoClickerMod;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

/**
 * Auto-update simple :
 * - lit latest.json depuis une URL configuree ;
 * - telecharge le nouveau jar dans le dossier Minecraft reel du launcher ;
 * - prepare un remplacement au prochain arret du jeu.
 */
public final class AutoUpdater {

    private AutoUpdater() {}

    private static final String DEFAULT_UPDATE_URL = "https://raw.githubusercontent.com/KzkHlib/danielclient/main/updates/latest.json";
    private static volatile boolean running = false;
    public static volatile String status = "";
    public static volatile String remoteVersion = "";
    public static volatile boolean updateReady = false;

    public static void checkAsync(final File configDir) {
        if (running) return;
        running = true;
        Thread t = new Thread(new Runnable() {
            @Override public void run() {
                try {
                    check(configDir);
                } catch (Exception e) {
                    status = "Update check impossible";
                } finally {
                    running = false;
                }
            }
        }, "DanielClient-Updater");
        t.setDaemon(true);
        t.start();
    }

    private static void check(File configDir) throws Exception {
        String updateUrl = readUpdateUrl(configDir);
        if (updateUrl.length() == 0 || updateUrl.contains("example.com")) {
            status = "Update URL non configuree";
            return;
        }

        JsonObject json = readJson(updateUrl);
        String remoteVersion = get(json, "version");
        String jarUrl = get(json, "jarUrl");
        String sha256 = get(json, "sha256");
        String changelog = get(json, "changelog");
        AutoUpdater.remoteVersion = remoteVersion;

        if (remoteVersion.length() == 0) {
            status = "latest.json invalide: version manquante";
            updateReady = false;
            return;
        }
        if (jarUrl.length() == 0) {
            status = "latest.json invalide: jarUrl manquant";
            updateReady = false;
            return;
        }
        if (compare(remoteVersion, UpdateChecker.LOCAL_VERSION) <= 0) {
            status = "A jour";
            updateReady = false;
            return;
        }

        File currentJar = currentJar();
        if (currentJar == null || !currentJar.isFile()) {
            status = "Update ignoree en dev";
            updateReady = false;
            return;
        }

        File mcDir = Minecraft.getMinecraft().mcDataDir;
        File updatesDir = new File(mcDir, "DanielClient/updates");
        if (!updatesDir.exists()) updatesDir.mkdirs();

        File downloaded = new File(updatesDir, "danielclient-" + remoteVersion + ".jar");
        download(jarUrl, downloaded);
        if (sha256.length() > 0 && !sha256(downloaded).equalsIgnoreCase(sha256)) {
            downloaded.delete();
            status = "Update hash invalide";
            notify("Update refusee: hash invalide");
            return;
        }

        writePending(updatesDir, currentJar, downloaded, remoteVersion, changelog);
        installOnExit(updatesDir, currentJar, downloaded);
        status = "Update " + remoteVersion + " prete";
        updateReady = true;
        notify("Update " + remoteVersion + " prete: redemarre Minecraft");
    }

    private static String readUpdateUrl(File configDir) {
        try {
            if (!configDir.exists()) configDir.mkdirs();
            File f = new File(configDir, "daniel-update-url.txt");
            if (!f.exists()) {
                PrintWriter pw = new PrintWriter(new FileWriter(f));
                pw.println(DEFAULT_UPDATE_URL);
                pw.close();
                return DEFAULT_UPDATE_URL;
            }
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = br.readLine();
            br.close();
            return line == null ? "" : line.trim();
        } catch (Exception e) {
            return "";
        }
    }

    private static JsonObject readJson(String url) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(6000);
        c.setReadTimeout(6000);
        c.setUseCaches(false);
        c.setRequestProperty("User-Agent", "DanielClient/" + UpdateChecker.LOCAL_VERSION);
        InputStream in = c.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        return new JsonParser().parse(sb.toString()).getAsJsonObject();
    }

    private static String get(JsonObject o, String key) {
        try {
            return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString().trim() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private static File currentJar() {
        File forgeSource = currentJarFromForge();
        if (forgeSource != null) return forgeSource;

        try {
            URL url = AutoUpdater.class.getProtectionDomain().getCodeSource().getLocation();
            File f = new File(url.toURI());
            if (f.isFile()) return f;
        } catch (Exception e) {
        }

        return currentJarFromModsFolder();
    }

    private static File currentJarFromForge() {
        try {
            ModContainer container = Loader.instance().getIndexedModList().get(AutoClickerMod.MODID);
            if (container == null) return null;
            File source = container.getSource();
            return source != null && source.isFile() ? source : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static File currentJarFromModsFolder() {
        try {
            File mods = new File(Minecraft.getMinecraft().mcDataDir, "mods");
            File[] files = mods.listFiles();
            if (files == null) return null;
            File best = null;
            for (File f : files) {
                String n = f.getName().toLowerCase();
                if (!f.isFile() || !n.endsWith(".jar")) continue;
                if (!n.contains("fullbright") && !n.contains("daniel")) continue;
                if (best == null || f.lastModified() > best.lastModified()) best = f;
            }
            return best;
        } catch (Exception e) {
            return null;
        }
    }

    private static void download(String url, File out) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(8000);
        c.setReadTimeout(15000);
        c.setUseCaches(false);
        c.setRequestProperty("User-Agent", "DanielClient/" + UpdateChecker.LOCAL_VERSION);
        BufferedInputStream in = new BufferedInputStream(c.getInputStream());
        FileOutputStream fos = new FileOutputStream(out);
        byte[] buf = new byte[8192];
        int r;
        while ((r = in.read(buf)) >= 0) fos.write(buf, 0, r);
        fos.close();
        in.close();
    }

    private static void writePending(File updatesDir, File currentJar, File downloaded,
                                     String version, String changelog) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(new File(updatesDir, "pending-update.txt")));
            pw.println("version=" + version);
            pw.println("target=" + currentJar.getAbsolutePath());
            pw.println("source=" + downloaded.getAbsolutePath());
            pw.println("changelog=" + changelog);
            pw.close();
        } catch (Exception ignored) {
        }
    }

    private static void installOnExit(final File updatesDir, final File currentJar, final File downloaded) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override public void run() {
                try {
                    if (isWindows()) writeAndRunBat(updatesDir, currentJar, downloaded);
                    else writeAndRunSh(updatesDir, currentJar, downloaded);
                } catch (Exception ignored) {
                }
            }
        }, "DanielClient-InstallUpdate"));
    }

    private static void writeAndRunBat(File dir, File target, File source) throws Exception {
        File bat = new File(dir, "install-update.bat");
        PrintWriter pw = new PrintWriter(new FileWriter(bat));
        pw.println("@echo off");
        pw.println("ping 127.0.0.1 -n 3 > nul");
        pw.println(":again");
        pw.println("copy /Y \"" + source.getAbsolutePath() + "\" \"" + target.getAbsolutePath() + "\" > nul");
        pw.println("if errorlevel 1 (");
        pw.println("  ping 127.0.0.1 -n 2 > nul");
        pw.println("  goto again");
        pw.println(")");
        pw.println("del \"" + new File(dir, "pending-update.txt").getAbsolutePath() + "\" > nul 2> nul");
        pw.close();
        Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "\"\"", "/min", bat.getAbsolutePath()});
    }

    private static void writeAndRunSh(File dir, File target, File source) throws Exception {
        File sh = new File(dir, "install-update.sh");
        PrintWriter pw = new PrintWriter(new FileWriter(sh));
        pw.println("#!/bin/sh");
        pw.println("sleep 2");
        pw.println("while ! cp \"" + source.getAbsolutePath() + "\" \"" + target.getAbsolutePath() + "\"; do sleep 1; done");
        pw.println("rm -f \"" + new File(dir, "pending-update.txt").getAbsolutePath() + "\"");
        pw.close();
        sh.setExecutable(true);
        Runtime.getRuntime().exec(new String[]{"sh", sh.getAbsolutePath()});
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    private static String sha256(File file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        BufferedInputStream in = new BufferedInputStream(new java.io.FileInputStream(file));
        byte[] buf = new byte[8192];
        int r;
        while ((r = in.read(buf)) >= 0) md.update(buf, 0, r);
        in.close();
        byte[] dig = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : dig) {
            String s = Integer.toHexString(b & 0xFF);
            if (s.length() == 1) sb.append('0');
            sb.append(s);
        }
        return sb.toString();
    }

    private static int compare(String a, String b) {
        String[] aa = a.split("\\.");
        String[] bb = b.split("\\.");
        int n = Math.max(aa.length, bb.length);
        for (int i = 0; i < n; i++) {
            int ai = i < aa.length ? parseInt(aa[i]) : 0;
            int bi = i < bb.length ? parseInt(bb[i]) : 0;
            if (ai != bi) return ai - bi;
        }
        return 0;
    }

    private static int parseInt(String s) {
        try {
            String clean = s.replaceAll("[^0-9]", "");
            return clean.length() == 0 ? 0 : Integer.parseInt(clean);
        } catch (Exception e) {
            return 0;
        }
    }

    private static void notify(String msg) {
        if (Client.INSTANCE != null) Client.INSTANCE.notifications.add(new Client.Notif(msg));
    }
}
