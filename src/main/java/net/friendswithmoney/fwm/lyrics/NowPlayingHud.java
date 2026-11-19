/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.renderer.RenderPipelines
 */
package net.friendswithmoney.fwm.lyrics;

import net.friendswithmoney.fwm.lyrics.CoverTexture;
import net.friendswithmoney.fwm.lyrics.Lyrics;
import net.friendswithmoney.fwm.lyrics.LyricsConfig;
import net.friendswithmoney.fwm.lyrics.LyricsFetcher;
import net.friendswithmoney.fwm.lyrics.LyricsTime;
import net.friendswithmoney.fwm.lyrics.MediaWatcher;
import net.friendswithmoney.fwm.lyrics.SkyLyrics;
import net.friendswithmoney.fwm.lyrics.SkyTone;
import net.friendswithmoney.fwm.lyrics.T;
import net.friendswithmoney.fwm.ui.FwmScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

public final class NowPlayingHud {
    private static float show = 0.0f;
    private static long lastNanos;
    private static final int TEXT_MAX = 190;
    private static volatile long flashNanos;

    private NowPlayingHud() {
    }

    public static void flash() {
        flashNanos = System.nanoTime();
    }

    public static void render(GuiGraphicsExtractor var0) {
        Minecraft var1 = Minecraft.getInstance();
        LyricsConfig var2 = SkyLyrics.cfg;
        if (flashNanos != 0L && var1.player != null) {
            float var3 = (float)(System.nanoTime() - flashNanos) / 1.0E9f;
            if (var3 < 0.55f) {
                float var4 = 1.0f - var3 / 0.55f;
                int var5 = Math.round(var4 * var4 * 0.42f * 255.0f);
                if (var5 > 2) {
                    var0.fill(0, 0, var0.guiWidth(), var0.guiHeight(), var5 << 24 | 0xFFFFFF);
                }
            } else {
                flashNanos = 0L;
            }
        }
        if (var1.player != null && !(var1.gui.screen() instanceof FwmScreen)) {
            long var23 = System.nanoTime();
            float var24 = lastNanos == 0L ? 0.0f : Math.min(0.1f, (float)(var23 - lastNanos) / 1.0E9f);
            lastNanos = var23;
            MediaWatcher.Track var6 = MediaWatcher.track();
            boolean var7 = var2.enabled && var2.pillEnabled && var6 != null && MediaWatcher.alive() && MediaWatcher.isPlaying();
            if (!((show += ((var7 ? 1.0f : 0.0f) - show) * Math.min(1.0f, var24 * 6.0f)) < 0.02f) && var6 != null) {
                Object var8 = var6.title() + (String)(var6.artist().isEmpty() ? "" : "  \u00b7  " + var6.artist());
                float var9 = -1.0f;
                Lyrics var10 = LyricsFetcher.get(var6);
                long var11 = LyricsTime.songMs();
                String var13 = "";
                if (var10 != null && var10.synced) {
                    int var14;
                    if (var10.approximate) {
                        var13 = T.tr("\u2248 ungef\u00e4hr");
                    }
                    if ((var14 = var10.indexAt(var11)) >= 0 && !var10.lines.get(var14).text().isBlank() && var11 < var10.endOf(var14, var6.durationMs())) {
                        Lyrics.Line var15 = var10.lines.get(var14);
                        var8 = var15.text();
                        int var16 = var15.words() == null ? 0 : var15.words().size();
                        var9 = var16 == 0 ? 0.0f : Math.min(1.0f, Lyrics.wordPosition(var15, var11) / (float)var16);
                    }
                } else {
                    var13 = var10 != null && var10.instrumental ? T.tr("Instrumental") : LyricsFetcher.statusText(var6);
                }
                float var25 = var6.durationMs() > 0L ? Math.min(1.0f, (float)MediaWatcher.positionMs() / (float)var6.durationMs()) : -1.0f;
                boolean var26 = CoverTexture.update(var6.key());
                Font var27 = var1.font;
                int var17 = var0.guiWidth();
                int var18 = var0.guiHeight();
                int var19 = NowPlayingHud.pillWidth(var27, (String)var8, var13);
                int var20 = (var17 - var19) / 2;
                boolean var21 = "bottom".equalsIgnoreCase(var2.pillPosition);
                int var22 = var21 ? var18 - 23 - 40 - 20 : 3;
                NowPlayingHud.drawPill(var0, var20, var22 += Math.round((1.0f - show) * (float)(var21 ? 10 : -10)), (String)var8, var13, var9, var25, MediaWatcher.isPlaying(), var26, show, var2);
            }
        }
    }

    public static int pillWidth(Font var0, String var1, String var2) {
        int var3 = Math.min(var0.width(var1), 190);
        int var4 = var2.isEmpty() ? 0 : var0.width(var2) + 6;
        return 26 + var3 + var4 + 8 + 13 + 6;
    }

    public static void drawPill(GuiGraphicsExtractor var0, int var1, int var2, String var3, String var4, float var5, float var6, boolean var7, boolean var8, float var9, LyricsConfig var10) {
        Font var11 = Minecraft.getInstance().font;
        long var12 = System.nanoTime();
        int var14 = LyricsConfig.rgb(var10.accentColor, 10980346);
        int var15 = NowPlayingHud.pillWidth(var11, var3, var4);
        int var16 = 20;
        int var17 = Math.min(var11.width(var3), 190);
        int var18 = Math.round(var9 * 255.0f);
        float var19 = (float)var10.pillOpacity / 100.0f;
        NowPlayingHud.roundRect(var0, var1 - 1, var2 - 1, var15 + 2, var16 + 2, 10, NowPlayingHud.argb(Math.round((float)var18 * 0.55f), NowPlayingHud.mix(var14, 2761792, 0.55f)));
        NowPlayingHud.roundRect(var0, var1, var2, var15, var16, 9, NowPlayingHud.argb(Math.round((float)var18 * var19), 1380895));
        int var20 = var1 + 6;
        int var21 = var2 + 3;
        if (var8) {
            var0.blit(RenderPipelines.GUI_TEXTURED, CoverTexture.ID, var20, var21, 0.0f, 0.0f, 14, 14, 14, 14);
        } else {
            NowPlayingHud.roundRect(var0, var20, var21, 14, 14, 3, NowPlayingHud.argb(var18, NowPlayingHud.mix(var14, 0, 0.35f)));
            var0.text(var11, "\u266a", var20 + 4, var21 + 3, NowPlayingHud.argb(var18, 0xFFFFFF), false);
        }
        int var22 = var20 + 14 + 6;
        int var23 = var2 + 6;
        int var24 = var11.width(var3);
        int var25 = 0;
        if (var24 > 190) {
            double var26 = (double)var12 / 1.0E9 % ((double)(var24 - 190) / 20.0 + 4.0);
            var25 = (int)Math.max(0.0, Math.min((double)(var24 - 190), (var26 - 1.5) * 20.0));
        }
        var0.enableScissor(var22, var2, var22 + var17, var2 + var16);
        if (var5 >= 0.0f) {
            String[] var33 = var3.split(" ");
            int var27 = Math.max(0, Math.min(var33.length - 1, (int)Math.floor(var5 * (float)var33.length)));
            int var28 = var22 - var25;
            int var29 = var11.width(" ");
            for (int var30 = 0; var30 < var33.length; ++var30) {
                int var31 = var11.width(var33[var30]);
                if (var30 == var27) {
                    NowPlayingHud.roundRect(var0, var28 - 2, var23 - 2, var31 + 4, 12, 4, NowPlayingHud.argb(Math.round((float)var18 * 0.8f), var14));
                }
                int var32 = var30 < var27 ? 0xF2EEFF : (var30 == var27 ? 0xFFFFFF : 9474216);
                var0.text(var11, var33[var30], var28, var23, NowPlayingHud.argb(var18, var32), false);
                var28 += var31 + var29;
            }
        } else {
            var0.text(var11, var3, var22 - var25, var23, NowPlayingHud.argb(var18, 15131125), false);
        }
        var0.disableScissor();
        if (!var4.isEmpty()) {
            var0.text(var11, var4, var22 + var17 + 6, var23, NowPlayingHud.argb(Math.round((float)var18 * 0.6f), 12104396), false);
        }
        int var34 = var1 + var15 - 6 - 13;
        int var35 = var2 + 5;
        double var36 = (double)var12 / 1.0E9;
        for (int var37 = 0; var37 < 4; ++var37) {
            int var39 = var7 ? 3 + (int)Math.round((Math.sin(var36 * (5.3 + (double)var37 * 1.7) + (double)var37 * 1.3) * 0.5 + 0.5) * 7.0) : 2;
            var0.fill(var34 + var37 * 3, var35 + 10 - var39, var34 + var37 * 3 + 2, var35 + 10, NowPlayingHud.argb(var18, var14));
        }
        if (var6 >= 0.0f) {
            int var38 = var1 + 9;
            int var40 = var1 + var15 - 9;
            var0.fill(var38, var2 + var16 - 2, var40, var2 + var16 - 1, NowPlayingHud.argb(Math.round((float)var18 * 0.25f), 0xFFFFFF));
            var0.fill(var38, var2 + var16 - 2, var38 + Math.round((float)(var40 - var38) * var6), var2 + var16 - 1, NowPlayingHud.argb(var18, var14));
        }
    }

    static void roundRect(GuiGraphicsExtractor var0, int var1, int var2, int var3, int var4, int var5, int var6) {
        var5 = Math.min(var5, Math.min(var3, var4) / 2);
        var0.fill(var1, var2 + var5, var1 + var3, var2 + var4 - var5, var6);
        for (int var7 = 0; var7 < var5; ++var7) {
            double var8 = (double)(var5 - var7) - 0.5;
            int var10 = (int)Math.round((double)var5 - Math.sqrt(Math.max(0.0, (double)(var5 * var5) - var8 * var8)));
            var0.fill(var1 + var10, var2 + var7, var1 + var3 - var10, var2 + var7 + 1, var6);
            var0.fill(var1 + var10, var2 + var4 - 1 - var7, var1 + var3 - var10, var2 + var4 - var7, var6);
        }
    }

    static int argb(int var0, int var1) {
        return Math.max(0, Math.min(255, var0)) << 24 | var1 & 0xFFFFFF;
    }

    static int mix(int var0, int var1, float var2) {
        return SkyTone.lerp(var0, var1, var2);
    }
}

