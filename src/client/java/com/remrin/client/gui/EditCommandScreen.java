package com.remrin.client.gui;

import com.remrin.client.config.CommandConfig;
import net.minecraft.network.chat.Component;

import java.util.List;

public class EditCommandScreen extends BaseCommandEditorScreen {
	private final String originalName;
	private final String originalCommand;
	private final List<String> originalCommands;
	private final String originalDescription;

	public EditCommandScreen(CommandGUIScreen parent, String name, CommandConfig.CommandEntry entry) {
		super(Component.translatable("screen.command-gui.edit_title"), parent);
		this.originalName = name;
		this.originalCommands = entry.getCommands();
		this.originalCommand = String.join("\n", originalCommands);
		this.originalDescription = entry.description;
	}

	@Override
	protected int getFieldStartY(int centerY) {
		return centerY - 30;
	}

	@Override
	protected int getTitleY(int centerY) {
		return centerY - 45;
	}

	@Override
	protected String getInitialName() {
		return originalName;
	}

	@Override
	protected String getInitialDescription() {
		return originalDescription != null ? originalDescription : "";
	}

	@Override
	protected String getInitialCommand() {
		return originalCommand != null ? originalCommand : "";
	}

	@Override
	protected void performSave() {
		String newName = nameField.getValue().trim();
		String newCommand = commandField.getValue().trim();
		String newDescription = descriptionField.getValue().trim();

		if (!newName.equals(originalName)) {
			String categoryId = CommandConfig.findCommandCategory(originalName);
			CommandConfig.removeCommand(originalName);
			CommandConfig.addCommand(categoryId != null ? categoryId : "default", newName, newCommand, newDescription);
		} else {
			CommandConfig.updateCommand(newName, newCommand, newDescription);
		}
	}
}
