/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.network.chat.Component
 *  net.minecraft.util.Util
 */
package net.friendswithmoney.fwm.ui.panel;

import java.awt.image.BufferedImage;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import net.friendswithmoney.fwm.config.ConfigManager;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.textures.BuiltinPacks;
import net.friendswithmoney.fwm.textures.FwmSky;
import net.friendswithmoney.fwm.textures.SkyStudio;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.gfx.ImageTex;
import net.friendswithmoney.fwm.ui.gfx.Shapes;
import net.friendswithmoney.fwm.ui.panel.StudioBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

public class SkyPanel
extends StudioBase {
    private static final int RIGHT_W = 196;
    private static final String ADD = "\u0000add";
    private List<String> skies = List.of();
    private int scroll;

    @Override
    public Component title() {
        return Component.literal((String)Lang.t("Himmel", "Sky"));
    }

    @Override
    protected void onResize() {
        FwmSky.rescan();
        this.skies = FwmSky.skies();
    }

    @Override
    public void onHidden() {
        ImageTex.release("sky/");
    }

    private boolean right() {
        return this.width >= 470;
    }

    private String name(String pack) {
        if (pack.isEmpty()) {
            return "Vanilla";
        }
        if (BuiltinPacks.is(pack)) {
            return BuiltinPacks.label(pack);
        }
        String n = pack.replaceAll("(?i)\\.zip$", "").replaceAll("\u00a7.", "");
        if (n.startsWith("FWM Himmel ")) {
            n = n.substring("FWM Himmel ".length());
        }
        return n;
    }

    @Override
    protected void draw(GuiGraphicsExtractor ctx, Font font, int mx, int my, float delta) {
        int row;
        V2 c = V2.INSTANCE;
        int gridW = this.right() ? this.width - 196 - 14 : this.width;
        ArrayList<String> items = new ArrayList<String>();
        items.add("");
        items.addAll(this.skies);
        items.add(ADD);
        int cols = Math.max(2, Math.min(4, gridW / 140));
        float gap = 8.0f;
        float cw = ((float)gridW - gap * (float)(cols - 1)) / (float)cols;
        float imgH = cw / 3.0f;
        float ch = imgH + 22.0f;
        int visibleRows = Math.max(1, (int)((float)(this.height - 4) / (ch + gap)));
        int rows = (items.size() + cols - 1) / cols;
        this.scroll = Math.max(0, Math.min(this.scroll, rows - visibleRows));
        for (int i = this.scroll * cols; i < items.size() && (row = i / cols - this.scroll) < visibleRows; ++i) {
            float cx = (float)this.x + (float)(i % cols) * (cw + gap);
            float cy = (float)this.y + (float)row * (ch + gap);
            String pack = (String)items.get(i);
            boolean over = SkyPanel.in(mx, my, cx, cy, cw, ch);
            if (pack.equals(ADD)) {
                SkyPanel.r(ctx, cx, cy, cw, ch, 7.0f, over ? 511570118 : 0x10FFFFFF);
                SkyPanel.ring(ctx, cx, cy, cw, ch, 7.0f, over ? -2005012282 : 0x30FFFFFF);
                Shapes.icon(ctx, "plus", cx + cw / 2.0f - 6.0f, cy + ch / 2.0f - 13.0f, 12.0f, -8523578);
                String l = Lang.t("Eigener Himmel aus Bild", "Own sky from a picture");
                SkyPanel.tc(ctx, SkyPanel.fit(l, 7.0f, true, cw - 10.0f), cx + (cw - Math.min(cw - 10.0f, SkyPanel.tw(l, 7.0f, true))) / 2.0f, cy + ch / 2.0f + 7.0f, -1, 7.0f, true);
                this.hits.add(new StudioBase.Hit(cx, cy, cw, ch, this::pick));
                continue;
            }
            boolean sel = pack.equals(c.skyPack);
            SkyPanel.r(ctx, cx, cy, cw, ch, 7.0f, sel ? 981332166 : (over ? 0x30FFFFFF : 0x22000000));
            SkyPanel.ring(ctx, cx, cy, cw, ch, 7.0f, sel ? -864161594 : 0x18FFFFFF);
            this.drawSky(ctx, pack, "sky/c" + i, cx + 4.0f, cy + 4.0f, cw - 8.0f, imgH);
            SkyPanel.tc(ctx, SkyPanel.fit(this.name(pack), 7.0f, true, cw - 40.0f), cx + 6.0f, cy + imgH + 13.0f, -1, 7.0f, true);
            if (sel) {
                float pw = SkyPanel.tw(Lang.t("AKTIV", "ACTIVE"), 5.6f, true) + 10.0f;
                SkyPanel.r(ctx, cx + cw - 6.0f - pw, cy + imgH + 8.0f, pw, 10.0f, 5.0f, -8523578);
                SkyPanel.tc(ctx, Lang.t("AKTIV", "ACTIVE"), cx + cw - 6.0f - pw + 5.0f, cy + imgH + 13.0f, -16045536, 5.6f, true);
            }
            this.hits.add(new StudioBase.Hit(cx, cy, cw, ch, () -> this.choose(pack)));
        }
        if (rows > visibleRows) {
            String s = this.scroll + 1 + " / " + (rows - visibleRows + 1);
            SkyPanel.t(ctx, s, (float)(this.x + gridW) - SkyPanel.tw(s, 6.0f, false), this.y + this.height - 7, -7362144, 6.0f, false);
        }
        if (this.right()) {
            this.drawRight(ctx, mx, my);
        }
        this.drawNote(ctx, (float)this.x + (float)gridW / 2.0f, this.y + this.height - 18);
    }

    private void drawRight(GuiGraphicsExtractor ctx, int mx, int my) {
        V2 c = V2.INSTANCE;
        float rx = this.x + this.width - 196;
        float ry = this.y;
        SkyPanel.head(ctx, Lang.t("AUSGEW\u00c4HLT", "SELECTED"), rx, ry);
        float ih = 65.333336f;
        this.drawSky(ctx, c.skyPack, "sky/big", rx, ry + 10.0f, 196.0f, ih);
        SkyPanel.ring(ctx, rx, ry + 10.0f, 196.0f, ih, 4.0f, 0x22FFFFFF);
        float ty = ry + 14.0f + ih;
        SkyPanel.t(ctx, SkyPanel.fit(this.name(c.skyPack), 9.0f, true, 196.0f), rx, ty, -1, 9.0f, true);
        String origin = c.skyPack.isEmpty() ? Lang.t("Der normale Minecraft-Himmel", "The normal Minecraft sky") : (c.skyPack.startsWith("FWM Himmel ") ? Lang.t("Aus deinem Bild", "From your picture") : (BuiltinPacks.is(c.skyPack) ? Lang.t("FWM-Pack", "FWM pack") : Lang.t("Aus Pack", "From a pack")));
        SkyPanel.t(ctx, origin, rx, ty + 11.0f, -7362144, 6.4f, false);
        float oy = ty + 26.0f;
        SkyPanel.head(ctx, Lang.t("ANZEIGE", "DISPLAY"), rx, oy);
        this.toggleRow(ctx, Lang.t("Sonne, Mond & Sterne", "Sun, moon & stars"), rx, oy += 9.0f, 196.0f, c.skySunMoon, () -> this.set(() -> {
            c.skySunMoon = !c.skySunMoon;
        }));
        this.toggleRow(ctx, Lang.t("Nachts dunkler", "Darker at night"), rx, oy += 20.0f, 196.0f, c.skyNight, () -> this.set(() -> {
            c.skyNight = !c.skyNight;
        }));
        this.toggleRow(ctx, Lang.t("Auch im End", "In the End too"), rx, oy += 20.0f, 196.0f, c.skyEnd, () -> this.set(() -> {
            c.skyEnd = !c.skyEnd;
        }));
        SkyPanel.tc(ctx, Lang.t("Drehen", "Turning"), rx, (oy += 22.0f) + 7.0f, -1, 7.2f, true);
        String[] spins = new String[]{Lang.t("Aus", "Off"), Lang.t("Langsam", "Slow"), Lang.t("Schnell", "Fast")};
        int idx = c.skySpin <= 0 ? 0 : (c.skySpin <= 3 ? 1 : 2);
        float cwid = 0.0f;
        for (String s : spins) {
            cwid += SkyPanel.tw(s, 6.8f, true) + 16.0f;
        }
        this.chips(ctx, spins, idx, rx + 196.0f - cwid - 4.0f, oy, 14.0f, mx, my, i -> this.set(() -> {
            c.skySpin = i == 0 ? 0 : (i == 1 ? 2 : 10);
        }));
        float bw = 95.0f;
        this.ghost(ctx, Lang.t("Ordner \u00f6ffnen", "Open folder"), rx, oy += 22.0f, bw, 16.0f, mx, my, () -> {
            Util.getPlatform().openPath(Minecraft.getInstance().getResourcePackDirectory());
            this.say(Lang.t("Sky-Pack (ZIP/Ordner) hineinlegen oder ins Fenster ziehen", "Put a sky pack (ZIP/folder) in or drop it onto the window"));
        });
        if (c.skyPack.startsWith("FWM Himmel ")) {
            this.ghost(ctx, Lang.t("L\u00f6schen", "Delete"), rx + bw + 6.0f, oy, bw, 16.0f, mx, my, () -> this.askDelete(c.skyPack));
        } else {
            this.ghost(ctx, Lang.t("Neu suchen", "Look again"), rx + bw + 6.0f, oy, bw, 16.0f, mx, my, () -> {
                this.onResize();
                this.say(Lang.t(this.skies.size() + " Himmel gefunden", this.skies.size() + " skies found"));
            });
        }
        String hint = Lang.t("FWM zeichnet den Himmel selbst \u2013 keine andere Mod n\u00f6tig. Nur du siehst ihn.", "FWM draws the sky itself - no other mod needed. Only you see it.");
        SkyPanel.wrap(ctx, hint, rx, oy += 24.0f, 196.0f, -7362144, 6.2f);
    }

    private static void wrap(GuiGraphicsExtractor ctx, String s, float x, float y, float w, int color, float size) {
        StringBuilder line = new StringBuilder();
        for (String word : s.split(" ")) {
            String next;
            String string = next = line.isEmpty() ? word : String.valueOf(line) + " " + word;
            if (SkyPanel.tw(next, size, false) > w && !line.isEmpty()) {
                SkyPanel.t(ctx, line.toString(), x, y, color, size, false);
                y += size + 3.0f;
                line = new StringBuilder(word);
                continue;
            }
            line = new StringBuilder(next);
        }
        if (!line.isEmpty()) {
            SkyPanel.t(ctx, line.toString(), x, y, color, size, false);
        }
    }

    private void drawSky(GuiGraphicsExtractor ctx, String pack, String key, float x, float y, float w, float h) {
        if (pack.isEmpty()) {
            Shapes.roundGradient(ctx, x, y, w, h, 4.0f, -9462806, -3876621);
            Shapes.circle(ctx, x + w * 0.74f, y + h * 0.38f, Math.max(3.0f, h * 0.13f), -2872);
            return;
        }
        BufferedImage img = FwmSky.thumb(pack);
        if (img == null) {
            SkyPanel.r(ctx, x, y, w, h, 4.0f, 0x30000000);
            String l = Lang.t("l\u00e4dt \u2026", "loading \u2026");
            SkyPanel.tc(ctx, l, x + (w - SkyPanel.tw(l, 6.4f, false)) / 2.0f, y + h / 2.0f, -7362144, 6.4f, false);
            return;
        }
        ImageTex.draw(ctx, key + "|" + pack, img, Math.round(x), Math.round(y), Math.round(w), Math.round(h));
    }

    private void set(Runnable change) {
        change.run();
        V2.save();
    }

    private void choose(String pack) {
        V2.INSTANCE.skyPack = pack;
        V2.save();
        FwmSky.reload();
        this.say((String)(pack.isEmpty() ? Lang.t("Vanilla-Himmel", "Vanilla sky") : Lang.t("Himmel aktiv: ", "Sky on: ") + this.name(pack)));
    }

    private void pick() {
        this.say(Lang.t("Bild w\u00e4hlen \u2026", "Choose a picture \u2026"));
        SkyPanel.background("FWM sky picker", () -> {
            Path picked = SkyStudio.pick();
            if (picked == null) {
                return;
            }
            Minecraft.getInstance().execute(() -> this.filesDropped(List.of(picked)));
        });
    }

    private void askDelete(String pack) {
        this.confirm.ask((Component)Component.literal((String)Lang.t("\u201e" + this.name(pack) + "\u201c l\u00f6schen?", "Delete \"" + this.name(pack) + "\"?")), () -> {
            try {
                Path dir = Minecraft.getInstance().getResourcePackDirectory().resolve(pack).normalize();
                if (!dir.getFileName().toString().startsWith("FWM Himmel ")) {
                    return;
                }
                try (Stream<Path> walk = Files.walk(dir, new FileVisitOption[0]);){
                    for (Path p : walk.sorted(Comparator.reverseOrder()).toList()) {
                        Files.deleteIfExists(p);
                    }
                }
                this.choose("");
                this.onResize();
                this.say(Lang.t("Gel\u00f6scht", "Deleted"));
            }
            catch (Exception e) {
                this.say(Lang.t("Konnte nicht gel\u00f6scht werden.", "Could not be deleted."));
            }
        });
    }

    @Override
    public void filesDropped(List<Path> paths) {
        Path dir = Minecraft.getInstance().getResourcePackDirectory();
        this.say(Lang.t("Wird eingelesen \u2026", "Reading \u2026"));
        SkyPanel.background("FWM sky import", () -> {
            String made = null;
            int added = 0;
            for (Path path : paths) {
                try {
                    String file = path.getFileName().toString();
                    String lower = file.toLowerCase(Locale.ROOT);
                    if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                        String n = SkyStudio.make(path);
                        if (n == null) continue;
                        made = n;
                        ++added;
                        continue;
                    }
                    if (!lower.endsWith(".zip")) continue;
                    Files.copy(path, dir.resolve(file), StandardCopyOption.REPLACE_EXISTING);
                    ++added;
                }
                catch (Exception exception) {}
            }
            String chosen = made;
            int n = added;
            Minecraft.getInstance().execute(() -> {
                this.onResize();
                if (chosen != null) {
                    this.choose(chosen);
                } else {
                    this.say(n > 0 ? Lang.t(n + " hinzugef\u00fcgt", n + " added") : Lang.t("Kein Himmel darin gefunden", "No sky found in it"));
                }
            });
        });
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double vertical) {
        this.scroll = Math.max(0, this.scroll - (int)Math.signum(vertical));
        return true;
    }

    public static void migrate() {
        String old;
        V2 c = V2.INSTANCE;
        if (c.skyMigrated) {
            return;
        }
        c.skyMigrated = true;
        Map<String, String> mix = FwmConfig.INSTANCE.textureMix;
        String string = old = mix == null ? null : mix.remove("sky");
        if (old != null && !old.isEmpty() && (c.skyPack == null || c.skyPack.isEmpty())) {
            c.skyPack = old;
        }
        V2.save();
        ConfigManager.save();
    }
}

