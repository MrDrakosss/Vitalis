package me.xavi.vitalis.client;

import me.xavi.vitalis.block.SurgeryTableBlock;
import me.xavi.vitalis.client.input.OrbitCameraHandler;
import me.xavi.vitalis.client.particle.BloodParticle;
import me.xavi.vitalis.client.renderer.MedicalEffectsOverlay;
import me.xavi.vitalis.client.renderer.MedicalStatusHud;
import me.xavi.vitalis.client.screen.SurgeryScreen;
import me.xavi.vitalis.registry.ModBlocks;
import me.xavi.vitalis.registry.ModNetwork;
import me.xavi.vitalis.registry.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

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
                    Direction facing = Direction.NORTH;

                    if (client.level != null) {
                        BlockState state = client.level.getBlockState(payload.tablePos());

                        if (state.getBlock() == ModBlocks.SURGERY_TABLE) {
                            facing = state.getValue(SurgeryTableBlock.FACING);
                        }
                    }

                    client.options.setCameraType(CameraType.THIRD_PERSON_BACK);

                    OrbitCameraHandler.activate(
                            payload.tablePos(),
                            facing,
                            player
                    );

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

        MedicalEffectsOverlay.register();
        MedicalStatusHud.register();
    }
}