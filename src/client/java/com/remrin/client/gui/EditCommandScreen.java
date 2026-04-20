package com.remrin.client.gui;

import com.remrin.client.config.CommandConfig;
import java.util.List;
import net.minecraft.network.chat.Component;

/**
 * Screen for editing an existing custom command. Shares the {@link BaseCommandEditorScreen}
 * framework with {@link AddCommandScreen}, but pre-fills the original data. If the name has changed
 * on save, the old entry is deleted and a new one is inserted (to preserve the original category
 * assignment).
 */
public class EditCommandScreen extends BaseCommandEditorScreen {

  private final String originalName;
  private final String originalCommand;
  private final List<String> originalCommands;
  private final String originalDescription;

  public EditCommandScreen(CommandGUIScreen parent, String name, CommandConfig.CommandEntry entry) {
    super(Component.translatable("screen.command-gui.edit_title"), parent);
    this.originalName = name;
    this.originalCommands = entry.getCommands();
    // For the command field, use the last command or first if only one
    this.originalCommand = originalCommands.isEmpty() ? "" :
        (originalCommands.size() == 1 ? originalCommands.get(0) : "");
    this.originalDescription = entry.description;
  }

  @Override
  protected String getInitialName() {
    return originalName;
  }

  @Override
  protected String getInitialDescription() {
    return originalDescription != null ? originalDescription : "";
  }

  @Override
  protected String getInitialCommand() {
    if (originalCommand == null) return "";
    // Strip leading / so the field works with commandsOnly=true suggestions
    return originalCommand.startsWith("/") ? originalCommand.substring(1) : originalCommand;
  }

  @Override
  protected List<String> getInitialCommandList() {
    // For multi-command entries, put all except the last in the list
    // and the last one in the command field
    if (originalCommands.size() > 1) {
      return originalCommands;
    }
    return List.of();
  }

  @Override
  protected void performSave() {
    String newName = nameField.getValue().trim();
    String newDescription = descriptionField.getValue().trim();
    List<String> commands = getAllCommands();

    if (!newName.equals(originalName)) {
      // Name changed: find the original entry's category, delete it, then re-insert with the new name to preserve category assignment
      String categoryId = CommandConfig.findCommandCategory(originalName);
      CommandConfig.removeCommand(originalName);
      if (commands.size() > 1) {
        CommandConfig.addCommandMulti(categoryId != null ? categoryId : "default", newName,
            commands, newDescription);
      } else if (!commands.isEmpty()) {
        CommandConfig.addCommand(categoryId != null ? categoryId : "default", newName,
            commands.get(0), newDescription);
      }
    } else {
      // Name unchanged: update the entry in place
      if (commands.size() > 1) {
        CommandConfig.updateCommandMulti(newName, commands, newDescription);
      } else if (!commands.isEmpty()) {
        CommandConfig.updateCommand(newName, commands.get(0), newDescription);
      }
    }
  }
}
