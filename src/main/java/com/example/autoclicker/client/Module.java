package com.example.autoclicker.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

/**
 * Module de base. Un module a un nom, une catégorie, une touche optionnelle,
 * une liste de réglages, et des hooks appelés par {@link Client}.
 */
public abstract class Module {

    protected final Minecraft mc = Minecraft.getMinecraft();

    public final String name;
    public final Category category;
    /** Code touche LWJGL (0 = aucune). */
    public int key = 0;
    /** Faut-il afficher ce module dans l'ArrayList quand il est actif ? */
    public boolean showInArrayList = true;
    /** true = la touche liée doit être MAINTENUE (ex. Zoom), pas un toggle. */
    public boolean holdKey = false;

    /** Position HUD pour les modules de rendu (-1 = position par défaut). */
    public int hudX = -1, hudY = -1;

    private boolean enabled = false;
    public final List<Setting> settings = new ArrayList<Setting>();

    protected Module(String name, Category category) {
        this.name = name;
        this.category = category;
    }

    protected Setting add(Setting s) { settings.add(s); return s; }

    public boolean isEnabled() { return enabled; }

    public void toggle() { setEnabled(!enabled); }

    public void setEnabled(boolean state) {
        if (enabled == state) return;
        enabled = state;
        if (enabled) onEnable(); else onDisable();
        if (Client.INSTANCE != null) Client.INSTANCE.onModuleToggled(this);
    }

    /** Suffixe affiché à côté du nom dans l'ArrayList (ex. "17-20"). */
    public String arrayListSuffix() { return null; }

    /** Nom lisible de la touche liée, ou null si aucune. */
    public String keyName() {
        if (key == 0) return null;
        try {
            String n = Keyboard.getKeyName(key);
            return (n == null || n.length() == 0) ? null : n;
        } catch (Exception e) {
            return null;
        }
    }

    // ---- hooks (override au besoin) ----
    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
    /** Rendu HUD (modules de catégorie RENDER). */
    public void onRenderHud(ScaledResolution sr) {}
    /** Rendu dans le monde 3D (ESP, Tracers). partialTicks = interpolation frame. */
    public void onRenderWorld(float partialTicks) {}
    /** Appelé à chaque FRAME (dt = secondes écoulées). Pour le lissage de visée. */
    public void onRender(float dt) {}
    /** Réglage de la caméra (FreeLook). */
    public void onCameraSetup(EntityViewRenderEvent.CameraSetup e) {}
    /** Surbrillance du bloc visé (BlockOverlay). */
    public void onDrawBlockHighlight(DrawBlockHighlightEvent e) {}
    /** Quand le joueur attaque une entité. */
    public void onAttackEntity(AttackEntityEvent e) {}

    // ---- positionnement HUD générique (déplaçable dans le ClickGUI) ----
    /** Largeur/hauteur du bloc HUD ; 0 = non déplaçable. */
    public int hudW(ScaledResolution sr) { return 0; }
    public int hudH(ScaledResolution sr) { return 0; }
    /** Position par défaut si l'utilisateur n'a rien déplacé. */
    protected int hudDefX(ScaledResolution sr) { return 2; }
    protected int hudDefY(ScaledResolution sr) { return 2; }
    /** Position effective (réglée par l'utilisateur ou défaut). */
    public final int getHudX(ScaledResolution sr) { return hudX >= 0 ? hudX : hudDefX(sr); }
    public final int getHudY(ScaledResolution sr) { return hudY >= 0 ? hudY : hudDefY(sr); }
}
