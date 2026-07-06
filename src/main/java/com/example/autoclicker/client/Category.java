package com.example.autoclicker.client;

/** Catégories de modules, façon client (Combat, Mouvement, Visuel, Render). */
public enum Category {
    COMBAT("Combat"),
    MOVEMENT("Mouvement"),
    VISUAL("Visuel"),
    RENDER("Render"),
    PLAYER("Player");

    public final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }
}
