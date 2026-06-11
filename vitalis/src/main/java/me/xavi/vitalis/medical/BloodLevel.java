package me.xavi.vitalis.medical;

/**
 * Represents the player's overall condition based on their current blood
 * volume (out of {@link #MAX_BLOOD_ML}).
 */
public enum BloodLevel {
    NORMAL("Normál", 3500),
    WEAK("Gyenge", 2500),
    DIZZY("Szédülés", 1500),
    UNCONSCIOUS("Eszméletvesztés", 500),
    DEAD("Halál", 0);

    /** Maximum (healthy) blood volume in milliliters. */
    public static final double MAX_BLOOD_ML = 5000.0;

    /** Lower bound (inclusive) for ml at which this level applies. */
    private final int thresholdMl;
    private final String displayName;

    BloodLevel(String displayName, int thresholdMl) {
        this.displayName = displayName;
        this.thresholdMl = thresholdMl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getThresholdMl() {
        return thresholdMl;
    }

    /** Returns the {@link BloodLevel} that corresponds to the given blood volume. */
    public static BloodLevel fromMl(double ml) {
        if (ml >= NORMAL.thresholdMl) return NORMAL;
        if (ml >= WEAK.thresholdMl) return WEAK;
        if (ml >= DIZZY.thresholdMl) return DIZZY;
        if (ml >= UNCONSCIOUS.thresholdMl) return UNCONSCIOUS;
        return DEAD;
    }
}
