/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package net.friendswithmoney.fwm.lyrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.lang.invoke.CallSite;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.friendswithmoney.fwm.lyrics.Lyrics;
import net.friendswithmoney.fwm.lyrics.LyricsConfig;
import net.friendswithmoney.fwm.lyrics.MediaWatcher;
import net.friendswithmoney.fwm.lyrics.SkyLyrics;
import net.friendswithmoney.fwm.lyrics.T;

public final class LyricsFetcher {
    private static final HttpClient HTTP = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(15L)).followRedirects(HttpClient.Redirect.NORMAL).build();
    private static final String UA = "FWM-Mod/2.1 (+https://friendswithmoney.net)";
    private static final ExecutorService POOL = Executors.newFixedThreadPool(2, var0 -> {
        Thread var1 = new Thread(var0, "SkyLyrics-Fetch");
        var1.setDaemon(true);
        return var1;
    });
    private static final long[] ERROR_BACKOFF_MS = new long[]{3000L, 6000L, 12000L, 20000L, 30000L};
    private static final long[] NOT_FOUND_RETRY_MS = new long[]{30000L, 120000L};
    private static final Map<String, Entry> entries = new ConcurrentHashMap<String, Entry>();

    private LyricsFetcher() {
    }

    public static Lyrics peek(MediaWatcher.Track var0) {
        Entry var1 = entries.get(var0.key());
        return var1 == null ? null : var1.lyrics;
    }

    public static boolean searchingVersion(MediaWatcher.Track var0) {
        Entry var1 = entries.get(var0.key());
        return var1 != null && var1.searching;
    }

    public static boolean switchedVersion(MediaWatcher.Track var0) {
        Entry var1 = entries.get(var0.key());
        return var1 != null && var1.switched;
    }

    static boolean versionMismatch(MediaWatcher.Track var0, Lyrics var1) {
        return var1 != null && var1.synced && !var1.instrumental && var1.sourceDurationMs > 0L && var0.durationMs() > 0L && Math.abs(var1.sourceDurationMs - var0.durationMs()) > 2500L;
    }

    public static Status status(MediaWatcher.Track var0) {
        Entry var1 = entries.get(var0.key());
        if (var1 == null) {
            return Status.LOADING;
        }
        if (var1.lyrics != null) {
            return Status.FOUND;
        }
        return var1.running ? Status.LOADING : var1.status;
    }

    public static String statusText(MediaWatcher.Track var0) {
        if (!SkyLyrics.online()) {
            return T.tr("Songtext aus (nur lokal)");
        }
        Entry var1 = entries.get(var0.key());
        if (var1 != null && var1.lyrics == null) {
            if (var1.running) {
                return var1.errors > 0 ? T.tr("l\u00e4dt \u2026 (Versuch ") + (var1.errors + 1) + ")" : T.tr("l\u00e4dt \u2026");
            }
            if (var1.status == Status.ERROR) {
                long var2 = Math.max(0L, (var1.nextTryMs - System.currentTimeMillis() + 999L) / 1000L);
                return T.tr("neuer Versuch in ") + var2 + " s";
            }
            return var1.status == Status.NOT_FOUND ? T.tr("keine Lyrics") : T.tr("l\u00e4dt \u2026");
        }
        return var1 == null ? T.tr("l\u00e4dt \u2026") : "";
    }

    public static int attempts(MediaWatcher.Track var0) {
        Entry var1 = entries.get(var0.key());
        return var1 == null ? 0 : var1.errors + var1.notFounds;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Lyrics get(MediaWatcher.Track var0) {
        if (!SkyLyrics.online()) {
            return null;
        }
        Entry var1 = entries.computeIfAbsent(var0.key(), var0x -> new Entry());
        Lyrics var2 = var1.lyrics;
        if (var2 != null) {
            return var2;
        }
        if (!var1.running && System.currentTimeMillis() >= var1.nextTryMs && LyricsFetcher.canRetry(var1)) {
            Entry entry = var1;
            synchronized (entry) {
                if (!var1.running) {
                    var1.running = true;
                    POOL.execute(() -> LyricsFetcher.load(var0, var1));
                }
            }
        }
        return null;
    }

    private static boolean canRetry(Entry var0) {
        return var0.status == Status.NOT_FOUND ? var0.notFounds <= NOT_FOUND_RETRY_MS.length : true;
    }

    public static void reload(MediaWatcher.Track var0) {
        entries.remove(var0.key());
        try {
            Files.deleteIfExists(LyricsFetcher.cacheFile(var0.key()));
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void load(MediaWatcher.Track var0, Entry var1) {
        block17: {
            String var2 = var0.key();
            try {
                Result var3 = LyricsFetcher.fetch(var0);
                if (var3.lyrics != null) {
                    var1.lyrics = var3.lyrics;
                    var1.status = Status.FOUND;
                    if (!LyricsFetcher.versionMismatch(var0, var3.lyrics) || var3.noBetter) break block17;
                    var1.searching = true;
                    try {
                        JsonObject var4 = LyricsFetcher.findBetter(var0);
                        Path var5 = LyricsFetcher.cacheFile(var2);
                        if (var4 != null) {
                            Lyrics var6 = LyricsFetcher.fromJson(var4, var0.durationMs());
                            if (var6 != null && var6.synced && !var6.lines.isEmpty()) {
                                var1.lyrics = var6;
                                var1.switched = true;
                                Files.createDirectories(var5.getParent(), new FileAttribute[0]);
                                Files.writeString(var5, (CharSequence)var4.toString(), new OpenOption[0]);
                                SkyLyrics.LOG.debug("Sky Lyrics: passendere Songtext-Version f\u00fcr {} gefunden", (Object)var2);
                            }
                        } else if (Files.exists(var5, new LinkOption[0])) {
                            JsonObject var21 = JsonParser.parseString((String)Files.readString(var5)).getAsJsonObject();
                            var21.addProperty("skyNoBetter", Boolean.valueOf(true));
                            Files.writeString(var5, (CharSequence)var21.toString(), new OpenOption[0]);
                        }
                        break block17;
                    }
                    catch (Exception var17) {
                        SkyLyrics.LOG.debug("Sky Lyrics: Versionssuche fehlgeschlagen", (Throwable)var17);
                        break block17;
                    }
                    finally {
                        var1.searching = false;
                    }
                }
                if (var3.hadError) {
                    ++var1.errors;
                    var1.status = Status.ERROR;
                    var1.nextTryMs = System.currentTimeMillis() + ERROR_BACKOFF_MS[Math.min(var1.errors - 1, ERROR_BACKOFF_MS.length - 1)];
                    SkyLyrics.LOG.debug("Sky Lyrics: LRCLIB nicht erreichbar f\u00fcr {} ({}), neuer Versuch folgt", (Object)var2, (Object)var3.error);
                } else {
                    ++var1.notFounds;
                    var1.status = Status.NOT_FOUND;
                    var1.nextTryMs = System.currentTimeMillis() + NOT_FOUND_RETRY_MS[Math.min(var1.notFounds - 1, NOT_FOUND_RETRY_MS.length - 1)];
                }
            }
            catch (Throwable var19) {
                ++var1.errors;
                var1.status = Status.ERROR;
                var1.lastError = String.valueOf(var19.getMessage());
                var1.nextTryMs = System.currentTimeMillis() + ERROR_BACKOFF_MS[Math.min(var1.errors - 1, ERROR_BACKOFF_MS.length - 1)];
                SkyLyrics.LOG.warn("Sky Lyrics: Lyrics laden fehlgeschlagen f\u00fcr {}", (Object)var2, (Object)var19);
            }
            finally {
                var1.running = false;
            }
        }
    }

    private static Result fetch(MediaWatcher.Track var0) throws Exception {
        Lyrics var20;
        Result var1 = new Result();
        String var2 = var0.key();
        Path var3 = LyricsFetcher.cacheFile(var2);
        if (Files.exists(var3, new LinkOption[0])) {
            try {
                JsonObject var4 = JsonParser.parseString((String)Files.readString(var3)).getAsJsonObject();
                Lyrics var5 = LyricsFetcher.fromJson(var4, 0L);
                if (var5 != null) {
                    var1.lyrics = var5;
                    var1.noBetter = var4.has("skyNoBetter");
                    return var1;
                }
            }
            catch (Exception var4) {
                // empty catch block
            }
        }
        long var18 = var0.durationMs() > 0L ? Math.round((double)var0.durationMs() / 1000.0) : 0L;
        JsonObject var6 = null;
        for (String[] var8 : LyricsFetcher.candidates(var0)) {
            String var9 = var8[0];
            String var10 = var8[1];
            try {
                JsonElement var12;
                StringBuilder var11 = new StringBuilder("https://lrclib.net/api/get?track_name=").append(LyricsFetcher.enc(var10)).append("&artist_name=").append(LyricsFetcher.enc(var9));
                if (!var0.album().isEmpty()) {
                    var11.append("&album_name=").append(LyricsFetcher.enc(var0.album()));
                }
                if (var18 > 0L) {
                    var11.append("&duration=").append(var18);
                }
                if ((var12 = LyricsFetcher.request(var11.toString())) != null && var12.isJsonObject()) {
                    JsonObject var13 = var12.getAsJsonObject();
                    if (LyricsFetcher.isInstrumental(var13) || LyricsFetcher.isSynced(var13)) {
                        return LyricsFetcher.found(var1, var13, var3, var0);
                    }
                    if (var6 == null && LyricsFetcher.hasPlain(var13)) {
                        var6 = var13;
                    }
                }
            }
            catch (Exception var17) {
                var1.hadError = true;
                var1.error = var17.getClass().getSimpleName();
            }
            try {
                String var22 = "https://lrclib.net/api/search?track_name=" + LyricsFetcher.enc(var10) + (String)(var9.isEmpty() ? "" : "&artist_name=" + LyricsFetcher.enc(var9));
                JsonObject var23 = LyricsFetcher.pick(LyricsFetcher.request(var22), var18);
                if (var23 == null) continue;
                if (LyricsFetcher.isSynced(var23) || LyricsFetcher.isInstrumental(var23)) {
                    return LyricsFetcher.found(var1, var23, var3, var0);
                }
                if (var6 != null) continue;
                var6 = var23;
            }
            catch (Exception var16) {
                var1.hadError = true;
                var1.error = var16.getClass().getSimpleName();
            }
        }
        try {
            JsonObject var21;
            String var19 = (LyricsFetcher.cleanArtist(var0.artist()) + " " + LyricsFetcher.cleanTitle(var0.title())).trim();
            if (!var19.isEmpty() && (var21 = LyricsFetcher.pick(LyricsFetcher.request("https://lrclib.net/api/search?q=" + LyricsFetcher.enc(var19)), var18)) != null) {
                if (LyricsFetcher.isSynced(var21) || LyricsFetcher.isInstrumental(var21)) {
                    return LyricsFetcher.found(var1, var21, var3, var0);
                }
                if (var6 == null) {
                    var6 = var21;
                }
            }
        }
        catch (Exception var15) {
            var1.hadError = true;
            var1.error = var15.getClass().getSimpleName();
        }
        if (var6 != null && LyricsFetcher.hasPlain(var6) && (var20 = Lyrics.fromPlain(var6.get("plainLyrics").getAsString(), var0.durationMs())) != null) {
            var1.lyrics = var20;
            var1.hadError = false;
            return var1;
        }
        return var1;
    }

    private static Result found(Result var0, JsonObject var1, Path var2, MediaWatcher.Track var3) {
        Lyrics var4 = LyricsFetcher.fromJson(var1, var3.durationMs());
        if (var4 != null) {
            var0.lyrics = var4;
            var0.hadError = false;
            if (LyricsFetcher.isSynced(var1) || LyricsFetcher.isInstrumental(var1)) {
                try {
                    Files.createDirectories(var2.getParent(), new FileAttribute[0]);
                    Files.writeString(var2, (CharSequence)var1.toString(), new OpenOption[0]);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        return var0;
    }

    private static JsonObject pick(JsonElement var0, long var1) {
        if (var0 != null && var0.isJsonArray()) {
            JsonArray var3 = var0.getAsJsonArray();
            JsonObject var4 = null;
            double var5 = Double.MAX_VALUE;
            for (JsonElement var8 : var3) {
                double var13;
                JsonObject var9;
                boolean var10;
                if (!var8.isJsonObject() || !(var10 = LyricsFetcher.isSynced(var9 = var8.getAsJsonObject())) && !LyricsFetcher.isInstrumental(var9) && !LyricsFetcher.hasPlain(var9)) continue;
                double var11 = var9.has("duration") && !var9.get("duration").isJsonNull() ? var9.get("duration").getAsDouble() : 0.0;
                double d = var13 = var1 > 0L && var11 > 0.0 ? Math.abs(var11 - (double)var1) : 5.0;
                if (var1 > 0L && var11 > 0.0 && var13 > (var10 ? 15.0 : 25.0)) continue;
                double d2 = var13 + (var13 > 8.0 ? 40.0 : 0.0);
                double d3 = var10 ? 0.0 : 100.0;
                double var15 = d2 + d3;
                if (!(var15 < var5)) continue;
                var5 = var15;
                var4 = var9;
            }
            return var4;
        }
        return null;
    }

    private static JsonObject findBetter(MediaWatcher.Track var0) {
        double var1 = (double)var0.durationMs() / 1000.0;
        JsonObject var3 = null;
        double var4 = 2.0;
        ArrayList<CallSite> var6 = new ArrayList<CallSite>();
        for (String[] var8 : LyricsFetcher.candidates(var0)) {
            var6.add((CallSite)((Object)("https://lrclib.net/api/search?track_name=" + LyricsFetcher.enc(var8[1]) + (String)(var8[0].isEmpty() ? "" : "&artist_name=" + LyricsFetcher.enc(var8[0])))));
            var6.add((CallSite)((Object)("https://lrclib.net/api/search?track_name=" + LyricsFetcher.enc(var8[1]))));
        }
        var6.add((CallSite)((Object)("https://lrclib.net/api/search?q=" + LyricsFetcher.enc((LyricsFetcher.cleanArtist(var0.artist()) + " " + LyricsFetcher.cleanTitle(var0.title())).trim()))));
        LinkedHashSet<String> var19 = new LinkedHashSet<String>();
        String var20 = LyricsFetcher.cleanTitle(var0.title()).toLowerCase(Locale.ROOT);
        for (String string : var6) {
            if (!var19.add(string) || var19.size() > 6) continue;
            try {
                JsonElement var11 = LyricsFetcher.request(string);
                if (var11 == null || !var11.isJsonArray()) continue;
                for (JsonElement var13 : var11.getAsJsonArray()) {
                    double var16;
                    String var15;
                    JsonObject var14;
                    if (!var13.isJsonObject() || !LyricsFetcher.isSynced(var14 = var13.getAsJsonObject()) || !var14.has("duration") || var14.get("duration").isJsonNull()) continue;
                    String string2 = var15 = var14.has("trackName") && !var14.get("trackName").isJsonNull() ? LyricsFetcher.cleanTitle(var14.get("trackName").getAsString()).toLowerCase(Locale.ROOT) : "";
                    if (!var20.isEmpty() && !var15.contains(var20) && !var20.contains(var15) || !((var16 = Math.abs(var14.get("duration").getAsDouble() - var1)) < var4)) continue;
                    var4 = var16;
                    var3 = var14;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (var3 == null || !(var4 < 0.8)) continue;
            break;
        }
        return var3;
    }

    static List<String[]> candidates(MediaWatcher.Track var0) {
        int var9;
        int var8;
        LinkedHashSet<String> var1 = new LinkedHashSet<String>();
        ArrayList<String[]> var2 = new ArrayList<String[]>();
        String var3 = LyricsFetcher.cleanArtist(var0.artist());
        String var4 = LyricsFetcher.cleanTitle(var0.title());
        LyricsFetcher.add(var2, var1, var3, var4);
        int var5 = LyricsFetcher.indexOfDash(var0.title());
        if (var5 > 0) {
            String var6 = LyricsFetcher.cleanArtist(var0.title().substring(0, var5));
            String var7 = LyricsFetcher.cleanTitle(var0.title().substring(var5 + 3));
            LyricsFetcher.add(var2, var1, var6, var7);
            LyricsFetcher.add(var2, var1, var3, var7);
        }
        if ((var8 = var3.indexOf(44)) > 0) {
            LyricsFetcher.add(var2, var1, var3.substring(0, var8).trim(), var4);
        }
        if ((var9 = var3.indexOf(" & ")) > 0) {
            LyricsFetcher.add(var2, var1, var3.substring(0, var9).trim(), var4);
        }
        return var2;
    }

    private static int indexOfDash(String var0) {
        for (String var4 : new String[]{" - ", " \u2013 ", " \u2014 "}) {
            int var5 = var0.indexOf(var4);
            if (var5 <= 0) continue;
            return var5;
        }
        return -1;
    }

    private static void add(List<String[]> var0, Set<String> var1, String var2, String var3) {
        if (!var3.isEmpty() && var1.add(var2.toLowerCase() + "|" + var3.toLowerCase())) {
            var0.add(new String[]{var2, var3});
        }
    }

    static String cleanTitle(String var0) {
        String var1 = var0.replaceAll("(?i)\\s*[(\\[][^)\\]]*(official|video|audio|lyrics?|lyric video|visualizer|music video|hd|4k|remaster|explicit|clean|live|mv|sped up|slowed)[^)\\]]*[)\\]]", "");
        var1 = var1.replaceAll("(?i)\\s*[(\\[]\\s*(feat|ft|with)\\.?[^)\\]]*[)\\]]", "");
        var1 = var1.replaceAll("(?i)\\s+(feat|ft)\\.?\\s+.*$", "");
        var1 = var1.replaceAll("(?i)\\s+-\\s+(\\d{4}\\s+)?remaster(ed)?.*$", "");
        var1 = var1.replaceAll("(?i)\\s+-\\s+(radio edit|single version|album version|mono|stereo|sped up|slowed)$", "");
        return var1.replaceAll("\\s{2,}", " ").trim();
    }

    static String cleanArtist(String var0) {
        String var1 = var0.replaceAll("(?i)\\s*-\\s*topic$", "").replaceAll("(?i)vevo$", "");
        var1 = var1.replaceAll("(?i)\\s+(feat|ft)\\.?\\s+.*$", "");
        return var1.trim();
    }

    private static JsonElement request(String var0) throws Exception {
        HttpRequest var1 = HttpRequest.newBuilder(URI.create(var0)).timeout(Duration.ofSeconds(25L)).header("User-Agent", UA).GET().build();
        HttpResponse<String> var2 = HTTP.send(var1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        int var3 = var2.statusCode();
        if (var3 == 404) {
            return null;
        }
        if (var3 != 200) {
            throw new IllegalStateException("LRCLIB HTTP " + var3);
        }
        return JsonParser.parseString((String)var2.body());
    }

    private static boolean isSynced(JsonObject var0) {
        return var0.has("syncedLyrics") && !var0.get("syncedLyrics").isJsonNull() && !var0.get("syncedLyrics").getAsString().isBlank();
    }

    private static boolean hasPlain(JsonObject var0) {
        return var0.has("plainLyrics") && !var0.get("plainLyrics").isJsonNull() && !var0.get("plainLyrics").getAsString().isBlank();
    }

    private static boolean isInstrumental(JsonObject var0) {
        return var0.has("instrumental") && !var0.get("instrumental").isJsonNull() && var0.get("instrumental").getAsBoolean();
    }

    private static Lyrics fromJson(JsonObject var0, long var1) {
        Lyrics var3 = LyricsFetcher.fromJson0(var0, var1);
        if (var3 != null && var0.has("duration") && !var0.get("duration").isJsonNull()) {
            try {
                var3.sourceDurationMs = Math.round(var0.get("duration").getAsDouble() * 1000.0);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return var3;
    }

    private static Lyrics fromJson0(JsonObject var0, long var1) {
        if (LyricsFetcher.isInstrumental(var0)) {
            return Lyrics.instrumental();
        }
        if (LyricsFetcher.isSynced(var0)) {
            Lyrics var3 = Lyrics.parseLrc(var0.get("syncedLyrics").getAsString());
            if (!var3.lines.isEmpty()) {
                return var3;
            }
        }
        return LyricsFetcher.hasPlain(var0) ? Lyrics.fromPlain(var0.get("plainLyrics").getAsString(), var1) : null;
    }

    private static String enc(String var0) {
        return URLEncoder.encode(var0, StandardCharsets.UTF_8);
    }

    private static Path cacheFile(String var0) throws Exception {
        byte[] var1 = MessageDigest.getInstance("SHA-1").digest(var0.toLowerCase().getBytes(StandardCharsets.UTF_8));
        return LyricsConfig.DIR.resolve("cache").resolve(HexFormat.of().formatHex(var1) + ".json");
    }

    private static final class Entry {
        volatile Lyrics lyrics;
        volatile Status status = Status.LOADING;
        volatile boolean running;
        volatile int errors;
        volatile int notFounds;
        volatile long nextTryMs;
        volatile String lastError = "";
        volatile boolean searching;
        volatile boolean switched;

        private Entry() {
        }
    }

    public static enum Status {
        LOADING,
        FOUND,
        NOT_FOUND,
        ERROR;

    }

    private static final class Result {
        Lyrics lyrics;
        boolean hadError;
        String error = "";
        boolean noBetter;

        private Result() {
        }
    }
}

