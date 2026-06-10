package me.xavi.vitalis.registry;

import me.xavi.vitalis.Vitalis;
import me.xavi.vitalis.network.SurgeryStatePayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.packet.CustomPayload;

public class ModNetwork {
    public static final CustomPayload.Id<SurgeryStatePayload> SURGERY_STATE =
            new CustomPayload.Id<>(Vitalis.id("surgery_state"));

    public static void initialize() {
        PayloadTypeRegistry.playS2C().register(SURGERY_STATE, SurgeryStatePayload.CODEC);
    }
}