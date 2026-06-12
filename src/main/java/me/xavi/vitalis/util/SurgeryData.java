package me.xavi.vitalis.util;

import com.mojang.serialization.Codec;
import me.xavi.vitalis.Vitalis;
import me.xavi.vitalis.medical.BloodLevel;
import me.xavi.vitalis.medical.BodyPart;
import me.xavi.vitalis.medical.InjuryStatus;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class SurgeryData {

    public static final String[] BODY_PARTS = {
            "head",
            "left_arm",
            "right_arm",
            "chest",
            "legs"
    };

    private static final Codec<List<Float>> INJURY_LIST_CODEC = Codec.FLOAT.listOf();

    public static final AttachmentType<Boolean> ON_TABLE = AttachmentRegistry.create(
            Vitalis.id("on_table"),
            builder -> builder
                    .initializer(() -> false)
                    .persistent(Codec.BOOL)
    );

    public static final AttachmentType<List<Integer>> LOCK_POS = AttachmentRegistry.create(
            Vitalis.id("lock_pos"),
            builder -> builder
                    .initializer(List::of)
                    .persistent(Codec.INT.listOf())
    );

    public static final AttachmentType<List<Integer>> TABLE_POS = AttachmentRegistry.create(
            Vitalis.id("table_pos"),
            builder -> builder
                    .initializer(List::of)
                    .persistent(Codec.INT.listOf())
    );

    public static final AttachmentType<List<Float>> INJURIES = AttachmentRegistry.create(
            Vitalis.id("injuries"),
            builder -> builder
                    .initializer(SurgeryData::defaultInjuries)
                    .persistent(INJURY_LIST_CODEC)
    );

    public static final AttachmentType<Float> LOCK_YAW = AttachmentRegistry.create(
            Vitalis.id("lock_yaw"),
            builder -> builder
                    .initializer(() -> 0.0F)
                    .persistent(Codec.FLOAT)
    );

    public static final AttachmentType<List<Integer>> BODY_PART_HP = AttachmentRegistry.create(
            Vitalis.id("body_part_hp"),
            builder -> builder
                    .initializer(SurgeryData::defaultBodyPartHp)
                    .persistent(Codec.INT.listOf())
    );

    public static final AttachmentType<List<Integer>> BODY_PART_STATUS = AttachmentRegistry.create(
            Vitalis.id("body_part_status"),
            builder -> builder
                    .initializer(SurgeryData::defaultBodyPartStatus)
                    .persistent(Codec.INT.listOf())
    );

    public static final AttachmentType<Double> BLOOD_ML = AttachmentRegistry.create(
            Vitalis.id("blood_ml"),
            builder -> builder
                    .initializer(() -> BloodLevel.MAX_BLOOD_ML)
                    .persistent(Codec.DOUBLE)
    );

    public static final AttachmentType<Integer> HEART_RATE = AttachmentRegistry.create(
            Vitalis.id("heart_rate"),
            builder -> builder
                    .initializer(() -> 75)
                    .persistent(Codec.INT)
    );

    public static final AttachmentType<Integer> BLOOD_PRESSURE_SYSTOLIC = AttachmentRegistry.create(
            Vitalis.id("blood_pressure_systolic"),
            builder -> builder
                    .initializer(() -> 120)
                    .persistent(Codec.INT)
    );

    public static final AttachmentType<Integer> BLOOD_PRESSURE_DIASTOLIC = AttachmentRegistry.create(
            Vitalis.id("blood_pressure_diastolic"),
            builder -> builder
                    .initializer(() -> 80)
                    .persistent(Codec.INT)
    );

    private static List<Float> defaultInjuries() {
        List<Float> list = new ArrayList<>(BODY_PARTS.length);

        for (int i = 0; i < BODY_PARTS.length; i++) {
            list.add(0.0F);
        }

        return list;
    }

    private static List<Integer> defaultBodyPartHp() {
        List<Integer> list = new ArrayList<>(BodyPart.VALUES.length);

        for (BodyPart part : BodyPart.VALUES) {
            list.add(part.getMaxHp());
        }

        return list;
    }

    private static List<Integer> defaultBodyPartStatus() {
        List<Integer> list = new ArrayList<>(BodyPart.VALUES.length);

        for (int i = 0; i < BodyPart.VALUES.length; i++) {
            list.add(InjuryStatus.NONE.ordinal());
        }

        return list;
    }

    public static void ensureRegistered() {
    }

    public static boolean isOnTable(Player player) {
        return player.getAttachedOrElse(ON_TABLE, false);
    }

    public static void setOnTable(Player player, boolean onTable) {
        player.setAttached(ON_TABLE, onTable);
    }

    public static BlockPos getTablePos(Player player) {
        return decodePos(player.getAttachedOrElse(TABLE_POS, List.of()));
    }

    public static void setTablePos(Player player, BlockPos pos) {
        player.setAttached(TABLE_POS, encodePos(pos));
    }

    public static BlockPos getLockPos(Player player) {
        BlockPos lock = decodePos(player.getAttachedOrElse(LOCK_POS, List.of()));
        return lock != null ? lock : getTablePos(player);
    }

    public static void setLockPos(Player player, BlockPos pos) {
        player.setAttached(LOCK_POS, encodePos(pos));
    }

    public static float getLockYaw(Player player) {
        return player.getAttachedOrElse(LOCK_YAW, 0.0F);
    }

    public static void setLockYaw(Player player, float yaw) {
        player.setAttached(LOCK_YAW, yaw);
    }

    public static float getInjury(Player player, String bodyPart) {
        int index = indexOf(bodyPart);

        if (index < 0) {
            return 0.0F;
        }

        List<Float> injuries = player.getAttachedOrElse(INJURIES, defaultInjuries());

        return index < injuries.size() ? injuries.get(index) : 0.0F;
    }

    public static void setInjury(Player player, String bodyPart, float value) {
        int index = indexOf(bodyPart);

        if (index < 0) {
            return;
        }

        float clamped = Math.min(1.0F, Math.max(0.0F, value));
        List<Float> current = new ArrayList<>(player.getAttachedOrElse(INJURIES, defaultInjuries()));

        while (current.size() <= index) {
            current.add(0.0F);
        }

        current.set(index, clamped);
        player.setAttached(INJURIES, current);
    }

    public static float[] getAllInjuries(Player player) {
        List<Float> injuries = player.getAttachedOrElse(INJURIES, defaultInjuries());
        float[] result = new float[BODY_PARTS.length];

        for (int i = 0; i < BODY_PARTS.length; i++) {
            result[i] = i < injuries.size() ? injuries.get(i) : 0.0F;
        }

        return result;
    }

    public static void clearAllInjuries(Player player) {
        player.setAttached(INJURIES, defaultInjuries());
    }

    private static int indexOf(String bodyPart) {
        for (int i = 0; i < BODY_PARTS.length; i++) {
            if (BODY_PARTS[i].equals(bodyPart)) {
                return i;
            }
        }

        return -1;
    }

    private static List<Integer> encodePos(BlockPos pos) {
        if (pos == null) {
            return List.of();
        }

        return List.of(pos.getX(), pos.getY(), pos.getZ());
    }

    private static BlockPos decodePos(List<Integer> list) {
        if (list == null || list.size() != 3) {
            return null;
        }

        return new BlockPos(list.get(0), list.get(1), list.get(2));
    }

    public static int getBodyPartHp(Player player, BodyPart part) {
        List<Integer> hp = player.getAttachedOrElse(BODY_PART_HP, defaultBodyPartHp());
        int index = part.ordinal();

        return index < hp.size() ? hp.get(index) : part.getMaxHp();
    }

    public static void setBodyPartHp(Player player, BodyPart part, int hp) {
        int clamped = Math.max(0, Math.min(part.getMaxHp(), hp));
        List<Integer> current = new ArrayList<>(player.getAttachedOrElse(BODY_PART_HP, defaultBodyPartHp()));

        while (current.size() <= part.ordinal()) {
            current.add(BodyPart.VALUES[current.size()].getMaxHp());
        }

        current.set(part.ordinal(), clamped);
        player.setAttached(BODY_PART_HP, current);
    }

    public static void addBodyPartHp(Player player, BodyPart part, int delta) {
        setBodyPartHp(player, part, getBodyPartHp(player, part) + delta);
    }

    public static int[] getAllBodyPartHp(Player player) {
        List<Integer> hp = player.getAttachedOrElse(BODY_PART_HP, defaultBodyPartHp());
        int[] result = new int[BodyPart.VALUES.length];

        for (int i = 0; i < BodyPart.VALUES.length; i++) {
            result[i] = i < hp.size() ? hp.get(i) : BodyPart.VALUES[i].getMaxHp();
        }

        return result;
    }

    public static InjuryStatus getBodyPartStatus(Player player, BodyPart part) {
        List<Integer> status = player.getAttachedOrElse(BODY_PART_STATUS, defaultBodyPartStatus());
        int index = part.ordinal();
        int ordinal = index < status.size() ? status.get(index) : InjuryStatus.NONE.ordinal();

        if (ordinal < 0 || ordinal >= InjuryStatus.VALUES.length) {
            return InjuryStatus.NONE;
        }

        return InjuryStatus.VALUES[ordinal];
    }

    public static void setBodyPartStatus(Player player, BodyPart part, InjuryStatus status) {
        List<Integer> current = new ArrayList<>(player.getAttachedOrElse(BODY_PART_STATUS, defaultBodyPartStatus()));

        while (current.size() <= part.ordinal()) {
            current.add(InjuryStatus.NONE.ordinal());
        }

        current.set(part.ordinal(), status.ordinal());
        player.setAttached(BODY_PART_STATUS, current);
    }

    public static InjuryStatus[] getAllBodyPartStatuses(Player player) {
        List<Integer> status = player.getAttachedOrElse(BODY_PART_STATUS, defaultBodyPartStatus());
        InjuryStatus[] result = new InjuryStatus[BodyPart.VALUES.length];

        for (int i = 0; i < BodyPart.VALUES.length; i++) {
            int ordinal = i < status.size() ? status.get(i) : InjuryStatus.NONE.ordinal();

            result[i] = ordinal >= 0 && ordinal < InjuryStatus.VALUES.length
                    ? InjuryStatus.VALUES[ordinal]
                    : InjuryStatus.NONE;
        }

        return result;
    }

    public static void resetBodyParts(Player player) {
        player.setAttached(BODY_PART_HP, defaultBodyPartHp());
        player.setAttached(BODY_PART_STATUS, defaultBodyPartStatus());
    }

    public static double getBloodMl(Player player) {
        return player.getAttachedOrElse(BLOOD_ML, BloodLevel.MAX_BLOOD_ML);
    }

    public static void setBloodMl(Player player, double ml) {
        double clamped = Math.max(0.0D, Math.min(BloodLevel.MAX_BLOOD_ML, ml));
        player.setAttached(BLOOD_ML, clamped);
    }

    public static void addBloodMl(Player player, double deltaMl) {
        setBloodMl(player, getBloodMl(player) + deltaMl);
    }

    public static void resetBlood(Player player) {
        setBloodMl(player, BloodLevel.MAX_BLOOD_ML);
    }

    public static int getHeartRate(Player player) {
        return player.getAttachedOrElse(HEART_RATE, 75);
    }

    public static void setHeartRate(Player player, int value) {
        player.setAttached(HEART_RATE, Math.max(0, value));
    }

    public static int getBloodPressureSystolic(Player player) {
        return player.getAttachedOrElse(BLOOD_PRESSURE_SYSTOLIC, 120);
    }

    public static void setBloodPressureSystolic(Player player, int value) {
        player.setAttached(BLOOD_PRESSURE_SYSTOLIC, Math.max(0, value));
    }

    public static int getBloodPressureDiastolic(Player player) {
        return player.getAttachedOrElse(BLOOD_PRESSURE_DIASTOLIC, 80);
    }

    public static void setBloodPressureDiastolic(Player player, int value) {
        player.setAttached(BLOOD_PRESSURE_DIASTOLIC, Math.max(0, value));
    }

    public static void resetVitals(Player player) {
        setHeartRate(player, 75);
        setBloodPressureSystolic(player, 120);
        setBloodPressureDiastolic(player, 80);
    }
}