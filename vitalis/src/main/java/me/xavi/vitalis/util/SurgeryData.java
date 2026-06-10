package me.xavi.vitalis.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class SurgeryData {
    private static final String KEY = "VitalisSurgery";

    public static boolean isOnTable(PlayerEntity player) {
        return getSurgeryNbt(player).getBoolean("OnTable").orElse(false);
    }

    public static void setOnTable(PlayerEntity player, boolean onTable) {
        getSurgeryNbt(player).putBoolean("OnTable", onTable);
    }

    public static BlockPos getTablePos(PlayerEntity player) {
        var posOpt = getSurgeryNbt(player).getIntArray("TablePos");
        if (posOpt.isPresent()) {
            int[] pos = posOpt.get();
            if (pos.length == 3) return new BlockPos(pos[0], pos[1], pos[2]);
        }
        return null;
    }

    public static void setTablePos(PlayerEntity player, BlockPos pos) {
        if (pos != null) {
            getSurgeryNbt(player).putIntArray("TablePos", new int[]{pos.getX(), pos.getY(), pos.getZ()});
        }
    }

    public static float getInjury(PlayerEntity player, String bodyPart) {
        return getInjuriesNbt(player).getFloat(bodyPart).orElse(0.0f);
    }

    public static void setInjury(PlayerEntity player, String bodyPart, float value) {
        float clamped = Math.min(1.0f, Math.max(0.0f, value));
        getInjuriesNbt(player).putFloat(bodyPart, clamped);
    }

    public static void clearAllInjuries(PlayerEntity player) {
        getSurgeryNbt(player).put("Injuries", new NbtCompound());
    }

    private static NbtCompound getSurgeryNbt(PlayerEntity player) {
        NbtCompound persistent = player.getPersistentData();
        if (!persistent.contains(KEY)) {
            persistent.put(KEY, new NbtCompound());
        }
        return persistent.getCompound(KEY).orElse(new NbtCompound());
    }

    private static NbtCompound getInjuriesNbt(PlayerEntity player) {
        NbtCompound surgeryNbt = getSurgeryNbt(player);
        if (!surgeryNbt.contains("Injuries")) {
            surgeryNbt.put("Injuries", new NbtCompound());
        }
        return surgeryNbt.getCompound("Injuries").orElse(new NbtCompound());
    }
}