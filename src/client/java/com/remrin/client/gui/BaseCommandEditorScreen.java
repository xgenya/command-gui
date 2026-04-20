package com.remrin.client.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * Base class for command editor screens, providing a common UI framework for name / description /
 * placeholder insertion / command list fields.
 * <p>
 * Placeholder buttons allow inserting dynamic variables such as {@code {player}} or
 * {@code {number}} into the command text; at execution time {@link ChainedCommandExecutor} pops the
 * corresponding input screen for each placeholder.
 * <p>
 * Subclasses implement {@link #performSave()} to decide how data is written to the config, and may
 * inject extra rows below the description field via {@link #initExtraRow} /
 * {@link #renderExtraLabel}.
 */
public abstract class BaseCommandEditorScreen extends BaseParentedScreen<CommandGUIScreen> {

  protected static final String[] TYPE_KEYS = {
      "screen.command-gui.type.player_all_full",
      "screen.command-gui.type.player_other_full",
      "screen.command-gui.type.player_fake_full",
      "screen.command-gui.type.text_full",
      "screen.command-gui.type.number_full",
      "screen.command-gui.type.time_full",
      "screen.command-gui.type.coord_full"
  };

  /**
   * Placeholder token array, corresponding 1:1 with TYPE_KEYS; clicking a type button inserts the
   * matching token
   */
  protected static final String[] PLACEHOLDERS = {
      "{player_all}",
      "{player}",
      "{player_fake}",
      "{name}",
      "{number}",
      "{time}",
      "{coords}"
  };

  protected static final int PLACEHOLDER_BTN_HEIGHT = 16;
  /** Gap between left form column and right placeholder column. */
  protected static final int COL_GAP = 10;
  protected static final int INPUT_HEIGHT = 16;
  /** Preferred maximum content width; actual width is clamped to screen width - 40. */
  protected static final int CONTENT_WIDTH = 300;
  /** Vertical gap from label top to its input field top (label sits above the field). */
  protected static final int LABEL_TO_FIELD = 10;
  protected static final int ROW_GAP = 28;
  protected static final int Y_OFFSET = -20;
  protected static final int BTN_GAP = 4;
  protected static final int ADD_BTN_WIDTH = 120;
  /**
   * Y position and height of the command field, matching the vanilla command block so that
   * {@link CommandSuggestions} (which hardcodes the suggestion popup at y=72 when
   * {@code anchorToBottom=false}) places suggestions directly below the field.
   */
  protected static final int CMD_FIELD_Y = 50;
  protected static final int CMD_FIELD_HEIGHT = 20;
  /**
   * Multi-command list (command sequence); commands are sent in order at execution time
   */
  protected final List<String> commandList = new ArrayList<>();
  private final List<Button> commandRemoveButtons = new ArrayList<>();
  protected EditBox nameField;
  protected EditBox descriptionField;
  protected EditBox commandField;
  protected CommandSuggestions commandSuggestions;
  private Button addToListButton;

  // Cached label Components — computed once in init(), reused every frame
  private Component cachedLabelName;
  private Component cachedLabelDesc;
  private Component cachedLabelPlaceholder;
  private Component cachedLabelCommands;
  private Component cachedLabelCommand;

  protected BaseCommandEditorScreen(Component title, CommandGUIScreen parent) {
    super(title, parent);
  }

  /**
   * Y coordinate of name field relative to screen top (left column start).
   * In the two-column layout, form fields begin at the same Y as placeholder buttons.
   */
  protected int getFieldStartY(int centerY) {
    return CMD_FIELD_Y + CMD_FIELD_HEIGHT + 6 + LABEL_TO_FIELD;
  }

  /**
   * Y coordinate of title text.
   */
  protected int getTitleY(int centerY) {
    return Math.max(6, (CMD_FIELD_Y - LABEL_TO_FIELD - this.font.lineHeight) / 2);
  }

  protected abstract String getInitialName();

  protected abstract String getInitialDescription();

  protected abstract String getInitialCommand();

  /**
   * Called to perform the actual save logic.
   */
  protected abstract void performSave();

  /**
   * Show placeholder hint on the name field. Default: false
   */
  protected boolean showNameHint() {
    return false;
  }

  /**
   * Hint to show on command field. Default: null (no hint)
   */
  protected Component getCommandHint() {
    return null;
  }

  /**
   * Initialise any extra widgets between name and description fields.
   * <p>
   * {@code currentY} is the Y at which the extra widget should be placed. Return
   * {@code currentY + ROW_GAP} if a widget was added, or {@code currentY} if not.
   */
  protected int initExtraRow(int fieldX, int currentY) {
    return currentY;
  }

  /**
   * Render any extra label between name and description labels.
   * <p>
   * {@code currentY} is the field Y for the extra row. Draw the label at
   * {@code currentY - LABEL_TO_FIELD}. Return {@code currentY + ROW_GAP} if rendered, or
   * {@code currentY} if not.
   */
  protected int renderExtraLabel(GuiGraphics guiGraphics, int fieldX, int currentY) {
    return currentY;
  }

  /**
   * Called before super.resize(); subclass should save extra state.
   */
  protected void onBeforeResize() {
  }

  /**
   * Called after fields are restored; subclass should restore extra state.
   */
  protected void onAfterResize() {
  }

  /**
   * Get the initial list of commands for editing (multi-command entries). Override in subclass if
   * editing an existing multi-command entry.
   */
  protected List<String> getInitialCommandList() {
    return List.of();
  }

  /**
   * Actual content width for the current screen: capped at {@link #CONTENT_WIDTH} but shrinks on
   * narrow screens to maintain at least 20 px of horizontal margin on each side.
   */
  protected int effectiveContentWidth() {
    return Math.min(CONTENT_WIDTH, this.width - 40);
  }

  /** Left X of all form fields, centres the content block horizontally. */
  protected int effectiveFieldX() {
    return (this.width - effectiveContentWidth()) / 2;
  }

  /** Width of the left (form) column in the two-column layout (80% of content). */
  protected int effectiveLeftColWidth() {
    int cw = effectiveContentWidth();
    return cw - cw / 5 - COL_GAP;
  }

  /** Width of the right (placeholder) column (20% of content). */
  protected int effectiveRightColWidth() {
    return effectiveContentWidth() / 5;
  }

  /** X coordinate of the right (placeholder) column. */
  protected int effectiveRightColX() {
    return effectiveFieldX() + effectiveLeftColWidth() + COL_GAP;
  }

  /**
   * Width of the name field. Override to return a narrower value when adding an inline widget
   * (e.g. a category button) to the right of the name field on the same row.
   */
  protected int getNameFieldWidth(int contentWidth) {
    return contentWidth;
  }

  @Override
  protected void init() {
    super.init();

    // Cache label Components once per init/resize
    cachedLabelName        = Component.translatable("screen.command-gui.name");
    cachedLabelDesc        = Component.translatable("screen.command-gui.description");
    cachedLabelPlaceholder = Component.translatable("screen.command-gui.placeholder_label");
    cachedLabelCommands    = Component.translatable("screen.command-gui.commands_label");
    cachedLabelCommand     = Component.translatable("screen.command-gui.command");

    int centerX = this.width / 2;
    int fieldX = effectiveFieldX();
    int contentWidth = effectiveContentWidth();
    int leftWidth = effectiveLeftColWidth();
    int rightWidth = effectiveRightColWidth();
    int rightColX = effectiveRightColX();
    // Y where left column form section begins (below command field)
    int formStartY = CMD_FIELD_Y + CMD_FIELD_HEIGHT + 6;  // = 76
    int bottomBarY = this.height - 26;

    // Command field — full width, fixed at top.
    // CommandSuggestions with anchorToBottom=false hardcodes the popup at y=72;
    // placing the field at CMD_FIELD_Y=50 with CMD_FIELD_HEIGHT=20 makes the popup
    // appear at y=71, directly below the field (same as vanilla).
    commandField = new EditBox(this.font, fieldX, CMD_FIELD_Y, leftWidth, CMD_FIELD_HEIGHT,
        Component.translatable("screen.command-gui.command"));
    commandField.setMaxLength(256);
    commandField.setValue(getInitialCommand());
    Component cmdHint = getCommandHint();
    if (cmdHint != null) {
      commandField.setHint(cmdHint);
    }
    this.addRenderableWidget(commandField);
    this.setInitialFocus(commandField);

    this.commandSuggestions = new CommandSuggestions(this.minecraft, this, commandField,
        this.font, true, true, 0, 7, false, Integer.MIN_VALUE);
    this.commandSuggestions.setAllowSuggestions(true);
    this.commandSuggestions.updateCommandInfo();
    commandField.setResponder(text -> this.commandSuggestions.updateCommandInfo());

    // Right column: placeholder buttons start at CMD_FIELD_Y (parallel with command field).
    // The suggestion popup (rendered on top) temporarily covers these when active.
    for (int i = 0; i < TYPE_KEYS.length; i++) {
      final int index = i;
      int btnY = CMD_FIELD_Y + i * (PLACEHOLDER_BTN_HEIGHT + 1);
      Button typeBtn = Button.builder(
          Component.translatable(TYPE_KEYS[i]),
          btn -> appendPlaceholder(index)
      ).bounds(rightColX, btnY, rightWidth, PLACEHOLDER_BTN_HEIGHT).build();
      this.addRenderableWidget(typeBtn);
    }

    // Left column: form fields starting at formStartY + LABEL_TO_FIELD = 86
    int currentY = formStartY + LABEL_TO_FIELD;  // = 86
    nameField = new EditBox(this.font, fieldX, currentY, getNameFieldWidth(leftWidth), INPUT_HEIGHT,
        Component.translatable("screen.command-gui.name"));
    nameField.setMaxLength(50);
    nameField.setValue(getInitialName());
    if (showNameHint()) {
      nameField.setHint(Component.translatable("screen.command-gui.name_hint"));
    }
    this.addRenderableWidget(nameField);

    currentY += ROW_GAP;  // = 114
    currentY = initExtraRow(fieldX, currentY);

    descriptionField = new EditBox(this.font, fieldX, currentY, leftWidth, INPUT_HEIGHT,
        Component.translatable("screen.command-gui.description"));
    descriptionField.setMaxLength(100);
    descriptionField.setValue(getInitialDescription());
    descriptionField.setHint(Component.translatable("screen.command-gui.description_hint"));
    this.addRenderableWidget(descriptionField);

    // Initialize command list from initial data
    if (commandList.isEmpty()) {
      List<String> initial = getInitialCommandList();
      if (initial != null && !initial.isEmpty()) {
        commandList.addAll(initial);
      }
    }

    // Bottom bar: [Add to List] [Save] [Cancel]
    int addBtnW = Math.min(ADD_BTN_WIDTH, contentWidth / 2);
    int saveCancelW = Math.min(80, contentWidth / 4);
    int barTotalW = addBtnW + BTN_GAP + saveCancelW + BTN_GAP + saveCancelW;
    int barStartX = fieldX + (contentWidth - barTotalW) / 2;

    addToListButton = Button.builder(
        Component.translatable("screen.command-gui.add_command_line"),
        btn -> addCurrentCommandToList()
    ).bounds(barStartX, bottomBarY, addBtnW, 20).build();
    this.addRenderableWidget(addToListButton);

    Button saveButton = Button.builder(
        Component.translatable("screen.command-gui.save"),
        btn -> saveAndClose()
    ).bounds(barStartX + addBtnW + BTN_GAP, bottomBarY, saveCancelW, 20).build();
    this.addRenderableWidget(saveButton);

    Button cancelButton = Button.builder(
        Component.translatable("screen.command-gui.cancel"),
        btn -> this.minecraft.setScreen(parent)
    ).bounds(barStartX + addBtnW + BTN_GAP + saveCancelW + BTN_GAP, bottomBarY, saveCancelW, 20).build();
    this.addRenderableWidget(cancelButton);

    rebuildCommandListButtons();
  }

  private void addCurrentCommandToList() {
    String cmd = commandField.getValue().trim();
    if (!cmd.isEmpty()) {
      if (!cmd.startsWith("/")) cmd = "/" + cmd;
      commandList.add(cmd);
      commandField.setValue("");
      rebuildCommandListButtons();
    }
  }

  private void rebuildCommandListButtons() {
    for (Button btn : commandRemoveButtons) {
      this.removeWidget(btn);
    }
    commandRemoveButtons.clear();

    int fieldX = effectiveFieldX();
    int leftWidth = effectiveLeftColWidth();
    int listY = getCommandListStartY();
    for (int i = 0; i < commandList.size(); i++) {
      final int idx = i;
      Button removeBtn = Button.builder(
          Component.translatable("screen.command-gui.remove_command"),
          btn -> {
            commandList.remove(idx);
            rebuildCommandListButtons();
          }
      ).bounds(fieldX + leftWidth - 14, listY + 12 + i * 12, 14, 12).build();
      commandRemoveButtons.add(removeBtn);
      this.addRenderableWidget(removeBtn);
    }
  }

  /**
   * Y coordinate of the command list label — just below the description field.
   */
  private int getCommandListStartY() {
    // Name field Y=86, desc field Y=114 (86+ROW_GAP), list label Y=136 (114+INPUT_HEIGHT+6)
    return CMD_FIELD_Y + CMD_FIELD_HEIGHT + 6 + LABEL_TO_FIELD + ROW_GAP + INPUT_HEIGHT + 6;
  }

  /**
   * Appends the placeholder to the end of the command input field and shifts focus to it. If the
   * current text does not end with a space, a space is prepended for readability.
   */
  protected void appendPlaceholder(int index) {
    String placeholder = PLACEHOLDERS[index];
    String current = commandField.getValue();
    if (!current.isEmpty() && !current.endsWith(" ")) {
      current += " ";
    }
    commandField.setValue(current + placeholder);
    this.setFocused(commandField);
  }

  /**
   * Get all commands: the list + any current text in the command field.
   * Ensures every command is prefixed with {@code /}.
   */
  protected List<String> getAllCommands() {
    List<String> all = new ArrayList<>(commandList);
    String current = commandField.getValue().trim();
    if (!current.isEmpty()) {
      if (!current.startsWith("/")) current = "/" + current;
      all.add(current);
    }
    return all;
  }

  protected final void saveAndClose() {
    String newName = nameField.getValue().trim();
    List<String> commands = getAllCommands();
    if (!newName.isEmpty() && !commands.isEmpty()) {
      performSave();
      parent.refresh();
      this.minecraft.setScreen(parent);
    }
  }

  @Override
  public void resize(int width, int height) {
    String name = this.nameField.getValue();
    String description = this.descriptionField.getValue();
    String command = this.commandField.getValue();
    List<String> savedList = new ArrayList<>(this.commandList);
    onBeforeResize();
    super.resize(width, height);
    this.nameField.setValue(name);
    this.descriptionField.setValue(description);
    this.commandField.setValue(command);
    this.commandList.clear();
    this.commandList.addAll(savedList);
    onAfterResize();
    this.commandSuggestions.updateCommandInfo();
    rebuildCommandListButtons();
  }

  @Override
  public boolean keyPressed(KeyEvent keyEvent) {
    int keyCode = keyEvent.key();

    if (this.commandSuggestions.keyPressed(keyEvent)) {
      return true;
    }

    if (keyCode == GLFW.GLFW_KEY_TAB) {
      return true;
    }

    if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
      String name = nameField.getValue().trim();
      List<String> commands = getAllCommands();
      if (!name.isEmpty() && !commands.isEmpty()) {
        saveAndClose();
      }
      return true;
    }

    if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
      this.minecraft.setScreen(parent);
      return true;
    }

    return super.keyPressed(keyEvent);
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
    if (this.commandSuggestions.mouseScrolled(scrollY)) {
      return true;
    }
    return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
  }

  @Override
  public boolean mouseClicked(MouseButtonEvent mouseEvent, boolean focused) {
    if (this.commandSuggestions.mouseClicked(mouseEvent)) {
      return true;
    }
    return super.mouseClicked(mouseEvent, focused);
  }

  @Override
  public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    super.render(guiGraphics, mouseX, mouseY, partialTick);

    int centerX = this.width / 2;
    int centerY = this.height / 2 + Y_OFFSET;
    int fieldX = effectiveFieldX();
    int leftWidth = effectiveLeftColWidth();
    int rightColX = effectiveRightColX();
    // Y where left column form section begins (below command field)
    int formStartY = CMD_FIELD_Y + CMD_FIELD_HEIGHT + 6;  // = 76

    guiGraphics.drawCenteredString(this.font, this.title, centerX, getTitleY(centerY), 0xFFFFFFFF);

    // Command field label
    guiGraphics.drawString(this.font, cachedLabelCommand,
        fieldX, CMD_FIELD_Y - LABEL_TO_FIELD, 0xFFAAAAAA);

    // Right column: placeholder label aligned with command field label
    guiGraphics.drawString(this.font, cachedLabelPlaceholder,
        rightColX, CMD_FIELD_Y - LABEL_TO_FIELD, 0xFFAAAAAA);

    // Left column: name label
    guiGraphics.drawString(this.font, cachedLabelName,
        fieldX, formStartY, 0xFFAAAAAA);

    // Description label — currentY is the desc field Y
    int currentY = formStartY + LABEL_TO_FIELD + ROW_GAP;  // = 114
    currentY = renderExtraLabel(guiGraphics, fieldX, currentY);
    guiGraphics.drawString(this.font, cachedLabelDesc,
        fieldX, currentY - LABEL_TO_FIELD, 0xFFAAAAAA);

    // Command list label and items
    if (!commandList.isEmpty()) {
      int listY = getCommandListStartY();
      guiGraphics.drawString(this.font, cachedLabelCommands, fieldX, listY, 0xFFAAAAAA);
      for (int i = 0; i < commandList.size(); i++) {
        String cmd = commandList.get(i);
        String display = this.font.plainSubstrByWidth(cmd, leftWidth - 20);
        guiGraphics.drawString(this.font, display, fieldX, listY + 12 + i * 12, 0xFF55FF55);
      }
    }

    this.commandSuggestions.render(guiGraphics, mouseX, mouseY);
  }
}
