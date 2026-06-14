package me.xavi.vitalis.block;

import com.mojang.serialization.MapCodec;
import me.xavi.vitalis.block.entity.MedicalCabinetBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class MedicalCabinetBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = EnumProperty.create("half", DoubleBlockHalf.class);

    public static final MapCodec<MedicalCabinetBlock> CODEC = simpleCodec(
            properties -> new MedicalCabinetBlock(properties, CabinetType.SUPPLY, false)
    );

    private final CabinetType cabinetType;
    private final boolean large;

    public MedicalCabinetBlock(Properties properties, CabinetType cabinetType, boolean large) {
        super(properties);
        this.cabinetType = cabinetType;
        this.large = large;

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    public CabinetType getCabinetType() {
        return cabinetType;
    }

    public boolean isLarge() {
        return large;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
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

        if (large && (pos.getY() >= level.getMaxBuildHeight() - 1 || !level.getBlockState(pos.above()).canBeReplaced(context))) {
            return null;
        }

        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(HALF, DoubleBlockHalf.LOWER);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (large && !level.isClientSide) {
            level.setBlock(
                    pos.above(),
                    state.setValue(HALF, DoubleBlockHalf.UPPER),
                    Block.UPDATE_ALL
            );
        }
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
        if (!large) {
            return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        }

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
            if (large) {
                DoubleBlockHalf half = state.getValue(HALF);
                BlockPos lowerPos = half == DoubleBlockHalf.LOWER ? pos : pos.below();
                BlockPos otherPos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();

                BlockEntity lowerBlockEntity = level.getBlockEntity(lowerPos);
                if (lowerBlockEntity instanceof MedicalCabinetBlockEntity cabinetBlockEntity) {
                    Containers.dropContents(level, lowerPos, cabinetBlockEntity);
                    level.updateNeighbourForOutputSignal(lowerPos, this);
                }

                BlockState otherState = level.getBlockState(otherPos);
                if (otherState.is(this)) {
                    level.removeBlock(otherPos, false);
                }
            } else {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof MedicalCabinetBlockEntity cabinetBlockEntity) {
                    Containers.dropContents(level, pos, cabinetBlockEntity);
                    level.updateNeighbourForOutputSignal(pos, this);
                }
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (large && state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return null;
        }

        return new MedicalCabinetBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        if (!level.isClientSide) {
            BlockPos inventoryPos = pos;

            if (large && state.getValue(HALF) == DoubleBlockHalf.UPPER) {
                inventoryPos = pos.below();
            }

            BlockEntity blockEntity = level.getBlockEntity(inventoryPos);

            if (blockEntity instanceof MenuProvider menuProvider) {
                player.openMenu(menuProvider);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }

    public enum CabinetType {
        REFRIGERATOR,
        SUPPLY,
        EQUIPMENT
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
}
