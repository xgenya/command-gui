package com.remrin;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandGUI implements ModInitializer {
	public static final String MOD_ID = "command-gui";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Command-GUI initialized!");
	}
}
