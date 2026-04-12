package com.remrin.client.gui;

import com.remrin.client.config.CommandConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddFakePlayerCommandScreen extends BaseParentedScreen<CommandGUIScreen> {
private static final int MARGIN = 8;
private static final int INNER_PAD = 6;
private static final int FIELD_WIDTH = 150;
private static final int FIELD_HEIGHT = 16;
private static final int ROW_GAP = 20;
private static final int COL_GAP = 16;
private static final int COORD_GAP = 8;
private static final int ROT_GAP = 28;
private static final int XYZ_FIELD_WIDTH = (FIELD_WIDTH - 2 * COORD_GAP) / 3; // 44
private static final int YAW_PITCH_FIELD_WIDTH = (FIELD_WIDTH - ROT_GAP) / 2;  // 61
private static final int ACTION_BTN_HEIGHT = 14;
private static final int ACTION_BTN_GAP = 2;
private static final int ACTIONS_PER_ROW = 4;
private static final int ACTION_BTN_WIDTH = (FIELD_WIDTH - (ACTIONS_PER_ROW - 1) * ACTION_BTN_GAP) / ACTIONS_PER_ROW; // 36
private static final int LABEL_COLOR = 0xFFAAAAAA;
private static final int BORDER_COLOR = 0xFF555555;
private static final int SELECTED_OVERLAY_COLOR = 0x6600CC00;
private static final int MIN_LABEL_SPACE = 60; // minimum pixels left of left column for side labels

private static final String[] DIMENSIONS = {
"minecraft:overworld",
"minecraft:the_nether",
"minecraft:the_end"
};
private static final String[] DIMENSION_NAMES = {
"screen.command-gui.fakeplayer.dim.overworld",
"screen.command-gui.fakeplayer.dim.nether",
"screen.command-gui.fakeplayer.dim.end"
};

private static final String[] GAMEMODES = {
"survival", "creative", "adventure", "spectator"
};
private static final String[] GAMEMODE_NAMES = {
"screen.command-gui.vanilla.gamemode.survival",
"screen.command-gui.vanilla.gamemode.creative",
"screen.command-gui.vanilla.gamemode.adventure",
"screen.command-gui.vanilla.gamemode.spectator"
};

private static final String[] ACTIONS = {
"attack continuous", "attack once", "use continuous", "use once",
"jump continuous", "sneak", "sprint", "drop", "dropStack", "swapHands"
};
private static final String[] ACTION_NAMES = {
"screen.command-gui.carpet.player.attack",
"screen.command-gui.carpet.player.attack_once",
"screen.command-gui.carpet.player.use",
"screen.command-gui.carpet.player.use_once",
"screen.command-gui.carpet.player.jump",
"screen.command-gui.carpet.player.sneak",
"screen.command-gui.carpet.player.sprint",
"screen.command-gui.carpet.player.drop",
"screen.command-gui.carpet.player.dropstack",
"screen.command-gui.carpet.player.swaphands"
};

private final String initialCategoryId;
private final String editingName;
private final CommandConfig.CommandEntry editingEntry;

// Fields
private EditBox nameField;
private EditBox descriptionField;
private EditBox fakePlayerNameField;
private Button fillCurrentPosButton;
private EditBox xField, yField, zField;
private EditBox yawField, pitchField;
private Button dimensionButton;
private Button gamemodeButton;
private final List<Button> actionButtons = new ArrayList<>();
private final List<EditBox> configFields = new ArrayList<>();
private final List<Button> configRemoveButtons = new ArrayList<>();
private Button addConfigButton;
private Button saveButton;
private Button cancelButton;

// State
private int dimensionIndex = 0;
private int gamemodeIndex = 0;
private final Set<Integer> selectedActions = new HashSet<>();
private final List<String> configCommands = new ArrayList<>();
private int lastSaveBtnY = -1;

// Layout (computed in init)
private int leftColX;
private int rightColX;
private int contentStartY;

public AddFakePlayerCommandScreen(CommandGUIScreen parent, String initialCategoryId) {
this(parent, initialCategoryId, null, null);
}

public AddFakePlayerCommandScreen(CommandGUIScreen parent, String initialCategoryId,
String editingName, CommandConfig.CommandEntry editingEntry) {
super(Component.translatable("screen.command-gui.add_fakeplayer_title"), parent);
this.initialCategoryId = initialCategoryId;
this.editingName = editingName;
this.editingEntry = editingEntry;
}

@Override
protected void init() {
super.init();

// Compute layout: two columns centered on screen
int contentW = FIELD_WIDTH * 2 + COL_GAP;
leftColX = Math.max(MARGIN + INNER_PAD + MIN_LABEL_SPACE, this.width / 2 - contentW / 2);
rightColX = leftColX + FIELD_WIDTH + COL_GAP;
contentStartY = MARGIN + INNER_PAD + 18;

int leftY = contentStartY;

// === Left column: spawn settings ===

// Name
nameField = new EditBox(this.font, leftColX, leftY, FIELD_WIDTH, FIELD_HEIGHT,
Component.translatable("screen.command-gui.name"));
nameField.setMaxLength(50);
nameField.setHint(Component.translatable("screen.command-gui.name_hint"));
this.addRenderableWidget(nameField);

// Description
leftY += ROW_GAP;
descriptionField = new EditBox(this.font, leftColX, leftY, FIELD_WIDTH, FIELD_HEIGHT,
Component.translatable("screen.command-gui.description"));
descriptionField.setMaxLength(100);
descriptionField.setHint(Component.translatable("screen.command-gui.description_hint"));
this.addRenderableWidget(descriptionField);

// Fake Player Name
leftY += ROW_GAP;
fakePlayerNameField = new EditBox(this.font, leftColX, leftY, FIELD_WIDTH, FIELD_HEIGHT,
Component.translatable("screen.command-gui.fakeplayer.playername"));
fakePlayerNameField.setMaxLength(20);
fakePlayerNameField.setValue("Bot_1");
fakePlayerNameField.setHint(Component.translatable("screen.command-gui.fakeplayer.playername_hint"));
this.addRenderableWidget(fakePlayerNameField);

// Fill current position button
leftY += ROW_GAP + 4;
fillCurrentPosButton = Button.builder(
Component.translatable("screen.command-gui.fakeplayer.pos.fill_current"),
btn -> fillCurrentPosition()
).bounds(leftColX, leftY, FIELD_WIDTH, FIELD_HEIGHT).build();
this.addRenderableWidget(fillCurrentPosButton);

// XYZ row — three equal-width fields
leftY += ROW_GAP;
xField = new EditBox(this.font, leftColX, leftY, XYZ_FIELD_WIDTH, FIELD_HEIGHT, Component.literal("X"));
xField.setMaxLength(12);
xField.setFilter(s -> s.isEmpty() || s.matches("-?\\d*\\.?\\d*"));
this.addRenderableWidget(xField);

yField = new EditBox(this.font, leftColX + XYZ_FIELD_WIDTH + COORD_GAP, leftY, XYZ_FIELD_WIDTH, FIELD_HEIGHT, Component.literal("Y"));
yField.setMaxLength(12);
yField.setFilter(s -> s.isEmpty() || s.matches("-?\\d*\\.?\\d*"));
this.addRenderableWidget(yField);

zField = new EditBox(this.font, leftColX + (XYZ_FIELD_WIDTH + COORD_GAP) * 2, leftY, XYZ_FIELD_WIDTH, FIELD_HEIGHT, Component.literal("Z"));
zField.setMaxLength(12);
zField.setFilter(s -> s.isEmpty() || s.matches("-?\\d*\\.?\\d*"));
this.addRenderableWidget(zField);

// Yaw / Pitch row — wider gap between fields to accommodate labels
leftY += ROW_GAP;
yawField = new EditBox(this.font, leftColX, leftY, YAW_PITCH_FIELD_WIDTH, FIELD_HEIGHT, Component.literal("Yaw"));
yawField.setMaxLength(10);
yawField.setFilter(s -> s.isEmpty() || s.matches("-?\\d*\\.?\\d*"));
this.addRenderableWidget(yawField);

pitchField = new EditBox(this.font, leftColX + YAW_PITCH_FIELD_WIDTH + ROT_GAP, leftY, YAW_PITCH_FIELD_WIDTH, FIELD_HEIGHT, Component.literal("Pitch"));
pitchField.setMaxLength(10);
pitchField.setFilter(s -> s.isEmpty() || s.matches("-?\\d*\\.?\\d*"));
this.addRenderableWidget(pitchField);

// Dimension button
leftY += ROW_GAP;
dimensionButton = Button.builder(
getDimensionLabel(),
btn -> {
dimensionIndex = (dimensionIndex + 1) % DIMENSIONS.length;
dimensionButton.setMessage(getDimensionLabel());
}
).bounds(leftColX, leftY, FIELD_WIDTH, FIELD_HEIGHT).build();
this.addRenderableWidget(dimensionButton);

// Gamemode button
leftY += ROW_GAP;
gamemodeButton = Button.builder(
getGamemodeLabel(),
btn -> {
gamemodeIndex = (gamemodeIndex + 1) % GAMEMODES.length;
gamemodeButton.setMessage(getGamemodeLabel());
}
).bounds(leftColX, leftY, FIELD_WIDTH, FIELD_HEIGHT).build();
this.addRenderableWidget(gamemodeButton);

// === Right column: actions then config ===

// Leave space for "Actions" section label
int rightY = contentStartY + 12;
buildActionButtons(rightColX, rightY);

int actionRows = (ACTIONS.length + ACTIONS_PER_ROW - 1) / ACTIONS_PER_ROW;
rightY += actionRows * (ACTION_BTN_HEIGHT + ACTION_BTN_GAP) + 8;

// Leave space for "Config" section label, then build config widgets
rightY += 12;
rebuildConfigWidgets(rightY);

// Fill coord fields with current player position (defaults)
fillCurrentPosition();

// If editing, restore values
if (editingEntry != null && editingName != null) {
nameField.setValue(editingName);
descriptionField.setValue(editingEntry.description != null ? editingEntry.description : "");
parseExistingCommands(editingEntry.getCommands());
}

updateActionButtonColors();
}

private void buildActionButtons(int startX, int startY) {
actionButtons.clear();
for (int i = 0; i < ACTIONS.length; i++) {
int row = i / ACTIONS_PER_ROW;
int col = i % ACTIONS_PER_ROW;
int btnX = startX + col * (ACTION_BTN_WIDTH + ACTION_BTN_GAP);
int btnY = startY + row * (ACTION_BTN_HEIGHT + ACTION_BTN_GAP);
final int actionIdx = i;
Button actionBtn = Button.builder(
Component.translatable(ACTION_NAMES[i]),
btn -> toggleAction(actionIdx)
).bounds(btnX, btnY, ACTION_BTN_WIDTH, ACTION_BTN_HEIGHT).build();
actionButtons.add(actionBtn);
this.addRenderableWidget(actionBtn);
}
}

private int getConfigStartY() {
// Config starts in the right column, after: content start + actions label (12) + action buttons + gap (8) + config label (12)
int actionRows = (ACTIONS.length + ACTIONS_PER_ROW - 1) / ACTIONS_PER_ROW;
return contentStartY + 12 + actionRows * (ACTION_BTN_HEIGHT + ACTION_BTN_GAP) + 8 + 12;
}

private void rebuildConfigWidgets(int startY) {
for (EditBox field : configFields) {
this.removeWidget(field);
}
for (Button btn : configRemoveButtons) {
this.removeWidget(btn);
}
if (addConfigButton != null) {
this.removeWidget(addConfigButton);
}
configFields.clear();
configRemoveButtons.clear();

int x = rightColX;
int currentY = startY;

for (int i = 0; i < configCommands.size(); i++) {
final int idx = i;
EditBox configField = new EditBox(this.font, x, currentY, FIELD_WIDTH - 18, FIELD_HEIGHT,
Component.translatable("screen.command-gui.fakeplayer.config"));
configField.setMaxLength(256);
configField.setValue(configCommands.get(i));
configField.setHint(Component.translatable("screen.command-gui.fakeplayer.config_hint"));
configField.setResponder(text -> {
if (idx < configCommands.size()) {
configCommands.set(idx, text);
}
});
configFields.add(configField);
this.addRenderableWidget(configField);

Button removeBtn = Button.builder(
Component.translatable("screen.command-gui.remove_command"),
btn -> {
configCommands.remove(idx);
rebuildConfigWidgets(getConfigStartY());
}
).bounds(x + FIELD_WIDTH - 16, currentY, 16, FIELD_HEIGHT).build();
configRemoveButtons.add(removeBtn);
this.addRenderableWidget(removeBtn);

currentY += ROW_GAP;
}

addConfigButton = Button.builder(
Component.translatable("screen.command-gui.fakeplayer.add_config"),
btn -> {
configCommands.add("");
rebuildConfigWidgets(getConfigStartY());
}
).bounds(x, currentY, 80, 14).build();
this.addRenderableWidget(addConfigButton);
}

private void toggleAction(int actionIndex) {
if (selectedActions.contains(actionIndex)) {
selectedActions.remove(actionIndex);
} else {
selectedActions.add(actionIndex);
}
updateActionButtonColors();
}

private void updateActionButtonColors() {
// Keep all action buttons active so selections can be toggled on/off.
// Selected state is shown via a colored overlay drawn in render().
for (int i = 0; i < actionButtons.size(); i++) {
actionButtons.get(i).active = true;
}
}

private void fillCurrentPosition() {
Minecraft mc = Minecraft.getInstance();
if (mc.player != null) {
double posX = Math.round(mc.player.getX() * 10.0) / 10.0;
double posY = Math.round(mc.player.getY() * 10.0) / 10.0;
double posZ = Math.round(mc.player.getZ() * 10.0) / 10.0;
float yaw = Math.round(mc.player.getYRot() * 10.0f) / 10.0f;
float pitch = Math.round(mc.player.getXRot() * 10.0f) / 10.0f;

xField.setValue(String.format("%.1f", posX));
yField.setValue(String.format("%.1f", posY));
zField.setValue(String.format("%.1f", posZ));
yawField.setValue(String.format("%.1f", yaw));
pitchField.setValue(String.format("%.1f", pitch));

net.minecraft.resources.ResourceKey<Level> dim = mc.player.level().dimension();
if (dim.equals(Level.NETHER)) {
dimensionIndex = 1;
} else if (dim.equals(Level.END)) {
dimensionIndex = 2;
} else {
dimensionIndex = 0;
}
if (dimensionButton != null) {
dimensionButton.setMessage(getDimensionLabel());
}

if (mc.gameMode != null) {
String mode = mc.gameMode.getPlayerMode().getName();
for (int i = 0; i < GAMEMODES.length; i++) {
if (GAMEMODES[i].equals(mode)) {
gamemodeIndex = i;
if (gamemodeButton != null) {
gamemodeButton.setMessage(getGamemodeLabel());
}
break;
}
}
}
}
}

private void parseExistingCommands(List<String> commands) {
if (commands == null || commands.isEmpty()) return;

String spawnCmd = commands.get(0);
if (spawnCmd.startsWith("/player ") || spawnCmd.startsWith("player ")) {
String cmd = spawnCmd.startsWith("/") ? spawnCmd.substring(1) : spawnCmd;
String[] parts = cmd.split("\\s+");
if (parts.length >= 2) {
fakePlayerNameField.setValue(parts[1]);
}

// Parse AT coordinates: "at X Y Z"
Matcher atMatcher = Pattern.compile(
" at (-?\\d+\\.?\\d*) (-?\\d+\\.?\\d*) (-?\\d+\\.?\\d*)").matcher(cmd);
if (atMatcher.find()) {
xField.setValue(atMatcher.group(1));
yField.setValue(atMatcher.group(2));
zField.setValue(atMatcher.group(3));
}

// Parse FACING: "facing Yaw Pitch"
Matcher facingMatcher = Pattern.compile(
" facing (-?\\d+\\.?\\d*) (-?\\d+\\.?\\d*)").matcher(cmd);
if (facingMatcher.find()) {
yawField.setValue(facingMatcher.group(1));
pitchField.setValue(facingMatcher.group(2));
}

// Parse dimension and gamemode: "in <dim> in <mode>"
int lastInIdx = cmd.lastIndexOf(" in ");
if (lastInIdx >= 0) {
String possibleGamemode = cmd.substring(lastInIdx + 4).trim();
for (int i = 0; i < GAMEMODES.length; i++) {
if (GAMEMODES[i].equals(possibleGamemode)) {
gamemodeIndex = i;
if (gamemodeButton != null) gamemodeButton.setMessage(getGamemodeLabel());
break;
}
}
int firstInIdx = cmd.indexOf(" in ");
if (firstInIdx >= 0 && firstInIdx < lastInIdx) {
String possibleDimension = cmd.substring(firstInIdx + 4, lastInIdx).trim();
for (int i = 0; i < DIMENSIONS.length; i++) {
if (DIMENSIONS[i].equals(possibleDimension)) {
dimensionIndex = i;
if (dimensionButton != null) dimensionButton.setMessage(getDimensionLabel());
break;
}
}
}
}
}

selectedActions.clear();
configCommands.clear();
for (int cmdIdx = 1; cmdIdx < commands.size(); cmdIdx++) {
String actionCmd = commands.get(cmdIdx);
String cmd = actionCmd.startsWith("/") ? actionCmd.substring(1) : actionCmd;
boolean matched = false;
String[] parts = cmd.split("\\s+", 3);
if (parts.length >= 3 && parts[0].equals("player")) {
String actionPart = parts[2];
for (int i = 0; i < ACTIONS.length; i++) {
if (actionPart.startsWith(ACTIONS[i])) {
selectedActions.add(i);
matched = true;
break;
}
}
}
if (!matched) {
configCommands.add(actionCmd);
}
}

updateActionButtonColors();
rebuildConfigWidgets(getConfigStartY());
}

private Component getDimensionLabel() {
return Component.translatable("screen.command-gui.fakeplayer.dimension")
.append(": ")
.append(Component.translatable(DIMENSION_NAMES[dimensionIndex]));
}

private Component getGamemodeLabel() {
return Component.translatable("screen.command-gui.fakeplayer.gamemode")
.append(": ")
.append(Component.translatable(GAMEMODE_NAMES[gamemodeIndex]));
}

private List<String> buildCommands() {
List<String> commands = new ArrayList<>();
String fpName = fakePlayerNameField.getValue().trim();
if (fpName.isEmpty()) return commands;

StringBuilder spawnCmd = new StringBuilder("/player ").append(fpName).append(" spawn");

String x = xField.getValue().trim();
String y = yField.getValue().trim();
String z = zField.getValue().trim();
String yawStr = yawField.getValue().trim();
String pitchStr = pitchField.getValue().trim();

if (!x.isEmpty() && !y.isEmpty() && !z.isEmpty()) {
spawnCmd.append(" at ").append(x).append(" ").append(y).append(" ").append(z);
}
if (!yawStr.isEmpty() && !pitchStr.isEmpty()) {
spawnCmd.append(" facing ").append(yawStr).append(" ").append(pitchStr);
} else if (!yawStr.isEmpty()) {
spawnCmd.append(" facing ").append(yawStr).append(" 0");
}
spawnCmd.append(" in ").append(DIMENSIONS[dimensionIndex]);
spawnCmd.append(" in ").append(GAMEMODES[gamemodeIndex]);

commands.add(spawnCmd.toString());

// Action commands
List<Integer> sortedActions = new ArrayList<>(selectedActions);
sortedActions.sort(Integer::compareTo);
for (int actionIdx : sortedActions) {
commands.add("/player " + fpName + " " + ACTIONS[actionIdx]);
}

// Config commands
for (int i = 0; i < configCommands.size(); i++) {
String config;
if (i < configFields.size()) {
config = configFields.get(i).getValue().trim();
} else {
config = configCommands.get(i).trim();
}
if (!config.isEmpty()) {
commands.add(config);
}
}

return commands;
}

private void saveAndClose() {
String name = nameField.getValue().trim();
String description = descriptionField.getValue().trim();
List<String> commands = buildCommands();

if (name.isEmpty() || commands.isEmpty()) return;

String categoryId = initialCategoryId != null ? initialCategoryId : "default";

if (editingName != null) {
if (!name.equals(editingName)) {
String oldCategoryId = CommandConfig.findCommandCategory(editingName);
CommandConfig.removeCommand(editingName);
CommandConfig.addCommandMulti(oldCategoryId != null ? oldCategoryId : categoryId, name, commands, description);
} else {
CommandConfig.updateCommandMulti(name, commands, description);
}
} else {
CommandConfig.addCommandMulti(categoryId, name, commands, description);
}

parent.refresh();
this.minecraft.setScreen(parent);
}

@Override
public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
super.render(guiGraphics, mouseX, mouseY, partialTick);

// === Outer border ===
int bx = MARGIN, by = MARGIN, bw = this.width - 2 * MARGIN, bh = this.height - 2 * MARGIN;
guiGraphics.fill(bx, by, bx + bw, by + 1, BORDER_COLOR);
guiGraphics.fill(bx, by + bh - 1, bx + bw, by + bh, BORDER_COLOR);
guiGraphics.fill(bx, by, bx + 1, by + bh, BORDER_COLOR);
guiGraphics.fill(bx + bw - 1, by, bx + bw, by + bh, BORDER_COLOR);

// Title
guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, MARGIN + 6, 0xFFFFFFFF);

// === Left column labels ===
int labelX = leftColX - 4;
int currentY = contentStartY;

guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.name"),
labelX - this.font.width(Component.translatable("screen.command-gui.name")), currentY + 4, LABEL_COLOR);

currentY += ROW_GAP;
guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.description"),
labelX - this.font.width(Component.translatable("screen.command-gui.description")), currentY + 4, LABEL_COLOR);

currentY += ROW_GAP;
guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.fakeplayer.playername"),
labelX - this.font.width(Component.translatable("screen.command-gui.fakeplayer.playername")), currentY + 4, LABEL_COLOR);

currentY += ROW_GAP + 4;
guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.fakeplayer.spawn_at"),
labelX - this.font.width(Component.translatable("screen.command-gui.fakeplayer.spawn_at")), currentY + 4, LABEL_COLOR);

// XYZ labels: X to the left, Y and Z inside the gaps between fields
currentY += ROW_GAP;
guiGraphics.drawString(this.font, "X",
leftColX - 2 - this.font.width("X"), currentY + 4, 0xFFFF5555);
guiGraphics.drawString(this.font, "Y",
leftColX + XYZ_FIELD_WIDTH + 2, currentY + 4, 0xFF55FF55);
guiGraphics.drawString(this.font, "Z",
leftColX + (XYZ_FIELD_WIDTH + COORD_GAP) + XYZ_FIELD_WIDTH + 2, currentY + 4, 0xFF5555FF);

// Yaw/Pitch labels: Yaw to the left, Pitch inside the wider ROT_GAP
currentY += ROW_GAP;
guiGraphics.drawString(this.font, "Yaw",
labelX - this.font.width("Yaw"), currentY + 4, LABEL_COLOR);
guiGraphics.drawString(this.font, "Pitch",
leftColX + YAW_PITCH_FIELD_WIDTH + 2, currentY + 4, LABEL_COLOR);

// === Right column labels ===
// "Actions" header just above the action buttons
guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.fakeplayer.actions_label"),
rightColX, contentStartY + 2, LABEL_COLOR);

// "Config" header just above the config fields
int configStartY = getConfigStartY();
guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.fakeplayer.config_label"),
rightColX, configStartY - 10, LABEL_COLOR);

// === Selected action overlays ===
for (int i = 0; i < actionButtons.size(); i++) {
if (selectedActions.contains(i)) {
Button btn = actionButtons.get(i);
guiGraphics.fill(btn.getX(), btn.getY(),
btn.getX() + btn.getWidth(), btn.getY() + btn.getHeight(), SELECTED_OVERLAY_COLOR);
}
}

// === Command preview (spanning full content width) ===
List<String> commands = buildCommands();
int configEndY = configStartY + configCommands.size() * ROW_GAP + 20;

guiGraphics.drawString(this.font,
Component.translatable("screen.command-gui.fakeplayer.config_desc"),
rightColX, configEndY + 2, 0xFF888888);

int contentRight = rightColX + FIELD_WIDTH;
int previewLabelY = configEndY + 14;
guiGraphics.drawCenteredString(this.font,
Component.translatable("screen.command-gui.fakeplayer.command_preview"),
leftColX + (contentRight - leftColX) / 2, previewLabelY, 0xFF888888);

int previewY = previewLabelY + 10;
int previewMaxW = contentRight - leftColX - 4;
for (int i = 0; i < commands.size(); i++) {
String cmd = commands.get(i);
String display = this.font.plainSubstrByWidth(cmd, previewMaxW);
guiGraphics.drawString(this.font, display, leftColX + 2, previewY + i * 10, 0xFF55FF55);
}

// Save / Cancel buttons (positioned dynamically near bottom)
int saveBtnY = previewY + commands.size() * 10 + 4;
saveBtnY = Math.min(saveBtnY, this.height - MARGIN - INNER_PAD - 24);
renderSaveCancel(saveBtnY);
}

private void renderSaveCancel(int saveBtnY) {
if (saveBtnY != lastSaveBtnY) {
if (saveButton != null) this.removeWidget(saveButton);
if (cancelButton != null) this.removeWidget(cancelButton);

// Center save/cancel within the two-column content area
int contentCenterX = leftColX + (FIELD_WIDTH * 2 + COL_GAP) / 2;
saveButton = Button.builder(
Component.translatable("screen.command-gui.save"),
btn -> saveAndClose()
).bounds(contentCenterX - 102, saveBtnY, 100, 20).build();
this.addRenderableWidget(saveButton);

cancelButton = Button.builder(
Component.translatable("screen.command-gui.cancel"),
btn -> this.minecraft.setScreen(parent)
).bounds(contentCenterX + 2, saveBtnY, 100, 20).build();
this.addRenderableWidget(cancelButton);

lastSaveBtnY = saveBtnY;
}
}

@Override
public boolean keyPressed(KeyEvent keyEvent) {
int keyCode = keyEvent.key();
if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
this.minecraft.setScreen(parent);
return true;
}
if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
String name = nameField.getValue().trim();
String fpName = fakePlayerNameField.getValue().trim();
if (!name.isEmpty() && !fpName.isEmpty()) {
saveAndClose();
}
return true;
}
return super.keyPressed(keyEvent);
}
}
