package me.xavi.vitalis;

import me.xavi.vitalis.block.SurgeryTableBlock;
import me.xavi.vitalis.command.VitalisCommands;
import me.xavi.vitalis.medical.InjuryStatus;
import me.xavi.vitalis.medical.MedicalSystem;
import me.xavi.vitalis.network.MedicalStatePayload;
import me.xavi.vitalis.registry.*;
import me.xavi.vitalis.util.SurgeryData;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Vitalis implements ModInitializer {

    public static final String MOD_ID = "vitalis";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final double TABLE_TOP_HEIGHT = 1.0D;

    @Override
    public void onInitialize() {
        ModBlocks.initialize();
        ModBlockEntities.initialize();
        ModNetwork.initialize();
        ModItems.initialize();
        ModParticles.initialize();

        SurgeryData.ensureRegistered();


        ServerTickEvents.END_WORLD_TICK.register(level -> {
            for (Player player : level.players()) {

                MedicalSystem.tick(player);

                if (player.tickCount % 20 == 0 && player instanceof ServerPlayer serverPlayer) {
                    ServerPlayNetworking.send(
                            serverPlayer,
                            new MedicalStatePayload(
                                    SurgeryData.getAllBodyPartHp(player),
                                    statusOrdinals(SurgeryData.getAllBodyPartStatuses(player)),
                                    SurgeryData.getBloodMl(player)
                            )
                    );
                }

                if (!SurgeryData.isOnTable(player)) {
                    continue;
                }

                if (!player.isAlive() || player.isShiftKeyDown()) {
                    SurgeryTableBlock.getUp(player);
                    continue;
                }

                BlockPos lockPos = SurgeryData.getLockPos(player);

                if (lockPos == null) {
                    continue;
                }

                BlockState tableState = level.getBlockState(lockPos);

                double offX = 0.0D;
                double offZ = 0.0D;

                if (tableState.getBlock() == ModBlocks.SURGERY_TABLE) {
                    Direction facing = tableState.getValue(SurgeryTableBlock.FACING);

                    offX = facing.getStepX() * 1.0D;
                    offZ = facing.getStepZ() * 1.0D;
                }

                double x = lockPos.getX() + 0.5D + offX;
                double y = lockPos.getY() + SurgeryTableBlock.TABLE_SURFACE_HEIGHT;
                double z = lockPos.getZ() + 0.5D + offZ;

                float yaw = SurgeryData.getLockYaw(player);

                double dx = player.getX() - x;
                double dy = player.getY() - y;
                double dz = player.getZ() - z;

                boolean drifted =
                        (dx * dx + dy * dy + dz * dz) > 0.0025D;

                if (drifted) {
                    if (player instanceof ServerPlayer serverPlayer) {
                        serverPlayer.connection.teleport(
                                x,
                                y,
                                z,
                                yaw,
                                0.0F
                        );
                    } else {
                        player.setPos(x, y, z);
                        player.setYRot(yaw);
                        player.setXRot(0.0F);
                    }

                    player.setYBodyRot(yaw);
                    player.setYHeadRot(yaw);
                }

                player.setDeltaMovement(Vec3.ZERO);
                player.fallDistance = 0.0F;
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                VitalisCommands.register(dispatcher)
        );

        LOGGER.info("Vitalis initialized!");
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(
                MOD_ID,
                path
        );
    }

    private static int[] statusOrdinals(InjuryStatus[] statuses) {
        int[] result = new int[statuses.length];

        for (int i = 0; i < statuses.length; i++) {
            result[i] = statuses[i].ordinal();
        }

        return result;
    }
}