package com.example.autoclicker.client;

import org.lwjgl.opengl.Display;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Personnalise la fenêtre du jeu : titre + icône. L'icône est chargée depuis la
 * ressource /danielclient_icon.png (à placer dans src/main/resources/).
 */
public final class AppBranding {

    private AppBranding() {}

    public static final String TITLE = "Daniel Client " + UpdateChecker.LOCAL_VERSION;
    private static boolean done = false;

    /** À appeler une fois, après création de la fenêtre (premier tick client). */
    public static void apply() {
        if (done) return;
        done = true;
        try {
            Display.setTitle(TITLE);
        } catch (Throwable ignored) {
        }
        try {
            InputStream in = AppBranding.class.getResourceAsStream("/danielclient_icon.png");
            if (in == null) return;                 // pas d'icône fournie -> on garde le titre seul
            BufferedImage img = ImageIO.read(in);
            in.close();
            if (img == null) return;
            Display.setIcon(new ByteBuffer[]{
                    toBuffer(scale(img, 16)),
                    toBuffer(scale(img, 32)),
                    toBuffer(scale(img, 64))
            });
        } catch (Throwable ignored) {
        }
    }

    private static BufferedImage scale(BufferedImage src, int size) {
        BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = out.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, size, size, null);
        g.dispose();
        return out;
    }

    /** BufferedImage -> ByteBuffer RGBA (format attendu par Display.setIcon). */
    private static ByteBuffer toBuffer(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        int[] px = img.getRGB(0, 0, w, h, null, 0, w);
        ByteBuffer buf = ByteBuffer.allocateDirect(w * h * 4);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int p = px[y * w + x];
                buf.put((byte) ((p >> 16) & 0xFF)); // R
                buf.put((byte) ((p >> 8) & 0xFF));  // G
                buf.put((byte) (p & 0xFF));         // B
                buf.put((byte) ((p >> 24) & 0xFF)); // A
            }
        }
        buf.flip();
        return buf;
    }
}
