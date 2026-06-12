package me.xavi.vitalis.mixin;

import me.xavi.vitalis.client.state.ClientSurgeryState;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class ClientInteractionMixin {

    @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void vitalis$cancelDestroyBlock(
            BlockPos pos,
            Direction direction,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (ClientSurgeryState.isActive()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "continueDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void vitalis$cancelContinueDestroyBlock(
            BlockPos pos,
            Direction direction,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (ClientSurgeryState.isActive()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void vitalis$cancelUseBlock(
            LocalPlayer player,
            InteractionHand hand,
            BlockHitResult hitResult,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (ClientSurgeryState.isActive()) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void vitalis$cancelUseItemOn(
            LocalPlayer localPlayer,
            InteractionHand interactionHand,
            BlockHitResult blockHitResult,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (ClientSurgeryState.isActive()) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void vitalis$cancelAttack(
            Player player,
            Entity entity,
            CallbackInfo ci
    ) {
        if (ClientSurgeryState.isActive()) {
            ci.cancel();
        }
    }
}