package me.xavi.vitalis;

import me.xavi.vitalis.block.SurgeryTableBlock;
import me.xavi.vitalis.registry.ModBlockEntities;
import me.xavi.vitalis.registry.ModBlocks;
import me.xavi.vitalis.registry.ModNetwork;
import me.xavi.vitalis.util.SurgeryData;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Vitalis implements ModInitializer {

    public static final String MOD_ID = "vitalis";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /** Table top height in blocks (matches the full-block-tall table). */
    public static final double TABLE_TOP_HEIGHT = 1.0;

    @Override
    public void onInitialize() {
        ModBlocks.initialize();
        ModBlockEntities.initialize();
        ModNetwork.initialize();

        // Force-load SurgeryData so its AttachmentType fields register at init time.
        SurgeryData.ensureRegistered();

        ServerTickEvents.END_WORLD_TICK.register(world -> {
            for (PlayerEntity player : world.getPlayers()) {
                me.xavi.vitalis.medical.MedicalSystem.tick(player);

                // Send the always-on medical HUD state once per second.
                if (player.age % 20 == 0 && player instanceof ServerPlayerEntity serverPlayer) {
                    net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(serverPlayer,
                            new me.xavi.vitalis.network.MedicalStatePayload(
                                    SurgeryData.getAllBodyPartHp(player),
                                    statusOrdinals(SurgeryData.getAllBodyPartStatuses(player)),
                                    SurgeryData.getBloodMl(player)));
                }

                if (!SurgeryData.isOnTable(player)) continue;

                // Dead or sneaking players automatically get up, which also
                // resets their camera back to first person.
                if (!player.isAlive() || player.isSneaking()) {
                    SurgeryTableBlock.getUp(player);
                    continue;
                }

                BlockPos lockPos = SurgeryData.getLockPos(player);
                if (lockPos == null) continue;

                double x = lockPos.getX() + 0.5;
                double y = lockPos.getY() + TABLE_TOP_HEIGHT;
                double z = lockPos.getZ() + 0.5;
                float yaw = SurgeryData.getLockYaw(player);

                double dx = player.getX() - x;
                double dy = player.getY() - y;
                double dz = player.getZ() - z;
                boolean drifted = (dx * dx + dy * dy + dz * dz) > 0.0025;
                boolean rotated = Math.abs(player.getYaw() - yaw) > 0.5f
                        || Math.abs(player.getBodyYaw() - yaw) > 0.5f
                        || Math.abs(player.getPitch()) > 0.5f;

                if (drifted || rotated) {
                    if (player instanceof ServerPlayerEntity serverPlayer) {
                        // requestTeleport forces an authoritative position/rotation
                        // sync to the client, overriding any in-flight movement
                        // packets that would otherwise snap the player back.
                        serverPlayer.networkHandler.requestTeleport(x, y, z, yaw, 0.0F);
                    } else {
                        player.setPosition(x, y, z);
                        player.setYaw(yaw);
                        player.setPitch(0.0F);
                    }
                    player.setBodyYaw(yaw);
                    player.setHeadYaw(yaw);
                }

                player.setVelocity(Vec3d.ZERO);
                player.fallDistance = 0;
            }
        });

        LOGGER.info("Vitalis initialized for Minecraft 1.21.11!");
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    private static int[] statusOrdinals(me.xavi.vitalis.medical.InjuryStatus[] statuses) {
        int[] result = new int[statuses.length];
        for (int i = 0; i < statuses.length; i++) result[i] = statuses[i].ordinal();
        return result;
    }
}
