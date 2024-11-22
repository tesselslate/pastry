package com.tesselslate.pastry.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

public abstract class ScreenExtended extends Screen {
    protected final Screen parent;

    protected ScreenExtended(Screen parent, Text title) {
        super(title);

        this.parent = parent;
    }

    @Override
    public void onClose() {
        this.client.openScreen(this.parent);

        if (this.parent == null || this.parent instanceof TitleScreen) {
            this.client.getToastManager().clear();
        }
    }

    protected ButtonWidget createDoneButton(int x, int y, int width, int height) {
        return new ButtonWidget(x, y, width, height, ScreenTexts.DONE, button -> {
            this.client.openScreen(this.parent);

            if (this.parent == null || this.parent instanceof TitleScreen) {
                this.client.getToastManager().clear();
            }
        });
    }

    protected void drawProgressText(MatrixStack matrices, String text) {
        String progressString = getProgressString();
        int x = (this.width / 2) - (this.textRenderer.getWidth(progressString) / 2);
        int y = this.height / 2;
        this.textRenderer.draw(matrices, progressString, x, y, Formatting.GRAY.getColorValue());

        x = (this.width / 2) - (this.textRenderer.getWidth(text) / 2);
        y = (this.height / 2) - (this.textRenderer.fontHeight + 1);
        this.textRenderer.draw(matrices, text, x, y, Formatting.WHITE.getColorValue());
    }

    protected static String getProgressString() {
        switch ((int) (Util.getMeasuringTimeMs() / 300L % 4L)) {
            case 1:
                return "o O o";
            case 2:
                return "o o O";
            case 3:
                return "o O o";
            default:
                return "O o o";
        }
    }
}
