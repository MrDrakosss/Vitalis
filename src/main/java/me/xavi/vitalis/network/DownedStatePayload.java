package me.xavi.vitalis.network;

import me.xavi.vitalis.registry.ModNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record DownedStatePayload(
        boolean active,
        int remainingTicks
) implements CustomPacketPayload {

    public static final Type<DownedStatePayload> ID = ModNetwork.DOWNED_STATE;

    public static final StreamCodec<FriendlyByteBuf, DownedStatePayload> CODEC =
            StreamCodec.of(
                    (buffer, payload) -> {
                        buffer.writeBoolean(payload.active);
                        buffer.writeVarInt(payload.remainingTicks);
                    },
                    buffer -> new DownedStatePayload(
                            buffer.readBoolean(),
                            buffer.readVarInt()
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}