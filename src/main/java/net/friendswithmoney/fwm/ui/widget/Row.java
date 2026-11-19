/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.world.item.ItemStack
 */
package net.friendswithmoney.fwm.ui.widget;

import java.util.Objects;
import net.friendswithmoney.fwm.ui.FwmTheme;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.ItemStack;

public abstract class Row {
    public static final int HEIGHT = 20;
    private static final int MAX_CONTROL_WIDTH = 106;
    private static final int MIN_CONTROL_WIDTH = 60;
    private static final int MIN_LABEL_WIDTH = 40;
    protected final Component label;
    protected int x;
    protected int y;
    protected int width;
    private ItemStack icon;

    protected Row(Component label) {
        this.label = label;
    }

    public void setBounds(int x, int y, int width) {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    protected int padding() {
        return this.width < 180 ? 5 : 10;
    }

    protected int controlWidth() {
        int room = this.width - 2 * this.padding() - 40;
        return Math.max(60, Math.min(106, room));
    }

    protected int controlX() {
        return this.x + this.width - this.padding() - this.controlWidth();
    }

    protected int controlRight() {
        return this.x + this.width - this.padding();
    }

    protected Component trimmedLabel(Font font) {
        int room = this.controlX() - (this.x + this.padding() + this.iconRoom()) - 4;
        if (font.width((FormattedText)this.label) <= room) {
            return this.label;
        }
        return Component.literal((String)font.plainSubstrByWidth(this.label.getString(), Math.max(8, room)));
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= (double)this.x && mouseX < (double)(this.x + this.width) && mouseY >= (double)this.y && mouseY < (double)(this.y + 20);
    }

    public void render(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY) {
        if (this.isHovered(mouseX, mouseY)) {
            FwmTheme.hover(ctx, this.x, this.y, this.width, 20);
        }
        if (this.icon != null) {
            ctx.item(this.icon, this.x + this.padding(), this.y + 2);
        }
        ctx.text(font, this.trimmedLabel(font), this.x + this.padding() + this.iconRoom(), this.textY(font), FwmTheme.TEXT, false);
        this.renderControl(ctx, font, mouseX, mouseY);
    }

    public Row withIcon(ItemStack stack) {
        this.icon = stack;
        return this;
    }

    private int iconRoom() {
        return this.icon == null ? 0 : 20;
    }

    protected int textY(Font font) {
        Objects.requireNonNull(font);
        return this.y + (20 - 9) / 2 + 1;
    }

    protected abstract void renderControl(GuiGraphicsExtractor var1, Font var2, int var3, int var4);

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        return false;
    }

    public void mouseReleased() {
    }

    public boolean isCapturingKeys() {
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public boolean charTyped(CharacterEvent input) {
        return false;
    }

    public void commitTyped() {
    }
}

