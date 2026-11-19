/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 */
package net.friendswithmoney.fwm.ui.widget;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.friendswithmoney.fwm.ui.FwmTheme;
import net.friendswithmoney.fwm.ui.widget.Row;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;

public class WordRow
extends Row {
    private static final int MAX_BOX_W = 96;
    private static final int BOX_H = 14;
    private static final int MAX_TYPED = 16;
    private final Supplier<String> getter;
    private final Consumer<String> setter;
    private StringBuilder typed;

    public WordRow(Component label, Supplier<String> getter, Consumer<String> setter) {
        super(label);
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    protected void renderControl(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY) {
        int boxW = Math.min(96, this.controlWidth());
        int bx = this.controlRight() - boxW;
        int by = this.y + 3;
        boolean editing = this.typed != null;
        boolean hovered = mouseX >= bx && mouseX < bx + boxW && mouseY >= by && mouseY < by + 14;
        FwmTheme.box(ctx, bx, by, boxW, 14, FwmTheme.RAISED, editing || hovered ? FwmTheme.ACCENT : FwmTheme.OFF);
        Object shown = editing ? String.valueOf(this.typed) + "_" : this.getter.get();
        MutableComponent text = Component.literal((String)font.plainSubstrByWidth((String)shown, boxW - 6));
        int n = bx + (boxW - font.width((FormattedText)text)) / 2;
        Objects.requireNonNull(font);
        ctx.text(font, (Component)text, n, by + (14 - 9) / 2 + 1, editing || hovered ? FwmTheme.ACCENT : FwmTheme.TEXT, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !this.isHovered(mouseX, mouseY)) {
            this.commit();
            return false;
        }
        this.typed = new StringBuilder();
        return true;
    }

    @Override
    public boolean isCapturingKeys() {
        return this.typed != null;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.typed == null) {
            return false;
        }
        if (keyCode == 256) {
            this.typed = null;
            return true;
        }
        if (keyCode == 257 || keyCode == 335) {
            this.commit();
            return true;
        }
        if (keyCode == 259) {
            if (!this.typed.isEmpty()) {
                this.typed.setLength(this.typed.length() - 1);
            }
            return true;
        }
        if (this.typed.length() >= 16) {
            return true;
        }
        char c = '\u0000';
        if (keyCode >= 65 && keyCode <= 90) {
            c = (char)(65 + keyCode - 65);
        } else if (keyCode >= 48 && keyCode <= 57) {
            c = (char)(48 + keyCode - 48);
        } else if (keyCode >= 320 && keyCode <= 329) {
            c = (char)(48 + keyCode - 320);
        } else if (keyCode == 45) {
            c = (char)((modifiers & 1) != 0 ? 95 : 45);
        }
        if (c != '\u0000') {
            this.typed.append(c);
        }
        return true;
    }

    private void commit() {
        if (this.typed == null) {
            return;
        }
        String text = this.typed.toString();
        this.typed = null;
        if (text.length() >= 2) {
            this.setter.accept(text);
        }
    }
}

