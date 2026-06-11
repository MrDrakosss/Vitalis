package me.xavi.vitalis.client;

import me.xavi.vitalis.client.input.OrbitCameraHandler;
import me.xavi.vitalis.client.renderer.InjuryHologramFeatureRenderer;
import me.xavi.vitalis.client.renderer.MedicalStatusHud;
import me.xavi.vitalis.network.MedicalStatePayload;
import me.xavi.vitalis.network.SurgeryStatePayload;
import me.xavi.vitalis.registry.ModNetwork;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.entity.player.PlayerEntity;

public class VitalisClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ModNetwork.SURGERY_STATE, (payload, context) -> {
            context.client().execute(() -> {
                PlayerEntity player = MinecraftClient.getInstance().player;
                if (player == null) return;

                ClientSurgeryState.update(payload.tablePos(), payload.active(), payload.injuries());

                MinecraftClient client = MinecraftClient.getInstance();
                if (payload.active()) {
                    client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                    OrbitCameraHandler.activate(payload.tablePos(), player);
                } else {
                    OrbitCameraHandler.deactivate();
                    client.options.setPerspective(Perspective.FIRST_PERSON);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ModNetwork.MEDICAL_STATE, (payload, context) -> {
            context.client().execute(() ->
                    ClientMedicalState.update(payload.bodyPartHp(), payload.bodyPartStatus(), payload.bloodMl()));
        });

        // Keep the perspective locked while the orbit camera is active.
        // (Cursor recentering is handled inside OrbitCameraHandler.tick()
        // itself, so it isn't done twice per frame.)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            OrbitCameraHandler.enforcePerspective();
        });

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            if (entityRenderer instanceof PlayerEntityRenderer playerRenderer) {
                registrationHelper.register(new InjuryHologramFeatureRenderer(playerRenderer));
            }
        });

        MedicalStatusHud.register();
    }
}
