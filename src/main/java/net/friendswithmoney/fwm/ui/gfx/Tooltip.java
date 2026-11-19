/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.util.FormattedCharSequence
 */
package net.friendswithmoney.fwm.ui.gfx;

import java.util.List;
import java.util.Objects;
import net.friendswithmoney.fwm.ui.gfx.Shapes;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

public final class Tooltip {
    public static final int MAX_WIDTH = 200;

    private Tooltip() {
    }

    public static void draw(GuiGraphicsExtractor ctx, Font font, String text, int mouseX, int mouseY) {
        Tooltip.draw(ctx, font, (Component)Component.literal((String)text), mouseX, mouseY);
    }

    public static void draw(GuiGraphicsExtractor ctx, Font font, Component text, int mouseX, int mouseY) {
        int ty;
        List lines = font.split((FormattedText)text, 200);
        if (lines.isEmpty()) {
            return;
        }
        int w = 0;
        for (FormattedCharSequence l : lines) {
            w = Math.max(w, font.width(l));
        }
        Objects.requireNonNull(font);
        int lineH = 9 + 1;
        int bw = w + 12;
        int bh = lines.size() * lineH + 8;
        int screenW = ctx.guiWidth();
        int screenH = ctx.guiHeight();
        int tx = mouseX + 10;
        if (tx + bw > screenW - 2) {
            tx = Math.max(2, mouseX - 8 - bw);
        }
        if ((ty = mouseY - bh - 4) < 2) {
            ty = Math.min(screenH - bh - 2, mouseY + 14);
        }
        ctx.nextStratum();
        Tooltip.box(ctx, tx, ty, bw, bh);
        int y = ty + 5;
        for (FormattedCharSequence l : lines) {
            ctx.text(font, l, tx + 6, y, -1116943, false);
            y += lineH;
        }
    }

    public static void box(GuiGraphicsExtractor ctx, int x, int y, int w, int h) {
        Shapes.shadow(ctx, x, y, w, h, 6.0f, 4.0f, 0x55000000);
        Shapes.round(ctx, x, y, w, h, 6.0f, -233824741);
        Shapes.ring(ctx, x, y, w, h, 6.0f, 1081995462);
        Shapes.round(ctx, x + 6, (float)y + 0.5f, w - 12, 0.6f, 0.3f, 1434316998);
    }
}

