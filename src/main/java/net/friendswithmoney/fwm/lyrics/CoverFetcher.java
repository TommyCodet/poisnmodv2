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
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.friendswithmoney.fwm.lyrics.LyricsConfig;
import net.friendswithmoney.fwm.lyrics.LyricsFetcher;
import net.friendswithmoney.fwm.lyrics.MediaWatcher;
import net.friendswithmoney.fwm.lyrics.SkyLyrics;

public final class CoverFetcher {
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8L)).followRedirects(HttpClient.Redirect.NORMAL).build();
    private static final ExecutorService POOL = Executors.newSingleThreadExecutor(var0 -> {
        Thread var1 = new Thread(var0, "SkyLyrics-Cover");
        var1.setDaemon(true);
        return var1;
    });
    private static final Map<String, String> files = new ConcurrentHashMap<String, String>();
    private static final Map<String, Boolean> running = new ConcurrentHashMap<String, Boolean>();
    private static volatile String lastWhy = "";

    private CoverFetcher() {
    }

    public static String path(MediaWatcher.Track var0) {
        if (!SkyLyrics.online() || !SkyLyrics.cfg.netCover) {
            return null;
        }
        if (var0 != null && !var0.title().isEmpty()) {
            String var1 = var0.key();
            String var2 = files.get(var1);
            if (var2 != null) {
                return var2.isEmpty() ? null : var2;
            }
            if (running.putIfAbsent(var1, Boolean.TRUE) == null) {
                POOL.execute(() -> {
                    try {
                        String var2x = CoverFetcher.fetch(var0);
                        files.put(var1, var2x);
                        SkyLyrics.LOG.debug("Sky Lyrics: Ersatz-Cover f\u00fcr '{}': {}", (Object)var1, var2x.isEmpty() ? "nichts gefunden (" + lastWhy + ")" : "ok");
                    }
                    catch (Throwable var6) {
                        SkyLyrics.LOG.debug("Sky Lyrics: Ersatz-Cover f\u00fcr '{}' fehlgeschlagen: {}", (Object)var1, (Object)var6.toString());
                        files.put(var1, "");
                    }
                    finally {
                        running.remove(var1);
                    }
                });
            }
            return null;
        }
        return null;
    }

    private static String fetch(MediaWatcher.Track var0) throws Exception {
        String var1 = LyricsFetcher.cleanArtist(var0.artist());
        String var2 = LyricsFetcher.cleanTitle(var0.title());
        lastWhy = "";
        String var3 = null;
        for (String var7 : new String[]{"DE", "US"}) {
            try {
                var3 = CoverFetcher.itunes(var1, var2, var7);
            }
            catch (Exception var10) {
                lastWhy = lastWhy + "iTunes " + var7 + ": " + String.valueOf(var10) + "; ";
            }
            if (var3 != null) break;
        }
        if (var3 == null) {
            try {
                var3 = CoverFetcher.deezer((String)(var1.isEmpty() ? var2 : "artist:\"" + var1 + "\" track:\"" + var2 + "\""));
                if (var3 == null) {
                    var3 = CoverFetcher.deezer((var1 + " " + var2).trim());
                }
            }
            catch (Exception var9) {
                lastWhy = lastWhy + "Deezer: " + String.valueOf(var9) + "; ";
            }
        }
        if (var3 == null) {
            return "";
        }
        HttpResponse<byte[]> var11 = HTTP.send(HttpRequest.newBuilder(URI.create(var3)).timeout(Duration.ofSeconds(15L)).header("User-Agent", "FWM-Mod/2.1 (+https://friendswithmoney.net)").GET().build(), HttpResponse.BodyHandlers.ofByteArray());
        if (var11.statusCode() == 200 && var11.body().length >= 200) {
            Path var12 = LyricsConfig.DIR.resolve("cover");
            Files.createDirectories(var12, new FileAttribute[0]);
            Path var13 = var12.resolve("net_" + Integer.toHexString(var0.key().hashCode()) + ".img");
            Files.write(var13, var11.body(), new OpenOption[0]);
            return var13.toString();
        }
        return "";
    }

    private static JsonElement get(String var0) throws Exception {
        HttpResponse<String> var1 = HTTP.send(HttpRequest.newBuilder(URI.create(var0)).timeout(Duration.ofSeconds(12L)).header("User-Agent", "FWM-Mod/2.1 (+https://friendswithmoney.net)").header("Accept", "application/json").GET().build(), HttpResponse.BodyHandlers.ofString());
        if (var1.statusCode() != 200) {
            lastWhy = lastWhy + URI.create(var0).getHost() + " HTTP " + var1.statusCode() + "; ";
            return null;
        }
        return JsonParser.parseString((String)var1.body());
    }

    private static String enc(String var0) {
        return URLEncoder.encode(var0, StandardCharsets.UTF_8);
    }

    private static String str(JsonObject var0, String var1) {
        return var0.has(var1) && !var0.get(var1).isJsonNull() ? var0.get(var1).getAsString() : "";
    }

    private static String itunes(String var0, String var1, String var2) throws Exception {
        JsonElement var3 = CoverFetcher.get("https://itunes.apple.com/search?media=music&entity=song&limit=10&country=" + var2 + "&term=" + CoverFetcher.enc((var0 + " " + var1).trim()));
        if (var3 != null && var3.isJsonObject() && var3.getAsJsonObject().has("results")) {
            JsonArray var4 = var3.getAsJsonObject().getAsJsonArray("results");
            String var5 = null;
            for (JsonElement var7 : var4) {
                JsonObject var8 = var7.getAsJsonObject();
                String var9 = CoverFetcher.str(var8, "artworkUrl100");
                if (var9.isEmpty()) continue;
                var9 = var9.replace("100x100bb", "300x300bb");
                if (var5 == null) {
                    var5 = var9;
                }
                if (var0.isEmpty() || !CoverFetcher.str(var8, "artistName").toLowerCase(Locale.ROOT).contains(var0.toLowerCase(Locale.ROOT))) continue;
                return var9;
            }
            return var0.isEmpty() ? var5 : null;
        }
        return null;
    }

    private static String deezer(String var0) throws Exception {
        JsonElement var1 = CoverFetcher.get("https://api.deezer.com/search?limit=1&q=" + CoverFetcher.enc(var0));
        if (var1 != null && var1.isJsonObject()) {
            JsonArray var2 = var1.getAsJsonObject().getAsJsonArray("data");
            if (var2 != null && !var2.isEmpty()) {
                JsonObject var3 = var2.get(0).getAsJsonObject().getAsJsonObject("album");
                String var4 = var3 == null ? "" : CoverFetcher.str(var3, "cover_medium");
                return var4.isEmpty() ? null : var4;
            }
            return null;
        }
        return null;
    }
}

