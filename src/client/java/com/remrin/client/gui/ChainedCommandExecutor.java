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
		TIME,
		COORDS
	}
	
	public static class Config {
		public Integer minValue;
		public Integer maxValue;
		public int[] quickValues;
		public String[] quickStrValues;
		
		public static Config defaultConfig() {
			return new Config();
		}
		
		public Config withNumberRange(Integer min, Integer max, int[] quickValues) {
			this.minValue = min;
			this.maxValue = max;
			this.quickValues = quickValues;
			return this;
		}
		
		public Config withTimeRange(Integer min, Integer max, String[] quickStrValues) {
			this.minValue = min;
			this.maxValue = max;
			this.quickStrValues = quickStrValues;
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
			int timeIdx = command.indexOf("{time}", index);
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
			if (timeIdx >= 0 && timeIdx < minIdx) {
				minIdx = timeIdx;
				minType = PlaceholderType.TIME;
				skipLen = "{time}".length();
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
			
			case TIME -> {
				TimeInputScreen screen = new TimeInputScreen(
						parent,
						Component.translatable("screen.command-gui.input_time"),
						null,
						config.quickStrValues
				) {
					@Override
					protected void onTimeConfirmed(String time) {
						currentCommand = currentCommand.replaceFirst("\\{time\\}", time);
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
			
			onExecutionComplete();
			
			if (!CommandGUIScreen.shouldKeepOpen()) {
				if (parent != null) {
					parent.onClose();
				}
			} else {
				mc.setScreen(parent);
			}
		}
	}

	protected void onExecutionComplete() {
		// Hook for subclasses to execute additional commands after the main command
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
			command.contains("{time}") ||
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

	public static void executeMulti(Screen parent, java.util.List<String> commands) {
		executeMulti(parent, commands, Config.defaultConfig());
	}

	public static void executeMulti(Screen parent, java.util.List<String> commands, Config config) {
		if (commands == null || commands.isEmpty()) return;
		if (commands.size() == 1) {
			execute(parent, commands.get(0), config);
			return;
		}
		// Execute first command with placeholder support, then remaining commands directly
		String first = commands.get(0);
		java.util.List<String> rest = commands.subList(1, commands.size());
		boolean needsDelay = isFakePlayerSpawnCommand(first) && !rest.isEmpty();
		if (hasPlaceholders(first)) {
			new ChainedCommandExecutor(parent, first, config) {
				@Override
				protected void onExecutionComplete() {
					if (needsDelay) {
						scheduleDelayed(new ArrayList<>(rest), 0);
					} else {
						for (String cmd : rest) {
							sendCommand(cmd);
						}
					}
				}
			}.start();
		} else {
			Minecraft mc = Minecraft.getInstance();
			if (mc != null && mc.player != null) {
				sendCommand(first);
				if (needsDelay) {
					scheduleDelayed(new ArrayList<>(rest), 0);
				} else {
					for (String cmd : rest) {
						sendCommand(cmd);
					}
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

	/**
	 * Detect if a command is a fake player spawn command (/player X spawn ...).
	 * When spawn is followed by action commands, we need a delay to ensure
	 * the fake player is fully spawned before executing actions.
	 */
	private static boolean isFakePlayerSpawnCommand(String command) {
		String cmd = command.trim().toLowerCase();
		if (cmd.startsWith("/")) cmd = cmd.substring(1);
		return cmd.startsWith("player ") && cmd.contains(" spawn");
	}

	// --- Delayed command execution queue ---
	private static final int SPAWN_DELAY_TICKS = 20; // 1 second delay after spawn
	private static final List<DelayedBatch> delayedQueue = new ArrayList<>();

	private static class DelayedBatch {
		final List<String> commands;
		int remainingTicks;

		DelayedBatch(List<String> commands, int delayTicks) {
			this.commands = commands;
			this.remainingTicks = delayTicks;
		}
	}

	/**
	 * Schedule commands to execute after a delay (in ticks).
	 * Used to ensure fake player spawn completes before running actions.
	 */
	private static void scheduleDelayed(List<String> commands, int extraDelay) {
		delayedQueue.add(new DelayedBatch(commands, SPAWN_DELAY_TICKS + extraDelay));
	}

	/**
	 * Called every client tick to process delayed command batches.
	 * Must be registered in the client tick event handler.
	 */
	public static void tickDelayed() {
		if (delayedQueue.isEmpty()) return;

		java.util.Iterator<DelayedBatch> it = delayedQueue.iterator();
		while (it.hasNext()) {
			DelayedBatch batch = it.next();
			batch.remainingTicks--;
			if (batch.remainingTicks <= 0) {
				for (String cmd : batch.commands) {
					sendCommand(cmd);
				}
				it.remove();
			}
		}
	}
}
