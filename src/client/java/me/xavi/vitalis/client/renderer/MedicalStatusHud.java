package me.xavi.vitalis.client.renderer;

import me.xavi.vitalis.client.state.ClientDownedState;
import me.xavi.vitalis.client.state.ClientMedicalState;
import me.xavi.vitalis.client.state.ClientSurgeryState;
import me.xavi.vitalis.medical.BloodLevel;
import me.xavi.vitalis.medical.BodyPart;
import me.xavi.vitalis.medical.InjuryStatus;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;

public class MedicalStatusHud {

    private static final int X = 8;
    private static final int Y = 8;

    private static final int SCALE = 3;
    private static final int OUTLINE = 1;

    public static void register() {
        HudRenderCallback.EVENT.register(MedicalStatusHud::render);
    }

    private static void render(GuiGraphics graphics, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();

        if (client.player == null || client.options.hideGui) {
            return;
        }

        if (ClientSurgeryState.isActive()) {
            return;
        }
        if (ClientDownedState.isActive()) {
            return;
        }
        renderPlayerSkinPreview(graphics, client.player);
        renderBloodAndCondition(graphics);
    }

    private static void renderPlayerSkinPreview(GuiGraphics graphics, AbstractClientPlayer player) {
        PlayerSkin skin = player.getSkin();
        ResourceLocation texture = skin.texture();

        boolean slim = skin.model().id().equals("slim");

        int headW = 8 * SCALE;
        int headH = 8 * SCALE;

        int bodyW = 8 * SCALE;
        int bodyH = 12 * SCALE;

        int armW = (slim ? 3 : 4) * SCALE;
        int armH = 12 * SCALE;

        int legW = 4 * SCALE;
        int legH = 12 * SCALE;

        int totalW = armW + OUTLINE + bodyW + OUTLINE + armW;
        int centerX = X + totalW / 2;

        int headX = centerX - headW / 2;
        int headY = Y;

        int bodyX = centerX - bodyW / 2;
        int bodyY = headY + headH + OUTLINE;

        int leftArmX = bodyX - armW - OUTLINE;
        int rightArmX = bodyX + bodyW + OUTLINE;

        int legY = bodyY + bodyH + OUTLINE;
        int leftLegX = centerX - legW - OUTLINE / 2;
        int rightLegX = centerX + OUTLINE / 2;

        drawOutlinedTintedSkinPart(graphics, texture, headX, headY, 8, 8, 8, 8, BodyPart.HEAD);
        drawTintedSkinPart(graphics, texture, headX, headY, 40, 8, 8, 8, BodyPart.HEAD);

        drawOutlinedTintedSkinPart(graphics, texture, bodyX, bodyY, 20, 20, 8, 12, BodyPart.CHEST);
        drawTintedSkinPart(graphics, texture, bodyX, bodyY, 20, 36, 8, 12, BodyPart.CHEST);

        if (slim) {
            drawOutlinedTintedSkinPart(graphics, texture, leftArmX, bodyY, 44, 20, 3, 12, BodyPart.LEFT_ARM);
            drawTintedSkinPart(graphics, texture, leftArmX, bodyY, 44, 36, 3, 12, BodyPart.LEFT_ARM);

            drawOutlinedTintedSkinPart(graphics, texture, rightArmX, bodyY, 36, 52, 3, 12, BodyPart.RIGHT_ARM);
            drawTintedSkinPart(graphics, texture, rightArmX, bodyY, 52, 52, 3, 12, BodyPart.RIGHT_ARM);
        } else {
            drawOutlinedTintedSkinPart(graphics, texture, leftArmX, bodyY, 44, 20, 4, 12, BodyPart.LEFT_ARM);
            drawTintedSkinPart(graphics, texture, leftArmX, bodyY, 44, 36, 4, 12, BodyPart.LEFT_ARM);

            drawOutlinedTintedSkinPart(graphics, texture, rightArmX, bodyY, 36, 52, 4, 12, BodyPart.RIGHT_ARM);
            drawTintedSkinPart(graphics, texture, rightArmX, bodyY, 52, 52, 4, 12, BodyPart.RIGHT_ARM);
        }

        drawOutlinedTintedSkinPart(graphics, texture, leftLegX, legY, 4, 20, 4, 12, BodyPart.LEFT_LEG);
        drawTintedSkinPart(graphics, texture, leftLegX, legY, 4, 36, 4, 12, BodyPart.LEFT_LEG);

        drawOutlinedTintedSkinPart(graphics, texture, rightLegX, legY, 20, 52, 4, 12, BodyPart.RIGHT_LEG);
        drawTintedSkinPart(graphics, texture, rightLegX, legY, 4, 52, 4, 12, BodyPart.RIGHT_LEG);
    }

    private static void renderBloodAndCondition(GuiGraphics graphics) {
        Minecraft client = Minecraft.getInstance();

        double bloodPercent = ClientMedicalState.getBloodMl() / BloodLevel.MAX_BLOOD_ML * 100.0D;
        bloodPercent = Math.max(0.0D, Math.min(100.0D, bloodPercent));

        String bloodText = String.format("Vér: %.0f%%", bloodPercent);
        String conditionText = getOverallConditionText();

        int textY = Y + 98;

        graphics.drawString(client.font, bloodText, X, textY, bloodColor(), true);
        graphics.drawString(client.font, conditionText, X, textY + 11, conditionColor(), true);

        graphics.drawString(
                client.font,
                "Pulzus: " + ClientMedicalState.getHeartRate() + " BPM",
                X,
                textY + 22,
                0xFFFFFFFF,
                true
        );

        graphics.drawString(
                client.font,
                "Vérnyomás: "
                        + ClientMedicalState.getBloodPressureSystolic()
                        + "/"
                        + ClientMedicalState.getBloodPressureDiastolic(),
                X,
                textY + 33,
                0xFFFFFFFF,
                true
        );
    }

    private static void drawOutlinedTintedSkinPart(
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
        int renderedWidth = width * SCALE;
        int renderedHeight = height * SCALE;

        drawOutline(graphics, x, y, renderedWidth, renderedHeight, bodyPartLineColor(part));
        drawTintedSkinPart(graphics, texture, x, y, u, v, width, height, part);
    }

    private static void drawTintedSkinPart(
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
        drawSkinPart(graphics, texture, x, y, u, v, width, height);

        int tint = bodyPartTint(part);

        if (tint != 0) {
            graphics.fill(
                    x,
                    y,
                    x + width * SCALE,
                    y + height * SCALE,
                    tint
            );
        }
    }

    private static void drawSkinPart(
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
                width * SCALE,
                height * SCALE,
                u,
                v,
                width,
                height,
                64,
                64
        );
    }

    private static void drawOutline(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            int color
    ) {
        graphics.fill(x - OUTLINE, y - OUTLINE, x + width + OUTLINE, y, color);
        graphics.fill(x - OUTLINE, y + height, x + width + OUTLINE, y + height + OUTLINE, color);
        graphics.fill(x - OUTLINE, y, x, y + height, color);
        graphics.fill(x + width, y, x + width + OUTLINE, y + height, color);
    }

    private static int bodyPartLineColor(BodyPart part) {
        int hp = ClientMedicalState.getHp(part);
        int maxHp = part.getMaxHp();
        InjuryStatus status = ClientMedicalState.getStatus(part);

        if (hp <= 0) {
            return 0xFF777777;
        }

        double ratio = maxHp <= 0 ? 0.0D : (double) hp / (double) maxHp;

        if (status == InjuryStatus.OPEN_FRACTURE
                || status == InjuryStatus.BULLET_WOUND
                || ratio <= 0.34D) {
            return 0xFFFF3333;
        }

        if (status != InjuryStatus.NONE || ratio <= 0.70D) {
            return 0xFFFFFF44;
        }

        return 0xFF55FF55;
    }

    private static int bodyPartTint(BodyPart part) {
        int hp = ClientMedicalState.getHp(part);
        int maxHp = part.getMaxHp();
        InjuryStatus status = ClientMedicalState.getStatus(part);

        if (hp <= 0) {
            return 0x88777777;
        }

        double ratio = maxHp <= 0 ? 0.0D : (double) hp / (double) maxHp;
        ratio = Math.max(0.0D, Math.min(1.0D, ratio));

        if (status == InjuryStatus.NONE && ratio >= 0.95D) {
            return 0;
        }

        int alpha;
        int red;
        int green;
        int blue;

        if (ratio <= 0.34D
                || status == InjuryStatus.OPEN_FRACTURE
                || status == InjuryStatus.BULLET_WOUND) {
            alpha = lerpInt(80, 160, 1.0D - ratio);
            red = 255;
            green = 35;
            blue = 35;
        } else if (ratio <= 0.70D || status != InjuryStatus.NONE) {
            alpha = lerpInt(45, 115, 1.0D - ratio);
            red = 255;
            green = 210;
            blue = 35;
        } else {
            alpha = lerpInt(20, 55, 1.0D - ratio);
            red = 85;
            green = 255;
            blue = 85;
        }

        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private static String getOverallConditionText() {
        BloodLevel bloodLevel = ClientMedicalState.getBloodLevel();

        if (bloodLevel == BloodLevel.DEAD) {
            return "Állapot: halálos";
        }

        if (bloodLevel == BloodLevel.UNCONSCIOUS) {
            return "Állapot: eszméletlen";
        }

        boolean critical = false;
        boolean injured = false;

        for (BodyPart part : BodyPart.VALUES) {
            int hp = ClientMedicalState.getHp(part);
            int maxHp = part.getMaxHp();
            InjuryStatus status = ClientMedicalState.getStatus(part);

            double ratio = maxHp <= 0 ? 0.0D : (double) hp / (double) maxHp;

            if (hp <= 0
                    || ratio <= 0.34D
                    || status == InjuryStatus.OPEN_FRACTURE
                    || status == InjuryStatus.BULLET_WOUND) {
                critical = true;
            } else if (ratio <= 0.70D || status != InjuryStatus.NONE) {
                injured = true;
            }
        }

        if (critical) {
            return "Állapot: kritikus";
        }

        if (injured) {
            return "Állapot: sérült";
        }

        return "Állapot: stabil";
    }

    private static int conditionColor() {
        String condition = getOverallConditionText();

        if (condition.contains("kritikus")
                || condition.contains("eszméletlen")
                || condition.contains("halálos")) {
            return 0xFFFF5555;
        }

        if (condition.contains("sérült")) {
            return 0xFFFFFF55;
        }

        return 0xFF55FF55;
    }

    private static int bloodColor() {
        return switch (ClientMedicalState.getBloodLevel()) {
            case NORMAL -> 0xFF55FF55;
            case WEAK, DIZZY -> 0xFFFFFF55;
            case UNCONSCIOUS, DEAD -> 0xFFFF5555;
        };
    }

    private static int lerpInt(int min, int max, double t) {
        t = Math.max(0.0D, Math.min(1.0D, t));
        return (int) Math.round(min + (max - min) * t);
    }
}