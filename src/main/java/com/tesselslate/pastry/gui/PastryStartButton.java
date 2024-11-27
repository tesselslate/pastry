package com.tesselslate.pastry.gui;

import com.tesselslate.pastry.capture.PastryCaptureManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.StringRenderable;
import net.minecraft.util.Identifier;

public class PastryStartButton extends IconButton {
    private static final Identifier STOP_TEXTURE = new Identifier("pastry", "textures/gui/stop.png");
    private static final Identifier RESTART_TEXTURE = new Identifier("pastry", "textures/gui/restart.png");

    public PastryStartButton(Screen screen, int x, int y) {
        super(screen, x, y, null, PastryStartButton::onPress, PastryStartButton::onTooltip);

        this.texture = PastryCaptureManager.isCapturing() ? STOP_TEXTURE : RESTART_TEXTURE;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.texture = PastryCaptureManager.isCapturing() ? STOP_TEXTURE : RESTART_TEXTURE;

        super.renderButton(matrices, mouseX, mouseY, delta);
    }

    private static void onPress(ButtonWidget button) {
        PastryCaptureManager.runLocked(capture -> {
            if (capture != null) {
                PastryCaptureManager.stopCapture();
            } else {
                PastryCaptureManager.startCapture();
            }
        });
    }

    private static void onTooltip(ButtonWidget button, MatrixStack matrices, int mouseX, int mouseY) {
        PastryStartButton instance = (PastryStartButton) button;

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        String tooltip = PastryCaptureManager.isCapturing() ? "Stop Capture" : "Restart Capture";
        instance.screen.renderTooltip(
                matrices,
                StringRenderable.plain(tooltip),
                instance.x - 20 - textRenderer.getWidth(tooltip),
                instance.y + 22 - (textRenderer.fontHeight / 2));
    }
}
