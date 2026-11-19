/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.network.chat.Component
 */
package net.friendswithmoney.fwm.ui.widget;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.friendswithmoney.fwm.ui.FwmTheme;
import net.friendswithmoney.fwm.ui.widget.Row;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class HexRow
extends Row {
    private static final int BOX_W = 64;
    private static final int BOX_H = 14;
    private final IntSupplier getter;
    private final IntConsumer setter;
    private StringBuilder typed;
    private final Runnable onSwatch;
    private final Runnable onRemove;

    public HexRow(Component label, IntSupplier getter, IntConsumer setter) {
        this(label, getter, setter, null);
    }

    public HexRow(Component label, IntSupplier getter, IntConsumer setter, Runnable onSwatch) {
        this(label, getter, setter, onSwatch, null);
    }

    public HexRow(Component label, IntSupplier getter, IntConsumer setter, Runnable onSwatch, Runnable onRemove) {
        super(label);
        this.getter = getter;
        this.setter = setter;
        this.onSwatch = onSwatch;
        this.onRemove = onRemove;
    }

    @Override
    protected void renderControl(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY) {
        int bx = this.controlRight() - 64;
        int by = this.y + 3;
        boolean hovered = mouseX >= bx - 16 && mouseX < bx + 64 && mouseY >= by && mouseY < by + 14;
        int rgb = this.getter.getAsInt() & 0xFFFFFF;
        ctx.fill(bx - 16, by + 1, bx - 4, by + 14 - 1, 0xFF000000 | rgb);
        FwmTheme.box(ctx, bx, by, 64, 14, FwmTheme.RAISED, this.typed != null || hovered ? FwmTheme.ACCENT : FwmTheme.OFF);
        if (this.onRemove != null) {
            int rx = bx - 30;
            boolean overX = mouseX >= rx && mouseX < rx + 11 && mouseY >= by && mouseY < by + 14;
            ctx.text(font, (Component)Component.literal((String)"\u00d7"), rx + 3, by + 3, overX ? -1017230 : -7362144, false);
        }
        String shown = this.typed != null ? "#" + String.valueOf(this.typed) + "_" : String.format("#%06X", rgb);
        ctx.text(font, (Component)Component.literal((String)shown), bx + (64 - font.width(shown)) / 2, by + 4, this.typed != null ? FwmTheme.ACCENT : FwmTheme.TEXT, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int bx = this.controlRight() - 64;
        int by = this.y + 3;
        if (button == 0 && this.onRemove != null && mouseX >= (double)(bx - 30) && mouseX < (double)(bx - 19) && mouseY >= (double)by && mouseY < (double)(by + 14)) {
            this.typed = null;
            this.onRemove.run();
            return true;
        }
        if (button == 0 && this.onSwatch != null && mouseX >= (double)(bx - 16) && mouseX < (double)(bx - 2) && mouseY >= (double)by && mouseY < (double)(by + 14)) {
            this.commit();
            this.onSwatch.run();
            return true;
        }
        if (button == 0 && mouseX >= (double)(bx - 16) && mouseX < (double)(bx + 64) && mouseY >= (double)by && mouseY < (double)(by + 14)) {
            this.typed = new StringBuilder();
            return true;
        }
        this.commit();
        return false;
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
        if (keyCode == 257 || keyCode == 335 || keyCode == 258) {
            this.commit();
            return true;
        }
        if (keyCode == 259) {
            if (!this.typed.isEmpty()) {
                this.typed.setLength(this.typed.length() - 1);
            }
            return true;
        }
        if (this.typed.length() >= 6) {
            return true;
        }
        char c = '\u0000';
        if (keyCode >= 48 && keyCode <= 57) {
            c = (char)(48 + keyCode - 48);
        } else if (keyCode >= 320 && keyCode <= 329) {
            c = (char)(48 + keyCode - 320);
        } else if (keyCode >= 65 && keyCode <= 70) {
            c = (char)(65 + keyCode - 65);
        }
        if (c != '\u0000') {
            this.typed.append(c);
        }
        return true;
    }

    @Override
    public void commitTyped() {
        this.commit();
    }

    private void commit() {
        if (this.typed == null) {
            return;
        }
        Object hex = this.typed.toString();
        this.typed = null;
        if (((String)hex).length() == 3) {
            hex = "" + ((String)hex).charAt(0) + ((String)hex).charAt(0) + ((String)hex).charAt(1) + ((String)hex).charAt(1) + ((String)hex).charAt(2) + ((String)hex).charAt(2);
        }
        if (((String)hex).length() == 6) {
            this.setter.accept(Integer.parseInt((String)hex, 16));
        }
    }
}

