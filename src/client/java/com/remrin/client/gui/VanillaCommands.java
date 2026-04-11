package com.remrin.client.gui;

import com.remrin.client.config.PresetConfig;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class VanillaCommands {

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
		public final Integer minValue;
		public final Integer maxValue;
		public final int[] quickValues;
		
		public VanillaCommand(String nameKey, String command, Integer minValue, Integer maxValue, int[] quickValues) {
			this.nameKey = nameKey;
			this.command = command;
			this.minValue = minValue;
			this.maxValue = maxValue;
			this.quickValues = quickValues;
		}
		
		public Component getName() {
			return Component.translatable(nameKey);
		}
	}
	
	public static List<CommandGroup> getGroups(String presetId) {
		List<CommandGroup> groups = new ArrayList<>();
		
		PresetConfig.Preset preset = PresetConfig.getPreset(presetId);
		if (preset == null) {
			return groups;
		}
		
		for (PresetConfig.CommandGroup pg : preset.groups) {
			CommandGroup group = new CommandGroup(pg.nameKey);
			for (PresetConfig.PresetCommand pc : pg.commands) {
				group.add(new VanillaCommand(pc.nameKey, pc.command, pc.minValue, pc.maxValue, pc.quickValues));
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
}
