/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.input.CharacterEvent
 */
package net.friendswithmoney.fwm.ui.panel;

import java.util.ArrayList;
import java.util.List;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.ui.ConfirmBox;
import net.friendswithmoney.fwm.ui.LauncherGfx;
import net.friendswithmoney.fwm.ui.gfx.Shapes;
import net.friendswithmoney.fwm.ui.panel.Panel;
import net.friendswithmoney.fwm.ui.widget.Row;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;

public abstract class RowsPanel
extends Panel {
    private static final int SCROLLBAR_W = 3;
    private static final int MAX_ROW_WIDTH = 440;
    protected final List<Row> rows = new ArrayList<Row>();
    protected final ConfirmBox confirm = new ConfirmBox();
    private int scroll;
    private int maxScroll;
    private int rowsShown;
    private float glide;
    private long glideAt;

    protected abstract void buildRows();

    @Override
    protected void onResize() {
        this.rows.clear();
        this.buildRows();
        this.rowsShown = Math.max(1, Math.min(this.rows.size(), this.height / 20));
        this.maxScroll = Math.max(0, this.rows.size() - this.rowsShown);
        this.scroll = Math.max(0, Math.min(this.maxScroll, this.scroll));
        this.positionRows();
    }

    private void positionRows() {
        int rowWidth = this.rowWidth();
        int rowX = this.rowX();
        for (int i = 0; i < this.rows.size(); ++i) {
            int visibleIndex = i - this.scroll;
            if (visibleIndex < 0 || visibleIndex >= this.rowsShown) {
                this.rows.get(i).setBounds(rowX, -1073741824, rowWidth);
                continue;
            }
            this.rows.get(i).setBounds(rowX, this.y + visibleIndex * 20, rowWidth);
        }
    }

    public int contentHeight() {
        return this.rows.size() * 20 + 8;
    }

    public boolean compact() {
        return true;
    }

    private int rowWidth() {
        int full = this.width - (this.maxScroll > 0 ? 5 : 0);
        return this.compact() ? Math.min(440, full) : full;
    }

    private int rowX() {
        return this.x + Math.max(0, (this.width - (this.maxScroll > 0 ? 5 : 0) - this.rowWidth()) / 2);
    }

    private int lastVisible() {
        return Math.min(this.rows.size(), this.scroll + this.rowsShown);
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY, float delta) {
        if (this.compact() && this.rowWidth() < this.width - 20) {
            int shown = this.lastVisible() - this.scroll;
            LauncherGfx.round(ctx, this.rowX() - 6, this.y - 1, this.rowWidth() + 12, shown * 20 + 4, 6, 0x26000000);
            LauncherGfx.ring(ctx, this.rowX() - 6, this.y - 1, this.rowWidth() + 12, shown * 20 + 4, 6, 0x14FFFFFF);
        }
        long now = System.currentTimeMillis();
        float dt = this.glideAt == 0L ? 0.0f : Math.min(0.1f, (float)(now - this.glideAt) / 1000.0f);
        this.glideAt = now;
        if (V2.INSTANCE.animations == 0) {
            this.glide = 0.0f;
        }
        this.glide *= (float)Math.exp(-dt * 26.0f);
        if (Math.abs(this.glide) < 0.2f) {
            this.glide = 0.0f;
        }
        int listH = this.rowsShown * 20;
        if (this.glide != 0.0f) {
            ctx.enableScissor(this.x, this.y - 1, this.x + this.width, this.y + listH + 1);
            ctx.pose().pushMatrix();
            ctx.pose().translate(0.0f, this.glide);
        }
        int my = this.glide != 0.0f ? mouseY - Math.round(this.glide) : mouseY;
        for (int i = this.scroll; i < this.lastVisible(); ++i) {
            this.rows.get(i).render(ctx, font, mouseX, my);
        }
        if (this.glide != 0.0f) {
            ctx.pose().popMatrix();
            ctx.disableScissor();
        }
        if (this.maxScroll > 0) {
            int trackX = this.rowX() + this.rowWidth() + 2;
            float thumbH = Math.max(10.0f, (float)(listH * this.rowsShown) / (float)this.rows.size());
            float at = Math.max(0.0f, Math.min((float)this.maxScroll, (float)this.scroll - this.glide / 20.0f));
            float thumbY = (float)this.y + ((float)listH - thumbH) * at / (float)this.maxScroll;
            Shapes.round(ctx, trackX, this.y, 3.0f, listH, 1.5f, 0x14FFFFFF);
            Shapes.round(ctx, trackX, thumbY, 3.0f, thumbH, 1.5f, -1434586938);
        }
        this.confirm.render(ctx, font, this.x, this.y, this.width, this.height, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.confirm.open()) {
            return this.confirm.mouseClicked(mouseX, mouseY);
        }
        for (Row row : this.rows) {
            if (!row.isCapturingKeys() || row.isHovered(mouseX, mouseY)) continue;
            row.commitTyped();
        }
        for (int i = this.scroll; i < this.lastVisible(); ++i) {
            if (!this.rows.get(i).mouseClicked(mouseX, mouseY, button)) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        for (int i = this.scroll; i < this.lastVisible(); ++i) {
            if (!this.rows.get(i).mouseDragged(mouseX, mouseY, button)) continue;
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased() {
        for (Row row : this.rows) {
            row.mouseReleased();
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double vertical) {
        if (this.maxScroll <= 0) {
            return false;
        }
        int before = this.scroll;
        this.scroll = Math.max(0, Math.min(this.maxScroll, this.scroll - (int)Math.signum(vertical)));
        this.glide += (float)((this.scroll - before) * 20);
        this.glide = Math.max(-60.0f, Math.min(60.0f, this.glide));
        this.positionRows();
        return true;
    }

    @Override
    public boolean isCapturingKeys() {
        if (this.confirm.open()) {
            return true;
        }
        for (Row row : this.rows) {
            if (!row.isCapturingKeys()) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        for (Row row : this.rows) {
            if (!row.isCapturingKeys() || !row.charTyped(input)) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.confirm.open()) {
            return this.confirm.keyPressed(keyCode);
        }
        for (Row row : this.rows) {
            if (!row.isCapturingKeys() || !row.keyPressed(keyCode, scanCode, modifiers)) continue;
            return true;
        }
        return false;
    }
}

