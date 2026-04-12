package com.remrin.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TimedKillSetupScreen extends BaseParentedScreen<Screen> {
	private final String playerName;
	private EditBox hoursField;
	private EditBox minutesField;
	private EditBox secondsField;
	
	private int hours = 0;
	private int minutes = 0;
	private int seconds = 10;

	public TimedKillSetupScreen(Screen parent, String playerName) {
		super(Component.translatable("screen.command-gui.fakeplayer.timed.kill.title"), parent);
		this.playerName = playerName;
	}

	@Override
	protected void init() {
		super.init();

		int centerX = this.width / 2;
		int centerY = this.height / 2;
		int timeFieldWidth = 40;

		int y = centerY - 30;
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
					if (hours > 0 || minutes > 0 || seconds > 0) {
						TimedTaskManager.addKillTask(playerName, hours, minutes, seconds);
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

		guiGraphics.drawCenteredString(this.font, this.title, centerX, centerY - 70, 0xFFFFFFFF);
		
		guiGraphics.drawCenteredString(this.font, 
				Component.literal(playerName),
				centerX, centerY - 55, 0xFF55FF55);

		int y = centerY - 30;
		guiGraphics.drawCenteredString(this.font, 
				Component.translatable("screen.command-gui.fakeplayer.timed.time"),
				centerX, y - 12, 0xFFAAAAAA);
		
		int timeFieldWidth = 40;
		int timeStartX = centerX - (timeFieldWidth * 3 + 20) / 2;
		
		guiGraphics.drawString(this.font, ":", timeStartX + timeFieldWidth + 2, y + 6, 0xFFFFFFFF);
		guiGraphics.drawString(this.font, ":", timeStartX + timeFieldWidth * 2 + 12, y + 6, 0xFFFFFFFF);
	}
}
