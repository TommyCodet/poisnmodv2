/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
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
import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import net.friendswithmoney.fwm.lyrics.LyricsConfig;
import net.friendswithmoney.fwm.lyrics.LyricsFetcher;
import net.friendswithmoney.fwm.lyrics.MediaWatcher;
import net.friendswithmoney.fwm.lyrics.SkyLyrics;
import net.friendswithmoney.fwm.lyrics.T;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public final class GenreWeather {
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8L)).followRedirects(HttpClient.Redirect.NORMAL).build();
    private static final String UA = "FWM-Mod/2.1 (+https://friendswithmoney.net)";
    private static final Map<String, String> genres = new ConcurrentHashMap<String, String>();
    private static final Map<String, Boolean> loading = new ConcurrentHashMap<String, Boolean>();
    private static final Map<String, Long> retryAt = new ConcurrentHashMap<String, Long>();
    private static boolean overriding;
    private static float serverRain;
    private static float serverThunder;
    private static float lastSetRain;
    private static float lastSetThunder;
    private static float curRain;
    private static float curThunder;
    private static ClientLevel lastLevel;
    private static int nextFlashTicks;
    private static final Random RNG;
    private static final Map<String, Weather> TABLE;
    private static final String[][] ALIASES;
    private static Weather lastWeather;
    private static long lastWeatherNanos;

    private GenreWeather() {
    }

    public static String genre(MediaWatcher.Track var0) {
        if (var0 == null) {
            return null;
        }
        if (var0.genres() != null && !var0.genres().isBlank()) {
            return var0.genres();
        }
        if (!SkyLyrics.online() || !SkyLyrics.cfg.netGenre) {
            return null;
        }
        String var1 = var0.key();
        String var2 = genres.get(var1);
        if (var2 != null) {
            return var2.isEmpty() ? null : var2;
        }
        Long var3 = retryAt.get(var1);
        if (var3 != null && System.currentTimeMillis() < var3) {
            return null;
        }
        if (loading.putIfAbsent(var1, true) == null) {
            CompletableFuture.runAsync(() -> {
                try {
                    String var2x = GenreWeather.lookup(var0);
                    genres.put(var1, var2x == null ? "" : var2x);
                    retryAt.remove(var1);
                }
                catch (Exception var6) {
                    retryAt.put(var1, System.currentTimeMillis() + 20000L);
                    SkyLyrics.LOG.debug("Sky Lyrics: Genre-Suche fehlgeschlagen f\u00fcr {}", (Object)var1, (Object)var6);
                }
                finally {
                    loading.remove(var1);
                }
            });
        }
        return null;
    }

    private static void put(Weather var0, String var1) {
        for (String var5 : var1.split(",")) {
            TABLE.put(var5.trim(), var0);
        }
    }

    static String normalize(String var0) {
        Object var1 = " " + var0.toLowerCase(Locale.ROOT).trim() + " ";
        for (String[] var5 : ALIASES) {
            var1 = ((String)var1).replaceAll("(?<![a-z0-9])" + Pattern.quote(var5[0]) + "(?![a-z0-9])", var5[1]);
        }
        return ((String)var1).replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
    }

    public static String matchGenre(String var0) {
        if (var0 == null) {
            return null;
        }
        for (String var4 : var0.split("[,;|/]")) {
            String var5 = GenreWeather.normalize(var4);
            if (var5.isEmpty()) continue;
            String var6 = "-" + var5 + "-";
            String var7 = null;
            for (String var9 : TABLE.keySet()) {
                if (!var6.contains("-" + var9 + "-") || var7 != null && var9.length() <= var7.length()) continue;
                var7 = var9;
            }
            if (var7 == null) continue;
            return var7;
        }
        String var10 = GenreWeather.normalize(var0);
        String var11 = "-" + var10 + "-";
        String var12 = null;
        for (String var14 : TABLE.keySet()) {
            if (!var11.contains("-" + var14 + "-") || var12 != null && var14.length() <= var12.length()) continue;
            var12 = var14;
        }
        return var12;
    }

    public static Weather weatherFor(String var0) {
        if (var0 == null) {
            return null;
        }
        String var1 = GenreWeather.matchGenre(var0);
        return var1 == null ? Weather.CLEAR : TABLE.get(var1);
    }

    private static String lookup(MediaWatcher.Track var0) throws Exception {
        String var6;
        String var1 = LyricsFetcher.cleanArtist(var0.artist());
        String var2 = LyricsFetcher.cleanTitle(var0.title());
        String var3 = var0.app().toLowerCase(Locale.ROOT);
        boolean var4 = var3.contains("chrome") || var3.contains("firefox") || var3.contains("opera") || var3.contains("edge") || var3.contains("brave");
        int var5 = var0.title().indexOf(" - ");
        if (var5 > 0 && (var4 || var1.isEmpty())) {
            var1 = LyricsFetcher.cleanArtist(var0.title().substring(0, var5));
            var2 = LyricsFetcher.cleanTitle(var0.title().substring(var5 + 3));
        }
        if ((var6 = GenreWeather.itunes(var1, var2)) == null || var6.isEmpty()) {
            var6 = GenreWeather.deezer(var1, var2);
        }
        return var6 == null ? "" : var6;
    }

    private static String itunes(String var0, String var1) throws Exception {
        String var2 = "https://itunes.apple.com/search?media=music&entity=song&limit=10&country=DE&term=" + GenreWeather.enc((var0 + " " + var1).trim());
        JsonElement var3 = GenreWeather.get(var2);
        if (var3 != null && var3.isJsonObject()) {
            JsonArray var4 = var3.getAsJsonObject().getAsJsonArray("results");
            if (var4 == null) {
                return null;
            }
            String var5 = null;
            for (JsonElement var7 : var4) {
                JsonObject var8 = var7.getAsJsonObject();
                String var9 = GenreWeather.str(var8, "primaryGenreName");
                if (var9.isEmpty()) continue;
                if (var5 == null) {
                    var5 = var9;
                }
                if (var0.isEmpty() || !GenreWeather.str(var8, "artistName").toLowerCase(Locale.ROOT).contains(var0.toLowerCase(Locale.ROOT))) continue;
                return var9;
            }
            return var5;
        }
        return null;
    }

    private static String deezer(String var0, String var1) throws Exception {
        Object var2 = var0.isEmpty() ? var1 : "artist:\"" + var0 + "\" track:\"" + var1 + "\"";
        JsonElement var3 = GenreWeather.get("https://api.deezer.com/search?limit=1&q=" + GenreWeather.enc((String)var2));
        if (var3 != null && var3.isJsonObject()) {
            JsonArray var4 = var3.getAsJsonObject().getAsJsonArray("data");
            if (var4 != null && !var4.isEmpty()) {
                JsonObject var5 = var4.get(0).getAsJsonObject().getAsJsonObject("album");
                if (var5 != null && var5.has("id")) {
                    JsonElement var6 = GenreWeather.get("https://api.deezer.com/album/" + var5.get("id").getAsLong());
                    if (var6 != null && var6.isJsonObject()) {
                        JsonObject var7 = var6.getAsJsonObject().getAsJsonObject("genres");
                        if (var7 == null) {
                            return null;
                        }
                        JsonArray var8 = var7.getAsJsonArray("data");
                        if (var8 != null && !var8.isEmpty()) {
                            StringBuilder var9 = new StringBuilder();
                            for (JsonElement var11 : var8) {
                                if (var9.length() > 0) {
                                    var9.append(", ");
                                }
                                var9.append(GenreWeather.str(var11.getAsJsonObject(), "name"));
                            }
                            return var9.toString();
                        }
                        return null;
                    }
                    return null;
                }
                return null;
            }
            return null;
        }
        return null;
    }

    private static JsonElement get(String var0) throws Exception {
        HttpRequest var1 = HttpRequest.newBuilder(URI.create(var0)).timeout(Duration.ofSeconds(10L)).header("User-Agent", UA).GET().build();
        HttpResponse<String> var2 = HTTP.send(var1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return var2.statusCode() != 200 ? null : JsonParser.parseString((String)var2.body());
    }

    private static String str(JsonObject var0, String var1) {
        return var0.has(var1) && !var0.get(var1).isJsonNull() ? var0.get(var1).getAsString().trim() : "";
    }

    private static String enc(String var0) {
        return URLEncoder.encode(var0, StandardCharsets.UTF_8);
    }

    public static Weather target(LyricsConfig var0) {
        if (var0.enabled && var0.worldSync && var0.genreWeather) {
            Weather var3;
            long var1 = System.nanoTime();
            Weather weather = var3 = MediaWatcher.isPlaying() ? GenreWeather.weatherFor(GenreWeather.genre(MediaWatcher.track())) : null;
            if (var3 != null) {
                lastWeather = var3;
                lastWeatherNanos = var1;
                return var3;
            }
            if (lastWeather != null && (MediaWatcher.isPlaying() || var1 - lastWeatherNanos < 12000000000L)) {
                if (MediaWatcher.isPlaying()) {
                    lastWeatherNanos = var1;
                }
                return lastWeather;
            }
            lastWeather = null;
            return null;
        }
        lastWeather = null;
        return null;
    }

    public static void tick(Minecraft var0) {
        ClientLevel var1 = var0.level;
        LocalPlayer var2 = var0.player;
        if (var1 != null && var2 != null) {
            Weather var5;
            if (var1 != lastLevel) {
                lastLevel = var1;
                overriding = false;
                lastSetThunder = -1.0f;
                lastSetRain = -1.0f;
            }
            float var3 = var1.getRainLevel(1.0f);
            float var4 = var1.getThunderLevel(1.0f);
            if (!overriding || Math.abs(var3 - lastSetRain) > 0.002f) {
                serverRain = var3;
            }
            if (!overriding || Math.abs(var4 - lastSetThunder) > 0.002f) {
                serverThunder = var4;
            }
            if ((var5 = GenreWeather.target(SkyLyrics.cfg)) == null) {
                if (overriding) {
                    curRain = GenreWeather.approach(curRain, serverRain, 0.02f);
                    curThunder = GenreWeather.approach(curThunder, serverThunder, 0.02f);
                    GenreWeather.apply(var1);
                    if (Math.abs(curRain - serverRain) < 0.01f && Math.abs(curThunder - serverThunder) < 0.01f) {
                        var1.setRainLevel(serverRain);
                        var1.setThunderLevel(serverThunder);
                        overriding = false;
                    }
                }
            } else {
                if (!overriding) {
                    overriding = true;
                    curRain = var3;
                    curThunder = var4;
                }
                curRain = GenreWeather.approach(curRain, var5.rain, 0.02f);
                curThunder = GenreWeather.approach(curThunder, var5.thunder, 0.02f);
                GenreWeather.apply(var1);
                if (var5 == Weather.THUNDER && curThunder > 0.8f && --nextFlashTicks <= 0) {
                    nextFlashTicks = 160 + RNG.nextInt(340);
                    var1.setSkyFlashTime(2);
                    double var6 = RNG.nextDouble() * Math.PI * 2.0;
                    var1.playLocalSound(var2.getX() + Math.cos(var6) * 60.0, var2.getY() + 20.0, var2.getZ() + Math.sin(var6) * 60.0, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 2.5f, 0.8f + RNG.nextFloat() * 0.2f, false);
                }
            }
        } else {
            overriding = false;
            lastLevel = null;
            lastSetThunder = -1.0f;
            lastSetRain = -1.0f;
        }
    }

    private static void apply(ClientLevel var0) {
        var0.setRainLevel(curRain);
        var0.setThunderLevel(curThunder);
        lastSetRain = var0.getRainLevel(1.0f);
        lastSetThunder = var0.getThunderLevel(1.0f);
    }

    private static float approach(float var0, float var1, float var2) {
        return var0 < var1 ? Math.min(var1, var0 + var2) : Math.max(var1, var0 - var2);
    }

    public static String status() {
        MediaWatcher.Track var0 = MediaWatcher.track();
        if (var0 == null) {
            return T.tr("kein Song");
        }
        String var1 = GenreWeather.genre(var0);
        if (var1 == null) {
            return genres.containsKey(var0.key()) ? T.tr("Genre unbekannt") : T.tr("Genre wird gesucht\u2026");
        }
        String var2 = GenreWeather.matchGenre(var1);
        String var3 = var2 != null ? var2 : var1.split(",")[0].trim();
        return var3 + " \u2192 " + T.tr(GenreWeather.weatherFor((String)var1).label);
    }

    static {
        lastSetRain = -1.0f;
        lastSetThunder = -1.0f;
        nextFlashTicks = 200;
        RNG = new Random();
        TABLE = new HashMap<String, Weather>();
        ALIASES = new String[][]{{"r&b", "r-n-b"}, {"rnb", "r-n-b"}, {"rhythm and blues", "r-n-b"}, {"rhythm & blues", "r-n-b"}, {"rock & roll", "rock-n-roll"}, {"rock and roll", "rock-n-roll"}, {"rock'n'roll", "rock-n-roll"}, {"rock 'n' roll", "rock-n-roll"}, {"drum & bass", "drum-and-bass"}, {"drum and bass", "drum-and-bass"}, {"drum'n'bass", "drum-and-bass"}, {"dnb", "drum-and-bass"}, {"singer/songwriter", "singer-songwriter"}, {"liedermacher", "singer-songwriter"}, {"hip hop", "hip-hop"}, {"hiphop", "hip-hop"}, {"deutschrap", "hip-hop"}, {"rap", "hip-hop"}, {"lo-fi", "chill"}, {"lofi", "chill"}, {"lo fi", "chill"}, {"electronica", "electronic"}, {"elektronisch", "electronic"}, {"children's music", "kids"}, {"kinder", "kids"}, {"show tunes", "show-tunes"}, {"musicals", "show-tunes"}, {"christian & gospel", "gospel"}, {"bossa nova", "bossanova"}, {"new age", "new-age"}, {"trip hop", "trip-hop"}, {"synthpop", "synth-pop"}, {"klassik", "classical"}, {"volksmusik", "folk"}, {"schlager", "german"}, {"film", "pop-film"}, {"heavy metal", "heavy-metal"}, {"hard rock", "hard-rock"}, {"punk rock", "punk-rock"}, {"death metal", "death-metal"}, {"black metal", "black-metal"}, {"alt rock", "alt-rock"}, {"alternative rock", "alternative"}, {"psychedelic rock", "psych-rock"}, {"deep house", "deep-house"}, {"chicago house", "chicago-house"}, {"detroit techno", "detroit-techno"}, {"minimal techno", "minimal-techno"}, {"progressive house", "progressive-house"}, {"indie pop", "indie-pop"}, {"world", "world-music"}, {"k pop", "k-pop"}, {"j pop", "j-pop"}};
        GenreWeather.put(Weather.THUNDER, "alt-rock, alternative, black-metal, breakbeat, chicago-house, death-metal, deep-house, detroit-techno, drum-and-bass, dubstep, emo, goth, grunge, hard-rock, hardcore, hardstyle, heavy-metal, industrial, metal, metal-misc, metalcore, minimal-techno, psych-rock, punk, punk-rock, rock, rock-n-roll, rockabilly, trance, techno");
        GenreWeather.put(Weather.RAIN, "acoustic, ambient, bluegrass, blues, bossanova, chill, classical, country, folk, groove, gospel, indie, indie-pop, jazz, new-age, piano, post-dubstep, r-n-b, sad, show-tunes, singer-songwriter, sleep, soul, soundtrack, study, trip-hop, work-out");
        GenreWeather.put(Weather.CLEAR, "afrobeat, anime, brazil, cantopop, club, comedy, dance, dancehall, disco, disney, edm, electro, electronic, forro, french, funk, garage, german, happy, hip-hop, honky-tonk, house, idm, indian, iranian, j-dance, j-idol, j-pop, j-rock, k-pop, kids, latin, latino, malay, mandopop, mpb, pagode, party, philippines-opm, pop, pop-film, progressive-house, samba, sertanejo, reggae, reggaeton, ska, spanish, swedish, synth-pop, tango, techno-pop, world-music");
        GenreWeather.put(Weather.THUNDER, "trap, drill, phonk");
    }

    public static enum Weather {
        CLEAR("klar", 0.0f, 0.0f),
        RAIN("Regen", 1.0f, 0.0f),
        THUNDER("Gewitter", 1.0f, 1.0f);

        public final String label;
        final float rain;
        final float thunder;

        private Weather(String nullxx, float nullxxx, float nullxxxx) {
            this.label = nullxx;
            this.rain = nullxxx;
            this.thunder = nullxxxx;
        }
    }
}

