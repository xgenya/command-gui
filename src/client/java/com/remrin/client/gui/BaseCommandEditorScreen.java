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
  protected static final int PLACEHOLDER_BTNS_PER_ROW = 3;
  protected static final int INPUT_HEIGHT = 16;
  protected static final int CONTENT_WIDTH = 170;
  protected static final int LABEL_WIDTH = 45;
  protected static final int ROW_GAP = 20;
  protected static final int Y_OFFSET = -20;
  protected static final int BTN_GAP = 4;
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

  protected BaseCommandEditorScreen(Component title, CommandGUIScreen parent) {
    super(title, parent);
  }

  /**
   * Y coordinate of name field relative to screen top
   */
  protected abstract int getFieldStartY(int centerY);

  /**
   * Y coordinate of title text
   */
  protected abstract int getTitleY(int centerY);

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
   * Initialise any extra widgets between description and placeholder buttons.
   *
   * @return the Y of the last extra widget, or currentY if none added
   */
  protected int initExtraRow(int fieldX, int currentY) {
    return currentY;
  }

  /**
   * Render any extra label between description and placeholder labels.
   *
   * @return updated currentY after the extra row (or same if none)
   */
  protected int renderExtraLabel(GuiGraphics guiGraphics, int labelX, int currentY) {
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

  @Override
  protected void init() {
    super.init();

    int centerX = this.width / 2;
    int centerY = this.height / 2 + Y_OFFSET;
    int fieldX = centerX - CONTENT_WIDTH / 2 + LABEL_WIDTH;

    int currentY = getFieldStartY(centerY);
    nameField = new EditBox(this.font, fieldX, currentY, CONTENT_WIDTH, INPUT_HEIGHT,
        Component.translatable("screen.command-gui.name"));
    nameField.setMaxLength(50);
    nameField.setValue(getInitialName());
    if (showNameHint()) {
      nameField.setHint(Component.translatable("screen.command-gui.name_hint"));
    }
    this.addRenderableWidget(nameField);
    this.setInitialFocus(nameField);

    currentY += ROW_GAP;
    descriptionField = new EditBox(this.font, fieldX, currentY, CONTENT_WIDTH, INPUT_HEIGHT,
        Component.translatable("screen.command-gui.description"));
    descriptionField.setMaxLength(100);
    descriptionField.setValue(getInitialDescription());
    descriptionField.setHint(Component.translatable("screen.command-gui.description_hint"));
    this.addRenderableWidget(descriptionField);

    currentY = initExtraRow(fieldX, currentY);

    currentY += ROW_GAP + 4;
    int placeholderBtnWidth =
        (CONTENT_WIDTH - BTN_GAP * (PLACEHOLDER_BTNS_PER_ROW - 1)) / PLACEHOLDER_BTNS_PER_ROW;
    for (int i = 0; i < TYPE_KEYS.length; i++) {
      final int index = i;
      int row = i / PLACEHOLDER_BTNS_PER_ROW;
      int col = i % PLACEHOLDER_BTNS_PER_ROW;
      int btnX = fieldX + col * (placeholderBtnWidth + BTN_GAP);
      int btnY = currentY + row * (PLACEHOLDER_BTN_HEIGHT + 2);
      Button typeBtn = Button.builder(
          Component.translatable(TYPE_KEYS[i]),
          btn -> appendPlaceholder(index)
      ).bounds(btnX, btnY, placeholderBtnWidth, PLACEHOLDER_BTN_HEIGHT).build();
      this.addRenderableWidget(typeBtn);
    }

    // Initialize command list from initial data
    if (commandList.isEmpty()) {
      List<String> initial = getInitialCommandList();
      if (initial != null && !initial.isEmpty()) {
        commandList.addAll(initial);
      }
    }

    // Bottom: command input + "add to list" button
    int addBtnWidth = 50;
    commandField = new EditBox(this.font, 4, this.height - 12, this.width - 8 - addBtnWidth - 4, 12,
        Component.translatable("screen.command-gui.command"));
    commandField.setMaxLength(256);
    commandField.setValue(getInitialCommand());
    commandField.setBordered(false);
    Component cmdHint = getCommandHint();
    if (cmdHint != null) {
      commandField.setHint(cmdHint);
    }
    this.addRenderableWidget(commandField);

    addToListButton = Button.builder(
        Component.translatable("screen.command-gui.add_command_line"),
        btn -> addCurrentCommandToList()
    ).bounds(this.width - addBtnWidth - 4, this.height - 16, addBtnWidth, 14).build();
    this.addRenderableWidget(addToListButton);

    this.commandSuggestions = new CommandSuggestions(this.minecraft, this, commandField,
        this.font, false, false, 1, 10, true, -805306368);
    this.commandSuggestions.setAllowSuggestions(true);
    this.commandSuggestions.updateCommandInfo();
    commandField.setResponder(text -> this.commandSuggestions.updateCommandInfo());

    rebuildCommandListButtons();
  }

  private void addCurrentCommandToList() {
    String cmd = commandField.getValue().trim();
    if (!cmd.isEmpty()) {
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

    int centerX = this.width / 2;
    int fieldX = centerX - CONTENT_WIDTH / 2 + LABEL_WIDTH;
    int rows = (TYPE_KEYS.length + PLACEHOLDER_BTNS_PER_ROW - 1) / PLACEHOLDER_BTNS_PER_ROW;
    int centerY = this.height / 2 + Y_OFFSET;
    int startY = getFieldStartY(centerY);

    // Skip through fields to find placeholder area end
    int placeholderStartY = startY;
    placeholderStartY += ROW_GAP; // description
    // Account for extra row
    if (hasExtraRow()) {
      placeholderStartY += ROW_GAP;
    }
    placeholderStartY += ROW_GAP + 4; // placeholder section
    int placeholderEndY = placeholderStartY + rows * (PLACEHOLDER_BTN_HEIGHT + 2) + 16;

    int listY = placeholderEndY + 2;
    for (int i = 0; i < commandList.size(); i++) {
      final int idx = i;
      Button removeBtn = Button.builder(
          Component.translatable("screen.command-gui.remove_command"),
          btn -> {
            commandList.remove(idx);
            rebuildCommandListButtons();
          }
      ).bounds(fieldX + CONTENT_WIDTH - 14, listY + i * 12, 14, 12).build();
      commandRemoveButtons.add(removeBtn);
      this.addRenderableWidget(removeBtn);
    }
  }

  /**
   * Override in subclass if it adds an extra row
   */
  protected boolean hasExtraRow() {
    return false;
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
   */
  protected List<String> getAllCommands() {
    List<String> all = new ArrayList<>(commandList);
    String current = commandField.getValue().trim();
    if (!current.isEmpty()) {
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
    int labelX = centerX - CONTENT_WIDTH / 2 - 4;
    int fieldX = centerX - CONTENT_WIDTH / 2 + LABEL_WIDTH;

    guiGraphics.drawCenteredString(this.font, this.title, centerX, getTitleY(centerY), 0xFFFFFFFF);

    int currentY = getFieldStartY(centerY);
    guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.name"),
        labelX - this.font.width(Component.translatable("screen.command-gui.name")), currentY + 4,
        0xFFAAAAAA);

    currentY += ROW_GAP;
    guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.description"),
        labelX - this.font.width(Component.translatable("screen.command-gui.description")),
        currentY + 4, 0xFFAAAAAA);

    currentY = renderExtraLabel(guiGraphics, labelX, currentY);

    currentY += ROW_GAP + 4;
    guiGraphics.drawString(this.font,
        Component.translatable("screen.command-gui.placeholder_label"),
        labelX - this.font.width(Component.translatable("screen.command-gui.placeholder_label")),
        currentY + 4, 0xFFAAAAAA);

    // Placeholder description text
    int rows = (TYPE_KEYS.length + PLACEHOLDER_BTNS_PER_ROW - 1) / PLACEHOLDER_BTNS_PER_ROW;
    int afterPlaceholders = currentY + rows * (PLACEHOLDER_BTN_HEIGHT + 2) + 4;
    Component placeholderDesc = Component.translatable("screen.command-gui.placeholder_desc");
    int maxDescWidth = this.width - 20;
    List<net.minecraft.util.FormattedCharSequence> descLines = this.font.split(placeholderDesc,
        maxDescWidth);
    int descY = afterPlaceholders;
    for (net.minecraft.util.FormattedCharSequence line : descLines) {
      guiGraphics.drawString(this.font, line, 10, descY, 0xFF666666);
      descY += 10;
    }

    // Command list label and items
    if (!commandList.isEmpty()) {
      descY += 2;
      guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.commands_label"),
          labelX - this.font.width(Component.translatable("screen.command-gui.commands_label")),
          descY + 2, 0xFFAAAAAA);
      for (int i = 0; i < commandList.size(); i++) {
        String cmd = commandList.get(i);
        String display = this.font.plainSubstrByWidth(cmd, CONTENT_WIDTH - 20);
        guiGraphics.drawString(this.font, display, fieldX, descY + i * 12 + 2, 0xFF55FF55);
      }
    }

    // Bottom command area
    guiGraphics.fill(2, this.height - 14, this.width - 2, this.height - 2, 0x80000000);
    guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.command"),
        4, this.height - 24, 0xFFAAAAAA);

    guiGraphics.drawCenteredString(this.font,
        Component.translatable("screen.command-gui.enter_to_save"),
        centerX, this.height - 34, 0xFF888888);

    this.commandSuggestions.render(guiGraphics, mouseX, mouseY);
  }
}
