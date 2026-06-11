package me.xavi.vitalis.mixin;

import me.xavi.vitalis.client.input.OrbitCameraHandler;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {
    /**
     * Runs after vanilla camera update logic so the orbit camera's position
     * and rotation are not immediately overwritten.
     */
    @Inject(method = "update", at = @At("TAIL"))
    private void onUpdate(CallbackInfo ci) {
        OrbitCameraHandler.tick();
    }
}
