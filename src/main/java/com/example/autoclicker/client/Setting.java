package com.example.autoclicker.client;

/**
 * Réglage de module. Trois types concrets imbriqués :
 *  - {@link Bool}   : interrupteur on/off
 *  - {@link Number} : valeur numérique avec min/max/pas (slider)
 *  - {@link Mode}   : choix dans une liste de modes
 *
 * Chaque réglage sait se sérialiser en String pour la persistance.
 */
public abstract class Setting {

    public final String name;

    protected Setting(String name) {
        this.name = name;
    }

    /** Sérialise la valeur courante. */
    public abstract String write();

    /** Recharge la valeur depuis une String (sans planter si invalide). */
    public abstract void read(String raw);

    // ------------------------------------------------------------------

    /** Interrupteur booléen. */
    public static class Bool extends Setting {
        public boolean value;

        public Bool(String name, boolean def) {
            super(name);
            this.value = def;
        }

        public void toggle() { value = !value; }

        @Override public String write() { return Boolean.toString(value); }
        @Override public void read(String raw) {
            try { value = Boolean.parseBoolean(raw); } catch (Exception ignored) {}
        }
    }

    /** Valeur numérique bornée (rendue sous forme de slider). */
    public static class Number extends Setting {
        public double value;
        public final double min, max, step;
        /** true = pas de décimales (affichage entier). */
        public final boolean integer;

        public Number(String name, double def, double min, double max, double step, boolean integer) {
            super(name);
            this.value = def;
            this.min = min;
            this.max = max;
            this.step = step;
            this.integer = integer;
        }

        /** Règle la valeur depuis une position de slider 0..1. */
        public void setFromSlider(double ratio) {
            if (ratio < 0) ratio = 0;
            if (ratio > 1) ratio = 1;
            double v = min + ratio * (max - min);
            // arrondi au pas
            v = Math.round(v / step) * step;
            if (v < min) v = min;
            if (v > max) v = max;
            value = v;
        }

        /** Position du curseur 0..1. */
        public double slider() {
            if (max == min) return 0;
            return (value - min) / (max - min);
        }

        public String display() {
            return integer ? Integer.toString((int) Math.round(value))
                            : String.format("%.1f", value);
        }

        @Override public String write() { return Double.toString(value); }
        @Override public void read(String raw) {
            try { value = Double.parseDouble(raw); } catch (Exception ignored) {}
        }
    }

    /** Choix parmi plusieurs modes. */
    public static class Mode extends Setting {
        public final String[] modes;
        public int index;

        public Mode(String name, int def, String... modes) {
            super(name);
            this.modes = modes;
            this.index = def;
        }

        public String current() { return modes[index]; }
        public void cycle() { index = (index + 1) % modes.length; }
        public boolean is(String m) { return modes[index].equalsIgnoreCase(m); }

        @Override public String write() { return Integer.toString(index); }
        @Override public void read(String raw) {
            try {
                int i = Integer.parseInt(raw);
                if (i >= 0 && i < modes.length) index = i;
            } catch (Exception ignored) {}
        }
    }
}
