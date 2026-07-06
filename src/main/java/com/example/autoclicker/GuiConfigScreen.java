package com.example.autoclicker;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

/** Menu de configuration de l'autoclicker (touche O par défaut). */
public class GuiConfigScreen extends GuiScreen {

    private GuiButton btnCpsMinDown, btnCpsMinUp;
    private GuiButton btnCpsMaxDown, btnCpsMaxUp;
    private GuiButton btnButton;   // gauche / droit
    private GuiButton btnMode;     // HOLD / TOGGLE
    private GuiButton btnBlockGui; // bloquer dans les menus
    private GuiButton btnShowCps;  // afficher le compteur de CPS
    private GuiButton btnDone;

    @Override
    public void initGui() {
        this.buttonList.clear();
        int cx = this.width / 2;
        int y = this.height / 4;

        btnCpsMinDown = add(1, cx - 100, y, 20, "-");
        btnCpsMinUp   = add(2, cx + 80, y, 20, "+");

        btnCpsMaxDown = add(3, cx - 100, y + 26, 20, "-");
        btnCpsMaxUp   = add(4, cx + 80, y + 26, 20, "+");

        btnButton   = add(5, cx - 100, y + 56, 200, "");
        btnMode     = add(6, cx - 100, y + 82, 200, "");
        btnBlockGui = add(7, cx - 100, y + 108, 200, "");
        btnShowCps  = add(9, cx - 100, y + 134, 200, "");

        btnDone = add(8, cx - 100, y + 166, 200, "Fermer");

        refreshLabels();
    }

    private GuiButton add(int id, int x, int y, int w, String label) {
        GuiButton b = new GuiButton(id, x, y, w, 20, label);
        this.buttonList.add(b);
        return b;
    }

    private void refreshLabels() {
        btnButton.displayString = "Bouton : " + (Config.rightClick ? "Clic droit" : "Clic gauche");
        btnMode.displayString = "Mode : " + (Config.mode == Config.Mode.HOLD
                ? "Maintenir le bouton" : "Toggle (touche R)");
        btnBlockGui.displayString = "Bloquer dans les menus : " + (Config.blockInGui ? "Oui" : "Non");
        btnShowCps.displayString = "Compteur de CPS : " + (Config.showCps ? "Affiché" : "Masqué");
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 1: Config.cpsMin -= 1; break;
            case 2: Config.cpsMin += 1; break;
            case 3: Config.cpsMax -= 1; break;
            case 4: Config.cpsMax += 1; break;
            case 5: Config.rightClick = !Config.rightClick; break;
            case 6: Config.mode = (Config.mode == Config.Mode.HOLD)
                        ? Config.Mode.TOGGLE : Config.Mode.HOLD; break;
            case 7: Config.blockInGui = !Config.blockInGui; break;
            case 9: Config.showCps = !Config.showCps; break;
            case 8: this.mc.displayGuiScreen(null); return;
        }
        Config.clampCps();
        refreshLabels();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        int cx = this.width / 2;
        int y = this.height / 4;

        drawCenteredString(this.fontRendererObj, "AutoClicker", cx, y - 30, 0xFFFFFF);
        String state = Config.mode == Config.Mode.TOGGLE
                ? (Config.enabled ? "ON" : "OFF") : "(maintien)";
        drawCenteredString(this.fontRendererObj, "Etat : " + state, cx, y - 18, 0xAAAAAA);

        drawCenteredString(this.fontRendererObj,
                "CPS min : " + fmt(Config.cpsMin), cx, y + 6, 0xFFFFFF);
        drawCenteredString(this.fontRendererObj,
                "CPS max : " + fmt(Config.cpsMax), cx, y + 32, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String fmt(double d) {
        return String.format("%.0f", d);
    }

    @Override
    public void onGuiClosed() {
        Config.clampCps();
        Config.save();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
