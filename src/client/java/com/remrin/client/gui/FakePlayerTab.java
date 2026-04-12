package com.remrin.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.network.chat.Component;

import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FakePlayerTab implements Tab {
	private static final int PLAYER_ITEM_WIDTH = 148;
	private static final int PLAYER_ITEM_HEIGHT = 26;
	private static final int ITEM_GAP = 0;
	private static final int FACE_SIZE = 16;
	private static final int ACTION_BUTTON_WIDTH = 80;
	private static final int ACTION_BUTTON_HEIGHT = 20;
	private static final int SEPARATOR_WIDTH = 1;
	private static final int FACE_PAD_LEFT = 4;
	private static final int NAME_PAD_LEFT = 4;

	// Vanilla sprites
	private static final Identifier BUTTON_SPRITE = Identifier.withDefaultNamespace("widget/button");
	private static final Identifier BUTTON_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("widget/button_highlighted");
	private static final Identifier TELEPORT_TO_PLAYER_SPRITE = Identifier.withDefaultNamespace("spectator/teleport_to_player");
	private static final Identifier REMOVE_PLAYER_SPRITE = Identifier.withDefaultNamespace("player_list/remove_player");
	private static final Identifier CLOCK_SPRITE = Identifier.parse("command-gui:icon/clock");

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
		int visibleRows = area.height() / PLAYER_ITEM_HEIGHT;
		int startIndex = scrollOffset;
		// +1 to include any partially-visible last row
		int endIndex = Math.min(startIndex + visibleRows + 1, displayList.size());

		for (int i = startIndex; i < endIndex; i++) {
			String playerName = displayList.get(i);
			int y = startY + (i - scrollOffset) * PLAYER_ITEM_HEIGHT;
			// Transparent click target — all visuals rendered manually
			Button btn = Button.builder(
					Component.empty(),
					b -> selectPlayer(playerName)
			).bounds(playerListX, y, PLAYER_ITEM_WIDTH, PLAYER_ITEM_HEIGHT).build();
			playerButtons.add(btn);
		}
	}
	
	private boolean isPendingSpawn(String name) {
		TimedTaskManager.TimedTask task = TimedTaskManager.getTask(name);
		return task != null && task.type == TimedTaskManager.TaskType.SPAWN;
	}
	
	private boolean isOnlineFakePlayer(String name) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.getConnection() != null) {
			for (PlayerInfo info : mc.getConnection().getListedOnlinePlayers()) {
				if (info.getProfile().name().equals(name) && CommandHelper.isFakePlayer(name)) {
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
					Component.literal("+ ").withStyle(ChatFormatting.GREEN)
							.append(Component.translatable("screen.command-gui.fakeplayer.batch.title").withStyle(ChatFormatting.WHITE)),
					b -> openBatchSpawnScreen()
			).bounds(rightX, startY, ACTION_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT).build();
			batchButtons.add(batchSpawnBtn);

			Button killAllBtn = Button.builder(
					Component.literal("x ").withStyle(ChatFormatting.RED)
							.append(Component.translatable("screen.command-gui.fakeplayer.killall").withStyle(ChatFormatting.WHITE)),
					b -> {
						long handle = org.lwjgl.glfw.GLFW.glfwGetCurrentContext();
						if (org.lwjgl.glfw.GLFW.glfwGetKey(handle, org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS
								|| org.lwjgl.glfw.GLFW.glfwGetKey(handle, org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS) {
							killAllFakePlayers();
							fireBeforeRebuild();
							rebuildActionButtons();
							fireAfterRebuild();
						}
					}
			).bounds(rightX + ACTION_BUTTON_WIDTH + ITEM_GAP, startY, ACTION_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT).build();
			killAllBtn.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
					Component.translatable("screen.command-gui.fakeplayer.killall.shift_hint")));
			batchButtons.add(killAllBtn);
			Button timedSpawnBtn = Button.builder(
					Component.literal("+ ").withStyle(ChatFormatting.YELLOW)
							.append(Component.translatable("screen.command-gui.fakeplayer.timed.spawn.short").withStyle(ChatFormatting.WHITE)),
					b -> openTimedSpawnScreen()
			).bounds(rightX, startY + ACTION_BUTTON_HEIGHT + ITEM_GAP, ACTION_BUTTON_WIDTH * 2 + ITEM_GAP, ACTION_BUTTON_HEIGHT).build();
			batchButtons.add(timedSpawnBtn);

			return;
		}
		
		boolean isPending = isPendingSpawn(selectedPlayer);
		
		if (isPending) {
			Button cancelBtn = Button.builder(
					Component.literal("x ").withStyle(ChatFormatting.RED)
							.append(Component.translatable("screen.command-gui.fakeplayer.timed.cancel").withStyle(ChatFormatting.WHITE)),
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
					Component.literal("x ").withStyle(ChatFormatting.RED)
							.append(Component.translatable("screen.command-gui.fakeplayer.timed.cancel").withStyle(ChatFormatting.WHITE)),
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
					Component.literal("x ").withStyle(ChatFormatting.YELLOW)
							.append(Component.translatable("screen.command-gui.fakeplayer.timed.kill.short").withStyle(ChatFormatting.WHITE)),
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
	
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		if (area == null) return;

		if (displayList.isEmpty()) {
			Minecraft mc = Minecraft.getInstance();
			guiGraphics.drawCenteredString(mc.font,
					Component.translatable("screen.command-gui.fakeplayer.empty"),
					playerListX + PLAYER_ITEM_WIDTH / 2,
					area.top() + area.height() / 2 - 4,
					0xFFAAAAAA);
			return;
		}

		int startY = area.top();
		int visibleRows = area.height() / PLAYER_ITEM_HEIGHT;
		int endIndex = Math.min(scrollOffset + visibleRows + 1, displayList.size());

		for (int i = scrollOffset; i < endIndex; i++) {
			String name = displayList.get(i);
			int y = startY + (i - scrollOffset) * PLAYER_ITEM_HEIGHT;
			if (y >= area.bottom()) break;

			boolean isSelected = name.equals(selectedPlayer);
			boolean isHovered = !isSelected
					&& mouseX >= playerListX && mouseX < playerListX + PLAYER_ITEM_WIDTH
					&& mouseY >= y && mouseY < y + PLAYER_ITEM_HEIGHT;

			Identifier sprite = (isSelected || isHovered) ? BUTTON_HIGHLIGHTED_SPRITE : BUTTON_SPRITE;
			guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite,
					playerListX, y, PLAYER_ITEM_WIDTH, PLAYER_ITEM_HEIGHT);

			// Subtle green tint for selected item
			if (isSelected) {
				guiGraphics.fill(playerListX, y, playerListX + PLAYER_ITEM_WIDTH, y + PLAYER_ITEM_HEIGHT, 0x3300AA00);
			}
		}

		// Separator between list and actions
		guiGraphics.fill(separatorX, area.top(), separatorX + SEPARATOR_WIDTH, area.bottom(), 0xFF888888);
	}
	
	public void renderScrollbar(GuiGraphics guiGraphics) {
		if (area == null || displayList.isEmpty()) return;

		int maxScroll = getMaxScroll();
		if (maxScroll <= 0) return;

		int scrollbarX = separatorX - 6;
		int scrollbarTop = area.top();
		int scrollbarHeight = area.height();
		int scrollbarWidth = 3;

		guiGraphics.fill(scrollbarX, scrollbarTop, scrollbarX + scrollbarWidth, scrollbarTop + scrollbarHeight, 0xFF111111);

		int thumbHeight = Math.max(12, scrollbarHeight * scrollbarHeight / (scrollbarHeight + maxScroll * PLAYER_ITEM_HEIGHT));
		int thumbY = scrollbarTop + (scrollbarHeight - thumbHeight) * scrollOffset / maxScroll;

		guiGraphics.fill(scrollbarX, thumbY, scrollbarX + scrollbarWidth, thumbY + thumbHeight, 0xFF555555);
		guiGraphics.fill(scrollbarX, thumbY, scrollbarX + scrollbarWidth - 1, thumbY + thumbHeight - 1, 0xFF888888);
	}
	
	public void renderFaces(GuiGraphics guiGraphics) {
		if (area == null) return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.getConnection() == null) return;

		int startY = area.top();
		int faceX = playerListX + FACE_PAD_LEFT;
		int nameX = faceX + FACE_SIZE + NAME_PAD_LEFT;
		int faceVertOffset = (PLAYER_ITEM_HEIGHT - FACE_SIZE) / 2;
		int textVertOffset = (PLAYER_ITEM_HEIGHT - 9) / 2;

		for (int i = 0; i < displayList.size(); i++) {
			String playerName = displayList.get(i);
			int y = startY + (i - scrollOffset) * PLAYER_ITEM_HEIGHT;

			if (y + PLAYER_ITEM_HEIGHT <= area.top() || y >= area.bottom()) continue;

			boolean isPending = isPendingSpawn(playerName);
			boolean isSelected = playerName.equals(selectedPlayer);
			TimedTaskManager.TimedTask task = TimedTaskManager.getTask(playerName);

			int faceY = y + faceVertOffset;

			if (isPending) {
				// Pending spawn: use vanilla "teleport to player" sprite as icon
				guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
						TELEPORT_TO_PLAYER_SPRITE, faceX, faceY, FACE_SIZE, FACE_SIZE);
			} else {
				PlayerInfo playerInfo = getPlayerInfo(playerName);
				if (playerInfo != null) {
					PlayerSkin skin = playerInfo.getSkin();
					PlayerFaceRenderer.draw(guiGraphics, skin, faceX, faceY, FACE_SIZE);
				} else {
					// Offline: grey placeholder box
					guiGraphics.fill(faceX, faceY, faceX + FACE_SIZE, faceY + FACE_SIZE, 0xFF555555);
				}
				// Pending kill: vanilla "remove player" icon in top-right corner of face
				if (task != null && task.type == TimedTaskManager.TaskType.KILL) {
					// Semi-transparent red overlay over entire face
					guiGraphics.fill(faceX, faceY, faceX + FACE_SIZE, faceY + FACE_SIZE, 0xBBCC2222);
					// remove_player icon centered on face (scaled 2x)
					int iconSize = 10;
					guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
							REMOVE_PLAYER_SPRITE,
							faceX + (FACE_SIZE - iconSize) / 2, faceY + (FACE_SIZE - iconSize) / 2,
							iconSize, iconSize);
				}
			}

			int timerWidth = 0;
			if (task != null) {
				String timeStr = formatTime(task.getRemainingSeconds());
				int clockSize = 10;
				int clockTimerGap = 2;
				timerWidth = mc.font.width(timeStr) + clockSize + clockTimerGap + 6;
				int timeColor = task.type == TimedTaskManager.TaskType.SPAWN ? 0xFF55FF55 : 0xFFFF5555;
				int textX = playerListX + PLAYER_ITEM_WIDTH - mc.font.width(timeStr) - 4;
				int clockX = textX - clockSize - clockTimerGap;
				int clockY = y + (PLAYER_ITEM_HEIGHT - clockSize) / 2;
				guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, CLOCK_SPRITE, clockX, clockY, clockSize, clockSize);
				guiGraphics.drawString(mc.font, timeStr, textX, y + textVertOffset, timeColor);
			}

			int maxNameWidth = playerListX + PLAYER_ITEM_WIDTH - nameX - timerWidth - 4;
			String displayName = mc.font.plainSubstrByWidth(playerName, maxNameWidth);
			int nameColor = isPending ? 0xFF55FF55 : isSelected ? 0xFFFFFFFF : 0xFFDDDDDD;
			guiGraphics.drawString(mc.font, displayName, nameX, y + textVertOffset, nameColor);
		}
	}
	
	private String formatTime(int seconds) {
		return CommandHelper.formatTime(seconds);
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
		return CommandHelper.isFakePlayer(name);
	}
	
	private void executeCommand(String command) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null) {
			CommandHelper.sendCommand(command);
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
