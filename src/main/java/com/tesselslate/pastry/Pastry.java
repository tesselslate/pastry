package com.tesselslate.pastry;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import com.tesselslate.pastry.capture.PastryCaptureManager;
import com.tesselslate.pastry.cullvis.CullState;
import com.tesselslate.pastry.mixin.accessor.WorldRendererAccessor;

public class Pastry implements ClientModInitializer {
    private static final Version VERSION = FabricLoader.getInstance().getModContainer("pastry")
            .orElseThrow(IllegalStateException::new).getMetadata().getVersion();

    public static final String MOD_ID = "pastry";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static ExecutorService EXECUTOR;

    @Nullable
    public static CullState CURRENT_CULLING_STATE;

    @Nullable
    public static CullState CAPTURED_CULLING_STATE;

    public static boolean DISPLAY_CULLING_STATE = false;

    @Override
    public void onInitializeClient() {
        EXECUTOR = Executors.newCachedThreadPool(runnable -> new Thread(runnable, "pastry-worker"));
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
        debugText.add(PastryCaptureManager.size() + " events captured");

        return debugText;
    }
}
