package me.xavi.vitalis.client.renderer;

import me.xavi.vitalis.client.ClientMedicalState;
import me.xavi.vitalis.medical.BloodLevel;
import me.xavi.vitalis.medical.BodyPart;
import me.xavi.vitalis.medical.InjuryStatus;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class MedicalEffectsOverlay {

    private static final int BLEED_RGB = 0x6E0A0A;
    private static final int DIZZY_RGB = 0x101018;

    public static void register() {
        HudRenderCallback.EVENT.register(MedicalEffectsOverlay::render);
    }

    private static void render(GuiGraphics graphics, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        int width = graphics.guiWidth();
        int height = graphics.guiHeight();

        float partialTick = tickCounter.getGameTimeDeltaPartialTick(false);
        float time = (client.player.tickCount + partialTick) / 20.0F;

        renderBleeding(graphics, width, height, time);
        renderDizziness(graphics, width, height, time);
    }

    private static void renderBleeding(GuiGraphics graphics, int width, int height, float time) {
        double bleedRate = 0.0D;

        for (BodyPart part : BodyPart.VALUES) {
            InjuryStatus status = ClientMedicalState.getStatus(part);
            bleedRate += status.getBleedRateMlPerSecond();
        }

        if (bleedRate <= 0.0D) return;

        float intensity = (float) Math.min(0.6D, 0.25D + bleedRate / 200.0D);
        float pulse = 0.85F + 0.15F * (float) Math.sin(time * 3.0F);
        float strength = intensity * pulse;

        drawVignette(graphics, width, height, BLEED_RGB, strength);
    }

    private static void renderDizziness(GuiGraphics graphics, int width, int height, float time) {
        BloodLevel level = ClientMedicalState.getBloodLevel();

        float baseAlpha;

        switch (level) {
            case DIZZY -> baseAlpha = 0.22F;
            case UNCONSCIOUS -> baseAlpha = 0.55F;
            default -> {
                return;
            }
        }

        float pulse = 0.80F + 0.20F * (float) Math.sin(time * 1.6F);
        float alpha = baseAlpha * pulse;

        graphics.fill(0, 0, width, height, withAlpha(DIZZY_RGB, alpha));
        drawVignette(graphics, width, height, DIZZY_RGB, alpha * 0.8F);
    }

    private static void drawVignette(GuiGraphics graphics, int width, int height, int rgb, float strength) {
        int bands = 12;
        int marginX = Math.max(8, width / 6);
        int marginY = Math.max(8, height / 6);

        for (int i = 0; i < bands; i++) {
            float t = 1.0F - (float) i / bands;
            float alpha = strength * t * t;

            if (alpha <= 0.002F) continue;

            int color = withAlpha(rgb, alpha);

            int insetX = marginX * i / bands;
            int insetY = marginY * i / bands;

            int bandThicknessX = Math.max(1, marginX / bands);
            int bandThicknessY = Math.max(1, marginY / bands);

            graphics.fill(0, insetY, width, insetY + bandThicknessY, color);
            graphics.fill(0, height - insetY - bandThicknessY, width, height - insetY, color);
            graphics.fill(insetX, 0, insetX + bandThicknessX, height, color);
            graphics.fill(width - insetX - bandThicknessX, 0, width - insetX, height, color);
        }
    }

    private static int withAlpha(int rgb, float alpha) {
        int a = Math.max(0, Math.min(255, Math.round(alpha * 255.0F)));
        return (a << 24) | (rgb & 0xFFFFFF);
    }
}