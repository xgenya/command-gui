package com.remrin.client.gui;

import com.remrin.client.config.CommandConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.List;

public class AddCommandScreen extends BaseCommandEditorScreen {
	private final String initialCategoryId;
	private List<CommandConfig.Category> categories;
	private int selectedCategoryIndex = 0;
	private Button categoryButton;

	public AddCommandScreen(CommandGUIScreen parent) {
		this(parent, null);
	}

	public AddCommandScreen(CommandGUIScreen parent, String initialCategoryId) {
		super(Component.translatable("screen.command-gui.add_title"), parent);
		this.initialCategoryId = initialCategoryId;
	}

	@Override
	protected int getFieldStartY(int centerY) {
		return centerY - 40;
	}

	@Override
	protected int getTitleY(int centerY) {
		return centerY - 55;
	}

	@Override
	protected String getInitialName() {
		return "";
	}

	@Override
	protected String getInitialDescription() {
		return "";
	}

	@Override
	protected String getInitialCommand() {
		return "";
	}

	@Override
	protected boolean showNameHint() {
		return true;
	}

	@Override
	protected Component getCommandHint() {
		return Component.translatable("screen.command-gui.command_hint");
	}

	@Override
	protected void init() {
		categories = CommandConfig.getCategories();
		if (initialCategoryId != null) {
			for (int i = 0; i < categories.size(); i++) {
				if (categories.get(i).id.equals(initialCategoryId)) {
					selectedCategoryIndex = i;
					break;
				}
			}
		}
		super.init();
	}

	@Override
	protected int initExtraRow(int fieldX, int currentY) {
		if (categories.size() > 1) {
			int newY = currentY + ROW_GAP;
			categoryButton = Button.builder(getCategoryDisplayName(), btn -> cycleCategory())
					.bounds(fieldX, newY, CONTENT_WIDTH, INPUT_HEIGHT).build();
			this.addRenderableWidget(categoryButton);
			return newY;
		}
		return currentY;
	}

	@Override
	protected int renderExtraLabel(GuiGraphics guiGraphics, int labelX, int currentY) {
		if (categories.size() > 1) {
			int newY = currentY + ROW_GAP;
			guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.category"),
					labelX - this.font.width(Component.translatable("screen.command-gui.category")), newY + 4, 0xFFAAAAAA);
			return newY;
		}
		return currentY;
	}

	private int savedCategoryIndex;

	@Override
	protected void onBeforeResize() {
		savedCategoryIndex = selectedCategoryIndex;
	}

	@Override
	protected void onAfterResize() {
		selectedCategoryIndex = savedCategoryIndex;
		if (categoryButton != null) {
			categoryButton.setMessage(getCategoryDisplayName());
		}
	}

	private void cycleCategory() {
		selectedCategoryIndex = (selectedCategoryIndex + 1) % categories.size();
		categoryButton.setMessage(getCategoryDisplayName());
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

	@Override
	protected void performSave() {
		String name = nameField.getValue().trim();
		String command = commandField.getValue().trim();
		String description = descriptionField.getValue().trim();
		String categoryId = categories.isEmpty() ? "default" : categories.get(selectedCategoryIndex).id;
		CommandConfig.addCommand(categoryId, name, command, description);
	}
}
