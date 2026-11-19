/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 */
package net.friendswithmoney.fwm.net;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.net.FwmApi;
import net.minecraft.client.Minecraft;

public final class MarketPrices {
    private static final long REFRESH_MS = 3600000L;
    private static final long RETRY_MS = 600000L;
    private static volatile Map<String, Double[]> items = Map.of();
    private static volatile Map<String, String> byName = Map.of();
    private static volatile Map<String, String> shown = Map.of();
    private static boolean busy;
    private static long nextLoad;
    private static int requests;

    private MarketPrices() {
    }

    public static void ensure() {
        if (!Features.isOn("fwm.feature.ahprice")) {
            return;
        }
        if (busy || System.currentTimeMillis() < nextLoad) {
            return;
        }
        busy = true;
        ++requests;
        Minecraft client = Minecraft.getInstance();
        FwmApi.getUrl(MarketPrices.base() + "/prices", Data.class).whenCompleteAsync((result, error) -> client.execute(() -> {
            busy = false;
            if (error != null || result == null || result.items == null) {
                System.err.println("[FWM] Market prices failed: " + (error == null ? "empty response" : error.getMessage()));
                nextLoad = System.currentTimeMillis() + 600000L;
                return;
            }
            items = result.items;
            HashMap<String, String> names = new HashMap<String, String>();
            if (result.names != null) {
                result.names.forEach((key, name) -> names.put(MarketPrices.normalize(name), (String)key));
                shown = new HashMap<String, String>(result.names);
            }
            byName = names;
            nextLoad = System.currentTimeMillis() + 3600000L;
        }), FwmApi.executor());
    }

    public static Double[] get(String key) {
        return key == null ? null : items.get(key);
    }

    public static boolean ahOutlier(Double[] p) {
        if (p == null || p.length < 2 || p[0] == null || p[1] == null) {
            return false;
        }
        return p[1] > 0.0 && p[0] > 500.0 * p[1];
    }

    public static double ahForValue(Double[] p) {
        if (p == null || p.length < 1 || p[0] == null) {
            return 0.0;
        }
        return MarketPrices.ahOutlier(p) ? p[1] : p[0];
    }

    public static String keyForName(String displayName) {
        return displayName == null ? null : byName.get(MarketPrices.normalize(displayName));
    }

    public static String nameFor(String key) {
        return key == null ? null : shown.get(key);
    }

    public static int requests() {
        return requests;
    }

    private static String normalize(String name) {
        return name.replaceAll("\u00a7.", "").toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N} ]", " ").replaceAll("\\s+", " ").trim();
    }

    private static String base() {
        return FwmConfig.INSTANCE.marketHistoryUrl.replaceAll("/+$", "");
    }

    public static class Data {
        public boolean ok;
        public String day;
        public Map<String, Double[]> items;
        public Map<String, String> names;
    }
}

