package com.example.autoclicker.client.auth;

/**
 * Compte Microsoft enregistré (compte légitime de l'utilisateur). Le
 * refreshToken permet de re-générer une session sans redemander le login.
 */
public class Account {

    public static final String MICROSOFT = "microsoft";
    public static final String OFFLINE = "offline";

    public String name;          // pseudo Minecraft
    public String uuid;          // UUID sans tirets
    public String accessToken;   // token de session Minecraft (volatile)
    public String refreshToken;  // token Microsoft pour rafraîchir
    public String type = MICROSOFT;
    public long addedAt;
    public long lastLogin;       // dernière utilisation (jouer)

    public Account() {}

    public Account(String name, String uuid, String accessToken, String refreshToken) {
        this.name = name;
        this.uuid = uuid;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.type = MICROSOFT;
        this.addedAt = System.currentTimeMillis();
        this.lastLogin = this.addedAt;
    }

    /** Compte hors ligne (pseudo + UUID offline, pas d'auth premium). */
    public static Account offline(String name) {
        Account a = new Account();
        a.name = name;
        a.type = OFFLINE;
        a.uuid = java.util.UUID.nameUUIDFromBytes(
                ("OfflinePlayer:" + name).getBytes(java.nio.charset.Charset.forName("UTF-8")))
                .toString().replace("-", "");
        a.accessToken = "0";
        a.addedAt = System.currentTimeMillis();
        a.lastLogin = a.addedAt;
        return a;
    }

    public boolean isOffline() { return OFFLINE.equals(type); }
}
