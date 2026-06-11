package me.xavi.vitalis.client;

import me.xavi.vitalis.medical.BloodLevel;
import me.xavi.vitalis.medical.BodyPart;
import me.xavi.vitalis.medical.InjuryStatus;

public final class ClientMedicalState {

    private static final int[] BODY_PART_HP = new int[BodyPart.VALUES.length];
    private static final InjuryStatus[] BODY_PART_STATUS = new InjuryStatus[BodyPart.VALUES.length];

    private static double bloodMl = BloodLevel.MAX_BLOOD_ML;

    static {
        reset();
    }

    private ClientMedicalState() {
    }

    public static void reset() {
        for (int i = 0; i < BodyPart.VALUES.length; i++) {
            BODY_PART_HP[i] = BodyPart.VALUES[i].getMaxHp();
            BODY_PART_STATUS[i] = InjuryStatus.NONE;
        }

        bloodMl = BloodLevel.MAX_BLOOD_ML;
    }

    public static void update(int[] hp, int[] status, double newBloodMl) {
        for (int i = 0; i < BodyPart.VALUES.length; i++) {
            BODY_PART_HP[i] = i < hp.length
                    ? hp[i]
                    : BodyPart.VALUES[i].getMaxHp();

            int ordinal = i < status.length ? status[i] : InjuryStatus.NONE.ordinal();

            BODY_PART_STATUS[i] = ordinal >= 0 && ordinal < InjuryStatus.VALUES.length
                    ? InjuryStatus.VALUES[ordinal]
                    : InjuryStatus.NONE;
        }

        bloodMl = newBloodMl;
    }

    public static int getHp(BodyPart part) {
        return BODY_PART_HP[part.ordinal()];
    }

    public static InjuryStatus getStatus(BodyPart part) {
        return BODY_PART_STATUS[part.ordinal()];
    }

    public static double getBloodMl() {
        return bloodMl;
    }

    public static BloodLevel getBloodLevel() {
        return BloodLevel.fromMl(bloodMl);
    }

    public static boolean needsSurgery(BodyPart part) {
        InjuryStatus status = getStatus(part);

        return status == InjuryStatus.FRACTURE
                || status == InjuryStatus.OPEN_FRACTURE
                || status == InjuryStatus.BULLET_WOUND
                || getHp(part) <= part.getMaxHp() * 0.35D;
    }
}