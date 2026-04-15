package com.remrin;

import com.remrin.sync.ServerCommandPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main mod entry point (server/common side). Registers payload types for the sync feature.
 */
public class CommandGUI implements ModInitializer {

  public static final String MOD_ID = "command-gui";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  @Override
  public void onInitialize() {
    // Register S2C payload for eun_carpet server-sync feature.
    // Guard against double-registration in case eun_carpet is also loaded (e.g. integrated play).
    try {
      PayloadTypeRegistry.playS2C().register(ServerCommandPayload.TYPE, ServerCommandPayload.CODEC);
    } catch (IllegalArgumentException e) {
      LOGGER.debug("ServerCommandPayload type already registered (eun_carpet present): {}", e.getMessage());
    }
    LOGGER.info("Command-GUI initialized!");
  }
}
