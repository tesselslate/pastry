package com.tesselslate.pastry.gui.screen;

import java.io.File;
import java.util.concurrent.ForkJoinTask;

import com.tesselslate.pastry.Pastry;
import com.tesselslate.pastry.analysis.preemptive.PreemptiveAnalysis;
import com.tesselslate.pastry.capture.PastryCapture;
import com.tesselslate.pastry.capture.PastryCaptureHeader;
import com.tesselslate.pastry.task.AnalyzeCaptureTask;
import com.tesselslate.pastry.task.Exceptional;
import com.tesselslate.pastry.task.ReadCaptureTask;

import net.minecraft.client.gui.screen.Screen;

public class PrepareCaptureAnalysisScreen extends TaskProgressScreen {
    private final File file;
    private final PastryCaptureHeader header;

    private ForkJoinTask<Exceptional<PreemptiveAnalysis>> analyzeTask;
    private ForkJoinTask<Exceptional<PastryCapture>> readTask;

    private PastryCapture capture;

    public PrepareCaptureAnalysisScreen(Screen parent, File file, PastryCaptureHeader header) {
        super(parent);

        this.file = file;
        this.header = header;

        this.readTask = Pastry.TASK_POOL.submit(new ReadCaptureTask(file));
    }

    @Override
    public void cancel() {
        if (this.analyzeTask != null) {
            this.analyzeTask.cancel(true);
        }

        if (this.readTask != null) {
            this.readTask.cancel(true);
        }
    }

    @Override
    public String getProgressText() {
        return this.analyzeTask != null ? "Analyzing capture..." : "Reading capture...";
    }

    @Override
    public Exceptional<Screen> poll() {
        if (this.readTask != null) {
            Exceptional<PastryCapture> result = this.readTask.getRawResult();
            if (result == null) {
                return null;
            }

            if (result.hasError()) {
                Exception error = result.getError();
                Pastry.LOGGER.error("Failed to read capture: " + error);

                return new Exceptional<>(error);
            } else {
                assert result.hasValue();

                this.capture = result.getValue();
                this.analyzeTask = Pastry.TASK_POOL.submit(new AnalyzeCaptureTask(this.capture));

                this.readTask = null;
                return null;
            }
        }

        if (this.analyzeTask != null) {
            Exceptional<PreemptiveAnalysis> result = this.analyzeTask.getRawResult();
            if (result == null) {
                return null;
            }

            if (result.hasError()) {
                Exception error = result.getError();
                Pastry.LOGGER.error("Failed to analyze capture: " + error);

                return new Exceptional<>(error);
            } else {
                assert result.hasValue();

                return new Exceptional<>(this.finish(result.getValue()));
            }
        }

        return new Exceptional<>(new RuntimeException("No tasks to poll"));
    }

    private Screen finish(PreemptiveAnalysis analysis) {
        return new CaptureAnalysisScreen(parent, this.file, this.header, this.capture, analysis);
    }
}
