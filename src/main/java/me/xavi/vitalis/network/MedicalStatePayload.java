package me.xavi.vitalis.network;

import me.xavi.vitalis.medical.BodyPart;
import me.xavi.vitalis.registry.ModNetwork;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import java.util.ArrayList;
import java.util.List;

/**
 * Sent from the server to the client periodically (and on any change) so
 * the client can render the always-on medical status HUD. {@code bodyPartHp}
 * and {@code bodyPartStatus} both have {@link BodyPart#VALUES}.length
 * entries, in that fixed order. {@code bodyPartStatus} entries are
 * {@link me.xavi.vitalis.medical.InjuryStatus} ordinals.
 */
public record MedicalStatePayload(int[] bodyPartHp, int[] bodyPartStatus, double bloodMl) implements CustomPayload {
    public static final CustomPayload.Id<MedicalStatePayload> ID = ModNetwork.MEDICAL_STATE;

    private static final PacketCodec<RegistryByteBuf, List<Integer>> INT_LIST_CODEC =
            PacketCodecs.VAR_INT.<RegistryByteBuf>cast().collect(PacketCodecs.toList(BodyPart.VALUES.length));

    public static final PacketCodec<RegistryByteBuf, MedicalStatePayload> CODEC =
            PacketCodec.tuple(
                    INT_LIST_CODEC, payload -> toList(payload.bodyPartHp),
                    INT_LIST_CODEC, payload -> toList(payload.bodyPartStatus),
                    PacketCodecs.DOUBLE.<RegistryByteBuf>cast(), MedicalStatePayload::bloodMl,
                    (hp, status, blood) -> new MedicalStatePayload(toArray(hp), toArray(status), blood)
            );

    private static List<Integer> toList(int[] arr) {
        List<Integer> list = new ArrayList<>(arr.length);
        for (int v : arr) list.add(v);
        return list;
    }

    private static int[] toArray(List<Integer> list) {
        int[] arr = new int[list.size()];
        for (int i = 0; i < arr.length; i++) arr[i] = list.get(i);
        return arr;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
