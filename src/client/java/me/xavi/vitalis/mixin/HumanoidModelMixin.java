package me.xavi.vitalis.mixin;

import me.xavi.vitalis.client.ClientSurgeryState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends LivingEntity> {

    @Shadow
    @Final
    public ModelPart head;

    @Shadow
    @Final
    public ModelPart hat;

    private static final float HEAD_REST_ANGLE = (float) Math.toRadians(-30.0D);

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void vitalis$raiseHeadOnSurgeryTable(
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci
    ) {
        if (!ClientSurgeryState.isLying(entity.getUUID())) {
            return;
        }

        this.head.xRot += HEAD_REST_ANGLE;
        this.hat.xRot += HEAD_REST_ANGLE;
    }
}