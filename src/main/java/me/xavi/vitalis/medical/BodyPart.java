package me.xavi.vitalis.medical;

/**
 * The seven trackable body parts. Order is fixed and used for networking
 * (index-based arrays), so DO NOT reorder existing entries - only append.
 */
public enum BodyPart {
    HEAD("Fej", 100, true),
    CHEST("Mellkas", 100, true),
    ABDOMEN("Has", 100, true),
    LEFT_ARM("Bal kar", 100, false),
    RIGHT_ARM("Jobb kar", 100, false),
    LEFT_LEG("Bal láb", 100, false),
    RIGHT_LEG("Jobb láb", 100, false);

    public static final BodyPart[] VALUES = values();

    private final String displayName;
    private final int maxHp;
    private final boolean critical;

    BodyPart(String displayName, int maxHp, boolean critical) {
        this.displayName = displayName;
        this.maxHp = maxHp;
        this.critical = critical;
    }

    /** Hungarian display name, e.g. "Bal kar". */
    public String getDisplayName() {
        return displayName;
    }

    public int getMaxHp() {
        return maxHp;
    }

    /** Whether reaching 0 HP on this part should be lethal. */
    public boolean isCritical() {
        return critical;
    }
}
