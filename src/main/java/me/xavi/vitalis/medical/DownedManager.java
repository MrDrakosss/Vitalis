package me.xavi.vitalis.medical;

import me.xavi.vitalis.network.DownedStatePayload;
import me.xavi.vitalis.util.DownedData;
import me.xavi.vitalis.util.SurgeryData;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class DownedManager {

    private DownedManager() {
    }

    public static void startDowned(ServerPlayer player) {
        if (DownedData.isDowned(player)) {
            return;
        }

        player.setHealth(1.0F);
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;

        SurgeryData.setBloodMl(player, Math.max(0.0D, SurgeryData.getBloodMl(player)));

        DownedData.setDowned(player, true);
        DownedData.setDownedTicks(player, DownedData.MAX_DOWNED_TICKS);
        DownedData.setForceDeath(player, false);

        sync(player);
    }

    public static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!DownedData.isDowned(player)) {
                continue;
            }

            int ticks = DownedData.getDownedTicks(player);

            player.setHealth(1.0F);
            player.setDeltaMovement(Vec3.ZERO);
            player.fallDistance = 0.0F;

            ticks--;
            DownedData.setDownedTicks(player, ticks);

            if (ticks % 20 == 0) {
                sync(player);
            }

            if (ticks <= 0) {
                forceDeath(player);
            }
        }
    }

    public static void revive(ServerPlayer player) {
        DownedData.clear(player);

        SurgeryData.setBloodMl(player, Math.max(1500.0D, SurgeryData.getBloodMl(player)));

        player.setHealth(6.0F);
        sync(player);
    }

    public static void giveUp(ServerPlayer player) {
        if (!DownedData.isDowned(player)) {
            return;
        }

        forceDeath(player);
    }

    private static void forceDeath(ServerPlayer player) {
        DownedData.setForceDeath(player, true);
        DownedData.setDowned(player, false);
        DownedData.setDownedTicks(player, 0);

        sync(player);

        player.hurt(player.damageSources().genericKill(), Float.MAX_VALUE);
    }

    public static void sync(ServerPlayer player) {
        ServerPlayNetworking.send(
                player,
                new DownedStatePayload(
                        DownedData.isDowned(player),
                        DownedData.getDownedTicks(player)
                )
        );
    }
}