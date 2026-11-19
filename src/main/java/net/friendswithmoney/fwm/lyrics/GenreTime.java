/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 */
package net.friendswithmoney.fwm.lyrics;

import java.util.HashMap;
import java.util.Map;
import net.friendswithmoney.fwm.lyrics.GenreWeather;
import net.friendswithmoney.fwm.lyrics.LyricsConfig;
import net.friendswithmoney.fwm.lyrics.MediaWatcher;
import net.friendswithmoney.fwm.lyrics.SkyLyrics;
import net.friendswithmoney.fwm.lyrics.T;
import net.minecraft.client.Minecraft;

public final class GenreTime {
    private static final Map<String, Mood> TABLE = new HashMap<String, Mood>();
    private static volatile double cur = -1.0;
    private static volatile boolean overriding;
    private static volatile long realTod;
    private static Mood lastMood;
    private static long lastMoodNanos;

    private static void put(Mood var0, String var1) {
        for (String var5 : var1.split(",")) {
            TABLE.put(var5.trim(), var0);
        }
    }

    private GenreTime() {
    }

    public static Mood moodFor(String var0) {
        if (var0 == null) {
            return null;
        }
        String var1 = GenreWeather.matchGenre(var0);
        return var1 == null ? null : TABLE.getOrDefault(var1, Mood.DAY);
    }

    public static Mood target() {
        LyricsConfig var0 = SkyLyrics.cfg;
        if (var0.enabled && var0.worldSync && var0.genreTime) {
            Mood var3;
            long var1 = System.nanoTime();
            Mood mood = var3 = MediaWatcher.isPlaying() ? GenreTime.moodFor(GenreWeather.genre(MediaWatcher.track())) : null;
            if (var3 != null) {
                lastMood = var3;
                lastMoodNanos = var1;
                return var3;
            }
            if (lastMood != null && MediaWatcher.isPlaying()) {
                lastMoodNanos = var1;
                return lastMood;
            }
            if (lastMood != null && var1 - lastMoodNanos < 12000000000L) {
                return lastMood;
            }
            lastMood = null;
            return null;
        }
        lastMood = null;
        return null;
    }

    private static double ahead(double var0, double var2) {
        double var4 = (var2 - var0) % 24000.0;
        return var4 < 0.0 ? var4 + 24000.0 : var4;
    }

    public static void tick(Minecraft var0) {
        if (var0.level != null && realTod >= 0L) {
            double var2;
            Mood var1 = GenreTime.target();
            double d = var2 = var1 != null ? (double)var1.time : (double)realTod;
            if (var1 != null && !overriding) {
                overriding = true;
                cur = realTod;
            }
            if (overriding) {
                double var4 = GenreTime.ahead(cur, var2);
                if (var1 == null && GenreTime.ahead(var2, cur) < 600.0) {
                    overriding = false;
                    cur = -1.0;
                } else {
                    double var6 = Math.max(12.0, Math.min(420.0, var4 * 0.07));
                    if (!(var4 <= var6) && !(var4 < 2.0)) {
                        cur = (cur + var6) % 24000.0;
                    } else {
                        cur = var2;
                        if (var1 == null) {
                            overriding = false;
                            cur = -1.0;
                            return;
                        }
                    }
                }
            }
        } else {
            overriding = false;
            cur = -1.0;
        }
    }

    public static long override(long var0) {
        long var2;
        realTod = var2 = Math.floorMod(var0, 24000L);
        double var4 = cur;
        return overriding && !(var4 < 0.0) ? var0 - var2 + (long)var4 : var0;
    }

    public static String status() {
        MediaWatcher.Track var0 = MediaWatcher.track();
        if (var0 == null) {
            return T.tr("kein Song");
        }
        String var1 = GenreWeather.genre(var0);
        if (var1 == null) {
            return T.tr("Genre wird gesucht\u2026");
        }
        Mood var2 = GenreTime.moodFor(var1);
        return var2 == null ? T.tr("Genre unbekannt") : T.tr(var2.label);
    }

    static {
        realTod = -1L;
        GenreTime.put(Mood.NIGHT, "black-metal, death-metal, metal, metal-misc, metalcore, heavy-metal, goth, industrial, hardstyle, hardcore, dubstep, drum-and-bass, techno, minimal-techno, detroit-techno, trance, deep-house, chicago-house, breakbeat, trap, drill, phonk, club, edm, sleep, ambient, idm, emo, grunge");
        GenreTime.put(Mood.SUNSET, "r-n-b, soul, jazz, blues, chill, trip-hop, sad, indie, indie-pop, acoustic, piano, singer-songwriter, post-dubstep, groove, rock, alt-rock, alternative, hard-rock, psych-rock, punk, punk-rock, rock-n-roll, rockabilly, hip-hop, garage, synth-pop, techno-pop, electro, electronic, house, progressive-house, tango, bossanova, soundtrack, show-tunes");
        GenreTime.put(Mood.SUNRISE, "classical, new-age, folk, country, bluegrass, gospel, study, work-out, world-music, iranian, indian");
        GenreTime.put(Mood.DAY, "afrobeat, anime, brazil, cantopop, comedy, dance, dancehall, disco, disney, forro, french, funk, german, happy, honky-tonk, j-dance, j-idol, j-pop, j-rock, k-pop, kids, latin, latino, malay, mandopop, mpb, pagode, party, philippines-opm, pop, pop-film, samba, sertanejo, reggae, reggaeton, ska, spanish, swedish");
    }

    public static enum Mood {
        SUNRISE("Sonnenaufgang", 23300L),
        DAY("Mittag", 6000L),
        SUNSET("Sonnenuntergang", 12300L),
        NIGHT("Nacht", 18000L);

        public final String label;
        final long time;

        private Mood(String nullxx, long nullxxx) {
            this.label = nullxx;
            this.time = nullxxx;
        }
    }
}

