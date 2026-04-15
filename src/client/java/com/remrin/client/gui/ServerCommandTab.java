package com.remrin.client.gui;

import com.remrin.client.sync.ServerCommandStore;
import com.remrin.sync.ServerCommandEntry;
import com.remrin.sync.ServerCommandGroup;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * A read-only tab displaying command presets received from the server (via the eun_carpet mod).
 * <p>
 * Command data comes from {@link ServerCommandStore}; each group maps to one category button in the
 * sidebar. Commands are executed via {@link ChainedCommandExecutor}.
 */
public class ServerCommandTab extends AbstractCommandTab {

	private final List<ServerCommandGroup> allGroups = new ArrayList<>();
	private final List<ServerCommandEntry> filteredCommands = new ArrayList<>();
	private int selectedCategoryIndex = -1;

	public ServerCommandTab(Screen parent) {
		super(parent);
		refreshData();
	}

	/**
	 * Reloads groups from {@link ServerCommandStore} and rebuilds the tab if it has been laid out.
	 */
	public void refreshData() {
		allGroups.clear();
		allGroups.addAll(ServerCommandStore.getGroups());
		buildFilteredCommands();
		if (area != null) {
			buildAllCategoryButtons();
			rebuildVisibleCategoryButtons();
			rebuildButtons();
		}
	}

	@Override
	public Component getTabTitle() {
		return Component.translatable("screen.command-gui.tab.server");
	}

	@Override
	protected int getFilteredCommandCount() {
		return filteredCommands.size();
	}

	@Override
	protected void buildFilteredCommands() {
		filteredCommands.clear();
		String search = searchText;
		for (int i = 0; i < allGroups.size(); i++) {
			if (selectedCategoryIndex >= 0 && selectedCategoryIndex != i) continue;
			ServerCommandGroup group = allGroups.get(i);
			if (group.getCommands() == null) continue;
			for (ServerCommandEntry entry : group.getCommands()) {
				if (search.isEmpty()
						|| entry.getName().toLowerCase().contains(search)
						|| entry.getDescription().toLowerCase().contains(search)
						|| entry.getCommands().stream().anyMatch(c -> c.toLowerCase().contains(search))) {
					filteredCommands.add(entry);
				}
			}
		}
	}

	@Override
	protected void buildAllCategoryButtons() {
		allCategoryButtons.clear();
		if (area == null) return;

		int x = area.left();
		int y = area.top();

		Button allBtn = Button.builder(
				Component.translatable("screen.command-gui.category.all"),
				btn -> onCategoryButtonClick(-1)
		).bounds(x, y, CATEGORY_TAB_WIDTH, CATEGORY_TAB_HEIGHT).build();
		allBtn.active = (selectedCategoryIndex != -1);
		allCategoryButtons.add(allBtn);

		for (int i = 0; i < allGroups.size(); i++) {
			ServerCommandGroup group = allGroups.get(i);
			final int index = i;
			Button catBtn = Button.builder(
					Component.literal(group.getName()),
					btn -> onCategoryButtonClick(index)
			).bounds(x, y, CATEGORY_TAB_WIDTH, CATEGORY_TAB_HEIGHT).build();
			catBtn.active = (selectedCategoryIndex != index);
			allCategoryButtons.add(catBtn);
		}
	}

	@Override
	protected Button buildCommandButton(int index, int x, int y, int width, int height) {
		ServerCommandEntry entry = filteredCommands.get(index);
		Button btn = Button.builder(
				Component.literal(entry.getName()),
				b -> handleCommand(entry)
		).bounds(x, y, width, height).build();

		StringBuilder tooltipText = new StringBuilder();
		if (!entry.getDescription().isEmpty()) {
			tooltipText.append(entry.getDescription()).append("\n");
		}
		tooltipText.append("§7").append(String.join("\n§7", entry.getCommands()));
		btn.setTooltip(Tooltip.create(Component.literal(tooltipText.toString())));

		return btn;
	}

	private void onCategoryButtonClick(int index) {
		if (selectedCategoryIndex != index) {
			selectCategory(index);
		}
	}

	private void updateCategoryButtonStates() {
		if (allCategoryButtons.isEmpty()) return;
		allCategoryButtons.get(0).active = (selectedCategoryIndex != -1);
		for (int i = 1; i < allCategoryButtons.size(); i++) {
			allCategoryButtons.get(i).active = (selectedCategoryIndex != (i - 1));
		}
	}

	private void selectCategory(int index) {
		notifyCategoryChange(() -> {
			selectedCategoryIndex = index;
			scrollOffset = 0;
			buildFilteredCommands();
			rebuildButtons();
			updateCategoryButtonStates();
		});
	}

	private void handleCommand(ServerCommandEntry entry) {
		if (entry.getCommands().size() == 1) {
			ChainedCommandExecutor.execute(parent, entry.getCommands().get(0),
					ChainedCommandExecutor.Config.defaultConfig());
		} else {
			ChainedCommandExecutor.executeMulti(parent, entry.getCommands());
		}
	}
}
