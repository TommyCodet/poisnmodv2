/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.item.ItemStack
 *  org.joml.Matrix3x2fStack
 */
package net.friendswithmoney.fwm.ui.panel;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.friendswithmoney.fwm.auction.Money;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.stream.DonationSounds;
import net.friendswithmoney.fwm.stream.DonationStore;
import net.friendswithmoney.fwm.stream.DonationTier;
import net.friendswithmoney.fwm.stream.Donations;
import net.friendswithmoney.fwm.stream.fx.FxAnim;
import net.friendswithmoney.fwm.stream.fx.FxCanvas;
import net.friendswithmoney.fwm.stream.fx.FxPlayer;
import net.friendswithmoney.fwm.stream.fx.Play;
import net.friendswithmoney.fwm.ui.FwmTheme;
import net.friendswithmoney.fwm.ui.GlassBackground;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.LauncherGfx;
import net.friendswithmoney.fwm.ui.gfx.Shapes;
import net.friendswithmoney.fwm.ui.panel.DonationDropdown;
import net.friendswithmoney.fwm.util.Fmt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

public final class DonationStudioScreen
extends Screen {
    private static final float CW = 760.0f;
    private static final float CH = 428.0f;
    private static final float M = 16.0f;
    private static final float SX = 16.0f;
    private static final float SY = 44.0f;
    private static final float SW = 464.0f;
    private static final float SH = 368.0f;
    private static final float IX = 28.0f;
    private static final float IY = 78.0f;
    private static final float IW = 440.0f;
    private static final float IH = 248.0f;
    private static final float COLX = 488.0f;
    private static final float COLW = 256.0f;
    private static final float SEC_HEAD = 16.0f;
    private static final float ROW = 18.0f;
    private static final float SEC_PAD = 6.0f;
    private static final float SEC_GAP = 8.0f;
    private static final float LABEL_W = 72.0f;
    private static final int BG_TOP = -1978388437;
    private static final int BG_BOTTOM = -1676994025;
    private static final int CARD = 0x10FFFFFF;
    private static final int CARD_EDGE = 0x17FFFFFF;
    private static final int TEXT = -1116943;
    private static final int SUB = -6902107;
    private static final int LABEL = -4666172;
    private static final int GOLD = -673493;
    private static final int RED = -1017230;
    private static final int AMBER = -670398;
    private static final Identifier STAGE = Identifier.fromNamespaceAndPath((String)"fwm", (String)"textures/gui/donation/stage.png");
    private static final int STAGE_W = 960;
    private static final int STAGE_H = 540;
    private static final String[] ITEMS = new String[]{"minecraft:dragon_head", "minecraft:totem_of_undying", "minecraft:nether_star", "minecraft:diamond_block", "minecraft:diamond", "minecraft:gold_ingot", "minecraft:gold_block", "minecraft:emerald", "minecraft:emerald_block", "minecraft:netherite_ingot", "minecraft:beacon", "minecraft:end_crystal", "minecraft:enchanted_golden_apple", "minecraft:elytra", "minecraft:wither_skeleton_skull", "minecraft:spawner"};
    private static final float[] KEY_MS = new float[]{1500.0f, 1500.0f, 1500.0f, 2600.0f, 3200.0f, 1650.0f, 1600.0f, 650.0f, 2400.0f, 1100.0f};
    private final Screen parent;
    private int tierIndex;
    private float s = 1.0f;
    private float ox;
    private float oy;
    private float mx = -1.0f;
    private float my = -1.0f;
    private long shownAt;
    private final FxPlayer big = new FxPlayer();
    private final FxPlayer hoverPlayer = new FxPlayer();
    private final FxPlayer[] stills = new FxPlayer[FxAnim.ALL.size()];
    private final FxCanvas canvas = new FxCanvas();
    private int hoverAnim = -1;
    private int playAll = -1;
    private long nextAt;
    private int stillsFor = -1;
    private static final int DD_ANIM = 0;
    private static final int DD_COLOR = 1;
    private static final int DD_ITEM = 2;
    private static final int DD_DUR = 3;
    private static final int DD_SOUND = 4;
    private static final int DD_SOUND2 = 5;
    private final DonationDropdown dropdown = new DonationDropdown();
    private final float[][] ddBox = new float[6][4];
    private final DonationDropdown.Entries animEntries = new DonationDropdown.Entries(this){
        {
            Objects.requireNonNull(this$0);
        }

        @Override
        public int count() {
            return FxAnim.ALL.size();
        }

        @Override
        public String label(int i) {
            return FxAnim.ALL.get(i).name();
        }

        @Override
        public String detail(int i) {
            return DonationStudioScreen.seconds(FxAnim.ALL.get((int)i).defMs);
        }
    };
    private final DonationDropdown.Thumb animThumb = (ctx, i, x, y, w, h, hovered) -> {
        ctx.enableScissor((int)Math.floor(x), (int)Math.floor(y), (int)Math.ceil(x + w), (int)Math.ceil(y + h));
        DonationStudioScreen.stage(ctx, x, y, w, h);
        this.canvas.shake = false;
        this.canvas.begin(ctx, x, y, w, h, true);
        this.stills[i].drawAt(this.canvas, KEY_MS[Math.min(i, KEY_MS.length - 1)]);
        this.canvas.end();
        ctx.disableScissor();
        Shapes.ring(ctx, x, y, w, h, 3.0f, 0x1FFFFFFF);
    };
    private final DonationDropdown.Entries colorEntries = new DonationDropdown.Entries(this){
        final /* synthetic */ DonationStudioScreen this$0;
        {
            DonationStudioScreen donationStudioScreen = this$0;
            Objects.requireNonNull(donationStudioScreen);
            this.this$0 = donationStudioScreen;
        }

        @Override
        public int count() {
            return DonationTier.COLORS.length + 1;
        }

        @Override
        public String label(int i) {
            return DonationStudioScreen.colorName(i);
        }

        @Override
        public int swatch(int i) {
            return i < DonationTier.COLORS.length ? DonationTier.COLORS[i] : this.this$0.tier().color | 0xFF000000;
        }

        @Override
        public String detail(int i) {
            return i < DonationTier.COLORS.length ? null : String.format("#%06X", this.this$0.tier().color & 0xFFFFFF);
        }
    };
    private final DonationDropdown.Entries itemEntries = new DonationDropdown.Entries(this){
        {
            Objects.requireNonNull(this$0);
        }

        @Override
        public int count() {
            return ITEMS.length;
        }

        @Override
        public String label(int i) {
            return FxCanvas.itemOf(ITEMS[i]).getHoverName().getString();
        }

        @Override
        public ItemStack icon(int i) {
            return FxCanvas.itemOf(ITEMS[i]);
        }
    };
    private final DonationDropdown.Entries durEntries = new DonationDropdown.Entries(this){
        final /* synthetic */ DonationStudioScreen this$0;
        {
            DonationStudioScreen donationStudioScreen = this$0;
            Objects.requireNonNull(donationStudioScreen);
            this.this$0 = donationStudioScreen;
        }

        @Override
        public int count() {
            return 20;
        }

        @Override
        public String label(int i) {
            return i == 0 ? Lang.t("Standard", "Default") : i + 1 + " s";
        }

        @Override
        public String detail(int i) {
            return i == 0 ? DonationStudioScreen.seconds(FxAnim.byId((String)this.this$0.tier().anim).defMs) : null;
        }
    };
    private final DonationDropdown.Entries soundEntries = new DonationDropdown.Entries(this){
        {
            Objects.requireNonNull(this$0);
        }

        @Override
        public int count() {
            return DonationSounds.ALL.size();
        }

        @Override
        public String label(int i) {
            return DonationSounds.ALL.get(i).label();
        }

        @Override
        public boolean playable(int i) {
            return !"none".equals(DonationSounds.ALL.get(i).id());
        }
    };
    private static final int F_FROM = 0;
    private static final int F_TO = 1;
    private static final int F_NAME = 2;
    private static final int F_TEXT = 3;
    private static final int F_TESTNAME = 4;
    private static final int F_TESTAMT = 5;
    private static final int F_HEX = 6;
    private final StringBuilder typed = new StringBuilder();
    private int focus = -1;
    private final boolean[] badField = new boolean[7];
    private float textFieldX;
    private float textFieldY;
    private float textFieldW;
    private static final int SL_VOL = 0;
    private static final int SL_PITCH = 1;
    private final float[][] sliderBox = new float[2][4];
    private int dragging = -1;
    private long deleteArmedAt;
    private static final int A_TIER = 1;
    private static final int A_ADD = 2;
    private static final int A_DEL = 3;
    private static final int A_FIELD = 4;
    private static final int A_DD = 5;
    private static final int A_SND = 6;
    private static final int A_ON = 7;
    private static final int A_ANIMSND = 8;
    private static final int A_PLAY = 9;
    private static final int A_STOP = 10;
    private static final int A_ALL = 11;
    private static final int A_INGAME = 12;
    private static final int A_CLOSE = 13;
    private static final int A_SLIDER = 14;
    private final Hit[] hits = new Hit[96];
    private int hitCount;
    private static final int[] TAB_ORDER = new int[]{2, 0, 1, 6, 3, 4, 5};

    public DonationStudioScreen(Screen parent, int tierIndex) {
        super((Component)Component.literal((String)"Donations"));
        int i2;
        this.parent = parent;
        this.tierIndex = tierIndex;
        for (i2 = 0; i2 < this.hits.length; ++i2) {
            this.hits[i2] = new Hit();
        }
        for (i2 = 0; i2 < this.stills.length; ++i2) {
            this.stills[i2] = new FxPlayer();
        }
        this.hoverPlayer.loopPause = 500;
    }

    private List<DonationTier> tiers() {
        return DonationStore.tiers();
    }

    private DonationTier tier() {
        List<DonationTier> list = this.tiers();
        if (list.isEmpty()) {
            list.add(DonationTier.defaults().get(1));
        }
        this.tierIndex = Math.max(0, Math.min(list.size() - 1, this.tierIndex));
        return list.get(this.tierIndex);
    }

    protected void init() {
        double gui = Minecraft.getInstance().getWindow().getGuiScale();
        float fit = Math.min((float)this.width / 760.0f, (float)this.height / 428.0f) * 0.95f;
        float px = (float)Math.floor((double)fit * gui * 4.0) / 4.0f;
        this.s = px > 0.0f ? (float)((double)px / gui) : fit;
        this.ox = (float)Math.floor(((float)this.width - 760.0f * this.s) / 2.0f);
        this.oy = (float)Math.floor(((float)this.height - 428.0f * this.s) / 2.0f);
        if (this.shownAt == 0L) {
            this.shownAt = System.currentTimeMillis();
        }
        if (!this.big.running()) {
            this.playBig(true);
        }
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void onClose() {
        this.commitField();
        DonationStore.save();
        if (this.minecraft != null) {
            this.minecraft.gui.setScreen(this.parent);
        }
    }

    private void playBig(boolean sound) {
        DonationTier t = this.tier();
        this.big.loopPause = -1;
        this.big.load(DonationStore.get().testName, t.sampleAmount(), t.sampleAmount() * 3L, 1, t, FxAnim.byId(t.anim));
        this.big.play.preview = true;
        this.big.play.sound = sound;
        this.big.start(System.currentTimeMillis());
    }

    private void stopAll() {
        this.big.stop();
        this.playAll = -1;
        DonationSounds.stopAll();
    }

    private int stillsKey() {
        DonationTier t = this.tier();
        return System.identityHashCode(t) ^ t.color ^ t.name.hashCode() ^ t.item.hashCode() ^ t.text.hashCode() ^ Long.hashCode(t.sampleAmount()) ^ DonationStore.get().testName.hashCode();
    }

    private void loadStills() {
        DonationTier t = this.tier();
        for (int i = 0; i < this.stills.length; ++i) {
            this.stills[i].load(DonationStore.get().testName, t.sampleAmount(), t.sampleAmount() * 3L, 1, t, FxAnim.ALL.get(i));
            this.stills[i].play.preview = true;
            this.stills[i].play.sound = false;
        }
        this.stillsFor = this.stillsKey();
    }

    private static void txt(GuiGraphicsExtractor ctx, String str, float x, float y, int color, float size, boolean bold) {
        LauncherGfx.text(ctx, str, x, y, color, size, bold);
    }

    private static float w(String str, float size, boolean bold) {
        return LauncherGfx.width(str, size, bold);
    }

    private boolean over(float x, float y, float ww, float hh) {
        return !this.dropdown.contains(this.mx, this.my) && this.mx >= x && this.mx < x + ww && this.my >= y && this.my < y + hh;
    }

    private void hit(float x, float y, float ww, float hh, int action, int arg) {
        if (this.hitCount >= this.hits.length) {
            return;
        }
        Hit h = this.hits[this.hitCount++];
        h.x = x;
        h.y = y;
        h.w = ww;
        h.h = hh;
        h.action = action;
        h.arg = arg;
    }

    private static void card(GuiGraphicsExtractor ctx, float x, float y, float ww, float hh) {
        Shapes.round(ctx, x, y, ww, hh, 10.0f, 0x10FFFFFF);
        Shapes.ring(ctx, x, y, ww, hh, 10.0f, 0x17FFFFFF);
    }

    private float button(GuiGraphicsExtractor ctx, float x, float y, float hh, String icon, String label, int style, int action, int arg) {
        int ink;
        int edge;
        int fill;
        float size = 7.0f;
        float iw = icon == null ? 0.0f : 10.0f;
        float ww = Math.round(DonationStudioScreen.w(label, size, true) + iw + 20.0f);
        boolean ov = this.over(x, y, ww, hh);
        if (style == 1) {
            fill = ov ? Shapes.mix(FwmTheme.ACCENT, -1, 0.2f) : FwmTheme.ACCENT;
            edge = 0;
            ink = FwmTheme.TEXT_ON_ACCENT;
        } else if (style == 2) {
            fill = ov ? 620083499 : 284539179;
            edge = ov ? -856311509 : 1727379755;
            ink = -673493;
        } else {
            fill = ov ? 0x24FFFFFF : 0x12FFFFFF;
            edge = ov ? 0x40FFFFFF : 0x1CFFFFFF;
            ink = -1116943;
        }
        Shapes.round(ctx, x, y, ww, hh, hh / 2.0f, fill);
        if (edge != 0) {
            Shapes.ring(ctx, x, y, ww, hh, hh / 2.0f, edge);
        }
        float tx = x + 10.0f;
        if (icon != null) {
            Shapes.icon(ctx, icon, tx - 1.0f, y + hh / 2.0f - 4.0f, 8.0f, ink);
            tx += iw;
        }
        DonationStudioScreen.txt(ctx, label, tx, y + hh / 2.0f - 3.5f, ink, size, true);
        this.hit(x, y, ww, hh, action, arg);
        return ww;
    }

    private void toggle(GuiGraphicsExtractor ctx, float x, float y, boolean on, int action) {
        float tw = 20.0f;
        float th = 11.0f;
        boolean ov = this.over(x - 2.0f, y - 2.0f, tw + 4.0f, th + 4.0f);
        Shapes.round(ctx, x, y, tw, th, th / 2.0f, on ? FwmTheme.ACCENT : (ov ? 0x40FFFFFF : 0x2EFFFFFF));
        float k = th - 3.0f;
        float kx = on ? x + tw - 1.5f - k : x + 1.5f;
        Shapes.circle(ctx, kx + k / 2.0f, y + th / 2.0f, k / 2.0f, on ? FwmTheme.TEXT_ON_ACCENT : -1116943);
        this.hit(x - 2.0f, y - 2.0f, tw + 4.0f, th + 4.0f, action, 0);
    }

    private void field(GuiGraphicsExtractor ctx, int id, float x, float y, float ww, float hh, String value, String placeholder, String hint, int hintColor) {
        boolean focused = this.focus == id;
        boolean ov = this.over(x, y, ww, hh);
        Shapes.round(ctx, x, y, ww, hh, 5.0f, focused ? 0x22FFFFFF : (ov ? 0x1CFFFFFF : 0x12FFFFFF));
        Shapes.ring(ctx, x, y, ww, hh, 5.0f, focused ? FwmTheme.ACCENT : (this.badField[id] ? -1017230 : (ov ? 0x40FFFFFF : 0x1CFFFFFF)));
        float size = 7.0f;
        float cy = y + hh / 2.0f;
        float hintW = 0.0f;
        if (hint != null && !hint.isEmpty()) {
            hintW = DonationStudioScreen.w(hint, 6.5f, false);
            DonationStudioScreen.txt(ctx, hint, x + ww - 7.0f - hintW, cy - 3.5f, hintColor, 6.5f, false);
            hintW += 8.0f;
        }
        String shown = focused ? this.typed.toString() : value;
        boolean empty = shown == null || shown.isEmpty();
        String text = empty ? placeholder : shown;
        float room = ww - 14.0f - hintW;
        Object fit = text;
        if (DonationStudioScreen.w((String)fit, size, false) > room) {
            if (focused) {
                while (((String)fit).length() > 1 && DonationStudioScreen.w("\u2026" + (String)fit, size, false) > room) {
                    fit = ((String)fit).substring(1);
                }
                fit = "\u2026" + (String)fit;
            } else {
                fit = LauncherGfx.fit((String)fit, size, false, room);
            }
        }
        DonationStudioScreen.txt(ctx, (String)fit, x + 7.0f, cy - 3.5f, empty ? -2136556108 : -1116943, size, false);
        if (focused && System.currentTimeMillis() / 500L % 2L == 0L) {
            float cx = x + 7.0f + (empty ? 0.0f : DonationStudioScreen.w((String)fit, size, false)) + 0.5f;
            Shapes.round(ctx, cx, cy - 4.0f, 0.8f, 8.0f, 0.0f, -1116943);
        }
        this.hit(x, y, ww, hh, 4, id);
    }

    private void ddField(GuiGraphicsExtractor ctx, int id, float x, float y, float ww, float hh, String value, ItemStack icon, int swatch) {
        this.ddBox[id][0] = x;
        this.ddBox[id][1] = y;
        this.ddBox[id][2] = ww;
        this.ddBox[id][3] = hh;
        boolean open = this.dropdown.isOpen() && this.dropdown.openId() == id;
        DonationDropdown.field(ctx, x, y, ww, hh, value, icon, swatch, this.over(x, y, ww, hh), open);
        this.hit(x, y, ww, hh, 5, id);
    }

    private void playButton(GuiGraphicsExtractor ctx, float x, float y, float d, int arg) {
        boolean ov = this.over(x, y, d, d);
        Shapes.circle(ctx, x + d / 2.0f, y + d / 2.0f, d / 2.0f, ov ? FwmTheme.ACCENT : 0x1CFFFFFF);
        if (!ov) {
            Shapes.ring(ctx, x, y, d, d, d / 2.0f, 0x1CFFFFFF);
        }
        Shapes.icon(ctx, "play", x + d / 2.0f - 3.5f, y + d / 2.0f - 3.5f, 7.0f, ov ? FwmTheme.TEXT_ON_ACCENT : -1116943);
        this.hit(x, y, d, d, 6, arg);
    }

    private void slider(GuiGraphicsExtractor ctx, int id, float x, float y, float ww, float hh, float part, String value) {
        float vw = 28.0f;
        float tx = x + 5.0f;
        float tw = ww - vw - 10.0f;
        float cy = y + hh / 2.0f;
        this.sliderBox[id][0] = tx;
        this.sliderBox[id][1] = y;
        this.sliderBox[id][2] = tw;
        this.sliderBox[id][3] = hh;
        boolean ov = this.over(x, y, ww - vw, hh) || this.dragging == id;
        part = Math.max(0.0f, Math.min(1.0f, part));
        Shapes.round(ctx, tx, cy - 2.0f, tw, 4.0f, 2.0f, 0x1FFFFFFF);
        if (part > 0.0f) {
            Shapes.round(ctx, tx, cy - 2.0f, Math.max(4.0f, tw * part), 4.0f, 2.0f, FwmTheme.ACCENT);
        }
        float kx = tx + tw * part;
        Shapes.circle(ctx, kx, cy, ov ? 5.5f : 5.0f, -1116943);
        Shapes.ring(ctx, kx - (ov ? 5.5f : 5.0f), cy - (ov ? 5.5f : 5.0f), ov ? 11.0f : 10.0f, ov ? 11.0f : 10.0f, ov ? 5.5f : 5.0f, 0x33000000);
        DonationStudioScreen.txt(ctx, value, x + ww - DonationStudioScreen.w(value, 7.0f, false), cy - 3.5f, -6902107, 7.0f, false);
        this.hit(x, y, ww - vw, hh, 14, id);
    }

    public void extractBackground(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        if (V2.INSTANCE.menuBlur || this.minecraft == null || this.minecraft.level == null) {
            super.extractBackground(ctx, mouseX, mouseY, delta);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        int hovered;
        ctx.fill(0, 0, this.width, this.height, FwmTheme.OVERLAY);
        long now = System.currentTimeMillis();
        this.mx = ((float)mouseX - this.ox) / this.s;
        this.my = ((float)mouseY - this.oy) / this.s;
        this.hitCount = 0;
        DonationTier t = this.tier();
        if (this.stillsFor != this.stillsKey()) {
            this.loadStills();
        }
        this.dropdown.track(this.mx, this.my);
        if (this.dragging >= 0) {
            this.dragTo(this.dragging, this.mx);
        }
        if (this.big.running()) {
            this.big.sounds(now);
            if (!this.big.update(now)) {
                if (this.playAll >= 0) {
                    this.nextAt = now + 400L;
                } else {
                    this.big.loopPause = 900;
                    this.big.play.sound = false;
                    this.big.start(now);
                }
            }
        }
        if (this.playAll >= 0 && !this.big.running() && now >= this.nextAt) {
            ++this.playAll;
            if (this.playAll >= this.tiers().size()) {
                this.playAll = -1;
                this.playBig(false);
            } else {
                this.tierIndex = this.playAll;
                this.focus = -1;
                this.playBig(true);
            }
        }
        int n = hovered = this.dropdown.openId() == 0 ? this.dropdown.hovered() : -1;
        if (hovered != this.hoverAnim) {
            this.hoverAnim = hovered;
            if (hovered >= 0) {
                this.hoverPlayer.load(DonationStore.get().testName, t.sampleAmount(), t.sampleAmount() * 3L, 1, t, FxAnim.ALL.get(hovered));
                this.hoverPlayer.play.preview = true;
                this.hoverPlayer.play.sound = false;
                this.hoverPlayer.start(now);
            } else {
                this.hoverPlayer.stop();
            }
        }
        Matrix3x2fStack m = ctx.pose();
        m.pushMatrix();
        m.translate(this.ox, this.oy);
        m.scale(this.s, this.s);
        float shown = Math.min(1.0f, (float)(now - this.shownAt) / 170.0f);
        Shapes.alpha = shown = 1.0f - (1.0f - shown) * (1.0f - shown) * (1.0f - shown);
        if (shown < 1.0f) {
            m.translate(0.0f, (1.0f - shown) * 5.0f);
        }
        try {
            float pixels = this.s * (float)Minecraft.getInstance().getWindow().getGuiScale();
            GlassBackground.draw(ctx, 760, 428, 13, pixels, -1978388437, -1676994025, 5021318, 88, 13608076, 38);
            Shapes.ring(ctx, 0.0f, 0.0f, 760.0f, 428.0f, 13.0f, 0x2EFFFFFF);
            this.drawHeader(ctx, t);
            this.drawStage(ctx, t, now);
            this.drawColumn(ctx, t);
            this.drawFieldHelp(ctx);
            this.dropdown.render(ctx);
        }
        finally {
            Shapes.alpha = 1.0f;
        }
        m.popMatrix();
    }

    private void drawHeader(GuiGraphicsExtractor ctx, DonationTier t) {
        String title = Lang.t("Donation-Studio", "Donation Studio");
        DonationStudioScreen.txt(ctx, title, 18.0f, 17.0f, -1116943, 10.0f, true);
        float x = 18.0f + DonationStudioScreen.w(title, 10.0f, true) + 8.0f;
        String profile = Lang.t("Profil ", "Profile ") + DonationStore.get().profile;
        DonationStudioScreen.txt(ctx, LauncherGfx.fit(profile, 7.0f, false, 90.0f), x, 18.5f, -6902107, 7.0f, false);
        float doneW = Math.round(DonationStudioScreen.w(Lang.t("Fertig", "Done"), 7.0f, true) + 20.0f);
        float right = 744.0f - doneW - 8.0f - 20.0f - 8.0f;
        List<DonationTier> list = this.tiers();
        int n = list.size();
        float avail = right - (x += Math.min(90.0f, DonationStudioScreen.w(profile, 7.0f, false)) + 16.0f) - 4.0f;
        float[] widths = new float[n];
        float natural = 0.0f;
        for (int i = 0; i < n; ++i) {
            widths[i] = DonationStudioScreen.w(list.get((int)i).name, 7.0f, true) + 24.0f;
            natural += widths[i];
        }
        float squeeze = natural > avail ? avail / natural : 1.0f;
        float total = 4.0f;
        for (int i = 0; i < n; ++i) {
            widths[i] = (float)Math.floor(widths[i] * squeeze);
            total += widths[i];
        }
        float hh = 22.0f;
        float y = 12.0f;
        Shapes.round(ctx, x, y, total, hh, hh / 2.0f, 0x12FFFFFF);
        Shapes.ring(ctx, x, y, total, hh, hh / 2.0f, 0x17FFFFFF);
        float px = x + 2.0f;
        for (int i = 0; i < n; ++i) {
            DonationTier ti = list.get(i);
            boolean on = i == this.tierIndex;
            boolean ov = this.over(px, y + 2.0f, widths[i], hh - 4.0f);
            if (on) {
                Shapes.round(ctx, px, y + 2.0f, widths[i], hh - 4.0f, (hh - 4.0f) / 2.0f, 0x26FFFFFF);
                Shapes.ring(ctx, px, y + 2.0f, widths[i], hh - 4.0f, (hh - 4.0f) / 2.0f, 0x30FFFFFF);
            } else if (ov) {
                Shapes.round(ctx, px, y + 2.0f, widths[i], hh - 4.0f, (hh - 4.0f) / 2.0f, 0x10FFFFFF);
            }
            if (ti.on) {
                Shapes.circle(ctx, px + 10.0f, y + hh / 2.0f, 3.0f, ti.color | 0xFF000000);
            } else {
                Shapes.ring(ctx, px + 7.0f, y + hh / 2.0f - 3.0f, 6.0f, 6.0f, 3.0f, -2130706433);
            }
            String name = LauncherGfx.fit(ti.name, 7.0f, true, widths[i] - 22.0f);
            DonationStudioScreen.txt(ctx, name, px + 17.0f, y + hh / 2.0f - 3.5f, on ? -1116943 : (ti.on ? -4666172 : -6902107), 7.0f, true);
            this.hit(px, y + 2.0f, widths[i], hh - 4.0f, 1, i);
            px += widths[i];
        }
        float ax = x + total + 8.0f;
        if (n < 10) {
            boolean ov = this.over(ax, y + 1.0f, 20.0f, 20.0f);
            Shapes.circle(ctx, ax + 10.0f, y + 11.0f, 10.0f, ov ? 0x26FFFFFF : 0x12FFFFFF);
            Shapes.ring(ctx, ax, y + 1.0f, 20.0f, 20.0f, 10.0f, ov ? 0x40FFFFFF : 0x17FFFFFF);
            Shapes.icon(ctx, "plus", ax + 6.0f, y + 7.0f, 8.0f, ov ? FwmTheme.ACCENT : -1116943);
            this.hit(ax, y + 1.0f, 20.0f, 20.0f, 2, 0);
        }
        this.button(ctx, 744.0f - doneW, 13.0f, 20.0f, null, Lang.t("Fertig", "Done"), 1, 13, 0);
    }

    private static String range(DonationTier t) {
        return Play.money(t.from) + (String)(t.to < 0L ? " +" : Lang.t(" bis unter ", " to under ") + Play.money(t.to));
    }

    private static void stage(GuiGraphicsExtractor ctx, float x, float y, float ww, float hh) {
        Matrix3x2fStack m = ctx.pose();
        m.pushMatrix();
        m.translate(x, y);
        m.scale(ww / 960.0f, hh / 540.0f);
        ctx.blit(RenderPipelines.GUI_TEXTURED, STAGE, 0, 0, 0.0f, 0.0f, 960, 540, 960, 540);
        m.popMatrix();
    }

    private void drawStage(GuiGraphicsExtractor ctx, DonationTier t, long now) {
        DonationStudioScreen.card(ctx, 16.0f, 44.0f, 464.0f, 368.0f);
        float hy = 56.0f;
        Shapes.circle(ctx, 32.0f, hy + 4.0f, 3.5f, t.on ? t.color | 0xFF000000 : -2130706433);
        String name = LauncherGfx.fit(t.name, 9.0f, true, 190.0f);
        DonationStudioScreen.txt(ctx, name, 40.0f, hy - 0.5f, -1116943, 9.0f, true);
        DonationStudioScreen.txt(ctx, DonationStudioScreen.range(t), 40.0f + DonationStudioScreen.w(name, 9.0f, true) + 8.0f, hy + 0.5f, -6902107, 7.0f, false);
        FxAnim shownAnim = this.hoverAnim >= 0 ? FxAnim.ALL.get(this.hoverAnim) : FxAnim.byId(t.anim);
        String chip = (this.hoverAnim >= 0 ? Lang.t("Vorschau: ", "Preview: ") : "") + shownAnim.name() + "  \u00b7  " + DonationStudioScreen.seconds(t.seconds > 0 && this.hoverAnim < 0 ? t.seconds * 1000 : shownAnim.defMs);
        float cw = DonationStudioScreen.w(chip, 6.5f, true) + 16.0f;
        Shapes.round(ctx, 468.0f - cw, hy - 2.0f, cw, 13.0f, 6.5f, this.hoverAnim >= 0 ? 526182548 : 0x12FFFFFF);
        Shapes.ring(ctx, 468.0f - cw, hy - 2.0f, cw, 13.0f, 6.5f, this.hoverAnim >= 0 ? 1717364884 : 0x1CFFFFFF);
        DonationStudioScreen.txt(ctx, chip, 468.0f - cw + 8.0f, hy + 1.0f, this.hoverAnim >= 0 ? FwmTheme.ACCENT : -4666172, 6.5f, true);
        ctx.enableScissor(28, 78, 468, 326);
        DonationStudioScreen.stage(ctx, 28.0f, 78.0f, 440.0f, 248.0f);
        this.canvas.shake = true;
        this.canvas.begin(ctx, 28.0f, 78.0f, 440.0f, 248.0f, true);
        if (this.hoverAnim >= 0) {
            this.hoverPlayer.draw(this.canvas, now);
        } else {
            this.big.draw(this.canvas, now);
        }
        this.canvas.end();
        ctx.disableScissor();
        Shapes.ring(ctx, 28.0f, 78.0f, 440.0f, 248.0f, 3.0f, 0x26FFFFFF);
        float by = 336.0f;
        float bh = 20.0f;
        float bx = 28.0f;
        bx += this.button(ctx, bx, by, bh, "play", Lang.t("Abspielen", "Play"), 1, 9, 0) + 6.0f;
        bx += this.button(ctx, bx, by, bh, null, Lang.t("Stopp", "Stop"), 0, 10, 0) + 6.0f;
        this.button(ctx, bx, by, bh, null, (String)(this.playAll >= 0 ? Lang.t("Alle Stufen ", "All tiers ") + (this.playAll + 1) + "/" + this.tiers().size() : Lang.t("Alle Stufen", "All tiers")), 0, 11, 0);
        String inGame = Lang.t("Im Spiel testen", "Test in game");
        float gw = Math.round(DonationStudioScreen.w(inGame, 7.0f, true) + 30.0f);
        this.button(ctx, 468.0f - gw, by, bh, "play", inGame, 2, 12, 0);
        FxAnim a = FxAnim.byId(t.anim);
        float iy = by + bh + 12.0f;
        List<String> clash = DonationStore.overlaps(t);
        String desc = a.desc();
        DonationStudioScreen.txt(ctx, a.name(), 28.0f, iy, -1116943, 7.5f, true);
        float dx = 28.0f + DonationStudioScreen.w(a.name(), 7.5f, true) + 8.0f;
        DonationStudioScreen.txt(ctx, LauncherGfx.fit(desc, 7.0f, false, 468.0f - dx), dx, iy + 0.5f, -6902107, 7.0f, false);
        if (!clash.isEmpty()) {
            String warn = Lang.t("\u00dcberschneidet sich mit ", "Overlaps ") + String.join((CharSequence)", ", clash) + Lang.t(" \u2013 das h\u00f6here \u201eAb\u201c gewinnt", " \u2013 the higher \"from\" wins");
            Shapes.circle(ctx, 31.0f, iy + 17.0f, 3.0f, -670398);
            DonationStudioScreen.txt(ctx, LauncherGfx.fit(warn, 7.0f, false, 428.0f), 39.0f, iy + 13.5f, -670398, 7.0f, false);
        } else {
            StringBuilder used = new StringBuilder();
            for (DonationTier other : this.tiers()) {
                if (other == t || !other.anim.equals(a.id)) continue;
                if (used.length() > 0) {
                    used.append(", ");
                }
                used.append(other.name);
            }
            String line = used.length() > 0 ? Lang.t("Auch benutzt von ", "Also used by ") + String.valueOf(used) : Lang.t("Leertaste spielt ab \u00b7 \u00c4nderungen werden sofort gespeichert", "Space plays \u00b7 changes save by themselves");
            DonationStudioScreen.txt(ctx, LauncherGfx.fit(line, 6.5f, false, 440.0f), 28.0f, iy + 14.0f, -9464447, 6.5f, false);
        }
    }

    private float section(GuiGraphicsExtractor ctx, float y, int rows, String title) {
        float hh = 16.0f + (float)rows * 18.0f + 6.0f;
        DonationStudioScreen.card(ctx, 488.0f, y, 256.0f, hh);
        DonationStudioScreen.txt(ctx, title, 500.0f, y + 8.0f, -4666172, 7.5f, true);
        return y + 16.0f + 2.0f;
    }

    private void rowLabel(GuiGraphicsExtractor ctx, String label, float ry) {
        DonationStudioScreen.txt(ctx, LauncherGfx.fit(label, 7.0f, false, 68.0f), 500.0f, ry + 9.0f - 4.0f, -6902107, 7.0f, false);
    }

    private void drawColumn(GuiGraphicsExtractor ctx, DonationTier t) {
        float cx = 572.0f;
        float cw = 160.0f;
        float fh = 15.0f;
        float inset = (18.0f - fh) / 2.0f - 0.5f;
        float y = 44.0f;
        float ry = this.section(ctx, y, 3, Lang.t("Stufe", "Tier"));
        boolean armed = System.currentTimeMillis() - this.deleteArmedAt < 3000L;
        float headY = y + 6.0f;
        if (this.tiers().size() > 1) {
            String del = armed ? Lang.t("Wirklich l\u00f6schen?", "Really delete?") : Lang.t("L\u00f6schen", "Delete");
            float dw = DonationStudioScreen.w(del, 6.5f, true) + 12.0f;
            float dx = 732.0f - dw;
            boolean ov = this.over(dx, headY - 1.0f, dw, 13.0f);
            Shapes.round(ctx, dx, headY - 1.0f, dw, 13.0f, 6.5f, armed ? 871398002 : (ov ? 451967602 : 0));
            if (armed || ov) {
                Shapes.ring(ctx, dx, headY - 1.0f, dw, 13.0f, 6.5f, -2131723662);
            }
            DonationStudioScreen.txt(ctx, del, dx + 6.0f, headY + 1.5f, armed || ov ? -1017230 : -6902107, 6.5f, true);
            this.hit(dx, headY - 1.0f, dw, 13.0f, 3, 0);
            float onX = dx - 10.0f - 20.0f;
            this.toggle(ctx, onX, headY, t.on, 7);
            DonationStudioScreen.txt(ctx, Lang.t("Aktiv", "On"), onX - 4.0f - DonationStudioScreen.w(Lang.t("Aktiv", "On"), 6.5f, false), headY + 2.0f, -6902107, 6.5f, false);
        } else {
            float onX = 712.0f;
            this.toggle(ctx, onX, headY, t.on, 7);
            DonationStudioScreen.txt(ctx, Lang.t("Aktiv", "On"), onX - 4.0f - DonationStudioScreen.w(Lang.t("Aktiv", "On"), 6.5f, false), headY + 2.0f, -6902107, 6.5f, false);
        }
        this.rowLabel(ctx, Lang.t("Name", "Name"), ry);
        this.field(ctx, 2, cx, ry + inset, cw, fh, t.name, "", null, 0);
        this.rowLabel(ctx, Lang.t("Ab Betrag", "From"), ry += 18.0f);
        this.field(ctx, 0, cx, ry + inset, cw, fh, Fmt.shortMoney(t.from), "0", (String)(this.badField[0] ? Lang.t("z. B. 500k, 2.5m", "e.g. 500k, 2.5m") : "= " + Fmt.fullMoney(t.from)), this.badField[0] ? -1017230 : -6902107);
        this.rowLabel(ctx, Lang.t("Bis unter", "Under"), ry += 18.0f);
        this.field(ctx, 1, cx, ry + inset, cw, fh, t.to < 0L ? "" : Fmt.shortMoney(t.to), Lang.t("offen", "open"), (String)(this.badField[1] ? Lang.t("z. B. 500k, 2.5m", "e.g. 500k, 2.5m") : (t.to < 0L ? Lang.t("nach oben offen", "no upper end") : "= " + Fmt.fullMoney(t.to))), this.badField[1] ? -1017230 : -6902107);
        ry = this.section(ctx, y += 84.0f, 5, Lang.t("Aussehen", "Look"));
        this.rowLabel(ctx, Lang.t("Animation", "Animation"), ry);
        this.ddField(ctx, 0, cx, ry + inset, cw, fh, FxAnim.byId(t.anim).name(), null, 0);
        this.rowLabel(ctx, Lang.t("Farbe", "Colour"), ry += 18.0f);
        int preset = DonationStudioScreen.colorIndex(t.color);
        float hexW = 54.0f;
        this.ddField(ctx, 1, cx, ry + inset, cw - hexW - 6.0f, fh, DonationStudioScreen.colorName(preset), null, t.color | 0xFF000000);
        this.field(ctx, 6, cx + cw - hexW, ry + inset, hexW, fh, String.format("#%06X", t.color & 0xFFFFFF), "#RRGGBB", null, 0);
        this.rowLabel(ctx, Lang.t("Item", "Item"), ry += 18.0f);
        ItemStack icon = FxCanvas.itemOf(t.item);
        this.ddField(ctx, 2, cx, ry + inset, cw, fh, icon.getHoverName().getString(), icon, 0);
        this.rowLabel(ctx, Lang.t("Dauer", "Length"), ry += 18.0f);
        int def = FxAnim.byId((String)t.anim).defMs;
        this.ddField(ctx, 3, cx, ry + inset, cw, fh, t.seconds <= 0 ? Lang.t("Standard", "Default") + " \u00b7 " + DonationStudioScreen.seconds(def) : t.seconds + " s", null, 0);
        this.rowLabel(ctx, Lang.t("Text", "Text"), ry += 18.0f);
        this.textFieldX = cx;
        this.textFieldY = ry + inset;
        this.textFieldW = cw;
        this.field(ctx, 3, cx, ry + inset, cw, fh, t.text, DonationTier.defaultText(), null, 0);
        ry = this.section(ctx, y += 120.0f, 4, Lang.t("Ton", "Sound"));
        float fxX = 712.0f;
        this.toggle(ctx, fxX, y + 6.0f, t.animSounds, 8);
        String fxLabel = Lang.t("Effekt-T\u00f6ne", "FX sounds");
        DonationStudioScreen.txt(ctx, fxLabel, fxX - 4.0f - DonationStudioScreen.w(fxLabel, 6.5f, false), y + 8.0f, -6902107, 6.5f, false);
        float pd = fh;
        this.rowLabel(ctx, Lang.t("Ton", "Sound"), ry);
        this.ddField(ctx, 4, cx, ry + inset, cw - pd - 5.0f, fh, DonationSounds.label(t.sound), null, 0);
        this.playButton(ctx, cx + cw - pd, ry + inset, pd, 0);
        this.rowLabel(ctx, Lang.t("Einschlag", "Impact"), ry += 18.0f);
        this.ddField(ctx, 5, cx, ry + inset, cw - pd - 5.0f, fh, DonationSounds.label(t.sound2), null, 0);
        this.playButton(ctx, cx + cw - pd, ry + inset, pd, 1);
        this.rowLabel(ctx, Lang.t("Lautst\u00e4rke", "Volume"), ry += 18.0f);
        this.slider(ctx, 0, cx, ry + inset, cw, fh, t.volume, Math.round(t.volume * 100.0f) + " %");
        this.rowLabel(ctx, Lang.t("Tonh\u00f6he", "Pitch"), ry += 18.0f);
        this.slider(ctx, 1, cx, ry + inset, cw, fh, (t.pitch - 0.5f) / 1.5f, String.format(Locale.GERMANY, "%.2f", Float.valueOf(t.pitch)));
        ry = this.section(ctx, y += 102.0f, 2, Lang.t("Test", "Test"));
        this.rowLabel(ctx, Lang.t("Spender", "Donor"), ry);
        this.field(ctx, 4, cx, ry + inset, cw, fh, DonationStore.get().testName, "TestSpender", null, 0);
        this.rowLabel(ctx, Lang.t("Betrag", "Amount"), ry += 18.0f);
        long sample = t.sampleAmount();
        boolean inside = sample >= t.from && (t.to < 0L || sample < t.to);
        this.field(ctx, 5, cx, ry + inset, cw, fh, Fmt.shortMoney(sample), "", (String)(this.badField[5] ? Lang.t("z. B. 2.5m", "e.g. 2.5m") : (inside ? "= " + Fmt.fullMoney(sample) : Lang.t("nicht in der Stufe", "not in this tier"))), inside && !this.badField[5] ? -6902107 : -1017230);
    }

    private void drawFieldHelp(GuiGraphicsExtractor ctx) {
        if (this.focus != 3 || this.dropdown.isOpen()) {
            return;
        }
        ctx.nextStratum();
        String help = "{name}  {amount}  {total}  {count}";
        float hw = DonationStudioScreen.w(help, 6.5f, false) + 16.0f;
        float x = this.textFieldX + this.textFieldW - hw;
        float y = this.textFieldY + 19.0f;
        Shapes.round(ctx, x, y, hw, 15.0f, 7.5f, -15653857);
        Shapes.ring(ctx, x, y, hw, 15.0f, 7.5f, 0x2EFFFFFF);
        DonationStudioScreen.txt(ctx, help, x + 8.0f, y + 4.0f, FwmTheme.ACCENT, 6.5f, false);
    }

    private static int colorIndex(int color) {
        for (int i = 0; i < DonationTier.COLORS.length; ++i) {
            if ((color | 0xFF000000) != DonationTier.COLORS[i]) continue;
            return i;
        }
        return DonationTier.COLORS.length;
    }

    private static String colorName(int i) {
        return switch (i) {
            case 0 -> "Mint";
            case 1 -> "Gold";
            case 2 -> Lang.t("Lila", "Purple");
            case 3 -> Lang.t("Rot", "Red");
            case 4 -> Lang.t("Blau", "Blue");
            default -> Lang.t("Eigene Farbe", "Custom");
        };
    }

    private static String seconds(int ms) {
        return String.format(Locale.GERMANY, "%.1f s", Float.valueOf((float)ms / 1000.0f));
    }

    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        float cx = (float)((click.x() - (double)this.ox) / (double)this.s);
        float cy = (float)((click.y() - (double)this.oy) / (double)this.s);
        if (this.dropdown.isOpen()) {
            int id = this.dropdown.openId();
            int r = this.dropdown.click(cx, cy);
            if (r >= 10000) {
                this.preview(id, r - 10000);
            } else if (r >= 0) {
                this.choose(id, r);
            } else if (r == -1) {
                for (int i = this.hitCount - 1; i >= 0; --i) {
                    Hit h = this.hits[i];
                    if (h.action != 5 || h.arg == id || !(cx >= h.x) || !(cx < h.x + h.w) || !(cy >= h.y) || !(cy < h.y + h.h)) continue;
                    this.act(h.action, h.arg);
                    break;
                }
            }
            return true;
        }
        for (int i = this.hitCount - 1; i >= 0; --i) {
            Hit h = this.hits[i];
            if (!(cx >= h.x) || !(cx < h.x + h.w) || !(cy >= h.y) || !(cy < h.y + h.h)) continue;
            this.act(h.action, h.arg);
            if (h.action == 14) {
                this.dragging = h.arg;
                this.dragTo(h.arg, cx);
            }
            return true;
        }
        this.commitField();
        return super.mouseClicked(click, doubled);
    }

    public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
        if (this.dragging >= 0) {
            this.dragTo(this.dragging, (float)((click.x() - (double)this.ox) / (double)this.s));
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    public boolean mouseReleased(MouseButtonEvent click) {
        if (this.dragging >= 0) {
            this.dragging = -1;
            DonationTier t = this.tier();
            DonationSounds.play(t.sound, t.pitch, t.volume);
            DonationStore.changed();
            return true;
        }
        return super.mouseReleased(click);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (this.dropdown.isOpen()) {
            this.dropdown.scroll(vertical);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    private void dragTo(int id, float x) {
        float[] b = this.sliderBox[id];
        if (b[2] <= 0.0f) {
            return;
        }
        float part = Math.max(0.0f, Math.min(1.0f, (x - b[0]) / b[2]));
        DonationTier t = this.tier();
        if (id == 0) {
            t.volume = (float)Math.round(part * 20.0f) / 20.0f;
        } else {
            t.pitch = (float)Math.round((0.5f + part * 1.5f) * 20.0f) / 20.0f;
        }
    }

    private void act(int action, int arg) {
        DonationTier t = this.tier();
        if (action != 4) {
            this.commitField();
        }
        if (action != 3) {
            this.deleteArmedAt = 0L;
        }
        switch (action) {
            case 13: {
                this.onClose();
                break;
            }
            case 1: {
                this.tierIndex = arg;
                this.playAll = -1;
                this.playBig(true);
                break;
            }
            case 2: {
                if (this.tiers().size() >= 10) {
                    return;
                }
                DonationTier last = this.tiers().get(this.tiers().size() - 1);
                DonationTier n = last.copy();
                n.name = Lang.t("Neue Stufe", "New tier");
                n.from = last.to < 0L ? Math.max(100L, last.from * 2L) : last.to;
                n.to = -1L;
                n.testAmount = 0L;
                this.tiers().add(n);
                this.tierIndex = this.tiers().size() - 1;
                this.changed(true);
                break;
            }
            case 3: {
                if (this.tiers().size() <= 1) {
                    return;
                }
                if (System.currentTimeMillis() - this.deleteArmedAt > 3000L) {
                    this.deleteArmedAt = System.currentTimeMillis();
                    return;
                }
                this.deleteArmedAt = 0L;
                this.tiers().remove(this.tierIndex);
                this.tierIndex = Math.max(0, this.tierIndex - 1);
                this.changed(true);
                break;
            }
            case 4: {
                this.focusField(arg);
                break;
            }
            case 5: {
                this.openDropdown(arg);
                break;
            }
            case 6: {
                DonationSounds.play(arg == 0 ? t.sound : t.sound2, t.pitch, t.volume);
                break;
            }
            case 7: {
                t.on = !t.on;
                this.changed(false);
                break;
            }
            case 8: {
                t.animSounds = !t.animSounds;
                this.changed(false);
                break;
            }
            case 9: {
                this.playAll = -1;
                this.playBig(true);
                break;
            }
            case 10: {
                this.stopAll();
                break;
            }
            case 11: {
                this.playAll = 0;
                this.tierIndex = 0;
                this.playBig(true);
                break;
            }
            case 12: {
                DonationTier copy = t.copy();
                long amount = t.sampleAmount();
                DonationStore.save();
                if (this.minecraft != null) {
                    this.minecraft.gui.setScreen(null);
                }
                Donations.test(copy, amount);
                break;
            }
        }
    }

    private void openDropdown(int id) {
        if (this.dropdown.isOpen() && this.dropdown.openId() == id) {
            this.dropdown.close();
            return;
        }
        DonationTier t = this.tier();
        float[] b = this.ddBox[id];
        float top = 44.0f;
        float bottom = 420.0f;
        switch (id) {
            case 0: {
                float cell = 82.666664f;
                this.dropdown.openGrid(id, this.animEntries, this.animThumb, 3, cell, cell * 9.0f / 16.0f + 24.0f, FxAnim.indexOf(t.anim), 488.0f, b[1], 256.0f, b[3], top, bottom);
                break;
            }
            case 1: {
                this.dropdown.open(id, this.colorEntries, DonationStudioScreen.colorIndex(t.color), b[0], b[1], b[2], b[3], top, bottom);
                break;
            }
            case 2: {
                int sel = -1;
                for (int k = 0; k < ITEMS.length; ++k) {
                    if (!ITEMS[k].equals(t.item)) continue;
                    sel = k;
                }
                this.dropdown.open(id, this.itemEntries, sel, b[0], b[1], b[2], b[3], top, bottom);
                break;
            }
            case 3: {
                this.dropdown.open(id, this.durEntries, t.seconds <= 0 ? 0 : t.seconds - 1, b[0], b[1], b[2], b[3], top, bottom);
                break;
            }
            case 4: {
                this.dropdown.open(id, this.soundEntries, DonationSounds.indexOf(t.sound), b[0], b[1], b[2] + 20.0f, b[3], top, bottom);
                break;
            }
            case 5: {
                this.dropdown.open(id, this.soundEntries, DonationSounds.indexOf(t.sound2), b[0], b[1], b[2] + 20.0f, b[3], top, bottom);
                break;
            }
        }
    }

    private void choose(int id, int i) {
        DonationTier t = this.tier();
        switch (id) {
            case 0: {
                t.anim = FxAnim.ALL.get((int)i).id;
                this.changed(true);
                break;
            }
            case 1: {
                if (i < DonationTier.COLORS.length) {
                    t.color = DonationTier.COLORS[i];
                    this.changed(false);
                    break;
                }
                this.focusField(6);
                break;
            }
            case 2: {
                t.item = ITEMS[i];
                this.changed(true);
                break;
            }
            case 3: {
                t.seconds = i == 0 ? 0 : i + 1;
                this.changed(true);
                break;
            }
            case 4: {
                t.sound = DonationSounds.ALL.get(i).id();
                this.changed(false);
                DonationSounds.play(t.sound, t.pitch, t.volume);
                break;
            }
            case 5: {
                t.sound2 = DonationSounds.ALL.get(i).id();
                this.changed(false);
                DonationSounds.play(t.sound2, t.pitch, t.volume);
                break;
            }
        }
    }

    private void preview(int id, int i) {
        if (id != 4 && id != 5) {
            return;
        }
        DonationTier t = this.tier();
        DonationSounds.play(DonationSounds.ALL.get(i).id(), t.pitch, t.volume);
    }

    private void changed(boolean replay) {
        DonationStore.changed();
        this.stillsFor = -1;
        if (replay) {
            this.playAll = -1;
            this.playBig(true);
        } else if (this.big.running()) {
            DonationTier t = this.tier();
            FxAnim anim = this.big.play.anim;
            this.big.play.prepare(anim, t);
        }
    }

    private void focusField(int id) {
        if (this.focus == id) {
            return;
        }
        this.commitField();
        this.dropdown.close();
        this.focus = id;
        this.typed.setLength(0);
        DonationTier t = this.tier();
        this.typed.append(switch (id) {
            case 0 -> Fmt.shortMoney(t.from);
            case 1 -> {
                if (t.to < 0L) {
                    yield "";
                }
                yield Fmt.shortMoney(t.to);
            }
            case 2 -> t.name;
            case 3 -> t.text;
            case 4 -> DonationStore.get().testName;
            case 5 -> Fmt.shortMoney(t.sampleAmount());
            case 6 -> String.format("#%06X", t.color & 0xFFFFFF);
            default -> "";
        });
    }

    private void applyTyped() {
        if (this.focus < 0) {
            return;
        }
        DonationTier t = this.tier();
        String v = this.typed.toString();
        this.badField[this.focus] = false;
        switch (this.focus) {
            case 0: {
                long c = Money.cents(v);
                if (v.isBlank()) {
                    c = 0L;
                } else if (c <= 0L && !v.trim().equals("0")) {
                    this.badField[this.focus] = true;
                    return;
                }
                t.from = Math.max(0L, c);
                break;
            }
            case 1: {
                if (v.isBlank()) {
                    t.to = -1L;
                    break;
                }
                long c = Money.cents(v);
                if (c <= 0L) {
                    this.badField[this.focus] = true;
                    return;
                }
                t.to = c;
                break;
            }
            case 2: {
                t.name = v.isBlank() ? "?" : v;
                break;
            }
            case 3: {
                t.text = v;
                break;
            }
            case 4: {
                String name = v.replaceAll("[^A-Za-z0-9_]", "");
                DonationStore.get().testName = name.isEmpty() ? "TestSpender" : name;
                break;
            }
            case 5: {
                long c = Money.cents(v);
                if (c <= 0L) {
                    this.badField[this.focus] = true;
                    return;
                }
                t.testAmount = c;
                break;
            }
            case 6: {
                String hex = v.replace("#", "").trim();
                if (hex.length() != 6) {
                    this.badField[this.focus] = hex.length() > 6;
                    return;
                }
                try {
                    t.color = 0xFF000000 | Integer.parseInt(hex, 16);
                    break;
                }
                catch (NumberFormatException e) {
                    this.badField[this.focus] = true;
                    return;
                }
            }
        }
        DonationStore.changed();
        this.stillsFor = -1;
        if (this.big.running() && this.big.play.anim != null) {
            this.big.play.amount = this.tier().sampleAmount();
            this.big.play.donor = DonationStore.get().testName;
            this.big.play.prepare(this.big.play.anim, t);
        }
    }

    private void commitField() {
        if (this.focus < 0) {
            return;
        }
        this.applyTyped();
        this.badField[this.focus] = false;
        this.focus = -1;
        this.typed.setLength(0);
    }

    public boolean charTyped(CharacterEvent input) {
        int max;
        if (this.focus < 0) {
            return super.charTyped(input);
        }
        if (!input.isAllowedChatCharacter()) {
            return true;
        }
        int n = this.focus == 3 ? 80 : (max = this.focus == 2 ? 28 : 16);
        if (this.typed.length() < max) {
            this.typed.append(input.codepointAsString());
            this.applyTyped();
        }
        return true;
    }

    public boolean keyPressed(KeyEvent input) {
        int key = input.key();
        if (this.dropdown.isOpen() && key == 256) {
            this.dropdown.close();
            return true;
        }
        if (this.focus >= 0) {
            if (key == 259) {
                if (!this.typed.isEmpty()) {
                    this.typed.setLength(this.typed.length() - 1);
                    this.applyTyped();
                }
                return true;
            }
            if (key == 257 || key == 335 || key == 258) {
                int next = this.focus;
                this.commitField();
                if (key == 258) {
                    int at = 0;
                    for (int i = 0; i < TAB_ORDER.length; ++i) {
                        if (TAB_ORDER[i] != next) continue;
                        at = i;
                    }
                    this.focusField(TAB_ORDER[(at + 1) % TAB_ORDER.length]);
                }
                return true;
            }
            if (key == 256) {
                this.commitField();
                return true;
            }
            if (key == 86 && (input.modifiers() & 2) != 0 && this.minecraft != null) {
                String clip = this.minecraft.keyboardHandler.getClipboard();
                if (clip != null) {
                    this.typed.append(clip.replace("\n", " "));
                    if (this.typed.length() > 80) {
                        this.typed.setLength(80);
                    }
                    this.applyTyped();
                }
                return true;
            }
            if (key == 65 && (input.modifiers() & 2) != 0) {
                this.typed.setLength(0);
                this.applyTyped();
                return true;
            }
            return true;
        }
        if (key == 32) {
            this.playBig(true);
            return true;
        }
        return super.keyPressed(input);
    }

    public static void gameFrame() {
    }

    public static void maybeSnapshot(Screen screen) {
    }

    public static void open(int tierIndex) {
        Minecraft client = Minecraft.getInstance();
        client.gui.setScreen((Screen)new DonationStudioScreen(client.gui.screen(), tierIndex));
    }

    public void replay() {
        this.playBig(true);
    }

    private static final class Hit {
        float x;
        float y;
        float w;
        float h;
        int action;
        int arg;

        private Hit() {
        }
    }
}

