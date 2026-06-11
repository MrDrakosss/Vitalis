package me.xavi.vitalis.client;

import me.xavi.vitalis.medical.BloodLevel;
import me.xavi.vitalis.medical.BodyPart;
import me.xavi.vitalis.medical.InjuryStatus;

/**
 * Client-side mirror of the local player's full medical state (per-body-part
 * HP and injury status, plus overall blood volume), updated whenever a
 * {@link me.xavi.vitalis.network.MedicalStatePayload} arrives from the
 * server. Used to render the always-on status HUD.
 */
public class ClientMedicalState {
    private static final int[] bodyPartHp = new int[BodyPart.VALUES.length];
    private static final InjuryStatus[] bodyPartStatus = new InjuryStatus[BodyPart.VALUES.length];
    private static double bloodMl = BloodLevel.MAX_BLOOD_ML;
    private static boolean received = false;

    static {
        for (int i = 0; i < BodyPart.VALUES.length; i++) {
            bodyPartHp[i] = BodyPart.VALUES[i].getMaxHp();
            bodyPartStatus[i] = InjuryStatus.NONE;
        }
    }

    public static void update(int[] newHp, int[] newStatus, double newBloodMl) {
        if (newHp != null) {
            int len = Math.min(bodyPartHp.length, newHp.length);
            System.arraycopy(newHp, 0, bodyPartHp, 0, len);
        }
        if (newStatus != null) {
            int len = Math.min(bodyPartStatus.length, newStatus.length);
            for (int i = 0; i < len; i++) {
                int ordinal = newStatus[i];
                bodyPartStatus[i] = (ordinal >= 0 && ordinal < InjuryStatus.VALUES.length)
                        ? InjuryStatus.VALUES[ordinal]
                        : InjuryStatus.NONE;
            }
        }
        bloodMl = newBloodMl;
        received = true;
    }

    /** Whether at least one update has been received from the server yet. */
    public static boolean hasData() {
        return received;
    }

    public static int getHp(BodyPart part) {
        return bodyPartHp[part.ordinal()];
    }

    public static InjuryStatus getStatus(BodyPart part) {
        return bodyPartStatus[part.ordinal()];
    }

    public static double getBloodMl() {
        return bloodMl;
    }

    public static BloodLevel getBloodLevel() {
        return BloodLevel.fromMl(bloodMl);
    }
}
