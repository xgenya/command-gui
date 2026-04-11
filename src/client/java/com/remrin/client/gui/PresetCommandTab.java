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
	private static final int COLUMNS = 3;
	private static final int CATEGORY_TAB_WIDTH = 50;
	private static final int CATEGORY_TAB_HEIGHT = 16;
	private static final int CATEGORY_TAB_GAP = 2;

	private final Screen parent;
	private final String presetId;
	private final String nameKey;
	private final List<Button> commandButtons = new ArrayList<>();
	private final List<Button> categoryButtons = new ArrayList<>();
	private final List<VanillaCommands.CommandGroup> allGroups = new ArrayList<>();
	private final List<VanillaCommands.VanillaCommand> filteredCommands = new ArrayList<>();
	private int scrollOffset = 0;
	private ScreenRectangle area;
	private String searchText = "";
	private int selectedCategoryIndex = -1; // -1 means "All"
	private Runnable onBeforeCategoryChanged;
	private Runnable onAfterCategoryChanged;

	public PresetCommandTab(Screen parent, String presetId, String nameKey) {
		this.parent = parent;
		this.presetId = presetId;
		this.nameKey = nameKey;
		this.allGroups.addAll(VanillaCommands.getGroups(presetId));
		buildFilteredCommands();
	}

	public void setOnCategoryChanged(Runnable before, Runnable after) {
		this.onBeforeCategoryChanged = before;
		this.onAfterCategoryChanged = after;
	}

	public void setSearchText(String text) {
		this.searchText = text.toLowerCase().trim();
		this.scrollOffset = 0;
		buildFilteredCommands();
		rebuildButtons();
	}

	public void setSelectedCategory(int index) {
		this.selectedCategoryIndex = index;
		this.scrollOffset = 0;
		buildFilteredCommands();
		rebuildButtons();
	}

	private void buildFilteredCommands() {
		filteredCommands.clear();
		String search = searchText;
		
		for (int i = 0; i < allGroups.size(); i++) {
			if (selectedCategoryIndex >= 0 && selectedCategoryIndex != i) {
				continue;
			}
			
			VanillaCommands.CommandGroup group = allGroups.get(i);
			for (VanillaCommands.VanillaCommand cmd : group.commands) {
				if (search.isEmpty() ||
					cmd.getName().getString().toLowerCase().contains(search) ||
					cmd.command.toLowerCase().contains(search)) {
					filteredCommands.add(cmd);
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
		categoryButtons.forEach(consumer);
	}

	@Override
	public void doLayout(ScreenRectangle rectangle) {
		this.area = rectangle;
		rebuildCategoryButtons();
		rebuildButtons();
	}

	private int getCommandAreaLeft() {
		return area.left() + CATEGORY_TAB_WIDTH + 8;
	}

	private int getCommandAreaWidth() {
		return area.width() - CATEGORY_TAB_WIDTH - 8;
	}

	private void rebuildCategoryButtons() {
		categoryButtons.clear();
		if (area == null) return;

		int x = area.left();
		int y = area.top();

		// "All" button
		Button allBtn = Button.builder(
				Component.translatable("screen.command-gui.category.all"),
				btn -> onCategoryButtonClick(-1)
		).bounds(x, y, CATEGORY_TAB_WIDTH, CATEGORY_TAB_HEIGHT).build();
		allBtn.active = (selectedCategoryIndex != -1);
		categoryButtons.add(allBtn);

		y += CATEGORY_TAB_HEIGHT + CATEGORY_TAB_GAP;

		// Category buttons
		for (int i = 0; i < allGroups.size(); i++) {
			if (y + CATEGORY_TAB_HEIGHT > area.bottom()) break;

			VanillaCommands.CommandGroup group = allGroups.get(i);
			final int index = i;

			Button catBtn = Button.builder(
					Component.translatable(group.nameKey),
					btn -> onCategoryButtonClick(index)
			).bounds(x, y, CATEGORY_TAB_WIDTH, CATEGORY_TAB_HEIGHT).build();
			catBtn.active = (selectedCategoryIndex != index);
			categoryButtons.add(catBtn);

			y += CATEGORY_TAB_HEIGHT + CATEGORY_TAB_GAP;
		}
	}

	private void onCategoryButtonClick(int index) {
		if (selectedCategoryIndex != index) {
			selectCategory(index);
		}
	}

	private void updateCategoryButtonStates() {
		if (categoryButtons.isEmpty()) return;

		// First button is "All"
		categoryButtons.get(0).active = (selectedCategoryIndex != -1);

		// Rest are category buttons
		for (int i = 1; i < categoryButtons.size(); i++) {
			categoryButtons.get(i).active = (selectedCategoryIndex != (i - 1));
		}
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

			VanillaCommands.VanillaCommand cmd = filteredCommands.get(index);

			int col = i % COLUMNS;
			int row = i / COLUMNS;
			int x = commandAreaLeft + col * colWidth;
			int btnY = y + row * ITEM_HEIGHT;

			if (btnY + ITEM_HEIGHT > maxY) break;

			int btnWidth = colWidth - 4;

			Button cmdBtn = Button.builder(
					cmd.getName(),
					btn -> handleCommand(cmd)
			).bounds(x + 2, btnY, btnWidth, ITEM_HEIGHT - 2).build();
			
			Component desc = cmd.getDescription();
			if (desc != null) {
				cmdBtn.setTooltip(Tooltip.create(desc.copy().append("\n§7" + cmd.command)));
			} else {
				cmdBtn.setTooltip(Tooltip.create(Component.literal(cmd.command)));
			}

			commandButtons.add(cmdBtn);
		}
	}

	public void renderCategoryTabs(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		if (area == null) return;

		// Draw separator line between categories and commands
		int separatorX = area.left() + CATEGORY_TAB_WIDTH + 4;
		guiGraphics.fill(separatorX, area.top(), separatorX + 1, area.bottom(), 0xFF555555);
	}

	public boolean handleCategoryClick(double mouseX, double mouseY) {
		// Category buttons handle their own clicks via Button callback
		return false;
	}

	private void selectCategory(int index) {
		if (onBeforeCategoryChanged != null) {
			onBeforeCategoryChanged.run();
		}

		selectedCategoryIndex = index;
		scrollOffset = 0;
		buildFilteredCommands();
		rebuildButtons();
		updateCategoryButtonStates();

		if (onAfterCategoryChanged != null) {
			onAfterCategoryChanged.run();
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
		if (area == null || filteredCommands.isEmpty()) return 0;
		int visibleRows = area.height() / ITEM_HEIGHT;
		int totalRows = (filteredCommands.size() + COLUMNS - 1) / COLUMNS;
		if (totalRows <= visibleRows) return 0;
		return totalRows - visibleRows;
	}

	public List<Button> getButtons() {
		return commandButtons;
	}

	public List<Button> getCategoryButtons() {
		return categoryButtons;
	}
	
	public String getPresetId() {
		return presetId;
	}

	public ScreenRectangle getArea() {
		return area;
	}
}
