package me.xavi.vitalis.client;

import me.xavi.vitalis.block.SurgeryTableBlock;
import me.xavi.vitalis.util.SurgeryData;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * Client-side mirror of the local player's surgery state, updated whenever
 * a {@link me.xavi.vitalis.network.SurgeryStatePayload} arrives from the server.
 */
public class ClientSurgeryState {
    private static boolean active = false;
    private static BlockPos tablePos = null;
    private static final float[] injuries = new float[SurgeryData.BODY_PARTS.length];

    public static void update(BlockPos pos, boolean isActive, float[] newInjuries) {
        active = isActive;
        tablePos = pos;
        if (newInjuries != null) {
            int len = Math.min(injuries.length, newInjuries.length);
            System.arraycopy(newInjuries, 0, injuries, 0, len);
        }
        if (!isActive) {
            for (int i = 0; i < injuries.length; i++) injuries[i] = 0f;
        }
    }

    public static boolean isActive() {
        return active;
    }

    public static BlockPos getTablePos() {
        return tablePos;
    }

    /**
     * Returns the locked lying-down yaw, derived from the surgery table's
     * {@code facing} block state at {@link #getTablePos()}. Mirrors the
     * yaw formula used server-side in {@code SurgeryTableBlock.lieDown}.
     * Returns 0 if the table position/state can't be resolved.
     */
    public static float getLockYaw() {
        if (tablePos == null) return 0.0F;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return 0.0F;

        BlockState state = client.world.getBlockState(tablePos);
        if (!state.isOf(me.xavi.vitalis.registry.ModBlocks.SURGERY_TABLE)) return 0.0F;

        Direction facing = state.get(SurgeryTableBlock.FACING);
        return switch (facing) {
            case SOUTH -> 0.0F;
            case NORTH -> 180.0F;
            case WEST -> 90.0F;
            case EAST -> -90.0F;
            default -> 0.0F;
        };
    }

    /** Returns the injury value (0..1) for the given body part, or 0 if unknown. */
    public static float getInjury(String bodyPart) {
        for (int i = 0; i < SurgeryData.BODY_PARTS.length; i++) {
            if (SurgeryData.BODY_PARTS[i].equals(bodyPart)) {
                return injuries[i];
            }
        }
        return 0f;
    }
}
