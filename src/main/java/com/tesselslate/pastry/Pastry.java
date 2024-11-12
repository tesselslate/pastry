package com.tesselslate.pastry;

import net.fabricmc.api.ClientModInitializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Pastry implements ClientModInitializer {
    public static final String MOD_ID = "pastry";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private static PastryRecorder RECORDER;

    @Override
    public void onInitializeClient() {
        try {
            RECORDER = new PastryRecorder();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        LOGGER.info("Initialized Pastry");
    }

    public static PastryRecorder getRecorder() {
        return RECORDER;
    }
}
