package com.remrin.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PresetCommandTab implements Tab {
	private static final int ITEM_HEIGHT = 24;
	private static final int CATEGORY_HEIGHT = 18;
	private static final int COLUMNS = 3;

	private final Screen parent;
	private final String presetId;
	private final String nameKey;
	private final List<Button> commandButtons = new ArrayList<>();
	private final List<RenderItem> renderItems = new ArrayList<>();
	private int scrollOffset = 0;
	private ScreenRectangle area;
	private String searchText = "";

	private static class RenderItem {
		enum Type { CATEGORY, COMMAND }
		Type type;
		String categoryNameKey;
		VanillaCommands.VanillaCommand command;
		
		static RenderItem category(String nameKey) {
			RenderItem item = new RenderItem();
			item.type = Type.CATEGORY;
			item.categoryNameKey = nameKey;
			return item;
		}
		
		static RenderItem command(VanillaCommands.VanillaCommand cmd) {
			RenderItem item = new RenderItem();
			item.type = Type.COMMAND;
			item.command = cmd;
			return item;
		}
	}

	public PresetCommandTab(Screen parent, String presetId, String nameKey) {
		this.parent = parent;
		this.presetId = presetId;
		this.nameKey = nameKey;
		buildRenderItems();
	}

	public void setSearchText(String text) {
		this.searchText = text.toLowerCase().trim();
		this.scrollOffset = 0;
		buildRenderItems();
		rebuildButtons();
	}

	private void buildRenderItems() {
		renderItems.clear();
		String search = searchText;
		
		for (VanillaCommands.CommandGroup group : VanillaCommands.getGroups(presetId)) {
			List<VanillaCommands.VanillaCommand> matchedCommands = new ArrayList<>();
			
			for (VanillaCommands.VanillaCommand cmd : group.commands) {
				if (search.isEmpty() ||
					cmd.getName().getString().toLowerCase().contains(search) ||
					cmd.command.toLowerCase().contains(search)) {
					matchedCommands.add(cmd);
				}
			}
			
			if (!matchedCommands.isEmpty()) {
				renderItems.add(RenderItem.category(group.nameKey));
				for (VanillaCommands.VanillaCommand cmd : matchedCommands) {
					renderItems.add(RenderItem.command(cmd));
				}
			}
		}
	}

	@Override
	public Component getTabTitle() {
		return Component.translatable(nameKey);
	}

	@Override
	public Component getTabExtraNarration() {
		return Component.empty();
	}

	@Override
	public void visitChildren(Consumer consumer) {
		commandButtons.forEach(consumer);
	}

	@Override
	public void doLayout(ScreenRectangle rectangle) {
		this.area = rectangle;
		rebuildButtons();
	}

	private void rebuildButtons() {
		commandButtons.clear();
		if (area == null) return;

		int listWidth = area.width();
		int colWidth = listWidth / COLUMNS;
		int y = area.top();
		int currentCol = 0;
		int maxY = area.bottom();
		int skippedRows = 0;

		for (int i = 0; i < renderItems.size(); i++) {
			RenderItem item = renderItems.get(i);
			
			if (item.type == RenderItem.Type.CATEGORY) {
				if (currentCol > 0) {
					if (skippedRows < scrollOffset) {
						skippedRows++;
					} else {
						y += ITEM_HEIGHT;
					}
					currentCol = 0;
				}
				
				if (skippedRows < scrollOffset) {
					skippedRows++;
					continue;
				}
				
				if (y + CATEGORY_HEIGHT > maxY) break;
				y += CATEGORY_HEIGHT;
			} else {
				if (skippedRows >= scrollOffset) {
					if (y + ITEM_HEIGHT > maxY) break;
					
					int x = area.left() + currentCol * colWidth;
					int btnWidth = colWidth - 4;

					VanillaCommands.VanillaCommand cmd = item.command;
					Button cmdBtn = Button.builder(
							cmd.getName(),
							btn -> handleCommand(cmd)
					).bounds(x + 2, y, btnWidth, ITEM_HEIGHT - 2).build();
					cmdBtn.setTooltip(Tooltip.create(Component.literal(cmd.command)));

					commandButtons.add(cmdBtn);
				}
				
				currentCol++;
				if (currentCol >= COLUMNS) {
					currentCol = 0;
					if (skippedRows < scrollOffset) {
						skippedRows++;
					} else {
						y += ITEM_HEIGHT;
					}
				}
			}
		}
	}

	public void renderCategories(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		if (area == null) return;
		
		Minecraft mc = Minecraft.getInstance();
		int y = area.top();
		int currentCol = 0;
		int skippedRows = 0;

		for (int i = 0; i < renderItems.size(); i++) {
			RenderItem item = renderItems.get(i);
			
			if (item.type == RenderItem.Type.CATEGORY) {
				if (currentCol > 0) {
					if (skippedRows < scrollOffset) {
						skippedRows++;
					} else {
						y += ITEM_HEIGHT;
					}
					currentCol = 0;
				}
				
				if (skippedRows < scrollOffset) {
					skippedRows++;
					continue;
				}
				
				if (y + CATEGORY_HEIGHT > area.bottom()) break;
				
				guiGraphics.drawString(mc.font, 
						Component.translatable(item.categoryNameKey),
						area.left() + 2, y + 4, 0xFFAAAAAA);
				y += CATEGORY_HEIGHT;
			} else {
				currentCol++;
				if (currentCol >= COLUMNS) {
					currentCol = 0;
					if (skippedRows < scrollOffset) {
						skippedRows++;
					} else {
						y += ITEM_HEIGHT;
					}
				}
			}
		}
	}

	private void handleCommand(VanillaCommands.VanillaCommand cmd) {
		ChainedCommandExecutor.Config config = ChainedCommandExecutor.Config.defaultConfig();
		
		if (cmd.minValue != null || cmd.maxValue != null || cmd.quickValues != null) {
			config.withNumberRange(cmd.minValue, cmd.maxValue, cmd.quickValues);
		}
		
		ChainedCommandExecutor.execute(parent, cmd.command, config);
	}

	public void scroll(double delta) {
		if (area == null) return;

		int maxScroll = getMaxScroll();
		if (maxScroll > 0) {
			if (delta > 0 && scrollOffset > 0) {
				scrollOffset--;
				rebuildButtons();
			} else if (delta < 0 && scrollOffset < maxScroll) {
				scrollOffset++;
				rebuildButtons();
			}
		}
	}

	public void setScrollOffset(int offset) {
		int maxScroll = getMaxScroll();
		this.scrollOffset = Math.max(0, Math.min(offset, maxScroll));
		rebuildButtons();
	}

	public int getScrollOffset() {
		return scrollOffset;
	}

	public int getMaxScroll() {
		if (area == null || renderItems.isEmpty()) return 0;
		int totalHeight = calculateTotalHeight();
		int visibleHeight = area.height();
		if (totalHeight <= visibleHeight) return 0;
		return renderItems.size() - 1;
	}

	private int calculateTotalHeight() {
		int height = 0;
		int currentCol = 0;
		
		for (RenderItem item : renderItems) {
			if (item.type == RenderItem.Type.CATEGORY) {
				if (currentCol > 0) {
					height += ITEM_HEIGHT;
					currentCol = 0;
				}
				height += CATEGORY_HEIGHT;
			} else {
				currentCol++;
				if (currentCol >= COLUMNS) {
					currentCol = 0;
					height += ITEM_HEIGHT;
				}
			}
		}
		if (currentCol > 0) {
			height += ITEM_HEIGHT;
		}
		return height;
	}

	public List<Button> getButtons() {
		return commandButtons;
	}
	
	public String getPresetId() {
		return presetId;
	}
}
