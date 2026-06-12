package me.xavi.vitalis.network;

import me.xavi.vitalis.registry.ModNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record DownedGiveUpPayload() implements CustomPacketPayload {

    public static final Type<DownedGiveUpPayload> ID = ModNetwork.DOWNED_GIVE_UP;

    public static final StreamCodec<FriendlyByteBuf, DownedGiveUpPayload> CODEC =
            StreamCodec.unit(new DownedGiveUpPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}