package com.remrin.client.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class PresetCommandTab extends AbstractCommandTab {
	private final String presetId;
	private final String nameKey;
	private final List<VanillaCommands.CommandGroup> allGroups = new ArrayList<>();
	private final List<VanillaCommands.VanillaCommand> filteredCommands = new ArrayList<>();
	private int selectedCategoryIndex = -1;

	public PresetCommandTab(Screen parent, String presetId, String nameKey) {
		super(parent);
		this.presetId = presetId;
		this.nameKey = nameKey;
		this.allGroups.addAll(VanillaCommands.getGroups(presetId));
		buildFilteredCommands();
	}

	@Override
	public Component getTabTitle() {
		return Component.translatable(nameKey);
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
			VanillaCommands.CommandGroup group = allGroups.get(i);
			final int index = i;
			Button catBtn = Button.builder(
					Component.translatable(group.nameKey),
					btn -> onCategoryButtonClick(index)
			).bounds(x, y, CATEGORY_TAB_WIDTH, CATEGORY_TAB_HEIGHT).build();
			catBtn.active = (selectedCategoryIndex != index);
			allCategoryButtons.add(catBtn);
		}
	}

	@Override
	protected Button buildCommandButton(int index, int x, int y, int width, int height) {
		VanillaCommands.VanillaCommand cmd = filteredCommands.get(index);
		Button btn = Button.builder(cmd.getName(), b -> handleCommand(cmd))
				.bounds(x, y, width, height).build();

		Component desc = cmd.getDescription();
		if (desc != null) {
			btn.setTooltip(Tooltip.create(desc.copy().append("\n§7" + cmd.command)));
		} else {
			btn.setTooltip(Tooltip.create(Component.literal(cmd.command)));
		}
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

	private void handleCommand(VanillaCommands.VanillaCommand cmd) {
		ChainedCommandExecutor.Config config = ChainedCommandExecutor.Config.defaultConfig();
		if (cmd.quickStrValues != null && cmd.quickStrValues.length > 0) {
			config.withTimeRange(cmd.minValue, cmd.maxValue, cmd.quickStrValues);
		} else if (cmd.minValue != null || cmd.maxValue != null || cmd.quickValues != null) {
			config.withNumberRange(cmd.minValue, cmd.maxValue, cmd.quickValues);
		}
		ChainedCommandExecutor.execute(parent, cmd.command, config);
	}

	public String getPresetId() {
		return presetId;
	}

	public int getScrollOffset() {
		return scrollOffset;
	}

	public VanillaCommands.VanillaCommand getCommandAt(int index) {
		if (index >= 0 && index < filteredCommands.size()) {
			return filteredCommands.get(index);
		}
		return null;
	}

	public int getCommandIndexAtPosition(double mouseX, double mouseY) {
		if (area == null) return -1;
		
		int cmdAreaLeft = getCommandAreaLeft();
		int cmdAreaWidth = getCommandAreaWidth();
		
		if (mouseX < cmdAreaLeft || mouseX > cmdAreaLeft + cmdAreaWidth) return -1;
		if (mouseY < area.top() || mouseY > area.bottom()) return -1;

		int colWidth = cmdAreaWidth / COLUMNS;
		int col = (int) ((mouseX - cmdAreaLeft) / colWidth);
		int row = (int) ((mouseY - area.top()) / ITEM_HEIGHT);
		
		int index = (scrollOffset + row) * COLUMNS + col;
		if (index >= 0 && index < filteredCommands.size()) {
			return index;
		}
		return -1;
	}
}
