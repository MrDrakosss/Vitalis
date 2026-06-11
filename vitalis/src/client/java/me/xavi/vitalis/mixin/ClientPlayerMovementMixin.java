package me.xavi.vitalis.mixin;

import me.xavi.vitalis.client.ClientSurgeryState;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Neutralizes the local player's movement input while they're lying on a
 * surgery table. Zeroing only the derived sidewaysSpeed/forwardSpeed fields
 * isn't enough, since tickMovement() recomputes those from the raw Input
 * (WASD) every tick - so the raw Input itself is zeroed here, at the HEAD
 * of tickMovement, before anything reads it. The "sneak" flag is preserved
 * so shift-to-get-up keeps working.
 */
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerMovementMixin {

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void vitalis$cancelMovementWhileLying(CallbackInfo ci) {
        if (!ClientSurgeryState.isActive()) return;

        ClientPlayerEntity self = (ClientPlayerEntity) (Object) this;

        Input input = self.input;
        if (input != null) {

            PlayerInput pi = input.playerInput;
            if (pi != null) {
                input.playerInput = new PlayerInput(false, false, false, false, false, pi.sneak(), false);
            }
        }

        self.sidewaysSpeed = 0.0F;
        self.forwardSpeed = 0.0F;
        self.upwardSpeed = 0.0F;
        self.setJumping(false);
        self.setSprinting(false);
    }
}
