package com.example.autoclicker.client.gui;

import com.example.autoclicker.client.LicenseManager;
import com.example.autoclicker.client.Theme;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.awt.Color;
import java.io.IOException;

/** Fenêtre de saisie de la clé de licence (affichée au tout premier lancement). */
public class KeyEntryGui extends GuiScreen {

    private final LicenseManager license;
    private final Runnable onSuccess;

    private GuiTextField field;
    private String status = "";
    private int statusColor = 0xFFFFFFFF;

    public KeyEntryGui(LicenseManager license, Runnable onSuccess) {
        this.license = license;
        this.onSuccess = onSuccess;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        int cx = this.width / 2;
        int cy = this.height / 2;
        field = new GuiTextField(0, this.fontRendererObj, cx - 110, cy - 10, 220, 20);
        field.setMaxStringLength(40);
        field.setFocused(true);

        this.buttonList.clear();
        this.buttonList.add(new GuiButton(1, cx - 110, cy + 18, 105, 20, "Valider"));
        this.buttonList.add(new GuiButton(2, cx + 5, cy + 18, 105, 20, "Plus tard"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 1) submit();
        else if (button.id == 2) close();
    }

    /** Ferme proprement (revient au menu principal si aucun monde n'est chargé). */
    private void close() {
        if (this.mc.theWorld == null) this.mc.displayGuiScreen(new GuiMainMenu());
        else this.mc.displayGuiScreen(null);
    }

    private void submit() {
        String key = field.getText().trim();
        if (key.isEmpty()) {
            status = "Entre ta clé.";
            statusColor = 0xFFFFD040;
            return;
        }
        status = "Vérification...";
        statusColor = 0xFFAAAAAA;
        boolean ok = license.validateKey(key);
        if (ok) {
            if (onSuccess != null) onSuccess.run();
            close();
        } else {
            status = "Clé invalide, expirée ou serveur injoignable.";
            statusColor = 0xFFFF5555;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_RETURN) { submit(); return; }
        if (keyCode == Keyboard.KEY_ESCAPE) { close(); return; }
        field.textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        field.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        field.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GuiFx.render(this.width, this.height);
        int cx = this.width / 2;
        int cy = this.height / 2;

        // panneau central avec bordure accent
        int pw = 270, ph = 132;
        int px = cx - pw / 2, py = cy - ph / 2;
        Gui.drawRect(px, py, px + pw, py + ph, 0xCC101018);
        Gui.drawRect(px, py, px + pw, py + 2, Theme.ACCENT);
        Gui.drawRect(px, py + ph - 2, px + pw, py + ph, Theme.ACCENT);
        Gui.drawRect(px, py, px + 2, py + ph, Theme.ACCENT);
        Gui.drawRect(px + pw - 2, py, px + pw, py + ph, Theme.ACCENT);

        drawAnimatedTitle("DANIEL CLIENT", cx, py + 16);
        drawCenteredString(this.fontRendererObj, "Entre Ta License", cx, py + 36, 0xFFFFFFFF);

        field.drawTextBox();
        if (!status.isEmpty()) {
            drawCenteredString(this.fontRendererObj, status, cx, cy + 46, statusColor);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /** Titre avec dégradé animé qui défile lettre par lettre. */
    private void drawAnimatedTitle(String text, int centerX, int y) {
        int w = this.fontRendererObj.getStringWidth(text);
        int x = centerX - w / 2;
        long t = System.currentTimeMillis();
        for (int i = 0; i < text.length(); i++) {
            float hue = ((t / 12.0f) + i * 20.0f) % 360.0f / 360.0f;
            int color = Color.HSBtoRGB(hue, 0.5f, 1.0f) | 0xFF000000;
            String ch = String.valueOf(text.charAt(i));
            this.fontRendererObj.drawStringWithShadow(ch, x, y, color);
            x += this.fontRendererObj.getCharWidth(text.charAt(i));
        }
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
