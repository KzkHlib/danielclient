package com.example.autoclicker.client.gui;

import com.example.autoclicker.client.Theme;
import com.example.autoclicker.client.auth.Account;
import com.example.autoclicker.client.auth.AltManager;
import com.example.autoclicker.client.auth.MicrosoftAuth;
import com.example.autoclicker.client.render.Render2D;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;

/** "Select your login method" : Microsoft + Hors Ligne. */
public class LoginMethodScreen extends GuiScreen {

    private static final int CHOOSE = 0, MICROSOFT = 1, OFFLINE = 2;

    private final GuiScreen parent;
    private int stage = CHOOSE;

    // carte dimensions
    private static final int CW = 170, CH = 190, CGAP = 24;

    // micro flow
    private volatile boolean adding = false;
    private volatile String status = "", userCode = "", verifUri = "";
    private GuiTextField offlineField;

    public LoginMethodScreen(GuiScreen parent) { this.parent = parent; }

    @Override public boolean doesGuiPauseGame() { return false; }

    @Override
    public void initGui() {
        offlineField = new GuiTextField(0, fontRendererObj, width / 2 - 100, height / 2 - 44, 200, 20);
        offlineField.setMaxStringLength(16);
    }

    private int cardsLeft() { return width / 2 - (CW + CGAP / 2); }
    private int cardsTop() { return height / 2 - CH / 2; }

    @Override
    public void drawScreen(int mouseX, int mouseY, float pt) {
        GuiFx.render(width, height);
        String title = stage == CHOOSE ? "Select your login method"
                : stage == MICROSOFT ? "Microsoft" : "Select your username";
        drawCentered(title, height / 6, Theme.TEXT, 2f);

        if (stage == CHOOSE) {
            int left = cardsLeft(), top = cardsTop();
            drawCard("Microsoft", left, top, mouseX, mouseY, 0);
            drawCard("Hors Ligne", left + CW + CGAP, top, mouseX, mouseY, 1);
        } else if (stage == MICROSOFT) {
            drawMicrosoft(mouseX, mouseY);
        } else {
            drawOffline(mouseX, mouseY);
        }

        // retour
        button("Retour", 16, height - 30, 90, mouseX, mouseY);
        super.drawScreen(mouseX, mouseY, pt);
    }

    private void drawCard(String label, int x, int y, int mouseX, int mouseY, int icon) {
        boolean hover = inside(mouseX, mouseY, x, y, CW, CH);
        Render2D.roundedRect(x, y, x + CW, y + CH, 8, hover ? 0xF01C1C2A : 0xD0141420);
        Render2D.roundedRect(x + 8, y, x + CW - 8, y + 2, 1, Theme.ACCENT);
        drawCentered2(label, x + CW / 2, y + 24, Theme.TEXT, 1.4f);

        int ix = x + CW / 2 - 24, iy = y + 70;
        if (icon == 0) {                       // logo Microsoft (4 carreaux)
            Gui.drawRect(ix, iy, ix + 22, iy + 22, 0xFFF35325);
            Gui.drawRect(ix + 26, iy, ix + 48, iy + 22, 0xFF81BC06);
            Gui.drawRect(ix, iy + 26, ix + 22, iy + 48, 0xFF05A6F0);
            Gui.drawRect(ix + 26, iy + 26, ix + 48, iy + 48, 0xFFFFBA08);
        } else {                               // hors ligne : bloc gris + tête
            Gui.drawRect(ix, iy, ix + 48, iy + 48, 0xFF3A3A44);
            GuiSkin.drawHead(ix + 8, iy + 8, 32);
        }
    }

    private void drawMicrosoft(int mouseX, int mouseY) {
        int cx = width / 2;
        if (!MicrosoftAuth.configured()) {
            drawCentered("§eConfigure AZURE_CLIENT_ID dans MicrosoftAuth.java", height / 2 - 10, 0xFFE7C24C, 1f);
            drawCentered("(app Azure gratuite, device code activé)", height / 2 + 4, 0xFF8A8A98, 1f);
            return;
        }
        if (!adding) {
            button("Démarrer la connexion", cx - 90, height / 2 - 12, 180, mouseX, mouseY);
            if (!status.isEmpty()) drawCentered(status, height / 2 + 24, Theme.ACCENT2, 1f);
            return;
        }
        if (!userCode.isEmpty()) {
            drawCentered("Va sur §b" + verifUri, height / 2 - 30, Theme.TEXT, 1f);
            drawCentered("Code : §a" + userCode + " §7(clic = copier)", height / 2 - 14, Theme.TEXT, 1f);
        }
        drawCentered("§7" + status, height / 2 + 10, 0xFFB0B0C0, 1f);
    }

    private void drawOffline(int mouseX, int mouseY) {
        // champ Username
        Gui.drawRect(width / 2 - 102, height / 2 - 46, width / 2 + 102, height / 2 - 22, 0xFF0D0D15);
        if (offlineField.getText().isEmpty() && !offlineField.isFocused())
            fontRendererObj.drawString("Username", width / 2 - 96, height / 2 - 38, 0xFF6A6A78);
        offlineField.drawTextBox();

        // Generate random
        button("Generate random", width / 2 - 100, height / 2 - 16, 200, mouseX, mouseY);

        // Add / Login / Cancel
        int left = width / 2 - 145;
        button("Add", left, height / 2 + 16, 90, mouseX, mouseY);
        button("Login", left + 100, height / 2 + 16, 90, mouseX, mouseY);
        button("Cancel", left + 200, height / 2 + 16, 90, mouseX, mouseY);

        fontRendererObj.drawString("Serveur offline / perso uniquement (premium refusé).",
                width / 2 - fontRendererObj.getStringWidth("Serveur offline / perso uniquement (premium refusé).") / 2,
                height / 2 + 44, 0xFF8A8A98);
        if (!status.isEmpty()) drawCentered(status, height / 2 + 58, Theme.ACCENT2, 1f);
    }

    private static final String[] SYL = {"za","ke","ri","no","vex","lu","mi","dra","sko","fy","pho","nyx","ra","ze","tox"};

    private String randomName() {
        java.util.Random r = new java.util.Random();
        int parts = 2 + r.nextInt(2);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts; i++) sb.append(SYL[r.nextInt(SYL.length)]);
        String s = sb.toString();
        s = Character.toUpperCase(s.charAt(0)) + s.substring(1);
        s += (10 + r.nextInt(90));
        return s.length() > 16 ? s.substring(0, 16) : s;
    }

    private void button(String label, int x, int y, int w, int mouseX, int mouseY) {
        boolean hover = inside(mouseX, mouseY, x, y, w, 20);
        Render2D.roundedRect(x, y, x + w, y + 20, 5, hover ? 0xFF22222E : 0xFF17171F);
        Render2D.roundedRect(x + 5, y + 18, x + w - 5, y + 20, 1, Theme.ACCENT);
        fontRendererObj.drawStringWithShadow(label, x + (w - fontRendererObj.getStringWidth(label)) / 2, y + 6,
                hover ? Theme.TEXT : 0xFFC0C0CE);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        if (inside(mouseX, mouseY, 16, height - 30, 90, 20)) { back(); return; }

        if (stage == CHOOSE) {
            int left = cardsLeft(), top = cardsTop();
            if (inside(mouseX, mouseY, left, top, CW, CH)) { stage = MICROSOFT; status = ""; return; }
            if (inside(mouseX, mouseY, left + CW + CGAP, top, CW, CH)) { stage = OFFLINE; status = ""; return; }
            return;
        }
        if (stage == MICROSOFT) {
            if (adding && !userCode.isEmpty()) { copy(userCode); status = "Code copié"; return; }
            if (!adding && MicrosoftAuth.configured()
                    && inside(mouseX, mouseY, width / 2 - 90, height / 2 - 12, 180, 20)) {
                startMicrosoft();
            }
            return;
        }
        if (stage == OFFLINE) {
            offlineField.mouseClicked(mouseX, mouseY, button);
            if (inside(mouseX, mouseY, width / 2 - 100, height / 2 - 16, 200, 20)) {
                offlineField.setText(randomName());
                return;
            }
            int left = width / 2 - 145;
            if (inside(mouseX, mouseY, left, height / 2 + 16, 90, 20)) { confirmOffline(false); return; }
            if (inside(mouseX, mouseY, left + 100, height / 2 + 16, 90, 20)) { confirmOffline(true); return; }
            if (inside(mouseX, mouseY, left + 200, height / 2 + 16, 90, 20)) { back(); return; }
        }
    }

    /** Ajoute le compte hors ligne ; login=true applique aussi la session. */
    private void confirmOffline(boolean login) {
        String n = offlineField.getText().trim();
        if (n.isEmpty()) { status = "Pseudo vide"; return; }
        Account acc = Account.offline(n);
        AltManager.add(acc);
        if (login) {
            com.example.autoclicker.client.auth.SessionUtil.apply(acc);
            AltManager.save();
            mc.displayGuiScreen(null);     // ferme le menu, retour au jeu / titre
        } else {
            mc.displayGuiScreen(parent);   // retour au dashboard
        }
    }

    private void startMicrosoft() {
        adding = true; status = "Demande du code..."; userCode = ""; verifUri = "";
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    MicrosoftAuth.DeviceCode dc = MicrosoftAuth.requestDeviceCode();
                    userCode = dc.userCode; verifUri = dc.verificationUri; copy(dc.userCode);
                    Account acc = MicrosoftAuth.authenticate(dc, new MicrosoftAuth.Listener() {
                        @Override public void onCode(String c, String u) { userCode = c; verifUri = u; }
                        @Override public void onStatus(String m) { status = m; }
                    });
                    AltManager.add(acc);
                    status = "Ajouté : " + acc.name;
                } catch (Exception e) {
                    status = "Erreur : " + e.getMessage();
                } finally { adding = false; userCode = ""; }
            }
        }, "DanielClient-MSAuth").start();
    }

    private void back() {
        if (stage == CHOOSE) mc.displayGuiScreen(parent);
        else { stage = CHOOSE; status = ""; }
    }

    private void copy(String s) {
        try { Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(s), null); }
        catch (Throwable ignored) {}
    }

    // texte mis à l'échelle, centré sur l'écran
    private void drawCentered(String s, int y, int color, float scale) {
        drawCentered2(s, width / 2, y, color, scale);
    }

    private void drawCentered2(String s, int cx, int y, int color, float scale) {
        net.minecraft.client.renderer.GlStateManager.pushMatrix();
        net.minecraft.client.renderer.GlStateManager.translate(cx, y, 0);
        net.minecraft.client.renderer.GlStateManager.scale(scale, scale, 1f);
        fontRendererObj.drawStringWithShadow(s, -fontRendererObj.getStringWidth(s) / 2f, 0, color);
        net.minecraft.client.renderer.GlStateManager.popMatrix();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) { back(); return; }
        if (stage == OFFLINE) {
            if (keyCode == 28) { confirmOffline(true); return; }   // Entrée = Login
            offlineField.textboxKeyTyped(typedChar, keyCode);
        }
    }

    @Override public void updateScreen() { if (offlineField != null) offlineField.updateCursorCounter(); }

    private boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
