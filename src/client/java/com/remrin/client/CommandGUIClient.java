package com.remrin.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.remrin.client.config.CommandConfig;
import com.remrin.client.config.PresetConfig;
import com.remrin.client.gui.CommandGUIScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.glfw.GLFW;

public class CommandGUIClient implements ClientModInitializer {
	public static final Category CMD_GUI_CATEGORY = Category.register(Identifier.parse("command-gui:general"));
	private static KeyMapping openGuiKey;

	@Override
	public void onInitializeClient() {
		CommandConfig.load();
		
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
				new SimpleSynchronousResourceReloadListener() {
					@Override
					public Identifier getFabricId() {
						return Identifier.parse("command-gui:presets");
					}

					@Override
					public void onResourceManagerReload(ResourceManager resourceManager) {
						PresetConfig.load();
					}
				}
		);

		openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.command-gui.open_gui",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_C,
				CMD_GUI_CATEGORY
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openGuiKey.consumeClick()) {
				if (client.screen instanceof CommandGUIScreen) {
					client.setScreen(null);
				} else if (client.screen == null) {
					client.setScreen(new CommandGUIScreen());
				}
			}
		});
	}
}
