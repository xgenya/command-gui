package com.remrin.client.gui;

import com.remrin.client.config.CommandConfig;
import com.remrin.client.config.PresetConfig;
import com.remrin.client.config.SettingsConfig;
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
	private static final int CONTEXT_MENU_WIDTH = 80;
	private static final int CONTEXT_MENU_HEIGHT = 40;
	private static final int CONTEXT_MENU_ITEM_HEIGHT = 20;

	private static int lastSelectedTabIndex = 0;
	private static boolean keepOpenAfterExecute = false;
	private static CommandGUIScreen currentInstance = null;

	private TabManager tabManager;
	private TabNavigationBar tabNavigationBar;
	private CustomCommandTab customTab;
	private FakePlayerTab fakePlayerTab;
	private List<PresetCommandTab> presetTabs = new ArrayList<>();

	private EditBox searchField;
	private Button addButton;
	private Button closeButton;
	private Checkbox keepOpenCheckbox;
	private SettingsButton settingsButton;
	private String searchText = "";

	private String contextMenuName = null;
	private CommandConfig.CommandEntry contextMenuEntry = null;
	private int contextMenuX = 0;
	private int contextMenuY = 0;
	
	private Tab lastTab = null;
	private ScreenRectangle tabArea;
	private int fakePlayerRefreshTicks = 0;
	private static final int REFRESH_INTERVAL = 10;

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
				() -> removeTabButtons(customTab),
				() -> addTabButtons(customTab)
		);
		
		this.fakePlayerTab = new FakePlayerTab(this);
		this.fakePlayerTab.setOnRebuild(
				() -> removeTabButtons(fakePlayerTab),
				() -> addTabButtons(fakePlayerTab)
		);
		
		presetTabs.clear();
		boolean hasPermission = hasCommandPermission();
		for (PresetConfig.Preset preset : PresetConfig.getPresets()) {
			if ("vanilla".equals(preset.id)) {
				if (!hasPermission || !SettingsConfig.getBoolean("show_vanilla_commands")) {
					continue;
				}
			}
			if ("carpet".equals(preset.id)) {
				if (!SettingsConfig.getBoolean("show_carpet_commands")) {
					continue;
				}
			}
			PresetCommandTab tab = new PresetCommandTab(this, preset.id, preset.nameKey);
			tab.setOnCategoryChanged(
				() -> removeTabButtons(tab),
				() -> addTabButtons(tab)
			);
			presetTabs.add(tab);
		}

		TabNavigationBar.Builder builder = TabNavigationBar.builder(this.tabManager, this.width);
		builder.addTabs(this.customTab);
		if (SettingsConfig.getBoolean("show_fakeplayer_tab")) {
			builder.addTabs(this.fakePlayerTab);
		}
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
		closeButton = Button.builder(
				Component.translatable("screen.command-gui.close"),
				button -> this.onClose()
		).bounds(this.width / 2 + 30, closeBtnY, 60, 20).build();
		this.addRenderableWidget(closeButton);

		keepOpenCheckbox = Checkbox.builder(
				Component.translatable("screen.command-gui.keep_open"),
				this.font
		).pos(this.width / 2 - 120, closeBtnY).selected(keepOpenAfterExecute).build();
		this.addRenderableWidget(keepOpenCheckbox);

		settingsButton = new SettingsButton(
				this.width - PADDING - 20, closeBtnY,
				20, 20,
				btn -> this.minecraft.setScreen(new SettingsScreen(this))
		);
		this.addRenderableWidget(settingsButton);

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
		this.fakePlayerTab.doLayout(tabArea);
		for (PresetCommandTab tab : presetTabs) {
			tab.doLayout(tabArea);
		}
		
		addTabButtons(tabManager.getCurrentTab());
		updateTabDependentWidgets(tabManager.getCurrentTab());
		lastTab = tabManager.getCurrentTab();
	}

	/** Add only the current tab's buttons to the screen widget list. */
	private void addTabButtons(Tab tab) {
		if (tab instanceof AbstractCommandTab ct) {
			ct.getCategoryButtons().forEach(this::addRenderableWidget);
			ct.getButtons().forEach(this::addRenderableWidget);
		} else if (tab == fakePlayerTab) {
			fakePlayerTab.getButtons().forEach(this::addRenderableWidget);
		}
	}

	/** Remove only the given tab's buttons from the screen widget list. */
	private void removeTabButtons(Tab tab) {
		if (tab instanceof AbstractCommandTab ct) {
			ct.getCategoryButtons().forEach(this::removeWidget);
			ct.getButtons().forEach(this::removeWidget);
		} else if (tab == fakePlayerTab) {
			fakePlayerTab.getButtons().forEach(this::removeWidget);
		}
	}

	/** Update visibility of widgets that depend on which tab is active. */
	private void updateTabDependentWidgets(Tab tab) {
		boolean isFakePlayerTab = (tab == fakePlayerTab);
		searchField.visible = !isFakePlayerTab;
		searchField.active = !isFakePlayerTab;
		addButton.visible = (tab == customTab);
		addButton.active = (tab == customTab);
		closeButton.visible = !isFakePlayerTab;
		closeButton.active = !isFakePlayerTab;
		keepOpenCheckbox.visible = !isFakePlayerTab;
		keepOpenCheckbox.active = !isFakePlayerTab;
		settingsButton.visible = !isFakePlayerTab;
		settingsButton.active = !isFakePlayerTab;
	}

	private void onSearchChanged(String text) {
		searchText = text;
		Tab currentTab = tabManager.getCurrentTab();

		if (currentTab instanceof AbstractCommandTab ct) {
			removeTabButtons(ct);
			ct.setSearchText(text);
			addTabButtons(ct);
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
			
			Tab currentTab = tabManager.getCurrentTab();
			removeTabButtons(currentTab);
			customTab.doLayout(tabArea);
			fakePlayerTab.doLayout(tabArea);
			for (PresetCommandTab tab : presetTabs) {
				tab.doLayout(tabArea);
			}
			addTabButtons(currentTab);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (contextMenuName != null) {
			clearContextMenu();
			return true;
		}

		Tab currentTab = tabManager.getCurrentTab();
		if (currentTab == fakePlayerTab) {
			fakePlayerTab.scroll(scrollY);
		} else if (currentTab == customTab) {
			if (customTab.isInCategoryArea(mouseX, mouseY)) {
				removeTabButtons(customTab);
				customTab.scrollCategory(scrollY);
				addTabButtons(customTab);
			} else {
				removeTabButtons(customTab);
				customTab.scroll(scrollY);
				addTabButtons(customTab);
			}
		} else {
			for (PresetCommandTab presetTab : presetTabs) {
				if (currentTab == presetTab) {
					if (presetTab.isInCategoryArea(mouseX, mouseY)) {
						removeTabButtons(presetTab);
						presetTab.scrollCategory(scrollY);
						addTabButtons(presetTab);
					} else {
						removeTabButtons(presetTab);
						presetTab.scroll(scrollY);
						addTabButtons(presetTab);
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

		if (contextMenuEntry != null) {
			int menuWidth = CONTEXT_MENU_WIDTH;
			int menuHeight = CONTEXT_MENU_HEIGHT;
			
			int menuX = contextMenuX;
			int menuY = contextMenuY;
			if (menuX + menuWidth > this.width) menuX = this.width - menuWidth;
			if (menuY + menuHeight > this.height - FOOTER_HEIGHT) menuY = this.height - FOOTER_HEIGHT - menuHeight;

			if (mouseX >= menuX && mouseX < menuX + menuWidth &&
					mouseY >= menuY && mouseY < menuY + menuHeight) {

				if (mouseY < menuY + CONTEXT_MENU_ITEM_HEIGHT) {
					clearContextMenu();
					this.minecraft.setScreen(new EditCommandScreen(this, contextMenuName, contextMenuEntry));
					return true;
				} else {
					CommandConfig.removeCommand(contextMenuName);
					removeTabButtons(customTab);
					customTab.refresh();
					addTabButtons(customTab);
				}
				clearContextMenu();
				return true;
			}
			clearContextMenu();
			return true;
		}

		Tab currentTab = tabManager.getCurrentTab();

		if (button == 1 && currentTab == customTab) {
			for (Button btn : customTab.getButtons()) {
				if (btn.visible && mouseX >= btn.getX() && mouseX < btn.getX() + btn.getWidth() &&
						mouseY >= btn.getY() && mouseY < btn.getY() + btn.getHeight()) {
					String name = btn.getMessage().getString();
					String categoryId = CommandConfig.findCommandCategory(name);
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
	
	private void clearContextMenu() {
		contextMenuName = null;
		contextMenuEntry = null;
	}
	
	private void executeCommand(String command) {
		if (this.minecraft != null && this.minecraft.player != null) {
			if (command.startsWith("/")) {
				this.minecraft.player.connection.sendCommand(command.substring(1));
			} else {
				this.minecraft.player.connection.sendCommand(command);
			}
		}
		if (!shouldKeepOpen()) {
			this.minecraft.setScreen(null);
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);

		Tab currentTab = tabManager.getCurrentTab();

		if (currentTab != fakePlayerTab) {
			int bottomSeparatorY = this.height - FOOTER_HEIGHT;
			guiGraphics.fill(0, bottomSeparatorY, this.width, bottomSeparatorY + 1, 0xFF555555);
			renderScrollbar(guiGraphics, mouseX, mouseY);
		}
		
		if (currentTab == customTab) {
			customTab.renderCategoryScrollbar(guiGraphics);
			
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
		} else if (currentTab == fakePlayerTab) {
			fakePlayerTab.render(guiGraphics);
			fakePlayerTab.renderFaces(guiGraphics);
			fakePlayerTab.renderScrollbar(guiGraphics);
			
			int tabBarBottom = this.tabNavigationBar.getRectangle().bottom();
			if (fakePlayerTab.isEmpty()) {
				ScreenRectangle area = fakePlayerTab.getArea();
				if (area != null) {
					guiGraphics.drawCenteredString(this.font,
							Component.translatable("screen.command-gui.fakeplayer.empty"),
							this.width / 2, area.top() + area.height() / 2 - 4, 0xFF888888);
				}
			} else if (fakePlayerTab.getSelectedPlayer() == null) {
				guiGraphics.drawCenteredString(this.font,
						Component.translatable("screen.command-gui.fakeplayer.select_hint"),
						this.width / 2, tabBarBottom + 8, 0xFF888888);
			} else {
				guiGraphics.drawCenteredString(this.font,
						Component.translatable("screen.command-gui.fakeplayer.selected", fakePlayerTab.getSelectedPlayer()),
						this.width / 2, tabBarBottom + 8, 0xFF55FF55);
			}
		} else {
			for (PresetCommandTab presetTab : presetTabs) {
				if (currentTab == presetTab) {
					presetTab.renderCategoryScrollbar(guiGraphics);
					break;
				}
			}
		}

		renderContextMenu(guiGraphics, mouseX, mouseY);
	}
	
	private void renderContextMenu(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		if (contextMenuEntry == null) return;
		
		int menuWidth = CONTEXT_MENU_WIDTH;
		int menuHeight = CONTEXT_MENU_HEIGHT;

		int menuX = contextMenuX;
		int menuY = contextMenuY;
		if (menuX + menuWidth > this.width) menuX = this.width - menuWidth;
		if (menuY + menuHeight > this.height - FOOTER_HEIGHT) menuY = this.height - FOOTER_HEIGHT - menuHeight;

		guiGraphics.fill(menuX - 1, menuY - 1, menuX + menuWidth + 1, menuY + menuHeight + 1, 0xFF333333);
		guiGraphics.fill(menuX, menuY, menuX + menuWidth, menuY + menuHeight, 0xFF000000);

		boolean hoverEdit = mouseX >= menuX && mouseX < menuX + menuWidth &&
				mouseY >= menuY && mouseY < menuY + CONTEXT_MENU_ITEM_HEIGHT;
		boolean hoverDelete = mouseX >= menuX && mouseX < menuX + menuWidth &&
				mouseY >= menuY + CONTEXT_MENU_ITEM_HEIGHT && mouseY < menuY + CONTEXT_MENU_HEIGHT;

		if (hoverEdit) {
			guiGraphics.fill(menuX, menuY, menuX + menuWidth, menuY + CONTEXT_MENU_ITEM_HEIGHT, 0xFF444444);
		}
		if (hoverDelete) {
			guiGraphics.fill(menuX, menuY + CONTEXT_MENU_ITEM_HEIGHT, menuX + menuWidth, menuY + CONTEXT_MENU_HEIGHT, 0xFF444444);
		}

		guiGraphics.drawCenteredString(this.font,
				Component.translatable("screen.command-gui.edit"),
				menuX + menuWidth / 2, menuY + 6, hoverEdit ? 0xFF55FF55 : 0xFFFFFFFF);
		guiGraphics.drawCenteredString(this.font,
				Component.translatable("screen.command-gui.delete"),
				menuX + menuWidth / 2, menuY + 26, hoverDelete ? 0xFFFF5555 : 0xFFFFFFFF);
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
		fakePlayerRefreshTicks = 0;
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
		removeTabButtons(customTab);
		customTab.refresh();
		addTabButtons(customTab);
	}

	private void renderScrollbar(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		if (tabArea == null) return;

		int scrollOffset;
		int maxScroll;
		
		Tab currentTab = tabManager.getCurrentTab();
		if (currentTab == customTab) {
			scrollOffset = customTab.getScrollOffset();
			maxScroll = customTab.getMaxScroll();
		} else if (currentTab == fakePlayerTab) {
			scrollOffset = fakePlayerTab.getScrollOffset();
			maxScroll = fakePlayerTab.getMaxScroll();
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
			removeTabButtons(lastTab);
			if (lastTab == fakePlayerTab) {
				fakePlayerTab.clearSelection();
			}
			
			if (currentTab == customTab) {
				customTab.setSearchText(searchText);
				lastSelectedTabIndex = 0;
			} else if (currentTab == fakePlayerTab) {
				fakePlayerTab.refresh();
				fakePlayerRefreshTicks = 0;
				lastSelectedTabIndex = 1;
			} else {
				for (int i = 0; i < presetTabs.size(); i++) {
					if (currentTab == presetTabs.get(i)) {
						presetTabs.get(i).setSearchText(searchText);
						lastSelectedTabIndex = i + 2;
						break;
					}
				}
			}
			
			addTabButtons(currentTab);
			updateTabDependentWidgets(currentTab);
			lastTab = currentTab;
		}
		
		if (currentTab == fakePlayerTab) {
			fakePlayerRefreshTicks++;
			if (fakePlayerRefreshTicks >= REFRESH_INTERVAL) {
				fakePlayerRefreshTicks = 0;
				removeTabButtons(fakePlayerTab);
				fakePlayerTab.refresh();
				addTabButtons(fakePlayerTab);
			}
		}
	}
}
