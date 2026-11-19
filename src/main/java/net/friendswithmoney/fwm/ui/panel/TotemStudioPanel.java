/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.network.chat.Component
 *  org.joml.Matrix3x2fStack
 */
package net.friendswithmoney.fwm.ui.panel;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import javax.imageio.ImageIO;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.textures.SkyStudio;
import net.friendswithmoney.fwm.textures.TextureMixer;
import net.friendswithmoney.fwm.textures.TotemStudio;
import net.friendswithmoney.fwm.ui.Design;
import net.friendswithmoney.fwm.ui.FwmTheme;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.gfx.FwmFont;
import net.friendswithmoney.fwm.ui.gfx.ImageTex;
import net.friendswithmoney.fwm.ui.gfx.Shapes;
import net.friendswithmoney.fwm.ui.panel.PanelHost;
import net.friendswithmoney.fwm.ui.panel.StudioBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;

public class TotemStudioPanel
extends StudioBase {
    private static final int SKIN = 0;
    private static final int PNG = 1;
    private static final int PIXEL = 2;
    private static final int PEN = 0;
    private static final int ERASER = 1;
    private static final int FILL = 2;
    private static final int PICK = 3;
    private static final int[] PALETTE = new int[]{-1, -3750202, -9737365, -15329766, -8762840, -3761043, -665688, -7578, -673493, -2065889, -2999236, -7464165, -11747222, -14714310, -8523578, -12605472, -14004288, -7643914, -2073419, -13948109};
    private final PanelHost host;
    private final boolean sub;
    private int tab = 0;
    private int size;
    private int[] px;
    private int version;
    private BufferedImage skin;
    private String skinState = "";
    private boolean loadingSkin;
    private BufferedImage picture;
    private String pictureName = "";
    private boolean removeBg = true;
    private int colors = 0;
    private boolean pngOutline = false;
    private int tool = 0;
    private boolean mirror;
    private int color = -673493;
    private final ArrayDeque<int[]> undo = new ArrayDeque();
    private final ArrayDeque<int[]> redo = new ArrayDeque();
    private boolean painting;
    private int lastCell = -1;
    private int view = 0;
    private float yaw = -30.0f;
    private boolean turning;
    private double turnFromX;
    private float turnYaw;
    private long popAt = System.currentTimeMillis();
    private EditBox title;
    private EditBox skinName;
    private EditBox hex;
    private float[] canvas = new float[4];
    private float[] previewBox = new float[4];
    private boolean busy;
    private final Map<String, BufferedImage> thumbs = new HashMap<String, BufferedImage>();

    public TotemStudioPanel(PanelHost host, boolean sub) {
        this.host = host;
        this.sub = sub;
        V2 c = V2.INSTANCE;
        this.size = c.totemSize == 32 || c.totemSize == 64 ? c.totemSize : 16;
        this.px = TotemStudio.vanilla(this.size);
    }

    @Override
    public Component title() {
        return Component.literal((String)"Totem Studio");
    }

    private float leftW() {
        return Math.max(132.0f, Math.min(170.0f, (float)this.width * 0.24f));
    }

    private float rightW() {
        return Math.max(132.0f, Math.min(170.0f, (float)this.width * 0.24f));
    }

    private float bodyY() {
        return this.y + 24;
    }

    private float footY() {
        return this.y + this.height - 17;
    }

    @Override
    protected void onResize() {
        Font font = FwmFont.ui();
        String t0 = this.title == null ? this.defaultTitle() : this.title.getValue();
        String s0 = this.skinName == null ? V2.INSTANCE.totemSkin : this.skinName.getValue();
        String h0 = this.hex == null ? TotemStudioPanel.hexOf(this.color) : this.hex.getValue();
        this.detachAll();
        float rw = this.rightW();
        this.title = this.field(font, Math.round((float)(this.x + this.width) - rw + 6.0f), this.y + 4, Math.round(rw - 12.0f), 32, Lang.t("Name des Totems", "Totem name"));
        this.title.setValue(t0);
        this.skinName = this.field(font, this.x + 6, Math.round(this.bodyY() + 14.0f), Math.round(this.leftW() - 56.0f), 16, Lang.t("Spielername", "Player name"));
        this.skinName.setValue(s0);
        this.hex = this.field(font, this.x + 30, 0, 52, 7, "#RRGGBB");
        this.hex.setValue(h0);
        this.hex.setResponder(s -> {
            Integer v = TotemStudioPanel.parseHex(s);
            if (v != null) {
                this.color = v;
            }
        });
        this.showFields();
    }

    private String defaultTitle() {
        String n = Minecraft.getInstance().getUser() == null ? "" : Minecraft.getInstance().getUser().getName();
        return "FWM Totem " + (n == null || n.isEmpty() ? "Eigenes" : n);
    }

    private EditBox field(Font font, int fx, int fy, int w, int max, String hint) {
        EditBox f = new EditBox(font, fx, fy, Math.max(20, w), 10, (Component)Component.literal((String)hint));
        f.setBordered(false);
        f.setMaxLength(max);
        f.setTextColor(FwmTheme.TEXT);
        f.setHint((Component)Component.literal((String)hint));
        return f;
    }

    private void detachAll() {
        if (this.title != null) {
            this.host.detach((AbstractWidget)this.title);
        }
        if (this.skinName != null) {
            this.host.detach((AbstractWidget)this.skinName);
        }
        if (this.hex != null) {
            this.host.detach((AbstractWidget)this.hex);
        }
    }

    private void showFields() {
        this.detachAll();
        this.host.attach((AbstractWidget)this.title);
        if (this.tab == 0) {
            this.host.attach((AbstractWidget)this.skinName);
        }
        if (this.tab == 2) {
            this.host.attach((AbstractWidget)this.hex);
        }
    }

    @Override
    public void onHidden() {
        this.detachAll();
        ImageTex.release("totem/");
    }

    private void changed() {
        ++this.version;
    }

    private void rebuild() {
        V2 c = V2.INSTANCE;
        if (this.tab == 0 && this.skin != null) {
            this.px = TotemStudio.fromSkin(this.skin, this.size, c.totemHat, c.totemOutline);
        } else if (this.tab == 1 && this.picture != null) {
            this.px = TotemStudio.fromPicture(this.picture, this.size, this.removeBg, this.colors, this.pngOutline);
        }
        this.changed();
    }

    private void setSize(int s) {
        if (s == this.size) {
            return;
        }
        V2.INSTANCE.totemSize = s;
        V2.save();
        if (this.tab == 2 || this.tab == 0 && this.skin == null || this.tab == 1 && this.picture == null) {
            this.pushUndo();
            this.px = TotemStudio.resize(this.px, this.size, s);
            this.size = s;
            this.changed();
        } else {
            this.size = s;
            this.rebuild();
        }
    }

    private boolean threeD() {
        return this.tab == 0 && this.skin != null && V2.INSTANCE.totem3d;
    }

    @Override
    protected void draw(GuiGraphicsExtractor ctx, Font font, int mx, int my, float delta) {
        String[] tabs = new String[]{Lang.t("Aus Skin", "From skin"), Lang.t("Aus PNG", "From PNG"), Lang.t("Selbst pixeln", "Pixel it")};
        float tw = 4.0f;
        for (String s : tabs) {
            tw += TotemStudioPanel.tw(s, 6.8f, true) + 16.0f;
        }
        float lw = this.leftW();
        float rw = this.rightW();
        this.chips(ctx, tabs, this.tab, (float)this.x + ((float)this.width - tw) / 2.0f, this.y + 1, 17.0f, mx, my, this::switchTab);
        float fx = (float)(this.x + this.width) - rw;
        TotemStudioPanel.r(ctx, fx, this.y + 1, rw, 17.0f, 6.0f, 0x30000000);
        TotemStudioPanel.ring(ctx, fx, this.y + 1, rw, 17.0f, 6.0f, this.title.isFocused() ? -2005012282 : 0x14FFFFFF);
        this.title.setX(Math.round(fx + 7.0f));
        this.title.setY(this.y + 5);
        this.title.setWidth(Math.round(rw - 14.0f));
        this.title.extractWidgetRenderState(ctx, mx, my, 0.0f);
        float by = this.bodyY();
        if (this.tab == 0) {
            this.drawSkinSide(ctx, this.x, by, lw, mx, my);
        } else if (this.tab == 1) {
            this.drawPngSide(ctx, this.x, by, lw, mx, my);
        } else {
            this.drawPixelSide(ctx, this.x, by, lw, mx, my);
        }
        float cx0 = (float)this.x + lw + 12.0f;
        float cw = (float)this.width - lw - rw - 24.0f;
        float side = Math.min(cw, this.footY() - by - 20.0f);
        float sx = cx0 + (cw - side) / 2.0f;
        float sy = by;
        TotemStudioPanel.r(ctx, sx - 6.0f, sy - 6.0f, side + 12.0f, side + 12.0f, 8.0f, 0x26000000);
        TotemStudioPanel.ring(ctx, sx - 6.0f, sy - 6.0f, side + 12.0f, side + 12.0f, 8.0f, 0x14FFFFFF);
        this.canvas = new float[]{sx, sy, side, side};
        TotemStudioPanel.checker(ctx, sx, sy, side, side, side / (float)Math.min(16, this.size));
        ImageTex.draw(ctx, "totem/canvas", this.px, this.size, this.size, this.version, Math.round(sx), Math.round(sy), Math.round(side), Math.round(side));
        if (this.tab == 2) {
            this.grid(ctx, sx, sy, side);
        }
        String cap = this.tab == 0 ? (this.threeD() ? Lang.t("3D: der Skin wird als kleine Figur gebaut \u00b7 das Raster ist die flache Ansicht", "3D: the skin becomes a little figure \u00b7 the grid is the flat view") : Lang.t("Kopf, K\u00f6rper, Arme und Beine werden automatisch aus dem Skin gesetzt", "Head, body, arms and legs are set from the skin automatically")) : (this.tab == 1 ? Lang.t("Zugeschnitten, Hintergrund weg, auf " + this.size + " \u00d7 " + this.size + " Pixel gebracht", "Cropped, background removed, made " + this.size + " \u00d7 " + this.size + " pixels") : Lang.t("Linke Maustaste malt \u00b7 Rechte radiert \u00b7 Strg+Z r\u00fcckg\u00e4ngig", "Left button paints \u00b7 right erases \u00b7 Ctrl+Z undo"));
        cap = TotemStudioPanel.fit(cap, 6.2f, false, cw);
        TotemStudioPanel.t(ctx, cap, cx0 + (cw - TotemStudioPanel.tw(cap, 6.2f, false)) / 2.0f, sy + side + 9.0f, -7362144, 6.2f, false);
        this.drawRight(ctx, (float)(this.x + this.width) - rw, by, rw, mx, my);
        float fy = this.footY();
        String reset = Lang.t("Zur\u00fccksetzen", "Reset");
        boolean overReset = TotemStudioPanel.in(mx, my, this.x, fy, TotemStudioPanel.tw(reset, 7.0f, true) + 8.0f, 16.0f);
        TotemStudioPanel.tc(ctx, reset, this.x + 4, fy + 8.0f, overReset ? -1 : -4601405, 7.0f, true);
        this.hits.add(new StudioBase.Hit(this.x, fy, TotemStudioPanel.tw(reset, 7.0f, true) + 8.0f, 16.0f, this::reset));
        String apply = this.busy ? Lang.t("Wird gebaut \u2026", "Building \u2026") : Lang.t("Anwenden", "Apply");
        float aw = TotemStudioPanel.tw(apply, 7.2f, true) + 26.0f;
        this.primary(ctx, apply, (float)(this.x + this.width) - aw, fy, aw, 16.0f, mx, my, !this.busy && !TextureMixer.building(), this::apply);
        String cancel = Lang.t("Abbrechen", "Cancel");
        float cwid = TotemStudioPanel.tw(cancel, 7.0f, true) + 22.0f;
        this.ghost(ctx, cancel, (float)(this.x + this.width) - aw - 6.0f - cwid, fy, cwid, 16.0f, mx, my, this::leave);
        this.drawNote(ctx, (float)this.x + (float)this.width / 2.0f, fy - 2.0f);
    }

    private void grid(GuiGraphicsExtractor ctx, float sx, float sy, float side) {
        float cell = side / (float)this.size;
        if (cell < 4.0f) {
            return;
        }
        for (int i = 1; i < this.size; ++i) {
            int a = i % 8 == 0 ? 0x30FFFFFF : 0x14FFFFFF;
            TotemStudioPanel.r(ctx, sx + (float)i * cell - 0.3f, sy, 0.6f, side, 0.0f, a);
            TotemStudioPanel.r(ctx, sx, sy + (float)i * cell - 0.3f, side, 0.6f, 0.0f, a);
        }
        if (this.mirror) {
            TotemStudioPanel.r(ctx, sx + side / 2.0f - 0.5f, sy, 1.0f, side, 0.0f, -2005012282);
        }
    }

    private void drawSkinSide(GuiGraphicsExtractor ctx, float lx, float ly, float lw, int mx, int my) {
        V2 c = V2.INSTANCE;
        TotemStudioPanel.head(ctx, "SKIN", lx, ly);
        float fy = ly + 10.0f;
        TotemStudioPanel.r(ctx, lx, fy, lw - 50.0f, 17.0f, 6.0f, 0x30000000);
        TotemStudioPanel.ring(ctx, lx, fy, lw - 50.0f, 17.0f, 6.0f, this.skinName.isFocused() ? -2005012282 : 0x14FFFFFF);
        this.skinName.setX(Math.round(lx + 6.0f));
        this.skinName.setY(Math.round(fy + 4.0f));
        this.skinName.extractWidgetRenderState(ctx, mx, my, 0.0f);
        this.primary(ctx, this.loadingSkin ? "\u2026" : Lang.t("Laden", "Load"), lx + lw - 46.0f, fy, 46.0f, 17.0f, mx, my, !this.loadingSkin, this::loadSkin);
        float cy = fy + 23.0f;
        TotemStudioPanel.r(ctx, lx, cy, lw, 46.0f, 7.0f, 0x24000000);
        TotemStudioPanel.ring(ctx, lx, cy, lw, 46.0f, 7.0f, 0x14FFFFFF);
        if (this.skin != null) {
            int[] face = TotemStudio.face(this.skin);
            ImageTex.draw(ctx, "totem/face", face, 8, 8, System.identityHashCode(this.skin), Math.round(lx + 6.0f), Math.round(cy + 6.0f), 34, 34);
            TotemStudioPanel.t(ctx, TotemStudioPanel.fit(this.skinState, 7.2f, true, lw - 52.0f), lx + 46.0f, cy + 8.0f, -1, 7.2f, true);
            TotemStudioPanel.t(ctx, Lang.t("Skin geladen", "Skin loaded"), lx + 46.0f, cy + 18.0f, -7362144, 6.0f, false);
            TotemStudioPanel.t(ctx, Lang.t("oder Skin-PNG reinziehen", "or drop a skin PNG"), lx + 46.0f, cy + 30.0f, -7362144, 6.0f, false);
        } else {
            TotemStudioPanel.r(ctx, lx + 6.0f, cy + 6.0f, 34.0f, 34.0f, 4.0f, 0x20FFFFFF);
            Shapes.icon(ctx, "smile", lx + 15.0f, cy + 15.0f, 16.0f, -7362144);
            TotemStudioPanel.t(ctx, TotemStudioPanel.fit(this.skinState.isEmpty() ? Lang.t("Noch kein Skin", "No skin yet") : this.skinState, 7.0f, true, lw - 52.0f), lx + 46.0f, cy + 10.0f, -1, 7.0f, true);
            TotemStudioPanel.t(ctx, Lang.t("Name eingeben oder", "Type a name or"), lx + 46.0f, cy + 21.0f, -7362144, 6.0f, false);
            TotemStudioPanel.t(ctx, Lang.t("Skin-PNG reinziehen", "drop a skin PNG"), lx + 46.0f, cy + 30.0f, -7362144, 6.0f, false);
        }
        float sy = cy + 56.0f;
        TotemStudioPanel.head(ctx, Lang.t("STIL", "STYLE"), lx, sy);
        TotemStudioPanel.tc(ctx, Lang.t("Darstellung", "Look"), lx, (sy += 9.0f) + 9.0f, -1, 7.2f, true);
        String[] d = new String[]{"2D", "3D"};
        float dw = 4.0f + TotemStudioPanel.tw("2D", 6.8f, true) + 16.0f + TotemStudioPanel.tw("3D", 6.8f, true) + 16.0f;
        this.chips(ctx, d, c.totem3d ? 1 : 0, lx + lw - dw, sy + 2.0f, 15.0f, mx, my, i -> {
            c.totem3d = i == 1;
            V2.save();
            this.changed();
        });
        TotemStudioPanel.r(ctx, lx, sy + 18.0f, lw, 0.6f, 0.0f, 0x14FFFFFF);
        this.toggleRow(ctx, Lang.t("Hut-Schicht", "Hat layer"), lx, sy += 20.0f, lw, c.totemHat, () -> {
            c.totemHat = !c.totemHat;
            V2.save();
            this.rebuild();
        });
        this.toggleRow(ctx, Lang.t("Umriss", "Outline"), lx, sy += 20.0f, lw, c.totemOutline, () -> {
            c.totemOutline = !c.totemOutline;
            V2.save();
            this.rebuild();
        });
        this.sizeRow(ctx, lx, sy += 22.0f, lw, mx, my);
    }

    private void sizeRow(GuiGraphicsExtractor ctx, float lx, float sy, float lw, int mx, int my) {
        TotemStudioPanel.tc(ctx, Lang.t("Gr\u00f6\u00dfe", "Size"), lx, sy + 8.0f, -1, 7.2f, true);
        String[] s = new String[]{"16", "32", "64"};
        float w = 4.0f;
        for (String l : s) {
            w += TotemStudioPanel.tw(l, 6.8f, true) + 16.0f;
        }
        this.chips(ctx, s, this.size == 16 ? 0 : (this.size == 32 ? 1 : 2), lx + lw - w, sy + 1.0f, 15.0f, mx, my, i -> this.setSize(i == 0 ? 16 : (i == 1 ? 32 : 64)));
        TotemStudioPanel.t(ctx, Lang.t("32 / 64 = HD", "32 / 64 = HD"), lx, sy + 19.0f, -7362144, 5.8f, false);
    }

    private void drawPngSide(GuiGraphicsExtractor ctx, float lx, float ly, float lw, int mx, int my) {
        TotemStudioPanel.head(ctx, Lang.t("BILD", "PICTURE"), lx, ly);
        float cy = ly + 10.0f;
        boolean over = TotemStudioPanel.in(mx, my, lx, cy, lw, 62.0f);
        TotemStudioPanel.r(ctx, lx, cy, lw, 62.0f, 7.0f, over ? 511570118 : 0x24000000);
        TotemStudioPanel.ring(ctx, lx, cy, lw, 62.0f, 7.0f, over ? -2005012282 : 0x30FFFFFF);
        if (this.picture != null) {
            float ph = 50.0f;
            float pw = Math.min(lw * 0.4f, ph * (float)this.picture.getWidth() / Math.max(1.0f, (float)this.picture.getHeight()));
            TotemStudioPanel.checker(ctx, lx + 6.0f, cy + 6.0f, pw, ph, 5.0f);
            ImageTex.draw(ctx, "totem/src", this.picture, Math.round(lx + 6.0f), Math.round(cy + 6.0f), Math.round(pw), Math.round(ph));
            TotemStudioPanel.t(ctx, TotemStudioPanel.fit(this.pictureName, 7.0f, true, lw - pw - 18.0f), lx + pw + 12.0f, cy + 12.0f, -1, 7.0f, true);
            TotemStudioPanel.t(ctx, this.picture.getWidth() + " \u00d7 " + this.picture.getHeight(), lx + pw + 12.0f, cy + 23.0f, -7362144, 6.0f, false);
            TotemStudioPanel.t(ctx, Lang.t("Anderes Bild \u2026", "Other picture \u2026"), lx + pw + 12.0f, cy + 40.0f, -8523578, 6.2f, true);
        } else {
            Shapes.icon(ctx, "plus", lx + lw / 2.0f - 6.0f, cy + 12.0f, 12.0f, -8523578);
            String a = Lang.t("Bild w\u00e4hlen \u2026", "Choose a picture \u2026");
            String b = Lang.t("oder PNG/JPG hier reinziehen", "or drop a PNG/JPG here");
            TotemStudioPanel.t(ctx, a, lx + (lw - TotemStudioPanel.tw(a, 7.2f, true)) / 2.0f, cy + 30.0f, -1, 7.2f, true);
            TotemStudioPanel.t(ctx, b, lx + (lw - TotemStudioPanel.tw(b, 6.0f, false)) / 2.0f, cy + 42.0f, -7362144, 6.0f, false);
        }
        this.hits.add(new StudioBase.Hit(lx, cy, lw, 62.0f, this::pickPicture));
        float sy = cy + 72.0f;
        TotemStudioPanel.head(ctx, Lang.t("ANPASSEN", "ADJUST"), lx, sy);
        this.toggleRow(ctx, Lang.t("Hintergrund weg", "Remove background"), lx, sy += 9.0f, lw, this.removeBg, () -> {
            this.removeBg = !this.removeBg;
            this.rebuild();
        });
        this.toggleRow(ctx, Lang.t("Umriss", "Outline"), lx, sy += 20.0f, lw, this.pngOutline, () -> {
            this.pngOutline = !this.pngOutline;
            this.rebuild();
        });
        TotemStudioPanel.tc(ctx, Lang.t("Farben", "Colours"), lx, (sy += 22.0f) + 8.0f, -1, 7.2f, true);
        String[] cs = new String[]{Lang.t("Alle", "All"), "16", "8", "4"};
        float w = 4.0f;
        for (String l : cs) {
            w += TotemStudioPanel.tw(l, 6.8f, true) + 16.0f;
        }
        int idx = this.colors == 0 ? 0 : (this.colors == 16 ? 1 : (this.colors == 8 ? 2 : 3));
        this.chips(ctx, cs, idx, lx + lw - w, sy + 1.0f, 15.0f, mx, my, i -> {
            this.colors = i == 0 ? 0 : (i == 1 ? 16 : (i == 2 ? 8 : 4));
            this.rebuild();
        });
        this.sizeRow(ctx, lx, sy += 22.0f, lw, mx, my);
    }

    private void drawPixelSide(GuiGraphicsExtractor ctx, float lx, float ly, float lw, int mx, int my) {
        TotemStudioPanel.head(ctx, Lang.t("WERKZEUG", "TOOL"), lx, ly);
        String[] names = new String[]{Lang.t("Stift", "Pen"), Lang.t("Radierer", "Eraser"), Lang.t("F\u00fcllen", "Fill"), Lang.t("Pipette", "Picker")};
        float bw = (lw - 4.0f) / 2.0f;
        int i = 0;
        while (i < 4) {
            float bx = lx + (float)(i % 2) * (bw + 4.0f);
            float byy = ly + 10.0f + (float)(i / 2 * 19);
            boolean sel = this.tool == i;
            boolean over = TotemStudioPanel.in(mx, my, bx, byy, bw, 16.0f);
            TotemStudioPanel.r(ctx, bx, byy, bw, 16.0f, 5.0f, sel ? -13874621 : (over ? 0x24FFFFFF : 0x14FFFFFF));
            TotemStudioPanel.ring(ctx, bx, byy, bw, 16.0f, 5.0f, sel ? -2005012282 : 0x14FFFFFF);
            TotemStudioPanel.tc(ctx, names[i], bx + (bw - TotemStudioPanel.tw(names[i], 6.8f, true)) / 2.0f, byy + 8.0f, sel ? -1 : -3549487, 6.8f, true);
            int t = i++;
            this.hits.add(new StudioBase.Hit(bx, byy, bw, 16.0f, () -> {
                this.tool = t;
            }));
        }
        float sy = ly + 50.0f;
        this.toggleRow(ctx, Lang.t("Spiegeln (links/rechts)", "Mirror (left/right)"), lx, sy, lw, this.mirror, () -> {
            this.mirror = !this.mirror;
        });
        float uw = (lw - 4.0f) / 2.0f;
        this.ghost(ctx, Lang.t("\u21b6 Zur\u00fcck", "\u21b6 Undo"), lx, sy += 22.0f, uw, 15.0f, mx, my, this::undo);
        this.ghost(ctx, Lang.t("Vor \u21b7", "Redo \u21b7"), lx + uw + 4.0f, sy, uw, 15.0f, mx, my, this::redo);
        TotemStudioPanel.head(ctx, Lang.t("FARBE", "COLOUR"), lx, sy += 22.0f);
        sy += 9.0f;
        int per = 10;
        float cell = (lw - (float)((per - 1) * 2)) / (float)per;
        for (int i2 = 0; i2 < PALETTE.length; ++i2) {
            float cx = lx + (float)(i2 % per) * (cell + 2.0f);
            float cy = sy + (float)(i2 / per) * (cell + 2.0f);
            TotemStudioPanel.r(ctx, cx, cy, cell, cell, 2.5f, PALETTE[i2]);
            if (PALETTE[i2] == this.color) {
                TotemStudioPanel.ring(ctx, cx - 1.0f, cy - 1.0f, cell + 2.0f, cell + 2.0f, 3.0f, -1);
            }
            int col = PALETTE[i2];
            this.hits.add(new StudioBase.Hit(cx, cy, cell, cell, () -> this.pickColor(col)));
        }
        TotemStudioPanel.r(ctx, lx, sy += 2.0f * (cell + 2.0f) + 4.0f, 18.0f, 15.0f, 4.0f, this.color);
        TotemStudioPanel.ring(ctx, lx, sy, 18.0f, 15.0f, 4.0f, 0x40FFFFFF);
        TotemStudioPanel.r(ctx, lx + 22.0f, sy, lw - 22.0f, 15.0f, 5.0f, 0x30000000);
        TotemStudioPanel.ring(ctx, lx + 22.0f, sy, lw - 22.0f, 15.0f, 5.0f, this.hex.isFocused() ? -2005012282 : 0x14FFFFFF);
        this.hex.setX(Math.round(lx + 28.0f));
        this.hex.setY(Math.round(sy + 3.0f));
        this.hex.setWidth(Math.round(lw - 34.0f));
        this.hex.extractWidgetRenderState(ctx, mx, my, 0.0f);
        TotemStudioPanel.head(ctx, Lang.t("START", "START"), lx, sy += 22.0f);
        sy += 9.0f;
        String[] starts = new String[]{"Vanilla", "Skin", "PNG", Lang.t("Leer", "Empty")};
        float w = 4.0f;
        for (String l : starts) {
            w += TotemStudioPanel.tw(l, 6.6f, true) + 16.0f;
        }
        this.chips(ctx, starts, -1, lx, sy, 15.0f, mx, my, this::start);
        this.sizeRow(ctx, lx, sy += 21.0f, lw, mx, my);
    }

    private void drawRight(GuiGraphicsExtractor ctx, float rx, float ry, float rw, int mx, int my) {
        List<String> made;
        TotemStudioPanel.head(ctx, Lang.t("VORSCHAU", "PREVIEW"), rx, ry);
        float ph = Math.min(rw * 0.78f, 120.0f);
        float py = ry + 10.0f;
        Shapes.roundGradient(ctx, rx, py, rw, ph, 8.0f, -14663622, -15589347);
        TotemStudioPanel.ring(ctx, rx, py, rw, ph, 8.0f, 0x14FFFFFF);
        this.previewBox = new float[]{rx, py, rw, ph};
        ctx.enableScissor(Math.round(rx), Math.round(py), Math.round(rx + rw), Math.round(py + ph));
        this.drawPreview(ctx, rx, py, rw, ph);
        ctx.disableScissor();
        String[] views = new String[]{"Item", Lang.t("Hand", "Hand"), "Pop", "3D"};
        float cw = (rw - 4.0f) / 4.0f;
        float vy = py + ph + 6.0f;
        TotemStudioPanel.r(ctx, rx, vy, rw, 17.0f, 6.0f, 0x30000000);
        TotemStudioPanel.ring(ctx, rx, vy, rw, 17.0f, 6.0f, 0x14FFFFFF);
        int i = 0;
        while (i < 4) {
            boolean over;
            float vx = rx + 2.0f + (float)i * cw;
            boolean sel = this.view == i;
            boolean bl = over = !sel && TotemStudioPanel.in(mx, my, vx, vy + 2.0f, cw, 13.0f);
            if (sel) {
                TotemStudioPanel.r(ctx, vx, vy + 2.0f, cw, 13.0f, 5.0f, -13874621);
                TotemStudioPanel.ring(ctx, vx, vy + 2.0f, cw, 13.0f, 5.0f, 0x30FFFFFF);
            } else if (over) {
                TotemStudioPanel.r(ctx, vx, vy + 2.0f, cw, 13.0f, 5.0f, 0x14FFFFFF);
            }
            TotemStudioPanel.tc(ctx, views[i], vx + (cw - TotemStudioPanel.tw(views[i], 6.6f, true)) / 2.0f, vy + 8.5f, sel ? -1 : -7362144, 6.6f, true);
            int v = i++;
            this.hits.add(new StudioBase.Hit(vx, vy + 2.0f, cw, 13.0f, () -> {
                this.view = v;
                this.popAt = System.currentTimeMillis();
            }));
        }
        float by = vy + 23.0f;
        if (this.tab != 2) {
            this.ghost(ctx, "\u270e " + Lang.t("Im Editor weiterpixeln", "Keep pixelling in the editor"), rx, by, rw, 17.0f, mx, my, () -> this.switchTab(2));
            by += 24.0f;
        }
        if (!(made = TotemStudio.made()).isEmpty() && by + 30.0f < this.footY() - 30.0f) {
            TotemStudioPanel.head(ctx, Lang.t("DEINE TOTEMS", "YOUR TOTEMS"), rx, by);
            by += 9.0f;
            int per = Math.max(1, (int)((rw + 4.0f) / 26.0f));
            for (int i2 = 0; i2 < Math.min(made.size(), per * 2); ++i2) {
                float tx = rx + (float)(i2 % per * 26);
                float ty = by + (float)(i2 / per * 26);
                if (ty + 22.0f > this.footY() - 24.0f) break;
                String pack = made.get(i2);
                boolean over = TotemStudioPanel.in(mx, my, tx, ty, 22.0f, 22.0f);
                TotemStudioPanel.r(ctx, tx, ty, 22.0f, 22.0f, 5.0f, over ? 0x30FFFFFF : 0x1AFFFFFF);
                BufferedImage pic = this.thumb(pack);
                if (pic != null) {
                    ImageTex.draw(ctx, "totem/made/" + pack, pic, Math.round(tx + 3.0f), Math.round(ty + 3.0f), 16, 16);
                }
                this.hits.add(new StudioBase.Hit(tx, ty, 22.0f, 22.0f, () -> this.open(pack)));
            }
        }
        String hint = Lang.t("Nur du siehst dein Totem. Andere nur mit demselben Pack.", "Only you see your totem. Others only with the same pack.");
        float hy = this.footY() - 30.0f;
        String[] words = hint.split(" ");
        StringBuilder line = new StringBuilder();
        for (String w : words) {
            String next;
            String string = next = line.isEmpty() ? w : String.valueOf(line) + " " + w;
            if (TotemStudioPanel.tw(next, 5.8f, false) > rw && !line.isEmpty()) {
                TotemStudioPanel.t(ctx, line.toString(), rx, hy, -7362144, 5.8f, false);
                hy += 8.0f;
                line = new StringBuilder(w);
                continue;
            }
            line = new StringBuilder(next);
        }
        if (!line.isEmpty()) {
            TotemStudioPanel.t(ctx, line.toString(), rx, hy, -7362144, 5.8f, false);
        }
    }

    private BufferedImage thumb(String pack) {
        return this.thumbs.computeIfAbsent(pack, TotemStudio::picture);
    }

    private void drawPreview(GuiGraphicsExtractor ctx, float bx, float by, float bw, float bh) {
        float cx = bx + bw / 2.0f;
        float cy = by + bh / 2.0f;
        Shapes.circle(ctx, cx, cy, bh * 0.36f, 410906822);
        long now = System.currentTimeMillis();
        switch (this.view) {
            case 0: {
                this.subject(ctx, cx, cy, bh * 0.62f, this.threeD() ? -28.0f : 0.0f, 0.0f, now);
                break;
            }
            case 1: {
                int arm = this.skin != null ? this.skin.getRGB(45, 24) | 0xFF000000 : -3761043;
                float ax = bx + bw * 0.78f;
                float ay = by + bh * 0.72f;
                float[] pts = new float[]{ax - 9.0f, ay - 4.0f, ax + 9.0f, ay - 10.0f, ax + 30.0f, by + bh + 4.0f, ax + 6.0f, by + bh + 10.0f};
                Shapes.quads(ctx, pts, new int[]{arm}, 1);
                this.subject(ctx, bx + bw * 0.62f, by + bh * 0.42f, bh * 0.5f, this.threeD() ? -18.0f : 0.0f, -0.32f, now);
                break;
            }
            case 2: {
                float t = (float)((now - this.popAt) % 2200L) / 1000.0f;
                float p = Math.min(1.0f, t / 1.6f);
                float grow = p < 0.25f ? p / 0.25f : 1.0f;
                float s = bh * (0.35f + 0.55f * (float)Math.sin((double)grow * Math.PI / 2.0)) * (p > 0.8f ? (1.0f - p) / 0.2f + 1.0E-4f : 1.0f);
                float rot = (float)Math.sin((double)p * Math.PI * 1.5) * 0.35f;
                this.particles(ctx, cx, cy, bh, t);
                if (!(p < 1.0f)) break;
                this.subject(ctx, cx, cy - p * bh * 0.05f, s, this.threeD() ? 0.0f : 0.0f, rot, now);
                break;
            }
            default: {
                float yw;
                float f = yw = this.turning ? this.yaw : this.yaw + (float)(now % 8000L) / 8000.0f * 360.0f;
                if (this.threeD()) {
                    this.figure(ctx, cx, cy + bh * 0.04f, bh * 0.05f, yw);
                } else {
                    this.slab(ctx, cx, cy, bh * 0.62f / (float)this.size, yw);
                }
                TotemStudioPanel.t(ctx, Lang.t("Ziehen zum Drehen", "Drag to turn"), bx + 6.0f, by + bh - 10.0f, -1711276033, 5.6f, false);
            }
        }
    }

    private void subject(GuiGraphicsExtractor ctx, float cx, float cy, float s, float figYaw, float rot, long now) {
        if (this.threeD()) {
            this.figure(ctx, cx, cy, s / 17.0f, figYaw);
            return;
        }
        Matrix3x2fStack m = ctx.pose();
        m.pushMatrix();
        m.translate(cx, cy);
        if (rot != 0.0f) {
            m.rotate(rot);
        }
        int is = Math.max(1, Math.round(s));
        ImageTex.draw(ctx, "totem/canvas", this.px, this.size, this.size, this.version, -is / 2, -is / 2, is, is);
        m.popMatrix();
    }

    private void particles(GuiGraphicsExtractor ctx, float cx, float cy, float bh, float t) {
        Random r = new Random(7L);
        for (int i = 0; i < 40; ++i) {
            double a = r.nextDouble() * Math.PI * 2.0;
            float speed = 0.3f + r.nextFloat() * 0.7f;
            float d = t * speed * bh * 0.55f;
            float px0 = cx + (float)Math.cos(a) * d;
            float py0 = cy + (float)Math.sin(a) * d + t * t * 8.0f;
            float alpha = Math.max(0.0f, 1.0f - t / 1.8f);
            if (alpha <= 0.0f) continue;
            int base = r.nextBoolean() ? 5029994 : 16111690;
            float sz = 1.5f + r.nextFloat() * 1.5f;
            TotemStudioPanel.r(ctx, px0, py0, sz, sz, 0.0f, (int)(alpha * 230.0f) << 24 | base);
        }
    }

    private void slab(GuiGraphicsExtractor ctx, float cx, float cy, float scale, float yawDeg) {
        double yr = Math.toRadians(yawDeg);
        double cos = Math.cos(yr);
        double sin = Math.sin(yr);
        ArrayList<float[]> quads = new ArrayList<float[]>();
        ArrayList<Integer> cols = new ArrayList<Integer>();
        float half = (float)this.size / 2.0f;
        float th = Math.max(0.5f, (float)this.size / 32.0f);
        for (int j = 0; j < this.size; ++j) {
            for (int i = 0; i < this.size; ++i) {
                boolean rightOpen;
                int c = this.px[j * this.size + i];
                if (c >>> 24 == 0) continue;
                float x0 = (float)i - half;
                float x1 = (float)(i + 1) - half;
                float y0 = half - (float)j - 1.0f;
                float y1 = half - (float)j;
                for (int side = 0; side < 2; ++side) {
                    float z = side == 0 ? th : -th;
                    float nz = side == 0 ? 1.0f : -1.0f;
                    double vz = (double)nz * cos;
                    if (vz <= 0.0) continue;
                    quads.add(TotemStudioPanel.rotQuad(new float[][]{{x0, y1, z}, {x0, y0, z}, {x1, y0, z}, {x1, y1, z}}, cos, sin, cx, cy, scale));
                    cols.add(TotemStudioPanel.shade(c, side == 0 ? 1.0f : 0.7f));
                }
                boolean leftOpen = i == 0 || this.px[j * this.size + i - 1] >>> 24 == 0;
                boolean bl = rightOpen = i == this.size - 1 || this.px[j * this.size + i + 1] >>> 24 == 0;
                if (leftOpen && -sin > 0.0) {
                    quads.add(TotemStudioPanel.rotQuad(new float[][]{{x0, y1, th}, {x0, y0, th}, {x0, y0, -th}, {x0, y1, -th}}, cos, sin, cx, cy, scale));
                    cols.add(TotemStudioPanel.shade(c, 0.6f));
                }
                if (!rightOpen || !(sin > 0.0)) continue;
                quads.add(TotemStudioPanel.rotQuad(new float[][]{{x1, y1, th}, {x1, y0, th}, {x1, y0, -th}, {x1, y1, -th}}, cos, sin, cx, cy, scale));
                cols.add(TotemStudioPanel.shade(c, 0.6f));
            }
        }
        TotemStudioPanel.emit(ctx, quads, cols, null);
    }

    private static float[] rotQuad(float[][] v, double cos, double sin, float cx, float cy, float scale) {
        float[] out = new float[9];
        float depth = 0.0f;
        for (int k = 0; k < 4; ++k) {
            double x = (double)v[k][0] * cos + (double)v[k][2] * sin;
            double z = (double)(-v[k][0]) * sin + (double)v[k][2] * cos;
            out[k * 2] = cx + (float)x * scale;
            out[k * 2 + 1] = cy - v[k][1] * scale;
            depth += (float)z;
        }
        out[8] = depth;
        return out;
    }

    private void figure(GuiGraphicsExtractor ctx, float cx, float cy, float scale, float yawDeg) {
        double yr = Math.toRadians(yawDeg);
        double pr = Math.toRadians(10.0);
        double cy0 = Math.cos(yr);
        double sy0 = Math.sin(yr);
        double cp = Math.cos(pr);
        double sp = Math.sin(pr);
        ArrayList<float[]> quads = new ArrayList<float[]>();
        ArrayList<Integer> cols = new ArrayList<Integer>();
        boolean hat = V2.INSTANCE.totemHat;
        float[][] normals = new float[][]{{0.0f, 1.0f, 0.0f}, {0.0f, -1.0f, 0.0f}, {-1.0f, 0.0f, 0.0f}, {0.0f, 0.0f, 1.0f}, {1.0f, 0.0f, 0.0f}, {0.0f, 0.0f, -1.0f}};
        for (int b = 0; b < TotemStudio.FIGURE.length; ++b) {
            boolean overlay;
            float[] box = TotemStudio.FIGURE[b];
            boolean bl = overlay = box[11] > 0.0f;
            if (overlay && !hat) continue;
            float g = box[11];
            float x0 = box[0] - g;
            float y0 = box[1] - g;
            float z0 = box[2] - g;
            float x1 = box[3] + g;
            float y1 = box[4] + g;
            float z1 = box[5] + g;
            int[][] faces = TotemStudio.faces((int)box[6], (int)box[7], (int)box[8], (int)box[9], (int)box[10]);
            for (int f = 0; f < 6; ++f) {
                float[] n = normals[f];
                double nx = (double)n[0] * cy0 + (double)n[2] * sy0;
                double nz0 = (double)(-n[0]) * sy0 + (double)n[2] * cy0;
                double nz = (double)n[1] * sp + nz0 * cp;
                if (nz <= 0.001) continue;
                float light = (float)(0.84 + 0.16 * Math.max(0.0, (double)n[1] * 0.8 + nz * 0.5 + nx * 0.15));
                int[] uv = faces[f];
                int u0 = uv[0];
                int v0 = uv[1];
                int u1 = uv[2];
                int v1 = uv[3];
                int du = u1 - u0;
                int dv = v1 - v0;
                for (int pv = Math.min(v0, v1); pv < Math.max(v0, v1); ++pv) {
                    for (int pu = Math.min(u0, u1); pu < Math.max(u0, u1); ++pu) {
                        int c = this.skin.getRGB(pu, pv);
                        if (!overlay) {
                            c |= 0xFF000000;
                        } else if (c >>> 24 < 128) continue;
                        float sa = (float)(pu - u0) / (float)du;
                        float sb = (float)(pu + 1 - u0) / (float)du;
                        float ta = (float)(pv - v0) / (float)dv;
                        float tb = (float)(pv + 1 - v0) / (float)dv;
                        float[][] v = new float[][]{TotemStudioPanel.point(f, sa, ta, x0, y0, z0, x1, y1, z1), TotemStudioPanel.point(f, sb, ta, x0, y0, z0, x1, y1, z1), TotemStudioPanel.point(f, sb, tb, x0, y0, z0, x1, y1, z1), TotemStudioPanel.point(f, sa, tb, x0, y0, z0, x1, y1, z1)};
                        float[] q = new float[9];
                        float depth = 0.0f;
                        for (int k = 0; k < 4; ++k) {
                            double X = v[k][0] - 8.0f;
                            double Y = v[k][1] - 8.0f;
                            double Z = v[k][2] - 8.0f;
                            double x = X * cy0 + Z * sy0;
                            double z = -X * sy0 + Z * cy0;
                            double yy = Y * cp - z * sp;
                            double zz = Y * sp + z * cp;
                            q[k * 2] = cx + (float)x * scale;
                            q[k * 2 + 1] = cy - (float)yy * scale;
                            depth += (float)zz;
                        }
                        q[8] = depth + (overlay ? 0.05f : 0.0f);
                        quads.add(q);
                        cols.add(TotemStudioPanel.shade(c, light));
                    }
                }
            }
        }
        TotemStudioPanel.emit(ctx, quads, cols, null);
    }

    private static float[] point(int f, float s, float t, float x0, float y0, float z0, float x1, float y1, float z1) {
        float[] fArray;
        switch (f) {
            case 0: {
                float[] fArray2 = new float[3];
                fArray2[0] = x0 + s * (x1 - x0);
                fArray2[1] = y1;
                fArray = fArray2;
                fArray2[2] = z0 + t * (z1 - z0);
                break;
            }
            case 1: {
                float[] fArray3 = new float[3];
                fArray3[0] = x0 + s * (x1 - x0);
                fArray3[1] = y0;
                fArray = fArray3;
                fArray3[2] = z1 - t * (z1 - z0);
                break;
            }
            case 2: {
                float[] fArray4 = new float[3];
                fArray4[0] = x0;
                fArray4[1] = y1 - t * (y1 - y0);
                fArray = fArray4;
                fArray4[2] = z0 + s * (z1 - z0);
                break;
            }
            case 3: {
                float[] fArray5 = new float[3];
                fArray5[0] = x0 + s * (x1 - x0);
                fArray5[1] = y1 - t * (y1 - y0);
                fArray = fArray5;
                fArray5[2] = z1;
                break;
            }
            case 4: {
                float[] fArray6 = new float[3];
                fArray6[0] = x1;
                fArray6[1] = y1 - t * (y1 - y0);
                fArray = fArray6;
                fArray6[2] = z1 - s * (z1 - z0);
                break;
            }
            default: {
                float[] fArray7 = new float[3];
                fArray7[0] = x1 - s * (x1 - x0);
                fArray7[1] = y1 - t * (y1 - y0);
                fArray = fArray7;
                fArray7[2] = z0;
            }
        }
        return fArray;
    }

    private static int shade(int c, float f) {
        int a = c >>> 24;
        int r = Math.min(255, Math.round((float)(c >> 16 & 0xFF) * f));
        int g = Math.min(255, Math.round((float)(c >> 8 & 0xFF) * f));
        int b = Math.min(255, Math.round((float)(c & 0xFF) * f));
        return a << 24 | r << 16 | g << 8 | b;
    }

    private static void emit(GuiGraphicsExtractor ctx, List<float[]> quads, List<Integer> cols, Object unused) {
        int n = quads.size();
        if (n == 0) {
            return;
        }
        Integer[] order = new Integer[n];
        for (int i = 0; i < n; ++i) {
            order[i] = i;
        }
        Arrays.sort(order, (a, b) -> Float.compare(((float[])quads.get((int)a))[8], ((float[])quads.get((int)b))[8]));
        float[] pts = new float[n * 8];
        int[] cs = new int[n];
        for (int k = 0; k < n; ++k) {
            float[] q = quads.get(order[k]);
            System.arraycopy(q, 0, pts, k * 8, 8);
            cs[k] = cols.get(order[k]);
        }
        Shapes.quads(ctx, pts, cs, n);
    }

    private void switchTab(int t) {
        if (t == this.tab) {
            return;
        }
        this.tab = t;
        this.showFields();
        if (t == 0 && this.skin != null) {
            this.rebuild();
        } else if (t == 1 && this.picture != null) {
            this.rebuild();
        }
    }

    private void loadSkin() {
        String name = this.skinName.getValue().trim();
        if (name.isEmpty()) {
            this.say(Lang.t("Erst einen Spielernamen eingeben", "Type a player name first"));
            return;
        }
        this.loadingSkin = true;
        this.skinState = Lang.t("L\u00e4dt \u2026", "Loading \u2026");
        TotemStudioPanel.background("FWM skin", () -> {
            BufferedImage got = null;
            try {
                got = TotemStudio.fetchSkin(name);
            }
            catch (Exception exception) {
                // empty catch block
            }
            BufferedImage result = got;
            Minecraft.getInstance().execute(() -> {
                this.loadingSkin = false;
                if (result == null) {
                    this.skinState = Lang.t("Nicht gefunden", "Not found");
                    this.say(Lang.t("Kein Skin f\u00fcr \u201e" + name + "\u201c gefunden", "No skin found for \"" + name + "\""));
                    return;
                }
                this.useSkin(result, name);
            });
        });
    }

    private void useSkin(BufferedImage s, String name) {
        this.skin = s;
        this.skinState = name;
        V2.INSTANCE.totemSkin = name;
        V2.save();
        if (this.title != null && (this.title.getValue().isEmpty() || this.title.getValue().startsWith("FWM Totem"))) {
            this.title.setValue("FWM Totem " + name);
        }
        this.tab = 0;
        if (this.title != null) {
            this.showFields();
        }
        this.rebuild();
    }

    private void pickPicture() {
        this.say(Lang.t("Bild w\u00e4hlen \u2026", "Choose a picture \u2026"));
        TotemStudioPanel.background("FWM totem picker", () -> {
            Path picked = SkyStudio.pick();
            if (picked == null) {
                return;
            }
            Minecraft.getInstance().execute(() -> this.filesDropped(List.of(picked)));
        });
    }

    @Override
    public void filesDropped(List<Path> paths) {
        for (Path p : paths) {
            String n = p.getFileName().toString();
            String lower = n.toLowerCase(Locale.ROOT);
            if (!lower.endsWith(".png") && !lower.endsWith(".jpg") && !lower.endsWith(".jpeg")) continue;
            try {
                BufferedImage asSkin;
                BufferedImage img = ImageIO.read(p.toFile());
                if (img == null) continue;
                BufferedImage bufferedImage = asSkin = this.tab != 1 ? TotemStudio.normalize(img) : null;
                if (asSkin != null) {
                    this.useSkin(asSkin, n.replaceAll("\\.[A-Za-z]+$", ""));
                } else {
                    BufferedImage argb = new BufferedImage(img.getWidth(), img.getHeight(), 2);
                    Graphics2D g = argb.createGraphics();
                    g.drawImage((Image)img, 0, 0, null);
                    g.dispose();
                    this.picture = argb;
                    this.pictureName = n;
                    this.tab = 1;
                    this.showFields();
                    this.rebuild();
                }
                return;
            }
            catch (Exception e) {
                this.say(Lang.t("Bild konnte nicht gelesen werden", "The picture could not be read"));
            }
        }
    }

    private void start(int which) {
        this.pushUndo();
        switch (which) {
            case 0: {
                this.px = TotemStudio.vanilla(this.size);
                break;
            }
            case 1: {
                if (this.skin == null) {
                    this.say(Lang.t("Erst unter \u201eAus Skin\u201c einen Skin laden", "Load a skin under \"From skin\" first"));
                    return;
                }
                this.px = TotemStudio.fromSkin(this.skin, this.size, V2.INSTANCE.totemHat, V2.INSTANCE.totemOutline);
                break;
            }
            case 2: {
                if (this.picture == null) {
                    this.say(Lang.t("Erst unter \u201eAus PNG\u201c ein Bild w\u00e4hlen", "Choose a picture under \"From PNG\" first"));
                    return;
                }
                this.px = TotemStudio.fromPicture(this.picture, this.size, this.removeBg, this.colors, this.pngOutline);
                break;
            }
            default: {
                this.px = new int[this.size * this.size];
            }
        }
        this.changed();
    }

    private void open(String pack) {
        BufferedImage pic = TotemStudio.picture(pack);
        if (pic == null || pic.getWidth() != pic.getHeight()) {
            return;
        }
        this.pushUndo();
        int s = pic.getWidth() <= 16 ? 16 : (pic.getWidth() <= 32 ? 32 : 64);
        int[] raw = pic.getRGB(0, 0, pic.getWidth(), pic.getHeight(), null, 0, pic.getWidth());
        this.px = TotemStudio.resize(raw, pic.getWidth(), s);
        this.size = s;
        this.title.setValue("FWM Totem " + pack.substring("FWM Totem ".length()));
        this.tab = 2;
        this.showFields();
        this.changed();
    }

    private void reset() {
        V2 c = V2.INSTANCE;
        c.totemHat = true;
        c.totemOutline = false;
        c.totem3d = false;
        this.removeBg = true;
        this.colors = 0;
        this.pngOutline = false;
        V2.save();
        this.pushUndo();
        if (this.tab == 0 && this.skin != null || this.tab == 1 && this.picture != null) {
            this.rebuild();
        } else {
            this.px = TotemStudio.vanilla(this.size);
            this.changed();
        }
        this.say(Lang.t("Zur\u00fcckgesetzt", "Reset"));
    }

    private void leave() {
        if (this.sub) {
            this.host.closeSub();
        } else {
            Minecraft.getInstance().gui.setScreen(Design.open(null, "fwm.tab.textures", -1));
        }
    }

    private void apply() {
        boolean empty = true;
        for (int c : this.px) {
            if (c >>> 24 == 0) continue;
            empty = false;
            break;
        }
        if (empty) {
            this.say(Lang.t("Das Totem ist noch leer", "The totem is still empty"));
            return;
        }
        this.busy = true;
        String name = this.title.getValue();
        int[] pic = (int[])this.px.clone();
        int s = this.size;
        BufferedImage three = this.threeD() ? this.skin : null;
        TotemStudioPanel.background("FWM totem pack", () -> {
            String folder = null;
            try {
                folder = TotemStudio.write(name, pic, s, three);
            }
            catch (Exception exception) {
                // empty catch block
            }
            String done = folder;
            Minecraft.getInstance().execute(() -> {
                this.busy = false;
                if (done == null) {
                    this.say(Lang.t("Konnte nicht gespeichert werden", "Could not be saved"));
                    return;
                }
                this.thumbs.remove(done);
                ImageTex.release("totem/made/" + done);
                TextureMixer.setActive("totem", true);
                TextureMixer.choose("totem", done);
                TextureMixer.buildCategory(Minecraft.getInstance(), "totem");
                this.say(Lang.t("Angewendet: ", "Applied: ") + done.substring("FWM Totem ".length()));
            });
        });
    }

    private void pushUndo() {
        this.undo.push((int[])this.px.clone());
        if (this.undo.size() > 60) {
            this.undo.removeLast();
        }
        this.redo.clear();
    }

    private void undo() {
        if (this.undo.isEmpty()) {
            return;
        }
        this.redo.push((int[])this.px.clone());
        int[] prev = this.undo.pop();
        if (prev.length != this.px.length) {
            this.size = (int)Math.round(Math.sqrt(prev.length));
        }
        this.px = prev;
        this.changed();
    }

    private void redo() {
        if (this.redo.isEmpty()) {
            return;
        }
        this.undo.push((int[])this.px.clone());
        int[] next = this.redo.pop();
        if (next.length != this.px.length) {
            this.size = (int)Math.round(Math.sqrt(next.length));
        }
        this.px = next;
        this.changed();
    }

    private void pickColor(int c) {
        this.color = c;
        this.hex.setValue(TotemStudioPanel.hexOf(c));
        if (this.tool == 1 || this.tool == 3) {
            this.tool = 0;
        }
    }

    private static String hexOf(int c) {
        return String.format(Locale.ROOT, "#%06X", c & 0xFFFFFF);
    }

    private static Integer parseHex(String s) {
        String h = s.trim().replace("#", "");
        if (!h.matches("[0-9A-Fa-f]{6}")) {
            return null;
        }
        return 0xFF000000 | Integer.parseInt(h, 16);
    }

    private int cellAt(double mx, double my) {
        float sx = this.canvas[0];
        float sy = this.canvas[1];
        float side = this.canvas[2];
        if (mx < (double)sx || my < (double)sy || mx >= (double)(sx + side) || my >= (double)(sy + side)) {
            return -1;
        }
        int cx = (int)((mx - (double)sx) / (double)side * (double)this.size);
        int cy = (int)((my - (double)sy) / (double)side * (double)this.size);
        return Math.max(0, Math.min(this.size - 1, cy)) * this.size + Math.max(0, Math.min(this.size - 1, cx));
    }

    private void paint(int cell, boolean erase) {
        if (cell < 0) {
            return;
        }
        int t = erase ? 1 : this.tool;
        int cx = cell % this.size;
        int cy = cell / this.size;
        switch (t) {
            case 3: {
                int c = this.px[cell];
                if (c >>> 24 != 0) {
                    this.pickColor(c);
                }
                return;
            }
            case 2: {
                this.fill(cx, cy, this.color);
                if (!this.mirror) break;
                this.fill(this.size - 1 - cx, cy, this.color);
                break;
            }
            default: {
                int c;
                this.px[cell] = c = t == 1 ? 0 : this.color;
                if (!this.mirror) break;
                this.px[cy * this.size + (this.size - 1 - cx)] = c;
            }
        }
        this.changed();
    }

    private void fill(int x0, int y0, int c) {
        int from = this.px[y0 * this.size + x0];
        if (from == c) {
            return;
        }
        ArrayDeque<Integer> todo = new ArrayDeque<Integer>();
        todo.add(y0 * this.size + x0);
        while (!todo.isEmpty()) {
            int i = (Integer)todo.poll();
            if (this.px[i] != from) continue;
            this.px[i] = c;
            int x = i % this.size;
            int y = i / this.size;
            if (x > 0) {
                todo.add(i - 1);
            }
            if (x < this.size - 1) {
                todo.add(i + 1);
            }
            if (y > 0) {
                todo.add(i - this.size);
            }
            if (y >= this.size - 1) continue;
            todo.add(i + this.size);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.confirm.open()) {
            return this.confirm.mouseClicked(mouseX, mouseY);
        }
        if (!this.contains(mouseX, mouseY)) {
            return false;
        }
        this.title.setFocused(TotemStudioPanel.in(mouseX, mouseY, (float)(this.x + this.width) - this.rightW(), this.y + 1, this.rightW(), 17.0f));
        this.skinName.setFocused(this.tab == 0 && TotemStudioPanel.in(mouseX, mouseY, this.skinName.getX() - 6, this.skinName.getY() - 4, this.skinName.getWidth() + 6, 17.0f));
        this.hex.setFocused(this.tab == 2 && TotemStudioPanel.in(mouseX, mouseY, this.hex.getX() - 6, this.hex.getY() - 3, this.hex.getWidth() + 8, 15.0f));
        if (this.title.isFocused() || this.skinName.isFocused() || this.hex.isFocused()) {
            return true;
        }
        int cell = this.cellAt(mouseX, mouseY);
        if (cell >= 0 && (button == 0 || button == 1)) {
            if (this.tab != 2) {
                this.switchTab(2);
                return true;
            }
            this.pushUndo();
            this.painting = true;
            this.lastCell = cell;
            this.paint(cell, button == 1);
            return true;
        }
        if (this.view == 3 && button == 0 && TotemStudioPanel.in(mouseX, mouseY, this.previewBox[0], this.previewBox[1], this.previewBox[2], this.previewBox[3])) {
            this.turning = true;
            this.turnFromX = mouseX;
            long now = System.currentTimeMillis();
            this.yaw += (float)(now % 8000L) / 8000.0f * 360.0f;
            this.turnYaw = this.yaw;
            return true;
        }
        if (this.view == 2 && TotemStudioPanel.in(mouseX, mouseY, this.previewBox[0], this.previewBox[1], this.previewBox[2], this.previewBox[3])) {
            this.popAt = System.currentTimeMillis();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        if (this.painting) {
            int cell = this.cellAt(mouseX, mouseY);
            if (cell >= 0 && cell != this.lastCell && this.tool != 2 && this.tool != 3) {
                this.lastCell = cell;
                this.paint(cell, button == 1);
            }
            return true;
        }
        if (this.turning) {
            this.yaw = this.turnYaw + (float)(mouseX - this.turnFromX) * 1.4f;
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased() {
        this.painting = false;
        this.lastCell = -1;
        if (this.turning) {
            long now = System.currentTimeMillis();
            this.yaw -= (float)(now % 8000L) / 8000.0f * 360.0f;
        }
        this.turning = false;
    }

    private EditBox focused() {
        if (this.title != null && this.title.isFocused()) {
            return this.title;
        }
        if (this.skinName != null && this.skinName.isFocused()) {
            return this.skinName;
        }
        if (this.hex != null && this.hex.isFocused()) {
            return this.hex;
        }
        return null;
    }

    @Override
    public boolean isCapturingKeys() {
        return this.confirm.open() || this.focused() != null;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean ctrl;
        if (this.confirm.open()) {
            return this.confirm.keyPressed(keyCode);
        }
        EditBox f = this.focused();
        if (f != null) {
            if (keyCode == 256) {
                f.setFocused(false);
                return true;
            }
            if (keyCode == 257 || keyCode == 335) {
                f.setFocused(false);
                if (f == this.skinName) {
                    this.loadSkin();
                }
                return true;
            }
            return f.keyPressed(new KeyEvent(keyCode, scanCode, modifiers));
        }
        boolean bl = ctrl = (modifiers & 2) != 0;
        if (ctrl && keyCode == 90) {
            this.undo();
            return true;
        }
        if (ctrl && keyCode == 89) {
            this.redo();
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        EditBox f = this.focused();
        return f != null && f.charTyped(input);
    }
}

