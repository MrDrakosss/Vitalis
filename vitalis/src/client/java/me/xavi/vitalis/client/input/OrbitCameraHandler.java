package me.xavi.vitalis.client.input;

import me.xavi.vitalis.mixin.CameraAccessor;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.lwjgl.glfw.GLFW;

/**
 * Drives a fixed-radius horizontal orbit camera centered on a fixed pivot
 * point above the table while the player is lying down. Only mouse-X
 * (yaw) input rotates the camera around the pivot; pitch is constant
 * (looking down at the patient from above). The pivot is a fixed
 * world-space point computed once at activation time, so the camera never
 * drifts on its own. Activated/deactivated from the network handler in
 * {@code VitalisClient}.
 */
public class OrbitCameraHandler {
    private static boolean active = false;
    private static float yaw = 0;

    /** Constant downward look angle (degrees). Higher = looking more straight down. */
    private static final float PITCH = 25.0f;

    /** Orbit radius around the pivot, in blocks. */
    private static final float ORBIT_RADIUS = 3.0f;

    /** Pivot height above the table-top/lock position, in blocks. */
    private static final double PIVOT_HEIGHT_ABOVE_LOCK = 1.0;

    private static final float MIN_DISTANCE = 0.3f;
    private static final float COLLISION_MARGIN = 0.2f;

    private static boolean firstTick = true;

    /** Fixed world-space pivot point, computed once at activation. */
    private static Vec3d pivot = Vec3d.ZERO;

    public static boolean isActive() {
        return active;
    }

    /**
     * Activates the orbit camera. {@code pos} is the lock/head position
     * (block position) the player is anchored to; the pivot is placed
     * directly above its center.
     */
    public static void activate(BlockPos pos, PlayerEntity player) {
        active = true;
        yaw = player.getYaw();

        pivot = new Vec3d(pos.getX() + 0.5, pos.getY() + PIVOT_HEIGHT_ABOVE_LOCK + 1.0, pos.getZ() + 0.5);

        // Recenter the cursor immediately so the first tick() compares
        // against a known (centered) position rather than wherever the
        // cursor happened to be before activation.
        firstTick = true;
        recenterCursor();
    }

    public static void deactivate() {
        active = false;
    }

    public static void tick() {
        if (!active) return;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) return;

        if (client.mouse.isCursorLocked()) {
            double cx = client.getWindow().getWidth() / 2.0;
            double cy = client.getWindow().getHeight() / 2.0;

            double mouseX = client.mouse.getX();

            if (!firstTick) {
                // Compare against the known recenter target (the window
                // center), not the previous frame's mouse position. Reading
                // an absolute GLFW cursor position after glfwSetCursorPos
                // can lag by a frame, which would otherwise produce a
                // persistent non-zero deltaX every tick (and a constant
                // auto-rotation) even with no input.
                double deltaX = mouseX - cx;
                yaw += (float) (deltaX * 0.15);
            }

            recenterCursor();
            firstTick = false;
        }

        float radYaw = (float) Math.toRadians(yaw);
        float radPitch = (float) Math.toRadians(PITCH);
        float horizontal = MathHelper.cos(radPitch);
        float xDir = -MathHelper.sin(radYaw) * horizontal;
        float zDir = MathHelper.cos(radYaw) * horizontal;
        float yDir = MathHelper.sin(radPitch);

        Vec3d direction = new Vec3d(xDir, yDir, zDir);

        float distance = resolveDistance(client, pivot, direction);

        Vec3d cameraPos = pivot.add(direction.multiply(distance));

        Camera camera = client.gameRenderer.getCamera();
        CameraAccessor accessor = (CameraAccessor) camera;

        // Yaw/pitch so the camera looks back towards the pivot. The camera
        // sits above and to the side of the pivot (yDir > 0 since PITCH is
        // positive), so it must look DOWN at the pivot - positive pitch in
        // Minecraft's convention.
        float lookYaw = yaw + 180.0F;
        float lookPitch = PITCH;

        accessor.invokeSetRotation(lookYaw, lookPitch);
        accessor.invokeSetPos(cameraPos.x, cameraPos.y, cameraPos.z);
    }

    /**
     * Returns the largest distance up to {@link #ORBIT_RADIUS} that doesn't
     * place the camera inside a solid block, by raycasting from the pivot
     * towards the desired camera position.
     */
    private static float resolveDistance(MinecraftClient client, Vec3d pivot, Vec3d direction) {
        Vec3d desiredPos = pivot.add(direction.multiply(ORBIT_RADIUS));

        HitResult hit = client.world.raycast(new RaycastContext(
                pivot,
                desiredPos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                ShapeContext.absent()
        ));

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hit;
            double hitDistance = pivot.distanceTo(blockHit.getPos());
            float clamped = (float) hitDistance - COLLISION_MARGIN;
            return MathHelper.clamp(clamped, MIN_DISTANCE, ORBIT_RADIUS);
        }

        return ORBIT_RADIUS;
    }

    /** Centers the cursor each frame so it never leaves the window while orbiting. */
    public static void recenterCursor() {
        if (!active) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (!client.mouse.isCursorLocked()) return;

        long windowHandle = client.getWindow().getHandle();
        double cx = client.getWindow().getWidth() / 2.0;
        double cy = client.getWindow().getHeight() / 2.0;
        GLFW.glfwSetCursorPos(windowHandle, cx, cy);
    }

    /**
     * Forces the perspective back to third-person-back every tick while the
     * orbit camera is active, so pressing F5 (which toggles perspective)
     * has no lasting effect.
     */
    public static void enforcePerspective() {
        if (!active) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.getPerspective() != net.minecraft.client.option.Perspective.THIRD_PERSON_BACK) {
            client.options.setPerspective(net.minecraft.client.option.Perspective.THIRD_PERSON_BACK);
        }
    }
}
