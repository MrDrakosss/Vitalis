package me.xavi.vitalis.network;

import me.xavi.vitalis.registry.ModNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SurgeryLeavePayload() implements CustomPacketPayload {

    public static final Type<SurgeryLeavePayload> ID = ModNetwork.SURGERY_LEAVE;

    public static final StreamCodec<FriendlyByteBuf, SurgeryLeavePayload> CODEC =
            StreamCodec.unit(new SurgeryLeavePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}