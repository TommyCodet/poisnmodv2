/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 */
package net.friendswithmoney.fwm.ui.panel;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.friendswithmoney.fwm.net.MemberData;
import net.friendswithmoney.fwm.net.Members;
import net.friendswithmoney.fwm.ui.FwmGfx;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.LauncherGfx;
import net.friendswithmoney.fwm.ui.RankColors;
import net.friendswithmoney.fwm.ui.gfx.FwmFont;
import net.friendswithmoney.fwm.ui.panel.Panel;
import net.friendswithmoney.fwm.ui.panel.PanelHost;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;

public class MembersPanel
extends Panel {
    private static final int TEXT = -1379602;
    private static final int SUB = -7362144;
    private static final int MINT = -8523578;
    private static final int HEAD_H = 26;
    private static final int SEARCH_H = 16;
    private static final int CHIP_H = 13;
    private static final int SECTION_H = 18;
    private static final int CARD_H = 28;
    private static final int CARD_GAP = 5;
    private static final int FOOTER_H = 11;
    private static final int SCROLLBAR_W = 2;
    private final List<Section> sections = new ArrayList<Section>();
    private int builtFor = -1;
    private String builtQuery = "";
    private String builtClan = "";
    private int matches;
    private final PanelHost host;
    private EditBox search;
    private String query = "";
    private String clan = "";
    private int listY;
    private int listH;
    private int scroll;
    private int contentH;
    private final List<int[]> chipBoxes = new ArrayList<int[]>();
    private final List<String> chipClans = new ArrayList<String>();

    public MembersPanel(PanelHost host) {
        this.host = host;
    }

    @Override
    public Component title() {
        return Lang.of("fwm.tab.members", new Object[0]);
    }

    private int searchY() {
        return this.y + 26 + 2;
    }

    private int searchW() {
        return Math.min(220, this.width / 2);
    }

    @Override
    protected void onResize() {
        if (this.search != null) {
            this.host.detach((AbstractWidget)this.search);
        }
        Font font = FwmFont.ui();
        this.search = new EditBox(font, this.x + 18, this.searchY() + 4, this.searchW() - 24, 10, (Component)Lang.of("fwm.members.search", new Object[0]));
        this.search.setBordered(false);
        this.search.setMaxLength(32);
        this.search.setTextColor(-1379602);
        this.search.setHint((Component)Component.literal((String)Lang.t("Minecraft-Namen suchen \u2026", "Search Minecraft names \u2026")));
        this.search.setValue(this.query);
        this.search.setResponder(text -> {
            this.query = text == null ? "" : text.trim();
            this.scroll = 0;
        });
        this.host.attach((AbstractWidget)this.search);
        this.scroll = Math.min(this.scroll, this.maxScroll());
    }

    private static Map<String, Integer> clanCounts() {
        ArrayList<MemberData.Member> all = new ArrayList<MemberData.Member>(Members.list());
        all.sort(Comparator.comparingInt(m -> Members.clanOrder(m.clan)).thenComparing(m -> m.clan == null ? "" : m.clan));
        LinkedHashMap<String, Integer> out = new LinkedHashMap<String, Integer>();
        for (MemberData.Member m2 : all) {
            out.merge(m2.clan == null ? "?" : m2.clan, 1, Integer::sum);
        }
        return out;
    }

    private static String rankTitle(String role) {
        if (role == null || role.isBlank()) {
            return Lang.t("Mitglied", "Member");
        }
        int cut = role.indexOf(8226);
        return (cut > 0 ? role.substring(0, cut) : role).trim();
    }

    private void build() {
        List<MemberData.Member> members = Members.list();
        if (!(this.builtFor != members.size() || !this.builtQuery.equals(this.query) || !this.builtClan.equals(this.clan) || this.sections.isEmpty() && this.query.isEmpty() && this.clan.isEmpty())) {
            return;
        }
        String needle = this.query.toLowerCase(Locale.ROOT);
        ArrayList<MemberData.Member> sorted = new ArrayList<MemberData.Member>();
        for (MemberData.Member m2 : members) {
            if (!this.clan.isEmpty() && !this.clan.equals(m2.clan) || !needle.isEmpty() && !MembersPanel.contains(m2.name, needle) && !MembersPanel.contains(m2.clan, needle) && !MembersPanel.contains(m2.role, needle)) continue;
            sorted.add(m2);
        }
        sorted.sort(Comparator.comparingInt(m -> Members.rankIndex(m.role)).thenComparingInt(m -> Members.clanOrder(m.clan)).thenComparing(m -> m.name == null ? "" : m.name.toLowerCase(Locale.ROOT)));
        this.sections.clear();
        Section current = null;
        for (MemberData.Member m3 : sorted) {
            int rank = Members.rankIndex(m3.role);
            if (current == null || current.rank() != rank) {
                current = new Section(MembersPanel.rankTitle(m3.role), rank, new ArrayList<MemberData.Member>());
                this.sections.add(current);
            }
            current.members().add(m3);
        }
        this.builtFor = members.size();
        this.builtQuery = this.query;
        this.builtClan = this.clan;
        this.matches = sorted.size();
        this.scroll = Math.min(this.scroll, this.maxScroll());
    }

    private static boolean contains(String text, String needle) {
        return text != null && text.toLowerCase(Locale.ROOT).contains(needle);
    }

    private int columns() {
        return Math.max(2, Math.min(4, (this.width - 2 - 4) / 170));
    }

    private int measure() {
        int cols = this.columns();
        int h = 0;
        for (Section s : this.sections) {
            int rows = (s.members().size() + cols - 1) / cols;
            h += 18 + rows * 33 + 6;
        }
        return h;
    }

    private int maxScroll() {
        return Math.max(0, this.contentH - this.listH);
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY, float delta) {
        this.build();
        this.header(ctx, font);
        boolean focused = this.search.isFocused();
        LauncherGfx.round(ctx, this.x, this.searchY(), this.searchW(), 16, 5, 0x33000000);
        LauncherGfx.ring(ctx, this.x, this.searchY(), this.searchW(), 16, 5, focused ? -1434586938 : 0x22FFFFFF);
        MembersPanel.searchGlyph(ctx, this.x + 6, this.searchY() + 4, -7362144);
        this.search.extractWidgetRenderState(ctx, mouseX, mouseY, delta);
        int chipsBottom = this.chips(ctx, font, mouseX, mouseY);
        this.listY = chipsBottom + 6;
        this.listH = Math.max(28, this.y + this.height - 11 - 2 - this.listY);
        this.contentH = this.measure();
        this.scroll = Math.max(0, Math.min(this.maxScroll(), this.scroll));
        this.list(ctx, font, mouseX, mouseY);
        this.scrollbar(ctx);
        this.footer(ctx, font);
    }

    private void header(GuiGraphicsExtractor ctx, Font font) {
        LauncherGfx.big(ctx, font, Lang.t("Clan-Mitglieder", "Clan members"), this.x, this.y + 1, 1.5f, -1379602, true);
        ctx.text(font, Lang.t("Viele K\u00f6pfe. Ein Clan.", "Many heads. One clan."), this.x, this.y + 16, -7362144, false);
        int clans = MembersPanel.clanCounts().size();
        String a = String.valueOf(Members.list().size());
        String b = String.valueOf(clans);
        String al = Lang.t("Mitglieder", "Members");
        String bl = "Clans";
        int bw = Math.max(font.width(b) * 2, font.width(bl));
        int bx = this.x + this.width - bw;
        LauncherGfx.big(ctx, font, b, bx + (bw - font.width(b) * 2) / 2, this.y, 2.0f, -1379602, true);
        ctx.text(font, bl, bx + (bw - font.width(bl)) / 2, this.y + 17, -7362144, false);
        ctx.fill(bx - 9, this.y + 2, bx - 8, this.y + 24, 0x22FFFFFF);
        int aw = Math.max(font.width(a) * 2, font.width(al));
        int ax = bx - 18 - aw;
        LauncherGfx.big(ctx, font, a, ax + (aw - font.width(a) * 2) / 2, this.y, 2.0f, -1379602, true);
        ctx.text(font, al, ax + (aw - font.width(al)) / 2, this.y + 17, -7362144, false);
    }

    private int chips(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY) {
        this.chipBoxes.clear();
        this.chipClans.clear();
        int cx = this.x + this.searchW() + 8;
        int cy = this.searchY() + 1;
        int right = this.x + this.width;
        ArrayList<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>();
        list.add(Map.entry("", Members.list().size()));
        list.addAll(MembersPanel.clanCounts().entrySet());
        for (Map.Entry entry : list) {
            boolean over;
            String name = ((String)entry.getKey()).isEmpty() ? Lang.t("Alle", "All") : (String)entry.getKey();
            String count = String.valueOf(entry.getValue());
            int w = font.width(name) + font.width(count) + 14;
            if (cx + w > right) {
                cx = this.x;
                cy += 17;
            }
            boolean on = this.clan.equals(entry.getKey());
            boolean bl = over = mouseX >= cx && mouseX < cx + w && mouseY >= cy && mouseY < cy + 13;
            LauncherGfx.round(ctx, cx, cy, w, 13, 4, on ? 863891654 : (over ? 0x22FFFFFF : 0x10FFFFFF));
            LauncherGfx.ring(ctx, cx, cy, w, 13, 4, on ? -1434586938 : 0x18FFFFFF);
            ctx.text(font, name, cx + 5, cy + 3, on ? -8523578 : -1379602, false);
            ctx.text(font, count, cx + 9 + font.width(name), cy + 3, -7362144, false);
            this.chipBoxes.add(new int[]{cx, cy, w, 13});
            this.chipClans.add((String)entry.getKey());
            cx += w + 4;
        }
        return Math.max(this.searchY() + 16, cy + 13);
    }

    private void list(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY) {
        if (this.sections.isEmpty()) {
            MutableComponent note = Lang.of(!this.query.isEmpty() || !this.clan.isEmpty() ? "fwm.items.noMatch" : (Members.state() == Members.State.ERROR ? "fwm.board.offline" : "fwm.board.loading"), new Object[0]);
            ctx.text(font, (Component)note, this.x + (this.width - font.width((FormattedText)note)) / 2, this.listY + this.listH / 2 - 4, -7362144, false);
            return;
        }
        String self = FwmGfx.playerName();
        int cols = this.columns();
        int areaW = this.width - 2 - 4;
        int cardW = (areaW - (cols - 1) * 5) / cols;
        boolean mouseIn = mouseY >= this.listY && mouseY < this.listY + this.listH;
        ctx.enableScissor(this.x, this.listY, this.x + this.width, this.listY + this.listH);
        int cy = this.listY - this.scroll;
        for (Section s : this.sections) {
            int rows = (s.members().size() + cols - 1) / cols;
            int sectionH = 18 + rows * 33 + 6;
            if (cy + sectionH < this.listY || cy > this.listY + this.listH) {
                cy += sectionH;
                continue;
            }
            int color = RankColors.flat(s.rank());
            LauncherGfx.round(ctx, this.x + 1, cy + 6, 4, 4, 2, color);
            LauncherGfx.bold(ctx, font, s.title(), this.x + 9, cy + 4, color);
            String n = String.valueOf(s.members().size());
            int nx = this.x + 9 + font.width((FormattedText)Component.literal((String)s.title()).withStyle(ChatFormatting.BOLD)) + 6;
            int nw = font.width(n) + 8;
            LauncherGfx.round(ctx, nx, cy + 2, nw, 12, 3, 0x22FFFFFF);
            ctx.text(font, n, nx + 4, cy + 4, -7362144, false);
            ctx.fill(nx + nw + 6, cy + 8, this.x + areaW, cy + 9, 0x18FFFFFF);
            cy += 18;
            List<MemberData.Member> ms = s.members();
            for (int r = 0; r < rows; ++r) {
                int inRow = Math.min(cols, ms.size() - r * cols);
                int rowX = this.x + (areaW - (inRow * cardW + (inRow - 1) * 5)) / 2;
                for (int c = 0; c < inRow; ++c) {
                    MemberData.Member m = ms.get(r * cols + c);
                    int cx = rowX + c * (cardW + 5);
                    this.card(ctx, font, m, cx, cy, cardW, color, self, mouseIn ? mouseX : -1, mouseIn ? mouseY : -1);
                }
                cy += 33;
            }
            cy += 6;
        }
        ctx.disableScissor();
    }

    private void card(GuiGraphicsExtractor ctx, Font font, MemberData.Member m, int cx, int cy, int w, int rankColor, String self, int mouseX, int mouseY) {
        String since;
        int sx;
        String name = m.name == null ? "?" : m.name;
        boolean mine = name.equalsIgnoreCase(self);
        boolean over = mouseX >= cx && mouseX < cx + w && mouseY >= cy && mouseY < cy + 28;
        LauncherGfx.round(ctx, cx, cy, w, 28, 6, over ? 0x2CFFFFFF : 0x1AFFFFFF);
        LauncherGfx.ring(ctx, cx, cy, w, 28, 6, mine ? -1434586938 : (over ? 0x55FFFFFF : 0x1CFFFFFF));
        LauncherGfx.round(ctx, cx + 4, cy + 4, 20, 20, 4, 0x40000000);
        LauncherGfx.ring(ctx, cx + 4, cy + 4, 20, 20, 4, 0x22FFFFFF);
        FwmGfx.playerHead(ctx, name, cx + 6, cy + 6, 16);
        int tx = cx + 30;
        int room = w - (tx - cx) - 6;
        LauncherGfx.bold(ctx, font, font.plainSubstrByWidth(name, room), tx, cy + 5, mine ? -8523578 : -1379602);
        String tag = m.clan == null ? "?" : m.clan;
        int tw = font.width(tag) + 6;
        LauncherGfx.round(ctx, tx, cy + 15, tw, 10, 2, 0x22FFFFFF);
        LauncherGfx.ring(ctx, tx, cy + 15, tw, 10, 2, 0x22FFFFFF);
        ctx.text(font, tag, tx + 3, cy + 16, -4666172, false);
        if (m.joinedAt > 0L && (sx = tx + tw + 5) + font.width(since = Lang.t("Seit ", "Since ") + Instant.ofEpochMilli(m.joinedAt).atZone(ZoneId.systemDefault()).toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))) <= cx + w - 4) {
            ctx.text(font, since, sx, cy + 16, -7362144, false);
        }
    }

    private static void searchGlyph(GuiGraphicsExtractor ctx, int gx, int gy, int c) {
        ctx.fill(gx + 1, gy, gx + 5, gy + 1, c);
        ctx.fill(gx, gy + 1, gx + 1, gy + 5, c);
        ctx.fill(gx + 5, gy + 1, gx + 6, gy + 5, c);
        ctx.fill(gx + 1, gy + 5, gx + 5, gy + 6, c);
        ctx.fill(gx + 5, gy + 6, gx + 6, gy + 7, c);
        ctx.fill(gx + 6, gy + 7, gx + 7, gy + 8, c);
    }

    private void scrollbar(GuiGraphicsExtractor ctx) {
        if (this.maxScroll() <= 0) {
            return;
        }
        int trackX = this.x + this.width - 2;
        ctx.fill(trackX, this.listY, trackX + 2, this.listY + this.listH, 0x18FFFFFF);
        int thumbH = Math.max(12, this.listH * this.listH / Math.max(1, this.contentH));
        int thumbY = this.listY + (this.listH - thumbH) * this.scroll / this.maxScroll();
        ctx.fill(trackX, thumbY, trackX + 2, thumbY + thumbH, -8523578);
    }

    private void footer(GuiGraphicsExtractor ctx, Font font) {
        int barY = this.y + this.height - 11;
        MutableComponent left = this.query.isEmpty() && this.clan.isEmpty() ? Lang.of("fwm.members.count", Members.list().size()) : Lang.of("fwm.members.countFiltered", this.matches, Members.list().size());
        ctx.text(font, (Component)left, this.x + 2, barY + 2, -7362144, false);
        long age = Members.ageMinutes();
        MutableComponent right = age < 0L ? Lang.of("fwm.board.offline", new Object[0]) : Lang.of("fwm.board.updated", age);
        ctx.text(font, (Component)right, this.x + this.width - 2 - font.width((FormattedText)right), barY + 2, -7362144, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !this.contains(mouseX, mouseY)) {
            return false;
        }
        this.search.setFocused(mouseX >= (double)this.x && mouseX < (double)(this.x + this.searchW()) && mouseY >= (double)this.searchY() && mouseY < (double)(this.searchY() + 16));
        for (int i = 0; i < this.chipBoxes.size(); ++i) {
            int[] b = this.chipBoxes.get(i);
            if (!(mouseX >= (double)b[0]) || !(mouseX < (double)(b[0] + b[2])) || !(mouseY >= (double)b[1]) || !(mouseY < (double)(b[1] + b[3]))) continue;
            this.clan = this.chipClans.get(i);
            this.scroll = 0;
            return true;
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.search != null && this.search.isFocused() && this.search.keyPressed(new KeyEvent(keyCode, scanCode, modifiers));
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        return this.search != null && this.search.isFocused() && this.search.charTyped(input);
    }

    @Override
    public boolean isCapturingKeys() {
        return this.search != null && this.search.isFocused();
    }

    @Override
    public void onHidden() {
        if (this.search != null) {
            this.host.detach((AbstractWidget)this.search);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double vertical) {
        if (!this.contains(mouseX, mouseY)) {
            return false;
        }
        this.scroll = Math.max(0, Math.min(this.maxScroll(), this.scroll - (int)Math.signum(vertical) * 33));
        return true;
    }

    private record Section(String title, int rank, List<MemberData.Member> members) {
    }
}

