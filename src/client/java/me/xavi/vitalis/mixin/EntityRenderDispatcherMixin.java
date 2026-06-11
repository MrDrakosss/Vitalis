package me.xavi.vitalis.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import me.xavi.vitalis.client.ClientSurgeryState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Inject(
            method = "renderShadow",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void vitalis$hidePlayerShadowOnSurgeryTable(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            Entity entity,
            float opacity,
            float partialTick,
            LevelReader level,
            float size,
            CallbackInfo ci
    ) {
        if (ClientSurgeryState.isActive()) {
            ci.cancel();
        }
    }
}
