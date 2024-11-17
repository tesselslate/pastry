package com.tesselslate.pastry.gui;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import com.tesselslate.pastry.Pastry;
import com.tesselslate.pastry.gui.widget.CaptureListWidget;
import com.tesselslate.pastry.task.ListCapturesTask;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public class PastryCapturesScreen extends ScreenExtended {
    private ListCapturesTask.Result captureTaskResult;
    private CompletableFuture<ListCapturesTask.Result> captureTask;
    private Exception captureTaskError;

    private CaptureListWidget captureList;
    private ButtonWidget doneButton;
    private LiteralText subtitle;

    public PastryCapturesScreen(Screen parent) {
        super(parent, new LiteralText("Pastry Captures"));

        this.captureTask = CompletableFuture.supplyAsync(ListCapturesTask::run, Pastry.EXECUTOR);
    }

    @Override
    public void onClose() {
        this.client.openScreen(this.parent);

        if (this.captureTask != null) {
            this.captureTask.cancel(true);
        }

        this.client.getToastManager().clear();
    }

    @Override
    protected void init() {
        if (this.captureTaskResult == null) {
            return;
        }

        this.captureList = this.addChild(
                new CaptureListWidget(this.captureTaskResult.entries, this, this.client, this.width, this.height, 32,
                        this.height - 32, this.captureList != null ? this.captureList.getPage() : 0));
        this.addButton(this.captureList.createNextPageButton(this.width / 2 + 36, this.height - 27, 20, 20));
        this.addButton(this.captureList.createPrevPageButton(this.width / 2 - 56, this.height - 27, 20, 20));

        this.doneButton = this.addButton(this.createDoneButton(this.width / 2 - 32, this.height - 27, 64, 20));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        if (this.captureTaskError != null) {
            this.drawCenteredStringWrapping(matrices, "Failed to get captures: " + this.captureTaskError,
                    Formatting.RED.getColorValue());
        } else if (this.captureTask != null) {
            this.drawProgressText(matrices, "Loading captures...");
        } else if (this.captureList != null) {
            this.renderWidgets(matrices, mouseX, mouseY, delta);
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void tick() {
        if (this.captureTask == null) {
            return;
        }

        ListCapturesTask.Result result;
        try {
            result = this.captureTask.getNow(null);
            if (result == null) {
                return;
            }

            this.captureTask = null;
        } catch (Exception e) {
            this.captureTaskError = e;

            this.captureTask.cancel(true);
            this.captureTask = null;
            return;
        }

        this.processTaskResult(result);
    }

    private void processTaskResult(ListCapturesTask.Result result) {
        Collections.sort(result.entries, (a, b) -> b.header.recordedAt.compareTo(a.header.recordedAt));

        this.captureTaskResult = result;
        this.init();

        int entryCount = this.captureTaskResult.entries.size();
        this.subtitle = new LiteralText(String.format("%d %s (%s)", entryCount, entryCount > 1 ? "captures" : "capture",
                FileUtils.byteCountToDisplaySize(this.captureTaskResult.entries.stream().map(entry -> entry.size)
                        .collect(Collectors.summingLong(Long::longValue)))));

        if (result.exceptions.size() > 0) {
            this.client.getToastManager().add(new TaskErrorToast(result.exceptions.size()));

            Pastry.LOGGER.error("Failed to process " + result.exceptions.size() + " captures:");
            result.exceptions.forEach((file, exception) -> {
                Pastry.LOGGER.error(file.getName() + ": " + exception);
            });
        }
    }

    private void renderWidgets(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.captureList.render(matrices, mouseX, mouseY, delta);

        this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 5,
                Formatting.WHITE.getColorValue());
        this.drawCenteredText(matrices, this.textRenderer, this.subtitle, this.width / 2, 16,
                Formatting.GRAY.getColorValue());

        this.doneButton.render(matrices, mouseX, mouseY, delta);
    }

    private class TaskErrorToast implements Toast {
        private static final String TOAST_TEXT = "See latest.log for more info";

        private final int errors;

        private long startTime = Long.MIN_VALUE;

        public TaskErrorToast(int numErrors) {
            this.errors = numErrors;
        }

        @Override
        public Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
            if (this.startTime == Long.MIN_VALUE) {
                this.startTime = startTime;
            }

            @SuppressWarnings("resource")
            TextRenderer textRenderer = manager.getGame().textRenderer;

            manager.getGame().getTextureManager().bindTexture(TOASTS_TEX);
            manager.drawTexture(matrices, 0, 0, 0, 0, this.getWidth(), this.getHeight());

            String title = String.format("%d captures not included", this.errors);
            textRenderer.draw(matrices, title, 7.0f, 7.0f, Formatting.RED.getColorValue());
            textRenderer.draw(matrices, TOAST_TEXT, 7.0f, 18.0f, Formatting.WHITE.getColorValue());

            return (startTime - this.startTime) > 4000 ? Visibility.HIDE : Visibility.SHOW;
        }
    }
}
