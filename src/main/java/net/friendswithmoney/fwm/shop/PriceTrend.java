/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.Window
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 */
package net.friendswithmoney.fwm.shop;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import java.util.List;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.net.Market;
import net.friendswithmoney.fwm.net.MarketHistoryData;
import net.friendswithmoney.fwm.shop.MarketTooltip;
import net.friendswithmoney.fwm.ui.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class PriceTrend {
    private static final char[] BARS = new char[]{'\u2581', '\u2582', '\u2583', '\u2584', '\u2585', '\u2586', '\u2587', '\u2588'};

    private PriceTrend() {
    }

    public static void decorate(MutableComponent ahLine, String key, List<Component> extra) {
        if (!Features.isOn("fwm.feature.pricetrend") || key == null || key.contains("#")) {
            return;
        }
        Market.ensureChanges();
        if (Market.changeSpan() < 3) {
            return;
        }
        Double change = Market.change(key);
        if (change == null || change.isNaN() || change.isInfinite()) {
            return;
        }
        ahLine.append((Component)Component.literal((String)("  " + PriceTrend.arrow(change))).withStyle(PriceTrend.color(change)));
        if (!V2.INSTANCE.trendGraph) {
            return;
        }
        if (!PriceTrend.shift()) {
            return;
        }
        Market.requestHistory(key);
        MarketHistoryData.History h = Market.history(key);
        if (h == null) {
            extra.add((Component)Component.literal((String)Lang.t("Verlauf l\u00e4dt \u2026", "Loading history \u2026")).withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        String g = PriceTrend.graph(h.list());
        if (g == null) {
            return;
        }
        extra.add((Component)Component.literal((String)Lang.t("7 Tage: ", "7 days: ")).withStyle(ChatFormatting.GRAY).append((Component)Component.literal((String)g).withStyle(PriceTrend.color(change))));
        double[] mm = PriceTrend.minMax(h.list());
        if (mm != null) {
            extra.add((Component)Component.literal((String)("  min " + MarketTooltip.money(Math.round(mm[0] * 100.0)) + "  max " + MarketTooltip.money(Math.round(mm[1] * 100.0)))).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    static String arrow(double pct) {
        if (Math.abs(pct) < 2.0) {
            return "\u25ba \u00b10 %";
        }
        long r = Math.round(pct);
        return (pct > 0.0 ? "\u25b2 +" : "\u25bc ") + r + " %";
    }

    private static ChatFormatting color(double pct) {
        if (Math.abs(pct) < 2.0) {
            return ChatFormatting.GRAY;
        }
        return pct > 0.0 ? ChatFormatting.GREEN : ChatFormatting.RED;
    }

    private static boolean shift() {
        Window window = Minecraft.getInstance().getWindow();
        return InputConstants.isKeyDown((Window)window, (int)340) || InputConstants.isKeyDown((Window)window, (int)344);
    }

    private static double price(MarketHistoryData.Point p) {
        if (p.ah != null && p.ah > 0.0) {
            return p.ah;
        }
        if (p.orders != null && p.orders > 0.0) {
            return p.orders;
        }
        return p.p;
    }

    static String graph(List<MarketHistoryData.Point> points) {
        int from = Math.max(0, points.size() - 7);
        int n = points.size() - from;
        if (n < 3) {
            return null;
        }
        double[] mm = PriceTrend.minMax(points);
        if (mm == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(n);
        for (int i = from; i < points.size(); ++i) {
            double v = PriceTrend.price(points.get(i));
            int idx = mm[1] - mm[0] < 1.0E-9 ? 3 : (int)Math.round((v - mm[0]) / (mm[1] - mm[0]) * (double)(BARS.length - 1));
            sb.append(BARS[Math.max(0, Math.min(BARS.length - 1, idx))]);
        }
        return sb.toString();
    }

    private static double[] minMax(List<MarketHistoryData.Point> points) {
        double[] dArray;
        int from = Math.max(0, points.size() - 7);
        double min = Double.MAX_VALUE;
        double max = -1.7976931348623157E308;
        for (int i = from; i < points.size(); ++i) {
            double v = PriceTrend.price(points.get(i));
            if (v <= 0.0) continue;
            min = Math.min(min, v);
            max = Math.max(max, v);
        }
        if (max < 0.0) {
            dArray = null;
        } else {
            double[] dArray2 = new double[2];
            dArray2[0] = min;
            dArray = dArray2;
            dArray2[1] = max;
        }
        return dArray;
    }
}

