package com.remrin.client.gui;

import com.remrin.client.config.CommandConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CustomCommandTab extends AbstractCommandTab {
	private record FilteredCommand(String name, String categoryId, CommandConfig.CommandEntry entry) {}

	private static final int ACTION_BTN_WIDTH = 18;
	private static final int NUM_ACTION_BTNS = 3; // edit, delete, move
	private static final int ACTION_BTNS_TOTAL = NUM_ACTION_BTNS * (ACTION_BTN_WIDTH + 1);

	// MC item icons for action buttons
	private static final ItemStack EDIT_ICON = new ItemStack(Items.WRITABLE_BOOK);
	private static final ItemStack DELETE_ICON = new ItemStack(Items.LAVA_BUCKET);
	private static final ItemStack MOVE_ICON = new ItemStack(Items.PURPLE_SHULKER_BOX);

	private final List<FilteredCommand> filteredCommands = new ArrayList<>();
	private final List<Button> extraButtons = new ArrayList<>();
	private String selectedCategoryId = null;

	public CustomCommandTab(Screen parent) {
		super(parent);
		buildFilteredCommands();
	}

	@Override
	public Component getTabTitle() {
		return Component.translatable("screen.command-gui.tab.custom");
	}

	@Override
	protected int getFilteredCommandCount() {
		return filteredCommands.size();
	}

	@Override
	protected void buildFilteredCommands() {
		filteredCommands.clear();
		String search = searchText;
		for (CommandConfig.Category category : CommandConfig.getCategories()) {
			if (selectedCategoryId != null && !selectedCategoryId.equals(category.id)) {
				continue;
			}
			for (Map.Entry<String, CommandConfig.CommandEntry> entry : category.commands.entrySet()) {
				if (search.isEmpty() ||
					entry.getKey().toLowerCase().contains(search) ||
					entry.getValue().description.toLowerCase().contains(search) ||
					matchesAnyCommand(entry.getValue(), search)) {
					filteredCommands.add(new FilteredCommand(entry.getKey(), category.id, entry.getValue()));
				}
			}
		}
	}

	private boolean matchesAnyCommand(CommandConfig.CommandEntry entry, String search) {
		for (String cmd : entry.getCommands()) {
			if (cmd.toLowerCase().contains(search)) return true;
		}
		return false;
	}

	@Override
	protected void buildAllCategoryButtons() {
		allCategoryButtons.clear();
		if (area == null) return;

		int x = area.left();
		int y = area.top();

		Button allBtn = Button.builder(
				Component.translatable("screen.command-gui.category.all"),
				btn -> onCategoryButtonClick(null)
		).bounds(x, y, CATEGORY_TAB_WIDTH, CATEGORY_TAB_HEIGHT).build();
		allBtn.active = (selectedCategoryId != null);
		allCategoryButtons.add(allBtn);

		for (CommandConfig.Category category : CommandConfig.getCategories()) {
			final String catId = category.id;
			Component btnText = category.getDisplayName() != null
					? Component.literal(category.getDisplayName())
					: Component.translatable(category.nameKey);

			Button catBtn = Button.builder(btnText, btn -> onCategoryButtonClick(catId))
					.bounds(x, y, CATEGORY_TAB_WIDTH, CATEGORY_TAB_HEIGHT).build();
			catBtn.active = !catId.equals(selectedCategoryId);
			allCategoryButtons.add(catBtn);
		}

		Button addCatBtn = Button.builder(
				Component.literal("+"),
				btn -> openAddCategoryScreen()
		).bounds(x, y, CATEGORY_TAB_WIDTH, CATEGORY_TAB_HEIGHT).build();
		addCatBtn.setTooltip(Tooltip.create(Component.translatable("screen.command-gui.add_category")));
		allCategoryButtons.add(addCatBtn);
	}

	@Override
	protected Button buildCommandButton(int index, int x, int y, int width, int height) {
		FilteredCommand cmd = filteredCommands.get(index);
		// Make command button narrower to leave room for action buttons
		int cmdBtnWidth = width - ACTION_BTNS_TOTAL;
		Button btn = Button.builder(
				Component.literal(cmd.name()),
				b -> handleCommand(cmd.entry())
		).bounds(x, y, cmdBtnWidth, height).build();

		java.util.List<String> commands = cmd.entry().getCommands();
		String commandText = String.join("\n", commands);
		String tooltipText = commandText;
		if (cmd.entry().description != null && !cmd.entry().description.isEmpty()) {
			tooltipText = cmd.entry().description + "\n§7" + commandText;
		}
		btn.setTooltip(Tooltip.create(Component.literal(tooltipText)));
		return btn;
	}

	@Override
	protected void rebuildButtons() {
		super.rebuildButtons();
		// Build action buttons next to each command button
		extraButtons.clear();
		if (area == null) return;

		int commandAreaLeft = getCommandAreaLeft();
		int commandAreaWidth = getCommandAreaWidth();
		int colWidth = commandAreaWidth / COLUMNS;
		int y = area.top();
		int maxY = area.bottom();

		int startIndex = scrollOffset * COLUMNS;
		int visibleRows = area.height() / ITEM_HEIGHT;
		int maxItems = visibleRows * COLUMNS;
		int count = getFilteredCommandCount();

		for (int i = 0; i < Math.min(maxItems, count - startIndex); i++) {
			int index = startIndex + i;
			if (index >= count) break;

			int col = i % COLUMNS;
			int row = i / COLUMNS;
			int btnX = commandAreaLeft + col * colWidth;
			int btnY = y + row * ITEM_HEIGHT;
			if (btnY + ITEM_HEIGHT > maxY) break;

			int btnWidth = colWidth - 4;
			int cmdBtnWidth = btnWidth - ACTION_BTNS_TOTAL;
			int actionX = btnX + 2 + cmdBtnWidth + 1;

			FilteredCommand cmd = filteredCommands.get(index);
			final String cmdName = cmd.name();
			final CommandConfig.CommandEntry cmdEntry = cmd.entry();

			// Edit button
			ItemIconButton editBtn = new ItemIconButton(
					actionX, btnY, ACTION_BTN_WIDTH, ITEM_HEIGHT - 2,
					EDIT_ICON,
					Component.translatable("screen.command-gui.edit"),
					b -> editCommand(cmdName, cmdEntry));
			extraButtons.add(editBtn);
			actionX += ACTION_BTN_WIDTH + 1;

			// Delete button
			ItemIconButton deleteBtn = new ItemIconButton(
					actionX, btnY, ACTION_BTN_WIDTH, ITEM_HEIGHT - 2,
					DELETE_ICON,
					Component.translatable("screen.command-gui.delete"),
					b -> deleteCommand(cmdName));
			extraButtons.add(deleteBtn);
			actionX += ACTION_BTN_WIDTH + 1;

			// Move button
			ItemIconButton moveBtn = new ItemIconButton(
					actionX, btnY, ACTION_BTN_WIDTH, ITEM_HEIGHT - 2,
					MOVE_ICON,
					Component.translatable("screen.command-gui.move"),
					b -> moveCommand(cmdName));
			extraButtons.add(moveBtn);
		}
	}

	private void editCommand(String name, CommandConfig.CommandEntry entry) {
		Minecraft mc = Minecraft.getInstance();
		CommandGUIScreen parentScreen = (CommandGUIScreen) parent;
		if (isFakePlayerCommand(entry)) {
			String categoryId = CommandConfig.findCommandCategory(name);
			mc.setScreen(new AddFakePlayerCommandScreen(parentScreen, categoryId, name, entry));
		} else {
			mc.setScreen(new EditCommandScreen(parentScreen, name, entry));
		}
	}

	private void deleteCommand(String name) {
		CommandConfig.removeCommand(name);
		CommandGUIScreen parentScreen = (CommandGUIScreen) parent;
		notifyCategoryChange(() -> {
			buildFilteredCommands();
			buildAllCategoryButtons();
			rebuildVisibleCategoryButtons();
			rebuildButtons();
		});
	}

	private void moveCommand(String name) {
		if (CommandConfig.getCategories().size() <= 1) return;
		Minecraft mc = Minecraft.getInstance();
		mc.setScreen(new MoveCategoryScreen((CommandGUIScreen) parent, name));
	}

	private boolean isFakePlayerCommand(CommandConfig.CommandEntry entry) {
		if (entry == null) return false;
		java.util.List<String> commands = entry.getCommands();
		if (commands.isEmpty()) return false;
		String first = commands.get(0).trim().toLowerCase();
		return first.startsWith("/player ") && first.contains(" spawn");
	}

	@Override
	public List<Button> getButtons() {
		List<Button> all = new ArrayList<>(commandButtons);
		all.addAll(extraButtons);
		return all;
	}

	private void onCategoryButtonClick(String categoryId) {
		if (Objects.equals(selectedCategoryId, categoryId)) return;
		notifyCategoryChange(() -> {
			selectedCategoryId = categoryId;
			scrollOffset = 0;
			buildFilteredCommands();
			buildAllCategoryButtons();
			rebuildVisibleCategoryButtons();
			rebuildButtons();
		});
	}

	private void openAddCategoryScreen() {
		net.minecraft.client.Minecraft.getInstance().setScreen(new AddCategoryScreen((CommandGUIScreen) parent));
	}

	private void handleCommand(CommandConfig.CommandEntry entry) {
		java.util.List<String> commands = entry.getCommands();
		if (commands.size() > 1) {
			ChainedCommandExecutor.executeMulti(parent, commands);
		} else if (!commands.isEmpty()) {
			ChainedCommandExecutor.execute(parent, commands.get(0));
		}
	}

	public void refresh() {
		this.scrollOffset = 0;
		buildFilteredCommands();
		buildAllCategoryButtons();
		rebuildVisibleCategoryButtons();
		rebuildButtons();
	}

	public boolean isEmpty() {
		return filteredCommands.isEmpty() && CommandConfig.getCategories().isEmpty();
	}

	public String getSelectedCategoryId() {
		return selectedCategoryId;
	}
}
