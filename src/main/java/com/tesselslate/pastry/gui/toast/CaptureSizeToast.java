package com.tesselslate.pastry.gui.toast;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Formatting;

public class CaptureSizeToast implements Toast {
    private static final String TOAST_TITLE = "Capture size too large";
    private static final String TOAST_DESCRIPTION = "Recording stopped";
    private static final long TIMEOUT = 4000;

    private long startTime = Long.MIN_VALUE;

    @Override
    public Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
        if (this.startTime == Long.MIN_VALUE) {
            this.startTime = startTime;
        }

        MinecraftClient client = manager.getGame();
        TextRenderer textRenderer = client.textRenderer;

        client.getTextureManager().bindTexture(TOASTS_TEX);
        manager.drawTexture(matrices, 0, 0, 0, 0, this.getWidth(), this.getHeight());

        textRenderer.draw(matrices, TOAST_TITLE, 7.0f, 7.0f, Formatting.YELLOW.getColorValue());
        textRenderer.draw(matrices, TOAST_DESCRIPTION, 7.0f, 18.0f, Formatting.WHITE.getColorValue());

        return (startTime - this.startTime) > TIMEOUT ? Visibility.HIDE : Visibility.SHOW;
    }
}
