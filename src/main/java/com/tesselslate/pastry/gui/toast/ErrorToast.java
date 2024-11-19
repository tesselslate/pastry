package com.tesselslate.pastry.gui.toast;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Formatting;

public class ErrorToast implements Toast {
    private static final String TOAST_TEXT = "See latest.log for more info";
    private static final long DEFAULT_TIMEOUT = 4000;

    private final String title;
    private final String description;

    private final long timeout;
    private long startTime = Long.MIN_VALUE;

    public ErrorToast(String title) {
        this(title, TOAST_TEXT, DEFAULT_TIMEOUT);
    }

    public ErrorToast(String title, String description) {
        this(title, description, DEFAULT_TIMEOUT);
    }

    public ErrorToast(String title, String description, long timeout) {
        this.title = title;
        this.description = description;

        this.timeout = timeout;
    }

    @Override
    public Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
        if (this.startTime == Long.MIN_VALUE) {
            this.startTime = startTime;
        }

        MinecraftClient client = manager.getGame();
        TextRenderer textRenderer = client.textRenderer;

        client.getTextureManager().bindTexture(TOASTS_TEX);
        manager.drawTexture(matrices, 0, 0, 0, 0, this.getWidth(), this.getHeight());

        textRenderer.draw(matrices, this.title, 7.0f, 7.0f, Formatting.RED.getColorValue());
        textRenderer.draw(matrices, this.description, 7.0f, 18.0f, Formatting.WHITE.getColorValue());

        return (startTime - this.startTime) > this.timeout ? Visibility.HIDE : Visibility.SHOW;
    }
}
