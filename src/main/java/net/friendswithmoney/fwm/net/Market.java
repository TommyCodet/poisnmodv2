/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.client.Minecraft
 */
package net.friendswithmoney.fwm.net;

import com.google.gson.Gson;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.loader.api.FabricLoader;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.net.FwmApi;
import net.friendswithmoney.fwm.net.MarketData;
import net.friendswithmoney.fwm.net.MarketHistoryData;
import net.friendswithmoney.fwm.net.MarketStatsData;
import net.minecraft.client.Minecraft;

public final class Market {
    private static final int PAGE = 100;
    private static final long STALE_MS = 300000L;
    private static final int STATS_CACHE = 50;
    private static final Gson GSON = new Gson();
    private static final Comparator<MarketData.Item> BY_PRICE = Comparator.comparingInt(item -> Market.avgPrice(item.itemKey) != null ? 1 : 0).thenComparingDouble(item -> {
        Double avg = Market.avgPrice(item.itemKey);
        if (avg != null) {
            return avg;
        }
        double price = Market.number(item.highestUnitPrice24h);
        return Double.isNaN(price) ? -1.0 : price;
    }).reversed();
    private static final List<MarketData.Item> ITEMS = new ArrayList<MarketData.Item>();
    private static final List<MarketData.Item> ALL = new ArrayList<MarketData.Item>();
    private static final Map<String, MarketStatsData> STATS = new LinkedHashMap<String, MarketStatsData>(16, 0.75f, true){

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, MarketStatsData> eldest) {
            return this.size() > 50;
        }
    };
    private static final Set<String> STATS_PENDING = new HashSet<String>();
    private static final Set<String> STATS_FAILED = new HashSet<String>();
    private static volatile Map<String, Double> changes = Map.of();
    private static volatile Map<String, Double> avgPrices = Map.of();
    private static int changeSpan;
    private static boolean changesBusy;
    private static final Map<String, MarketHistoryData.History> HISTORY;
    private static final Set<String> HISTORY_PENDING;
    private static final Set<String> HISTORY_FAILED;
    private static String query;
    private static State allState;
    private static boolean allBusy;
    private static long allFetchedAt;
    private static State searchState;
    private static boolean searchBusy;
    private static int searchTotal;
    private static boolean searchHasMore;
    private static long searchFetchedAt;
    private static int generation;
    private static long changesAt;

    private Market() {
    }

    private static boolean searching() {
        return !query.isBlank();
    }

    public static State state() {
        return Market.searching() ? searchState : allState;
    }

    public static List<MarketData.Item> items() {
        return ITEMS;
    }

    public static int totalItems() {
        return Market.searching() ? searchTotal : ALL.size();
    }

    public static boolean hasMore() {
        return Market.searching() && searchHasMore;
    }

    public static boolean busy() {
        return Market.searching() ? searchBusy : allBusy;
    }

    public static String query() {
        return query;
    }

    public static long ageMinutes() {
        long at;
        long l = at = Market.searching() ? searchFetchedAt : allFetchedAt;
        if (at == 0L) {
            return -1L;
        }
        return (System.currentTimeMillis() - at) / 60000L;
    }

    public static void openPage() {
        if (changes.isEmpty()) {
            Market.loadChanges();
        }
        if (Market.searching()) {
            if (!searchBusy && ITEMS.isEmpty()) {
                Market.requestSearch(0, query);
            }
            return;
        }
        Market.showAll();
        if (ALL.isEmpty() || System.currentTimeMillis() - allFetchedAt >= 300000L) {
            Market.loadAll();
        }
    }

    public static void search(String text) {
        String next;
        String string = next = text == null ? "" : text.trim();
        if (next.equals(query)) {
            return;
        }
        query = next;
        ++generation;
        searchBusy = false;
        if (next.isEmpty()) {
            Market.openPage();
            return;
        }
        searchTotal = 0;
        searchHasMore = false;
        Market.requestSearch(0, next);
    }

    public static void refresh() {
        HISTORY.clear();
        HISTORY_FAILED.clear();
        Market.loadChanges();
        if (Market.searching()) {
            if (!searchBusy) {
                Market.requestSearch(0, query);
            }
            return;
        }
        Market.loadAll();
    }

    public static void loadMore() {
        if (!Market.searching() || searchBusy || !searchHasMore) {
            return;
        }
        Market.requestSearch(ITEMS.size(), query);
    }

    private static void showAll() {
        ITEMS.clear();
        ITEMS.addAll(ALL);
    }

    private static void loadAll() {
        Market.loadChanges();
        if (allBusy) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        allBusy = true;
        if (ALL.isEmpty()) {
            allState = State.LOADING;
        }
        ((CompletableFuture)FwmApi.getUrl(Market.overviewUrl(0, ""), MarketData.class).thenCompose(first -> {
            if (first == null || first.items == null) {
                throw new IllegalStateException("empty response");
            }
            int total = first.meta == null ? first.list().size() : first.meta.totalItems;
            ArrayList<CompletableFuture<MarketData>> rest = new ArrayList<CompletableFuture<MarketData>>();
            for (int offset = first.list().size(); offset < total; offset += 100) {
                rest.add(FwmApi.getUrl(Market.overviewUrl(offset, ""), MarketData.class));
            }
            return CompletableFuture.allOf(rest.toArray(new CompletableFuture[0])).thenApply(done -> {
                LinkedHashMap<String, MarketData.Item> merged = new LinkedHashMap<String, MarketData.Item>();
                for (MarketData.Item item : first.list()) {
                    merged.put(Market.key(item), item);
                }
                for (CompletableFuture page : rest) {
                    for (MarketData.Item item : ((MarketData)page.join()).list()) {
                        merged.putIfAbsent(Market.key(item), item);
                    }
                }
                ArrayList sorted = new ArrayList(merged.values());
                sorted.sort(BY_PRICE);
                return sorted;
            });
        })).whenCompleteAsync((sorted, error) -> {
            if (error != null) {
                System.err.println("[FWM] Market fetch failed: " + error.getMessage());
            } else {
                Market.save(sorted);
            }
            client.execute(() -> {
                allBusy = false;
                if (error != null) {
                    allState = ALL.isEmpty() ? State.ERROR : State.OK;
                    return;
                }
                ALL.clear();
                ALL.addAll((Collection<MarketData.Item>)sorted);
                ALL.sort(BY_PRICE);
                allState = State.OK;
                allFetchedAt = System.currentTimeMillis();
                if (!Market.searching()) {
                    Market.showAll();
                }
            });
        }, FwmApi.executor());
    }

    private static String key(MarketData.Item item) {
        return item.itemKey == null ? "" : item.itemKey;
    }

    private static void requestSearch(int offset, String forQuery) {
        Minecraft client = Minecraft.getInstance();
        searchBusy = true;
        if (offset == 0) {
            ITEMS.clear();
            searchState = State.LOADING;
        }
        int mine = ++generation;
        FwmApi.getUrl(Market.overviewUrl(offset, forQuery), MarketData.class).whenCompleteAsync((result, error) -> {
            boolean failed;
            boolean bl = failed = error != null || result == null || result.items == null;
            if (failed) {
                System.err.println("[FWM] Market search failed: " + (error == null ? "empty response" : error.getMessage()));
            }
            client.execute(() -> {
                if (mine != generation) {
                    return;
                }
                searchBusy = false;
                if (failed) {
                    searchState = ITEMS.isEmpty() ? State.ERROR : State.OK;
                    return;
                }
                ITEMS.addAll(result.list());
                if (result.meta != null) {
                    searchTotal = result.meta.totalItems;
                    searchHasMore = result.meta.hasMore;
                } else {
                    searchHasMore = false;
                }
                searchState = State.OK;
                searchFetchedAt = System.currentTimeMillis();
            });
        }, FwmApi.executor());
    }

    public static Double change(String itemKey) {
        return itemKey == null ? null : changes.get(itemKey);
    }

    public static Double avgPrice(String itemKey) {
        return itemKey == null ? null : avgPrices.get(itemKey);
    }

    public static int changeSpan() {
        return changeSpan;
    }

    public static void ensureChanges() {
        long now = System.currentTimeMillis();
        if (changesBusy || now - changesAt < 1800000L) {
            return;
        }
        changesAt = now;
        Market.loadChanges();
    }

    private static void loadChanges() {
        if (changesBusy) {
            return;
        }
        changesBusy = true;
        Minecraft client = Minecraft.getInstance();
        FwmApi.getUrl(Market.historyBase() + "/changes", MarketHistoryData.Changes.class).whenCompleteAsync((result, error) -> client.execute(() -> {
            changesBusy = false;
            if (error != null || result == null || result.items == null) {
                System.err.println("[FWM] Market changes failed: " + (error == null ? "empty response" : error.getMessage()));
                return;
            }
            changes = result.items;
            changeSpan = result.span;
            avgPrices = result.prices == null ? Map.of() : result.prices;
            ALL.sort(BY_PRICE);
            if (!Market.searching()) {
                Market.showAll();
            }
        }), FwmApi.executor());
    }

    public static MarketHistoryData.History history(String itemKey) {
        return itemKey == null ? null : HISTORY.get(itemKey);
    }

    public static boolean historyFailed(String itemKey) {
        return itemKey != null && HISTORY_FAILED.contains(itemKey);
    }

    public static void requestHistory(String itemKey) {
        if (itemKey == null || itemKey.isBlank()) {
            return;
        }
        if (HISTORY.containsKey(itemKey) || HISTORY_PENDING.contains(itemKey)) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        HISTORY_PENDING.add(itemKey);
        HISTORY_FAILED.remove(itemKey);
        FwmApi.getUrl(Market.historyBase() + "/history?item=" + Market.encode(itemKey), MarketHistoryData.History.class).whenCompleteAsync((result, error) -> client.execute(() -> {
            HISTORY_PENDING.remove(itemKey);
            if (error != null || result == null) {
                System.err.println("[FWM] Market history failed for " + itemKey + ": " + (error == null ? "empty response" : error.getMessage()));
                HISTORY_FAILED.add(itemKey);
                return;
            }
            HISTORY.put(itemKey, (MarketHistoryData.History)result);
        }), FwmApi.executor());
    }

    private static String historyBase() {
        return FwmConfig.INSTANCE.marketHistoryUrl.replaceAll("/+$", "");
    }

    public static MarketStatsData stats(String itemKey) {
        return itemKey == null ? null : STATS.get(itemKey);
    }

    public static boolean statsFailed(String itemKey) {
        return itemKey != null && STATS_FAILED.contains(itemKey);
    }

    public static void requestStats(String itemKey) {
        if (itemKey == null || itemKey.isBlank()) {
            return;
        }
        if (STATS.containsKey(itemKey) || STATS_PENDING.contains(itemKey)) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        STATS_PENDING.add(itemKey);
        STATS_FAILED.remove(itemKey);
        FwmApi.getUrl(Market.statsUrl(itemKey), MarketStatsData.class).whenCompleteAsync((result, error) -> client.execute(() -> {
            STATS_PENDING.remove(itemKey);
            if (error != null || result == null) {
                System.err.println("[FWM] Market figures failed for " + itemKey + ": " + (error == null ? "empty response" : error.getMessage()));
                STATS_FAILED.add(itemKey);
                return;
            }
            STATS.put(itemKey, (MarketStatsData)result);
        }), FwmApi.executor());
    }

    private static String base() {
        return FwmConfig.INSTANCE.marketApiUrl.replaceAll("/+$", "");
    }

    private static String overviewUrl(int offset, String forQuery) {
        StringBuilder url = new StringBuilder(Market.base()).append("/overview?limit=").append(100).append("&offset=").append(Math.max(0, offset)).append("&sort=entries_desc");
        if (forQuery != null && !forQuery.isBlank()) {
            url.append("&q=").append(Market.encode(forQuery));
        }
        return url.toString();
    }

    private static String statsUrl(String itemKey) {
        return Market.base() + "/items/" + Market.encode(itemKey) + "/stats";
    }

    private static String encode(String raw) {
        return URLEncoder.encode(raw, StandardCharsets.UTF_8).replace("+", "%20");
    }

    public static double number(String raw) {
        if (raw == null || raw.isBlank()) {
            return Double.NaN;
        }
        try {
            return Double.parseDouble(raw.trim());
        }
        catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    public static long rounded(String raw) {
        double value = Market.number(raw);
        return Double.isNaN(value) ? 0L : Math.round(value);
    }

    public static String lower(String raw) {
        return raw == null ? "" : raw.toLowerCase(Locale.ROOT);
    }

    public static void load() {
        Path path = Market.cacheFile();
        if (!Files.exists(path, new LinkOption[0])) {
            return;
        }
        try {
            Snapshot snapshot = (Snapshot)GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), Snapshot.class);
            if (snapshot == null || snapshot.items == null || snapshot.items.isEmpty()) {
                return;
            }
            ALL.clear();
            ALL.addAll(snapshot.items);
            ALL.sort(BY_PRICE);
            allFetchedAt = snapshot.complete ? snapshot.fetchedAt : 0L;
            allState = State.OK;
            Market.showAll();
        }
        catch (Exception e) {
            System.err.println("[FWM] Could not read " + String.valueOf(path) + ": " + e.getMessage());
        }
    }

    private static void save(List<MarketData.Item> sorted) {
        Snapshot snapshot = new Snapshot();
        snapshot.fetchedAt = System.currentTimeMillis();
        snapshot.complete = true;
        snapshot.items = sorted;
        Path path = Market.cacheFile();
        try {
            Files.createDirectories(path.getParent(), new FileAttribute[0]);
            Files.writeString(path, (CharSequence)GSON.toJson((Object)snapshot), StandardCharsets.UTF_8, new OpenOption[0]);
        }
        catch (Exception e) {
            System.err.println("[FWM] Could not write " + String.valueOf(path) + ": " + e.getMessage());
        }
    }

    private static Path cacheFile() {
        return FabricLoader.getInstance().getConfigDir().resolve("fwm-market.json");
    }

    static {
        HISTORY = new LinkedHashMap<String, MarketHistoryData.History>(16, 0.75f, true){

            @Override
            protected boolean removeEldestEntry(Map.Entry<String, MarketHistoryData.History> eldest) {
                return this.size() > 50;
            }
        };
        HISTORY_PENDING = new HashSet<String>();
        HISTORY_FAILED = new HashSet<String>();
        query = "";
        allState = State.EMPTY;
        searchState = State.EMPTY;
    }

    public static enum State {
        EMPTY,
        LOADING,
        OK,
        ERROR;

    }

    private static class Snapshot {
        long fetchedAt;
        boolean complete;
        List<MarketData.Item> items;

        private Snapshot() {
        }
    }
}

