package me.xavi.vitalis.medical;

/**
 * The kind of injury a body part currently has, in addition to its raw HP
 * value. {@link #NONE} means the body part has no special condition beyond
 * its HP.
 */
public enum InjuryStatus {
    /** No special condition. */
    NONE("Egészséges"),

    /** Closed fracture: Slowness II, can't sprint, reduced jump height. */
    FRACTURE("Törött"),

    /** Open fracture: bleeding, infection risk, continuous HP loss. */
    OPEN_FRACTURE("Nyílt törés"),

    /** Gunshot wound: bleeding + shock. */
    BULLET_WOUND("Lövési sérülés"),

    /** Cut/stab wound: slow bleeding. */
    CUT("Vágott seb"),

    /** Burn: long regeneration time. */
    BURN("Égés");

    public static final InjuryStatus[] VALUES = values();

    private final String displayName;

    InjuryStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Blood loss rate in milliliters per second caused by this injury
     * status, independent of the body part's HP. 0 for non-bleeding
     * statuses.
     */
    public double getBleedRateMlPerSecond() {
        return switch (this) {
            case OPEN_FRACTURE -> 50.0;
            case BULLET_WOUND -> 20.0;
            case CUT -> 5.0;
            default -> 0.0;
        };
    }

    public boolean causesBleeding() {
        return getBleedRateMlPerSecond() > 0.0;
    }
}
