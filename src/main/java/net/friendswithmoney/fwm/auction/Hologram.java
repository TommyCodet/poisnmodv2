/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
 *  net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font$DisplayMode
 *  net.minecraft.client.renderer.SubmitNodeCollector
 *  net.minecraft.client.renderer.item.ItemStackRenderState
 *  net.minecraft.client.renderer.rendertype.RenderTypes
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FontDescription
 *  net.minecraft.network.chat.FontDescription$Resource
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.Style
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 */
package net.friendswithmoney.fwm.auction;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.friendswithmoney.fwm.auction.Auction;
import net.friendswithmoney.fwm.auction.Giveaway;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.ui.FwmGfx;
import net.friendswithmoney.fwm.ui.FwmTheme;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.gfx.FwmFont;
import net.friendswithmoney.fwm.ui.gfx.Shapes;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public final class Hologram {
    private static final double DISTANCE = 5.0;
    private static final float UNIT = 0.016666668f;
    private static final int W = 240;
    private static final float PAD = 10.0f;
    private static final int GLASS = -435083742;
    private static final int RIM = -2005012282;
    private static final int MINT = -8523578;
    private static final int GOLD = -670398;
    private static final int RED = -38037;
    private static final int WHITE = -1116943;
    private static final int DIM = -7362144;
    private static Vec3 anchor;
    private static float anchorYaw;
    private static String anchorDim;
    private static final ItemStackRenderState ITEM;
    private static final int LIVE_ROWS = 5;
    private static final int TOP_ROWS = 3;

    private Hologram() {
    }

    public static void register() {
        LevelRenderEvents.AFTER_SOLID_FEATURES.register(Hologram::render);
    }

    public static void moveHere() {
        anchor = null;
    }

    private static void render(LevelRenderContext ctx) {
        boolean giveaway;
        Minecraft client = Minecraft.getInstance();
        if (!V2.INSTANCE.hologram || client.level == null || client.player == null || client.gui.hud.isHidden()) {
            return;
        }
        boolean auction = Auction.visible();
        boolean bl = giveaway = !auction && Giveaway.visible();
        if (!auction && !giveaway) {
            anchor = null;
            return;
        }
        String dim = client.level.dimension().identifier().toString();
        if (anchor == null || !dim.equals(anchorDim)) {
            float yaw = client.player.getYRot();
            Vec3 flat = Vec3.directionFromRotation((float)0.0f, (float)yaw);
            anchor = client.player.getEyePosition().add(flat.scale(5.0));
            anchorYaw = yaw;
            anchorDim = dim;
        }
        PoseStack matrices = ctx.poseStack();
        SubmitNodeCollector queue = ctx.submitNodeCollector();
        Camera camera = client.gameRenderer.mainCamera();
        if (matrices == null || queue == null || camera == null) {
            return;
        }
        Vec3 cam = camera.position();
        if (anchor.distanceToSqr(cam) > 4096.0) {
            return;
        }
        Board b = new Board(queue, matrices, 0xF000F0);
        matrices.pushPose();
        matrices.translate(Hologram.anchor.x - cam.x, Hologram.anchor.y - cam.y, Hologram.anchor.z - cam.z);
        matrices.mulPose((Quaternionfc)new Quaternionf().rotationY((float)Math.toRadians(180.0f - anchorYaw)));
        matrices.scale(0.016666668f, -0.016666668f, 0.016666668f);
        float h = auction ? (Auction.running() ? Hologram.liveHeight() : Hologram.endedHeight()) : 120.0f;
        float below = Math.min(h / 2.0f, 90.0f);
        matrices.translate(-120.0f, -(h - below), 0.0f);
        b.panel(0.0f, 0.0f, 240.0f, h);
        if (auction) {
            if (Auction.running()) {
                Hologram.live(b);
            } else {
                Hologram.ended(b);
            }
            Hologram.item(client, matrices, queue, Auction.item(), b.light);
        } else {
            Hologram.giveaway(b);
            Hologram.item(client, matrices, queue, Giveaway.displayStack(), b.light);
        }
        matrices.popPose();
    }

    private static void item(Minecraft client, PoseStack m, SubmitNodeCollector queue, ItemStack stack, int light) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        client.getItemModelResolver().updateForTopItem(ITEM, stack, ItemDisplayContext.FIXED, (Level)client.level, null, 0);
        m.pushPose();
        float bob = (float)Math.sin((double)System.currentTimeMillis() / 900.0) * 2.0f;
        m.translate(120.0f, -26.0f + bob, 0.0f);
        m.scale(40.0f, -40.0f, 40.0f);
        m.mulPose((Quaternionfc)new Quaternionf().rotationY((float)((double)(System.currentTimeMillis() % 8000L) / 8000.0 * Math.PI * 2.0)));
        ITEM.submit(m, queue, light, OverlayTexture.NO_OVERLAY, 0);
        m.popPose();
    }

    private static float liveHeight() {
        int rows = Math.min(5, Math.max(1, Auction.ranking().size()));
        return 104.0f + (float)(rows * 15) + 6.0f + 24.0f + 10.0f;
    }

    private static float endedHeight() {
        int rows = Math.min(3, Auction.ranking().size());
        int filtered = Auction.late().size() + Auction.blocked().size();
        float h = 92.0f + (float)(rows > 0 ? 12 + rows * 15 : 0);
        if (filtered > 0) {
            h += (float)(18 + Math.min(4, filtered) * 15);
        }
        if (Auction.fakePayments() > 0) {
            h += 13.0f;
        }
        if (Auction.leader() != null) {
            h += 26.0f;
        }
        return h + 10.0f;
    }

    private static void head(Board b, String me, String title, String sub, boolean live) {
        b.logo(10.0f, 9.0f, 22.0f);
        b.text(title, 37.0f, 11.0f, 8.6f, true, -1116943);
        b.text(sub, 37.0f, 22.0f, 6.2f, false, -7362144);
        float x = 230.0f;
        String status = live ? "\u25cf LIVE" : Lang.t("BEENDET", "ENDED");
        x -= b.pill(status, x, 10.0f, 6.0f, live ? 1090481003 : 0x26FFFFFF, live ? -38037 : -3615023, true);
        int filtered = Auction.late().size() + Auction.blocked().size();
        if (filtered > 0) {
            b.pill(filtered + Lang.t(" gefiltert", " filtered"), x -= 4.0f, 10.0f, 6.0f, 822045547, -38037, true);
        }
    }

    private static void live(Board b) {
        String me = FwmGfx.playerName();
        Hologram.head(b, me, me + Lang.t("s Auktion", "'s auction"), Hologram.itemLine(Auction.item()), true);
        float y0 = 36.0f;
        float cx = 33.0f;
        float cy = y0 + 23.0f;
        b.arc(cx, cy, 19.0f, 22.5f, 0.0f, 1.0f, 0.2f, 0x30FFFFFF);
        b.arc(cx, cy, 19.0f, 22.5f, 0.0f, Auction.progress(), 0.3f, -8523578);
        String clock = Hologram.clock(Auction.remainingMs());
        b.text(clock, cx - b.width(clock, 9.6f, true) / 2.0f, cy - 4.0f, 9.6f, true, -1116943);
        float rx = 64.0f;
        List<Auction.Bid> ranking = Auction.ranking();
        String leader = Auction.leader();
        if (leader == null) {
            b.text(Lang.t("NOCH KEIN GEBOT", "NO BID YET"), rx, y0 + 2.0f, 5.6f, true, -7362144);
            b.text(Lang.t("ab ", "from ") + Hologram.money(Auction.nextMinimum()), rx, y0 + 12.0f, 15.0f, true, -1116943);
        } else {
            b.text((Lang.t("H\u00d6CHSTGEBOT \u00b7 ", "HIGHEST BID \u00b7 ") + leader).toUpperCase(Locale.ROOT), rx, y0 + 2.0f, 5.6f, true, -7362144);
            b.text(Hologram.money(Auction.price()), rx, y0 + 12.0f, 17.0f, true, -1116943);
            if (ranking.size() > 1) {
                String over = "+" + Hologram.money(ranking.get(0).amount() - ranking.get(1).amount());
                b.text(over, rx, y0 + 32.0f, 6.8f, true, -8523578);
                b.text(Lang.t("\u00fcberboten", "outbid"), rx + b.width(over, 6.8f, true) + 3.0f, y0 + 32.0f, 6.8f, false, -7362144);
            }
        }
        float chipX = rx;
        float chipY = y0 + 42.0f;
        if (Auction.extendedTotal() > 0) {
            chipX += b.pill("+" + Auction.extendedTotal() + Lang.t(" s Nachspielzeit", " s extra time"), chipX, chipY, 5.6f, 871744834, -670398, false) + 3.0f;
        }
        if (FwmConfig.INSTANCE.auctionStackPayments) {
            chipX += b.pill("\u2713 Stacking", chipX, chipY, 5.6f, 645787846, -8523578, false) + 3.0f;
        }
        if (FwmConfig.INSTANCE.auctionAntiSnipe) {
            b.pill("\u2713 Anti-Snipe", chipX, chipY, 5.6f, 645787846, -8523578, false);
        }
        float y1 = y0 + 56.0f;
        b.text(Lang.t("GEBOTE", "BIDS"), 10.0f, y1, 5.6f, true, -7362144);
        String counts = Auction.bidderCount() + Lang.t(" Bieter \u00b7 ", " bidders \u00b7 ") + Auction.bidCount() + Lang.t(" Zahlungen", " payments");
        b.text(counts, 230.0f - b.width(counts, 5.8f, false), y1, 5.8f, false, -7362144);
        float ry = y1 + 10.0f;
        if (ranking.isEmpty()) {
            b.text(Lang.t("Noch keine Zahlungen", "No payments yet"), 16.0f, ry + 4.0f, 6.6f, false, -7362144);
            ry += 15.0f;
        }
        for (int i = 0; i < Math.min(5, ranking.size()); ++i) {
            Hologram.bidRow(b, ranking.get(i), i, i == 0 && leader != null, ry);
            ry += 15.0f;
        }
        float fy = ry + 6.0f;
        b.round(10.0f, fy, 220.0f, 20.0f, 6.0f, 0.2f, 0x18FFFFFF);
        List<Auction.Bid> blocked = Auction.blocked();
        String pay = "/pay " + FwmGfx.playerName() + " " + Lang.t("<Betrag>", "<amount>");
        float pw = b.width(pay, 6.4f, true) + 12.0f;
        b.round(226.0f - pw, fy + 3.5f, pw, 13.0f, 5.0f, 0.3f, -8523578);
        b.text(pay, 226.0f - pw + 6.0f, fy + 6.0f, 6.4f, true, -16045536);
        if (!blocked.isEmpty()) {
            float ns;
            Auction.Bid last = blocked.get(blocked.size() - 1);
            String note = last.name() + Lang.t(" ist gesperrt \u00b7 ", " is blocked \u00b7 ") + Hologram.money(last.amount()) + Lang.t(" nicht gewertet", " not counted");
            float room = 220.0f - pw - 14.0f;
            for (ns = 6.2f; ns > 4.8f && b.width(note, ns, false) > room; ns -= 0.2f) {
            }
            b.text(b.fit(note, ns, false, room), 16.0f, fy + 10.0f - ns * 0.56f, ns, false, -38037);
        } else {
            b.text(Lang.t("Mitbieten per /pay", "Bid with /pay"), 16.0f, fy + 6.5f, 6.2f, false, -7362144);
        }
    }

    private static void bidRow(Board b, Auction.Bid bid, int i, boolean leads, float ry) {
        Hologram.bidRow(b, bid, i, leads, ry, true);
    }

    private static void bidRow(Board b, Auction.Bid bid, int i, boolean leads, float ry, boolean pill) {
        if (leads) {
            b.round(10.0f, ry, 220.0f, 14.0f, 5.0f, 0.2f, -2005012282);
            b.round(10.6f, ry + 0.6f, 218.8f, 12.8f, 4.4f, 0.21f, 645787846);
        }
        b.text(String.valueOf(i + 1), 15.0f, ry + 3.5f, 6.4f, true, leads ? -8523578 : -7362144);
        b.face(bid.name(), 25.0f, ry + 3.0f, 8.0f);
        b.text(bid.name(), 37.0f, ry + 3.5f, 7.0f, true, -1116943);
        if (leads && pill) {
            b.pill(Lang.t("F\u00dcHRT", "LEADS"), 41.0f + b.width(bid.name(), 7.0f, true), ry + 2.0f, 5.2f, -8523578, -16045536, false);
        }
        String amount = Hologram.money(bid.amount());
        b.text(amount, 190.0f - b.width(amount, 7.0f, true), ry + 3.5f, 7.0f, true, -1116943);
        String ago = Hologram.ago(bid.time());
        b.text(ago, 226.0f - b.width(ago, 5.8f, false), ry + 4.0f, 5.8f, false, -7362144);
    }

    private static void ended(Board b) {
        String me = FwmGfx.playerName();
        Hologram.head(b, me, me + Lang.t("s Auktion", "'s auction"), Hologram.itemLine(Auction.item()), false);
        String winner = Auction.leader();
        float y0 = 36.0f;
        float cx = 33.0f;
        float cy = y0 + 23.0f;
        b.arc(cx, cy, 20.5f, 22.5f, 0.0f, 1.0f, 0.2f, -2005012282);
        b.disc(cx, cy, 20.5f, 0.15f, 0x30000000);
        if (winner != null) {
            b.face(winner, cx - 11.0f, cy - 11.0f, 22.0f);
        }
        float rx = 64.0f;
        String stats = Auction.bidderCount() + Lang.t(" Bieter \u00b7 ", " bidders \u00b7 ") + Auction.bidCount() + Lang.t(" Zahlungen \u00b7 Einnahmen ", " payments \u00b7 earned ") + Hologram.money(Auction.earnings());
        if (winner == null) {
            b.text(Lang.t("NICHT VERKAUFT", "NOT SOLD"), rx, y0 + 4.0f, 5.6f, true, -7362144);
            b.text(Lang.t("Kein Gebot", "No bid"), rx, y0 + 14.0f, 15.0f, true, -1116943);
        } else {
            b.text((Lang.t("GEWONNEN VON ", "WON BY ") + winner).toUpperCase(Locale.ROOT), rx, y0 + 4.0f, 5.6f, true, -7362144);
            b.text(Hologram.money(Auction.price()), rx, y0 + 14.0f, 17.0f, true, -1116943);
        }
        b.text(b.fit(stats, 6.0f, false, 240.0f - rx - 10.0f), rx, y0 + 35.0f, 6.0f, false, -7362144);
        float y = y0 + 56.0f;
        List<Auction.Bid> ranking = Auction.ranking();
        if (!ranking.isEmpty()) {
            b.text(Lang.t("TOP 3", "TOP 3"), 10.0f, y, 5.6f, true, -7362144);
            y += 10.0f;
            for (int i = 0; i < Math.min(3, ranking.size()); ++i) {
                Hologram.bidRow(b, ranking.get(i), i, i == 0 && winner != null, y, false);
                y += 15.0f;
            }
        }
        List<Auction.Bid> late = Auction.late();
        List<Auction.Bid> blocked = Auction.blocked();
        if (!late.isEmpty() || !blocked.isEmpty()) {
            b.text(Lang.t("RAUSGEFILTERT \u00b7 NICHT GEWERTET", "FILTERED OUT \u00b7 NOT COUNTED"), 10.0f, y += 6.0f, 5.6f, true, -38037);
            long due = Auction.refundDue();
            if (due > 0L) {
                String back = Hologram.money(due) + Lang.t(" zur\u00fcckzahlen", " to pay back");
                b.text(back, 230.0f - b.width(back, 5.8f, true), y, 5.8f, true, -38037);
            }
            y += 10.0f;
            int shown = 0;
            for (Auction.Bid bid : late) {
                if (shown++ >= 4) break;
                Hologram.filteredRow(b, bid, false, y);
                y += 15.0f;
            }
            for (Auction.Bid bid : blocked) {
                if (shown++ >= 4) break;
                Hologram.filteredRow(b, bid, true, y);
                y += 15.0f;
            }
        }
        if (Auction.fakePayments() > 0) {
            b.disc(14.0f, y + 6.0f, 3.0f, 0.2f, 0x30FFFFFF);
            b.text("i", 13.2f, y + 3.4f, 5.0f, true, -7362144);
            b.text(Auction.fakePayments() + Lang.t(" gef\u00e4lschte Zahlungen im Chat erkannt und ignoriert", " fake payments in the chat recognised and ignored"), 21.0f, y + 3.5f, 5.8f, false, -7362144);
            y += 13.0f;
        }
        if (winner != null) {
            b.round(10.0f, (y += 8.0f) - 4.0f, 220.0f, 0.6f, 0.3f, 0.2f, 0x18FFFFFF);
            b.face(winner, 10.0f, y + 3.0f, 9.0f);
            b.text(winner + Lang.t(" \u00fcbergeben:", " hand over:"), 23.0f, y + 4.0f, 6.6f, false, -3615023);
            float px = 230.0f;
            px -= b.pill("TPA-Here", px, y + 2.0f, 6.4f, 0x1CFFFFFF, -1116943, true) + 4.0f;
            b.pill(Lang.t("TPA senden", "Send TPA"), px, y + 2.0f, 6.4f, -8523578, -16045536, true);
        }
    }

    private static void filteredRow(Board b, Auction.Bid bid, boolean isBlocked, float ry) {
        b.round(10.0f, ry, 220.0f, 14.0f, 5.0f, 0.2f, 822045547);
        b.round(10.6f, ry + 0.6f, 218.8f, 12.8f, 4.4f, 0.21f, 452946795);
        b.face(bid.name(), 15.0f, ry + 3.0f, 8.0f);
        b.text(bid.name(), 27.0f, ry + 3.5f, 7.0f, true, -1116943);
        float tagX = 31.0f + b.width(bid.name(), 7.0f, true);
        if (isBlocked) {
            b.pill(Lang.t("gesperrt", "blocked"), tagX, ry + 2.5f, 5.2f, 1090481003, -38037, false);
        } else {
            b.pill(Lang.t("zu sp\u00e4t", "too late"), tagX, ry + 2.5f, 5.2f, 871744834, -670398, false);
        }
        String amount = Hologram.money(bid.amount());
        b.text(amount, 178.0f - b.width(amount, 7.0f, true), ry + 3.5f, 7.0f, true, -38037);
        String when = isBlocked ? Lang.t("w\u00e4hrend Auktion", "during auction") : Hologram.clockShort(bid.time() - Auction.endsAt()) + Lang.t(" nach Ende", " after end");
        b.text(when, 226.0f - b.width(when, 5.8f, false), ry + 4.0f, 5.8f, false, -7362144);
    }

    private static void giveaway(Board b) {
        String me = FwmGfx.playerName();
        boolean active = Giveaway.active();
        b.logo(10.0f, 9.0f, 22.0f);
        b.text(me + Lang.t("s Giveaway", "'s giveaway"), 37.0f, 11.0f, 8.6f, true, -1116943);
        b.text(Giveaway.prizeName(), 37.0f, 22.0f, 6.2f, false, -7362144);
        String status = active ? "\u25cf LIVE" : Lang.t("BEENDET", "ENDED");
        b.pill(status, 230.0f, 10.0f, 6.0f, active ? 1090481003 : 0x26FFFFFF, active ? -38037 : -3615023, true);
        float y0 = 36.0f;
        float cx = 33.0f;
        float cy = y0 + 23.0f;
        float rx = 64.0f;
        if (active) {
            b.arc(cx, cy, 19.0f, 22.5f, 0.0f, 1.0f, 0.2f, 0x30FFFFFF);
            b.arc(cx, cy, 19.0f, 22.5f, 0.0f, Giveaway.progress(), 0.3f, -8523578);
            String clock = Hologram.clock(Giveaway.remainingMs());
            b.text(clock, cx - b.width(clock, 9.6f, true) / 2.0f, cy - 4.0f, 9.6f, true, -1116943);
            b.text(Lang.t("TEILNEHMER", "ENTRANTS"), rx, y0 + 4.0f, 5.6f, true, -7362144);
            b.text(String.valueOf(Giveaway.participants().size()), rx, y0 + 14.0f, 17.0f, true, -1116943);
        } else {
            String winner = Giveaway.winner();
            b.arc(cx, cy, 20.5f, 22.5f, 0.0f, 1.0f, 0.2f, -2005012282);
            b.disc(cx, cy, 20.5f, 0.15f, 0x30000000);
            if (winner != null && !Giveaway.drawing()) {
                b.face(winner, cx - 11.0f, cy - 11.0f, 22.0f);
            }
            b.text(Lang.t("GEWONNEN VON", "WON BY"), rx, y0 + 4.0f, 5.6f, true, -7362144);
            b.text(winner == null ? "\u2013" : (Giveaway.drawing() ? "\u2026" : winner), rx, y0 + 14.0f, 15.0f, true, -1116943);
        }
        float fy = y0 + 54.0f;
        b.round(10.0f, fy, 220.0f, 20.0f, 6.0f, 0.2f, 0x18FFFFFF);
        String join = Giveaway.joinCommand();
        b.text(Lang.t("Mach mit:", "Join:"), 16.0f, fy + 6.5f, 6.2f, false, -7362144);
        float jw = b.width(join, 6.4f, true) + 12.0f;
        b.round(226.0f - jw, fy + 3.5f, jw, 13.0f, 5.0f, 0.3f, -8523578);
        b.text(join, 226.0f - jw + 6.0f, fy + 6.0f, 6.4f, true, -16045536);
    }

    private static String itemLine(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "?";
        }
        return stack.getCount() + "\u00d7 " + stack.getHoverName().getString();
    }

    private static String money(long cents) {
        return String.format(Locale.GERMANY, "%,d", cents / 100L);
    }

    private static String clock(long ms) {
        long s = Math.max(0L, (ms + 999L) / 1000L);
        return s >= 3600L ? String.format("%d:%02d:%02d", s / 3600L, s / 60L % 60L, s % 60L) : String.format("%d:%02d", s / 60L, s % 60L);
    }

    private static String clockShort(long ms) {
        long s = Math.max(0L, ms / 1000L);
        return String.format("%d:%02d", s / 60L, s % 60L);
    }

    private static String ago(long time) {
        long s = Math.max(0L, (System.currentTimeMillis() - time) / 1000L);
        return s < 60L ? Lang.t("vor " + s + " s", s + " s ago") : Lang.t("vor " + Hologram.clockShort(s * 1000L), Hologram.clockShort(s * 1000L) + " ago");
    }

    static {
        ITEM = new ItemStackRenderState();
    }

    private static final class Board {
        final SubmitNodeCollector queue;
        final PoseStack m;
        final int light;

        Board(SubmitNodeCollector queue, PoseStack m, int light) {
            this.queue = queue;
            this.m = m;
            this.light = light;
        }

        void panel(float x, float y, float w, float h) {
            this.round(x, y, w, h, 12.0f, 0.0f, -2005012282);
            this.round(x + 0.8f, y + 0.8f, w - 1.6f, h - 1.6f, 11.2f, 0.05f, -435083742);
            this.round(x + 12.0f, y + 1.2f, w - 24.0f, 0.6f, 0.3f, 0.1f, 0x22FFFFFF);
        }

        static int onGlass(int argb, float z) {
            if (z < 0.1f) {
                return argb;
            }
            int a = argb >>> 24 & 0xFF;
            if (a == 255) {
                return argb;
            }
            return 0xFF000000 | Shapes.mix(-435083742, argb | 0xFF000000, (float)a / 255.0f) & 0xFFFFFF;
        }

        void round(float x, float y, float w, float h, float r, float z, int argb0) {
            if (w <= 0.0f || h <= 0.0f) {
                return;
            }
            int argb = Board.onGlass(argb0, z);
            float rad = Math.min(r, Math.min(w, h) / 2.0f);
            this.queue.submitCustomGeometry(this.m, RenderTypes.textBackground(), (entry, c) -> {
                Matrix4f mat = entry.pose();
                Board.rect(c, mat, x + rad, y, x + w - rad, y + h, z, argb);
                Board.rect(c, mat, x, y + rad, x + rad, y + h - rad, z, argb);
                Board.rect(c, mat, x + w - rad, y + rad, x + w, y + h - rad, z, argb);
                Board.fan(c, mat, x + rad, y + rad, rad, Math.PI, 4.71238898038469, z, argb);
                Board.fan(c, mat, x + w - rad, y + rad, rad, 4.71238898038469, Math.PI * 2, z, argb);
                Board.fan(c, mat, x + w - rad, y + h - rad, rad, 0.0, 1.5707963267948966, z, argb);
                Board.fan(c, mat, x + rad, y + h - rad, rad, 1.5707963267948966, Math.PI, z, argb);
            });
        }

        void ringBox(float x, float y, float w, float h, float r, float z, int argb) {
            float t = 0.6f;
            this.round(x, y, w, t, Math.min(r, t / 2.0f), z, argb);
            this.round(x, y + h - t, w, t, Math.min(r, t / 2.0f), z, argb);
            this.round(x, y + r, t, h - 2.0f * r, t / 2.0f, z, argb);
            this.round(x + w - t, y + r, t, h - 2.0f * r, t / 2.0f, z, argb);
        }

        void disc(float cx, float cy, float r, float z, int argb0) {
            int argb = Board.onGlass(argb0, z);
            this.queue.submitCustomGeometry(this.m, RenderTypes.textBackground(), (entry, c) -> Board.fan(c, entry.pose(), cx, cy, r, 0.0, Math.PI * 2, z, argb));
        }

        void arc(float cx, float cy, float r0, float r1, float from, float to, float z, int argb0) {
            if (to <= from) {
                return;
            }
            int argb = Board.onGlass(argb0, z);
            this.queue.submitCustomGeometry(this.m, RenderTypes.textBackground(), (entry, c) -> {
                Matrix4f mat = entry.pose();
                int steps = Math.max(2, Math.round(48.0f * (to - from)));
                for (int i = 0; i < steps; ++i) {
                    double a0 = -1.5707963267948966 + Math.PI * 2 * (double)(from + (to - from) * (float)i / (float)steps);
                    double a1 = -1.5707963267948966 + Math.PI * 2 * (double)(from + (to - from) * (float)(i + 1) / (float)steps);
                    float ox0 = (float)((double)cx + Math.cos(a0) * (double)r1);
                    float oy0 = (float)((double)cy + Math.sin(a0) * (double)r1);
                    float ox1 = (float)((double)cx + Math.cos(a1) * (double)r1);
                    float oy1 = (float)((double)cy + Math.sin(a1) * (double)r1);
                    float ix0 = (float)((double)cx + Math.cos(a0) * (double)r0);
                    float iy0 = (float)((double)cy + Math.sin(a0) * (double)r0);
                    float ix1 = (float)((double)cx + Math.cos(a1) * (double)r0);
                    float iy1 = (float)((double)cy + Math.sin(a1) * (double)r0);
                    Board.both(c, mat, ix0, iy0, ox0, oy0, ox1, oy1, ix1, iy1, z, argb);
                }
            });
        }

        float pill(String label, float at, float y, float size, int fill, int text, boolean fromRight) {
            float w = this.width(label, size, true) + 10.0f;
            float x = fromRight ? at - w : at;
            this.round(x, y, w, size + 5.0f, (size + 5.0f) / 2.0f, 0.4f, fill);
            this.text(label, x + 5.0f, y + 2.5f, size, true, text);
            return w;
        }

        void face(String name, float x, float y, float size) {
            Identifier skin = FwmGfx.skinTexture(name);
            this.queue.submitCustomGeometry(this.m, RenderTypes.entityCutout((Identifier)skin), (entry, c) -> {
                Matrix4f mat = entry.pose();
                Board.tex(c, mat, x, y, x + size, y + size, 0.125f, 0.125f, 0.25f, 0.25f, 0.6f, this.light);
                Board.tex(c, mat, x - size * 0.06f, y - size * 0.06f, x + size * 1.06f, y + size * 1.06f, 0.625f, 0.125f, 0.75f, 0.25f, 0.62f, this.light);
            });
        }

        void logo(float x, float y, float w) {
            float h = w / 1.3333334f;
            float s = 256.0f;
            this.queue.submitCustomGeometry(this.m, RenderTypes.entityCutout((Identifier)FwmTheme.LOGO), (entry, c) -> Board.tex(c, entry.pose(), x, y, x + w, y + h, 0.0f, 0.0f, 256.0f / s, 192.0f / s, 0.6f, this.light));
        }

        void text(String s, float x, float y, float size, boolean bold, int color) {
            MutableComponent t;
            this.m.pushPose();
            this.m.translate(x, y, 0.9f);
            if (FwmFont.enabled()) {
                t = Component.literal((String)s).setStyle(Style.EMPTY.withFont((FontDescription)new FontDescription.Resource(FwmFont.worldFont(size))).withBold(Boolean.valueOf(bold)));
            } else {
                float k = size / 8.0f;
                this.m.translate(0.0f, 3.5f - 3.5f * k, 0.0f);
                this.m.scale(k, k, 1.0f);
                t = bold ? Component.literal((String)s).withStyle(st -> st.withBold(Boolean.valueOf(true))) : Component.literal((String)s);
            }
            this.queue.submitText(this.m, 0.0f, 0.0f, t.getVisualOrderText(), false, Font.DisplayMode.POLYGON_OFFSET, this.light, color, 0, 0);
            this.m.popPose();
        }

        float width(String s, float size, boolean bold) {
            if (FwmFont.enabled()) {
                MutableComponent t = bold ? Component.literal((String)s).withStyle(st -> st.withBold(Boolean.valueOf(true))) : Component.literal((String)s);
                return FwmFont.renderer(size).width((FormattedText)t);
            }
            MutableComponent t = bold ? Component.literal((String)s).withStyle(st -> st.withBold(Boolean.valueOf(true))) : Component.literal((String)s);
            return (float)Minecraft.getInstance().font.width((FormattedText)t) * size / 8.0f;
        }

        String fit(String s, float size, boolean bold, float max) {
            if (this.width(s, size, bold) <= max) {
                return s;
            }
            String cut = s;
            while (cut.length() > 1 && this.width(cut + "\u2026", size, bold) > max) {
                cut = cut.substring(0, cut.length() - 1);
            }
            return cut + "\u2026";
        }

        private static void rect(VertexConsumer c, Matrix4f m, float x0, float y0, float x1, float y1, float z, int argb) {
            if (x1 <= x0 || y1 <= y0) {
                return;
            }
            Board.both(c, m, x0, y0, x0, y1, x1, y1, x1, y0, z, argb);
        }

        private static void fan(VertexConsumer c, Matrix4f m, float cx, float cy, float r, double a0, double a1, float z, int argb) {
            int steps = Math.max(3, (int)Math.ceil((a1 - a0) / 1.5707963267948966 * 6.0));
            for (int i = 0; i < steps; ++i) {
                double p = a0 + (a1 - a0) * (double)i / (double)steps;
                double q = a0 + (a1 - a0) * (double)(i + 1) / (double)steps;
                float x1 = (float)((double)cx + Math.cos(p) * (double)r);
                float y1 = (float)((double)cy + Math.sin(p) * (double)r);
                float x2 = (float)((double)cx + Math.cos(q) * (double)r);
                float y2 = (float)((double)cy + Math.sin(q) * (double)r);
                Board.both(c, m, cx, cy, x1, y1, x2, y2, x2, y2, z, argb);
            }
        }

        private static void both(VertexConsumer c, Matrix4f m, float ax, float ay, float bx, float by, float cx, float cy, float dx, float dy, float z, int argb) {
            int light = 0xF000F0;
            c.addVertex((Matrix4fc)m, ax, ay, z).setColor(argb).setLight(light);
            c.addVertex((Matrix4fc)m, bx, by, z).setColor(argb).setLight(light);
            c.addVertex((Matrix4fc)m, cx, cy, z).setColor(argb).setLight(light);
            c.addVertex((Matrix4fc)m, dx, dy, z).setColor(argb).setLight(light);
            c.addVertex((Matrix4fc)m, ax, ay, z).setColor(argb).setLight(light);
            c.addVertex((Matrix4fc)m, dx, dy, z).setColor(argb).setLight(light);
            c.addVertex((Matrix4fc)m, cx, cy, z).setColor(argb).setLight(light);
            c.addVertex((Matrix4fc)m, bx, by, z).setColor(argb).setLight(light);
        }

        private static void tex(VertexConsumer c, Matrix4f m, float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1, float z, int light) {
            c.addVertex((Matrix4fc)m, x0, y0, z).setColor(-1).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0.0f, 0.0f, 1.0f);
            c.addVertex((Matrix4fc)m, x0, y1, z).setColor(-1).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0.0f, 0.0f, 1.0f);
            c.addVertex((Matrix4fc)m, x1, y1, z).setColor(-1).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0.0f, 0.0f, 1.0f);
            c.addVertex((Matrix4fc)m, x1, y0, z).setColor(-1).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0.0f, 0.0f, 1.0f);
        }
    }
}

