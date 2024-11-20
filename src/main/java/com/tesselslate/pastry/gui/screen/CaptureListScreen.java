package com.tesselslate.pastry.gui.screen;

import java.util.Collections;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import com.tesselslate.pastry.Pastry;
import com.tesselslate.pastry.gui.ScreenExtended;
import com.tesselslate.pastry.gui.toast.ErrorToast;
import com.tesselslate.pastry.gui.widget.CaptureListWidget;
import com.tesselslate.pastry.task.ListCapturesTask;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public class CaptureListScreen extends ScreenExtended {
    private ListCapturesTask.Result result;
    private ForkJoinTask<ListCapturesTask.Result> task;

    private CaptureListWidget captureList;
    private ButtonWidget doneButton;
    private LiteralText subtitle;

    public CaptureListScreen(Screen parent) {
        super(parent, new LiteralText("Pastry Captures"));

        this.task = Pastry.TASK_POOL.submit(new ListCapturesTask());
    }

    @Override
    public void onClose() {
        super.onClose();

        if (this.task != null) {
            this.task.cancel(true);
        }
    }

    @Override
    protected void init() {
        if (this.result == null) {
            return;
        }

        this.captureList = this.addChild(
                new CaptureListWidget(this.result.entries, this, this.client, this.width, this.height, 32,
                        this.height - 32, this.captureList != null ? this.captureList.getPage() : 0));
        this.addButton(this.captureList.createNextPageButton(this.width / 2 + 36, this.height - 27, 20, 20));
        this.addButton(this.captureList.createPrevPageButton(this.width / 2 - 56, this.height - 27, 20, 20));

        this.doneButton = this.addButton(this.createDoneButton(this.width / 2 - 32, this.height - 27, 64, 20));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        if (this.task != null) {
            this.drawProgressText(matrices, "Loading captures...");
        } else if (this.captureList != null) {
            this.renderWidgets(matrices, mouseX, mouseY, delta);
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void tick() {
        if (this.task == null) {
            return;
        }

        ListCapturesTask.Result result = this.task.getRawResult();
        if (result == null) {
            return;
        }
        this.task = null;

        this.processTaskResult(result);
    }

    private void processTaskResult(ListCapturesTask.Result result) {
        Collections.sort(result.entries, (a, b) -> b.header.recordedAt.compareTo(a.header.recordedAt));

        this.result = result;
        this.init();

        int entryCount = this.result.entries.size();
        this.subtitle = new LiteralText(String.format("%d %s (%s)", entryCount,
                entryCount != 1 ? "captures" : "capture", FileUtils.byteCountToDisplaySize(this.result.entries.stream()
                        .map(entry -> entry.size).collect(Collectors.summingLong(Long::longValue)))));

        if (result.exceptions.size() > 0) {
            String error = String.format("%d captures not included", result.exceptions.size());
            this.client.getToastManager().add(new ErrorToast(error));

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
}