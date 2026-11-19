/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package net.friendswithmoney.fwm.ui.panel;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import net.friendswithmoney.fwm.ui.ConfirmBox;
import net.friendswithmoney.fwm.ui.LauncherGfx;
import net.friendswithmoney.fwm.ui.gfx.Shapes;
import net.friendswithmoney.fwm.ui.panel.Panel;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public abstract class StudioBase
extends Panel {
    protected static final int MINT = -8523578;
    protected static final int MINT_HI = -6555690;
    protected static final int INK = -16045536;
    protected static final int WHITE = -1;
    protected static final int DIM = -7362144;
    protected static final int GOLD = -670398;
    protected static final int LINE = 0x14FFFFFF;
    protected static final int CARD = 0x24000000;
    protected final List<Hit> hits = new ArrayList<Hit>();
    protected final ConfirmBox confirm = new ConfirmBox();
    private String note = "";
    private long noteAt;

    protected static void r(GuiGraphicsExtractor ctx, float x, float y, float w, float h, float rad, int c) {
        Shapes.round(ctx, x, y, w, h, rad, c);
    }

    protected static void ring(GuiGraphicsExtractor ctx, float x, float y, float w, float h, float rad, int c) {
        Shapes.ring(ctx, x, y, w, h, rad, c);
    }

    protected static void t(GuiGraphicsExtractor ctx, String s, float x, float y, int c, float size, boolean bold) {
        LauncherGfx.text(ctx, s, x, y, c, size, bold);
    }

    protected static void tc(GuiGraphicsExtractor ctx, String s, float x, float cy, int c, float size, boolean bold) {
        LauncherGfx.text(ctx, s, x, cy - size * 0.52f, c, size, bold);
    }

    protected static float tw(String s, float size, boolean bold) {
        return LauncherGfx.width(s, size, bold);
    }

    protected static String fit(String s, float size, boolean bold, float w) {
        return LauncherGfx.fit(s, size, bold, w);
    }

    protected static boolean in(double mx, double my, float x, float y, float w, float h) {
        return mx >= (double)x && mx < (double)(x + w) && my >= (double)y && my < (double)(y + h);
    }

    protected static void head(GuiGraphicsExtractor ctx, String s, float x, float y) {
        StudioBase.t(ctx, s, x, y, -8523578, 6.2f, true);
    }

    protected static void card(GuiGraphicsExtractor ctx, float x, float y, float w, float h) {
        StudioBase.r(ctx, x, y, w, h, 7.0f, 0x24000000);
        StudioBase.ring(ctx, x, y, w, h, 7.0f, 0x14FFFFFF);
    }

    protected float primary(GuiGraphicsExtractor ctx, String label, float x, float y, float w, float h, int mx, int my, boolean enabled, Runnable action) {
        boolean over;
        if (w <= 0.0f) {
            w = StudioBase.tw(label, 7.2f, true) + 22.0f;
        }
        boolean bl = over = enabled && StudioBase.in(mx, my, x, y, w, h);
        StudioBase.r(ctx, x, y, w, h, 6.0f, enabled ? (over ? -6555690 : -8523578) : 0x1CFFFFFF);
        StudioBase.tc(ctx, label, x + (w - StudioBase.tw(label, 7.2f, true)) / 2.0f, y + h / 2.0f, enabled ? -16045536 : -7362144, 7.2f, true);
        if (enabled) {
            this.hits.add(new Hit(x, y, w, h, action));
        }
        return w;
    }

    protected float ghost(GuiGraphicsExtractor ctx, String label, float x, float y, float w, float h, int mx, int my, Runnable action) {
        boolean over;
        if (w <= 0.0f) {
            w = StudioBase.tw(label, 7.0f, true) + 20.0f;
        }
        StudioBase.r(ctx, x, y, w, h, 6.0f, (over = StudioBase.in(mx, my, x, y, w, h)) ? 0x2CFFFFFF : 0x16FFFFFF);
        StudioBase.ring(ctx, x, y, w, h, 6.0f, over ? 1719529670 : 0x20FFFFFF);
        StudioBase.tc(ctx, label, x + (w - StudioBase.tw(label, 7.0f, true)) / 2.0f, y + h / 2.0f, -1, 7.0f, true);
        this.hits.add(new Hit(x, y, w, h, action));
        return w;
    }

    protected void toggleRow(GuiGraphicsExtractor ctx, String label, float x, float y, float w, boolean on, Runnable flip) {
        StudioBase.tc(ctx, label, x, y + 9.0f, -1, 7.2f, true);
        LauncherGfx.toggle(ctx, x + w - 22.0f, y + 3.0f, on ? 1.0f : 0.0f, -8523578, -12957884);
        StudioBase.r(ctx, x, y + 18.0f, w, 0.6f, 0.0f, 0x14FFFFFF);
        this.hits.add(new Hit(x, y, w, 18.0f, flip));
    }

    protected float chips(GuiGraphicsExtractor ctx, String[] labels, int chosen, float x, float y, float h, int mx, int my, IntConsumer onPick) {
        float total = 4.0f;
        float[] ws = new float[labels.length];
        for (int i = 0; i < labels.length; ++i) {
            ws[i] = StudioBase.tw(labels[i], 6.8f, true) + 16.0f;
            total += ws[i];
        }
        StudioBase.r(ctx, x, y, total, h, 6.0f, 0x30000000);
        StudioBase.ring(ctx, x, y, total, h, 6.0f, 0x14FFFFFF);
        float cx = x + 2.0f;
        for (int i = 0; i < labels.length; ++i) {
            boolean over;
            boolean sel = i == chosen;
            boolean bl = over = !sel && StudioBase.in(mx, my, cx, y + 2.0f, ws[i], h - 4.0f);
            if (sel) {
                StudioBase.r(ctx, cx, y + 2.0f, ws[i], h - 4.0f, 5.0f, -13874621);
                StudioBase.ring(ctx, cx, y + 2.0f, ws[i], h - 4.0f, 5.0f, 0x30FFFFFF);
            } else if (over) {
                StudioBase.r(ctx, cx, y + 2.0f, ws[i], h - 4.0f, 5.0f, 0x14FFFFFF);
            }
            StudioBase.tc(ctx, labels[i], cx + 8.0f, y + h / 2.0f, sel ? -1 : -7362144, 6.8f, true);
            int idx = i;
            this.hits.add(new Hit(cx, y + 2.0f, ws[i], h - 4.0f, () -> onPick.accept(idx)));
            cx += ws[i];
        }
        return total;
    }

    protected static void checker(GuiGraphicsExtractor ctx, float x, float y, float w, float h, float cell) {
        StudioBase.r(ctx, x, y, w, h, 0.0f, -7431529);
        int cols = (int)Math.ceil(w / cell);
        int rows = (int)Math.ceil(h / cell);
        for (int j = 0; j < rows; ++j) {
            for (int i = j & 1; i < cols; i += 2) {
                float cw = Math.min(cell, x + w - (x + (float)i * cell));
                float ch = Math.min(cell, y + h - (y + (float)j * cell));
                if (!(cw > 0.0f) || !(ch > 0.0f)) continue;
                StudioBase.r(ctx, x + (float)i * cell, y + (float)j * cell, cw, ch, 0.0f, -8615803);
            }
        }
    }

    protected void say(String text) {
        this.note = text;
        this.noteAt = System.currentTimeMillis();
    }

    protected void drawNote(GuiGraphicsExtractor ctx, float cx, float y) {
        if (this.note.isEmpty() || System.currentTimeMillis() - this.noteAt > 4500L) {
            return;
        }
        float w = StudioBase.tw(this.note, 6.6f, false) + 18.0f;
        StudioBase.r(ctx, cx - w / 2.0f, y, w, 15.0f, 7.0f, -435149792);
        StudioBase.ring(ctx, cx - w / 2.0f, y, w, 15.0f, 7.0f, 1434316998);
        StudioBase.tc(ctx, this.note, cx - w / 2.0f + 9.0f, y + 7.5f, -2562078, 6.6f, false);
    }

    @Override
    public final void render(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY, float delta) {
        this.hits.clear();
        int mx = this.confirm.open() ? -1000 : mouseX;
        int my = this.confirm.open() ? -1000 : mouseY;
        this.draw(ctx, font, mx, my, delta);
        this.confirm.render(ctx, font, this.x, this.y, this.width, this.height, mouseX, mouseY);
    }

    protected abstract void draw(GuiGraphicsExtractor var1, Font var2, int var3, int var4, float var5);

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.confirm.open()) {
            return this.confirm.mouseClicked(mouseX, mouseY);
        }
        if (!this.contains(mouseX, mouseY)) {
            return false;
        }
        if (button == 0) {
            for (int i = this.hits.size() - 1; i >= 0; --i) {
                Hit h = this.hits.get(i);
                if (!StudioBase.in(mouseX, mouseY, h.x(), h.y(), h.w(), h.h())) continue;
                h.action().run();
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean isCapturingKeys() {
        return this.confirm.open();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.confirm.open()) {
            return this.confirm.keyPressed(keyCode);
        }
        return false;
    }

    public void filesDropped(List<Path> paths) {
    }

    protected static void background(String name, Runnable work) {
        Thread t = new Thread(work, name);
        t.setDaemon(true);
        t.start();
    }

    protected record Hit(float x, float y, float w, float h, Runnable action) {
    }
}

