package com.remrin.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class BatchSpawnScreen extends BaseParentedScreen<Screen> {
	private static final String[] ENGLISH_NAMES = {
		"Alex", "Ben", "Carl", "David", "Eric", "Frank", "George", "Henry", "Ivan", "Jack",
		"Kevin", "Leo", "Mike", "Nick", "Oscar", "Paul", "Quinn", "Ryan", "Steve", "Tom",
		"Uma", "Victor", "Will", "Xavier", "York", "Zack"
	};
	
	private EditBox prefixField;
	private EditBox startNumField;
	private EditBox countField;
	
	private String prefix = "Bot_";
	private int startNum = 1;
	private int count = 1;
	private boolean useEnglishNames = false;
	private Button typeButton;

	public BatchSpawnScreen(Screen parent) {
		super(Component.translatable("screen.command-gui.fakeplayer.batch.title"), parent);
	}

	@Override
	protected void init() {
		super.init();

		int centerX = this.width / 2;
		int centerY = this.height / 2;
		int fieldWidth = 150;
		int labelWidth = 80;

		int y = centerY - 75;
		
		typeButton = Button.builder(
				Component.translatable(useEnglishNames ? 
					"screen.command-gui.fakeplayer.batch.type.english" : 
					"screen.command-gui.fakeplayer.batch.type.numbered"),
				btn -> {
					useEnglishNames = !useEnglishNames;
					updateTypeButton();
					updateFieldsVisibility();
				}
		).bounds(centerX - fieldWidth / 2 + labelWidth / 2, y, fieldWidth, 20).build();
		this.addRenderableWidget(typeButton);

		y += 30;
		prefixField = new EditBox(this.font, centerX - fieldWidth / 2 + labelWidth / 2, y, fieldWidth, 20,
				Component.translatable("screen.command-gui.fakeplayer.batch.prefix"));
		prefixField.setMaxLength(20);
		prefixField.setValue(prefix);
		prefixField.setResponder(s -> prefix = s);
		this.addRenderableWidget(prefixField);

		y += 30;
		startNumField = new EditBox(this.font, centerX - fieldWidth / 2 + labelWidth / 2, y, fieldWidth, 20,
				Component.translatable("screen.command-gui.fakeplayer.batch.start"));
		startNumField.setMaxLength(5);
		startNumField.setValue(String.valueOf(startNum));
		startNumField.setResponder(s -> {
			try {
				startNum = Integer.parseInt(s);
			} catch (NumberFormatException ignored) {}
		});
		this.addRenderableWidget(startNumField);

		y += 30;
		countField = new EditBox(this.font, centerX - fieldWidth / 2 + labelWidth / 2, y, fieldWidth, 20,
				Component.translatable("screen.command-gui.fakeplayer.batch.count"));
		countField.setMaxLength(3);
		countField.setValue(String.valueOf(count));
		countField.setResponder(s -> {
			try {
				int max = useEnglishNames ? ENGLISH_NAMES.length : 50;
				count = Math.max(1, Math.min(max, Integer.parseInt(s)));
			} catch (NumberFormatException ignored) {}
		});
		this.addRenderableWidget(countField);

		y += 45;
		this.addRenderableWidget(Button.builder(
				Component.translatable("screen.command-gui.fakeplayer.batch.spawn"),
				btn -> spawnBatch()
		).bounds(centerX - 102, y, 100, 20).build());

		this.addRenderableWidget(Button.builder(
				Component.translatable("screen.command-gui.back"),
				btn -> this.minecraft.setScreen(parent)
		).bounds(centerX + 2, y, 100, 20).build());
		
		updateFieldsVisibility();
	}
	
	private void updateTypeButton() {
		typeButton.setMessage(Component.translatable(useEnglishNames ? 
			"screen.command-gui.fakeplayer.batch.type.english" : 
			"screen.command-gui.fakeplayer.batch.type.numbered"));
	}
	
	private void updateFieldsVisibility() {
		prefixField.visible = !useEnglishNames;
		prefixField.active = !useEnglishNames;
		startNumField.visible = !useEnglishNames;
		startNumField.active = !useEnglishNames;
		
		if (useEnglishNames) {
			count = Math.min(count, ENGLISH_NAMES.length);
			countField.setValue(String.valueOf(count));
		}
	}

	private void spawnBatch() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;

		if (useEnglishNames) {
			for (int i = 0; i < count && i < ENGLISH_NAMES.length; i++) {
				mc.player.connection.sendCommand("player " + ENGLISH_NAMES[i] + " spawn");
			}
		} else {
			for (int i = 0; i < count; i++) {
				String name = prefix + (startNum + i);
				mc.player.connection.sendCommand("player " + name + " spawn");
			}
		}
		
		this.minecraft.setScreen(parent);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);

		int centerX = this.width / 2;
		int centerY = this.height / 2;
		int labelWidth = 80;
		int fieldWidth = 150;
		int labelX = centerX - fieldWidth / 2 - labelWidth / 2 - 5;

		guiGraphics.drawCenteredString(this.font, this.title, centerX, centerY - 100, 0xFFFFFFFF);

		int y = centerY - 75;
		guiGraphics.drawString(this.font, 
				Component.translatable("screen.command-gui.fakeplayer.batch.type"),
				labelX, y + 6, 0xFFFFFFFF);

		if (!useEnglishNames) {
			y += 30;
			guiGraphics.drawString(this.font, 
					Component.translatable("screen.command-gui.fakeplayer.batch.prefix"),
					labelX, y + 6, 0xFFFFFFFF);

			y += 30;
			guiGraphics.drawString(this.font, 
					Component.translatable("screen.command-gui.fakeplayer.batch.start"),
					labelX, y + 6, 0xFFFFFFFF);
			
			y += 30;
		} else {
			y += 90;
		}
		
		guiGraphics.drawString(this.font, 
				Component.translatable("screen.command-gui.fakeplayer.batch.count"),
				labelX, y + 6, 0xFFFFFFFF);

		y += 75;
		String preview;
		if (useEnglishNames) {
			if (count == 1) {
				preview = ENGLISH_NAMES[0];
			} else {
				preview = ENGLISH_NAMES[0] + " ~ " + ENGLISH_NAMES[Math.min(count - 1, ENGLISH_NAMES.length - 1)];
			}
		} else {
			preview = prefix + startNum;
			if (count > 1) {
				preview += " ~ " + prefix + (startNum + count - 1);
			}
		}
		guiGraphics.drawCenteredString(this.font,
				Component.translatable("screen.command-gui.fakeplayer.batch.preview", preview),
				centerX, y, 0xFF888888);
	}
}
