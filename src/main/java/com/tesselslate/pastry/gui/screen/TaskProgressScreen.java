package com.tesselslate.pastry.gui.screen;

import com.tesselslate.pastry.gui.ScreenExtended;
import com.tesselslate.pastry.gui.toast.ErrorToast;
import com.tesselslate.pastry.task.Exceptional;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

public abstract class TaskProgressScreen extends ScreenExtended {
    public TaskProgressScreen(Screen parent) {
        super(parent, new LiteralText("Task Progress"));
    }

    public abstract void cancel();

    public abstract String getProgressText();

    public abstract Exceptional<Screen> poll();

    @Override
    public void onClose() {
        this.cancel();

        super.onClose();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        String progressText = this.getProgressText();
        String progressBar = getProgressString();
        int x = (this.width / 2) - (this.textRenderer.getWidth(progressBar) / 2);
        int y = this.height / 2;
        this.textRenderer.draw(matrices, progressBar, x, y, Formatting.GRAY.getColorValue());

        x = (this.width / 2) - (this.textRenderer.getWidth(progressText) / 2);
        y = (this.height / 2) - (this.textRenderer.fontHeight + 1);
        this.textRenderer.draw(matrices, progressText, x, y, Formatting.WHITE.getColorValue());

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void tick() {
        Exceptional<Screen> result = this.poll();
        if (result == null) {
            return;
        }

        if (result.hasValue()) {
            this.client.openScreen(result.getValue());
        } else {
            assert result.hasError();

            this.client.openScreen(this.parent);
            this.client.getToastManager().add(new ErrorToast("Failed to run task"));
        }

        super.tick();
    }

    private static String getProgressString() {
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
