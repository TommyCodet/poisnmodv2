/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.item.ItemStack
 */
package net.friendswithmoney.fwm.ui.panel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.net.Market;
import net.friendswithmoney.fwm.net.MarketData;
import net.friendswithmoney.fwm.ui.FwmGfx;
import net.friendswithmoney.fwm.ui.FwmTheme;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.SharpIcons;
import net.friendswithmoney.fwm.ui.WebIcons;
import net.friendswithmoney.fwm.ui.gfx.FwmFont;
import net.friendswithmoney.fwm.ui.panel.MarketItemPanel;
import net.friendswithmoney.fwm.ui.panel.Panel;
import net.friendswithmoney.fwm.ui.panel.PanelHost;
import net.friendswithmoney.fwm.ui.panel.PricePingPanel;
import net.friendswithmoney.fwm.util.Fmt;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class MarketPanel
extends Panel {
    private static final int SEARCH_H = 15;
    private static final int HEADER_H = 10;
    private static final int ROW_H = 18;
    private static final int STATUS_H = 13;
    private static final int SCROLLBAR_W = 4;
    private static final int ICON = 16;
    static final int UP = -12723068;
    static final int DOWN = -1547164;
    private static final String PRICE_SAMPLE = "$888.8M";
    private static final int NAME_MIN = 40;
    private static final long TYPE_PAUSE_MS = 300L;
    private static final int PREFETCH_ROWS = 4;
    private static final Map<String, ItemStack> ICONS = new HashMap<String, ItemStack>();
    private final PanelHost host;
    private EditBox search;
    private String pendingQuery;
    private long pendingAt;
    private int listY;
    private int listH;
    private int rowsVisible;
    private int scroll;
    private boolean draggingBar;
    private int grabOffset;
    private static final int BAR_GRIP = 3;
    private final boolean pickForPing;
    private int[] watchChip = new int[4];
    private static final int REFRESH = 11;

    public MarketPanel(PanelHost host) {
        this(host, false);
    }

    public MarketPanel(PanelHost host, boolean pickForPing) {
        this.host = host;
        this.pickForPing = pickForPing;
    }

    @Override
    public Component title() {
        return Lang.of("fwm.tab.market", new Object[0]);
    }

    @Override
    protected void onResize() {
        String previous;
        this.listY = this.y + 15 + 2 + 10;
        this.listH = Math.max(18, this.y + this.height - 13 - 2 - this.listY);
        this.rowsVisible = Math.max(1, this.listH / 18);
        String string = previous = this.search == null ? Market.query() : this.search.getValue();
        if (this.search != null) {
            this.host.detach((AbstractWidget)this.search);
        }
        Font font = FwmFont.ui();
        this.search = new EditBox(font, this.x + 5, this.y + 2, this.width - 10, 11, (Component)Lang.of("fwm.market.search", new Object[0]));
        this.search.setBordered(false);
        this.search.setMaxLength(32);
        this.search.setTextColor(FwmTheme.TEXT);
        this.search.setHint((Component)Lang.of("fwm.market.search", new Object[0]));
        this.search.setValue(previous);
        this.search.setResponder(text -> {
            this.pendingQuery = text;
            this.pendingAt = System.currentTimeMillis();
        });
        this.host.attach((AbstractWidget)this.search);
        Market.openPage();
    }

    private List<MarketData.Item> items() {
        List<MarketData.Item> all = Market.items();
        if (!V2.INSTANCE.marketWatchOnly) {
            return all;
        }
        ArrayList<MarketData.Item> out = new ArrayList<MarketData.Item>();
        for (MarketData.Item item : all) {
            if (!MarketPanel.watched(item)) continue;
            out.add(item);
        }
        return out;
    }

    private static boolean watched(MarketData.Item item) {
        return item.itemKey != null && V2.INSTANCE.marketWatch.contains(item.itemKey);
    }

    private static void toggleWatch(MarketData.Item item) {
        if (item.itemKey == null) {
            return;
        }
        List<String> list = V2.INSTANCE.marketWatch;
        if (!list.remove(item.itemKey)) {
            list.add(item.itemKey);
        }
        V2.save();
    }

    private static void star(GuiGraphicsExtractor ctx, int sx, int sy, boolean filled, int color) {
        String[] rows = new String[]{"...#...", "..###..", "#######", ".#####.", "..###..", ".##.##.", ".#...#."};
        for (int r = 0; r < rows.length; ++r) {
            for (int c = 0; c < 7; ++c) {
                boolean edge;
                if (rows[r].charAt(c) != '#') continue;
                boolean bl = edge = r == 0 || c == 0 || c == 6 || r == 6 || rows[r].charAt(Math.max(0, c - 1)) == '.' || rows[r].charAt(Math.min(6, c + 1)) == '.';
                if (!filled && !edge) continue;
                ctx.fill(sx + c, sy + r, sx + c + 1, sy + r + 1, color);
            }
        }
    }

    private int maxScroll() {
        return Math.max(0, this.items().size() - this.rowsVisible);
    }

    static Component nameFor(MarketData.Item item) {
        boolean hasDisplay;
        boolean german = "de".equalsIgnoreCase(FwmConfig.INSTANCE.language);
        boolean bl = hasDisplay = item.displayName != null && !item.displayName.isBlank();
        if (german && hasDisplay) {
            return Component.literal((String)item.displayName);
        }
        ItemStack stack = MarketPanel.iconFor(item);
        if (!stack.isEmpty()) {
            String base = stack.getHoverName().getString();
            if (item.variantKey == null || item.variantKey.isBlank()) {
                return Component.literal((String)base);
            }
            return Component.literal((String)(base + " - " + MarketPanel.spell(item.variantKey)));
        }
        if (hasDisplay) {
            return Component.literal((String)item.displayName);
        }
        return Component.literal((String)(item.itemKey == null ? "?" : item.itemKey));
    }

    private static String spell(String key) {
        StringBuilder out = new StringBuilder();
        for (String part : key.split("_")) {
            if (part.isEmpty()) continue;
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return out.length() == 0 ? key : out.toString();
    }

    public static void drawIcon(GuiGraphicsExtractor ctx, MarketData.Item item, int left, int top) {
        ItemStack stack = MarketPanel.iconFor(item);
        if (!stack.isEmpty()) {
            ctx.item(stack, left, top);
            return;
        }
        Identifier web = WebIcons.get(item.icon);
        if (web != null) {
            FwmGfx.blit(ctx, web, left, top, 16, 16, 16, 16, 16);
        }
    }

    static ItemStack iconFor(MarketData.Item item) {
        String key;
        String string = key = item.baseItemKey == null ? item.itemKey : item.baseItemKey;
        if (key == null || key.isBlank()) {
            return ItemStack.EMPTY;
        }
        ItemStack cached = ICONS.get(key);
        if (cached != null) {
            return cached;
        }
        ItemStack stack = ItemStack.EMPTY;
        try {
            Identifier id = Identifier.tryParse((String)(key.contains(":") ? key : "minecraft:" + key));
            if (id != null) {
                stack = BuiltInRegistries.ITEM.getOptional(id).map(ItemStack::new).orElse(ItemStack.EMPTY);
            }
        }
        catch (Exception e) {
            stack = ItemStack.EMPTY;
        }
        ICONS.put(key, stack);
        return stack;
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY, float delta) {
        this.applyPendingSearch();
        FwmTheme.box(ctx, this.x, this.y, this.width, 15, FwmTheme.RAISED, this.search.isFocused() ? FwmTheme.ACCENT : FwmTheme.OFF);
        this.search.extractWidgetRenderState(ctx, mouseX, mouseY, delta);
        this.renderHeader(ctx, font);
        this.renderList(ctx, font, mouseX, mouseY);
        this.renderStatus(ctx, font, mouseX, mouseY);
    }

    private int rowWidth() {
        return this.width - (this.maxScroll() > 0 ? 6 : 0);
    }

    private void renderHeader(GuiGraphicsExtractor ctx, Font font) {
        int rowW = this.rowWidth();
        int textY = this.y + 15 + 2;
        int color = FwmTheme.TEXT_DIM;
        boolean only = V2.INSTANCE.marketWatchOnly;
        String watch = Lang.t("Watchlist", "Watchlist") + " (" + V2.INSTANCE.marketWatch.size() + ")";
        MarketPanel.star(ctx, this.x + 2, textY, only, only ? -673493 : color);
        ctx.text(font, (Component)Component.literal((String)watch), this.x + 12, textY, only ? -673493 : color, false);
        this.watchChip = new int[]{this.x, textY - 1, 14 + font.width(watch), 10};
        String price = " " + Lang.str("fwm.market.colPrice", new Object[0]);
        int priceRight = this.x + rowW - 4;
        int priceX = priceRight - font.width(price);
        ctx.text(font, (Component)Component.literal((String)price), priceX, textY, color, false);
        MarketPanel.drawAverageSign(ctx, font, priceX - font.width("O"), textY, color);
        String change = Lang.str("fwm.market.colChange", new Object[0]);
        int changeRight = this.x + rowW - 4 - font.width(PRICE_SAMPLE) - 6;
        ctx.text(font, (Component)Component.literal((String)change), changeRight - font.width(change), textY, color, false);
    }

    private static void drawAverageSign(GuiGraphicsExtractor ctx, Font font, int left, int top, int color) {
        ctx.text(font, (Component)Component.literal((String)"O"), left, top, color, false);
        int w = font.width("O") - 1;
        for (int i = 0; i <= 8; ++i) {
            int px = left + w - Math.round((float)(i * (w + 1)) / 8.0f);
            ctx.fill(px, top - 1 + i, px + 1, top + i, color);
        }
    }

    private void applyPendingSearch() {
        if (this.pendingQuery == null) {
            return;
        }
        if (System.currentTimeMillis() - this.pendingAt < 300L) {
            return;
        }
        Market.search(this.pendingQuery);
        this.pendingQuery = null;
        this.scroll = 0;
    }

    private void renderList(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY) {
        int index;
        List<MarketData.Item> all = this.items();
        if (all.isEmpty()) {
            MutableComponent empty = Lang.of(this.emptyKey(), new Object[0]);
            ctx.text(font, (Component)empty, this.x + (this.width - font.width((FormattedText)empty)) / 2, this.listY + this.listH / 2 - 4, FwmTheme.TEXT_DIM, false);
            return;
        }
        this.scroll = Math.min(this.scroll, this.maxScroll());
        ctx.enableScissor(this.x, this.listY, this.x + this.width, this.listY + this.listH);
        for (int i = 0; i < this.rowsVisible + 1 && (index = this.scroll + i) < all.size(); ++i) {
            this.renderRow(ctx, font, all.get(index), index, this.listY + i * 18, mouseX, mouseY);
        }
        ctx.disableScissor();
        this.renderScrollbar(ctx);
        if (Market.hasMore() && !Market.busy() && all.size() <= this.rowsVisible) {
            Market.loadMore();
        }
    }

    private String emptyKey() {
        if (Market.busy()) {
            return "fwm.market.loading";
        }
        if (Market.state() == Market.State.ERROR) {
            return "fwm.market.offline";
        }
        if (!Market.query().isBlank()) {
            return "fwm.items.noMatch";
        }
        return "fwm.market.loading";
    }

    private void renderRow(GuiGraphicsExtractor ctx, Font font, MarketData.Item item, int index, int rowY, int mouseX, int mouseY) {
        String text;
        int changeX;
        boolean on;
        boolean hovered;
        int rowW = this.rowWidth();
        boolean bl = hovered = mouseX >= this.x && mouseX < this.x + rowW && mouseY >= rowY && mouseY < rowY + 18 && mouseY >= this.listY && mouseY < this.listY + this.listH;
        if (hovered) {
            ctx.fill(this.x, rowY, this.x + rowW, rowY + 18, FwmTheme.HOVER);
        }
        MarketPanel.star(ctx, this.x + 2, rowY + 5, on, (on = MarketPanel.watched(item)) ? -673493 : 0x55FFFFFF);
        MarketPanel.drawIcon(ctx, item, this.x + 12, rowY + 1);
        Objects.requireNonNull(font);
        int textY = rowY + (18 - 9) / 2 + 1;
        int nameX = this.x + 12 + 16 + 4;
        String price = this.priceText(item);
        boolean hasPrice = Market.avgPrice(item.itemKey) != null;
        ctx.text(font, (Component)Component.literal((String)price), this.x + rowW - 4 - font.width(price), textY, hovered ? FwmTheme.ACCENT : (hasPrice ? FwmTheme.TEXT : FwmTheme.TEXT_DIM), false);
        int priceCol = Math.max(font.width(price), font.width(PRICE_SAMPLE));
        int changeRight = this.x + rowW - 4 - priceCol - 6;
        int nameRoom = changeRight - 4 - nameX;
        Double change = Market.change(item.itemKey);
        if (change != null && (changeX = changeRight - font.width(text = MarketPanel.changeText(change))) - 4 - nameX >= 40) {
            ctx.text(font, (Component)Component.literal((String)text), changeX, textY, MarketPanel.changeColor(change), false);
            nameRoom = changeX - 4 - nameX;
        }
        MutableComponent name = Component.literal((String)font.plainSubstrByWidth(MarketPanel.nameFor(item).getString(), Math.max(8, nameRoom)));
        ctx.text(font, (Component)name, nameX, textY, FwmTheme.TEXT, false);
        FwmTheme.divider(ctx, this.x, rowY + 18 - 1, rowW);
    }

    static String changeText(double change) {
        String number = Math.abs(change) >= 100.0 ? String.valueOf(Math.round(change)) : String.format(Locale.ROOT, "%.1f", change);
        return (change > 0.0 ? "+" : "") + number + "%";
    }

    static int changeColor(double change) {
        if (change > 0.0) {
            return -12723068;
        }
        if (change < 0.0) {
            return -1547164;
        }
        return FwmTheme.TEXT_DIM;
    }

    private String priceText(MarketData.Item item) {
        Double avg = Market.avgPrice(item.itemKey);
        return avg == null ? "-" : "$" + Fmt.compactUp(Math.round(avg));
    }

    private void renderScrollbar(GuiGraphicsExtractor ctx) {
        if (this.maxScroll() <= 0) {
            return;
        }
        int trackX = this.x + this.width - 4;
        ctx.fill(trackX, this.listY, trackX + 4, this.listY + this.listH, FwmTheme.OFF);
        int thumbH = this.thumbH();
        int thumbY = this.thumbY(thumbH);
        ctx.fill(trackX, thumbY, trackX + 4, thumbY + thumbH, FwmTheme.ACCENT);
    }

    private int thumbH() {
        return Math.max(12, this.listH * this.rowsVisible / Math.max(1, this.items().size()));
    }

    private int thumbY(int thumbH) {
        return this.listY + (this.listH - thumbH) * this.scroll / Math.max(1, this.maxScroll());
    }

    private boolean overBar(double mouseX, double mouseY) {
        return this.maxScroll() > 0 && mouseX >= (double)(this.x + this.width - 4 - 3) && mouseX < (double)(this.x + this.width) && mouseY >= (double)this.listY && mouseY < (double)(this.listY + this.listH);
    }

    private void dragTo(double mouseY) {
        int thumbH = this.thumbH();
        int range = Math.max(1, this.listH - thumbH);
        double pos = Math.max(0.0, Math.min((double)range, mouseY - (double)this.grabOffset - (double)this.listY));
        this.scroll = (int)Math.round(pos / (double)range * (double)this.maxScroll());
        this.prefetch();
    }

    private void prefetch() {
        if (Market.hasMore() && this.scroll + this.rowsVisible + 4 >= this.items().size()) {
            Market.loadMore();
        }
    }

    private void renderStatus(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY) {
        int barY = this.y + this.height - 13;
        FwmTheme.divider(ctx, this.x, barY - 1, this.width);
        Objects.requireNonNull(font);
        int textY = barY + (13 - 9) / 2 + 1;
        MutableComponent left = Lang.of("fwm.market.count", this.items().size(), Market.totalItems());
        ctx.text(font, (Component)left, this.x + 4, textY, FwmTheme.TEXT_DIM, false);
        Component right = this.statusRight();
        ctx.text(font, right, this.refreshX() - 4 - font.width((FormattedText)right), textY, Market.state() == Market.State.ERROR ? -2056032 : FwmTheme.TEXT_DIM, false);
        boolean hot = this.overRefresh(mouseX, mouseY) && !Market.busy();
        int by = this.refreshY();
        FwmTheme.box(ctx, this.refreshX(), by, 11, 11, FwmTheme.RAISED, hot ? FwmTheme.ACCENT : FwmTheme.OFF);
        SharpIcons.draw(ctx, "icon_refresh", this.refreshX() + 1, by + 1, 9, Market.busy() ? FwmTheme.TEXT_DIM : (hot ? FwmTheme.ACCENT : FwmTheme.TEXT));
    }

    private int refreshX() {
        return this.x + this.width - 11 - 1;
    }

    private int refreshY() {
        return this.y + this.height - 13 + 1;
    }

    private boolean overRefresh(double mouseX, double mouseY) {
        return mouseX >= (double)this.refreshX() && mouseX < (double)(this.refreshX() + 11) && mouseY >= (double)this.refreshY() && mouseY < (double)(this.refreshY() + 11);
    }

    private Component statusRight() {
        if (Market.busy()) {
            return Lang.of("fwm.market.loading", new Object[0]);
        }
        if (Market.state() == Market.State.ERROR) {
            return Lang.of("fwm.market.offline", new Object[0]);
        }
        long age = Market.ageMinutes();
        return age < 0L ? Lang.of("fwm.market.loading", new Object[0]) : Lang.of("fwm.board.updated", age);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !this.contains(mouseX, mouseY)) {
            return false;
        }
        if (mouseY < (double)(this.y + 15)) {
            this.search.setFocused(true);
            return true;
        }
        this.search.setFocused(false);
        if (this.overRefresh(mouseX, mouseY)) {
            Market.refresh();
            return true;
        }
        if (mouseX >= (double)this.watchChip[0] && mouseX < (double)(this.watchChip[0] + this.watchChip[2]) && mouseY >= (double)this.watchChip[1] && mouseY < (double)(this.watchChip[1] + this.watchChip[3])) {
            V2 c = V2.INSTANCE;
            c.marketWatchOnly = !c.marketWatchOnly;
            V2.save();
            this.scroll = 0;
            return true;
        }
        if (this.overBar(mouseX, mouseY)) {
            int thumbH = this.thumbH();
            int thumbY = this.thumbY(thumbH);
            this.grabOffset = mouseY >= (double)thumbY && mouseY < (double)(thumbY + thumbH) ? (int)(mouseY - (double)thumbY) : thumbH / 2;
            this.draggingBar = true;
            this.dragTo(mouseY);
            return true;
        }
        if (mouseY >= (double)this.listY && mouseY < (double)(this.listY + this.listH)) {
            int index = this.scroll + (int)((mouseY - (double)this.listY) / 18.0);
            List<MarketData.Item> all = this.items();
            if (index >= 0 && index < all.size()) {
                MarketData.Item picked = all.get(index);
                if (mouseX < (double)(this.x + 11)) {
                    MarketPanel.toggleWatch(picked);
                    return true;
                }
                if (this.pickForPing) {
                    this.host.closeSub();
                    this.host.openSub(new PricePingPanel(picked, picked.displayName == null ? picked.itemKey : picked.displayName, this.host));
                } else {
                    this.host.openSub(new MarketItemPanel(picked, this.host));
                }
            }
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double vertical) {
        this.scroll = Math.max(0, Math.min(this.maxScroll(), this.scroll - (int)Math.signum(vertical) * 3));
        this.prefetch();
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        if (!this.draggingBar) {
            return false;
        }
        this.dragTo(mouseY);
        return true;
    }

    @Override
    public void mouseReleased() {
        this.draggingBar = false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.search.isFocused() && this.search.keyPressed(new KeyEvent(keyCode, scanCode, modifiers));
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        return this.search.isFocused() && this.search.charTyped(input);
    }

    @Override
    public void onHidden() {
        if (this.search != null) {
            this.host.detach((AbstractWidget)this.search);
        }
    }
}

