package me.xavi.vitalis.network;

import me.xavi.vitalis.registry.ModNetwork;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;   // <- beépített osztály importja
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record SurgeryStatePayload(BlockPos tablePos, boolean active) implements CustomPayload {
    public static final CustomPayload.Id<SurgeryStatePayload> ID = ModNetwork.SURGERY_STATE;

    public static final PacketCodec<RegistryByteBuf, SurgeryStatePayload> CODEC =
            PacketCodec.tuple(
                    BlockPos.PACKET_CODEC, SurgeryStatePayload::tablePos,
                    PacketCodecs.BOOLEAN, SurgeryStatePayload::active,   // <- beépített BOOL
                    SurgeryStatePayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}