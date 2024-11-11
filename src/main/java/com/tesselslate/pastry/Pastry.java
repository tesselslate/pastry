package com.tesselslate.pastry;

import net.fabricmc.api.ClientModInitializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Pastry implements ClientModInitializer {
	public static final String MOD_ID = "pastry";

	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
        LOGGER.info("Initialized Pastry");
	}
}
