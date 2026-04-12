package com.remrin.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractCommandTab implements Tab {
    protected static final int ITEM_HEIGHT = 24;
    protected static final int COLUMNS = 3;
    protected static final int CATEGORY_TAB_WIDTH = 50;
    protected static final int CATEGORY_TAB_HEIGHT = 16;
    protected static final int CATEGORY_TAB_GAP = 2;
    protected static final int CATEGORY_SCROLLBAR_WIDTH = 4;

    protected final Screen parent;
    protected final List<Button> commandButtons = new ArrayList<>();
    protected final List<Button> categoryButtons = new ArrayList<>();
    protected final List<Button> allCategoryButtons = new ArrayList<>();
    protected int scrollOffset = 0;
    protected int categoryScrollOffset = 0;
    protected String searchText = "";
    protected ScreenRectangle area;
    private Runnable onBeforeCategoryChanged;
    private Runnable onAfterCategoryChanged;

    protected AbstractCommandTab(Screen parent) {
        this.parent = parent;
    }

    protected abstract int getFilteredCommandCount();
    protected abstract void buildFilteredCommands();
    protected abstract void buildAllCategoryButtons();
    protected abstract Button buildCommandButton(int index, int x, int y, int width, int height);

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
        buildAllCategoryButtons();
        rebuildVisibleCategoryButtons();
        rebuildButtons();
    }

    protected void rebuildVisibleCategoryButtons() {
        categoryButtons.clear();
        if (area == null) return;

        int visibleCount = getVisibleCategoryCount();
        int startIndex = categoryScrollOffset;
        int endIndex = Math.min(startIndex + visibleCount, allCategoryButtons.size());

        for (int i = startIndex; i < endIndex; i++) {
            Button btn = allCategoryButtons.get(i);
            int y = area.top() + (i - startIndex) * (CATEGORY_TAB_HEIGHT + CATEGORY_TAB_GAP);
            btn.setY(y);
            categoryButtons.add(btn);
        }
    }

    protected int getVisibleCategoryCount() {
        if (area == null) return 0;
        return area.height() / (CATEGORY_TAB_HEIGHT + CATEGORY_TAB_GAP);
    }

    public int getCategoryScrollOffset() {
        return categoryScrollOffset;
    }

    public int getMaxCategoryScroll() {
        if (area == null || allCategoryButtons.isEmpty()) return 0;
        int visibleCount = getVisibleCategoryCount();
        return Math.max(0, allCategoryButtons.size() - visibleCount);
    }

    public void scrollCategory(double delta) {
        if (area == null) return;
        int maxScroll = getMaxCategoryScroll();
        if (maxScroll > 0) {
            if (delta > 0 && categoryScrollOffset > 0) {
                categoryScrollOffset--;
                rebuildVisibleCategoryButtons();
            } else if (delta < 0 && categoryScrollOffset < maxScroll) {
                categoryScrollOffset++;
                rebuildVisibleCategoryButtons();
            }
        }
    }

    public boolean isInCategoryArea(double mouseX, double mouseY) {
        if (area == null) return false;
        return mouseX >= area.left() && mouseX < area.left() + CATEGORY_TAB_WIDTH + CATEGORY_SCROLLBAR_WIDTH + 2 &&
               mouseY >= area.top() && mouseY < area.bottom();
    }

    public void setSearchText(String text) {
        this.searchText = text.toLowerCase().trim();
        this.scrollOffset = 0;
        buildFilteredCommands();
        rebuildButtons();
    }

    public void setOnCategoryChanged(Runnable before, Runnable after) {
        this.onBeforeCategoryChanged = before;
        this.onAfterCategoryChanged = after;
    }

    protected void notifyCategoryChange(Runnable changeAction) {
        if (onBeforeCategoryChanged != null) onBeforeCategoryChanged.run();
        changeAction.run();
        if (onAfterCategoryChanged != null) onAfterCategoryChanged.run();
    }

    protected int getCommandAreaLeft() {
        return area.left() + CATEGORY_TAB_WIDTH + CATEGORY_SCROLLBAR_WIDTH + 8;
    }

    protected int getCommandAreaWidth() {
        return area.width() - CATEGORY_TAB_WIDTH - CATEGORY_SCROLLBAR_WIDTH - 8;
    }

    protected void rebuildButtons() {
        commandButtons.clear();
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
            commandButtons.add(buildCommandButton(index, btnX + 2, btnY, btnWidth, ITEM_HEIGHT - 2));
        }
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
        if (area == null || getFilteredCommandCount() == 0) return 0;
        int visibleRows = area.height() / ITEM_HEIGHT;
        int totalRows = (getFilteredCommandCount() + COLUMNS - 1) / COLUMNS;
        return Math.max(0, totalRows - visibleRows);
    }

    public List<Button> getButtons() {
        return commandButtons;
    }

    public List<Button> getCategoryButtons() {
        return categoryButtons;
    }

    public ScreenRectangle getArea() {
        return area;
    }

    public void renderSeparator(GuiGraphics guiGraphics) {
        if (area == null) return;
        int separatorX = area.left() + CATEGORY_TAB_WIDTH + CATEGORY_SCROLLBAR_WIDTH + 4;
        guiGraphics.fill(separatorX, area.top(), separatorX + 1, area.bottom(), 0xFF555555);
    }

    public void renderCategoryScrollbar(GuiGraphics guiGraphics) {
        if (area == null) return;

        int maxScroll = getMaxCategoryScroll();
        int scrollbarX = area.left() + CATEGORY_TAB_WIDTH + 2;
        int scrollbarTop = area.top();
        int scrollbarHeight = area.height();

        guiGraphics.fill(scrollbarX, scrollbarTop, scrollbarX + CATEGORY_SCROLLBAR_WIDTH, scrollbarTop + scrollbarHeight, 0xFF000000);

        if (maxScroll <= 0) {
            guiGraphics.fill(scrollbarX, scrollbarTop, scrollbarX + CATEGORY_SCROLLBAR_WIDTH, scrollbarTop + scrollbarHeight, 0xFF555555);
            return;
        }

        int thumbHeight = Math.max(10, scrollbarHeight * getVisibleCategoryCount() / allCategoryButtons.size());
        int thumbY = scrollbarTop + (scrollbarHeight - thumbHeight) * categoryScrollOffset / maxScroll;

        guiGraphics.fill(scrollbarX, thumbY, scrollbarX + CATEGORY_SCROLLBAR_WIDTH, thumbY + thumbHeight, 0xFF808080);
        guiGraphics.fill(scrollbarX, thumbY, scrollbarX + CATEGORY_SCROLLBAR_WIDTH - 1, thumbY + thumbHeight - 1, 0xFFC0C0C0);
    }
}
