package com.example.autoclicker.client.gui;

import com.example.autoclicker.client.Theme;
import com.example.autoclicker.client.AutoUpdater;
import com.example.autoclicker.client.UpdateChecker;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.renderer.GlStateManager;

import java.io.IOException;

/**
 * Menu principal thémé : fond aurora animé + nom du client en grand, tout en
 * gardant les boutons vanilla (Solo / Multi / Options…) et leur logique.
 * Ajoute un bouton "Alt Manager".
 */
public class CustomMainMenu extends GuiMainMenu {

    @Override
    public void initGui() {
        super.initGui();
        com.example.autoclicker.client.Sounds.playMenuMusic();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // fond thémé (remplace le panorama)
        GuiFx.render(this.width, this.height);

        // nom du client en grand
        String name = "Daniel";
        GlStateManager.pushMatrix();
        GlStateManager.translate(this.width / 2f, this.height / 6f, 0);
        GlStateManager.scale(4f, 4f, 1f);
        this.fontRendererObj.drawStringWithShadow(name, -this.fontRendererObj.getStringWidth(name) / 2f, 0, Theme.ACCENT);
        GlStateManager.popMatrix();
        String sub = "CLIENT  " + UpdateChecker.LOCAL_VERSION;
        this.fontRendererObj.drawStringWithShadow(sub,
                this.width / 2 - this.fontRendererObj.getStringWidth(sub) / 2, this.height / 6 + 38, 0xFFB6B6C6);
        drawUpdateStatus();

        // boutons + labels vanilla (sans le panorama/logo d'origine)
        for (int i = 0; i < this.buttonList.size(); i++) {
            this.buttonList.get(i).drawButton(this.mc, mouseX, mouseY);
        }
        for (int i = 0; i < this.labelList.size(); i++) {
            this.labelList.get(i).drawLabel(this.mc, mouseX, mouseY);
        }
    }

    private void drawUpdateStatus() {
        String status = AutoUpdater.status == null ? "" : AutoUpdater.status;
        if (status.length() == 0) status = "Verification update...";

        int color = AutoUpdater.updateReady ? 0xFFFFD166 : 0xFF9EE493;
        if (status.toLowerCase().contains("impossible") || status.toLowerCase().contains("invalide")) {
            color = 0xFFFF6B6B;
        }

        String text = "Update: " + status;
        this.fontRendererObj.drawStringWithShadow(text,
                this.width / 2 - this.fontRendererObj.getStringWidth(text) / 2,
                this.height - 18,
                color);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
    }
}
