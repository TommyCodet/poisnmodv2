/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.util.FormattedCharSequence
 *  net.minecraft.util.Util
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  org.joml.Matrix3x2fStack
 */
package net.friendswithmoney.fwm.ui.panel;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import net.friendswithmoney.fwm.antileak.AntiLeakPack;
import net.friendswithmoney.fwm.config.ConfigManager;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.textures.BuiltinPacks;
import net.friendswithmoney.fwm.textures.FwmStyles;
import net.friendswithmoney.fwm.textures.SkyStudio;
import net.friendswithmoney.fwm.textures.TextureMixer;
import net.friendswithmoney.fwm.ui.ConfirmBox;
import net.friendswithmoney.fwm.ui.FwmGfx;
import net.friendswithmoney.fwm.ui.FwmTheme;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.LauncherGfx;
import net.friendswithmoney.fwm.ui.gfx.FwmFont;
import net.friendswithmoney.fwm.ui.gfx.Shapes;
import net.friendswithmoney.fwm.ui.panel.Panel;
import net.friendswithmoney.fwm.ui.panel.PanelHost;
import net.friendswithmoney.fwm.ui.panel.TotemStudioPanel;
import net.friendswithmoney.fwm.ui.widget.Anim;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.joml.Matrix3x2fStack;

public class TexturesPanel
extends Panel {
    private static final int LEFT_W = 158;
    private static final int RIGHT_W = 186;
    private static final int ITEM_H = 20;
    private static final int HEAD_H = 15;
    private static final int BTN_H = 14;
    private static final int GAP = 6;
    private static final int GOLD = -673493;
    private static final int MINT = -8523578;
    private static final String IMPORT_PREFIX = "FWM Import ";
    private final PanelHost host;
    private final List<Hit> hits = new ArrayList<Hit>();
    private final ConfirmBox confirm = new ConfirmBox();
    private List<String> packs = List.of();
    private EditBox search;
    private EditBox themeName;
    private boolean themesTab;
    private Filter filter = Filter.ALL;
    private String category = "crystal";
    private int listScroll;
    private int gridScroll;
    private int themeScroll;
    private FwmConfig.TextureTheme renaming;
    private String hovered;
    private String note = "";
    private long noteAt;
    private static final int DIM = -7362144;
    private static final int WHITE = -1116943;
    private static final Map<String, Item> ICON = Map.ofEntries(Map.entry("crystal", Items.END_CRYSTAL), Map.entry("obsidian", Items.OBSIDIAN), Map.entry("anchor", Items.RESPAWN_ANCHOR), Map.entry("glowstone", Items.GLOWSTONE), Map.entry("mace", Items.MACE), Map.entry("windcharge", Items.WIND_CHARGE), Map.entry("breezerod", Items.BREEZE_ROD), Map.entry("swords", Items.NETHERITE_SWORD), Map.entry("axes", Items.NETHERITE_AXE), Map.entry("spears", Items.NETHERITE_SPEAR), Map.entry("pickaxes", Items.NETHERITE_PICKAXE), Map.entry("shovels", Items.NETHERITE_SHOVEL), Map.entry("helmets", Items.NETHERITE_HELMET), Map.entry("chestplates", Items.NETHERITE_CHESTPLATE), Map.entry("leggings", Items.NETHERITE_LEGGINGS), Map.entry("boots", Items.NETHERITE_BOOTS), Map.entry("elytra", Items.ELYTRA), Map.entry("bows", Items.BOW), Map.entry("arrows", Items.ARROW), Map.entry("crossbow", Items.CROSSBOW), Map.entry("trident", Items.TRIDENT), Map.entry("totem", Items.TOTEM_OF_UNDYING), Map.entry("apples", Items.GOLDEN_APPLE), Map.entry("opapples", Items.ENCHANTED_GOLDEN_APPLE), Map.entry("pearls", Items.ENDER_PEARL), Map.entry("potions", Items.SPLASH_POTION), Map.entry("xp", Items.EXPERIENCE_BOTTLE), Map.entry("cobweb", Items.COBWEB), Map.entry("sky", Items.SUNFLOWER), Map.entry("fire", Items.FIRE_CHARGE), Map.entry("tnt", Items.TNT), Map.entry("inventory", Items.CHEST), Map.entry("hud", Items.CLOCK), Map.entry("particles", Items.FIREWORK_ROCKET), Map.entry("effects", Items.ENCHANTED_BOOK));
    private final Map<String, ItemStack> iconStacks = new HashMap<String, ItemStack>();
    private boolean showBefore;
    private boolean dragging;
    private double dragFromX;
    private float dragBase;
    private float dragFrames;
    private long draggedAt;
    private static final String[] SHOWROOM = new String[]{"crystal", "anchor", "glowstone", "obsidian", "mace", "windcharge", "spears", "swords", "axes", "totem", "chestplates", "leggings", "apples", "pearls", "potions", "bows", "xp", "cobweb"};
    private final Map<String, ItemStack> showroomStacks = new HashMap<String, ItemStack>();

    public TexturesPanel(PanelHost host) {
        this.host = host;
    }

    @Override
    public Component title() {
        return Lang.of("fwm.tab.textures", new Object[0]);
    }

    @Override
    protected void onResize() {
        String beforeName;
        this.packs = TextureMixer.packs();
        for (String pack : this.packs) {
            TextureMixer.content(pack);
        }
        Font font = FwmFont.ui();
        String before = this.search == null ? "" : this.search.getValue();
        String string = beforeName = this.themeName == null ? "" : this.themeName.getValue();
        if (this.search != null) {
            this.host.detach((AbstractWidget)this.search);
        }
        if (this.themeName != null) {
            this.host.detach((AbstractWidget)this.themeName);
        }
        int[] sb = this.searchBox();
        this.search = this.field(font, sb[0] + 16, sb[1] + 3, sb[2] - 22, 40, Lang.t("Item oder Pack suchen \u2026", "Search item or pack \u2026"));
        this.search.setValue(before);
        this.themeName = this.field(font, this.midX() + 4, this.y + 26, this.midW() - 70, 40, Lang.t("Name des Mixes", "Mix name"));
        this.themeName.setValue(beforeName);
        this.showField();
    }

    private EditBox field(Font font, int fx, int fy, int w, int max, String hint) {
        EditBox f = new EditBox(font, fx, fy, Math.max(20, w), 10, (Component)Component.literal((String)hint));
        f.setBordered(false);
        f.setMaxLength(max);
        f.setTextColor(FwmTheme.TEXT);
        f.setHint((Component)Component.literal((String)hint));
        return f;
    }

    private void showField() {
        this.host.detach((AbstractWidget)this.search);
        this.host.detach((AbstractWidget)this.themeName);
        this.host.attach((AbstractWidget)(this.themesTab ? this.themeName : this.search));
    }

    private int midX() {
        return this.x + 158 + 10;
    }

    private int midW() {
        return this.width - 158 - 10 - (this.showRight() ? 196 : 0);
    }

    private boolean showRight() {
        return this.width >= 564;
    }

    private int rightX() {
        return this.x + this.width - 186;
    }

    private int footY() {
        return this.y + this.height - 16;
    }

    private int gridY() {
        return this.y + 44;
    }

    private int cols() {
        return Math.max(2, Math.min(6, this.midW() / 60));
    }

    private int tileW() {
        return Math.max(20, (this.midW() - (this.cols() - 1) * 6) / this.cols());
    }

    private int tileH() {
        return Math.round((float)this.tileW() * 0.78f) + 12;
    }

    private List<TextureMixer.Category> categories() {
        ArrayList<TextureMixer.Category> out = new ArrayList<TextureMixer.Category>();
        for (String id : TextureMixer.DISPLAY_ORDER()) {
            TextureMixer.Category c = TextureMixer.category(id);
            if (c == null || !c.id().equals(id)) continue;
            out.add(c);
        }
        return out;
    }

    private List<String> styles() {
        String query = this.search == null ? "" : this.search.getValue().trim().toLowerCase(Locale.ROOT);
        ArrayList<String> out = new ArrayList<String>();
        if (this.filter == Filter.ALL && query.isEmpty()) {
            out.add("");
        }
        if (this.filter == Filter.ALL || this.filter == Filter.FWM || this.filter == Filter.FAVS) {
            for (FwmStyles.Style s : FwmStyles.forCategory(this.category)) {
                if (this.filter == Filter.FAVS && !TextureMixer.favourite(s.key()) || !query.isEmpty() && !s.label().toLowerCase(Locale.ROOT).contains(query)) continue;
                out.add(s.key());
            }
        }
        ArrayList<String> own = new ArrayList<String>();
        for (String pack : this.packs) {
            Map<String, String> c;
            boolean builtin = BuiltinPacks.is(pack);
            if (this.filter == Filter.FWM && !builtin || this.filter == Filter.OWN && builtin || (c = TextureMixer.content(pack)) == null || !c.containsKey(this.category) || this.filter == Filter.FAVS && !TextureMixer.favourite(pack) || !query.isEmpty() && !TexturesPanel.name(pack).toLowerCase(Locale.ROOT).contains(query)) continue;
            own.add(pack);
        }
        own.sort((a, b) -> Boolean.compare(TextureMixer.favourite(b), TextureMixer.favourite(a)));
        out.addAll(own);
        return out;
    }

    private static String name(String choice) {
        if (choice == null || choice.isEmpty()) {
            return "Vanilla";
        }
        FwmStyles.Style s = FwmStyles.byKey(choice);
        if (s != null) {
            return s.label();
        }
        if (BuiltinPacks.is(choice)) {
            return BuiltinPacks.label(choice);
        }
        String p = choice.toLowerCase(Locale.ROOT).endsWith(".zip") ? choice.substring(0, choice.length() - 4) : choice;
        return p.replaceAll("\u00a7.", "");
    }

    private static String origin(String choice) {
        if (choice == null || choice.isEmpty()) {
            return Lang.t("Minecraft", "Minecraft");
        }
        if (FwmStyles.isStyle(choice)) {
            return Lang.t("von FWM \u00b7 eingebaut", "by FWM \u00b7 built in");
        }
        if (BuiltinPacks.is(choice)) {
            return Lang.t("FWM Pack", "FWM pack");
        }
        return Lang.t("eigenes Pack \u00b7 lokal", "own pack \u00b7 local");
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY, float delta) {
        this.hits.clear();
        this.hovered = null;
        TextureMixer.previewBudget(3);
        int mx = this.confirm.open() ? -1000 : mouseX;
        int my = this.confirm.open() ? -1000 : mouseY;
        int half = 78;
        Shapes.round(ctx, this.x, this.y - 1, 158.0f, 16.0f, 6.0f, 0x10FFFFFF);
        Shapes.ring(ctx, this.x, this.y - 1, 158.0f, 16.0f, 6.0f, 0x18FFFFFF);
        this.tab(ctx, font, this.x, this.y, half, "Styles", !this.themesTab, mx, my, () -> this.setTab(false));
        this.tab(ctx, font, this.x + half + 2, this.y, 158 - half - 2, Lang.t("Mixe", "Mixes"), this.themesTab, mx, my, () -> this.setTab(true));
        Shapes.round(ctx, (float)(this.x + 158) + 2.5f, this.y, 0.6f, this.footY() - 4 - this.y, 0.3f, 0x18FFFFFF);
        if (this.themesTab) {
            this.themes(ctx, font, mx, my);
        } else {
            this.categoryList(ctx, font, mx, my);
            this.grid(ctx, font, mx, my);
            if (this.showRight()) {
                this.details(ctx, font, mx, my);
            }
        }
        this.footer(ctx, font, mx, my);
        this.confirm.render(ctx, font, this.x, this.y, this.width, this.height, mouseX, mouseY);
    }

    private void setTab(boolean themes) {
        this.themesTab = themes;
        this.showField();
    }

    private void tab(GuiGraphicsExtractor ctx, Font font, int bx, int by, int bw, String label, boolean on, int mouseX, int mouseY, Runnable action) {
        boolean over = TexturesPanel.in(mouseX, mouseY, bx, by, bw, 14);
        float h = Anim.toward("txtab" + label, on, 120);
        if (over && !on) {
            Shapes.round(ctx, (float)bx + 1.5f, (float)by + 0.5f, bw - 3, 13.0f, 5.0f, 0x12FFFFFF);
        }
        if (h > 0.0f) {
            Shapes.round(ctx, (float)bx + 1.5f, (float)by + 0.5f, bw - 3, 13.0f, 5.0f, Shapes.mix(8253638, -8523578, h));
        }
        MutableComponent t = on ? Component.literal((String)font.plainSubstrByWidth(label, bw - 4)).withStyle(ChatFormatting.BOLD) : Component.literal((String)font.plainSubstrByWidth(label, bw - 4));
        int n = bx + (bw - font.width((FormattedText)t)) / 2;
        Objects.requireNonNull(font);
        ctx.text(font, (Component)t, n, by + (14 - 9) / 2 + 1, on ? -16045536 : (over ? FwmTheme.TEXT : FwmTheme.TEXT_DIM), false);
        this.hits.add(new Hit(bx, by, bw, 14, 0, action));
    }

    private static void r(GuiGraphicsExtractor ctx, float x, float y, float w, float h, float rad, int c) {
        Shapes.round(ctx, x, y, w, h, rad, c);
    }

    private static void ring(GuiGraphicsExtractor ctx, float x, float y, float w, float h, float rad, int c) {
        Shapes.ring(ctx, x, y, w, h, rad, c);
    }

    private static void t(GuiGraphicsExtractor ctx, String s, float x, float y, int c, float size, boolean bold) {
        LauncherGfx.text(ctx, s, x, y, c, size, bold);
    }

    private static float tw(String s, float size, boolean bold) {
        return LauncherGfx.width(s, size, bold);
    }

    private static int mix(int a, int b, float f) {
        return Shapes.mix(a, b, f);
    }

    private ItemStack icon(String category) {
        return this.iconStacks.computeIfAbsent(category, c -> new ItemStack((ItemLike)ICON.getOrDefault(c, Items.PAPER)));
    }

    private static String groupLabel(String group) {
        for (String[] g : TextureMixer.GROUPS) {
            if (!g[0].equals(group)) continue;
            return Lang.t(g[1], g[2]);
        }
        return group;
    }

    private static float pill(GuiGraphicsExtractor ctx, float px, float py, String label, int fill, int text, float size) {
        float w = TexturesPanel.tw(label, size, true) + 8.0f;
        TexturesPanel.r(ctx, px, py, w, 11.0f, 5.5f, fill);
        TexturesPanel.t(ctx, label, px + 4.0f, py + 5.5f - 3.5f, text, size, true);
        return w;
    }

    private void categoryList(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY) {
        List<TextureMixer.Category> cats = this.categories();
        if (!cats.isEmpty() && cats.stream().noneMatch(c -> c.id().equals(this.category))) {
            this.category = cats.get(0).id();
            this.gridScroll = 0;
        }
        ArrayList<Object> rows = new ArrayList<Object>();
        String group = null;
        for (TextureMixer.Category c2 : cats) {
            if (!c2.group().equals(group)) {
                group = c2.group();
                rows.add(group);
            }
            rows.add(c2);
        }
        int top = this.y + 14 + 6;
        int bottom = this.footY() - 18;
        int fit = 0;
        int h = 0;
        for (int i = rows.size() - 1; i >= 0 && (h += rows.get(i) instanceof String ? 15 : 20) <= bottom - top; --i) {
            ++fit;
        }
        this.listScroll = Math.max(0, Math.min(this.listScroll, rows.size() - fit));
        int ry = top;
        int last = this.listScroll;
        for (int i = this.listScroll; i < rows.size(); ++i) {
            Object row = rows.get(i);
            if (row instanceof String) {
                String g = (String)row;
                if (ry + 15 > bottom) break;
                int n = 0;
                for (TextureMixer.Category c3 : cats) {
                    if (!c3.group().equals(g)) continue;
                    ++n;
                }
                TexturesPanel.t(ctx, TexturesPanel.groupLabel(g).toUpperCase(Locale.ROOT), this.x + 4, (float)(ry + 8) - 3.5f, -8523578, 6.2f, true);
                String count = String.valueOf(n);
                TexturesPanel.t(ctx, count, (float)(this.x + 158 - 4) - TexturesPanel.tw(count, 6.2f, false), (float)(ry + 8) - 3.5f, -7362144, 6.2f, false);
                ry += 15;
                last = i;
                continue;
            }
            if (ry + 20 > bottom) break;
            last = i;
            TextureMixer.Category c4 = (TextureMixer.Category)row;
            boolean on = c4.id().equals(this.category);
            boolean over = TexturesPanel.in(mouseX, mouseY, this.x, ry, 158, 20);
            float hv = Anim.toward("txcat" + c4.id(), on || over, 110);
            if (hv > 0.0f) {
                TexturesPanel.r(ctx, this.x, (float)ry + 0.5f, 158.0f, 19.0f, 5.0f, TexturesPanel.mix(0xFFFFFF, on ? 0x1EFFFFFF : 0x10FFFFFF, hv));
                TexturesPanel.ring(ctx, this.x, (float)ry + 0.5f, 158.0f, 19.0f, 5.0f, on ? TexturesPanel.mix(8253638, -1719799610, hv) : TexturesPanel.mix(0xFFFFFF, 0x1AFFFFFF, hv));
            }
            ctx.item(this.icon(c4.id()), this.x + 5, ry + 2);
            String choice = TextureMixer.chosen(c4.id());
            boolean hasChoice = !choice.isEmpty();
            boolean elementOn = TextureMixer.active(c4.id());
            String label = hasChoice ? LauncherGfx.fit(TexturesPanel.name(choice), 6.0f, true, 58.0f) + (elementOn ? "" : Lang.t(" \u00b7 aus", " \u00b7 off")) : "Vanilla";
            float pw = TexturesPanel.tw(label, 6.0f, true) + 8.0f;
            float px = (float)(this.x + 158 - 5) - pw;
            TexturesPanel.pill(ctx, px, (float)ry + 10.0f - 5.5f, label, hasChoice && elementOn ? -8523578 : 0x16FFFFFF, hasChoice && elementOn ? -16045536 : -7362144, 6.0f);
            if (hasChoice) {
                String cid = c4.id();
                this.hits.add(new Hit((int)px, ry, (int)pw, 20, 0, () -> TextureMixer.setActive(cid, !TextureMixer.active(cid))));
            }
            if (TextureMixer.pending(c4.id())) {
                TexturesPanel.r(ctx, px - 6.0f, (float)ry + 10.0f - 1.5f, 3.0f, 3.0f, 1.5f, -673493);
            }
            TexturesPanel.t(ctx, LauncherGfx.fit(c4.label(), 7.8f, true, px - (float)this.x - 34.0f), this.x + 26, (float)ry + 10.0f - 3.5f, on ? -1116943 : -2694435, 7.8f, true);
            String id = c4.id();
            this.hits.add(new Hit(this.x, ry, 158, 20, 0, () -> {
                this.category = id;
                this.gridScroll = 0;
            }));
            ry += 20;
        }
        if (last < rows.size() - 1) {
            ArrayList<String> more = new ArrayList<String>();
            for (int i = last + 1; i < rows.size(); ++i) {
                Object e = rows.get(i);
                if (!(e instanceof String)) continue;
                String g = (String)e;
                more.add(TexturesPanel.groupLabel(g));
            }
            if (more.isEmpty()) {
                more.add(Lang.t("weiter unten", "further down"));
            }
            TexturesPanel.t(ctx, LauncherGfx.fit("\u2193 " + String.join((CharSequence)" \u00b7 ", more), 6.2f, false, 150.0f), this.x + 4, (float)(bottom + 9) - 3.5f, -7362144, 6.2f, false);
        }
    }

    private int[] searchBox() {
        return new int[]{this.midX() + this.midW() - 118, this.y - 1, 118, 14};
    }

    private void grid(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY) {
        TextureMixer.Category cat = TextureMixer.category(this.category);
        ArrayList<String> styles = new ArrayList<String>(this.styles());
        styles.removeIf(sc -> TextureMixer.noPicture(sc, cat));
        String chosen = TextureMixer.chosen(this.category);
        TexturesPanel.pill(ctx, this.midX(), this.y + 1, TexturesPanel.groupLabel(cat.group()), 863891654, -8523578, 6.0f);
        int[] sb = this.searchBox();
        TexturesPanel.r(ctx, sb[0], sb[1], sb[2], sb[3], 7.0f, this.search.isFocused() ? 0x1CFFFFFF : 0x12FFFFFF);
        TexturesPanel.ring(ctx, sb[0], sb[1], sb[2], sb[3], 7.0f, this.search.isFocused() ? -1434586938 : 0x1FFFFFFF);
        Shapes.icon(ctx, "search", sb[0] + 5, sb[1] + 3, 8.0f, -7362144);
        this.search.extractWidgetRenderState(ctx, mouseX, mouseY, 0.0f);
        TexturesPanel.t(ctx, LauncherGfx.fit(cat.label(), 13.0f, true, this.midW() - 150), this.midX(), (float)(this.y + 22) - 3.5f, -1116943, 13.0f, true);
        String sub = styles.size() + Lang.t(" Styles \u00b7 nur Packs mit eigenem ", " styles \u00b7 only packs with their own ") + cat.label();
        TexturesPanel.t(ctx, LauncherGfx.fit(sub, 6.6f, false, this.midW() - 150), this.midX(), (float)(this.y + 33) - 3.5f, -7362144, 6.6f, false);
        String[] labels = new String[]{Lang.t("Alle", "All"), "FWM", "\u2605", Lang.t("Eigene", "Own")};
        Filter[] which = new Filter[]{Filter.ALL, Filter.FWM, Filter.FAVS, Filter.OWN};
        float segW = 0.0f;
        for (String l : labels) {
            segW += TexturesPanel.tw(l, 6.8f, true) + 12.0f;
        }
        float fx = (float)(this.midX() + this.midW()) - segW - 4.0f;
        float fy = this.y + 16;
        TexturesPanel.r(ctx, fx - 2.0f, fy - 1.0f, segW + 4.0f, 15.0f, 6.0f, 0x12FFFFFF);
        for (int i = 0; i < labels.length; ++i) {
            float w = TexturesPanel.tw(labels[i], 6.8f, true) + 12.0f;
            boolean on = this.filter == which[i];
            boolean over = TexturesPanel.in(mouseX, mouseY, (int)fx, (int)fy, (int)w, 13);
            if (on) {
                TexturesPanel.r(ctx, fx, fy, w, 13.0f, 5.0f, 0x30FFFFFF);
            } else if (over) {
                TexturesPanel.r(ctx, fx, fy, w, 13.0f, 5.0f, 0x14FFFFFF);
            }
            TexturesPanel.t(ctx, labels[i], fx + 6.0f, fy + 6.5f - 3.5f, on ? -1116943 : -7362144, 6.8f, true);
            Filter f = which[i];
            this.hits.add(new Hit((int)fx, (int)fy, (int)w, 13, 0, () -> {
                this.filter = f;
                this.gridScroll = 0;
            }));
            fx += w;
        }
        int cols = 4;
        int tileW = (this.midW() - (cols - 1) * 6) / cols;
        int rowsVisible = 3;
        int tileH = Math.max(40, (this.footY() - 10 - this.gridY() - (rowsVisible - 1) * 6) / rowsVisible);
        int count = styles.size() + 1;
        int totalRows = (count + cols - 1) / cols;
        this.gridScroll = Math.max(0, Math.min(this.gridScroll, totalRows - rowsVisible));
        for (int i = this.gridScroll * cols; i < count && i < (this.gridScroll + rowsVisible) * cols; ++i) {
            int slot = i - this.gridScroll * cols;
            int tx = this.midX() + slot % cols * (tileW + 6);
            int ty = this.gridY() + slot / cols * (tileH + 6);
            if (i == styles.size()) {
                this.dropTile(ctx, tx, ty, tileW, tileH, mouseX, mouseY);
                continue;
            }
            this.styleTile(ctx, (String)styles.get(i), cat, chosen, tx, ty, tileW, tileH, mouseX, mouseY);
        }
    }

    private void styleTile(GuiGraphicsExtractor ctx, String choice, TextureMixer.Category cat, String chosen, int tx, int ty, int tw, int th, int mouseX, int mouseY) {
        boolean builtin;
        TextureMixer.Preview preview;
        boolean on = choice.equals(chosen);
        boolean over = TexturesPanel.in(mouseX, mouseY, tx, ty, tw, th);
        if (over) {
            this.hovered = choice;
        }
        float hv = Anim.toward("txtile" + choice, over, 110);
        TexturesPanel.r(ctx, tx, ty, tw, th, 7.0f, TexturesPanel.mix(0x10FFFFFF, 0x1CFFFFFF, hv));
        if (on) {
            Shapes.ring(ctx, tx, ty, tw, th, 7.0f, -8523578, 0.9f);
        } else {
            TexturesPanel.ring(ctx, tx, ty, tw, th, 7.0f, TexturesPanel.mix(0x14FFFFFF, 1719529670, hv));
        }
        int sceneH = th - 24;
        LauncherGfx.roundGradient(ctx, tx + 3, ty + 3, tw - 6, sceneH, 5, 1075325478, 1074204429);
        TextureMixer.Preview preview2 = preview = choice.isEmpty() ? TextureMixer.vanillaPreview(cat) : TextureMixer.preview(choice, cat);
        if (preview != null) {
            TexturesPanel.fit(ctx, preview, tx + 8, ty + 6, tw - 16, sceneH - 6);
        }
        boolean bl = builtin = FwmStyles.isStyle(choice) || BuiltinPacks.is(choice);
        if (builtin) {
            TexturesPanel.pill(ctx, tx + 5, ty + 5, "FWM", 1089845547, -673493, 5.4f);
        } else if (!choice.isEmpty()) {
            TexturesPanel.pill(ctx, tx + 5, ty + 5, Lang.t("EIGENE", "OWN"), 1081995462, -8523578, 5.4f);
        }
        TexturesPanel.t(ctx, LauncherGfx.fit(TexturesPanel.name(choice), 7.6f, true, tw - 22), tx + 6, (float)(ty + th - 15) - 3.5f, on ? -1116943 : -2694435, 7.6f, true);
        String from = choice.isEmpty() ? "Minecraft" : (builtin ? Lang.t("FWM-Pack", "FWM pack") : Lang.t("Eigene", "Own"));
        TexturesPanel.t(ctx, from, tx + 6, (float)(ty + th - 7) - 3.5f, -7362144, 5.8f, false);
        if (on) {
            Shapes.circle(ctx, tx + tw - 10, ty + th - 11, 5.0f, -8523578);
            Shapes.icon(ctx, "check", tx + tw - 14, ty + th - 15, 8.0f, -16045536);
        } else if (!choice.isEmpty()) {
            boolean fav = TextureMixer.favourite(choice);
            int starX = tx + tw - 14;
            int starY = ty + th - 15;
            boolean overStar = TexturesPanel.in(mouseX, mouseY, starX - 1, starY - 1, 11, 11);
            if (fav || over) {
                TexturesPanel.t(ctx, fav ? "\u2605" : "\u2606", starX, (float)(starY + 4) - 3.5f, fav ? -673493 : (overStar ? -8523578 : -7362144), 7.0f, false);
            }
            this.hits.add(new Hit(starX - 1, starY - 1, 11, 11, 0, () -> TextureMixer.toggleFavourite(choice)));
        }
        if (choice.startsWith(IMPORT_PREFIX) || choice.startsWith("FWM Himmel ")) {
            this.hits.add(new Hit(tx, ty, tw, th, 1, () -> this.askDelete(choice)));
        }
        String id = this.category;
        this.hits.add(new Hit(tx, ty, tw, th, 0, () -> TextureMixer.choose(id, choice)));
    }

    private void dropTile(GuiGraphicsExtractor ctx, int tx, int ty, int tw, int th, int mouseX, int mouseY) {
        float i;
        boolean over = TexturesPanel.in(mouseX, mouseY, tx, ty, tw, th);
        float hv = Anim.toward("txdrop", over, 110);
        TexturesPanel.r(ctx, tx, ty, tw, th, 7.0f, TexturesPanel.mix(0x6FFFFFF, 0x12FFFFFF, hv));
        for (i = (float)(tx + 7); i < (float)(tx + tw - 7); i += 4.0f) {
            TexturesPanel.r(ctx, i, ty, Math.min(2.0f, (float)(tx + tw - 7) - i), 0.6f, 0.3f, 863891654);
            TexturesPanel.r(ctx, i, (float)(ty + th) - 0.6f, Math.min(2.0f, (float)(tx + tw - 7) - i), 0.6f, 0.3f, 863891654);
        }
        for (i = (float)(ty + 7); i < (float)(ty + th - 7); i += 4.0f) {
            TexturesPanel.r(ctx, tx, i, 0.6f, Math.min(2.0f, (float)(ty + th - 7) - i), 0.3f, 863891654);
            TexturesPanel.r(ctx, (float)(tx + tw) - 0.6f, i, 0.6f, Math.min(2.0f, (float)(ty + th - 7) - i), 0.3f, 863891654);
        }
        float cy = (float)ty + (float)th / 2.0f - 10.0f;
        TexturesPanel.r(ctx, (float)tx + (float)tw / 2.0f - 7.0f, cy - 7.0f, 14.0f, 14.0f, 4.0f, 780005574);
        Shapes.icon(ctx, "plus", (float)tx + (float)tw / 2.0f - 4.5f, cy - 4.5f, 9.0f, -8523578);
        String a = Lang.t("PNG reinziehen", "Drop a PNG");
        String b = Lang.t("oder klicken", "or click");
        TexturesPanel.t(ctx, a, (float)tx + ((float)tw - TexturesPanel.tw(a, 7.0f, true)) / 2.0f, cy + 14.0f - 3.5f, -1116943, 7.0f, true);
        TexturesPanel.t(ctx, b, (float)tx + ((float)tw - TexturesPanel.tw(b, 6.0f, false)) / 2.0f, cy + 24.0f - 3.5f, -7362144, 6.0f, false);
        this.hits.add(new Hit(tx, ty, tw, th, 0, this::openFolder));
    }

    private int[] previewBox() {
        return new int[]{this.rightX(), this.y, 186, 118};
    }

    private void details(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY) {
        int cell;
        int per;
        int rowsN;
        boolean inGame;
        TextureMixer.Preview p;
        int rx = this.rightX();
        TextureMixer.Category cat = TextureMixer.category(this.category);
        String applied = V2.INSTANCE.textureApplied.getOrDefault(this.category, "");
        String after = this.hovered != null ? this.hovered : TextureMixer.chosen(this.category);
        String shown = this.showBefore ? applied : after;
        int[] pb = this.previewBox();
        TexturesPanel.showroomBackdrop(ctx, pb[0], pb[1], pb[2], pb[3]);
        TextureMixer.Preview preview = p = shown.isEmpty() ? TextureMixer.vanillaPreview(cat) : TextureMixer.preview(shown, cat);
        if (p != null) {
            TexturesPanel.fitFrame(ctx, p, rx + 14, this.y + 20, 158, pb[3] - 26, this.frameNow(p));
        }
        float sx = rx + 5;
        float sy = this.y + 5;
        String bL = Lang.t("Vorher", "Before");
        String aL = Lang.t("Nachher", "After");
        float bw = TexturesPanel.tw(bL, 6.6f, true) + 12.0f;
        float aw = TexturesPanel.tw(aL, 6.6f, true) + 12.0f;
        TexturesPanel.r(ctx, sx, sy, bw + aw + 4.0f, 15.0f, 6.0f, -1341119456);
        TexturesPanel.r(ctx, this.showBefore ? sx + 2.0f : sx + 2.0f + bw, sy + 2.0f, this.showBefore ? bw : aw, 11.0f, 5.0f, -8523578);
        TexturesPanel.t(ctx, bL, sx + 2.0f + 6.0f, sy + 7.5f - 3.5f, this.showBefore ? -16045536 : -7362144, 6.6f, true);
        TexturesPanel.t(ctx, aL, sx + 2.0f + bw + 6.0f, sy + 7.5f - 3.5f, this.showBefore ? -7362144 : -16045536, 6.6f, true);
        this.hits.add(new Hit((int)sx, (int)sy, (int)bw + 2, 15, 0, () -> {
            this.showBefore = true;
        }));
        this.hits.add(new Hit((int)(sx + 2.0f + bw), (int)sy, (int)aw + 2, 15, 0, () -> {
            this.showBefore = false;
        }));
        if (p != null && p.height() > p.frameHeight()) {
            String turn = "\u27f2 " + Lang.t("Ziehen zum Drehen", "Drag to turn");
            float tw = TexturesPanel.tw(turn, 6.0f, false) + 10.0f;
            TexturesPanel.r(ctx, (float)(rx + 186 - 5) - tw, this.y + pb[3] - 17, tw, 12.0f, 6.0f, -1341119456);
            TexturesPanel.t(ctx, turn, (float)(rx + 186 - 5) - tw + 5.0f, (float)(this.y + pb[3] - 11) - 3.5f, -2694435, 6.0f, false);
        }
        int ty = this.y + pb[3] + 6;
        TexturesPanel.t(ctx, LauncherGfx.fit(TexturesPanel.name(after), 9.0f, true, 120.0f), rx, (float)(ty + 4) - 3.5f, -1116943, 9.0f, true);
        boolean bl = inGame = after.equals(applied) && !TextureMixer.pending(this.category);
        String origin = (after.isEmpty() ? "Minecraft" : (FwmStyles.isStyle(after) || BuiltinPacks.is(after) ? Lang.t("FWM-Pack", "FWM pack") : Lang.t("Eigenes Pack", "Own pack"))) + (inGame ? Lang.t(" \u00b7 gerade im Spiel", " \u00b7 in game now") : Lang.t(" \u00b7 noch nicht angewendet", " \u00b7 not applied yet"));
        TexturesPanel.t(ctx, LauncherGfx.fit(origin, 6.4f, false, 120.0f), rx, (float)(ty + 14) - 3.5f, inGame ? -7362144 : -673493, 6.4f, false);
        boolean canApply = !TextureMixer.building() && TextureMixer.pending(this.category);
        float abw = 60.0f;
        float abx = (float)(rx + 186) - abw;
        boolean overOne = canApply && TexturesPanel.in(mouseX, mouseY, (int)abx, ty, (int)abw, 16);
        TexturesPanel.r(ctx, abx, ty, abw, 16.0f, 6.0f, canApply ? (overOne ? -6555690 : -8523578) : 0x1CFFFFFF);
        String one = Lang.t("Anwenden", "Apply");
        TexturesPanel.t(ctx, one, abx + (abw - TexturesPanel.tw(one, 7.2f, true)) / 2.0f, (float)(ty + 8) - 3.5f, canApply ? -16045536 : -7362144, 7.2f, true);
        if (canApply) {
            Minecraft client = Minecraft.getInstance();
            this.hits.add(new Hit((int)abx, ty, (int)abw, 16, 0, () -> TextureMixer.buildCategory(client, this.category)));
        }
        int ay = ty + 22;
        if (this.category.equals("totem")) {
            String label = "+ " + Lang.t("Eigenes Totem", "Own totem");
            boolean overT = TexturesPanel.in(mouseX, mouseY, rx, ay, 186, 16);
            TexturesPanel.r(ctx, rx, ay, 186.0f, 16.0f, 5.0f, overT ? 1149104326 : 612233414);
            TexturesPanel.ring(ctx, rx, ay, 186.0f, 16.0f, 5.0f, 1719529670);
            TexturesPanel.t(ctx, label, (float)rx + (186.0f - TexturesPanel.tw(label, 7.0f, true)) / 2.0f, (float)ay + 8.0f - 3.6f, -8523578, 7.0f, true);
            this.hits.add(new Hit(rx, ay, 186, 16, 0, () -> this.host.openSub(new TotemStudioPanel(this.host, true))));
            ay += 20;
        }
        if (ay + 12 + (rowsN = (SHOWROOM.length + (per = 6) - 1) / per) * (cell = 176 / per) + 6 > this.footY() - 8) {
            return;
        }
        TexturesPanel.t(ctx, Lang.t("IM SPIEL JETZT", "IN GAME NOW"), rx, (float)(ay + 4) - 3.5f, -7362144, 6.0f, true);
        String note = Lang.t("Vanilla oder Pack", "Vanilla or pack");
        TexturesPanel.t(ctx, note, (float)(rx + 186) - TexturesPanel.tw(note, 6.0f, false), (float)(ay + 4) - 3.5f, -7362144, 6.0f, false);
        int gy = ay + 11;
        TexturesPanel.r(ctx, rx, gy, 186.0f, rowsN * cell + 6, 6.0f, 0x24000000);
        TexturesPanel.ring(ctx, rx, gy, 186.0f, rowsN * cell + 6, 6.0f, 0x12FFFFFF);
        int gx = rx + (186 - per * cell) / 2;
        for (int i = 0; i < SHOWROOM.length; ++i) {
            int ix = gx + i % per * cell;
            int iy = gy + 3 + i / per * cell;
            TextureMixer.Category c = TextureMixer.category(SHOWROOM[i]);
            boolean inGroup = c != null && c.group().equals(cat.group());
            boolean self = SHOWROOM[i].equals(this.category);
            TexturesPanel.r(ctx, ix + 1, iy + 1, cell - 2, cell - 2, 4.0f, self ? 863891654 : 0x10FFFFFF);
            if (inGroup) {
                TexturesPanel.ring(ctx, ix + 1, iy + 1, cell - 2, cell - 2, 4.0f, self ? -8523578 : 1719529670);
            }
            float sc = Math.min(1.25f, (float)(cell - 6) / 16.0f);
            Matrix3x2fStack m = ctx.pose();
            m.pushMatrix();
            m.translate((float)ix + ((float)cell - 16.0f * sc) / 2.0f, (float)iy + ((float)cell - 16.0f * sc) / 2.0f);
            m.scale(sc, sc);
            ctx.item(this.showroomStack(SHOWROOM[i]), 0, 0);
            m.popMatrix();
            String target = SHOWROOM[i];
            this.hits.add(new Hit(ix, iy, cell, cell, 0, () -> {
                if (TextureMixer.offered(target)) {
                    this.category = target;
                    this.gridScroll = 0;
                }
            }));
        }
    }

    private int frameNow(TextureMixer.Preview p) {
        int frames = Math.max(1, p.height() / Math.max(1, p.frameHeight()));
        if (frames <= 1) {
            return 0;
        }
        if (this.dragging || System.currentTimeMillis() - this.draggedAt < 2500L) {
            int f = Math.round(this.dragBase + this.dragFrames) % frames;
            return f < 0 ? f + frames : f;
        }
        return (int)(System.currentTimeMillis() / 100L % (long)frames);
    }

    private static void fitFrame(GuiGraphicsExtractor ctx, TextureMixer.Preview p, int px, int py, int bw, int bh, int frame) {
        int h;
        int w = p.width();
        float scale = Math.min((float)bw / (float)w, (float)bh / (float)(h = p.frameHeight()));
        if (scale >= 2.0f) {
            scale = (float)Math.floor(scale);
        }
        int dw = Math.max(1, Math.round((float)w * scale));
        int dh = Math.max(1, Math.round((float)h * scale));
        FwmGfx.blitAt(ctx, p.id(), px + (bw - dw) / 2, py + (bh - dh) / 2, dw, dh, (float)frame * (float)h, w, h, w, p.height());
    }

    private static void showroomBackdrop(GuiGraphicsExtractor ctx, int bx, int by, int w, int h) {
        int floor = by + h * 72 / 100;
        LauncherGfx.roundGradient(ctx, bx, by, w, h, 7, -12613928, -6501387);
        TexturesPanel.r(ctx, bx + 10, (float)by + (float)h / 3.0f, (float)w / 2.0f - 10.0f, 2.5f, 1.2f, 0x66FFFFFF);
        TexturesPanel.r(ctx, (float)bx + (float)w / 2.0f + 14.0f, (float)by + (float)h / 4.0f, (float)w / 2.0f - 26.0f, 1.8f, 0.9f, 0x55FFFFFF);
        ctx.enableScissor(bx, floor, bx + w, by + h);
        LauncherGfx.roundGradient(ctx, bx, by, w, h, 7, -9585851, -12615896);
        ctx.disableScissor();
        TexturesPanel.ring(ctx, bx, by, w, h, 7.0f, 0x33FFFFFF);
    }

    private ItemStack showroomStack(String category) {
        return this.showroomStacks.computeIfAbsent(category, c -> {
            ItemStack s = new ItemStack((ItemLike)ICON.getOrDefault(c, Items.PAPER));
            if (c.equals("swords") || c.equals("chestplates") || c.equals("leggings")) {
                s.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, (Object)true);
            }
            return s;
        });
    }

    private static void fit(GuiGraphicsExtractor ctx, TextureMixer.Preview p, int px, int py, int bw, int bh) {
        int h;
        int w = p.width();
        float scale = Math.min((float)bw / (float)w, (float)bh / (float)(h = p.frameHeight()));
        if (scale >= 2.0f) {
            scale = (float)Math.floor(scale);
        }
        int dw = Math.max(1, Math.round((float)w * scale));
        int dh = Math.max(1, Math.round((float)h * scale));
        FwmGfx.blitAt(ctx, p.id(), px + (bw - dw) / 2, py + (bh - dh) / 2, dw, dh, TexturesPanel.frameV(p), w, h, w, p.height());
    }

    private static float frameV(TextureMixer.Preview p) {
        int frames = Math.max(1, p.height() / Math.max(1, p.frameHeight()));
        if (frames <= 1) {
            return 0.0f;
        }
        long index = System.currentTimeMillis() / 100L % (long)frames;
        return index * (long)p.frameHeight();
    }

    private static void picture(GuiGraphicsExtractor ctx, TextureMixer.Preview p, boolean tiled, int px, int py, int size) {
        int h;
        if (tiled) {
            int s = size / 3;
            for (int r = 0; r < 3; ++r) {
                for (int c = 0; c < 3; ++c) {
                    FwmGfx.blitAt(ctx, p.id(), px + c * s + (size - 3 * s) / 2, py + r * s + (size - 3 * s) / 2, s, s, TexturesPanel.frameV(p), p.width(), p.frameHeight(), p.width(), p.height());
                }
            }
            return;
        }
        int w = p.width();
        float scale = Math.min((float)size / (float)w, (float)size / (float)(h = p.frameHeight()));
        if (scale >= 1.0f) {
            scale = (float)Math.floor(scale);
        }
        int dw = Math.max(1, Math.round((float)w * scale));
        int dh = Math.max(1, Math.round((float)h * scale));
        FwmGfx.blitAt(ctx, p.id(), px + (size - dw) / 2, py + (size - dh) / 2, dw, dh, TexturesPanel.frameV(p), w, h, w, p.height());
    }

    private void themes(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY) {
        int ty = this.y + 14 + 6;
        for (String line : TexturesPanel.wrap(font, Lang.t("Ein Mix merkt sich die ganze Auswahl, z. B. \u201ePvP\u201c oder \u201eStream\u201c. Laden setzt sie, Anwenden macht sie aktiv.", "A mix keeps the whole choice, e.g. \"PvP\" or \"Stream\". Load sets it, Apply makes it real."), 154)) {
            ctx.text(font, (Component)Component.literal((String)line), this.x + 2, ty, FwmTheme.TEXT_DIM, false);
            ty += 10;
        }
        ctx.text(font, (Component)Component.literal((String)(this.renaming == null ? Lang.t("Aktuelle Auswahl speichern", "Save current choice") : Lang.t("Umbenennen: ", "Rename: ") + this.renaming.name)), this.midX(), this.y + 12, FwmTheme.TEXT_DIM, false);
        int fw = this.midW() - 66;
        FwmTheme.box(ctx, this.midX(), this.y + 23, fw, 15, FwmTheme.INSET, this.themeName.isFocused() ? FwmTheme.ACCENT : FwmTheme.EDGE);
        this.themeName.extractWidgetRenderState(ctx, mouseX, mouseY, 0.0f);
        this.button(ctx, font, this.midX() + fw + 4, this.y + 23, 62, this.renaming == null ? Lang.t("Speichern", "Save") : "OK", mouseX, mouseY, true, this::saveTheme);
        List<FwmConfig.TextureTheme> themes = FwmConfig.INSTANCE.textureThemes;
        int top = this.gridY();
        int rowH = 16;
        int visible = Math.max(1, (this.footY() - 6 - top) / rowH);
        int total = themes.size() + 1;
        this.themeScroll = Math.max(0, Math.min(this.themeScroll, total - visible));
        for (int i = 0; i < visible && this.themeScroll + i < total; ++i) {
            FwmConfig.TextureTheme theme;
            int idx = this.themeScroll + i;
            int ry = top + i * rowH;
            FwmConfig.TextureTheme textureTheme = theme = idx == 0 ? null : themes.get(idx - 1);
            if (TexturesPanel.in(mouseX, mouseY, this.midX(), ry, this.midW(), rowH)) {
                FwmTheme.hover(ctx, this.midX(), ry, this.midW(), rowH);
            }
            String nm = theme == null ? Lang.t("Alles Vanilla", "All vanilla") : theme.name;
            String info = theme == null ? Lang.t("eingebaut", "built in") : theme.mix.size() + Lang.t(" Kategorien", " categories");
            ctx.text(font, (Component)Component.literal((String)font.plainSubstrByWidth(nm, this.midW() - 200)), this.midX() + 3, ry + 4, FwmTheme.TEXT, false);
            ctx.text(font, (Component)Component.literal((String)info), this.midX() + this.midW() - 190, ry + 4, FwmTheme.TEXT_DIM, false);
            int bx = this.midX() + this.midW() - 120;
            this.button(ctx, font, bx, ry + 1, 58, Lang.t("Laden", "Load"), mouseX, mouseY, true, () -> TextureMixer.applyTheme(theme));
            if (theme == null) continue;
            this.button(ctx, font, bx + 60, ry + 1, 44, Lang.t("Name", "Name"), mouseX, mouseY, true, () -> {
                this.renaming = theme;
                this.themeName.setValue(theme.name);
                this.themeName.setFocused(true);
            });
            this.button(ctx, font, bx + 106, ry + 1, 14, "\u00d7", mouseX, mouseY, true, () -> this.confirm.ask((Component)Lang.of("fwm.textures.deleteMix", theme.name), () -> {
                FwmConfig.INSTANCE.textureThemes.remove(theme);
                if (this.renaming == theme) {
                    this.renaming = null;
                }
                ConfigManager.save();
            }));
        }
    }

    private static List<String> wrap(Font font, String text, int width) {
        ArrayList<String> out = new ArrayList<String>();
        for (FormattedCharSequence line : font.split((FormattedText)Component.literal((String)text), width)) {
            StringBuilder b = new StringBuilder();
            line.accept((index, style, cp) -> {
                b.appendCodePoint(cp);
                return true;
            });
            out.add(b.toString());
        }
        return out;
    }

    private void saveTheme() {
        String nm = this.themeName.getValue().trim();
        if (nm.isEmpty()) {
            return;
        }
        if (this.renaming != null) {
            FwmConfig.INSTANCE.textureThemes.removeIf(t -> t != this.renaming && nm.equalsIgnoreCase(t.name));
            this.renaming.name = nm;
            this.renaming = null;
            ConfigManager.save();
        } else {
            TextureMixer.saveTheme(nm);
        }
        this.themeName.setValue("");
        this.themeName.setFocused(false);
    }

    private void footer(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY) {
        Minecraft client = Minecraft.getInstance();
        int by = this.footY();
        TexturesPanel.r(ctx, this.x, by - 6, this.width, 0.6f, 0.3f, 0x18FFFFFF);
        float bx = this.x;
        bx += this.footButton(ctx, bx, by, Lang.t("+ Importieren", "+ Import"), mouseX, mouseY, this::openFolder) + 5.0f;
        bx += this.footButton(ctx, bx, by, Lang.t("Zur\u00fccksetzen", "Reset"), mouseX, mouseY, () -> this.confirm.ask((Component)Lang.of("fwm.textures.resetAsk", new Object[0]), "fwm.confirm.confirm", () -> TextureMixer.reset(client))) + 8.0f;
        boolean can = !TextureMixer.building() && TextureMixer.pending();
        String apply = TextureMixer.building() ? Lang.t("Wird angewendet \u2026", "Applying \u2026") : Lang.t("Alle anwenden  \u23ce", "Apply all  \u23ce");
        float aw = TexturesPanel.tw(apply, 7.4f, true) + 18.0f;
        float ax = (float)(this.x + this.width) - aw;
        boolean over = can && TexturesPanel.in(mouseX, mouseY, (int)ax, by, (int)aw, 16);
        TexturesPanel.r(ctx, ax, by, aw, 16.0f, 6.0f, can ? (over ? -6555690 : -8523578) : 0x1CFFFFFF);
        TexturesPanel.t(ctx, apply, ax + 9.0f, (float)(by + 8) - 3.5f, can ? -16045536 : -7362144, 7.4f, true);
        if (can) {
            this.hits.add(new Hit((int)ax, by, (int)aw, 16, 0, () -> this.apply(client)));
        }
        float sw = ax - 10.0f - bx;
        if (TextureMixer.building()) {
            TexturesPanel.r(ctx, bx, by + 6, sw, 4.0f, 2.0f, 0x33FFFFFF);
            TexturesPanel.r(ctx, bx, by + 6, Math.max(4.0f, sw * TextureMixer.progress()), 4.0f, 2.0f, -8523578);
            return;
        }
        int open = 0;
        for (String id : TextureMixer.DISPLAY_ORDER()) {
            if (!TextureMixer.pending(id)) continue;
            ++open;
        }
        if (!this.note.isEmpty() && System.currentTimeMillis() - this.noteAt < 6000L) {
            TexturesPanel.t(ctx, LauncherGfx.fit(this.note, 6.6f, false, sw), bx, (float)(by + 8) - 3.5f, -7362144, 6.6f, false);
        } else if (open > 0) {
            String msg = open + (open == 1 ? Lang.t(" \u00c4nderung nicht angewendet", " change not applied") : Lang.t(" \u00c4nderungen nicht angewendet", " changes not applied"));
            float mw = TexturesPanel.tw(msg, 6.8f, true) + 18.0f;
            TexturesPanel.r(ctx, bx, by + 1, mw, 14.0f, 7.0f, 720746795);
            TexturesPanel.ring(ctx, bx, by + 1, mw, 14.0f, 7.0f, 1727379755);
            Shapes.circle(ctx, bx + 7.0f, by + 8, 1.6f, -673493);
            TexturesPanel.t(ctx, msg, bx + 12.0f, (float)(by + 8) - 3.5f, -673493, 6.8f, true);
        } else if (!TextureMixer.message().isEmpty()) {
            TexturesPanel.t(ctx, LauncherGfx.fit(TextureMixer.message(), 6.6f, false, sw), bx, (float)(by + 8) - 3.5f, -7362144, 6.6f, false);
        }
    }

    private float footButton(GuiGraphicsExtractor ctx, float bx, int by, String label, int mouseX, int mouseY, Runnable action) {
        float w = TexturesPanel.tw(label, 7.4f, true) + 16.0f;
        boolean over = TexturesPanel.in(mouseX, mouseY, (int)bx, by, (int)w, 16);
        float h = Anim.toward("txfoot" + label, over, 110);
        TexturesPanel.r(ctx, bx, by, w, 16.0f, 6.0f, TexturesPanel.mix(0x16FFFFFF, 0x26FFFFFF, h));
        TexturesPanel.ring(ctx, bx, by, w, 16.0f, 6.0f, TexturesPanel.mix(0x24FFFFFF, -2005012282, h));
        TexturesPanel.t(ctx, label, bx + 8.0f, (float)(by + 8) - 3.5f, -1116943, 7.4f, true);
        this.hits.add(new Hit((int)bx, by, (int)w, 16, 0, action));
        return w;
    }

    private void apply(Minecraft client) {
        if (TextureMixer.building()) {
            return;
        }
        HashMap<String, String> choice = new HashMap<String, String>(FwmConfig.INSTANCE.textureMix);
        choice.values().removeIf(v -> v == null || v.isEmpty());
        if (choice.isEmpty()) {
            TextureMixer.reset(client);
        } else {
            TextureMixer.build(client);
        }
    }

    private void pickSky() {
        this.say(Lang.t("Bild w\u00e4hlen \u2026", "Choose a picture \u2026"));
        Thread t = new Thread(() -> {
            Path picked = SkyStudio.pick();
            if (picked == null) {
                return;
            }
            Minecraft.getInstance().execute(() -> this.filesDropped(List.of(picked)));
        }, "FWM sky picker");
        t.setDaemon(true);
        t.start();
    }

    private void openFolder() {
        Minecraft client = Minecraft.getInstance();
        Util.getPlatform().openPath(client.getResourcePackDirectory());
        this.say(Lang.t("PNG, ZIP oder Ordner hineinlegen oder ins Fenster ziehen \u00b7 bleibt lokal", "Put a PNG, ZIP or folder in, or drop it onto the window \u00b7 stays local"));
    }

    private void say(String text) {
        this.note = text;
        this.noteAt = System.currentTimeMillis();
    }

    private void askDelete(String pack) {
        this.confirm.ask((Component)Lang.of("fwm.textures.deletePack", TexturesPanel.name(pack)), () -> {
            try {
                Path dir = Minecraft.getInstance().getResourcePackDirectory().resolve(pack).normalize();
                String folder = dir.getFileName().toString();
                if (!folder.startsWith(IMPORT_PREFIX) && !folder.startsWith("FWM Himmel ")) {
                    return;
                }
                try (Stream<Path> walk = Files.walk(dir, new FileVisitOption[0]);){
                    for (Path p : walk.sorted(Comparator.reverseOrder()).toList()) {
                        Files.deleteIfExists(p);
                    }
                }
                if (pack.equals(TextureMixer.chosen(this.category))) {
                    TextureMixer.choose(this.category, "");
                }
                TextureMixer.clearPreviews();
                this.onResize();
                this.say(Lang.t("Gel\u00f6scht: ", "Deleted: ") + TexturesPanel.name(pack));
            }
            catch (Exception e) {
                this.say(Lang.t("Konnte nicht gel\u00f6scht werden.", "Could not be deleted."));
            }
        });
    }

    public void filesDropped(List<Path> paths) {
        Path dir = Minecraft.getInstance().getResourcePackDirectory();
        int added = 0;
        for (Path path : paths) {
            try {
                Path png;
                TextureMixer.Category cat;
                String fileName = path.getFileName().toString();
                String lower = fileName.toLowerCase(Locale.ROOT);
                if (lower.endsWith(".zip")) {
                    Files.copy(path, dir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                    ++added;
                    continue;
                }
                if (Files.isDirectory(path, new LinkOption[0]) && Files.exists(path.resolve("pack.mcmeta"), new LinkOption[0])) {
                    Path target = dir.resolve(fileName);
                    try (Stream<Path> walk = Files.walk(path, new FileVisitOption[0]);){
                        for (Path p : walk.toList()) {
                            Path out = target.resolve(path.relativize(p).toString()).normalize();
                            if (!out.startsWith(target)) continue;
                            if (Files.isDirectory(p, new LinkOption[0])) {
                                Files.createDirectories(out, new FileAttribute[0]);
                                continue;
                            }
                            Files.copy(p, out, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                    ++added;
                    continue;
                }
                if ((lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")) && this.category.equals("sky")) {
                    if (SkyStudio.make(path) == null) continue;
                    ++added;
                    continue;
                }
                if (!lower.endsWith(".png") || (cat = TextureMixer.category(this.category)).preview() == null) continue;
                String base = fileName.substring(0, fileName.length() - 4).replaceAll("[^A-Za-z0-9 _-]", "");
                Path target = dir.resolve(IMPORT_PREFIX + cat.label().replaceAll("[^A-Za-z0-9 _-]", "") + " " + base).normalize();
                if (!target.startsWith(dir) || !(png = target.resolve(cat.preview()).normalize()).startsWith(target)) continue;
                Files.createDirectories(png.getParent(), new FileAttribute[0]);
                Files.copy(path, png, StandardCopyOption.REPLACE_EXISTING);
                Files.writeString(target.resolve("pack.mcmeta"), (CharSequence)AntiLeakPack.mcmeta(), StandardCharsets.UTF_8, new OpenOption[0]);
                ++added;
            }
            catch (Exception e) {
                System.err.println("[FWM] Import failed: " + e.getMessage());
            }
        }
        TextureMixer.clearPreviews();
        this.onResize();
        this.say((String)(added == 0 ? Lang.t("Nichts davon ist ein Pack oder PNG.", "None of that is a pack or PNG.") : added + Lang.t(" importiert \u00b7 lokal, nichts wird hochgeladen", " imported \u00b7 local, nothing uploaded")));
    }

    private void button(GuiGraphicsExtractor ctx, Font font, int bx, int by, int bw, String label, int mouseX, int mouseY, boolean enabled, Runnable action) {
        boolean over = enabled && TexturesPanel.in(mouseX, mouseY, bx, by, bw, 14);
        float h = Anim.toward("txbtn" + label, over, 110);
        Shapes.round(ctx, bx, by, bw, 14.0f, 5.0f, Shapes.mix(0x16FFFFFF, 712896710, h));
        Shapes.ring(ctx, bx, by, bw, 14.0f, 5.0f, Shapes.mix(0x24FFFFFF, -1434586938, h));
        MutableComponent text = Component.literal((String)font.plainSubstrByWidth(label, bw - 4));
        int n = bx + (bw - font.width((FormattedText)text)) / 2;
        Objects.requireNonNull(font);
        ctx.text(font, (Component)text, n, by + (14 - 9) / 2 + 1, !enabled ? FwmTheme.TEXT_DIM : (over ? FwmTheme.ACCENT : FwmTheme.TEXT), false);
        if (enabled) {
            this.hits.add(new Hit(bx, by, bw, 14, 0, action));
        }
    }

    private static boolean in(double mx, double my, int bx, int by, int bw, int bh) {
        return mx >= (double)bx && mx < (double)(bx + bw) && my >= (double)by && my < (double)(by + bh);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.confirm.open()) {
            return this.confirm.mouseClicked(mouseX, mouseY);
        }
        if (!this.contains(mouseX, mouseY)) {
            return false;
        }
        EditBox field = this.themesTab ? this.themeName : this.search;
        int[] sb = this.searchBox();
        boolean inField = this.themesTab ? TexturesPanel.in(mouseX, mouseY, this.midX(), this.y + 23, this.midW() - 66, 15) : TexturesPanel.in(mouseX, mouseY, sb[0], sb[1], sb[2], sb[3]);
        field.setFocused(inField);
        if (inField) {
            return true;
        }
        int[] pb = this.previewBox();
        if (!this.themesTab && this.showRight() && button == 0 && TexturesPanel.in(mouseX, mouseY, pb[0], pb[1] + 20, pb[2], pb[3] - 20)) {
            this.dragging = true;
            this.dragFromX = mouseX;
            TextureMixer.Category cat = TextureMixer.category(this.category);
            String after = TextureMixer.chosen(this.category);
            String shown = this.showBefore ? V2.INSTANCE.textureApplied.getOrDefault(this.category, "") : after;
            TextureMixer.Preview p = shown.isEmpty() ? TextureMixer.vanillaPreview(cat) : TextureMixer.preview(shown, cat);
            this.dragBase = p == null ? 0.0f : (float)this.frameNow(p);
            this.dragFrames = 0.0f;
            this.draggedAt = System.currentTimeMillis();
            return true;
        }
        for (Hit hit : this.hits) {
            if (hit.button() != button || !TexturesPanel.in(mouseX, mouseY, hit.x(), hit.y(), hit.w(), hit.h())) continue;
            hit.action().run();
            return true;
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        if (!this.dragging) {
            return false;
        }
        this.dragFrames = (float)((mouseX - this.dragFromX) / 4.0);
        this.draggedAt = System.currentTimeMillis();
        return true;
    }

    @Override
    public void mouseReleased() {
        if (this.dragging) {
            this.draggedAt = System.currentTimeMillis();
        }
        this.dragging = false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double vertical) {
        int step = -((int)Math.signum(vertical));
        if (mouseX < (double)(this.x + 158)) {
            this.listScroll = Math.max(0, this.listScroll + step);
        } else if (this.themesTab) {
            this.themeScroll = Math.max(0, this.themeScroll + step);
        } else {
            this.gridScroll = Math.max(0, this.gridScroll + step);
        }
        return true;
    }

    private EditBox focused() {
        if (this.themesTab) {
            return this.themeName != null && this.themeName.isFocused() ? this.themeName : null;
        }
        return this.search != null && this.search.isFocused() ? this.search : null;
    }

    @Override
    public boolean isCapturingKeys() {
        return this.confirm.open() || this.focused() != null;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.confirm.open()) {
            return this.confirm.keyPressed(keyCode);
        }
        EditBox f = this.focused();
        if (f == null) {
            if (keyCode == 47 || keyCode == 331) {
                if (!this.themesTab) {
                    this.search.setFocused(true);
                }
                return true;
            }
            if (keyCode == 70 && this.hovered != null && !this.hovered.isEmpty()) {
                TextureMixer.toggleFavourite(this.hovered);
                return true;
            }
            if ((keyCode == 257 || keyCode == 335) && TextureMixer.pending()) {
                this.apply(Minecraft.getInstance());
                return true;
            }
            return false;
        }
        if (keyCode == 256) {
            f.setFocused(false);
            if (f == this.themeName) {
                this.renaming = null;
            }
            return true;
        }
        if (keyCode == 257 && f == this.themeName) {
            this.saveTheme();
            return true;
        }
        boolean used = f.keyPressed(new KeyEvent(keyCode, scanCode, modifiers));
        if (f == this.search) {
            this.gridScroll = 0;
        }
        return used;
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        EditBox f = this.focused();
        if (f == null) {
            return false;
        }
        if (f == this.search) {
            this.gridScroll = 0;
        }
        return f.charTyped(input);
    }

    @Override
    public void onHidden() {
        if (this.search != null) {
            this.host.detach((AbstractWidget)this.search);
        }
        if (this.themeName != null) {
            this.host.detach((AbstractWidget)this.themeName);
        }
        TextureMixer.releasePreviews();
    }

    private static enum Filter {
        ALL,
        FWM,
        FAVS,
        OWN;

    }

    private record Hit(int x, int y, int w, int h, int button, Runnable action) {
    }
}

