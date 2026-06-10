package me.xavi.vitalis.block;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;

public class SurgeryTableBlock extends Block implements BlockEntityProvider {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
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
        if (!world.isClient) {
            var component = player.getComponent(ModComponents.SURGERY_PLAYER);
            if (!component.isOnTable()) {
                BlockPos headPos = getHeadPos(pos, state.get(FACING));
                player.setPosition(headPos.getX() + 0.5, headPos.getY() + 0.2, headPos.getZ() + 0.5);
                player.setYaw(state.get(FACING).asRotation());
                player.setPitch(0);
                player.setSneaking(false);

                component.setOnTable(true);
                component.setTablePos(pos);

                // Példa sérülések
                component.setInjury("head", 0.75f);
                component.setInjury("left_arm", 0.30f);
                component.setInjury("right_arm", 0.0f);
                component.setInjury("chest", 0.15f);
                component.setInjury("legs", 0.50f);

                ServerPlayNetworking.send(player, new SurgeryStatePayload(pos, true));
            } else {
                component.clearAllInjuries();
                player.sendMessage(net.minecraft.text.Text.literal("§aAll injuries healed!"), true);
                ServerPlayNetworking.send(player, new SurgeryStatePayload(pos, false));
            }
        }
        return ActionResult.SUCCESS;
    }

    private BlockPos getHeadPos(BlockPos tablePos, Direction facing) {
        return tablePos.offset(facing);
    }
}