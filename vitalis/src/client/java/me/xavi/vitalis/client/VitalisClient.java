package me.xavi.vitalis.client;

import me.xavi.vitalis.client.input.OrbitCameraHandler;
import me.xavi.vitalis.network.SurgeryStatePayload;
import me.xavi.vitalis.registry.ModComponents;
import me.xavi.vitalis.registry.ModNetwork;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.entity.player.PlayerEntity;

public class VitalisClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ModNetwork.SURGERY_STATE, (payload, context) -> {
            context.client().execute(() -> {
                PlayerEntity player = MinecraftClient.getInstance().player;
                if (player != null) {
                    var component = player.getComponent(ModComponents.SURGERY_PLAYER);
                    component.setOnTable(payload.active());
                    if (payload.active()) {
                        component.setTablePos(payload.tablePos());
                        MinecraftClient.getInstance().options.setPerspective(net.minecraft.client.option.Perspective.THIRD_PERSON_BACK);
                        OrbitCameraHandler.activate(payload.tablePos(), player);
                    } else {
                        OrbitCameraHandler.deactivate();
                        MinecraftClient.getInstance().options.setPerspective(net.minecraft.client.option.Perspective.FIRST_PERSON);
                    }
                }
            });
        });

        /*LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper) -> {
            if (entityRenderer instanceof PlayerEntityRenderer playerRenderer) {
                // A konstruktornak átadjuk a context-et
                registrationHelper.register(new InjuryHologramFeatureRenderer(playerRenderer));
            }
        });*/
    }
}