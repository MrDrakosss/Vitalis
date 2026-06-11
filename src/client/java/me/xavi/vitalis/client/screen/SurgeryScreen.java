package me.xavi.vitalis.client.screen;

import me.xavi.vitalis.Vitalis;
import me.xavi.vitalis.client.ClientMedicalState;
import me.xavi.vitalis.medical.BloodLevel;
import me.xavi.vitalis.medical.BodyPart;
import me.xavi.vitalis.medical.InjuryStatus;
import me.xavi.vitalis.network.SurgeryActionPayload;
import me.xavi.vitalis.network.SurgeryLeavePayload;
import me.xavi.vitalis.registry.ModItems;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SurgeryScreen extends Screen {

    private static final ResourceLocation INVENTORY_TEXTURE =
            Vitalis.id("textures/gui/surgery_inventory.png");

    // Surgary Inventory
    private static final int INV_WIDTH = 47;
    private static final int INV_HEIGHT = 212;

    private static final int INV_COLUMNS = 2;
    private static final int INV_ROWS = 11;
    private static final int SLOT_COUNT = INV_COLUMNS * INV_ROWS;

    private static final int SLOT_SIZE = 16;
    private static final int SLOT_STEP_X = 18;
    private static final int SLOT_STEP_Y = 18;

    // Ezeket állítsd finoman a PNG alapján, ha 1-2 pixellel még csúszik.
    private static final int INV_SLOT_START_X = 8;
    private static final int INV_SLOT_START_Y = 8;


    // Start Inventory
    private static final ResourceLocation SURGERY_START_TEXTURE =
            Vitalis.id("textures/gui/surgery_start_inventory.png");

    private static final int START_INV_WIDTH = 150;
    private static final int START_INV_HEIGHT = 66;

    private static final int START_SLOT_X = 13;
    private static final int START_SLOT_Y = 18;
    private static final int START_SLOT_STEP = 18;
    private static final int START_SLOT_COUNT = 7;

    private static final int START_BUTTON_X = 7;
    private static final int START_BUTTON_Y = 39;
    private static final int START_BUTTON_WIDTH = 136;
    private static final int START_BUTTON_HEIGHT = 20;

    private final List<ItemStack> medicalInventory = new ArrayList<>();

    private BodyPart selectedPart = BodyPart.HEAD;
    private Button surgeryButton;

    public SurgeryScreen() {
        super(Component.translatable("screen.vitalis.surgery"));
        buildDemoInventory();
    }

    private void buildDemoInventory() {
        medicalInventory.clear();

        medicalInventory.add(new ItemStack(ModItems.BANDAGE, 12));
        medicalInventory.add(new ItemStack(ModItems.STERILE_BANDAGE, 8));
        medicalInventory.add(new ItemStack(ModItems.PRESSURE_BANDAGE, 5));
        medicalInventory.add(new ItemStack(ModItems.TOURNIQUET, 1));
        medicalInventory.add(new ItemStack(ModItems.SPLINT, 4));
        medicalInventory.add(new ItemStack(ModItems.CAST, 2));
        medicalInventory.add(new ItemStack(ModItems.SURGICAL_KIT, 1));
        medicalInventory.add(new ItemStack(ModItems.SCALPEL, 1));
        medicalInventory.add(new ItemStack(ModItems.FORCEPS, 1));
        medicalInventory.add(new ItemStack(ModItems.SUTURE_KIT, 3));
        medicalInventory.add(new ItemStack(ModItems.PAINKILLER, 10));
        medicalInventory.add(new ItemStack(ModItems.MORPHINE, 2));
        medicalInventory.add(new ItemStack(ModItems.ANTIBIOTIC, 6));
        medicalInventory.add(new ItemStack(ModItems.BLOOD_BAG, 3));
        medicalInventory.add(new ItemStack(ModItems.IV_SET, 3));
        medicalInventory.add(new ItemStack(ModItems.VITAL_SCANNER, 1));
    }

    @Override
    protected void init() {
        int leftX = 16;
        int topY = 24;

        int buttonW = 112;
        int buttonH = 20;
        int gap = 5;

        int index = 0;

        for (BodyPart part : BodyPart.VALUES) {
            addRenderableWidget(Button.builder(
                    Component.translatable(partTranslationKey(part)),
                    button -> selectedPart = part
            ).bounds(
                    leftX + 12,
                    topY + 38 + (buttonH + gap) * index,
                    buttonW,
                    buttonH
            ).build());

            index++;
        }

        addRenderableWidget(Button.builder(
                Component.translatable("screen.vitalis.close"),
                button -> closeAndLeave()
        ).bounds(10, this.height - 30, 86, 20).build());

        int startPanelX = (this.width - START_INV_WIDTH) / 2;
        int startPanelY = this.height - 115;

        surgeryButton = addRenderableWidget(Button.builder(
                Component.translatable("screen.vitalis.surgery.start"),
                button -> ClientPlayNetworking.send(new SurgeryActionPayload(selectedPart))
        ).bounds(
                startPanelX + START_BUTTON_X,
                startPanelY + START_BUTTON_Y,
                START_BUTTON_WIDTH,
                START_BUTTON_HEIGHT
        ).build());
    }

    @Override
    public void tick() {
        super.tick();

        if (surgeryButton != null) {
            boolean visible = ClientMedicalState.needsSurgery(selectedPart);
            surgeryButton.visible = visible;
            surgeryButton.active = visible && hasAllRequirements(selectedPart);
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderLeftPanel(graphics);
        renderSurgeryStartInventory(graphics);
        renderMedicalInventory(graphics);

        super.render(graphics, mouseX, mouseY, partialTick);

        renderMedicalInventoryTooltip(graphics, mouseX, mouseY);
        renderRequirementTooltip(graphics, mouseX, mouseY);
    }

    private void renderLeftPanel(GuiGraphics graphics) {
        int x = 16;
        int y = 24;
        int w = 150;
        int h = this.height - 76;

        graphics.fill(x, y, x + w, y + h, 0xC20A1018);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0x99202A36);

        graphics.fill(x, y, x + w, y + 26, 0xE08B1E1E);
        graphics.fill(x + 2, y + 2, x + w - 2, y + 25, 0x804A0E0E);

        graphics.drawString(
                this.font,
                Component.translatable("screen.vitalis.surgery"),
                x + 10,
                y + 9,
                0xFFFFFFFF,
                false
        );

        int infoY = y + 225;

        int hp = ClientMedicalState.getHp(selectedPart);
        int maxHp = selectedPart.getMaxHp();
        InjuryStatus status = ClientMedicalState.getStatus(selectedPart);
        BloodLevel bloodLevel = ClientMedicalState.getBloodLevel();

        drawInfoBox(
                graphics,
                x + 10,
                infoY,
                w - 20,
                Component.translatable("screen.vitalis.selected"),
                Component.translatable(partTranslationKey(selectedPart)),
                0xFFFFFFFF
        );

        drawInfoBox(
                graphics,
                x + 10,
                infoY + 38,
                w - 20,
                Component.literal("HP"),
                Component.literal(hp + " / " + maxHp),
                colorForHp(hp, maxHp)
        );

        drawInfoBox(
                graphics,
                x + 10,
                infoY + 76,
                w - 20,
                Component.translatable("screen.vitalis.status"),
                Component.translatable(statusTranslationKey(status)),
                colorForStatus(status, hp, maxHp)
        );

        drawInfoBox(
                graphics,
                x + 10,
                infoY + 114,
                w - 20,
                Component.translatable("hud.vitalis.blood"),
                Component.literal(String.format("%.0f / %.0f ml", ClientMedicalState.getBloodMl(), BloodLevel.MAX_BLOOD_ML)),
                colorForBlood(bloodLevel)
        );
    }

    private void renderSurgeryStartInventory(GuiGraphics graphics) {
        if (!ClientMedicalState.needsSurgery(selectedPart)) {
            return;
        }

        int x = (this.width - START_INV_WIDTH) / 2;
        int y = this.height - 115;

        graphics.blit(
                SURGERY_START_TEXTURE,
                x,
                y,
                0,
                0,
                START_INV_WIDTH,
                START_INV_HEIGHT,
                START_INV_WIDTH,
                START_INV_HEIGHT
        );

        graphics.drawString(
                this.font,
                Component.translatable("screen.vitalis.required_supplies"),
                x + 8,
                y + 6,
                0xFFFFFFFF,
                false
        );

        List<Requirement> requirements = getRequirements(selectedPart);

        for (int i = 0; i < START_SLOT_COUNT; i++) {
            if (i >= requirements.size()) {
                continue;
            }

            Requirement requirement = requirements.get(i);
            int available = countItem(requirement.item());
            boolean hasEnough = available >= requirement.amount();

            int slotX = x + START_SLOT_X + i * START_SLOT_STEP;
            int slotY = y + START_SLOT_Y;

            ItemStack stack = new ItemStack(requirement.item(), requirement.amount());

            graphics.renderItem(stack, slotX, slotY);
            graphics.renderItemDecorations(this.font, stack, slotX, slotY);

            if (!hasEnough) {
                graphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0x88FF0000);
            }
        }
    }

    private boolean hasAllRequirements(BodyPart part) {
        for (Requirement requirement : getRequirements(part)) {
            if (countItem(requirement.item()) < requirement.amount()) {
                return false;
            }
        }

        return true;
    }

    private void renderSurgeryRequirement(GuiGraphics graphics) {
        if (!ClientMedicalState.needsSurgery(selectedPart)) {
            return;
        }

        List<Requirement> requirements = getRequirements(selectedPart);

        int panelW = 170;
        int panelH = 22 + requirements.size() * 18;

        int x = (this.width - panelW) / 2;
        int y = this.height - 58 - panelH - 6;

        graphics.fill(x, y, x + panelW, y + panelH, 0xC20A1018);
        graphics.fill(x + 1, y + 1, x + panelW - 1, y + panelH - 1, 0x99202A36);
        graphics.fill(x, y, x + panelW, y + 18, 0xCC7A1717);

        graphics.drawString(
                this.font,
                Component.translatable("screen.vitalis.supplies"),
                x + 8,
                y + 6,
                0xFFFFFFFF,
                false
        );

        int lineY = y + 23;

        for (Requirement requirement : requirements) {
            int available = countItem(requirement.item());
            boolean hasEnough = available >= requirement.amount();

            ItemStack stack = new ItemStack(requirement.item(), requirement.amount());

            graphics.renderItem(stack, x + 8, lineY - 4);
            graphics.renderItemDecorations(this.font, stack, x + 8, lineY - 4);

            int color = hasEnough ? 0xFF55FF55 : 0xFFFF5555;

            graphics.drawString(
                    this.font,
                    Component.literal(available + " / " + requirement.amount()),
                    x + 32,
                    lineY,
                    color,
                    false
            );

            graphics.drawString(
                    this.font,
                    stack.getHoverName(),
                    x + 72,
                    lineY,
                    color,
                    false
            );

            lineY += 18;
        }
    }

    private void renderMedicalInventory(GuiGraphics graphics) {
        int invX = this.width - INV_WIDTH - 2;
        int invY = Math.max(8, (this.height - INV_HEIGHT) / 2);

        graphics.blit(
                INVENTORY_TEXTURE,
                invX,
                invY,
                0,
                0,
                INV_WIDTH,
                INV_HEIGHT,
                INV_WIDTH,
                INV_HEIGHT
        );

        for (int i = 0; i < SLOT_COUNT; i++) {
            if (i >= medicalInventory.size()) {
                continue;
            }

            ItemStack stack = medicalInventory.get(i);

            if (stack.isEmpty()) {
                continue;
            }

            int col = i % INV_COLUMNS;
            int row = i / INV_COLUMNS;

            int slotX = invX + INV_SLOT_START_X + col * SLOT_STEP_X;
            int slotY = invY + INV_SLOT_START_Y + row * SLOT_STEP_Y;

            graphics.renderItem(stack, slotX, slotY);
            graphics.renderItemDecorations(this.font, stack, slotX, slotY);
        }
    }

    private void renderMedicalInventoryTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int hoveredSlot = getHoveredInventorySlot(mouseX, mouseY);

        if (hoveredSlot < 0 || hoveredSlot >= medicalInventory.size()) {
            return;
        }

        ItemStack stack = medicalInventory.get(hoveredSlot);

        if (stack.isEmpty()) {
            return;
        }

        graphics.renderTooltip(this.font, stack, mouseX, mouseY);
    }

    private void renderRequirementTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!ClientMedicalState.needsSurgery(selectedPart)) {
            return;
        }

        int x = (this.width - START_INV_WIDTH) / 2;
        int y = this.height - 115;

        List<Requirement> requirements = getRequirements(selectedPart);

        for (int i = 0; i < requirements.size() && i < START_SLOT_COUNT; i++) {
            int slotX = x + START_SLOT_X + i * START_SLOT_STEP;
            int slotY = y + START_SLOT_Y;

            if (mouseX >= slotX && mouseX < slotX + 16 && mouseY >= slotY && mouseY < slotY + 16) {
                Requirement requirement = requirements.get(i);
                graphics.renderTooltip(
                        this.font,
                        new ItemStack(requirement.item(), requirement.amount()),
                        mouseX,
                        mouseY
                );
                return;
            }
        }
    }

    private int getHoveredInventorySlot(int mouseX, int mouseY) {
        int invX = this.width - INV_WIDTH - 2;
        int invY = Math.max(8, (this.height - INV_HEIGHT) / 2);

        for (int row = 0; row < INV_ROWS; row++) {
            for (int col = 0; col < INV_COLUMNS; col++) {
                int index = row * INV_COLUMNS + col;

                int slotX = invX + INV_SLOT_START_X + col * SLOT_STEP_X;
                int slotY = invY + INV_SLOT_START_Y + row * SLOT_STEP_Y;

                if (mouseX >= slotX
                        && mouseX < slotX + SLOT_SIZE
                        && mouseY >= slotY
                        && mouseY < slotY + SLOT_SIZE) {
                    return index;
                }
            }
        }

        return -1;
    }

    private List<Requirement> getRequirements(BodyPart part) {
        InjuryStatus status = ClientMedicalState.getStatus(part);

        List<Requirement> requirements = new ArrayList<>();

        if (status == InjuryStatus.FRACTURE) {
            requirements.add(new Requirement(ModItems.SPLINT, 1));
            requirements.add(new Requirement(ModItems.CAST, 1));
            requirements.add(new Requirement(ModItems.PAINKILLER, 1));
            return requirements;
        }

        if (status == InjuryStatus.OPEN_FRACTURE) {
            requirements.add(new Requirement(ModItems.SURGICAL_KIT, 1));
            requirements.add(new Requirement(ModItems.SCALPEL, 1));
            requirements.add(new Requirement(ModItems.SUTURE_KIT, 1));
            requirements.add(new Requirement(ModItems.STERILE_BANDAGE, 2));
            return requirements;
        }

        if (status == InjuryStatus.BULLET_WOUND) {
            requirements.add(new Requirement(ModItems.SURGICAL_KIT, 1));
            requirements.add(new Requirement(ModItems.FORCEPS, 1));
            requirements.add(new Requirement(ModItems.SUTURE_KIT, 1));
            requirements.add(new Requirement(ModItems.PRESSURE_BANDAGE, 1));
            return requirements;
        }

        if (status == InjuryStatus.CUT) {
            requirements.add(new Requirement(ModItems.BANDAGE, 1));
            requirements.add(new Requirement(ModItems.SUTURE_KIT, 1));
            return requirements;
        }

        if (status == InjuryStatus.BURN) {
            requirements.add(new Requirement(ModItems.STERILE_BANDAGE, 1));
            requirements.add(new Requirement(ModItems.PAINKILLER, 1));
            return requirements;
        }

        int hp = ClientMedicalState.getHp(part);
        int maxHp = part.getMaxHp();

        if (hp <= maxHp * 0.35D) {
            requirements.add(new Requirement(ModItems.SURGICAL_KIT, 1));
            requirements.add(new Requirement(ModItems.BLOOD_BAG, 1));
            requirements.add(new Requirement(ModItems.IV_SET, 1));
        }

        return requirements;
    }

    private int countItem(Item item) {
        int amount = 0;

        for (ItemStack stack : medicalInventory) {
            if (stack.is(item)) {
                amount += stack.getCount();
            }
        }

        return amount;
    }

    private void drawInfoBox(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            Component label,
            Component value,
            int valueColor
    ) {
        graphics.fill(x, y, x + width, y + 30, 0xAA05080D);
        graphics.fill(x + 1, y + 1, x + width - 1, y + 29, 0x55222C38);

        graphics.drawString(this.font, label, x + 6, y + 5, 0xFFAAAAAA, false);
        graphics.drawString(this.font, value, x + 6, y + 17, valueColor, false);
    }

    private int colorForHp(int hp, int maxHp) {
        double ratio = (double) hp / (double) maxHp;

        if (ratio <= 0.35D) {
            return 0xFFFF5555;
        }

        if (ratio <= 0.7D) {
            return 0xFFFFFF55;
        }

        return 0xFF55FF55;
    }

    private int colorForStatus(InjuryStatus status, int hp, int maxHp) {
        if (status == InjuryStatus.OPEN_FRACTURE || status == InjuryStatus.BULLET_WOUND) {
            return 0xFFFF5555;
        }

        if (status != InjuryStatus.NONE) {
            return 0xFFFFFF55;
        }

        return colorForHp(hp, maxHp);
    }

    private int colorForBlood(BloodLevel bloodLevel) {
        return switch (bloodLevel) {
            case NORMAL -> 0xFF55FF55;
            case WEAK, DIZZY -> 0xFFFFFF55;
            case UNCONSCIOUS, DEAD -> 0xFFFF5555;
        };
    }

    private String partTranslationKey(BodyPart part) {
        return switch (part) {
            case HEAD -> "body_part.vitalis.head";
            case CHEST -> "body_part.vitalis.chest";
            case ABDOMEN -> "body_part.vitalis.abdomen";
            case LEFT_ARM -> "body_part.vitalis.left_arm";
            case RIGHT_ARM -> "body_part.vitalis.right_arm";
            case LEFT_LEG -> "body_part.vitalis.left_leg";
            case RIGHT_LEG -> "body_part.vitalis.right_leg";
        };
    }

    private String statusTranslationKey(InjuryStatus status) {
        return switch (status) {
            case NONE -> "status.vitalis.healthy";
            case FRACTURE -> "status.vitalis.fracture";
            case OPEN_FRACTURE -> "status.vitalis.open_fracture";
            case BULLET_WOUND -> "status.vitalis.bullet_wound";
            case CUT -> "status.vitalis.cut";
            case BURN -> "status.vitalis.burn";
        };
    }

    private void closeAndLeave() {
        ClientPlayNetworking.send(new SurgeryLeavePayload());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int hoveredSlot = getHoveredInventorySlot((int) mouseX, (int) mouseY);

        if (hoveredSlot >= 0) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        int hoveredSlot = getHoveredInventorySlot((int) mouseX, (int) mouseY);

        if (hoveredSlot >= 0) {
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        int hoveredSlot = getHoveredInventorySlot((int) mouseX, (int) mouseY);

        if (hoveredSlot >= 0) {
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            closeAndLeave();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record Requirement(Item item, int amount) {
    }
}