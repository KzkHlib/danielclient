package com.example.autoclicker.client.auth;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/** Remplace la session active du client (champ final Minecraft.session) par réflexion. */
public final class SessionUtil {

    private SessionUtil() {}

    /** Applique le compte comme session courante. Retourne false si échec. */
    public static boolean apply(Account acc) {
        if (acc == null || acc.name == null || acc.uuid == null) return false;
        try {
            String type = acc.isOffline() ? "legacy" : "msa";
            String token = acc.accessToken == null ? "0" : acc.accessToken;
            Session session = new Session(acc.name, acc.uuid, token, type);
            ReflectionHelper.setPrivateValue(
                    Minecraft.class, Minecraft.getMinecraft(), session,
                    "session", "field_71449_j");
            acc.lastLogin = System.currentTimeMillis();
            return true;
        } catch (Throwable t) {
            System.err.println("[Auth] Echec application session: " + t.getMessage());
            return false;
        }
    }

    public static String currentName() {
        Session s = Minecraft.getMinecraft().getSession();
        return s == null ? "?" : s.getUsername();
    }
}
