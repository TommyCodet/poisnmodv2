/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.NativeImage
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.resources.Identifier
 *  net.minecraft.util.FormattedCharSequence
 *  net.minecraft.util.Util
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package net.friendswithmoney.fwm.ui.panel;

import com.mojang.blaze3d.platform.NativeImage;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;
import net.friendswithmoney.fwm.game.Mapart;
import net.friendswithmoney.fwm.textures.SkyStudio;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.LauncherGfx;
import net.friendswithmoney.fwm.ui.gfx.Shapes;
import net.friendswithmoney.fwm.ui.panel.Panel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class MapartPanel
extends Panel {
    private static final int TEXT = -1379602;
    private static final int SUB = -7362144;
    private static final int MINT = -8523578;
    private static final Identifier PREVIEW = Identifier.fromNamespaceAndPath((String)"fwm", (String)"mapart/preview");
    private Path picture;
    private int mapsX = 1;
    private int mapsY = 1;
    private String madeFor = "";
    private volatile boolean saved;
    private boolean dither = true;
    private boolean gold;
    private boolean carpet;
    private boolean threeD;
    private int saturation = 100;
    private int brightness = 0;
    private int contrast = 100;
    private final int[][] sliders = new int[3][];
    private int dragging = -1;
    private volatile boolean working;
    private volatile Mapart.Result result;
    private volatile String message = "";
    private boolean uploaded;
    private int previewW;
    private int previewH;
    private final List<int[]> boxes = new ArrayList<int[]>();
    private final List<Runnable> actions = new ArrayList<Runnable>();

    @Override
    public Component title() {
        return Component.literal((String)"Mapart");
    }

    private void hit(int bx, int by, int w, int h, Runnable a) {
        this.boxes.add(new int[]{bx, by, w, h});
        this.actions.add(a);
    }

    private int button(GuiGraphicsExtractor ctx, Font font, int bx, int by, String label, boolean strong, boolean on, int mx, int my, Runnable a) {
        boolean over;
        int w = font.width(label) + 14;
        boolean bl = over = mx >= bx && mx < bx + w && my >= by && my < by + 15;
        int fill = strong ? (over ? -6555690 : -8523578) : (on ? 1149104326 : (over ? 0x33FFFFFF : 0x1AFFFFFF));
        LauncherGfx.round(ctx, bx, by, w, 15, 5, fill);
        if (!strong) {
            LauncherGfx.ring(ctx, bx, by, w, 15, 5, on ? -1434586938 : 0x22FFFFFF);
        }
        ctx.text(font, label, bx + 7, by + 4, strong ? -16045536 : (on ? -8523578 : -1379602), false);
        this.hit(bx, by, w, 15, a);
        return w;
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY, float delta) {
        Mapart.Result r;
        boolean fresh;
        int[][] quick;
        this.boxes.clear();
        this.actions.clear();
        int left = Math.min(230, this.width / 2 - 10);
        LauncherGfx.round(ctx, this.x, this.y, left, this.height, 8, 0x26000000);
        LauncherGfx.ring(ctx, this.x, this.y, left, this.height, 8, 0x14FFFFFF);
        int cx = this.x + 10;
        int cy = this.y + 10;
        LauncherGfx.bold(ctx, font, Lang.t("1. Bild", "1. Picture"), cx, cy, -1379602);
        this.button(ctx, font, cx, cy += 13, Lang.t("Bild w\u00e4hlen \u2026", "Choose picture \u2026"), false, false, mouseX, mouseY, this::pick);
        String name = this.picture == null ? Lang.t("noch keins", "none yet") : this.picture.getFileName().toString();
        ctx.text(font, LauncherGfx.trim(font, name, left - 110), cx + 96, cy + 4, -7362144, false);
        LauncherGfx.bold(ctx, font, Lang.t("2. Gr\u00f6\u00dfe (Karten)", "2. Size (maps)"), cx, cy += 24, -1379602);
        cy += 13;
        int bx = cx;
        for (int[] q : quick = new int[][]{{1, 1}, {1, 2}, {2, 1}, {2, 2}, {2, 3}, {3, 2}, {3, 3}, {4, 4}}) {
            String l = q[0] + "x" + q[1];
            int w = font.width(l) + 14;
            if (bx + w > this.x + left - 8) {
                bx = cx;
                cy += 18;
            }
            bx += this.button(ctx, font, bx, cy, l, false, this.mapsX == q[0] && this.mapsY == q[1], mouseX, mouseY, () -> {
                this.mapsX = q[0];
                this.mapsY = q[1];
            }) + 4;
        }
        bx = cx;
        bx += this.stepper(ctx, font, bx, cy += 18, Lang.t("Breite", "Width"), this.mapsX, mouseX, mouseY, v -> {
            this.mapsX = v;
        }) + 10;
        this.stepper(ctx, font, bx, cy, Lang.t("H\u00f6he", "Height"), this.mapsY, mouseX, mouseY, v -> {
            this.mapsY = v;
        });
        ctx.text(font, this.mapsX * 128 + " x " + this.mapsY * 128 + Lang.t(" Bl\u00f6cke \u00b7 ", " blocks \u00b7 ") + this.mapsX * this.mapsY + Lang.t(this.mapsX * this.mapsY == 1 ? " Karte" : " Karten", this.mapsX * this.mapsY == 1 ? " map" : " maps"), cx, cy += 18, -7362144, false);
        LauncherGfx.bold(ctx, font, Lang.t("3. Aussehen", "3. Look"), cx, cy += 16, -1379602);
        bx = cx;
        bx += this.button(ctx, font, bx, cy += 13, Lang.t("Bl\u00f6cke", "Blocks"), false, !this.carpet, mouseX, mouseY, () -> {
            this.carpet = false;
        }) + 4;
        bx += this.button(ctx, font, bx, cy, Lang.t("Nur Teppich", "Only carpet"), false, this.carpet, mouseX, mouseY, () -> {
            this.carpet = true;
            this.threeD = false;
        }) + 10;
        bx += this.button(ctx, font, bx, cy, "2D", false, !this.threeD, mouseX, mouseY, () -> {
            this.threeD = false;
        }) + 4;
        if (!this.carpet) {
            this.button(ctx, font, bx, cy, "3D", false, this.threeD, mouseX, mouseY, () -> {
                this.threeD = true;
            });
        }
        bx = cx;
        bx += this.button(ctx, font, bx, cy += 18, Lang.t("Dithering", "Dithering"), false, this.dither, mouseX, mouseY, () -> {
            this.dither = !this.dither;
        }) + 4;
        if (!this.carpet) {
            this.button(ctx, font, bx, cy, Lang.t("Gold erlaubt", "Gold allowed"), false, this.gold, mouseX, mouseY, () -> {
                this.gold = !this.gold;
            });
        }
        ctx.text(font, this.threeD ? Lang.t("3D: drei Helligkeiten je Farbe, Treppe", "3D: three shades per colour, staircase") : Lang.t("2D: flach, ein Farbton je Block", "2D: flat, one shade per block"), cx, cy += 18, -7362144, false);
        cy += 14;
        int sw = left - 20;
        cy = this.slider(ctx, font, 0, Lang.t("S\u00e4ttigung", "Saturation"), this.saturation, 0, 200, this.saturation + "%", cx, cy, sw, mouseX, mouseY);
        cy = this.slider(ctx, font, 1, Lang.t("Helligkeit", "Brightness"), this.brightness, -50, 50, (this.brightness > 0 ? "+" : "") + this.brightness, cx, cy, sw, mouseX, mouseY);
        cy = this.slider(ctx, font, 2, Lang.t("Kontrast", "Contrast"), this.contrast, 50, 150, this.contrast + "%", cx, cy, sw, mouseX, mouseY);
        if (this.saturation != 100 || this.brightness != 0 || this.contrast != 100) {
            String reset = Lang.t("Regler zur\u00fccksetzen", "Reset sliders");
            boolean over = mouseX >= cx && mouseX < cx + font.width(reset) && mouseY >= cy && mouseY < cy + 9;
            ctx.text(font, reset, cx, cy, over ? -8523578 : -7362144, false);
            this.hit(cx, cy, font.width(reset), 9, () -> {
                this.saturation = 100;
                this.brightness = 0;
                this.contrast = 100;
            });
        }
        cy += 14;
        boolean bl = fresh = this.result != null && this.madeFor.equals(this.key());
        String go = this.working ? Lang.t("Wird erstellt \u2026", "Building \u2026") : (fresh ? Lang.t("Neu erstellen", "Make again") : Lang.t("Vorschau erstellen", "Make preview"));
        int goX = cx;
        if (this.picture != null && !this.working) {
            goX += this.button(ctx, font, cx, cy, go, !fresh, false, mouseX, mouseY, this::build) + 4;
        } else {
            ctx.text(font, go, cx, cy + 4, -7362144, false);
            goX += font.width(go) + 8;
        }
        if (fresh && !this.working) {
            String save = this.saved ? Lang.t("Gespeichert \u2714", "Saved \u2714") : Lang.t("Speichern", "Save");
            goX += this.button(ctx, font, goX, cy, save, !this.saved, this.saved, mouseX, mouseY, this::save) + 4;
        }
        this.button(ctx, font, cx, cy += 18, Lang.t("Ordner \u00f6ffnen", "Open folder"), false, false, mouseX, mouseY, this::openFolder);
        cy += 22;
        if (!this.message.isEmpty()) {
            for (FormattedCharSequence line : font.split((FormattedText)Component.literal((String)this.message), left - 20)) {
                ctx.text(font, line, cx, cy, -7362144, false);
                cy += 10;
            }
        }
        if ((r = this.result) != null && cy + 26 < this.y + this.height - 6) {
            LauncherGfx.bold(ctx, font, Lang.t("Material", "Materials") + " (" + r.blocks() + ")", cx, cy += 4, -1379602);
            cy += 12;
            for (Map.Entry<String, Integer> e : r.materials().entrySet()) {
                if (cy + 10 > this.y + this.height - 6) break;
                Item item = (Item)BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace((String)e.getKey()));
                LauncherGfx.item(ctx, new ItemStack((ItemLike)item), cx, cy - 2, 0.625f);
                int n = e.getValue();
                String amount = n >= 64 ? n / 64 + " St." + (String)(n % 64 > 0 ? " + " + n % 64 : "") : String.valueOf(n);
                ctx.text(font, LauncherGfx.trim(font, e.getKey().replace('_', ' '), left - 90), cx + 13, cy, -1379602, false);
                ctx.text(font, amount, this.x + left - 10 - font.width(amount), cy, -7362144, false);
                cy += 10;
            }
        }
        int px = this.x + left + 10;
        int pw = this.width - left - 10;
        LauncherGfx.round(ctx, px, this.y, pw, this.height, 8, 0x26000000);
        LauncherGfx.ring(ctx, px, this.y, pw, this.height, 8, 0x14FFFFFF);
        if (r != null && !this.uploaded) {
            this.upload(r.preview());
        }
        if (this.uploaded) {
            int mx = r.mapsX();
            int my = r.mapsY();
            float scale = Math.min(((float)pw - 20.0f) / (float)this.previewW, ((float)this.height - 34.0f) / (float)this.previewH);
            int dw = Math.round((float)this.previewW * scale);
            int dh = Math.round((float)this.previewH * scale);
            int dx = px + (pw - dw) / 2;
            int dy = this.y + (this.height - 14 - dh) / 2;
            ctx.blit(RenderPipelines.GUI_TEXTURED, PREVIEW, dx, dy, 0.0f, 0.0f, dw, dh, this.previewW, this.previewH, this.previewW, this.previewH);
            for (int i = 1; i < mx; ++i) {
                float lx = (float)dx + (float)(dw * i) / (float)mx;
                Shapes.round(ctx, lx - 0.4f, dy, 0.8f, dh, 0.0f, 0x66FFFFFF);
            }
            for (int j = 1; j < my; ++j) {
                float ly = (float)dy + (float)(dh * j) / (float)my;
                Shapes.round(ctx, dx, ly - 0.4f, dw, 0.8f, 0.0f, 0x66FFFFFF);
            }
            Shapes.ring(ctx, dx, dy, dw, dh, 0.0f, 0x33FFFFFF);
            Object info = mx + "x" + my + Lang.t(" Karten \u00b7 je 128 \u00d7 128", " maps \u00b7 128 \u00d7 128 each");
            if (!this.madeFor.equals(this.key())) {
                info = Lang.t("Einstellungen ge\u00e4ndert \u2013 Vorschau neu erstellen", "Settings changed - make the preview again");
            } else if (!this.saved) {
                info = (String)info + Lang.t(" \u00b7 noch nicht gespeichert", " \u00b7 not saved yet");
            }
            ctx.text(font, (String)info, px + (pw - font.width((String)info)) / 2, dy + dh + 5, this.madeFor.equals(this.key()) ? -7362144 : -670398, false);
        } else {
            String hint = Lang.t("Hier erscheint die Vorschau", "The preview appears here");
            ctx.text(font, hint, px + (pw - font.width(hint)) / 2, this.y + this.height / 2 - 4, -7362144, false);
        }
    }

    private void upload(BufferedImage img) {
        NativeImage ni = new NativeImage(img.getWidth(), img.getHeight(), false);
        for (int yy = 0; yy < img.getHeight(); ++yy) {
            for (int xx = 0; xx < img.getWidth(); ++xx) {
                ni.setPixel(xx, yy, 0xFF000000 | img.getRGB(xx, yy));
            }
        }
        Minecraft client = Minecraft.getInstance();
        client.getTextureManager().release(PREVIEW);
        client.getTextureManager().register(PREVIEW, (AbstractTexture)new DynamicTexture(() -> "fwm mapart", ni));
        this.previewW = img.getWidth();
        this.previewH = img.getHeight();
        this.uploaded = true;
    }

    private void pick() {
        Thread t = new Thread(() -> {
            Path chosen = SkyStudio.pick();
            if (chosen != null) {
                this.picture = chosen;
                this.message = "";
            }
        }, "FWM mapart picker");
        t.setDaemon(true);
        t.start();
    }

    private String key() {
        return String.valueOf(this.picture) + "|" + this.mapsX + "x" + this.mapsY + "|" + this.dither + this.gold + this.carpet + this.threeD + "|" + this.saturation + "," + this.brightness + "," + this.contrast;
    }

    private int stepper(GuiGraphicsExtractor ctx, Font font, int bx, int by, String label, int value, int mx, int my, IntConsumer set) {
        ctx.text(font, label, bx, by + 4, -7362144, false);
        int x0 = bx + font.width(label) + 5;
        int w = this.button(ctx, font, x0, by, "\u2212", false, false, mx, my, () -> set.accept(Math.max(1, value - 1)));
        String v = String.valueOf(value);
        ctx.text(font, v, x0 + w + 4, by + 4, -1379602, false);
        int w2 = this.button(ctx, font, x0 + w + 8 + font.width(v), by, "+", false, false, mx, my, () -> set.accept(Math.min(6, value + 1)));
        return x0 + w + 8 + font.width(v) + w2 - bx;
    }

    private void save() {
        Mapart.Result r = this.result;
        if (r == null || this.working || this.saved) {
            return;
        }
        this.working = true;
        Thread t = new Thread(() -> {
            try {
                Mapart.Result done = r.save();
                Minecraft.getInstance().execute(() -> {
                    this.saved = true;
                    this.message = Lang.t("Gespeichert in schematics/: ", "Saved in schematics/: ") + String.valueOf(done.schematic().getFileName()) + Lang.t(" (+ Vorschau und Materialliste). In Litematica laden, Norden = oben.", " (+ preview and material list). Load it in Litematica, north = top.");
                });
            }
            catch (Exception e) {
                this.message = Lang.t("Speichern ging nicht: ", "Saving did not work: ") + e.getMessage();
            }
            finally {
                this.working = false;
            }
        }, "FWM mapart save");
        t.setDaemon(true);
        t.start();
    }

    private void build() {
        if (this.picture == null || this.working) {
            return;
        }
        this.working = true;
        this.message = "";
        Path pic = this.picture;
        int sx = this.mapsX;
        int sy = this.mapsY;
        String forKey = this.key();
        boolean d = this.dither;
        boolean g = this.gold;
        boolean k = this.carpet;
        boolean t3 = this.threeD;
        Mapart.Look look = new Mapart.Look(this.saturation, this.brightness, this.contrast);
        Thread t = new Thread(() -> {
            try {
                Mapart.Result r = Mapart.make(pic, sx, sy, d, g, k, t3, look);
                Minecraft.getInstance().execute(() -> {
                    this.result = r;
                    this.madeFor = forKey;
                    this.saved = false;
                    this.uploaded = false;
                    this.message = Lang.t("Vorschau fertig. Passt sie, auf \u201eSpeichern\u201c dr\u00fccken.", "Preview ready. If it looks right, press \"Save\".");
                });
            }
            catch (Exception e) {
                this.message = Lang.t("Das Bild ging nicht: ", "That picture did not work: ") + e.getMessage();
            }
            finally {
                this.working = false;
            }
        }, "FWM mapart");
        t.setDaemon(true);
        t.start();
    }

    private int slider(GuiGraphicsExtractor ctx, Font font, int id, String label, int value, int min, int max, String shown, int sx, int sy, int sw, int mx, int my) {
        int labelW = 62;
        int valueW = 30;
        int tx = sx + labelW;
        int tw = sw - labelW - valueW;
        int ty = sy + 4;
        ctx.text(font, label, sx, sy + 1, -1379602, false);
        LauncherGfx.round(ctx, tx, ty, tw, 3, 1, 0x33FFFFFF);
        int fill = Math.round((float)(value - min) / (float)(max - min) * (float)tw);
        LauncherGfx.round(ctx, tx, ty, Math.max(2, fill), 3, 1, -2005012282);
        boolean over = this.dragging == id || mx >= tx - 2 && mx < tx + tw + 2 && my >= sy && my < sy + 11;
        LauncherGfx.round(ctx, tx + fill - 3, ty - 3, 6, 9, 2, over ? -6555690 : -8523578);
        ctx.text(font, shown, sx + sw - font.width(shown), sy + 1, -7362144, false);
        this.sliders[id] = new int[]{tx, sy, tw, min, max};
        return sy + 13;
    }

    private void slide(int id, double mouseX) {
        int[] s = this.sliders[id];
        if (s == null) {
            return;
        }
        float t = (float)Math.max(0.0, Math.min(1.0, (mouseX - (double)s[0]) / (double)s[2]));
        int v = Math.round((float)s[3] + t * (float)(s[4] - s[3]));
        switch (id) {
            case 0: {
                this.saturation = v;
                break;
            }
            case 1: {
                this.brightness = v;
                break;
            }
            default: {
                this.contrast = v;
            }
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        if (this.dragging < 0) {
            return false;
        }
        this.slide(this.dragging, mouseX);
        return true;
    }

    @Override
    public void mouseReleased() {
        this.dragging = -1;
    }

    private void openFolder() {
        try {
            Path dir = Minecraft.getInstance().gameDirectory.toPath().resolve("schematics");
            Files.createDirectories(dir, new FileAttribute[0]);
            Util.getPlatform().openPath(dir);
        }
        catch (Exception e) {
            this.message = Lang.t("Ordner ging nicht auf: ", "Could not open the folder: ") + e.getMessage();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int i;
        if (button != 0) {
            return false;
        }
        for (i = 0; i < this.sliders.length; ++i) {
            int[] s = this.sliders[i];
            if (s == null || !(mouseX >= (double)(s[0] - 3)) || !(mouseX < (double)(s[0] + s[2] + 3)) || !(mouseY >= (double)s[1]) || !(mouseY < (double)(s[1] + 11))) continue;
            this.dragging = i;
            this.slide(i, mouseX);
            return true;
        }
        for (i = this.boxes.size() - 1; i >= 0; --i) {
            int[] b = this.boxes.get(i);
            if (!(mouseX >= (double)b[0]) || !(mouseX < (double)(b[0] + b[2])) || !(mouseY >= (double)b[1]) || !(mouseY < (double)(b[1] + b[3]))) continue;
            this.actions.get(i).run();
            return true;
        }
        return this.contains(mouseX, mouseY);
    }

    @Override
    public void onHidden() {
        Minecraft.getInstance().getTextureManager().release(PREVIEW);
        this.uploaded = false;
    }
}

