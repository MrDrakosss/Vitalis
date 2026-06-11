package me.xavi.vitalis.network;

import me.xavi.vitalis.registry.ModNetwork;
import me.xavi.vitalis.util.SurgeryData;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Sent from the server to the client whenever the player's surgery state
 * changes (lying down, getting healed). {@code injuries} always has
 * {@link SurgeryData#BODY_PARTS}.length entries, in that same order.
 * <p>
 * Internally the float array is encoded as a {@code List<Float>} via
 * {@link PacketCodecs#toList(int)}, since that is the well-documented,
 * stable way to encode a fixed-size collection of primitives.
 */
public record SurgeryStatePayload(BlockPos tablePos, boolean active, float[] injuries) implements CustomPayload {
    public static final CustomPayload.Id<SurgeryStatePayload> ID = ModNetwork.SURGERY_STATE;

    private static final PacketCodec<RegistryByteBuf, List<Float>> INJURY_LIST_CODEC =
            PacketCodecs.FLOAT.<RegistryByteBuf>cast().collect(PacketCodecs.toList(SurgeryData.BODY_PARTS.length));

    public static final PacketCodec<RegistryByteBuf, SurgeryStatePayload> CODEC =
            PacketCodec.tuple(
                    BlockPos.PACKET_CODEC.<RegistryByteBuf>cast(), SurgeryStatePayload::tablePos,
                    PacketCodecs.BOOLEAN.<RegistryByteBuf>cast(), SurgeryStatePayload::active,
                    INJURY_LIST_CODEC, payload -> {
                        List<Float> list = new ArrayList<>(payload.injuries.length);
                        for (float f : payload.injuries) list.add(f);
                        return list;
                    },
                    (pos, active, list) -> {
                        float[] arr = new float[list.size()];
                        for (int i = 0; i < arr.length; i++) arr[i] = list.get(i);
                        return new SurgeryStatePayload(pos, active, arr);
                    }
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
