package me.xavi.vitalis.mixin;

import me.xavi.vitalis.client.state.ClientMedicalState;
import me.xavi.vitalis.client.state.ClientSurgeryState;
import me.xavi.vitalis.medical.BodyPart;
import me.xavi.vitalis.medical.InjuryStatus;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class ClientPlayerMovementMixin {

    private static final double BROKEN_LEG_MAX_JUMP_VELOCITY = 0.16D;

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

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void vitalis$reduceJumpWithBrokenLeg(CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer) (Object) this;

        int brokenLegs = brokenLegCount();

        if (brokenLegs <= 0) {
            return;
        }

        Vec3 movement = self.getDeltaMovement();

        if (brokenLegs >= 2) {
            if (movement.y > 0.0D) {
                self.setDeltaMovement(
                        movement.x,
                        0.0D,
                        movement.z
                );
            }

            return;
        }

        if (movement.y > 0.16D) {
            self.setDeltaMovement(
                    movement.x,
                    0.16D,
                    movement.z
            );
        }
    }

    private static int brokenLegCount() {
        int count = 0;

        InjuryStatus leftLeg = ClientMedicalState.getStatus(BodyPart.LEFT_LEG);
        InjuryStatus rightLeg = ClientMedicalState.getStatus(BodyPart.RIGHT_LEG);

        if (leftLeg == InjuryStatus.FRACTURE || leftLeg == InjuryStatus.OPEN_FRACTURE) {
            count++;
        }

        if (rightLeg == InjuryStatus.FRACTURE || rightLeg == InjuryStatus.OPEN_FRACTURE) {
            count++;
        }

        return count;
    }
}