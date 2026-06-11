package me.xavi.vitalis.network;

import me.xavi.vitalis.medical.BodyPart;
import me.xavi.vitalis.registry.ModNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;

public record MedicalStatePayload(
        int[] bodyPartHp,
        int[] bodyPartStatus,
        double bloodMl
) implements CustomPacketPayload {

    public static final Type<MedicalStatePayload> ID =
            ModNetwork.MEDICAL_STATE;

    public static final StreamCodec<FriendlyByteBuf, MedicalStatePayload> CODEC =
            StreamCodec.of(
                    MedicalStatePayload::write,
                    MedicalStatePayload::read
            );

    private static void write(FriendlyByteBuf buffer, MedicalStatePayload payload) {
        writeIntArray(buffer, payload.bodyPartHp);
        writeIntArray(buffer, payload.bodyPartStatus);
        buffer.writeDouble(payload.bloodMl);
    }

    private static MedicalStatePayload read(FriendlyByteBuf buffer) {
        int[] hp = readIntArray(buffer);
        int[] status = readIntArray(buffer);
        double blood = buffer.readDouble();

        return new MedicalStatePayload(hp, status, blood);
    }

    private static void writeIntArray(FriendlyByteBuf buffer, int[] values) {
        int expectedLength = BodyPart.VALUES.length;

        buffer.writeVarInt(expectedLength);

        for (int i = 0; i < expectedLength; i++) {
            int value = i < values.length ? values[i] : 0;
            buffer.writeVarInt(value);
        }
    }

    private static int[] readIntArray(FriendlyByteBuf buffer) {
        int length = buffer.readVarInt();

        int safeLength = Math.max(0, Math.min(length, BodyPart.VALUES.length));
        int[] values = new int[safeLength];

        for (int i = 0; i < length; i++) {
            int value = buffer.readVarInt();

            if (i < safeLength) {
                values[i] = value;
            }
        }

        return values;
    }

    private static List<Integer> toList(int[] array) {
        List<Integer> list = new ArrayList<>(array.length);

        for (int value : array) {
            list.add(value);
        }

        return list;
    }

    private static int[] toArray(List<Integer> list) {
        int[] array = new int[list.size()];

        for (int i = 0; i < array.length; i++) {
            array[i] = list.get(i);
        }

        return array;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}