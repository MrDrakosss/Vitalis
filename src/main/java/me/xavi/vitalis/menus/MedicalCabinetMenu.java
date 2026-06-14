package me.xavi.vitalis.menus;

import me.xavi.vitalis.block.MedicalCabinetBlock;
import me.xavi.vitalis.registry.ModItems;
import me.xavi.vitalis.registry.ModMenuTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class MedicalCabinetMenu extends AbstractContainerMenu {
    public static final int PREVIEW_HEIGHT = 38;

    private final Container container;
    private final int rows;
    private final MedicalCabinetBlock.CabinetType cabinetType;
    private final List<ItemStack> allowedPreviewItems;

    public MedicalCabinetMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(18), 2, MedicalCabinetBlock.CabinetType.SUPPLY);
    }

    public MedicalCabinetMenu(
            int syncId,
            Inventory playerInventory,
            Container container,
            int rows,
            MedicalCabinetBlock.CabinetType cabinetType
    ) {
        super(getMenuType(cabinetType, rows), syncId);

        this.container = container;
        this.rows = rows;
        this.cabinetType = cabinetType;
        this.allowedPreviewItems = createAllowedPreviewItems(cabinetType);

        int containerSize = rows * 9;
        checkContainerSize(container, containerSize);

        container.startOpen(playerInventory.player);

        int cabinetStartY = PREVIEW_HEIGHT + 18;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = col + row * 9;
                int x = 8 + col * 18;
                int y = cabinetStartY + row * 18;

                this.addSlot(new Slot(container, slotIndex, x, y) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return container.canPlaceItem(this.index, stack);
                    }

                    @Override
                    public void setChanged() {
                        super.setChanged();
                        container.setChanged();
                    }
                });
            }
        }

        int playerInventoryY = PREVIEW_HEIGHT + 18 + rows * 18 + 14;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        8 + col * 18,
                        playerInventoryY + row * 18
                ));
            }
        }

        int hotbarY = playerInventoryY + 58;

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, hotbarY));
        }
    }

    private static MenuType<MedicalCabinetMenu> getMenuType(
            MedicalCabinetBlock.CabinetType cabinetType,
            int rows
    ) {
        boolean large = rows == 4;

        return switch (cabinetType) {
            case REFRIGERATOR -> large
                    ? ModMenuTypes.LARGE_MEDICAL_REFRIGERATOR
                    : ModMenuTypes.MEDICAL_REFRIGERATOR;

            case SUPPLY -> large
                    ? ModMenuTypes.LARGE_SUPPLY_CABINET
                    : ModMenuTypes.SUPPLY_CABINET;

            case EQUIPMENT -> large
                    ? ModMenuTypes.LARGE_EQUIPMENT_CABINET
                    : ModMenuTypes.EQUIPMENT_CABINET;
        };
    }

    public static List<ItemStack> createAllowedPreviewItems(MedicalCabinetBlock.CabinetType cabinetType) {
        return switch (cabinetType) {
            case REFRIGERATOR -> List.of(
                    new ItemStack(ModItems.BLOOD_BAG),
                    new ItemStack(ModItems.MORPHINE),
                    new ItemStack(ModItems.ANTIBIOTIC),
                    new ItemStack(ModItems.IV_SET),
                    new ItemStack(ModItems.ADRENALINE_INJECTION)
            );

            case SUPPLY -> List.of(
                    new ItemStack(ModItems.BANDAGE),
                    new ItemStack(ModItems.STERILE_BANDAGE),
                    new ItemStack(ModItems.PRESSURE_BANDAGE),
                    new ItemStack(ModItems.TOURNIQUET),
                    new ItemStack(ModItems.SPLINT),
                    new ItemStack(ModItems.CAST),
                    new ItemStack(ModItems.PAINKILLER),
                    new ItemStack(ModItems.ENERGY_DRINK)
            );

            case EQUIPMENT -> List.of(
                    new ItemStack(ModItems.SURGICAL_KIT),
                    new ItemStack(ModItems.SCALPEL),
                    new ItemStack(ModItems.FORCEPS),
                    new ItemStack(ModItems.SUTURE_KIT),
                    new ItemStack(ModItems.CPR_KIT),
                    new ItemStack(ModItems.OXYGEN_MASK),
                    new ItemStack(ModItems.VITAL_SCANNER)
            );
        };
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        int containerSize = this.container.getContainerSize();
        Slot slot = this.slots.get(index);

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack originalStack = slot.getItem();
        ItemStack copiedStack = originalStack.copy();

        if (index < containerSize) {
            if (!this.moveItemStackTo(originalStack, containerSize, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.moveItemStackTo(originalStack, 0, containerSize, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (originalStack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return copiedStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    public int getRows() {
        return rows;
    }

    public MedicalCabinetBlock.CabinetType getCabinetType() {
        return cabinetType;
    }

    public List<ItemStack> getAllowedPreviewItems() {
        return allowedPreviewItems;
    }
}
