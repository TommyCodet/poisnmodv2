/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.Identifier
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.friendswithmoney.fwm.lyrics;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LazySkyFonts {
    private static final Logger LOG = LoggerFactory.getLogger((String)"FWM Sky Lyrics");
    public static boolean EAGER = Boolean.getBoolean("fwm.eagerSkyFonts");
    private static boolean building;
    private static int skipped;
    private static long timerStart;
    private static long vanillaNanos;

    private LazySkyFonts() {
    }

    public static boolean isSkyFont(Identifier id) {
        return id != null && "fwm".equals(id.getNamespace()) && id.getPath().startsWith("sky_");
    }

    public static void begin(Identifier id) {
        building = LazySkyFonts.isSkyFont(id);
    }

    public static void end() {
        building = false;
    }

    public static boolean building() {
        return building;
    }

    public static void skipped() {
        ++skipped;
    }

    public static void startTimer(long t0) {
        timerStart = t0;
    }

    public static void stopTimer() {
        if (timerStart != 0L) {
            vanillaNanos += System.nanoTime() - timerStart;
        }
        timerStart = 0L;
    }

    public static void report() {
        LOG.info("Schriften geladen: Breiten-Index {} ms, bei {} Schriften auf sp\u00e4ter verschoben{}", new Object[]{vanillaNanos / 1000000L, skipped, EAGER ? " (eager-Vergleichsmodus)" : ""});
        vanillaNanos = 0L;
        skipped = 0;
    }
}

