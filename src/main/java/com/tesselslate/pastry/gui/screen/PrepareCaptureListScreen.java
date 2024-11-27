package com.tesselslate.pastry.gui.screen;

import com.tesselslate.pastry.Pastry;
import com.tesselslate.pastry.gui.toast.ErrorToast;
import com.tesselslate.pastry.task.Exceptional;
import com.tesselslate.pastry.task.ListCapturesTask;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import java.util.Collections;
import java.util.concurrent.ForkJoinTask;

public class PrepareCaptureListScreen extends TaskProgressScreen {
    private final ForkJoinTask<Exceptional<ListCapturesTask.Result>> task;

    public PrepareCaptureListScreen(Screen parent) {
        super(parent);

        this.task = Pastry.TASK_POOL.submit(new ListCapturesTask());
    }

    @Override
    public void cancel() {
        this.task.cancel(true);
    }

    @Override
    public String getProgressText() {
        return "Finding captures...";
    }

    @Override
    public Exceptional<Screen> poll() {
        Exceptional<ListCapturesTask.Result> result = this.task.getRawResult();
        if (result == null) {
            return null;
        }

        if (result.hasError()) {
            Exception error = result.getError();
            Pastry.LOGGER.error("Failed to list captures: " + error);

            return new Exceptional<>(error);
        } else {
            assert result.hasValue();

            return new Exceptional<>(this.finish(result.getValue()));
        }
    }

    private Screen finish(ListCapturesTask.Result result) {
        if (result.exceptions.size() > 0) {
            String error = String.format("%d captures not included", result.exceptions.size());

            MinecraftClient client = MinecraftClient.getInstance();
            client.getToastManager().add(new ErrorToast(error));

            Pastry.LOGGER.error("Failed to process " + result.exceptions.size() + " captures");
            result.exceptions.forEach((file, exception) -> {
                Pastry.LOGGER.error(file.getName() + ": " + exception);
            });
        }

        Collections.sort(result.entries, (a, b) -> b.header.recordedAt.compareTo(a.header.recordedAt));

        return new CaptureListScreen(this.parent, result);
    }
}
