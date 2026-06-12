package me.xavi.vitalis.client.input;

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

    private enum Mode {
        NONE,
        SURGERY_TABLE,
        DOWNED
    }

    private static Mode mode = Mode.NONE;

    private static float yaw = 0.0F;

    private static final float TABLE_ORBIT_RADIUS = 3.4F;
    private static final float DOWNED_ORBIT_RADIUS = 3.0F;

    private static final float ORBIT_SPEED = 0.55F;

    private static final float TABLE_CAMERA_PITCH = 18.0F;
    private static final float DOWNED_CAMERA_PITCH = 50.0F;

    private static final double TABLE_PIVOT_HEIGHT = 1.25D;
    private static final double TABLE_CAMERA_HEIGHT = 0.65D;

    private static final double DOWNED_PIVOT_HEIGHT = 0.55D;
    private static final double DOWNED_CAMERA_HEIGHT = 2.2D;

    private static final float MIN_DISTANCE = 0.45F;
    private static final float COLLISION_MARGIN = 0.2F;

    private static Vec3 pivot = Vec3.ZERO;

    public static boolean isActive() {
        return mode != Mode.NONE;
    }

    public static boolean isDownedMode() {
        return mode == Mode.DOWNED;
    }


    public static void activateTable(BlockPos pos, Direction facing, Player player) {
        mode = Mode.SURGERY_TABLE;
        yaw = player.getYRot();

        pivot = Vec3.atCenterOf(pos).add(
                facing.getStepX() * 0.65D,
                TABLE_PIVOT_HEIGHT,
                facing.getStepZ() * 0.65D
        );
    }

    public static void activateDowned(Player player) {
        mode = Mode.DOWNED;
        yaw = player.getYRot();

        pivot = new Vec3(
                player.getX() * 0.5D,
                player.getY() + DOWNED_PIVOT_HEIGHT,
                player.getZ() * 0.5D
        );
    }

    public static void deactivate() {
        mode = Mode.NONE;
    }

    public static void tick() {
        if (mode == Mode.NONE) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;

        if (player == null || client.level == null) {
            return;
        }

        if (mode == Mode.DOWNED) {
            pivot = new Vec3(
                    player.getX(),
                    player.getY() + DOWNED_PIVOT_HEIGHT,
                    player.getZ()
            );
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

        float radius = mode == Mode.DOWNED ? DOWNED_ORBIT_RADIUS : TABLE_ORBIT_RADIUS;
        double cameraHeight = mode == Mode.DOWNED ? DOWNED_CAMERA_HEIGHT : TABLE_CAMERA_HEIGHT;
        float pitch = mode == Mode.DOWNED ? DOWNED_CAMERA_PITCH : TABLE_CAMERA_PITCH;

        float distance = resolveDistance(client, player, pivot, direction, radius);

        Vec3 cameraPos = pivot
                .add(direction.scale(distance))
                .add(0.0D, cameraHeight, 0.0D);

        Camera camera = client.gameRenderer.getMainCamera();
        CameraAccessor accessor = (CameraAccessor) camera;

        accessor.invokeSetRotation(yaw + 180.0F, pitch);
        accessor.invokeSetPos(cameraPos.x, cameraPos.y, cameraPos.z);
    }

    private static float resolveDistance(
            Minecraft client,
            LocalPlayer player,
            Vec3 pivot,
            Vec3 direction,
            float radius
    ) {
        Vec3 desiredPos = pivot.add(direction.scale(radius));

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
            return Mth.clamp((float) hitDistance - COLLISION_MARGIN, MIN_DISTANCE, radius);
        }

        return radius;
    }

    public static void enforcePerspective() {
        if (!isActive()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();

        if (client.options.getCameraType() != CameraType.THIRD_PERSON_BACK) {
            client.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }
    }
}