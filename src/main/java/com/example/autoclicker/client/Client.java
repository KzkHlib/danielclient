package com.example.autoclicker.client;

import com.example.autoclicker.client.gui.ClickGuiScreen;
import com.example.autoclicker.client.modules.ArmorHUDModule;
import com.example.autoclicker.client.modules.ArrayListModule;
import com.example.autoclicker.client.modules.AutoClickerModule;
import com.example.autoclicker.client.modules.AutoHealModule;
import com.example.autoclicker.client.modules.AntiAFKModule;
import com.example.autoclicker.client.modules.AntiVoidModule;
import com.example.autoclicker.client.modules.AutoRespawnModule;
import com.example.autoclicker.client.modules.AutoRodModule;
import com.example.autoclicker.client.modules.AutoToolModule;
import com.example.autoclicker.client.modules.BreadcrumbsModule;
import com.example.autoclicker.client.modules.InvCleanerModule;
import com.example.autoclicker.client.modules.ItemESPModule;
import com.example.autoclicker.client.modules.ParkourModule;
import com.example.autoclicker.client.modules.SafeWalkModule;
import com.example.autoclicker.client.modules.HitBoxModule;
import com.example.autoclicker.client.modules.FastLadderModule;
import com.example.autoclicker.client.modules.LongJumpModule;
import com.example.autoclicker.client.modules.JesusModule;
import com.example.autoclicker.client.modules.SpiderModule;
import com.example.autoclicker.client.modules.TrajectoriesModule;
import com.example.autoclicker.client.modules.ChamsModule;
import com.example.autoclicker.client.modules.ScaffoldModule;
import com.example.autoclicker.client.modules.SuperKnockbackModule;
import com.example.autoclicker.client.modules.TriggerBotModule;
import com.example.autoclicker.client.modules.AntiHungerModule;
import com.example.autoclicker.client.modules.VClipModule;
import com.example.autoclicker.client.modules.PhaseModule;
import com.example.autoclicker.client.modules.WebKickModule;
import com.example.autoclicker.client.modules.IceSpeedModule;
import com.example.autoclicker.client.modules.StrafeModule;

import com.example.autoclicker.client.modules.BlockOverlayModule;
import com.example.autoclicker.client.modules.AntiDebuffModule;
import com.example.autoclicker.client.modules.AutoWaterModule;
import com.example.autoclicker.client.modules.CpsModule;
import com.example.autoclicker.client.modules.AntiBotModule;
import com.example.autoclicker.client.modules.AutoArmorModule;
import com.example.autoclicker.client.modules.AutoBlockModule;
import com.example.autoclicker.client.modules.AutoPotModule;
import com.example.autoclicker.client.modules.AutoSoupModule;
import com.example.autoclicker.client.modules.BlinkModule;
import com.example.autoclicker.client.modules.BowAimbotModule;
import com.example.autoclicker.client.modules.ChestEspModule;
import com.example.autoclicker.client.modules.ChestStealerModule;
import com.example.autoclicker.client.modules.ClickRecorderModule;
import com.example.autoclicker.client.modules.SprintModule;
import com.example.autoclicker.client.modules.StepModule;
import com.example.autoclicker.client.modules.EspModule;
import com.example.autoclicker.client.modules.FlightModule;
import com.example.autoclicker.client.modules.KillAuraModule;
import com.example.autoclicker.client.modules.WTapModule;
import com.example.autoclicker.client.modules.FriendsModule;
import com.example.autoclicker.client.modules.LavaMacroModule;
import com.example.autoclicker.client.modules.TimerModule;
import com.example.autoclicker.client.modules.VelocityModule;
import com.example.autoclicker.client.modules.FastBreakModule;
import com.example.autoclicker.client.modules.FastPlaceModule;
import com.example.autoclicker.client.modules.FreeLookModule;
import com.example.autoclicker.client.modules.FullbrightModule;
import com.example.autoclicker.client.modules.NametagsModule;
import com.example.autoclicker.client.modules.NoFallModule;
import com.example.autoclicker.client.modules.NoHurtCamModule;
import com.example.autoclicker.client.modules.NoSlowdownModule;
import com.example.autoclicker.client.modules.NotificationsModule;
import com.example.autoclicker.client.modules.ReachModule;
import com.example.autoclicker.client.modules.RodMacroModule;
import com.example.autoclicker.client.modules.PotionHUDModule;
import com.example.autoclicker.client.modules.AimAssistModule;
import com.example.autoclicker.client.modules.AnimationsModule;
import com.example.autoclicker.client.modules.CriticalsModule;
import com.example.autoclicker.client.modules.CustomCrosshairModule;
import com.example.autoclicker.client.modules.InterfaceModule;
import com.example.autoclicker.client.modules.KeepSprintModule;
import com.example.autoclicker.client.modules.SpeedModule;
import com.example.autoclicker.client.modules.TargetStrafeModule;
import com.example.autoclicker.client.modules.TargetHUDModule;
import com.example.autoclicker.client.modules.ToggleSneakModule;
import com.example.autoclicker.client.modules.ToggleSprintModule;
import com.example.autoclicker.client.modules.TracersModule;
import com.example.autoclicker.client.modules.ViewModelModule;
import com.example.autoclicker.client.modules.XrayModule;
import com.example.autoclicker.client.modules.ZoomModule;

import com.example.autoclicker.client.gui.AltManagerScreen;
import com.example.autoclicker.client.gui.CustomMainMenu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import com.example.autoclicker.client.FriendManager;
import com.example.autoclicker.client.render.Projection;

import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Properties;

/**
 * Cœur du client : possède les modules, gère les évènements (tick, rendu HUD,
 * touches), le compteur de CPS centralisé et la persistance.
 */
public class Client {

    public static Client INSTANCE;

    private final Minecraft mc = Minecraft.getMinecraft();
    public final List<Module> modules = new ArrayList<Module>();

    /** Touche d'ouverture du ClickGUI (Shift droit par défaut). */
    public int guiKey = Keyboard.KEY_RSHIFT;

    private File configDir;
    private File configFile;            // profil "default"
    private File profilesDir;           // profils nommés
    private File metaFile;              // mémorise le profil actif
    private String activeProfile = "default";

    // --- Compteur de CPS centralisé (fenêtre glissante 1 s) ---
    private final Deque<Long> leftClicks = new ArrayDeque<Long>();
    private final Deque<Long> rightClicks = new ArrayDeque<Long>();
    private boolean prevLeftDown = false;
    private boolean prevRightDown = false;

    // --- Notifications (module toggle) ---
    public static class Notif {
        public final String text;
        public final boolean on;
        public final boolean generic;   // true = message libre (pas de "activé/désactivé")
        public final long time;
        public Notif(String text, boolean on) {
            this.text = text;
            this.on = on;
            this.generic = false;
            this.time = System.currentTimeMillis();
        }
        public Notif(String text) {     // notif générique
            this.text = text;
            this.on = true;
            this.generic = true;
            this.time = System.currentTimeMillis();
        }
    }
    public final List<Notif> notifications = new ArrayList<Notif>();

    /** Appelé par Module.setEnabled : empile une notif si le module Notifications est actif. */
    public void onModuleToggled(Module m) {
        if (mc.theWorld == null) return;            // pas de notif/son au démarrage
        Sounds.toggle(m.isEnabled());               // petit son satisfaisant
        if (m instanceof NotificationsModule) return;
        Module n = getModule(NotificationsModule.class);
        if (n == null || !n.isEnabled()) return;
        notifications.add(new Notif(m.name, m.isEnabled()));
    }

    public Client(File configDir) {
        INSTANCE = this;
        this.configDir = configDir;
        this.configFile = new File(configDir, "client.cfg");
        this.profilesDir = new File(configDir, "profiles");
        this.metaFile = new File(configDir, "client.meta");
        if (!profilesDir.exists()) profilesDir.mkdirs();
        com.example.autoclicker.client.auth.AltManager.init(configDir);

        // Enregistrement des modules
        modules.add(new AutoClickerModule("AutoClickL", false)); // clic gauche
        modules.add(new AutoClickerModule("AutoClickR", true));  // clic droit
        modules.add(new ClickRecorderModule());
        modules.add(new AimAssistModule());
        modules.add(new CriticalsModule());
        modules.add(new KeepSprintModule());
        modules.add(new TargetStrafeModule());
        modules.add(new ReachModule());
        modules.add(new VelocityModule());
        modules.add(new SpeedModule());
        modules.add(new NoSlowdownModule());
        modules.add(new NoFallModule());
        modules.add(new FlightModule());
        modules.add(new TimerModule());
        modules.add(new KillAuraModule());
        modules.add(new WTapModule());
        modules.add(new RodMacroModule());
        modules.add(new AutoBlockModule());
        modules.add(new BowAimbotModule());
        modules.add(new BreadcrumbsModule());
        modules.add(new AntiAFKModule());
        modules.add(new AntiBotModule());
        modules.add(new AntiVoidModule());
        modules.add(new AutoHealModule());
        modules.add(new AutoPotModule());
        modules.add(new AutoRespawnModule());
        modules.add(new AutoRodModule());
        modules.add(new AutoSoupModule());
        modules.add(new AutoArmorModule());
        modules.add(new ChestStealerModule());
        modules.add(new SafeWalkModule());
        modules.add(new StepModule());
        modules.add(new SprintModule());
        modules.add(new BlinkModule());
        modules.add(new AntiDebuffModule());
        modules.add(new LavaMacroModule());
        modules.add(new AutoWaterModule());
        modules.add(new XrayModule());
        modules.add(new ChestEspModule());
        modules.add(new EspModule());
        modules.add(new TracersModule());
        modules.add(new NametagsModule());
        modules.add(new ViewModelModule());
        modules.add(new AnimationsModule());
        modules.add(new CustomCrosshairModule());
        modules.add(new InterfaceModule());
        modules.add(new FastPlaceModule());
        modules.add(new FastBreakModule());
        modules.add(new AutoToolModule());
        modules.add(new FullbrightModule());
        modules.add(new InvCleanerModule());
        modules.add(new ItemESPModule());
        modules.add(new NoHurtCamModule());
        modules.add(new BlockOverlayModule());
        modules.add(new FreeLookModule());
        modules.add(new ParkourModule());
        modules.add(new ZoomModule());
        modules.add(new ToggleSprintModule());
        modules.add(new ToggleSneakModule());
        modules.add(new CpsModule());
        modules.add(new ArrayListModule());
        modules.add(new com.example.autoclicker.client.modules.WatermarkModule());
        modules.add(new TargetHUDModule());
        modules.add(new PotionHUDModule());
        modules.add(new ArmorHUDModule());
        modules.add(new NotificationsModule());
        modules.add(new FriendsModule());
        modules.add(new HitBoxModule());
        modules.add(new FastLadderModule());
        modules.add(new LongJumpModule());
        modules.add(new JesusModule());
        modules.add(new SpiderModule());
        modules.add(new TrajectoriesModule());
        modules.add(new ChamsModule());
        modules.add(new ScaffoldModule());
        modules.add(new SuperKnockbackModule());
        modules.add(new TriggerBotModule());
        modules.add(new AntiHungerModule());
        modules.add(new VClipModule());
        modules.add(new PhaseModule());
        modules.add(new WebKickModule());
        modules.add(new IceSpeedModule());
        modules.add(new StrafeModule());

        readMeta();
        load();
    }

    // ====================== Modules ======================

    public Module getModule(Class<? extends Module> clazz) {
        for (Module m : modules) if (clazz.isInstance(m)) return m;
        return null;
    }

    public List<Module> modulesIn(Category cat) {
        List<Module> out = new ArrayList<Module>();
        for (Module m : modules) if (m.category == cat) out.add(m);
        return out;
    }

    // ====================== CPS ======================

    /** Appelé par l'autoclicker à chaque clic injecté. */
    public void recordClick(boolean right) {
        long now = System.currentTimeMillis();
        if (right) rightClicks.addLast(now); else leftClicks.addLast(now);
    }

    private void trackManualClicks() {
        boolean inGame = mc.inGameHasFocus && mc.currentScreen == null;
        boolean l = inGame && Mouse.isButtonDown(0);
        boolean r = inGame && Mouse.isButtonDown(1);
        if (l && !prevLeftDown) recordClick(false);
        if (r && !prevRightDown) recordClick(true);
        prevLeftDown = l;
        prevRightDown = r;
    }

    public int leftCps() { return count(leftClicks); }
    public int rightCps() { return count(rightClicks); }

    private int count(Deque<Long> times) {
        long cutoff = System.currentTimeMillis() - 1000L;
        while (!times.isEmpty() && times.peekFirst() < cutoff) times.removeFirst();
        return times.size();
    }

    // ====================== Évènements ======================

    private boolean brandingApplied = false;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        // branding fenêtre (titre + icône), une seule fois
        if (!brandingApplied) {
            brandingApplied = true;
            AppBranding.apply();
        }

        if (mc.theWorld == null || mc.thePlayer == null) return;

        Sounds.stopMenuMusic();   // coupe la musique du menu une fois en jeu

        trackManualClicks();

        for (Module m : modules) {
            if (m.isEnabled()) m.onTick();
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        int key = Keyboard.getEventKey();
        if (!Keyboard.getEventKeyState()) return;        // uniquement à l'appui
        if (mc.currentScreen != null) return;            // pas dans un menu

        if (key == guiKey) {
            mc.displayGuiScreen(new ClickGuiScreen());
            return;
        }
        // touches de modules (les modules "holdKey" gèrent leur touche eux-mêmes)
        for (Module m : modules) {
            if (m.holdKey) continue;
            if (m.key != 0 && m.key == key) m.toggle();
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (mc.thePlayer == null) return;
        // on masque le HUD pendant qu'on tape dans le chat pour ne pas gêner
        boolean chatting = mc.currentScreen instanceof GuiChat;
        if (chatting) return;
        ScaledResolution sr = new ScaledResolution(mc);
        for (Module m : modules) {
            if (m.isEnabled()) m.onRenderHud(sr);
        }
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        for (Module m : modules) {
            if (m.isEnabled()) m.onAttackEntity(event);
        }
    }

    /** Bouton "Alts" injecté dans le menu multijoueur (face au Proxy). */
    private static final int ALTS_BUTTON_ID = 91823;

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui instanceof GuiMainMenu && !(event.gui instanceof CustomMainMenu)) {
            event.gui = new CustomMainMenu();   // menu principal thémé
        }
    }

    @SubscribeEvent
    public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.gui instanceof GuiMultiplayer) {
            event.buttonList.add(new GuiButton(ALTS_BUTTON_ID, 6, 6, 80, 20, "Alts"));
        }
    }

    @SubscribeEvent
    public void onGuiAction(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        if (event.gui instanceof GuiMultiplayer && event.button.id == ALTS_BUTTON_ID) {
            mc.displayGuiScreen(new AltManagerScreen(event.gui));
        }
    }

    private long lastRenderNanos = System.nanoTime();

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        long now = System.nanoTime();
        float dt = (float) Math.min(0.1, (now - lastRenderNanos) / 1.0e9);
        lastRenderNanos = now;
        for (Module m : modules) {
            if (m.isEnabled()) m.onRender(dt);
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        Projection.capture(event.partialTicks);     // matrices pour la projection 3D->2D
        for (Module m : modules) {
            if (m.isEnabled()) m.onRenderWorld(event.partialTicks);
        }
    }

    @SubscribeEvent
    public void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        if (mc.thePlayer == null) return;
        for (Module m : modules) {
            if (m.isEnabled()) m.onCameraSetup(event);
        }
    }

    @SubscribeEvent
    public void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
        for (Module m : modules) {
            if (m.isEnabled()) m.onDrawBlockHighlight(event);
        }
    }

    // ====================== Persistance ======================

    /** Fichier du profil actif. */
    private File currentFile() {
        return activeProfile.equals("default")
                ? configFile
                : new File(profilesDir, sanitize(activeProfile) + ".cfg");
    }

    public String activeProfile() { return activeProfile; }

    /** Liste des profils ("default" + fichiers .cfg). */
    public List<String> listProfiles() {
        List<String> out = new ArrayList<String>();
        out.add("default");
        File[] files = profilesDir.listFiles();
        if (files != null) {
            for (File f : files) {
                String n = f.getName();
                if (n.endsWith(".cfg")) out.add(n.substring(0, n.length() - 4));
            }
        }
        return out;
    }

    /** Sauve l'état actuel sous le profil donné et le rend actif. */
    public void saveProfileAs(String name) {
        if (name == null || name.trim().isEmpty()) return;
        activeProfile = name.trim().equals("default") ? "default" : sanitize(name.trim());
        save();
        writeMeta();
    }

    /** Charge un profil (et applique tous les réglages). */
    public void loadProfile(String name) {
        if (name == null) return;
        activeProfile = name.equals("default") ? "default" : sanitize(name);
        load();
        writeMeta();
    }

    public void deleteProfile(String name) {
        if (name == null || name.equals("default")) return;
        File f = new File(profilesDir, sanitize(name) + ".cfg");
        if (f.exists()) f.delete();
        if (activeProfile.equals(sanitize(name))) loadProfile("default");
    }

    private static String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }

    public void save() {
        File target = currentFile();
        if (target == null) return;
        Properties p = new Properties();
        p.setProperty("guiKey", Integer.toString(guiKey));
        p.setProperty("friends", FriendManager.write());
        for (Module m : modules) {
            String base = m.name + ".";
            p.setProperty(base + "enabled", Boolean.toString(m.isEnabled()));
            p.setProperty(base + "key", Integer.toString(m.key));
            p.setProperty(base + "hudX", Integer.toString(m.hudX));
            p.setProperty(base + "hudY", Integer.toString(m.hudY));
            for (Setting s : m.settings) {
                p.setProperty(base + "s." + s.name, s.write());
            }
        }
        try {
            FileOutputStream out = new FileOutputStream(target);
            try { p.store(out, "Daniel Client - profil " + activeProfile); }
            finally { out.close(); }
        } catch (Exception e) {
            System.err.println("[Client] Erreur ecriture config: " + e.getMessage());
        }
    }

    public void load() {
        File target = currentFile();
        if (target == null || !target.exists()) { save(); return; }
        Properties p = new Properties();
        try {
            FileInputStream in = new FileInputStream(target);
            try { p.load(in); } finally { in.close(); }
        } catch (Exception e) {
            System.err.println("[Client] Erreur lecture config: " + e.getMessage());
            return;
        }
        try { guiKey = Integer.parseInt(p.getProperty("guiKey", Integer.toString(guiKey))); }
        catch (Exception ignored) {}
        FriendManager.read(p.getProperty("friends", ""));

        for (Module m : modules) {
            String base = m.name + ".";
            try { m.key = Integer.parseInt(p.getProperty(base + "key", Integer.toString(m.key))); } catch (Exception ignored) {}
            try { m.hudX = Integer.parseInt(p.getProperty(base + "hudX", Integer.toString(m.hudX))); } catch (Exception ignored) {}
            try { m.hudY = Integer.parseInt(p.getProperty(base + "hudY", Integer.toString(m.hudY))); } catch (Exception ignored) {}
            for (Setting s : m.settings) {
                String v = p.getProperty(base + "s." + s.name);
                if (v != null) s.read(v);
            }
            boolean en = Boolean.parseBoolean(
                    p.getProperty(base + "enabled", Boolean.toString(m.isEnabled())));
            m.setEnabled(en);
        }
    }

    // ---- profil actif mémorisé entre deux sessions ----
    private void readMeta() {
        if (metaFile == null || !metaFile.exists()) return;
        Properties p = new Properties();
        try {
            FileInputStream in = new FileInputStream(metaFile);
            try { p.load(in); } finally { in.close(); }
            activeProfile = p.getProperty("active", "default");
        } catch (Exception ignored) {}
    }

    private void writeMeta() {
        if (metaFile == null) return;
        Properties p = new Properties();
        p.setProperty("active", activeProfile);
        try {
            FileOutputStream out = new FileOutputStream(metaFile);
            try { p.store(out, "Daniel Client - meta"); } finally { out.close(); }
        } catch (Exception ignored) {}
    }
}
