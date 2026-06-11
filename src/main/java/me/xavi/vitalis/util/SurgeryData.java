package me.xavi.vitalis.util;

import com.mojang.serialization.Codec;
import me.xavi.vitalis.Vitalis;
import me.xavi.vitalis.medical.BodyPart;
import me.xavi.vitalis.medical.InjuryStatus;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Stores surgery-related state (whether the player is on the table, the
 * position they should be locked to, and the injury percentages) using
 * Fabric's Data Attachment API. Replaces the old, removed
 * {@code Entity#getPersistentData()} approach.
 * <p>
 * All attachments here are server-side, persistent storage; the relevant
 * parts are mirrored to the client via
 * {@link me.xavi.vitalis.network.SurgeryStatePayload}.
 */
public class SurgeryData {

    /** All body parts that can be injured, in a fixed order used for networking. */
    public static final String[] BODY_PARTS = {
            "head", "left_arm", "right_arm", "chest", "legs"
    };

    private static final Codec<List<Float>> INJURY_LIST_CODEC = Codec.FLOAT.listOf();

    public static final AttachmentType<Boolean> ON_TABLE = AttachmentRegistry.create(
            Vitalis.id("on_table"),
            builder -> builder
                    .initializer(() -> false)
                    .persistent(Codec.BOOL)
    );

    /** Stored as a list of 3 ints [x, y, z]; empty list means "not set". */
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
                    .initializer(() -> defaultInjuries())
                    .persistent(INJURY_LIST_CODEC)
    );

    public static final AttachmentType<Float> LOCK_YAW = AttachmentRegistry.create(
            Vitalis.id("lock_yaw"),
            builder -> builder
                    .initializer(() -> 0.0f)
                    .persistent(Codec.FLOAT)
    );

    /**
     * Per-body-part HP, in the fixed order of {@link BodyPart#VALUES}. Each
     * entry ranges from 0 to {@link BodyPart#getMaxHp()}.
     */
    public static final AttachmentType<List<Integer>> BODY_PART_HP = AttachmentRegistry.create(
            Vitalis.id("body_part_hp"),
            builder -> builder
                    .initializer(SurgeryData::defaultBodyPartHp)
                    .persistent(Codec.INT.listOf())
    );

    /**
     * Per-body-part injury status, stored as the ordinal of
     * {@link InjuryStatus}, in the fixed order of {@link BodyPart#VALUES}.
     */
    public static final AttachmentType<List<Integer>> BODY_PART_STATUS = AttachmentRegistry.create(
            Vitalis.id("body_part_status"),
            builder -> builder
                    .initializer(SurgeryData::defaultBodyPartStatus)
                    .persistent(Codec.INT.listOf())
    );

    /** Total blood volume in milliliters, 0 to {@link me.xavi.vitalis.medical.BloodLevel#MAX_BLOOD_ML}. */
    public static final AttachmentType<Double> BLOOD_ML = AttachmentRegistry.create(
            Vitalis.id("blood_ml"),
            builder -> builder
                    .initializer(() -> me.xavi.vitalis.medical.BloodLevel.MAX_BLOOD_ML)
                    .persistent(Codec.DOUBLE)
    );

    private static List<Float> defaultInjuries() {
        List<Float> list = new ArrayList<>(BODY_PARTS.length);
        for (int i = 0; i < BODY_PARTS.length; i++) list.add(0.0f);
        return list;
    }

    private static List<Integer> defaultBodyPartHp() {
        List<Integer> list = new ArrayList<>(BodyPart.VALUES.length);
        for (BodyPart part : BodyPart.VALUES) list.add(part.getMaxHp());
        return list;
    }

    private static List<Integer> defaultBodyPartStatus() {
        List<Integer> list = new ArrayList<>(BodyPart.VALUES.length);
        for (int i = 0; i < BodyPart.VALUES.length; i++) list.add(InjuryStatus.NONE.ordinal());
        return list;
    }

    public static void ensureRegistered() {
        // No-op; referencing this class triggers static initialization of
        // the AttachmentType fields above.
    }

    public static boolean isOnTable(PlayerEntity player) {
        return player.getAttachedOrElse(ON_TABLE, false);
    }

    public static void setOnTable(PlayerEntity player, boolean onTable) {
        player.setAttached(ON_TABLE, onTable);
    }

    public static BlockPos getTablePos(PlayerEntity player) {
        return decodePos(player.getAttachedOrElse(TABLE_POS, List.of()));
    }

    public static void setTablePos(PlayerEntity player, BlockPos pos) {
        player.setAttached(TABLE_POS, encodePos(pos));
    }

    /** The exact world position the player should be locked to while lying down. */
    public static BlockPos getLockPos(PlayerEntity player) {
        BlockPos lock = decodePos(player.getAttachedOrElse(LOCK_POS, List.of()));
        return lock != null ? lock : getTablePos(player);
    }

    public static void setLockPos(PlayerEntity player, BlockPos pos) {
        player.setAttached(LOCK_POS, encodePos(pos));
    }

    public static float getLockYaw(PlayerEntity player) {
        return player.getAttachedOrElse(LOCK_YAW, 0.0f);
    }

    public static void setLockYaw(PlayerEntity player, float yaw) {
        player.setAttached(LOCK_YAW, yaw);
    }

    public static float getInjury(PlayerEntity player, String bodyPart) {
        int index = indexOf(bodyPart);
        if (index < 0) return 0.0f;
        List<Float> injuries = player.getAttachedOrElse(INJURIES, defaultInjuries());
        return index < injuries.size() ? injuries.get(index) : 0.0f;
    }

    public static void setInjury(PlayerEntity player, String bodyPart, float value) {
        int index = indexOf(bodyPart);
        if (index < 0) return;

        float clamped = Math.min(1.0f, Math.max(0.0f, value));
        List<Float> current = new ArrayList<>(player.getAttachedOrElse(INJURIES, defaultInjuries()));
        while (current.size() <= index) current.add(0.0f);
        current.set(index, clamped);
        player.setAttached(INJURIES, current);
    }

    /** Returns the injury values in the same fixed order as {@link #BODY_PARTS}. */
    public static float[] getAllInjuries(PlayerEntity player) {
        List<Float> injuries = player.getAttachedOrElse(INJURIES, defaultInjuries());
        float[] result = new float[BODY_PARTS.length];
        for (int i = 0; i < BODY_PARTS.length; i++) {
            result[i] = i < injuries.size() ? injuries.get(i) : 0.0f;
        }
        return result;
    }

    public static void clearAllInjuries(PlayerEntity player) {
        player.setAttached(INJURIES, defaultInjuries());
    }

    private static int indexOf(String bodyPart) {
        for (int i = 0; i < BODY_PARTS.length; i++) {
            if (BODY_PARTS[i].equals(bodyPart)) return i;
        }
        return -1;
    }

    private static List<Integer> encodePos(BlockPos pos) {
        if (pos == null) return List.of();
        return List.of(pos.getX(), pos.getY(), pos.getZ());
    }

    private static BlockPos decodePos(List<Integer> list) {
        if (list == null || list.size() != 3) return null;
        return new BlockPos(list.get(0), list.get(1), list.get(2));
    }

    // ------------------------------------------------------------------
    // Body part HP / injury status / blood volume
    // ------------------------------------------------------------------

    /** Current HP (0..maxHp) for the given body part. */
    public static int getBodyPartHp(PlayerEntity player, BodyPart part) {
        List<Integer> hp = player.getAttachedOrElse(BODY_PART_HP, defaultBodyPartHp());
        int index = part.ordinal();
        return index < hp.size() ? hp.get(index) : part.getMaxHp();
    }

    /** Sets the HP (clamped to 0..maxHp) for the given body part. */
    public static void setBodyPartHp(PlayerEntity player, BodyPart part, int hp) {
        int clamped = Math.max(0, Math.min(part.getMaxHp(), hp));
        List<Integer> current = new ArrayList<>(player.getAttachedOrElse(BODY_PART_HP, defaultBodyPartHp()));
        while (current.size() <= part.ordinal()) current.add(BodyPart.VALUES[current.size()].getMaxHp());
        current.set(part.ordinal(), clamped);
        player.setAttached(BODY_PART_HP, current);
    }

    /** Adjusts the HP of the given body part by {@code delta} (can be negative), clamped to 0..maxHp. */
    public static void addBodyPartHp(PlayerEntity player, BodyPart part, int delta) {
        setBodyPartHp(player, part, getBodyPartHp(player, part) + delta);
    }

    /** Returns the HP of every body part, in {@link BodyPart#VALUES} order. */
    public static int[] getAllBodyPartHp(PlayerEntity player) {
        List<Integer> hp = player.getAttachedOrElse(BODY_PART_HP, defaultBodyPartHp());
        int[] result = new int[BodyPart.VALUES.length];
        for (int i = 0; i < BodyPart.VALUES.length; i++) {
            result[i] = i < hp.size() ? hp.get(i) : BodyPart.VALUES[i].getMaxHp();
        }
        return result;
    }

    /** Current {@link InjuryStatus} for the given body part. */
    public static InjuryStatus getBodyPartStatus(PlayerEntity player, BodyPart part) {
        List<Integer> status = player.getAttachedOrElse(BODY_PART_STATUS, defaultBodyPartStatus());
        int index = part.ordinal();
        int ordinal = index < status.size() ? status.get(index) : InjuryStatus.NONE.ordinal();
        if (ordinal < 0 || ordinal >= InjuryStatus.VALUES.length) return InjuryStatus.NONE;
        return InjuryStatus.VALUES[ordinal];
    }

    /** Sets the {@link InjuryStatus} for the given body part. */
    public static void setBodyPartStatus(PlayerEntity player, BodyPart part, InjuryStatus status) {
        List<Integer> current = new ArrayList<>(player.getAttachedOrElse(BODY_PART_STATUS, defaultBodyPartStatus()));
        while (current.size() <= part.ordinal()) current.add(InjuryStatus.NONE.ordinal());
        current.set(part.ordinal(), status.ordinal());
        player.setAttached(BODY_PART_STATUS, current);
    }

    /** Returns the {@link InjuryStatus} of every body part, in {@link BodyPart#VALUES} order. */
    public static InjuryStatus[] getAllBodyPartStatuses(PlayerEntity player) {
        List<Integer> status = player.getAttachedOrElse(BODY_PART_STATUS, defaultBodyPartStatus());
        InjuryStatus[] result = new InjuryStatus[BodyPart.VALUES.length];
        for (int i = 0; i < BodyPart.VALUES.length; i++) {
            int ordinal = i < status.size() ? status.get(i) : InjuryStatus.NONE.ordinal();
            result[i] = (ordinal >= 0 && ordinal < InjuryStatus.VALUES.length) ? InjuryStatus.VALUES[ordinal] : InjuryStatus.NONE;
        }
        return result;
    }

    /** Resets all body parts to full HP and {@link InjuryStatus#NONE}. */
    public static void resetBodyParts(PlayerEntity player) {
        player.setAttached(BODY_PART_HP, defaultBodyPartHp());
        player.setAttached(BODY_PART_STATUS, defaultBodyPartStatus());
    }

    /** Current blood volume in milliliters (0..{@link me.xavi.vitalis.medical.BloodLevel#MAX_BLOOD_ML}). */
    public static double getBloodMl(PlayerEntity player) {
        return player.getAttachedOrElse(BLOOD_ML, me.xavi.vitalis.medical.BloodLevel.MAX_BLOOD_ML);
    }

    /** Sets the blood volume in milliliters, clamped to 0..MAX_BLOOD_ML. */
    public static void setBloodMl(PlayerEntity player, double ml) {
        double clamped = Math.max(0.0, Math.min(me.xavi.vitalis.medical.BloodLevel.MAX_BLOOD_ML, ml));
        player.setAttached(BLOOD_ML, clamped);
    }

    /** Adjusts the blood volume by {@code deltaMl} (can be negative), clamped to 0..MAX_BLOOD_ML. */
    public static void addBloodMl(PlayerEntity player, double deltaMl) {
        setBloodMl(player, getBloodMl(player) + deltaMl);
    }

    /** Resets blood volume to its maximum. */
    public static void resetBlood(PlayerEntity player) {
        setBloodMl(player, me.xavi.vitalis.medical.BloodLevel.MAX_BLOOD_ML);
    }
}
