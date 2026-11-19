/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.network.chat.Component
 */
package net.friendswithmoney.fwm.ui.widget;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import net.friendswithmoney.fwm.config.ConfigManager;
import net.friendswithmoney.fwm.ui.FwmTheme;
import net.friendswithmoney.fwm.ui.gfx.Shapes;
import net.friendswithmoney.fwm.ui.widget.Row;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class ColorRow
extends Row {
    private static final int SWATCH = 12;
    private static final int GAP = 4;
    private static final int TRACK_H = 8;
    private static final int KNOB_W = 5;
    private static final int KNOB_H = 12;
    private static final int MIN_TRACK_W = 30;
    private final DoubleSupplier getter;
    private final DoubleConsumer setter;
    private final Gradient gradient;
    private final DoubleSupplier fallback;
    private final IntSupplier swatch;
    private final Runnable reset;
    private boolean dragging;

    public ColorRow(Component label, DoubleSupplier getter, DoubleConsumer setter, Gradient gradient, DoubleSupplier fallback, IntSupplier swatch, Runnable reset) {
        super(label);
        this.getter = getter;
        this.setter = setter;
        this.gradient = gradient;
        this.fallback = fallback;
        this.swatch = swatch;
        this.reset = reset;
    }

    private boolean hasSwatch() {
        return this.reset != null && this.swatch != null;
    }

    private int swatchX() {
        return this.controlX();
    }

    private int trackX() {
        return this.controlX() + (this.hasSwatch() ? 16 : 0);
    }

    private int trackWidth() {
        return Math.max(30, this.controlRight() - this.trackX());
    }

    private float shown() {
        double value = this.getter.getAsDouble();
        if (value < 0.0 && this.fallback != null) {
            value = this.fallback.getAsDouble();
        }
        return (float)Math.max(0.0, Math.min(1.0, value));
    }

    @Override
    protected void renderControl(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY) {
        int tx = this.trackX();
        int tw = this.trackWidth();
        int ty = this.y + 6;
        for (int i = 0; i < tw; ++i) {
            ctx.fill(tx + i, ty, tx + i + 1, ty + 8, 0xFF000000 | this.gradient.at((float)i / (float)tw));
        }
        int knobX = tx + Math.round(this.shown() * (float)tw) - 2;
        float kc = (float)tx + this.shown() * (float)tw;
        float kcy = (float)this.y + 10.0f;
        Shapes.circle(ctx, kc, kcy + 0.4f, 5.0f, 0x55000000);
        Shapes.circle(ctx, kc, kcy, 4.6f, -1);
        Shapes.circle(ctx, kc, kcy, 2.6f, 0xFF000000 | this.gradient.at(this.shown()));
        if (!this.hasSwatch()) {
            return;
        }
        boolean hovered = mouseX >= this.swatchX() && mouseX < this.swatchX() + 12 && mouseY >= this.y + 4 && mouseY < this.y + 16;
        FwmTheme.box(ctx, this.swatchX(), this.y + 4, 12, 12, 0xFF000000 | this.swatch.getAsInt() & 0xFFFFFF, hovered ? FwmTheme.TEXT : FwmTheme.OFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !this.isHovered(mouseX, mouseY)) {
            return false;
        }
        if (this.hasSwatch() && mouseX >= (double)this.swatchX() && mouseX < (double)(this.swatchX() + 12)) {
            this.reset.run();
            ConfigManager.save();
            return true;
        }
        if (mouseX < (double)(this.trackX() - 5)) {
            return false;
        }
        this.dragging = true;
        this.applyFromMouse(mouseX);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        if (!this.dragging) {
            return false;
        }
        this.applyFromMouse(mouseX);
        return true;
    }

    @Override
    public void mouseReleased() {
        if (!this.dragging) {
            return;
        }
        this.dragging = false;
        ConfigManager.save();
    }

    private void applyFromMouse(double mouseX) {
        double progress = (mouseX - (double)this.trackX()) / (double)this.trackWidth();
        this.setter.accept(Math.max(0.0, Math.min(1.0, progress)));
    }

    public static interface Gradient {
        public int at(float var1);
    }
}

