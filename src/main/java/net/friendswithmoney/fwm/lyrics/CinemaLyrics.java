/*
 * Decompiled with CFR 0.152.
 */
package net.friendswithmoney.fwm.lyrics;

import net.friendswithmoney.fwm.lyrics.AfkCinema;
import net.friendswithmoney.fwm.lyrics.CinemaShots;
import net.friendswithmoney.fwm.lyrics.LyricsConfig;
import net.friendswithmoney.fwm.lyrics.SkyText;

public final class CinemaLyrics {
    private static boolean lastActive;
    private static int lastShot;

    private CinemaLyrics() {
    }

    static boolean cinema() {
        try {
            return AfkCinema.isActive();
        }
        catch (Throwable var1) {
            return false;
        }
    }

    public static void frame() {
        int var1;
        boolean var0 = CinemaLyrics.cinema();
        int n = var1 = var0 ? CinemaShots.currentShot() : Integer.MIN_VALUE;
        if (var0 != lastActive || var1 != lastShot) {
            SkyText.reset();
        }
        lastActive = var0;
        lastShot = var1;
    }

    public static boolean openSkyOnly(LyricsConfig var0) {
        return var0.onlyUnderOpenSky && !CinemaLyrics.cinema();
    }

    static {
        lastShot = Integer.MIN_VALUE;
    }
}

