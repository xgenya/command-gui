package com.remrin.client.gui;

import com.remrin.client.config.CommandConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class EditCommandScreen extends Screen {
	private static final String[] TYPE_KEYS = {
		"screen.command-gui.type.player_all",
		"screen.command-gui.type.player_other",
		"screen.command-gui.type.player_fake",
		"screen.command-gui.type.text",
		"screen.command-gui.type.number",
		"screen.command-gui.type.coord"
	};
	
	private static final String[] PLACEHOLDERS = {
		"{player_all}",
		"{player}",
		"{player_fake}",
		"{name}",
		"{number}",
		"{coords}"
	};

	private final CommandGUIScreen parent;
	private final String originalName;
	private final String originalCommand;
	private final String originalDescription;
	private EditBox nameField;
	private EditBox descriptionField;
	private EditBox commandField;
	private CommandSuggestions commandSuggestions;

	public EditCommandScreen(CommandGUIScreen parent, String name, CommandConfig.CommandEntry entry) {
		super(Component.translatable("screen.command-gui.edit_title"));
		this.parent = parent;
		this.originalName = name;
		this.originalCommand = entry.command;
		this.originalDescription = entry.description;
	}

	@Override
	protected void init() {
		super.init();

		int centerX = this.width / 2;
		int centerY = this.height / 2;

		nameField = new EditBox(this.font, centerX - 100, centerY - 70, 200, 20,
				Component.translatable("screen.command-gui.name"));
		nameField.setMaxLength(50);
		nameField.setValue(originalName);
		this.addRenderableWidget(nameField);
		this.setInitialFocus(nameField);

		descriptionField = new EditBox(this.font, centerX - 100, centerY - 30, 200, 20,
				Component.translatable("screen.command-gui.description"));
		descriptionField.setMaxLength(100);
		descriptionField.setValue(originalDescription != null ? originalDescription : "");
		descriptionField.setHint(Component.translatable("screen.command-gui.description_hint"));
		this.addRenderableWidget(descriptionField);

		int typeY = centerY + 10;
		int btnWidth = 32;
		int gap = 2;
		int totalWidth = btnWidth * TYPE_KEYS.length + gap * (TYPE_KEYS.length - 1);
		int startX = centerX - totalWidth / 2;
		
		for (int i = 0; i < TYPE_KEYS.length; i++) {
			final int index = i;
			Button typeBtn = Button.builder(
					Component.translatable(TYPE_KEYS[i]),
					btn -> appendPlaceholder(index)
			).bounds(startX + i * (btnWidth + gap), typeY, btnWidth, 20).build();
			this.addRenderableWidget(typeBtn);
		}

		commandField = new EditBox(this.font, 4, this.height - 12, this.width - 8, 12,
				Component.translatable("screen.command-gui.command"));
		commandField.setMaxLength(256);
		commandField.setValue(originalCommand != null ? originalCommand : "");
		commandField.setBordered(false);
		this.addRenderableWidget(commandField);

		this.commandSuggestions = new CommandSuggestions(this.minecraft, this, commandField,
				this.font, false, false, 1, 10, true, -805306368);
		this.commandSuggestions.setAllowSuggestions(true);
		this.commandSuggestions.updateCommandInfo();

		commandField.setResponder(text -> this.commandSuggestions.updateCommandInfo());
	}

	private void appendPlaceholder(int index) {
		String placeholder = PLACEHOLDERS[index];
		String current = commandField.getValue();
		if (!current.isEmpty() && !current.endsWith(" ")) {
			current += " ";
		}
		commandField.setValue(current + placeholder);
		this.setFocused(commandField);
	}

	private void saveAndClose() {
		String newName = nameField.getValue().trim();
		String newCommand = commandField.getValue().trim();
		String newDescription = descriptionField.getValue().trim();
		if (!newName.isEmpty() && !newCommand.isEmpty()) {
			if (!newName.equals(originalName)) {
				CommandConfig.removeCommand(originalName);
			}
			CommandConfig.updateCommand(newName, newCommand, newDescription);
			parent.refresh();
			this.minecraft.setScreen(parent);
		}
	}

	@Override
	public void resize(int width, int height) {
		String name = this.nameField.getValue();
		String description = this.descriptionField.getValue();
		String command = this.commandField.getValue();
		super.resize(width, height);
		this.nameField.setValue(name);
		this.descriptionField.setValue(description);
		this.commandField.setValue(command);
		this.commandSuggestions.updateCommandInfo();
	}

	@Override
	public boolean keyPressed(KeyEvent keyEvent) {
		int keyCode = keyEvent.key();
		
		if (this.commandSuggestions.keyPressed(keyEvent)) {
			return true;
		}
		
		if (keyCode == GLFW.GLFW_KEY_TAB) {
			return true;
		}
		
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			if (commandField.isFocused() && !commandField.getValue().trim().isEmpty()) {
				saveAndClose();
				return true;
			} else if (nameField.isFocused() && !nameField.getValue().trim().isEmpty()) {
				this.setFocused(descriptionField);
				return true;
			} else if (descriptionField.isFocused()) {
				this.setFocused(commandField);
				return true;
			}
			return true;
		}
		
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			this.minecraft.setScreen(parent);
			return true;
		}
		
		return super.keyPressed(keyEvent);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (this.commandSuggestions.mouseScrolled(scrollY)) {
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent mouseEvent, boolean focused) {
		if (this.commandSuggestions.mouseClicked(mouseEvent)) {
			return true;
		}
		return super.mouseClicked(mouseEvent, focused);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		
		int centerX = this.width / 2;
		int centerY = this.height / 2;
		
		guiGraphics.drawCenteredString(this.font, this.title, centerX, centerY - 100, 0xFFFFFFFF);
		
		guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.name"),
				centerX - 100, centerY - 82, 0xFFAAAAAA);
		
		guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.description"),
				centerX - 100, centerY - 42, 0xFFAAAAAA);
		
		int typeY = centerY + 10;
		guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.placeholder_label"),
				centerX - 100, typeY - 12, 0xFFAAAAAA);
		
		guiGraphics.fill(2, this.height - 14, this.width - 2, this.height - 2, 0x80000000);
		guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.command"),
				4, this.height - 24, 0xFFAAAAAA);
		
		guiGraphics.drawCenteredString(this.font, 
				Component.translatable("screen.command-gui.enter_to_save"),
				centerX, typeY + 30, 0xFF888888);
		
		this.commandSuggestions.render(guiGraphics, mouseX, mouseY);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
