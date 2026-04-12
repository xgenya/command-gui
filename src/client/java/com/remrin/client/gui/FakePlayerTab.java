package com.remrin.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FakePlayerTab implements Tab {
	private static final int PLAYER_ITEM_WIDTH = 140;
	private static final int PLAYER_ITEM_HEIGHT = 20;
	private static final int ITEM_GAP = 2;
	private static final int FACE_SIZE = 16;
	private static final int ACTION_BUTTON_WIDTH = 80;
	private static final int ACTION_BUTTON_HEIGHT = 20;
	private static final int SEPARATOR_WIDTH = 1;

	private final CommandGUIScreen parent;
	private final List<Button> playerButtons = new ArrayList<>();
	private final List<Button> actionButtons = new ArrayList<>();
	private final List<Button> batchButtons = new ArrayList<>();
	private final List<String> displayList = new ArrayList<>();
	
	private ScreenRectangle area;
	private int scrollOffset = 0;
	private String searchText = "";
	private String selectedPlayer = null;
	private int separatorX = 0;
	
	private Runnable onBeforeRebuild;
	private Runnable onAfterRebuild;

	private static final String[] ACTIONS = {
		"stop", "kill", "attack continuous", "attack once", "use continuous", "use once",
		"jump continuous", "sneak", "unsneak", "sprint", "unsprint", "drop", "dropStack"
	};
	private static final String[] ACTION_KEYS = {
		"screen.command-gui.fakeplayer.action.stop",
		"screen.command-gui.fakeplayer.action.kill",
		"screen.command-gui.fakeplayer.action.attack",
		"screen.command-gui.fakeplayer.action.attack_once",
		"screen.command-gui.fakeplayer.action.use",
		"screen.command-gui.fakeplayer.action.use_once",
		"screen.command-gui.fakeplayer.action.jump",
		"screen.command-gui.fakeplayer.action.sneak",
		"screen.command-gui.fakeplayer.action.unsneak",
		"screen.command-gui.fakeplayer.action.sprint",
		"screen.command-gui.fakeplayer.action.unsprint",
		"screen.command-gui.fakeplayer.action.drop",
		"screen.command-gui.fakeplayer.action.dropstack"
	};

	public FakePlayerTab(CommandGUIScreen parent) {
		this.parent = parent;
	}

	@Override
	public Component getTabTitle() {
		return Component.translatable("screen.command-gui.tab.fakeplayer");
	}

	@Override
	public Component getTabExtraNarration() {
		return Component.empty();
	}

	@Override
	public void visitChildren(Consumer consumer) {
		playerButtons.forEach(consumer);
		actionButtons.forEach(consumer);
		batchButtons.forEach(consumer);
	}

	@Override
	public void doLayout(ScreenRectangle rectangle) {
		this.area = rectangle;
		this.scrollOffset = 0;
		refresh();
	}
	
	public void refresh() {
		playerButtons.clear();
		actionButtons.clear();
		batchButtons.clear();
		displayList.clear();
		
		for (TimedTaskManager.TimedTask task : TimedTaskManager.getPendingSpawnTasks()) {
			if (searchText.isEmpty() || task.playerName.toLowerCase().contains(searchText.toLowerCase())) {
				displayList.add(task.playerName);
			}
		}
		
		Minecraft mc = Minecraft.getInstance();
		if (mc.getConnection() != null) {
			for (PlayerInfo info : mc.getConnection().getListedOnlinePlayers()) {
				String name = info.getProfile().name();
				if (isFakePlayer(name) && !displayList.contains(name)) {
					if (searchText.isEmpty() || name.toLowerCase().contains(searchText.toLowerCase())) {
						displayList.add(name);
					}
				}
			}
		}
		
		if (selectedPlayer != null && !displayList.contains(selectedPlayer)) {
			selectedPlayer = null;
		}
		
		if (area == null) return;
		
		buildLayoutConstants();
		rebuildPlayerButtons();
		rebuildActionButtons();
	}

	private int playerListX;

	private void buildLayoutConstants() {
		int leftWidth = PLAYER_ITEM_WIDTH;
		int actionCols = 2;
		int rightWidth = actionCols * ACTION_BUTTON_WIDTH + (actionCols - 1) * ITEM_GAP;
		int totalWidth = leftWidth + SEPARATOR_WIDTH + rightWidth + 40;
		int baseX = area.left() + (area.width() - totalWidth) / 2;
		playerListX = baseX;
		separatorX = baseX + leftWidth + 20;
	}

	private void rebuildPlayerButtons() {
		playerButtons.clear();
		if (area == null) return;

		int startY = area.top();
		int visibleRows = area.height() / (PLAYER_ITEM_HEIGHT + ITEM_GAP);
		int startIndex = scrollOffset;
		int endIndex = Math.min(startIndex + visibleRows, displayList.size());

		for (int i = startIndex; i < endIndex; i++) {
			String playerName = displayList.get(i);
			int y = startY + (i - scrollOffset) * (PLAYER_ITEM_HEIGHT + ITEM_GAP);
			Button btn = Button.builder(
					Component.literal("    " + playerName),
					b -> selectPlayer(playerName)
			).bounds(playerListX, y, PLAYER_ITEM_WIDTH, PLAYER_ITEM_HEIGHT).build();
			playerButtons.add(btn);
		}
	}
	
	private boolean isPendingSpawn(String name) {
		return TimedTaskManager.getTask(name) != null && 
			   TimedTaskManager.getTask(name).type == TimedTaskManager.TaskType.SPAWN;
	}
	
	private boolean isOnlineFakePlayer(String name) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.getConnection() != null) {
			for (PlayerInfo info : mc.getConnection().getListedOnlinePlayers()) {
				if (info.getProfile().name().equals(name) && isFakePlayer(name)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private void selectPlayer(String playerName) {
		if (playerName.equals(selectedPlayer)) {
			selectedPlayer = null;
		} else {
			selectedPlayer = playerName;
		}
		fireBeforeRebuild();
		rebuildActionButtons();
		fireAfterRebuild();
	}
	
	public void setOnRebuild(Runnable before, Runnable after) {
		this.onBeforeRebuild = before;
		this.onAfterRebuild = after;
	}

	private void fireBeforeRebuild() {
		if (onBeforeRebuild != null) onBeforeRebuild.run();
	}

	private void fireAfterRebuild() {
		if (onAfterRebuild != null) onAfterRebuild.run();
	}
	
	private void rebuildActionButtons() {
		actionButtons.clear();
		batchButtons.clear();
		
		if (area == null) return;
		
		int rightX = separatorX + 20;
		int startY = area.top();
		int actionCols = 2;
		
		if (selectedPlayer == null) {
			Button batchSpawnBtn = Button.builder(
					Component.translatable("screen.command-gui.fakeplayer.batch.title"),
					b -> openBatchSpawnScreen()
			).bounds(rightX, startY, ACTION_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT).build();
			batchButtons.add(batchSpawnBtn);
			
			Button killAllBtn = Button.builder(
					Component.translatable("screen.command-gui.fakeplayer.killall"),
					b -> killAllFakePlayers()
			).bounds(rightX + ACTION_BUTTON_WIDTH + ITEM_GAP, startY, ACTION_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT).build();
			batchButtons.add(killAllBtn);
			
			Button timedSpawnBtn = Button.builder(
					Component.translatable("screen.command-gui.fakeplayer.timed.spawn.short"),
					b -> openTimedSpawnScreen()
			).bounds(rightX, startY + ACTION_BUTTON_HEIGHT + ITEM_GAP, ACTION_BUTTON_WIDTH * 2 + ITEM_GAP, ACTION_BUTTON_HEIGHT).build();
			batchButtons.add(timedSpawnBtn);
			
			return;
		}
		
		boolean isPending = isPendingSpawn(selectedPlayer);
		
		if (isPending) {
			Button cancelBtn = Button.builder(
					Component.translatable("screen.command-gui.fakeplayer.timed.cancel"),
					b -> {
						TimedTaskManager.removeTask(selectedPlayer);
						selectedPlayer = null;
						refresh();
					}
			).bounds(rightX, startY, ACTION_BUTTON_WIDTH * 2 + ITEM_GAP, ACTION_BUTTON_HEIGHT).build();
			actionButtons.add(cancelBtn);
			return;
		}
		
		TimedTaskManager.TimedTask killTask = TimedTaskManager.getTask(selectedPlayer);
		boolean hasKillTask = killTask != null && killTask.type == TimedTaskManager.TaskType.KILL;
		
		int idx = 0;
		for (int i = 0; i < ACTIONS.length; i++) {
			if (i == 1 && hasKillTask) continue;
			
			int row = idx / actionCols;
			int col = idx % actionCols;
			
			int x = rightX + col * (ACTION_BUTTON_WIDTH + ITEM_GAP);
			int y = startY + row * (ACTION_BUTTON_HEIGHT + ITEM_GAP);
			
			String action = ACTIONS[i];
			String player = selectedPlayer;
			
			Button btn = Button.builder(
					Component.translatable(ACTION_KEYS[i]),
					b -> executeAction(player, action)
			).bounds(x, y, ACTION_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT).build();
			
			actionButtons.add(btn);
			idx++;
		}
		
		int timedKillRow = (idx + 1) / actionCols;
		int timedKillY = startY + timedKillRow * (ACTION_BUTTON_HEIGHT + ITEM_GAP) + 10;
		
		if (hasKillTask) {
			Button cancelKillBtn = Button.builder(
					Component.translatable("screen.command-gui.fakeplayer.timed.cancel"),
					b -> {
						TimedTaskManager.removeTask(selectedPlayer);
						fireBeforeRebuild();
						rebuildActionButtons();
						fireAfterRebuild();
					}
			).bounds(rightX, timedKillY, ACTION_BUTTON_WIDTH * 2 + ITEM_GAP, ACTION_BUTTON_HEIGHT).build();
			actionButtons.add(cancelKillBtn);
		} else {
			Button timedKillBtn = Button.builder(
					Component.translatable("screen.command-gui.fakeplayer.timed.kill.short"),
					b -> openTimedKillScreen(selectedPlayer)
			).bounds(rightX, timedKillY, ACTION_BUTTON_WIDTH * 2 + ITEM_GAP, ACTION_BUTTON_HEIGHT).build();
			actionButtons.add(timedKillBtn);
		}
	}
	
	private void openBatchSpawnScreen() {
		Minecraft mc = Minecraft.getInstance();
		mc.setScreen(new BatchSpawnScreen(parent));
	}
	
	private void openTimedSpawnScreen() {
		Minecraft mc = Minecraft.getInstance();
		mc.setScreen(new TimedSpawnSetupScreen(parent));
	}
	
	private void openTimedKillScreen(String playerName) {
		Minecraft mc = Minecraft.getInstance();
		mc.setScreen(new TimedKillSetupScreen(parent, playerName));
	}
	
	private void killAllFakePlayers() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		
		for (String playerName : displayList) {
			if (isOnlineFakePlayer(playerName)) {
				mc.player.connection.sendCommand("player " + playerName + " kill");
			}
		}
	}
	
	private void executeAction(String player, String action) {
		String command = "/player " + player + " " + action;
		executeCommand(command);
		
		if (action.equals("kill")) {
			TimedTaskManager.removeTask(player);
			selectedPlayer = null;
			refresh();
		}
	}
	
	public void render(GuiGraphics guiGraphics) {
		if (area == null) return;
		
		if (!displayList.isEmpty()) {
			guiGraphics.fill(separatorX, area.top(), separatorX + SEPARATOR_WIDTH, area.bottom(), 0xFF555555);
		}
	}
	
	public void renderScrollbar(GuiGraphics guiGraphics) {
		if (area == null || displayList.isEmpty()) return;
		
		int maxScroll = getMaxScroll();
		if (maxScroll <= 0) return;
		
		int scrollbarX = separatorX - 8;
		int scrollbarTop = area.top();
		int scrollbarHeight = area.height();
		int scrollbarWidth = 4;
		
		guiGraphics.fill(scrollbarX, scrollbarTop, scrollbarX + scrollbarWidth, scrollbarTop + scrollbarHeight, 0xFF000000);
		
		int thumbHeight = Math.max(15, scrollbarHeight * scrollbarHeight / (scrollbarHeight + maxScroll * (PLAYER_ITEM_HEIGHT + ITEM_GAP)));
		int thumbY = scrollbarTop + (scrollbarHeight - thumbHeight) * scrollOffset / maxScroll;
		
		guiGraphics.fill(scrollbarX, thumbY, scrollbarX + scrollbarWidth, thumbY + thumbHeight, 0xFF808080);
		guiGraphics.fill(scrollbarX, thumbY, scrollbarX + scrollbarWidth - 1, thumbY + thumbHeight - 1, 0xFFC0C0C0);
	}
	
	public void renderFaces(GuiGraphics guiGraphics) {
		if (area == null) return;
		
		Minecraft mc = Minecraft.getInstance();
		if (mc.getConnection() == null) return;
		
		int startY = area.top();
		
		for (int i = 0; i < displayList.size(); i++) {
			String playerName = displayList.get(i);
			int y = startY + (i - scrollOffset) * (PLAYER_ITEM_HEIGHT + ITEM_GAP);
			
			if (y < area.top() - PLAYER_ITEM_HEIGHT || y > area.bottom()) continue;
			
			boolean isPending = isPendingSpawn(playerName);
			TimedTaskManager.TimedTask task = TimedTaskManager.getTask(playerName);
			
			if (!isPending) {
				PlayerInfo playerInfo = getPlayerInfo(playerName);
				if (playerInfo != null) {
					PlayerSkin skin = playerInfo.getSkin();
					int faceX = playerListX + 2;
					int faceY = y + (PLAYER_ITEM_HEIGHT - FACE_SIZE) / 2;
					PlayerFaceRenderer.draw(guiGraphics, skin, faceX, faceY, FACE_SIZE);
				}
			} else {
				guiGraphics.drawString(mc.font, "+", playerListX + 6, y + 6, 0xFF55FF55);
			}
			
			if (task != null) {
				String timeStr = formatTime(task.getRemainingSeconds());
				int timeColor = task.type == TimedTaskManager.TaskType.SPAWN ? 0xFF55FF55 : 0xFFFF5555;
				int timeWidth = mc.font.width(timeStr);
				guiGraphics.drawString(mc.font, timeStr, playerListX + PLAYER_ITEM_WIDTH - timeWidth - 4, y + 6, timeColor);
			}
			
			if (playerName.equals(selectedPlayer)) {
				guiGraphics.fill(playerListX, y, playerListX + PLAYER_ITEM_WIDTH, y + 1, 0xFF55FF55);
				guiGraphics.fill(playerListX, y + PLAYER_ITEM_HEIGHT - 1, playerListX + PLAYER_ITEM_WIDTH, y + PLAYER_ITEM_HEIGHT, 0xFF55FF55);
				guiGraphics.fill(playerListX, y, playerListX + 1, y + PLAYER_ITEM_HEIGHT, 0xFF55FF55);
				guiGraphics.fill(playerListX + PLAYER_ITEM_WIDTH - 1, y, playerListX + PLAYER_ITEM_WIDTH, y + PLAYER_ITEM_HEIGHT, 0xFF55FF55);
			}
		}
	}
	
	private String formatTime(int seconds) {
		if (seconds >= 3600) {
			int h = seconds / 3600;
			int m = (seconds % 3600) / 60;
			int s = seconds % 60;
			return String.format("%d:%02d:%02d", h, m, s);
		} else if (seconds >= 60) {
			int m = seconds / 60;
			int s = seconds % 60;
			return String.format("%d:%02d", m, s);
		} else {
			return seconds + "s";
		}
	}
	
	private PlayerInfo getPlayerInfo(String name) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.getConnection() != null) {
			for (PlayerInfo info : mc.getConnection().getListedOnlinePlayers()) {
				if (info.getProfile().name().equals(name)) {
					return info;
				}
			}
		}
		return null;
	}
	
	private boolean isFakePlayer(String name) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null && name.equals(mc.player.getName().getString())) {
			return false;
		}
		
		if (mc.getConnection() != null) {
			for (PlayerInfo info : mc.getConnection().getListedOnlinePlayers()) {
				if (info.getProfile().name().equals(name)) {
					int ping = info.getLatency();
					if (ping == 0) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private void executeCommand(String command) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null) {
			if (command.startsWith("/")) {
				mc.player.connection.sendCommand(command.substring(1));
			} else {
				mc.player.connection.sendCommand(command);
			}
		}
		if (!CommandGUIScreen.shouldKeepOpen()) {
			mc.setScreen(null);
		}
	}
	
	public void scroll(double amount) {
		int maxScroll = getMaxScroll();
		int newOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) amount));
		if (newOffset == scrollOffset) return;
		scrollOffset = newOffset;
		fireBeforeRebuild();
		rebuildPlayerButtons();
		fireAfterRebuild();
	}
	
	public int getScrollOffset() {
		return scrollOffset;
	}
	
	public int getMaxScroll() {
		int rows = displayList.size();
		int visibleRows = area != null ? area.height() / (PLAYER_ITEM_HEIGHT + ITEM_GAP) : 1;
		return Math.max(0, rows - visibleRows);
	}
	
	private void updateButtonPositions() {
		rebuildPlayerButtons();
	}

	public void setSearchText(String text) {
		this.searchText = text;
		this.scrollOffset = 0;
		fireBeforeRebuild();
		refresh();
		fireAfterRebuild();
	}
	
	public List<Button> getButtons() {
		List<Button> all = new ArrayList<>();
		all.addAll(playerButtons);
		all.addAll(actionButtons);
		all.addAll(batchButtons);
		return all;
	}
	
	public List<Button> getPlayerButtons() {
		return playerButtons;
	}
	
	public List<Button> getActionButtons() {
		return actionButtons;
	}
	
	public List<Button> getBatchButtons() {
		return batchButtons;
	}
	
	public ScreenRectangle getArea() {
		return area;
	}
	
	public boolean isEmpty() {
		return displayList.isEmpty();
	}
	
	public String getSelectedPlayer() {
		return selectedPlayer;
	}
	
	public void clearSelection() {
		selectedPlayer = null;
		actionButtons.clear();
		batchButtons.clear();
	}
}
