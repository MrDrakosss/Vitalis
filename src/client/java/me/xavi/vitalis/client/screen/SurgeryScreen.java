package me.xavi.vitalis.client.screen;

import me.xavi.vitalis.Vitalis;
import me.xavi.vitalis.client.state.ClientMedicalState;
import me.xavi.vitalis.client.state.ClientSurgeryState;
import me.xavi.vitalis.client.state.ClientSurgeryTreatmentState;
import me.xavi.vitalis.medical.BloodLevel;
import me.xavi.vitalis.medical.BodyPart;
import me.xavi.vitalis.medical.InjuryStatus;
import me.xavi.vitalis.network.SurgeryActionPayload;
import me.xavi.vitalis.network.SurgeryLeavePayload;
import me.xavi.vitalis.registry.ModItems;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SurgeryScreen extends Screen {

    private static final ResourceLocation INVENTORY_TEXTURE =
            Vitalis.id("textures/gui/surgery_inventory.png");

    private static final ResourceLocation SURGERY_START_TEXTURE =
            Vitalis.id("textures/gui/surgery_start_inventory.png");

    private static final int INV_WIDTH = 47;
    private static final int INV_HEIGHT = 212;

    private static final int INV_COLUMNS = 2;
    private static final int INV_ROWS = 11;
    private static final int SLOT_COUNT = INV_COLUMNS * INV_ROWS;

    private static final int SLOT_SIZE = 16;
    private static final int SLOT_STEP_X = 18;
    private static final int SLOT_STEP_Y = 18;

    private static final int INV_SLOT_START_X = 8;
    private static final int INV_SLOT_START_Y = 8;

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

    private static final int BODY_SCALE = 4;
    private static final int BODY_OUTLINE = 1;

    private final List<ItemStack> medicalInventory = new ArrayList<>();
    private final List<BodyRegion> bodyRegions = new ArrayList<>();

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
        addRenderableWidget(Button.builder(
                Component.translatable("screen.vitalis.close"),
                button -> closeAndLeave()
        ).bounds(10, this.height - 30, 86, 20).build());

        int startPanelX = (this.width - START_INV_WIDTH) / 2;
        int startPanelY = this.height - 115;

        surgeryButton = addRenderableWidget(Button.builder(
                Component.translatable("screen.vitalis.surgery.start"),
                button -> {
                    if (shouldTreatBlood()) {
                        ClientPlayNetworking.send(SurgeryActionPayload.blood());
                    } else {
                        ClientPlayNetworking.send(SurgeryActionPayload.bodyPart(selectedPart));
                    }
                }
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
        renderBodyPanel(graphics, mouseX, mouseY);
        renderMedicalInventory(graphics);
        renderSurgeryStartInventory(graphics);

        renderTreatmentProgress(graphics);

        super.render(graphics, mouseX, mouseY, partialTick);

        renderMedicalInventoryTooltip(graphics, mouseX, mouseY);
        renderRequirementTooltip(graphics, mouseX, mouseY);
    }

    private boolean shouldTreatBlood() {
        boolean allPartsHealthy = true;

        for (BodyPart part : BodyPart.VALUES) {
            if (ClientMedicalState.getHp(part) < part.getMaxHp()
                    || ClientMedicalState.getStatus(part) != InjuryStatus.NONE) {
                allPartsHealthy = false;
                break;
            }
        }

        return allPartsHealthy && ClientMedicalState.getBloodMl() < BloodLevel.MAX_BLOOD_ML;
    }

    private void renderBodyPanel(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = 16;
        int y = 24;
        int w = 166;
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

        renderClickableBody(graphics, x + 58, y + 38);

        int infoY = y + 188;

        int hp = ClientMedicalState.getHp(selectedPart);
        int maxHp = selectedPart.getMaxHp();
        InjuryStatus status = ClientMedicalState.getStatus(selectedPart);

        double percent = maxHp <= 0 ? 0.0D : ((double) hp / (double) maxHp) * 100.0D;

        drawInfoBox(
                graphics,
                x + 10,
                infoY,
                w - 20,
                Component.translatable("screen.vitalis.selected"),
                Component.translatable(partTranslationKey(selectedPart)),
                bodyPartColor(selectedPart)
        );

        drawInfoBox(
                graphics,
                x + 10,
                infoY + 38,
                w - 20,
                Component.translatable("screen.vitalis.injury"),
                Component.literal(String.format("%d / %d HP (%.0f%%)", hp, maxHp, percent)),
                bodyPartColor(selectedPart)
        );

        drawInfoBox(
                graphics,
                x + 10,
                infoY + 76,
                w - 20,
                Component.translatable("screen.vitalis.status"),
                Component.translatable(statusTranslationKey(status)),
                bodyPartColor(selectedPart)
        );

        BloodLevel bloodLevel = ClientMedicalState.getBloodLevel();

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

    private void renderTreatmentProgress(GuiGraphics graphics) {
        if (!ClientSurgeryTreatmentState.isActive()) {
            return;
        }

        int x = (this.width - START_INV_WIDTH) / 2;
        int y = this.height - 135;

        int w = START_INV_WIDTH;
        int h = 8;

        float progress = ClientSurgeryTreatmentState.getProgress();
        int filled = (int) (w * progress);

        graphics.fill(x, y, x + w, y + h, 0xAA111111);
        graphics.fill(x, y, x + filled, y + h, 0xFFAA2222);

        graphics.drawString(
                this.font,
                Component.translatable("screen.vitalis.surgery.in_progress"),
                x,
                y - 12,
                0xFFFFFFFF,
                false
        );

        graphics.drawString(
                this.font,
                ClientSurgeryTreatmentState.getRemainingSeconds() + "s",
                x + w - 24,
                y - 12,
                0xFFFFFFFF,
                false
        );
    }

    private void renderClickableBody(GuiGraphics graphics, int startX, int startY) {
        Minecraft client = Minecraft.getInstance();

        if (!(client.player instanceof AbstractClientPlayer player)) {
            return;
        }

        bodyRegions.clear();

        PlayerSkin skin = player.getSkin();
        ResourceLocation texture = skin.texture();
        boolean slim = skin.model().id().equals("slim");

        int headW = 8 * BODY_SCALE;
        int headH = 8 * BODY_SCALE;

        int bodyW = 8 * BODY_SCALE;
        int bodyH = 12 * BODY_SCALE;

        int armW = (slim ? 3 : 4) * BODY_SCALE;
        int armH = 12 * BODY_SCALE;

        int legW = 4 * BODY_SCALE;
        int legH = 12 * BODY_SCALE;

        int totalW = armW + BODY_OUTLINE + bodyW + BODY_OUTLINE + armW;
        int centerX = startX + totalW / 2;

        int headX = centerX - headW / 2;
        int headY = startY;

        int bodyX = centerX - bodyW / 2;
        int bodyY = headY + headH + BODY_OUTLINE;

        int leftArmX = bodyX - armW - BODY_OUTLINE;
        int rightArmX = bodyX + bodyW + BODY_OUTLINE;

        int legY = bodyY + bodyH + BODY_OUTLINE;
        int leftLegX = centerX - legW;
        int rightLegX = centerX + BODY_OUTLINE;

        drawBodyPart(graphics, texture, headX, headY, 8, 8, 8, 8, BodyPart.HEAD);
        drawSkinPart(graphics, texture, headX, headY, 40, 8, 8, 8);

        drawBodyPart(graphics, texture, bodyX, bodyY, 20, 20, 8, 12, BodyPart.CHEST);
        drawSkinPart(graphics, texture, bodyX, bodyY, 20, 36, 8, 12);

        if (slim) {
            drawBodyPart(graphics, texture, leftArmX, bodyY, 44, 20, 3, 12, BodyPart.LEFT_ARM);
            drawSkinPart(graphics, texture, leftArmX, bodyY, 44, 36, 3, 12);

            drawBodyPart(graphics, texture, rightArmX, bodyY, 36, 52, 3, 12, BodyPart.RIGHT_ARM);
            drawSkinPart(graphics, texture, rightArmX, bodyY, 52, 52, 3, 12);
        } else {
            drawBodyPart(graphics, texture, leftArmX, bodyY, 44, 20, 4, 12, BodyPart.LEFT_ARM);
            drawSkinPart(graphics, texture, leftArmX, bodyY, 44, 36, 4, 12);

            drawBodyPart(graphics, texture, rightArmX, bodyY, 36, 52, 4, 12, BodyPart.RIGHT_ARM);
            drawSkinPart(graphics, texture, rightArmX, bodyY, 52, 52, 4, 12);
        }

        drawBodyPart(graphics, texture, leftLegX, legY, 4, 20, 4, 12, BodyPart.LEFT_LEG);
        drawSkinPart(graphics, texture, leftLegX, legY, 4, 36, 4, 12);

        drawBodyPart(graphics, texture, rightLegX, legY, 20, 52, 4, 12, BodyPart.RIGHT_LEG);
        drawSkinPart(graphics, texture, rightLegX, legY, 4, 52, 4, 12);
    }

    private void drawBodyPart(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int u,
            int v,
            int width,
            int height,
            BodyPart part
    ) {
        int renderedWidth = width * BODY_SCALE;
        int renderedHeight = height * BODY_SCALE;

        bodyRegions.add(new BodyRegion(part, x, y, renderedWidth, renderedHeight));

        drawOutline(graphics, x, y, renderedWidth, renderedHeight, bodyPartColor(part));
        drawSkinPart(graphics, texture, x, y, u, v, width, height);

        if (part == selectedPart) {
            drawSelectionOutline(graphics, x, y, renderedWidth, renderedHeight);
        }
    }

    private void drawSkinPart(
            GuiGraphics graphics,
            ResourceLocation texture,
            int x,
            int y,
            int u,
            int v,
            int width,
            int height
    ) {
        graphics.blit(
                texture,
                x,
                y,
                width * BODY_SCALE,
                height * BODY_SCALE,
                u,
                v,
                width,
                height,
                64,
                64
        );
    }

    private void drawOutline(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x - BODY_OUTLINE, y - BODY_OUTLINE, x + width + BODY_OUTLINE, y, color);
        graphics.fill(x - BODY_OUTLINE, y + height, x + width + BODY_OUTLINE, y + height + BODY_OUTLINE, color);
        graphics.fill(x - BODY_OUTLINE, y, x, y + height, color);
        graphics.fill(x + width, y, x + width + BODY_OUTLINE, y + height, color);
    }

    private void drawSelectionOutline(GuiGraphics graphics, int x, int y, int width, int height) {
        int color = 0xFFFFFFFF;

        graphics.fill(x - 2, y - 2, x + width + 2, y - 1, color);
        graphics.fill(x - 2, y + height + 1, x + width + 2, y + height + 2, color);
        graphics.fill(x - 2, y - 1, x - 1, y + height + 1, color);
        graphics.fill(x + width + 1, y - 1, x + width + 2, y + height + 1, color);
    }

    private void renderMedicalInventory(GuiGraphics graphics) {
        int invX = this.width - INV_WIDTH;
        int invY = Math.max(8, (this.height - INV_HEIGHT) / 2);

        graphics.blit(INVENTORY_TEXTURE, invX, invY, 0, 0, INV_WIDTH, INV_HEIGHT, INV_WIDTH, INV_HEIGHT);

        for (int i = 0; i < SLOT_COUNT; i++) {
            if (i >= medicalInventory.size()) continue;

            ItemStack stack = medicalInventory.get(i);
            if (stack.isEmpty()) continue;

            int col = i % INV_COLUMNS;
            int row = i / INV_COLUMNS;

            int slotX = invX + INV_SLOT_START_X + col * SLOT_STEP_X;
            int slotY = invY + INV_SLOT_START_Y + row * SLOT_STEP_Y;

            graphics.renderItem(stack, slotX, slotY);
            graphics.renderItemDecorations(this.font, stack, slotX, slotY);
        }
    }

    private void renderSurgeryStartInventory(GuiGraphics graphics) {
        if (!ClientMedicalState.needsSurgery(selectedPart)) {
            return;
        }

        int x = (this.width - START_INV_WIDTH) / 2;
        int y = this.height - 115;

        graphics.blit(SURGERY_START_TEXTURE, x, y, 0, 0, START_INV_WIDTH, START_INV_HEIGHT, START_INV_WIDTH, START_INV_HEIGHT);

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
            if (i >= requirements.size()) continue;

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

    private void renderMedicalInventoryTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int hoveredSlot = getHoveredInventorySlot(mouseX, mouseY);

        if (hoveredSlot < 0 || hoveredSlot >= medicalInventory.size()) return;

        ItemStack stack = medicalInventory.get(hoveredSlot);
        if (stack.isEmpty()) return;

        graphics.renderTooltip(this.font, stack, mouseX, mouseY);
    }

    private void renderRequirementTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!ClientMedicalState.needsSurgery(selectedPart)) return;

        int x = (this.width - START_INV_WIDTH) / 2;
        int y = this.height - 115;

        List<Requirement> requirements = getRequirements(selectedPart);

        for (int i = 0; i < requirements.size() && i < START_SLOT_COUNT; i++) {
            int slotX = x + START_SLOT_X + i * START_SLOT_STEP;
            int slotY = y + START_SLOT_Y;

            if (mouseX >= slotX && mouseX < slotX + 16 && mouseY >= slotY && mouseY < slotY + 16) {
                Requirement requirement = requirements.get(i);
                graphics.renderTooltip(this.font, new ItemStack(requirement.item(), requirement.amount()), mouseX, mouseY);
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

                if (mouseX >= slotX && mouseX < slotX + SLOT_SIZE && mouseY >= slotY && mouseY < slotY + SLOT_SIZE) {
                    return index;
                }
            }
        }

        return -1;
    }

    private BodyPart getClickedBodyPart(double mouseX, double mouseY) {
        for (BodyRegion region : bodyRegions) {
            if (mouseX >= region.x()
                    && mouseX < region.x() + region.width()
                    && mouseY >= region.y()
                    && mouseY < region.y() + region.height()) {
                return region.part();
            }
        }

        return null;
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

    private boolean hasAllRequirements(BodyPart part) {
        for (Requirement requirement : getRequirements(part)) {
            if (countItem(requirement.item()) < requirement.amount()) {
                return false;
            }
        }

        return true;
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

    private void drawInfoBox(GuiGraphics graphics, int x, int y, int width, Component label, Component value, int valueColor) {
        graphics.fill(x, y, x + width, y + 30, 0xAA05080D);
        graphics.fill(x + 1, y + 1, x + width - 1, y + 29, 0x55222C38);

        graphics.drawString(this.font, label, x + 6, y + 5, 0xFFAAAAAA, false);
        graphics.drawString(this.font, value, x + 6, y + 17, valueColor, false);
    }

    private int bodyPartColor(BodyPart part) {
        int hp = ClientMedicalState.getHp(part);
        int maxHp = part.getMaxHp();
        InjuryStatus status = ClientMedicalState.getStatus(part);

        if (hp <= 0) return 0xFF777777;

        double ratio = maxHp <= 0 ? 0.0D : (double) hp / (double) maxHp;

        if (status == InjuryStatus.OPEN_FRACTURE || status == InjuryStatus.BULLET_WOUND || ratio <= 0.34D) {
            return 0xFFFF3333;
        }

        if (status != InjuryStatus.NONE || ratio <= 0.70D) {
            return 0xFFFFFF44;
        }

        return 0xFF55FF55;
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
        BodyPart clickedPart = getClickedBodyPart(mouseX, mouseY);

        if (clickedPart != null) {
            selectedPart = clickedPart;
            ClientSurgeryState.setSelectedBodyPart(clickedPart);
            return true;
        }

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

    private record BodyRegion(BodyPart part, int x, int y, int width, int height) {
    }
}