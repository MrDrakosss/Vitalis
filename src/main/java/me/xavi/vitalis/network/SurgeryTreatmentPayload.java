package me.xavi.vitalis.network;

import me.xavi.vitalis.registry.ModNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SurgeryTreatmentPayload(
        boolean active,
        int progressTicks,
        int totalTicks
) implements CustomPacketPayload {

    public static final Type<SurgeryTreatmentPayload> ID = ModNetwork.SURGERY_TREATMENT;

    public static final StreamCodec<FriendlyByteBuf, SurgeryTreatmentPayload> CODEC =
            StreamCodec.of(
                    (buffer, payload) -> {
                        buffer.writeBoolean(payload.active);
                        buffer.writeVarInt(payload.progressTicks);
                        buffer.writeVarInt(payload.totalTicks);
                    },
                    buffer -> new SurgeryTreatmentPayload(
                            buffer.readBoolean(),
                            buffer.readVarInt(),
                            buffer.readVarInt()
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}