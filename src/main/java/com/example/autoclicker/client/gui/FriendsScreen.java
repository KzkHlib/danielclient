package com.example.autoclicker.client.gui;

import com.example.autoclicker.client.Client;
import com.example.autoclicker.client.FriendManager;
import com.example.autoclicker.client.Theme;
import com.example.autoclicker.client.render.Render2D;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import java.io.IOException;
import java.util.List;

/** Liste d'amis : ajouter par nom, retirer. (Ou via la touche du module Friends en jeu.) */
public class FriendsScreen extends GuiScreen {

    private static final int WIDTH = 280, HEIGHT = 220, HEADER = 28, ROW = 20;

    private final GuiScreen parent;
    private int wx, wy;
    private GuiTextField nameField;
    private int scroll = 0;

    public FriendsScreen(GuiScreen parent) { this.parent = parent; }

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
        fontRendererObj.drawStringWithShadow("AMIS", wx + 12, wy + 10, Theme.TEXT);
        String n = FriendManager.count() + " ami(s)";
        fontRendererObj.drawStringWithShadow(n, wx + WIDTH - 12 - fontRendererObj.getStringWidth(n), wy + 10, Theme.ACCENT2);

        List<String> friends = FriendManager.all();
        int y = wy + HEADER + 6 - scroll;
        if (friends.isEmpty()) {
            fontRendererObj.drawStringWithShadow("Aucun ami. Ajoute un pseudo ci-dessous,", wx + 14, y + 4, 0xFF8A8A98);
            fontRendererObj.drawStringWithShadow("ou vise un joueur + touche B en jeu.", wx + 14, y + 16, 0xFF8A8A98);
        }
        for (String f : friends) {
            if (y >= wy + HEADER && y + ROW <= wy + HEIGHT - 34) {
                boolean hover = inside(mouseX, mouseY, wx + 8, y, WIDTH - 16, ROW);
                Render2D.roundedRect(wx + 8, y, wx + WIDTH - 8, y + ROW, 4, hover ? 0xFF1C1C2A : 0xFF141420);
                Render2D.roundedRect(wx + 8, y + 3, wx + 10, y + ROW - 3, 1, Theme.ACCENT2);
                fontRendererObj.drawStringWithShadow(f, wx + 16, y + 6, Theme.TEXT);
                drawMini("Retirer", wx + WIDTH - 60, y + 3, 0xFFE74C3C);
            }
            y += ROW;
        }

        Gui.drawRect(wx + 8, wy + HEIGHT - 30, wx + WIDTH - 70, wy + HEIGHT - 10, 0xFF0D0D15);
        if (nameField.getText().isEmpty() && !nameField.isFocused())
            fontRendererObj.drawString("Pseudo...", wx + 12, wy + HEIGHT - 25, 0xFF6A6A78);
        nameField.drawTextBox();
        drawMini("Ajouter", wx + WIDTH - 62, wy + HEIGHT - 26, Theme.ACCENT);

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

        List<String> friends = FriendManager.all();
        int y = wy + HEADER + 6 - scroll;
        for (String f : friends) {
            if (y >= wy + HEADER && y + ROW <= wy + HEIGHT - 34) {
                if (inside(mouseX, mouseY, wx + WIDTH - 60, y + 3, miniW("Retirer"), 14)) {
                    FriendManager.remove(f);
                    Client.INSTANCE.save();
                    return;
                }
            }
            y += ROW;
        }
        if (inside(mouseX, mouseY, wx + WIDTH - 62, wy + HEIGHT - 26, miniW("Ajouter"), 14)) {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                FriendManager.add(name);
                Client.INSTANCE.save();
                nameField.setText("");
            }
        }
    }

    private int miniW(String s) { return fontRendererObj.getStringWidth(s) + 8; }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) { mc.displayGuiScreen(parent); return; }
        if (nameField.isFocused()) nameField.textboxKeyTyped(typedChar, keyCode);
    }

    @Override public void updateScreen() { nameField.updateCursorCounter(); }

    private boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
