package com.example.autoclicker.client.gui;

import com.example.autoclicker.client.Category;
import com.example.autoclicker.client.Client;
import com.example.autoclicker.client.Descriptions;
import com.example.autoclicker.client.Module;
import com.example.autoclicker.client.Setting;
import com.example.autoclicker.client.Sounds;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClickGuiScreen extends GuiScreen {

    // ── couleurs ──
    private static final int BG          = 0xFF1E1E1E;
    private static final int HEADER_BG   = 0xFF242424;
    private static final int HOVER_BG    = 0xFF2C2C2C;
    private static final int SELECTED_BG = 0xFF2A203A;
    private static final int ACCENT      = 0xFF8B5CF6;
    private static final int ACCENT_DIM  = 0xFF6B3FD6;
    private static final int TEXT        = 0xFFFFFFFF;
    private static final int TEXT_SEC    = 0xFFAAAAAA;
    private static final int TEXT_DIM    = 0xFF666666;
    private static final int SETTINGS_BG = 0xFF1A1A1A;
    private static final int SETTINGS_BD = 0xFF2A2A2A;
    private static final int SCROLL_TRACK = 0xFF2A2A2A;
    private static final int OVERLAY      = 0x60000000;

    private static final int PANEL_W = 130;
    private static final int HEADER_H = 22;
    private static final int ROW_H = 20;
    private static final int GAP = 6;
    private static final int CORNER = 5;
    private static final int MAX_PANEL_H = 320;

    // ── icônes catégories ──
    private static final String[] CAT_ICONS = {
        "\u2694", "\u26A1", "\u25C9", "\u25CF", "\u25C8", "\u2699"
    };

    // ── classes internes ──

    private static class AnimatedFloat {
        float value, target;
        float speed = 0.18f;
        void update() { value += (target - value) * speed; if (Math.abs(value - target) < 0.001f) value = target; }
        void set(float v) { target = value = v; }
        boolean done() { return value == target; }
    }

    private class Panel {
        final Category cat;
        final String label;
        final String icon;
        float x, y;
        int w = PANEL_W;
        int h;
        float scroll, scrollTarget;
        boolean collapsed;
        boolean dragging;
        int dragOffX, dragOffY;
        final AnimatedFloat animOpen = new AnimatedFloat();
        final AnimatedFloat animCol = new AnimatedFloat();
        final Map<Module, AnimatedFloat> toggleAnims = new HashMap<Module, AnimatedFloat>();
        final Map<Module, AnimatedFloat> hoverAnims = new HashMap<Module, AnimatedFloat>();

        Panel(Category cat, String icon, int idx) {
            this.cat = cat;
            this.icon = icon;
            this.label = cat.displayName;
            ScaledResolution sr = new ScaledResolution(mc);
            int totalW = Client.INSTANCE != null ? Client.INSTANCE.modulesIn(cat).size() : 0;
            int rows = Math.max(4, totalW);
            this.h = Math.min(HEADER_H + rows * ROW_H + 4, MAX_PANEL_H);
            int gap = GAP;
            int startX = 8;
            this.x = startX + idx * (PANEL_W + gap);
            this.y = 30;
            animOpen.set(0f);
            animOpen.target = 1f;
        }

        void updateAnims() {
            animOpen.update();
            animCol.update();
            if (collapsed) animCol.target = 1f; else animCol.target = 0f;
        }

        int contentHeight() {
            int count = Client.INSTANCE != null ? Client.INSTANCE.modulesIn(cat).size() : 0;
            if (count == 0) return 0;
            int sh = 0;
            for (Module m : Client.INSTANCE.modulesIn(cat)) {
                sh += ROW_H;
                if (expandedModule == m) {
                    sh += m.settings.size() * SET_H;
                    sh += SET_H; // bind row
                }
            }
            return sh;
        }

        int visibleH() {
            return h - HEADER_H;
        }

        void draw(int mx, int my) {
            if (animOpen.value < 0.01f) return;

            float s = animOpen.value;
            // scale entry animation
            float scale = 0.92f + 0.08f * s;
            float alpha = s;

            int drawX = (int) x;
            int drawY = (int) y;
            int drawW = w;
            int drawH = collapsed ? HEADER_H : h;

            // shadow
            int sh = 4;
            Gui.drawRect(drawX + sh, drawY + sh, drawX + drawW + sh, drawY + drawH + sh, 0x44000000);
            Gui.drawRect(drawX + sh + 1, drawY + sh + 1, drawX + drawW + sh - 1, drawY + drawH + sh - 1, 0x22000000);

            // panel bg
            drawRound(drawX, drawY, drawW, drawH, CORNER, BG);

            // header
            drawRound(drawX, drawY, drawW, HEADER_H, CORNER, HEADER_BG);
            Gui.drawRect(drawX + 2, drawY + HEADER_H - 1, drawX + drawW - 2, drawY + HEADER_H, 0xFF3A3A3A);

            // cat icon + name
            fontRendererObj.drawStringWithShadow(icon + " " + label, drawX + 6, drawY + 6, TEXT);

            // collapse button
            String colSym = collapsed ? "+" : "-";
            fontRendererObj.drawStringWithShadow(colSym, drawX + drawW - 12, drawY + 5, TEXT_DIM);

            if (collapsed) return;

            // modules
            int x0 = drawX + 2;
            int y0 = drawY + HEADER_H + 2;
            int listW = drawW - 4;
            int listH = drawH - HEADER_H - 4;

            scroll += (scrollTarget - scroll) * 0.15f;
            if (scroll < 0) scroll = 0;
            int scrollOff = Math.round(scroll);

            int yy = y0 - scrollOff;

            List<Module> mods = Client.INSTANCE != null ? Client.INSTANCE.modulesIn(cat) : new ArrayList<Module>();
            for (Module m : mods) {
                boolean vis = yy >= y0 - ROW_H && yy <= y0 + listH;
                if (vis) drawModule(m, x0, yy, listW, mx, my);
                yy += ROW_H;
                if (expandedModule == m) {
                    for (Setting set : m.settings) {
                        if (yy >= y0 - SET_H && yy <= y0 + listH) drawSettingRow(m, x0, yy, listW, set);
                        yy += SET_H;
                    }
                    // bind row
                    if (yy >= y0 - SET_H && yy <= y0 + listH) drawBindRow(m, x0, yy, listW);
                    yy += SET_H;
                }
            }

            // clamp scroll
            int totalH = Client.INSTANCE != null ? contentHeight() : 0;
            int maxS = Math.max(0, totalH - listH);
            if (scrollTarget > maxS) scrollTarget = maxS;
            if (scrollTarget < 0) scrollTarget = 0;
            scroll = Math.min(scroll, maxS);

            // scrollbar
            if (totalH > listH) {
                int sbX = drawX + drawW - 3;
                int sbY = y0;
                int sbH = listH;
                float ratio = listH / (float) totalH;
                int barH = (int) (sbH * ratio);
                int barY = sbY + (int) ((scroll / (float) (totalH - listH)) * (sbH - barH));
                Gui.drawRect(sbX, sbY, sbX + 2, sbY + sbH, SCROLL_TRACK);
                Gui.drawRect(sbX, barY, sbX + 2, barY + barH, ACCENT_DIM);
            }
        }

        private void drawModule(Module m, int x, int y, int w, int mx, int my) {
            boolean hov = mx >= x && mx <= x + w && my >= y && my <= y + ROW_H;

            AnimatedFloat ta = toggleAnims.get(m);
            if (ta == null) { ta = new AnimatedFloat(); ta.set(m.isEnabled() ? 1f : 0f); toggleAnims.put(m, ta); }
            ta.target = m.isEnabled() ? 1f : 0f;
            ta.update();

            AnimatedFloat ha = hoverAnims.get(m);
            if (ha == null) { ha = new AnimatedFloat(); ha.set(0f); hoverAnims.put(m, ha); }
            ha.target = hov ? 1f : 0f;
            ha.update();

            int bg = BG;
            if (ta.value > 0.01f) bg = lerpColor(bg, SELECTED_BG, ta.value);
            if (ha.value > 0.01f) bg = lerpColor(bg, HOVER_BG, ha.value);
            if (hov && ta.value < 0.5f) bg = HOVER_BG;

            drawRound(x, y, w, ROW_H, 3, bg);

            if (ta.value > 0.01f) {
                Gui.drawRect(x, y + 2, x + 2, y + ROW_H - 2, ACCENT);
            }

            int col = ta.value > 0.5f ? TEXT : TEXT_SEC;
            fontRendererObj.drawStringWithShadow(m.name, x + 8, y + 6, col);
        }

        private void drawSettingRow(Module m, int x, int y, int w, Setting s) {
            Gui.drawRect(x, y, x + w, y + SET_H, 0xFF141414);
            fontRendererObj.drawStringWithShadow(s.name, x + 8, y + 2, TEXT_DIM);

            if (s instanceof Setting.Bool) {
                Setting.Bool b = (Setting.Bool) s;
                int bx = x + w - 18, by = y + 2;
                drawRound(bx, by, 10, 7, 3, b.value ? ACCENT : 0xFF3A3A3A);
            } else if (s instanceof Setting.Mode) {
                Setting.Mode md = (Setting.Mode) s;
                String cur = md.current();
                fontRendererObj.drawStringWithShadow(cur, x + w - 4 - fontRendererObj.getStringWidth(cur), y + 2, ACCENT_DIM);
            } else if (s instanceof Setting.Number) {
                Setting.Number n = (Setting.Number) s;
                String val = n.display();
                fontRendererObj.drawStringWithShadow(val, x + w - 4 - fontRendererObj.getStringWidth(val), y + 2, ACCENT_DIM);
                int tx0 = x + 6, tx1 = x + w - 22, ty = y + SET_H - 2;
                Gui.drawRect(tx0, ty, tx1, ty + 1, 0xFF2A2A2A);
                int fill = tx0 + (int) ((tx1 - tx0) * n.slider());
                Gui.drawRect(tx0, ty, fill, ty + 1, ACCENT);
            }
        }

        private void drawBindRow(Module m, int x, int y, int w) {
            Gui.drawRect(x, y, x + w, y + SET_H, 0xFF141414);
            fontRendererObj.drawStringWithShadow("Keybind", x + 8, y + 2, TEXT_DIM);
            String kn = (bindingModule == m) ? "..." : (m.keyName() != null ? m.keyName() : "None");
            fontRendererObj.drawStringWithShadow(kn, x + w - 4 - fontRendererObj.getStringWidth(kn), y + 2, ACCENT_DIM);
        }

        boolean isMouseOver(int mx, int my) {
            int dh = collapsed ? HEADER_H : h;
            return mx >= x && mx <= x + w && my >= y && my <= y + dh;
        }

        boolean isOverHeader(int mx, int my) {
            return mx >= x && mx <= x + w && my >= y && my <= y + HEADER_H;
        }

        boolean isOverCollapse(int mx, int my) {
            return mx >= x + w - 16 && mx <= x + w && my >= y && my <= y + HEADER_H;
        }

        void handleClick(int mx, int my, int button) {
            if (isOverCollapse(mx, my)) {
                collapsed = !collapsed;
                Sounds.click();
                return;
            }
            if (isOverHeader(mx, my)) {
                dragging = true;
                dragOffX = (int) (mx - x);
                dragOffY = (int) (my - y);
                draggedPanel = this;
                return;
            }

            List<Module> mods = Client.INSTANCE != null ? Client.INSTANCE.modulesIn(cat) : new ArrayList<Module>();
            int x0 = (int) x + 2;
            int y0 = (int) y + HEADER_H + 2;
            int listW = w - 4;
            int scrollOff = Math.round(scroll);
            int yy = y0 - scrollOff;

            for (Module m : mods) {
                if (mx >= x0 && mx <= x0 + listW && my >= yy && my <= yy + ROW_H) {
                    if (button == 1) {
                        if (expandedModule == m) expandedModule = null;
                        else { expandedModule = m; expandedPanel = this; }
                    } else {
                        m.toggle();
                    }
                    Sounds.click();
                    return;
                }
                yy += ROW_H;
                if (expandedModule == m) {
                    for (Setting s : m.settings) {
                        if (mx >= x0 && mx <= x0 + listW && my >= yy && my <= yy + SET_H) {
                            settingClick(m, s, button, mx, x0, listW);
                            Sounds.click();
                            return;
                        }
                        yy += SET_H;
                    }
                    if (mx >= x0 && mx <= x0 + listW && my >= yy && my <= yy + SET_H) {
                        bindingModule = m;
                        Sounds.click();
                        return;
                    }
                    yy += SET_H;
                }
            }
        }

        void handleScroll(int wheel) {
            if (collapsed) return;
            scrollTarget -= (wheel > 0 ? 1 : -1) * 20;
            if (scrollTarget < 0) scrollTarget = 0;
            int totalH = contentHeight();
            int listH = visibleH() - 4;
            int maxS = Math.max(0, totalH - listH);
            if (scrollTarget > maxS) scrollTarget = maxS;
        }
    }

    // ── settings constants ──
    private static final int SET_H = 11;
    private static final int SETTINGS_W = 140;
    private static final int SETTINGS_MAX_H = 250;

    // ── champs ──
    private List<Panel> panels;
    private Panel draggedPanel;
    private Module expandedModule;
    private Panel expandedPanel;
    private Module bindingModule;
    private long openTime;
    private final Map<Module, AnimatedFloat> globalToggleAnims = new HashMap<Module, AnimatedFloat>();

    // ── cycle de vie ──

    @Override
    public void initGui() {
        panels = new ArrayList<Panel>();
        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; i++) {
            panels.add(new Panel(cats[i], i < CAT_ICONS.length ? CAT_ICONS[i] : "?", i));
        }
        draggedPanel = null;
        openTime = System.currentTimeMillis();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    // ── rendu ──

    @Override
    public void drawScreen(int mx, int my, float pt) {
        // overlay
        Gui.drawRect(0, 0, width, height, OVERLAY);

        // update panel animations
        for (Panel p : panels) p.updateAnims();

        // draw panels
        for (int i = panels.size() - 1; i >= 0; i--) {
            Panel p = panels.get(i);
            if (p != draggedPanel) p.draw(mx, my);
        }
        if (draggedPanel != null) draggedPanel.draw(mx, my);

        // tooltip
        if (hoveredModule(mx, my) != null) {
            Module hm = hoveredModule(mx, my);
            String desc = Descriptions.module(hm.name);
            if (desc != null) drawTooltip(desc, mx, my);
        }

        super.drawScreen(mx, my, pt);
    }

    // ── input ──

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getDWheel();
        if (wheel != 0) {
            for (Panel p : panels) {
                if (p.isMouseOver(mx(), my())) {
                    p.handleScroll(wheel);
                    break;
                }
            }
        }
    }

    @Override
    protected void mouseClicked(int mx, int my, int button) throws IOException {
        super.mouseClicked(mx, my, button);

        // bring panel to front
        for (int i = panels.size() - 1; i >= 0; i--) {
            Panel p = panels.get(i);
            if (p.isMouseOver(mx, my)) {
                panels.remove(i);
                panels.add(p);
                p.handleClick(mx, my, button);
                return;
            }
        }

        // click outside → collapse expanded
        expandedModule = null;
        expandedPanel = null;
        bindingModule = null;
    }

    @Override
    protected void mouseReleased(int mx, int my, int state) {
        super.mouseReleased(mx, my, state);
        if (draggedPanel != null) {
            draggedPanel.dragging = false;
            draggedPanel = null;
        }
    }

    @Override
    protected void keyTyped(char c, int key) throws IOException {
        if (bindingModule != null) {
            if (key == Keyboard.KEY_ESCAPE || key == Keyboard.KEY_DELETE || key == Keyboard.KEY_BACK) {
                bindingModule.key = 0;
            } else {
                bindingModule.key = key;
            }
            bindingModule = null;
            return;
        }
        if (key == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null);
            return;
        }
    }

    @Override
    public void onGuiClosed() {
        if (Client.INSTANCE != null) Client.INSTANCE.save();
    }

    // ── helpers ──

    private int mx() { return Mouse.getX() * width / mc.displayWidth; }
    private int my() { return height - Mouse.getY() * height / mc.displayHeight - 1; }

    private Module hoveredModule(int mx, int my) {
        for (Panel p : panels) {
            if (!p.isMouseOver(mx, my) || p.collapsed) continue;
            List<Module> mods = Client.INSTANCE != null ? Client.INSTANCE.modulesIn(p.cat) : new ArrayList<Module>();
            int x0 = (int) p.x + 2;
            int y0 = (int) p.y + HEADER_H + 2;
            int scrollOff = Math.round(p.scroll);
            int yy = y0 - scrollOff;
            for (Module m : mods) {
                if (mx >= x0 && mx <= x0 + p.w - 4 && my >= yy && my <= yy + ROW_H) return m;
                yy += ROW_H;
                if (expandedModule == m) yy += m.settings.size() * SET_H + SET_H;
            }
        }
        return null;
    }

    private void settingClick(Module m, Setting s, int button, int mx, int x0, int w) {
        if (s instanceof Setting.Bool) {
            ((Setting.Bool) s).toggle();
        } else if (s instanceof Setting.Mode) {
            ((Setting.Mode) s).cycle();
        } else if (s instanceof Setting.Number) {
            Setting.Number n = (Setting.Number) s;
            int tx0 = x0 + 6, tx1 = x0 + w - 22;
            double ratio = (double) (mx - tx0) / (tx1 - tx0);
            n.setFromSlider(ratio);
        }
    }

    // ── render helpers ──

    private void drawRound(int x, int y, int w, int h, int r, int argb) {
        if (w <= 0 || h <= 0) return;
        int r2 = Math.min(r, Math.min(w, h) / 2);
        float a = (argb >> 24 & 0xFF) / 255f;
        float rd = (argb >> 16 & 0xFF) / 255f;
        float g = (argb >> 8 & 0xFF) / 255f;
        float b = (argb & 0xFF) / 255f;

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();
        GlStateManager.color(rd, g, b, a);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x + r2, y); GL11.glVertex2f(x + w - r2, y);
        GL11.glVertex2f(x + w - r2, y + h); GL11.glVertex2f(x + r2, y + h);
        GL11.glEnd();
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y + r2); GL11.glVertex2f(x + r2, y + r2);
        GL11.glVertex2f(x + r2, y + h - r2); GL11.glVertex2f(x, y + h - r2);
        GL11.glEnd();
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x + w - r2, y + r2); GL11.glVertex2f(x + w, y + r2);
        GL11.glVertex2f(x + w, y + h - r2); GL11.glVertex2f(x + w - r2, y + h - r2);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
        glFan(x + r2, y + r2, r2, 180);
        glFan(x + w - r2, y + r2, r2, 270);
        glFan(x + w - r2, y + h - r2, r2, 0);
        glFan(x + r2, y + h - r2, r2, 90);
        GL11.glDisable(GL11.GL_POLYGON_SMOOTH);

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1, 1, 1, 1);
    }

    private void glFan(float cx, float cy, float r, int startDeg) {
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(cx, cy);
        for (int i = 0; i <= 90; i += 10) {
            double ang = Math.toRadians(startDeg + i);
            GL11.glVertex2f(cx + (float) Math.cos(ang) * r, cy + (float) Math.sin(ang) * r);
        }
        GL11.glEnd();
    }

    private void drawTooltip(String text, int mx, int my) {
        int tw = fontRendererObj.getStringWidth(text);
        int tx = mx + 10;
        int ty = my + 6;
        if (tx + tw + 8 > width) tx = width - tw - 10;
        Gui.drawRect(tx - 4, ty - 4, tx + tw + 4, ty + 10, 0xEE1E1E1E);
        Gui.drawRect(tx - 4, ty - 4, tx + tw + 4, ty - 2, ACCENT);
        fontRendererObj.drawStringWithShadow(text, tx, ty, TEXT);
    }

    private int lerpColor(int a, int b, float t) {
        if (t <= 0) return a; if (t >= 1) return b;
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int or = (int) (ar + (br - ar) * t);
        int og = (int) (ag + (bg - ag) * t);
        int ob = (int) (ab + (bb - ab) * t);
        return 0xFF000000 | (or << 16) | (og << 8) | ob;
    }
}
