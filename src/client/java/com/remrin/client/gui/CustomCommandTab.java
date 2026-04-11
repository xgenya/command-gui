package com.remrin.client.gui;

import com.remrin.client.config.CommandConfig;
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
import java.util.Map;
import java.util.function.Consumer;

public class CustomCommandTab implements Tab {
	private static final int ITEM_HEIGHT = 24;
	private static final int CATEGORY_HEIGHT = 18;
	private static final int COLUMNS = 3;
	
	private final Screen parent;
	private final List<Button> commandButtons = new ArrayList<>();
	private final List<RenderItem> renderItems = new ArrayList<>();
	private int scrollOffset = 0;
	private String searchText = "";
	private ScreenRectangle area;
	
	private static class RenderItem {
		enum Type { CATEGORY, COMMAND }
		Type type;
		String categoryId;
		String categoryNameKey;
		String commandName;
		CommandConfig.CommandEntry commandEntry;
		
		static RenderItem category(String id, String nameKey) {
			RenderItem item = new RenderItem();
			item.type = Type.CATEGORY;
			item.categoryId = id;
			item.categoryNameKey = nameKey;
			return item;
		}
		
		static RenderItem command(String categoryId, String name, CommandConfig.CommandEntry entry) {
			RenderItem item = new RenderItem();
			item.type = Type.COMMAND;
			item.categoryId = categoryId;
			item.commandName = name;
			item.commandEntry = entry;
			return item;
		}
	}
	
	public CustomCommandTab(Screen parent) {
		this.parent = parent;
		buildRenderItems();
	}

	@Override
	public Component getTabTitle() {
		return Component.translatable("screen.command-gui.tab.custom");
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

	public void setSearchText(String text) {
		this.searchText = text;
		this.scrollOffset = 0;
		buildRenderItems();
		rebuildButtons();
	}

	private void buildRenderItems() {
		renderItems.clear();
		String search = searchText.toLowerCase().trim();
		
		for (CommandConfig.Category category : CommandConfig.getCategories()) {
			List<Map.Entry<String, CommandConfig.CommandEntry>> commands = new ArrayList<>();
			
			for (Map.Entry<String, CommandConfig.CommandEntry> entry : category.commands.entrySet()) {
				if (search.isEmpty() || 
					entry.getKey().toLowerCase().contains(search) ||
					entry.getValue().command.toLowerCase().contains(search) ||
					entry.getValue().description.toLowerCase().contains(search)) {
					commands.add(entry);
				}
			}
			
			if (!commands.isEmpty()) {
				renderItems.add(RenderItem.category(category.id, category.nameKey));
				for (Map.Entry<String, CommandConfig.CommandEntry> entry : commands) {
					renderItems.add(RenderItem.command(category.id, entry.getKey(), entry.getValue()));
				}
			}
		}
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

					CommandConfig.CommandEntry entry = item.commandEntry;
					Button cmdBtn = Button.builder(
							Component.literal(item.commandName),
							btn -> handleCommand(entry)
					).bounds(x + 2, y, btnWidth, ITEM_HEIGHT - 2).build();

					String tooltipText = item.commandEntry.command;
					if (item.commandEntry.description != null && !item.commandEntry.description.isEmpty()) {
						tooltipText = item.commandEntry.description + "\n§7" + item.commandEntry.command;
					}
					cmdBtn.setTooltip(Tooltip.create(Component.literal(tooltipText)));

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

	private void handleCommand(CommandConfig.CommandEntry entry) {
		ChainedCommandExecutor.execute(parent, entry.command);
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

	public void refresh() {
		this.scrollOffset = 0;
		buildRenderItems();
		rebuildButtons();
	}

	public List<RenderItem> getRenderItems() {
		return renderItems;
	}
	
	public boolean isEmpty() {
		return renderItems.isEmpty();
	}

	public ScreenRectangle getArea() {
		return area;
	}

	public int getScrollOffset() {
		return scrollOffset;
	}

	public List<Button> getButtons() {
		return commandButtons;
	}
	
	public String findCommandCategory(String name) {
		return CommandConfig.findCommandCategory(name);
	}
}
