package com.remrin.client.gui;

import com.remrin.client.config.PresetConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;

/**
 * Preset command data adapter that converts raw JSON data from {@link PresetConfig} into
 * {@link CommandGroup} and {@link VanillaCommand} objects ready for use by the GUI.
 * <p>
 * Data comes from JSON files in the {@code config/command-gui/presets/} directory (excluding
 * custom.json), read and cached by {@link PresetConfig} during resource pack loading.
 */
public class VanillaCommands {

  /**
   * Retrieves the command groups (categories) for the specified preset, used for sidebar category
   * filtering.
   *
   * @param presetId the preset ID (e.g. "vanilla", "carpet")
   * @return all command groups for that preset, or an empty list if the preset is not found
   */
  public static List<CommandGroup> getGroups(String presetId) {
    List<CommandGroup> groups = new ArrayList<>();

    PresetConfig.Preset preset = PresetConfig.getPreset(presetId);
    if (preset == null) {
      return groups;
    }

    for (PresetConfig.CommandGroup pg : preset.groups) {
      CommandGroup group = new CommandGroup(pg.nameKey);
      for (PresetConfig.PresetCommand pc : pg.commands) {
        group.add(
            new VanillaCommand(pc.nameKey, pc.command, pc.description, pc.minValue, pc.maxValue,
                pc.quickValues, pc.quickStrValues));
      }
      groups.add(group);
    }

    return groups;
  }

  public static List<VanillaCommand> getAllCommands(String presetId) {
    List<VanillaCommand> commands = new ArrayList<>();
    for (CommandGroup group : getGroups(presetId)) {
      commands.addAll(group.commands);
    }
    return commands;
  }

  public static class CommandGroup {

    public final String nameKey;
    public final List<VanillaCommand> commands;

    public CommandGroup(String nameKey) {
      this.nameKey = nameKey;
      this.commands = new ArrayList<>();
    }

    public CommandGroup add(VanillaCommand cmd) {
      commands.add(cmd);
      return this;
    }
  }

  public static class VanillaCommand {

    public final String nameKey;
    public final String command;
    public final String description;
    public final Integer minValue;
    public final Integer maxValue;
    public final int[] quickValues;
    public final String[] quickStrValues;

    public VanillaCommand(String nameKey, String command, String description, Integer minValue,
        Integer maxValue, int[] quickValues, String[] quickStrValues) {
      this.nameKey = nameKey;
      this.command = command;
      this.description = description;
      this.minValue = minValue;
      this.maxValue = maxValue;
      this.quickValues = quickValues;
      this.quickStrValues = quickStrValues;
    }

    public Component getName() {
      return Component.translatable(nameKey);
    }

    public Component getDescription() {
      if (description != null && !description.isEmpty()) {
        return Component.translatable(description);
      }
      return null;
    }
  }
}
