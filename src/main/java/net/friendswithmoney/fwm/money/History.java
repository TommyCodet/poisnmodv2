/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.reflect.TypeToken
 *  net.fabricmc.loader.api.FabricLoader
 */
package net.friendswithmoney.fwm.money;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.money.MoneyStore;

public final class History {
    private static final int MAX = 5000;
    private static final long MERGE_MS = 60000L;
    private static final Gson GSON = new GsonBuilder().create();
    private static List<Entry> entries = new ArrayList<Entry>();
    private static String loadedFor;
    private static boolean dirty;
    private static long lastSave;

    private History() {
    }

    public static void add(MoneyStore.Kind kind, long cents, String player, String item, String line) {
        Entry last;
        long signed;
        if (!FwmConfig.INSTANCE.historyEnabled) {
            return;
        }
        History.ensureLoaded();
        long now = System.currentTimeMillis();
        long l = signed = kind.income ? cents : -cents;
        if (kind == MoneyStore.Kind.SELL && FwmConfig.INSTANCE.historyMergeSell && !entries.isEmpty() && (last = entries.get(entries.size() - 1)).kind() == MoneyStore.Kind.SELL && now - last.t < 60000L) {
            last.c += signed;
            ++last.n;
            last.t = now;
            dirty = true;
            return;
        }
        Entry entry = new Entry();
        entry.t = now;
        entry.k = kind.ordinal();
        entry.c = signed;
        entry.p = player;
        entry.i = item;
        entry.l = line == null ? null : (line.length() > 200 ? line.substring(0, 200) : line);
        entries.add(entry);
        while (entries.size() > 5000) {
            entries.remove(0);
        }
        dirty = true;
    }

    public static List<Entry> newestFirst() {
        History.ensureLoaded();
        ArrayList<Entry> out = new ArrayList<Entry>(entries);
        Collections.reverse(out);
        return out;
    }

    public static void tick() {
        if (dirty && System.currentTimeMillis() - lastSave > 60000L) {
            History.save();
        }
    }

    public static void save() {
        if (!dirty || loadedFor == null) {
            return;
        }
        long cut = System.currentTimeMillis() - (long)FwmConfig.INSTANCE.historyDays * 86400000L;
        entries.removeIf(e -> e.t < cut);
        try {
            Path file = History.file(loadedFor);
            Files.createDirectories(file.getParent(), new FileAttribute[0]);
            Files.writeString(file, (CharSequence)GSON.toJson(entries), StandardCharsets.UTF_8, new OpenOption[0]);
            dirty = false;
            lastSave = System.currentTimeMillis();
        }
        catch (Exception e2) {
            System.err.println("[FWM] Could not write history: " + e2.getMessage());
        }
    }

    private static void ensureLoaded() {
        String id = MoneyStore.account();
        if (id.equals(loadedFor)) {
            return;
        }
        if (loadedFor != null) {
            History.save();
        }
        loadedFor = id;
        entries = new ArrayList<Entry>();
        try {
            List read;
            Path file = History.file(id);
            if (Files.exists(file, new LinkOption[0]) && (read = (List)GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), new TypeToken<List<Entry>>(){}.getType())) != null) {
                entries = new ArrayList<Entry>(read);
            }
        }
        catch (Exception e) {
            System.err.println("[FWM] Could not read history: " + e.getMessage());
        }
    }

    private static Path file(String id) {
        return FabricLoader.getInstance().getConfigDir().resolve("fwm-history-" + id + ".json");
    }

    public static final class Entry {
        public long t;
        public int k;
        public long c;
        public String p;
        public String i;
        public int n = 1;
        public String l;

        public MoneyStore.Kind kind() {
            MoneyStore.Kind[] all = MoneyStore.Kind.values();
            return this.k >= 0 && this.k < all.length ? all[this.k] : MoneyStore.Kind.OTHER_IN;
        }
    }
}

