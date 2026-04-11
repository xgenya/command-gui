package com.remrin.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ChainedCommandExecutor {
	
	public enum PlaceholderType {
		PLAYER_ALL,
		PLAYER_OTHER,
		PLAYER_FAKE,
		NAME,
		NUMBER,
		COORDS
	}
	
	public static class Config {
		public Integer minValue;
		public Integer maxValue;
		public int[] quickValues;
		
		public static Config defaultConfig() {
			return new Config();
		}
		
		public Config withNumberRange(Integer min, Integer max, int[] quickValues) {
			this.minValue = min;
			this.maxValue = max;
			this.quickValues = quickValues;
			return this;
		}
	}
	
	private final Screen parent;
	private String currentCommand;
	private final List<PlaceholderType> pendingTypes = new ArrayList<>();
	private int currentIndex = 0;
	private final Config config;
	
	public ChainedCommandExecutor(Screen parent, String command) {
		this(parent, command, Config.defaultConfig());
	}
	
	public ChainedCommandExecutor(Screen parent, String command, Config config) {
		this.parent = parent;
		this.currentCommand = command;
		this.config = config;
		parseTypes(command);
	}
	
	private void parseTypes(String command) {
		int index = 0;
		while (index < command.length()) {
			int playerAllIdx = command.indexOf("{player_all}", index);
			int playerOtherIdx = command.indexOf("{player}", index);
			int playerFakeIdx = command.indexOf("{player_fake}", index);
			int nameIdx = command.indexOf("{name}", index);
			int numberIdx = command.indexOf("{number}", index);
			int coordsIdx = command.indexOf("{coords}", index);
			int xIdx = command.indexOf("{x}", index);
			
			int minIdx = Integer.MAX_VALUE;
			PlaceholderType minType = null;
			int skipLen = 0;
			
			if (playerAllIdx >= 0 && playerAllIdx < minIdx) {
				minIdx = playerAllIdx;
				minType = PlaceholderType.PLAYER_ALL;
				skipLen = "{player_all}".length();
			}
			if (playerFakeIdx >= 0 && playerFakeIdx < minIdx) {
				minIdx = playerFakeIdx;
				minType = PlaceholderType.PLAYER_FAKE;
				skipLen = "{player_fake}".length();
			}
			if (playerOtherIdx >= 0 && playerOtherIdx < minIdx) {
				if (minType != PlaceholderType.PLAYER_ALL && minType != PlaceholderType.PLAYER_FAKE) {
					minIdx = playerOtherIdx;
					minType = PlaceholderType.PLAYER_OTHER;
					skipLen = "{player}".length();
				}
			}
			if (nameIdx >= 0 && nameIdx < minIdx) {
				minIdx = nameIdx;
				minType = PlaceholderType.NAME;
				skipLen = "{name}".length();
			}
			if (numberIdx >= 0 && numberIdx < minIdx) {
				minIdx = numberIdx;
				minType = PlaceholderType.NUMBER;
				skipLen = "{number}".length();
			}
			if (coordsIdx >= 0 && coordsIdx < minIdx) {
				minIdx = coordsIdx;
				minType = PlaceholderType.COORDS;
				skipLen = "{coords}".length();
			}
			if (xIdx >= 0 && xIdx < minIdx) {
				minIdx = xIdx;
				minType = PlaceholderType.COORDS;
				skipLen = "{x}".length();
			}
			
			if (minType == null) {
				break;
			}
			
			pendingTypes.add(minType);
			index = minIdx + skipLen;
		}
	}
	
	public void start() {
		if (currentIndex < pendingTypes.size()) {
			showNextInput();
		} else {
			executeCommand();
		}
	}
	
	private void showNextInput() {
		Minecraft mc = Minecraft.getInstance();
		PlaceholderType type = pendingTypes.get(currentIndex);
		
		switch (type) {
			case PLAYER_ALL -> {
				PlayerSelectorScreen screen = new PlayerSelectorScreen(
						parent,
						Component.translatable("screen.command-gui.select_player"),
						null,
						PlayerSelectorScreen.FilterMode.ALL,
						playerName -> {
							currentCommand = currentCommand.replaceFirst("\\{player_all\\}", playerName);
							currentIndex++;
							start();
						}
				);
				mc.setScreen(screen);
			}
			
			case PLAYER_OTHER -> {
				PlayerSelectorScreen screen = new PlayerSelectorScreen(
						parent,
						Component.translatable("screen.command-gui.select_player"),
						null,
						PlayerSelectorScreen.FilterMode.EXCLUDE_SELF,
						playerName -> {
							currentCommand = currentCommand.replaceFirst("\\{player\\}", playerName);
							currentIndex++;
							start();
						}
				);
				mc.setScreen(screen);
			}
			
			case PLAYER_FAKE -> {
				PlayerSelectorScreen screen = new PlayerSelectorScreen(
						parent,
						Component.translatable("screen.command-gui.select_player"),
						null,
						PlayerSelectorScreen.FilterMode.ONLY_FAKE_PLAYERS,
						playerName -> {
							currentCommand = currentCommand.replaceFirst("\\{player_fake\\}", playerName);
							currentIndex++;
							start();
						}
				);
				mc.setScreen(screen);
			}
			
			case NAME -> {
				TextInputScreen screen = new TextInputScreen(
						parent,
						Component.translatable("screen.command-gui.input_name"),
						null,
						"Steve"
				) {
					@Override
					protected void onInputConfirmed(String input) {
						currentCommand = currentCommand.replaceFirst("\\{name\\}", input);
						currentIndex++;
						start();
					}
				};
				mc.setScreen(screen);
			}
			
			case NUMBER -> {
				NumberInputScreen screen = new NumberInputScreen(
						parent,
						Component.translatable("screen.command-gui.input_number"),
						null,
						config.minValue, config.maxValue, config.quickValues
				) {
					@Override
					protected void onNumberConfirmed(String number) {
						currentCommand = currentCommand.replaceFirst("\\{number\\}", number);
						currentIndex++;
						start();
					}
				};
				mc.setScreen(screen);
			}
			
			case COORDS -> {
				CoordinateInputScreen screen = new CoordinateInputScreen(
						parent,
						Component.translatable("screen.command-gui.input_coord"),
						null
				) {
					@Override
					protected void onCoordsConfirmed(String x, String y, String z) {
						currentCommand = currentCommand
								.replace("{x}", x)
								.replace("{y}", y)
								.replace("{z}", z)
								.replace("{coords}", x + " " + y + " " + z);
						currentIndex++;
						start();
					}
				};
				mc.setScreen(screen);
			}
		}
	}
	
	private void executeCommand() {
		Minecraft mc = Minecraft.getInstance();
		if (mc != null && mc.player != null) {
			String command = currentCommand;
			if (command.startsWith("/")) {
				mc.player.connection.sendCommand(command.substring(1));
			} else {
				mc.player.connection.sendChat(command);
			}
			
			if (!CommandGUIScreen.shouldKeepOpen()) {
				if (parent != null) {
					parent.onClose();
				}
			} else {
				mc.setScreen(parent);
			}
		}
	}
	
	public static void sendCommand(String command) {
		Minecraft mc = Minecraft.getInstance();
		if (mc != null && mc.player != null) {
			if (command.startsWith("/")) {
				mc.player.connection.sendCommand(command.substring(1));
			} else {
				mc.player.connection.sendChat(command);
			}
		}
	}

	public static boolean hasPlaceholders(String command) {
		return command != null && (
			command.contains("{player_all}") ||
			command.contains("{player}") ||
			command.contains("{player_fake}") ||
			command.contains("{name}") ||
			command.contains("{number}") ||
			command.contains("{coords}") ||
			command.contains("{x}")
		);
	}
	
	public static void execute(Screen parent, String command) {
		execute(parent, command, Config.defaultConfig());
	}
	
	public static void execute(Screen parent, String command, Config config) {
		if (hasPlaceholders(command)) {
			new ChainedCommandExecutor(parent, command, config).start();
		} else {
			Minecraft mc = Minecraft.getInstance();
			if (mc != null && mc.player != null) {
				if (command.startsWith("/")) {
					mc.player.connection.sendCommand(command.substring(1));
				} else {
					mc.player.connection.sendChat(command);
				}
				
				if (!CommandGUIScreen.shouldKeepOpen()) {
					if (parent != null) {
						parent.onClose();
					}
				} else {
					mc.setScreen(parent);
				}
			}
		}
	}
}
