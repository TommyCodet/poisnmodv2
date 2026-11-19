/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 */
package net.friendswithmoney.fwm.ui.widget;

import java.util.List;
import java.util.Objects;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.ui.FwmTheme;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.gfx.Shapes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;

public class TabStrip {
    public static final int HEIGHT = 13;
    private final List<String> labelKeys;
    private int active;
    private int x;
    private int y;
    private int width;
    private float marker = -1.0f;
    private long markerAt;

    public TabStrip(List<String> labelKeys) {
        this.labelKeys = labelKeys;
    }

    public void setBounds(int x, int y, int width) {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    public int active() {
        return this.active;
    }

    public void setActive(int index) {
        if (index >= 0 && index < this.labelKeys.size()) {
            this.active = index;
        }
    }

    public int size() {
        return this.labelKeys.size();
    }

    private int slotW() {
        return Math.max(1, this.width / this.labelKeys.size());
    }

    private int slotX(int slot) {
        return this.x + slot * this.slotW();
    }

    public int indexAt(double mouseX, double mouseY) {
        if (mouseY < (double)this.y || mouseY >= (double)(this.y + 13)) {
            return -1;
        }
        for (int i = 0; i < this.labelKeys.size(); ++i) {
            if (!(mouseX >= (double)this.slotX(i)) || !(mouseX < (double)(this.slotX(i) + this.slotW()))) continue;
            return i;
        }
        return -1;
    }

    public void render(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY) {
        int hovered = this.indexAt(mouseX, mouseY);
        int n = this.labelKeys.size();
        if (n == 0) {
            return;
        }
        Shapes.round(ctx, this.x, this.y - 1, this.slotW() * n, 15.0f, 6.0f, 0x10FFFFFF);
        Shapes.ring(ctx, this.x, this.y - 1, this.slotW() * n, 15.0f, 6.0f, 0x18FFFFFF);
        long now = System.currentTimeMillis();
        float dt = this.markerAt == 0L ? 1.0f : Math.min(0.1f, (float)(now - this.markerAt) / 1000.0f);
        this.markerAt = now;
        this.marker = this.marker < 0.0f || V2.INSTANCE.animations == 0 ? (float)this.active : (this.marker += ((float)this.active - this.marker) * (1.0f - (float)Math.exp(-dt * 20.0f)));
        if (Math.abs((float)this.active - this.marker) < 0.005f) {
            this.marker = this.active;
        }
        if (hovered >= 0 && hovered != this.active) {
            Shapes.round(ctx, (float)this.slotX(hovered) + 1.5f, (float)this.y + 0.5f, this.slotW() - 3, 12.0f, 5.0f, 0x12FFFFFF);
        }
        Shapes.round(ctx, (float)this.x + this.marker * (float)this.slotW() + 1.5f, (float)this.y + 0.5f, this.slotW() - 3, 12.0f, 5.0f, -8523578);
        for (int i = 0; i < n; ++i) {
            boolean selected = i == this.active;
            int sx = this.slotX(i);
            int sw = this.slotW() - 2;
            String label = font.plainSubstrByWidth(Lang.str(this.labelKeys.get(i), new Object[0]), sw - 4);
            MutableComponent t = selected ? Component.literal((String)label).withStyle(ChatFormatting.BOLD) : Component.literal((String)label);
            int n2 = sx + 1 + (sw - font.width((FormattedText)t)) / 2;
            Objects.requireNonNull(font);
            ctx.text(font, (Component)t, n2, this.y + (13 - 9) / 2 + 1, selected ? -16045536 : (i == hovered ? FwmTheme.TEXT : FwmTheme.TEXT_DIM), false);
        }
    }
}

