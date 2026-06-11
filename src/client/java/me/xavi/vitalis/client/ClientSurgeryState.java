package me.xavi.vitalis.client;

import me.xavi.vitalis.block.SurgeryTableBlock;
import me.xavi.vitalis.registry.ModBlocks;
import me.xavi.vitalis.util.SurgeryData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientSurgeryState {

    private static boolean active = false;
    private static BlockPos tablePos = null;
    private static final float[] injuries = new float[SurgeryData.BODY_PARTS.length];

    private static final Map<UUID, BlockPos> LYING_PLAYERS = new HashMap<>();

    public static void update(UUID playerUuid, BlockPos pos, boolean isActive, float[] newInjuries) {
        Minecraft client = Minecraft.getInstance();

        if (isActive) {
            LYING_PLAYERS.put(playerUuid, pos);
        } else {
            LYING_PLAYERS.remove(playerUuid);
        }

        if (client.player != null && client.player.getUUID().equals(playerUuid)) {
            active = isActive;
            tablePos = pos;

            if (newInjuries != null) {
                int len = Math.min(injuries.length, newInjuries.length);
                System.arraycopy(newInjuries, 0, injuries, 0, len);
            }

            if (!isActive) {
                for (int i = 0; i < injuries.length; i++) {
                    injuries[i] = 0.0F;
                }
            }
        }
    }

    public static boolean isActive() {
        return active;
    }

    public static boolean isLying(UUID uuid) {
        return LYING_PLAYERS.containsKey(uuid);
    }

    public static BlockPos getTablePos(UUID uuid) {
        return LYING_PLAYERS.get(uuid);
    }

    public static BlockPos getTablePos() {
        return tablePos;
    }

    public static float getLockYaw() {
        if (tablePos == null) {
            return 0.0F;
        }

        return getLockYaw(tablePos);
    }

    public static float getLockYaw(UUID uuid) {
        BlockPos pos = LYING_PLAYERS.get(uuid);

        if (pos == null) {
            return 0.0F;
        }

        return getLockYaw(pos);
    }

    private static float getLockYaw(BlockPos pos) {
        Minecraft client = Minecraft.getInstance();

        if (client.level == null) {
            return 0.0F;
        }

        BlockState state = client.level.getBlockState(pos);

        if (state.getBlock() != ModBlocks.SURGERY_TABLE) {
            return 0.0F;
        }

        Direction facing = state.getValue(SurgeryTableBlock.FACING);

        return switch (facing) {
            case SOUTH -> 0.0F;
            case NORTH -> 180.0F;
            case WEST -> 90.0F;
            case EAST -> -90.0F;
            default -> 0.0F;
        };
    }

    public static float getInjury(String bodyPart) {
        for (int i = 0; i < SurgeryData.BODY_PARTS.length; i++) {
            if (SurgeryData.BODY_PARTS[i].equals(bodyPart)) {
                return injuries[i];
            }
        }

        return 0.0F;
    }
}