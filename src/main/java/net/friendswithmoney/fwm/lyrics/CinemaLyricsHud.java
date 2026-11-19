/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.network.chat.Style
 *  net.minecraft.util.Mth
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix3x2fStack
 */
package net.friendswithmoney.fwm.lyrics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.friendswithmoney.fwm.lyrics.Lyrics;
import net.friendswithmoney.fwm.lyrics.LyricsConfig;
import net.friendswithmoney.fwm.lyrics.LyricsFetcher;
import net.friendswithmoney.fwm.lyrics.LyricsTime;
import net.friendswithmoney.fwm.lyrics.MediaWatcher;
import net.friendswithmoney.fwm.lyrics.SkyLyrics;
import net.friendswithmoney.fwm.lyrics.SkyText;
import net.friendswithmoney.fwm.lyrics.SkyTone;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3x2fStack;

public final class CinemaLyricsHud {
    private static final List<Shown> shown = new ArrayList<Shown>();
    private static long lastNanos;

    private CinemaLyricsHud() {
    }

    public static boolean active() {
        LyricsConfig var0 = SkyLyrics.cfg;
        return false;
    }

    public static void render(GuiGraphicsExtractor var0, int var1, int var2) {
        Minecraft var3 = Minecraft.getInstance();
        LyricsConfig var4 = SkyLyrics.cfg;
        long var5 = System.nanoTime();
        float var7 = lastNanos == 0L ? 0.0f : Math.min(0.1f, (float)(var5 - lastNanos) / 1.0E9f);
        lastNanos = var5;
        if (CinemaLyricsHud.active() && var3.player != null && var3.level != null) {
            int var13;
            Lyrics var12;
            Lyrics.Line var8 = null;
            MediaWatcher.Track var9 = MediaWatcher.track();
            long var10 = LyricsTime.songMs();
            if (var9 != null && MediaWatcher.isPlaying() && (var12 = LyricsFetcher.get(var9)) != null && var12.synced && !var12.lines.isEmpty() && (var13 = var12.indexAt(var10)) >= 0 && var10 < var12.endOf(var13, var9.durationMs()) && !var12.lines.get(var13).text().isBlank()) {
                var8 = var12.lines.get(var13);
            }
            boolean var22 = false;
            for (Shown var14 : shown) {
                if (var14.line == var8 && var14.dying < 0L) {
                    var22 = true;
                    continue;
                }
                if (var14.dying >= 0L) continue;
                var14.dying = var5;
            }
            int var24 = var0.guiWidth();
            int var25 = var0.guiHeight();
            Font var15 = var3.font;
            Style var16 = SkyText.style(var4);
            float[] var17 = CinemaLyricsHud.playerOnScreen(var3, var24, var25);
            if (var8 != null && !var22) {
                Shown var18 = new Shown(var8, var5);
                CinemaLyricsHud.pickSpot(var18, var17, var24, var25, var1, var2, var15, var16, var4);
                var18.fx = var18.tx;
                var18.fy = var18.ty;
                shown.add(var18);
            }
            Iterator<Shown> var26 = shown.iterator();
            while (var26.hasNext()) {
                Shown var19 = var26.next();
                if (var19.dying <= 0L || !((float)(var5 - var19.dying) / 1.0E9f > 0.45f)) continue;
                var26.remove();
            }
            for (Shown var20 : shown) {
                if (var20.dying < 0L && CinemaLyricsHud.overlapsPlayer(var20, var17, var24, var25, var15, var16, var4)) {
                    CinemaLyricsHud.pickSpot(var20, var17, var24, var25, var1, var2, var15, var16, var4);
                }
                float var21 = Math.min(1.0f, var7 * 3.0f);
                var20.fx += (var20.tx - var20.fx) * var21;
                var20.fy += (var20.ty - var20.fy) * var21;
                CinemaLyricsHud.draw(var0, var15, var20, var16, var4, var10, var5, var24, var25);
            }
        } else {
            shown.clear();
        }
    }

    private static float[] playerOnScreen(Minecraft var0, int var1, int var2) {
        Camera var3 = var0.gameRenderer.mainCamera();
        Vec3 var4 = var3.position();
        Vec3 var5 = var0.player.getPosition(1.0f);
        Vec3 var6 = var5.add(0.0, (double)var0.player.getBbHeight() * 0.55, 0.0);
        Vec3 var7 = var6.subtract(var4);
        double var8 = Math.toRadians(var3.yRot());
        double var10 = Math.toRadians(var3.xRot());
        Vec3 var12 = new Vec3(-Math.sin(var8) * Math.cos(var10), -Math.sin(var10), Math.cos(var8) * Math.cos(var10));
        Vec3 var13 = new Vec3(-Math.cos(var8), 0.0, -Math.sin(var8));
        Vec3 var14 = var13.cross(var12);
        double var15 = var7.dot(var12);
        if (var15 < 0.3) {
            return null;
        }
        double var17 = var7.dot(var13);
        double var19 = var7.dot(var14);
        double var21 = ((Integer)var0.options.fov().get()).intValue();
        double var23 = Math.tan(Math.toRadians(var21 / 2.0));
        double var25 = var23 * (double)var1 / Math.max(1.0, (double)var2);
        float var27 = (float)((double)var1 / 2.0 + var17 / var15 / var25 * ((double)var1 / 2.0));
        float var28 = (float)((double)var2 / 2.0 - var19 / var15 / var23 * ((double)var2 / 2.0));
        float var29 = (float)((double)var0.player.getBbHeight() * 0.6 / var15 / var23 * ((double)var2 / 2.0));
        float var30 = (float)(Math.max(0.9, (double)var0.player.getBbWidth() * 1.6) / var15 / var25 * ((double)var1 / 2.0));
        return new float[]{var27, var28, var30, var29};
    }

    private static float scaleFor(Font var0, Lyrics.Line var1, Style var2, int var3) {
        float var4 = SkyText.layoutHalfWidth(var0, var1.text(), var2);
        return Math.max(1.2f, Math.min(2.6f, (float)var3 * 0.42f / Math.max(1.0f, var4 * 2.0f)));
    }

    private static float[] box(Shown var0, float var1, float var2, int var3, int var4, Font var5, Style var6) {
        float var7 = CinemaLyricsHud.scaleFor(var5, var0.line, var6, var3);
        float var8 = SkyText.layoutHalfWidth(var5, var0.line.text(), var6) * 2.0f * var7;
        float var9 = SkyText.layoutHalfHeight(var5, var0.line.text(), var6) * 2.0f * var7;
        float var10 = var0.leftAligned ? var1 : var1 - var8;
        return new float[]{var10, var2 - var9 / 2.0f, var10 + var8, var2 + var9 / 2.0f};
    }

    private static boolean overlapsPlayer(Shown var0, float[] var1, int var2, int var3, Font var4, Style var5, LyricsConfig var6) {
        if (var1 == null) {
            return false;
        }
        float[] var7 = CinemaLyricsHud.box(var0, var0.tx * (float)var2, var0.ty * (float)var3, var2, var3, var4, var5);
        return var7[0] < var1[0] + var1[2] && var7[2] > var1[0] - var1[2] && var7[1] < var1[1] + var1[3] && var7[3] > var1[1] - var1[3];
    }

    private static void pickSpot(Shown var0, float[] var1, int var2, int var3, int var4, int var5, Font var6, Style var7, LyricsConfig var8) {
        float var11;
        float var9 = Math.max(12.0f, (float)var2 * 0.05f);
        float var10 = SkyText.layoutHalfHeight(var6, var0.line.text(), var7) * 2.0f * CinemaLyricsHud.scaleFor(var6, var0.line, var7, var2);
        float var12 = (float)var5 - 10.0f - var10 / 2.0f;
        if (var12 < (var11 = (float)var4 + 10.0f + var10 / 2.0f)) {
            var12 = var11 = (float)(var4 + var5) / 2.0f;
        }
        float[][] var13 = new float[][]{{var9, var12, 1.0f}, {(float)var2 - var9, var12, 0.0f}, {var9, var11, 1.0f}, {(float)var2 - var9, var11, 0.0f}, {var9, (var11 + var12) / 2.0f, 1.0f}, {(float)var2 - var9, (var11 + var12) / 2.0f, 0.0f}};
        float var14 = -3.4028235E38f;
        for (float[] var18 : var13) {
            float var20;
            var0.leftAligned = var18[2] > 0.5f;
            float[] var19 = CinemaLyricsHud.box(var0, var18[0], var18[1], var2, var3, var6, var7);
            if (var1 == null) {
                var20 = var18[1] == var12 ? 2.0f : 1.0f;
            } else {
                float var21 = Math.max(0.0f, Math.max(var19[0] - (var1[0] + var1[2]), var1[0] - var1[2] - var19[2]));
                float var22 = Math.max(0.0f, Math.max(var19[1] - (var1[1] + var1[3]), var1[1] - var1[3] - var19[3]));
                boolean var23 = var21 == 0.0f && var22 == 0.0f;
                var20 = (float)Math.sqrt(var21 * var21 + var22 * var22) - (var23 ? 10000.0f : 0.0f) + (var18[1] == var12 ? 25.0f : 0.0f);
            }
            if (Math.abs(var18[0] / (float)var2 - var0.tx) < 0.01f && Math.abs(var18[1] / (float)var3 - var0.ty) < 0.01f) {
                var20 += 15.0f;
            }
            if (!(var20 > var14)) continue;
            var14 = var20;
            var0.tx = var18[0] / (float)var2;
            var0.ty = var18[1] / (float)var3;
        }
        var0.leftAligned = var0.tx < 0.5f;
    }

    private static void draw(GuiGraphicsExtractor var0, Font var1, Shown var2, Style var3, LyricsConfig var4, long var5, long var7, int var9, int var10) {
        float var11 = Math.min(1.0f, (float)(var7 - var2.born) / 1.0E9f / 0.3f);
        if (var2.dying > 0L) {
            var11 *= Math.max(0.0f, 1.0f - (float)(var7 - var2.dying) / 1.0E9f / 0.45f);
        }
        if (!(var11 < 0.02f)) {
            float var12 = CinemaLyricsHud.scaleFor(var1, var2.line, var3, var9);
            SkyText.Layout var13 = SkyText.layout(var1, var2.line.text(), var3);
            float var14 = SkyText.layoutHalfWidth(var1, var2.line.text(), var3) * 2.0f;
            float var15 = SkyText.layoutHalfHeight(var1, var2.line.text(), var3) * 2.0f;
            float var16 = var2.fx * (float)var9;
            float var17 = var2.fy * (float)var10;
            float var18 = var2.leftAligned ? var16 : var16 - var14 * var12;
            int var19 = SkyTone.textColor(SkyLyrics.cfg, 0.6f);
            if ("auto".equalsIgnoreCase(var4.colorMode)) {
                var19 = LyricsConfig.rgb(var4.lightTextColor, 0xFFF8E8);
            }
            int var20 = SkyTone.haloColor(var19);
            boolean var21 = var4.buildMode();
            List<Lyrics.Word> var22 = var2.line.words();
            float var23 = Lyrics.wordPosition(var2.line, var5);
            Matrix3x2fStack var24 = var0.pose();
            var24.pushMatrix();
            var24.translate(var18, var17 - var15 * var12 / 2.0f);
            var24.scale(var12, var12);
            int var25 = 0;
            float var26 = 0.0f;
            for (List<SkyText.Word> var28 : var13.rows()) {
                float var29 = -3.0f;
                for (SkyText.Word var31 : var28) {
                    var29 += (float)var31.width() * var31.scale() + 3.0f;
                }
                float var40 = var2.leftAligned ? 0.0f : var14 - var29;
                for (SkyText.Word var32 : var28) {
                    float var33 = var11;
                    float var34 = 0.0f;
                    if (var22 != null && var25 < var22.size()) {
                        float var35 = var23 - (float)var25;
                        if (var21) {
                            float var36 = Mth.clamp((float)(var35 * 4.0f), (float)0.0f, (float)1.0f);
                            var36 = var36 * var36 * (3.0f - 2.0f * var36);
                            var33 = var11 * var36;
                            var34 = (1.0f - var36) * 3.0f;
                        } else {
                            var33 = var11 * (1.0f - Mth.clamp((float)var35, (float)0.0f, (float)1.0f) * 0.85f);
                        }
                    }
                    if (var33 > 0.02f) {
                        int var42 = Math.max(8, Math.round(var33 * 255.0f));
                        int var44 = var42 << 24 | var19 & 0xFFFFFF;
                        int var37 = var42 << 24 | var20 & 0xFFFFFF;
                        var24.pushMatrix();
                        var24.translate(var40, var26 + (9.0f - 9.0f * var32.scale()) + var34);
                        var24.scale(var32.scale(), var32.scale());
                        if ("outline".equals(var4.textEffect)) {
                            for (int var38 = -1; var38 <= 1; ++var38) {
                                for (int var39 = -1; var39 <= 1; ++var39) {
                                    if (var38 == 0 && var39 == 0) continue;
                                    var0.text(var1, var32.comp(), var38, var39, var37, false);
                                }
                            }
                            var0.text(var1, var32.comp(), 0, 0, var44, false);
                        } else {
                            var0.text(var1, var32.comp(), 1, 1, var44 & 0xFF000000 | var44 >> 2 & 0x3F3F3F, false);
                            var0.text(var1, var32.comp(), 0, 0, var44, false);
                        }
                        var24.popMatrix();
                    }
                    var40 += (float)var32.width() * var32.scale() + 3.0f;
                    ++var25;
                }
                var26 += 11.0f;
            }
            var24.popMatrix();
        }
    }

    private static final class Shown {
        final Lyrics.Line line;
        final long born;
        long dying = -1L;
        float fx;
        float fy;
        float tx;
        float ty;
        boolean leftAligned;

        Shown(Lyrics.Line var1, long var2) {
            this.line = var1;
            this.born = var2;
        }
    }
}

