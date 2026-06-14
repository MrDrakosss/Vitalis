package me.xavi.vitalis.block.entity;

import me.xavi.vitalis.block.MedicalCabinetBlock;
import me.xavi.vitalis.menus.MedicalCabinetMenu;
import me.xavi.vitalis.registry.ModBlockEntities;
import me.xavi.vitalis.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Set;

public class MedicalCabinetBlockEntity extends BlockEntity implements Container, MenuProvider {
    private NonNullList<ItemStack> items = NonNullList.withSize(36, ItemStack.EMPTY);

    public MedicalCabinetBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MEDICAL_CABINET, pos, state);
    }

    private MedicalCabinetBlock.CabinetType getCabinetType() {
        if (this.getBlockState().getBlock() instanceof MedicalCabinetBlock cabinetBlock) {
            return cabinetBlock.getCabinetType();
        }

        return MedicalCabinetBlock.CabinetType.SUPPLY;
    }

    private Set<Item> getAllowedItems() {
        return switch (getCabinetType()) {
            case REFRIGERATOR -> Set.of(
                    ModItems.BLOOD_BAG,
                    ModItems.MORPHINE,
                    ModItems.ANTIBIOTIC,
                    ModItems.IV_SET,
                    ModItems.ADRENALINE_INJECTION
            );

            case SUPPLY -> Set.of(
                    ModItems.BANDAGE,
                    ModItems.STERILE_BANDAGE,
                    ModItems.PRESSURE_BANDAGE,
                    ModItems.TOURNIQUET,
                    ModItems.SPLINT,
                    ModItems.CAST,
                    ModItems.PAINKILLER,
                    ModItems.ENERGY_DRINK
            );

            case EQUIPMENT -> Set.of(
                    ModItems.SURGICAL_KIT,
                    ModItems.SCALPEL,
                    ModItems.FORCEPS,
                    ModItems.SUTURE_KIT,
                    ModItems.CPR_KIT,
                    ModItems.OXYGEN_MASK,
                    ModItems.VITAL_SCANNER
            );
        };
    }

    public List<ItemStack> getAllowedPreviewItems() {
        return MedicalCabinetMenu.createAllowedPreviewItems(getCabinetType());
    }

    private int getRows() {
        if (this.getBlockState().getBlock() instanceof MedicalCabinetBlock cabinetBlock && cabinetBlock.isLarge()) {
            return 4;
        }

        return 2;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return getAllowedItems().contains(stack.getItem());
    }

    @Override
    public Component getDisplayName() {
        return switch (getCabinetType()) {
            case REFRIGERATOR -> Component.translatable("container.vitalis.medical_refrigerator");
            case SUPPLY -> Component.translatable("container.vitalis.supply_cabinet");
            case EQUIPMENT -> Component.translatable("container.vitalis.equipment_cabinet");
        };
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new MedicalCabinetMenu(
                syncId,
                playerInventory,
                this,
                getRows(),
                getCabinetType()
        );
    }

    @Override
    public int getContainerSize() {
        return getRows() * 9;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < getContainerSize(); i++) {
            if (!items.get(i).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(items, slot, amount);

        if (!stack.isEmpty()) {
            setChanged();
        }

        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);

        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }

        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null) {
            return false;
        }

        return level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D
        ) <= 64.0D;
    }

    @Override
    public void clearContent() {
        this.items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
    }
}
