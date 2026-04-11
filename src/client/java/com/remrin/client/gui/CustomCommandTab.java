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
	private static final int COLUMNS = 3;
	private static final int CATEGORY_TAB_WIDTH = 50;
	private static final int CATEGORY_TAB_HEIGHT = 16;
	private static final int CATEGORY_TAB_GAP = 2;
	
	private final Screen parent;
	private final List<Button> commandButtons = new ArrayList<>();
	private final List<Button> categoryButtons = new ArrayList<>();
	private final List<CommandConfig.CommandEntry> filteredCommands = new ArrayList<>();
	private final List<String> filteredCommandNames = new ArrayList<>();
	private final List<String> filteredCommandCategories = new ArrayList<>();
	private int scrollOffset = 0;
	private String searchText = "";
	private ScreenRectangle area;
	private String selectedCategoryId = null; // null means "All"
	private Runnable onBeforeCategoryChanged;
	private Runnable onAfterCategoryChanged;
	
	public CustomCommandTab(Screen parent) {
		this.parent = parent;
		buildFilteredCommands();
	}

	public void setOnCategoryChanged(Runnable before, Runnable after) {
		this.onBeforeCategoryChanged = before;
		this.onAfterCategoryChanged = after;
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
		categoryButtons.forEach(consumer);
	}

	@Override
	public void doLayout(ScreenRectangle rectangle) {
		this.area = rectangle;
		rebuildCategoryButtons();
		rebuildButtons();
	}

	public void setSearchText(String text) {
		this.searchText = text;
		this.scrollOffset = 0;
		buildFilteredCommands();
		rebuildButtons();
	}

	private int getCommandAreaLeft() {
		return area.left() + CATEGORY_TAB_WIDTH + 8;
	}

	private int getCommandAreaWidth() {
		return area.width() - CATEGORY_TAB_WIDTH - 8;
	}

	private void buildFilteredCommands() {
		filteredCommands.clear();
		filteredCommandNames.clear();
		filteredCommandCategories.clear();
		String search = searchText.toLowerCase().trim();
		
		for (CommandConfig.Category category : CommandConfig.getCategories()) {
			if (selectedCategoryId != null && !selectedCategoryId.equals(category.id)) {
				continue;
			}
			
			for (Map.Entry<String, CommandConfig.CommandEntry> entry : category.commands.entrySet()) {
				if (search.isEmpty() || 
					entry.getKey().toLowerCase().contains(search) ||
					entry.getValue().command.toLowerCase().contains(search) ||
					entry.getValue().description.toLowerCase().contains(search)) {
					filteredCommands.add(entry.getValue());
					filteredCommandNames.add(entry.getKey());
					filteredCommandCategories.add(category.id);
				}
			}
		}
	}

	private void rebuildCategoryButtons() {
		categoryButtons.clear();
		if (area == null) return;

		int x = area.left();
		int y = area.top();

		// "All" button
		Button allBtn = Button.builder(
				Component.translatable("screen.command-gui.category.all"),
				btn -> onCategoryButtonClick(null)
		).bounds(x, y, CATEGORY_TAB_WIDTH, CATEGORY_TAB_HEIGHT).build();
		allBtn.active = (selectedCategoryId != null);
		categoryButtons.add(allBtn);

		y += CATEGORY_TAB_HEIGHT + CATEGORY_TAB_GAP;

		// Category buttons
		for (CommandConfig.Category category : CommandConfig.getCategories()) {
			if (y + CATEGORY_TAB_HEIGHT > area.bottom() - CATEGORY_TAB_HEIGHT - CATEGORY_TAB_GAP) break;

			final String catId = category.id;

			Component btnText = category.getDisplayName() != null 
					? Component.literal(category.getDisplayName())
					: Component.translatable(category.nameKey);

			Button catBtn = Button.builder(
					btnText,
					btn -> onCategoryButtonClick(catId)
			).bounds(x, y, CATEGORY_TAB_WIDTH, CATEGORY_TAB_HEIGHT).build();
			catBtn.active = !catId.equals(selectedCategoryId);
			categoryButtons.add(catBtn);

			y += CATEGORY_TAB_HEIGHT + CATEGORY_TAB_GAP;
		}

		// Add category button at bottom
		y = area.bottom() - CATEGORY_TAB_HEIGHT;
		Button addCatBtn = Button.builder(
				Component.literal("+"),
				btn -> openAddCategoryScreen()
		).bounds(x, y, CATEGORY_TAB_WIDTH, CATEGORY_TAB_HEIGHT).build();
		addCatBtn.setTooltip(Tooltip.create(Component.translatable("screen.command-gui.add_category")));
		categoryButtons.add(addCatBtn);
	}

	private void onCategoryButtonClick(String categoryId) {
		if ((selectedCategoryId == null && categoryId == null) ||
			(selectedCategoryId != null && selectedCategoryId.equals(categoryId))) {
			return;
		}
		
		// Notify before rebuilding so old buttons can be removed from screen
		if (onBeforeCategoryChanged != null) {
			onBeforeCategoryChanged.run();
		}
		
		selectedCategoryId = categoryId;
		scrollOffset = 0;
		buildFilteredCommands();
		rebuildCategoryButtons();
		rebuildButtons();
		
		// Notify after rebuilding so new buttons can be added to screen
		if (onAfterCategoryChanged != null) {
			onAfterCategoryChanged.run();
		}
	}

	private void openAddCategoryScreen() {
		Minecraft mc = Minecraft.getInstance();
		mc.setScreen(new AddCategoryScreen((CommandGUIScreen) parent));
	}

	private void rebuildButtons() {
		commandButtons.clear();
		if (area == null) return;

		int commandAreaLeft = getCommandAreaLeft();
		int commandAreaWidth = getCommandAreaWidth();
		int colWidth = commandAreaWidth / COLUMNS;
		int y = area.top();
		int maxY = area.bottom();

		int startIndex = scrollOffset * COLUMNS;
		int visibleRows = (area.height()) / ITEM_HEIGHT;
		int maxItems = visibleRows * COLUMNS;

		for (int i = 0; i < Math.min(maxItems, filteredCommands.size() - startIndex); i++) {
			int index = startIndex + i;
			if (index >= filteredCommands.size()) break;

			CommandConfig.CommandEntry entry = filteredCommands.get(index);
			String name = filteredCommandNames.get(index);

			int col = i % COLUMNS;
			int row = i / COLUMNS;
			int btnX = commandAreaLeft + col * colWidth;
			int btnY = y + row * ITEM_HEIGHT;

			if (btnY + ITEM_HEIGHT > maxY) break;

			int btnWidth = colWidth - 4;

			Button cmdBtn = Button.builder(
					Component.literal(name),
					btn -> handleCommand(entry)
			).bounds(btnX + 2, btnY, btnWidth, ITEM_HEIGHT - 2).build();

			String tooltipText = entry.command;
			if (entry.description != null && !entry.description.isEmpty()) {
				tooltipText = entry.description + "\n§7" + entry.command;
			}
			cmdBtn.setTooltip(Tooltip.create(Component.literal(tooltipText)));

			commandButtons.add(cmdBtn);
		}
	}

	public void renderCategories(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		if (area == null) return;

		// Draw separator line between categories and commands
		int separatorX = area.left() + CATEGORY_TAB_WIDTH + 4;
		guiGraphics.fill(separatorX, area.top(), separatorX + 1, area.bottom(), 0xFF555555);
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
		if (area == null || filteredCommands.isEmpty()) return 0;
		int visibleRows = area.height() / ITEM_HEIGHT;
		int totalRows = (filteredCommands.size() + COLUMNS - 1) / COLUMNS;
		if (totalRows <= visibleRows) return 0;
		return totalRows - visibleRows;
	}

	public void refresh() {
		this.scrollOffset = 0;
		buildFilteredCommands();
		rebuildCategoryButtons();
		rebuildButtons();
	}
	
	public boolean isEmpty() {
		return filteredCommands.isEmpty() && CommandConfig.getCategories().isEmpty();
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

	public List<Button> getCategoryButtons() {
		return categoryButtons;
	}
	
	public String findCommandCategory(String name) {
		return CommandConfig.findCommandCategory(name);
	}

	public String getSelectedCategoryId() {
		return selectedCategoryId;
	}
}
