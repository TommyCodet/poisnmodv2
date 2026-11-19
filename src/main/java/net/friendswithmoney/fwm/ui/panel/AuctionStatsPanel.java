/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 */
package net.friendswithmoney.fwm.ui.panel;

import java.util.ArrayList;
import java.util.List;
import net.friendswithmoney.fwm.auction.AuctionStats;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.ui.ConfirmBox;
import net.friendswithmoney.fwm.ui.FwmTheme;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.LauncherGfx;
import net.friendswithmoney.fwm.ui.panel.NameListPanel;
import net.friendswithmoney.fwm.ui.panel.Panel;
import net.friendswithmoney.fwm.ui.panel.PanelHost;
import net.friendswithmoney.fwm.util.Fmt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;

public class AuctionStatsPanel
extends Panel {
    private static final int BTN_H = 14;
    private static final int ROW_H = 12;
    private static final int MINT = -8523578;
    private static final int GOLD = -673493;
    private final PanelHost host;
    private final List<Hit> hits = new ArrayList<Hit>();
    private final ConfirmBox confirm = new ConfirmBox();
    private int board;
    private AuctionStats.Range range = AuctionStats.Range.WEEK;
    private String note = "";

    public AuctionStatsPanel(PanelHost host) {
        this.host = host;
    }

    @Override
    public Component title() {
        return Component.literal((String)Lang.t("Auktions-Statistik", "Auction statistics"));
    }

    private static String boardName(int i) {
        return switch (i) {
            case 0 -> Lang.t("Meist gezahlt", "Most paid");
            case 1 -> Lang.t("Siege", "Wins");
            case 2 -> Lang.t("Teilnahmen", "Joined");
            case 3 -> Lang.t("Gr\u00f6\u00dfter Fail", "Biggest fail");
            default -> Lang.t("Meine Stats", "My stats");
        };
    }

    private static String rangeName(AuctionStats.Range r) {
        return switch (r) {
            case AuctionStats.Range.TODAY -> Lang.t("Heute", "Today");
            case AuctionStats.Range.WEEK -> Lang.t("Woche", "Week");
            case AuctionStats.Range.MONTH -> Lang.t("Monat", "Month");
            default -> Lang.t("Gesamt", "All");
        };
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY, float delta) {
        this.hits.clear();
        int mx = this.confirm.open() ? -1000 : mouseX;
        int my = this.confirm.open() ? -1000 : mouseY;
        int bx = this.x;
        for (int i = 0; i < 5; ++i) {
            AuctionStats.Range[] label = AuctionStatsPanel.boardName(i);
            int w = font.width((String)label) + 10;
            int which = i;
            this.chip(ctx, font, bx, this.y, w, (String)label, this.board == i, mx, my, () -> {
                this.board = which;
            });
            bx += w + 4;
        }
        int rx = this.x;
        for (AuctionStats.Range r : AuctionStats.Range.values()) {
            String label = AuctionStatsPanel.rangeName(r);
            int w = font.width(label) + 10;
            this.chip(ctx, font, rx, this.y + 14 + 4, w, label, this.range == r, mx, my, () -> {
                this.range = r;
            });
            rx += w + 4;
        }
        int top = this.y + 28 + 12;
        int bottom = this.y + this.height - 14 - 6;
        if (this.board == 4) {
            long[] own = AuctionStats.own(this.range);
            String[][] lines = new String[][]{{Lang.t("Auktionen gehostet", "Auctions hosted"), String.valueOf(own[0])}, {Lang.t("Einnahmen", "Takings"), AuctionStatsPanel.money(own[1])}, {Lang.t("davon zu sp\u00e4t", "of it late"), AuctionStatsPanel.money(own[2])}, {Lang.t("Durchschnitt pro Auktion", "Average per auction"), AuctionStatsPanel.money(own[3])}};
            int ly = top;
            for (String[] line : lines) {
                ctx.text(font, (Component)Component.literal((String)line[0]), this.x + 4, ly, FwmTheme.TEXT_DIM, false);
                ctx.text(font, (Component)Component.literal((String)line[1]), this.x + 170, ly, FwmTheme.TEXT, false);
                ly += 14;
            }
        } else {
            AuctionStats.Board which = AuctionStats.Board.values()[this.board];
            List<AuctionStats.Row> rows = AuctionStats.board(which, this.range, Math.max(1, (bottom - top) / 12));
            if (rows.isEmpty()) {
                String none = Lang.t("Noch keine Auktionen in diesem Zeitraum.", "No auctions in this range yet.");
                ctx.text(font, (Component)Component.literal((String)none), this.x + (this.width - font.width(none)) / 2, top + 20, FwmTheme.TEXT_DIM, false);
            }
            int ly = top;
            for (int i = 0; i < rows.size(); ++i) {
                AuctionStats.Row row = rows.get(i);
                int color = i == 0 ? -673493 : (i < 3 ? -8523578 : FwmTheme.TEXT);
                ctx.text(font, (Component)Component.literal((String)(i + 1 + ".")), this.x + 4, ly, FwmTheme.TEXT_DIM, false);
                ctx.text(font, (Component)Component.literal((String)row.name()), this.x + 22, ly, color, false);
                String value = which == AuctionStats.Board.WINS || which == AuctionStats.Board.JOINED ? String.valueOf(row.value()) : AuctionStatsPanel.money(row.value());
                ctx.text(font, (Component)Component.literal((String)value), this.x + this.width - 4 - font.width(value), ly, color, false);
                ly += 12;
            }
        }
        int fy = this.y + this.height - 14;
        this.button(ctx, font, this.x, fy, 90, Lang.t("Als Text kopieren", "Copy as text"), mx, my, () -> {
            AuctionStats.Board which = AuctionStats.Board.values()[Math.min(3, this.board)];
            Minecraft.getInstance().keyboardHandler.setClipboard(AuctionStats.export(which, this.range));
            this.note = Lang.t("Kopiert \u2013 in Discord einf\u00fcgen.", "Copied \u2013 paste it into Discord.");
        });
        this.button(ctx, font, this.x + 94, fy, 90, Lang.t("Blacklist (" + V2.INSTANCE.auctionBlacklist.size() + ")", "Blacklist (" + V2.INSTANCE.auctionBlacklist.size() + ")"), mx, my, () -> this.host.openSub(new NameListPanel(this.host, "fwm.auction.blacklist", "fwm.auction.blacklistNone", "fwm.auction.blacklistAdd", () -> V2.INSTANCE.auctionBlacklist)));
        this.button(ctx, font, this.x + this.width - 80, fy, 80, Lang.t("Zur\u00fccksetzen", "Reset"), mx, my, () -> this.confirm.ask((Component)Component.literal((String)Lang.t("Alle Auktions-Statistiken l\u00f6schen?", "Delete all auction statistics?")), AuctionStats::reset));
        if (!this.note.isEmpty()) {
            ctx.text(font, (Component)Component.literal((String)this.note), this.x + 190, fy + 3, -8523578, false);
        }
        this.confirm.render(ctx, font, this.x, this.y, this.width, this.height, mouseX, mouseY);
    }

    private static String money(long cents) {
        return V2.INSTANCE != null && FwmConfig.INSTANCE.moneyHideAmounts ? "\u2022\u2022\u2022" : Fmt.compactMoney(cents);
    }

    private void chip(GuiGraphicsExtractor ctx, Font font, int cx, int cy, int w, String label, boolean on, int mx, int my, Runnable action) {
        boolean over;
        boolean bl = over = mx >= cx && mx < cx + w && my >= cy && my < cy + 14;
        LauncherGfx.round(ctx, cx, cy, w, 14, 5, on ? 1149104326 : (over ? 0x22FFFFFF : 0x11FFFFFF));
        ctx.text(font, (Component)Component.literal((String)label), cx + 5, cy + 3, on ? -8523578 : FwmTheme.TEXT, false);
        this.hits.add(new Hit(cx, cy, w, 14, action));
    }

    private void button(GuiGraphicsExtractor ctx, Font font, int bx, int by, int bw, String label, int mx, int my, Runnable action) {
        boolean over = mx >= bx && mx < bx + bw && my >= by && my < by + 14;
        FwmTheme.box(ctx, bx, by, bw, 14, FwmTheme.RAISED, over ? FwmTheme.ACCENT : FwmTheme.OFF);
        MutableComponent t = Component.literal((String)font.plainSubstrByWidth(label, bw - 4));
        ctx.text(font, (Component)t, bx + (bw - font.width((FormattedText)t)) / 2, by + 4, over ? FwmTheme.ACCENT : FwmTheme.TEXT, false);
        this.hits.add(new Hit(bx, by, bw, 14, action));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.confirm.open()) {
            return this.confirm.mouseClicked(mouseX, mouseY);
        }
        if (button != 0 || !this.contains(mouseX, mouseY)) {
            return false;
        }
        for (Hit h : this.hits) {
            if (!(mouseX >= (double)h.x()) || !(mouseX < (double)(h.x() + h.w())) || !(mouseY >= (double)h.y()) || !(mouseY < (double)(h.y() + h.h()))) continue;
            h.action().run();
            return true;
        }
        return false;
    }

    @Override
    public boolean isCapturingKeys() {
        return this.confirm.open();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.confirm.open() && this.confirm.keyPressed(keyCode);
    }

    private record Hit(int x, int y, int w, int h, Runnable action) {
    }
}

