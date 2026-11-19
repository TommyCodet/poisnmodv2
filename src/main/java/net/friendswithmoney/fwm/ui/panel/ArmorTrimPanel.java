/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.decoration.ArmorStand
 *  org.joml.Matrix3x2fStack
 *  org.joml.Vector2f
 */
package net.friendswithmoney.fwm.ui.panel;

import java.util.ArrayList;
import java.util.List;
import net.friendswithmoney.fwm.game.ArmorTrims;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.LauncherGfx;
import net.friendswithmoney.fwm.ui.panel.Panel;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2f;

public class ArmorTrimPanel
extends Panel {
    private static final int TEXT = -1379602;
    private static final int SUB = -7362144;
    private static final int MINT = -8523578;
    private static final int CARD = 0x26000000;
    private static final int RING = 0x14FFFFFF;
    private final List<int[]> boxes = new ArrayList<int[]>();
    private final List<Runnable> actions = new ArrayList<Runnable>();
    private float spin = 180.0f;
    private boolean dragging;
    private double lastDragX;
    private ArmorTrims.Slot pickSlot;
    private Column pickCol;
    private final List<int[]> pickBoxes = new ArrayList<int[]>();
    private final List<Runnable> pickActions = new ArrayList<Runnable>();

    @Override
    public Component title() {
        return Component.literal((String)Lang.t("R\u00fcstungs-Trims", "Armor Trims"));
    }

    private void hit(int bx, int by, int w, int h, Runnable a) {
        this.boxes.add(new int[]{bx, by, w, h});
        this.actions.add(a);
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY, float delta) {
        this.boxes.clear();
        this.actions.clear();
        int left = Math.max(120, Math.round((float)this.width * 0.42f));
        LauncherGfx.round(ctx, this.x, this.y, left, this.height, 8, 0x26000000);
        LauncherGfx.ring(ctx, this.x, this.y, left, this.height, 8, 0x14FFFFFF);
        this.drawFigure(ctx, this.x, this.y, left, this.height, delta);
        int px = this.x + left + 10;
        int pw = this.width - left - 10;
        int cy = this.y;
        cy += this.topButtons(ctx, font, px, cy, pw, mouseX, mouseY);
        int gap = 6;
        int cardH = (this.height - (cy - this.y) - gap * 3) / 4;
        for (ArmorTrims.Slot s : ArmorTrims.Slot.values()) {
            this.slotCard(ctx, font, px, cy, pw, cardH, s, mouseX, mouseY);
            cy += cardH + gap;
        }
        if (this.pickSlot != null) {
            this.drawPicker(ctx, font, mouseX, mouseY);
        }
    }

    private int topButtons(GuiGraphicsExtractor ctx, Font font, int px, int py, int pw, int mx, int my) {
        int bw = (pw - 6) / 2;
        this.button(ctx, font, px, py, bw, Lang.t("Zufall", "Random"), true, mx, my, ArmorTrims::randomize);
        this.button(ctx, font, px + bw + 6, py, bw, Lang.t("Zur\u00fccksetzen", "Reset"), false, mx, my, ArmorTrims::reset);
        return 23;
    }

    private void button(GuiGraphicsExtractor ctx, Font font, int bx, int by, int bw, String label, boolean strong, int mx, int my, Runnable a) {
        boolean over;
        boolean bl = over = mx >= bx && mx < bx + bw && my >= by && my < by + 15;
        int fill = strong ? (over ? -6555690 : -8523578) : (over ? 0x33FFFFFF : 0x1AFFFFFF);
        LauncherGfx.round(ctx, bx, by, bw, 15, 5, fill);
        if (!strong) {
            LauncherGfx.ring(ctx, bx, by, bw, 15, 5, 0x14FFFFFF);
        }
        ctx.text(font, label, bx + (bw - font.width(label)) / 2, by + 4, strong ? -16045536 : -1379602, false);
        this.hit(bx, by, bw, 15, a);
    }

    private void slotCard(GuiGraphicsExtractor ctx, Font font, int px, int py, int pw, int ch, ArmorTrims.Slot s, int mx, int my) {
        LauncherGfx.round(ctx, px, py, pw, ch, 6, 0x26000000);
        LauncherGfx.ring(ctx, px, py, pw, ch, 6, 0x14FFFFFF);
        ctx.text(font, Lang.t(s.de, s.en), px + 7, py + 5, -1379602, false);
        LauncherGfx.item(ctx, ArmorTrims.stack(s), px + pw - 22, py + 3, 0.9f);
        int rbx = px + pw - 22 - 32;
        this.iconButton(ctx, font, rbx, py + 4, "\u21bb", mx, my, () -> ArmorTrims.randomize(s));
        this.iconButton(ctx, font, rbx + 15, py + 4, "\u2715", mx, my, () -> ArmorTrims.clear(s));
        int fy = py + 18;
        int fw = (pw - 14 - 8) / 3;
        this.field(ctx, font, px + 7, fy, fw, ch - 22, Lang.t("Basis", "Base"), this.baseName(s), mx, my, () -> this.openPicker(s, Column.BASE));
        this.field(ctx, font, px + 7 + fw + 4, fy, fw, ch - 22, Lang.t("Muster", "Pattern"), this.patternName(s), mx, my, () -> this.openPicker(s, Column.PATTERN));
        this.field(ctx, font, px + 7 + (fw + 4) * 2, fy, fw, ch - 22, "Material", this.materialName(s), mx, my, () -> this.openPicker(s, Column.MATERIAL));
    }

    private void field(GuiGraphicsExtractor ctx, Font font, int bx, int by, int bw, int bh, String caption, String value, int mx, int my, Runnable open) {
        boolean over = mx >= bx && mx < bx + bw && my >= by && my < by + bh;
        LauncherGfx.round(ctx, bx, by, bw, bh, 4, over ? 0x33FFFFFF : 0x22000000);
        LauncherGfx.ring(ctx, bx, by, bw, bh, 4, 0x14FFFFFF);
        ctx.text(font, caption, bx + 4, by + 3, -7362144, false);
        ctx.text(font, LauncherGfx.trim(font, value, bw - 8), bx + 4, by + bh - 11, value.equals("\u2014") ? -7362144 : -1379602, false);
        this.hit(bx, by, bw, bh, open);
    }

    private void iconButton(GuiGraphicsExtractor ctx, Font font, int bx, int by, String glyph, int mx, int my, Runnable a) {
        boolean over = mx >= bx && mx < bx + 13 && my >= by && my < by + 11;
        LauncherGfx.round(ctx, bx, by, 13, 11, 3, over ? 0x33FFFFFF : 0x1AFFFFFF);
        ctx.text(font, glyph, bx + (13 - font.width(glyph)) / 2, by + 2, over ? -8523578 : -7362144, false);
        this.hit(bx, by, 13, 11, a);
    }

    private String baseName(ArmorTrims.Slot s) {
        int b = ArmorTrims.base(s);
        return b < 0 ? "\u2014" : ArmorTrims.BASES.get(b).name();
    }

    private String patternName(ArmorTrims.Slot s) {
        int p = ArmorTrims.pattern(s);
        return p < 0 ? "\u2014" : ArmorTrims.PATTERNS.get(p).label();
    }

    private String materialName(ArmorTrims.Slot s) {
        int m = ArmorTrims.material(s);
        return m < 0 ? "\u2014" : ArmorTrims.MATERIALS.get(m).label();
    }

    private void drawFigure(GuiGraphicsExtractor ctx, int bx, int by, int bw, int bh, float delta) {
        ArmorStand stand = ArmorTrims.stand();
        if (stand == null) {
            return;
        }
        if (!this.dragging) {
            this.spin += delta * 0.6f;
        }
        stand.yBodyRot = this.spin;
        stand.setYRot(this.spin);
        stand.yHeadRot = this.spin;
        Matrix3x2fStack m = ctx.pose();
        Vector2f a = m.transformPosition((float)(bx + 6), (float)(by + 6), new Vector2f());
        Vector2f b = m.transformPosition((float)(bx + bw - 6), (float)(by + bh - 6), new Vector2f());
        float scale = m.m00();
        int size = Math.round(Math.min((float)bw * 0.46f, (float)bh * 0.35f) * scale);
        float cx = (a.x + b.x) / 2.0f;
        float cy = (a.y + b.y) / 2.0f;
        try {
            InventoryScreen.extractEntityInInventoryFollowsMouse((GuiGraphicsExtractor)ctx, (int)Math.round(a.x), (int)Math.round(a.y), (int)Math.round(b.x), (int)Math.round(b.y), (int)size, (float)0.0f, (float)cx, (float)cy, (LivingEntity)stand);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private void openPicker(ArmorTrims.Slot s, Column c) {
        this.pickSlot = s;
        this.pickCol = c;
    }

    private void closePicker() {
        this.pickSlot = null;
    }

    private void drawPicker(GuiGraphicsExtractor ctx, Font font, int mx, int my) {
        this.pickBoxes.clear();
        this.pickActions.clear();
        ctx.fill(this.x, this.y, this.x + this.width, this.y + this.height, -1442840576);
        ArrayList<String> labels = new ArrayList<String>();
        ArrayList<Runnable> picks = new ArrayList<Runnable>();
        ArrayList<Integer> values = new ArrayList<Integer>();
        values.add(-1);
        labels.add(Lang.t("\u2014 keins \u2014", "\u2014 none \u2014"));
        picks.add(() -> {
            this.set(this.pickSlot, this.pickCol, -1);
            this.closePicker();
        });
        int count = switch (this.pickCol.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> ArmorTrims.BASES.size();
            case 1 -> ArmorTrims.PATTERNS.size();
            case 2 -> ArmorTrims.MATERIALS.size();
        };
        for (int i = 0; i < count; ++i) {
            if (this.pickCol == Column.BASE && !ArmorTrims.fits(i, this.pickSlot)) continue;
            int idx = i;
            values.add(i);
            labels.add(switch (this.pickCol.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> ArmorTrims.BASES.get(i).name();
                case 1 -> ArmorTrims.PATTERNS.get(i).label();
                case 2 -> ArmorTrims.MATERIALS.get(i).label();
            });
            picks.add(() -> {
                this.set(this.pickSlot, this.pickCol, idx);
                this.closePicker();
            });
        }
        int w = Math.min(220, this.width - 40);
        int rows = labels.size();
        int rh = 13;
        int h = Math.min(this.height - 20, 24 + rows * rh);
        int bx = this.x + (this.width - w) / 2;
        int by = this.y + (this.height - h) / 2;
        LauncherGfx.round(ctx, bx, by, w, h, 8, -267443938);
        LauncherGfx.ring(ctx, bx, by, w, h, 8, -2005012282);
        String head = switch (this.pickCol.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> Lang.t("Basis w\u00e4hlen", "Choose base");
            case 1 -> Lang.t("Muster w\u00e4hlen", "Choose pattern");
            case 2 -> Lang.t("Material w\u00e4hlen", "Choose material");
        };
        LauncherGfx.bold(ctx, font, head, bx + 10, by + 7, -8523578);
        int listTop = by + 20;
        int shown = Math.min(rows, (h - 24) / rh);
        for (int i = 0; i < shown; ++i) {
            boolean chosen;
            int ry = listTop + i * rh;
            boolean over = mx >= bx + 4 && mx < bx + w - 4 && my >= ry && my < ry + rh;
            boolean bl = chosen = ((Integer)values.get(i)).intValue() == this.current(this.pickSlot, this.pickCol);
            if (over || chosen) {
                LauncherGfx.round(ctx, bx + 4, ry, w - 8, rh, 3, over ? 0x33FFFFFF : 578678982);
            }
            ctx.text(font, LauncherGfx.trim(font, (String)labels.get(i), w - 16), bx + 10, ry + 3, chosen ? -8523578 : -1379602, false);
            this.pickBoxes.add(new int[]{bx + 4, ry, w - 8, rh});
            this.pickActions.add((Runnable)picks.get(i));
        }
    }

    private int current(ArmorTrims.Slot s, Column c) {
        return switch (c.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> ArmorTrims.base(s);
            case 1 -> ArmorTrims.pattern(s);
            case 2 -> ArmorTrims.material(s);
        };
    }

    private void set(ArmorTrims.Slot s, Column c, int v) {
        switch (c.ordinal()) {
            case 0: {
                ArmorTrims.setBase(s, v);
                break;
            }
            case 1: {
                ArmorTrims.setPattern(s, v);
                break;
            }
            case 2: {
                ArmorTrims.setMaterial(s, v);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return this.contains(mouseX, mouseY);
        }
        if (this.pickSlot != null) {
            for (int i = this.pickBoxes.size() - 1; i >= 0; --i) {
                int[] b = this.pickBoxes.get(i);
                if (!(mouseX >= (double)b[0]) || !(mouseX < (double)(b[0] + b[2])) || !(mouseY >= (double)b[1]) || !(mouseY < (double)(b[1] + b[3]))) continue;
                this.pickActions.get(i).run();
                return true;
            }
            this.closePicker();
            return true;
        }
        for (int i = this.boxes.size() - 1; i >= 0; --i) {
            int[] b = this.boxes.get(i);
            if (!(mouseX >= (double)b[0]) || !(mouseX < (double)(b[0] + b[2])) || !(mouseY >= (double)b[1]) || !(mouseY < (double)(b[1] + b[3]))) continue;
            this.actions.get(i).run();
            return true;
        }
        int left = Math.max(120, Math.round((float)this.width * 0.42f));
        if (mouseX >= (double)this.x && mouseX < (double)(this.x + left)) {
            this.dragging = true;
            this.lastDragX = mouseX;
        }
        return this.contains(mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        if (this.dragging) {
            this.spin += (float)(mouseX - this.lastDragX);
            this.lastDragX = mouseX;
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased() {
        this.dragging = false;
    }

    private static enum Column {
        BASE,
        PATTERN,
        MATERIAL;

    }
}

