package me.xavi.vitalis.client.renderer;

import me.xavi.vitalis.client.ClientMedicalState;
import me.xavi.vitalis.medical.BloodLevel;
import me.xavi.vitalis.medical.BodyPart;
import me.xavi.vitalis.medical.InjuryStatus;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer;

/**
 * Renders a fixed-text status panel in the top-right corner of the screen,
 * always visible, showing each body part's HP/condition and the player's
 * overall blood level. Plain text for now; can be replaced with a more
 * polished/iconified layout later.
 */
public class MedicalStatusHud {

    private static final int LINE_HEIGHT = 10;
    private static final int MARGIN_RIGHT = 8;
    private static final int MARGIN_TOP = 8;

    private static final int COLOR_HEALTHY = 0x55FF55;
    private static final int COLOR_INJURED = 0xFFFF55;
    private static final int COLOR_CRITICAL = 0xFF5555;
    private static final int COLOR_HEADER = 0xFFFFFF;

    public static void register() {
        HudRenderCallback.EVENT.register(MedicalStatusHud::render);
    }

    private static void render(DrawContext context, net.minecraft.client.render.RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;

        TextRenderer textRenderer = client.textRenderer;
        int screenWidth = context.getScaledWindowWidth();

        int y = MARGIN_TOP;

        // Header.
        drawRightAligned(context, textRenderer, "Egészségi állapot", screenWidth, y, COLOR_HEADER);
        y += LINE_HEIGHT;

        for (BodyPart part : BodyPart.VALUES) {
            int hp = ClientMedicalState.getHp(part);
            InjuryStatus status = ClientMedicalState.getStatus(part);

            String line;
            if (status == InjuryStatus.NONE) {
                line = String.format("%s: %d/%d", part.getDisplayName(), hp, part.getMaxHp());
            } else {
                line = String.format("%s: %d/%d (%s)", part.getDisplayName(), hp, part.getMaxHp(), status.getDisplayName());
            }

            int color;
            double ratio = (double) hp / part.getMaxHp();
            if (ratio <= 0.34 || status == InjuryStatus.OPEN_FRACTURE || status == InjuryStatus.BULLET_WOUND) {
                color = COLOR_CRITICAL;
            } else if (ratio <= 0.7 || status != InjuryStatus.NONE) {
                color = COLOR_INJURED;
            } else {
                color = COLOR_HEALTHY;
            }

            drawRightAligned(context, textRenderer, line, screenWidth, y, color);
            y += LINE_HEIGHT;
        }

        // Blood level line.
        double bloodMl = ClientMedicalState.getBloodMl();
        BloodLevel bloodLevel = ClientMedicalState.getBloodLevel();
        String bloodLine = String.format("Vér: %.0f/%.0f ml (%s)", bloodMl, BloodLevel.MAX_BLOOD_ML, bloodLevel.getDisplayName());

        int bloodColor = switch (bloodLevel) {
            case NORMAL -> COLOR_HEALTHY;
            case WEAK, DIZZY -> COLOR_INJURED;
            case UNCONSCIOUS, DEAD -> COLOR_CRITICAL;
        };

        drawRightAligned(context, textRenderer, bloodLine, screenWidth, y, bloodColor);
    }

    private static void drawRightAligned(DrawContext context, TextRenderer textRenderer, String text, int screenWidth, int y, int color) {
        int width = textRenderer.getWidth(text);
        int x = screenWidth - MARGIN_RIGHT - width;
        context.drawText(textRenderer, text, x, y, color, true);
        System.out.println("Width: "+ width);
        System.out.println("X: "+ x);
    }
}
