package me.xavi.vitalis.network;

import me.xavi.vitalis.registry.ModNetwork;
import me.xavi.vitalis.util.SurgeryData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record SurgeryStatePayload(
        UUID playerUuid,
        BlockPos tablePos,
        boolean active,
        float[] injuries
) implements CustomPacketPayload {

    public static final Type<SurgeryStatePayload> ID =
            ModNetwork.SURGERY_STATE;

    public static final StreamCodec<FriendlyByteBuf, SurgeryStatePayload> CODEC =
            StreamCodec.of(
                    SurgeryStatePayload::write,
                    SurgeryStatePayload::read
            );

    private static void write(FriendlyByteBuf buffer, SurgeryStatePayload payload) {
        buffer.writeUUID(payload.playerUuid);
        buffer.writeBlockPos(payload.tablePos);
        buffer.writeBoolean(payload.active);

        int expectedLength = SurgeryData.BODY_PARTS.length;
        buffer.writeVarInt(expectedLength);

        for (int i = 0; i < expectedLength; i++) {
            float value = i < payload.injuries.length ? payload.injuries[i] : 0.0F;
            buffer.writeFloat(value);
        }
    }

    private static SurgeryStatePayload read(FriendlyByteBuf buffer) {
        UUID uuid = buffer.readUUID();
        BlockPos tablePos = buffer.readBlockPos();
        boolean active = buffer.readBoolean();

        int length = buffer.readVarInt();
        int safeLength = Math.max(0, Math.min(length, SurgeryData.BODY_PARTS.length));
        float[] injuries = new float[safeLength];

        for (int i = 0; i < length; i++) {
            float value = buffer.readFloat();

            if (i < safeLength) {
                injuries[i] = value;
            }
        }

        return new SurgeryStatePayload(uuid, tablePos, active, injuries);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}