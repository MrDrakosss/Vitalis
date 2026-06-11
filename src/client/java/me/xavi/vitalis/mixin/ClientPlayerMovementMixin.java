package me.xavi.vitalis.mixin;

import me.xavi.vitalis.client.ClientSurgeryState;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class ClientPlayerMovementMixin {

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void vitalis$cancelMovementWhileLying(CallbackInfo ci) {
        if (!ClientSurgeryState.isActive()) {
            return;
        }

        LocalPlayer self = (LocalPlayer) (Object) this;

        Input input = self.input;

        if (input != null) {
            input.leftImpulse = 0.0F;
            input.forwardImpulse = 0.0F;

            input.up = false;
            input.down = false;
            input.left = false;
            input.right = false;

            input.jumping = false;
            input.shiftKeyDown = false;
        }

        self.setSprinting(false);
        self.setJumping(false);

        self.xxa = 0.0F;
        self.yya = 0.0F;
        self.zza = 0.0F;

        self.setDeltaMovement(Vec3.ZERO);
    }
}