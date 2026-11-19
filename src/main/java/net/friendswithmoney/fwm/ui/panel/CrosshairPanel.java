/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.network.chat.Component
 */
package net.friendswithmoney.fwm.ui.panel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.game.Crosshair;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.LauncherGfx;
import net.friendswithmoney.fwm.ui.panel.RowsPanel;
import net.friendswithmoney.fwm.ui.widget.ButtonRow;
import net.friendswithmoney.fwm.ui.widget.ColorPickRow;
import net.friendswithmoney.fwm.ui.widget.HexRow;
import net.friendswithmoney.fwm.ui.widget.SliderRow;
import net.friendswithmoney.fwm.ui.widget.TextRow;
import net.friendswithmoney.fwm.ui.widget.ToggleRow;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class CrosshairPanel
extends RowsPanel {
    private static final int MINT = -8523578;
    private static final int TEXT = -1379602;
    private static final int SUB = -7362144;
    private int fx;
    private int fy;
    private int fw;
    private int fh;
    private int gx;
    private int gy;
    private int cell;
    private int[][] buttons = new int[0][];
    private int previewMode;
    private Boolean painting;
    private boolean[] canvas;
    private static final int NONE = -99;
    private static final int TARGET = -2;
    private static final int HIT = -3;
    private int editing = -99;
    private int pickHue;
    private int pickShade = 50;
    private static final int[] PRESETS = new int[]{-1, -8523578, -43691, -12288, -11149057, -43521};
    private int[][] presetBoxes = new int[0][];
    private int[][] modeChips = new int[0][];

    private int colorOf(int which) {
        V2 c = V2.INSTANCE;
        if (which == -2) {
            return c.hitResult;
        }
        if (which == -3) {
            return c.crosshairHit;
        }
        return c.crosshairColors.get(Math.max(0, Math.min(c.crosshairColors.size() - 1, which)));
    }

    private void setColor(int which, int argb) {
        V2 c = V2.INSTANCE;
        if (which == -2) {
            c.hitResult = argb;
        } else if (which == -3) {
            c.crosshairHit = argb;
        } else if (which >= 0 && which < c.crosshairColors.size()) {
            c.crosshairColors.set(which, argb);
        }
        V2.save();
    }

    private void readPick(int argb) {
        float min;
        float r = (float)(argb >> 16 & 0xFF) / 255.0f;
        float g = (float)(argb >> 8 & 0xFF) / 255.0f;
        float b = (float)(argb & 0xFF) / 255.0f;
        float max = Math.max(r, Math.max(g, b));
        float h = max == (min = Math.min(r, Math.min(g, b))) ? 0.0f : (max == r ? 60.0f * ((g - b) / (max - min) % 6.0f) : (max == g ? 60.0f * ((b - r) / (max - min) + 2.0f) : 60.0f * ((r - g) / (max - min) + 4.0f)));
        this.pickHue = Math.round((h + 360.0f) % 360.0f);
        this.pickShade = max < 1.0f ? Math.round(max * 50.0f) : Math.round(50.0f + min * 50.0f);
    }

    private void openPicker(int which) {
        if (this.editing == which) {
            this.editing = -99;
        } else {
            this.editing = which;
            this.readPick(this.colorOf(which));
        }
        this.rebuild();
    }

    private void pickerRows(int which) {
        if (this.editing != which) {
            return;
        }
        this.rows.add(new ColorPickRow(CrosshairPanel.t("Farbton", "Hue"), false, () -> this.pickHue, () -> this.pickHue, v -> {
            this.pickHue = v;
        }, null));
        this.rows.add(new ColorPickRow(CrosshairPanel.t("Helligkeit", "Shade"), true, () -> this.pickHue, () -> this.pickShade, v -> {
            this.pickShade = v;
        }, () -> {
            this.setColor(which, ColorPickRow.color(this.pickHue, this.pickShade));
            this.editing = -99;
            this.rebuild();
        }));
    }

    @Override
    public Component title() {
        return Lang.of("fwm.tab.crosshair", new Object[0]);
    }

    static Component t(String de, String en) {
        return Component.literal((String)Lang.t(de, en));
    }

    static ToggleRow toggle(String de, String en, BooleanSupplier get, Consumer<Boolean> set) {
        return new ToggleRow(CrosshairPanel.t(de, en), get, v -> {
            set.accept((Boolean)v);
            V2.save();
        });
    }

    @Override
    protected void onResize() {
        this.fx = this.x;
        this.fy = this.y;
        this.fw = this.width;
        this.fh = this.height;
        int left = Math.min(this.fw * 46 / 100, this.fh + 40);
        this.cell = Math.max(3, Math.min((left - 20) / 21, (this.fh - 18 - 20 - 60 - 20) / 21));
        this.gx = this.fx + (left - this.cell * 21) / 2;
        this.gy = this.fy + 18;
        this.x = this.fx + left + 10;
        this.width = this.fw - left - 10;
        super.onResize();
    }

    @Override
    protected void buildRows() {
        V2 c = V2.INSTANCE;
        this.rows.add(new TextRow(CrosshairPanel.t("Farben \u00b7 Feld anklicken zum W\u00e4hlen", "Colours \u00b7 click a swatch to pick")));
        for (int i = 0; i < 5; ++i) {
            int slot = i;
            if (slot >= c.crosshairColors.size()) continue;
            this.rows.add(new HexRow((Component)Component.literal((String)(Lang.t("Farbe ", "Colour ") + (slot + 1) + (slot == c.crosshairSelected ? " \u25c0" : ""))), () -> (this.editing == slot ? ColorPickRow.color(this.pickHue, this.pickShade) : c.crosshairColors.get(slot)) & 0xFFFFFF, v -> {
                c.crosshairColors.set(slot, 0xFF000000 | v);
                V2.save();
            }, () -> this.openPicker(slot), c.crosshairColors.size() > 1 ? () -> {
                c.crosshairColors.remove(slot);
                c.crosshairSelected = Math.max(0, Math.min(c.crosshairColors.size() - 1, c.crosshairSelected));
                this.editing = -99;
                V2.save();
                this.rebuild();
            } : null));
            this.pickerRows(slot);
        }
        this.rows.add(new ButtonRow(CrosshairPanel.t("Farbplatz", "Colour slot"), () -> CrosshairPanel.t(c.crosshairColors.size() < 5 ? "+ Farbe" : "N\u00e4chste", c.crosshairColors.size() < 5 ? "+ colour" : "Next"), () -> {
            if (c.crosshairColors.size() < 5) {
                c.crosshairColors.add(-8523578);
            } else {
                c.crosshairSelected = (c.crosshairSelected + 1) % c.crosshairColors.size();
            }
            V2.save();
            this.rebuild();
        }));
        this.rows.add(CrosshairPanel.toggle("Farbverlauf", "Gradient", () -> c.crosshairGradient, v -> {
            c.crosshairGradient = v;
        }));
        this.rows.add(new SliderRow(CrosshairPanel.t("Dauer des Verlaufs", "Gradient length"), 1, 40, () -> Math.round(c.crosshairGradientSeconds * 2.0f), v -> {
            c.crosshairGradientSeconds = (float)v / 2.0f;
            V2.save();
        }, v -> Component.literal((String)((float)v / 2.0f + " s"))));
        this.rows.add(new TextRow(CrosshairPanel.t("Form", "Shape")));
        this.rows.add(CrosshairPanel.toggle("Eigene Pixel", "Own pixels", () -> c.crosshairCustom, v -> {
            c.crosshairCustom = v;
        }));
        this.rows.add(new SliderRow(CrosshairPanel.t("L\u00e4nge", "Length"), 1, 9, () -> c.crosshairSize, v -> {
            c.crosshairSize = v;
            c.crosshairCustom = false;
            V2.save();
        }, v -> Component.literal((String)(v + " px"))));
        this.rows.add(new SliderRow(CrosshairPanel.t("Dicke", "Thickness"), 1, 5, () -> c.crosshairThickness, v -> {
            c.crosshairThickness = v;
            c.crosshairCustom = false;
            V2.save();
        }, v -> Component.literal((String)(v + " px"))));
        this.rows.add(new SliderRow(CrosshairPanel.t("L\u00fccke", "Gap"), 0, 6, () -> c.crosshairGap, v -> {
            c.crosshairGap = v;
            c.crosshairCustom = false;
            V2.save();
        }, v -> Component.literal((String)(v + " px"))));
        this.rows.add(CrosshairPanel.toggle("Punkt in der Mitte", "Dot in the middle", () -> c.crosshairDot, v -> {
            c.crosshairDot = v;
            c.crosshairCustom = false;
        }));
        this.rows.add(new TextRow(CrosshairPanel.t("Spieler und Treffer", "Players and hits")));
        this.rows.add(CrosshairPanel.toggle("Farbe bei Spieler", "Colour on player", () -> c.crosshairTargetColor, v -> {
            c.crosshairTargetColor = v;
        }));
        this.rows.add(new HexRow(CrosshairPanel.t("Ziel-Farbe", "Target colour"), () -> (this.editing == -2 ? ColorPickRow.color(this.pickHue, this.pickShade) : c.hitResult) & 0xFFFFFF, v -> {
            c.hitResult = 0xFF000000 | v;
            V2.save();
        }, () -> this.openPicker(-2)));
        this.pickerRows(-2);
        this.rows.add(CrosshairPanel.toggle("Treffer-Blitz", "Hit flash", () -> c.crosshairHitFlash, v -> {
            c.crosshairHitFlash = v;
        }));
        this.rows.add(new HexRow(CrosshairPanel.t("Blitz-Farbe", "Flash colour"), () -> (this.editing == -3 ? ColorPickRow.color(this.pickHue, this.pickShade) : c.crosshairHit) & 0xFFFFFF, v -> {
            c.crosshairHit = 0xFF000000 | v;
            V2.save();
        }, () -> this.openPicker(-3)));
        this.pickerRows(-3);
        this.rows.add(new ButtonRow(CrosshairPanel.t("Alles", "Everything"), () -> CrosshairPanel.t("Zur\u00fccksetzen", "Reset"), () -> {
            c.crosshairColors = new ArrayList<Integer>(List.of(Integer.valueOf(-1)));
            c.crosshairSelected = 0;
            c.crosshairGradient = false;
            c.crosshairSize = 5;
            c.crosshairThickness = 1;
            c.crosshairGap = 1;
            c.crosshairDot = false;
            c.crosshairCustom = false;
            c.crosshairPixels = "";
            V2.save();
            this.rebuild();
        }));
    }

    private void rebuild() {
        this.setBounds(this.fx, this.fy, this.fw, this.fh);
    }

    private int previewColor() {
        V2 c = V2.INSTANCE;
        return this.previewMode == 1 ? c.hitResult : (this.previewMode == 2 ? c.crosshairHit : Crosshair.baseColor());
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY, float delta) {
        int left = this.x - 10 - this.fx;
        LauncherGfx.round(ctx, this.fx, this.fy, left, this.fh, 8, 0x30000000);
        LauncherGfx.ring(ctx, this.fx, this.fy, left, this.fh, 8, 0x22FFFFFF);
        LauncherGfx.bold(ctx, font, Lang.t("Zeichnen", "Draw"), this.fx + 8, this.fy + 6, -1379602);
        String hint = Lang.t("Links malt \u00b7 Rechts radiert", "Left draws \u00b7 right erases");
        LauncherGfx.text(ctx, font, hint, this.fx + left - 8 - font.width(hint), this.fy + 6, -7362144);
        int n = 21;
        int size = this.cell * n;
        for (int r = 0; r < n; ++r) {
            for (int k = 0; k < n; ++k) {
                int shade;
                int n2 = shade = (r + k) % 2 == 0 ? -14930904 : -14535632;
                if (r == 10 || k == 10) {
                    shade = (r + k) % 2 == 0 ? -14535121 : -14140363;
                }
                ctx.fill(this.gx + k * this.cell, this.gy + r * this.cell, this.gx + (k + 1) * this.cell, this.gy + (r + 1) * this.cell, shade);
            }
        }
        boolean[] g = this.canvas != null ? this.canvas : Crosshair.grid();
        Crosshair.draw(ctx, g, this.gx, this.gy, this.cell, this.previewColor());
        LauncherGfx.ring(ctx, this.gx - 1, this.gy - 1, size + 2, size + 2, 2, 0x33FFFFFF);
        int[] hover = this.cellAt(mouseX, mouseY);
        if (hover != null) {
            int hx = this.gx + hover[0] * this.cell;
            int hy = this.gy + hover[1] * this.cell;
            ctx.fill(hx, hy, hx + this.cell, hy + 1, -855638017);
            ctx.fill(hx, hy + this.cell - 1, hx + this.cell, hy + this.cell, -855638017);
            ctx.fill(hx, hy, hx + 1, hy + this.cell, -855638017);
            ctx.fill(hx + this.cell - 1, hy, hx + this.cell, hy + this.cell, -855638017);
        }
        V2 c = V2.INSTANCE;
        String[] labels = new String[]{Lang.t("Form", "Shape"), Lang.t("Leeren", "Clear"), Lang.t("Spiegeln", "Mirror")};
        int by = this.gy + size + 6;
        int bx = this.gx;
        int gap = 4;
        int bw = (size - 2 * gap) / 3;
        this.buttons = new int[3][];
        for (int i = 0; i < 3; ++i) {
            this.buttons[i] = new int[]{bx + i * (bw + gap), by, bw, 14};
            boolean over = CrosshairPanel.inside(this.buttons[i], mouseX, mouseY);
            boolean lit = i == 2 && c.crosshairMirror;
            LauncherGfx.round(ctx, this.buttons[i][0], by, bw, 14, 4, lit ? 1149104326 : (over ? 0x33FFFFFF : 0x1AFFFFFF));
            LauncherGfx.ring(ctx, this.buttons[i][0], by, bw, 14, 4, lit ? -2005012282 : 0x22FFFFFF);
            String label = LauncherGfx.trim(font, labels[i], bw - 6);
            LauncherGfx.text(ctx, font, label, this.buttons[i][0] + (bw - font.width(label)) / 2, by + 3, lit ? -8523578 : -1379602);
        }
        int py = by + 20;
        int ph = Math.max(40, this.fy + this.fh - 6 - py - 16 - 18);
        int pw = size;
        ctx.fillGradient(this.gx, py, this.gx + pw, py + ph * 2 / 3, -8408347, -4860176);
        ctx.fill(this.gx, py + ph * 2 / 3, this.gx + pw, py + ph * 5 / 6, -11628742);
        ctx.fill(this.gx, py + ph * 5 / 6, this.gx + pw, py + ph, -9743817);
        if (this.previewMode == 1) {
            int sx = this.gx + pw / 2 - 3;
            ctx.fill(sx, py + ph / 2 - 10, sx + 6, py + ph / 2 - 4, -3566486);
            ctx.fill(sx, py + ph / 2 - 4, sx + 6, py + ph * 2 / 3 + 2, -13668432);
        }
        Crosshair.draw(ctx, g, this.gx + pw / 2 - 20, py + ph / 2 - 20, 2, this.previewColor());
        LauncherGfx.ring(ctx, this.gx - 1, py - 1, pw + 2, ph + 2, 2, 0x33FFFFFF);
        String[] modes = new String[]{Lang.t("Normal", "Normal"), Lang.t("Im Ziel", "In sight"), Lang.t("Treffer", "Hit")};
        int my0 = py + ph + 4;
        int cw = (size - 2 * gap) / 3;
        int[][] chips = new int[3][];
        for (int i = 0; i < 3; ++i) {
            int cx0 = this.gx + i * (cw + gap);
            chips[i] = new int[]{cx0, my0, cw, 12};
            boolean on = this.previewMode == i;
            LauncherGfx.round(ctx, cx0, my0, cw, 12, 6, on ? -8523578 : 0x22FFFFFF);
            String label = LauncherGfx.trim(font, modes[i], cw - 6);
            LauncherGfx.text(ctx, font, label, cx0 + (cw - font.width(label)) / 2, my0 + 2, on ? -16045536 : -1379602);
        }
        this.modeChips = chips;
        int sy = my0 + 16;
        int sw = Math.max(10, (size - 5 * gap) / 6);
        this.presetBoxes = new int[PRESETS.length][];
        for (int i = 0; i < PRESETS.length; ++i) {
            int sx = this.gx + i * (sw + gap) + (size - (6 * sw + 5 * gap)) / 2;
            this.presetBoxes[i] = new int[]{sx, sy, sw, 12};
            boolean over = CrosshairPanel.inside(this.presetBoxes[i], mouseX, mouseY);
            LauncherGfx.round(ctx, sx, sy, sw, 12, 3, PRESETS[i]);
            LauncherGfx.ring(ctx, sx, sy, sw, 12, 3, over ? -1 : 0x55000000);
        }
        super.render(ctx, font, mouseX, mouseY, delta);
    }

    private static boolean inside(int[] b, double mx, double my) {
        return b != null && mx >= (double)b[0] && mx < (double)(b[0] + b[2]) && my >= (double)b[1] && my < (double)(b[1] + b[3]);
    }

    private int[] cellAt(double mx, double my) {
        int n = 21;
        if (mx < (double)this.gx || my < (double)this.gy || mx >= (double)(this.gx + n * this.cell) || my >= (double)(this.gy + n * this.cell)) {
            return null;
        }
        return new int[]{(int)((mx - (double)this.gx) / (double)this.cell), (int)((my - (double)this.gy) / (double)this.cell)};
    }

    private void paint(int[] at) {
        int[][] spots;
        int[][] nArrayArray;
        int n = 21;
        int a = at[0];
        int b = at[1];
        if (V2.INSTANCE.crosshairMirror) {
            int[][] nArrayArray2 = new int[4][];
            nArrayArray2[0] = new int[]{a, b};
            nArrayArray2[1] = new int[]{n - 1 - a, b};
            nArrayArray2[2] = new int[]{a, n - 1 - b};
            nArrayArray = nArrayArray2;
            nArrayArray2[3] = new int[]{n - 1 - a, n - 1 - b};
        } else {
            int[][] nArrayArray3 = new int[1][];
            nArrayArray = nArrayArray3;
            nArrayArray3[0] = new int[]{a, b};
        }
        for (int[] s : spots = nArrayArray) {
            this.canvas[s[1] * n + s[0]] = this.painting;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int i;
        V2 c = V2.INSTANCE;
        int[] at = this.cellAt(mouseX, mouseY);
        if (at != null && (button == 0 || button == 1)) {
            this.canvas = (boolean[])Crosshair.grid().clone();
            this.painting = button == 0;
            this.paint(at);
            return true;
        }
        for (i = 0; i < this.buttons.length; ++i) {
            if (!CrosshairPanel.inside(this.buttons[i], mouseX, mouseY)) continue;
            if (i == 0) {
                c.crosshairPixels = Crosshair.write(Crosshair.shape());
                c.crosshairCustom = true;
            } else if (i == 1) {
                boolean[] empty = new boolean[441];
                empty[220] = true;
                c.crosshairPixels = Crosshair.write(empty);
                c.crosshairCustom = true;
            } else {
                c.crosshairMirror = !c.crosshairMirror;
            }
            V2.save();
            return true;
        }
        for (i = 0; i < this.presetBoxes.length; ++i) {
            if (!CrosshairPanel.inside(this.presetBoxes[i], mouseX, mouseY)) continue;
            if (this.editing != -99) {
                this.readPick(PRESETS[i]);
            } else {
                this.setColor(c.crosshairSelected, PRESETS[i]);
            }
            return true;
        }
        for (i = 0; i < this.modeChips.length; ++i) {
            if (!CrosshairPanel.inside(this.modeChips[i], mouseX, mouseY)) continue;
            this.previewMode = i;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        if (this.painting != null) {
            int[] at = this.cellAt(mouseX, mouseY);
            if (at != null) {
                this.paint(at);
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased() {
        if (this.painting != null && this.canvas != null) {
            V2 c = V2.INSTANCE;
            c.crosshairPixels = Crosshair.write(this.canvas);
            c.crosshairCustom = true;
            V2.save();
        }
        this.painting = null;
        this.canvas = null;
        super.mouseReleased();
    }
}

