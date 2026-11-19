/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.ConfirmLinkScreen
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  org.joml.Matrix3x2fStack
 */
package net.friendswithmoney.fwm.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import net.friendswithmoney.fwm.FwmClient;
import net.friendswithmoney.fwm.chat.Messages;
import net.friendswithmoney.fwm.config.ConfigManager;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.game.PvpFx;
import net.friendswithmoney.fwm.lyrics.MediaWatcher;
import net.friendswithmoney.fwm.lyrics.SkyLyrics;
import net.friendswithmoney.fwm.net.Giveaways;
import net.friendswithmoney.fwm.net.ServerContext;
import net.friendswithmoney.fwm.ui.DesignHost;
import net.friendswithmoney.fwm.ui.FwmGfx;
import net.friendswithmoney.fwm.ui.FwmNotice;
import net.friendswithmoney.fwm.ui.FwmTheme;
import net.friendswithmoney.fwm.ui.FwmTutorial;
import net.friendswithmoney.fwm.ui.GlassBackground;
import net.friendswithmoney.fwm.ui.IntroScreen;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.LauncherGfx;
import net.friendswithmoney.fwm.ui.SharpIcons;
import net.friendswithmoney.fwm.ui.StreamerScreen;
import net.friendswithmoney.fwm.ui.gfx.FwmFont;
import net.friendswithmoney.fwm.ui.gfx.Shapes;
import net.friendswithmoney.fwm.ui.panel.Panel;
import net.friendswithmoney.fwm.ui.panel.RowsPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.joml.Matrix3x2fStack;

public class LauncherScreen
extends DesignHost {
    private static final int VW = 800;
    private static final int VH = 446;
    public static final Group STREAMER = new Group("Streamer", "Streamer", "group.streamer", new Tile[]{Tile.of("fwm.tab.auction", "Auktion", "Auction", "Items versteigern, Gebote per /pay, Rangliste und Statistik.", "Auction items, bids via /pay, ranking and statistics."), Tile.of("fwm.tab.giveaway", "Giveaway", "Giveaway", "Teilnahme per /msg-Wort, Gewinner per Rad auslosen.", "Join with a /msg word, draw the winner on a wheel."), Tile.of("fwm.tab.donations", "Donations", "Donations", "Ingame-/pay als Donation mit Animation und Top-Donatern.", "In-game /pay as a donation with animation and top donors."), Tile.of("fwm.tab.antileak", "Anti-Leak", "Anti-Leak", "Versteckt Erze, Bedrock-Muster und Koordinaten.", "Hides ores, bedrock patterns and coordinates.")});
    public static final Group FARMING = new Group("Farmen", "Farming", "group.farming", new Tile[]{Tile.of("fwm.tab.sellmacro", "Sell-Macro", "Sell macro", "Verkauft deine Farm automatisch", "Sells your farm by itself"), new Tile("stats", "fwm.tab.sellmacro", 1, "Statistik & Tests", "Statistics & tests", "Einnahmen & Minutentest", "Earnings & minute test", "fwm.tab.sellstats"), Tile.of("fwm.tab.spawnermacro", "Spawner-Macro", "Spawner macro", "Leert Spawner f\u00fcr dich", "Empties spawners for you"), new Tile("leaderboard", "fwm.tab.auction", 2, "Leaderboard", "Leaderboard", "Bestenlisten deiner Auktionen", "Boards of your auctions", "fwm.tab.auctionstats")});
    public static final Group[] COLUMNS = new Group[]{new Group("Chat", "Chat", "group.chat", new Tile[]{Tile.of("fwm.tab.messages", "Nachrichten", "Messages", "Privatnachrichten als Chat", "Private messages as a chat"), Tile.of("fwm.tab.secondchat", "Second Chat", "Second chat", "Zweites Chatfenster", "A second chat window"), Tile.of("fwm.tab.adblock", "Adblocker", "Ad blocker", "Blendet Werbung aus", "Hides adverts"), Tile.of("fwm.tab.emotes", "Emotes", "Emotes", "7TV- & BTTV-Emotes", "7TV & BTTV emotes"), Tile.of("fwm.tab.colorcodes", "Colour Codes", "Colour codes", "Farbige Namen bauen", "Build coloured names")}), new Group("Handel", "Trading", "group.trade", new Tile[]{Tile.of("fwm.tab.market", "Marktpreise", "Market prices", "Preise aller Items", "Prices of every item"), Tile.of("fwm.tab.kommt", "Price Pings", "Price pings", "Alarm bei gutem Preis", "An alarm at a good price"), Tile.of("fwm.tab.money", "Money", "Money", "Einnahmen & Ausgaben", "Income & spending"), Tile.of("fwm.tab.spawner", "Spawner", "Spawners", "Spawner-Preise", "Spawner prices"), Tile.of("fwm.tab.tickets", "Tickets", "Tickets", "Spawner An- & Verkauf, Middleman", "Spawner trade & middleman")}), new Group("Clan & Welt", "Clan & world", "group.clan", new Tile[]{Tile.of("fwm.tab.members", "Mitglieder", "Members", "Clan-Mitglieder", "Clan members"), Tile.of("fwm.tab.rtp", "RTP-Karte", "RTP map", "RTP-Punkte & Wegpunkte", "RTP points & waypoints"), Tile.of("fwm.tab.waypoints", "Wegpunkte", "Waypoints", "Marker in der Welt", "Markers in the world"), Tile.of("fwm.tab.fightkit", "Fight Kits", "Fight kits", "Zeigt, was im Kit fehlt", "Shows what the kit lacks")}), new Group("Extras", "Extras", "fwm.tab.extras", new Tile[]{Tile.of("fwm.tab.todos", "To-Dos", "To-dos", "Dailies mit Erinnerung", "Dailies with reminders"), Tile.of("fwm.tab.toolguard", "Tool Guard", "Tool guard", "Sch\u00fctzt fast kaputtes Werkzeug", "Saves tools about to break"), Tile.of("fwm.tab.skylyrics", "Sky Lyrics", "Sky Lyrics", "Songtext am Himmel", "Lyrics in the sky"), Tile.of("fwm.tab.sounds", "Sound Muffler", "Sound muffler", "Sounds einzeln leiser", "Each sound quieter"), Tile.of("fwm.tab.crosshair", "Crosshair", "Crosshair", "Eigenes Fadenkreuz", "Your own crosshair"), Tile.of("fwm.tab.calc", "Rechner", "Calculator", "Im Inventar", "In the inventory"), Tile.of("fwm.tab.mapart", "Mapart", "Mapart", "Bild als Karte bauen", "Build a picture as a map"), Tile.of("fwm.tab.armortrim", "R\u00fcstungs-Trims", "Armor Trims", "R\u00fcstung mit Trims planen", "Plan armour with trims"), Tile.of("fwm.tab.armorwarn", "R\u00fcstungs-Warnung", "Armour warning", "Warnt vor kaputter R\u00fcstung", "Warns before armour breaks"), Tile.of("fwm.tab.autotpa", "Auto-TPA", "Auto TPA", "Nimmt TPA deiner Leute an", "Accepts TPA from your people")}), new Group("PvP-Tools", "PvP tools", "group.pvp", new Tile[]{Tile.of("fwm.tab.hitcolor", "Hit-Farbe", "Hit colour", "Eigene Farbe beim Treffen", "Your colour on a hit"), Tile.of("fwm.tab.totempop", "Totem-Pop", "Totem pop", "Pop-Farben & Pop Chams", "Pop colours & pop chams"), new Tile("pvplyrics", "fwm.tab.skylyrics", 3, "PvP-Lyrics", "PvP lyrics", "Songtext beim Gegner", "Lyrics at the opponent", "fwm.tab.skylyrics"), Tile.of("fwm.tab.totemhold", "Totem hochhalten", "Hold up totem", "Arm mit Totem nach vorne", "Arm with totem forward"), Tile.of("fwm.tab.textures", "Texturen", "Textures", "Kristalle, Totems & mehr", "Crystals, totems & more"), Tile.of("fwm.tab.totemstudio", "Totem Studio", "Totem Studio", "Eigenes Totem bauen", "Build your own totem"), Tile.of("fwm.tab.sky", "Himmel", "Sky", "Eigener Himmel, ohne Extra-Mod", "Own sky, no extra mod")})};
    static final String[][] FOOT = new String[][]{{"Tasten", "Keys", "fwm.tab.settings", "3"}, {"HUD", "HUD", "fwm.tab.settings", "2"}, {"Datenschutz", "Privacy", "fwm.tab.settings", "4"}};
    private static final int BG_TOP = -1978388437;
    private static final int BG_BOTTOM = -1676994025;
    private static final int CARD = 0x10FFFFFF;
    private static final int CARD_EDGE = 0x17FFFFFF;
    private static final int TILE = 0x10FFFFFF;
    private static final int TILE_HI = 0x1EFFFFFF;
    private static final int TILE_EDGE = 0x12FFFFFF;
    private static final int MINT = -8523578;
    private static final int MINT_DIM = -11552618;
    private static final int GOLD = -673493;
    private static final int HEART = -42386;
    private static final int TEXT = -1116943;
    private static final int SUB = -6902107;
    private static final int LABEL = -4666172;
    private static final int PAD = 12;
    private static final int FAV_Y = 50;
    private static final int TOP_Y = 77;
    private static final int TOP_H = 99;
    private static final int GAP = 7;
    private static final int TILE_H = 27;
    private static final int STEP = 31;
    private static final float CARD_R = 8.5f;
    private static final float TILE_R = 6.0f;
    private String query = "";
    private boolean allModules;
    private final float[] colScroll = new float[COLUMNS.length + 2];
    private final float[] colTarget = new float[COLUMNS.length + 2];
    private final int[][] colBox = new int[COLUMNS.length + 2][];
    private long lastFrame;
    private final List<Hit> hits = new ArrayList<Hit>();
    private String hoverSub;
    private static final int ROWS_VW = 640;
    private static final int PAGE_VW = 700;
    private static final float T_GROUP = 7.7f;
    private static final float T_TILE = 7.0f;
    private static final float T_SUB = 6.2f;
    private static final float T_BIG = 8.5f;
    private static final float T_SMALL = 6.6f;
    private final HashMap<String, float[]> hoverAnim = new HashMap();
    private float frameDt;
    private long lastDraw;
    private final HashMap<String, List<String>> wrapped = new HashMap();

    private Group[] columnGroups() {
        if (!this.allModules) {
            return COLUMNS;
        }
        Group[] out = new Group[COLUMNS.length + 2];
        out[0] = STREAMER;
        out[1] = FARMING;
        System.arraycopy(COLUMNS, 0, out, 2, COLUMNS.length);
        return out;
    }

    static ItemStack item(String id) {
        Item i = switch (id) {
            case "fwm.tab.auction" -> Items.BELL;
            case "fwm.tab.giveaway" -> Items.CHEST;
            case "fwm.tab.antileak" -> Items.ENDER_EYE;
            case "fwm.tab.sellmacro" -> Items.HOPPER;
            case "stats", "fwm.tab.sellstats" -> Items.CLOCK;
            case "fwm.tab.spawnermacro", "fwm.tab.spawner" -> Items.SPAWNER;
            case "leaderboard", "fwm.tab.auctionstats" -> Items.GOLDEN_HELMET;
            case "fwm.tab.messages" -> Items.WRITABLE_BOOK;
            case "fwm.tab.secondchat" -> Items.OAK_HANGING_SIGN;
            case "fwm.tab.adblock" -> Items.BARRIER;
            case "fwm.tab.emotes" -> Items.CREEPER_HEAD;
            case "fwm.tab.colorcodes" -> Items.NAME_TAG;
            case "fwm.tab.market" -> Items.EMERALD;
            case "fwm.tab.kommt" -> Items.NOTE_BLOCK;
            case "fwm.tab.money" -> Items.GOLD_INGOT;
            case "fwm.tab.members" -> Items.PLAYER_HEAD;
            case "fwm.tab.tickets" -> Items.PAPER;
            case "fwm.tab.rtp" -> Items.MAP;
            case "fwm.tab.waypoints" -> Items.COMPASS;
            case "fwm.tab.fightkit" -> Items.DIAMOND_SWORD;
            case "fwm.tab.tpaguard" -> Items.SHIELD;
            case "fwm.tab.toolguard" -> Items.DIAMOND_PICKAXE;
            case "fwm.tab.totem" -> Items.TOTEM_OF_UNDYING;
            case "fwm.tab.sounds" -> Items.JUKEBOX;
            case "fwm.tab.crosshair" -> Items.TARGET;
            case "fwm.tab.calc" -> Items.COMPARATOR;
            case "fwm.tab.todos" -> Items.WRITABLE_BOOK;
            case "fwm.tab.donations" -> Items.DRAGON_HEAD;
            case "fwm.tab.textures" -> Items.END_CRYSTAL;
            case "fwm.tab.mapart" -> Items.FILLED_MAP;
            case "fwm.tab.sky" -> Items.SUNFLOWER;
            case "fwm.tab.skylyrics" -> Items.JUKEBOX;
            case "pvplyrics" -> Items.NETHERITE_SWORD;
            case "fwm.tab.totemstudio" -> Items.TOTEM_OF_UNDYING;
            case "fwm.tab.armortrim" -> Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE;
            case "fwm.tab.hitcolor" -> Items.NETHERITE_SWORD;
            case "fwm.tab.totempop" -> Items.TOTEM_OF_UNDYING;
            case "fwm.tab.totemhold" -> Items.TOTEM_OF_UNDYING;
            case "group.pvp" -> Items.END_CRYSTAL;
            case "fwm.tab.armorwarn" -> Items.DIAMOND_CHESTPLATE;
            case "fwm.tab.autotpa" -> Items.ENDER_PEARL;
            case "fwm.tab.extras" -> Items.NETHER_STAR;
            case "group.streamer" -> Items.SPYGLASS;
            case "group.farming" -> Items.WHEAT;
            case "group.chat" -> Items.BOOK;
            case "group.trade" -> Items.EMERALD;
            case "group.clan" -> Items.GRASS_BLOCK;
            default -> Items.PAPER;
        };
        return new ItemStack((ItemLike)i);
    }

    static float launcherCrisp(int width, int height, double gui) {
        double fit = Math.min((double)width * gui * 0.76 / 800.0, (double)height * gui * 0.82 / 446.0);
        if (FwmFont.enabled()) {
            return (float)(Math.max(0.5, Math.floor(fit * 8.0) / 8.0) / gui);
        }
        double whole = Math.floor(fit);
        double px = whole >= 1.0 && whole / fit >= 0.88 ? whole : Math.max(0.5, Math.floor(fit * 4.0) / 4.0);
        return (float)(px / gui);
    }

    @Override
    protected float crisp(int width, int height, double gui) {
        return LauncherScreen.launcherCrisp(width, height, gui);
    }

    public static int maxScalePercent() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getWindow() == null) {
            return 150;
        }
        int w = client.getWindow().getGuiScaledWidth();
        int h = client.getWindow().getGuiScaledHeight();
        float crisp = LauncherScreen.launcherCrisp(w, h, client.getWindow().getGuiScale());
        float fits = Math.min((float)w / 800.0f, (float)h / 446.0f);
        int most = (int)Math.floor(fits / crisp * 100.0f);
        return Math.max(70, Math.min(150, most));
    }

    public LauncherScreen(Screen parent) {
        this(parent, null, -1);
    }

    public LauncherScreen(Screen parent, String page, int tab) {
        super(parent);
        if (page != null) {
            this.openPage(page, tab);
        }
        if (!V2.INSTANCE.whatsNewSeen) {
            V2.INSTANCE.whatsNewSeen = true;
            V2.save();
            FwmNotice.show("FWM Mod 2.0", Lang.t("Willkommen in 2.0", "Welcome to 2.0"), Lang.t("Neues Men\u00fc, Texturen, Totem-Counter, Crosshair, Adblocker und mehr", "New menu, textures, totem counter, crosshair, ad blocker and more"), 16103723, 9000L);
        }
    }

    @Override
    protected int vw() {
        if (this.rowsPage() != null) {
            return 640;
        }
        return this.pageKey != null ? 700 : 800;
    }

    private RowsPanel rowsPage() {
        RowsPanel rows;
        if (this.pageKey == null) {
            return null;
        }
        Panel panel = this.active();
        return panel instanceof RowsPanel && (rows = (RowsPanel)panel).compact() ? rows : null;
    }

    @Override
    protected int vh() {
        RowsPanel rows = this.rowsPage();
        if (rows != null) {
            return Math.max(300, Math.min(446, 57 + Math.round((float)rows.contentHeight() * this.pageScale()) + 30));
        }
        if (this.pageKey != null || LauncherScreen.all().isEmpty()) {
            return 446;
        }
        int most = 0;
        for (Group g : this.columnGroups()) {
            most = Math.max(most, LauncherScreen.visible(g.tiles()).size());
        }
        int needed = this.columnsTop() + 21 + most * 31 - 4 + 6 + 28;
        return Math.max(300, Math.min(446, needed));
    }

    private int columnsTop() {
        boolean topRow = !this.allModules && (!LauncherScreen.visible(STREAMER.tiles()).isEmpty() || !LauncherScreen.visible(FARMING.tiles()).isEmpty());
        return topRow ? 183 : 77;
    }

    private static String page(Tile t) {
        if (t.id().equals("stats") && !Features.allowedHere("fwm.tab.sellmacro")) {
            return "fwm.tab.sellstats";
        }
        return t.page();
    }

    private static int tab(Tile t) {
        return t.id().equals("stats") && !Features.allowedHere("fwm.tab.sellmacro") ? -1 : t.tab();
    }

    private static boolean visible(Tile t) {
        return Features.allowedHere(LauncherScreen.page(t)) && (!LauncherScreen.hidden(t) || V2.INSTANCE.showHidden);
    }

    private static boolean hidden(Tile t) {
        return V2.INSTANCE.hiddenTiles.contains(t.id());
    }

    private static void toggleHidden(Tile t) {
        String p;
        boolean nowHidden;
        boolean bl = nowHidden = !V2.INSTANCE.hiddenTiles.remove(t.id());
        if (nowHidden) {
            V2.INSTANCE.hiddenTiles.add(t.id());
        }
        if (LauncherScreen.switchable(p = LauncherScreen.page(t))) {
            Features.set(LauncherScreen.feature(p), !nowHidden);
            ConfigManager.save();
        }
        if (nowHidden) {
            V2 c = V2.INSTANCE;
            switch (p) {
                case "fwm.tab.hitcolor": {
                    c.hitColorOn = false;
                    PvpFx.applyHitColor();
                    break;
                }
                case "fwm.tab.totempop": {
                    c.totemPopOn = false;
                    c.popChamsOn = false;
                    break;
                }
                case "fwm.tab.totemhold": {
                    c.totemHoldUp = false;
                    break;
                }
            }
        }
        V2.save();
    }

    private static boolean locked(Tile t) {
        return !Features.allowedHere(LauncherScreen.page(t));
    }

    private static String lockedNote() {
        ServerContext.Server server = ServerContext.current();
        String name = server.label != null ? server.label : Lang.t("diesem Server", "this server");
        return Lang.t("Auf " + name + " gesperrt", "Locked on " + name);
    }

    private boolean matches(Tile t) {
        if (this.query.isBlank()) {
            return true;
        }
        String q = this.query.toLowerCase(Locale.ROOT).trim();
        return t.name().toLowerCase(Locale.ROOT).contains(q) || t.sub().toLowerCase(Locale.ROOT).contains(q) || Lang.str(t.page(), new Object[0]).toLowerCase(Locale.ROOT).contains(q);
    }

    private static List<Tile> visible(Tile[] tiles) {
        ArrayList<Tile> out = new ArrayList<Tile>();
        for (Tile t : tiles) {
            if (!LauncherScreen.visible(t)) continue;
            out.add(t);
        }
        return out;
    }

    private static List<Tile> all() {
        ArrayList<Tile> out = new ArrayList<Tile>(LauncherScreen.visible(STREAMER.tiles()));
        out.addAll(LauncherScreen.visible(FARMING.tiles()));
        for (Group g : COLUMNS) {
            out.addAll(LauncherScreen.visible(g.tiles()));
        }
        return out;
    }

    private Tile firstMatch() {
        if (this.query.isBlank()) {
            return null;
        }
        for (Tile t : LauncherScreen.all()) {
            if (!this.matches(t)) continue;
            return t;
        }
        return null;
    }

    private static Tile byId(String id) {
        for (Tile t : LauncherScreen.all()) {
            if (!t.id().equals(id)) continue;
            return t;
        }
        return null;
    }

    private void open(Tile t) {
        if (LauncherScreen.locked(t)) {
            return;
        }
        this.query = "";
        Screen live = StreamerScreen.ifLive(LauncherScreen.page(t));
        if (live != null) {
            this.minecraft.gui.setScreen(live);
        } else {
            this.openPage(LauncherScreen.page(t), LauncherScreen.tab(t));
        }
    }

    private static boolean fav(Tile t) {
        return V2.INSTANCE.favourites.contains(t.id());
    }

    private static void toggleFav(Tile t) {
        if (!V2.INSTANCE.favourites.remove(t.id())) {
            V2.INSTANCE.favourites.add(t.id());
        }
        V2.save();
    }

    private static boolean off(Tile t) {
        String p = LauncherScreen.page(t);
        return LauncherScreen.switchable(p) && !LauncherScreen.on(p);
    }

    private float hov(String key, boolean over) {
        float[] v = this.hoverAnim.computeIfAbsent(key, k -> new float[1]);
        float target = over ? 1.0f : 0.0f;
        v[0] = V2.INSTANCE.animations == 0 ? target : v[0] + (target - v[0]) * (1.0f - (float)Math.exp(-this.frameDt * 22.0f));
        if (Math.abs(target - v[0]) < 0.01f) {
            v[0] = target;
        }
        return v[0];
    }

    private static int mix(int a, int b, float t) {
        return Shapes.mix(a, b, t);
    }

    private void t(GuiGraphicsExtractor ctx, String s, float x, float y, int color, float size, boolean bold) {
        LauncherGfx.text(ctx, s, x, y, color, size, bold);
    }

    private static float tw(String s, float size, boolean bold) {
        return LauncherGfx.width(s, size, bold);
    }

    @Override
    protected void drawCanvas(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
        this.hits.clear();
        this.hoverSub = null;
        long now = System.nanoTime();
        this.frameDt = this.lastDraw == 0L ? 0.0f : Math.min(0.1f, (float)(now - this.lastDraw) / 1.0E9f);
        this.lastDraw = now;
        this.header(ctx, mx, my);
        if (this.pageKey != null) {
            this.setFrame(12, 16, this.vw() - 24, this.vh() - 16 - 22);
            this.drawPage(ctx, mx, my, delta, "Start");
            this.footer(ctx, mx, my);
            return;
        }
        if (LauncherScreen.all().isEmpty()) {
            this.singleplayerNote(ctx, mx, my);
            this.footer(ctx, mx, my);
            return;
        }
        this.favourites(ctx, mx, my);
        if (!this.allModules) {
            this.topRow(ctx, mx, my);
        }
        this.columns(ctx, mx, my, this.columnsTop());
        this.footer(ctx, mx, my);
    }

    @Override
    protected void drawFrame(GuiGraphicsExtractor ctx) {
        int vh = this.vh();
        float pixels = this.scale() * (float)Minecraft.getInstance().getWindow().getGuiScale();
        GlassBackground.draw(ctx, this.vw(), vh, 13, pixels, -1978388437, -1676994025, 5021318, 88, 13608076, 38);
        Shapes.ring(ctx, 0.0f, 0.0f, this.vw(), vh, 13.0f, GlassBackground.themed(0x2EFFFFFF));
    }

    private void header(GuiGraphicsExtractor ctx, int mx, int my) {
        FwmTheme.logo(ctx, 12, 8, 36);
        float x = 52.0f;
        String badge = "MOD " + LauncherScreen.shortVersion();
        float bwid = LauncherScreen.tw(badge, 9.0f, true) + 12.0f;
        Shapes.roundGradient(ctx, x, 18.5f, bwid, 16.0f, 4.5f, -13499, -1531106);
        this.t(ctx, badge, x + 6.0f, 22.0f, -14018048, 9.0f, true);
        ServerContext.Server server = ServerContext.current();
        String name = server.label != null ? server.label : (server == ServerContext.Server.SINGLE ? "Singleplayer" : (server == ServerContext.Server.OTHER ? Lang.t("Server", "Server") : "Offline"));
        float sw = LauncherScreen.tw(name, 8.0f, true) + 23.0f;
        LauncherGfx.round(ctx, x += bwid + 7.0f, 18.0f, sw, 17.0f, 8.5f, 0x12FFFFFF);
        LauncherGfx.ring(ctx, x, 18.0f, sw, 17.0f, 8.5f, 0x1FFFFFFF);
        Shapes.circle(ctx, x + 9.0f, 26.5f, 2.7f, server.label != null ? -11148390 : -6902107);
        this.t(ctx, name, x + 15.0f, 22.5f, -1116943, 8.0f, true);
        float afterServer = x + sw;
        this.crumbX = Math.round(afterServer + 12.0f);
        if (this.pageKey != null) {
            return;
        }
        float cogX = this.vw() - 12 - 21;
        this.iconButton(ctx, "cog", cogX, mx, my, null, () -> this.openPage("fwm.tab.settings"));
        float infoX = cogX - 5.0f - 21.0f;
        this.iconButton(ctx, "info", infoX, mx, my, null, () -> this.openPage("fwm.tab.news"));
        int giveaways = Giveaways.open();
        if (giveaways > 0) {
            String n = String.valueOf(Math.min(9, giveaways));
            Shapes.circle(ctx, infoX + 18.5f, 18.5f, 4.5f, -673493);
            this.t(ctx, n, infoX + 18.5f - LauncherScreen.tw(n, 6.0f, true) / 2.0f, 15.0f, -14018048, 6.0f, true);
        }
        String allLabel = this.allModules ? Lang.t("Gruppen", "Groups") : Lang.t("Alle Module", "All modules");
        float aw = LauncherScreen.tw(allLabel, 7.2f, true) + 25.0f;
        float ax = infoX - 8.0f - aw;
        boolean overAll = (float)mx >= ax && (float)mx < ax + aw && my >= 17 && my < 37;
        float ha = this.hov("all", overAll);
        LauncherGfx.round(ctx, ax, 17.0f, aw, 19.5f, 6.0f, LauncherScreen.mix(-8523578, -6555690, ha));
        Shapes.icon(ctx, "grid", ax + 7.0f, 22.5f, 8.5f, -16045536);
        this.t(ctx, allLabel, ax + 18.5f, 23.25f, -16045536, 7.2f, true);
        this.hits.add(new Hit((int)ax, 17, (int)aw, 20, () -> {
            this.allModules = !this.allModules;
            Arrays.fill(this.colTarget, 0.0f);
            Arrays.fill(this.colScroll, 0.0f);
        }, null));
        float sWidth = Math.min(201.0f, ax - 9.0f - (afterServer + 24.0f));
        String dLabel = "Join FWM Discord";
        float dw = LauncherScreen.tw(dLabel, 7.2f, true) + 30.0f;
        if (ax - 9.0f - (afterServer + 24.0f) >= 120.0f + dw + 9.0f) {
            float dx = ax - 9.0f - sWidth - 9.0f - dw;
            boolean overD = (float)mx >= dx && (float)mx < dx + dw && my >= 17 && my < 37;
            float hd = this.hov("discord", overD);
            LauncherGfx.round(ctx, dx, 17.0f, dw, 19.5f, 6.0f, LauncherScreen.mix(-10983950, -8813579, hd));
            Shapes.icon(ctx, "discord", dx + 7.0f, 21.75f, 10.0f, -1);
            this.t(ctx, dLabel, dx + 21.0f, 23.25f, -1, 7.2f, true);
            this.hits.add(new Hit((int)dx, 17, (int)dw, 20, () -> ConfirmLinkScreen.confirmLinkNow((Screen)this, (String)FwmConfig.INSTANCE.discordUrl), null));
        } else {
            sWidth = Math.min(sWidth, ax - 9.0f - (afterServer + 24.0f));
        }
        float sx = ax - 9.0f - sWidth;
        boolean active = !this.query.isEmpty();
        boolean overSearch = (float)mx >= sx && (float)mx < sx + sWidth && my >= 16 && my < 37;
        float hs = this.hov("search", overSearch || active);
        LauncherGfx.round(ctx, sx, 16.0f, sWidth, 21.0f, 6.5f, LauncherScreen.mix(0x14FFFFFF, 0x1CFFFFFF, hs));
        LauncherGfx.ring(ctx, sx, 16.0f, sWidth, 21.0f, 6.5f, active ? -1434586938 : LauncherScreen.mix(0x1FFFFFFF, 0x33FFFFFF, hs));
        Shapes.icon(ctx, "search", sx + 8.0f, 22.0f, 9.0f, active ? -8523578 : -6902107);
        if (active) {
            this.t(ctx, this.query, sx + 21.0f, 23.0f, -1116943, 7.4f, false);
            if (System.currentTimeMillis() / 500L % 2L == 0L) {
                float cx = sx + 21.0f + LauncherScreen.tw(this.query, 7.4f, false) + 1.0f;
                LauncherGfx.round(ctx, cx, 21.5f, 0.8f, 10.0f, 0.4f, -8523578);
            }
        } else {
            this.t(ctx, LauncherGfx.fit(Lang.t("Suchen \u2026 (einfach lostippen)", "Search \u2026 (just start typing)"), 7.4f, false, sWidth - 40.0f), sx + 21.0f, 23.0f, -7954281, 7.4f, false);
        }
        LauncherGfx.ring(ctx, sx + sWidth - 17.0f, 22.0f, 10.0f, 9.5f, 2.5f, 0x40FFFFFF);
        this.t(ctx, "/", sx + sWidth - 12.0f - LauncherScreen.tw("/", 6.0f, false) / 2.0f, 23.25f, -6902107, 6.0f, false);
    }

    private static String shortVersion() {
        String[] p;
        String v = FwmClient.MOD_VERSION;
        if (v == null) {
            return "2.1";
        }
        int plus = v.indexOf(43);
        if (plus > 0) {
            v = v.substring(0, plus);
        }
        return (p = v.split("\\.")).length >= 2 ? p[0] + "." + p[1] : v;
    }

    private void iconButton(GuiGraphicsExtractor ctx, String icon, float x, int mx, int my, String note, Runnable click) {
        boolean over = (float)mx >= x && (float)mx < x + 21.0f && my >= 17 && my < 37;
        float h = this.hov("btn_" + icon, over);
        LauncherGfx.round(ctx, x, 17.0f, 21.0f, 19.5f, 6.0f, LauncherScreen.mix(0x14FFFFFF, 0x26FFFFFF, h));
        LauncherGfx.ring(ctx, x, 17.0f, 21.0f, 19.5f, 6.0f, LauncherScreen.mix(0x1FFFFFFF, 1719529670, h));
        int c = LauncherScreen.mix(-4666172, -1116943, h);
        if (icon.equals("cog")) {
            SharpIcons.draw(ctx, "icon_cog", (int)x + 5, 21, 11, c);
        } else {
            Shapes.icon(ctx, icon, x + 5.0f, 21.25f, 11.0f, c);
        }
        this.hits.add(new Hit((int)x, 17, 21, 20, click, null));
    }

    private void brandRight(GuiGraphicsExtractor ctx) {
        String name = FwmGfx.playerName();
        float cw = LauncherScreen.tw(name, 6.6f, false) + 22.0f;
        float rx = (float)(this.vw() - 12) - cw;
        LauncherGfx.round(ctx, rx, 19.5f, cw, 15.0f, 7.5f, 0x16FFFFFF);
        FwmGfx.playerHead(ctx, (int)rx + 4, 23, 8);
        this.t(ctx, name, rx + 16.0f, 23.5f, -1116943, 6.6f, false);
    }

    private void favourites(GuiGraphicsExtractor ctx, int mx, int my) {
        float x = 12.0f;
        Shapes.icon(ctx, "star_line", x, 54.5f, 9.0f, -673493);
        String fav = Lang.t("Favoriten", "Favourites");
        this.t(ctx, fav, x + 12.0f, 55.5f, -673493, 7.0f, true);
        x += 12.0f + LauncherScreen.tw(fav, 7.0f, true) + 8.0f;
        for (String id : V2.INSTANCE.favourites) {
            Tile t = LauncherScreen.byId(id);
            if (t == null) continue;
            float w = LauncherScreen.tw(t.name(), 7.0f, true) + 28.0f;
            if (x + w > (float)(this.vw() - 12 - 100)) break;
            boolean over = (float)mx >= x && (float)mx < x + w && my >= 50 && my < 70;
            float h = this.hov("fav_" + id, over);
            LauncherGfx.round(ctx, x, 50.0f, w, 20.0f, 6.0f, LauncherScreen.mix(0x14FFFFFF, 0x22FFFFFF, h));
            LauncherGfx.ring(ctx, x, 50.0f, w, 20.0f, 6.0f, LauncherScreen.mix(0x1AFFFFFF, -2005012282, h));
            this.iconBox(ctx, t, x + 4.0f, 54.0f, 12.0f, false);
            this.t(ctx, t.name(), x + 20.5f, 56.5f, -1116943, 7.0f, true);
            this.hits.add(new Hit((int)x, 50, (int)w, 20, () -> this.open(t), null));
            x += w + 4.5f;
        }
        String add = "+ " + Lang.t("Herz auf einer Kachel", "Heart on a tile");
        float aw = LauncherScreen.tw(add, 7.0f, false) + 14.0f;
        LauncherScreen.dashed(ctx, x, 50.0f, aw, 20.0f, 0x2EFFFFFF);
        this.t(ctx, add, x + 7.0f, 56.5f, -6902107, 7.0f, false);
    }

    private static void dashed(GuiGraphicsExtractor ctx, float x, float y, float w, float h, int c) {
        float e;
        float i;
        float r = 6.0f;
        for (i = x + r; i < x + w - r - 1.0f; i += 4.0f) {
            e = Math.min(i + 2.0f, x + w - r);
            LauncherGfx.round(ctx, i, y, e - i, 0.6f, 0.3f, c);
            LauncherGfx.round(ctx, i, y + h - 0.6f, e - i, 0.6f, 0.3f, c);
        }
        for (i = y + r; i < y + h - r - 1.0f; i += 4.0f) {
            e = Math.min(i + 2.0f, y + h - r);
            LauncherGfx.round(ctx, x, i, 0.6f, e - i, 0.3f, c);
            LauncherGfx.round(ctx, x + w - 0.6f, i, 0.6f, e - i, 0.3f, c);
        }
        ctx.enableScissor((int)x, (int)y, (int)(x + r), (int)(y + r));
        LauncherGfx.ring(ctx, x, y, w, h, r, c);
        ctx.disableScissor();
        ctx.enableScissor((int)(x + w - r), (int)y, (int)Math.ceil(x + w), (int)(y + r));
        LauncherGfx.ring(ctx, x, y, w, h, r, c);
        ctx.disableScissor();
        ctx.enableScissor((int)x, (int)(y + h - r), (int)(x + r), (int)Math.ceil(y + h));
        LauncherGfx.ring(ctx, x, y, w, h, r, c);
        ctx.disableScissor();
        ctx.enableScissor((int)(x + w - r), (int)(y + h - r), (int)Math.ceil(x + w), (int)Math.ceil(y + h));
        LauncherGfx.ring(ctx, x, y, w, h, r, c);
        ctx.disableScissor();
    }

    private void panel(GuiGraphicsExtractor ctx, float x, float y, float w, float h, Group g, boolean gold) {
        LauncherGfx.round(ctx, x, y, w, h, 8.5f, gold ? 318093611 : 0x10FFFFFF);
        LauncherGfx.ring(ctx, x, y, w, h, 8.5f, gold ? -1999002054 : 0x17FFFFFF);
        LauncherGfx.item(ctx, LauncherScreen.item(g.icon()), (int)(x + 9.0f), (int)(y + 6.5f), 0.6875f);
        this.t(ctx, g.name(), x + 23.5f, y + 12.0f - 3.5f, gold ? -10121 : -1116943, 7.7f, true);
    }

    private void iconBox(GuiGraphicsExtractor ctx, Tile t, float x, float y, float size, boolean gold) {
        boolean off = LauncherScreen.off(t) || LauncherScreen.locked(t);
        float r = Math.max(2.5f, size / 4.2f);
        LauncherGfx.round(ctx, x, y, size, size, r, gold ? 871741739 : 612233414);
        LauncherGfx.ring(ctx, x, y, size, size, r, gold ? 1442167083 : 780005574);
        float scale = Math.min(1.0f, (size - 3.0f) / 16.0f);
        float inner = 16.0f * scale;
        LauncherScreen.tileIcon(ctx, t.icon(), Math.round(x + (size - inner) / 2.0f), Math.round(y + (size - inner) / 2.0f), Math.round(inner), scale);
        if (off) {
            LauncherGfx.round(ctx, x, y, size, size, r, -1726997479);
        } else if (LauncherScreen.hidden(t)) {
            LauncherGfx.round(ctx, x, y, size, size, r, -2002210130);
        }
    }

    static void tileIcon(GuiGraphicsExtractor ctx, String icon, int x, int y, int size, float scale) {
        String picture;
        switch (icon) {
            case "fwm.tab.spawner": {
                String string = "iron_golem";
                break;
            }
            case "fwm.tab.spawnermacro": {
                String string = "blaze";
                break;
            }
            default: {
                String string = picture = null;
            }
        }
        if (picture != null) {
            ctx.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath((String)"fwm", (String)("textures/gui/spawners/" + picture + ".png")), x, y, 0.0f, 0.0f, size, size, 64, 64, 64, 64);
            return;
        }
        if (icon.equals("fwm.tab.members")) {
            int head = Math.max(8, size - 2);
            FwmGfx.playerHead(ctx, "HydraVB", x + (size - head) / 2, y + (size - head) / 2, head);
            return;
        }
        LauncherGfx.item(ctx, LauncherScreen.item(icon), x, y, scale);
    }

    private void heart(GuiGraphicsExtractor ctx, Tile t, float x, float y, int mx, int my, float rowHover) {
        boolean f = LauncherScreen.fav(t);
        boolean over = (float)mx >= x - 2.0f && (float)mx < x + 10.0f && (float)my >= y - 2.0f && (float)my < y + 10.0f;
        float h = this.hov("heart_" + t.id(), over);
        int c = f ? LauncherScreen.mix(-42386, -32624, h) : LauncherScreen.mix(LauncherScreen.mix(0x33FFFFFF, 0x66FFFFFF, rowHover), -570425345, h);
        Shapes.icon(ctx, f ? "heart" : "heart_line", x, y, 8.0f, c);
        this.hits.add(new Hit((int)x - 2, (int)y - 2, 12, 12, () -> LauncherScreen.toggleFav(t), null));
    }

    static void drawHeart(GuiGraphicsExtractor ctx, int x, int y, boolean filled, int c) {
        Shapes.icon(ctx, filled ? "heart" : "heart_line", (float)x - 0.5f, y - 1, 8.0f, c);
    }

    private void topRow(GuiGraphicsExtractor ctx, int mx, int my) {
        int i;
        float tw;
        float fw;
        List<Tile> streamer = LauncherScreen.visible(STREAMER.tiles());
        List<Tile> farming = LauncherScreen.visible(FARMING.tiles());
        float w = this.vw() - 24;
        float sw = farming.isEmpty() ? w : (streamer.isEmpty() ? 0.0f : (w - 7.0f) * 0.5f + 0.2f);
        float fx = streamer.isEmpty() ? 12.0f : 12.0f + sw + 7.0f;
        float f = fw = streamer.isEmpty() ? w : w - sw - 7.0f;
        if (!streamer.isEmpty()) {
            this.panel(ctx, 12.0f, 77.0f, sw, 99.0f, STREAMER, true);
            int n = streamer.size();
            tw = (sw - 15.2f - (float)(n - 1) * 6.8f) / (float)n;
            for (i = 0; i < n; ++i) {
                this.streamerTile(ctx, streamer.get(i), 19.6f + (float)i * (tw + 6.8f), 99.0f, tw, 70.0f, mx, my);
            }
        }
        if (!farming.isEmpty() && fw > 0.0f) {
            this.panel(ctx, fx, 77.0f, fw, 99.0f, FARMING, false);
            int cols = farming.size() > 1 ? 2 : 1;
            tw = (fw - 13.2f - (float)(cols - 1) * 4.2f) / (float)cols;
            for (i = 0; i < farming.size(); ++i) {
                float tx = fx + 6.6f + (float)(i % cols) * (tw + 4.2f);
                float ty = 99.0f + (float)(i / cols) * 30.6f;
                this.tile(ctx, farming.get(i), tx, ty, tw, 27.0f, mx, my, ty);
            }
        }
    }

    private void streamerTile(GuiGraphicsExtractor ctx, Tile t, float x, float y, float w, float h, int mx, int my) {
        boolean over;
        boolean match = this.matches(t);
        boolean bl = over = match && (float)mx >= x && (float)mx < x + w && (float)my >= y && (float)my < y + h;
        if (over) {
            this.hoverSub = t.sub();
        }
        float ho = this.hov("st_" + t.id(), over);
        LauncherGfx.round(ctx, x, y, w, h, 7.5f, LauncherScreen.mix(0x2E000000, 707404816, ho));
        LauncherGfx.ring(ctx, x, y, w, h, 7.5f, LauncherScreen.mix(0x14FFFFFF, -1711949525, ho));
        if (!match) {
            return;
        }
        this.iconBox(ctx, t, x + 6.4f, y + 5.2f, 21.0f, true);
        this.t(ctx, LauncherGfx.fit(t.name(), 8.5f, true, w - 48.0f), x + 33.2f, y + 11.6f - 3.5f, -1116943, 8.5f, true);
        float ly = y + 22.2f - 3.5f;
        float lines = 0.0f;
        for (String line : this.wrap(t.sub(), 6.3999996f, w - 38.0f)) {
            if (lines >= 3.0f || ly + 8.0f > y + h - 18.0f) break;
            this.t(ctx, line, x + 33.5f, ly, -6902107, 6.3999996f, false);
            ly += 8.3f;
            lines += 1.0f;
        }
        this.heart(ctx, t, x + w - 12.5f, y + 6.5f, mx, my, ho);
        float by = y + h - 21.4f;
        float bx = x + 6.4f;
        float bwid = w - 12.8f;
        if (LauncherScreen.locked(t)) {
            if (over) {
                this.hoverSub = LauncherScreen.lockedNote();
            }
            LauncherGfx.round(ctx, x, y, w, h, 7.5f, -2012210151);
            this.t(ctx, LauncherGfx.fit(LauncherScreen.lockedNote(), 6.2f, false, w - 10.0f), x + 6.0f, by + 7.5f - 3.5f, -6902107, 6.2f, false);
            return;
        }
        if (t.page().equals("fwm.tab.antileak")) {
            boolean onNow = Features.isOn("fwm.feature.antileak");
            float tg = this.hov("antileak_on", onNow);
            LauncherGfx.round(ctx, bx, by - 2.0f, bwid, 16.0f, 5.0f, 0x10FFFFFF);
            this.t(ctx, Lang.t("Schutz", "Protection"), bx + 5.0f, by + 6.0f - 3.5f, -1116943, 7.0f, true);
            LauncherGfx.toggle(ctx, bx + bwid - 25.0f, by, tg, -8523578, -12957119);
            this.hits.add(new Hit((int)bx, (int)by - 2, (int)bwid, 16, () -> {
                Features.toggle("fwm.feature.antileak");
                ConfigManager.save();
            }, null));
        } else {
            boolean overBtn = (float)mx >= bx && (float)mx < bx + bwid && (float)my >= by && (float)my < by + 15.3f;
            float hb = this.hov("btn_" + t.id(), overBtn);
            LauncherGfx.round(ctx, bx, by, bwid, 15.3f, 5.0f, LauncherScreen.mix(-8523578, -6555690, hb));
            String label = Lang.t("\u00d6ffnen", "Open");
            this.t(ctx, label, bx + (bwid - LauncherScreen.tw(label, 7.0f, true)) / 2.0f, by + 7.65f - 3.5f, -16045536, 7.0f, true);
            this.hits.add(new Hit((int)bx, (int)by, (int)bwid, 16, () -> this.open(t), null));
        }
        if (LauncherScreen.hidden(t)) {
            LauncherGfx.round(ctx, x, y, w, h, 7.5f, 1722331822);
        }
        this.hits.add(new Hit((int)x, (int)y, (int)w, (int)h, () -> this.open(t), () -> LauncherScreen.toggleHidden(t)));
    }

    private List<String> wrap(String s, float size, float width) {
        String key = s + "\u0000" + size + "\u0000" + Math.round(width);
        List<String> have = this.wrapped.get(key);
        if (have != null) {
            return have;
        }
        ArrayList<String> out = new ArrayList<String>();
        StringBuilder line = new StringBuilder();
        for (String word : s.split(" ")) {
            String next;
            String string = next = line.isEmpty() ? word : String.valueOf(line) + " " + word;
            if (!line.isEmpty() && LauncherScreen.tw(next, size, false) > width) {
                out.add(line.toString());
                line = new StringBuilder(word);
                continue;
            }
            line = new StringBuilder(next);
        }
        if (!line.isEmpty()) {
            out.add(line.toString());
        }
        if (this.wrapped.size() > 200) {
            this.wrapped.clear();
        }
        this.wrapped.put(key, out);
        return out;
    }

    private void tile(GuiGraphicsExtractor ctx, Tile t, float x, float y, float w, float h, int mx, int my, float realY) {
        boolean match = this.matches(t);
        boolean over = match && (float)mx >= x && (float)mx < x + w && (float)my >= realY && (float)my < realY + h;
        boolean first = !this.query.isBlank() && t == this.firstMatch();
        boolean off = LauncherScreen.off(t) || LauncherScreen.locked(t);
        boolean hid = LauncherScreen.hidden(t);
        if (over) {
            this.hoverSub = LauncherScreen.locked(t) ? LauncherScreen.lockedNote() : (hid ? Lang.t("Ausgeblendet und aus \u00b7 Rechtsklick blendet ein und schaltet an", "Hidden and off \u00b7 right click shows it and turns it on") : t.sub() + (off ? Lang.t(" \u00b7 aus", " \u00b7 off") : ""));
        }
        float ho = this.hov("tile_" + t.id(), over || first);
        LauncherGfx.round(ctx, x, y, w, h, 6.0f, !match ? 0x8FFFFFF : LauncherScreen.mix(0x10FFFFFF, 0x1EFFFFFF, ho));
        LauncherGfx.ring(ctx, x, y, w, h, 6.0f, LauncherScreen.mix(0x12FFFFFF, -1719799610, ho));
        if (!match) {
            return;
        }
        this.iconBox(ctx, t, x + 5.0f, y + 5.0f, h - 10.0f, false);
        float tx = x + 27.6f;
        float room = w - 27.6f - 16.0f;
        this.t(ctx, LauncherGfx.fit(t.name(), 7.0f, true, room), tx, y + 7.65f - 3.5f, hid ? -5721426 : (off ? -4666172 : -1116943), 7.0f, true);
        Object sub = t.sub();
        if (t.page().equals("fwm.tab.messages") && Messages.unreadTotal() > 0) {
            sub = Messages.unreadTotal() + Lang.t(" ungelesen", " unread");
        }
        if (t.page().equals("fwm.tab.sellmacro") && t.tab() < 0 && FwmConfig.INSTANCE.enabled) {
            sub = Lang.t("L\u00e4uft gerade", "Running now");
        }
        if (t.id().equals("fwm.tab.skylyrics") && SkyLyrics.active()) {
            MediaWatcher.Track song = MediaWatcher.track();
            Object object = song == null ? Lang.t("Kein Song erkannt", "No song detected") : (sub = "\u266a " + song.title() + (MediaWatcher.isPlaying() ? "" : Lang.t(" \u00b7 Pause", " \u00b7 paused")));
        }
        if (t.id().equals("fwm.tab.skylyrics") && !MediaWatcher.supported()) {
            sub = Lang.t("Nur unter Windows", "Windows only");
        }
        if (LauncherScreen.locked(t)) {
            sub = LauncherScreen.lockedNote();
        }
        if (hid) {
            sub = Lang.t("Ausgeblendet", "Hidden");
        }
        this.t(ctx, LauncherGfx.fit((String)sub, 6.2f, false, room), tx, y + 18.75f - 3.5f, hid ? -7563630 : (off ? -9534853 : -6902107), 6.2f, false);
        if (LauncherScreen.locked(t)) {
            this.hits.add(new Hit((int)x, (int)realY, (int)w, (int)h, () -> {}, null));
            return;
        }
        this.heart(ctx, t, x + w - 13.4f, y + h / 2.0f - 4.0f, mx, my, ho);
        this.hits.add(new Hit((int)x, (int)realY, (int)w, (int)h, () -> this.open(t), () -> LauncherScreen.toggleHidden(t)));
    }

    private void columns(GuiGraphicsExtractor ctx, int mx, int my, int top) {
        int g;
        this.animate();
        ArrayList<Integer> shown = new ArrayList<Integer>();
        Group[] groups = this.columnGroups();
        for (g = 0; g < groups.length; ++g) {
            if (LauncherScreen.visible(groups[g].tiles()).isEmpty()) continue;
            shown.add(g);
        }
        for (g = 0; g < this.colBox.length; ++g) {
            this.colBox[g] = null;
        }
        if (shown.isEmpty()) {
            return;
        }
        float bottom = this.vh() - 28;
        float cw = (float)(this.vw() - 24 - (shown.size() - 1) * 7) / (float)shown.size();
        float x = 12.0f;
        Iterator iterator = shown.iterator();
        while (iterator.hasNext()) {
            int g2 = (Integer)iterator.next();
            List<Tile> tiles = LauncherScreen.visible(groups[g2].tiles());
            float h = bottom - (float)top;
            this.panel(ctx, x, top, cw, h, groups[g2], false);
            int viewTop = top + 21;
            int fullH = (int)(h - 27.0f);
            boolean overflow = tiles.size() * 31 - 4 > fullH;
            int viewH = overflow ? (fullH - 18 + 4) / 31 * 31 - 4 : fullH;
            this.colBox[g2] = new int[]{(int)x, viewTop, (int)cw, viewH};
            int max = Math.max(0, tiles.size() * 31 - 4 - viewH);
            this.colTarget[g2] = Math.max(0.0f, Math.min((float)max, this.colTarget[g2]));
            float off = Math.min(this.colScroll[g2], (float)max);
            ctx.enableScissor((int)x + 1, viewTop - 1, (int)(x + cw) - 1, viewTop + viewH + 1);
            Matrix3x2fStack m = ctx.pose();
            m.pushMatrix();
            m.translate(0.0f, -off);
            int visibleMy = my >= viewTop && my < viewTop + viewH ? my : -999;
            for (int i = 0; i < tiles.size(); ++i) {
                int ty = viewTop + i * 31;
                float real = (float)ty - off;
                if (real + 27.0f < (float)viewTop || real > (float)(viewTop + viewH)) continue;
                int before = this.hits.size();
                this.tile(ctx, tiles.get(i), x + 7.4f, ty, cw - 14.8f, 27.0f, mx, (int)((float)visibleMy + off), ty);
                for (int k = before; k < this.hits.size(); ++k) {
                    Hit hb = this.hits.get(k);
                    int y0 = Math.max(Math.round((float)hb.y() - off), viewTop);
                    int y1 = Math.min(Math.round((float)hb.y() - off) + hb.h(), viewTop + viewH);
                    this.hits.set(k, y1 > y0 ? new Hit(hb.x(), y0, hb.w(), y1 - y0, hb.left(), hb.right()) : new Hit(0, 0, 0, 0, null, null));
                }
            }
            m.popMatrix();
            ctx.disableScissor();
            if (max > 0) {
                int below = (int)Math.ceil(((float)max - this.colScroll[g2]) / 31.0f);
                float bx = x + 7.4f;
                float bwid = cw - 14.8f;
                float by = viewTop + viewH + 4;
                boolean overBar = (float)mx >= bx && (float)mx < bx + bwid && (float)my >= by && (float)my < by + 14.0f;
                float hb = this.hov("more_" + g2, overBar);
                LauncherGfx.round(ctx, bx, by, bwid, 14.0f, 5.0f, LauncherScreen.mix(0x12FFFFFF, 0x24FFFFFF, hb));
                String label = below > 0 ? below + Lang.t(" weitere", " more") : Lang.t("nach oben", "back up");
                float lw = LauncherScreen.tw(label, 7.0f, true) + 10.0f;
                float lx = bx + (bwid - lw) / 2.0f;
                Shapes.icon(ctx, below > 0 ? "chev_down" : "chev_up", lx, by + 3.0f, 8.0f, -8523578);
                this.t(ctx, label, lx + 10.0f, by + 7.0f - 3.5f, -8523578, 7.0f, true);
                int gg = g2;
                int jump = below > 0 ? 93 : -9999;
                this.hits.add(0, new Hit((int)bx, (int)by, (int)bwid, 14, () -> {
                    int n = gg;
                    this.colTarget[n] = this.colTarget[n] + (float)jump;
                }, null));
                float thumbH = Math.max(12.0f, (float)(viewH * viewH) / (float)(viewH + max));
                float thumbY = (float)viewTop + ((float)viewH - thumbH) * (this.colScroll[g2] / (float)max);
                LauncherGfx.round(ctx, x + cw - 3.6f, thumbY, 1.6f, thumbH, 0.8f, -2005012282);
            }
            x += cw + 7.0f;
        }
    }

    private void animate() {
        long now = System.currentTimeMillis();
        float dt = this.lastFrame == 0L ? 0.0f : Math.min(0.1f, (float)(now - this.lastFrame) / 1000.0f);
        this.lastFrame = now;
        float ease = V2.INSTANCE.animations == 0 ? 1.0f : 1.0f - (float)Math.exp(-dt * 14.0f);
        for (int g = 0; g < this.colScroll.length; ++g) {
            int n = g;
            this.colScroll[n] = this.colScroll[n] + (this.colTarget[g] - this.colScroll[g]) * ease;
            if (!(Math.abs(this.colTarget[g] - this.colScroll[g]) < 0.25f)) continue;
            this.colScroll[g] = this.colTarget[g];
        }
    }

    private void singleplayerNote(GuiGraphicsExtractor ctx, int mx, int my) {
        String a = Lang.t("Im Singleplayer ist die FWM Mod aus.", "The FWM Mod is off in singleplayer.");
        String b = Lang.t("Einschalten unter Einstellungen \u203a Datenschutz", "Switch it on in Settings \u203a Privacy");
        float cy = (float)this.vh() / 2.0f - 16.0f;
        this.t(ctx, a, ((float)this.vw() - LauncherScreen.tw(a, 7.7f, true)) / 2.0f, cy, -1116943, 7.7f, true);
        float bw = LauncherScreen.tw(b, 7.0f, false) + 18.0f;
        float bx = ((float)this.vw() - bw) / 2.0f;
        boolean over = (float)mx >= bx && (float)mx < bx + bw && (float)my >= cy + 14.0f && (float)my < cy + 30.0f;
        float h = this.hov("sp", over);
        LauncherGfx.round(ctx, bx, cy + 14.0f, bw, 16.0f, 6.0f, LauncherScreen.mix(578678982, 1149104326, h));
        this.t(ctx, b, bx + 9.0f, cy + 22.0f - 3.5f, LauncherScreen.mix(-8523578, -1116943, h), 7.0f, false);
        this.hits.add(new Hit((int)bx, (int)cy + 14, (int)bw, 16, () -> this.openPage("fwm.tab.privacy"), null));
    }

    private float chip(GuiGraphicsExtractor ctx, float x, float fy, String icon, String label, int mx, int my, Runnable click) {
        float w = LauncherScreen.tw(label, 6.6f, false) + 22.0f;
        boolean over = (float)mx >= x && (float)mx < x + w && (float)my >= fy && (float)my < fy + 14.5f;
        float h = this.hov("chip_" + label, over);
        LauncherGfx.round(ctx, x, fy, w, 14.5f, 5.0f, LauncherScreen.mix(0x12FFFFFF, 0x24FFFFFF, h));
        LauncherGfx.ring(ctx, x, fy, w, 14.5f, 5.0f, LauncherScreen.mix(0x1AFFFFFF, 1434316998, h));
        Shapes.icon(ctx, icon, x + 5.5f, fy + 3.25f, 8.0f, LauncherScreen.mix(-4666172, -1116943, h));
        this.t(ctx, label, x + 16.0f, fy + 7.25f - 3.5f, LauncherScreen.mix(-4666172, -1116943, h), 6.6f, false);
        this.hits.add(new Hit((int)x, (int)fy, (int)w, 15, click, null));
        return w;
    }

    private void footer(GuiGraphicsExtractor ctx, int mx, int my) {
        float fy = this.vh() - 21;
        if (this.pageKey != null) {
            int flags = this.footer(ctx, 12, (int)fy + 4, this.vw() - 12);
            this.right(ctx, Lang.t("Esc zur\u00fcck zum Start", "Esc back to start"), flags - 10, (int)fy + 4, -6902107);
            return;
        }
        float x = 12.0f;
        for (String[] f : FOOT) {
            String label = Lang.t(f[0], f[1]);
            String icon = f[0].equals("Tasten") ? "keys" : (f[0].equals("HUD") ? "hud" : "shield");
            int tab = Integer.parseInt(f[3]);
            x += this.chip(ctx, x, fy, icon, label, mx, my, () -> this.openPage(f[2], tab)) + 5.0f;
        }
        x += this.chip(ctx, x, fy, "help", "Tutorial", mx, my, FwmTutorial::start) + 5.0f;
        if (IntroScreen.available()) {
            x += this.chip(ctx, x, fy, "play", "Intro", mx, my, () -> this.minecraft.gui.setScreen((Screen)new IntroScreen(this, IntroScreen.forMenu()))) + 9.0f;
        }
        String label = Lang.t("Ausgeblendete", "Hidden");
        float lw = LauncherScreen.tw(label, 6.6f, false);
        float sw = lw + 7.0f + 22.0f;
        boolean on = V2.INSTANCE.showHidden;
        boolean overSw = (float)mx >= x && (float)mx < x + sw && (float)my >= fy && (float)my < fy + 14.5f;
        this.t(ctx, label, x, fy + 7.25f - 3.5f, overSw ? -1116943 : -4666172, 6.6f, false);
        LauncherGfx.toggle(ctx, x + lw + 6.0f, fy + 1.25f, this.hov("hidden_sw", on), -8523578, -12957119);
        this.hits.add(new Hit((int)x, (int)fy, (int)sw, 15, () -> {
            V2.INSTANCE.showHidden = !V2.INSTANCE.showHidden;
            V2.save();
        }, null));
        x += sw + 10.0f;
        int flags = this.flags(ctx, (int)fy + 4, this.vw() - 12);
        String by = "by ";
        String fwm = "Friends With Money";
        float byX = (float)(flags - 10) - LauncherScreen.tw(by, 6.6f, false) - LauncherScreen.tw(fwm, 6.6f, true);
        this.t(ctx, by, byX, fy + 7.25f - 3.5f, -6902107, 6.6f, false);
        this.t(ctx, fwm, byX + LauncherScreen.tw(by, 6.6f, false), fy + 7.25f - 3.5f, -673493, 6.6f, true);
        Object note = this.hoverSub;
        if (note == null) {
            int hidden = 0;
            ArrayList<Group> every = new ArrayList<Group>(List.of(STREAMER, FARMING));
            every.addAll(List.of(COLUMNS));
            for (Group g : every) {
                for (Tile t : g.tiles()) {
                    if (!LauncherScreen.hidden(t) || !Features.allowedHere(LauncherScreen.page(t))) continue;
                    ++hidden;
                }
            }
            note = Lang.t("Rechtsklick = ausblenden + aus \u00b7 Herz = Favorit", "Right click = hide + off \u00b7 heart = favourite") + (hidden > 0 ? Lang.t(" \u00b7 " + hidden + " ausgeblendet", " \u00b7 " + hidden + " hidden") : "");
        }
        this.t(ctx, LauncherGfx.fit((String)note, 6.6f, false, byX - 12.0f - x), x, fy + 7.25f - 3.5f, this.hoverSub != null ? -4666172 : -6902107, 6.6f, false);
    }

    @Override
    protected String crumbGroup() {
        if (this.pageKey == null) {
            return null;
        }
        for (Group g : COLUMNS) {
            if (!g.icon().equals("group.pvp")) continue;
            for (Tile t : g.tiles()) {
                if (!t.page().equals(this.pageKey) || t.id().equals("pvplyrics")) continue;
                return g.name();
            }
        }
        return null;
    }

    @Override
    protected boolean designClicked(double mx, double my, int button) {
        if (this.pageKey != null) {
            return false;
        }
        for (Hit h : this.hits) {
            Runnable r;
            if (h.w() <= 0 || mx < (double)h.x() || mx >= (double)(h.x() + h.w()) || my < (double)h.y() || my >= (double)(h.y() + h.h())) continue;
            Runnable runnable = button == 0 ? h.left() : (r = button == 1 ? h.right() : null);
            if (r == null) {
                if (button == 1) continue;
                return false;
            }
            r.run();
            return true;
        }
        return false;
    }

    @Override
    protected void openPage(String key, int tab) {
        if (key.equals("fwm.tab.privacy")) {
            super.openPage("fwm.tab.settings", 4);
            return;
        }
        super.openPage(key, tab);
    }

    @Override
    protected boolean designScrolled(double mx, double my, double vertical) {
        if (this.pageKey != null || vertical == 0.0) {
            return false;
        }
        for (int g = 0; g < this.colBox.length; ++g) {
            int[] b = this.colBox[g];
            if (b == null || mx < (double)b[0] || mx >= (double)(b[0] + b[2]) || my < (double)(b[1] - 22) || my >= (double)(b[1] + b[3] + 6)) continue;
            int n = g;
            this.colTarget[n] = this.colTarget[n] - (float)Math.signum(vertical) * 31.0f;
            return true;
        }
        return false;
    }

    @Override
    protected void leavePage() {
        this.closePage();
    }

    @Override
    protected boolean escape() {
        if (super.escape()) {
            return true;
        }
        if (this.pageKey != null) {
            this.closePage();
            return true;
        }
        if (!this.query.isEmpty()) {
            this.query = "";
            return true;
        }
        return false;
    }

    @Override
    protected boolean designTyping() {
        return !this.query.isEmpty();
    }

    @Override
    protected boolean designKey(KeyEvent input) {
        if (this.pageKey != null) {
            return false;
        }
        int key = input.key();
        if (key == 259) {
            if (!this.query.isEmpty()) {
                this.query = this.query.substring(0, this.query.length() - 1);
            }
            return true;
        }
        if (key == 257 || key == 335) {
            Tile first = this.firstMatch();
            if (first != null) {
                this.click();
                this.open(first);
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean designChar(CharacterEvent input) {
        if (this.pageKey != null) {
            return false;
        }
        int cp = input.codepoint();
        if (cp < 32 || this.query.length() >= 30) {
            return false;
        }
        if (this.query.isEmpty() && (cp == 32 || cp == 47)) {
            return true;
        }
        this.query = this.query + new String(Character.toChars(cp));
        return true;
    }

    public record Group(String de, String en, String icon, Tile[] tiles) {
        String name() {
            return Lang.t(this.de, this.en);
        }
    }

    public record Tile(String id, String page, int tab, String de, String en, String subDe, String subEn, String icon) {
        String name() {
            return Lang.t(this.de, this.en);
        }

        String sub() {
            return Lang.t(this.subDe, this.subEn);
        }

        static Tile of(String page, String de, String en, String subDe, String subEn) {
            return new Tile(page, page, -1, de, en, subDe, subEn, page);
        }
    }

    private record Hit(int x, int y, int w, int h, Runnable left, Runnable right) {
    }
}

