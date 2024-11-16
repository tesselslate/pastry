package com.tesselslate.pastry.task;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPInputStream;

import com.tesselslate.pastry.capture.PastryCaptureHeader;
import com.tesselslate.pastry.capture.PastryCaptureManager;
import com.tesselslate.pastry.capture.PastryCaptureVersionException;

/**
 * Finds all available capture files from the instance's pastry-recordings
 * directory and reads the {@link PastryCaptureHeader} of each available
 * capture.
 */
public class CaptureListTask {
    public static void run(CompletableFuture<Result> future) {
        File capturesDir = PastryCaptureManager.getCaptureDirectory();
        File[] captures = capturesDir.listFiles();

        Result result = new Result();
        for (File file : captures) {
            try {
                result.addEntry(new Entry(file));
            } catch (Exception e) {
                result.addException(file, e);
            }
        }

        future.complete(result);
    }

    public static class Entry {
        public PastryCaptureHeader header;
        public File path;
        public long size;

        public Entry(File path) throws FileNotFoundException, IOException, PastryCaptureVersionException {
            this.path = path;
            this.size = path.length();

            try (DataInputStream input = new DataInputStream(new GZIPInputStream(new FileInputStream(path)))) {
                this.header = new PastryCaptureHeader(input);
            }
        }
    }

    public static class Result {
        public List<Entry> entries;
        public Map<File, Exception> exceptions;

        public Result() {
            this.entries = new ArrayList<>();
            this.exceptions = new HashMap<>();
        }

        private void addEntry(Entry entry) {
            this.entries.add(entry);
        }

        private void addException(File file, Exception e) {
            this.exceptions.put(file, e);
        }
    }
}
