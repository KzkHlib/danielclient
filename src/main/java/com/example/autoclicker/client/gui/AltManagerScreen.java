package com.example.autoclicker.client.gui;

import com.example.autoclicker.client.Theme;
import com.example.autoclicker.client.auth.Account;
import com.example.autoclicker.client.auth.AltManager;
import com.example.autoclicker.client.auth.SessionUtil;
import com.example.autoclicker.client.render.Render2D;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Alt Manager : grille de cartes de comptes (tête, pseudo, dernière
 * connexion, actions Rename/Delete), clic sur une carte = jouer avec ce compte,
 * boutons Add Account / Cancel en bas.
 */
public class AltManagerScreen extends GuiScreen {

    private static final int CARD_W = 184, CARD_H = 54, GAP = 10, COLS = 3;
    private static final SimpleDateFormat DATE = new SimpleDateFormat("dd/MM/yyyy");

    private final GuiScreen parent;
    private int scroll = 0;
    private String status = "";

    private Account renaming;
    private GuiTextField renameField;

    public AltManagerScreen(GuiScreen parent) { this.parent = parent; }

    @Override public boolean doesGuiPauseGame() { return false; }

    @Override
    public void initGui() {
        renameField = new GuiTextField(0, fontRendererObj, width / 2 - 80, 30, 160, 18);
        renameField.setMaxStringLength(16);
    }

    private int gridLeft() {
        int totalW = COLS * CARD_W + (COLS - 1) * GAP;
        return (width - totalW) / 2;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float pt) {
        GuiFx.render(width, height);

        List<Account> accs = AltManager.accounts();
        int left = gridLeft();
        int top = 24 - scroll;

        for (int i = 0; i < accs.size(); i++) {
            int col = i % COLS, row = i / COLS;
            int x = left + col * (CARD_W + GAP);
            int y = top + row * (CARD_H + GAP);
            if (y + CARD_H < 0 || y > height - 40) continue;
            drawCard(accs.get(i), x, y, mouseX, mouseY);
        }

        if (accs.isEmpty())
            drawCentered("Aucun compte. Clique sur Add Account.", height / 2 - 10, 0xFF9A9AAA);

        // barre du bas
        Gui.drawRect(0, height - 34, width, height, 0xC00B0B12);
        button("Add Account", width / 2 - 130, height - 28, 120, mouseX, mouseY, Theme.ACCENT);
        button("Cancel", width / 2 + 10, height - 28, 120, mouseX, mouseY, 0xFF3A3A48);
        if (!status.isEmpty())
            drawCentered(status, height - 44, Theme.ACCENT2);

        // overlay de renommage
        if (renaming != null) {
            Gui.drawRect(0, 0, width, height, 0xA0000000);
            drawCentered("Renommer " + renaming.name, 14, Theme.TEXT);
            renameField.drawTextBox();
            drawCentered("Entrée = valider, Échap = annuler", 54, 0xFF8A8A98);
        }

        super.drawScreen(mouseX, mouseY, pt);
    }

    private void drawCard(Account a, int x, int y, int mouseX, int mouseY) {
        boolean active = a.name != null && a.name.equals(SessionUtil.currentName());
        boolean hover = inside(mouseX, mouseY, x, y, CARD_W, CARD_H) && renaming == null;
        Render2D.roundedRect(x, y, x + CARD_W, y + CARD_H, 6, hover ? 0xF01C1C2A : 0xE0141420);
        Render2D.roundedRect(x + 6, y, x + CARD_W - 6, y + 2, 1, active ? 0xFF2ECC71 : Theme.ACCENT);
        if (active) Render2D.roundedRect(x, y + 6, x + 2, y + CARD_H - 6, 1, 0xFF2ECC71);

        GuiSkin.drawHead(x + 9, y + 9, 36);

        int tx = x + 54;
        fontRendererObj.drawStringWithShadow(a.name == null ? "?" : a.name, tx, y + 8, Theme.TEXT);
        String last = "Last login: " + DATE.format(new Date(a.lastLogin == 0 ? a.addedAt : a.lastLogin));
        fontRendererObj.drawStringWithShadow(last, tx, y + 22, 0xFF9A9AAA);
        // actions
        fontRendererObj.drawStringWithShadow("Actions:", tx, y + 38, 0xFFB0B0C0);
        int ax = tx + fontRendererObj.getStringWidth("Actions: ");
        tag("Rename", ax, y + 37, 0xFFE7C24C);
        int del = ax + fontRendererObj.getStringWidth("Rename") + 10;
        tag("Delete", del, y + 37, 0xFFE74C3C);
        // pastille type
        String badge = a.isOffline() ? "OFF" : "MS";
        int bw = fontRendererObj.getStringWidth(badge) + 6;
        Gui.drawRect(x + CARD_W - bw - 4, y + 6, x + CARD_W - 4, y + 16, a.isOffline() ? 0xFF555560 : 0xFF2A6CDb);
        fontRendererObj.drawStringWithShadow(badge, x + CARD_W - bw - 1, y + 7, 0xFFFFFFFF);
    }

    private void tag(String label, int x, int y, int color) {
        int w = fontRendererObj.getStringWidth(label) + 6;
        Render2D.roundedRect(x - 2, y, x + w - 2, y + 11, 2.5f, (color & 0x00FFFFFF) | 0x40000000);
        fontRendererObj.drawStringWithShadow(label, x + 1, y + 2, color);
    }

    private void button(String label, int x, int y, int w, int mouseX, int mouseY, int color) {
        boolean hover = inside(mouseX, mouseY, x, y, w, 20);
        Render2D.roundedRect(x, y, x + w, y + 20, 5, hover ? 0xFF22222E : 0xFF17171F);
        Render2D.roundedRect(x + 5, y + 18, x + w - 5, y + 20, 1, color);
        fontRendererObj.drawStringWithShadow(label, x + (w - fontRendererObj.getStringWidth(label)) / 2, y + 6,
                hover ? Theme.TEXT : 0xFFC0C0CE);
    }

    private void drawCentered(String s, int y, int color) {
        fontRendererObj.drawStringWithShadow(s, (width - fontRendererObj.getStringWidth(s)) / 2, y, color);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        if (renaming != null) { renameField.mouseClicked(mouseX, mouseY, button); return; }

        // boutons bas
        if (inside(mouseX, mouseY, width / 2 - 130, height - 28, 120, 20)) {
            mc.displayGuiScreen(new LoginMethodScreen(this));
            return;
        }
        if (inside(mouseX, mouseY, width / 2 + 10, height - 28, 120, 20)) {
            mc.displayGuiScreen(parent);
            return;
        }

        // cartes
        List<Account> accs = AltManager.accounts();
        int left = gridLeft();
        int top = 24 - scroll;
        for (int i = 0; i < accs.size(); i++) {
            int col = i % COLS, row = i / COLS;
            int x = left + col * (CARD_W + GAP);
            int y = top + row * (CARD_H + GAP);
            Account a = accs.get(i);
            int tx = x + 54;
            int ax = tx + fontRendererObj.getStringWidth("Actions: ");
            int renW = fontRendererObj.getStringWidth("Rename") + 6;
            int del = ax + fontRendererObj.getStringWidth("Rename") + 10;
            int delW = fontRendererObj.getStringWidth("Delete") + 6;

            if (inside(mouseX, mouseY, ax - 2, y + 37, renW, 11)) {
                renaming = a; renameField.setText(a.name == null ? "" : a.name); renameField.setFocused(true);
                return;
            }
            if (inside(mouseX, mouseY, del - 2, y + 37, delW, 11)) {
                AltManager.remove(a); return;
            }
            if (inside(mouseX, mouseY, x, y, CARD_W, CARD_H)) {
                boolean ok = SessionUtil.apply(a);
                AltManager.save();
                status = ok ? ("Connecté : " + a.name) : "Échec";
                return;
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = org.lwjgl.input.Mouse.getDWheel();
        if (wheel != 0) { scroll -= (wheel > 0 ? 1 : -1) * 24; if (scroll < 0) scroll = 0; }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (renaming != null) {
            if (keyCode == 1) { renaming = null; return; }            // Échap
            if (keyCode == 28) {                                      // Entrée
                String n = renameField.getText().trim();
                if (!n.isEmpty()) { renaming.name = n; AltManager.save(); }
                renaming = null;
                return;
            }
            renameField.textboxKeyTyped(typedChar, keyCode);
            return;
        }
        if (keyCode == 1) mc.displayGuiScreen(parent);
    }

    @Override public void updateScreen() { if (renameField != null) renameField.updateCursorCounter(); }

    private boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
