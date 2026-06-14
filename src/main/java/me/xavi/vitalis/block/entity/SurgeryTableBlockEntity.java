package me.xavi.vitalis.block.entity;

import me.xavi.vitalis.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class SurgeryTableBlockEntity extends BlockEntity {

    private UUID occupant;

    public SurgeryTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SURGERY_TABLE_BE, pos, state);
    }

    public boolean isOccupied() {
        return occupant != null;
    }

    public UUID getOccupant() {
        return occupant;
    }

    public boolean isOccupiedBy(UUID uuid) {
        return occupant != null && occupant.equals(uuid);
    }

    public void setOccupant(UUID occupant) {
        this.occupant = occupant;
        setChanged();
    }

    public void clearOccupant() {
        this.occupant = null;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        if (occupant != null) {
            tag.putUUID("Occupant", occupant);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.hasUUID("Occupant")) {
            occupant = tag.getUUID("Occupant");
        } else {
            occupant = null;
        }
    }
}