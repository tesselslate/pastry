package com.tesselslate.pastry;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import com.tesselslate.pastry.capture.PastryCapture;

public class Pastry implements ClientModInitializer {
    private static final DateFormat OUTPUT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

    public static final String MOD_ID = "pastry";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Nullable
    private static PastryCapture ACTIVE_CAPTURE;

    @Override
    public void onInitializeClient() {
        // Do nothing.
    }

    public static void endCapture() {
        if (ACTIVE_CAPTURE == null) {
            throw new RuntimeException("No ongoing pastry capture");
        }

        try {
            writeCapture();
        } catch (Exception e) {
            LOGGER.error("Failed to write capture: " + e);
        }

        ACTIVE_CAPTURE = null;
    }

    public static @Nullable PastryCapture getActiveCapture() {
        return ACTIVE_CAPTURE;
    }

    public static void startCapture() {
        if (ACTIVE_CAPTURE != null) {
            throw new RuntimeException("Cannot replace ongoing pastry capture");
        }

        ACTIVE_CAPTURE = new PastryCapture();

        LOGGER.info("Started new capture");
    }

    private static void writeCapture() throws FileNotFoundException, IOException {
        @SuppressWarnings("resource")
        File outputDir = new File(MinecraftClient.getInstance().runDirectory, "pastry-recordings");
        outputDir.mkdir();

        File output = new File(outputDir, OUTPUT_DATE_FORMAT.format(new Date()) + ".bin.gz");
        FileOutputStream outputStream = new FileOutputStream(output);

        // writeTo will close the FileOutputStream when closing the
        // PastryCaptureOutputStream it creates.
        ACTIVE_CAPTURE.writeTo(outputStream);

        LOGGER.info("Wrote capture with " + ACTIVE_CAPTURE.size() + " events to " + output);
    }
}
