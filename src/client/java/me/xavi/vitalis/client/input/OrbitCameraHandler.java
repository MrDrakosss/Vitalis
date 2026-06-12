package me.xavi.vitalis.client.input;

import me.xavi.vitalis.block.SurgeryTableBlock;
import me.xavi.vitalis.mixin.CameraAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class OrbitCameraHandler {

    private static boolean active = false;

    private static float yaw = 0.0F;

    private static final float ORBIT_RADIUS = 3.4F;
    private static final float ORBIT_SPEED = 0.55F;
    private static final float CAMERA_PITCH = 18.0F;

    private static final double PIVOT_HEIGHT = 1.25D;
    private static final double CAMERA_HEIGHT = 0.65D;

    private static final float MIN_DISTANCE = 0.45F;
    private static final float COLLISION_MARGIN = 0.2F;

    private static Vec3 pivot = Vec3.ZERO;

    public static boolean isActive() {
        return active;
    }

    public static void activate(BlockPos pos, Direction facing, Player player) {
        active = true;
        yaw = player.getYRot();

        pivot = Vec3.atCenterOf(pos).add(
                facing.getStepX() * 0.4D,
                PIVOT_HEIGHT,
                facing.getStepZ() * 0.4D
        );
    }

    public static void deactivate() {
        active = false;
    }

    public static void tick() {
        if (!active) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;

        if (player == null || client.level == null) {
            return;
        }

        yaw += ORBIT_SPEED;

        if (yaw >= 360.0F) {
            yaw -= 360.0F;
        }

        float radYaw = (float) Math.toRadians(yaw);

        Vec3 direction = new Vec3(
                -Mth.sin(radYaw),
                0.0D,
                Mth.cos(radYaw)
        ).normalize();

        float distance = resolveDistance(client, player, pivot, direction);

        Vec3 cameraPos = pivot
                .add(direction.scale(distance))
                .add(0.0D, CAMERA_HEIGHT, 0.0D);

        Camera camera = client.gameRenderer.getMainCamera();
        CameraAccessor accessor = (CameraAccessor) camera;

        accessor.invokeSetRotation(yaw + 180.0F, CAMERA_PITCH);
        accessor.invokeSetPos(cameraPos.x, cameraPos.y, cameraPos.z);
    }

    private static float resolveDistance(
            Minecraft client,
            LocalPlayer player,
            Vec3 pivot,
            Vec3 direction
    ) {
        Vec3 desiredPos = pivot.add(direction.scale(ORBIT_RADIUS));

        HitResult hit = client.level.clip(
                new ClipContext(
                        pivot,
                        desiredPos,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        player
                )
        );

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hit;
            double hitDistance = pivot.distanceTo(blockHit.getLocation());
            return Mth.clamp((float) hitDistance - COLLISION_MARGIN, MIN_DISTANCE, ORBIT_RADIUS);
        }

        return ORBIT_RADIUS;
    }

    public static void enforcePerspective() {
        if (!active) {
            return;
        }

        Minecraft client = Minecraft.getInstance();

        if (client.options.getCameraType() != CameraType.THIRD_PERSON_BACK) {
            client.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }
    }
}