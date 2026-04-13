package com.remrin.client.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;

/**
 * Timed task manager that maintains lists of timed fake player spawn and kill tasks.
 * <p>
 * Each {@link TimedTask} stores a countdown in ticks, which is decremented on every client tick;
 * when it expires, the corresponding {@code /player} command is automatically sent to the server.
 * Pending spawn tasks are shown ahead of time in the fake player list in {@link FakePlayerTab}.
 */
public class TimedTaskManager {

  private static final List<TimedTask> pendingTasks = new ArrayList<>();

  public static void addSpawnTask(String playerName, int hours, int minutes, int seconds) {
    addSpawnTask(playerName, hours, minutes, seconds, null, null, null);
  }

  public static void addSpawnTask(String playerName, int hours, int minutes, int seconds,
      Double x, Double y, Double z) {
    int totalTicks = (hours * 3600 + minutes * 60 + seconds) * 20;
    if (totalTicks > 0) {
      // Keep only the latest task for the same fake player to avoid stacking and duplicate spawns
      removeTask(playerName);
      pendingTasks.add(new TimedTask(TaskType.SPAWN, playerName, totalTicks, x, y, z));
    }
  }

  public static void addKillTask(String playerName, int hours, int minutes, int seconds) {
    int totalTicks = (hours * 3600 + minutes * 60 + seconds) * 20;
    if (totalTicks > 0) {
      // Keep only the latest task for the same fake player to avoid stacking
      removeTask(playerName);
      pendingTasks.add(new TimedTask(TaskType.KILL, playerName, totalTicks));
    }
  }

  public static void removeTask(String playerName) {
    pendingTasks.removeIf(task -> task.playerName.equals(playerName));
  }

  public static TimedTask getTask(String playerName) {
    for (TimedTask task : pendingTasks) {
      if (task.playerName.equals(playerName)) {
        return task;
      }
    }
    return null;
  }

  public static List<TimedTask> getPendingSpawnTasks() {
    List<TimedTask> result = new ArrayList<>();
    for (TimedTask task : pendingTasks) {
      if (task.type == TaskType.SPAWN) {
        result.add(task);
      }
    }
    return result;
  }

  public static List<TimedTask> getAllTasks() {
    return new ArrayList<>(pendingTasks);
  }

  /**
   * Called every client tick; advances the countdown of all pending tasks. Expired tasks
   * automatically send the corresponding spawn or kill command, then are removed from the list.
   */
  public static void tick() {
    if (pendingTasks.isEmpty()) {
      return;
    }

    Minecraft mc = Minecraft.getInstance();
    if (mc.player == null) {
      return;
    }

    Iterator<TimedTask> it = pendingTasks.iterator();
    while (it.hasNext()) {
      TimedTask task = it.next();
      task.remainingTicks--;
      if (task.remainingTicks <= 0) {
        if (task.type == TaskType.SPAWN) {
          String cmd;
          if (task.spawnX != null && task.spawnY != null && task.spawnZ != null) {
            cmd = String.format("player %s spawn at %.1f %.1f %.1f",
                task.playerName, task.spawnX, task.spawnY, task.spawnZ);
          } else {
            cmd = "player " + task.playerName + " spawn";
          }
          mc.player.connection.sendCommand(cmd);
        } else {
          mc.player.connection.sendCommand("player " + task.playerName + " kill");
        }
        it.remove();
      }
    }
  }

  public static void clear() {
    pendingTasks.clear();
  }

  public enum TaskType {
    SPAWN, KILL
  }

  public static class TimedTask {

    public final TaskType type;
    public final String playerName;
    /**
     * Optional spawn coordinates; if null, spawns at the player's current position
     */
    public final Double spawnX, spawnY, spawnZ;
    public int remainingTicks;

    public TimedTask(TaskType type, String playerName, int delayTicks) {
      this(type, playerName, delayTicks, null, null, null);
    }

    public TimedTask(TaskType type, String playerName, int delayTicks, Double x, Double y,
        Double z) {
      this.type = type;
      this.playerName = playerName;
      this.remainingTicks = delayTicks;
      this.spawnX = x;
      this.spawnY = y;
      this.spawnZ = z;
    }

    public int getRemainingSeconds() {
      return (remainingTicks + 19) / 20;
    }
  }
}
