package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Client;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import org.lwjgl.input.Mouse;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ClickRecorder : module separe qui capture tes vrais clics gauche.
 * Bind ON = record, bind OFF = sauvegarde le fichier .clicks.
 */
public class ClickRecorderModule extends Module {

    private final Setting.Number minInterval =
            (Setting.Number) add(new Setting.Number("Min interval ms", 15, 1, 100, 1, true));
    private final Setting.Number maxInterval =
            (Setting.Number) add(new Setting.Number("Max interval ms", 1000, 100, 2000, 25, true));

    private boolean previousDown = false;
    private long lastClickAt = 0L;
    private long startedAt = 0L;
    private final List<Long> intervals = new ArrayList<Long>();

    public ClickRecorderModule() {
        super("ClickRecorder", Category.COMBAT);
    }

    @Override
    public String arrayListSuffix() {
        return "REC " + intervals.size();
    }

    @Override
    public void onEnable() {
        intervals.clear();
        previousDown = Mouse.isButtonDown(0);
        lastClickAt = 0L;
        startedAt = System.currentTimeMillis();
        ensureDir();
        notify("Recorder ON: clic gauche naturel");
    }

    @Override
    public void onDisable() {
        save();
        previousDown = false;
        lastClickAt = 0L;
        startedAt = 0L;
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null || mc.currentScreen != null || !mc.inGameHasFocus) return;

        boolean down = Mouse.isButtonDown(0);
        long now = System.currentTimeMillis();
        if (down && !previousDown) {
            if (lastClickAt > 0L) {
                long interval = now - lastClickAt;
                if (interval >= minInterval.value && interval <= maxInterval.value) {
                    intervals.add(interval);
                }
            }
            lastClickAt = now;
            if (Client.INSTANCE != null) Client.INSTANCE.recordClick(false);
        }
        previousDown = down;
    }

    private File recordsDir() {
        return new File(mc.mcDataDir, "DanielClient/click-records");
    }

    private void ensureDir() {
        File dir = recordsDir();
        if (!dir.exists()) dir.mkdirs();
    }

    private void save() {
        ensureDir();
        if (intervals.size() < 3) {
            notify("Recorder OFF: pas assez de clics");
            return;
        }

        String stamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        File out = new File(recordsDir(), "autoclick-left-" + stamp + ".clicks");
        File latest = new File(recordsDir(), "autoclick-left-latest.clicks");

        write(out);
        write(latest);
        notify("Recorder save: " + intervals.size() + " intervalles");
    }

    private void write(File file) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(file));
            pw.println("# Daniel Client AutoClickL recording");
            pw.println("# prism_instance_dir=" + mc.mcDataDir.getAbsolutePath());
            pw.println("# duration_ms=" + Math.max(0L, System.currentTimeMillis() - startedAt));
            pw.println("# intervals_ms=" + intervals.size());
            pw.println("# avg_cps=" + String.format("%.2f", averageCps()));
            for (Long interval : intervals) pw.println(interval.longValue());
            pw.close();
        } catch (Exception ignored) {
        }
    }

    private double averageCps() {
        if (intervals.isEmpty()) return 0.0;
        long total = 0L;
        for (Long interval : intervals) total += interval.longValue();
        double avgMs = (double) total / (double) intervals.size();
        return avgMs <= 0.0 ? 0.0 : 1000.0 / avgMs;
    }

    private void notify(String message) {
        if (Client.INSTANCE != null) Client.INSTANCE.notifications.add(new Client.Notif(message));
    }
}
