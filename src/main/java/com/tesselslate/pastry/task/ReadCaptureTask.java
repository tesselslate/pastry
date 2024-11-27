package com.tesselslate.pastry.task;

import com.tesselslate.pastry.capture.PastryCapture;
import com.tesselslate.pastry.capture.PastryCaptureVersionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

/**
 * Reads and parses a single {@link PastryCapture} from {@code file}.
 */
public class ReadCaptureTask extends PastryTask<PastryCapture> {
    private final File file;

    public ReadCaptureTask(File file) {
        super("ReadCaptureTask");

        this.file = file;
    }

    @Override
    protected PastryCapture runTask() throws FileNotFoundException, IOException, PastryCaptureVersionException {
        try (GZIPInputStream input = new GZIPInputStream(new FileInputStream(file))) {
            return new PastryCapture(input);
        }
    }
}
