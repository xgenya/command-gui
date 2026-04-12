package com.remrin.client.gui;

import com.remrin.client.config.CommandConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class AddFakePlayerCommandScreen extends BaseParentedScreen<CommandGUIScreen> {
	private static final int FIELD_WIDTH = 180;
	private static final int FIELD_HEIGHT = 16;
	private static final int ROW_GAP = 20;
	private static final int COORD_FIELD_WIDTH = 55;
	private static final int COORD_GAP = 5;

	private static final String[] DIMENSIONS = {
		"minecraft:overworld",
		"minecraft:the_nether",
		"minecraft:the_end"
	};
	private static final String[] DIMENSION_NAMES = {
		"screen.command-gui.fakeplayer.dim.overworld",
		"screen.command-gui.fakeplayer.dim.nether",
		"screen.command-gui.fakeplayer.dim.end"
	};

	private static final String[] GAMEMODES = {
		"survival", "creative", "adventure", "spectator"
	};
	private static final String[] GAMEMODE_NAMES = {
		"screen.command-gui.vanilla.gamemode.survival",
		"screen.command-gui.vanilla.gamemode.creative",
		"screen.command-gui.vanilla.gamemode.adventure",
		"screen.command-gui.vanilla.gamemode.spectator"
	};

	private static final String[] ACTIONS = {
		"", "attack continuous", "attack once", "use continuous", "use once",
		"jump continuous", "sneak", "sprint", "drop", "dropStack", "swapHands"
	};
	private static final String[] ACTION_NAMES = {
		"screen.command-gui.fakeplayer.action.none",
		"screen.command-gui.carpet.player.attack",
		"screen.command-gui.carpet.player.attack_once",
		"screen.command-gui.carpet.player.use",
		"screen.command-gui.carpet.player.use_once",
		"screen.command-gui.carpet.player.jump",
		"screen.command-gui.carpet.player.sneak",
		"screen.command-gui.carpet.player.sprint",
		"screen.command-gui.carpet.player.drop",
		"screen.command-gui.carpet.player.dropstack",
		"screen.command-gui.carpet.player.swaphands"
	};

	private final String initialCategoryId;
	private final String editingName;
	private final CommandConfig.CommandEntry editingEntry;

	// Fields
	private EditBox nameField;
	private EditBox descriptionField;
	private EditBox fakePlayerNameField;
	private Button positionModeButton;
	private EditBox xField, yField, zField;
	private EditBox yawField, pitchField;
	private Button dimensionButton;
	private Button gamemodeButton;
	private Button actionButton;
	private EditBox configField;

	// State
	private boolean useCurrentPosition = true;
	private int dimensionIndex = 0;
	private int gamemodeIndex = 0;
	private int actionIndex = 0;
	private double posX, posY, posZ;
	private float yaw, pitch;

	public AddFakePlayerCommandScreen(CommandGUIScreen parent, String initialCategoryId) {
		this(parent, initialCategoryId, null, null);
	}

	public AddFakePlayerCommandScreen(CommandGUIScreen parent, String initialCategoryId,
			String editingName, CommandConfig.CommandEntry editingEntry) {
		super(Component.translatable("screen.command-gui.add_fakeplayer_title"), parent);
		this.initialCategoryId = initialCategoryId;
		this.editingName = editingName;
		this.editingEntry = editingEntry;
	}

	@Override
	protected void init() {
		super.init();

		int centerX = this.width / 2;
		int fieldX = centerX - FIELD_WIDTH / 2;
		int currentY = 25;

		// Name field
		nameField = new EditBox(this.font, fieldX, currentY, FIELD_WIDTH, FIELD_HEIGHT,
				Component.translatable("screen.command-gui.name"));
		nameField.setMaxLength(50);
		nameField.setHint(Component.translatable("screen.command-gui.name_hint"));
		this.addRenderableWidget(nameField);

		// Description field
		currentY += ROW_GAP;
		descriptionField = new EditBox(this.font, fieldX, currentY, FIELD_WIDTH, FIELD_HEIGHT,
				Component.translatable("screen.command-gui.description"));
		descriptionField.setMaxLength(100);
		descriptionField.setHint(Component.translatable("screen.command-gui.description_hint"));
		this.addRenderableWidget(descriptionField);

		// Fake player name
		currentY += ROW_GAP + 8;
		fakePlayerNameField = new EditBox(this.font, fieldX, currentY, FIELD_WIDTH, FIELD_HEIGHT,
				Component.translatable("screen.command-gui.fakeplayer.playername"));
		fakePlayerNameField.setMaxLength(20);
		fakePlayerNameField.setValue("Bot_1");
		fakePlayerNameField.setHint(Component.translatable("screen.command-gui.fakeplayer.playername_hint"));
		this.addRenderableWidget(fakePlayerNameField);

		// Position mode toggle
		currentY += ROW_GAP + 4;
		positionModeButton = Button.builder(
				getPositionModeLabel(),
				btn -> {
					useCurrentPosition = !useCurrentPosition;
					positionModeButton.setMessage(getPositionModeLabel());
					updateCoordFieldVisibility();
					if (useCurrentPosition) {
						fillCurrentPosition();
					}
				}
		).bounds(fieldX, currentY, FIELD_WIDTH, 20).build();
		this.addRenderableWidget(positionModeButton);

		// Coordinate fields
		currentY += 24;
		int coordTotalWidth = COORD_FIELD_WIDTH * 3 + COORD_GAP * 2;
		int coordStartX = centerX - coordTotalWidth / 2;

		xField = new EditBox(this.font, coordStartX, currentY, COORD_FIELD_WIDTH, FIELD_HEIGHT, Component.literal("X"));
		xField.setMaxLength(12);
		xField.setFilter(s -> s.isEmpty() || s.matches("-?\\d*\\.?\\d*"));
		this.addRenderableWidget(xField);

		yField = new EditBox(this.font, coordStartX + COORD_FIELD_WIDTH + COORD_GAP, currentY, COORD_FIELD_WIDTH, FIELD_HEIGHT, Component.literal("Y"));
		yField.setMaxLength(12);
		yField.setFilter(s -> s.isEmpty() || s.matches("-?\\d*\\.?\\d*"));
		this.addRenderableWidget(yField);

		zField = new EditBox(this.font, coordStartX + (COORD_FIELD_WIDTH + COORD_GAP) * 2, currentY, COORD_FIELD_WIDTH, FIELD_HEIGHT, Component.literal("Z"));
		zField.setMaxLength(12);
		zField.setFilter(s -> s.isEmpty() || s.matches("-?\\d*\\.?\\d*"));
		this.addRenderableWidget(zField);

		// Yaw and Pitch fields
		currentY += ROW_GAP;
		int angleTotalWidth = COORD_FIELD_WIDTH * 2 + COORD_GAP;
		int angleStartX = centerX - angleTotalWidth / 2;

		yawField = new EditBox(this.font, angleStartX, currentY, COORD_FIELD_WIDTH, FIELD_HEIGHT, Component.literal("Yaw"));
		yawField.setMaxLength(10);
		yawField.setFilter(s -> s.isEmpty() || s.matches("-?\\d*\\.?\\d*"));
		this.addRenderableWidget(yawField);

		pitchField = new EditBox(this.font, angleStartX + COORD_FIELD_WIDTH + COORD_GAP, currentY, COORD_FIELD_WIDTH, FIELD_HEIGHT, Component.literal("Pitch"));
		pitchField.setMaxLength(10);
		pitchField.setFilter(s -> s.isEmpty() || s.matches("-?\\d*\\.?\\d*"));
		this.addRenderableWidget(pitchField);

		// Dimension selector
		currentY += ROW_GAP + 4;
		dimensionButton = Button.builder(
				getDimensionLabel(),
				btn -> {
					dimensionIndex = (dimensionIndex + 1) % DIMENSIONS.length;
					dimensionButton.setMessage(getDimensionLabel());
				}
		).bounds(fieldX, currentY, FIELD_WIDTH, 20).build();
		this.addRenderableWidget(dimensionButton);

		// Gamemode selector
		currentY += 24;
		gamemodeButton = Button.builder(
				getGamemodeLabel(),
				btn -> {
					gamemodeIndex = (gamemodeIndex + 1) % GAMEMODES.length;
					gamemodeButton.setMessage(getGamemodeLabel());
				}
		).bounds(fieldX, currentY, FIELD_WIDTH, 20).build();
		this.addRenderableWidget(gamemodeButton);

		// Action selector
		currentY += ROW_GAP + 8;
		actionButton = Button.builder(
				getActionLabel(),
				btn -> {
					actionIndex = (actionIndex + 1) % ACTIONS.length;
					actionButton.setMessage(getActionLabel());
				}
		).bounds(fieldX, currentY, FIELD_WIDTH, 20).build();
		this.addRenderableWidget(actionButton);

		// Config field (manual input)
		currentY += ROW_GAP + 8;
		configField = new EditBox(this.font, fieldX, currentY, FIELD_WIDTH, FIELD_HEIGHT,
				Component.translatable("screen.command-gui.fakeplayer.config"));
		configField.setMaxLength(256);
		configField.setHint(Component.translatable("screen.command-gui.fakeplayer.config_hint"));
		this.addRenderableWidget(configField);

		// Save and Cancel buttons
		currentY += ROW_GAP + 4;
		this.addRenderableWidget(Button.builder(
				Component.translatable("screen.command-gui.save"),
				btn -> saveAndClose()
		).bounds(centerX - 102, currentY, 100, 20).build());

		this.addRenderableWidget(Button.builder(
				Component.translatable("screen.command-gui.cancel"),
				btn -> this.minecraft.setScreen(parent)
		).bounds(centerX + 2, currentY, 100, 20).build());

		// Fill current position data
		fillCurrentPosition();

		// If editing, restore values
		if (editingEntry != null && editingName != null) {
			nameField.setValue(editingName);
			descriptionField.setValue(editingEntry.description != null ? editingEntry.description : "");
			parseExistingCommands(editingEntry.getCommands());
		}

		updateCoordFieldVisibility();
	}

	private void fillCurrentPosition() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null) {
			posX = Math.round(mc.player.getX() * 10.0) / 10.0;
			posY = Math.round(mc.player.getY() * 10.0) / 10.0;
			posZ = Math.round(mc.player.getZ() * 10.0) / 10.0;
			yaw = Math.round(mc.player.getYRot() * 10.0f) / 10.0f;
			pitch = Math.round(mc.player.getXRot() * 10.0f) / 10.0f;

			xField.setValue(String.format("%.1f", posX));
			yField.setValue(String.format("%.1f", posY));
			zField.setValue(String.format("%.1f", posZ));
			yawField.setValue(String.format("%.1f", yaw));
			pitchField.setValue(String.format("%.1f", pitch));

			// Auto-detect dimension by comparing against known Level constants
			net.minecraft.resources.ResourceKey<Level> dim = mc.player.level().dimension();
			if (dim.equals(Level.NETHER)) {
				dimensionIndex = 1;
			} else if (dim.equals(Level.END)) {
				dimensionIndex = 2;
			} else {
				dimensionIndex = 0; // default to overworld
			}
			if (dimensionButton != null) {
				dimensionButton.setMessage(getDimensionLabel());
			}

			// Auto-detect gamemode
			if (mc.gameMode != null) {
				String mode = mc.gameMode.getPlayerMode().getName();
				for (int i = 0; i < GAMEMODES.length; i++) {
					if (GAMEMODES[i].equals(mode)) {
						gamemodeIndex = i;
						if (gamemodeButton != null) {
							gamemodeButton.setMessage(getGamemodeLabel());
						}
						break;
					}
				}
			}
		}
	}

	private void parseExistingCommands(List<String> commands) {
		if (commands == null || commands.isEmpty()) return;

		String spawnCmd = commands.get(0);
		// Try to extract fake player name from: /player <name> spawn ...
		if (spawnCmd.startsWith("/player ") || spawnCmd.startsWith("player ")) {
			String cmd = spawnCmd.startsWith("/") ? spawnCmd.substring(1) : spawnCmd;
			String[] parts = cmd.split("\\s+");
			if (parts.length >= 2) {
				fakePlayerNameField.setValue(parts[1]);
			}
			// Try to detect if it uses custom position
			if (cmd.contains(" at ")) {
				useCurrentPosition = false;
				positionModeButton.setMessage(getPositionModeLabel());
			}
		}

		// Parse action command
		if (commands.size() > 1) {
			String actionCmd = commands.get(1);
			String cmd = actionCmd.startsWith("/") ? actionCmd.substring(1) : actionCmd;
			// Try to match action: /player <name> <action>
			String[] parts = cmd.split("\\s+", 3);
			if (parts.length >= 3) {
				String actionPart = parts[2];
				for (int i = 0; i < ACTIONS.length; i++) {
					if (!ACTIONS[i].isEmpty() && actionPart.startsWith(ACTIONS[i])) {
						actionIndex = i;
						actionButton.setMessage(getActionLabel());
						break;
					}
				}
			}
		}

		// Parse config command
		if (commands.size() > 2) {
			configField.setValue(commands.get(2));
		}

		updateCoordFieldVisibility();
	}

	private Component getPositionModeLabel() {
		return Component.translatable(useCurrentPosition
				? "screen.command-gui.fakeplayer.timed.spawn.pos.current"
				: "screen.command-gui.fakeplayer.timed.spawn.pos.custom");
	}

	private Component getDimensionLabel() {
		return Component.translatable("screen.command-gui.fakeplayer.dimension")
				.append(": ")
				.append(Component.translatable(DIMENSION_NAMES[dimensionIndex]));
	}

	private Component getGamemodeLabel() {
		return Component.translatable("screen.command-gui.fakeplayer.gamemode")
				.append(": ")
				.append(Component.translatable(GAMEMODE_NAMES[gamemodeIndex]));
	}

	private Component getActionLabel() {
		return Component.translatable("screen.command-gui.fakeplayer.action_label")
				.append(": ")
				.append(Component.translatable(ACTION_NAMES[actionIndex]));
	}

	private void updateCoordFieldVisibility() {
		boolean showCoords = !useCurrentPosition;
		xField.visible = showCoords;
		xField.active = showCoords;
		yField.visible = showCoords;
		yField.active = showCoords;
		zField.visible = showCoords;
		zField.active = showCoords;
		yawField.visible = showCoords;
		yawField.active = showCoords;
		pitchField.visible = showCoords;
		pitchField.active = showCoords;
		dimensionButton.visible = showCoords;
		dimensionButton.active = showCoords;
		gamemodeButton.visible = showCoords;
		gamemodeButton.active = showCoords;
	}

	private List<String> buildCommands() {
		List<String> commands = new ArrayList<>();
		String fpName = fakePlayerNameField.getValue().trim();
		if (fpName.isEmpty()) return commands;

		// Build spawn command
		StringBuilder spawnCmd = new StringBuilder("/player ").append(fpName).append(" spawn");

		if (useCurrentPosition) {
			fillCurrentPosition();
		}

		String x = xField.getValue().trim();
		String y = yField.getValue().trim();
		String z = zField.getValue().trim();
		String yawStr = yawField.getValue().trim();
		String pitchStr = pitchField.getValue().trim();

		if (!x.isEmpty() && !y.isEmpty() && !z.isEmpty()) {
			spawnCmd.append(" at ").append(x).append(" ").append(y).append(" ").append(z);
		}
		if (!yawStr.isEmpty() && !pitchStr.isEmpty()) {
			spawnCmd.append(" facing ").append(yawStr).append(" ").append(pitchStr);
		} else if (!yawStr.isEmpty()) {
			spawnCmd.append(" facing ").append(yawStr).append(" 0");
		}
		spawnCmd.append(" in ").append(DIMENSIONS[dimensionIndex]);
		spawnCmd.append(" in ").append(GAMEMODES[gamemodeIndex]);

		commands.add(spawnCmd.toString());

		// Build action command (if selected)
		if (actionIndex > 0 && !ACTIONS[actionIndex].isEmpty()) {
			commands.add("/player " + fpName + " " + ACTIONS[actionIndex]);
		}

		// Add config command (if any)
		String config = configField.getValue().trim();
		if (!config.isEmpty()) {
			commands.add(config);
		}

		return commands;
	}

	private void saveAndClose() {
		String name = nameField.getValue().trim();
		String description = descriptionField.getValue().trim();
		List<String> commands = buildCommands();

		if (name.isEmpty() || commands.isEmpty()) return;

		String categoryId = initialCategoryId != null ? initialCategoryId : "default";

		if (editingName != null) {
			// Editing existing command
			if (!name.equals(editingName)) {
				String oldCategoryId = CommandConfig.findCommandCategory(editingName);
				CommandConfig.removeCommand(editingName);
				CommandConfig.addCommandMulti(oldCategoryId != null ? oldCategoryId : categoryId, name, commands, description);
			} else {
				CommandConfig.updateCommandMulti(name, commands, description);
			}
		} else {
			// Adding new command
			CommandConfig.addCommandMulti(categoryId, name, commands, description);
		}

		parent.refresh();
		this.minecraft.setScreen(parent);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);

		int centerX = this.width / 2;
		int labelX = centerX - FIELD_WIDTH / 2 - 4;
		int currentY = 25;

		// Title
		guiGraphics.drawCenteredString(this.font, this.title, centerX, 8, 0xFFFFFFFF);

		// Labels
		guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.name"),
				labelX - this.font.width(Component.translatable("screen.command-gui.name")), currentY + 4, 0xFFAAAAAA);

		currentY += ROW_GAP;
		guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.description"),
				labelX - this.font.width(Component.translatable("screen.command-gui.description")), currentY + 4, 0xFFAAAAAA);

		currentY += ROW_GAP + 8;
		guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.fakeplayer.playername"),
				labelX - this.font.width(Component.translatable("screen.command-gui.fakeplayer.playername")), currentY + 4, 0xFFAAAAAA);

		currentY += ROW_GAP + 4;
		guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.fakeplayer.spawn_at"),
				labelX - this.font.width(Component.translatable("screen.command-gui.fakeplayer.spawn_at")), currentY + 5, 0xFFAAAAAA);

		if (!useCurrentPosition) {
			currentY += 24;
			int coordTotalWidth = COORD_FIELD_WIDTH * 3 + COORD_GAP * 2;
			int coordStartX = centerX - coordTotalWidth / 2;
			guiGraphics.drawCenteredString(this.font, "X", coordStartX + COORD_FIELD_WIDTH / 2, currentY - 8, 0xFFFF5555);
			guiGraphics.drawCenteredString(this.font, "Y", coordStartX + COORD_FIELD_WIDTH + COORD_GAP + COORD_FIELD_WIDTH / 2, currentY - 8, 0xFF55FF55);
			guiGraphics.drawCenteredString(this.font, "Z", coordStartX + (COORD_FIELD_WIDTH + COORD_GAP) * 2 + COORD_FIELD_WIDTH / 2, currentY - 8, 0xFF5555FF);

			currentY += ROW_GAP;
			int angleTotalWidth = COORD_FIELD_WIDTH * 2 + COORD_GAP;
			int angleStartX = centerX - angleTotalWidth / 2;
			guiGraphics.drawCenteredString(this.font, "Yaw", angleStartX + COORD_FIELD_WIDTH / 2, currentY - 8, 0xFFCCCCCC);
			guiGraphics.drawCenteredString(this.font, "Pitch", angleStartX + COORD_FIELD_WIDTH + COORD_GAP + COORD_FIELD_WIDTH / 2, currentY - 8, 0xFFCCCCCC);
		}

		// Command preview at bottom
		List<String> commands = buildCommands();
		int previewY = this.height - 12 - commands.size() * 10;
		guiGraphics.fill(2, previewY - 2, this.width - 2, this.height - 2, 0x80000000);
		for (int i = 0; i < commands.size(); i++) {
			String cmd = commands.get(i);
			String display = this.font.plainSubstrByWidth(cmd, this.width - 10);
			guiGraphics.drawString(this.font, display, 4, previewY + i * 10, 0xFF55FF55);
		}
	}

	@Override
	public boolean keyPressed(KeyEvent keyEvent) {
		int keyCode = keyEvent.key();
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			this.minecraft.setScreen(parent);
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			String name = nameField.getValue().trim();
			String fpName = fakePlayerNameField.getValue().trim();
			if (!name.isEmpty() && !fpName.isEmpty()) {
				saveAndClose();
			}
			return true;
		}
		return super.keyPressed(keyEvent);
	}
}
