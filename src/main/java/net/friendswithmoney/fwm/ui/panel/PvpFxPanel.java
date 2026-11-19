/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package net.friendswithmoney.fwm.ui.panel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.game.PvpFx;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.LauncherGfx;
import net.friendswithmoney.fwm.ui.gfx.Shapes;
import net.friendswithmoney.fwm.ui.panel.RowsPanel;
import net.friendswithmoney.fwm.ui.widget.ChipsRow;
import net.friendswithmoney.fwm.ui.widget.ColorPickRow;
import net.friendswithmoney.fwm.ui.widget.HexRow;
import net.friendswithmoney.fwm.ui.widget.SliderRow;
import net.friendswithmoney.fwm.ui.widget.TextRow;
import net.friendswithmoney.fwm.ui.widget.ToggleRow;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class PvpFxPanel
extends RowsPanel {
    private final int mode;
    private static final int NONE = -1;
    private int editing = -1;
    private int pickHue;
    private int pickShade = 50;
    private static final int MINT = -8523578;
    private static final int WHITE = -1;
    private static final int DIM = -7363684;
    private int fullW;
    private int[] preview;
    private long popAt;
    private boolean spin = false;
    private boolean previewParticles;
    private float yaw = -32.0f;
    private long spinAt;
    private boolean dragging;
    private double dragFromX;
    private float dragYaw;
    private final List<Hit> hits = new ArrayList<Hit>();

    public PvpFxPanel(int mode) {
        this.mode = mode;
    }

    private void readPick(int rgb) {
        float min;
        float r = (float)(rgb >> 16 & 0xFF) / 255.0f;
        float g = (float)(rgb >> 8 & 0xFF) / 255.0f;
        float b = (float)(rgb & 0xFF) / 255.0f;
        float max = Math.max(r, Math.max(g, b));
        float h = max == (min = Math.min(r, Math.min(g, b))) ? 0.0f : (max == r ? 60.0f * ((g - b) / (max - min) % 6.0f) : (max == g ? 60.0f * ((b - r) / (max - min) + 2.0f) : 60.0f * ((r - g) / (max - min) + 4.0f)));
        this.pickHue = Math.round((h + 360.0f) % 360.0f);
        this.pickShade = max < 1.0f ? Math.round(max * 50.0f) : Math.round(50.0f + min * 50.0f);
    }

    private void rebuild() {
        this.setBounds(this.x, this.y, this.preview != null ? this.fullW : this.width, this.height);
    }

    @Override
    protected void onResize() {
        this.preview = null;
        if (this.mode == 1 && this.width >= 430) {
            this.fullW = this.width;
            int left = Math.min(440, Math.round((float)this.width * 0.56f));
            int px = this.x + left + 16;
            int pw = this.x + this.fullW - px;
            int ph = Math.max(120, Math.min(Math.round((float)pw * 0.92f), this.height - 70));
            this.preview = new int[]{px, this.y + 12, pw, ph};
            this.width = left;
        }
        super.onResize();
    }

    @Override
    public boolean compact() {
        return this.preview == null;
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY, float delta) {
        if (this.preview != null) {
            int shown = Math.min(this.rows.size(), this.height / 20);
            LauncherGfx.round(ctx, this.x - 6, this.y - 1, this.width + 12, shown * 20 + 4, 6, 0x26000000);
            LauncherGfx.ring(ctx, this.x - 6, this.y - 1, this.width + 12, shown * 20 + 4, 6, 0x14FFFFFF);
        }
        super.render(ctx, font, mouseX, mouseY, delta);
        if (this.preview != null) {
            this.renderPreview(ctx, mouseX, mouseY);
        }
    }

    private void renderPreview(GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
        String play;
        boolean o1;
        float w1;
        float by;
        float bx;
        this.hits.clear();
        V2 c = V2.INSTANCE;
        int px = this.preview[0];
        int py = this.preview[1];
        int pw = this.preview[2];
        int ph = this.preview[3];
        LauncherGfx.text(ctx, Lang.t("LIVE-VORSCHAU", "LIVE PREVIEW"), px, this.y + 1, -8523578, 6.4f, true);
        int horizon = py + Math.round((float)ph * 0.66f);
        Shapes.roundGradient(ctx, px, py, pw, ph, 7.0f, -7950616, -2891022);
        ctx.enableScissor(px, horizon, px + pw, py + ph);
        Shapes.roundGradient(ctx, px, py, pw, ph, 7.0f, -7564910, -10657180);
        ctx.disableScissor();
        Shapes.ring(ctx, px, py, pw, ph, 7.0f, 0x22FFFFFF);
        long now = System.currentTimeMillis();
        float dt = this.spinAt == 0L ? 0.0f : Math.min(0.1f, (float)(now - this.spinAt) / 1000.0f);
        this.spinAt = now;
        if (this.spin && !this.dragging) {
            this.yaw += dt * 40.0f;
        }
        long life = Math.max(200L, (long)c.popChamsTenths * 100L);
        float fade = 1.0f;
        if (this.popAt > 0L) {
            long age = now - this.popAt;
            if (age < life) {
                fade = 1.0f - (float)age / (float)life;
            } else if (age < life + 450L) {
                fade = 0.0f;
            } else {
                this.popAt = 0L;
            }
        }
        float cx = (float)px + (float)pw / 2.0f;
        float base = (float)py + (float)ph * 0.84f;
        float scale = (float)ph * 0.64f / 32.0f;
        ctx.enableScissor(px, py, px + pw, py + ph);
        if (fade > 0.0f) {
            this.figure(ctx, cx, base, scale, fade);
        }
        if (this.popAt > 0L && this.previewParticles && c.popParticles) {
            this.particles(ctx, cx, base - 16.0f * scale, scale, now - this.popAt);
        }
        ctx.disableScissor();
        float lw = LauncherGfx.width("LIVE", 6.0f, true) + 16.0f;
        Shapes.round(ctx, px + 7, py + 7, lw, 12.0f, 6.0f, -871488480);
        Shapes.circle(ctx, px + 13, py + 13, 1.8f, -8523578);
        LauncherGfx.text(ctx, "LIVE", px + 17, (float)(py + 13) - 3.0f, -8523578, 6.0f, true);
        String note = Lang.t("\u00e4ndert sich mit deinen Farben", "follows your colours");
        float nw = LauncherGfx.width(note, 5.8f, false) + 12.0f;
        if (nw < (float)pw - lw - 24.0f) {
            Shapes.round(ctx, (float)(px + pw - 7) - nw, py + 7, nw, 12.0f, 6.0f, -871488480);
            LauncherGfx.text(ctx, note, (float)(px + pw - 7) - nw + 6.0f, (float)(py + 13) - 3.0f, -2759970, 5.8f, false);
        }
        Shapes.round(ctx, bx, by, w1, 14.0f, 5.0f, (o1 = PvpFxPanel.in(mouseX, mouseY, bx = (float)(px + 7), by = (float)(py + ph - 21), w1 = LauncherGfx.width(play = Lang.t("Pop abspielen", "Play pop"), 6.6f, true) + 22.0f, 14.0f)) ? -6555690 : -8523578);
        Shapes.icon(ctx, "play", bx + 6.0f, by + 3.5f, 7.0f, -16045536);
        LauncherGfx.text(ctx, play, bx + 15.0f, by + 7.0f - 3.3f, -16045536, 6.6f, true);
        this.hits.add(new Hit(bx, by, w1, 14.0f, () -> {
            this.popAt = System.currentTimeMillis();
        }));
        String turn = Lang.t("Drehen", "Turn");
        float w2 = LauncherGfx.width(turn, 6.6f, true) + 14.0f;
        boolean o2 = PvpFxPanel.in(mouseX, mouseY, bx += w1 + 5.0f, by, w2, 14.0f);
        Shapes.round(ctx, bx, by, w2, 14.0f, 5.0f, this.spin ? -434619853 : (o2 ? -433833933 : -870701533));
        if (this.spin) {
            Shapes.ring(ctx, bx, by, w2, 14.0f, 5.0f, -2005012282);
        }
        LauncherGfx.text(ctx, turn, bx + 7.0f, by + 7.0f - 3.3f, this.spin ? -8523578 : -1, 6.6f, true);
        this.hits.add(new Hit(bx, by, w2, 14.0f, () -> {
            this.spin = !this.spin;
        }));
        String lab = Lang.t("Partikel", "Particles");
        String on = Lang.t("An", "On");
        String off = Lang.t("Aus", "Off");
        float sOn = LauncherGfx.width(on, 6.2f, true) + 10.0f;
        float sOff = LauncherGfx.width(off, 6.2f, true) + 10.0f;
        float sl = LauncherGfx.width(lab, 6.0f, false) + 8.0f;
        float sw = sl + sOn + sOff + 4.0f;
        float sx = (float)(px + pw - 7) - sw;
        if (sx > bx + w2 + 4.0f) {
            Shapes.round(ctx, sx, by, sw, 14.0f, 5.0f, -870701533);
            LauncherGfx.text(ctx, lab, sx + 5.0f, by + 7.0f - 3.2f, -7363684, 6.0f, false);
            float ox = sx + sl;
            Shapes.round(ctx, ox, by + 2.0f, sOn, 10.0f, 4.0f, this.previewParticles ? -13874621 : 0);
            LauncherGfx.text(ctx, on, ox + 5.0f, by + 7.0f - 3.2f, this.previewParticles ? -1 : -7363684, 6.2f, true);
            this.hits.add(new Hit(ox, by, sOn, 14.0f, () -> {
                this.previewParticles = true;
            }));
            float fx = ox + sOn + 2.0f;
            Shapes.round(ctx, fx, by + 2.0f, sOff, 10.0f, 4.0f, this.previewParticles ? 0 : -13874621);
            LauncherGfx.text(ctx, off, fx + 5.0f, by + 7.0f - 3.2f, this.previewParticles ? -7363684 : -1, 6.2f, true);
            this.hits.add(new Hit(fx, by, sOff, 14.0f, () -> {
                this.previewParticles = false;
            }));
        }
        float ty = py + ph + 6;
        LauncherGfx.text(ctx, LauncherGfx.fit(Lang.t("Gleiche Box-Figur wie im Spiel. Ziehen zum Drehen.", "The same box figure as in the game. Drag to turn."), 6.0f, false, pw), px, ty, -7363684, 6.0f, false);
        String reset = Lang.t("Zur\u00fccksetzen", "Reset");
        float rw = LauncherGfx.width(reset, 6.8f, true) + 16.0f;
        float ry = ty + 12.0f;
        if (ry + 15.0f <= (float)(this.y + this.height)) {
            boolean o3 = PvpFxPanel.in(mouseX, mouseY, px, ry, rw, 15.0f);
            Shapes.round(ctx, px, ry, rw, 15.0f, 5.0f, o3 ? 0x26FFFFFF : 0x16FFFFFF);
            Shapes.ring(ctx, px, ry, rw, 15.0f, 5.0f, o3 ? -2005012282 : 0x24FFFFFF);
            LauncherGfx.text(ctx, reset, px + 8, ry + 7.5f - 3.4f, -1, 6.8f, true);
            this.hits.add(new Hit(px, ry, rw, 15.0f, () -> this.confirm.ask((Component)Component.literal((String)Lang.t("Alle Totem-Pop-Einstellungen zur\u00fccksetzen?", "Reset all totem pop settings?")), "fwm.confirm.confirm", this::resetPop)));
        }
    }

    private void resetPop() {
        V2 c = V2.INSTANCE;
        c.totemPopOn = false;
        c.totemColorA = 0xFFD000;
        c.totemColorB = 8253638;
        c.popChamsOn = true;
        c.popChamsColor = 10181887;
        c.popChamsTenths = 10;
        c.popParticles = true;
        c.popParticleAmount = 100;
        c.popChamsMode = 2;
        c.popChamsFillAlpha = 38;
        c.popChamsEdgeColor = -1;
        c.popChamsEdge = 2;
        V2.save();
        this.editing = -1;
        this.rebuild();
    }

    private void figure(GuiGraphicsExtractor ctx, float cx, float base, float s, float fade) {
        V2 c = V2.INSTANCE;
        int mode = c.popChamsMode;
        boolean drawFill = mode != 1;
        boolean drawEdge = mode != 0;
        int rgb = c.popChamsColor & 0xFFFFFF;
        int edgeRgb = PvpFx.edgeColor();
        int fillA = Math.round((float)PvpFx.fillAlpha() * fade);
        int edgeA = Math.round(224.0f * fade);
        double a = Math.toRadians(this.yaw);
        double pitch = Math.toRadians(14.0);
        float ca = (float)Math.cos(a);
        float sa = (float)Math.sin(a);
        float cp = (float)Math.cos(pitch);
        float sp = (float)Math.sin(pitch);
        int parts = PvpFx.PARTS.length;
        float[] pts = new float[parts * 6 * 8 + parts * 12 * 8];
        int[] cols = new int[parts * 6 + parts * 12];
        float[] depth = new float[parts * 6];
        int n = 0;
        float[] sx = new float[8];
        float[] sy = new float[8];
        float[] sd = new float[8];
        int[][] faces = new int[][]{{0, 1, 3, 2}, {4, 5, 7, 6}, {0, 4, 6, 2}, {1, 5, 7, 3}, {2, 3, 7, 6}, {0, 1, 5, 4}};
        float[] shade = new float[]{0.82f, 0.82f, 0.7f, 0.7f, 1.0f, 0.6f};
        int[][] edges = new int[][]{{0, 1}, {2, 3}, {4, 5}, {6, 7}, {0, 2}, {1, 3}, {4, 6}, {5, 7}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};
        float[][] proj = new float[parts][24];
        for (int pi = 0; pi < parts; ++pi) {
            float[] b = PvpFx.PARTS[pi];
            for (int k = 0; k < 8; ++k) {
                float X = (k & 1) == 0 ? b[0] : b[3];
                float Y = (k & 2) == 0 ? b[1] : b[4];
                float Z = (k & 4) == 0 ? b[2] : b[5];
                float xr = X * ca + Z * sa;
                float zr = -X * sa + Z * ca;
                sx[k] = cx + xr * s;
                sy[k] = base + (-Y * cp + zr * sp) * s;
                sd[k] = zr * cp + Y * sp;
                proj[pi][k * 3] = sx[k];
                proj[pi][k * 3 + 1] = sy[k];
                proj[pi][k * 3 + 2] = sd[k];
            }
            if (!drawFill || fillA <= 0) continue;
            for (int f = 0; f < 6; ++f) {
                int[] q = faces[f];
                int o = n * 8;
                float d = 0.0f;
                for (int k = 0; k < 4; ++k) {
                    pts[o + k * 2] = sx[q[k]];
                    pts[o + k * 2 + 1] = sy[q[k]];
                    d += sd[q[k]];
                }
                int r = Math.round((float)(rgb >> 16 & 0xFF) * shade[f]);
                int g = Math.round((float)(rgb >> 8 & 0xFF) * shade[f]);
                int bl = Math.round((float)(rgb & 0xFF) * shade[f]);
                cols[n] = fillA << 24 | r << 16 | g << 8 | bl;
                depth[n] = d;
                ++n;
            }
        }
        if (n > 1) {
            Integer[] order = new Integer[n];
            for (int i2 = 0; i2 < n; ++i2) {
                order[i2] = i2;
            }
            float[] dd = depth;
            Arrays.sort(order, (i, j) -> Float.compare(dd[i], dd[j]));
            float[] p2 = new float[pts.length];
            int[] c2 = new int[cols.length];
            for (int i3 = 0; i3 < n; ++i3) {
                System.arraycopy(pts, order[i3] * 8, p2, i3 * 8, 8);
                c2[i3] = cols[order[i3]];
            }
            pts = p2;
            cols = c2;
        }
        if (drawEdge && edgeA > 0) {
            float half = Math.max(0.5f, PvpFx.edgeHalf() * s);
            int ec = edgeA << 24 | edgeRgb;
            for (int pi = 0; pi < parts; ++pi) {
                for (int[] e : edges) {
                    float x1 = proj[pi][e[1] * 3];
                    float x0 = proj[pi][e[0] * 3];
                    float dx = x1 - x0;
                    float y1 = proj[pi][e[1] * 3 + 1];
                    float y0 = proj[pi][e[0] * 3 + 1];
                    float dy = y1 - y0;
                    float len = (float)Math.sqrt(dx * dx + dy * dy);
                    if (len < 0.01f) continue;
                    float nx = -(dy /= len) * half;
                    float ny = (dx /= len) * half;
                    x1 += dx * half;
                    y1 += dy * half;
                    int o = n * 8;
                    pts[o] = (x0 -= dx * half) + nx;
                    pts[o + 1] = (y0 -= dy * half) + ny;
                    pts[o + 2] = x0 - nx;
                    pts[o + 3] = y0 - ny;
                    pts[o + 4] = x1 - nx;
                    pts[o + 5] = y1 - ny;
                    pts[o + 6] = x1 + nx;
                    pts[o + 7] = y1 + ny;
                    cols[n] = ec;
                    ++n;
                }
            }
        }
        Shapes.quads(ctx, pts, cols, n);
    }

    private void particles(GuiGraphicsExtractor ctx, float cx, float cy, float s, long age) {
        V2 c = V2.INSTANCE;
        float t = (float)age / 1000.0f;
        if (t > 1.6f) {
            return;
        }
        int count = Math.round((float)(60 * Math.max(0, Math.min(100, c.popParticleAmount))) / 100.0f);
        Random r = new Random(this.popAt);
        float[] pts = new float[count * 8];
        int[] cols = new int[count];
        int n = 0;
        for (int i = 0; i < count; ++i) {
            int rgb;
            double ang = r.nextDouble() * Math.PI * 2.0;
            float speed = (10.0f + r.nextFloat() * 18.0f) * s;
            float vx = (float)Math.cos(ang) * speed;
            float vy = (float)Math.sin(ang) * speed * 0.8f - 8.0f * s;
            float life = 0.9f + r.nextFloat() * 0.7f;
            if (c.totemPopOn) {
                rgb = r.nextBoolean() ? c.totemColorA : c.totemColorB;
            } else {
                int n2 = rgb = r.nextBoolean() ? 14217802 : 15910970;
            }
            if (t > life) continue;
            float k = 1.0f - (float)Math.exp(-t * 2.2f);
            float x = cx + vx * k / 2.2f;
            float y = cy + vy * k / 2.2f + 6.0f * s * t * t;
            float half = Math.max(0.8f, s * 0.6f);
            int alpha = Math.round(255.0f * (1.0f - t / life));
            int o = n * 8;
            pts[o] = x - half;
            pts[o + 1] = y - half;
            pts[o + 2] = x - half;
            pts[o + 3] = y + half;
            pts[o + 4] = x + half;
            pts[o + 5] = y + half;
            pts[o + 6] = x + half;
            pts[o + 7] = y - half;
            cols[n] = alpha << 24 | rgb & 0xFFFFFF;
            ++n;
        }
        Shapes.quads(ctx, pts, cols, n);
    }

    private static boolean in(double mx, double my, float x, float y, float w, float h) {
        return mx >= (double)x && mx < (double)(x + w) && my >= (double)y && my < (double)(y + h);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.preview != null && !this.confirm.open()) {
            for (Hit h : this.hits) {
                if (!PvpFxPanel.in(mouseX, mouseY, h.x(), h.y(), h.w(), h.h())) continue;
                h.action().run();
                return true;
            }
            if (button == 0 && PvpFxPanel.in(mouseX, mouseY, this.preview[0], this.preview[1], this.preview[2], this.preview[3])) {
                this.dragging = true;
                this.dragFromX = mouseX;
                this.dragYaw = this.yaw;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        if (this.dragging) {
            this.yaw = this.dragYaw + (float)(mouseX - this.dragFromX) * 1.6f;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased() {
        this.dragging = false;
        super.mouseReleased();
    }

    private void colour(Component label, int id, IntSupplier get, IntConsumer set) {
        this.rows.add(new HexRow(label, () -> (this.editing == id ? ColorPickRow.color(this.pickHue, this.pickShade) : get.getAsInt()) & 0xFFFFFF, v -> {
            set.accept(v & 0xFFFFFF);
            V2.save();
        }, () -> {
            if (this.editing == id) {
                this.editing = -1;
            } else {
                this.editing = id;
                this.readPick(get.getAsInt());
            }
            this.rebuild();
        }));
        if (this.editing != id) {
            return;
        }
        this.rows.add(new ColorPickRow(PvpFxPanel.t("Farbton", "Hue"), false, () -> this.pickHue, () -> this.pickHue, v -> {
            this.pickHue = v;
        }, null));
        this.rows.add(new ColorPickRow(PvpFxPanel.t("Helligkeit", "Shade"), true, () -> this.pickHue, () -> this.pickShade, v -> {
            this.pickShade = v;
        }, () -> {
            set.accept(ColorPickRow.color(this.pickHue, this.pickShade) & 0xFFFFFF);
            V2.save();
            this.editing = -1;
            this.rebuild();
        }));
    }

    @Override
    public Component title() {
        return switch (this.mode) {
            case 0 -> PvpFxPanel.t("Hit-Farbe", "Hit colour");
            case 1 -> PvpFxPanel.t("Totem-Pop", "Totem pop");
            default -> PvpFxPanel.t("Totem hochhalten", "Hold up totem");
        };
    }

    private static Component t(String de, String en) {
        return Component.literal((String)Lang.t(de, en));
    }

    @Override
    protected void buildRows() {
        V2 c = V2.INSTANCE;
        switch (this.mode) {
            case 0: {
                this.rows.add(new ToggleRow(PvpFxPanel.t("Eigene Farbe beim Treffen", "Own colour on a hit"), () -> c.hitColorOn, v -> {
                    c.hitColorOn = v;
                    V2.save();
                    PvpFx.applyHitColor();
                }).withIcon(new ItemStack((ItemLike)Items.NETHERITE_SWORD)));
                this.colour(PvpFxPanel.t("Farbe", "Colour"), 0, () -> c.hitColor, v -> {
                    c.hitColor = v;
                    PvpFx.applyHitColor();
                });
                this.rows.add(new SliderRow(PvpFxPanel.t("St\u00e4rke", "Strength"), 20, 255, () -> c.hitAlpha, v -> {
                    c.hitAlpha = v;
                    V2.save();
                    PvpFx.applyHitColor();
                }, v -> Component.literal((String)(Math.round((float)v / 2.55f) + "%"))));
                this.rows.add(new TextRow(PvpFxPanel.t("F\u00e4rbt jeden getroffenen Spieler und Mob statt rot.", "Tints every player and mob you hit instead of red."), null));
                break;
            }
            case 1: {
                this.rows.add(new TextRow(PvpFxPanel.t("POP-PARTIKEL", "POP PARTICLES")));
                this.rows.add(new ToggleRow(PvpFxPanel.t("Pop-Partikel anzeigen", "Show pop particles"), () -> c.popParticles, v -> {
                    c.popParticles = v;
                    V2.save();
                }).withIcon(new ItemStack((ItemLike)Items.TOTEM_OF_UNDYING)));
                this.rows.add(new ToggleRow(PvpFxPanel.t("Eigene Pop-Farben", "Own pop colours"), () -> c.totemPopOn, v -> {
                    c.totemPopOn = v;
                    V2.save();
                }));
                this.colour(PvpFxPanel.t("Farbe 1", "Colour 1"), 2, () -> c.totemColorA, v -> {
                    c.totemColorA = v;
                });
                this.colour(PvpFxPanel.t("Farbe 2", "Colour 2"), 3, () -> c.totemColorB, v -> {
                    c.totemColorB = v;
                });
                this.rows.add(new SliderRow(PvpFxPanel.t("Menge", "Amount"), 10, 100, () -> c.popParticleAmount, v -> {
                    c.popParticleAmount = v;
                    V2.save();
                }, v -> Component.literal((String)(v + " %"))));
                this.rows.add(new TextRow(PvpFxPanel.t("POP CHAMS", "POP CHAMS")));
                this.rows.add(new ToggleRow(PvpFxPanel.t("Pop Chams anzeigen", "Show pop chams"), () -> c.popChamsOn, v -> {
                    c.popChamsOn = v;
                    V2.save();
                }).withIcon(new ItemStack((ItemLike)Items.TOTEM_OF_UNDYING)));
                this.rows.add(new ChipsRow(PvpFxPanel.t("Darstellung", "Look"), List.of(new ChipsRow.Chip(Lang.t("Gef\u00fcllt", "Filled"), 0), new ChipsRow.Chip(Lang.t("Nur Umriss", "Outline"), 1), new ChipsRow.Chip(Lang.t("Beides", "Both"), 2)), () -> c.popChamsMode, v -> {
                    c.popChamsMode = v;
                    V2.save();
                }));
                this.colour(PvpFxPanel.t("F\u00fcllfarbe", "Fill colour"), 1, () -> c.popChamsColor, v -> {
                    c.popChamsColor = v;
                });
                this.rows.add(new SliderRow(PvpFxPanel.t("Deckkraft", "Opacity"), 5, 100, () -> c.popChamsFillAlpha, v -> {
                    c.popChamsFillAlpha = v;
                    V2.save();
                }, v -> Component.literal((String)(v + " %"))));
                this.colour(PvpFxPanel.t("Umrissfarbe", "Outline colour"), 4, PvpFx::edgeColor, v -> {
                    c.popChamsEdgeColor = v;
                });
                this.rows.add(new SliderRow(PvpFxPanel.t("Umrissdicke", "Outline width"), 1, 5, () -> c.popChamsEdge, v -> {
                    c.popChamsEdge = v;
                    V2.save();
                }, v -> Component.literal((String)(v + " px"))));
                this.rows.add(new SliderRow(PvpFxPanel.t("Anzeigedauer", "Duration"), 3, 30, () -> c.popChamsTenths, v -> {
                    c.popChamsTenths = v;
                    V2.save();
                }, v -> Component.literal((String)String.format(Locale.GERMANY, "%.1f s", Float.valueOf((float)v / 10.0f)))));
                break;
            }
            default: {
                this.rows.add(new ToggleRow(PvpFxPanel.t("Arm mit Totem nach vorne", "Arm with a totem forward"), () -> c.totemHoldUp, v -> {
                    c.totemHoldUp = v;
                    V2.save();
                }).withIcon(new ItemStack((ItemLike)Items.TOTEM_OF_UNDYING)));
                this.rows.add(new TextRow(PvpFxPanel.t("Gilt f\u00fcr alle Spieler, in der Ansicht von au\u00dfen (F5).", "For every player, seen from outside (F5)."), null));
            }
        }
    }

    private record Hit(float x, float y, float w, float h, Runnable action) {
    }
}

