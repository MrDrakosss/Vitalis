package me.xavi.vitalis.client.screen;

import me.xavi.vitalis.menus.MedicalCabinetMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class MedicalCabinetScreen extends AbstractContainerScreen<MedicalCabinetMenu> {
    private static final ResourceLocation CONTAINER_BACKGROUND =
            ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");

    public MedicalCabinetScreen(MedicalCabinetMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.imageWidth = 176;
        this.imageHeight = MedicalCabinetMenu.PREVIEW_HEIGHT + (menu.getRows() * 18) + 114;
        this.titleLabelY = MedicalCabinetMenu.PREVIEW_HEIGHT + 5;
        this.inventoryLabelY = MedicalCabinetMenu.PREVIEW_HEIGHT + (menu.getRows() * 18) + 21;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int rows = this.menu.getRows();
        int containerTop = this.topPos + MedicalCabinetMenu.PREVIEW_HEIGHT;

        graphics.fill(
                this.leftPos,
                this.topPos,
                this.leftPos + this.imageWidth,
                this.topPos + MedicalCabinetMenu.PREVIEW_HEIGHT,
                0xCCDDEFF2
        );

        graphics.blit(
                CONTAINER_BACKGROUND,
                this.leftPos,
                containerTop,
                0,
                0,
                this.imageWidth,
                rows * 18 + 17
        );

        graphics.blit(
                CONTAINER_BACKGROUND,
                this.leftPos,
                containerTop + rows * 18 + 17,
                0,
                126,
                this.imageWidth,
                96
        );

        renderAllowedItems(graphics, mouseX, mouseY);
    }

    private void renderAllowedItems(GuiGraphics graphics, int mouseX, int mouseY) {
        List<ItemStack> allowedItems = this.menu.getAllowedPreviewItems();

        int startX = this.leftPos + 8;
        int startY = this.topPos + 6;

        graphics.drawString(
                this.font,
                Component.translatable("screen.vitalis.allowed_items"),
                startX,
                startY,
                0xFFFFFF,
                true
        );

        int itemY = startY + 13;

        for (int i = 0; i < allowedItems.size(); i++) {
            ItemStack stack = allowedItems.get(i);

            int x = startX + (i % 9) * 18;
            int y = itemY + (i / 9) * 18;

            graphics.renderFakeItem(stack, x, y);

            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                graphics.renderTooltip(this.font, stack, mouseX, mouseY);
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
