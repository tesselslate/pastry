package com.tesselslate.pastry.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import com.tesselslate.pastry.capture.PastryCapture;
import com.tesselslate.pastry.capture.PastryCaptureVersionException;

/**
 * Reads and parses a single {@link PastryCapture} from {@code file}.
 */
public class ReadCaptureTask {
    public static PastryCapture run(File file)
            throws FileNotFoundException, IOException, PastryCaptureVersionException {
        try (GZIPInputStream input = new GZIPInputStream(new FileInputStream(file))) {
            PastryCapture capture = new PastryCapture(input);

            return capture;
        } catch (Exception e) {
            throw e;
        }
    }
}
