package me.xavi.vitalis.menus;

import me.xavi.vitalis.registry.ModMenuTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class ComputerMenu extends AbstractContainerMenu {

    public ComputerMenu(int syncId, Inventory inventory) {
        super(ModMenuTypes.COMPUTER, syncId);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
