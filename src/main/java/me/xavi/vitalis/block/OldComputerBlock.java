package me.xavi.vitalis.block;

import com.mojang.serialization.MapCodec;
import me.xavi.vitalis.menus.ComputerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class OldComputerBlock extends Block {
    public static final MapCodec<OldComputerBlock> CODEC = simpleCodec(OldComputerBlock::new);

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    private static final VoxelShape SHAPE_NORTH = Shapes.or(
            Block.box(0, 0, 0, 16, 4, 16),
            Block.box(1, 5, 1, 15, 16, 8),
            Block.box(2, 5, 8, 14, 13, 15),
            Block.box(3, 4, 3, 13, 5, 12)
    );

    private static final VoxelShape SHAPE_EAST = Shapes.or(
            Block.box(0, 0, 0, 16, 4, 16),
            Block.box(8, 5, 1, 15, 16, 15),
            Block.box(1, 5, 2, 8, 13, 14),
            Block.box(4, 4, 3, 13, 5, 13)
    );

    private static final VoxelShape SHAPE_SOUTH = Shapes.or(
            Block.box(0, 0, 0, 16, 4, 16),
            Block.box(1, 5, 8, 15, 16, 15),
            Block.box(2, 5, 1, 14, 13, 8),
            Block.box(3, 4, 4, 13, 5, 13)
    );

    private static final VoxelShape SHAPE_WEST = Shapes.or(
            Block.box(0, 0, 0, 16, 4, 16),
            Block.box(1, 5, 1, 8, 16, 15),
            Block.box(8, 5, 2, 15, 13, 14),
            Block.box(3, 4, 3, 12, 5, 13)
    );

    public OldComputerBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(POWERED, false);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        if (isPowerButtonHit(state, hit)) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(POWERED, !state.getValue(POWERED)), Block.UPDATE_ALL);
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (isScreenHit(state, hit)) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(new SimpleMenuProvider(
                        (syncId, inv, p) -> new ComputerMenu(syncId, inv),
                        Component.translatable("screen.vitalis.computer")
                ));
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    private static boolean isPowerButtonHit(BlockState state, BlockHitResult hit) {
        Direction facing = state.getValue(FACING);

        double localX = hit.getLocation().x - hit.getBlockPos().getX();
        double localY = hit.getLocation().y - hit.getBlockPos().getY();
        double localZ = hit.getLocation().z - hit.getBlockPos().getZ();

        double x;
        double z;

        switch (facing) {
            case NORTH -> {
                x = localX;
                z = localZ;
            }
            case SOUTH -> {
                x = 1.0D - localX;
                z = 1.0D - localZ;
            }
            case EAST -> {
                x = localZ;
                z = 1.0D - localX;
            }
            case WEST -> {
                x = 1.0D - localZ;
                z = localX;
            }
            default -> {
                x = localX;
                z = localZ;
            }
        }

        return x >= 0.0D / 16.0D
                && x <= 4.0D / 16.0D
                && localY >= 1.0D / 16.0D
                && localY <= 4.0D / 16.0D
                && z >= -1.0D / 16.0D
                && z <= 0.0D / 16.0D;
    }

    public static boolean isPowerButtonHitClient(BlockState state, BlockHitResult hit) {
        return isPowerButtonHit(state, hit);
    }

    public static boolean isScreenHit(BlockState state, BlockHitResult hit) {
        if (!state.getValue(POWERED)) {
            return false;
        }

        Direction facing = state.getValue(FACING);

        double localX = hit.getLocation().x - hit.getBlockPos().getX();
        double localY = hit.getLocation().y - hit.getBlockPos().getY();
        double localZ = hit.getLocation().z - hit.getBlockPos().getZ();

        double x;
        double z;

        switch (facing) {
            case NORTH -> {
                x = localX * 16.0D;
                z = localZ * 16.0D;
            }
            case SOUTH -> {
                x = (1.0D - localX) * 16.0D;
                z = (1.0D - localZ) * 16.0D;
            }
            case EAST -> {
                x = localZ * 16.0D;
                z = (1.0D - localX) * 16.0D;
            }
            case WEST -> {
                x = (1.0D - localZ) * 16.0D;
                z = localX * 16.0D;
            }
            default -> {
                x = localX * 16.0D;
                z = localZ * 16.0D;
            }
        }

        return x >= 2.0D
                && x <= 14.0D
                && localY * 16.0D >= 6.0D
                && localY * 16.0D <= 15.0D
                && z >= 1.0D
                && z <= 2.0D;
    }

    private static boolean yInRange(double y) {
        return y >= 5.0D / 16.0D && y <= 7.5D / 16.0D;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return switch (state.getValue(FACING)) {
            case EAST -> SHAPE_EAST;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return getShape(state, level, pos, context);
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }
}