package me.xavi.vitalis.mixin;

import me.xavi.vitalis.client.ClientSurgeryState;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class CapeVisibilityMixin {

    @Inject(method = "isCapeLoaded", at = @At("HEAD"), cancellable = true)
    private void vitalis$hideCape(CallbackInfoReturnable<Boolean> cir) {
        if (ClientSurgeryState.isActive()) {
            cir.setReturnValue(false);
        }
    }
}