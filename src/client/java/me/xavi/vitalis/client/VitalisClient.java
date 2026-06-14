package me.xavi.vitalis.client;

import me.xavi.vitalis.block.OldComputerBlock;
import me.xavi.vitalis.block.SurgeryTableBlock;
import me.xavi.vitalis.client.input.OrbitCameraHandler;
import me.xavi.vitalis.client.particle.BloodParticle;
import me.xavi.vitalis.client.renderer.MedicalEffectsOverlay;
import me.xavi.vitalis.client.renderer.MedicalStatusHud;
import me.xavi.vitalis.client.screen.ComputerScreen;
import me.xavi.vitalis.client.screen.DownedScreen;
import me.xavi.vitalis.client.screen.MedicalCabinetScreen;
import me.xavi.vitalis.client.screen.SurgeryScreen;
import me.xavi.vitalis.client.state.ClientDownedState;
import me.xavi.vitalis.client.state.ClientMedicalState;
import me.xavi.vitalis.client.state.ClientSurgeryState;
import me.xavi.vitalis.client.state.ClientSurgeryTreatmentState;
import me.xavi.vitalis.registry.ModBlocks;
import me.xavi.vitalis.registry.ModMenuTypes;
import me.xavi.vitalis.registry.ModNetwork;
import me.xavi.vitalis.registry.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

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

                    OrbitCameraHandler.activateTable(
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
                    payload.bloodMl(),
                    payload.heartRate(),
                    payload.bloodPressureSystolic(),
                    payload.bloodPressureDiastolic()
            ));
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> OrbitCameraHandler.enforcePerspective());

        ParticleFactoryRegistry.getInstance().register(
                ModParticles.BLOOD,
                BloodParticle.Provider::new
        );

        ClientPlayNetworking.registerGlobalReceiver(ModNetwork.SURGERY_TREATMENT, (payload, context) -> {
            context.client().execute(() -> ClientSurgeryTreatmentState.update(
                    payload.active(),
                    payload.progressTicks(),
                    payload.totalTicks()
            ));
        });

        // Egyedi halál
        ClientPlayNetworking.registerGlobalReceiver(ModNetwork.DOWNED_STATE, (payload, context) -> {
            context.client().execute(() -> {
                Minecraft client = Minecraft.getInstance();

                ClientDownedState.update(payload.active(), payload.remainingTicks());

                if (payload.active()) {
                    client.options.setCameraType(CameraType.THIRD_PERSON_BACK);

                    if (!OrbitCameraHandler.isDownedMode()) {
                        OrbitCameraHandler.activateDowned(client.player);
                    }

                    if (!(client.screen instanceof DownedScreen)) {
                        client.setScreen(new DownedScreen());
                    }
                } else {
                    OrbitCameraHandler.deactivate();

                    if (client.screen instanceof DownedScreen) {
                        client.setScreen(null);
                    }
                }
            });
        });

        MedicalEffectsOverlay.register();
        MedicalStatusHud.register();

        MenuScreens.register(ModMenuTypes.MEDICAL_REFRIGERATOR, MedicalCabinetScreen::new);
        MenuScreens.register(ModMenuTypes.SUPPLY_CABINET, MedicalCabinetScreen::new);
        MenuScreens.register(ModMenuTypes.EQUIPMENT_CABINET, MedicalCabinetScreen::new);

        MenuScreens.register(ModMenuTypes.LARGE_MEDICAL_REFRIGERATOR, MedicalCabinetScreen::new);
        MenuScreens.register(ModMenuTypes.LARGE_SUPPLY_CABINET, MedicalCabinetScreen::new);
        MenuScreens.register(ModMenuTypes.LARGE_EQUIPMENT_CABINET, MedicalCabinetScreen::new);

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.STAND, RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WET_FLOOR, RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.OLD_COMPUTER, RenderType.translucent());

        HudRenderCallback.EVENT.register((graphics, tickDelta) -> {
            Minecraft client = Minecraft.getInstance();

            if (client.player == null || client.level == null || client.hitResult == null) {
                return;
            }

            if (!(client.hitResult instanceof BlockHitResult hit)) {
                return;
            }

            BlockState state = client.level.getBlockState(hit.getBlockPos());

            if (state.getBlock() instanceof OldComputerBlock) {
                Component text = null;

                if (OldComputerBlock.isPowerButtonHitClient(state, hit)) {
                    text = state.getValue(OldComputerBlock.POWERED)
                            ? Component.translatable("screen.vitalis.turn_off")
                            : Component.translatable("screen.vitalis.turn_on");
                } else if (OldComputerBlock.isScreenHit(state, hit)) {
                    text = Component.translatable("screen.vitalis.use");
                }

                if (text != null) {
                    int x = client.getWindow().getGuiScaledWidth() / 2 + 10;
                    int y = client.getWindow().getGuiScaledHeight() / 2 + 8;
                    graphics.drawString(client.font, text, x, y, 0xFFFFFF, true);
                }
            }
        });

        MenuScreens.register(
                ModMenuTypes.COMPUTER,
                ComputerScreen::new
        );
    }
}