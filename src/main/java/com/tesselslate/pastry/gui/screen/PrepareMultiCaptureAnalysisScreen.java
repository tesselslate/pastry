package com.tesselslate.pastry.gui.screen;

import com.tesselslate.pastry.Pastry;
import com.tesselslate.pastry.gui.toast.ErrorToast;
import com.tesselslate.pastry.task.AnalyzeCapturesTask;
import com.tesselslate.pastry.task.Exceptional;
import com.tesselslate.pastry.task.ListCapturesTask;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import java.util.List;

public class PrepareMultiCaptureAnalysisScreen extends TaskProgressScreen {
    private final AnalyzeCapturesTask task;

    private final int numCaptures;

    public PrepareMultiCaptureAnalysisScreen(Screen parent, List<ListCapturesTask.Entry> captures) {
        super(parent);

        this.task = new AnalyzeCapturesTask(captures);
        Pastry.TASK_POOL.submit(this.task);

        this.numCaptures = captures.size();
    }

    @Override
    public void cancel() {
        this.task.cancel(true);
    }

    @Override
    public String getProgressText() {
        return String.format("Processing... (%d/%d captures)", this.task.getProgress(), this.numCaptures);
    }

    @Override
    public Exceptional<Screen> poll() {
        Exceptional<AnalyzeCapturesTask.Result> result = this.task.getRawResult();
        if (result == null) {
            return null;
        }

        if (result.hasError()) {
            Exception error = result.getError();
            Pastry.LOGGER.error("Failed to analyze captures: " + error);

            return new Exceptional<>(error);
        } else {
            assert result.hasValue();

            return new Exceptional<>(this.finish(result.getValue()));
        }
    }

    private Screen finish(AnalyzeCapturesTask.Result result) {
        if (result.exceptions.size() > 0) {
            String error = String.format("%d captures not included", result.exceptions.size());

            MinecraftClient client = MinecraftClient.getInstance();
            client.getToastManager().add(new ErrorToast(error));

            Pastry.LOGGER.error("Failed to process " + result.exceptions.size() + " captures");
            result.exceptions.forEach((file, exception) -> {
                Pastry.LOGGER.error(file.getName() + ": " + exception);
            });
        }

        return new CaptureAnalysisScreen(this.parent, result.analysis);
    }
}
