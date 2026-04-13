package com.remrin;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main mod entry point (server/common side). Only responsible for registering the mod ID and
 * logger; actual functionality is implemented in the client entry class {@code CommandGUIClient}.
 */
public class CommandGUI implements ModInitializer {

  /**
   * Unique mod identifier, used for resource paths and the config directory name
   */
  public static final String MOD_ID = "command-gui";
  /**
   * Global logger, used consistently throughout the mod
   */
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  @Override
  public void onInitialize() {
    LOGGER.info("Command-GUI initialized!");
  }
}
