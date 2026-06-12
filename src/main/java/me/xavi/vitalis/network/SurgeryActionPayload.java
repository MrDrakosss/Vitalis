package me.xavi.vitalis.network;

import me.xavi.vitalis.medical.BodyPart;
import me.xavi.vitalis.registry.ModNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SurgeryActionPayload(boolean bloodTreatment, BodyPart bodyPart) implements CustomPacketPayload {

    public static final Type<SurgeryActionPayload> ID = ModNetwork.SURGERY_ACTION;

    public static final StreamCodec<FriendlyByteBuf, SurgeryActionPayload> CODEC =
            StreamCodec.of(
                    (buffer, payload) -> {
                        buffer.writeBoolean(payload.bloodTreatment);
                        buffer.writeEnum(payload.bodyPart);
                    },
                    buffer -> new SurgeryActionPayload(
                            buffer.readBoolean(),
                            buffer.readEnum(BodyPart.class)
                    )
            );

    public static SurgeryActionPayload bodyPart(BodyPart part) {
        return new SurgeryActionPayload(false, part);
    }

    public static SurgeryActionPayload blood() {
        return new SurgeryActionPayload(true, BodyPart.CHEST);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}