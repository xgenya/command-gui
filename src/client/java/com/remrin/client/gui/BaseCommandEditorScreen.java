package com.remrin.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public abstract class BaseCommandEditorScreen extends BaseParentedScreen<CommandGUIScreen> {
    protected static final String[] TYPE_KEYS = {
        "screen.command-gui.type.player_all_full",
        "screen.command-gui.type.player_other_full",
        "screen.command-gui.type.player_fake_full",
        "screen.command-gui.type.text_full",
        "screen.command-gui.type.number_full",
        "screen.command-gui.type.coord_full"
    };

    protected static final String[] PLACEHOLDERS = {
        "{player_all}",
        "{player}",
        "{player_fake}",
        "{name}",
        "{number}",
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

    protected EditBox nameField;
    protected EditBox descriptionField;
    protected EditBox commandField;
    protected CommandSuggestions commandSuggestions;

    protected BaseCommandEditorScreen(Component title, CommandGUIScreen parent) {
        super(title, parent);
    }

    /** Y coordinate of name field relative to screen top */
    protected abstract int getFieldStartY(int centerY);

    /** Y coordinate of title text */
    protected abstract int getTitleY(int centerY);

    protected abstract String getInitialName();
    protected abstract String getInitialDescription();
    protected abstract String getInitialCommand();

    /** Called to perform the actual save logic. */
    protected abstract void performSave();

    /** Show placeholder hint on the name field. Default: false */
    protected boolean showNameHint() {
        return false;
    }

    /** Hint to show on command field. Default: null (no hint) */
    protected Component getCommandHint() {
        return null;
    }

    /**
     * Initialise any extra widgets between description and placeholder buttons.
     * @return the Y of the last extra widget, or currentY if none added
     */
    protected int initExtraRow(int fieldX, int currentY) {
        return currentY;
    }

    /**
     * Render any extra label between description and placeholder labels.
     * @return updated currentY after the extra row (or same if none)
     */
    protected int renderExtraLabel(GuiGraphics guiGraphics, int labelX, int currentY) {
        return currentY;
    }

    /** Called before super.resize(); subclass should save extra state. */
    protected void onBeforeResize() {}

    /** Called after fields are restored; subclass should restore extra state. */
    protected void onAfterResize() {}

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
        int placeholderBtnWidth = (CONTENT_WIDTH - BTN_GAP * (PLACEHOLDER_BTNS_PER_ROW - 1)) / PLACEHOLDER_BTNS_PER_ROW;
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

        commandField = new EditBox(this.font, 4, this.height - 12, this.width - 8, 12,
                Component.translatable("screen.command-gui.command"));
        commandField.setMaxLength(256);
        commandField.setValue(getInitialCommand());
        commandField.setBordered(false);
        Component cmdHint = getCommandHint();
        if (cmdHint != null) {
            commandField.setHint(cmdHint);
        }
        this.addRenderableWidget(commandField);

        this.commandSuggestions = new CommandSuggestions(this.minecraft, this, commandField,
                this.font, false, false, 1, 10, true, -805306368);
        this.commandSuggestions.setAllowSuggestions(true);
        this.commandSuggestions.updateCommandInfo();
        commandField.setResponder(text -> this.commandSuggestions.updateCommandInfo());
    }

    protected void appendPlaceholder(int index) {
        String placeholder = PLACEHOLDERS[index];
        String current = commandField.getValue();
        if (!current.isEmpty() && !current.endsWith(" ")) {
            current += " ";
        }
        commandField.setValue(current + placeholder);
        this.setFocused(commandField);
    }

    protected final void saveAndClose() {
        String newName = nameField.getValue().trim();
        String newCommand = commandField.getValue().trim();
        if (!newName.isEmpty() && !newCommand.isEmpty()) {
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
        onBeforeResize();
        super.resize(width, height);
        this.nameField.setValue(name);
        this.descriptionField.setValue(description);
        this.commandField.setValue(command);
        onAfterResize();
        this.commandSuggestions.updateCommandInfo();
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
            String command = commandField.getValue().trim();
            if (!name.isEmpty() && !command.isEmpty()) {
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

        guiGraphics.drawCenteredString(this.font, this.title, centerX, getTitleY(centerY), 0xFFFFFFFF);

        int currentY = getFieldStartY(centerY);
        guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.name"),
                labelX - this.font.width(Component.translatable("screen.command-gui.name")), currentY + 4, 0xFFAAAAAA);

        currentY += ROW_GAP;
        guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.description"),
                labelX - this.font.width(Component.translatable("screen.command-gui.description")), currentY + 4, 0xFFAAAAAA);

        currentY = renderExtraLabel(guiGraphics, labelX, currentY);

        currentY += ROW_GAP + 4;
        guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.placeholder_label"),
                labelX - this.font.width(Component.translatable("screen.command-gui.placeholder_label")), currentY + 4, 0xFFAAAAAA);

        guiGraphics.fill(2, this.height - 14, this.width - 2, this.height - 2, 0x80000000);
        guiGraphics.drawString(this.font, Component.translatable("screen.command-gui.command"),
                4, this.height - 24, 0xFFAAAAAA);

        int rows = (TYPE_KEYS.length + PLACEHOLDER_BTNS_PER_ROW - 1) / PLACEHOLDER_BTNS_PER_ROW;
        guiGraphics.drawCenteredString(this.font,
                Component.translatable("screen.command-gui.enter_to_save"),
                centerX, currentY + rows * (PLACEHOLDER_BTN_HEIGHT + 2) + 8, 0xFF888888);

        this.commandSuggestions.render(guiGraphics, mouseX, mouseY);
    }
}
