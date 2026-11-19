/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.reflect.TypeToken
 *  net.fabricmc.loader.api.FabricLoader
 */
package net.friendswithmoney.fwm.stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import net.friendswithmoney.fwm.net.ServerContext;
import net.friendswithmoney.fwm.stream.DonationTier;
import net.friendswithmoney.fwm.ui.Lang;

public final class DonationStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int HISTORY = 200;
    public static final int MAX_TIERS = 10;
    public static final int MAX_QUEUE = 10;
    public static Data data = new Data();
    private static boolean loaded;
    private static boolean dirty;
    private static long dirtyAt;
    public static int revision;
    private static int ticks;
    private static int blacklistHash;
    private static final String EXPORT_PREFIX = "FWM-DONATIONS:";
    static final Type TIER_LIST;

    private DonationStore() {
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("fwm").resolve("donations.json");
    }

    public static Data get() {
        if (!loaded) {
            DonationStore.load();
        }
        return data;
    }

    public static void load() {
        loaded = true;
        Path path = DonationStore.file();
        Data read = null;
        if (Files.exists(path, new LinkOption[0])) {
            try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);){
                read = (Data)GSON.fromJson((Reader)reader, Data.class);
            }
            catch (Exception e) {
                System.err.println("[FWM] donations.json could not be read: " + e.getMessage());
            }
        }
        data = read == null ? new Data() : read;
        DonationStore.repair();
        ++revision;
    }

    private static void repair() {
        Data d = data;
        if (d.blacklist == null) {
            d.blacklist = new ArrayList<String>();
        }
        if (d.history == null) {
            d.history = new ArrayList<Entry>();
        }
        if (d.totals == null) {
            d.totals = new HashMap<String, Total>();
        }
        if (d.profiles == null) {
            d.profiles = new LinkedHashMap<String, List<DonationTier>>();
        }
        if (d.profiles.isEmpty()) {
            d.profiles.put("Stream", DonationTier.defaults());
        }
        for (List<DonationTier> list : d.profiles.values()) {
            if (list == null) continue;
            list.removeIf(t -> t == null);
            block21: for (DonationTier t2 : list) {
                t2.repair();
                if (t2.name != null) {
                    switch (t2.name) {
                        case "Kleingeld": {
                            t2.name = "Small Donation";
                            break;
                        }
                        case "DONATION": {
                            t2.name = "Donation";
                            break;
                        }
                        case "BIG DONATION": {
                            t2.name = "Big Donation";
                            break;
                        }
                        case "CRAZY CRAZY DONATION": {
                            t2.name = "Crazy Donation";
                            break;
                        }
                    }
                }
                if (t2.text == null) continue;
                switch (t2.text) {
                    case "+{betrag} von {name}": {
                        t2.text = "+{amount} from {name}";
                        continue block21;
                    }
                    case "{name} hat {betrag} gespendet!": {
                        t2.text = "{name} donated {amount}!";
                        continue block21;
                    }
                }
                t2.text = t2.text.replace("{betrag}", "{amount}").replace("{gesamt}", "{total}").replace("{anzahl}", "{count}");
            }
        }
        d.profiles.values().removeIf(l -> l == null);
        if (d.profile == null || !d.profiles.containsKey(d.profile)) {
            d.profile = d.profiles.keySet().iterator().next();
        }
        if (d.fightAnim == null) {
            d.fightAnim = "ticker";
        }
        if (d.testName == null || d.testName.isBlank()) {
            d.testName = "TestSpender";
        }
        if (d.goalName == null) {
            d.goalName = "";
        }
        d.mergeSeconds = Math.max(0, Math.min(60, d.mergeSeconds));
        d.hudPeriod = Math.max(0, Math.min(3, d.hudPeriod));
        int n = d.topCount = d.topCount <= 3 ? 3 : 5;
        if (d.sessionStart <= 0L) {
            d.sessionStart = System.currentTimeMillis();
        }
    }

    public static void changed() {
        dirty = true;
        dirtyAt = System.currentTimeMillis();
    }

    public static void tick() {
        int hash;
        if (++ticks % 40 == 0 && loaded && (hash = DonationStore.data.blacklist.hashCode()) != blacklistHash) {
            if (blacklistHash != 0 || !DonationStore.data.blacklist.isEmpty()) {
                DonationStore.changed();
            }
            blacklistHash = hash;
        }
        if (dirty && System.currentTimeMillis() - dirtyAt > 500L) {
            DonationStore.save();
        }
    }

    public static void save() {
        dirty = false;
        Path path = DonationStore.file();
        try {
            Files.createDirectories(path.getParent(), new FileAttribute[0]);
            Path temp = path.resolveSibling("donations.json.tmp");
            try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8, new OpenOption[0]);){
                GSON.toJson((Object)data, (Appendable)writer);
            }
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        }
        catch (Exception e) {
            System.err.println("[FWM] donations.json could not be saved: " + e.getMessage());
        }
    }

    public static List<DonationTier> tiers() {
        Data d = DonationStore.get();
        List<DonationTier> list = d.profiles.get(d.profile);
        if (list == null) {
            list = DonationTier.defaults();
            d.profiles.put(d.profile, list);
        }
        return list;
    }

    public static DonationTier tierFor(long cents) {
        DonationTier best = null;
        for (DonationTier t : DonationStore.tiers()) {
            if (!t.matches(cents) || best != null && t.from <= best.from) continue;
            best = t;
        }
        return best;
    }

    public static long weight(DonationTier tier) {
        return tier == null ? -1L : tier.from;
    }

    public static List<String> overlaps(DonationTier tier) {
        ArrayList<String> out = new ArrayList<String>();
        for (DonationTier other : DonationStore.tiers()) {
            if (other == tier || !other.on || !tier.on || !tier.overlaps(other)) continue;
            out.add(other.name);
        }
        return out;
    }

    public static void restoreDefaults() {
        List<DonationTier> list = DonationStore.tiers();
        list.clear();
        list.addAll(DonationTier.defaults());
        DonationStore.changed();
    }

    public static String exportProfile() {
        Export e = new Export();
        e.name = DonationStore.get().profile;
        e.tiers = DonationStore.tiers();
        String json = new Gson().toJson((Object)e);
        return EXPORT_PREFIX + Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    public static String importProfile(String text) {
        if (text == null) {
            return null;
        }
        if (!(text = text.trim()).startsWith(EXPORT_PREFIX)) {
            return null;
        }
        try {
            String json = new String(Base64.getDecoder().decode(text.substring(EXPORT_PREFIX.length())), StandardCharsets.UTF_8);
            Export e = (Export)new Gson().fromJson(json, Export.class);
            if (e == null || e.tiers == null || e.tiers.isEmpty()) {
                return null;
            }
            ArrayList<DonationTier> list = new ArrayList<DonationTier>();
            for (DonationTier t : e.tiers) {
                if (t == null) continue;
                t.repair();
                list.add(t);
                if (list.size() < 10) continue;
                break;
            }
            String name = DonationStore.uniqueName(e.name == null || e.name.isBlank() ? "Import" : e.name.trim());
            DonationStore.get().profiles.put(name, list);
            DonationStore.get().profile = name;
            DonationStore.changed();
            return name;
        }
        catch (RuntimeException ex) {
            return null;
        }
    }

    public static String uniqueName(String base) {
        String name;
        String string = name = base.length() > 20 ? base.substring(0, 20) : base;
        if (!DonationStore.get().profiles.containsKey(name)) {
            return name;
        }
        int i = 2;
        String n;
        while (DonationStore.get().profiles.containsKey(n = name + " " + i)) {
            ++i;
        }
        return n;
    }

    public static String newProfile() {
        String name = DonationStore.uniqueName(Lang.t("Profil", "Profile"));
        ArrayList<DonationTier> copy = new ArrayList<DonationTier>();
        for (DonationTier t : DonationStore.tiers()) {
            copy.add(t.copy());
        }
        DonationStore.get().profiles.put(name, copy);
        DonationStore.get().profile = name;
        DonationStore.changed();
        return name;
    }

    public static void deleteProfile() {
        Data d = DonationStore.get();
        if (d.profiles.size() <= 1) {
            return;
        }
        d.profiles.remove(d.profile);
        d.profile = d.profiles.keySet().iterator().next();
        DonationStore.changed();
    }

    public static void renameProfile(String to) {
        Data d = DonationStore.get();
        if (to == null || to.isBlank() || to.equals(d.profile)) {
            return;
        }
        String name = DonationStore.uniqueName(to.trim());
        LinkedHashMap<String, List<DonationTier>> renamed = new LinkedHashMap<String, List<DonationTier>>();
        for (Map.Entry<String, List<DonationTier>> e : d.profiles.entrySet()) {
            renamed.put(e.getKey().equals(d.profile) ? name : e.getKey(), e.getValue());
        }
        d.profiles = renamed;
        d.profile = name;
        DonationStore.changed();
    }

    public static long record(String name, long amount, int count, String tier, long now) {
        Data d = DonationStore.get();
        Entry e = new Entry();
        e.time = now;
        e.name = name;
        e.amount = amount;
        e.count = Math.max(1, count);
        e.tier = tier == null ? "" : tier;
        ServerContext.Server server = ServerContext.current();
        e.server = server == null || server.label == null ? "" : server.label;
        d.history.add(e);
        while (d.history.size() > 200) {
            d.history.remove(0);
        }
        String key = name.toLowerCase(Locale.ROOT);
        Total total = d.totals.computeIfAbsent(key, k -> new Total());
        total.name = name;
        total.sum += amount;
        total.count += e.count;
        ++revision;
        DonationStore.changed();
        return total.sum;
    }

    public static void delete(Entry e) {
        Data d = DonationStore.get();
        if (!d.history.remove(e)) {
            return;
        }
        Total total = d.totals.get(e.name.toLowerCase(Locale.ROOT));
        if (total != null) {
            total.sum = Math.max(0L, total.sum - e.amount);
            total.count = Math.max(0, total.count - e.count);
            if (total.sum <= 0L) {
                d.totals.remove(e.name.toLowerCase(Locale.ROOT));
            }
        }
        ++revision;
        DonationStore.changed();
    }

    public static long totalOf(String name) {
        Total t = DonationStore.get().totals.get(name.toLowerCase(Locale.ROOT));
        return t == null ? 0L : t.sum;
    }

    public static void resetSession() {
        DonationStore.get().sessionStart = System.currentTimeMillis();
        DonationStore.get().goalDone = false;
        ++revision;
        DonationStore.changed();
    }

    public static long periodStart(int period) {
        ZoneId zone = ZoneId.systemDefault();
        return switch (period) {
            case 0 -> DonationStore.get().sessionStart;
            case 1 -> LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli();
            case 2 -> LocalDate.now(zone).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay(zone).toInstant().toEpochMilli();
            default -> Long.MIN_VALUE;
        };
    }

    public static List<Rank> ranking(int period) {
        Data d = DonationStore.get();
        LinkedHashMap<Object, long[]> sums = new LinkedHashMap<Object, long[]>();
        HashMap<Object, String> names = new HashMap<Object, String>();
        if (period == 3) {
            for (Total t : d.totals.values()) {
                if (t.name == null) continue;
                String key = t.name.toLowerCase(Locale.ROOT);
                sums.put(key, new long[]{t.sum, t.count});
                names.put(key, t.name);
            }
        } else {
            long from = DonationStore.periodStart(period);
            for (Entry e : d.history) {
                if (e.time < from || e.name == null) continue;
                String key = e.name.toLowerCase(Locale.ROOT);
                long[] s = sums.computeIfAbsent(key, k -> new long[2]);
                s[0] = s[0] + e.amount;
                s[1] = s[1] + (long)e.count;
                names.put(key, e.name);
            }
        }
        ArrayList<Rank> out = new ArrayList<Rank>();
        for (Map.Entry e : sums.entrySet()) {
            out.add(new Rank((String)names.get(e.getKey()), ((long[])e.getValue())[0], (int)((long[])e.getValue())[1]));
        }
        out.sort((a, b) -> Long.compare(b.sum(), a.sum()));
        return out;
    }

    public static long periodSum(int period) {
        long sum = 0L;
        for (Rank r : DonationStore.ranking(period)) {
            sum += r.sum();
        }
        return sum;
    }

    public static Entry last() {
        List<Entry> h = DonationStore.get().history;
        return h.isEmpty() ? null : h.get(h.size() - 1);
    }

    public static boolean blocked(String name) {
        for (String b : DonationStore.get().blacklist) {
            if (b == null || !b.trim().equalsIgnoreCase(name)) continue;
            return true;
        }
        return false;
    }

    static {
        blacklistHash = 1;
        TIER_LIST = new TypeToken<List<DonationTier>>(){}.getType();
    }

    public static final class Data {
        public int version = 1;
        public boolean onlyStreamer = true;
        public boolean streamLive = false;
        public int mergeSeconds = 10;
        public List<String> blacklist = new ArrayList<String>();
        public boolean gapChat = false;
        public boolean fightSmall = true;
        public String fightAnim = "ticker";
        public boolean shake = true;
        public String testName = "TestSpender";
        public String profile = "Stream";
        public Map<String, List<DonationTier>> profiles = new LinkedHashMap<String, List<DonationTier>>();
        public boolean topHud = false;
        public int topCount = 5;
        public boolean topLast = true;
        public boolean topGoal = true;
        public int hudPeriod = 0;
        public boolean crown = true;
        public long sessionStart;
        public long goal;
        public String goalName = "";
        public long goalStart;
        public boolean goalDone;
        public List<Entry> history = new ArrayList<Entry>();
        public Map<String, Total> totals = new HashMap<String, Total>();
    }

    private static final class Export {
        String name;
        List<DonationTier> tiers;

        private Export() {
        }
    }

    public static final class Entry {
        public long time;
        public String name;
        public long amount;
        public int count = 1;
        public String tier = "";
        public String server = "";
    }

    public static final class Total {
        public String name;
        public long sum;
        public int count;
    }

    public record Rank(String name, long sum, int count) {
    }
}

