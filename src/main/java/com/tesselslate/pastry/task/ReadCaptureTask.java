package com.tesselslate.pastry.task;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.RecursiveTask;
import java.util.zip.GZIPInputStream;

import com.tesselslate.pastry.capture.PastryCapture;

/**
 * Reads and parses a single {@link PastryCapture} from {@code file}.
 */
public class ReadCaptureTask extends RecursiveTask<Exceptional<PastryCapture>> {
    private final File file;

    public ReadCaptureTask(File file) {
        this.file = file;
    }

    @Override
    protected Exceptional<PastryCapture> compute() {
        try (GZIPInputStream input = new GZIPInputStream(new FileInputStream(file))) {
            PastryCapture capture = new PastryCapture(input);

            return new Exceptional<>(capture);
        } catch (Exception e) {
            return new Exceptional<>(e);
        }
    }
}
