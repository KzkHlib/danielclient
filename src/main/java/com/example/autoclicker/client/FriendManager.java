package com.example.autoclicker.client;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Liste d'amis partagée par le client. Les amis sont ignorés par l'AimAssist et
 * affichés d'une couleur distincte par l'ESP / les Nametags. Persisté par
 * {@link Client} (CSV).
 */
public final class FriendManager {

    private FriendManager() {}

    private static final Set<String> friends = new LinkedHashSet<String>();

    public static boolean isFriend(String name) {
        return name != null && friends.contains(name.toLowerCase());
    }

    /** Ajoute/retire ; retourne true si désormais ami. */
    public static boolean toggle(String name) {
        if (name == null || name.isEmpty()) return false;
        String key = name.toLowerCase();
        if (friends.remove(key)) return false;
        friends.add(key);
        return true;
    }

    public static void add(String name)    { if (name != null) friends.add(name.toLowerCase()); }
    public static void remove(String name) { if (name != null) friends.remove(name.toLowerCase()); }
    public static List<String> all()       { return new ArrayList<String>(friends); }
    public static int count()              { return friends.size(); }

    // ---- persistance (CSV) ----
    public static String write() {
        StringBuilder sb = new StringBuilder();
        for (String f : friends) {
            if (sb.length() > 0) sb.append(',');
            sb.append(f);
        }
        return sb.toString();
    }

    public static void read(String csv) {
        friends.clear();
        if (csv == null || csv.isEmpty()) return;
        for (String part : csv.split(",")) {
            String t = part.trim().toLowerCase();
            if (!t.isEmpty()) friends.add(t);
        }
    }
}
