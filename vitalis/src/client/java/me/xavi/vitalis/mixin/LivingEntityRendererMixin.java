package me.xavi.vitalis.mixin;

import me.xavi.vitalis.client.ClientSurgeryState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Rotates the local player's model to lie flat on top of the surgery table
 * while {@link ClientSurgeryState} is active. We avoid relying on vanilla's
 * bed-sleeping rotation (which depends on a real bed block at the entity's
 * sleeping position) and instead apply our own transform.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Inject(method = "setupTransforms", at = @At("TAIL"))
    private void vitalis$lyingDownTransform(LivingEntityRenderState state, MatrixStack matrices,
                                             float bodyYaw, float baseHeight, CallbackInfo ci) {
        if (!(state instanceof PlayerEntityRenderState playerState)) return;
        if (!ClientSurgeryState.isActive()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || playerState.id != client.player.getId()) return;

        // Lay the model down by rotating it 90 degrees around the
        // entity's feet (the origin of this matrix stack at this point).
        // The body then extends horizontally from the entity's actual
        // position instead of floating away from it. The entity's actual
        // yaw is kept server-authoritative and locked every tick (see
        // Vitalis.java's tick lock and the fixed requestTeleport call), so
        // we don't need to override bodyYaw here.
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
    }

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void vitalis$hideCapeWhileLying(net.minecraft.entity.LivingEntity entity, LivingEntityRenderState state,
                                             float tickDelta, CallbackInfo ci) {
        if (!(state instanceof PlayerEntityRenderState playerState)) return;
        if (!ClientSurgeryState.isActive()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || playerState.id != client.player.getId()) return;

        playerState.capeVisible = false;
    }
}
