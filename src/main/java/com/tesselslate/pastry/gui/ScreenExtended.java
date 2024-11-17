package com.tesselslate.pastry.gui;

import java.util.List;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.StringRenderable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

public abstract class ScreenExtended extends Screen {
    protected final Screen parent;

    protected ScreenExtended(Screen parent, Text title) {
        super(title);

        this.parent = parent;
    }

    protected ButtonWidget createDoneButton() {
        return new ButtonWidget(this.width / 2 - 100, this.height - 27, 200, 20, ScreenTexts.DONE, button -> {
            this.client.openScreen(this.parent);
        });
    }

    protected void drawCenteredStringWrapping(MatrixStack matrices, String text, int color) {
        List<StringRenderable> lines = this.textRenderer.wrapLines(StringRenderable.plain(text), this.width / 2);

        int height = lines.size() * (this.textRenderer.fontHeight + 1);
        for (int row = 0; row < lines.size(); row++) {
            int x = (this.width / 2) - (this.textRenderer.getWidth(lines.get(row)) / 2);
            int y = (this.height / 2) - (height / 2) + (row * (this.textRenderer.fontHeight + 1));

            this.textRenderer.draw(matrices, lines.get(row), x, y, color);
        }
    }

    protected void drawProgressText(MatrixStack matrices, String text) {
        String progressString = getProgressString();
        int x = (this.width / 2) - (this.textRenderer.getWidth(progressString) / 2);
        int y = (this.height / 2) - (this.textRenderer.fontHeight + 1);
        this.textRenderer.draw(matrices, progressString, x, y, Formatting.GRAY.getColorValue());

        x = (this.width / 2) - (this.textRenderer.getWidth(text) / 2);
        y = this.height / 2;
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