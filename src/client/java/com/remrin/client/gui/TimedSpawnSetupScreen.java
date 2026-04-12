package com.remrin.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TimedSpawnSetupScreen extends BaseParentedScreen<Screen> {
	private static final int TIME_FIELD_WIDTH = 45;
	private static final int COLON_GAP = 14;
	private static final int COORD_FIELD_WIDTH = 58;
	private static final int COORD_GAP = 6;
	// Approximate total content height from title top to save/cancel bottom (with coords).
	// Used to vertically center the layout with a slight upward bias.
	private static final int FULL_CONTENT_H = 205;

	private EditBox nameField;
	private EditBox hoursField;
	private EditBox minutesField;
	private EditBox secondsField;
	private EditBox xField, yField, zField;
	private Button posToggle;

	private String playerName = "Bot_1";
	private int hours = 0;
	private int minutes = 0;
	private int seconds = 10;
	private boolean useCurrentPos = true;
	private double spawnX = 0, spawnY = 64, spawnZ = 0;

	// Computed in init(); used by both init() and render() to keep positions in sync.
	private int titleY;

	public TimedSpawnSetupScreen(Screen parent) {
		super(Component.translatable("screen.command-gui.fakeplayer.timed.spawn.title"), parent);
	}

	private int computeTitleY() {
		// Center content with a slight upward bias (-10) so it sits above the screen midpoint.
		return Math.max(15, (this.height - FULL_CONTENT_H) / 2 - 10);
	}

	@Override
	protected void init() {
		super.init();

		int centerX = this.width / 2;
		titleY = computeTitleY();

		// Layout anchors relative to titleY (matching FULL_CONTENT_H breakdown)
		int nameFieldY   = titleY + 34;
		int timeFieldY   = nameFieldY + 45;
		int posToggleY   = timeFieldY + 42;
		int coordFieldY  = posToggleY + 28;
		int saveCancelY  = coordFieldY + 36;

		// Player name
		nameField = new EditBox(this.font, centerX - 75, nameFieldY, 150, 20,
				Component.translatable("screen.command-gui.fakeplayer.timed.name"));
		nameField.setMaxLength(20);
		nameField.setValue(playerName);
		nameField.setResponder(s -> playerName = s);
		this.addRenderableWidget(nameField);

		// Time fields (H : M : S)
		int totalTimeWidth = TIME_FIELD_WIDTH * 3 + COLON_GAP * 2;
		int timeStartX = centerX - totalTimeWidth / 2;

		hoursField = new EditBox(this.font, timeStartX, timeFieldY, TIME_FIELD_WIDTH, 20, Component.literal("H"));
		hoursField.setMaxLength(5);
		hoursField.setValue(String.valueOf(hours));
		hoursField.setFilter(s -> s.isEmpty() || s.matches("\\d+"));
		hoursField.setResponder(s -> { try { hours   = Math.max(0, Integer.parseInt(s)); } catch (NumberFormatException e) { hours   = 0; } });
		this.addRenderableWidget(hoursField);

		minutesField = new EditBox(this.font, timeStartX + TIME_FIELD_WIDTH + COLON_GAP, timeFieldY, TIME_FIELD_WIDTH, 20, Component.literal("M"));
		minutesField.setMaxLength(5);
		minutesField.setValue(String.valueOf(minutes));
		minutesField.setFilter(s -> s.isEmpty() || s.matches("\\d+"));
		minutesField.setResponder(s -> { try { minutes = Math.max(0, Integer.parseInt(s)); } catch (NumberFormatException e) { minutes = 0; } });
		this.addRenderableWidget(minutesField);

		secondsField = new EditBox(this.font, timeStartX + (TIME_FIELD_WIDTH + COLON_GAP) * 2, timeFieldY, TIME_FIELD_WIDTH, 20, Component.literal("S"));
		secondsField.setMaxLength(5);
		secondsField.setValue(String.valueOf(seconds));
		secondsField.setFilter(s -> s.isEmpty() || s.matches("\\d+"));
		secondsField.setResponder(s -> { try { seconds = Math.max(0, Integer.parseInt(s)); } catch (NumberFormatException e) { seconds = 0; } });
		this.addRenderableWidget(secondsField);

		// Position toggle
		posToggle = Button.builder(
				getPosToggleLabel(),
				btn -> {
					useCurrentPos = !useCurrentPos;
					posToggle.setMessage(getPosToggleLabel());
					updateCoordFieldVisibility();
				}
		).bounds(centerX - 90, posToggleY, 180, 20).build();
		this.addRenderableWidget(posToggle);

		// Coordinate fields (X Y Z)
		int coordTotalWidth = COORD_FIELD_WIDTH * 3 + COORD_GAP * 2;
		int coordStartX = centerX - coordTotalWidth / 2;

		xField = new EditBox(this.font, coordStartX, coordFieldY, COORD_FIELD_WIDTH, 20, Component.literal("X"));
		xField.setMaxLength(12);
		xField.setFilter(s -> s.isEmpty() || s.matches("-?\\d*\\.?\\d*"));
		xField.setResponder(s -> { try { spawnX = Double.parseDouble(s); } catch (NumberFormatException e) { spawnX = 0; } });
		this.addRenderableWidget(xField);

		yField = new EditBox(this.font, coordStartX + COORD_FIELD_WIDTH + COORD_GAP, coordFieldY, COORD_FIELD_WIDTH, 20, Component.literal("Y"));
		yField.setMaxLength(12);
		yField.setFilter(s -> s.isEmpty() || s.matches("-?\\d*\\.?\\d*"));
		yField.setResponder(s -> { try { spawnY = Double.parseDouble(s); } catch (NumberFormatException e) { spawnY = 64; } });
		this.addRenderableWidget(yField);

		zField = new EditBox(this.font, coordStartX + (COORD_FIELD_WIDTH + COORD_GAP) * 2, coordFieldY, COORD_FIELD_WIDTH, 20, Component.literal("Z"));
		zField.setMaxLength(12);
		zField.setFilter(s -> s.isEmpty() || s.matches("-?\\d*\\.?\\d*"));
		zField.setResponder(s -> { try { spawnZ = Double.parseDouble(s); } catch (NumberFormatException e) { spawnZ = 0; } });
		this.addRenderableWidget(zField);

		// Pre-fill coordinates from current player position
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null) {
			spawnX = mc.player.getX();
			spawnY = mc.player.getY();
			spawnZ = mc.player.getZ();
		}
		xField.setValue(String.format("%.1f", spawnX));
		yField.setValue(String.format("%.1f", spawnY));
		zField.setValue(String.format("%.1f", spawnZ));

		updateCoordFieldVisibility();

		// Save / Cancel
		this.addRenderableWidget(Button.builder(
				Component.translatable("screen.command-gui.save"),
				btn -> {
					if (!playerName.isEmpty() && (hours > 0 || minutes > 0 || seconds > 0)) {
						if (useCurrentPos) {
							TimedTaskManager.addSpawnTask(playerName, hours, minutes, seconds);
						} else {
							TimedTaskManager.addSpawnTask(playerName, hours, minutes, seconds, spawnX, spawnY, spawnZ);
						}
						this.minecraft.setScreen(parent);
					}
				}
		).bounds(centerX - 102, saveCancelY, 100, 20).build());

		this.addRenderableWidget(Button.builder(
				Component.translatable("screen.command-gui.cancel"),
				btn -> this.minecraft.setScreen(parent)
		).bounds(centerX + 2, saveCancelY, 100, 20).build());
	}

	private Component getPosToggleLabel() {
		return Component.translatable(useCurrentPos
				? "screen.command-gui.fakeplayer.timed.spawn.pos.current"
				: "screen.command-gui.fakeplayer.timed.spawn.pos.custom");
	}

	private void updateCoordFieldVisibility() {
		xField.visible = !useCurrentPos;
		xField.active = !useCurrentPos;
		yField.visible = !useCurrentPos;
		yField.active = !useCurrentPos;
		zField.visible = !useCurrentPos;
		zField.active = !useCurrentPos;
	}

	private String formatDuration(int totalSeconds) {
		if (totalSeconds <= 0) return "--";
		int h = totalSeconds / 3600;
		int m = (totalSeconds % 3600) / 60;
		int s = totalSeconds % 60;
		if (h > 0) return String.format("%dh %02dm %02ds", h, m, s);
		if (m > 0) return String.format("%dm %02ds", m, s);
		return s + "s";
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);

		int centerX = this.width / 2;
		int nameFieldY  = titleY + 34;
		int timeFieldY  = nameFieldY + 45;
		int coordFieldY = timeFieldY + 42 + 28;

		// Title
		guiGraphics.drawCenteredString(this.font, this.title, centerX, titleY, 0xFFFFFFFF);

		// Name label
		guiGraphics.drawCenteredString(this.font,
				Component.translatable("screen.command-gui.fakeplayer.timed.name"),
				centerX, nameFieldY - 12, 0xFFAAAAAA);

		// Time section label and column headers
		guiGraphics.drawCenteredString(this.font,
				Component.translatable("screen.command-gui.fakeplayer.timed.time"),
				centerX, timeFieldY - 24, 0xFFAAAAAA);

		int totalTimeWidth = TIME_FIELD_WIDTH * 3 + COLON_GAP * 2;
		int timeStartX = centerX - totalTimeWidth / 2;
		guiGraphics.drawCenteredString(this.font, "H", timeStartX + TIME_FIELD_WIDTH / 2, timeFieldY - 12, 0xFF888888);
		guiGraphics.drawCenteredString(this.font, "M", timeStartX + TIME_FIELD_WIDTH + COLON_GAP + TIME_FIELD_WIDTH / 2, timeFieldY - 12, 0xFF888888);
		guiGraphics.drawCenteredString(this.font, "S", timeStartX + (TIME_FIELD_WIDTH + COLON_GAP) * 2 + TIME_FIELD_WIDTH / 2, timeFieldY - 12, 0xFF888888);

		int colonY = timeFieldY + 6;
		guiGraphics.drawString(this.font, ":", timeStartX + TIME_FIELD_WIDTH + COLON_GAP / 2 - 2, colonY, 0xFFCCCCCC);
		guiGraphics.drawString(this.font, ":", timeStartX + TIME_FIELD_WIDTH + COLON_GAP + TIME_FIELD_WIDTH + COLON_GAP / 2 - 2, colonY, 0xFFCCCCCC);

		// Duration preview
		int totalSec = hours * 3600 + minutes * 60 + seconds;
		int previewColor = totalSec > 0 ? 0xFF55FFFF : 0xFF666666;
		guiGraphics.drawCenteredString(this.font,
				Component.literal(formatDuration(totalSec)),
				centerX, timeFieldY + 28, previewColor);

		// Coordinate column labels (only when using custom position)
		if (!useCurrentPos) {
			int coordTotalWidth = COORD_FIELD_WIDTH * 3 + COORD_GAP * 2;
			int coordStartX = centerX - coordTotalWidth / 2;
			guiGraphics.drawCenteredString(this.font, "X", coordStartX + COORD_FIELD_WIDTH / 2, coordFieldY - 10, 0xFF888888);
			guiGraphics.drawCenteredString(this.font, "Y", coordStartX + COORD_FIELD_WIDTH + COORD_GAP + COORD_FIELD_WIDTH / 2, coordFieldY - 10, 0xFF888888);
			guiGraphics.drawCenteredString(this.font, "Z", coordStartX + (COORD_FIELD_WIDTH + COORD_GAP) * 2 + COORD_FIELD_WIDTH / 2, coordFieldY - 10, 0xFF888888);
		}
	}
}
