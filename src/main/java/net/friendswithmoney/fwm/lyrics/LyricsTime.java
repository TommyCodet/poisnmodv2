/*
 * Decompiled with CFR 0.152.
 */
package net.friendswithmoney.fwm.lyrics;

import java.util.regex.Pattern;
import net.friendswithmoney.fwm.lyrics.Lyrics;
import net.friendswithmoney.fwm.lyrics.LyricsFetcher;
import net.friendswithmoney.fwm.lyrics.MediaWatcher;
import net.friendswithmoney.fwm.lyrics.SkyLyrics;
import net.friendswithmoney.fwm.lyrics.SongOffsets;

public final class LyricsTime {
    private static final Pattern TEMPO = Pattern.compile("(?i)sped|speed ?up|slowed|nightcore|daycore|\\bslow(?:ed)? ?\\+ ?reverb");

    private LyricsTime() {
    }

    public static double scale(MediaWatcher.Track var0, Lyrics var1) {
        if (var0 != null && var1 != null && var1.synced && var1.sourceDurationMs > 0L && var0.durationMs() > 0L) {
            if (!TEMPO.matcher(var0.title() + " " + var0.album()).find()) {
                return 1.0;
            }
            double var2 = (double)var1.sourceDurationMs / (double)var0.durationMs();
            return Math.abs(var2 - 1.0) >= 0.05 && Math.abs(var2 - 1.0) <= 0.4 ? var2 : 1.0;
        }
        return 1.0;
    }

    public static int offset(MediaWatcher.Track var0) {
        return SkyLyrics.cfg.offsetMs + (var0 == null ? 0 : SongOffsets.get(var0.key()));
    }

    public static long songMs() {
        MediaWatcher.Track var0 = MediaWatcher.track();
        long var1 = MediaWatcher.positionMs();
        if (var0 == null) {
            return var1 - (long)SkyLyrics.cfg.offsetMs;
        }
        double var3 = LyricsTime.scale(var0, LyricsFetcher.peek(var0));
        return Math.round((double)var1 * var3) - (long)LyricsTime.offset(var0);
    }
}

