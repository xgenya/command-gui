package com.remrin.client;

import com.remrin.client.gui.CommandGUIScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> new CommandGUIScreen();
	}
}
