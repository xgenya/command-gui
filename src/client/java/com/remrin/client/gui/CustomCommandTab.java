package com.remrin.client.gui;

import com.remrin.client.config.CommandConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CustomCommandTab extends AbstractCommandTab {
	private record FilteredCommand(String name, String categoryId, CommandConfig.CommandEntry entry) {}

	private final List<FilteredCommand> filteredCommands = new ArrayList<>();
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
					entry.getValue().command.toLowerCase().contains(search) ||
					entry.getValue().description.toLowerCase().contains(search)) {
					filteredCommands.add(new FilteredCommand(entry.getKey(), category.id, entry.getValue()));
				}
			}
		}
	}

	@Override
	protected void rebuildCategoryButtons() {
		categoryButtons.clear();
		if (area == null) return;

		int x = area.left();
		int y = area.top();

		Button allBtn = Button.builder(
				Component.translatable("screen.command-gui.category.all"),
				btn -> onCategoryButtonClick(null)
		).bounds(x, y, CATEGORY_TAB_WIDTH, CATEGORY_TAB_HEIGHT).build();
		allBtn.active = (selectedCategoryId != null);
		categoryButtons.add(allBtn);

		y += CATEGORY_TAB_HEIGHT + CATEGORY_TAB_GAP;

		for (CommandConfig.Category category : CommandConfig.getCategories()) {
			if (y + CATEGORY_TAB_HEIGHT > area.bottom() - CATEGORY_TAB_HEIGHT - CATEGORY_TAB_GAP) break;

			final String catId = category.id;
			Component btnText = category.getDisplayName() != null
					? Component.literal(category.getDisplayName())
					: Component.translatable(category.nameKey);

			Button catBtn = Button.builder(btnText, btn -> onCategoryButtonClick(catId))
					.bounds(x, y, CATEGORY_TAB_WIDTH, CATEGORY_TAB_HEIGHT).build();
			catBtn.active = !catId.equals(selectedCategoryId);
			categoryButtons.add(catBtn);

			y += CATEGORY_TAB_HEIGHT + CATEGORY_TAB_GAP;
		}

		y = area.bottom() - CATEGORY_TAB_HEIGHT;
		Button addCatBtn = Button.builder(
				Component.literal("+"),
				btn -> openAddCategoryScreen()
		).bounds(x, y, CATEGORY_TAB_WIDTH, CATEGORY_TAB_HEIGHT).build();
		addCatBtn.setTooltip(Tooltip.create(Component.translatable("screen.command-gui.add_category")));
		categoryButtons.add(addCatBtn);
	}

	@Override
	protected Button buildCommandButton(int index, int x, int y, int width, int height) {
		FilteredCommand cmd = filteredCommands.get(index);
		Button btn = Button.builder(
				Component.literal(cmd.name()),
				b -> handleCommand(cmd.entry())
		).bounds(x, y, width, height).build();

		String tooltipText = cmd.entry().command;
		if (cmd.entry().description != null && !cmd.entry().description.isEmpty()) {
			tooltipText = cmd.entry().description + "\n§7" + cmd.entry().command;
		}
		btn.setTooltip(Tooltip.create(Component.literal(tooltipText)));
		return btn;
	}

	private void onCategoryButtonClick(String categoryId) {
		if (Objects.equals(selectedCategoryId, categoryId)) return;
		notifyCategoryChange(() -> {
			selectedCategoryId = categoryId;
			scrollOffset = 0;
			buildFilteredCommands();
			rebuildCategoryButtons();
			rebuildButtons();
		});
	}

	private void openAddCategoryScreen() {
		net.minecraft.client.Minecraft.getInstance().setScreen(new AddCategoryScreen((CommandGUIScreen) parent));
	}

	private void handleCommand(CommandConfig.CommandEntry entry) {
		ChainedCommandExecutor.execute(parent, entry.command);
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

	public String getSelectedCategoryId() {
		return selectedCategoryId;
	}
}
