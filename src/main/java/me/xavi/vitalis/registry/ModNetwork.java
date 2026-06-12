package me.xavi.vitalis.registry;

import me.xavi.vitalis.Vitalis;
import me.xavi.vitalis.block.SurgeryTableBlock;
import me.xavi.vitalis.medical.BodyPart;
import me.xavi.vitalis.medical.DownedManager;
import me.xavi.vitalis.medical.InjuryStatus;
import me.xavi.vitalis.medical.SurgeryTreatmentManager;
import me.xavi.vitalis.network.*;
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

    public static final CustomPacketPayload.Type<SurgeryTreatmentPayload> SURGERY_TREATMENT =
            new CustomPacketPayload.Type<>(Vitalis.id("surgery_treatment"));

    public static final CustomPacketPayload.Type<DownedStatePayload> DOWNED_STATE =
            new CustomPacketPayload.Type<>(Vitalis.id("downed_state"));

    public static final CustomPacketPayload.Type<DownedGiveUpPayload> DOWNED_GIVE_UP =
            new CustomPacketPayload.Type<>(Vitalis.id("downed_give_up"));

    private ModNetwork() {
    }

    public static void initialize() {
        PayloadTypeRegistry.playS2C().register(SURGERY_STATE, SurgeryStatePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(MEDICAL_STATE, MedicalStatePayload.CODEC);

        PayloadTypeRegistry.playC2S().register(SURGERY_LEAVE, SurgeryLeavePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SURGERY_TREATMENT, SurgeryTreatmentPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SURGERY_ACTION, SurgeryActionPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SURGERY_LEAVE, (payload, context) ->
                context.server().execute(() -> SurgeryTableBlock.getUp(context.player()))
        );

        ServerPlayNetworking.registerGlobalReceiver(SURGERY_ACTION, (payload, context) ->
                context.server().execute(() -> {
                    if (!SurgeryData.isOnTable(context.player())) {
                        return;
                    }

                    if (payload.bloodTreatment()) {
                        SurgeryTreatmentManager.startBloodTreatment(context.player());
                    } else {
                        SurgeryTreatmentManager.startBodyPartTreatment(context.player(), payload.bodyPart());
                    }
                })
        );

        PayloadTypeRegistry.playS2C().register(DOWNED_STATE, DownedStatePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(DOWNED_GIVE_UP, DownedGiveUpPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(DOWNED_GIVE_UP, (payload, context) ->
                context.server().execute(() -> DownedManager.giveUp(context.player()))
        );
    }
}