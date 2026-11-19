/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.InputConstants$Key
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  com.mojang.blaze3d.platform.Window
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.core.Holder
 *  net.minecraft.network.chat.Component
 *  net.minecraft.sounds.SoundEvents
 *  org.joml.Matrix3x2fStack
 */
package net.friendswithmoney.fwm.ui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import net.friendswithmoney.fwm.FwmClient;
import net.friendswithmoney.fwm.antileak.AntiLeak;
import net.friendswithmoney.fwm.config.ConfigManager;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.input.Keybinds;
import net.friendswithmoney.fwm.net.Giveaways;
import net.friendswithmoney.fwm.net.Members;
import net.friendswithmoney.fwm.net.ModNews;
import net.friendswithmoney.fwm.seventv.EmotePicker;
import net.friendswithmoney.fwm.ui.FwmGfx;
import net.friendswithmoney.fwm.ui.FwmTheme;
import net.friendswithmoney.fwm.ui.FwmTutorial;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.LauncherGfx;
import net.friendswithmoney.fwm.ui.LauncherScreen;
import net.friendswithmoney.fwm.ui.SharpIcons;
import net.friendswithmoney.fwm.ui.gfx.FwmFont;
import net.friendswithmoney.fwm.ui.gfx.Shapes;
import net.friendswithmoney.fwm.ui.panel.AdBlockPanel;
import net.friendswithmoney.fwm.ui.panel.AntiLeakPanel;
import net.friendswithmoney.fwm.ui.panel.ArmorTrimPanel;
import net.friendswithmoney.fwm.ui.panel.ArmorWarnPanel;
import net.friendswithmoney.fwm.ui.panel.AuctionPanel;
import net.friendswithmoney.fwm.ui.panel.AuctionStatsPanel;
import net.friendswithmoney.fwm.ui.panel.AutoTpaPanel;
import net.friendswithmoney.fwm.ui.panel.CalcPanel;
import net.friendswithmoney.fwm.ui.panel.ColorCodePanel;
import net.friendswithmoney.fwm.ui.panel.ColorsPanel;
import net.friendswithmoney.fwm.ui.panel.CrosshairPanel;
import net.friendswithmoney.fwm.ui.panel.DonationPanel;
import net.friendswithmoney.fwm.ui.panel.EmotesPanel;
import net.friendswithmoney.fwm.ui.panel.ExtrasPanel;
import net.friendswithmoney.fwm.ui.panel.FightKitPanel;
import net.friendswithmoney.fwm.ui.panel.GiveawayPanel;
import net.friendswithmoney.fwm.ui.panel.KeysPanel;
import net.friendswithmoney.fwm.ui.panel.MapartPanel;
import net.friendswithmoney.fwm.ui.panel.MarketPanel;
import net.friendswithmoney.fwm.ui.panel.MembersPanel;
import net.friendswithmoney.fwm.ui.panel.MessagesPanel;
import net.friendswithmoney.fwm.ui.panel.ModGiveawayPanel;
import net.friendswithmoney.fwm.ui.panel.MoneyPanels;
import net.friendswithmoney.fwm.ui.panel.NameListPanel;
import net.friendswithmoney.fwm.ui.panel.NewsPanel;
import net.friendswithmoney.fwm.ui.panel.OverlayPanel;
import net.friendswithmoney.fwm.ui.panel.Panel;
import net.friendswithmoney.fwm.ui.panel.PanelHost;
import net.friendswithmoney.fwm.ui.panel.PingListPanel;
import net.friendswithmoney.fwm.ui.panel.PrivacyPanel;
import net.friendswithmoney.fwm.ui.panel.PvpFxPanel;
import net.friendswithmoney.fwm.ui.panel.RtpInfoPanel;
import net.friendswithmoney.fwm.ui.panel.SecondChatPanel;
import net.friendswithmoney.fwm.ui.panel.SellStatsPanel;
import net.friendswithmoney.fwm.ui.panel.SellmacroPanel;
import net.friendswithmoney.fwm.ui.panel.SettingsPanel;
import net.friendswithmoney.fwm.ui.panel.SkyLyricsPanels;
import net.friendswithmoney.fwm.ui.panel.SkyPanel;
import net.friendswithmoney.fwm.ui.panel.SoundsPanel;
import net.friendswithmoney.fwm.ui.panel.SpawnerAdPanel;
import net.friendswithmoney.fwm.ui.panel.SpawnerHudPanel;
import net.friendswithmoney.fwm.ui.panel.SpawnerMacroPanel;
import net.friendswithmoney.fwm.ui.panel.SpawnerPanel;
import net.friendswithmoney.fwm.ui.panel.StudioBase;
import net.friendswithmoney.fwm.ui.panel.TabbedPanel;
import net.friendswithmoney.fwm.ui.panel.TestsPanel;
import net.friendswithmoney.fwm.ui.panel.TexturesPanel;
import net.friendswithmoney.fwm.ui.panel.TicketsPanel;
import net.friendswithmoney.fwm.ui.panel.TodoPanel;
import net.friendswithmoney.fwm.ui.panel.ToolGuardPanel;
import net.friendswithmoney.fwm.ui.panel.TotemPanel;
import net.friendswithmoney.fwm.ui.panel.TotemStudioPanel;
import net.friendswithmoney.fwm.ui.panel.TpaGuardPanel;
import net.friendswithmoney.fwm.ui.panel.WaypointsPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.joml.Matrix3x2fStack;

public abstract class DesignHost
extends Screen
implements PanelHost {
    protected static final int CARD = 1544960042;
    protected static final int CARD_EDGE = 0x1FFFFFFF;
    protected static final int CARD_HI = -14923216;
    protected static final int LINE = 0x1AFFFFFF;
    protected static final int WHITE = -1;
    protected static final int SOFT = -5846860;
    protected static final int DIMTXT = -9464447;
    protected static final int GREEN = -12136081;
    protected static final int BRIGHT = -10688364;
    protected static final int GREEN_DARK = -16045545;
    protected static final int RED = -1017230;
    protected static final int OFF_TRACK = -12957119;
    private static final int SETTINGS_KEYS = 3;
    protected final Screen parent;
    protected String pageKey;
    protected Panel base;
    protected final Deque<Panel> subs = new ArrayDeque<Panel>();
    private int frameX;
    private int frameY;
    private int frameW;
    private int frameH;
    private int[] backBox = new int[4];
    private int[] startBox = new int[4];
    protected int crumbX = -1;
    private int[] switchBox = new int[4];
    private int[] miniBox = new int[4];
    private int[] cogBox = new int[4];
    private int[] nameBox = new int[4];
    private int[] pageKeyBox = new int[4];
    private boolean capturingPageKey;
    private long pageKeyRejectedAt;
    private int[] infoBox = new int[4];
    private int lastMx = -1;
    private int lastMy = -1;
    private int[] flagBoxDe = new int[4];
    private int[] flagBoxEn = new int[4];
    private boolean menuKeyArmed = !DesignHost.menuKeyHeld();
    private int appliedScalePercent = -1;
    private boolean mouseDown;
    private long shownAt = System.currentTimeMillis();
    private static final int TITLE_H = 22;
    private static final float PAGE_SCALE = 1.3f;

    protected String crumbGroup() {
        return null;
    }

    protected DesignHost(Screen parent) {
        super(Minecraft.getInstance(), FwmFont.ui(), (Component)Component.literal((String)"FWM"));
        this.parent = parent;
    }

    protected abstract int vw();

    protected abstract int vh();

    protected float scale() {
        int width = this.width - FwmTutorial.reservedSide(this.width);
        double gui = Minecraft.getInstance().getWindow().getGuiScale();
        float crisp = this.crisp(width, this.height, gui);
        int percent = this.appliedPercent();
        if (percent == 100) {
            return crisp;
        }
        float fits = Math.min((float)width / (float)this.vw(), (float)this.height / (float)this.vh());
        return Math.min(fits, crisp * (float)percent / 100.0f);
    }

    protected float crisp(int width, int height, double gui) {
        return DesignHost.crispScale(width, height, this.vw(), this.vh(), gui);
    }

    protected static float crispScale(int width, int height, int vw, int vh, double gui) {
        float refW = Math.max(460, vw);
        float refH = Math.max(290, vh);
        float fit = Math.min((float)width / refW, (float)height / refH);
        int most = Math.max(1, (int)Math.floor(gui * (double)Math.min((float)width / (float)vw, (float)height / (float)vh)));
        int px = Math.max(1, (int)Math.floor(gui * (double)fit * 0.86));
        if (px < 2 && most >= 2) {
            px = 2;
        }
        return (float)((double)Math.min(px, most) / gui);
    }

    private int appliedPercent() {
        if (this.appliedScalePercent < 0 || !this.mouseDown) {
            this.appliedScalePercent = DesignHost.configuredPercent();
        }
        return this.appliedScalePercent;
    }

    private static int configuredPercent() {
        return FwmConfig.INSTANCE.menuScale <= 0 ? 100 : FwmConfig.INSTANCE.menuScale;
    }

    protected float offX() {
        int reserved = FwmTutorial.reservedSide(this.width);
        return (float)(FwmTutorial.onLeft() ? reserved : 0) + (float)Math.floor(((float)(this.width - reserved) - (float)this.vw() * this.scale()) / 2.0f);
    }

    protected float offY() {
        return (float)Math.floor(((float)this.height - (float)this.vh() * this.scale()) / 2.0f);
    }

    protected double vx(double x) {
        return (x - (double)this.offX()) / (double)this.scale();
    }

    protected double vy(double y) {
        return (y - (double)this.offY()) / (double)this.scale();
    }

    protected static String feature(String key) {
        return key.equals("fwm.tab.antileak") ? "fwm.feature.antileak" : key;
    }

    protected static boolean switchable(String key) {
        return Features.canSwitch(DesignHost.feature(key));
    }

    protected static boolean on(String key) {
        return Features.isOn(DesignHost.feature(key));
    }

    protected Panel create(String key) {
        return switch (key) {
            case "fwm.tab.sellmacro" -> new TabbedPanel("fwm.tab.sellmacro", List.of(new TabbedPanel.Tab("fwm.tab.control", () -> new SellmacroPanel(this)), new TabbedPanel.Tab("fwm.tab.statsShort", () -> new SellStatsPanel(this)), new TabbedPanel.Tab("fwm.tab.tests", () -> new TestsPanel(this))));
            case "fwm.tab.messages" -> new MessagesPanel(this);
            case "fwm.tab.toolguard" -> new ToolGuardPanel();
            case "fwm.tab.secondchat" -> new SecondChatPanel(this);
            case "fwm.tab.auction" -> {
                ArrayList<TabbedPanel.Tab> tabs = new ArrayList<TabbedPanel.Tab>();
                tabs.add(new TabbedPanel.Tab("fwm.tab.auction", () -> new AuctionPanel(this)));
                if (Features.allowedHere("fwm.tab.giveaway")) {
                    tabs.add(new TabbedPanel.Tab("fwm.tab.giveaway", () -> new GiveawayPanel(this)));
                }
                tabs.add(new TabbedPanel.Tab("fwm.tab.auctionstats", () -> new AuctionStatsPanel(this)));
                yield new TabbedPanel("fwm.tab.streamertools", tabs);
            }
            case "fwm.tab.colorcodes" -> new ColorCodePanel(this);
            case "fwm.tab.market" -> new MarketPanel(this);
            case "fwm.tab.rtp" -> new RtpInfoPanel(this);
            case "fwm.tab.extras" -> new ExtrasPanel();
            case "fwm.tab.antileak" -> new AntiLeakPanel();
            case "fwm.tab.spawner" -> new TabbedPanel("fwm.tab.spawner", List.of(new TabbedPanel.Tab("fwm.spawner.tabPrices", () -> new SpawnerPanel(this)), new TabbedPanel.Tab("fwm.tab.spawnerhud", SpawnerHudPanel::new)));
            case "fwm.tab.kommt" -> new TabbedPanel("fwm.tab.kommt", List.of(new TabbedPanel.Tab("fwm.kommt.pings", () -> new PingListPanel(this)), new TabbedPanel.Tab("fwm.kommt.ahMessages", MoneyPanels.Messages::new)));
            case "fwm.tab.tickets" -> new TicketsPanel(this);
            case "fwm.tab.members" -> new MembersPanel(this);
            case "fwm.tab.fightkit" -> new FightKitPanel(this);
            case "fwm.tab.waypoints" -> new WaypointsPanel();
            case "fwm.tab.emotes" -> new EmotesPanel();
            case "fwm.tab.autotpa" -> new TabbedPanel("fwm.tab.autotpa", List.of(new TabbedPanel.Tab("fwm.autotpa.tabSettings", AutoTpaPanel::new), new TabbedPanel.Tab("fwm.autotpa.tabNames", () -> new NameListPanel(this, "fwm.autotpa.tabNames", "fwm.autotpa.none", "fwm.autotpa.add", () -> FwmConfig.INSTANCE.autoTpaNames))));
            case "fwm.tab.spawnermacro" -> new SpawnerMacroPanel(this);
            case "fwm.tab.money" -> new TabbedPanel("fwm.tab.money", List.of(new TabbedPanel.Tab("fwm.money.today", MoneyPanels.Today::new), new TabbedPanel.Tab("fwm.money.week", MoneyPanels.Week::new), new TabbedPanel.Tab("fwm.money.month", MoneyPanels.Month::new), new TabbedPanel.Tab("fwm.money.history", () -> new MoneyPanels.HistoryList(this)), new TabbedPanel.Tab("fwm.money.settings", MoneyPanels.Settings::new)));
            case "fwm.tab.textures" -> new TexturesPanel(this);
            case "fwm.tab.mapart" -> new MapartPanel();
            case "fwm.tab.sky" -> new SkyPanel();
            case "fwm.tab.skylyrics" -> new TabbedPanel("fwm.tab.skylyrics", List.of(new TabbedPanel.Tab("fwm.skylyrics.overview", SkyLyricsPanels.Overview::new), new TabbedPanel.Tab("fwm.skylyrics.style", SkyLyricsPanels.Looks::new), new TabbedPanel.Tab("fwm.skylyrics.effects", SkyLyricsPanels.Effects::new), new TabbedPanel.Tab("fwm.skylyrics.world", SkyLyricsPanels.Pvp::new), new TabbedPanel.Tab("fwm.skylyrics.privacy", SkyLyricsPanels.Privacy::new)));
            case "fwm.tab.totemstudio" -> new TotemStudioPanel(this, false);
            case "fwm.tab.armortrim" -> new ArmorTrimPanel();
            case "fwm.tab.hitcolor" -> new PvpFxPanel(0);
            case "fwm.tab.totempop" -> new PvpFxPanel(1);
            case "fwm.tab.totemhold" -> new PvpFxPanel(2);
            case "fwm.tab.adblock" -> new AdBlockPanel(this);
            case "fwm.tab.sellstats" -> new SellStatsPanel(this);
            case "fwm.tab.totem" -> new TotemPanel();
            case "fwm.tab.crosshair" -> new CrosshairPanel();
            case "fwm.tab.sounds" -> new SoundsPanel(this);
            case "fwm.tab.calc" -> new CalcPanel();
            case "fwm.tab.armorwarn" -> new ArmorWarnPanel();
            case "fwm.tab.tpaguard" -> new TpaGuardPanel();
            case "fwm.tab.todos" -> new TodoPanel(this);
            case "fwm.tab.donations" -> DonationPanel.create(this);
            case "fwm.tab.news" -> {
                ArrayList<TabbedPanel.Tab> tabs = new ArrayList<TabbedPanel.Tab>();
                if (Features.allowedHere("fwm.news.giveaway")) {
                    tabs.add(new TabbedPanel.Tab("fwm.news.giveaway", ModGiveawayPanel::new));
                }
                tabs.add(new TabbedPanel.Tab("fwm.news.infos", () -> new NewsPanel(ModNews.Kind.INFOS)));
                tabs.add(new TabbedPanel.Tab("fwm.news.bugs", () -> new NewsPanel(ModNews.Kind.BUGS)));
                tabs.add(new TabbedPanel.Tab("fwm.news.fixes", () -> new NewsPanel(ModNews.Kind.FIXES)));
                yield new TabbedPanel("fwm.tab.news", tabs);
            }
            default -> new TabbedPanel("fwm.tab.settings", List.of(new TabbedPanel.Tab("fwm.tab.general", SettingsPanel::new), new TabbedPanel.Tab("fwm.tab.gui", ColorsPanel::new), new TabbedPanel.Tab("fwm.tab.overlay", OverlayPanel::new), new TabbedPanel.Tab("fwm.tab.keys", KeysPanel::new), new TabbedPanel.Tab("fwm.tab.privacy", PrivacyPanel::new)));
        };
    }

    protected void openPage(String key) {
        this.openPage(key, -1);
    }

    protected void openPage(String key, int tab) {
        Panel panel;
        if (key.equals("fwm.tab.giveaway")) {
            key = "fwm.tab.auction";
            tab = 1;
        }
        if (key.equals(this.pageKey) && tab < 0) {
            while (!this.subs.isEmpty()) {
                this.closeSub();
            }
            return;
        }
        this.closePage();
        this.pageKey = key;
        this.shownAt = System.currentTimeMillis();
        this.base = this.create(key);
        if (tab >= 0 && (panel = this.base) instanceof TabbedPanel) {
            TabbedPanel tabbed = (TabbedPanel)panel;
            tabbed.setTab(tab);
        }
        this.applyBounds();
    }

    protected void closePage() {
        if (this.emotePickerHere()) {
            EmotePicker.get().close();
        }
        while (!this.subs.isEmpty()) {
            this.closeSub();
        }
        if (this.base != null) {
            this.base.onHidden();
            this.shownAt = System.currentTimeMillis();
        }
        this.base = null;
        this.pageKey = null;
    }

    protected float shown() {
        if (V2.INSTANCE.animations == 0) {
            return 1.0f;
        }
        float t = Math.min(1.0f, (float)(System.currentTimeMillis() - this.shownAt) / 170.0f);
        return 1.0f - (1.0f - t) * (1.0f - t) * (1.0f - t);
    }

    public void extractBackground(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        if (V2.INSTANCE.menuBlur || this.minecraft == null || this.minecraft.level == null) {
            super.extractBackground(ctx, mouseX, mouseY, delta);
        }
    }

    protected Panel active() {
        return this.subs.isEmpty() ? this.base : this.subs.peek();
    }

    protected void setFrame(int x, int y, int w, int h) {
        if (x == this.frameX && y == this.frameY && w == this.frameW && h == this.frameH) {
            return;
        }
        this.frameX = x;
        this.frameY = y;
        this.frameW = w;
        this.frameH = h;
        this.applyBounds();
    }

    private void applyBounds() {
        if (this.base == null || this.frameW <= 0) {
            return;
        }
        int px = this.frameX;
        int py = this.frameY + 22 + 3;
        float s = this.pageScale();
        int pw = Math.round((float)this.frameW / s);
        int ph = Math.round((float)(this.frameH - 22 - 3) / s);
        this.base.setBounds(px, py, pw, ph);
        for (Panel panel : this.subs) {
            panel.setBounds(px, py, pw, ph);
        }
    }

    public float pageScale() {
        Panel page = this.active();
        if (page instanceof TexturesPanel) {
            return 1.0f;
        }
        if (page instanceof FightKitPanel || page instanceof MembersPanel) {
            return 1.2f;
        }
        return 1.3f;
    }

    private double pageX(double mx) {
        return (double)this.frameX + (mx - (double)this.frameX) / (double)this.pageScale();
    }

    private double pageY(double my) {
        int py = this.frameY + 22 + 3;
        return (double)py + (my - (double)py) / (double)this.pageScale();
    }

    private boolean pageLive() {
        if (!this.subs.isEmpty() || this.pageKey == null) {
            return true;
        }
        if (this.pageKey.equals("fwm.tab.antileak")) {
            return true;
        }
        return !DesignHost.switchable(this.pageKey) || DesignHost.on(this.pageKey);
    }

    protected void init() {
        super.init();
        if (this instanceof LauncherScreen && FwmTutorial.pending() && !FwmTutorial.active()) {
            FwmTutorial.start();
        }
    }

    void tourShow(String key, int tab) {
        Panel panel;
        if ("fwm.tab.overview".equals(key)) {
            this.closePage();
            return;
        }
        if (key.equals(this.pageKey) && tab >= 0 && (panel = this.base) instanceof TabbedPanel) {
            TabbedPanel tabbed = (TabbedPanel)panel;
            while (!this.subs.isEmpty()) {
                this.closeSub();
            }
            tabbed.setTab(tab);
            this.applyBounds();
            return;
        }
        this.openPage(key, tab);
    }

    void tourRelayout() {
    }

    @Override
    public void openSub(Panel panel) {
        this.subs.push(panel);
        this.applyBounds();
    }

    @Override
    public void closeSub() {
        if (this.subs.isEmpty()) {
            return;
        }
        this.subs.pop().onHidden();
        this.applyBounds();
    }

    @Override
    public void attach(AbstractWidget widget) {
        this.addWidget((GuiEventListener)widget);
    }

    @Override
    public void detach(AbstractWidget widget) {
        this.removeWidget((GuiEventListener)widget);
    }

    @Override
    public List<String> sidebarKeys() {
        return List.of();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        float shown;
        ctx.fill(0, 0, this.width, this.height, FwmTheme.OVERLAY);
        Matrix3x2fStack m = ctx.pose();
        m.pushMatrix();
        m.translate(this.offX(), this.offY());
        m.scale(this.scale(), this.scale());
        Shapes.alpha = shown = this.shown();
        if (shown < 1.0f) {
            m.translate(0.0f, (1.0f - shown) * 5.0f);
        }
        try {
            this.drawFrame(ctx);
            this.lastMx = (int)Math.floor(this.vx(mouseX));
            this.lastMy = (int)Math.floor(this.vy(mouseY));
            this.drawCanvas(ctx, this.lastMx, this.lastMy, delta);
        }
        finally {
            Shapes.alpha = 1.0f;
        }
        m.popMatrix();
        super.extractRenderState(ctx, mouseX, mouseY, delta);
        if (this.emotePickerHere()) {
            ctx.nextStratum();
            EmotePicker.get().render(ctx, mouseX, mouseY, delta, this.width, this.height);
        }
        if (FwmTutorial.active()) {
            FwmTutorial.dock(Math.round(this.offX()), Math.round(this.offX() + (float)this.vw() * this.scale()), Math.round(this.offY()), Math.round(this.offY() + (float)this.vh() * this.scale()));
            FwmTutorial.render(ctx, this.font, this.width, this.height, mouseX, mouseY);
        }
    }

    protected abstract void drawCanvas(GuiGraphicsExtractor var1, int var2, int var3, float var4);

    protected void drawFrame(GuiGraphicsExtractor ctx) {
        LauncherGfx.card(ctx, 0, 0, this.vw(), this.vh(), 8);
    }

    protected void text(GuiGraphicsExtractor ctx, String s, int x, int y, int color) {
        LauncherGfx.text(ctx, this.font, s, x, y, color);
    }

    protected void right(GuiGraphicsExtractor ctx, String s, int rightX, int y, int color) {
        this.text(ctx, s, rightX - this.font.width(s), y, color);
    }

    protected static boolean inside(int[] box, double x, double y) {
        return x >= (double)box[0] && x < (double)(box[0] + box[2]) && y >= (double)box[1] && y < (double)(box[1] + box[3]);
    }

    protected void brand(GuiGraphicsExtractor ctx, int x, int y, int rightX) {
        FwmTheme.logo(ctx, x, y, 20);
        this.text(ctx, "Friends With Money", x + 25, y + 5, -1);
        String name = FwmGfx.playerName();
        int cw = this.font.width(name) + 22;
        this.nameBox = new int[]{rightX - cw, y + 2, cw, 14};
        LauncherGfx.round(ctx, rightX - cw, y + 2, cw, 14, 7, 0x24FFFFFF);
        FwmGfx.playerHead(ctx, rightX - cw + 4, y + 5, 8);
        this.text(ctx, name, rightX - cw + 16, y + 5, -1);
        int ix = rightX - cw - 18;
        this.infoBox = new int[]{ix, y + 2, 14, 14};
        boolean hover = DesignHost.inside(this.infoBox, this.lastMx, this.lastMy);
        boolean open = "fwm.tab.news".equals(this.pageKey);
        if (hover) {
            LauncherGfx.ring(ctx, ix - 1, y + 1, 16, 16, 5, 1432152212);
        }
        LauncherGfx.round(ctx, ix, y + 2, 14, 14, 4, hover ? 1079830676 : (open ? 0x44FFFFFF : 0x24FFFFFF));
        LauncherGfx.ring(ctx, ix, y + 2, 14, 14, 4, hover ? -10688364 : (open ? -1 : 0x66FFFFFF));
        LauncherGfx.infoGlyph(ctx, ix, y + 2, hover || open ? -1 : -5846860);
        int giveaways = Giveaways.open();
        if (giveaways > 0) {
            String n = String.valueOf(Math.min(9, giveaways));
            LauncherGfx.round(ctx, ix + 9, y - 1, 8, 8, 4, -865972);
            ctx.text(this.font, (Component)Component.literal((String)n), ix + 13 - this.font.width(n) / 2, y, -15066598, false);
        }
    }

    protected int infoLeft() {
        return this.infoBox[0];
    }

    protected int footer(GuiGraphicsExtractor ctx, int x, int y, int rightX) {
        this.text(ctx, "FWM Mod v" + FwmClient.MOD_VERSION, x, y, -9464447);
        return this.flags(ctx, y, rightX);
    }

    protected int flags(GuiGraphicsExtractor ctx, int y, int rightX) {
        int fx = rightX - 12;
        this.flagBoxEn = new int[]{fx - 1, y - 1, 14, 10};
        FwmTheme.usFlag(ctx, fx, y, 12, 8, !Lang.isGerman());
        this.flagBoxDe = new int[]{(fx -= 16) - 1, y - 1, 14, 10};
        FwmTheme.germanFlag(ctx, fx, y, 12, 8, Lang.isGerman());
        return fx;
    }

    protected String title(String key) {
        return Lang.str(key, new Object[0]);
    }

    protected void drawPage(GuiGraphicsExtractor ctx, int mx, int my, float delta, String back) {
        String group;
        Panel page = this.active();
        if (page == null) {
            return;
        }
        int x = this.frameX;
        int y = this.frameY;
        int right = this.frameX + this.frameW;
        int tx = Math.max(x, this.crumbX);
        this.backBox = new int[4];
        this.startBox = new int[4];
        ArrayList<String> crumbs = new ArrayList<String>();
        if (back != null) {
            crumbs.add(back);
        }
        if ((group = this.crumbGroup()) != null) {
            crumbs.add(group);
        }
        if (!this.subs.isEmpty() && this.base != null) {
            crumbs.add(this.base.title().getString());
        }
        float cx = tx;
        float crumbY = (float)(y + 11) - 3.5f;
        for (int i = 0; i < crumbs.size(); ++i) {
            String c = (String)crumbs.get(i);
            float cw = LauncherGfx.width(c, 7.4f, false);
            int[] box = new int[]{(int)cx - 2, y + 3, (int)cw + 4, 16};
            boolean over = DesignHost.inside(box, mx, my);
            LauncherGfx.text(ctx, c, cx, crumbY, over ? -1 : -5846860, 7.4f, false);
            if (i == 0) {
                this.startBox = box;
            }
            if (i == crumbs.size() - 1) {
                this.backBox = box;
            }
            LauncherGfx.text(ctx, "\u203a", cx += cw + 4.0f, crumbY, 0x66FFFFFF, 7.4f, false);
            cx += LauncherGfx.width("\u203a", 7.4f, false) + 4.0f;
        }
        tx = (int)Math.ceil(cx);
        String heading = page.title().getString();
        int rx = right;
        this.switchBox = new int[4];
        this.miniBox = new int[4];
        this.cogBox = new int[4];
        if (this.subs.isEmpty() && this.pageKey != null && DesignHost.switchable(this.pageKey)) {
            boolean isOn = DesignHost.on(this.pageKey);
            String label = isOn ? Lang.t("An", "On") : Lang.t("Aus", "Off");
            int lw = this.font.width(label);
            this.switchBox = new int[]{rx - 26 - lw - 4, y + 4, 26 + lw + 4, 14};
            this.text(ctx, label, rx - 26 - lw - 2, y + 7, isOn ? -5846860 : -1017230);
            LauncherGfx.toggle(ctx, rx - 22, y + 5, isOn, -12136081, -12957119);
            rx = this.switchBox[0] - 8;
        }
        if (page.settingsCog()) {
            this.cogBox = new int[]{rx - 14, y + 4, 14, 14};
            boolean over = DesignHost.inside(this.cogBox, mx, my);
            LauncherGfx.round(ctx, rx - 14, y + 4, 14, 14, 7, over ? 0x33FFFFFF : 0x1AFFFFFF);
            SharpIcons.draw(ctx, "icon_cog", rx - 12, y + 6, 10, over ? -1 : -5846860);
            rx -= 20;
        }
        this.pageKeyBox = new int[4];
        if (this.subs.isEmpty() && this.pageKey != null) {
            boolean rejected;
            String bound = V2.INSTANCE.pageKeys.get(this.pageKey);
            boolean bl = rejected = System.currentTimeMillis() - this.pageKeyRejectedAt < 1500L;
            String label = this.capturingPageKey ? Lang.t("Taste dr\u00fccken \u2026", "Press a key \u2026") : (rejected ? Lang.t("schon belegt", "already used") : (bound != null && Keybinds.key(bound) != null ? Keybinds.key(bound).getDisplayName().getString() : Lang.t("Taste", "Key")));
            int lw = this.font.width(label) + 10;
            this.pageKeyBox = new int[]{rx - lw, y + 4, lw, 14};
            boolean over = DesignHost.inside(this.pageKeyBox, mx, my);
            LauncherGfx.round(ctx, rx - lw, y + 4, lw, 14, 4, this.capturingPageKey ? 1149104326 : (over ? 0x33FFFFFF : 0x1AFFFFFF));
            LauncherGfx.ring(ctx, rx - lw, y + 4, lw, 14, 4, this.capturingPageKey ? -8523578 : (rejected ? -1017230 : 0x44FFFFFF));
            this.text(ctx, label, rx - lw + 5, y + 7, rejected ? -1017230 : (bound != null || this.capturingPageKey ? -1 : -9464447));
            rx -= lw + 6;
        }
        if (page.headerSwitch() != null) {
            String label = Lang.str("fwm.header.minimap", new Object[0]);
            boolean isOn = Features.isOn(page.headerSwitch());
            int lw = this.font.width(label);
            this.miniBox = new int[]{rx - 22 - lw - 4, y + 4, 22 + lw + 4, 14};
            this.text(ctx, label, rx - 22 - lw - 3, y + 7, isOn ? -5846860 : -9464447);
            LauncherGfx.toggle(ctx, rx - 22, y + 5, isOn, -12136081, -12957119);
            rx = this.miniBox[0] - 8;
        }
        LauncherGfx.text(ctx, LauncherGfx.fit(heading, 8.0f, true, Math.max(10, rx - tx - 4)), tx, (float)(y + 11) - 3.5f, -1, 8.0f, true);
        if (!this.pageLive()) {
            String note = Lang.t("Dieses Feature ist ausgeschaltet.", "This feature is switched off.");
            String hint = Lang.t("Mit dem Schalter oben rechts wieder anschalten", "Switch it back on at the top right");
            int cy = this.frameY + this.frameH / 2;
            this.text(ctx, note, x + (this.frameW - this.font.width(note)) / 2, cy - 8, -1017230);
            this.text(ctx, hint, x + (this.frameW - this.font.width(hint)) / 2, cy + 4, -9464447);
            return;
        }
        float s = this.pageScale();
        if (s == 1.0f) {
            page.render(ctx, this.font, mx, my, delta);
            return;
        }
        int py = this.frameY + 22 + 3;
        Matrix3x2fStack m = ctx.pose();
        m.pushMatrix();
        m.translate((float)this.frameX, (float)py);
        m.scale(s, s);
        m.translate((float)(-this.frameX), (float)(-py));
        page.render(ctx, this.font, (int)Math.floor(this.pageX(mx)), (int)Math.floor(this.pageY(my)), delta);
        m.popMatrix();
    }

    protected static void chevron(GuiGraphicsExtractor ctx, int x, int y, int color) {
        ctx.fill(x + 2, y, x + 3, y + 1, color);
        ctx.fill(x + 1, y + 1, x + 2, y + 2, color);
        ctx.fill(x, y + 2, x + 1, y + 3, color);
        ctx.fill(x + 1, y + 3, x + 2, y + 4, color);
        ctx.fill(x + 2, y + 4, x + 3, y + 5, color);
    }

    protected void click() {
        if (FwmConfig.INSTANCE.menuSounds) {
            Minecraft.getInstance().getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((Holder)SoundEvents.UI_BUTTON_CLICK, (float)1.0f));
        }
    }

    protected abstract boolean designClicked(double var1, double var3, int var5);

    protected abstract void leavePage();

    private boolean emotePickerHere() {
        if (!(this.active() instanceof MessagesPanel)) {
            return false;
        }
        return EmotePicker.get().isOpen();
    }

    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double mx = this.vx(click.x());
        double my = this.vy(click.y());
        int button = click.button();
        this.mouseDown = true;
        if (this.emotePickerHere()) {
            EmotePicker picker = EmotePicker.get();
            if (picker.isInside(click.x(), click.y())) {
                picker.mouseClicked(click, doubled);
                return true;
            }
            if (!EmotePicker.toggledRecently()) {
                picker.close();
            }
        }
        if (FwmTutorial.mouseClicked(click.x(), click.y(), button)) {
            return true;
        }
        if (FwmTutorial.consumes(click.x(), this.width)) {
            return true;
        }
        if (button == 0) {
            TabbedPanel t;
            Panel panel;
            if (DesignHost.inside(this.flagBoxDe, mx, my) || DesignHost.inside(this.flagBoxEn, mx, my)) {
                Lang.set(DesignHost.inside(this.flagBoxDe, mx, my) ? "de" : "en");
                ConfigManager.save();
                this.applyBounds();
                this.click();
                return true;
            }
            if (DesignHost.inside(this.infoBox, mx, my)) {
                this.openPage("fwm.tab.news");
                this.click();
                return true;
            }
            if (this.subs.isEmpty() && (this.base instanceof SpawnerPanel || (panel = this.base) instanceof TabbedPanel && (t = (TabbedPanel)panel).shows(SpawnerPanel.class)) && DesignHost.inside(this.nameBox, mx, my) && Members.isMember(FwmGfx.playerName())) {
                this.openSub(new SpawnerAdPanel(this));
                this.click();
                return true;
            }
            if (this.active() != null) {
                if (DesignHost.inside(this.startBox, mx, my)) {
                    while (!this.subs.isEmpty()) {
                        this.closeSub();
                    }
                    this.leavePage();
                    this.click();
                    return true;
                }
                if (DesignHost.inside(this.backBox, mx, my)) {
                    if (!this.subs.isEmpty()) {
                        while (!this.subs.isEmpty()) {
                            this.closeSub();
                        }
                    } else {
                        this.leavePage();
                    }
                    this.click();
                    return true;
                }
                if (DesignHost.inside(this.switchBox, mx, my)) {
                    Features.toggle(DesignHost.feature(this.pageKey));
                    ConfigManager.save();
                    this.click();
                    return true;
                }
                if (DesignHost.inside(this.miniBox, mx, my)) {
                    Features.toggle(this.active().headerSwitch());
                    this.click();
                    return true;
                }
                if (DesignHost.inside(this.pageKeyBox, mx, my)) {
                    this.capturingPageKey = !this.capturingPageKey;
                    this.click();
                    return true;
                }
                if (DesignHost.inside(this.cogBox, mx, my)) {
                    this.openPage("fwm.tab.settings", 3);
                    this.click();
                    return true;
                }
            }
        }
        if (this.designClicked(mx, my, button)) {
            this.click();
            return true;
        }
        if (this.active() != null && this.pageLive() && this.active().mouseClicked(this.pageX(mx), this.pageY(my), button)) {
            this.click();
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
        if (this.active() != null && this.active().mouseDragged(this.pageX(this.vx(click.x())), this.pageY(this.vy(click.y())), click.button())) {
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    public boolean mouseReleased(MouseButtonEvent click) {
        if (this.emotePickerHere() && EmotePicker.get().mouseReleased(click.button())) {
            return true;
        }
        if (this.active() != null) {
            this.active().mouseReleased();
        }
        this.mouseDown = false;
        return super.mouseReleased(click);
    }

    protected boolean designScrolled(double mx, double my, double vertical) {
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        double my;
        if (this.emotePickerHere() && EmotePicker.get().mouseScrolled(mouseX, mouseY, vertical)) {
            return true;
        }
        double mx = this.vx(mouseX);
        if (this.designScrolled(mx, my = this.vy(mouseY), vertical)) {
            return true;
        }
        if (this.active() != null && this.active().mouseScrolled(this.pageX(mx), this.pageY(my), vertical)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    protected boolean designKey(KeyEvent input) {
        return false;
    }

    protected boolean designChar(CharacterEvent input) {
        return false;
    }

    protected boolean typingInField() {
        for (Object child : this.children()) {
            EditBox field;
            if (!(child instanceof EditBox) || !(field = (EditBox)child).isFocused()) continue;
            return true;
        }
        return false;
    }

    public boolean keyPressed(KeyEvent input) {
        boolean typing;
        if (this.emotePickerHere()) {
            EmotePicker picker = EmotePicker.get();
            if (input.key() == 256) {
                picker.close();
                return true;
            }
            if (picker.keyPressed(input)) {
                return true;
            }
        }
        Panel page = this.active();
        if (this.capturingPageKey && this.pageKey != null) {
            this.capturingPageKey = false;
            Map<String, String> keys = V2.INSTANCE.pageKeys;
            int code = input.key();
            if (code == 256 || code == 261 || code == 259) {
                keys.remove(this.pageKey);
            } else if (code >= 32 && code <= 348) {
                InputConstants.Key key = InputConstants.Type.KEYSYM.getOrCreate(code);
                String name = key.getName();
                boolean taken = Keybinds.isTaken(null, key);
                for (Map.Entry<String, String> e : keys.entrySet()) {
                    if (e.getKey().equals(this.pageKey) || !e.getValue().equals(name)) continue;
                    taken = true;
                }
                if (taken) {
                    this.pageKeyRejectedAt = System.currentTimeMillis();
                } else {
                    keys.put(this.pageKey, name);
                }
            }
            V2.save();
            return true;
        }
        if (FwmTutorial.keyPressed(input.key())) {
            return true;
        }
        if (page != null && page.isCapturingKeys() && page.keyPressed(input.key(), input.scancode(), input.modifiers())) {
            return true;
        }
        boolean bl = typing = this.typingInField() || page != null && page.isCapturingKeys();
        if (this.menuKeyArmed && !typing && Keybinds.OPEN_MENU != null && Keybinds.OPEN_MENU.matches(input) && !this.designTyping()) {
            this.onClose();
            return true;
        }
        if (input.key() == 256) {
            if (this.escape()) {
                return true;
            }
            this.onClose();
            return true;
        }
        if (page != null && this.pageLive() && page.keyPressed(input.key(), input.scancode(), input.modifiers())) {
            return true;
        }
        if (!typing && this.designKey(input)) {
            return true;
        }
        return super.keyPressed(input);
    }

    protected boolean designTyping() {
        return false;
    }

    protected boolean escape() {
        if (!this.subs.isEmpty()) {
            this.closeSub();
            return true;
        }
        return false;
    }

    public boolean keyReleased(KeyEvent input) {
        if (Keybinds.OPEN_MENU != null && Keybinds.OPEN_MENU.matches(input)) {
            this.menuKeyArmed = true;
        }
        return super.keyReleased(input);
    }

    public boolean charTyped(CharacterEvent input) {
        if (this.emotePickerHere() && EmotePicker.get().charTyped(input)) {
            return true;
        }
        Panel page = this.active();
        if (page != null && this.pageLive() && page.charTyped(input)) {
            return true;
        }
        if (!this.typingInField() && this.designChar(input)) {
            return true;
        }
        return super.charTyped(input);
    }

    private static boolean menuKeyHeld() {
        if (Keybinds.OPEN_MENU == null) {
            return false;
        }
        InputConstants.Key key = Keybinds.key(Keybinds.OPEN_MENU.saveString());
        if (key == null || key.getType() != InputConstants.Type.KEYSYM || !Keybinds.valid(key) || key.equals((Object)InputConstants.UNKNOWN)) {
            return false;
        }
        return InputConstants.isKeyDown((Window)Minecraft.getInstance().getWindow(), (int)key.getValue());
    }

    public void onClose() {
        this.closePage();
        FwmConfig.INSTANCE.clamp();
        ConfigManager.save();
        AntiLeak.sync(Minecraft.getInstance());
        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(this.parent);
        }
    }

    public void onFilesDrop(List<Path> paths) {
        Panel panel = this.active();
        if (panel instanceof TexturesPanel) {
            TexturesPanel textures = (TexturesPanel)panel;
            textures.filesDropped(paths);
            return;
        }
        panel = this.active();
        if (panel instanceof StudioBase) {
            StudioBase studio = (StudioBase)panel;
            studio.filesDropped(paths);
            return;
        }
        super.onFilesDrop(paths);
    }

    public boolean isPauseScreen() {
        return false;
    }
}

