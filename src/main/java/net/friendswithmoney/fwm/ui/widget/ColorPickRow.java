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
import net.friendswithmoney.fwm.ui.LauncherGfx;
import net.friendswithmoney.fwm.ui.gfx.Shapes;
import net.friendswithmoney.fwm.ui.widget.Row;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class ColorPickRow
extends Row {
    private final boolean shade;
    private final IntSupplier hue;
    private final IntSupplier level;
    private final IntConsumer setValue;
    private final Runnable apply;
    private boolean dragging;

    public ColorPickRow(Component label, boolean shade, IntSupplier hue, IntSupplier level, IntConsumer setValue, Runnable apply) {
        super(label);
        this.shade = shade;
        this.hue = hue;
        this.level = level;
        this.setValue = setValue;
        this.apply = apply;
    }

    public static int color(int hue, int shade) {
        float b;
        float g;
        float r;
        float h = (float)((hue % 360 + 360) % 360) / 60.0f;
        float c = 1.0f;
        float x = c * (1.0f - Math.abs(h % 2.0f - 1.0f));
        if (h < 1.0f) {
            r = c;
            g = x;
            b = 0.0f;
        } else if (h < 2.0f) {
            r = x;
            g = c;
            b = 0.0f;
        } else if (h < 3.0f) {
            r = 0.0f;
            g = c;
            b = x;
        } else if (h < 4.0f) {
            r = 0.0f;
            g = x;
            b = c;
        } else if (h < 5.0f) {
            r = x;
            g = 0.0f;
            b = c;
        } else {
            r = c;
            g = 0.0f;
            b = x;
        }
        float t = (float)shade / 50.0f;
        if (t <= 1.0f) {
            r *= t;
            g *= t;
            b *= t;
        } else {
            float w = t - 1.0f;
            r += (1.0f - r) * w;
            g += (1.0f - g) * w;
            b += (1.0f - b) * w;
        }
        return 0xFF000000 | Math.round(r * 255.0f) << 16 | Math.round(g * 255.0f) << 8 | Math.round(b * 255.0f);
    }

    private int barX() {
        return this.x + this.padding() + 4;
    }

    private int barW() {
        return Math.max(40, this.width - 2 * this.padding() - 8 - (this.apply != null ? 22 : 0));
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY) {
        this.renderControl(ctx, font, mouseX, mouseY);
    }

    @Override
    protected void renderControl(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY) {
        int bx = this.barX();
        int bw = this.barW();
        int by = this.y + 5;
        int bh = 10;
        int max = this.shade ? 100 : 359;
        for (int i = 0; i < bw; ++i) {
            int v = Math.round((float)(i * max) / (float)Math.max(1, bw - 1));
            int c = this.shade ? ColorPickRow.color(this.hue.getAsInt(), v) : ColorPickRow.color(v, 50);
            ctx.fill(bx + i, by, bx + i + 1, by + bh, c);
        }
        LauncherGfx.ring(ctx, bx - 1, by - 1, bw + 2, bh + 2, 2, 0x55FFFFFF);
        int kx = bx + Math.round((float)(this.level.getAsInt() * (bw - 1)) / (float)max);
        Shapes.round(ctx, (float)kx - 1.5f, (float)by - 2.5f, 3.5f, bh + 5, 1.75f, 0x66000000);
        Shapes.round(ctx, (float)kx - 1.0f, (float)by - 2.0f, 2.5f, bh + 4, 1.25f, -1);
        if (this.apply != null) {
            int tx = bx + bw + 6;
            int ty = this.y + 2;
            int ts = 16;
            boolean over = mouseX >= tx && mouseX < tx + ts && mouseY >= ty && mouseY < ty + ts;
            LauncherGfx.round(ctx, tx, ty, ts, ts, 4, over ? -9703282 : -12136081);
            int cx = tx + ts / 2 - 4;
            int cy = ty + ts / 2;
            int col = -16045545;
            Shapes.icon(ctx, "check", cx - 1, cy - 5, 10.0f, col);
        }
    }

    private void pick(double mouseX) {
        int max = this.shade ? 100 : 359;
        double t = (mouseX - (double)this.barX()) / (double)Math.max(1, this.barW() - 1);
        this.setValue.accept((int)Math.round(Math.max(0.0, Math.min(1.0, t)) * (double)max));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int tx;
        if (button != 0 || !this.isHovered(mouseX, mouseY)) {
            return false;
        }
        if (this.apply != null && mouseX >= (double)(tx = this.barX() + this.barW() + 6) && mouseX < (double)(tx + 20 - 4)) {
            this.apply.run();
            return true;
        }
        if (mouseX >= (double)(this.barX() - 2) && mouseX < (double)(this.barX() + this.barW() + 2)) {
            this.dragging = true;
            this.pick(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        if (!this.dragging) {
            return false;
        }
        this.pick(mouseX);
        return true;
    }

    @Override
    public void mouseReleased() {
        this.dragging = false;
    }
}

