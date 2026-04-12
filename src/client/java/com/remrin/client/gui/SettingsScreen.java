package com.remrin.client.gui;

import com.remrin.client.config.SettingsConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SettingsScreen extends BaseParentedScreen<Screen> {
	private Checkbox showVanillaCheckbox;
	private Checkbox showCarpetCheckbox;
	private Checkbox showFakePlayerCheckbox;

	public SettingsScreen(Screen parent) {
		super(Component.translatable("screen.command-gui.settings.title"), parent);
	}

	@Override
	protected void init() {
		super.init();

		int centerX = this.width / 2;
		int centerY = this.height / 2;
		int contentWidth = 200;

		int y = centerY - 50;
		
		showVanillaCheckbox = Checkbox.builder(
				Component.translatable("screen.command-gui.settings.show_vanilla"),
				this.font
		).pos(centerX - contentWidth / 2, y).selected(SettingsConfig.getBoolean("show_vanilla_commands")).build();
		this.addRenderableWidget(showVanillaCheckbox);

		y += 25;
		showCarpetCheckbox = Checkbox.builder(
				Component.translatable("screen.command-gui.settings.show_carpet"),
				this.font
		).pos(centerX - contentWidth / 2, y).selected(SettingsConfig.getBoolean("show_carpet_commands")).build();
		this.addRenderableWidget(showCarpetCheckbox);

		y += 25;
		showFakePlayerCheckbox = Checkbox.builder(
				Component.translatable("screen.command-gui.settings.show_fakeplayer"),
				this.font
		).pos(centerX - contentWidth / 2, y).selected(SettingsConfig.getBoolean("show_fakeplayer_tab")).build();
		this.addRenderableWidget(showFakePlayerCheckbox);

		y += 45;
		this.addRenderableWidget(Button.builder(
				Component.translatable("screen.command-gui.settings.save"),
				btn -> saveAndClose()
		).bounds(centerX - 102, y, 100, 20).build());

		this.addRenderableWidget(Button.builder(
				Component.translatable("screen.command-gui.back"),
				btn -> this.minecraft.setScreen(parent)
		).bounds(centerX + 2, y, 100, 20).build());
	}

	private void saveAndClose() {
		SettingsConfig.setBoolean("show_vanilla_commands", showVanillaCheckbox.selected());
		SettingsConfig.setBoolean("show_carpet_commands", showCarpetCheckbox.selected());
		SettingsConfig.setBoolean("show_fakeplayer_tab", showFakePlayerCheckbox.selected());
		SettingsConfig.save();
		this.minecraft.setScreen(null);
		this.minecraft.setScreen(new CommandGUIScreen());
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);

		int centerX = this.width / 2;
		int centerY = this.height / 2;

		guiGraphics.drawCenteredString(this.font, this.title, centerX, centerY - 80, 0xFFFFFFFF);
	}
}
