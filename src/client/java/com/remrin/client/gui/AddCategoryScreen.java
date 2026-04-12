package com.remrin.client.gui;

import com.remrin.client.config.CommandConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class AddCategoryScreen extends BaseParentedScreen<CommandGUIScreen> {
	private EditBox nameField;

	public AddCategoryScreen(CommandGUIScreen parent) {
		super(Component.translatable("screen.command-gui.add_category_title"), parent);
	}

	@Override
	protected void init() {
		super.init();

		int centerX = this.width / 2;
		int centerY = this.height / 2;

		// Name input
		nameField = new EditBox(this.font, centerX - 100, centerY - 20, 200, 20,
				Component.translatable("screen.command-gui.category_name_hint"));
		nameField.setHint(Component.translatable("screen.command-gui.category_name_hint"));
		nameField.setMaxLength(50);
		this.addRenderableWidget(nameField);
		this.setInitialFocus(nameField);

		// Save button
		this.addRenderableWidget(Button.builder(
				Component.translatable("screen.command-gui.save"),
				button -> saveCategory()
		).bounds(centerX - 102, centerY + 15, 100, 20).build());

		// Cancel button
		this.addRenderableWidget(Button.builder(
				Component.translatable("screen.command-gui.cancel"),
				button -> this.minecraft.setScreen(parent)
		).bounds(centerX + 2, centerY + 15, 100, 20).build());
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);

		int centerX = this.width / 2;
		int centerY = this.height / 2;

		// Title
		guiGraphics.drawCenteredString(this.font, this.title, centerX, centerY - 55, 0xFFFFFF);

		// Description
		guiGraphics.drawCenteredString(this.font,
				Component.translatable("screen.command-gui.add_category_desc"),
				centerX, centerY - 40, 0x888888);

		// Label
		guiGraphics.drawString(this.font,
				Component.translatable("screen.command-gui.category_name"),
				centerX - 100, centerY - 32, 0xAAAAAA);

		// Hint
		guiGraphics.drawCenteredString(this.font,
				Component.translatable("screen.command-gui.enter_to_save"),
				centerX, centerY + 45, 0x888888);
	}

	@Override
	public boolean keyPressed(KeyEvent keyEvent) {
		int keyCode = keyEvent.key();
		
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			this.minecraft.setScreen(parent);
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			saveCategory();
			return true;
		}
		return super.keyPressed(keyEvent);
	}

	private void saveCategory() {
		String name = nameField.getValue().trim();
		if (name.isEmpty()) {
			return;
		}

		// Generate ID from name (lowercase, replace spaces with underscores)
		String id = name.toLowerCase().replaceAll("\\s+", "_").replaceAll("[^a-z0-9_]", "");
		if (id.isEmpty()) {
			id = "category_" + System.currentTimeMillis();
		}

		// Create translation key
		String nameKey = "screen.command-gui.custom." + id;

		CommandConfig.addCategory(id, nameKey, name);
		
		if (parent != null) {
			parent.refresh();
		}
		this.minecraft.setScreen(parent);
	}
}
