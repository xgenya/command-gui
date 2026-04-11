package com.remrin.client.gui;

import com.remrin.client.config.CommandConfig;
import com.remrin.client.config.PresetConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class CommandGUIScreen extends Screen {
	private static final int FOOTER_HEIGHT = 33;
	private static final int PADDING = 10;
	private static final int SCROLLBAR_WIDTH = 6;

	private static int lastSelectedTabIndex = 0;
	private static boolean keepOpenAfterExecute = false;
	private static CommandGUIScreen currentInstance = null;

	private TabManager tabManager;
	private TabNavigationBar tabNavigationBar;
	private CustomCommandTab customTab;
	private List<PresetCommandTab> presetTabs = new ArrayList<>();

	private EditBox searchField;
	private Button addButton;
	private Checkbox keepOpenCheckbox;
	private String searchText = "";

	private String contextMenuName = null;
	private CommandConfig.CommandEntry contextMenuEntry = null;
	private int contextMenuX = 0;
	private int contextMenuY = 0;
	
	private Tab lastTab = null;
	private ScreenRectangle tabArea;

	public CommandGUIScreen() {
		super(Component.translatable("screen.command-gui.title"));
	}

	private boolean hasCommandPermission() {
		if (this.minecraft == null || this.minecraft.player == null) return false;
		if (this.minecraft.hasSingleplayerServer()) {
			return true;
		}
		var abilities = this.minecraft.player.getAbilities();
		return abilities.instabuild;
	}

	@Override
	protected void init() {
		super.init();

		currentInstance = this;
		this.contextMenuName = null;
		this.contextMenuEntry = null;

		this.tabManager = new TabManager(w -> {}, w -> {});
		
		this.customTab = new CustomCommandTab(this);
		this.customTab.setOnCategoryChanged(
				this::removeCustomTabButtons,
				this::addCustomTabButtons
		);
		
		presetTabs.clear();
		boolean hasPermission = hasCommandPermission();
		for (PresetConfig.Preset preset : PresetConfig.getPresets()) {
			if ("vanilla".equals(preset.id) && !hasPermission) {
				continue;
			}
			PresetCommandTab tab = new PresetCommandTab(this, preset.id, preset.nameKey);
			tab.setOnCategoryChanged(
				() -> removePresetTabButtons(tab),
				() -> addPresetTabButtons(tab)
			);
			presetTabs.add(tab);
		}

		TabNavigationBar.Builder builder = TabNavigationBar.builder(this.tabManager, this.width);
		builder.addTabs(this.customTab);
		for (PresetCommandTab tab : presetTabs) {
			builder.addTabs(tab);
		}
		
		this.tabNavigationBar = builder.build();
		this.addRenderableWidget(this.tabNavigationBar);
		this.tabNavigationBar.arrangeElements();
		
		int tabBarBottom = this.tabNavigationBar.getRectangle().bottom();
		int searchY = tabBarBottom + 5;
		int searchWidth = this.width - PADDING * 2 - 30;

		searchField = new EditBox(this.font, PADDING, searchY, searchWidth, 20,
				Component.translatable("screen.command-gui.search_hint"));
		searchField.setHint(Component.translatable("screen.command-gui.search_hint"));
		searchField.setMaxLength(50);
		searchField.setValue(searchText);
		searchField.setResponder(this::onSearchChanged);
		this.addRenderableWidget(searchField);

		addButton = Button.builder(
				Component.translatable("screen.command-gui.add"),
				button -> this.minecraft.setScreen(new AddCommandScreen(this, customTab.getSelectedCategoryId()))
		).bounds(PADDING + searchWidth + 5, searchY, 20, 20).build();
		this.addRenderableWidget(addButton);

		int closeBtnY = this.height - FOOTER_HEIGHT + 6;
		this.addRenderableWidget(Button.builder(
				Component.translatable("screen.command-gui.close"),
				button -> this.onClose()
		).bounds(this.width / 2 + 30, closeBtnY, 60, 20).build());

		keepOpenCheckbox = Checkbox.builder(
				Component.translatable("screen.command-gui.keep_open"),
				this.font
		).pos(this.width / 2 - 120, closeBtnY).selected(keepOpenAfterExecute).build();
		this.addRenderableWidget(keepOpenCheckbox);

		this.tabNavigationBar.selectTab(lastSelectedTabIndex, false);
		
		int listTop = searchY + 25;
		
		tabArea = new ScreenRectangle(
				PADDING, 
				listTop, 
				this.width - PADDING * 2 - SCROLLBAR_WIDTH - 2, 
				this.height - listTop - FOOTER_HEIGHT
		);
		this.tabManager.setTabArea(tabArea);
		this.customTab.doLayout(tabArea);
		for (PresetCommandTab tab : presetTabs) {
			tab.doLayout(tabArea);
		}
		
		syncButtonsToScreen();
		lastTab = tabManager.getCurrentTab();
	}
	
	private void syncButtonsToScreen() {
		Tab currentTab = tabManager.getCurrentTab();
		
		addButton.visible = (currentTab == customTab);
		addButton.active = (currentTab == customTab);
		
		boolean isCustomTab = (currentTab == customTab);
		for (Button btn : customTab.getButtons()) {
			if (!this.children().contains(btn)) {
				this.addRenderableWidget(btn);
			}
			btn.visible = isCustomTab;
			btn.active = isCustomTab;
		}
		
		for (Button btn : customTab.getCategoryButtons()) {
			if (!this.children().contains(btn)) {
				this.addRenderableWidget(btn);
			}
			btn.visible = isCustomTab;
		}
		
		for (PresetCommandTab presetTab : presetTabs) {
			boolean isCurrentTab = (currentTab == presetTab);
			
			for (Button btn : presetTab.getButtons()) {
				if (!this.children().contains(btn)) {
					this.addRenderableWidget(btn);
				}
				btn.visible = isCurrentTab;
			}
			
			for (Button btn : presetTab.getCategoryButtons()) {
				if (!this.children().contains(btn)) {
					this.addRenderableWidget(btn);
				}
				btn.visible = isCurrentTab;
			}
		}
	}

	private void onSearchChanged(String text) {
		searchText = text;
		Tab currentTab = tabManager.getCurrentTab();
		removeTabButtons();
		
		if (currentTab == customTab) {
			customTab.setSearchText(text);
		} else {
			for (PresetCommandTab presetTab : presetTabs) {
				if (currentTab == presetTab) {
					presetTab.setSearchText(text);
					break;
				}
			}
		}
		syncButtonsToScreen();
	}

	private void removeTabButtons() {
		for (Button btn : customTab.getButtons()) {
			this.removeWidget(btn);
		}
		for (Button btn : customTab.getCategoryButtons()) {
			this.removeWidget(btn);
		}
		for (PresetCommandTab presetTab : presetTabs) {
			for (Button btn : presetTab.getButtons()) {
				this.removeWidget(btn);
			}
			for (Button btn : presetTab.getCategoryButtons()) {
				this.removeWidget(btn);
			}
		}
	}

	@Override
	public void repositionElements() {
		if (this.tabNavigationBar != null) {
			this.tabNavigationBar.setWidth(this.width);
			
			int tabBarBottom = this.tabNavigationBar.getRectangle().bottom();
			int searchY = tabBarBottom + 5;
			int listTop = searchY + 25;
			
			tabArea = new ScreenRectangle(
					PADDING, 
					listTop, 
					this.width - PADDING * 2 - SCROLLBAR_WIDTH - 2, 
					this.height - listTop - FOOTER_HEIGHT
			);
			
			removeTabButtons();
			customTab.doLayout(tabArea);
			for (PresetCommandTab tab : presetTabs) {
				tab.doLayout(tabArea);
			}
			syncButtonsToScreen();
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (contextMenuName != null) {
			contextMenuName = null;
			return true;
		}

		Tab currentTab = tabManager.getCurrentTab();
		if (currentTab == customTab) {
			for (Button btn : customTab.getButtons()) {
				this.removeWidget(btn);
			}
			customTab.scroll(scrollY);
			for (Button btn : customTab.getButtons()) {
				this.addRenderableWidget(btn);
			}
		} else {
			for (PresetCommandTab presetTab : presetTabs) {
				if (currentTab == presetTab) {
					for (Button btn : presetTab.getButtons()) {
						this.removeWidget(btn);
					}
					presetTab.scroll(scrollY);
					for (Button btn : presetTab.getButtons()) {
						this.addRenderableWidget(btn);
					}
					break;
				}
			}
		}
		return true;
	}

	@Override
	public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent mouseEvent, boolean focused) {
		double mouseX = mouseEvent.x();
		double mouseY = mouseEvent.y();
		int button = mouseEvent.button();

		if (contextMenuName != null) {
			int menuWidth = 60;
			int menuHeight = 40;

			if (mouseX >= contextMenuX && mouseX < contextMenuX + menuWidth &&
					mouseY >= contextMenuY && mouseY < contextMenuY + menuHeight) {

				if (mouseY < contextMenuY + 20) {
					this.minecraft.setScreen(new EditCommandScreen(this, contextMenuName, contextMenuEntry));
					return true;
				} else {
					CommandConfig.removeCommand(contextMenuName);
					removeTabButtons();
					customTab.refresh();
					syncButtonsToScreen();
					contextMenuName = null;
					return true;
				}
			}
			contextMenuName = null;
			return true;
		}

		if (button == 1 && tabManager.getCurrentTab() == customTab) {
			for (Button btn : customTab.getButtons()) {
				if (btn.visible && mouseX >= btn.getX() && mouseX < btn.getX() + btn.getWidth() &&
						mouseY >= btn.getY() && mouseY < btn.getY() + btn.getHeight()) {
					String name = btn.getMessage().getString();
					String categoryId = customTab.findCommandCategory(name);
					if (categoryId != null) {
						CommandConfig.CommandEntry entry = CommandConfig.getCommandsByCategory(categoryId).get(name);
						if (entry != null) {
							contextMenuName = name;
							contextMenuEntry = entry;
							contextMenuX = (int) mouseX;
							contextMenuY = (int) mouseY;
							return true;
						}
					}
				}
			}
		}

		return super.mouseClicked(mouseEvent, focused);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);

		int bottomSeparatorY = this.height - FOOTER_HEIGHT;
		guiGraphics.fill(0, bottomSeparatorY, this.width, bottomSeparatorY + 1, 0xFF555555);

		renderScrollbar(guiGraphics, mouseX, mouseY);

		Tab currentTab = tabManager.getCurrentTab();
		if (currentTab == customTab) {
			customTab.renderCategories(guiGraphics, mouseX, mouseY);
			
			guiGraphics.drawCenteredString(this.font,
					Component.translatable("screen.command-gui.right_click_hint"),
					this.width / 2, this.height - FOOTER_HEIGHT - 12, 0xFF888888);

			if (customTab.isEmpty()) {
				ScreenRectangle area = customTab.getArea();
				if (area != null) {
					guiGraphics.drawCenteredString(this.font,
							Component.translatable("screen.command-gui.empty"),
							this.width / 2, area.top() + area.height() / 2 - 4, 0xFF888888);
				}
			}
		} else {
			for (PresetCommandTab presetTab : presetTabs) {
				if (currentTab == presetTab) {
					presetTab.renderCategoryTabs(guiGraphics, mouseX, mouseY);
					break;
				}
			}
		}

		if (contextMenuName != null) {
			int menuWidth = 60;
			int menuHeight = 40;

			int menuX = contextMenuX;
			int menuY = contextMenuY;
			if (menuX + menuWidth > this.width) menuX = this.width - menuWidth;
			if (menuY + menuHeight > this.height - FOOTER_HEIGHT) menuY = this.height - FOOTER_HEIGHT - menuHeight;

			guiGraphics.fill(menuX - 1, menuY - 1, menuX + menuWidth + 1, menuY + menuHeight + 1, 0xFF333333);
			guiGraphics.fill(menuX, menuY, menuX + menuWidth, menuY + menuHeight, 0xFF000000);

			boolean hoverEdit = mouseX >= menuX && mouseX < menuX + menuWidth &&
					mouseY >= menuY && mouseY < menuY + 20;
			boolean hoverDelete = mouseX >= menuX && mouseX < menuX + menuWidth &&
					mouseY >= menuY + 20 && mouseY < menuY + 40;

			if (hoverEdit) {
				guiGraphics.fill(menuX, menuY, menuX + menuWidth, menuY + 20, 0xFF444444);
			}
			if (hoverDelete) {
				guiGraphics.fill(menuX, menuY + 20, menuX + menuWidth, menuY + 40, 0xFF444444);
			}

			guiGraphics.drawCenteredString(this.font,
					Component.translatable("screen.command-gui.edit"),
					menuX + menuWidth / 2, menuY + 6, hoverEdit ? 0xFF55FF55 : 0xFFFFFFFF);
			guiGraphics.drawCenteredString(this.font,
					Component.translatable("screen.command-gui.delete"),
					menuX + menuWidth / 2, menuY + 26, hoverDelete ? 0xFFFF5555 : 0xFFFFFFFF);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void onClose() {
		if (keepOpenCheckbox != null) {
			keepOpenAfterExecute = keepOpenCheckbox.selected();
		}
		currentInstance = null;
		super.onClose();
	}

	public static boolean shouldKeepOpen() {
		if (currentInstance != null && currentInstance.keepOpenCheckbox != null) {
			return currentInstance.keepOpenCheckbox.selected();
		}
		return keepOpenAfterExecute;
	}

	public void refresh() {
		removeTabButtons();
		customTab.refresh();
		syncButtonsToScreen();
	}

	private void removeCustomTabButtons() {
		for (Button btn : customTab.getCategoryButtons()) {
			this.removeWidget(btn);
		}
		for (Button btn : customTab.getButtons()) {
			this.removeWidget(btn);
		}
	}

	private void addCustomTabButtons() {
		for (Button btn : customTab.getCategoryButtons()) {
			this.addRenderableWidget(btn);
			btn.visible = true;
		}
		for (Button btn : customTab.getButtons()) {
			this.addRenderableWidget(btn);
			btn.visible = true;
		}
	}

	private void removePresetTabButtons(PresetCommandTab tab) {
		for (Button btn : tab.getButtons()) {
			this.removeWidget(btn);
		}
	}

	private void addPresetTabButtons(PresetCommandTab tab) {
		for (Button btn : tab.getButtons()) {
			this.addRenderableWidget(btn);
			btn.visible = true;
		}
	}

	private void renderScrollbar(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		if (tabArea == null) return;

		int scrollOffset;
		int maxScroll;
		
		Tab currentTab = tabManager.getCurrentTab();
		if (currentTab == customTab) {
			scrollOffset = customTab.getScrollOffset();
			maxScroll = customTab.getMaxScroll();
		} else {
			scrollOffset = 0;
			maxScroll = 0;
			for (PresetCommandTab presetTab : presetTabs) {
				if (currentTab == presetTab) {
					scrollOffset = presetTab.getScrollOffset();
					maxScroll = presetTab.getMaxScroll();
					break;
				}
			}
		}

		int scrollbarX = this.width - PADDING - SCROLLBAR_WIDTH;
		int scrollbarTop = tabArea.top();
		int scrollbarHeight = tabArea.height();

		guiGraphics.fill(scrollbarX, scrollbarTop, scrollbarX + SCROLLBAR_WIDTH, scrollbarTop + scrollbarHeight, 0xFF000000);

		if (maxScroll <= 0) {
			guiGraphics.fill(scrollbarX, scrollbarTop, scrollbarX + SCROLLBAR_WIDTH, scrollbarTop + scrollbarHeight, 0xFF808080);
			return;
		}

		int thumbHeight = Math.max(15, scrollbarHeight * scrollbarHeight / (scrollbarHeight + maxScroll * 24));
		int thumbY = scrollbarTop + (scrollbarHeight - thumbHeight) * scrollOffset / maxScroll;

		guiGraphics.fill(scrollbarX, thumbY, scrollbarX + SCROLLBAR_WIDTH, thumbY + thumbHeight, 0xFF808080);
		guiGraphics.fill(scrollbarX, thumbY, scrollbarX + SCROLLBAR_WIDTH - 1, thumbY + thumbHeight - 1, 0xFFC0C0C0);
		guiGraphics.fill(scrollbarX + 1, thumbY + 1, scrollbarX + SCROLLBAR_WIDTH - 1, thumbY + thumbHeight - 1, 0xFF808080);
	}
	
	@Override
	public void tick() {
		super.tick();
		
		Tab currentTab = tabManager.getCurrentTab();
		
		if (lastTab != currentTab) {
			removeTabButtons();
			
			if (currentTab == customTab) {
				customTab.setSearchText(searchText);
				lastSelectedTabIndex = 0;
			} else {
				for (int i = 0; i < presetTabs.size(); i++) {
					if (currentTab == presetTabs.get(i)) {
						presetTabs.get(i).setSearchText(searchText);
						lastSelectedTabIndex = i + 1;
						break;
					}
				}
			}
			
			syncButtonsToScreen();
			lastTab = currentTab;
		}
	}
}
