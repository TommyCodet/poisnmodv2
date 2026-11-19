/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.Identifier
 */
package net.friendswithmoney.fwm.controller;

import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.friendswithmoney.fwm.FwmClient;
import net.friendswithmoney.fwm.config.ConfigManager;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.net.FwmApi;
import net.friendswithmoney.fwm.net.UpdateCheck;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class SellPresets {
    private static final String URL = "https://269153435.de/api/mod/sell-presets";
    private static final long REFRESH_MS = 1800000L;
    private static final String CODE = "FWMSELL1:";
    private static final Gson GSON = new Gson();
    private static List<Remote> remote;
    private static long fetchedAt;
    private static boolean fetching;

    private SellPresets() {
    }

    private static Path cache() {
        return FabricLoader.getInstance().getConfigDir().resolve("fwm").resolve("sell-presets.json");
    }

    public static List<Remote> fwm() {
        if (remote == null) {
            SellPresets.loadCache();
        }
        SellPresets.refresh();
        ArrayList<Remote> out = new ArrayList<Remote>();
        for (Remote r2 : remote) {
            if (r2 == null || r2.name == null || !r2.sichtbar || r2.minVersion != null && UpdateCheck.newerThanThis(r2.minVersion)) continue;
            out.add(r2);
        }
        out.sort(Comparator.comparingInt(r -> r.reihenfolge));
        return out;
    }

    private static void loadCache() {
        remote = new ArrayList<Remote>();
        try {
            Answer a;
            if (Files.exists(SellPresets.cache(), new LinkOption[0]) && (a = (Answer)GSON.fromJson(Files.readString(SellPresets.cache(), StandardCharsets.UTF_8), Answer.class)) != null && a.presets != null) {
                remote = new ArrayList<Remote>(a.presets);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (remote.isEmpty()) {
            remote = SellPresets.builtIn();
        }
    }

    private static List<Remote> builtIn() {
        Remote flower = new Remote();
        flower.id = "blumenfarm";
        flower.name = "Blumenfarm";
        flower.beschreibung = "Mohn, L\u00f6wenzahn, Weizensamen";
        flower.icon = "minecraft:poppy";
        flower.items = new ArrayList<String>(FwmConfig.PRESET_FLOWER_FARM);
        return new ArrayList<Remote>(List.of(flower));
    }

    public static void refresh() {
        long now = System.currentTimeMillis();
        if (fetching || now - fetchedAt < 1800000L) {
            return;
        }
        fetching = true;
        fetchedAt = now;
        FwmApi.getUrl(URL, Answer.class).whenComplete((answer, error) -> {
            fetching = false;
            if (error != null || answer == null || answer.presets == null) {
                return;
            }
            remote = new ArrayList<Remote>(answer.presets);
            try {
                Files.createDirectories(SellPresets.cache().getParent(), new FileAttribute[0]);
                Files.writeString(SellPresets.cache(), (CharSequence)GSON.toJson(answer), StandardCharsets.UTF_8, new OpenOption[0]);
            }
            catch (Exception exception) {
                // empty catch block
            }
        });
    }

    private static List<String> known(List<String> items) {
        ArrayList<String> out = new ArrayList<String>();
        if (items == null) {
            return out;
        }
        for (String id : items) {
            Identifier ident = Identifier.tryParse((String)id);
            if (ident == null || !BuiltInRegistries.ITEM.containsKey(ident)) continue;
            out.add(id);
        }
        return out;
    }

    public static void load(String name, List<String> items, V2.Timings timings) {
        FwmConfig.INSTANCE.setWhitelist(SellPresets.known(items));
        if (timings != null) {
            FwmConfig c = FwmConfig.INSTANCE;
            c.itemMoveDelayTicks = timings.itemDelay;
            c.transferBurst = timings.stacksPerTick;
            c.sellPressDelayTicks = timings.buttonDelay;
            c.reopenDelayTicks = timings.reopenDelay;
            c.clamp();
        }
        ConfigManager.save();
        V2.INSTANCE.sellPresetActive = name;
        V2.save();
    }

    public static void load(V2.SellPreset p) {
        SellPresets.load(p.name, p.items, p.timings);
    }

    public static void load(Remote r) {
        SellPresets.load(r.name, r.items, r.timings);
    }

    private static V2.Timings currentTimings() {
        FwmConfig c = FwmConfig.INSTANCE;
        V2.Timings t = new V2.Timings();
        t.itemDelay = c.itemMoveDelayTicks;
        t.stacksPerTick = c.transferBurst;
        t.buttonDelay = c.sellPressDelayTicks;
        t.reopenDelay = c.reopenDelayTicks;
        return t;
    }

    public static V2.SellPreset find(String name) {
        for (V2.SellPreset p : V2.INSTANCE.sellPresets) {
            if (!p.name.equalsIgnoreCase(name)) continue;
            return p;
        }
        return null;
    }

    public static boolean save(String name, boolean withTimings) {
        String n;
        String string = n = name == null ? "" : name.trim();
        if (n.isEmpty() || SellPresets.find(n) != null) {
            return false;
        }
        V2.SellPreset p = new V2.SellPreset();
        p.name = n;
        p.items = new ArrayList<String>(FwmConfig.INSTANCE.sellWhitelist);
        p.timings = withTimings ? SellPresets.currentTimings() : null;
        V2.INSTANCE.sellPresets.add(p);
        V2.INSTANCE.sellPresetActive = n;
        V2.save();
        return true;
    }

    public static void overwrite(V2.SellPreset p) {
        p.items = new ArrayList<String>(FwmConfig.INSTANCE.sellWhitelist);
        if (p.timings != null) {
            p.timings = SellPresets.currentTimings();
        }
        V2.save();
    }

    public static void delete(V2.SellPreset p) {
        V2.INSTANCE.sellPresets.remove(p);
        if (p.name.equalsIgnoreCase(V2.INSTANCE.sellPresetActive)) {
            V2.INSTANCE.sellPresetActive = "";
        }
        V2.save();
    }

    public static V2.SellPreset copy(Remote r) {
        String base = r.name;
        Object n = base;
        int i = 2;
        while (SellPresets.find((String)n) != null) {
            n = base + " " + i;
            ++i;
        }
        V2.SellPreset p = new V2.SellPreset();
        p.name = n;
        p.items = new ArrayList<String>(SellPresets.known(r.items));
        p.timings = r.timings;
        V2.INSTANCE.sellPresets.add(p);
        V2.save();
        return p;
    }

    public static String share(V2.SellPreset p) {
        return CODE + Base64.getUrlEncoder().withoutPadding().encodeToString(GSON.toJson((Object)p).getBytes(StandardCharsets.UTF_8));
    }

    public static V2.SellPreset importCode(String text) {
        if (text == null) {
            return null;
        }
        String t = text.trim();
        int at = t.indexOf(CODE);
        if (at < 0) {
            return null;
        }
        try {
            String body = t.substring(at + CODE.length()).split("\\s")[0];
            V2.SellPreset p = (V2.SellPreset)GSON.fromJson(new String(Base64.getUrlDecoder().decode(body), StandardCharsets.UTF_8), V2.SellPreset.class);
            if (p == null || p.name == null || p.items == null) {
                return null;
            }
            p.items = SellPresets.known(p.items);
            String base = p.name.trim();
            Object n = base;
            int i = 2;
            while (SellPresets.find((String)n) != null) {
                n = base + " " + i;
                ++i;
            }
            p.name = n;
            V2.INSTANCE.sellPresets.add(p);
            V2.save();
            return p;
        }
        catch (Exception e) {
            return null;
        }
    }

    public static String next() {
        ArrayList<String> names = new ArrayList<String>();
        for (V2.SellPreset p : V2.INSTANCE.sellPresets) {
            names.add(p.name);
        }
        for (Remote r : SellPresets.fwm()) {
            names.add(r.name);
        }
        if (names.isEmpty()) {
            return null;
        }
        int i = names.indexOf(V2.INSTANCE.sellPresetActive);
        String name = (String)names.get((i + 1) % names.size());
        V2.SellPreset own = SellPresets.find(name);
        if (own != null) {
            SellPresets.load(own);
        } else {
            for (Remote r : SellPresets.fwm()) {
                if (!r.name.equals(name)) continue;
                SellPresets.load(r);
                break;
            }
        }
        return name;
    }

    static String version() {
        return FwmClient.MOD_VERSION;
    }

    public static final class Remote {
        public String id;
        public String name;
        public String beschreibung;
        public String icon;
        public List<String> items = new ArrayList<String>();
        public V2.Timings timings;
        public String server;
        public String minVersion;
        public boolean sichtbar = true;
        public int reihenfolge;
    }

    static final class Answer {
        List<Remote> presets;

        Answer() {
        }
    }
}

