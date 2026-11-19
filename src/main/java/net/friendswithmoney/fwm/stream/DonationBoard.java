/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
 *  net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.SubmitNodeCollector
 *  net.minecraft.client.renderer.state.level.CameraRenderState
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.entity.EntityAttachment
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix3x2fStack
 */
package net.friendswithmoney.fwm.stream;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.stream.DonationStore;
import net.friendswithmoney.fwm.stream.Donations;
import net.friendswithmoney.fwm.stream.fx.Play;
import net.friendswithmoney.fwm.ui.FwmGfx;
import net.friendswithmoney.fwm.ui.FwmHud;
import net.friendswithmoney.fwm.ui.HudEditScreen;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.LauncherGfx;
import net.friendswithmoney.fwm.ui.MemberTags;
import net.friendswithmoney.fwm.ui.NoRisk;
import net.friendswithmoney.fwm.ui.gfx.Shapes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3x2fStack;

public final class DonationBoard {
    private static final int W = 168;
    private static final int PAD = 8;
    private static final int DEFAULT_Y = 142;
    private static final int ROW = 18;
    private static final int GLASS_TOP = -1005310936;
    private static final int GLASS_BOTTOM = -804645356;
    private static final int RIM = 0x30FFFFFF;
    private static final int LINE = 0x14FFFFFF;
    private static final int TEXT = -1116943;
    private static final int SUB = -6902107;
    private static final int MINT = -8523578;
    public static final int GOLD = -670398;
    public static final int SILVER = -2760986;
    public static final int BRONZE = -2055829;
    private static final String[] SAMPLE = new String[]{"TestSpender", "Steve", "Alex", "Notch", "Jeb_"};
    private static List<DonationStore.Rank> top = new ArrayList<DonationStore.Rank>();
    private static int topRevision = -1;
    private static int topPeriod = -1;
    private static long topAt;
    private static final Vec3 ANCHOR;
    private static final MutableComponent[] LABELS;
    private static String labelsLang;

    private DonationBoard() {
    }

    static void register() {
        LevelRenderEvents.COLLECT_SUBMITS.register(DonationBoard::crowns);
    }

    public static String periodName(int period) {
        return switch (period) {
            case 0 -> Lang.t("Session", "Session");
            case 1 -> Lang.t("Heute", "Today");
            case 2 -> Lang.t("Woche", "Week");
            default -> Lang.t("Gesamt", "All time");
        };
    }

    static String[] emptyText(int period, boolean anyBefore) {
        String none = Lang.t("Noch keine Donations", "No donations yet");
        if (!anyBefore || period == 3) {
            return new String[]{none};
        }
        String[] stringArray = new String[2];
        stringArray[0] = none;
        stringArray[1] = switch (period) {
            case 0 -> Lang.t("in dieser Session", "this session");
            case 1 -> Lang.t("heute", "today");
            default -> Lang.t("in dieser Woche", "this week");
        };
        return stringArray;
    }

    public static List<DonationStore.Rank> top() {
        DonationStore.Data d = DonationStore.get();
        long now = System.currentTimeMillis();
        if (topRevision != DonationStore.revision || topPeriod != d.hudPeriod || now - topAt > 10000L) {
            topRevision = DonationStore.revision;
            topPeriod = d.hudPeriod;
            topAt = now;
            top = DonationStore.ranking(d.hudPeriod);
        }
        return top;
    }

    public static int place(String name) {
        List<DonationStore.Rank> list = DonationBoard.top();
        for (int i = 0; i < Math.min(3, list.size()); ++i) {
            if (list.get(i).name() == null || !list.get(i).name().equalsIgnoreCase(name)) continue;
            return i + 1;
        }
        return 0;
    }

    public static int placeColor(int index) {
        return index == 0 ? -670398 : (index == 1 ? -2760986 : (index == 2 ? -2055829 : -1116943));
    }

    private static void t(GuiGraphicsExtractor ctx, String s, float x, float y, int color, float size, boolean bold) {
        LauncherGfx.text(ctx, s, x, y, color, size, bold);
    }

    private static float tw(String s, float size, boolean bold) {
        return LauncherGfx.width(s, size, bold);
    }

    public static void drawHud(GuiGraphicsExtractor ctx, Minecraft client) {
        int max;
        DonationStore.Data d = DonationStore.get();
        if (!d.topHud || !Features.isOn("fwm.tab.donations")) {
            return;
        }
        boolean editing = client.gui.screen() instanceof HudEditScreen;
        List<DonationStore.Rank> list = DonationBoard.top();
        boolean sample = list.isEmpty() && editing;
        int rows = max = d.topCount <= 3 ? 3 : 5;
        DonationStore.Entry last = d.topLast ? DonationStore.last() : null;
        boolean showLast = d.topLast;
        boolean goal = d.topGoal && d.goal > 0L;
        int head = 22;
        int body = rows * 18;
        int foot = (showLast ? 16 : 0) + (goal ? 18 : 0);
        int h = 8 + head + body + (foot > 0 ? 7 + foot : 0) + 8 - 4;
        float scale = FwmHud.scaleOf("donors");
        int dw = Math.round(168.0f * scale);
        int dh = Math.round((float)h * scale);
        int[] at = FwmHud.placeCard(ctx, "donors", dw, dh, 6, 142);
        Matrix3x2fStack m = ctx.pose();
        m.pushMatrix();
        m.translate((float)at[0], (float)at[1]);
        m.scale(scale, scale);
        Shapes.roundGradient(ctx, 0.0f, 0.0f, 168.0f, h, 9.0f, -1005310936, -804645356);
        Shapes.ring(ctx, 0.0f, 0.0f, 168.0f, h, 9.0f, 0x30FFFFFF);
        DonationBoard.crownIcon(ctx, 8.0f, 10.0f, 10.0f, -670398);
        DonationBoard.t(ctx, "Top Donater", 22.0f, 9.0f, -1116943, 8.0f, true);
        String period = DonationBoard.periodName(d.hudPeriod);
        float pw = DonationBoard.tw(period, 6.0f, true) + 10.0f;
        Shapes.round(ctx, 160.0f - pw, 8.0f, pw, 11.0f, 5.5f, 0x16FFFFFF);
        Shapes.ring(ctx, 160.0f - pw, 8.0f, pw, 11.0f, 5.5f, 0x22FFFFFF);
        DonationBoard.t(ctx, period, 160.0f - pw + 5.0f, 10.0f, -8523578, 6.0f, true);
        float y = 8 + head - 6;
        Shapes.round(ctx, 8.0f, y, 152.0f, 1.0f, 0.0f, 0x14FFFFFF);
        y += 4.0f;
        if (!sample && list.isEmpty()) {
            String[] empty = DonationBoard.emptyText(d.hudPeriod, DonationStore.last() != null);
            float my = y + (float)(rows * 18 - 2) / 2.0f - (float)empty.length * 5.0f + 1.0f;
            for (int k = 0; k < empty.length; ++k) {
                String line = LauncherGfx.fit(empty[k], 7.0f, false, 152.0f);
                DonationBoard.t(ctx, line, 84.0f - DonationBoard.tw(line, 7.0f, false) / 2.0f, my + (float)(k * 10), k == 0 ? -1280918092 : -2136556108, 7.0f, false);
            }
        }
        for (int i = 0; i < rows && (sample || !list.isEmpty()); ++i) {
            if (!sample && i >= list.size()) {
                float ey = y + (float)(i * 18) + 8.0f;
                Shapes.ring(ctx, 8.0f, ey - 5.0f, 10.0f, 10.0f, 5.0f, 0x30FFFFFF);
                String en = Integer.toString(i + 1);
                DonationBoard.t(ctx, en, 13.0f - DonationBoard.tw(en, 6.0f, true) / 2.0f, ey - 3.5f, -2136556108, 6.0f, true);
                DonationBoard.t(ctx, "\u2013", 38.0f, ey - 3.5f, -2136556108, 7.0f, false);
                continue;
            }
            String name = sample ? SAMPLE[i] : list.get(i).name();
            long sum = sample ? (long)(500 - i * 90) * 1000000L * 100L : list.get(i).sum();
            int color = DonationBoard.placeColor(i);
            float ry = y + (float)(i * 18);
            if (i == 0) {
                Shapes.round(ctx, 4.0f, ry, 160.0f, 16.0f, 6.0f, 485868866);
                Shapes.ring(ctx, 4.0f, ry, 160.0f, 16.0f, 6.0f, 1442170178);
            }
            float cy = ry + 8.0f;
            String n = Integer.toString(i + 1);
            if (i < 3) {
                Shapes.circle(ctx, 13.0f, cy, 5.0f, color);
                DonationBoard.t(ctx, n, 13.0f - DonationBoard.tw(n, 6.0f, true) / 2.0f, cy - 3.5f, -15068155, 6.0f, true);
            } else {
                Shapes.ring(ctx, 8.0f, cy - 5.0f, 10.0f, 10.0f, 5.0f, 0x40FFFFFF);
                DonationBoard.t(ctx, n, 13.0f - DonationBoard.tw(n, 6.0f, true) / 2.0f, cy - 3.5f, -6902107, 6.0f, true);
            }
            int hx = 23;
            int hy = Math.round(cy - 5.0f);
            FwmGfx.playerHead(ctx, name, hx, hy, 10);
            String money = Play.money(sum);
            float mw = DonationBoard.tw(money, 7.0f, true);
            String shown = LauncherGfx.fit(name, 7.0f, i == 0, 122.0f - mw - 6.0f);
            DonationBoard.t(ctx, shown, hx + 15, cy - 3.5f, i == 0 ? -670398 : -1116943, 7.0f, i == 0);
            DonationBoard.t(ctx, money, 160.0f - mw, cy - 3.5f, i < 3 ? color : -1116943, 7.0f, true);
        }
        y += (float)(rows * 18);
        if (foot > 0) {
            Shapes.round(ctx, 8.0f, y += 1.0f, 152.0f, 1.0f, 0.0f, 0x14FFFFFF);
            y += 6.0f;
        }
        if (showLast && last == null && !sample) {
            DonationBoard.t(ctx, Lang.t("Letzte", "Last"), 8.0f, y + 1.0f, -6902107, 6.5f, false);
            DonationBoard.t(ctx, "\u2013", 8.0f + DonationBoard.tw(Lang.t("Letzte", "Last"), 6.5f, false) + 6.0f, y + 1.0f, -2136556108, 6.5f, false);
            y += 16.0f;
        } else if (showLast) {
            String who = sample ? "TestSpender" : last.name;
            String amount = Play.money(sample ? 200000000L : last.amount);
            String label = Lang.t("Letzte", "Last");
            DonationBoard.t(ctx, label, 8.0f, y + 1.0f, -6902107, 6.5f, false);
            float lx = 8.0f + DonationBoard.tw(label, 6.5f, false) + 6.0f;
            FwmGfx.playerHead(ctx, who, Math.round(lx), Math.round(y + 0.5f), 8);
            float aw = DonationBoard.tw(amount, 6.5f, true);
            DonationBoard.t(ctx, LauncherGfx.fit(who, 6.5f, false, 160.0f - aw - 6.0f - (lx + 12.0f)), lx + 12.0f, y + 1.0f, -1116943, 6.5f, false);
            DonationBoard.t(ctx, amount, 160.0f - aw, y + 1.0f, -8523578, 6.5f, true);
            y += 16.0f;
        }
        if (goal) {
            long have = Donations.goalProgress();
            boolean done = have >= d.goal;
            String label = d.goalName == null || d.goalName.isBlank() ? Lang.t("Spendenziel", "Goal") : d.goalName;
            String figures = Play.money(have) + " / " + Play.money(d.goal);
            float fw = DonationBoard.tw(figures, 6.5f, false);
            DonationBoard.t(ctx, LauncherGfx.fit(label, 6.5f, true, 152.0f - fw - 6.0f), 8.0f, y, -1116943, 6.5f, true);
            DonationBoard.t(ctx, figures, 160.0f - fw, y, done ? -670398 : -6902107, 6.5f, false);
            float barW = 152.0f;
            float part = Math.min(1.0f, (float)have / (float)d.goal);
            Shapes.round(ctx, 8.0f, y + 11.0f, barW, 4.0f, 2.0f, 0x1FFFFFFF);
            if (part > 0.0f) {
                Shapes.round(ctx, 8.0f, y + 11.0f, Math.max(4.0f, barW * part), 4.0f, 2.0f, done ? -670398 : -8523578);
            }
        }
        m.popMatrix();
    }

    public static void crownIcon(GuiGraphicsExtractor ctx, float x, float y, float w, int color) {
        float[] cx;
        float h = w * 0.62f;
        float band = h * 0.42f;
        float p = w * 0.22f;
        Shapes.round(ctx, x, y + h - band, w, band, 0.0f, color);
        for (float c : cx = new float[]{x + p / 2.0f, x + w / 2.0f, x + w - p / 2.0f}) {
            Shapes.round(ctx, c - p / 2.0f, y + h * 0.3f, p, h - band - h * 0.3f + 0.5f, 0.0f, color);
            Shapes.round(ctx, c - p / 4.0f, y, p / 2.0f, h * 0.32f, 0.0f, color);
        }
    }

    private static Component label(int place) {
        String lang = Lang.t("de", "en");
        if (!lang.equals(labelsLang)) {
            labelsLang = lang;
            DonationBoard.LABELS[0] = Component.literal((String)"\u265b Top Donater").withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD});
            DonationBoard.LABELS[1] = Component.literal((String)"#2 Donater").withStyle(ChatFormatting.GRAY);
            DonationBoard.LABELS[2] = Component.literal((String)"#3 Donater").withStyle(ChatFormatting.GOLD);
        }
        return LABELS[place - 1];
    }

    private static void crowns(LevelRenderContext ctx) {
        DonationStore.Data d = DonationStore.get();
        if (!d.crown || !Donations.live()) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null || client.gui.hud.isHidden()) {
            return;
        }
        if (DonationBoard.top().isEmpty()) {
            return;
        }
        PoseStack matrices = ctx.poseStack();
        SubmitNodeCollector queue = ctx.submitNodeCollector();
        if (matrices == null || queue == null || ctx.levelState() == null) {
            return;
        }
        CameraRenderState cameraState = ctx.levelState().cameraRenderState;
        Camera camera = client.gameRenderer.mainCamera();
        if (cameraState == null || camera == null) {
            return;
        }
        float tickDelta = client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        Vec3 cam = camera.position();
        for (AbstractClientPlayer player : client.level.players()) {
            int place;
            if (player.getGameProfile() == null || player == client.player && client.options.getCameraType().isFirstPerson() || player != client.player && player.isInvisibleTo((Player)client.player) || (place = DonationBoard.place(player.getGameProfile().name())) == 0) continue;
            Vec3 pos = player.getPosition(tickDelta);
            double dx = pos.x - cam.x;
            double dy = pos.y - cam.y;
            double dz = pos.z - cam.z;
            double squared = dx * dx + dy * dy + dz * dz;
            if (squared > 4096.0) continue;
            Vec3 nameAt = player.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, player.getViewYRot(tickDelta));
            double nameY = nameAt != null ? nameAt.y : (double)player.getBbHeight() + 0.5;
            double y = nameY + 0.5 + MemberTags.belowNameLift(client.level, squared) + NoRisk.extraLift() + 0.26;
            matrices.pushPose();
            matrices.translate(dx, dy + y, dz);
            float s = place == 1 ? 0.6f : 0.5f;
            matrices.scale(s, s, s);
            queue.submitNameTag(matrices, ANCHOR, 0, DonationBoard.label(place), !player.isDiscrete(), 0xF000F0, cameraState);
            matrices.popPose();
        }
    }

    public static List<String> podium() {
        ArrayList<String> out = new ArrayList<String>();
        for (DonationStore.Rank r : DonationBoard.top()) {
            if (out.size() >= 3) break;
            out.add(r.name().toLowerCase(Locale.ROOT));
        }
        return out;
    }

    static {
        ANCHOR = new Vec3(0.0, -0.5, 0.0);
        LABELS = new MutableComponent[3];
        labelsLang = "";
    }
}

