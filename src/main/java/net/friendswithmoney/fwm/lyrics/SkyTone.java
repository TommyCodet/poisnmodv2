/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.world.level.Level
 */
package net.friendswithmoney.fwm.lyrics;

import net.friendswithmoney.fwm.lyrics.LyricsConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;

public final class SkyTone {
    private static float smoothed = -1.0f;
    private static long lastNanos;

    private SkyTone() {
    }

    public static float skyLuminance(ClientLevel var0, float var1) {
        if (var0.dimension() == Level.NETHER) {
            return 0.18f;
        }
        if (var0.dimension() == Level.END) {
            return 0.05f;
        }
        long var2 = Math.floorMod(var0.getOverworldClockTime(), 24000L);
        double var4 = (double)var2 / 24000.0 - 0.25;
        var4 -= Math.floor(var4);
        double var6 = 0.5 - Math.cos(var4 * Math.PI) / 2.0;
        double var8 = (var4 * 2.0 + var6) / 3.0;
        double var10 = Math.cos(var8 * Math.PI * 2.0) * 2.0 + 0.5;
        var10 = Math.max(0.0, Math.min(1.0, var10));
        float var12 = var0.getRainLevel(var1);
        float var13 = var0.getThunderLevel(var1);
        var10 *= 1.0 - (double)var12 * 0.42;
        return (float)((var10 *= 1.0 - (double)var13 * 0.55) * 0.72);
    }

    public static float luminance(ClientLevel var0, float var1) {
        float var2 = SkyTone.skyLuminance(var0, var1);
        long var3 = System.nanoTime();
        float var5 = lastNanos == 0L ? 1.0f : Math.min(1.0f, (float)(var3 - lastNanos) / 1.0E9f);
        lastNanos = var3;
        if (smoothed < 0.0f) {
            smoothed = var2;
        }
        smoothed += (var2 - smoothed) * Math.min(1.0f, var5 * 2.5f);
        return smoothed;
    }

    public static int textColor(LyricsConfig var0, float var1) {
        String var4;
        int var2 = LyricsConfig.rgb(var0.lightTextColor, 0xFFF8E8);
        int var3 = LyricsConfig.rgb(var0.darkTextColor, 1975360);
        switch (var4 = var0.colorMode.toLowerCase()) {
            case "light": {
                return var2;
            }
            case "dark": {
                return var3;
            }
            case "custom": {
                return LyricsConfig.rgb(var0.customColor, 16765562);
            }
        }
        float var7 = SkyTone.smoothstep(0.3f, 0.42f, var1);
        return SkyTone.lerp(var2, var3, var7);
    }

    public static int haloColor(int var0) {
        int var1 = var0 >> 16 & 0xFF;
        int var2 = var0 >> 8 & 0xFF;
        int var3 = var0 & 0xFF;
        float var4 = (0.299f * (float)var1 + 0.587f * (float)var2 + 0.114f * (float)var3) / 255.0f;
        return var4 > 0.5f ? 658456 : 0xFFFFFF;
    }

    static float smoothstep(float var0, float var1, float var2) {
        float var3 = Math.max(0.0f, Math.min(1.0f, (var2 - var0) / (var1 - var0)));
        return var3 * var3 * (3.0f - 2.0f * var3);
    }

    static int lerp(int var0, int var1, float var2) {
        int var3 = var0 >> 16 & 0xFF;
        int var4 = var0 >> 8 & 0xFF;
        int var5 = var0 & 0xFF;
        int var6 = var1 >> 16 & 0xFF;
        int var7 = var1 >> 8 & 0xFF;
        int var8 = var1 & 0xFF;
        int var9 = Math.round((float)var3 + (float)(var6 - var3) * var2);
        int var10 = Math.round((float)var4 + (float)(var7 - var4) * var2);
        int var11 = Math.round((float)var5 + (float)(var8 - var5) * var2);
        return var9 << 16 | var10 << 8 | var11;
    }
}

