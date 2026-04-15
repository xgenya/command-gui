package com.remrin.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.remrin.client.config.CommandConfig;
import com.remrin.client.config.PresetConfig;
import com.remrin.client.config.SettingsConfig;
import com.remrin.client.gui.ChainedCommandExecutor;
import com.remrin.client.gui.CommandGUIScreen;
import com.remrin.client.gui.TimedTaskManager;
import com.remrin.client.sync.ServerCommandStore;
import com.remrin.sync.ServerCommandPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.glfw.GLFW;

/**
 * Client-side mod entry point. Responsible for initializing configs, registering the preset reload
 * listener, binding the hotkey, and driving per-tick logic (timed tasks and the delayed command
 * queue). Also registers the eun_carpet server-sync payload receiver.
 */
public class CommandGUIClient implements ClientModInitializer {

  public static final Category CMD_GUI_CATEGORY = Category.register(
      Identifier.parse("command-gui:general"));
  private static KeyMapping openGuiKey;

  @Override
  public void onInitializeClient() {
    CommandConfig.load();
    SettingsConfig.load();

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

    // Register receiver for eun_carpet server command preset sync
    ClientPlayNetworking.registerGlobalReceiver(ServerCommandPayload.TYPE, (payload, context) -> {
      context.client().execute(() -> {
        ServerCommandStore.update(payload.groups());
        // If the GUI is currently open, reopen to show the server tab
        if (context.client().screen instanceof CommandGUIScreen) {
          context.client().setScreen(new CommandGUIScreen());
        }
      });
    });

    // Clear server commands on disconnect
    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
        ServerCommandStore.clear());

    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      while (openGuiKey.consumeClick()) {
        if (client.screen instanceof CommandGUIScreen) {
          client.setScreen(null);
        } else if (client.screen == null) {
          client.setScreen(new CommandGUIScreen());
        }
      }

      TimedTaskManager.tick();
      ChainedCommandExecutor.tickDelayed();
    });
  }
}
