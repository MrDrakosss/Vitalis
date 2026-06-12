package me.xavi.vitalis.client.screen;

import me.xavi.vitalis.client.state.ClientDownedState;
import me.xavi.vitalis.network.DownedGiveUpPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class DownedScreen extends Screen {

    public DownedScreen() {
        super(Component.translatable("screen.vitalis.downed"));
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(
                Component.translatable("screen.vitalis.give_up"),
                button -> ClientPlayNetworking.send(new DownedGiveUpPayload())
        ).bounds((this.width - 110) / 2, this.height - 54, 110, 20).build());
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0x66000000);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int centerY = this.height / 2 - 30;

        graphics.drawCenteredString(
                this.font,
                Component.translatable("screen.vitalis.downed"),
                centerX,
                centerY,
                0xFFFF5555
        );

        graphics.drawCenteredString(
                this.font,
                Component.translatable("screen.vitalis.revive_time")
                        .append(": ")
                        .append(String.valueOf(ClientDownedState.getRemainingSeconds()))
                        .append("s"),
                centerX,
                centerY + 16,
                0xFFFFFFFF
        );
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}