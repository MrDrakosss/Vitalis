package me.xavi.vitalis.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.xavi.vitalis.client.state.ClientDownedState;
import me.xavi.vitalis.client.state.ClientSurgeryState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {

    @Inject(
            method = "setupRotations(Lnet/minecraft/world/entity/LivingEntity;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V",
            at = @At("TAIL")
    )
    private void vitalis$lyingDownTransform(
            T entity,
            PoseStack poseStack,
            float ageInTicks,
            float rotationYaw,
            float partialTick,
            float scale,
            CallbackInfo ci
    ) {
        Minecraft client = Minecraft.getInstance();

        boolean isLocalPlayerDowned =
                client.player != null
                        && entity.getId() == client.player.getId()
                        && ClientDownedState.isActive();

        if (isLocalPlayerDowned) {
            poseStack.mulPose(Axis.YP.rotationDegrees(-(180.0F - rotationYaw)));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            return;
        }

        if (!ClientSurgeryState.isLying(entity.getUUID())) {
            return;
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(-(180.0F - rotationYaw)));

        float lockYaw = ClientSurgeryState.getLockYaw(entity.getUUID());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - lockYaw));

        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
    }
}