package com.remrin.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * Coordinate input screen providing three input fields for X, Y, and Z. Supports absolute
 * coordinates (numeric values), relative coordinates ({@code ~}), and local coordinates
 * ({@code ^}). Provides quick-fill buttons for the player's current exact position and block
 * position.
 */
public class CoordinateInputScreen extends BaseParentedScreen<Screen> {

  private final String commandTemplate;

  private EditBox xField;
  private EditBox yField;
  private EditBox zField;

  public CoordinateInputScreen(Screen parent, Component title, String commandTemplate) {
    super(title, parent);
    this.commandTemplate = commandTemplate;
  }

  @Override
  protected void init() {
    super.init();

    int centerX = this.width / 2;
    int centerY = this.height / 2;
    int fieldWidth = 60;
    int gap = 5;
    int totalWidth = fieldWidth * 3 + gap * 2;
    int startX = centerX - totalWidth / 2;

    xField = new EditBox(this.font, startX, centerY - 30, fieldWidth, 20, Component.literal("X"));
    xField.setMaxLength(10);
    xField.setHint(Component.literal("X"));
    xField.setFilter(this::isValidCoordInput);
    this.addRenderableWidget(xField);
    this.setInitialFocus(xField);

    yField = new EditBox(this.font, startX + fieldWidth + gap, centerY - 30, fieldWidth, 20,
        Component.literal("Y"));
    yField.setMaxLength(10);
    yField.setHint(Component.literal("Y"));
    yField.setFilter(this::isValidCoordInput);
    this.addRenderableWidget(yField);

    zField = new EditBox(this.font, startX + (fieldWidth + gap) * 2, centerY - 30, fieldWidth, 20,
        Component.literal("Z"));
    zField.setMaxLength(10);
    zField.setHint(Component.literal("Z"));
    zField.setFilter(this::isValidCoordInput);
    this.addRenderableWidget(zField);

    this.addRenderableWidget(Button.builder(
        Component.translatable("screen.command-gui.coord.current"),
        btn -> fillCurrentPosition()
    ).bounds(centerX - 75, centerY + 5, 150, 20).build());

    this.addRenderableWidget(Button.builder(
        Component.translatable("screen.command-gui.coord.current_block"),
        btn -> fillCurrentBlockPosition()
    ).bounds(centerX - 75, centerY + 30, 150, 20).build());

    this.addRenderableWidget(Button.builder(
        Component.translatable("screen.command-gui.back"),
        btn -> this.minecraft.setScreen(parent)
    ).bounds(centerX - 75, centerY + 60, 150, 20).build());
  }

  /**
   * Validates coordinate input format, allowing: plain numbers (including negatives and decimals),
   * {@code ~}, {@code ^}, and relative/local coordinates starting with {@code ~} or {@code ^}
   * followed by an optional offset.
   */
  private boolean isValidCoordInput(String text) {
    if (text.isEmpty() || text.equals("-") || text.equals("~") || text.equals("^")) {
      return true;
    }
    if (text.startsWith("~") || text.startsWith("^")) {
      String rest = text.substring(1);
      if (rest.isEmpty() || rest.equals("-")) {
        return true;
      }
      try {
        Double.parseDouble(rest);
        return true;
      } catch (NumberFormatException e) {
        return false;
      }
    }
    try {
      Double.parseDouble(text);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private void fillCurrentPosition() {
    Minecraft mc = Minecraft.getInstance();
    if (mc != null && mc.player != null) {
      xField.setValue(String.format("%.2f", mc.player.getX()));
      yField.setValue(String.format("%.2f", mc.player.getY()));
      zField.setValue(String.format("%.2f", mc.player.getZ()));
    }
  }

  private void fillCurrentBlockPosition() {
    Minecraft mc = Minecraft.getInstance();
    if (mc != null && mc.player != null) {
      xField.setValue(String.valueOf(mc.player.getBlockX()));
      yField.setValue(String.valueOf(mc.player.getBlockY()));
      zField.setValue(String.valueOf(mc.player.getBlockZ()));
    }
  }

  protected void onCoordsConfirmed(String x, String y, String z) {
  }

  @Override
  public boolean keyPressed(KeyEvent keyEvent) {
    int keyCode = keyEvent.key();

    if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
      String x = xField.getValue().trim();
      String y = yField.getValue().trim();
      String z = zField.getValue().trim();
      if (!x.isEmpty() && !y.isEmpty() && !z.isEmpty()) {
        onCoordsConfirmed(x, y, z);
        if (commandTemplate != null) {
          String command = commandTemplate
              .replace("{x}", x)
              .replace("{y}", y)
              .replace("{z}", z)
              .replace("{coords}", x + " " + y + " " + z);
          executeCommand(command);
        }
      }
      return true;
    }

    if (keyCode == GLFW.GLFW_KEY_TAB) {
      if (xField.isFocused()) {
        this.setFocused(yField);
      } else if (yField.isFocused()) {
        this.setFocused(zField);
      } else if (zField.isFocused()) {
        this.setFocused(xField);
      }
      return true;
    }

    if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
      this.minecraft.setScreen(parent);
      return true;
    }

    return super.keyPressed(keyEvent);
  }

  private void executeCommand(String command) {
    Minecraft mc = Minecraft.getInstance();
    if (mc != null && mc.player != null) {
      mc.setScreen(null);
      ChainedCommandExecutor.sendCommand(command);
    }
  }

  @Override
  public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    super.render(guiGraphics, mouseX, mouseY, partialTick);

    int centerX = this.width / 2;
    int centerY = this.height / 2;

    guiGraphics.drawCenteredString(this.font, this.title, centerX, centerY - 60, 0xFFFFFFFF);

    int fieldWidth = 60;
    int gap = 5;
    int totalWidth = fieldWidth * 3 + gap * 2;
    int startX = centerX - totalWidth / 2;

    guiGraphics.drawString(this.font, "X", startX, centerY - 42, 0xFFFF5555);
    guiGraphics.drawString(this.font, "Y", startX + fieldWidth + gap, centerY - 42, 0xFF55FF55);
    guiGraphics.drawString(this.font, "Z", startX + (fieldWidth + gap) * 2, centerY - 42,
        0xFF5555FF);

    guiGraphics.drawCenteredString(this.font,
        Component.translatable("screen.command-gui.enter_to_confirm"),
        centerX, centerY + 90, 0xFF888888);
  }
}
