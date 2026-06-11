package me.xavi.vitalis.mixin;

import me.xavi.vitalis.client.ClientSurgeryState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Cancels block breaking and block-use interactions while the local player
 * is lying on a surgery table. Only sneaking (handled separately, server
 * side, to get up) should have any effect while lying down.
 */
@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientInteractionMixin {

    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    private void vitalis$cancelAttackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (ClientSurgeryState.isActive()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    private void vitalis$cancelInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (ClientSurgeryState.isActive()) {
            cir.setReturnValue(ActionResult.FAIL);
        }
    }
}
