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

import java.util.List;

public class AddCommandScreen extends Screen {
	private static final String[] TYPE_KEYS = {
		"screen.command-gui.type.player_all_full",
		"screen.command-gui.type.player_other_full",
		"screen.command-gui.type.player_fake_full",
		"screen.command-gui.type.text_full",
		"screen.command-gui.type.number_full",
		"screen.command-gui.type.coord_full"
	};
	
	private static final String[] PLACEHOLDERS = {
		"{player_all}",
		"{player}",
		"{player_fake}",
		"{name}",
		"{number}",
		"{coords}"
	};
	
	private static final int PLACEHOLDER_BTN_HEIGHT = 16;
	private static final int PLACEHOLDER_BTNS_PER_ROW = 3;
	private static final int INPUT_HEIGHT = 16;
	private static final int CONTENT_WIDTH = 170;
	private static final int LABEL_WIDTH = 45;
	private static final int ROW_GAP = 20;
	private static final int Y_OFFSET = -20;
	private static final int BTN_GAP = 4;

	private final CommandGUIScreen parent;
	private final String initialCategoryId;
	private EditBox nameField;
	private EditBox descriptionField;
	private EditBox commandField;
	private CommandSuggestions commandSuggestions;
	
	private List<CommandConfig.Category> categories;
	private int selectedCategoryIndex = 0;
	private Button categoryButton;

	public AddCommandScreen(CommandGUIScreen parent) {
		this(parent, null);
	}

	public AddCommandScreen(CommandGUIScreen parent, String initialCategoryId) {
		super(Component.translatable("screen.command-gui.add_title"));
		this.parent = parent;
		this.initialCategoryId = initialCategoryId;
	}

	@Override
	protected void init() {
		super.init();
		
		categories = CommandConfig.getCategories();
		
		// Set initial category index based on initialCategoryId
		if (initialCategoryId != null) {
			for (int i = 0; i < categories.size(); i++) {
				if (categories.get(i).id.equals(initialCategoryId)) {
					selectedCategoryIndex = i;
					break;
				}
			}
		}

		int centerX = this.width / 2;
		int centerY = this.height / 2 + Y_OFFSET;
		int fieldX = centerX - CONTENT_WIDTH / 2 + LABEL_WIDTH;

		int currentY = centerY - 40;
		nameField = new EditBox(this.font, fieldX, currentY, CONTENT_WIDTH, INPUT_HEIGHT,
				Component.translatable("screen.command-gui.name"));
		nameField.setMaxLength(50);
		nameField.setHint(Component.translatable("screen.command-gui.name_hint"));
		this.addRenderableWidget(nameField);
		this.setInitialFocus(nameField);

		currentY += ROW_GAP;
		descriptionField = new EditBox(this.font, fieldX, currentY, CONTENT_WIDTH, INPUT_HEIGHT,
				Component.translatable("screen.command-gui.description"));
		descriptionField.setMaxLength(100);
		descriptionField.setHint(Component.translatable("screen.command-gui.description_hint"));
		this.addRenderableWidget(descriptionField);

		if (categories.size() > 1) {
			currentY += ROW_GAP;
			categoryButton = Button.builder(
					getCategoryDisplayName(),
					btn -> cycleCategory()
			).bounds(fieldX, currentY, CONTENT_WIDTH, INPUT_HEIGHT).build();
			this.addRenderableWidget(categoryButton);
		}

		currentY += ROW_GAP + 4;
		int placeholderBtnWidth = (CONTENT_WIDTH - BTN_GAP * (PLACEHOLDER_BTNS_PER_ROW - 1)) / PLACEHOLDER_BTNS_PER_ROW;
		
		for (int i = 0; i < TYPE_KEYS.length; i++) {
			final int index = i;
			int row = i / PLACEHOLDER_BTNS_PER_ROW;
			int col = i % PLACEHOLDER_BTNS_PER_ROW;
			int btnX = fieldX + col * (placeholderBtnWidth + BTN_GAP);
			int btnY = currentY + row * (PLACEHOLDER_BTN_HEIGHT + 2);
			
			Button typeBtn = Button.builder(
					Component.translatable(TYPE_KEYS[i]),
					btn -> appendPlaceholder(index)
			).bounds(btnX, btnY, placeholderBtnWidth, PLACEHOLDER_BTN_HEIGHT).build();
			this.addRenderableWidget(typeBtn);
		}

		commandField = new EditBox(this.font, 4, this.height - 12, this.width - 8, 12,
				Component.translatable("screen.command-gui.command"));
		commandField.setMaxLength(256);
		commandField.setHint(Component.translatable("screen.command-gui.command_hint"));
		commandField.setBordered(false);
		this.addRenderableWidget(commandField);

		this.commandSuggestions = new CommandSuggestions(this.minecraft, this, commandField,
				this.font, false, false, 1, 10, true, -805306368);
		this.commandSuggestions.setAllowSuggestions(true);
		this.commandSuggestions.updateCommandInfo();

		commandField.setResponder(text -> this.commandSuggestions.updateCommandInfo());
	}

	private void cycleCategory() {
		selectedCategoryIndex = (selectedCategoryIndex + 1) % categories.size();
		categoryButton.setMessage(getCategoryDisplayName());
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

	private Component getCategoryDisplayName() {
		if (categories.isEmpty()) {
			return Component.translatable("screen.command-gui.category.default");
		}
		CommandConfig.Category cat = categories.get(selectedCategoryIndex);
		if (cat.getDisplayName() != null) {
			return Component.literal(cat.getDisplayName());
		}
		return Component.translatable(cat.nameKey);
	}

	private void saveAndClose() {
		String name = nameField.getValue().trim();
		String command = commandField.getValue().trim();
		String description = descriptionField.getValue().trim();
		if (!name.isEmpty() && !command.isEmpty()) {
			String categoryId = categories.isEmpty() ? "default" : categories.get(selectedCategoryIndex).id;
			CommandConfig.addCommand(categoryId, name, command, description);
			parent.refresh();
			this.minecraft.setScreen(parent);
		}
	}

	@Override
	public void resize(int width, int height) {
		String name = this.nameField.getValue();
		String description = this.descriptionField.getValue();
		String command = this.commandField.getValue();
		int catIndex = this.selectedCategoryIndex;
		super.resize(width, height);
		this.nameField.setValue(name);
		this.descriptionField.setValue(description);
		this.commandField.setValue(command);
		this.selectedCategoryIndex = catIndex;
		if (categoryButton != null) {
			this.categoryButton.setMessage(getCategoryDisplayName());
		}
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
			String name = nameField.getValue().trim();
			String command = commandField.getValue().trim();
			if (!name.isEmpty() && !command.isEmpty()) {
				saveAndClose();
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
		int centerY = this.height / 2 + Y_OFFSET;
		int labelX = centerX - CONTENT_WIDTH / 2 - 4;
		
		guiGraphics.drawCenteredString(this.font, this.title, centerX, centerY - 55, 0xFFFFFFFF);
		
		// Labels on the left side of input fields
		int currentY = centerY - 40;
		guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.name"),
				labelX - this.font.width(Component.translatable("screen.command-gui.name")), currentY + 4, 0xFFAAAAAA);
		
		currentY += ROW_GAP;
		guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.description"),
				labelX - this.font.width(Component.translatable("screen.command-gui.description")), currentY + 4, 0xFFAAAAAA);
		
		if (categories.size() > 1) {
			currentY += ROW_GAP;
			guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.category"),
					labelX - this.font.width(Component.translatable("screen.command-gui.category")), currentY + 4, 0xFFAAAAAA);
		}
		
		currentY += ROW_GAP + 4;
		guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.placeholder_label"),
				labelX - this.font.width(Component.translatable("screen.command-gui.placeholder_label")), currentY + 4, 0xFFAAAAAA);
		
		guiGraphics.fill(2, this.height - 14, this.width - 2, this.height - 2, 0x80000000);
		guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.command"),
				4, this.height - 24, 0xFFAAAAAA);
		
		int rows = (TYPE_KEYS.length + PLACEHOLDER_BTNS_PER_ROW - 1) / PLACEHOLDER_BTNS_PER_ROW;
		guiGraphics.drawCenteredString(this.font, 
				Component.translatable("screen.command-gui.enter_to_save"),
				centerX, currentY + rows * (PLACEHOLDER_BTN_HEIGHT + 2) + 8, 0xFF888888);
		
		this.commandSuggestions.render(guiGraphics, mouseX, mouseY);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
