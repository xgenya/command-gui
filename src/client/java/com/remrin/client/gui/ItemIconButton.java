package com.remrin.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ItemIconButton extends Button {
	private final ItemStack iconItem;

	public ItemIconButton(int x, int y, int width, int height, ItemStack icon, OnPress onPress) {
		super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
		this.iconItem = icon;
	}

	public ItemIconButton(int x, int y, int width, int height, ItemStack icon, Component tooltip, OnPress onPress) {
		this(x, y, width, height, icon, onPress);
		this.setTooltip(Tooltip.create(tooltip));
	}

	@Override
	protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		int iconSize = Math.min(this.getWidth(), this.getHeight()) - 2;
		if (iconSize >= 16) {
			int iconX = this.getX() + (this.getWidth() - 16) / 2;
			int iconY = this.getY() + (this.getHeight() - 16) / 2;
			guiGraphics.renderItem(iconItem, iconX, iconY);
		} else {
			// Scale down for smaller buttons
			float scale = iconSize / 16.0f;
			int scaledX = this.getX() + (this.getWidth() - iconSize) / 2;
			int scaledY = this.getY() + (this.getHeight() - iconSize) / 2;
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(scaledX, scaledY, 0);
			guiGraphics.pose().scale(scale, scale, 1.0f);
			guiGraphics.renderItem(iconItem, 0, 0);
			guiGraphics.pose().popPose();
		}
	}
}
