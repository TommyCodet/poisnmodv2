/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.world.item.ItemStack
 *  org.joml.Matrix3x2fStack
 */
package net.friendswithmoney.fwm.ui.panel;

import net.friendswithmoney.fwm.ui.FwmTheme;
import net.friendswithmoney.fwm.ui.LauncherGfx;
import net.friendswithmoney.fwm.ui.gfx.Shapes;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

public final class DonationDropdown {
    public static final int OUTSIDE = -1;
    public static final int NOTHING = -2;
    public static final int PLAY = 10000;
    private static final int TEXT = -1116943;
    private static final int SUB = -6902107;
    private static final int LIST_FILL = -15653857;
    private static final int LIST_EDGE = 0x2EFFFFFF;
    private static final float ROW = 17.0f;
    private static final float PADDING = 4.0f;
    private static final int MAX_ROWS = 9;
    private int openId = -1;
    private Entries entries;
    private Thumb thumb;
    private int columns = 1;
    private float cellW;
    private float cellH;
    private int selected = -1;
    private float lx;
    private float ly;
    private float lw;
    private float lh;
    private float scroll;
    private float contentH;
    private int hovered = -1;
    private boolean hoverPlay;
    private long openedAt;

    public boolean isOpen() {
        return this.openId >= 0;
    }

    public int openId() {
        return this.openId;
    }

    public int hovered() {
        return this.isOpen() ? this.hovered : -1;
    }

    public void close() {
        this.openId = -1;
        this.hovered = -1;
    }

    public void open(int id, Entries e, int selected, float x, float y, float w, float h, float top, float bottom) {
        this.openGrid(id, e, null, 1, w, 17.0f, selected, x, y, w, h, top, bottom);
    }

    public void openGrid(int id, Entries e, Thumb thumb, int columns, float cellW, float cellH, int selected, float x, float y, float w, float h, float top, float bottom) {
        this.openId = id;
        this.entries = e;
        this.thumb = thumb;
        this.columns = Math.max(1, columns);
        this.cellW = cellW;
        this.cellH = cellH;
        this.selected = selected;
        this.openedAt = System.currentTimeMillis();
        int rows = (e.count() + this.columns - 1) / this.columns;
        this.contentH = (float)rows * cellH;
        float gridW = thumb == null ? w : (float)this.columns * cellW + 8.0f;
        this.lw = Math.max(w, gridW);
        float want = Math.min(this.contentH, thumb == null ? 153.0f : this.contentH) + 8.0f;
        float below = bottom - (y + h + 4.0f);
        float above = y - 4.0f - top;
        if (want <= below) {
            this.lh = want;
            this.ly = y + h + 4.0f;
        } else if (want <= above) {
            this.lh = want;
            this.ly = y - 4.0f - this.lh;
        } else {
            this.lh = Math.min(want, bottom - top);
            this.ly = Math.max(top, bottom - this.lh);
        }
        this.lx = thumb == null ? x : x + w - this.lw;
        float sel = selected < 0 ? 0.0f : (float)(selected / this.columns) * cellH;
        this.scroll = Math.max(0.0f, Math.min(this.maxScroll(), sel - (this.lh - 8.0f) / 2.0f + cellH / 2.0f));
        this.hovered = -1;
    }

    private float maxScroll() {
        return Math.max(0.0f, this.contentH - (this.lh - 8.0f));
    }

    public void scroll(double amount) {
        this.scroll = Math.max(0.0f, Math.min(this.maxScroll(), this.scroll - (float)amount * 17.0f * 1.5f));
    }

    public boolean contains(float mx, float my) {
        return this.isOpen() && mx >= this.lx && mx < this.lx + this.lw && my >= this.ly && my < this.ly + this.lh;
    }

    public void track(float mx, float my) {
        int col;
        this.hovered = -1;
        this.hoverPlay = false;
        if (!this.contains(mx, my)) {
            return;
        }
        float iy = my - (this.ly + 4.0f) + this.scroll;
        float ix = mx - (this.lx + 4.0f);
        if (iy < 0.0f || my > this.ly + this.lh - 4.0f) {
            return;
        }
        int row = (int)(iy / this.cellH);
        int n = col = this.thumb == null ? 0 : (int)(ix / this.cellW);
        if (col < 0 || col >= this.columns) {
            return;
        }
        int i = row * this.columns + col;
        if (i < 0 || i >= this.entries.count()) {
            return;
        }
        this.hovered = i;
        this.hoverPlay = this.thumb == null && this.entries.playable(i) && mx >= this.lx + this.lw - 4.0f - 18.0f;
    }

    public int click(float mx, float my) {
        if (!this.isOpen()) {
            return -1;
        }
        if (!this.contains(mx, my)) {
            this.close();
            return -1;
        }
        this.track(mx, my);
        if (this.hovered < 0) {
            return -2;
        }
        int i = this.hovered;
        if (this.hoverPlay) {
            return 10000 + i;
        }
        this.close();
        return i;
    }

    public static void field(GuiGraphicsExtractor ctx, float x, float y, float w, float h, String value, ItemStack icon, int swatch, boolean hover, boolean open) {
        Shapes.round(ctx, x, y, w, h, 5.0f, open ? 0x22FFFFFF : (hover ? 0x1CFFFFFF : 0x12FFFFFF));
        Shapes.ring(ctx, x, y, w, h, 5.0f, open ? FwmTheme.ACCENT : (hover ? 0x40FFFFFF : 0x1CFFFFFF));
        float tx = x + 7.0f;
        float cy = y + h / 2.0f;
        if (icon != null && !icon.isEmpty()) {
            DonationDropdown.item(ctx, icon, tx - 1.0f, cy - 5.0f, 10.0f);
            tx += 12.0f;
        } else if (swatch != 0) {
            Shapes.circle(ctx, tx + 3.0f, cy, 3.5f, swatch);
            tx += 11.0f;
        }
        float room = x + w - 16.0f - tx;
        LauncherGfx.text(ctx, LauncherGfx.fit(value, 7.0f, false, room), tx, cy - 3.5f, -1116943, 7.0f, false);
        Shapes.icon(ctx, open ? "chev_up" : "chev_down", x + w - 13.0f, cy - 4.0f, 8.0f, open ? FwmTheme.ACCENT : -6902107);
    }

    public static void item(GuiGraphicsExtractor ctx, ItemStack stack, float x, float y, float size) {
        Matrix3x2fStack m = ctx.pose();
        m.pushMatrix();
        m.translate(x, y);
        m.scale(size / 16.0f, size / 16.0f);
        ctx.item(stack, 0, 0);
        m.popMatrix();
    }

    public void render(GuiGraphicsExtractor ctx) {
        int first;
        if (!this.isOpen()) {
            return;
        }
        ctx.nextStratum();
        float t = Math.min(1.0f, (float)(System.currentTimeMillis() - this.openedAt) / 120.0f);
        float ease = 1.0f - (1.0f - t) * (1.0f - t);
        float rise = (1.0f - ease) * 4.0f;
        Matrix3x2fStack m = ctx.pose();
        m.pushMatrix();
        m.translate(0.0f, -rise);
        Shapes.round(ctx, this.lx, this.ly, this.lw, this.lh, 8.0f, -15653857);
        Shapes.ring(ctx, this.lx, this.ly, this.lw, this.lh, 8.0f, 0x2EFFFFFF);
        ctx.enableScissor((int)Math.floor(this.lx), (int)Math.floor(this.ly + 2.0f), (int)Math.ceil(this.lx + this.lw), (int)Math.ceil(this.ly + this.lh - 2.0f));
        int n = this.entries.count();
        for (int i = first = Math.max(0, (int)(this.scroll / this.cellH) * this.columns); i < n; ++i) {
            int row = i / this.columns;
            int col = i % this.columns;
            float cx = this.lx + 4.0f + (float)col * this.cellW;
            float cy = this.ly + 4.0f + (float)row * this.cellH - this.scroll;
            if (cy > this.ly + this.lh) break;
            if (this.thumb != null) {
                this.gridCell(ctx, i, cx, cy);
                continue;
            }
            this.listRow(ctx, i, cx, cy, this.lw - 8.0f);
        }
        ctx.disableScissor();
        float max = this.maxScroll();
        if (max > 0.0f) {
            float track = this.lh - 12.0f;
            float bar = Math.max(14.0f, track * (this.lh - 8.0f) / this.contentH);
            float by = this.ly + 6.0f + (track - bar) * (this.scroll / max);
            Shapes.round(ctx, this.lx + this.lw - 4.0f, by, 2.0f, bar, 1.0f, 0x40FFFFFF);
        }
        m.popMatrix();
    }

    private void listRow(GuiGraphicsExtractor ctx, int i, float x, float y, float w) {
        boolean sel = i == this.selected;
        boolean ov = i == this.hovered;
        float h = 16.0f;
        if (ov) {
            Shapes.round(ctx, x, y, w, h, 5.0f, 0x18FFFFFF);
        } else if (sel) {
            Shapes.round(ctx, x, y, w, h, 5.0f, 0xEFFFFFF);
        }
        float tx = x + 6.0f;
        float cy = y + h / 2.0f;
        ItemStack icon = this.entries.icon(i);
        int swatch = this.entries.swatch(i);
        if (icon != null && !icon.isEmpty()) {
            DonationDropdown.item(ctx, icon, tx - 1.0f, cy - 5.0f, 10.0f);
            tx += 13.0f;
        } else if (swatch != 0) {
            Shapes.circle(ctx, tx + 3.0f, cy, 3.5f, swatch);
            tx += 11.0f;
        }
        boolean play = this.entries.playable(i);
        float right = x + w - 6.0f - (float)(play ? 18 : 0) - (float)(sel ? 12 : 0);
        String detail = this.entries.detail(i);
        if (detail != null) {
            float dw = LauncherGfx.width(detail, 6.5f, false);
            LauncherGfx.text(ctx, detail, right - dw, cy - 3.5f, -6902107, 6.5f, false);
            right -= dw + 6.0f;
        }
        LauncherGfx.text(ctx, LauncherGfx.fit(this.entries.label(i), 7.0f, sel, right - tx), tx, cy - 3.5f, sel ? FwmTheme.ACCENT : -1116943, 7.0f, sel);
        if (sel) {
            Shapes.icon(ctx, "check", x + w - 6.0f - (float)(play ? 18 : 0) - 9.0f, cy - 4.0f, 8.0f, FwmTheme.ACCENT);
        }
        if (play) {
            boolean ovPlay = ov && this.hoverPlay;
            float bx = x + w - 17.0f;
            Shapes.circle(ctx, bx + 7.0f, cy, 6.5f, ovPlay ? FwmTheme.ACCENT : 0x1FFFFFFF);
            Shapes.icon(ctx, "play", bx + 3.5f, cy - 3.5f, 7.0f, ovPlay ? FwmTheme.TEXT_ON_ACCENT : -1116943);
        }
    }

    private void gridCell(GuiGraphicsExtractor ctx, int i, float x, float y) {
        String label;
        boolean sel = i == this.selected;
        boolean ov = i == this.hovered;
        float w = this.cellW - 4.0f;
        float h = this.cellH - 4.0f;
        Shapes.round(ctx, x, y, w, h, 6.0f, ov ? 0x1CFFFFFF : (sel ? 0x12FFFFFF : 0x8FFFFFF));
        if (sel || ov) {
            Shapes.ring(ctx, x, y, w, h, 6.0f, sel ? FwmTheme.ACCENT : 0x40FFFFFF);
        }
        float pad = 4.0f;
        float pw = w - pad * 2.0f;
        float ph = Math.min(h - 22.0f, pw * 9.0f / 16.0f);
        this.thumb.draw(ctx, i, x + pad, y + pad, pw, ph, ov);
        float ty = y + pad + ph + 4.0f;
        String detail = this.entries.detail(i);
        if (detail != null) {
            float dw = LauncherGfx.width(detail, 6.0f, false);
            float bx = x + pad + pw - dw - 6.0f;
            float by = y + pad + ph - 10.0f;
            Shapes.round(ctx, bx, by, dw + 4.0f, 8.0f, 3.0f, -1342177280);
            LauncherGfx.text(ctx, detail, bx + 2.0f, by + 1.0f, -1642774, 6.0f, false);
        }
        float size = LauncherGfx.width(label = this.entries.label(i), 7.0f, true) <= pw ? 7.0f : 6.5f;
        LauncherGfx.text(ctx, LauncherGfx.fit(label, size, true, pw), x + pad, ty + (7.0f - size) / 2.0f, sel ? FwmTheme.ACCENT : -1116943, size, true);
    }

    public static interface Entries {
        public int count();

        public String label(int var1);

        default public ItemStack icon(int i) {
            return null;
        }

        default public int swatch(int i) {
            return 0;
        }

        default public String detail(int i) {
            return null;
        }

        default public boolean playable(int i) {
            return false;
        }
    }

    public static interface Thumb {
        public void draw(GuiGraphicsExtractor var1, int var2, float var3, float var4, float var5, float var6, boolean var7);
    }
}

