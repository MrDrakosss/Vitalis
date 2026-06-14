package me.xavi.vitalis.block;

import com.mojang.serialization.MapCodec;
import me.xavi.vitalis.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class StandBlock extends Block {
    public static final MapCodec<StandBlock> CODEC = simpleCodec(StandBlock::new);

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = EnumProperty.create("half", DoubleBlockHalf.class);
    public static final IntegerProperty BAGS = IntegerProperty.create("bags", 0, 4);

    private static final VoxelShape LOWER_NORTH = Shapes.or(
            Block.box(0, 2.5, 7, 16, 3.5, 9),
            Block.box(7, 2.5, 0, 9, 3.5, 16),
            Block.box(13.5, 0, 7.5, 15.5, 2, 8.5),
            Block.box(0.5, 0, 7.5, 2.5, 2, 8.5),
            Block.box(7.5, 0, 13.5, 8.5, 2, 15.5),
            Block.box(7.5, 0, 0.5, 8.5, 2, 2.5),
            Block.box(7, 3, 7.5, 9, 16, 8.5),
            Block.box(6, 14, 6, 10, 16, 8)
    );

    private static final VoxelShape UPPER_NORTH_BASE = Shapes.or(
            Block.box(7, 0, 7.5, 9, 13, 8.5),
            Block.box(6, 0, 6, 10, 3, 8),
            Block.box(0, 8, 7.9, 16, 11, 8.1),
            Block.box(7.9, 8, 0, 8.1, 11, 16)
    );

    private static VoxelShape rotateY90(VoxelShape shape) {
        VoxelShape[] result = new VoxelShape[]{Shapes.empty()};

        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            result[0] = Shapes.or(
                    result[0],
                    Block.box(
                            16.0D - maxZ * 16.0D,
                            minY * 16.0D,
                            minX * 16.0D,
                            16.0D - minZ * 16.0D,
                            maxY * 16.0D,
                            maxX * 16.0D
                    )
            );
        });

        return result[0];
    }

    private static final VoxelShape LOWER_EAST = rotateY90(LOWER_NORTH);
    private static final VoxelShape LOWER_SOUTH = rotateY90(LOWER_EAST);
    private static final VoxelShape LOWER_WEST = rotateY90(LOWER_SOUTH);

    private static final VoxelShape UPPER_EAST_BASE = rotateY90(UPPER_NORTH_BASE);
    private static final VoxelShape UPPER_SOUTH_BASE = rotateY90(UPPER_EAST_BASE);
    private static final VoxelShape UPPER_WEST_BASE = rotateY90(UPPER_SOUTH_BASE);

    private static final VoxelShape BAG_1_NORTH = Block.box(1, 2.5, 7.75, 5, 8.5, 8.25);
    private static final VoxelShape BAG_2_NORTH = Block.box(11, 2.5, 7.75, 15, 8.5, 8.25);
    private static final VoxelShape BAG_3_NORTH = Block.box(7.75, 2.5, 11, 8.25, 8.5, 15);
    private static final VoxelShape BAG_4_NORTH = Block.box(7.75, 2.5, 1, 8.25, 8.5, 5);

    private static final VoxelShape BAG_1_EAST = rotateY90(BAG_1_NORTH);
    private static final VoxelShape BAG_2_EAST = rotateY90(BAG_2_NORTH);
    private static final VoxelShape BAG_3_EAST = rotateY90(BAG_3_NORTH);
    private static final VoxelShape BAG_4_EAST = rotateY90(BAG_4_NORTH);

    private static final VoxelShape BAG_1_SOUTH = rotateY90(BAG_1_EAST);
    private static final VoxelShape BAG_2_SOUTH = rotateY90(BAG_2_EAST);
    private static final VoxelShape BAG_3_SOUTH = rotateY90(BAG_3_EAST);
    private static final VoxelShape BAG_4_SOUTH = rotateY90(BAG_4_EAST);

    private static final VoxelShape BAG_1_WEST = rotateY90(BAG_1_SOUTH);
    private static final VoxelShape BAG_2_WEST = rotateY90(BAG_2_SOUTH);
    private static final VoxelShape BAG_3_WEST = rotateY90(BAG_3_SOUTH);
    private static final VoxelShape BAG_4_WEST = rotateY90(BAG_4_SOUTH);

    private static VoxelShape getBaseShape(Direction facing, DoubleBlockHalf half) {
        return switch (facing) {
            case EAST -> half == DoubleBlockHalf.LOWER ? LOWER_EAST : UPPER_EAST_BASE;
            case SOUTH -> half == DoubleBlockHalf.LOWER ? LOWER_SOUTH : UPPER_SOUTH_BASE;
            case WEST -> half == DoubleBlockHalf.LOWER ? LOWER_WEST : UPPER_WEST_BASE;
            default -> half == DoubleBlockHalf.LOWER ? LOWER_NORTH : UPPER_NORTH_BASE;
        };
    }

    private static VoxelShape withBags(VoxelShape shape, Direction facing, int bags) {
        if (bags <= 0) {
            return shape;
        }

        VoxelShape result = shape;

        if (bags >= 1) {
            result = Shapes.or(result, getBagShape(facing, 1));
        }

        if (bags >= 2) {
            result = Shapes.or(result, getBagShape(facing, 2));
        }

        if (bags >= 3) {
            result = Shapes.or(result, getBagShape(facing, 3));
        }

        if (bags >= 4) {
            result = Shapes.or(result, getBagShape(facing, 4));
        }

        return result;
    }

    private static VoxelShape getBagShape(Direction facing, int index) {
        return switch (facing) {
            case EAST -> switch (index) {
                case 1 -> BAG_1_EAST;
                case 2 -> BAG_2_EAST;
                case 3 -> BAG_3_EAST;
                default -> BAG_4_EAST;
            };
            case SOUTH -> switch (index) {
                case 1 -> BAG_1_SOUTH;
                case 2 -> BAG_2_SOUTH;
                case 3 -> BAG_3_SOUTH;
                default -> BAG_4_SOUTH;
            };
            case WEST -> switch (index) {
                case 1 -> BAG_1_WEST;
                case 2 -> BAG_2_WEST;
                case 3 -> BAG_3_WEST;
                default -> BAG_4_WEST;
            };
            default -> switch (index) {
                case 1 -> BAG_1_NORTH;
                case 2 -> BAG_2_NORTH;
                case 3 -> BAG_3_NORTH;
                default -> BAG_4_NORTH;
            };
        };
    }

    public StandBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(BAGS, 0));
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        VoxelShape shape = getBaseShape(
                state.getValue(FACING),
                state.getValue(HALF)
        );

        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            shape = withBags(shape, state.getValue(FACING), state.getValue(BAGS));
        }

        return shape;
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
    protected VoxelShape getInteractionShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos
    ) {
        return getShape(state, level, pos, CollisionContext.empty());
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
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        if (pos.getY() >= level.getMaxBuildHeight() - 1 || !level.getBlockState(pos.above()).canBeReplaced(context)) {
            return null;
        }

        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(BAGS, 0);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide) {
            level.setBlock(
                    pos.above(),
                    state.setValue(HALF, DoubleBlockHalf.UPPER),
                    Block.UPDATE_ALL
            );
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (!stack.is(ModItems.BLOOD_BAG)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        BlockState lowerState = level.getBlockState(lowerPos);

        if (!(lowerState.getBlock() instanceof StandBlock)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        int bags = lowerState.getValue(BAGS);

        if (bags >= 4) {
            return ItemInteractionResult.CONSUME;
        }

        if (!level.isClientSide) {
            BlockState newLowerState = lowerState.setValue(BAGS, bags + 1);
            level.setBlock(lowerPos, newLowerState, Block.UPDATE_ALL);

            BlockState upperState = level.getBlockState(lowerPos.above());
            if (upperState.is(this)) {
                level.setBlock(lowerPos.above(), upperState.setValue(BAGS, bags + 1), Block.UPDATE_ALL);
            }

            if (!player.isCreative()) {
                stack.shrink(1);
            }
        }

        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        BlockState lowerState = level.getBlockState(lowerPos);

        if (!(lowerState.getBlock() instanceof StandBlock)) {
            return InteractionResult.PASS;
        }

        int bags = lowerState.getValue(BAGS);

        if (bags <= 0) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            BlockState newLowerState = lowerState.setValue(BAGS, bags - 1);
            level.setBlock(lowerPos, newLowerState, Block.UPDATE_ALL);

            BlockState upperState = level.getBlockState(lowerPos.above());
            if (upperState.is(this)) {
                level.setBlock(lowerPos.above(), upperState.setValue(BAGS, bags - 1), Block.UPDATE_ALL);
            }

            player.getInventory().placeItemBackInInventory(new ItemStack(ModItems.BLOOD_BAG));
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        DoubleBlockHalf half = state.getValue(HALF);

        if (direction == Direction.UP && half == DoubleBlockHalf.LOWER) {
            if (!neighborState.is(this) || neighborState.getValue(HALF) != DoubleBlockHalf.UPPER) {
                return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
            }
        }

        if (direction == Direction.DOWN && half == DoubleBlockHalf.UPPER) {
            if (!neighborState.is(this) || neighborState.getValue(HALF) != DoubleBlockHalf.LOWER) {
                return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
            }
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void onRemove(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState newState,
            boolean movedByPiston
    ) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            DoubleBlockHalf half = state.getValue(HALF);
            BlockPos lowerPos = half == DoubleBlockHalf.LOWER ? pos : pos.below();
            BlockState lowerState = level.getBlockState(lowerPos);

            int bags = state.getValue(BAGS);

            if (half == DoubleBlockHalf.UPPER && lowerState.is(this)) {
                bags = lowerState.getValue(BAGS);
            }

            if (bags > 0) {
                Containers.dropItemStack(
                        level,
                        lowerPos.getX() + 0.5D,
                        lowerPos.getY() + 1.0D,
                        lowerPos.getZ() + 0.5D,
                        new ItemStack(ModItems.BLOOD_BAG, bags)
                );
            }

            BlockPos otherPos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
            BlockState otherState = level.getBlockState(otherPos);

            if (otherState.is(this)) {
                level.removeBlock(otherPos, false);
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
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
        builder.add(FACING, HALF, BAGS);
    }
}