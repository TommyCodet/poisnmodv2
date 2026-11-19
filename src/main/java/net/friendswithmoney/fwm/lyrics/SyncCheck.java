/*
 * Decompiled with CFR 0.152.
 */
package net.friendswithmoney.fwm.lyrics;

import net.friendswithmoney.fwm.lyrics.Lyrics;
import net.friendswithmoney.fwm.lyrics.LyricsFetcher;
import net.friendswithmoney.fwm.lyrics.LyricsTime;
import net.friendswithmoney.fwm.lyrics.MediaWatcher;
import net.friendswithmoney.fwm.lyrics.SongOffsets;
import net.friendswithmoney.fwm.lyrics.T;

public final class SyncCheck {
    private SyncCheck() {
    }

    public static Status status(MediaWatcher.Track var0, Lyrics var1) {
        long var9;
        if (var0 == null || var1 == null || var1.instrumental || var1.approximate || var1.lines.isEmpty()) {
            return new Status(State.NONE, "");
        }
        if (LyricsFetcher.searchingVersion(var0)) {
            return new Status(State.ADAPTED, T.tr("\u21c4 sucht passende Version \u2026"));
        }
        long var2 = MediaWatcher.driftMs();
        if (MediaWatcher.isPlaying() && Math.abs(var2) > 400L) {
            return new Status(State.DRIFT, T.tr("\u21c4 gleicht ab \u2026 (") + (var2 > 0L ? "+" : "\u2212") + SyncCheck.sec(Math.abs(var2)) + ")");
        }
        if (MediaWatcher.sinceFixMs() < 6000L) {
            long var11 = MediaWatcher.lastFixMs();
            return new Status(State.FIXED, T.tr("\u21c4 Sync korrigiert (") + (var11 > 0L ? "+" : "\u2212") + SyncCheck.sec(Math.abs(var11)) + ")");
        }
        int var4 = SongOffsets.get(var0.key());
        double var5 = LyricsTime.scale(var0, var1);
        if (var5 != 1.0) {
            return new Status(State.ADAPTED, String.format(T.locale(), T.tr("\u2713 synchron \u00b7 Tempo angeglichen (\u00d7%.2f)"), var5));
        }
        if (LyricsFetcher.switchedVersion(var0)) {
            return new Status(State.OK, T.tr("\u2713 synchron \u00b7 passende Version geladen"));
        }
        if (var4 != 0) {
            return new Status(State.OK, T.tr("\u2713 synchron \u00b7 Song-Versatz ") + (var4 > 0 ? "+" : "\u2212") + SyncCheck.sec(Math.abs(var4)));
        }
        long var7 = var0.durationMs();
        long l = var9 = var7 > 0L && var1.sourceDurationMs > 0L ? var1.sourceDurationMs - var7 : 0L;
        return Math.abs(var9) > 2500L ? new Status(State.VERSION, T.tr("\u2248 andere Version (") + (var9 > 0L ? "+" : "\u2212") + SyncCheck.sec(Math.abs(var9)) + T.tr(") \u00b7 \u2212/+ merkt's")) : new Status(State.OK, T.tr("\u2713 Lyrics getaktet \u00b7 synchron"));
    }

    private static String sec(long var0) {
        return String.format(T.locale(), "%.1f s", (double)var0 / 1000.0);
    }

    public record Status(State state, String text) {
    }

    public static enum State {
        NONE,
        OK,
        FIXED,
        DRIFT,
        ADAPTED,
        VERSION;

    }
}

