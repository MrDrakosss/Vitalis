package me.xavi.vitalis.client;

import me.xavi.vitalis.client.input.OrbitCameraHandler;
import me.xavi.vitalis.client.particle.BloodParticle;
import me.xavi.vitalis.client.renderer.MedicalEffectsOverlay;
import me.xavi.vitalis.client.renderer.MedicalStatusHud;
import me.xavi.vitalis.client.screen.SurgeryScreen;
import me.xavi.vitalis.registry.ModNetwork;
import me.xavi.vitalis.registry.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class VitalisClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ModNetwork.SURGERY_STATE, (payload, context) -> {
            context.client().execute(() -> {
                Minecraft client = Minecraft.getInstance();

                ClientSurgeryState.update(
                        payload.playerUuid(),
                        payload.tablePos(),
                        payload.active(),
                        payload.injuries()
                );

                if (client.player == null || !client.player.getUUID().equals(payload.playerUuid())) {
                    return;
                }

                Player player = client.player;

                if (payload.active()) {
                    client.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                    OrbitCameraHandler.activate(payload.tablePos(), player);
                    client.setScreen(new SurgeryScreen());
                } else {
                    OrbitCameraHandler.deactivate();
                    client.options.setCameraType(CameraType.FIRST_PERSON);

                    if (client.screen instanceof SurgeryScreen) {
                        client.setScreen(null);
                    }
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ModNetwork.MEDICAL_STATE, (payload, context) -> {
            context.client().execute(() -> ClientMedicalState.update(
                    payload.bodyPartHp(),
                    payload.bodyPartStatus(),
                    payload.bloodMl()
            ));
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> OrbitCameraHandler.enforcePerspective());

        ParticleFactoryRegistry.getInstance().register(
                ModParticles.BLOOD,
                BloodParticle.Provider::new
        );

        MedicalStatusHud.register();
        MedicalEffectsOverlay.register();
    }
}