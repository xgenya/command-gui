package com.remrin.client.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Base screen class with a parent screen. Automatically returns to the parent screen on close, and
 * does not pause the game (to avoid pausing the world in singleplayer).
 */
public abstract class BaseParentedScreen<P extends Screen> extends Screen {

  protected final P parent;

  protected BaseParentedScreen(Component title, P parent) {
    super(title);
    this.parent = parent;
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }

  @Override
  public void onClose() {
    this.minecraft.setScreen(parent);
  }
}
