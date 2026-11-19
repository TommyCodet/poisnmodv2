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
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import net.friendswithmoney.fwm.auction.Money;
import net.friendswithmoney.fwm.ui.FwmTheme;
import net.friendswithmoney.fwm.ui.widget.Row;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;

public class NumberRow
extends Row {
    private static final int MAX_BOX_W = 96;
    private static final int BOX_H = 14;
    private static final int MAX_TYPED = 13;
    private final LongSupplier getter;
    private final LongConsumer setter;
    private final Formatter display;
    private StringBuilder typed;
    private final Parser parser;

    public NumberRow(Component label, LongSupplier getter, LongConsumer setter, Formatter display) {
        this(label, getter, setter, display, typed -> Money.cents(typed) / 100L);
    }

    public NumberRow(Component label, LongSupplier getter, LongConsumer setter, Formatter display, Parser parser) {
        super(label);
        this.getter = getter;
        this.setter = setter;
        this.display = display;
        this.parser = parser;
    }

    @Override
    protected void renderControl(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY) {
        int boxW = Math.min(96, this.controlWidth());
        int bx = this.controlRight() - boxW;
        int by = this.y + 3;
        boolean editing = this.typed != null;
        boolean hovered = mouseX >= bx && mouseX < bx + boxW && mouseY >= by && mouseY < by + 14;
        FwmTheme.box(ctx, bx, by, boxW, 14, FwmTheme.RAISED, editing || hovered ? FwmTheme.ACCENT : FwmTheme.OFF);
        String shown = editing ? String.valueOf(this.typed) + "_" : this.display.format(this.getter.getAsLong()).getString();
        MutableComponent text = Component.literal((String)font.plainSubstrByWidth(shown, boxW - 6));
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
    public void commitTyped() {
        this.commit();
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
        if (this.typed.length() >= 13) {
            return true;
        }
        int digit = NumberRow.digit(keyCode);
        if (digit >= 0) {
            if (digit != 0 || !this.typed.isEmpty()) {
                this.typed.append((char)(48 + digit));
            }
            return true;
        }
        char unit = NumberRow.unit(keyCode);
        if (unit != '\u0000' && !this.typed.isEmpty() && Character.isDigit(this.typed.charAt(this.typed.length() - 1))) {
            this.typed.append(unit);
        }
        return true;
    }

    private static int digit(int keyCode) {
        if (keyCode >= 48 && keyCode <= 57) {
            return keyCode - 48;
        }
        if (keyCode >= 320 && keyCode <= 329) {
            return keyCode - 320;
        }
        return -1;
    }

    private static char unit(int keyCode) {
        return switch (keyCode) {
            case 75 -> 'k';
            case 77 -> 'm';
            case 66 -> 'b';
            default -> '\u0000';
        };
    }

    private void commit() {
        if (this.typed == null) {
            return;
        }
        String text = this.typed.toString();
        this.typed = null;
        if (text.isEmpty()) {
            return;
        }
        this.setter.accept(this.parser.parse(text));
    }

    public static interface Parser {
        public long parse(String var1);
    }

    public static interface Formatter {
        public Component format(long var1);
    }
}

