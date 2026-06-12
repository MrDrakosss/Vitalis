package me.xavi.vitalis.block;

import me.xavi.vitalis.medical.BodyPart;
import me.xavi.vitalis.medical.InjuryStatus;
import me.xavi.vitalis.network.SurgeryStatePayload;
import me.xavi.vitalis.util.SurgeryData;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SurgeryTableBlock extends Block implements EntityBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<BedPart> PART = EnumProperty.create("part", BedPart.class);

    public static final double TABLE_SURFACE_HEIGHT = 0.9D;

    private static final VoxelShape SHAPE =
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    private static final VoxelShape FOOT_SHAPE_NORTH = Shapes.or(
            Block.box(3, 0, 0, 13, 2, 16),
            Block.box(5, 2, 4, 11, 6, 16),
            Block.box(4, 6, 3, 12, 10, 16),
            Block.box(0, 10, 3, 16, 13, 16)
    );

    private static final VoxelShape HEAD_SHAPE_NORTH = Shapes.or(
            Block.box(3, 0, 0, 13, 2, 16),
            Block.box(5, 2, 0, 11, 6, 15),
            Block.box(4, 6, 0, 12, 10, 16),
            Block.box(0, 10, 0, 16, 13, 16),
            Block.box(3, 7, 0, 13, 11, 8),
            Block.box(6, 4, 0, 10, 7, 8)
    );

    private static final VoxelShape FOOT_SHAPE_SOUTH = rotateShape180(FOOT_SHAPE_NORTH);
    private static final VoxelShape FOOT_SHAPE_EAST = rotateShape90(FOOT_SHAPE_NORTH);
    private static final VoxelShape FOOT_SHAPE_WEST = rotateShape270(FOOT_SHAPE_NORTH);

    private static final VoxelShape HEAD_SHAPE_SOUTH = rotateShape180(HEAD_SHAPE_NORTH);
    private static final VoxelShape HEAD_SHAPE_EAST = rotateShape90(HEAD_SHAPE_NORTH);
    private static final VoxelShape HEAD_SHAPE_WEST = rotateShape270(HEAD_SHAPE_NORTH);

    public SurgeryTableBlock(BlockBehaviour.Properties properties) {
        super(properties);

        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(PART, BedPart.FOOT)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos headPos = pos.relative(facing.getOpposite());

        if (!level.getBlockState(headPos).canBeReplaced(context)) {
            return null;
        }

        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(PART, BedPart.FOOT);
    }

    @Override
    public void setPlacedBy(
            Level level,
            BlockPos pos,
            BlockState state,
            @Nullable LivingEntity placer,
            ItemStack stack
    ) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (level.isClientSide) {
            return;
        }

        Direction facing = state.getValue(FACING);
        BlockPos headPos = pos.relative(facing.getOpposite());

        level.setBlock(
                headPos,
                state.setValue(PART, BedPart.HEAD),
                Block.UPDATE_ALL
        );
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            BedPart part = state.getValue(PART);
            Direction facing = state.getValue(FACING);

            BlockPos otherPos = part == BedPart.HEAD
                    ? pos.relative(facing)
                    : pos.relative(facing.getOpposite());

            BlockState otherState = level.getBlockState(otherPos);

            if (otherState.getBlock() == this && otherState.getValue(PART) != part) {
                level.destroyBlock(otherPos, false, player);
            }
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return getTableShape(state);
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return getTableShape(state);
    }

    private static VoxelShape getTableShape(BlockState state) {
        BedPart part = state.getValue(PART);
        Direction facing = state.getValue(FACING);

        boolean head = part == BedPart.HEAD;

        return switch (facing) {
            case NORTH -> head ? HEAD_SHAPE_NORTH : FOOT_SHAPE_NORTH;
            case SOUTH -> head ? HEAD_SHAPE_SOUTH : FOOT_SHAPE_SOUTH;
            case EAST -> head ? HEAD_SHAPE_EAST : FOOT_SHAPE_EAST;
            case WEST -> head ? HEAD_SHAPE_WEST : FOOT_SHAPE_WEST;
            default -> head ? HEAD_SHAPE_NORTH : FOOT_SHAPE_NORTH;
        };
    }

    private static VoxelShape rotateShape90(VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[]{Shapes.empty()};

        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            buffer[0] = Shapes.or(
                    buffer[0],
                    Shapes.box(
                            1.0D - maxZ,
                            minY,
                            minX,
                            1.0D - minZ,
                            maxY,
                            maxX
                    )
            );
        });

        return buffer[0];
    }

    private static VoxelShape rotateShape180(VoxelShape shape) {
        return rotateShape90(rotateShape90(shape));
    }

    private static VoxelShape rotateShape270(VoxelShape shape) {
        return rotateShape90(rotateShape180(shape));
    }

    @Override
    protected VoxelShape getOcclusionShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos
    ) {
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getVisualShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(
            BlockState state,
            BlockGetter level,
            BlockPos pos
    ) {
        return true;
    }

    @Override
    protected float getShadeBrightness(
            BlockState state,
            BlockGetter level,
            BlockPos pos
    ) {
        return 1.0F;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SurgeryTableBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockPos headPos = state.getValue(PART) == BedPart.HEAD
                ? pos
                : pos.relative(state.getValue(FACING).getOpposite());

        BlockState headState = level.getBlockState(headPos);

        if (headState.getBlock() != this) {
            headState = state;
            headPos = pos;
        }

        if (!SurgeryData.isOnTable(player)) {
            lieDown(headState, headPos, player);
        } else {
            heal(player);
        }

        return InteractionResult.SUCCESS;
    }

    private void lieDown(BlockState headState, BlockPos headPos, Player player) {
        BlockEntity be = player.level().getBlockEntity(headPos);

        if (!(be instanceof SurgeryTableBlockEntity tableEntity)) {
            return;
        }

        if (tableEntity.isOccupied() && !tableEntity.isOccupiedBy(player.getUUID())) {
            player.displayClientMessage(Component.literal("§cEz a műtőasztal már foglalt!"), true);
            return;
        }

        tableEntity.setOccupant(player.getUUID());

        Direction facing = headState.getValue(FACING);

        double x = headPos.getX() + 0.5D + (facing.getStepX() * 1.45D);
        double y = headPos.getY() + TABLE_SURFACE_HEIGHT;
        double z = headPos.getZ() + 0.5D + facing.getStepZ() * 1.45D;

        float yaw = switch (facing) {
            case SOUTH -> 0.0F;
            case NORTH -> 180.0F;
            case WEST -> 90.0F;
            case EAST -> -90.0F;
            default -> 0.0F;
        };

        player.setShiftKeyDown(false);
        player.setPose(Pose.SLEEPING);

        SurgeryData.setOnTable(player, true);
        SurgeryData.setTablePos(player, headPos);
        SurgeryData.setLockPos(player, headPos);
        SurgeryData.setLockYaw(player, yaw);

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.teleport(x, y, z, yaw, 0.0F);
        } else {
            player.setPos(x, y, z);
            player.setYRot(yaw);
            player.setXRot(0.0F);
        }

        player.setYBodyRot(yaw);
        player.setYHeadRot(yaw);

        /*SurgeryData.setBodyPartHp(player, BodyPart.HEAD, 75);
        SurgeryData.setBodyPartHp(player, BodyPart.CHEST, 85);
        SurgeryData.setBodyPartHp(player, BodyPart.ABDOMEN, 90);
        SurgeryData.setBodyPartHp(player, BodyPart.LEFT_ARM, 70);
        SurgeryData.setBodyPartHp(player, BodyPart.RIGHT_ARM, 100);
        SurgeryData.setBodyPartHp(player, BodyPart.LEFT_LEG, 35);
        SurgeryData.setBodyPartHp(player, BodyPart.RIGHT_LEG, 60);

        SurgeryData.setBodyPartStatus(player, BodyPart.HEAD, InjuryStatus.NONE);
        SurgeryData.setBodyPartStatus(player, BodyPart.CHEST, InjuryStatus.CUT);
        SurgeryData.setBodyPartStatus(player, BodyPart.ABDOMEN, InjuryStatus.NONE);
        SurgeryData.setBodyPartStatus(player, BodyPart.LEFT_ARM, InjuryStatus.CUT);
        SurgeryData.setBodyPartStatus(player, BodyPart.RIGHT_ARM, InjuryStatus.NONE);
        SurgeryData.setBodyPartStatus(player, BodyPart.LEFT_LEG, InjuryStatus.FRACTURE);
        SurgeryData.setBodyPartStatus(player, BodyPart.RIGHT_LEG, InjuryStatus.NONE);
        SurgeryData.setBloodMl(player, 4300.0D);*/

        sendState(player, headPos, true);
    }

    public static void getUp(Player player) {
        SurgeryData.clearAllInjuries(player);
        SurgeryData.setOnTable(player, false);
        SurgeryData.setLockPos(player, null);
        SurgeryData.setLockYaw(player, 0.0F);

        player.setPose(Pose.STANDING);

        BlockPos tablePos = SurgeryData.getTablePos(player);

        if (tablePos != null && player.level().getBlockEntity(tablePos) instanceof SurgeryTableBlockEntity tableEntity) {
            if (tableEntity.isOccupiedBy(player.getUUID())) {
                tableEntity.clearOccupant();
            }
        }

        sendState(player, tablePos != null ? tablePos : player.blockPosition(), false);
    }

    private void heal(Player player) {
        getUp(player);
        player.displayClientMessage(Component.literal("§aAll injuries healed!"), true);
    }

    private static void sendState(Player player, BlockPos tablePos, boolean active) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        SurgeryStatePayload payload = new SurgeryStatePayload(
                player.getUUID(),
                tablePos,
                active,
                SurgeryData.getAllInjuries(player)
        );

        for (ServerPlayer target : serverPlayer.serverLevel().players()) {
            ServerPlayNetworking.send(target, payload);
        }
    }
}