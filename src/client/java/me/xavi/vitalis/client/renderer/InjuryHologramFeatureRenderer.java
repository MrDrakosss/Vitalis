package me.xavi.vitalis.client.renderer;

import me.xavi.vitalis.client.ClientSurgeryState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Renders floating injury percentage holograms above the player while they
 * are lying on a surgery table. Only ever shows data for the local client
 * player, since {@link ClientSurgeryState} only tracks that player's state.
 * <p>
 * Uses the 1.21.9+ rendering pipeline: {@link FeatureRenderer} operates on a
 * {@link PlayerEntityRenderState} snapshot and submits draw commands to an
 * {@link OrderedRenderCommandQueue} instead of a {@code VertexConsumerProvider}.
 */
public class InjuryHologramFeatureRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {

    public InjuryHologramFeatureRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light,
                        PlayerEntityRenderState state, float limbAngle, float limbDistance) {

        if (!ClientSurgeryState.isActive()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || state.id != client.player.getId()) return;

        TextRenderer textRenderer = client.textRenderer;
        if (textRenderer == null) return;

        matrices.push();
        // Player is lying down; raise the holograms above the body.
        matrices.translate(0, 1.6, 0);

        float headInjury = ClientSurgeryState.getInjury("head");
        float leftArmInjury = ClientSurgeryState.getInjury("left_arm");
        float rightArmInjury = ClientSurgeryState.getInjury("right_arm");
        float chestInjury = ClientSurgeryState.getInjury("chest");
        float legsInjury = ClientSurgeryState.getInjury("legs");

        if (headInjury > 0) {
            renderHologram(matrices, queue, light, textRenderer,
                    Text.literal(String.format("\u26A1 HEAD: %.0f%%", headInjury * 100)).formatted(Formatting.RED),
                    0, 0.6f, -0.7f);
        }
        if (leftArmInjury > 0) {
            renderHologram(matrices, queue, light, textRenderer,
                    Text.literal(String.format("\uD83E\uDDBE LEFT ARM: %.0f%%", leftArmInjury * 100)).formatted(Formatting.YELLOW),
                    -0.6f, 0.3f, 0);
        }
        if (rightArmInjury > 0) {
            renderHologram(matrices, queue, light, textRenderer,
                    Text.literal(String.format("\uD83E\uDDBE RIGHT ARM: %.0f%%", rightArmInjury * 100)).formatted(Formatting.YELLOW),
                    0.6f, 0.3f, 0);
        }
        if (chestInjury > 0) {
            renderHologram(matrices, queue, light, textRenderer,
                    Text.literal(String.format("\u2764 CHEST: %.0f%%", chestInjury * 100)).formatted(Formatting.GOLD),
                    0, 0.3f, -0.2f);
        }
        if (legsInjury > 0) {
            renderHologram(matrices, queue, light, textRenderer,
                    Text.literal(String.format("\uD83E\uDDB5 LEGS: %.0f%%", legsInjury * 100)).formatted(Formatting.GREEN),
                    0, 0.3f, 0.7f);
        }
        matrices.pop();
    }

    private void renderHologram(MatrixStack matrices, OrderedRenderCommandQueue queue, int light,
                                  TextRenderer textRenderer, Text text, float x, float y, float z) {
        matrices.push();
        matrices.translate(x, y, z);
        matrices.scale(0.025f, -0.025f, 0.025f);

        float width = textRenderer.getWidth(text);
        queue.submitText(
                matrices,
                -width / 2f, 0,
                text.asOrderedText(),
                true,
                TextRenderer.TextLayerType.NORMAL,
                light,
                0xFFFFFF,
                0,
                0
        );

        matrices.pop();
    }
}
