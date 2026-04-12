package com.remrin.client.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

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
