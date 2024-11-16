package com.tesselslate.pastry.capture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jetbrains.annotations.Nullable;

import com.tesselslate.pastry.Pastry;

import net.minecraft.client.MinecraftClient;

/**
 * Maintains and synchronizes access to a {@link PastryCapture} for an active
 * Minecraft world.
 */
public class PastryCaptureManager {
    private static final DateFormat OUTPUT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

    private static final Object LOCK = new Object();

    private static @Nullable PastryCapture ACTIVE_CAPTURE;

    /**
     * Returns whether or not there is an active {@link PastryCapture}.
     *
     * @returns Whether there is an active {@link PastryCapture}.
     */
    public static boolean isCapturing() {
        synchronized (LOCK) {
            return ACTIVE_CAPTURE != null;
        }
    }

    /**
     * Returns the number of events present in the active {@link PastryCapture}, if
     * any.
     */
    public static int size() {
        synchronized (LOCK) {
            return ACTIVE_CAPTURE != null ? ACTIVE_CAPTURE.size() : 0;
        }
    }

    /**
     * Starts a new {@link PastryCapture}.
     *
     * @throws RuntimeException If there is already an active {@link PastryCapture}
     */
    public static void startCapture() {
        synchronized (LOCK) {
            if (ACTIVE_CAPTURE != null) {
                throw new RuntimeException("Cannot start new capture while another capture is active");
            }

            ACTIVE_CAPTURE = new PastryCapture();
        }

        Pastry.LOGGER.info("Started new capture");
    }

    /**
     * Stops the active {@link PastryCapture} and writes the results to disk.
     *
     * @throws RuntimeException If there is no active {@link PastryCapture}
     */
    public static void stopCapture() {
        synchronized (LOCK) {
            if (ACTIVE_CAPTURE == null) {
                throw new RuntimeException("Cannot stop capture (no active capture)");
            }

            try (OutputStream output = openCaptureFile()) {
                ACTIVE_CAPTURE.writeTo(output);

                Pastry.LOGGER.info("Stopped capture (wrote " + ACTIVE_CAPTURE.size() + " events)");
            } catch (Exception e) {
                Pastry.LOGGER.error("Failed to write capture (" + ACTIVE_CAPTURE.size() + " events lost)");
            } finally {
                ACTIVE_CAPTURE = null;
            }
        }
    }

    /**
     * Acquires exclusive access to the active {@link PastryCapture} if one exists
     * and runs {@code function}.
     */
    public static void update(Update function) {
        synchronized (LOCK) {
            if (ACTIVE_CAPTURE != null) {
                function.run(ACTIVE_CAPTURE);
            }
        }
    }

    /**
     * Returns a {@link File} pointing to the instance's capture directory.
     *
     * @returns A {@link File} pointing to the instance's capture directory
     */
    public static File getCaptureDirectory() {
        MinecraftClient client = MinecraftClient.getInstance();

        return new File(client.runDirectory, "pastry-recordings");
    }

    /**
     * Opens an output stream to which the active {@link PastryCapture} should be
     * written.
     *
     * @returns The output stream to write the active {@link PastryCapture} to
     */
    private static OutputStream openCaptureFile() throws FileNotFoundException {
        File outputDir = getCaptureDirectory();
        outputDir.mkdir();

        File output = new File(outputDir, OUTPUT_DATE_FORMAT.format(new Date()) + ".bin.gz");
        return new FileOutputStream(output);
    }

    /**
     * Represents a piece of code which will update the active
     * {@link PastryCapture}.
     */
    public interface Update {
        public void run(PastryCapture capture);
    }
}
