/*
 * Decompiled with CFR 0.152.
 */
package net.friendswithmoney.fwm.lyrics;

import net.friendswithmoney.fwm.lyrics.LyricsConfig;
import net.friendswithmoney.fwm.lyrics.MediaWatcher;
import net.friendswithmoney.fwm.lyrics.SkyLyrics;
import net.friendswithmoney.fwm.lyrics.T;

public final class BeatSync {
    private static volatile double bpm;
    private static volatile double level;
    private static volatile long anchorNanos;
    private static volatile long lastMsgNanos;

    private BeatSync() {
    }

    static void update(double var0, long var2, double var4, long var6) {
        lastMsgNanos = var6;
        level += (var4 - level) * 0.5;
        if (!(var0 <= 0.0) && var2 >= 0L) {
            long var8 = var6 - var2 * 1000000L;
            double var10 = bpm;
            bpm = var0;
            if (!(var10 <= 0.0) && !(Math.abs(var10 - var0) > 3.0) && anchorNanos != 0L) {
                double var12 = 6.0E10 / var0;
                double var14 = (double)(var8 - anchorNanos) % var12;
                if (var14 > var12 / 2.0) {
                    var14 -= var12;
                } else if (var14 < -var12 / 2.0) {
                    var14 += var12;
                }
                anchorNanos += (long)(var14 * 0.5);
            } else {
                anchorNanos = var8;
            }
        } else if (var0 <= 0.0) {
            bpm = 0.0;
        }
    }

    public static boolean active() {
        LyricsConfig var0 = SkyLyrics.cfg;
        return var0.enabled && var0.beatSync && MediaWatcher.isPlaying() && bpm > 0.0 && level > 0.05 && System.nanoTime() - lastMsgNanos < 3000000000L;
    }

    public static double bpm() {
        return bpm;
    }

    public static double level() {
        return level;
    }

    private static double period() {
        return 6.0E10 / Math.max(40.0, bpm);
    }

    public static float phase(long var0) {
        double var2 = (double)(var0 - anchorNanos) / BeatSync.period();
        return (float)(var2 - Math.floor(var2));
    }

    public static long index(long var0) {
        return (long)Math.floor((double)(var0 - anchorNanos) / BeatSync.period());
    }

    public static float pulse(long var0) {
        if (!BeatSync.active()) {
            return 0.0f;
        }
        float var2 = BeatSync.phase(var0);
        float var3 = (float)Math.min(1.0, 0.45 + level * 1.2);
        return (float)Math.exp((double)(-var2) * 7.0) * var3;
    }

    public static String status() {
        if (!SkyLyrics.cfg.beatSync) {
            return T.tr("aus");
        }
        if (!MediaWatcher.supported()) {
            return T.tr("nur unter Windows");
        }
        if (!MediaWatcher.isPlaying()) {
            return T.tr("wartet auf Musik");
        }
        return bpm <= 0.0 ? T.tr("h\u00f6rt zu \u2026") : Math.round(bpm) + " BPM";
    }
}

