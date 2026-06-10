/*
package me.xavi.vitalis.client.renderer;

import me.xavi.vitalis.registry.ModComponents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class InjuryHologramFeatureRenderer extends FeatureRenderer<PlayerEntity, PlayerEntityModel<PlayerEntity>> {

    public InjuryHologramFeatureRenderer(FeatureRendererContext<PlayerEntity, PlayerEntityModel<PlayerEntity>> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                       PlayerEntity player, float limbAngle, float limbDistance,
                       float tickDelta, float animationProgress, float headYaw, float headPitch) {

        if (!player.getComponent(ModComponents.SURGERY_PLAYER).isOnTable()) return;

        var injuries = player.getComponent(ModComponents.SURGERY_PLAYER);
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        if (textRenderer == null) return;

        matrices.push();
        matrices.translate(0, player.getHeight() + 0.3, 0);

        if (injuries.getInjury("head") > 0) {
            renderHologram(matrices, vertexConsumers, light, textRenderer,
                    Text.literal(String.format("⚡ HEAD: %.0f%%", injuries.getInjury("head") * 100)).formatted(Formatting.RED),
                    0, 0.5f, 0);
        }
        if (injuries.getInjury("left_arm") > 0) {
            renderHologram(matrices, vertexConsumers, light, textRenderer,
                    Text.literal(String.format("🦾 LEFT ARM: %.0f%%", injuries.getInjury("left_arm") * 100)).formatted(Formatting.YELLOW),
                    -0.8f, 0.2f, 0);
        }
        if (injuries.getInjury("right_arm") > 0) {
            renderHologram(matrices, vertexConsumers, light, textRenderer,
                    Text.literal(String.format("🦾 RIGHT ARM: %.0f%%", injuries.getInjury("right_arm") * 100)).formatted(Formatting.YELLOW),
                    0.8f, 0.2f, 0);
        }
        if (injuries.getInjury("chest") > 0) {
            renderHologram(matrices, vertexConsumers, light, textRenderer,
                    Text.literal(String.format("❤️ CHEST: %.0f%%", injuries.getInjury("chest") * 100)).formatted(Formatting.GOLD),
                    0, 0.0f, 0);
        }
        if (injuries.getInjury("legs") > 0) {
            renderHologram(matrices, vertexConsumers, light, textRenderer,
                    Text.literal(String.format("🦵 LEGS: %.0f%%", injuries.getInjury("legs") * 100)).formatted(Formatting.GREEN),
                    0, -0.4f, 0);
        }
        matrices.pop();
    }

    private void renderHologram(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                                TextRenderer textRenderer, Text text, float x, float y, float z) {
        matrices.push();
        matrices.translate(x, y, z);
        matrices.scale(0.025f, -0.025f, 0.025f);
        textRenderer.draw(text, -textRenderer.getWidth(text) / 2f, 0, 0xFFFFFF, true,
                matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
        matrices.pop();
    }
}*/
