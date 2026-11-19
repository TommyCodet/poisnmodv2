/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 */
package net.friendswithmoney.fwm.ui.panel;

import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.stream.Stream;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.lyrics.AfkCinema;
import net.friendswithmoney.fwm.lyrics.AfkConfig;
import net.friendswithmoney.fwm.lyrics.BeatSync;
import net.friendswithmoney.fwm.lyrics.CoverTexture;
import net.friendswithmoney.fwm.lyrics.FontPack;
import net.friendswithmoney.fwm.lyrics.Look;
import net.friendswithmoney.fwm.lyrics.Lyrics;
import net.friendswithmoney.fwm.lyrics.LyricsConfig;
import net.friendswithmoney.fwm.lyrics.LyricsFetcher;
import net.friendswithmoney.fwm.lyrics.LyricsTime;
import net.friendswithmoney.fwm.lyrics.MediaWatcher;
import net.friendswithmoney.fwm.lyrics.MusicFx;
import net.friendswithmoney.fwm.lyrics.SkyLyrics;
import net.friendswithmoney.fwm.lyrics.SkyText;
import net.friendswithmoney.fwm.lyrics.SyncCheck;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.LauncherGfx;
import net.friendswithmoney.fwm.ui.gfx.Shapes;
import net.friendswithmoney.fwm.ui.panel.Panel;
import net.friendswithmoney.fwm.ui.panel.RowsPanel;
import net.friendswithmoney.fwm.ui.widget.ButtonRow;
import net.friendswithmoney.fwm.ui.widget.ChipsRow;
import net.friendswithmoney.fwm.ui.widget.ColorPickRow;
import net.friendswithmoney.fwm.ui.widget.HexRow;
import net.friendswithmoney.fwm.ui.widget.KeybindRow;
import net.friendswithmoney.fwm.ui.widget.Row;
import net.friendswithmoney.fwm.ui.widget.SectionRow;
import net.friendswithmoney.fwm.ui.widget.SliderRow;
import net.friendswithmoney.fwm.ui.widget.TextRow;
import net.friendswithmoney.fwm.ui.widget.ToggleRow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;

public final class SkyLyricsPanels {
    private static final int MINT = -8523578;
    private static final int GOLD = -670398;
    private static final int WHITE = -1;
    private static final int DIM = -7363684;
    private static final int SUB = -4600634;
    private static final int RED = -38037;

    private SkyLyricsPanels() {
    }

    private static LyricsConfig cfg() {
        return SkyLyrics.cfg;
    }

    private static void save() {
        SkyLyricsPanels.cfg().clamp();
        SkyLyricsPanels.cfg().save();
    }

    private static Component t(String de, String en) {
        return Component.literal((String)Lang.t(de, en));
    }

    private static int hex(String s, int def) {
        return LyricsConfig.rgb(s, def);
    }

    private static List<ChipsRow.Chip> chips(String ... labels) {
        ArrayList<ChipsRow.Chip> out = new ArrayList<ChipsRow.Chip>();
        for (int i = 0; i < labels.length; ++i) {
            out.add(new ChipsRow.Chip(labels[i], i));
        }
        return out;
    }

    private static int index(String value, String ... ids) {
        for (int i = 0; i < ids.length; ++i) {
            if (!ids[i].equals(value)) continue;
            return i;
        }
        return 0;
    }

    public static final class Privacy
    extends RowsPanel {
        @Override
        public Component title() {
            return SkyLyricsPanels.t("Datenschutz", "Privacy");
        }

        @Override
        protected void buildRows() {
            LyricsConfig c = SkyLyricsPanels.cfg();
            this.rows.add(new ChipsRow(SkyLyricsPanels.t("Internet", "Internet"), SkyLyricsPanels.chips(Lang.t("Erlaubt", "Allowed"), Lang.t("Nur lokal", "Local only")), () -> "online".equals(c.consent) ? 0 : 1, i -> {
                c.consent = i == 0 ? "online" : "local";
                SkyLyricsPanels.save();
            }));
            this.rows.add(new TextRow(SkyLyricsPanels.t("Songtext: lrclib.net", "Lyrics: lrclib.net"), SkyLyricsPanels.t("Titel, Interpret, Album, Dauer", "title, artist, album, length")));
            this.rows.add(new ToggleRow(SkyLyricsPanels.t("Genre nachschlagen (iTunes/Deezer)", "Look up the genre (iTunes/Deezer)"), () -> c.netGenre, v -> {
                c.netGenre = v;
                SkyLyricsPanels.save();
            }));
            this.rows.add(new ToggleRow(SkyLyricsPanels.t("Fehlende Cover laden (iTunes/Deezer)", "Load missing covers (iTunes/Deezer)"), () -> c.netCover, v -> {
                c.netCover = v;
                SkyLyricsPanels.save();
            }));
            this.rows.add(new TextRow(SkyLyricsPanels.t("Nie gesendet", "Never sent"), SkyLyricsPanels.t("MC-Name, UUID, IDs, nichts an FWM", "MC name, UUID, IDs, nothing to FWM")));
            this.rows.add(new ButtonRow(SkyLyricsPanels.t("Songtext-Cache und Vers\u00e4tze l\u00f6schen", "Delete lyrics cache and offsets"), () -> SkyLyricsPanels.t("L\u00f6schen", "Delete"), () -> {
                block8: {
                    try {
                        Path dir = LyricsConfig.DIR;
                        if (!Files.isDirectory(dir, new LinkOption[0])) break block8;
                        try (Stream<Path> walk = Files.walk(dir, new FileVisitOption[0]);){
                            walk.sorted(Comparator.reverseOrder()).filter(p -> !p.equals(dir) && !p.getFileName().toString().endsWith(".ps1")).forEach(p -> p.toFile().delete());
                        }
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            }));
            this.rows.add(new ButtonRow(SkyLyricsPanels.t("Alle Sky-Lyrics-Einstellungen zur\u00fccksetzen", "Reset all Sky Lyrics settings"), () -> SkyLyricsPanels.t("Zur\u00fccksetzen", "Reset"), () -> this.confirm.ask(SkyLyricsPanels.t("Alle Sky-Lyrics-Einstellungen zur\u00fccksetzen?", "Reset all Sky Lyrics settings?"), "fwm.confirm.confirm", () -> {
                c.resetToDefaults();
                SkyText.reset();
                this.onResize();
            })));
            this.rows.add(new TextRow(SkyLyricsPanels.t("Basiert auf Sky Lyrics von Snoblex & Teufl08", "Based on Sky Lyrics by Snoblex & Teufl08")));
        }
    }

    public static final class Pvp
    extends RowsPanel {
        @Override
        public Component title() {
            return SkyLyricsPanels.t("PvP-Lyrics", "PvP lyrics");
        }

        @Override
        protected void buildRows() {
            LyricsConfig c = SkyLyricsPanels.cfg();
            this.rows.add(new ToggleRow(SkyLyricsPanels.t("Songtext neben deinem Gegner", "Lyrics next to your opponent"), () -> c.pvpEdit, v -> {
                c.pvpEdit = v;
                SkyLyricsPanels.save();
            }));
            this.rows.add(new SliderRow(SkyLyricsPanels.t("Textgr\u00f6\u00dfe", "Text size"), 5, 20, () -> Math.round(c.pvpScale * 10.0f), v -> {
                c.pvpScale = (float)v / 10.0f;
                SkyLyricsPanels.save();
            }, v -> Component.literal((String)String.format("%.1f\u00d7", Float.valueOf((float)v / 10.0f)))));
            this.rows.add(new SliderRow(SkyLyricsPanels.t("Rand", "Outline"), 0, 30, () -> Math.round(c.pvpOutline * 10.0f), v -> {
                c.pvpOutline = (float)v / 10.0f;
                SkyLyricsPanels.save();
            }, v -> Component.literal((String)(v == 0 ? Lang.t("aus", "off") : String.format("%.1f", Float.valueOf((float)v / 10.0f))))));
            this.rows.add(new TextRow(SkyLyricsPanels.t("Fair: nur sichtbare Spieler, nur mit freier Sicht", "Fair: visible players only, in plain sight only")));
            this.rows.add(new TextRow(SkyLyricsPanels.t("Keine Unsichtbaren, keine Mobs, nichts durch W\u00e4nde", "No invisible players, no mobs, nothing through walls")));
            this.rows.add(new TextRow(SkyLyricsPanels.t("Max. 24 Bl\u00f6cke, endet 8 s nach dem letzten Treffer", "Max 24 blocks, ends 8 s after the last hit")));
        }
    }

    public static final class Effects
    extends RowsPanel {
        @Override
        public Component title() {
            return SkyLyricsPanels.t("Effekte", "Effects");
        }

        @Override
        protected void buildRows() {
            LyricsConfig c = SkyLyricsPanels.cfg();
            this.rows.add(new TextRow(SkyLyricsPanels.t("Nur bei dir sichtbar \u2013 der Server bekommt nichts davon.", "Only you see them \u2013 nothing is sent to the server.")));
            this.rows.add(new ToggleRow(SkyLyricsPanels.t("Takt-Puls (Text + Boden-Wellen im Refrain)", "Beat pulse (text + ground waves in the chorus)"), () -> c.beatSync, v -> {
                c.beatSync = v;
                SkyLyricsPanels.save();
            }));
            this.rows.add(new ToggleRow(SkyLyricsPanels.t("W\u00f6rter zerfallen zu Staub", "Words dissolve into dust"), () -> c.wordDust, v -> {
                c.wordDust = v;
                SkyLyricsPanels.save();
            }));
            this.rows.add(new ToggleRow(SkyLyricsPanels.t("Titel beim Songwechsel", "Title on song change"), () -> c.songIntro, v -> {
                c.songIntro = v;
                SkyLyricsPanels.save();
            }));
            this.rows.add(new ToggleRow(SkyLyricsPanels.t("Feuerwerk (Refrain, Drop, Finale)", "Fireworks (chorus, drop, finale)"), () -> c.musicFx, v -> {
                c.musicFx = v;
                SkyLyricsPanels.save();
            }));
            this.rows.add(new ToggleRow(SkyLyricsPanels.t("Musiknoten um dich (Refrain)", "Music notes around you (chorus)"), () -> c.fxNotes, v -> {
                c.fxNotes = v;
                SkyLyricsPanels.save();
            }));
            this.rows.add(new ToggleRow(SkyLyricsPanels.t("Lichts\u00e4ulen um dich (Finale)", "Light pillars around you (finale)"), () -> c.fxPillars, v -> {
                c.fxPillars = v;
                SkyLyricsPanels.save();
            }));
            this.rows.add(new ToggleRow(SkyLyricsPanels.t("Blitze am Horizont (Rock, Metal \u2026)", "Lightning on the horizon (rock, metal \u2026)"), () -> c.fxLightning, v -> {
                c.fxLightning = v;
                SkyLyricsPanels.save();
            }));
            this.rows.add(new ToggleRow(SkyLyricsPanels.t("Sternschnuppen (nachts)", "Shooting stars (at night)"), () -> c.fxStars, v -> {
                c.fxStars = v;
                SkyLyricsPanels.save();
            }));
            this.rows.add(new ChipsRow(SkyLyricsPanels.t("St\u00e4rke", "Strength"), SkyLyricsPanels.chips(Lang.t("Dezent", "Subtle"), "Normal", Lang.t("Episch", "Epic")), () -> SkyLyricsPanels.index(c.fxLevel, "subtle", "normal", "epic"), i -> {
                c.fxLevel = (new String[]{"subtle", "normal", "epic"})[i];
                SkyLyricsPanels.save();
            }));
            this.rows.add(new ButtonRow(SkyLyricsPanels.t("Alle eingeschalteten Effekte zeigen", "Show all switched-on effects"), () -> SkyLyricsPanels.t("\u2726 Testen", "\u2726 Test"), MusicFx::preview));
            this.rows.add(new ToggleRow(SkyLyricsPanels.t("Wetter + Uhrzeit nach Genre (nur bei dir)", "Weather + time by genre (only you)"), () -> c.worldSync, v -> {
                c.worldSync = v;
                SkyLyricsPanels.save();
            }));
        }
    }

    public static final class Looks
    extends RowsPanel {
        private boolean more;
        private FontRow fontRow;
        private boolean dropOpen;
        private int dropScroll;
        private int[] drop;
        private static final int ITEM_H = 17;
        private int editing;
        private int pickHue;
        private int pickShade = 50;

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

        private void openPicker(int which, int rgb) {
            if (this.editing == which) {
                this.editing = 0;
            } else {
                this.editing = which;
                this.readPick(rgb);
            }
            this.onResize();
        }

        private void pickerRows(int which, IntConsumer apply) {
            if (this.editing != which) {
                return;
            }
            this.rows.add(new ColorPickRow(SkyLyricsPanels.t("Farbton", "Hue"), false, () -> this.pickHue, () -> this.pickHue, v -> {
                this.pickHue = v;
            }, null));
            this.rows.add(new ColorPickRow(SkyLyricsPanels.t("Helligkeit", "Shade"), true, () -> this.pickHue, () -> this.pickShade, v -> {
                this.pickShade = v;
            }, () -> {
                apply.accept(ColorPickRow.color(this.pickHue, this.pickShade) & 0xFFFFFF);
                this.editing = 0;
                this.onResize();
            }));
        }

        @Override
        public Component title() {
            return SkyLyricsPanels.t("Aussehen", "Look");
        }

        @Override
        protected void buildRows() {
            LyricsConfig c = SkyLyricsPanels.cfg();
            this.rows.add(new PreviewRow());
            this.rows.add(new Spacer());
            String src = MediaWatcher.sourceApp();
            this.rows.add(new TextRow(SkyLyricsPanels.t("Quelle", "Source"), (Component)Component.literal((String)(src.isEmpty() ? Lang.t("keine Musik-App erkannt", "no music app found") : src))));
            this.rows.add(new ToggleRow(SkyLyricsPanels.t("Nur Musik-Apps (Spotify, Apple Music \u2026)", "Music apps only (Spotify, Apple Music \u2026)"), () -> c.onlyMusicApps, v -> {
                c.onlyMusicApps = v;
                SkyLyricsPanels.save();
            }));
            this.rows.add(new ToggleRow(SkyLyricsPanels.t("Auch Musik aus dem Browser (z. B. YouTube Music)", "Music from the browser too (e.g. YouTube Music)"), () -> c.allowBrowser, v -> {
                c.allowBrowser = v;
                SkyLyricsPanels.save();
            }));
            this.rows.add(new ToggleRow(SkyLyricsPanels.t("Songtext im Himmel", "Lyrics in the sky"), () -> c.skyEnabled, v -> {
                c.skyEnabled = v;
                SkyLyricsPanels.save();
            }));
            ArrayList<ChipsRow.Chip> looks = new ArrayList<ChipsRow.Chip>();
            for (int i2 = 0; i2 < Look.ALL.size(); ++i2) {
                looks.add(new ChipsRow.Chip(Look.ALL.get(i2).name(), i2));
            }
            this.rows.add(new ChipsRow(SkyLyricsPanels.t("Look", "Look"), looks, () -> {
                for (int i = 0; i < Look.ALL.size(); ++i) {
                    if (!Look.active(c, Look.ALL.get(i))) continue;
                    return i;
                }
                return -1;
            }, i -> {
                Look.apply(c, Look.ALL.get(i));
                SkyText.reset();
                this.onResize();
            }));
            this.fontRow = new FontRow(() -> {
                this.dropOpen = !this.dropOpen;
                this.dropScroll = Math.max(0, FontPack.CHOICES.indexOf(FontPack.find(c.fontStyle)) - 3);
            });
            this.rows.add(this.fontRow);
            IntConsumer setColor = v -> {
                c.customColor = LyricsConfig.hex(v);
                c.colorMode = "custom";
                SkyLyricsPanels.save();
            };
            this.rows.add(new HexRow(SkyLyricsPanels.t("Farbe", "Colour"), () -> this.editing == 1 ? ColorPickRow.color(this.pickHue, this.pickShade) & 0xFFFFFF : SkyLyricsPanels.hex(c.customColor, 8253638), setColor, () -> this.openPicker(1, SkyLyricsPanels.hex(c.customColor, 8253638))));
            this.pickerRows(1, setColor);
            this.rows.add(new SliderRow(SkyLyricsPanels.t("Leuchten", "Glow"), 0, 100, () -> c.glow, v -> {
                c.glow = v;
                SkyLyricsPanels.save();
            }, v -> Component.literal((String)(v == 0 ? Lang.t("aus", "off") : v + " %"))));
            this.rows.add(new SliderRow(SkyLyricsPanels.t("Gr\u00f6\u00dfe", "Size"), 8, 60, () -> Math.round(c.skyScale * 10.0f), v -> {
                c.skyScale = (float)v / 10.0f;
                SkyLyricsPanels.save();
            }, v -> Component.literal((String)String.format("%.1f\u00d7", Float.valueOf((float)v / 10.0f)))));
            this.rows.add(new ChipsRow(SkyLyricsPanels.t("W\u00f6rter", "Words"), SkyLyricsPanels.chips(Lang.t("Aufbauen", "Build up"), Lang.t("Wegsingen", "Sing away"), Lang.t("Vorbeilaufen", "Walk past")), () -> SkyLyricsPanels.index(c.mode, "build", "vanish", "flyby"), i -> {
                c.mode = (new String[]{"build", "vanish", "flyby"})[i];
                SkyLyricsPanels.save();
                SkyText.reset();
            }));
            this.rows.add(new ToggleRow(SkyLyricsPanels.t("Leiste oben (Songname + Zeile)", "Bar at the top (song + line)"), () -> c.pillEnabled, v -> {
                c.pillEnabled = v;
                SkyLyricsPanels.save();
            }));
            this.rows.add(new SectionRow(SkyLyricsPanels.t("Mehr", "More"), Lang.t("Abstand, H\u00f6he, Farbe, Effekt \u2026", "distance, height, colour, effect \u2026"), () -> this.more, () -> {
                this.more = !this.more;
                this.onResize();
            }));
            if (!this.more) {
                return;
            }
            this.rows.add(new SliderRow(SkyLyricsPanels.t("Entfernung", "Distance"), 12, 160, () -> Math.round(c.skyDistance), v -> {
                c.skyDistance = v;
                SkyLyricsPanels.save();
            }, v -> Component.literal((String)(v + Lang.t(" Bl\u00f6cke", " blocks")))));
            this.rows.add(new SliderRow(SkyLyricsPanels.t("H\u00f6he", "Height"), 0, 70, () -> Math.round(c.skyHeight), v -> {
                c.skyHeight = v;
                SkyLyricsPanels.save();
            }, v -> Component.literal((String)(v + Lang.t(" Bl\u00f6cke", " blocks")))));
            this.rows.add(new SliderRow(SkyLyricsPanels.t("Neigung", "Tilt"), -30, 30, () -> Math.round(c.skyTilt), v -> {
                c.skyTilt = v;
                SkyLyricsPanels.save();
            }, v -> Component.literal((String)(v + "\u00b0"))));
            this.rows.add(new ChipsRow(SkyLyricsPanels.t("Farbmodus", "Colour mode"), SkyLyricsPanels.chips("Auto", Lang.t("Hell", "Light"), Lang.t("Dunkel", "Dark"), Lang.t("Eigene", "Custom")), () -> SkyLyricsPanels.index(c.colorMode, "auto", "light", "dark", "custom"), i -> {
                c.colorMode = (new String[]{"auto", "light", "dark", "custom"})[i];
                SkyLyricsPanels.save();
            }));
            this.rows.add(new ToggleRow(SkyLyricsPanels.t("Farbverlauf", "Gradient"), () -> c.gradient, v -> {
                c.gradient = v;
                SkyLyricsPanels.save();
            }));
            IntConsumer setColor2 = v -> {
                c.color2 = LyricsConfig.hex(v);
                SkyLyricsPanels.save();
            };
            this.rows.add(new HexRow(SkyLyricsPanels.t("Farbe 2", "Colour 2"), () -> this.editing == 2 ? ColorPickRow.color(this.pickHue, this.pickShade) & 0xFFFFFF : SkyLyricsPanels.hex(c.color2, 16020150), setColor2, () -> this.openPicker(2, SkyLyricsPanels.hex(c.color2, 16020150))));
            this.pickerRows(2, setColor2);
            this.rows.add(new ChipsRow(SkyLyricsPanels.t("Effekt", "Effect"), SkyLyricsPanels.chips(Lang.t("Keiner", "None"), Lang.t("Schatten", "Shadow"), Lang.t("Umriss", "Outline")), () -> SkyLyricsPanels.index(c.textEffect, "none", "shadow", "outline"), i -> {
                c.textEffect = (new String[]{"none", "shadow", "outline"})[i];
                SkyLyricsPanels.save();
            }));
            this.rows.add(new ToggleRow(SkyLyricsPanels.t("Fett", "Bold"), () -> c.bold, v -> {
                c.bold = v;
                SkyLyricsPanels.save();
                SkyText.reset();
            }));
            this.rows.add(new ToggleRow(SkyLyricsPanels.t("Kursiv", "Italic"), () -> c.italic, v -> {
                c.italic = v;
                SkyLyricsPanels.save();
                SkyText.reset();
            }));
            this.rows.add(new ToggleRow(SkyLyricsPanels.t("Zuf\u00e4llige Position", "Random position"), () -> c.randomPlacement, v -> {
                c.randomPlacement = v;
                SkyLyricsPanels.save();
            }));
            this.rows.add(new ToggleRow(SkyLyricsPanels.t("Weicht Bl\u00f6cken aus", "Avoids blocks"), () -> c.keepVisible, v -> {
                c.keepVisible = v;
                SkyLyricsPanels.save();
            }));
            this.rows.add(new ToggleRow(SkyLyricsPanels.t("Beim Rennen weiter vorne", "Further ahead when running"), () -> c.speedAdapt, v -> {
                c.speedAdapt = v;
                SkyLyricsPanels.save();
            }));
            this.rows.add(new ToggleRow(SkyLyricsPanels.t("Nur unter freiem Himmel", "Only under open sky"), () -> c.onlyUnderOpenSky, v -> {
                c.onlyUnderOpenSky = v;
                SkyLyricsPanels.save();
            }));
            this.rows.add(new ToggleRow(SkyLyricsPanels.t("Schwung (leicht gebogen)", "Swing (slightly curved)"), () -> c.arc, v -> {
                c.arc = v;
                SkyLyricsPanels.save();
            }));
            if (AfkCinema.toggleKey() != null) {
                this.rows.add(new KeybindRow(SkyLyricsPanels.t("Kamerafahrt an/aus (Taste)", "Camera ride on/off (key)"), AfkCinema.toggleKey()));
            }
            this.rows.add(new ToggleRow(SkyLyricsPanels.t("Kamerafahrt automatisch bei AFK", "Camera ride automatically when AFK"), () -> AfkCinema.cfg != null && AfkCinema.cfg.enabled, v -> {
                AfkConfig ac = AfkCinema.cfg;
                if (ac == null) {
                    return;
                }
                ac.enabled = v;
                ac.save();
            }));
            this.rows.add(new ChipsRow(SkyLyricsPanels.t("Leiste", "Bar"), SkyLyricsPanels.chips(Lang.t("Oben", "Top"), Lang.t("Unten", "Bottom")), () -> SkyLyricsPanels.index(c.pillPosition, "top", "bottom"), i -> {
                c.pillPosition = i == 0 ? "top" : "bottom";
                SkyLyricsPanels.save();
            }));
            this.rows.add(new SliderRow(SkyLyricsPanels.t("Versatz (alle Songs)", "Offset (all songs)"), -40, 40, () -> Math.round((float)c.offsetMs / 250.0f), v -> {
                c.offsetMs = v * 250;
                SkyLyricsPanels.save();
            }, v -> Component.literal((String)((v * 250 > 0 ? "+" : "") + v * 250 + " ms"))));
        }

        @Override
        public void render(GuiGraphicsExtractor ctx, Font font, int mx, int my, float delta) {
            int idx;
            super.render(ctx, font, this.dropOpen ? -9999 : mx, this.dropOpen ? -9999 : my, delta);
            this.drop = null;
            if (!this.dropOpen || this.fontRow == null) {
                return;
            }
            int bx = this.fontRow.bx();
            int bw = Math.max(this.fontRow.bw(), 130);
            bx = Math.min(bx, this.x + this.width - bw);
            int top = this.fontRow.by() + 20 - 1;
            int shown = Math.max(4, Math.min(FontPack.CHOICES.size(), (this.y + this.height - 4 - top) / 17));
            if (shown < 6) {
                shown = Math.min(FontPack.CHOICES.size(), Math.max(6, (this.fontRow.by() - this.y) / 17));
                top = this.fontRow.by() + 1 - shown * 17;
            }
            int h = shown * 17 + 4;
            this.dropScroll = Math.max(0, Math.min(FontPack.CHOICES.size() - shown, this.dropScroll));
            this.drop = new int[]{bx, top, bw, h};
            ctx.pose().pushMatrix();
            Shapes.round(ctx, bx, top, bw, h, 6.0f, -233955557);
            Shapes.ring(ctx, bx, top, bw, h, 6.0f, -2005012282);
            String current = FontPack.find(SkyLyricsPanels.cfg().fontStyle).id();
            for (int i = 0; i < shown && (idx = this.dropScroll + i) < FontPack.CHOICES.size(); ++i) {
                boolean over;
                FontPack.Choice ch = FontPack.CHOICES.get(idx);
                int iy = top + 2 + i * 17;
                boolean bl = over = mx >= bx && mx < bx + bw && my >= iy && my < iy + 17;
                if (over) {
                    Shapes.round(ctx, bx + 2, iy, bw - 4, 17.0f, 4.0f, 813560006);
                }
                MutableComponent name = Component.literal((String)ch.name()).setStyle(FontPack.fontOnly(ch));
                ctx.pose().pushMatrix();
                ctx.pose().translate((float)(bx + 8), (float)iy + 3.5f);
                ctx.pose().scale(1.15f, 1.15f);
                ctx.text(font, (Component)name, 0, 0, ch.id().equals(current) ? -8523578 : -1, false);
                ctx.pose().popMatrix();
                if (!ch.id().equals(current)) continue;
                ctx.text(font, (Component)Component.literal((String)"\u2714"), bx + bw - 12, iy + 4, -8523578, false);
            }
            if (FontPack.CHOICES.size() > shown) {
                float th = Math.max(10.0f, (float)(h * shown) / (float)FontPack.CHOICES.size());
                float ty = (float)top + ((float)h - th) * (float)this.dropScroll / (float)(FontPack.CHOICES.size() - shown);
                Shapes.round(ctx, bx + bw - 3, ty, 2.0f, th, 1.0f, -1434586938);
            }
            ctx.pose().popMatrix();
        }

        @Override
        public boolean mouseClicked(double mx, double my, int button) {
            if (this.dropOpen) {
                int idx;
                if (this.drop != null && mx >= (double)this.drop[0] && mx < (double)(this.drop[0] + this.drop[2]) && my >= (double)this.drop[1] && my < (double)(this.drop[1] + this.drop[3]) && (idx = this.dropScroll + (int)((my - (double)this.drop[1] - 2.0) / 17.0)) >= 0 && idx < FontPack.CHOICES.size()) {
                    SkyLyricsPanels.cfg().fontStyle = FontPack.CHOICES.get(idx).id();
                    SkyLyricsPanels.cfg().look = "";
                    SkyLyricsPanels.save();
                    SkyText.reset();
                }
                this.dropOpen = false;
                return true;
            }
            return super.mouseClicked(mx, my, button);
        }

        @Override
        public boolean mouseScrolled(double mx, double my, double v) {
            if (this.dropOpen) {
                this.dropScroll -= (int)Math.signum(v);
                return true;
            }
            return super.mouseScrolled(mx, my, v);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (this.dropOpen && keyCode == 256) {
                this.dropOpen = false;
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean isCapturingKeys() {
            return this.dropOpen || super.isCapturingKeys();
        }
    }

    private static final class Spacer
    extends Row {
        Spacer() {
            super((Component)Component.empty());
        }

        @Override
        public void render(GuiGraphicsExtractor ctx, Font font, int mx, int my) {
        }

        @Override
        protected void renderControl(GuiGraphicsExtractor ctx, Font font, int mx, int my) {
        }
    }

    private static final class PreviewRow
    extends Row {
        PreviewRow() {
            super((Component)Component.empty());
        }

        @Override
        public void render(GuiGraphicsExtractor ctx, Font font, int mx, int my) {
            LyricsConfig c = SkyLyricsPanels.cfg();
            Shapes.roundGradient(ctx, this.x, this.y + 1, this.width, 38.0f, 6.0f, -15721924, -12959878);
            int color = "custom".equals(c.colorMode) ? LyricsConfig.rgb(c.customColor, 8253638) : ("dark".equals(c.colorMode) ? LyricsConfig.rgb(c.darkTextColor, 1975360) : LyricsConfig.rgb(c.lightTextColor, 0xFFF8E8));
            MutableComponent line = Component.literal((String)Lang.t("blinded by the lights", "blinded by the lights")).setStyle(SkyText.style(c));
            float scale = 1.6f;
            int tw = font.width((FormattedText)line);
            float cx = (float)this.x + (float)this.width / 2.0f;
            float cy = this.y + 20 - 1;
            if (c.glow > 0) {
                for (int i = 0; i < 6; ++i) {
                    float grow = 14.0f - (float)i * 2.4f;
                    int alpha = Math.round((float)c.glow * 0.16f);
                    Shapes.round(ctx, cx - (float)tw * scale / 2.0f - grow, cy - 5.0f - grow / 2.0f, (float)tw * scale + grow * 2.0f, 10.0f + grow, 5.0f + grow / 2.0f, alpha << 24 | color & 0xFFFFFF);
                }
            }
            ctx.pose().pushMatrix();
            ctx.pose().translate(cx - (float)tw * scale / 2.0f, cy - 4.0f * scale);
            ctx.pose().scale(scale, scale);
            if ("shadow".equals(c.textEffect)) {
                ctx.text(font, (Component)line, 1, 1, 0xFF000000 | color >> 2 & 0x3F3F3F, false);
            }
            ctx.text(font, (Component)line, 0, 0, 0xFF000000 | color, false);
            ctx.pose().popMatrix();
        }

        @Override
        protected void renderControl(GuiGraphicsExtractor ctx, Font font, int mx, int my) {
        }
    }

    private static final class FontRow
    extends Row {
        private final Runnable open;

        FontRow(Runnable open) {
            super(SkyLyricsPanels.t("Schrift", "Font"));
            this.open = open;
        }

        int bx() {
            return this.controlX();
        }

        int by() {
            return this.y;
        }

        int bw() {
            return this.controlRight() - this.controlX();
        }

        @Override
        protected void renderControl(GuiGraphicsExtractor ctx, Font font, int mx, int my) {
            int bx = this.controlX();
            int bw = this.controlRight() - bx;
            boolean over = mx >= bx && mx < bx + bw && my >= this.y + 2 && my < this.y + 20 - 2;
            Shapes.round(ctx, bx, this.y + 2, bw, 16.0f, 4.0f, over ? 0x30FFFFFF : 0x22000000);
            Shapes.ring(ctx, bx, this.y + 2, bw, 16.0f, 4.0f, over ? -2005012282 : 0x30FFFFFF);
            FontPack.Choice c = FontPack.find(SkyLyricsPanels.cfg().fontStyle);
            MutableComponent name = Component.literal((String)c.name()).setStyle(FontPack.fontOnly(c));
            String shown = font.substrByWidth((FormattedText)name, bw - 18).getString();
            ctx.text(font, (Component)Component.literal((String)shown).setStyle(FontPack.fontOnly(c)), bx + 5, this.textY(font), -1, false);
            ctx.text(font, (Component)Component.literal((String)"\u25be"), bx + bw - 10, this.textY(font), -8523578, false);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int button) {
            if (button != 0 || !this.isHovered(mx, my)) {
                return false;
            }
            this.open.run();
            return true;
        }
    }

    public static final class Overview
    extends Panel {
        private final List<Hit> hits = new ArrayList<Hit>();
        private boolean draggingSeek;
        private boolean draggingVol;
        private float seekX;
        private float seekW;
        private float volX;
        private float volW;
        private float lastW;

        @Override
        public Component title() {
            return Component.literal((String)"Sky Lyrics");
        }

        @Override
        public String headerSwitch() {
            return "fwm.tab.skylyrics";
        }

        private boolean button(GuiGraphicsExtractor ctx, String label, float x, float y, boolean primary, int mx, int my, Runnable action) {
            boolean over;
            float w = LauncherGfx.width(label, 6.6f, true) + 16.0f;
            boolean bl = over = (float)mx >= x && (float)mx < x + w && (float)my >= y && (float)my < y + 15.0f;
            Shapes.round(ctx, x, y, w, 15.0f, 5.0f, primary ? (over ? -6555690 : -8523578) : (over ? 0x26FFFFFF : 0x16FFFFFF));
            if (!primary) {
                Shapes.ring(ctx, x, y, w, 15.0f, 5.0f, over ? -2005012282 : 0x24FFFFFF);
            }
            LauncherGfx.text(ctx, label, x + 8.0f, y + 7.5f - 3.4f, primary ? -16045536 : -1, 6.6f, true);
            this.hits.add(new Hit(x, y, w, 15.0f, action));
            this.lastW = w;
            return over;
        }

        @Override
        public void render(GuiGraphicsExtractor ctx, Font font, int mx, int my, float delta) {
            String status;
            this.hits.clear();
            LyricsConfig c = SkyLyricsPanels.cfg();
            int x0 = this.x + 4;
            int w = this.width - 8;
            float y0 = this.y + 2;
            if (!MediaWatcher.supported()) {
                this.card(ctx, x0, y0, w, 44.0f);
                LauncherGfx.text(ctx, Lang.t("Nur unter Windows", "Windows only"), x0 + 10, y0 + 9.0f, -38037, 8.0f, true);
                LauncherGfx.text(ctx, Lang.t("Die Song-Erkennung nutzt die Windows-Medienanzeige. Mac und Linux folgen sp\u00e4ter.", "Song detection uses the Windows media overlay. Mac and Linux follow later."), x0 + 10, y0 + 24.0f, -4600634, 6.4f, false);
                this.credits(ctx, x0, this.y + this.height - 12, w);
                return;
            }
            if (!Features.isOn("fwm.tab.skylyrics")) {
                this.card(ctx, x0, y0, w, 52.0f);
                LauncherGfx.text(ctx, Lang.t("Sky Lyrics ist aus", "Sky Lyrics is off"), x0 + 10, y0 + 9.0f, -1, 8.0f, true);
                LauncherGfx.text(ctx, Lang.t("Schalte es oben rechts ein. Solange es aus ist, l\u00e4uft nichts: kein Hintergrund-Prozess, kein Internet.", "Switch it on at the top right. While it is off nothing runs: no background process, no internet."), x0 + 10, y0 + 24.0f, -4600634, 6.2f, false);
                this.credits(ctx, x0, this.y + this.height - 12, w);
                return;
            }
            if (c.consent == null || c.consent.isEmpty()) {
                this.consent(ctx, x0, y0, w, mx, my);
                this.credits(ctx, x0, this.y + this.height - 12, w);
                return;
            }
            MediaWatcher.Track track = MediaWatcher.track();
            float cardH = 64.0f;
            this.card(ctx, x0, y0, w, cardH);
            if (track == null) {
                LauncherGfx.text(ctx, Lang.t("Gerade l\u00e4uft nichts", "Nothing is playing"), x0 + 10, y0 + 12.0f, -1, 9.0f, true);
                LauncherGfx.text(ctx, Lang.t("Starte Musik in Spotify, im Browser oder einer anderen App.", "Start music in Spotify, a browser or another app."), x0 + 10, y0 + 30.0f, -4600634, 6.4f, false);
                LauncherGfx.text(ctx, MediaWatcher.alive() ? Lang.t("\u25cf Medienanzeige verbunden", "\u25cf Media overlay connected") : Lang.t("\u25cb verbindet \u2026", "\u25cb connecting \u2026"), x0 + 10, y0 + 44.0f, MediaWatcher.alive() ? -8523578 : -7363684, 6.0f, true);
            } else {
                this.player(ctx, track, x0, y0, w, cardH, mx, my);
            }
            float y1 = y0 + cardH + 6.0f;
            Lyrics lyr = track == null ? null : LyricsFetcher.peek(track);
            this.card(ctx, x0, y1, w, 22.0f);
            int scol = -8523578;
            if (track == null) {
                status = "\u2013";
                scol = -7363684;
            } else if (!SkyLyrics.online()) {
                status = Lang.t("Songtext aus \u2013 Datenschutz: nur lokal", "Lyrics off \u2013 privacy: local only");
                scol = -670398;
            } else if (lyr == null) {
                status = LyricsFetcher.statusText(track);
                scol = -7363684;
            } else {
                SyncCheck.Status st = SyncCheck.status(track, lyr);
                status = st.text();
                scol = st.state() == SyncCheck.State.OK ? -8523578 : -670398;
            }
            LauncherGfx.text(ctx, LauncherGfx.fit(status, 6.4f, true, (float)w * 0.45f), x0 + 8, y1 + 11.0f - 3.2f, scol, 6.4f, true);
            float bx = x0 + w - 8;
            String off = (track != null ? LyricsTime.offset(track) : c.offsetMs) + " ms";
            String plus = "+250";
            String minus = "\u2212250";
            float wPlus = LauncherGfx.width(plus, 6.6f, true) + 16.0f;
            this.button(ctx, plus, bx -= wPlus, y1 + 3.5f, false, mx, my, () -> SkyLyrics.shift(Minecraft.getInstance(), 250));
            float ow = LauncherGfx.width(off, 6.4f, true) + 10.0f;
            LauncherGfx.text(ctx, off, (bx -= ow + 3.0f) + 5.0f, y1 + 11.0f - 3.2f, -1, 6.4f, true);
            float wMinus = LauncherGfx.width(minus, 6.6f, true) + 16.0f;
            this.button(ctx, minus, bx -= wMinus + 3.0f, y1 + 3.5f, false, mx, my, () -> SkyLyrics.shift(Minecraft.getInstance(), -250));
            float yq = y1 + 27.0f;
            float qx = x0;
            qx = this.quick(ctx, Lang.t("Text am Himmel", "Sky text"), c.skyEnabled, qx, yq, mx, my, () -> {
                c.skyEnabled = !c.skyEnabled;
            });
            qx = this.quick(ctx, Lang.t("PvP-Lyrics", "PvP lyrics"), c.pvpEdit, qx, yq, mx, my, () -> {
                c.pvpEdit = !c.pvpEdit;
            });
            qx = this.quick(ctx, Lang.t("Leiste oben", "Top bar"), c.pillEnabled, qx, yq, mx, my, () -> {
                c.pillEnabled = !c.pillEnabled;
            });
            qx = this.quick(ctx, Lang.t("Feuerwerk", "Fireworks"), c.musicFx, qx, yq, mx, my, () -> {
                c.musicFx = !c.musicFx;
            });
            String camLabel = Lang.t("\u25b6 Kamerafahrt", "\u25b6 Camera ride");
            float camW = LauncherGfx.width(camLabel, 6.6f, true) + 16.0f;
            this.button(ctx, camLabel, (float)(x0 + w) - camW, yq, true, mx, my, AfkCinema::requestStart);
            if (qx + 4.0f + LauncherGfx.width(Lang.t("Alles weitere: Aussehen \u00b7 Effekte \u00b7 PvP", "Everything else: Look \u00b7 Effects \u00b7 PvP"), 5.8f, false) < (float)(x0 + w) - camW - 6.0f) {
                LauncherGfx.text(ctx, Lang.t("Alles weitere: Aussehen \u00b7 Effekte \u00b7 PvP", "Everything else: Look \u00b7 Effects \u00b7 PvP"), qx + 4.0f, yq + 7.5f - 3.0f, -7363684, 5.8f, false);
            }
            float y2 = yq + 22.0f;
            LauncherGfx.text(ctx, Lang.t("SONGTEXT", "LYRICS"), x0 + 2, y2 + 4.0f, -8523578, 6.2f, true);
            float rb = x0 + w;
            String reload = Lang.t("\u21bb Neu laden", "\u21bb Reload");
            String resync = Lang.t("\u21c4 Zeit neu holen", "\u21c4 Resync time");
            this.button(ctx, reload, rb -= LauncherGfx.width(reload, 6.6f, true) + 16.0f, y2, false, mx, my, () -> {
                if (track != null) {
                    LyricsFetcher.reload(track);
                }
            });
            this.button(ctx, resync, rb -= LauncherGfx.width(resync, 6.6f, true) + 16.0f + 4.0f, y2, false, mx, my, MediaWatcher::resync);
            float y3 = y2 + 20.0f;
            float listH = (float)(this.y + this.height - 16) - y3;
            this.card(ctx, x0, y3, w, listH);
            if (lyr != null && !lyr.lines.isEmpty()) {
                this.lyricsList(ctx, lyr, x0, y3, w, listH);
            } else {
                LauncherGfx.text(ctx, track == null ? "" : Lang.t("Kein Songtext da.", "No lyrics yet."), x0 + 10, y3 + 10.0f, -7363684, 6.6f, false);
            }
            this.credits(ctx, x0, this.y + this.height - 12, w);
        }

        private float quick(GuiGraphicsExtractor ctx, String label, boolean on, float qx, float qy, int mx, int my, Runnable flip) {
            boolean over;
            float w = LauncherGfx.width(label, 6.4f, true) + 22.0f;
            boolean bl = over = (float)mx >= qx && (float)mx < qx + w && (float)my >= qy && (float)my < qy + 15.0f;
            Shapes.round(ctx, qx, qy, w, 15.0f, 7.5f, on ? (over ? 1149104326 : 780005574) : (over ? 0x26FFFFFF : 0x14FFFFFF));
            Shapes.ring(ctx, qx, qy, w, 15.0f, 7.5f, on ? -1719799610 : 0x24FFFFFF);
            Shapes.circle(ctx, qx + 8.0f, qy + 7.5f, 2.6f, on ? -8523578 : -10588567);
            LauncherGfx.text(ctx, label, qx + 14.0f, qy + 7.5f - 3.3f, on ? -1 : -4600634, 6.4f, true);
            this.hits.add(new Hit(qx, qy, w, 15.0f, () -> {
                flip.run();
                SkyLyricsPanels.save();
            }));
            return qx + w + 4.0f;
        }

        private void card(GuiGraphicsExtractor ctx, float x, float y, float w, float h) {
            Shapes.round(ctx, x, y, w, h, 6.0f, 0x22000000);
            Shapes.ring(ctx, x, y, w, h, 6.0f, 0x16FFFFFF);
        }

        private void credits(GuiGraphicsExtractor ctx, float x, float y, float w) {
            String s = Lang.t("Basiert auf Sky Lyrics + AFK Cinema von Snoblex & Teufl08 \u2013 mit Erlaubnis \u00fcbernommen und f\u00fcr FWM \u00fcberarbeitet \u2665", "Based on Sky Lyrics + AFK Cinema by Snoblex & Teufl08 \u2013 used with permission and reworked for FWM \u2665");
            LauncherGfx.text(ctx, LauncherGfx.fit(s, 5.6f, false, w), x + 2.0f, y, -7363684, 5.6f, false);
        }

        private void consent(GuiGraphicsExtractor ctx, float x0, float y0, float w, int mx, int my) {
            float h = 112.0f;
            this.card(ctx, x0, y0, w, h);
            LauncherGfx.text(ctx, Lang.t("Bevor es losgeht: Datenschutz", "Before we start: privacy"), x0 + 10.0f, y0 + 9.0f, -1, 8.4f, true);
            String[] de = new String[]{"Sky Lyrics liest aus der Windows-Medienanzeige, welcher Song l\u00e4uft. Das bleibt auf deinem PC.", "F\u00fcr den Songtext fragt die Mod lrclib.net an und sendet dabei Titel, Interpret, Album und Dauer.", "F\u00fcr Genre (Wetter/Uhrzeit) und fehlende Cover k\u00f6nnen Apple iTunes und Deezer gefragt werden.", "Wie bei jeder Webseite sehen diese Dienste deine IP-Adresse.", "Nie gesendet: dein Minecraft-Name, deine UUID, eine ID oder irgendwas an FWM."};
            String[] en = new String[]{"Sky Lyrics reads which song is playing from the Windows media overlay. That stays on your PC.", "For the lyrics the mod asks lrclib.net and sends title, artist, album and length.", "For the genre (weather/time) and missing covers it may ask Apple iTunes and Deezer.", "Like any website, these services see your IP address.", "Never sent: your Minecraft name, your UUID, an ID or anything to FWM."};
            for (int i = 0; i < de.length; ++i) {
                LauncherGfx.text(ctx, LauncherGfx.fit(Lang.t(de[i], en[i]), 6.2f, false, w - 20.0f), x0 + 10.0f, y0 + 26.0f + (float)(i * 11), i == 4 ? -8523578 : -4600634, 6.2f, false);
            }
            float by = y0 + h - 22.0f;
            this.button(ctx, Lang.t("Erlauben \u2013 Songtext laden", "Allow \u2013 load lyrics"), x0 + 10.0f, by, true, mx, my, () -> {
                SkyLyricsPanels.cfg().consent = "online";
                SkyLyricsPanels.save();
            });
            this.button(ctx, Lang.t("Nur lokal (ohne Songtext)", "Local only (no lyrics)"), x0 + 10.0f + this.lastW + 6.0f, by, false, mx, my, () -> {
                SkyLyricsPanels.cfg().consent = "local";
                SkyLyricsPanels.save();
            });
        }

        private void player(GuiGraphicsExtractor ctx, MediaWatcher.Track track, float x0, float y0, float w, float h, int mx, int my) {
            int cs = (int)h - 12;
            int cx = (int)x0 + 6;
            int cy = (int)y0 + 6;
            if (CoverTexture.update(track.key())) {
                ctx.blit(RenderPipelines.GUI_TEXTURED, CoverTexture.ID, cx, cy, 0.0f, 0.0f, cs, cs, cs, cs);
            } else {
                Shapes.round(ctx, cx, cy, cs, cs, 5.0f, -14992845);
                Shapes.icon(ctx, "play", (float)cx + (float)cs / 2.0f - 6.0f, (float)cy + (float)cs / 2.0f - 6.0f, 12.0f, -8523578);
            }
            float tx = cx + cs + 10;
            float tw = x0 + w - tx - 8.0f;
            String app = track.app() == null ? "" : track.app().replace(".exe", "");
            String chip = (MediaWatcher.isPlaying() ? "\u25b6 " : "\u275a\u275a ") + LauncherGfx.fit(app, 5.8f, true, 120.0f);
            float chw = LauncherGfx.width(chip, 5.8f, true) + 10.0f;
            Shapes.round(ctx, tx, y0 + 6.0f, chw, 10.0f, 5.0f, 712896710);
            LauncherGfx.text(ctx, chip, tx + 5.0f, y0 + 11.0f - 3.0f, -8523578, 5.8f, true);
            if (BeatSync.active() && BeatSync.bpm() > 0.0) {
                String bpm = Math.round(BeatSync.bpm()) + " BPM";
                Shapes.round(ctx, tx + chw + 4.0f, y0 + 6.0f, LauncherGfx.width(bpm, 5.8f, true) + 10.0f, 10.0f, 5.0f, 0x18FFFFFF);
                LauncherGfx.text(ctx, bpm, tx + chw + 9.0f, y0 + 11.0f - 3.0f, -4600634, 5.8f, true);
            }
            LauncherGfx.text(ctx, LauncherGfx.fit(track.title(), 9.6f, true, tw), tx, y0 + 20.0f, -1, 9.6f, true);
            LauncherGfx.text(ctx, LauncherGfx.fit(track.artist() + (String)(track.album().isEmpty() ? "" : " \u00b7 " + track.album()), 6.4f, false, tw), tx, y0 + 33.0f, -4600634, 6.4f, false);
            long pos = MediaWatcher.positionMs();
            long dur = Math.max(1L, track.durationMs());
            float py = y0 + h - 14.0f;
            float bw = tw * 0.62f;
            this.seekX = tx;
            this.seekW = bw;
            float frac = Math.max(0.0f, Math.min(1.0f, (float)pos / (float)dur));
            Shapes.round(ctx, tx, py, bw, 3.0f, 1.5f, 0x26FFFFFF);
            Shapes.round(ctx, tx, py, bw * frac, 3.0f, 1.5f, -8523578);
            Shapes.circle(ctx, tx + bw * frac, py + 1.5f, 3.2f, -1);
            LauncherGfx.text(ctx, Overview.time(pos) + " / " + Overview.time(track.durationMs()), tx, py + 5.0f, -7363684, 5.6f, false);
            this.hits.add(new Hit(tx, py - 4.0f, bw, 11.0f, () -> {
                this.draggingSeek = true;
            }));
            float cx2 = tx + bw + 10.0f;
            this.button(ctx, "\u23ee", cx2, py - 8.0f, false, mx, my, () -> MediaWatcher.command("prev"));
            this.button(ctx, MediaWatcher.isPlaying() ? "\u275a\u275a" : "\u25b6", cx2 += this.lastW + 3.0f, py - 8.0f, true, mx, my, () -> MediaWatcher.command("toggle"));
            this.button(ctx, "\u23ed", cx2 += this.lastW + 3.0f, py - 8.0f, false, mx, my, () -> MediaWatcher.command("next"));
            float vol = MediaWatcher.volume();
            if (vol >= 0.0f) {
                float vx = tx + bw + 10.0f;
                float vy = y0 + 22.0f;
                float vw = x0 + w - 10.0f - vx;
                if (vw > 30.0f) {
                    this.volX = vx;
                    this.volW = vw;
                    LauncherGfx.text(ctx, Lang.t("Lautst\u00e4rke", "Volume") + " " + Math.round(vol * 100.0f) + " %", vx, vy - 9.0f, -7363684, 5.6f, false);
                    Shapes.round(ctx, vx, vy, vw, 3.0f, 1.5f, 0x26FFFFFF);
                    Shapes.round(ctx, vx, vy, vw * vol, 3.0f, 1.5f, -8523578);
                    Shapes.circle(ctx, vx + vw * vol, vy + 1.5f, 3.2f, -1);
                    this.hits.add(new Hit(vx, vy - 4.0f, vw, 11.0f, () -> {
                        this.draggingVol = true;
                    }));
                }
            }
        }

        private static String time(long ms) {
            long s = Math.max(0L, ms / 1000L);
            return s / 60L + ":" + String.format("%02d", s % 60L);
        }

        private void lyricsList(GuiGraphicsExtractor ctx, Lyrics lyr, float x0, float y0, float w, float h) {
            long now = LyricsTime.songMs();
            int cur = Math.max(0, lyr.indexAt(now));
            float lineH = 13.0f;
            int shown = Math.max(3, (int)(h / lineH));
            int first = Math.max(0, Math.min(lyr.lines.size() - shown, cur - shown / 2));
            ctx.enableScissor((int)x0, (int)y0, (int)(x0 + w), (int)(y0 + h));
            for (int i = first; i < Math.min(lyr.lines.size(), first + shown); ++i) {
                Lyrics.Line line = lyr.lines.get(i);
                float ly = y0 + 5.0f + (float)(i - first) * lineH;
                String text = LauncherGfx.fit(line.text().isBlank() ? "\u266a" : line.text(), i == cur ? 8.0f : 6.8f, i == cur, w - 20.0f);
                if (i == cur) {
                    float fill = Lyrics.wordPosition(line, now);
                    float tw = LauncherGfx.width(text, 8.0f, true);
                    LauncherGfx.text(ctx, text, x0 + 10.0f, ly, 1726936562, 8.0f, true);
                    ctx.enableScissor((int)(x0 + 10.0f), (int)ly - 2, (int)(x0 + 10.0f + tw * Math.max(0.0f, Math.min(1.0f, fill))), (int)(ly + 12.0f));
                    LauncherGfx.text(ctx, text, x0 + 10.0f, ly, -8523578, 8.0f, true);
                    ctx.disableScissor();
                } else {
                    LauncherGfx.text(ctx, text, x0 + 10.0f, ly + 1.0f, i < cur ? 1726936562 : -1712392718, 6.8f, false);
                }
                if (!lyr.isHook(i) || i == cur) continue;
                float tw = LauncherGfx.width(text, 6.8f, false);
                Shapes.circle(ctx, x0 + 16.0f + tw, ly + 4.5f, 1.8f, -670398);
            }
            ctx.disableScissor();
        }

        @Override
        public boolean mouseClicked(double mx, double my, int button) {
            for (Hit h : new ArrayList<Hit>(this.hits)) {
                if (!(mx >= (double)h.x) || !(mx < (double)(h.x + h.w)) || !(my >= (double)h.y) || !(my < (double)(h.y + h.h))) continue;
                h.action.run();
                if (this.draggingSeek || this.draggingVol) {
                    this.mouseDragged(mx, my, button);
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseDragged(double mx, double my, int button) {
            if (this.draggingSeek && this.seekW > 0.0f) {
                MediaWatcher.Track tr = MediaWatcher.track();
                if (tr != null) {
                    float f = (float)Math.max(0.0, Math.min(1.0, (mx - (double)this.seekX) / (double)this.seekW));
                    MediaWatcher.seek((long)(f * (float)tr.durationMs()));
                }
                return true;
            }
            if (this.draggingVol && this.volW > 0.0f) {
                float f = (float)Math.max(0.0, Math.min(1.0, (mx - (double)this.volX) / (double)this.volW));
                MediaWatcher.setVolume(f, false);
                return true;
            }
            return false;
        }

        @Override
        public void mouseReleased() {
            if (this.draggingVol) {
                MediaWatcher.setVolume(Math.max(0.0f, MediaWatcher.volume()), true);
            }
            this.draggingSeek = false;
            this.draggingVol = false;
        }

        @Override
        public boolean mouseScrolled(double mx, double my, double v) {
            if (this.volW > 0.0f && MediaWatcher.volume() >= 0.0f) {
                MediaWatcher.setVolume(Math.max(0.0f, Math.min(1.0f, MediaWatcher.volume() + (float)v * 0.05f)), true);
                return true;
            }
            return false;
        }

        private record Hit(float x, float y, float w, float h, Runnable action) {
        }
    }
}

