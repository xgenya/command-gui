package com.remrin.client;

import com.remrin.client.gui.CommandGUIScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * ModMenu integration implementation. Links ModMenu's "Config" button to this mod's main GUI screen
 * instead of a separate settings page.
 */
public class ModMenuIntegration implements ModMenuApi {

  @Override
  public ConfigScreenFactory<?> getModConfigScreenFactory() {
    return parent -> new CommandGUIScreen();
  }
}
