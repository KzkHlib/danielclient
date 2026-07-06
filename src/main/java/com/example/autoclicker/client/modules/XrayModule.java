package com.example.autoclicker.client.modules;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Xray : les blocs hors whitelist deviennent translucides (opacité
 * réglable), les minerais whitelistés restent pleins et fullbright. Le rendu
 * réel est fait par les mixins (MixinBlock / MixinBlockModelRenderer) qui
 * lisent l'état statique de cette classe.
 */
public class XrayModule extends Module {

    // ---- état lu par les mixins (y compris les threads de compilation de chunks) ----
    public static volatile boolean active = false;
    /** Facteur de luminosité 0..1 des blocs non-minerai (slider Opacité). */
    public static volatile float opacityFactor = 0.3f;
    private static volatile Set<Block> whitelist = Collections.<Block>emptySet();
    /** Le bloc en cours de compilation est-il un minerai whitelisté ? (par thread) */
    public static final ThreadLocal<Boolean> CURRENT_ORE = new ThreadLocal<Boolean>() {
        @Override protected Boolean initialValue() { return Boolean.TRUE; }
    };

    public static boolean isXrayBlock(Block b) { return whitelist.contains(b); }

    // ---- réglages ----
    private final Setting.Number opacity =
            (Setting.Number) add(new Setting.Number("Opacite", 30, 0, 100, 5, true));
    private final Setting.Bool diamond  = (Setting.Bool) add(new Setting.Bool("Diamond Ore", true));
    private final Setting.Bool emerald  = (Setting.Bool) add(new Setting.Bool("Emerald Ore", true));
    private final Setting.Bool gold     = (Setting.Bool) add(new Setting.Bool("Gold Ore", true));
    private final Setting.Bool iron     = (Setting.Bool) add(new Setting.Bool("Iron Ore", true));
    private final Setting.Bool lapis    = (Setting.Bool) add(new Setting.Bool("Lapis Ore", true));
    private final Setting.Bool redstone = (Setting.Bool) add(new Setting.Bool("Redstone Ore", true));
    private final Setting.Bool coal     = (Setting.Bool) add(new Setting.Bool("Coal Ore", false));
    private final Setting.Bool quartz   = (Setting.Bool) add(new Setting.Bool("Quartz Ore", false));

    private Set<Block> lastWhitelist = null;
    private int lastOpacity = -1;
    /** Re-render différé : évite de recharger le monde à chaque cran de slider. */
    private long dirtySince = 0L;

    public XrayModule() {
        super("Xray", Category.VISUAL);
    }

    @Override
    public String arrayListSuffix() {
        return (int) Math.round(opacity.value) + "%";
    }

    @Override
    public void onEnable() {
        refresh();
        active = true;
        reloadWorld();
    }

    @Override
    public void onDisable() {
        active = false;
        dirtySince = 0L;
        reloadWorld();
    }

    @Override
    public void onTick() {
        long now = System.currentTimeMillis();
        if (refresh()) {
            dirtySince = now;                     // réglage en cours de modification
        } else if (dirtySince != 0L && now - dirtySince > 400L) {
            dirtySince = 0L;
            reloadWorld();                        // réglages stabilisés -> re-render
        }
    }

    /** Recalcule whitelist + opacité depuis les réglages ; true si ça a changé. */
    private boolean refresh() {
        Set<Block> wl = Collections.newSetFromMap(new IdentityHashMap<Block, Boolean>());
        if (diamond.value)  wl.add(Blocks.diamond_ore);
        if (emerald.value)  wl.add(Blocks.emerald_ore);
        if (gold.value)     wl.add(Blocks.gold_ore);
        if (iron.value)     wl.add(Blocks.iron_ore);
        if (lapis.value)    wl.add(Blocks.lapis_ore);
        if (redstone.value) { wl.add(Blocks.redstone_ore); wl.add(Blocks.lit_redstone_ore); }
        if (coal.value)     wl.add(Blocks.coal_ore);
        if (quartz.value)   wl.add(Blocks.quartz_ore);

        int op = (int) Math.round(opacity.value);
        boolean changed = op != lastOpacity || !wl.equals(lastWhitelist);
        if (changed) {
            whitelist = wl;
            lastWhitelist = wl;
            opacityFactor = op / 100f;
            lastOpacity = op;
        }
        return changed;
    }

    /** Recompile tous les chunks (équivalent F3+A). */
    private void reloadWorld() {
        if (mc.renderGlobal != null && mc.theWorld != null) {
            mc.renderGlobal.loadRenderers();
        }
    }
}
