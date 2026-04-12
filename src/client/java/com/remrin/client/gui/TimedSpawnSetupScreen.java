package com.remrin.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TimedSpawnSetupScreen extends BaseParentedScreen<Screen> {
	private EditBox nameField;
	private EditBox hoursField;
	private EditBox minutesField;
	private EditBox secondsField;
	
	private String playerName = "Bot_1";
	private int hours = 0;
	private int minutes = 0;
	private int seconds = 10;

	public TimedSpawnSetupScreen(Screen parent) {
		super(Component.translatable("screen.command-gui.fakeplayer.timed.spawn.title"), parent);
	}

	@Override
	protected void init() {
		super.init();

		int centerX = this.width / 2;
		int centerY = this.height / 2;
		int fieldWidth = 150;
		int timeFieldWidth = 40;

		int y = centerY - 60;
		
		nameField = new EditBox(this.font, centerX - fieldWidth / 2, y, fieldWidth, 20,
				Component.translatable("screen.command-gui.fakeplayer.timed.name"));
		nameField.setMaxLength(20);
		nameField.setValue(playerName);
		nameField.setResponder(s -> playerName = s);
		this.addRenderableWidget(nameField);

		y += 40;
		int timeStartX = centerX - (timeFieldWidth * 3 + 20) / 2;
		
		hoursField = new EditBox(this.font, timeStartX, y, timeFieldWidth, 20,
				Component.literal("H"));
		hoursField.setMaxLength(2);
		hoursField.setValue(String.valueOf(hours));
		hoursField.setResponder(s -> {
			try {
				hours = Math.max(0, Math.min(23, Integer.parseInt(s)));
			} catch (NumberFormatException e) {
				hours = 0;
			}
		});
		this.addRenderableWidget(hoursField);
		
		minutesField = new EditBox(this.font, timeStartX + timeFieldWidth + 10, y, timeFieldWidth, 20,
				Component.literal("M"));
		minutesField.setMaxLength(2);
		minutesField.setValue(String.valueOf(minutes));
		minutesField.setResponder(s -> {
			try {
				minutes = Math.max(0, Math.min(59, Integer.parseInt(s)));
			} catch (NumberFormatException e) {
				minutes = 0;
			}
		});
		this.addRenderableWidget(minutesField);
		
		secondsField = new EditBox(this.font, timeStartX + (timeFieldWidth + 10) * 2, y, timeFieldWidth, 20,
				Component.literal("S"));
		secondsField.setMaxLength(2);
		secondsField.setValue(String.valueOf(seconds));
		secondsField.setResponder(s -> {
			try {
				seconds = Math.max(0, Math.min(59, Integer.parseInt(s)));
			} catch (NumberFormatException e) {
				seconds = 0;
			}
		});
		this.addRenderableWidget(secondsField);

		y += 50;
		this.addRenderableWidget(Button.builder(
				Component.translatable("screen.command-gui.save"),
				btn -> {
					if (!playerName.isEmpty() && (hours > 0 || minutes > 0 || seconds > 0)) {
						TimedTaskManager.addSpawnTask(playerName, hours, minutes, seconds);
						this.minecraft.setScreen(parent);
					}
				}
		).bounds(centerX - 102, y, 100, 20).build());

		this.addRenderableWidget(Button.builder(
				Component.translatable("screen.command-gui.cancel"),
				btn -> this.minecraft.setScreen(parent)
		).bounds(centerX + 2, y, 100, 20).build());
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);

		int centerX = this.width / 2;
		int centerY = this.height / 2;

		guiGraphics.drawCenteredString(this.font, this.title, centerX, centerY - 90, 0xFFFFFFFF);

		int y = centerY - 60;
		guiGraphics.drawCenteredString(this.font, 
				Component.translatable("screen.command-gui.fakeplayer.timed.name"),
				centerX, y - 12, 0xFFAAAAAA);

		y += 40;
		guiGraphics.drawCenteredString(this.font, 
				Component.translatable("screen.command-gui.fakeplayer.timed.time"),
				centerX, y - 12, 0xFFAAAAAA);
		
		int timeFieldWidth = 40;
		int timeStartX = centerX - (timeFieldWidth * 3 + 20) / 2;
		
		guiGraphics.drawString(this.font, ":", timeStartX + timeFieldWidth + 2, y + 6, 0xFFFFFFFF);
		guiGraphics.drawString(this.font, ":", timeStartX + timeFieldWidth * 2 + 12, y + 6, 0xFFFFFFFF);
	}
}
