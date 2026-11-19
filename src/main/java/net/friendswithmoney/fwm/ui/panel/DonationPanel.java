/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.network.chat.Component
 */
package net.friendswithmoney.fwm.ui.panel;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import net.friendswithmoney.fwm.auction.StreamerStore;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.stream.DonationBoard;
import net.friendswithmoney.fwm.stream.DonationStore;
import net.friendswithmoney.fwm.stream.DonationTier;
import net.friendswithmoney.fwm.stream.Donations;
import net.friendswithmoney.fwm.stream.fx.FxAnim;
import net.friendswithmoney.fwm.stream.fx.Play;
import net.friendswithmoney.fwm.ui.ConfirmBox;
import net.friendswithmoney.fwm.ui.FwmGfx;
import net.friendswithmoney.fwm.ui.FwmTheme;
import net.friendswithmoney.fwm.ui.HudEditScreen;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.LauncherGfx;
import net.friendswithmoney.fwm.ui.gfx.Shapes;
import net.friendswithmoney.fwm.ui.panel.DonationStudioScreen;
import net.friendswithmoney.fwm.ui.panel.NameListPanel;
import net.friendswithmoney.fwm.ui.panel.Panel;
import net.friendswithmoney.fwm.ui.panel.PanelHost;
import net.friendswithmoney.fwm.ui.panel.RowsPanel;
import net.friendswithmoney.fwm.ui.panel.TabbedPanel;
import net.friendswithmoney.fwm.ui.widget.ButtonRow;
import net.friendswithmoney.fwm.ui.widget.ChipsRow;
import net.friendswithmoney.fwm.ui.widget.KeybindRow;
import net.friendswithmoney.fwm.ui.widget.NumberRow;
import net.friendswithmoney.fwm.ui.widget.SliderRow;
import net.friendswithmoney.fwm.ui.widget.TextInputRow;
import net.friendswithmoney.fwm.ui.widget.TextRow;
import net.friendswithmoney.fwm.ui.widget.ToggleRow;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.network.chat.Component;

public final class DonationPanel {
    private DonationPanel() {
    }

    public static Panel create(PanelHost host) {
        return new TabbedPanel("fwm.tab.donations", List.of(new TabbedPanel.Tab(Lang.t("Rangliste", "Leaderboard"), RanksPage::new), new TabbedPanel.Tab(Lang.t("Verlauf", "History"), HistoryPage::new), new TabbedPanel.Tab(Lang.t("Stufen", "Tiers"), TiersPage::new), new TabbedPanel.Tab(Lang.t("Einstellungen", "Settings"), () -> new SettingsPage(host))));
    }

    static final class SettingsPage
    extends RowsPanel {
        private final PanelHost host;

        SettingsPage(PanelHost host) {
            this.host = host;
        }

        @Override
        public Component title() {
            return Lang.of("fwm.tab.donations", new Object[0]);
        }

        private static Component t(String de, String en) {
            return Component.literal((String)Lang.t(de, en));
        }

        @Override
        protected void buildRows() {
            DonationStore.Data d = DonationStore.get();
            Runnable save = DonationStore::changed;
            this.rows.add(new TextRow(SettingsPage.t("Regeln", "Rules")));
            this.rows.add(new ToggleRow(SettingsPage.t("Donations aktiv", "Donations on"), () -> Features.isOn("fwm.tab.donations"), v -> Features.set("fwm.tab.donations", v)));
            this.rows.add(new ToggleRow(SettingsPage.t("Nur im Streamer-Modus", "Only in streamer mode"), () -> d.onlyStreamer, v -> {
                d.onlyStreamer = v;
                save.run();
            }));
            this.rows.add(new ToggleRow(SettingsPage.t("Stream l\u00e4uft (Streamer-Modus)", "Streaming (streamer mode)"), () -> d.streamLive, v -> {
                d.streamLive = v;
                save.run();
            }));
            this.rows.add(new SliderRow(SettingsPage.t("Zusammenfassen (s, 0 = aus)", "Merge payments (s, 0 = off)"), 0, 30, () -> d.mergeSeconds, v -> {
                d.mergeSeconds = v;
                save.run();
            }, v -> Component.literal((String)(v == 0 ? Lang.t("Aus", "Off") : v + " s"))));
            this.rows.add(new ToggleRow(SettingsPage.t("Chatzeile, wenn keine Stufe passt", "Chat line when no tier fits"), () -> d.gapChat, v -> {
                d.gapChat = v;
                save.run();
            }));
            this.rows.add(new ButtonRow(SettingsPage.t("Sperrliste (z\u00e4hlen nie)", "Block list (never count)"), () -> Component.literal((String)(d.blacklist.size() + Lang.t(" Namen", " names"))), () -> this.host.openSub(new NameListPanel(this.host, Lang.t("Sperrliste", "Block list"), Lang.t("Niemand gesperrt.", "Nobody blocked."), Lang.t("Name sperren", "Block a name"), () -> d.blacklist))));
            this.rows.add(new TextRow(SettingsPage.t("Abspielen", "Playing")));
            this.rows.add(new ToggleRow(SettingsPage.t("Im Kampf nur klein", "Small in a fight"), () -> d.fightSmall, v -> {
                d.fightSmall = v;
                save.run();
            }));
            String[] fight = new String[]{"ticker", "toast", "chat"};
            this.rows.add(new ChipsRow(SettingsPage.t("Im Kampf", "In a fight"), List.of(new ChipsRow.Chip(Lang.t("Laufschrift", "Ticker"), 0), new ChipsRow.Chip("Toast", 1), new ChipsRow.Chip(Lang.t("Chatzeile", "Chat"), 2)), () -> Math.max(0, Arrays.asList(fight).indexOf(d.fightAnim)), v -> {
                d.fightAnim = fight[Math.max(0, Math.min(2, v))];
                save.run();
            }));
            this.rows.add(new ToggleRow(SettingsPage.t("Anzeige wackeln lassen", "Shake the display"), () -> d.shake, v -> {
                d.shake = v;
                save.run();
            }));
            if (Donations.SKIP != null) {
                this.rows.add(new KeybindRow(SettingsPage.t("Taste: Animation abbrechen", "Key: skip animation"), Donations.SKIP, true));
            }
            this.rows.add(new TextRow(SettingsPage.t("Top-Donater-Fenster", "Top donors window")));
            this.rows.add(new ToggleRow(SettingsPage.t("Fenster \u201eTop Donater\u201c im Spiel", "In-game window \"Top Donater\""), () -> d.topHud, v -> {
                d.topHud = v;
                save.run();
            }));
            this.rows.add(new ChipsRow(SettingsPage.t("Zeitraum", "Period"), List.of(new ChipsRow.Chip("Session", 0), new ChipsRow.Chip(Lang.t("Heute", "Today"), 1), new ChipsRow.Chip(Lang.t("Woche", "Week"), 2), new ChipsRow.Chip(Lang.t("Gesamt", "All"), 3)), () -> d.hudPeriod, v -> {
                d.hudPeriod = v;
                save.run();
            }));
            this.rows.add(new ChipsRow(SettingsPage.t("Pl\u00e4tze", "Places"), List.of(new ChipsRow.Chip("Top 3", 0), new ChipsRow.Chip("Top 5", 1)), () -> d.topCount <= 3 ? 0 : 1, v -> {
                d.topCount = v == 0 ? 3 : 5;
                save.run();
            }));
            this.rows.add(new ToggleRow(SettingsPage.t("Zeile \u201eLetzte: Name $X\u201c", "Line \"Last: name $X\""), () -> d.topLast, v -> {
                d.topLast = v;
                save.run();
            }));
            this.rows.add(new ToggleRow(SettingsPage.t("Balken des Spendenziels", "Donation goal bar"), () -> d.topGoal, v -> {
                d.topGoal = v;
                save.run();
            }));
            this.rows.add(new ButtonRow(SettingsPage.t("Position und Gr\u00f6\u00dfe", "Position and size"), () -> SettingsPage.t("HUD-Editor", "HUD editor"), () -> {
                Minecraft mc = Minecraft.getInstance();
                mc.gui.setScreen((Screen)new HudEditScreen(mc.gui.screen()));
            }));
            this.rows.add(new ToggleRow(SettingsPage.t("Krone \u00fcber dem Namen (Top 3)", "Crown over the name (top 3)"), () -> d.crown, v -> {
                d.crown = v;
                save.run();
            }));
            this.rows.add(new TextRow(SettingsPage.t("Spendenziel", "Donation goal")));
            this.rows.add(new NumberRow(SettingsPage.t("Ziel (0 = aus)", "Goal (0 = off)"), () -> d.goal / 100L, v -> {
                if (d.goal <= 0L && v > 0L) {
                    d.goalStart = System.currentTimeMillis();
                }
                d.goal = Math.max(0L, v) * 100L;
                d.goalDone = Donations.goalProgress() >= d.goal && d.goal > 0L;
                save.run();
            }, v -> Component.literal((String)(v <= 0L ? Lang.t("Aus", "Off") : Play.money(v * 100L)))));
            this.rows.add(new TextInputRow(SettingsPage.t("Wof\u00fcr", "What for"), () -> d.goalName, v -> {
                d.goalName = v == null ? "" : v;
                save.run();
            }, Lang.t("z. B. Beacon", "e.g. beacon"), 24));
            this.rows.add(new ButtonRow(SettingsPage.t("Ziel neu starten", "Restart goal"), () -> SettingsPage.t("Neu starten", "Restart"), () -> {
                d.goalStart = System.currentTimeMillis();
                d.goalDone = false;
                save.run();
            }));
            this.rows.add(new ButtonRow(SettingsPage.t("Feier testen", "Test celebration"), () -> SettingsPage.t("Test", "Test"), () -> {
                Minecraft.getInstance().gui.setScreen(null);
                Donations.celebrateGoal(true);
            }));
            this.rows.add(new TextRow(SettingsPage.t("Test", "Test")));
            this.rows.add(new TextInputRow(SettingsPage.t("Test-Spender", "Test donor"), () -> d.testName, v -> {
                String name = v == null ? "" : v.replaceAll("[^A-Za-z0-9_]", "");
                d.testName = name.isEmpty() ? "TestSpender" : name;
                save.run();
            }, "TestSpender", 16));
            this.rows.add(new ButtonRow(SettingsPage.t("Status", "Status"), () -> Component.literal((String)(Donations.live() ? Lang.t("aktiv", "active") : Lang.t("wartet", "waiting"))).withStyle(Donations.live() ? ChatFormatting.GREEN : ChatFormatting.GOLD), () -> {}));
        }
    }

    static final class TiersPage
    extends Page {
        private static final int A_PROFILE = 1;
        private static final int A_NEW = 2;
        private static final int A_EXPORT = 3;
        private static final int A_IMPORT = 4;
        private static final int A_DELPROF = 5;
        private static final int A_ON = 6;
        private static final int A_UP = 7;
        private static final int A_DOWN = 8;
        private static final int A_TEST = 9;
        private static final int A_EDIT = 10;
        private static final int A_DEL = 11;
        private static final int A_ADD = 12;
        private static final int A_DEFAULTS = 13;
        private static final int A_STUDIO = 14;
        private static final int A_RENAME = 15;
        private String note = "";
        private long noteAt;
        private boolean renaming;
        private final StringBuilder typed = new StringBuilder();

        TiersPage() {
        }

        @Override
        void draw(GuiGraphicsExtractor ctx, Font font) {
            DonationStore.Data d = DonationStore.get();
            int cx = this.x + 4;
            int cy = this.y + 2;
            ctx.text(font, Lang.t("Profil:", "Profile:"), cx, cy + 3, FwmTheme.TEXT_DIM, false);
            int px = cx + font.width(Lang.t("Profil:", "Profile:")) + 6;
            int i = 0;
            for (String name : d.profiles.keySet()) {
                boolean active = name.equals(d.profile);
                if (active && this.renaming) {
                    String shown = String.valueOf(this.typed) + (System.currentTimeMillis() / 500L % 2L == 0L ? "_" : " ");
                    int w = Math.max(50, font.width(shown) + 10);
                    Shapes.round(ctx, px, cy, w, 13.0f, 4.0f, 0x40000000);
                    Shapes.ring(ctx, px, cy, w, 13.0f, 4.0f, -11870565);
                    ctx.text(font, shown, px + 5, cy + 3, FwmTheme.TEXT, false);
                    px += w + 4;
                } else {
                    px += this.button(ctx, font, px, cy, name, active ? 1 : 0, 1, i) + 4;
                }
                ++i;
            }
            px += 4;
            px += this.button(ctx, font, px, cy, "+", 0, 2, 0) + 3;
            px += this.button(ctx, font, px, cy, Lang.t("Umbenennen", "Rename"), 0, 15, 0) + 3;
            px += this.button(ctx, font, px, cy, "Export", 0, 3, 0) + 3;
            px += this.button(ctx, font, px, cy, "Import", 0, 4, 0) + 3;
            if (d.profiles.size() > 1) {
                this.button(ctx, font, px, cy, Lang.t("L\u00f6schen", "Delete"), 2, 5, 0);
            }
            cy += 18;
            List<DonationTier> tiers = DonationStore.tiers();
            int rowH = 18;
            for (int k = 0; k < tiers.size(); ++k) {
                DonationTier t = tiers.get(k);
                int ry = cy + k * rowH;
                if (this.over(this.x, ry, this.width, rowH)) {
                    FwmTheme.hover(ctx, this.x, ry, this.width, rowH);
                }
                LauncherGfx.toggle(ctx, cx, ry + 3, t.on, -11870565, 1087957214);
                this.hit(cx, ry + 3, 22, 12, 6, k);
                Shapes.round(ctx, cx + 27, ry + 5, 8.0f, 8.0f, 4.0f, t.color);
                ctx.text(font, LauncherGfx.trim(font, t.name, 110), cx + 39, ry + 5, t.on ? FwmTheme.TEXT : FwmTheme.TEXT_DIM, false);
                String range = Play.money(t.from) + (String)(t.to < 0L ? " +" : Lang.t(" \u2013 unter ", " \u2013 under ") + Play.money(t.to));
                ctx.text(font, range, cx + 152, ry + 5, FwmTheme.TEXT_DIM, false);
                ctx.text(font, FxAnim.byId(t.anim).name(), cx + 255, ry + 5, -11870565, false);
                List<String> clash = DonationStore.overlaps(t);
                if (!clash.isEmpty()) {
                    String warn = "\u26a0";
                    ctx.text(font, warn, cx + 142, ry + 5, -670398, false);
                }
                int bx = this.x + this.width - 4;
                this.button(ctx, font, bx -= font.width("x") + 10, ry + 2, "x", 2, 11, k);
                this.button(ctx, font, bx -= font.width(Lang.t("Bearbeiten", "Edit")) + 13, ry + 2, Lang.t("Bearbeiten", "Edit"), 0, 10, k);
                this.button(ctx, font, bx -= font.width("Test") + 13, ry + 2, "Test", 0, 9, k);
                this.button(ctx, font, bx -= font.width("\u25bc") + 13, ry + 2, "\u25bc", 0, 8, k);
                this.button(ctx, font, bx -= font.width("\u25b2") + 13, ry + 2, "\u25b2", 0, 7, k);
            }
            cy += tiers.size() * rowH + 4;
            for (DonationTier t : tiers) {
                List<String> clash = DonationStore.overlaps(t);
                if (clash.isEmpty()) continue;
                ctx.text(font, "\u26a0 \u201e" + t.name + "\u201c " + Lang.t("\u00fcberschneidet sich mit ", "overlaps ") + String.join((CharSequence)", ", clash) + Lang.t(" \u2013 die h\u00f6here \u201eAb\u201c gewinnt", " \u2013 the higher \"from\" wins"), cx, cy, -670398, false);
                cy += 11;
                break;
            }
            int bx = cx;
            if (tiers.size() < 10) {
                bx += this.button(ctx, font, bx, cy, "+ " + Lang.t("Stufe", "Tier"), 0, 12, 0) + 4;
            }
            bx += this.button(ctx, font, bx, cy, Lang.t("Auswahlfenster \u00f6ffnen", "Open the picker"), 1, 14, 0) + 4;
            this.button(ctx, font, bx, cy, Lang.t("Standard wiederherstellen", "Restore defaults"), 2, 13, 0);
            cy += 18;
            if (System.currentTimeMillis() - this.noteAt < 4000L) {
                ctx.text(font, this.note, cx, cy, -11870565, false);
            }
        }

        private void note(String text) {
            this.note = text;
            this.noteAt = System.currentTimeMillis();
        }

        @Override
        void act(int action, int arg, int button) {
            DonationStore.Data d = DonationStore.get();
            List<DonationTier> tiers = DonationStore.tiers();
            if (this.renaming && action != 15) {
                this.finishRename();
            }
            switch (action) {
                case 1: {
                    int i = 0;
                    for (String name : d.profiles.keySet()) {
                        if (i++ != arg) continue;
                        d.profile = name;
                    }
                    DonationStore.changed();
                    break;
                }
                case 2: {
                    this.note(Lang.t("Neues Profil: ", "New profile: ") + DonationStore.newProfile());
                    break;
                }
                case 15: {
                    if (this.renaming) {
                        this.finishRename();
                        break;
                    }
                    this.renaming = true;
                    this.typed.setLength(0);
                    this.typed.append(d.profile);
                    break;
                }
                case 3: {
                    StreamerStore.copy(DonationStore.exportProfile());
                    this.note(Lang.t("Profil in die Zwischenablage kopiert.", "Profile copied to the clipboard."));
                    break;
                }
                case 4: {
                    String name = DonationStore.importProfile(Minecraft.getInstance().keyboardHandler.getClipboard());
                    this.note((String)(name == null ? Lang.t("In der Zwischenablage ist kein FWM-Donation-Profil.", "No FWM donation profile on the clipboard.") : Lang.t("Importiert: ", "Imported: ") + name));
                    break;
                }
                case 5: {
                    this.confirm.ask((Component)Component.literal((String)(Lang.t("Profil \u201e", "Delete profile \"") + d.profile + Lang.t("\u201c l\u00f6schen?", "\"?"))), DonationStore::deleteProfile);
                    break;
                }
                case 6: {
                    tiers.get((int)arg).on = !tiers.get((int)arg).on;
                    DonationStore.changed();
                    break;
                }
                case 7: {
                    if (arg <= 0) break;
                    tiers.add(arg - 1, tiers.remove(arg));
                    DonationStore.changed();
                    break;
                }
                case 8: {
                    if (arg >= tiers.size() - 1) break;
                    tiers.add(arg + 1, tiers.remove(arg));
                    DonationStore.changed();
                    break;
                }
                case 9: {
                    DonationTier t = tiers.get(arg);
                    Minecraft.getInstance().gui.setScreen(null);
                    Donations.test(t, t.sampleAmount());
                    break;
                }
                case 10: {
                    DonationStudioScreen.open(arg);
                    break;
                }
                case 14: {
                    DonationStudioScreen.open(0);
                    break;
                }
                case 11: {
                    if (tiers.size() <= 1) {
                        return;
                    }
                    DonationTier t = tiers.get(arg);
                    this.confirm.ask((Component)Component.literal((String)(Lang.t("Stufe \u201e", "Delete tier \"") + t.name + Lang.t("\u201c l\u00f6schen?", "\"?"))), () -> {
                        tiers.remove(t);
                        DonationStore.changed();
                    });
                    break;
                }
                case 12: {
                    if (tiers.size() >= 10) {
                        return;
                    }
                    DonationTier last = tiers.get(tiers.size() - 1);
                    DonationTier n = last.copy();
                    n.name = Lang.t("Neue Stufe", "New tier");
                    n.from = last.to < 0L ? Math.max(100L, last.from * 2L) : last.to;
                    n.to = -1L;
                    tiers.add(n);
                    DonationStore.changed();
                    DonationStudioScreen.open(tiers.size() - 1);
                    break;
                }
                case 13: {
                    this.confirm.ask((Component)Component.literal((String)Lang.t("Die Stufen dieses Profils auf die Vorlage zur\u00fccksetzen?", "Reset this profile's tiers to the template?")), "fwm.confirm.confirm", DonationStore::restoreDefaults);
                    break;
                }
            }
        }

        private void finishRename() {
            this.renaming = false;
            if (!this.typed.toString().isBlank()) {
                DonationStore.renameProfile(this.typed.toString());
            }
        }

        @Override
        public boolean isCapturingKeys() {
            return this.renaming || super.isCapturingKeys();
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (this.confirm.open()) {
                return this.confirm.keyPressed(keyCode);
            }
            if (!this.renaming) {
                return false;
            }
            if (keyCode == 259) {
                if (!this.typed.isEmpty()) {
                    this.typed.setLength(this.typed.length() - 1);
                }
            } else if (keyCode == 257 || keyCode == 335) {
                this.finishRename();
            } else if (keyCode == 256) {
                this.renaming = false;
            }
            return true;
        }

        @Override
        public boolean charTyped(CharacterEvent input) {
            if (!this.renaming) {
                return false;
            }
            if (input.isAllowedChatCharacter() && this.typed.length() < 20) {
                this.typed.append(input.codepointAsString());
            }
            return true;
        }
    }

    static final class HistoryPage
    extends Page {
        private static final int A_DELETE = 1;
        private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd.MM. HH:mm").withZone(ZoneId.systemDefault());

        HistoryPage() {
        }

        @Override
        void draw(GuiGraphicsExtractor ctx, Font font) {
            List<DonationStore.Entry> history = DonationStore.get().history;
            int cx = this.x + 4;
            int cy = this.y + 2;
            ctx.text(font, Lang.t("Die letzten 200 Donations. Das X l\u00f6scht eine Fehlz\u00e4hlung (auch aus Gesamt).", "The last 200 donations. The X deletes a miscount (from all-time too)."), cx, cy, FwmTheme.TEXT_DIM, false);
            int rowH = 15;
            int rows = Math.max(1, (this.y + this.height - (cy += 14)) / rowH);
            this.scroll = Math.max(0, Math.min(this.scroll, Math.max(0, history.size() - rows)));
            if (history.isEmpty()) {
                ctx.text(font, Lang.t("Noch keine Donations.", "No donations yet."), cx, cy + 6, FwmTheme.TEXT_DIM, false);
                return;
            }
            for (int k = this.scroll; k < Math.min(history.size(), this.scroll + rows); ++k) {
                boolean ov;
                int dx;
                int i = history.size() - 1 - k;
                DonationStore.Entry e = history.get(i);
                int ry = cy + (k - this.scroll) * rowH;
                if (this.over(this.x, ry, this.width, rowH)) {
                    FwmTheme.hover(ctx, this.x, ry, this.width, rowH);
                }
                ctx.text(font, TIME.format(Instant.ofEpochMilli(e.time)), cx, ry + 4, FwmTheme.TEXT_DIM, false);
                FwmGfx.playerHead(ctx, e.name, cx + 62, ry + 2, 11);
                ctx.text(font, LauncherGfx.trim(font, e.name, 90), cx + 78, ry + 4, FwmTheme.TEXT, false);
                String amount = Play.money(e.amount) + (String)(e.count > 1 ? " \u00b7 " + e.count + " Pays" : "");
                ctx.text(font, amount, cx + 175, ry + 4, -670398, false);
                String tier = e.tier == null || e.tier.isEmpty() ? "\u2013" : e.tier;
                ctx.text(font, LauncherGfx.trim(font, tier, 120), cx + 265, ry + 4, FwmTheme.TEXT_DIM, false);
                if (e.server != null) {
                    ctx.text(font, LauncherGfx.trim(font, e.server, 60), cx + 390, ry + 4, FwmTheme.TEXT_DIM, false);
                }
                ctx.text(font, "x", dx + 4, ry + 4, (ov = this.over(dx = this.x + this.width - 16, ry, 14, rowH)) ? -2067340 : FwmTheme.TEXT_DIM, false);
                this.hit(dx, ry, 14, rowH, 1, i);
            }
        }

        @Override
        void act(int action, int arg, int button) {
            if (action != 1) {
                return;
            }
            List<DonationStore.Entry> history = DonationStore.get().history;
            if (arg < 0 || arg >= history.size()) {
                return;
            }
            DonationStore.Entry e = history.get(arg);
            this.confirm.ask((Component)Component.literal((String)(Lang.t("Diese Donation l\u00f6schen? ", "Delete this donation? ") + e.name + " " + Play.money(e.amount))), () -> DonationStore.delete(e));
        }
    }

    static final class RanksPage
    extends Page {
        private static final int A_PERIOD = 1;
        private static final int A_RESET = 2;
        private static final int A_LIVE = 3;
        private static final int A_HUD = 4;
        private int period;
        private List<DonationStore.Rank> ranks;
        private int ranksRev;
        private int ranksPeriod;

        RanksPage() {
            this.period = DonationStore.get().hudPeriod;
            this.ranksRev = -1;
            this.ranksPeriod = -1;
        }

        @Override
        void draw(GuiGraphicsExtractor ctx, Font font) {
            DonationStore.Data d = DonationStore.get();
            int cx = this.x + 4;
            int cy = this.y + 2;
            boolean on = Features.isOn("fwm.tab.donations");
            boolean live = Donations.live();
            String status = !on ? Lang.t("Donations sind aus", "Donations are off") : (live ? Lang.t("Aktiv: Pays werden gefeiert", "Active: payments are celebrated") : Lang.t("Wartet auf den Streamer-Modus", "Waiting for the streamer mode"));
            Shapes.circle(ctx, cx + 3, cy + 6, 3.0f, live ? -11870565 : (on ? -670398 : -2067340));
            ctx.text(font, status, cx + 10, cy + 2, FwmTheme.TEXT, false);
            int bx = this.x + this.width - 4;
            String liveLabel = Lang.t("Stream l\u00e4uft: ", "Streaming: ") + (d.streamLive ? Lang.t("an", "on") : Lang.t("aus", "off"));
            this.button(ctx, font, bx -= font.width(liveLabel) + 10, cy, liveLabel, d.streamLive ? 1 : 0, 3, 0);
            cy += 18;
            int px = cx;
            for (int p = 0; p < 4; ++p) {
                px += this.button(ctx, font, px, cy, DonationBoard.periodName(p), p == this.period ? 1 : 0, 1, p) + 4;
            }
            String hud = Lang.t("Als HUD-Zeitraum", "Use on HUD");
            if (d.hudPeriod != this.period) {
                px += this.button(ctx, font, px + 6, cy, hud, 0, 4, 0) + 10;
            }
            String reset = Lang.t("Session zur\u00fccksetzen", "Reset session");
            this.button(ctx, font, this.x + this.width - 4 - font.width(reset) - 10, cy, reset, 2, 2, 0);
            if (this.ranks == null || this.ranksRev != DonationStore.revision || this.ranksPeriod != this.period) {
                this.ranks = DonationStore.ranking(this.period);
                this.ranksRev = DonationStore.revision;
                this.ranksPeriod = this.period;
            }
            cy += 18;
            long sum = 0L;
            int count = 0;
            for (DonationStore.Rank r : this.ranks) {
                sum += r.sum();
                count += r.count();
            }
            ctx.text(font, Lang.t("Summe: ", "Total: ") + Play.money(sum) + " \u00b7 " + count + (count == 1 ? " Pay \u00b7 " : " Pays \u00b7 ") + this.ranks.size() + (this.ranks.size() == 1 ? Lang.t(" Spender", " donor") : Lang.t(" Spender", " donors")), cx, cy, FwmTheme.TEXT_DIM, false);
            int rowH = 16;
            int rows = Math.max(1, (this.y + this.height - (cy += 12)) / rowH);
            this.scroll = Math.max(0, Math.min(this.scroll, Math.max(0, this.ranks.size() - rows)));
            if (this.ranks.isEmpty()) {
                ctx.text(font, Lang.t("Noch keine Spenden in diesem Zeitraum.", "No donations in this period yet."), cx, cy + 6, FwmTheme.TEXT_DIM, false);
                return;
            }
            for (int i = this.scroll; i < Math.min(this.ranks.size(), this.scroll + rows); ++i) {
                DonationStore.Rank r = this.ranks.get(i);
                int ry = cy + (i - this.scroll) * rowH;
                if (this.over(this.x, ry, this.width, rowH)) {
                    FwmTheme.hover(ctx, this.x, ry, this.width, rowH);
                }
                int col = i == 0 ? -670398 : (i == 1 ? -2563350 : (i == 2 ? -2055829 : FwmTheme.TEXT_DIM));
                ctx.text(font, "#" + (i + 1), cx, ry + 4, col, false);
                FwmGfx.playerHead(ctx, r.name(), cx + 22, ry + 2, 12);
                ctx.text(font, r.name(), cx + 40, ry + 4, i == 0 ? -670398 : FwmTheme.TEXT, false);
                String money = Play.money(r.sum());
                String pays = r.count() + (r.count() == 1 ? " Pay" : " Pays");
                int mw = font.width(money);
                ctx.text(font, money, this.x + this.width - 8 - mw, ry + 4, col == FwmTheme.TEXT_DIM ? FwmTheme.TEXT : col, false);
                ctx.text(font, pays, this.x + this.width - 8 - mw - 14 - font.width(pays), ry + 4, FwmTheme.TEXT_DIM, false);
            }
        }

        @Override
        void act(int action, int arg, int button) {
            DonationStore.Data d = DonationStore.get();
            switch (action) {
                case 1: {
                    this.period = arg;
                    this.scroll = 0;
                    break;
                }
                case 4: {
                    d.hudPeriod = this.period;
                    DonationStore.changed();
                    break;
                }
                case 3: {
                    d.streamLive = !d.streamLive;
                    DonationStore.changed();
                    break;
                }
                case 2: {
                    this.confirm.ask((Component)Component.literal((String)Lang.t("Session zur\u00fccksetzen? Die Rangliste der Session beginnt neu (Verlauf und Gesamt bleiben).", "Reset the session? Its ranking starts over (history and all-time stay).")), "fwm.confirm.confirm", DonationStore::resetSession);
                    break;
                }
            }
        }
    }

    static abstract class Page
    extends Panel {
        final Hit[] hits = new Hit[200];
        int hitCount;
        int mouseX;
        int mouseY;
        int scroll;
        final ConfirmBox confirm = new ConfirmBox();

        Page() {
            for (int i = 0; i < this.hits.length; ++i) {
                this.hits[i] = new Hit();
            }
        }

        @Override
        public Component title() {
            return Lang.of("fwm.tab.donations", new Object[0]);
        }

        void hit(int x, int y, int w, int h, int action, int arg) {
            if (this.hitCount >= this.hits.length) {
                return;
            }
            Hit hh = this.hits[this.hitCount++];
            hh.x = x;
            hh.y = y;
            hh.w = w;
            hh.h = h;
            hh.action = action;
            hh.arg = arg;
        }

        boolean over(int x, int y, int w, int h) {
            return this.mouseX >= x && this.mouseX < x + w && this.mouseY >= y && this.mouseY < y + h;
        }

        int button(GuiGraphicsExtractor ctx, Font font, int x, int y, String label, int style, int action, int arg) {
            int fill;
            int w = font.width(label) + 10;
            int h = 13;
            boolean ov = this.over(x, y, w, h);
            int n = style == 1 ? -11870565 : (style == 2 ? (ov ? 1440773236 : 716189744) : (fill = ov ? 712896710 : 0x16FFFFFF));
            int edge = style == 1 ? -11870565 : (style == 2 ? -1713343372 : (ov ? -1434586938 : 0x24FFFFFF));
            Shapes.round(ctx, x, y, w, h, 4.0f, fill);
            Shapes.ring(ctx, x, y, w, h, 4.0f, edge);
            ctx.text(font, label, x + 5, y + 3, style == 1 ? -16373990 : (style == 2 ? -2060144 : FwmTheme.TEXT), false);
            this.hit(x, y, w, h, action, arg);
            return w;
        }

        abstract void draw(GuiGraphicsExtractor var1, Font var2);

        abstract void act(int var1, int var2, int var3);

        @Override
        public void render(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY, float delta) {
            this.mouseX = this.confirm.open() ? -9999 : mouseX;
            this.mouseY = this.confirm.open() ? -9999 : mouseY;
            this.hitCount = 0;
            this.draw(ctx, font);
            this.confirm.render(ctx, font, this.x, this.y, this.width, this.height, mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int button) {
            if (this.confirm.open()) {
                return this.confirm.mouseClicked(mx, my);
            }
            for (int i = this.hitCount - 1; i >= 0; --i) {
                Hit h = this.hits[i];
                if (!(mx >= (double)h.x) || !(mx < (double)(h.x + h.w)) || !(my >= (double)h.y) || !(my < (double)(h.y + h.h))) continue;
                this.act(h.action, h.arg, button);
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseScrolled(double mx, double my, double vertical) {
            this.scroll = Math.max(0, this.scroll - (int)Math.signum(vertical));
            return true;
        }

        @Override
        public boolean isCapturingKeys() {
            return this.confirm.open();
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return this.confirm.open() && this.confirm.keyPressed(keyCode);
        }

        static final class Hit {
            int x;
            int y;
            int w;
            int h;
            int action;
            int arg;

            Hit() {
            }
        }
    }
}

