package me.xavi.vitalis.block;

import me.xavi.vitalis.Vitalis;
import me.xavi.vitalis.network.SurgeryStatePayload;
import me.xavi.vitalis.util.SurgeryData;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * The surgery table. Like a vanilla bed, this is a true 1x2x1 structure made
 * of two block positions: a {@link BedPart#HEAD} and a {@link BedPart#FOOT},
 * each with its own (single-block) outline/collision shape. This avoids the
 * "phantom hitbox" issues that come from a single block claiming a shape
 * that extends into a neighboring (air) block position.
 */
public class SurgeryTableBlock extends Block implements BlockEntityProvider {
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final EnumProperty<BedPart> PART = EnumProperty.of("part", BedPart.class);

    public SurgeryTableBlock(AbstractBlock.Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(PART, BedPart.FOOT));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos pos = ctx.getBlockPos();
        Direction facing = ctx.getHorizontalPlayerFacing().getOpposite();
        BlockPos headPos = pos.offset(facing.getOpposite());

        if (!ctx.getWorld().getBlockState(headPos).canReplace(ctx)) {
            return null;
        }

        return getDefaultState().with(FACING, facing).with(PART, BedPart.FOOT);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable net.minecraft.entity.LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (world.isClient()) return;

        Direction facing = state.get(FACING);
        BlockPos headPos = pos.offset(facing.getOpposite());
        world.setBlockState(headPos, state.with(PART, BedPart.HEAD), Block.NOTIFY_ALL);
    }

    @Override
    public void onBroken(net.minecraft.world.WorldAccess world, BlockPos pos, BlockState state) {
        super.onBroken(world, pos, state);
        if (world.isClient()) return;

        BedPart part = state.get(PART);
        Direction facing = state.get(FACING);
        BlockPos otherPos = (part == BedPart.HEAD)
                ? pos.offset(facing)
                : pos.offset(facing.getOpposite());

        BlockState otherState = world.getBlockState(otherPos);
        if (otherState.isOf(this) && otherState.get(PART) != part) {
            world.removeBlock(otherPos, false);
        }
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return Block.createCuboidShape(0, 0, 0, 16, 16, 16);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return Block.createCuboidShape(0, 0, 0, 16, 16, 16);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SurgeryTableBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        // Always use the HEAD position as the canonical "table position",
        // regardless of which half the player clicked.
        BlockPos headPos = (state.get(PART) == BedPart.HEAD) ? pos : pos.offset(state.get(FACING).getOpposite());
        BlockState headState = world.getBlockState(headPos);
        if (!headState.isOf(this)) {
            // Structure is broken/incomplete; fall back to the clicked state.
            headState = state;
            headPos = pos;
        }

        if (!SurgeryData.isOnTable(player)) {
            lieDown(headState, headPos, player);
        } else {
            heal(player);
        }
        return ActionResult.SUCCESS;
    }

    private void lieDown(BlockState headState, BlockPos headPos, PlayerEntity player) {
        Direction facing = headState.get(FACING);

        // The HEAD block is where the player's head goes; the body extends
        // towards the FOOT block (i.e. opposite of `facing`, matching the
        // offset used in onPlaced/onBroken).
        double x = headPos.getX() + 0.5;
        double y = headPos.getY() + Vitalis.TABLE_TOP_HEIGHT;
        double z = headPos.getZ() + 0.5;

        // Yaw so the player's feet point towards the FOOT block (i.e.
        // towards `facing`, since FOOT = headPos + facing). Minecraft yaw:
        // 0=south(+Z), 90=west(-X), 180=north(-Z), 270/-90=east(+X).
        float yaw = switch (facing) {
            case SOUTH -> 0.0F;
            case NORTH -> 180.0F;
            case WEST -> 90.0F;
            case EAST -> -90.0F;
            default -> 0.0F;
        };

        player.setSneaking(false);
        player.setPose(EntityPose.SLEEPING);

        SurgeryData.setOnTable(player, true);
        SurgeryData.setTablePos(player, headPos);
        SurgeryData.setLockPos(player, headPos);
        SurgeryData.setLockYaw(player, yaw);

        if (player instanceof ServerPlayerEntity serverPlayer) {
            // requestTeleport authoritatively syncs position+rotation to the
            // client and discards stale movement packets, so the player
            // doesn't snap back to where they were standing.
            serverPlayer.networkHandler.requestTeleport(x, y, z, yaw, 0.0F);
        } else {
            player.setPosition(x, y, z);
            player.setYaw(yaw);
            player.setPitch(0.0F);
        }
        player.setBodyYaw(yaw);
        player.setHeadYaw(yaw);

        // Example injuries so the holograms have something to display.
        SurgeryData.setInjury(player, "head", 0.75f);
        SurgeryData.setInjury(player, "left_arm", 0.30f);
        SurgeryData.setInjury(player, "right_arm", 0.0f);
        SurgeryData.setInjury(player, "chest", 0.15f);
        SurgeryData.setInjury(player, "legs", 0.50f);

        sendState(player, headPos, true);
    }

    /** Called both when healing on a second use, and when the player gets up via shift. */
    public static void getUp(PlayerEntity player) {
        SurgeryData.clearAllInjuries(player);
        SurgeryData.setOnTable(player, false);
        SurgeryData.setLockPos(player, null);
        SurgeryData.setLockYaw(player, 0.0f);

        player.setPose(EntityPose.STANDING);

        BlockPos tablePos = SurgeryData.getTablePos(player);
        sendState(player, tablePos != null ? tablePos : player.getBlockPos(), false);
    }

    private void heal(PlayerEntity player) {
        getUp(player);
        player.sendMessage(Text.literal("§aAll injuries healed!"), true);
    }

    private static void sendState(PlayerEntity player, BlockPos tablePos, boolean active) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            ServerPlayNetworking.send(serverPlayer,
                    new SurgeryStatePayload(tablePos, active, SurgeryData.getAllInjuries(player)));
        }
    }
}
