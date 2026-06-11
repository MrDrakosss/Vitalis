package me.xavi.vitalis.network;

import me.xavi.vitalis.medical.BodyPart;
import me.xavi.vitalis.registry.ModNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SurgeryActionPayload(BodyPart bodyPart) implements CustomPacketPayload {

    public static final Type<SurgeryActionPayload> ID = ModNetwork.SURGERY_ACTION;

    public static final StreamCodec<FriendlyByteBuf, SurgeryActionPayload> CODEC =
            StreamCodec.of(
                    (buf, payload) -> buf.writeEnum(payload.bodyPart),
                    buf -> new SurgeryActionPayload(buf.readEnum(BodyPart.class))
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}