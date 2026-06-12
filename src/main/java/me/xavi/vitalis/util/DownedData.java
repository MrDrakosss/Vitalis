package me.xavi.vitalis.util;

import com.mojang.serialization.Codec;
import me.xavi.vitalis.Vitalis;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.world.entity.player.Player;

public final class DownedData {

    public static final int MAX_DOWNED_TICKS = 20 * 60;

    public static final AttachmentType<Boolean> DOWNED = AttachmentRegistry.create(
            Vitalis.id("downed"),
            builder -> builder.initializer(() -> false).persistent(Codec.BOOL)
    );

    public static final AttachmentType<Integer> DOWNED_TICKS = AttachmentRegistry.create(
            Vitalis.id("downed_ticks"),
            builder -> builder.initializer(() -> 0).persistent(Codec.INT)
    );

    public static final AttachmentType<Boolean> FORCE_DEATH = AttachmentRegistry.create(
            Vitalis.id("force_death"),
            builder -> builder.initializer(() -> false).persistent(Codec.BOOL)
    );

    private DownedData() {
    }

    public static boolean isDowned(Player player) {
        return player.getAttachedOrElse(DOWNED, false);
    }

    public static void setDowned(Player player, boolean downed) {
        player.setAttached(DOWNED, downed);
    }

    public static int getDownedTicks(Player player) {
        return player.getAttachedOrElse(DOWNED_TICKS, 0);
    }

    public static void setDownedTicks(Player player, int ticks) {
        player.setAttached(DOWNED_TICKS, Math.max(0, ticks));
    }

    public static boolean isForceDeath(Player player) {
        return player.getAttachedOrElse(FORCE_DEATH, false);
    }

    public static void setForceDeath(Player player, boolean forceDeath) {
        player.setAttached(FORCE_DEATH, forceDeath);
    }

    public static void clear(Player player) {
        setDowned(player, false);
        setDownedTicks(player, 0);
        setForceDeath(player, false);
    }
}