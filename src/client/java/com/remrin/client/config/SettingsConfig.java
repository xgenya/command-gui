package com.remrin.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.remrin.CommandGUI;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class SettingsConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir()
			.resolve("command-gui").resolve("settings.json");
	
	private static Map<String, Object> settings = new HashMap<>();
	
	public static void load() {
		if (!Files.exists(CONFIG_PATH)) {
			settings = new HashMap<>();
			initDefaults();
			save();
			return;
		}
		
		try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
			settings = GSON.fromJson(reader, new TypeToken<Map<String, Object>>(){}.getType());
			if (settings == null) {
				settings = new HashMap<>();
			}
			initDefaults();
		} catch (Exception e) {
			CommandGUI.LOGGER.error("Failed to load settings", e);
			settings = new HashMap<>();
			initDefaults();
		}
	}
	
	private static void initDefaults() {
		if (!settings.containsKey("show_vanilla_commands")) {
			settings.put("show_vanilla_commands", true);
		}
		if (!settings.containsKey("show_carpet_commands")) {
			settings.put("show_carpet_commands", true);
		}
		if (!settings.containsKey("show_fakeplayer_tab")) {
			settings.put("show_fakeplayer_tab", true);
		}
	}
	
	public static void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
				GSON.toJson(settings, writer);
			}
		} catch (Exception e) {
			CommandGUI.LOGGER.error("Failed to save settings", e);
		}
	}
	
	public static boolean getBoolean(String key) {
		Object value = settings.get(key);
		if (value instanceof Boolean) {
			return (Boolean) value;
		}
		return false;
	}
	
	public static void setBoolean(String key, boolean value) {
		settings.put(key, value);
	}
	
	public static String getString(String key) {
		Object value = settings.get(key);
		if (value instanceof String) {
			return (String) value;
		}
		return "";
	}
	
	public static void setString(String key, String value) {
		settings.put(key, value);
	}
	
	public static int getInt(String key) {
		Object value = settings.get(key);
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		return 0;
	}
	
	public static void setInt(String key, int value) {
		settings.put(key, value);
	}
}
