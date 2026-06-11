package me.xavi.vitalis.block;

import me.xavi.vitalis.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class SurgeryTableBlockEntity extends BlockEntity {
    public SurgeryTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SURGERY_TABLE_BE, pos, state);
    }
}
