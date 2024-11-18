package com.tesselslate.pastry.task;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.RecursiveTask;
import java.util.zip.GZIPInputStream;

import com.tesselslate.pastry.capture.PastryCapture;

/**
 * Reads and parses a single {@link PastryCapture} from {@code file}.
 */
public class ReadCaptureTask extends RecursiveTask<ReadCaptureTask.Result> {
    private final File file;

    public ReadCaptureTask(File file) {
        this.file = file;
    }

    @Override
    protected Result compute() {
        try (GZIPInputStream input = new GZIPInputStream(new FileInputStream(file))) {
            PastryCapture capture = new PastryCapture(input);

            return new ReadCaptureTask.Result(capture);
        } catch (Exception e) {
            return new ReadCaptureTask.Result(e);
        }
    }

    public static class Result {
        public final PastryCapture capture;

        public final Exception exception;

        private Result(PastryCapture capture) {
            this.capture = capture;
            this.exception = null;
        }

        private Result(Exception exception) {
            this.capture = null;
            this.exception = exception;
        }
    }
}
