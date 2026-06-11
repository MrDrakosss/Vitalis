package me.xavi.vitalis.registry;

import me.xavi.vitalis.Vitalis;
import me.xavi.vitalis.block.SurgeryTableBlock;
import me.xavi.vitalis.medical.BodyPart;
import me.xavi.vitalis.medical.InjuryStatus;
import me.xavi.vitalis.network.MedicalStatePayload;
import me.xavi.vitalis.network.SurgeryActionPayload;
import me.xavi.vitalis.network.SurgeryLeavePayload;
import me.xavi.vitalis.network.SurgeryStatePayload;
import me.xavi.vitalis.util.SurgeryData;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;

public final class ModNetwork {

    public static final CustomPacketPayload.Type<SurgeryStatePayload> SURGERY_STATE =
            new CustomPacketPayload.Type<>(Vitalis.id("surgery_state"));

    public static final CustomPacketPayload.Type<MedicalStatePayload> MEDICAL_STATE =
            new CustomPacketPayload.Type<>(Vitalis.id("medical_state"));

    public static final CustomPacketPayload.Type<SurgeryLeavePayload> SURGERY_LEAVE =
            new CustomPacketPayload.Type<>(Vitalis.id("surgery_leave"));

    public static final CustomPacketPayload.Type<SurgeryActionPayload> SURGERY_ACTION =
            new CustomPacketPayload.Type<>(Vitalis.id("surgery_action"));

    private ModNetwork() {
    }

    public static void initialize() {
        PayloadTypeRegistry.playS2C().register(SURGERY_STATE, SurgeryStatePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(MEDICAL_STATE, MedicalStatePayload.CODEC);

        PayloadTypeRegistry.playC2S().register(SURGERY_LEAVE, SurgeryLeavePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SURGERY_ACTION, SurgeryActionPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SURGERY_LEAVE, (payload, context) ->
                context.server().execute(() -> SurgeryTableBlock.getUp(context.player()))
        );

        ServerPlayNetworking.registerGlobalReceiver(SURGERY_ACTION, (payload, context) ->
                context.server().execute(() -> {
                    if (!SurgeryData.isOnTable(context.player())) {
                        return;
                    }

                    BodyPart part = payload.bodyPart();

                    SurgeryData.setBodyPartHp(context.player(), part, part.getMaxHp());
                    SurgeryData.setBodyPartStatus(context.player(), part, InjuryStatus.NONE);

                    context.player().displayClientMessage(
                            Component.translatable("message.vitalis.surgery_finished"),
                            true
                    );
                })
        );
    }
}