package me.xavi.vitalis.client.renderer;

import me.xavi.vitalis.client.ClientMedicalState;
import me.xavi.vitalis.client.ClientSurgeryState;
import me.xavi.vitalis.medical.BloodLevel;
import me.xavi.vitalis.medical.BodyPart;
import me.xavi.vitalis.medical.InjuryStatus;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class MedicalStatusHud {

    private static final int LINE_HEIGHT = 10;
    private static final int MARGIN_RIGHT = 8;
    private static final int MARGIN_TOP = 8;

    private static final int COLOR_HEALTHY = 0xFF55FF55;
    private static final int COLOR_INJURED = 0xFFFFFF55;
    private static final int COLOR_CRITICAL = 0xFFFF5555;
    private static final int COLOR_HEADER = 0xFFFFFFFF;

    public static void register() {
        HudRenderCallback.EVENT.register(MedicalStatusHud::render);
    }

    private static void render(GuiGraphics graphics, DeltaTracker tickCounter) {
        if (ClientSurgeryState.isActive()) {
            return;
        }
        Minecraft client = Minecraft.getInstance();

        if (client.player == null || client.options.hideGui) {
            return;
        }

        Font font = client.font;
        int screenWidth = graphics.guiWidth();

        int y = MARGIN_TOP;

        drawRightAligned(graphics, font, "Egészségi állapot", screenWidth, y, COLOR_HEADER);
        y += LINE_HEIGHT;

        for (BodyPart part : BodyPart.VALUES) {
            int hp = ClientMedicalState.getHp(part);
            InjuryStatus status = ClientMedicalState.getStatus(part);

            String line;

            if (status == InjuryStatus.NONE) {
                line = String.format(
                        "%s: %d/%d",
                        part.getDisplayName(),
                        hp,
                        part.getMaxHp()
                );
            } else {
                line = String.format(
                        "%s: %d/%d (%s)",
                        part.getDisplayName(),
                        hp,
                        part.getMaxHp(),
                        status.getDisplayName()
                );
            }

            double ratio = (double) hp / part.getMaxHp();

            int color;

            if (ratio <= 0.34D || status == InjuryStatus.OPEN_FRACTURE || status == InjuryStatus.BULLET_WOUND) {
                color = COLOR_CRITICAL;
            } else if (ratio <= 0.7D || status != InjuryStatus.NONE) {
                color = COLOR_INJURED;
            } else {
                color = COLOR_HEALTHY;
            }

            drawRightAligned(graphics, font, line, screenWidth, y, color);
            y += LINE_HEIGHT;
        }

        double bloodMl = ClientMedicalState.getBloodMl();
        BloodLevel bloodLevel = ClientMedicalState.getBloodLevel();

        String bloodLine = String.format(
                "Vér: %.0f/%.0f ml (%s)",
                bloodMl,
                BloodLevel.MAX_BLOOD_ML,
                bloodLevel.getDisplayName()
        );

        int bloodColor = switch (bloodLevel) {
            case NORMAL -> COLOR_HEALTHY;
            case WEAK, DIZZY -> COLOR_INJURED;
            case UNCONSCIOUS, DEAD -> COLOR_CRITICAL;
        };

        drawRightAligned(graphics, font, bloodLine, screenWidth, y, bloodColor);
    }

    private static void drawRightAligned(
            GuiGraphics graphics,
            Font font,
            String text,
            int screenWidth,
            int y,
            int color
    ) {
        int width = font.width(text);
        int x = screenWidth - MARGIN_RIGHT - width;

        graphics.drawString(
                font,
                text,
                x,
                y,
                color,
                true
        );
    }
}