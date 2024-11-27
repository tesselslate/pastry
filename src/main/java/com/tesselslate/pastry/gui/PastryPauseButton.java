package com.tesselslate.pastry.gui;

import com.tesselslate.pastry.capture.PastryCaptureManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.StringRenderable;
import net.minecraft.util.Identifier;

public class PastryPauseButton extends IconButton {
    private static final Identifier PAUSE_TEXTURE = new Identifier("pastry", "textures/gui/pause.png");
    private static final Identifier UNPAUSE_TEXTURE = new Identifier("pastry", "textures/gui/play.png");

    public PastryPauseButton(Screen screen, int x, int y) {
        super(screen, x, y, null, PastryPauseButton::onPress, PastryPauseButton::onTooltip);

        this.texture = PastryCaptureManager.isPaused() ? UNPAUSE_TEXTURE : PAUSE_TEXTURE;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.active = PastryCaptureManager.isCapturing();
        this.texture = PastryCaptureManager.isPaused() ? UNPAUSE_TEXTURE : PAUSE_TEXTURE;

        super.renderButton(matrices, mouseX, mouseY, delta);
    }

    private static void onPress(ButtonWidget button) {
        PastryCaptureManager.runLocked(capture -> {
            PastryCaptureManager.setPaused(!PastryCaptureManager.isPaused());
        });
    }

    private static void onTooltip(ButtonWidget button, MatrixStack matrices, int mouseX, int mouseY) {
        PastryPauseButton instance = (PastryPauseButton) button;

        if (!instance.active) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        String tooltip = PastryCaptureManager.isPaused() ? "Unpause Capture" : "Pause Capture";
        instance.screen.renderTooltip(
                matrices,
                StringRenderable.plain(tooltip),
                instance.x - 20 - textRenderer.getWidth(tooltip),
                instance.y + 22 - (textRenderer.fontHeight / 2));
    }
}
