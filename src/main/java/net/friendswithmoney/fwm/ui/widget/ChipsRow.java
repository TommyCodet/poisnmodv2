/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 */
package net.friendswithmoney.fwm.ui.widget;

import java.util.List;
import java.util.Objects;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.friendswithmoney.fwm.ui.FwmTheme;
import net.friendswithmoney.fwm.ui.widget.Row;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;

public class ChipsRow
extends Row {
    private static final int CHIP_H = 13;
    private static final int GAP = 3;
    private final List<Chip> chips;
    private final IntSupplier getter;
    private final IntConsumer setter;
    private int[] drawnX = new int[0];
    private int[] drawnW = new int[0];
    private int drawnY = Integer.MIN_VALUE;

    public ChipsRow(Component label, List<Chip> chips, IntSupplier getter, IntConsumer setter) {
        super(label);
        this.chips = chips;
        this.getter = getter;
        this.setter = setter;
    }

    private int chipW(Font font, Chip chip) {
        return font.width(chip.label()) + 8;
    }

    private int startX(Font font) {
        int total = -3;
        for (Chip chip : this.chips) {
            total += this.chipW(font, chip) + 3;
        }
        return Math.max(this.x + this.padding() + font.width((FormattedText)this.label) + 6, this.controlRight() - total);
    }

    @Override
    protected Component trimmedLabel(Font font) {
        return this.label;
    }

    @Override
    protected void renderControl(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY) {
        int cx = this.startX(font);
        int cy = this.y + 3;
        int current = this.getter.getAsInt();
        if (this.drawnX.length != this.chips.size()) {
            this.drawnX = new int[this.chips.size()];
            this.drawnW = new int[this.chips.size()];
        }
        this.drawnY = cy;
        for (int i = 0; i < this.chips.size(); ++i) {
            Chip chip = this.chips.get(i);
            int w = this.chipW(font, chip);
            this.drawnX[i] = cx;
            this.drawnW[i] = w;
            boolean on = chip.value() == current;
            boolean hovered = mouseX >= cx && mouseX < cx + w && mouseY >= cy && mouseY < cy + 13;
            FwmTheme.box(ctx, cx, cy, w, 13, on ? FwmTheme.ACCENT_DIM : FwmTheme.RAISED, on || hovered ? FwmTheme.ACCENT : FwmTheme.OFF);
            MutableComponent mutableComponent = Component.literal((String)chip.label());
            Objects.requireNonNull(font);
            ctx.text(font, (Component)mutableComponent, cx + 4, cy + (13 - 9) / 2 + 1, on || hovered ? FwmTheme.ACCENT : FwmTheme.TEXT, false);
            cx += w + 3;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !this.isHovered(mouseX, mouseY)) {
            return false;
        }
        Font font = Minecraft.getInstance().font;
        int cy = this.y + 3;
        if (this.drawnY == cy && this.drawnX.length == this.chips.size()) {
            for (int i = 0; i < this.chips.size(); ++i) {
                if (!(mouseX >= (double)this.drawnX[i]) || !(mouseX < (double)(this.drawnX[i] + this.drawnW[i])) || !(mouseY >= (double)cy) || !(mouseY < (double)(cy + 13))) continue;
                this.setter.accept(this.chips.get(i).value());
                return true;
            }
            return false;
        }
        int cx = this.startX(font);
        for (Chip chip : this.chips) {
            int w = this.chipW(font, chip);
            if (mouseX >= (double)cx && mouseX < (double)(cx + w) && mouseY >= (double)cy && mouseY < (double)(cy + 13)) {
                this.setter.accept(chip.value());
                return true;
            }
            cx += w + 3;
        }
        return false;
    }

    public record Chip(String label, int value) {
    }
}

