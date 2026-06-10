package me.xavi.vitalis.block;

import me.xavi.vitalis.network.SurgeryStatePayload;
import me.xavi.vitalis.util.SurgeryData;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.*;
        import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SurgeryTableBlock extends Block implements BlockEntityProvider {
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    private static final VoxelShape BASE_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(0, 0, 0, 16, 8, 16),
            Block.createCuboidShape(0, 8, 4, 16, 16, 12)
    );

    public SurgeryTableBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return BASE_SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SurgeryTableBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient()) {
            if (!SurgeryData.isOnTable(player)) {
                BlockPos headPos = getHeadPos(pos, state.get(FACING));
                float yaw = switch (state.get(FACING)) {
                    case NORTH -> 180.0F;
                    case SOUTH -> 0.0F;
                    case WEST -> 90.0F;
                    case EAST -> -90.0F;
                    default -> 0.0F;
                };
                player.setPosition(headPos.getX() + 0.5, headPos.getY() + 0.2, headPos.getZ() + 0.5);
                player.setYaw(yaw);
                player.setPitch(0);
                player.setSneaking(false);

                SurgeryData.setOnTable(player, true);
                SurgeryData.setTablePos(player, pos);

                // Példa sérülések
                SurgeryData.setInjury(player, "head", 0.75f);
                SurgeryData.setInjury(player, "left_arm", 0.30f);
                SurgeryData.setInjury(player, "right_arm", 0.0f);
                SurgeryData.setInjury(player, "chest", 0.15f);
                SurgeryData.setInjury(player, "legs", 0.50f);

                ServerPlayNetworking.send((ServerPlayerEntity) player, new SurgeryStatePayload(pos, true));
            } else {
                SurgeryData.clearAllInjuries(player);
                player.sendMessage(net.minecraft.text.Text.literal("§aAll injuries healed!"), true);
                ServerPlayNetworking.send((ServerPlayerEntity) player, new SurgeryStatePayload(pos, false));
            }
        }
        return ActionResult.SUCCESS;
    }

    private BlockPos getHeadPos(BlockPos tablePos, Direction facing) {
        return tablePos.offset(facing);
    }
}