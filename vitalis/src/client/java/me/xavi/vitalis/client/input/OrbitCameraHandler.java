package me.xavi.vitalis.client.input;

import me.xavi.vitalis.mixin.CameraAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class OrbitCameraHandler {
    private static boolean active = false;
    private static BlockPos targetPos;
    private static float yaw = 0;
    private static float pitch = 30;
    private static float distance = 5.0f;

    public static void activate(BlockPos pos, net.minecraft.entity.player.PlayerEntity player) {
        active = true;
        targetPos = pos;
        yaw = player.getYaw();
        pitch = player.getPitch();
    }

    public static void deactivate() {
        active = false;
    }

    public static void tick() {
        if (!active) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        double deltaX = client.mouse.getX() - client.getWindow().getWidth() / 2.0;
        double deltaY = client.mouse.getY() - client.getWindow().getHeight() / 2.0;
        if (client.mouse.isCursorLocked()) {
            yaw += deltaX * 0.15f;
            pitch += deltaY * 0.15f;
            pitch = MathHelper.clamp(pitch, -80, 80);

            // A kurzor pozíciójának középre állítása GLFW segítségével
            long windowHandle = client.getWindow().getHandle();
            int winWidth = client.getWindow().getWidth();
            int winHeight = client.getWindow().getHeight();
            GLFW.glfwSetCursorPos(windowHandle, winWidth / 2.0, winHeight / 2.0);
        }

        float radYaw = (float) Math.toRadians(yaw);
        float radPitch = (float) Math.toRadians(pitch);
        float xOffset = MathHelper.sin(radYaw) * MathHelper.cos(radPitch) * distance;
        float zOffset = MathHelper.cos(radYaw) * MathHelper.cos(radPitch) * distance;
        float yOffset = MathHelper.sin(radPitch) * distance;
        Vec3d cameraPos = new Vec3d(
                targetPos.getX() + 0.5 + xOffset,
                targetPos.getY() + 0.8 + yOffset,
                targetPos.getZ() + 0.5 + zOffset
        );

        Camera camera = client.gameRenderer.getCamera();
        CameraAccessor accessor = (CameraAccessor) camera;
        accessor.invokeSetRotation(yaw, pitch);
        accessor.invokeSetPos(cameraPos.x, cameraPos.y, cameraPos.z);
    }
}