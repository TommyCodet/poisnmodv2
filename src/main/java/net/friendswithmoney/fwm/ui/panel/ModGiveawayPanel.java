/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.util.FormattedCharSequence
 */
package net.friendswithmoney.fwm.ui.panel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.net.Giveaways;
import net.friendswithmoney.fwm.ui.ConfirmBox;
import net.friendswithmoney.fwm.ui.FwmTheme;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.panel.Panel;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

public class ModGiveawayPanel
extends Panel {
    private static final int LINE_H = 10;
    private static final int GAP = 6;
    private static final int SCROLL_STEP = 20;
    private static final int GOLD = -865972;
    private static final int GREEN = -10688364;
    private final ConfirmBox confirm = new ConfirmBox();
    private final List<Button> buttons = new ArrayList<Button>();
    private int scroll;
    private int contentH;

    public ModGiveawayPanel() {
        Giveaways.refresh();
    }

    @Override
    public Component title() {
        return Lang.of("fwm.news.giveaway", new Object[0]);
    }

    @Override
    public boolean showTitle() {
        return false;
    }

    private static String left(long ms) {
        if (ms <= 0L) {
            return Lang.t("vorbei", "ended");
        }
        long s = ms / 1000L;
        long d = s / 86400L;
        long h = s % 86400L / 3600L;
        long m = s % 3600L / 60L;
        if (d > 0L) {
            return Lang.t("noch ", "") + d + "d " + h + "h" + Lang.t("", " left");
        }
        if (h > 0L) {
            return Lang.t("noch ", "") + h + "h " + m + "m" + Lang.t("", " left");
        }
        return Lang.t("noch ", "") + m + "m " + s % 60L + "s" + Lang.t("", " left");
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY, float delta) {
        this.buttons.clear();
        int maxScroll = Math.max(0, this.contentH - this.height);
        this.scroll = Math.max(0, Math.min(this.scroll, maxScroll));
        long now = Giveaways.now();
        SimpleDateFormat end = new SimpleDateFormat(Lang.t("dd.MM. 'um' HH:mm", "dd.MM. 'at' HH:mm"));
        SimpleDateFormat day = new SimpleDateFormat("dd.MM.yyyy");
        int w = this.width - 8;
        int cy = this.y - this.scroll;
        ctx.enableScissor(this.x, this.y, this.x + this.width, this.y + this.height);
        List<Giveaways.Active> active = Giveaways.active();
        if (active.isEmpty()) {
            String none = Giveaways.loaded() ? Lang.t("Gerade l\u00e4uft kein Giveaway.", "No giveaway running right now.") : Lang.t("Giveaways gibt es auf HugoSMP \u2013 wird geladen \u2026", "Giveaways show on HugoSMP \u2013 loading \u2026");
            ctx.text(font, (Component)Component.literal((String)none), this.x + 4, cy + 6, FwmTheme.TEXT_DIM, false);
            cy += 20;
        }
        for (Giveaways.Active active2 : active) {
            List desc = active2.description == null || active2.description.isBlank() ? List.of() : font.split((FormattedText)Component.literal((String)active2.description), w - 14);
            String error = Giveaways.explain(Giveaways.joinResult(active2.id));
            int boxH = 46 + desc.size() * 10 + 18 + (error == null ? 0 : 10) + 4;
            if (cy + boxH > this.y && cy < this.y + this.height) {
                boolean over;
                FwmTheme.box(ctx, this.x, cy, w, boxH, FwmTheme.INSET, active2.joined ? -10688364 : FwmTheme.EDGE);
                ctx.fill(this.x, cy, this.x + 2, cy + boxH, -865972);
                ctx.text(font, (Component)Component.literal((String)"GIVEAWAY").withStyle(ChatFormatting.BOLD), this.x + 7, cy + 6, -865972, false);
                String rest = ModGiveawayPanel.left(active2.endsAt - now);
                ctx.text(font, (Component)Component.literal((String)rest), this.x + w - 6 - font.width(rest), cy + 6, FwmTheme.TEXT_DIM, false);
                int ly = cy + 6 + 10;
                ctx.text(font, (Component)Component.literal((String)font.plainSubstrByWidth(active2.title, w - 14)).withStyle(ChatFormatting.BOLD), this.x + 7, ly, FwmTheme.TEXT, false);
                ctx.text(font, (Component)Component.literal((String)font.plainSubstrByWidth(Lang.t("Preis: ", "Prize: ") + active2.prize, w - 14)), this.x + 7, ly += 10, FwmTheme.TEXT, false);
                ly += 10;
                for (FormattedCharSequence line : desc) {
                    ctx.text(font, line, this.x + 7, ly, FwmTheme.TEXT_DIM, false);
                    ly += 10;
                }
                String info = Lang.t("Endet: ", "Ends: ") + end.format(new Date(active2.endsAt)) + " \u00b7 " + active2.participants + Lang.t(" Teilnehmer", " entries");
                ctx.text(font, (Component)Component.literal((String)font.plainSubstrByWidth(info, w - 14)), this.x + 7, ly, FwmTheme.TEXT_DIM, false);
                Object label = active2.joined ? "\u2713 " + Lang.t("Du nimmst teil", "You're in") : (Giveaways.joining(active2.id) ? "\u2026" : Lang.t("Teilnehmen", "Join"));
                int bw = font.width((String)label) + 16;
                int bx = this.x + w - 6 - bw;
                boolean bl = over = !active2.joined && mouseX >= bx && mouseX < bx + bw && mouseY >= (ly += 12) && mouseY < ly + 14;
                FwmTheme.box(ctx, bx, ly, bw, 14, active2.joined ? 0x22FFFFFF : (over ? 1079830676 : FwmTheme.RAISED), active2.joined ? FwmTheme.OFF : FwmTheme.ACCENT);
                ctx.text(font, (Component)Component.literal((String)label), bx + 8, ly + 3, active2.joined ? FwmTheme.TEXT_DIM : FwmTheme.TEXT, false);
                if (!active2.joined) {
                    this.buttons.add(new Button(bx, ly, bw, 14, active2.id));
                }
                ly += 16;
                if (error != null) {
                    ctx.text(font, (Component)Component.literal((String)font.plainSubstrByWidth(error, w - 14)), this.x + 7, ly, -38037, false);
                }
            }
            cy += boxH + 6;
        }
        List<Giveaways.Recent> recent = Giveaways.recent();
        if (!recent.isEmpty()) {
            ctx.text(font, (Component)Component.literal((String)Lang.t("VERGANGENE GIVEAWAYS", "PAST GIVEAWAYS")), this.x + 2, cy += 4, FwmTheme.TEXT_DIM, false);
            cy += 12;
            for (Giveaways.Recent g : recent) {
                int color;
                String result = switch (g.status == null ? "" : g.status) {
                    case "drawn" -> Lang.t("Gewinner: ", "Winner: ") + g.winnerName;
                    case "cancelled" -> Lang.t("abgesagt", "cancelled");
                    default -> Lang.t("keine Teilnehmer", "no entries");
                };
                String line = (g.joined ? "\u2713 " : "") + g.title + " \u00b7 " + g.prize + " \u00b7 " + day.format(new Date(g.endedAt)) + " \u00b7 " + result;
                int n = g.won ? -865972 : (color = g.joined ? FwmTheme.TEXT : FwmTheme.TEXT_DIM);
                if (g.won) {
                    ctx.fill(this.x, cy - 1, this.x + w, cy + 10 - 1, 586336588);
                }
                ctx.text(font, (Component)Component.literal((String)font.plainSubstrByWidth(line, w - 4)), this.x + 2, cy, color, false);
                cy += 11;
            }
        }
        ctx.disableScissor();
        this.contentH = cy + this.scroll - this.y;
        if (this.contentH > this.height) {
            int n = Math.max(12, this.height * this.height / this.contentH);
            int barY = this.y + (this.height - n) * this.scroll / Math.max(1, this.contentH - this.height);
            ctx.fill(this.x + this.width - 3, this.y, this.x + this.width - 1, this.y + this.height, 0x22FFFFFF);
            ctx.fill(this.x + this.width - 3, barY, this.x + this.width - 1, barY + n, FwmTheme.ACCENT);
        }
        this.confirm.render(ctx, font, this.x, this.y, this.width, this.height, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.confirm.open()) {
            return this.confirm.mouseClicked(mouseX, mouseY);
        }
        if (button != 0 || !this.contains(mouseX, mouseY)) {
            return false;
        }
        for (Button b : this.buttons) {
            if (!(mouseX >= (double)b.x()) || !(mouseX < (double)(b.x() + b.w())) || !(mouseY >= (double)b.y()) || !(mouseY < (double)(b.y() + b.h()))) continue;
            this.join(b.id());
            return true;
        }
        return false;
    }

    private void join(String id) {
        if (V2.INSTANCE.modIdConsent) {
            Giveaways.join(id);
            return;
        }
        this.confirm.ask((Component)Lang.of("fwm.privacy.modIdAsk", new Object[0]), "fwm.confirm.agree", () -> {
            V2.INSTANCE.modIdConsent = true;
            V2.save();
            Giveaways.join(id);
        });
    }

    @Override
    public boolean isCapturingKeys() {
        return this.confirm.open();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.confirm.open() && this.confirm.keyPressed(keyCode);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double vertical) {
        if (!this.contains(mouseX, mouseY)) {
            return false;
        }
        this.scroll -= (int)Math.signum(vertical) * 20;
        return true;
    }

    private record Button(int x, int y, int w, int h, String id) {
    }
}

