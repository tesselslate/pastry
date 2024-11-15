package com.tesselslate.pastry;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import com.tesselslate.pastry.capture.PastryCapture;
import com.tesselslate.pastry.cullvis.CullState;
import com.tesselslate.pastry.mixin.accessor.WorldRendererAccessor;

public class Pastry implements ClientModInitializer {
    private static final DateFormat OUTPUT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

    private static final Version VERSION = FabricLoader.getInstance().getModContainer("pastry")
            .orElseThrow(IllegalStateException::new).getMetadata().getVersion();

    public static final String MOD_ID = "pastry";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Nullable
    private static PastryCapture ACTIVE_CAPTURE;

    @Nullable
    public static CullState CURRENT_CULLING_STATE;

    @Nullable
    public static CullState CAPTURED_CULLING_STATE;

    public static boolean DISPLAY_CULLING_STATE = false;

    @Override
    public void onInitializeClient() {
        // Do nothing.
    }

    public static void captureCullingState() {
        CAPTURED_CULLING_STATE = CURRENT_CULLING_STATE;
    }

    public static void toggleCullingVisualizer() {
        DISPLAY_CULLING_STATE = !DISPLAY_CULLING_STATE;
    }

    public static boolean toggleFrustumCapture() {
        @SuppressWarnings("resource")
        WorldRendererAccessor renderer = (WorldRendererAccessor) MinecraftClient.getInstance().worldRenderer;

        if (renderer.getCapturedFrustum() == null) {
            renderer.setShouldCaptureFrustum(true);
            return true;
        } else {
            renderer.setCapturedFrustum(null);
            return false;
        }
    }

    public static List<String> getDebugText() {
        List<String> debugText = new ArrayList<>();

        debugText.add("");
        debugText.add("pastry " + VERSION.getFriendlyString());

        if (ACTIVE_CAPTURE != null) {
            debugText.add(ACTIVE_CAPTURE.size() + " events captured");
        }

        return debugText;
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
