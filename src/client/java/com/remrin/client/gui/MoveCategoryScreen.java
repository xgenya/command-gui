package com.remrin.client.gui;

import com.remrin.client.config.CommandConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class MoveCategoryScreen extends BaseParentedScreen<CommandGUIScreen> {
	private final String commandName;

	public MoveCategoryScreen(CommandGUIScreen parent, String commandName) {
		super(Component.translatable("screen.command-gui.move_category_title"), parent);
		this.commandName = commandName;
	}

	@Override
	protected void init() {
		super.init();

		int centerX = this.width / 2;
		int startY = 30;

		List<CommandConfig.Category> categories = CommandConfig.getCategories();
		String currentCategoryId = CommandConfig.findCommandCategory(commandName);

		for (int i = 0; i < categories.size(); i++) {
			CommandConfig.Category cat = categories.get(i);
			if (cat.id.equals(currentCategoryId)) continue;

			Component btnText = cat.getDisplayName() != null
					? Component.literal(cat.getDisplayName())
					: Component.translatable(cat.nameKey);

			final String targetCategoryId = cat.id;
			Button catBtn = Button.builder(btnText, btn -> {
				CommandConfig.moveCommand(commandName, targetCategoryId);
				parent.refresh();
				this.minecraft.setScreen(parent);
			}).bounds(centerX - 75, startY, 150, 20).build();
			this.addRenderableWidget(catBtn);
			startY += 24;
		}

		// Cancel button
		this.addRenderableWidget(Button.builder(
				Component.translatable("screen.command-gui.cancel"),
				btn -> this.minecraft.setScreen(parent)
		).bounds(centerX - 50, startY + 10, 100, 20).build());
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);
	}

	@Override
	public boolean keyPressed(KeyEvent keyEvent) {
		if (keyEvent.key() == GLFW.GLFW_KEY_ESCAPE) {
			this.minecraft.setScreen(parent);
			return true;
		}
		return super.keyPressed(keyEvent);
	}
}
