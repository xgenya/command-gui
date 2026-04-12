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
private static final int FIELD_WIDTH = 170;
private static final int FIELD_HEIGHT = 16;
private static final int ROW_GAP = 20;
private static final int COORD_GAP = 4;
private static final int XYZ_FIELD_WIDTH = (FIELD_WIDTH - 2 * COORD_GAP) / 3; // 54
private static final int YAW_PITCH_FIELD_WIDTH = (FIELD_WIDTH - COORD_GAP) / 2; // 83
private static final int ACTION_BTN_WIDTH = 42;
private static final int ACTION_BTN_HEIGHT = 14;
private static final int ACTION_BTN_GAP = 2;
private static final int ACTIONS_PER_ROW = 4;
private static final int LABEL_COLOR = 0xFFAAAAAA;

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

int centerX = this.width / 2;
int leftX = centerX - FIELD_WIDTH / 2;
int currentY = 20;

// === Name ===
nameField = new EditBox(this.font, leftX, currentY, FIELD_WIDTH, FIELD_HEIGHT,
Component.translatable("screen.command-gui.name"));
nameField.setMaxLength(50);
nameField.setHint(Component.translatable("screen.command-gui.name_hint"));
this.addRenderableWidget(nameField);

// === Description ===
currentY += ROW_GAP;
descriptionField = new EditBox(this.font, leftX, currentY, FIELD_WIDTH, FIELD_HEIGHT,
Component.translatable("screen.command-gui.description"));
descriptionField.setMaxLength(100);
descriptionField.setHint(Component.translatable("screen.command-gui.description_hint"));
this.addRenderableWidget(descriptionField);

// === Fake Player Name ===
currentY += ROW_GAP;
fakePlayerNameField = new EditBox(this.font, leftX, currentY, FIELD_WIDTH, FIELD_HEIGHT,
Component.translatable("screen.command-gui.fakeplayer.playername"));
fakePlayerNameField.setMaxLength(20);
fakePlayerNameField.setValue("Bot_1");
fakePlayerNameField.setHint(Component.translatable("screen.command-gui.fakeplayer.playername_hint"));
this.addRenderableWidget(fakePlayerNameField);

// === Position section ===
currentY += ROW_GAP + 4;

// "Use Current Position" button — fills coord fields with player's current position
fillCurrentPosButton = Button.builder(
Component.translatable("screen.command-gui.fakeplayer.pos.fill_current"),
btn -> fillCurrentPosition()
).bounds(leftX, currentY, FIELD_WIDTH, FIELD_HEIGHT).build();
this.addRenderableWidget(fillCurrentPosButton);

// XYZ row — three equal-width fields
currentY += ROW_GAP;
xField = new EditBox(this.font, leftX, currentY, XYZ_FIELD_WIDTH, FIELD_HEIGHT, Component.literal("X"));
xField.setMaxLength(12);
xField.setFilter(s -> s.isEmpty() || s.matches("-?\\d*\\.?\\d*"));
this.addRenderableWidget(xField);

yField = new EditBox(this.font, leftX + XYZ_FIELD_WIDTH + COORD_GAP, currentY, XYZ_FIELD_WIDTH, FIELD_HEIGHT, Component.literal("Y"));
yField.setMaxLength(12);
yField.setFilter(s -> s.isEmpty() || s.matches("-?\\d*\\.?\\d*"));
this.addRenderableWidget(yField);

zField = new EditBox(this.font, leftX + (XYZ_FIELD_WIDTH + COORD_GAP) * 2, currentY, XYZ_FIELD_WIDTH, FIELD_HEIGHT, Component.literal("Z"));
zField.setMaxLength(12);
zField.setFilter(s -> s.isEmpty() || s.matches("-?\\d*\\.?\\d*"));
this.addRenderableWidget(zField);

// Yaw / Pitch row — two equal-width fields
currentY += ROW_GAP;
yawField = new EditBox(this.font, leftX, currentY, YAW_PITCH_FIELD_WIDTH, FIELD_HEIGHT, Component.literal("Yaw"));
yawField.setMaxLength(10);
yawField.setFilter(s -> s.isEmpty() || s.matches("-?\\d*\\.?\\d*"));
this.addRenderableWidget(yawField);

pitchField = new EditBox(this.font, leftX + YAW_PITCH_FIELD_WIDTH + COORD_GAP, currentY, YAW_PITCH_FIELD_WIDTH, FIELD_HEIGHT, Component.literal("Pitch"));
pitchField.setMaxLength(10);
pitchField.setFilter(s -> s.isEmpty() || s.matches("-?\\d*\\.?\\d*"));
this.addRenderableWidget(pitchField);

// Dimension button
currentY += ROW_GAP;
dimensionButton = Button.builder(
getDimensionLabel(),
btn -> {
dimensionIndex = (dimensionIndex + 1) % DIMENSIONS.length;
dimensionButton.setMessage(getDimensionLabel());
}
).bounds(leftX, currentY, FIELD_WIDTH, FIELD_HEIGHT).build();
this.addRenderableWidget(dimensionButton);

// Gamemode button
currentY += ROW_GAP;
gamemodeButton = Button.builder(
getGamemodeLabel(),
btn -> {
gamemodeIndex = (gamemodeIndex + 1) % GAMEMODES.length;
gamemodeButton.setMessage(getGamemodeLabel());
}
).bounds(leftX, currentY, FIELD_WIDTH, FIELD_HEIGHT).build();
this.addRenderableWidget(gamemodeButton);

// === Actions section ===
currentY += ROW_GAP + 4;
buildActionButtons(leftX, currentY);

int actionRows = (ACTIONS.length + ACTIONS_PER_ROW - 1) / ACTIONS_PER_ROW;
currentY += actionRows * (ACTION_BTN_HEIGHT + ACTION_BTN_GAP) + 4;

// === Config commands section ===
rebuildConfigWidgets(currentY);

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
int actionRows = (ACTIONS.length + ACTIONS_PER_ROW - 1) / ACTIONS_PER_ROW;
int currentY = 20;           // nameField
currentY += ROW_GAP;         // descriptionField
currentY += ROW_GAP;         // fakePlayerNameField
currentY += ROW_GAP + 4;     // fillCurrentPosButton
currentY += ROW_GAP;         // XYZ fields
currentY += ROW_GAP;         // Yaw/Pitch fields
currentY += ROW_GAP;         // dimension button
currentY += ROW_GAP;         // gamemode button
currentY += ROW_GAP + 4;     // action buttons start
currentY += actionRows * (ACTION_BTN_HEIGHT + ACTION_BTN_GAP) + 4;
return currentY;
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

int centerX = this.width / 2;
int leftX = centerX - FIELD_WIDTH / 2;
int currentY = startY;

for (int i = 0; i < configCommands.size(); i++) {
final int idx = i;
EditBox configField = new EditBox(this.font, leftX, currentY, FIELD_WIDTH - 18, FIELD_HEIGHT,
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
).bounds(leftX + FIELD_WIDTH - 16, currentY, 16, FIELD_HEIGHT).build();
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
).bounds(leftX, currentY, 80, 14).build();
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
for (int i = 0; i < actionButtons.size(); i++) {
actionButtons.get(i).active = !selectedActions.contains(i);
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

int centerX = this.width / 2;
int leftX = centerX - FIELD_WIDTH / 2;
int labelX = leftX - 4;
int currentY = 20;

// Title
guiGraphics.drawCenteredString(this.font, this.title, centerX, 6, 0xFFFFFFFF);

// Name label
guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.name"),
labelX - this.font.width(Component.translatable("screen.command-gui.name")), currentY + 4, LABEL_COLOR);

// Description label
currentY += ROW_GAP;
guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.description"),
labelX - this.font.width(Component.translatable("screen.command-gui.description")), currentY + 4, LABEL_COLOR);

// Player name label
currentY += ROW_GAP;
guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.fakeplayer.playername"),
labelX - this.font.width(Component.translatable("screen.command-gui.fakeplayer.playername")), currentY + 4, LABEL_COLOR);

// Spawn At label (same row as fillCurrentPosButton)
currentY += ROW_GAP + 4;
guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.fakeplayer.spawn_at"),
labelX - this.font.width(Component.translatable("screen.command-gui.fakeplayer.spawn_at")), currentY + 4, LABEL_COLOR);

// XYZ axis labels
currentY += ROW_GAP;
guiGraphics.drawString(this.font, "X", leftX - 9, currentY + 4, 0xFFFF5555);
guiGraphics.drawString(this.font, "Y", leftX + XYZ_FIELD_WIDTH + COORD_GAP - 9, currentY + 4, 0xFF55FF55);
guiGraphics.drawString(this.font, "Z", leftX + (XYZ_FIELD_WIDTH + COORD_GAP) * 2 - 9, currentY + 4, 0xFF5555FF);

// Yaw / Pitch labels
currentY += ROW_GAP;
guiGraphics.drawString(this.font, "Yaw",
labelX - this.font.width("Yaw"), currentY + 4, LABEL_COLOR);
guiGraphics.drawString(this.font, "Pitch",
leftX + YAW_PITCH_FIELD_WIDTH + COORD_GAP - this.font.width("Pitch") - 1, currentY + 4, LABEL_COLOR);

// Skip dimension + gamemode rows
currentY += ROW_GAP; // dimension
currentY += ROW_GAP; // gamemode

// Actions label
currentY += ROW_GAP + 4;
guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.fakeplayer.actions_label"),
labelX - this.font.width(Component.translatable("screen.command-gui.fakeplayer.actions_label")), currentY + 2, LABEL_COLOR);

// Config section label
int configStartY = getConfigStartY();
guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.fakeplayer.config_label"),
labelX - this.font.width(Component.translatable("screen.command-gui.fakeplayer.config_label")), configStartY + 4, LABEL_COLOR);

// === Command preview ===
List<String> commands = buildCommands();
int configEndY = configStartY + configCommands.size() * ROW_GAP + 20;

// Config section description (below add-config button)
guiGraphics.drawString(this.font,
Component.translatable("screen.command-gui.fakeplayer.config_desc"),
leftX, configEndY + 2, 0xFF888888);

int previewLabelY = configEndY + 14;
guiGraphics.drawCenteredString(this.font,
Component.translatable("screen.command-gui.fakeplayer.command_preview"),
centerX, previewLabelY, 0xFF888888);

int previewY = previewLabelY + 10;
for (int i = 0; i < commands.size(); i++) {
String cmd = commands.get(i);
String display = this.font.plainSubstrByWidth(cmd, this.width - 10);
guiGraphics.drawString(this.font, display, 4, previewY + i * 10, 0xFF55FF55);
}

// Save / Cancel buttons (positioned dynamically)
int saveBtnY = previewY + commands.size() * 10 + 4;
saveBtnY = Math.min(saveBtnY, this.height - 24);
renderSaveCancel(saveBtnY);
}

private void renderSaveCancel(int saveBtnY) {
if (saveBtnY != lastSaveBtnY) {
if (saveButton != null) this.removeWidget(saveButton);
if (cancelButton != null) this.removeWidget(cancelButton);

int centerX = this.width / 2;
saveButton = Button.builder(
Component.translatable("screen.command-gui.save"),
btn -> saveAndClose()
).bounds(centerX - 102, saveBtnY, 100, 20).build();
this.addRenderableWidget(saveButton);

cancelButton = Button.builder(
Component.translatable("screen.command-gui.cancel"),
btn -> this.minecraft.setScreen(parent)
).bounds(centerX + 2, saveBtnY, 100, 20).build();
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
