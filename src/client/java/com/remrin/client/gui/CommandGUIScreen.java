package com.remrin.client.gui;

import com.remrin.client.config.PresetConfig;
import com.remrin.client.config.SettingsConfig;
import com.remrin.client.sync.ServerCommandStore;
import java.util.ArrayList;
import java.util.List;
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

/**
 * The mod's main screen, containing multiple tabs (custom commands, fake player management, preset
 * commands).
 * <p>
 * Key responsibilities:
 * <ul>
 *   <li>Managing the {@link TabNavigationBar} and the lifecycle of each tab</li>
 *   <li>Registering/unregistering each tab's buttons in the parent screen widget list</li>
 *   <li>Rendering the scrollbar and the bottom separator line</li>
 *   <li>Maintaining the search filter state and the "keep open after execute" checkbox state</li>
 * </ul>
 */
public class CommandGUIScreen extends Screen {

  /**
   * Height of the bottom toolbar (pixels)
   */
  private static final int FOOTER_HEIGHT = 33;
  private static final int PADDING = 10;
  private static final int SCROLLBAR_WIDTH = 6;
  /**
   * Tick interval for auto-refreshing the fake player list
   */
  private static final int REFRESH_INTERVAL = 10;
  /**
   * Remembers the last selected tab index across screen rebuilds
   */
  private static int lastSelectedTabIndex = 0;
  /**
   * Persistent "keep GUI open after execute" state, preserved across screen instances
   */
  private static boolean keepOpenAfterExecute = false;
  /**
   * Current active instance, used by the static {@link #shouldKeepOpen()} method to query the
   * checkbox state in real time
   */
  private static CommandGUIScreen currentInstance = null;
  private TabManager tabManager;
  private TabNavigationBar tabNavigationBar;
  private CustomCommandTab customTab;
  private FakePlayerTab fakePlayerTab;
  private List<PresetCommandTab> presetTabs = new ArrayList<>();
  private ServerCommandTab serverCommandTab;
  private EditBox searchField;
  private Button addButton;
  private Button addFakePlayerButton;
  private Button closeButton;
  private Checkbox keepOpenCheckbox;
  private SettingsButton settingsButton;
  private String searchText = "";
  private Tab lastTab = null;
  private ScreenRectangle tabArea;
  private ScreenRectangle fakePlayerArea;
  /**
   * Tick counter for periodic fake player tab refresh (refreshes every {@link #REFRESH_INTERVAL}
   * ticks)
   */
  private int fakePlayerRefreshTicks = 0;

  public CommandGUIScreen() {
    super(Component.translatable("screen.command-gui.title"));
  }

  public static boolean shouldKeepOpen() {
    if (currentInstance != null && currentInstance.keepOpenCheckbox != null) {
      return currentInstance.keepOpenCheckbox.selected();
    }
    return keepOpenAfterExecute;
  }

  /**
   * Checks whether the current player has command permissions: always allowed in singleplayer,
   * requires creative-mode permissions on multiplayer. Used to decide whether to show the vanilla
   * commands tab.
   */
  private boolean hasCommandPermission() {
		if (this.minecraft == null || this.minecraft.player == null) {
			return false;
		}
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

    this.tabManager = new TabManager(w -> {
    }, w -> {
    });

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

    // Add server command tab if data has been received from eun_carpet
    if (ServerCommandStore.hasData()) {
      this.serverCommandTab = new ServerCommandTab(this);
      this.serverCommandTab.setOnCategoryChanged(
          () -> removeTabButtons(serverCommandTab),
          () -> addTabButtons(serverCommandTab)
      );
      builder.addTabs(this.serverCommandTab);
    } else {
      this.serverCommandTab = null;
    }

    this.tabNavigationBar = builder.build();
    this.addRenderableWidget(this.tabNavigationBar);
    this.tabNavigationBar.arrangeElements();

    int tabBarBottom = this.tabNavigationBar.getRectangle().bottom();

    int closeBtnY = this.height - FOOTER_HEIGHT + 6;

    // Keep-open checkbox: far left
    keepOpenCheckbox = Checkbox.builder(
        Component.translatable("screen.command-gui.keep_open"),
        this.font
    ).pos(PADDING, closeBtnY).selected(keepOpenAfterExecute).build();
    this.addRenderableWidget(keepOpenCheckbox);

    // Search bar + add buttons: centered
    int searchWidth = 90;
    int searchGroupWidth = searchWidth + 2 + 20 + 2 + 20; // 134
    int searchGroupX = this.width / 2 - searchGroupWidth / 2;
    searchField = new EditBox(this.font, searchGroupX, closeBtnY, searchWidth, 20,
        Component.translatable("screen.command-gui.search_hint"));
    searchField.setHint(Component.translatable("screen.command-gui.search_hint"));
    searchField.setMaxLength(50);
    searchField.setValue(searchText);
    searchField.setResponder(this::onSearchChanged);
    this.addRenderableWidget(searchField);

    addButton = Button.builder(
        Component.translatable("screen.command-gui.add"),
        button -> this.minecraft.setScreen(
            new AddCommandScreen(this, customTab.getSelectedCategoryId()))
    ).bounds(searchGroupX + searchWidth + 2, closeBtnY, 20, 20).build();
    this.addRenderableWidget(addButton);

    addFakePlayerButton = Button.builder(
        Component.literal("\uD83E\uDDD1"),
        button -> this.minecraft.setScreen(
            new AddFakePlayerCommandScreen(this, customTab.getSelectedCategoryId()))
    ).bounds(searchGroupX + searchWidth + 24, closeBtnY, 20, 20).build();
    addFakePlayerButton.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
        Component.translatable("screen.command-gui.add_fakeplayer_cmd")));
    this.addRenderableWidget(addFakePlayerButton);

    // Close button: far right
    closeButton = Button.builder(
        Component.translatable("screen.command-gui.close"),
        button -> this.onClose()
    ).bounds(this.width - PADDING - 60, closeBtnY, 60, 20).build();
    this.addRenderableWidget(closeButton);

    // Settings button: hidden (functionality kept, entry hidden)
    settingsButton = new SettingsButton(
        this.width - PADDING - 20, closeBtnY,
        20, 20,
        btn -> this.minecraft.setScreen(new SettingsScreen(this))
    );
    settingsButton.visible = false;
    settingsButton.active = false;
    this.addRenderableWidget(settingsButton);

    this.tabNavigationBar.selectTab(lastSelectedTabIndex, false);

    int listTop = tabBarBottom + 4;
    // Tab area now extends to the footer (no separate search bar row)
    int tabAreaHeight = Math.max(20, this.height - FOOTER_HEIGHT - listTop - 2);

    tabArea = new ScreenRectangle(
        PADDING,
        listTop,
        this.width - PADDING * 2 - SCROLLBAR_WIDTH - 2,
        tabAreaHeight
    );
    this.tabManager.setTabArea(tabArea);
    this.customTab.doLayout(tabArea);
    int fpAreaHeight = Math.max(20, this.height - (tabBarBottom + 4) - 4);
    fakePlayerArea = new ScreenRectangle(
        PADDING,
        tabBarBottom + 4,
        this.width - PADDING * 2 - SCROLLBAR_WIDTH - 2,
        fpAreaHeight
    );
    this.fakePlayerTab.doLayout(fakePlayerArea);
    for (PresetCommandTab tab : presetTabs) {
      tab.doLayout(tabArea);
    }
    if (serverCommandTab != null) {
      serverCommandTab.doLayout(tabArea);
    }

    addTabButtons(tabManager.getCurrentTab());
    updateTabDependentWidgets(tabManager.getCurrentTab());
    lastTab = tabManager.getCurrentTab();
  }

  /**
   * Add only the current tab's buttons to the screen widget list.
   */
  private void addTabButtons(Tab tab) {
    if (tab instanceof AbstractCommandTab ct) {
      ct.getCategoryButtons().forEach(this::addRenderableWidget);
      ct.getButtons().forEach(this::addRenderableWidget);
    } else if (tab == fakePlayerTab) {
      fakePlayerTab.getButtons().forEach(this::addRenderableWidget);
      fakePlayerTab.getIntervalFields().forEach(this::addRenderableWidget);
    }
  }

  /**
   * Remove only the given tab's buttons from the screen widget list.
   */
  private void removeTabButtons(Tab tab) {
    if (tab instanceof AbstractCommandTab ct) {
      ct.getCategoryButtons().forEach(this::removeWidget);
      ct.getButtons().forEach(this::removeWidget);
    } else if (tab == fakePlayerTab) {
      fakePlayerTab.getButtons().forEach(this::removeWidget);
      fakePlayerTab.getIntervalFields().forEach(this::removeWidget);
    }
  }

  /**
   * Update visibility of widgets that depend on which tab is active.
   */
  private void updateTabDependentWidgets(Tab tab) {
    boolean isFakePlayerTab = (tab == fakePlayerTab);
    boolean isCustomTab = (tab == customTab);
    searchField.visible = !isFakePlayerTab;
    searchField.active = !isFakePlayerTab;
    addButton.visible = isCustomTab;
    addButton.active = isCustomTab;
    addFakePlayerButton.visible = isCustomTab;
    addFakePlayerButton.active = isCustomTab;
    closeButton.visible = !isFakePlayerTab;
    closeButton.active = !isFakePlayerTab;
    keepOpenCheckbox.visible = !isFakePlayerTab;
    keepOpenCheckbox.active = !isFakePlayerTab;
    // Settings button is always hidden (functionality kept via SettingsScreen)
    settingsButton.visible = false;
    settingsButton.active = false;
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
      int listTop = tabBarBottom + 4;
      int tabAreaHeight = Math.max(20, this.height - FOOTER_HEIGHT - listTop - 2);

      tabArea = new ScreenRectangle(
          PADDING,
          listTop,
          this.width - PADDING * 2 - SCROLLBAR_WIDTH - 2,
          tabAreaHeight
      );

      Tab currentTab = tabManager.getCurrentTab();
      removeTabButtons(currentTab);
      customTab.doLayout(tabArea);
      int fpAreaHeight = Math.max(20, this.height - (tabBarBottom + 4) - 4);
      fakePlayerArea = new ScreenRectangle(
          PADDING,
          tabBarBottom + 4,
          this.width - PADDING * 2 - SCROLLBAR_WIDTH - 2,
          fpAreaHeight
      );
      fakePlayerTab.doLayout(fakePlayerArea);
      for (PresetCommandTab tab : presetTabs) {
        tab.doLayout(tabArea);
      }
      if (serverCommandTab != null) {
        serverCommandTab.doLayout(tabArea);
      }
      addTabButtons(currentTab);
    }
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
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
      if (currentTab == serverCommandTab && serverCommandTab != null) {
        if (serverCommandTab.isInCategoryArea(mouseX, mouseY)) {
          removeTabButtons(serverCommandTab);
          serverCommandTab.scrollCategory(scrollY);
          addTabButtons(serverCommandTab);
        } else {
          removeTabButtons(serverCommandTab);
          serverCommandTab.scroll(scrollY);
          addTabButtons(serverCommandTab);
        }
      }
    }
    return true;
  }

  @Override
  public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent mouseEvent,
      boolean focused) {
    return super.mouseClicked(mouseEvent, focused);
  }

  private void executeCommand(String command) {
    if (this.minecraft != null && this.minecraft.player != null) {
      CommandHelper.sendCommand(command);
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
      if (tabArea != null) {
        guiGraphics.enableScissor(tabArea.left(), tabArea.top(),
            tabArea.right(), tabArea.bottom());
      }
      customTab.renderCategoryScrollbar(guiGraphics);
      if (tabArea != null) {
        guiGraphics.disableScissor();
      }

      if (customTab.isEmpty()) {
        ScreenRectangle area = customTab.getArea();
        if (area != null) {
          guiGraphics.drawCenteredString(this.font,
              Component.translatable("screen.command-gui.empty"),
              this.width / 2, area.top() + area.height() / 2 - 4, 0xFF888888);
        }
      }
    } else if (currentTab == fakePlayerTab) {
      if (fakePlayerArea != null) {
        guiGraphics.enableScissor(fakePlayerArea.left(), fakePlayerArea.top(),
            fakePlayerArea.right(), fakePlayerArea.bottom());
      }
      fakePlayerTab.render(guiGraphics, mouseX, mouseY);
      fakePlayerTab.renderFaces(guiGraphics);
      fakePlayerTab.renderScrollbar(guiGraphics);
      if (fakePlayerArea != null) {
        guiGraphics.disableScissor();
      }
    } else {
      for (PresetCommandTab presetTab : presetTabs) {
        if (currentTab == presetTab) {
          if (tabArea != null) {
            guiGraphics.enableScissor(tabArea.left(), tabArea.top(),
                tabArea.right(), tabArea.bottom());
          }
          presetTab.renderCategoryScrollbar(guiGraphics);
          if (tabArea != null) {
            guiGraphics.disableScissor();
          }
          break;
        }
      }
      if (currentTab == serverCommandTab && serverCommandTab != null) {
        if (tabArea != null) {
          guiGraphics.enableScissor(tabArea.left(), tabArea.top(),
              tabArea.right(), tabArea.bottom());
        }
        serverCommandTab.renderSeparator(guiGraphics);
        serverCommandTab.renderCategoryScrollbar(guiGraphics);
        if (tabArea != null) {
          guiGraphics.disableScissor();
        }
      }
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
    fakePlayerRefreshTicks = 0;
    currentInstance = null;
    super.onClose();
  }

  public void refresh() {
    removeTabButtons(customTab);
    customTab.refresh();
    addTabButtons(customTab);
  }

  /**
   * Calculates the scrollbar thumb position based on the scroll offset and visible area height,
   * then renders it. When no scrolling is needed (maxScroll == 0), the entire track is filled with
   * grey to indicate no scroll.
   */
  private void renderScrollbar(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		if (tabArea == null) {
			return;
		}

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
      if (currentTab == serverCommandTab && serverCommandTab != null) {
        scrollOffset = serverCommandTab.getScrollOffset();
        maxScroll = serverCommandTab.getMaxScroll();
      }
    }

    int scrollbarX = this.width - PADDING - SCROLLBAR_WIDTH;
    int scrollbarTop = tabArea.top();
    int scrollbarHeight = tabArea.height();

    guiGraphics.fill(scrollbarX, scrollbarTop, scrollbarX + SCROLLBAR_WIDTH,
        scrollbarTop + scrollbarHeight, 0xFF000000);

    if (maxScroll <= 0) {
      guiGraphics.fill(scrollbarX, scrollbarTop, scrollbarX + SCROLLBAR_WIDTH,
          scrollbarTop + scrollbarHeight, 0xFF808080);
      return;
    }

    int thumbHeight = Math.max(12,
        scrollbarHeight * scrollbarHeight / (scrollbarHeight + maxScroll * 24));
    int thumbY = scrollbarTop + (scrollbarHeight - thumbHeight) * scrollOffset / maxScroll;

    guiGraphics.fill(scrollbarX, thumbY, scrollbarX + SCROLLBAR_WIDTH, thumbY + thumbHeight,
        0xFF808080);
    guiGraphics.fill(scrollbarX, thumbY, scrollbarX + SCROLLBAR_WIDTH - 1, thumbY + thumbHeight - 1,
        0xFFC0C0C0);
    guiGraphics.fill(scrollbarX + 1, thumbY + 1, scrollbarX + SCROLLBAR_WIDTH - 1,
        thumbY + thumbHeight - 1, 0xFF808080);
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
        if (currentTab == serverCommandTab && serverCommandTab != null) {
          serverCommandTab.setSearchText(searchText);
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
