package me.xavi.vitalis.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.xavi.vitalis.client.ClientSurgeryState;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;

public class InjuryHologramFeatureRenderer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public InjuryHologramFeatureRenderer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            AbstractClientPlayer player,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        if (!ClientSurgeryState.isActive()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();

        if (client.player == null || player.getId() != client.player.getId()) {
            return;
        }

        Font font = client.font;

        poseStack.pushPose();

        poseStack.translate(0.0D, 1.18D, 0.0D);

        float lockYaw = ClientSurgeryState.getLockYaw();

        poseStack.mulPose(Axis.YP.rotationDegrees(-(180.0F - lockYaw)));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));

        float headInjury = ClientSurgeryState.getInjury("head");
        float leftArmInjury = ClientSurgeryState.getInjury("left_arm");
        float rightArmInjury = ClientSurgeryState.getInjury("right_arm");
        float chestInjury = ClientSurgeryState.getInjury("chest");
        float legsInjury = ClientSurgeryState.getInjury("legs");

        if (headInjury > 0.0F) {
            renderHologram(
                    poseStack,
                    buffer,
                    packedLight,
                    font,
                    Component.literal(String.format("HEAD %.0f%%", headInjury * 100.0F)).withStyle(ChatFormatting.RED),
                    0.0F,
                    0.34F,
                    -0.78F
            );
        }

        if (chestInjury > 0.0F) {
            renderHologram(
                    poseStack,
                    buffer,
                    packedLight,
                    font,
                    Component.literal(String.format("CHEST %.0f%%", chestInjury * 100.0F)).withStyle(ChatFormatting.GOLD),
                    0.0F,
                    0.34F,
                    -0.25F
            );
        }

        if (leftArmInjury > 0.0F) {
            renderHologram(
                    poseStack,
                    buffer,
                    packedLight,
                    font,
                    Component.literal(String.format("L ARM %.0f%%", leftArmInjury * 100.0F)).withStyle(ChatFormatting.YELLOW),
                    -0.48F,
                    0.34F,
                    -0.15F
            );
        }

        if (rightArmInjury > 0.0F) {
            renderHologram(
                    poseStack,
                    buffer,
                    packedLight,
                    font,
                    Component.literal(String.format("R ARM %.0f%%", rightArmInjury * 100.0F)).withStyle(ChatFormatting.YELLOW),
                    0.48F,
                    0.34F,
                    -0.15F
            );
        }

        if (legsInjury > 0.0F) {
            renderHologram(
                    poseStack,
                    buffer,
                    packedLight,
                    font,
                    Component.literal(String.format("LEGS %.0f%%", legsInjury * 100.0F)).withStyle(ChatFormatting.GREEN),
                    0.0F,
                    0.34F,
                    0.62F
            );
        }

        poseStack.popPose();
    }

    private void renderHologram(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            Font font,
            Component text,
            float x,
            float y,
            float z
    ) {
        Minecraft client = Minecraft.getInstance();

        poseStack.pushPose();

        poseStack.translate(x, y, z);

        poseStack.mulPose(client.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(-0.018F, -0.018F, 0.018F);

        Matrix4f matrix = poseStack.last().pose();

        float width = font.width(text);
        float drawX = -width / 2.0F;

        font.drawInBatch(
                text,
                drawX,
                0.0F,
                0xFFFFFFFF,
                false,
                matrix,
                buffer,
                Font.DisplayMode.NORMAL,
                0,
                packedLight
        );

        poseStack.popPose();
    }
}