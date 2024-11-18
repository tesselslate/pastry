package com.tesselslate.pastry.gui;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.ForkJoinTask;

import com.tesselslate.pastry.Pastry;
import com.tesselslate.pastry.capture.PastryCapture;
import com.tesselslate.pastry.capture.PastryCaptureHeader;
import com.tesselslate.pastry.task.ReadCaptureTask;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public class CaptureOverviewScreen extends ScreenExtended {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private PastryCapture capture;
    private Exception taskException;
    private ForkJoinTask<ReadCaptureTask.Result> task;

    public CaptureOverviewScreen(Screen parent, File file, PastryCaptureHeader header) {
        super(parent, new LiteralText("Overview of " + DATE_FORMAT.format(header.recordedAt)));

        this.task = Pastry.TASK_POOL.submit(new ReadCaptureTask(file));
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
        if (this.capture == null) {
            return;
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        if (this.taskException != null) {
            this.drawCenteredStringWrapping(matrices, "Failed to read capture: " + this.taskException,
                    Formatting.RED.getColorValue());
        } else if (this.task != null) {
            this.drawProgressText(matrices, "Reading capture...");
        } else if (this.capture != null) {
            this.renderWidgets(matrices, mouseX, mouseY, delta);
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void tick() {
        if (this.task == null) {
            return;
        }

        ReadCaptureTask.Result result = this.task.getRawResult();
        if (result == null) {
            return;
        }
        this.task = null;

        if (result.exception != null) {
            this.taskException = result.exception;
            return;
        }

        this.processTaskResult(result.capture);
    }

    private void processTaskResult(PastryCapture capture) {

    }

    private void renderWidgets(MatrixStack matrices, int mouseX, int mouseY, float delta) {

    }
}
