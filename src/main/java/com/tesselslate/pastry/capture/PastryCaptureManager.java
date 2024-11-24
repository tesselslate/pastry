package com.tesselslate.pastry.capture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.tesselslate.pastry.Pastry;
import com.tesselslate.pastry.gui.toast.CaptureSizeToast;
import com.tesselslate.pastry.gui.toast.ErrorToast;

import net.minecraft.client.MinecraftClient;

/**
 * Maintains and synchronizes access to a {@link PastryCapture} for an active
 * Minecraft world.
 */
public class PastryCaptureManager {
    private static final int MAXIMUM_EVENTS = 10000000;
    private static final DateFormat OUTPUT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

    private static final Object LOCK = new Object();

    private static @Nullable PastryCapture ACTIVE_CAPTURE;
    private static boolean PAUSED;
    private static long START_MILLIS;

    /**
     * Returns the number of milliseconds elapsed since the start of the active
     * {@link PastryCapture}.
     *
     * @return The number of milliseconds elapsed since the start of the active
     *         {@link PastryCapture}
     */
    public static int getElapsedTime() {
        return (int) (new Date().getTime() - START_MILLIS);
    }

    /**
     * Returns whether or not there is an active {@link PastryCapture}.
     *
     * @return Whether there is an active {@link PastryCapture}.
     */
    public static boolean isCapturing() {
        synchronized (LOCK) {
            return ACTIVE_CAPTURE != null;
        }
    }

    /**
     * Returns whether or not the active {@link PastryCapture} is paused.
     *
     * @return Whether or not the active capture is paused
     */
    public static boolean isPaused() {
        synchronized (LOCK) {
            return PAUSED;
        }
    }

    /**
     * Acquires a lock for the capture manager and runs the provided function.
     *
     * @param function The function to run while the capture manager is locked
     */
    public static void runLocked(Consumer<PastryCapture> function) {
        synchronized (LOCK) {
            function.accept(ACTIVE_CAPTURE);
        }
    }

    /**
     * Acquires a lock for the capture manager and runs the provided function.
     *
     * @param function The function to run while the capture manager is locked
     * @return The return value of {@code function}
     */
    public static <T> T runLocked(Function<PastryCapture, T> function) {
        synchronized (LOCK) {
            return function.apply(ACTIVE_CAPTURE);
        }
    }

    /**
     * Pauses or unpauses the active {@link PastryCapture}, if any.
     *
     * @param paused Whether to pause or unpause the active capture
     */
    public static void setPaused(boolean paused) {
        synchronized (LOCK) {
            PAUSED = paused;
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
            START_MILLIS = new Date().getTime();
            PAUSED = false;
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

                MinecraftClient client = MinecraftClient.getInstance();
                client.getToastManager().add(new ErrorToast("Failed to stop capture"));
            } finally {
                ACTIVE_CAPTURE = null;
                PAUSED = false;
            }
        }
    }

    /**
     * Acquires exclusive access to the active {@link PastryCapture} if one exists
     * and runs {@code function}.
     */
    public static void update(Update function) {
        boolean shouldStop = false;

        synchronized (LOCK) {
            if (!PAUSED && ACTIVE_CAPTURE != null) {
                function.run(ACTIVE_CAPTURE);

                shouldStop = ACTIVE_CAPTURE.size() > MAXIMUM_EVENTS;
            }
        }

        if (shouldStop) {
            stopCapture();

            MinecraftClient client = MinecraftClient.getInstance();
            client.getToastManager().add(new CaptureSizeToast());
        }
    }

    /**
     * Returns a {@link File} pointing to the instance's capture directory.
     *
     * @return A {@link File} pointing to the instance's capture directory
     */
    public static File getCaptureDirectory() {
        MinecraftClient client = MinecraftClient.getInstance();

        return new File(client.runDirectory, "pastry-recordings");
    }

    /**
     * Opens an output stream to which the active {@link PastryCapture} should be
     * written.
     *
     * @return The output stream to write the active {@link PastryCapture} to
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
