package com.remrin.client.gui;

import com.remrin.client.config.CommandConfig;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;

/**
 * Shared utility methods for command execution, placeholder detection, fake player identification,
 * and time formatting.
 */
public final class CommandHelper {

  /**
   * Regex that matches any single placeholder token in a command string.
   */
  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(
      "\\{(?:player_all|player_fake|player|name|number|time|coords|x)\\}"
  );

  private CommandHelper() {
    // utility class
  }

  // ── Command Sending ──────────────────────────────────────────────

  /**
   * Send a command or chat message. Leading "/" is stripped and sent via {@code sendCommand};
   * everything else is sent via {@code sendChat}.
   */
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

  // ── Placeholder Helpers ──────────────────────────────────────────

  /**
   * Returns {@code true} if the command string contains any placeholder token.
   */
  public static boolean hasPlaceholders(String command) {
		if (command == null) {
			return false;
		}
    return PLACEHOLDER_PATTERN.matcher(command).find();
  }

  /**
   * Returns {@code true} if any command in the list contains a placeholder.
   */
  public static boolean hasPlaceholders(List<String> commands) {
		if (commands == null) {
			return false;
		}
    for (String cmd : commands) {
			if (hasPlaceholders(cmd)) {
				return true;
			}
    }
    return false;
  }

  /**
   * Returns the ordered placeholder pattern for use in parsing.
   */
  public static Pattern getPlaceholderPattern() {
    return PLACEHOLDER_PATTERN;
  }

  // ── Fake Player Detection ────────────────────────────────────────

  /**
   * Detect if a player is a Carpet fake player based on 0 ping and not being the local player. This
   * is the standard heuristic used by Carpet Mod.
   */
  public static boolean isFakePlayer(String name) {
    Minecraft mc = Minecraft.getInstance();
    if (mc.player != null && name.equals(mc.player.getName().getString())) {
      return false;
    }
    if (mc.getConnection() != null) {
      for (PlayerInfo info : mc.getConnection().getListedOnlinePlayers()) {
        if (info.getProfile().name().equals(name)) {
          return info.getLatency() == 0;
        }
      }
    }
    return false;
  }

  /**
   * Detect if a PlayerInfo represents a Carpet fake player. Excludes the local player even if they
   * have 0 latency.
   */
  public static boolean isFakePlayer(PlayerInfo playerInfo) {
    Minecraft mc = Minecraft.getInstance();
    if (mc.player != null && playerInfo.getProfile().id().equals(mc.player.getUUID())) {
      return false;
    }
    return playerInfo.getLatency() == 0;
  }

  // ── Fake Player Command Detection ────────────────────────────────

  /**
   * Detect if a command is a fake player spawn command (/player X spawn ...).
   */
  public static boolean isFakePlayerSpawnCommand(String command) {
		if (command == null) {
			return false;
		}
    String cmd = command.trim().toLowerCase();
		if (cmd.startsWith("/")) {
			cmd = cmd.substring(1);
		}
    return cmd.startsWith("player ") && cmd.contains(" spawn");
  }

  /**
   * Detect if a {@link CommandConfig.CommandEntry} represents a fake player command.
   */
  public static boolean isFakePlayerCommand(CommandConfig.CommandEntry entry) {
		if (entry == null) {
			return false;
		}
    List<String> commands = entry.getCommands();
		if (commands.isEmpty()) {
			return false;
		}
    return isFakePlayerSpawnCommand(commands.get(0));
  }

  // ── Time Formatting ──────────────────────────────────────────────

  /**
   * Format seconds into a human-readable duration string. Examples: "5s", "2:30", "1:05:30"
   */
  public static String formatTime(int seconds) {
    if (seconds >= 3600) {
      int h = seconds / 3600;
      int m = (seconds % 3600) / 60;
      int s = seconds % 60;
      return String.format("%d:%02d:%02d", h, m, s);
    } else if (seconds >= 60) {
      int m = seconds / 60;
      int s = seconds % 60;
      return String.format("%d:%02d", m, s);
    } else {
      return seconds + "s";
    }
  }

  /**
   * Format total seconds into a descriptive duration string with units. Examples: "5s", "2m 30s",
   * "1h 05m 30s", "--" for zero.
   */
  public static String formatDuration(int totalSeconds) {
		if (totalSeconds <= 0) {
			return "--";
		}
    int h = totalSeconds / 3600;
    int m = (totalSeconds % 3600) / 60;
    int s = totalSeconds % 60;
		if (h > 0) {
			return String.format("%dh %02dm %02ds", h, m, s);
		}
		if (m > 0) {
			return String.format("%dm %02ds", m, s);
		}
    return s + "s";
  }
}
