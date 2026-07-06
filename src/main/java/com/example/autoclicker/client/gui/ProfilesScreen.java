package com.example.autoclicker.client.gui;

import com.example.autoclicker.client.Client;
import com.example.autoclicker.client.Theme;
import com.example.autoclicker.client.render.Render2D;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.io.IOException;
import java.util.List;

/**
 * Gestion des profils de configuration : lister, charger, sauvegarder,
 * supprimer, créer. Le profil actif est mémorisé entre les sessions.
 */
public class ProfilesScreen extends GuiScreen {

    private static final int WIDTH = 300, HEIGHT = 220, HEADER = 28, ROW = 22;

    private final GuiScreen parent;
    private int wx, wy;
    private GuiTextField nameField;
    private int scroll = 0;

    public ProfilesScreen(GuiScreen parent) { this.parent = parent; }

    @Override public boolean doesGuiPauseGame() { return false; }

    @Override
    public void initGui() {
        wx = (width - WIDTH) / 2;
        wy = (height - HEIGHT) / 2;
        nameField = new GuiTextField(0, fontRendererObj, wx + 12, wy + HEIGHT - 26, WIDTH - 80, 16);
        nameField.setMaxStringLength(24);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float pt) {
        GuiFx.render(width, height);
        Gui.drawRect(wx, wy, wx + WIDTH, wy + HEIGHT, 0xFF0E0E16);
        Gui.drawRect(wx, wy, wx + WIDTH, wy + HEADER, 0xFF15151F);
        Gui.drawRect(wx, wy + HEADER - 1, wx + WIDTH, wy + HEADER, Theme.ACCENT);
        fontRendererObj.drawStringWithShadow("PROFILS", wx + 12, wy + 10, Theme.TEXT);
        String active = "Actif: " + Client.INSTANCE.activeProfile();
        fontRendererObj.drawStringWithShadow(active,
                wx + WIDTH - 12 - fontRendererObj.getStringWidth(active), wy + 10, Theme.ACCENT2);

        List<String> profiles = Client.INSTANCE.listProfiles();
        int y = wy + HEADER + 6 - scroll;
        for (String name : profiles) {
            if (y >= wy + HEADER && y + ROW <= wy + HEIGHT - 34) {
                boolean cur = name.equals(Client.INSTANCE.activeProfile());
                boolean hover = inside(mouseX, mouseY, wx + 8, y, WIDTH - 16, ROW);
                Render2D.roundedRect(wx + 8, y, wx + WIDTH - 8, y + ROW, 4, hover ? 0xFF1C1C2A : 0xFF141420);
                if (cur) Render2D.roundedRect(wx + 8, y + 3, wx + 10, y + ROW - 3, 1, Theme.ACCENT);
                fontRendererObj.drawStringWithShadow(name, wx + 16, y + 7, cur ? Theme.TEXT : 0xFFB0B0C0);
                // boutons charger / suppr
                drawMini("Charger", wx + WIDTH - 120, y + 4, Theme.ACCENT2);
                if (!name.equals("default")) drawMini("Suppr", wx + WIDTH - 56, y + 4, 0xFFE74C3C);
            }
            y += ROW;
        }

        // champ + bouton sauvegarder
        Gui.drawRect(wx + 8, wy + HEIGHT - 30, wx + WIDTH - 70, wy + HEIGHT - 10, 0xFF0D0D15);
        if (nameField.getText().isEmpty() && !nameField.isFocused())
            fontRendererObj.drawString("Nom du profil...", wx + 12, wy + HEIGHT - 25, 0xFF6A6A78);
        nameField.drawTextBox();
        drawMini("Sauver", wx + WIDTH - 60, wy + HEIGHT - 26, Theme.ACCENT);

        super.drawScreen(mouseX, mouseY, pt);
    }

    private void drawMini(String label, int x, int y, int color) {
        int w = fontRendererObj.getStringWidth(label) + 8;
        Render2D.roundedRect(x, y, x + w, y + 14, 3.5f, 0xFF20202C);
        Render2D.roundedRect(x + 3, y + 13, x + w - 3, y + 14, 0.5f, color);
        fontRendererObj.drawStringWithShadow(label, x + 4, y + 3, color);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        nameField.mouseClicked(mouseX, mouseY, button);

        List<String> profiles = Client.INSTANCE.listProfiles();
        int y = wy + HEADER + 6 - scroll;
        for (String name : profiles) {
            if (y >= wy + HEADER && y + ROW <= wy + HEIGHT - 34) {
                if (inside(mouseX, mouseY, wx + WIDTH - 120, y + 4, miniW("Charger"), 14)) {
                    Client.INSTANCE.loadProfile(name);
                    return;
                }
                if (!name.equals("default")
                        && inside(mouseX, mouseY, wx + WIDTH - 56, y + 4, miniW("Suppr"), 14)) {
                    Client.INSTANCE.deleteProfile(name);
                    return;
                }
            }
            y += ROW;
        }
        // sauver
        if (inside(mouseX, mouseY, wx + WIDTH - 60, wy + HEIGHT - 26, miniW("Sauver"), 14)) {
            String n = nameField.getText().trim();
            if (!n.isEmpty()) {
                Client.INSTANCE.saveProfileAs(n);
                nameField.setText("");
            }
        }
    }

    private int miniW(String s) { return fontRendererObj.getStringWidth(s) + 8; }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) { mc.displayGuiScreen(parent); return; } // ESC -> retour
        if (nameField.isFocused()) nameField.textboxKeyTyped(typedChar, keyCode);
    }

    @Override public void updateScreen() { nameField.updateCursorCounter(); }

    private boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
