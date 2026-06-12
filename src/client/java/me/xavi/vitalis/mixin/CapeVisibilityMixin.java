package me.xavi.vitalis.mixin;

import me.xavi.vitalis.client.ClientSurgeryState;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class CapeVisibilityMixin {

    @Inject(method = "isCapeLoaded", at = @At("HEAD"), cancellable = true)
    private void vitalis$hideCapeLoaded(CallbackInfoReturnable<Boolean> cir) {
        AbstractClientPlayer player = (AbstractClientPlayer) (Object) this;

        if (ClientSurgeryState.isLying(player.getUUID())) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isModelPartShown", at = @At("HEAD"), cancellable = true)
    private void vitalis$hideCapeModelPart(
            PlayerModelPart part,
            CallbackInfoReturnable<Boolean> cir
    ) {
        AbstractClientPlayer player = (AbstractClientPlayer) (Object) this;

        if (part == PlayerModelPart.CAPE && ClientSurgeryState.isLying(player.getUUID())) {
            cir.setReturnValue(false);
        }
    }
}