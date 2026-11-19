/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.Font$DisplayMode
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.core.particles.DustParticleOptions
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.Style
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package net.friendswithmoney.fwm.lyrics;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.friendswithmoney.fwm.lyrics.AfkCinema;
import net.friendswithmoney.fwm.lyrics.BeatSync;
import net.friendswithmoney.fwm.lyrics.CinemaLyrics;
import net.friendswithmoney.fwm.lyrics.FontPack;
import net.friendswithmoney.fwm.lyrics.GlowQuad;
import net.friendswithmoney.fwm.lyrics.Lyrics;
import net.friendswithmoney.fwm.lyrics.LyricsConfig;
import net.friendswithmoney.fwm.lyrics.LyricsFetcher;
import net.friendswithmoney.fwm.lyrics.LyricsTime;
import net.friendswithmoney.fwm.lyrics.MediaWatcher;
import net.friendswithmoney.fwm.lyrics.MusicFx;
import net.friendswithmoney.fwm.lyrics.NowPlayingHud;
import net.friendswithmoney.fwm.lyrics.PvpLyrics;
import net.friendswithmoney.fwm.lyrics.Sink;
import net.friendswithmoney.fwm.lyrics.SkyLyrics;
import net.friendswithmoney.fwm.lyrics.SkyTone;
import net.friendswithmoney.fwm.lyrics.Visibility;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public final class SkyText {
    static final Set<String> SMALL = Set.copyOf(List.of("a", "an", "the", "in", "on", "of", "to", "and", "or", "for", "at", "by", "is", "it", "my", "me", "i'm", "im", "be", "so", "as", "up", "we", "you", "your", "with", "from", "that", "this", "der", "die", "das", "den", "dem", "des", "ein", "eine", "einen", "und", "oder", "am", "zu", "zum", "zur", "mit", "von", "vom", "auf", "f\u00fcr", "ich", "du", "es", "wir", "ihr", "sie", "mein", "dein", "ist", "bin", "wie", "nur", "noch", "mir", "dir", "mich", "dich"));
    static final float PX = 0.13f;
    static final float CINEMA_BOOST = 1.45f;
    private static final List<Placed> placed = new ArrayList<Placed>();
    private static final Random RNG = new Random();
    private static int nextSide = 1;
    private static float openSky = 1.0f;
    private static long openSkyAt;
    private static boolean openSkyLast;
    private static float skyFade;
    private static long lastNanos;
    private static Vec3 lastCam;
    private static double speed;
    private static float curYaw;
    private static float curPitch;
    private static final float STEEP = 55.0f;
    static int lastColor;
    static Vec3 lastCamPos;
    private static String lastTrackKey;
    private static Placed title;
    private static String titleSub;
    private static long titleOutNanos;
    private static boolean[] titleDust;
    private static boolean titleSubDust;
    private static final float TITLE_SCALE = 1.7f;
    private static final float SUB_SCALE = 0.8f;
    private static Vec3 camVel;
    private static final float FADE_OUT = 0.9f;
    private static final float PAUSE_FADE = 0.35f;
    private static final float PASS_FADE = 0.45f;
    private static long lastBeatIdx;
    static float glowAmt;
    static boolean seeThrough;
    static int glowCol;
    private static final IdentityHashMap<Component, Integer> WIDTHS;
    static final float SHADOW_GAP = 0.8f;

    private static boolean openSkyNear(Level w, Vec3 cam) {
        long now = System.nanoTime();
        if (now - openSkyAt < 250000000L) {
            return openSkyLast;
        }
        openSkyAt = now;
        BlockPos head = BlockPos.containing((Position)cam).above();
        boolean open = false;
        for (int dx = -2; dx <= 2 && !open; dx += 2) {
            for (int dz = -2; dz <= 2 && !open; dz += 2) {
                open = w.getHeight(Heightmap.Types.MOTION_BLOCKING, head.getX() + dx, head.getZ() + dz) <= head.getY();
            }
        }
        openSkyLast = open;
        return open;
    }

    static float px(LyricsConfig var0, Placed var1) {
        return 0.13f * var0.skyScale * (var1 != null && var1.cinema ? 1.45f : 1.0f);
    }

    public static Vec3 velocity() {
        return camVel;
    }

    private SkyText() {
    }

    public static void reset() {
        placed.clear();
        title = null;
        lastCam = null;
        speed = 0.0;
        camVel = Vec3.ZERO;
    }

    public static double speed() {
        return speed;
    }

    public static void renderWorld(LevelRenderContext var0) {
        CinemaLyrics.frame();
        Minecraft var1 = Minecraft.getInstance();
        LyricsConfig var2 = SkyLyrics.cfg;
        if (var1.level != null && var1.player != null) {
            boolean var20;
            long var3 = System.nanoTime();
            float var5 = lastNanos == 0L ? 0.0f : Math.min(0.1f, (float)(var3 - lastNanos) / 1.0E9f);
            lastNanos = var3;
            Camera var6 = var1.gameRenderer.mainCamera();
            Vec3 var7 = var6.position();
            SkyText.trackSpeed(var7, var5);
            curYaw = var6.yRot();
            curPitch = var6.xRot();
            boolean var8 = PvpLyrics.active(var2);
            skyFade += ((var8 ? 0.0f : 1.0f) - skyFade) * Math.min(1.0f, var5 * 6.0f);
            PoseStack var9 = var0.poseStack() != null ? var0.poseStack() : new PoseStack();
            Sink var10 = new Sink(var0.submitNodeCollector());
            Style var12 = SkyText.style(var2);
            float var13 = SkyTone.luminance(var1.level, 1.0f);
            int var14 = SkyTone.textColor(var2, var13);
            int var15 = SkyTone.haloColor(var14);
            glowAmt = (float)var2.glow / 100.0f;
            int var16 = LyricsConfig.rgb(var2.color2, 16020150);
            int var17 = var14 >> 16 & 0xFF;
            int var18 = var14 >> 8 & 0xFF;
            int var19 = var14 & 0xFF;
            boolean bl = var20 = (0.299f * (float)var17 + 0.587f * (float)var18 + 0.114f * (float)var19) / 255.0f > 0.85f;
            glowCol = var2.gradient ? var16 : (var20 ? LyricsConfig.rgb(var2.accentColor, 10980346) : SkyText.mix(var14, 0xFFFFFF, 0.5f));
            long var28 = LyricsTime.songMs();
            lastColor = var14;
            lastCamPos = var7;
            float var29 = BeatSync.pulse(var3);
            var19 = var29 > 0.01f ? SkyText.mix(var14, 0xFFFFFF, 0.3f * var29) : var14;
            SkyText.checkSongChange(var2, var6, var3, var8);
            SkyText.updateLines(var2, var6, var3, var8);
            if (!placed.isEmpty()) {
                float var21;
                SkyText.moveLines(var2, var7, var6, var5, var3);
                float var31 = 1.0f;
                if (CinemaLyrics.openSkyOnly(var2)) {
                    float f = var31 = SkyText.openSkyNear((Level)var1.level, var7) ? 1.0f : 0.0f;
                }
                if ((var21 = (openSky += (var31 - openSky) * Math.min(1.0f, var5 * 4.0f)) * skyFade) >= 0.01f) {
                    for (Placed var23 : placed) {
                        float var35;
                        float var24 = var21;
                        if (var23.fastFadeNanos > 0L) {
                            var24 = var21 * Math.max(0.0f, 1.0f - (float)(var3 - var23.fastFadeNanos) / 1.0E9f / 0.25f);
                        }
                        if (!(var23.dyingNanos <= 0L || var23.flying || var23.ground && !var23.groundTimeFade)) {
                            float var25 = (float)(var3 - var23.dyingNanos) / 1.0E9f;
                            float var26 = MediaWatcher.isPlaying() ? 0.9f : 0.35f;
                            var24 *= Math.max(0.0f, 1.0f - var25 / var26);
                        }
                        if (var23.flying && var23.passed) {
                            var24 *= Math.max(0.0f, 1.0f - (float)(var3 - var23.passedNanos) / 1.0E9f / 0.45f);
                        }
                        if (var23.dyingNanos > 0L) {
                            var23.dieCap = Math.min(var23.dieCap, var23.nearFade);
                            var24 *= var23.dieCap;
                        } else {
                            var24 *= var23.nearFade;
                        }
                        var24 *= var23.blockFade;
                        if ((var24 *= var23.relocAlpha) < 0.02f) continue;
                        float var34 = (float)(var3 - var23.bornNanos) / 1.0E9f;
                        var23.beatPulse = var35 = var23.dyingNanos <= 0L && var23.fastFadeNanos <= 0L ? var29 : 0.0f;
                        float var27 = SkyText.px(var2, var23) * var23.sizeMul;
                        var9.pushPose();
                        var9.translate(var23.anchor.x - var7.x, var23.anchor.y - var7.y, var23.anchor.z - var7.z);
                        var9.mulPose((Quaternionfc)new Quaternionf().rotationY(var23.yawRad));
                        var9.mulPose((Quaternionfc)new Quaternionf().rotationX(var23.pitchRad));
                        var9.mulPose((Quaternionfc)new Quaternionf().rotationZ((float)Math.toRadians(var23.tiltDeg)));
                        var9.scale(var27, -var27, var27);
                        SkyText.drawBlock(var1.font, var23, var9.last().pose(), var10, var14, var15, var12, var24, var34, var28, var2.buildMode(), var2, var27, var3);
                        var9.popPose();
                    }
                }
            }
            if (title != null) {
                SkyText.renderTitle(var1, var6, var9, var10, var2, var12, titleOutNanos == 0L ? var19 : var14, var15, var3, var29, var28);
            }
            SkyText.beatFrame(var2, var3, var28);
            if (var8 || PvpLyrics.fading()) {
                PvpLyrics.render(var1, var6, var9, var10, var2, var28, var3, var5);
            }
            GlowQuad.flush(var10);
        } else {
            SkyText.reset();
        }
    }

    private static void trackSpeed(Vec3 var0, float var1) {
        if (lastCam != null && var1 > 0.0f) {
            double var2 = var0.distanceTo(lastCam);
            double var4 = var2 > 50.0 ? 0.0 : var2 / (double)var1;
            speed += (var4 - speed) * Math.min(1.0, (double)var1 * 10.0);
            Vec3 var6 = var2 > 50.0 ? Vec3.ZERO : var0.subtract(lastCam).scale(1.0 / (double)var1);
            camVel = camVel.add(var6.subtract(camVel).scale(Math.min(1.0, (double)var1 * 12.0)));
        }
        lastCam = var0;
    }

    public static Style style(LyricsConfig var0) {
        return FontPack.style(var0);
    }

    private static void updateLines(LyricsConfig var0, Camera var1, long var2, boolean var4) {
        Iterator var9;
        boolean var8;
        ArrayList<Lyrics.Line> var5 = new ArrayList<Lyrics.Line>(2);
        MediaWatcher.Track var6 = MediaWatcher.track();
        boolean var7 = MediaWatcher.isPlaying();
        boolean bl = var8 = var0.enabled && var0.skyEnabled && var7 && var6 != null && !var4 && (!var0.skyOnlyCinema || CinemaLyrics.cinema());
        if (var8 && (var9 = LyricsFetcher.get(var6)) != null && ((Lyrics)((Object)var9)).synced && !((Lyrics)((Object)var9)).lines.isEmpty()) {
            Lyrics.Line var14;
            long var10 = LyricsTime.songMs();
            int var12 = ((Lyrics)((Object)var9)).indexAt(var10);
            Lyrics.Line var13 = null;
            if (var12 >= 0 && var10 < ((Lyrics)((Object)var9)).endOf(var12, var6.durationMs()) && !((Lyrics)((Object)var9)).lines.get(var12).text().isBlank()) {
                var13 = ((Lyrics)((Object)var9)).lines.get(var12);
                var5.add(var13);
            }
            if (!var0.buildMode() && var12 + 1 < ((Lyrics)((Object)var9)).lines.size() && !(var14 = ((Lyrics)((Object)var9)).lines.get(var12 + 1)).text().isBlank() && SkyText.preShow(var13, var14, var10)) {
                var5.add(var14);
            }
        }
        for (Placed var18 : placed) {
            if (var18.dyingNanos >= 0L || var5.contains(var18.line)) continue;
            var18.dyingNanos = var2;
            if (var18.ground && speed < 1.5) {
                var18.groundTimeFade = true;
            }
            if (var18.flying && var7 && var0.enabled && var0.skyEnabled && !var4) continue;
            var18.flying = false;
        }
        for (Lyrics.Line var19 : var5) {
            boolean var11 = false;
            for (Placed var25 : placed) {
                if (var25.line == var19 && var25.dyingNanos < 0L) {
                    var11 = true;
                    break;
                }
                if (var25.line != var19 || var25.dyingNanos <= 0L || var2 - var25.dyingNanos >= 1500000000L) continue;
                var11 = true;
                break;
            }
            if (var11) continue;
            Placed var23 = SkyText.place(var19, var1, var0, var2);
            SkyText.clearOverlaps(var23, var1.position());
            placed.add(var23);
        }
        Iterator<Placed> var17 = placed.iterator();
        while (var17.hasNext()) {
            boolean var24;
            Placed var20 = var17.next();
            boolean var21 = var20.flying ? var20.passed && (float)(var2 - var20.passedNanos) / 1.0E9f > 0.45f || (float)(var2 - var20.dyingNanos) / 1.0E9f > 4.0f : (var20.ground && !var20.groundTimeFade ? var20.nearFade < 0.02f && (float)(var2 - var20.bornNanos) / 1.0E9f > 1.0f || (float)(var2 - var20.dyingNanos) / 1.0E9f > 12.0f : (float)(var2 - var20.dyingNanos) / 1.0E9f > 0.9f);
            boolean bl2 = var24 = var20.fastFadeNanos > 0L && (float)(var2 - var20.fastFadeNanos) / 1.0E9f > 0.3f;
            if ((var20.dyingNanos <= 0L || !var21) && !var24) continue;
            var17.remove();
        }
        while (placed.size() > 6) {
            placed.remove(0);
        }
    }

    private static void pickRoute(Minecraft mc, Placed p, Vec3 cam, Vec3 fwd, Vec3 right, double halfW, double y) {
        int pref = p.passSide != 0 ? p.passSide : 1;
        for (double lift = 0.0; lift <= 6.0; lift += 2.0) {
            for (int side : new int[]{pref, -pref}) {
                Vec3 t = cam.add(right.scale((double)side * (halfW + 1.8))).subtract(fwd.scale(3.0));
                t = new Vec3(t.x, y + lift, t.z);
                if (!Visibility.clear((Level)mc.level, p.anchor, t, (Entity)mc.player)) continue;
                p.routeSide = side;
                p.lift = lift;
                return;
            }
        }
        p.routeSide = 0;
        p.lift = 0.0;
    }

    private static void guardBlocks(Minecraft mc, Placed p, Vec3 cam, LyricsConfig cfg, float dt, long now) {
        if (mc.level == null || p.anchor == null) {
            return;
        }
        if (now - p.blockCheckNanos > 100000000L) {
            double hh;
            p.blockCheckNanos = now;
            double s = SkyText.px(cfg, p) * p.sizeMul;
            double hw = (double)p.halfWpx * s;
            p.inBlock = Visibility.intersects((Level)mc.level, cam, p.anchor, hw, hh = (double)p.halfHpx * s) || Visibility.blocked((Level)mc.level, cam, p.anchor, hw, hh, (Entity)mc.player) >= 3;
        }
        float want = p.inBlock ? 0.0f : 1.0f;
        p.blockFade = p.blockFade + (want - p.blockFade) * Math.min(1.0f, dt * (want < p.blockFade ? 10.0f : 3.0f));
    }

    static Vec3 right(float var0) {
        double var1 = Math.toRadians(var0);
        return new Vec3(-Math.cos(var1), 0.0, -Math.sin(var1));
    }

    static Vec3 forward(float var0) {
        double var1 = Math.toRadians(var0);
        return new Vec3(-Math.sin(var1), 0.0, Math.cos(var1));
    }

    private static void moveLines(LyricsConfig var0, Vec3 var1, Camera var2, float var3, long var4) {
        Minecraft var6 = Minecraft.getInstance();
        boolean var7 = var0.flybyMode() && MediaWatcher.isPlaying();
        long var8 = LyricsTime.songMs();
        Vec3 var10 = SkyText.forward(var2.yRot());
        Vec3 var11 = SkyText.right(var2.yRot());
        for (Placed var13 : placed) {
            if (var13.flying && var7) {
                Vec3 var43 = var13.anchor.subtract(var1);
                double var44 = var43.length();
                if (!var13.passed) {
                    boolean var47;
                    if (!var13.locked) {
                        double var46 = SkyText.px(var0, var13);
                        double var49 = Math.min((double)var13.baseSizeMul, 1.5 / Math.max(0.5, (double)var13.halfWpx * var46));
                        var13.sizeMul = (float)Math.max(var49, Math.min((double)var13.baseSizeMul, var44 / (double)var0.skyDistance));
                        double var51 = (double)var13.halfWpx * var46 * (double)var13.sizeMul;
                        double var56 = var43.dot(var11);
                        if (Math.abs(var56) > 0.4) {
                            var13.passSide = var56 > 0.0 ? 1 : -1;
                        }
                        double var25 = var6.player != null ? var6.player.getPosition((float)1.0f).y : var1.y - 1.62;
                        double var27 = (double)var13.halfHpx * var46 * (double)var13.sizeMul;
                        if (var6.level != null && var4 - var13.routeNanos > 250000000L) {
                            var13.routeNanos = var4;
                            SkyText.pickRoute(var6, var13, var1, var10, var11, var51, var25 + var27 + 0.3);
                        }
                        int side = var13.routeSide != 0 ? var13.routeSide : var13.passSide;
                        Vec3 var29 = var1.add(var11.scale((double)side * (var51 + 1.8))).subtract(var10.scale(3.0));
                        var29 = new Vec3(var29.x, var25 + var27 + 0.3 + var13.lift, var29.z);
                        Vec3 var59 = var29.subtract(var13.anchor);
                        double var31 = Math.max(0.001, var59.length());
                        var13.dirX = var59.x / var31;
                        var13.dirY = var59.y / var31;
                        var13.dirZ = var59.z / var31;
                        double var33 = (double)(var13.arriveMs - var8) / 1000.0;
                        if (var13.dyingNanos > 0L) {
                            var33 = Math.min(var33, 0.45);
                        }
                        var33 = Math.max(0.35, var33);
                        double var35 = var43.lengthSqr() > 1.0E-4 ? -camVel.dot(var43.normalize()) : 0.0;
                        double var61 = var31 / var33 - Math.max(0.0, var35);
                        var13.vel += (var61 - var13.vel) * Math.min(1.0, (double)var3 * 4.0);
                        var13.vel = Math.max(var13.vel, 1.0);
                        SkyText.face(var13, var1);
                        if (var44 < 18.0) {
                            var13.locked = true;
                        }
                    }
                    var13.nearFade = Math.min(var13.nearFade, (float)Math.max(0.0, Math.min(1.0, (var44 - 8.0) / 10.0)));
                    boolean bl = var47 = var43.dot(var10) < 0.0;
                    if (var13.nearFade <= 0.01f || var47) {
                        var13.passed = true;
                        var13.passedNanos = var4;
                    }
                }
                double var48 = var13.vel * (double)var3;
                var13.anchor = var13.anchor.add(var13.dirX * var48, var13.dirY * var48, var13.dirZ * var48);
                SkyText.guardBlocks(var6, var13, var1, var0, var3, var4);
                continue;
            }
            if (var13.flying) continue;
            if (var13.air) {
                SkyText.moveAir(var13, var2, var0, var3, var4, var8);
                SkyText.guardBlocks(var6, var13, var1, var0, var3, var4);
                continue;
            }
            if (!var13.cinema && !var13.ground) {
                var13.carry += (SkyText.carryTarget() - var13.carry) * Math.min(1.0f, var3 * 3.0f);
                if (var13.carry > 0.01f) {
                    Vec3 var14 = camVel.scale((double)(var13.carry * var3));
                    var13.anchor = var13.anchor.add(var14);
                    if (var13.target != null) {
                        var13.target = var13.target.add(var14);
                    }
                }
            }
            if (var13.cinema) {
                var13.nearFade += (1.0f - var13.nearFade) * Math.min(1.0f, var3 * 3.0f);
            } else {
                double var21;
                Vec3 var40 = var13.anchor.subtract(var1);
                double var15 = var40.length();
                double var17 = var15 > 1.0E-4 ? var40.dot(var10) / var15 : 1.0;
                double var19 = var13.steep ? 1.0 : Math.max(0.0, Math.min(1.0, (var15 - 5.0) / 7.0));
                double d = var21 = var13.steep ? 1.0 : Math.max(0.0, Math.min(1.0, (var17 + 0.2) / 0.5));
                if (var13.ground) {
                    Vec3 var23 = new Vec3(var40.x, 0.0, var40.z);
                    double var24 = var23.dot(var13.pathDir);
                    var21 = Math.max(0.0, Math.min(1.0, (var24 - 3.0) / 4.0));
                    var19 = 1.0;
                }
                float var54 = (float)Math.min(var19, var21);
                var13.nearFade = var13.nearFade + (var54 - var13.nearFade) * Math.min(1.0f, var3 * (var54 < var13.nearFade ? 6.0f : 2.0f));
            }
            if (var13.relocPhase == 1) {
                var13.relocAlpha = Math.max(0.0f, var13.relocAlpha - var3 / 0.25f);
                if (var13.relocAlpha <= 0.0f) {
                    var13.anchor = var13.target;
                    var13.sizeMul = var13.targetSizeMul;
                    if (!var13.ground) {
                        SkyText.face(var13, var1);
                    }
                    var13.relocPhase = 2;
                }
            } else if (var13.relocPhase == 2) {
                var13.relocAlpha = Math.min(1.0f, var13.relocAlpha + var3 / 0.35f);
                if (var13.relocAlpha >= 1.0f) {
                    var13.relocPhase = 0;
                }
            }
            if (var0.keepVisible && !var13.ground && !var13.cinema && var6.level != null && var13.dyingNanos < 0L && var13.relocPhase == 0 && var4 - var13.lastCheckNanos > 300000000L) {
                var13.lastCheckNanos = var4;
                double var41 = SkyText.px(var0, var13);
                double var16 = (double)var13.halfWpx * var41 * (double)var13.sizeMul;
                double var18 = (double)var13.halfHpx * var41 * (double)var13.sizeMul;
                int var20 = Visibility.blocked((Level)var6.level, var1, var13.anchor, var16, var18, (Entity)var6.player);
                boolean var50 = Visibility.intersects((Level)var6.level, var1, var13.anchor, var16, var18);
                int n = var13.insideHits = var50 ? var13.insideHits + 1 : 0;
                if (var13.insideHits >= 3 && var4 - var13.lastRelocNanos > 2500000000L) {
                    var13.insideHits = 0;
                    Visibility.Spot var52 = SkyText.findSpot(var13, var2, var0);
                    if (var52.clear() && var52.anchor().distanceTo(var13.anchor) > 0.5) {
                        var13.target = var52.anchor();
                        var13.targetSizeMul = (float)(var52.distance() / (double)var0.skyDistance);
                        var13.relocPhase = 1;
                        var13.lastRelocNanos = var4;
                    }
                    var13.occludedSince = 0L;
                } else if (var20 >= 2) {
                    if (var13.occludedSince == 0L) {
                        var13.occludedSince = var4;
                    } else if (var4 - var13.occludedSince > 350000000L && var4 - var13.lastRelocNanos > 1500000000L) {
                        Visibility.Spot var22 = SkyText.findSpot(var13, var2, var0);
                        if (var22.clear() && var22.anchor().distanceTo(var13.anchor) > 1.0) {
                            var13.target = var22.anchor();
                            var13.targetSizeMul = (float)(var22.distance() / (double)var0.skyDistance);
                            var13.relocPhase = 1;
                            var13.lastRelocNanos = var4;
                        }
                        var13.occludedSince = 0L;
                    }
                } else {
                    var13.occludedSince = 0L;
                }
                if (!var13.steep && var13.relocPhase == 0) {
                    Visibility.Frame var53 = SkyText.viewFrame(var6, var2, false);
                    Vec3 var55 = var13.anchor.subtract(var1);
                    double var57 = Math.sqrt(var55.x * var55.x + var55.z * var55.z);
                    double var26 = Math.toDegrees(Math.atan2(-var55.x, var55.z));
                    double var28 = Math.toDegrees(Math.atan2(var55.y, Math.max(0.01, var57)));
                    double var30 = Math.max(1.0, var55.length());
                    double var32 = Math.toDegrees(Math.atan2(var16, var30)) * 0.6;
                    double var34 = Math.toDegrees(Math.atan2(var18, var30)) * 0.6;
                    boolean var36 = var53.contains(var26, var28, var32, var34);
                    long var37 = var13.endMs - LyricsTime.songMs();
                    if (var36) {
                        var13.outSince = 0L;
                    } else if (var13.outSince == 0L) {
                        var13.outSince = var4;
                    } else if (var4 - var13.outSince > 1200000000L && var37 > 1500L && var4 - var13.lastRelocNanos > 1200000000L) {
                        Visibility.Spot var39 = SkyText.findSpot(var13, var2, var0);
                        if (var39.clear()) {
                            var13.target = var39.anchor();
                            var13.targetSizeMul = (float)(var39.distance() / (double)var0.skyDistance);
                            var13.relocPhase = 1;
                            var13.lastRelocNanos = var4;
                        }
                        var13.outSince = 0L;
                    }
                }
            }
            if (var13.ground) continue;
            double var42 = var13.anchor.x - var1.x;
            double var45 = var13.anchor.z - var1.z;
            if (!var13.cinema && !var13.steep) continue;
            SkyText.face(var13, var1);
        }
    }

    private static Visibility.Spot groundSpot(Placed var0, Camera var1, LyricsConfig var2) {
        double var21;
        Vec3 var10;
        Minecraft var3 = Minecraft.getInstance();
        Vec3 var4 = var1.position();
        double var5 = var3.player != null ? var3.player.getPosition((float)1.0f).y : var4.y - 1.62;
        Vec3 var7 = new Vec3(SkyText.camVel.x, 0.0, SkyText.camVel.z);
        double var8 = var7.length();
        var0.pathDir = var10 = var8 > 1.5 ? var7.scale(1.0 / var8) : SkyText.forward(var1.yRot());
        Vec3 var11 = new Vec3(-var10.z, 0.0, var10.x);
        List<Lyrics.Word> var12 = var0.line.words();
        long var13 = var12 != null && !var12.isEmpty() ? var12.get(var12.size() - 1).endMs() : var0.line.startMs() + 3000L;
        double var15 = Math.max(1.5, (double)(var13 - var0.line.startMs()) / 1000.0);
        double var17 = Math.max(9.0, Math.min(60.0, 6.0 + var8 * var15 * 0.85));
        double var19 = 0.13f * var2.skyScale;
        var0.groundSizeMul = var21 = Math.min(1.0, 2.5 / Math.max(0.5, (double)var0.halfWpx * var19));
        double var23 = (double)var0.halfWpx * var19 * var21;
        double var25 = (double)var0.halfHpx * var19 * var21;
        double var27 = var23 + 2.0;
        int[] var29 = new int[]{var0.side, -var0.side};
        double[] var30 = new double[]{1.0, 1.25, 0.8, 1.5};
        double[] var31 = new double[]{1.0, 1.6};
        for (int var35 : var29) {
            for (double var39 : var30) {
                for (double var44 : var31) {
                    Vec3 var48;
                    Vec3 var46 = var4.add(var10.scale(var17 * var39)).add(var11.scale((double)var35 * var27 * var44));
                    int var47 = var3.level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int)Math.floor(var46.x), (int)Math.floor(var46.z));
                    if (var47 <= var3.level.getMinY() + 1 || (double)var47 < var5 - 10.0 || (double)var47 > var5 + 6.0 || SkyText.overlapsOthers(var0, var4, var48 = new Vec3(var46.x, (double)var47 + var25 + 0.2, var46.z), var23, var25) || !Visibility.visible((Level)var3.level, var4, var48, var23, var25 * 0.8, (Entity)var3.player, false)) continue;
                    return new Visibility.Spot(var48, var4.distanceTo(var48), true);
                }
            }
        }
        Vec3 var49 = var4.add(var10.scale(var17)).add(var11.scale((double)var0.side * var27));
        var49 = new Vec3(var49.x, var5 + var25 + 0.2, var49.z);
        return new Visibility.Spot(var49, var4.distanceTo(var49), false);
    }

    private static boolean overlapAngles(double[] var0, double var1, double var3, double[] var5, double var6, double var8) {
        double var10 = var3 + var1 * 0.27;
        double var12 = var8 + var6 * 0.27;
        return Math.abs(Mth.wrapDegrees((double)(var0[0] - var5[0]))) < var1 + var6 + 2.5 && Math.abs(var0[1] - var5[1]) < var10 + var12 + 2.0;
    }

    private static void clearOverlaps(Placed var0, Vec3 var1) {
        LyricsConfig var2 = SkyLyrics.cfg;
        double[] var3 = new double[]{var1.x, var1.y, var1.z};
        double var4 = var0.halfWpx * SkyText.px(var2, var0) * var0.sizeMul;
        double var6 = var0.halfHpx * SkyText.px(var2, var0) * var0.sizeMul;
        double[] var8 = SkyText.angles(var3, var0.anchor);
        double var9 = Math.toDegrees(Math.atan2(var4, var8[2]));
        double var11 = Math.toDegrees(Math.atan2(var6, var8[2]));
        long var13 = System.nanoTime();
        for (Placed var16 : placed) {
            if (var16 == var0 || var16.anchor == null || var16.fastFadeNanos > 0L) continue;
            double var17 = var16.halfWpx * SkyText.px(var2, var16) * var16.sizeMul;
            double var19 = var16.halfHpx * SkyText.px(var2, var16) * var16.sizeMul;
            double[] var21 = SkyText.angles(var3, var16.anchor);
            if (!SkyText.overlapAngles(var8, var9, var11, var21, Math.toDegrees(Math.atan2(var17, var21[2])), Math.toDegrees(Math.atan2(var19, var21[2]))) || var16.dyingNanos <= 0L) continue;
            var16.fastFadeNanos = var13;
        }
    }

    private static boolean overlapsOthers(Placed var0, Vec3 var1, Vec3 var2, double var3, double var5) {
        LyricsConfig var7 = SkyLyrics.cfg;
        double[] var8 = new double[]{var1.x, var1.y, var1.z};
        double[] var9 = SkyText.angles(var8, var2);
        double var10 = Math.toDegrees(Math.atan2(var3, var9[2]));
        double var12 = Math.toDegrees(Math.atan2(var5, var9[2]));
        for (Placed var15 : placed) {
            double var23;
            double var21;
            if (var15 == var0 || var15.anchor == null || var15.nearFade < 0.05f || var15.passed || var15.fastFadeNanos > 0L) continue;
            double var16 = var15.halfWpx * SkyText.px(var7, var15) * var15.sizeMul;
            double var18 = var15.halfHpx * SkyText.px(var7, var15) * var15.sizeMul;
            double[] var20 = SkyText.angles(var8, var15.anchor);
            if (!SkyText.overlapAngles(var9, var10, var12, var20, var21 = Math.toDegrees(Math.atan2(var16, var20[2])), var23 = Math.toDegrees(Math.atan2(var18, var20[2])))) continue;
            return true;
        }
        return false;
    }

    private static void rememberRelative(Placed var0, Camera var1) {
        Vec3 var2 = var0.anchor.subtract(var1.position());
        double var3 = var2.length();
        if (!(var3 < 0.001)) {
            double var5 = Math.toDegrees(Math.atan2(-var2.x, var2.z));
            double var7 = Math.toDegrees(Math.asin(Math.max(-1.0, Math.min(1.0, var2.y / var3))));
            var0.relYaw = Mth.wrapDegrees((double)(var5 - (double)var1.yRot()));
            var0.relPitch = var7 - (double)(-var1.xRot());
            var0.relDist = var3;
        }
    }

    static boolean elytraMode() {
        Minecraft var0 = Minecraft.getInstance();
        if (var0.player == null) {
            return false;
        }
        return var0.player.isFallFlying() ? true : !var0.player.onGround() && SkyText.flightVel().length() > 14.0;
    }

    static Vec3 flightVel() {
        Minecraft var0 = Minecraft.getInstance();
        return var0.player == null ? Vec3.ZERO : var0.player.getDeltaMovement().scale(20.0);
    }

    private static Vec3 flightEye() {
        Minecraft var0 = Minecraft.getInstance();
        return var0.player.getPosition(1.0f).add(0.0, (double)var0.player.getBbHeight() * 0.6, 0.0);
    }

    private static long lineEnd(Lyrics.Line var0) {
        int var3;
        Lyrics var2;
        MediaWatcher.Track var1 = MediaWatcher.track();
        Lyrics lyrics = var2 = var1 != null ? LyricsFetcher.peek(var1) : null;
        if (var2 != null && (var3 = var2.lines.indexOf(var0)) >= 0 && var3 + 1 < var2.lines.size()) {
            return Math.min(var2.lines.get(var3 + 1).startMs(), var0.startMs() + 9000L);
        }
        return var0.startMs() + 4500L;
    }

    private static void airPlace(Placed var0, Camera var1, LyricsConfig var2) {
        Minecraft var3 = Minecraft.getInstance();
        Vec3 var4 = SkyText.flightVel();
        double var5 = Math.max(8.0, var4.length());
        Vec3 var7 = var4.lengthSqr() > 1.0 ? var4.normalize() : Vec3.directionFromRotation((float)var1.xRot(), (float)var1.yRot());
        Vec3 var8 = SkyText.flightEye();
        long var9 = LyricsTime.songMs();
        double var11 = Math.max(1.2, Math.min(9.0, (double)(var0.endMs - var9) / 1000.0));
        double var13 = Math.max(60.0, Math.min(260.0, (double)var3.options.getEffectiveRenderDistance() * 16.0 - 16.0));
        double var15 = Math.max(25.0, Math.min(var13, var5 * var11 + 6.0));
        var0.sizeMul = (float)Math.max(1.0, Math.min(3.0, 0.8 + 0.35 * var15 / (double)var2.skyDistance));
        double var17 = 0.13f * var2.skyScale * var0.sizeMul;
        Vec3 var19 = new Vec3(0.0, 1.0, 0.0);
        Vec3 var20 = var7.cross(var19);
        var20 = var20.lengthSqr() < 1.0E-4 ? SkyText.right(var1.yRot()) : var20.normalize();
        Vec3 var21 = var20.cross(var7).normalize();
        double var22 = (double)var0.halfWpx * var17 + 3.0;
        double var24 = (double)var0.halfHpx * var17 + 1.5;
        Vec3 var26 = var8.add(var7.scale(var15)).add(var20.scale((double)var0.side * var22)).add(var21.scale(var24));
        Vec3 var27 = var1.position();
        for (int var28 = 0; var28 < 4 && var3.level != null && !Visibility.clear((Level)var3.level, var27, var26, (Entity)var3.player); ++var28) {
            var26 = var26.add(0.0, 6.0, 0.0);
        }
        var0.anchor = var26;
        var0.target = var26;
        var0.airMove = var5 * 0.5;
        SkyText.face(var0, var26.subtract(var7.scale(30.0)));
    }

    private static void moveAir(Placed var0, Camera var1, LyricsConfig var2, float var3, long var4, long var6) {
        Minecraft var8 = Minecraft.getInstance();
        if (var8.player != null) {
            boolean var27;
            Vec3 var9 = SkyText.flightVel();
            double var10 = var9.length();
            Vec3 var12 = SkyText.flightEye();
            Vec3 var13 = var0.anchor.subtract(var12);
            if (var10 > 3.0) {
                Vec3 var14 = var9.scale(1.0 / var10);
                var0.airDir = var0.airDir == null ? var14 : var0.airDir.scale(1.0 - Math.min(1.0, (double)var3 * 3.0)).add(var14.scale(Math.min(1.0, (double)var3 * 3.0))).normalize();
            } else if (var0.airDir == null) {
                var0.airDir = var13.normalize();
            }
            Vec3 var29 = var0.airDir;
            double var15 = var13.dot(var29);
            double var17 = (double)(var0.endMs - var6) / 1000.0;
            double var19 = 0.0;
            if (var0.dyingNanos < 0L && var17 > 0.08 && var10 > 3.0 && var15 > 0.0) {
                double var21 = (var15 - 5.0) / var17;
                var19 = Math.max(-0.6 * var10, Math.min(0.97 * var10, var10 - var21));
            }
            var0.airMove += (var19 - var0.airMove) * Math.min(1.0, (double)var3 * 4.0);
            if (Math.abs(var0.airMove) > 0.01) {
                Vec3 var30 = var29.scale(var0.airMove * (double)var3);
                var0.anchor = var0.anchor.add(var30);
                if (var0.target != null) {
                    var0.target = var0.target.add(var30);
                }
            }
            float var31 = (float)Math.max(0.0, Math.min(1.0, (var15 - 4.0) / 8.0));
            Vec3 var22 = var1.position();
            Vec3 var23 = Vec3.directionFromRotation((float)var1.xRot(), (float)var1.yRot());
            Vec3 var24 = var0.anchor.subtract(var22);
            double var25 = Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, var24.normalize().dot(var23)))));
            boolean bl = var27 = var25 > 55.0;
            if (!var27 || var0.dyingNanos >= 0L || !(var15 > 6.0)) {
                var0.airOutSince = 0L;
            } else if (var0.airOutSince == 0L) {
                var0.airOutSince = var4;
            } else if (var4 - var0.airOutSince > 700000000L && var0.relocPhase == 0 && var17 > 1.5) {
                Vec3 var28 = var0.anchor;
                SkyText.airPlace(var0, var1, var2);
                var0.target = var0.anchor;
                var0.anchor = var28;
                var0.relocPhase = 1;
                var0.airOutSince = 0L;
            }
            if (var0.relocPhase == 1) {
                var0.relocAlpha = Math.max(0.0f, var0.relocAlpha - var3 / 0.25f);
                if (var0.relocAlpha <= 0.0f) {
                    var0.anchor = var0.target;
                    var0.relocPhase = 2;
                }
            } else if (var0.relocPhase == 2) {
                var0.relocAlpha = Math.min(1.0f, var0.relocAlpha + var3 / 0.35f);
                if (var0.relocAlpha >= 1.0f) {
                    var0.relocPhase = 0;
                }
            }
            if (var15 < 4.0) {
                var0.airPassed = true;
            }
            float var32 = var0.airPassed ? 0.0f : var31;
            var0.nearFade = var0.nearFade + (var32 - var0.nearFade) * Math.min(1.0f, var3 * (var32 < var0.nearFade ? 8.0f : 3.0f));
        }
    }

    static float carryTarget() {
        Minecraft var0 = Minecraft.getInstance();
        if (SkyText.elytraMode()) {
            return 0.0f;
        }
        double var1 = camVel.length();
        boolean var3 = var0.player != null && var0.player.isFallFlying();
        return !var3 && var1 < 14.0 ? 0.0f : (float)(Math.max(0.0, Math.min(1.0, (var1 - 6.0) / 12.0)) * 0.92);
    }

    static void faceSmooth(Placed var0, Vec3 var1, float var2) {
        float var3 = var0.yawRad;
        float var4 = var0.pitchRad;
        SkyText.face(var0, var1);
        float var5 = var0.yawRad;
        float var6 = Math.max((float)Math.toRadians(-30.0), Math.min((float)Math.toRadians(30.0), var0.pitchRad));
        float var7 = (float)Math.toRadians(25.0) * var2;
        float var8 = (float)Math.atan2(Math.sin(var5 - var3), Math.cos(var5 - var3));
        var0.yawRad = var3 + Math.max(-var7, Math.min(var7, var8));
        var0.pitchRad = var4 + Math.max(-var7, Math.min(var7, var6 - var4));
    }

    static void face(Placed var0, Vec3 var1) {
        if (var0.steep && curPitch > 45.0f) {
            var0.yawRad = (float)Math.toRadians(180.0 - (double)curYaw);
            var0.pitchRad = (float)Math.toRadians(-curPitch);
        } else {
            double var2 = var1.x - var0.anchor.x;
            double var4 = var1.z - var0.anchor.z;
            var0.yawRad = (float)Math.atan2(var2, var4);
            double var6 = Math.sqrt(var2 * var2 + var4 * var4);
            var0.pitchRad = (float)Math.atan2(var0.anchor.y - var1.y, Math.max(0.5, var6));
        }
    }

    private static boolean preShow(Lyrics.Line var0, Lyrics.Line var1, long var2) {
        if (var2 >= var1.startMs() - 1500L) {
            return true;
        }
        if (var0 == null) {
            return var2 >= var1.startMs() - 2500L;
        }
        int var4 = var0.words() == null ? 0 : var0.words().size();
        return var4 > 0 && Lyrics.wordPosition(var0, var2) >= (float)(var4 - 2);
    }

    private static float rand(float var0, float var1) {
        return var0 + RNG.nextFloat() * (var1 - var0);
    }

    private static Visibility.Spot findSpot(Placed var0, Camera var1, LyricsConfig var2) {
        Minecraft var3 = Minecraft.getInstance();
        Vec3 var4 = var1.position();
        double var5 = SkyText.px(var2, var0);
        boolean bl = var0.steep = var1.xRot() > 55.0f;
        if (var0.steep) {
            return SkyText.steepSpot(var0, var1, var2, var0.cinema ? SkyText.cinemaFrame(var3, var1) : SkyText.viewFrame(var3, var1, false));
        }
        if (var0.cinema) {
            double var25 = -var1.xRot();
            double var26 = var25 + Math.max(4.0, Math.min(16.0, var0.pitchUp * 0.5));
            if (var26 < 4.0 && var25 + 25.0 >= 4.0) {
                var26 = 4.0;
            }
            double var30 = 6.0;
            if (var3.player != null) {
                Vec3 var32 = var3.player.getPosition(1.0f);
                Vec3 var14 = new Vec3(var32.x, var32.y + (double)var3.player.getBbHeight() * 0.85, var32.z);
                var30 = Math.max(var30, var4.distanceTo(var14) + 4.0);
            }
            double var33 = Math.max(var30, Math.max(8.0, var0.wantDist));
            double var35 = var33 / (double)var2.skyDistance;
            Visibility.Frame var17 = SkyText.cinemaFrame(var3, var1);
            double var18 = Math.max(-var17.maxYaw() * 0.6, Math.min(var17.maxYaw() * 0.6, var0.latDeg * 0.9));
            var26 = Math.max(var25 - var17.maxPitch() * 0.5, Math.min(var25 + var17.maxPitch() * 0.5, var26));
            List<double[]> var20 = SkyText.cinemaPoses(var3, var1);
            Vec3[] var21 = new Vec3[var20.size()];
            for (int var22 = 0; var22 < var21.length; ++var22) {
                var21[var22] = new Vec3(var20.get(var22)[0], var20.get(var22)[1], var20.get(var22)[2]);
            }
            Vec3 var36 = var3.player != null ? var3.player.getPosition(1.0f).add(0.0, (double)var3.player.getBbHeight() * 0.55, 0.0) : null;
            Visibility.Filter var24 = (var5x, var6, var8) -> SkyText.cinemaOk(var20, var17, var36, var5x, var6, var8) && !SkyText.overlapsOthers(var0, var4, var5x, var6, var8);
            return Visibility.find((Level)var3.level, var21, (double)var1.yRot() + var18, var26, var33, var30, (double)var0.halfWpx * var5 * var35, (double)var0.halfHpx * var5 * var35, var18, (Entity)var3.player, var17, var24);
        }
        double var7 = var0.wantDist / (double)var2.skyDistance;
        Vec3 var9 = var4;
        if (speed > 1.5 && SkyText.carryTarget() < 0.3f) {
            Vec3 var16;
            List<Lyrics.Word> var10 = var0.line.words();
            long var11 = var10 != null && !var10.isEmpty() ? var10.get(var10.size() - 1).endMs() : var0.line.startMs() + 3000L;
            double var13 = Math.min(2.5, Math.max(0.5, (double)(var11 - var0.line.startMs()) / 2000.0));
            Vec3 var15 = camVel.scale(var13);
            if (var15.length() > 40.0) {
                var15 = var15.normalize().scale(40.0);
            }
            if (Visibility.clear((Level)var3.level, var4, var16 = var4.add(var15), (Entity)var3.player)) {
                var9 = var16;
            }
        }
        Visibility.Frame var28 = SkyText.viewFrame(var3, var1, false);
        double var29 = -var1.xRot();
        double var31 = Math.max(var29 - var28.maxPitch() * 0.55, Math.min(var29 + var28.maxPitch() * 0.55, var0.pitchUp));
        double var34 = Math.max(-var28.maxYaw() * 0.6, Math.min(var28.maxYaw() * 0.6, var0.latDeg));
        return Visibility.find((Level)var3.level, new Vec3[]{var9}, (double)var1.yRot() + var34, var31, var0.wantDist, 8.0, (double)var0.halfWpx * var5 * var7, (double)var0.halfHpx * var5 * var7, var34, (Entity)var3.player, var28);
    }

    private static Visibility.Spot steepSpot(Placed var0, Camera var1, LyricsConfig var2, Visibility.Frame var3) {
        boolean var28;
        Vec3 var17;
        double var18;
        double var13;
        Minecraft var4 = Minecraft.getInstance();
        Vec3 var5 = var1.position();
        double var6 = Math.toRadians(var1.yRot());
        double var8 = Math.toRadians(var1.xRot());
        Vec3 var10 = new Vec3(-Math.sin(var6) * Math.cos(var8), -Math.sin(var8), Math.cos(var6) * Math.cos(var8));
        Vec3 var11 = new Vec3(-Math.sin(var6) * Math.sin(var8), Math.cos(var8), Math.cos(var6) * Math.sin(var8));
        Vec3 var12 = var10.cross(var11).normalize();
        double d = var13 = var4.level != null ? Visibility.hitDistance((Level)var4.level, var5, var5.add(var10.scale(120.0)), (Entity)var4.player) : -1.0;
        if (var13 < 0.0) {
            var13 = 120.0;
        }
        double var15 = Math.min(var0.wantDist, var13 * 0.6);
        if (var4.player != null && (var18 = var5.distanceTo(var17 = var4.player.getPosition(1.0f).add(0.0, (double)var4.player.getBbHeight() * 0.55, 0.0))) > 3.0) {
            var15 = Math.min(var15, var18 * 0.7);
        }
        var15 = Math.max(3.0, var15);
        double var30 = Math.toDegrees(Math.atan(var0.halfHpx * SkyText.px(var2, var0) / var2.skyDistance));
        double var19 = Math.toDegrees(Math.atan(var0.halfWpx * SkyText.px(var2, var0) / var2.skyDistance));
        double var21 = Math.max(var30 + 6.0, Math.min(var3.maxPitch() - var30 - 2.0, var3.maxPitch() * 0.5));
        double var23 = Math.max(0.0, var3.maxYaw() - var19 - 2.0);
        double var25 = Math.max(-var23 * 0.5, Math.min(var23 * 0.5, var0.latDeg * 0.5));
        Vec3 var27 = var5.add(var10.scale(var15)).add(var11.scale(var15 * Math.tan(Math.toRadians(var21)))).add(var12.scale(var15 * Math.tan(Math.toRadians(var25))));
        boolean bl = var28 = var4.level == null || Visibility.clear((Level)var4.level, var5, var27, (Entity)var4.player);
        if (!var28) {
            var15 = Math.max(2.5, var15 * 0.5);
            var27 = var5.add(var10.scale(var15)).add(var11.scale(var15 * Math.tan(Math.toRadians(var21)))).add(var12.scale(var15 * Math.tan(Math.toRadians(var25))));
        }
        return new Visibility.Spot(var27, var15, true);
    }

    private static Visibility.Frame cinemaFrame(Minecraft var0, Camera var1) {
        return SkyText.viewFrame(var0, var1, true);
    }

    private static Visibility.Frame viewFrame(Minecraft var0, Camera var1, boolean var2) {
        double var3 = ((Integer)var0.options.fov().get()).intValue();
        double var5 = Math.tan(Math.toRadians(var3 / 2.0));
        double var7 = var0.getWindow().getHeight() > 0 ? (double)var0.getWindow().getWidth() / (double)var0.getWindow().getHeight() : 1.7777777777777777;
        boolean var9 = false;
        try {
            var9 = var2 && AfkCinema.cfg.letterbox;
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        double var10 = var9 ? 0.78 : 1.0;
        double var12 = Math.toDegrees(Math.atan(var5 * var10)) - 3.0;
        double var14 = Math.toDegrees(Math.atan(var5 * var7)) - 4.0;
        return new Visibility.Frame(var1.yRot(), -var1.xRot(), var14, var12);
    }

    private static List<double[]> cinemaPoses(Minecraft var0, Camera var1) {
        ArrayList<double[]> var2 = new ArrayList<double[]>();
        Vec3 var3 = var1.position();
        var2.add(new double[]{var3.x, var3.y, var3.z, var1.yRot(), -var1.xRot()});
        if (var0.player != null) {
            // empty if block
        }
        return var2;
    }

    private static boolean cinemaOk(List<double[]> var0, Visibility.Frame var1, Vec3 var2, Vec3 var3, double var4, double var6) {
        int var8 = 0;
        for (int var9 = 0; var9 < var0.size(); ++var9) {
            boolean var22;
            boolean var16;
            double[] var10 = var0.get(var9);
            double[] var11 = SkyText.angles(var10, var3);
            double var12 = Math.toDegrees(Math.atan2(var4, var11[2]));
            double var14 = Math.toDegrees(Math.atan2(var6, var11[2]));
            boolean bl = var16 = Math.abs(Mth.wrapDegrees((double)(var11[0] - var10[3]))) + var12 <= var1.maxYaw() && Math.abs(var11[1] - var10[4]) + var14 <= var1.maxPitch();
            if (var9 == 0 && !var16) {
                return false;
            }
            if (var16) {
                ++var8;
            }
            if (var2 == null) continue;
            double[] var17 = SkyText.angles(var10, var2);
            double var18 = Math.toDegrees(Math.atan2(1.5, var17[2]));
            double var20 = Math.toDegrees(Math.atan2(1.6, var17[2]));
            boolean bl2 = var22 = Math.abs(Mth.wrapDegrees((double)(var11[0] - var17[0]))) < var18 + var12 + 2.0 && Math.abs(var11[1] - var17[1]) < var20 + var14 + 2.0;
            if (!var22) continue;
            return false;
        }
        return var8 * 3 >= var0.size() * 2;
    }

    private static double[] angles(double[] var0, Vec3 var1) {
        double var2 = var1.x - var0[0];
        double var4 = var1.y - var0[1];
        double var6 = var1.z - var0[2];
        double var8 = Math.max(0.001, Math.sqrt(var2 * var2 + var4 * var4 + var6 * var6));
        return new double[]{Math.toDegrees(Math.atan2(-var2, var6)), Math.toDegrees(Math.asin(Math.max(-1.0, Math.min(1.0, var4 / var8)))), var8};
    }

    private static Placed place(Lyrics.Line var0, Camera var1, LyricsConfig var2, long var3) {
        Visibility.Spot var17;
        Minecraft var5 = Minecraft.getInstance();
        Vec3 var6 = var1.position();
        int var7 = nextSide;
        nextSide = -nextSide;
        double var8 = var2.skyDistance;
        double var10 = 20.8;
        double var12 = var2.skyHeight;
        float var14 = -var2.skyTilt * (float)var7;
        if (var2.randomPlacement) {
            var10 = SkyText.rand(9.0f, 30.0f);
            var12 = Math.max(2.0, Math.min(65.0, (double)(var2.skyHeight + SkyText.rand(-8.0f, 12.0f))));
            var8 *= (double)SkyText.rand(0.85f, 1.15f);
            var14 += SkyText.rand(-3.0f, 3.0f);
        }
        float var15 = SkyText.carryTarget();
        if (var2.speedAdapt && speed > 2.0 && var15 < 0.3f) {
            double var16 = Math.max(var2.skyDistance, (float)(var5.options.getEffectiveRenderDistance() * 16 - 24));
            var8 = Math.min(Math.min(240.0, var16), var8 + speed * 3.5);
        }
        Placed var21 = new Placed(var0, var7, var3);
        var21.latDeg = var10 * (double)var7;
        var21.pitchUp = var12;
        var21.wantDist = var8;
        var21.tiltDeg = var14;
        var21.cinema = CinemaLyrics.cinema();
        var21.halfWpx = SkyText.layoutHalfWidth(var5.font, var0.text(), SkyText.style(var2));
        var21.halfHpx = SkyText.layoutHalfHeight(var5.font, var0.text(), SkyText.style(var2));
        boolean var18 = SkyText.elytraMode();
        boolean var19 = var2.flybyMode() && !var21.cinema && var15 < 0.3f && !var18;
        var21.carry = var15;
        var21.endMs = SkyText.lineEnd(var0);
        if (var18) {
            var21.air = true;
            SkyText.airPlace(var21, var1, var2);
            var21.targetSizeMul = var21.sizeMul;
            var21.baseSizeMul = var21.sizeMul;
            var21.passSide = var7;
            var21.lastCheckNanos = var3;
            return var21;
        }
        if (var19) {
            var17 = SkyText.groundSpot(var21, var1, var2);
        } else if (!var2.keepVisible && !var21.cinema) {
            Vec3 var20 = var6.add(Visibility.dir((double)var1.yRot() + var21.latDeg, var12).scale(var8));
            var17 = new Visibility.Spot(var20, var8, true);
        } else {
            var17 = SkyText.findSpot(var21, var1, var2);
        }
        var21.anchor = var17.anchor();
        var21.target = var17.anchor();
        var21.sizeMul = (float)(var17.distance() / (double)var2.skyDistance);
        if (var19) {
            var21.sizeMul = (float)var21.groundSizeMul;
            var21.ground = true;
        }
        var21.targetSizeMul = var21.sizeMul;
        var21.baseSizeMul = var21.sizeMul;
        var21.passSide = var7;
        var21.lastCheckNanos = var3;
        SkyText.face(var21, var6);
        if (var19) {
            Vec3 var22 = var21.anchor.subtract(var21.pathDir.scale(12.0));
            SkyText.face(var21, new Vec3(var22.x, var6.y, var22.z));
        }
        return var21;
    }

    static Layout layout(Font var0, String var1, Style var2) {
        String[] var3 = var1.split("\\s+");
        ArrayList<Word> var4 = new ArrayList<Word>();
        for (String var8 : var3) {
            String var9 = var8.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}']", "");
            boolean var10 = SMALL.contains(var9) || var9.length() <= 2;
            MutableComponent var11 = Component.literal((String)var8).setStyle(var2);
            var4.add(new Word((Component)var11, var10 && var3.length > 1 ? 0.6f : 1.0f, var0.width((FormattedText)var11)));
        }
        int var12 = var4.size();
        float var13 = 0.0f;
        for (Word var16 : var4) {
            var13 += (float)var16.width * var16.scale;
        }
        if (var4.size() > 3 || var13 > 100.0f) {
            var12 = (var4.size() + 1) / 2;
        }
        ArrayList<List<Word>> var15 = new ArrayList<List<Word>>();
        var15.add(var4.subList(0, var12));
        if (var12 < var4.size()) {
            var15.add(var4.subList(var12, var4.size()));
        }
        return new Layout(var15, var4.size());
    }

    static float layoutHalfHeight(Font var0, String var1, Style var2) {
        int var3 = SkyText.layout(var0, var1, var2).rows().size();
        return ((float)var3 * 9.0f + (float)(var3 - 1) * 2.0f) / 2.0f;
    }

    static float layoutHalfWidth(Font var0, String var1, Style var2) {
        float var3 = 0.0f;
        for (List<Word> var5 : SkyText.layout(var0, var1, var2).rows()) {
            float var6 = -3.0f;
            for (Word var8 : var5) {
                var6 += (float)var8.width * var8.scale + 3.0f;
            }
            var3 = Math.max(var3, var6);
        }
        return var3 / 2.0f;
    }

    private static void drawBlock(Font var0, Placed var1, Matrix4f var2, Sink var3, int var4, int var5, Style var6, float var7, float var8, long var9, boolean var11, LyricsConfig var12, float var13, long var14) {
        boolean var17;
        List<Lyrics.Word> var16 = var1.line.words();
        boolean bl = var17 = var12.wordDust && !var1.flying && !var1.ground && var1.fastFadeNanos == 0L && (var1.dustLock || var7 > 0.15f);
        if (var17 && var1.dyingNanos > 0L) {
            var1.dustLock = true;
        }
        float var18 = var1.dyingNanos > 0L ? (float)(var14 - var1.dyingNanos) / 1.0E9f : -1.0f;
        float var19 = var1.dissolve ? 0.03f : 0.06f;
        float var20 = Lyrics.wordPosition(var1.line, var9);
        Layout var21 = SkyText.layout(var0, var1.text, var6);
        List<List<Word>> var22 = var21.rows();
        float var23 = 9.0f;
        float var24 = 3.0f;
        float var25 = (float)var22.size() * var23 + (float)((var22.size() - 1) * 2);
        float var26 = -var25 / 2.0f;
        int var27 = var21.words();
        int var28 = LyricsConfig.rgb(var12.color2, 16020150);
        boolean var29 = var12.gradient;
        float var30 = var12.wordByWord ? Math.min(0.16f, 1.2f / (float)Math.max(1, var27)) : 0.0f;
        int var31 = 0;
        if (var1.dusted == null || var1.dusted.length < var27) {
            var1.dusted = new boolean[Math.max(1, var27)];
        }
        for (int var32 = 0; var32 < var22.size(); ++var32) {
            float var51;
            List<Word> var33 = var22.get(var32);
            float var34 = -var24;
            for (Word var36 : var33) {
                var34 += (float)var36.width * var36.scale + var24;
            }
            float var52 = var51 = -var34 / 2.0f + (var22.size() > 1 ? (var32 == 0 ? -var34 * 0.08f : var34 * 0.12f) : 0.0f);
            for (Word var38 : var33) {
                Lyrics.Word var56;
                long var60;
                float var39 = var7;
                float var40 = 0.0f;
                float var41 = 0.0f;
                if (var16 != null && var31 < var16.size()) {
                    float var42 = var20 - (float)var31;
                    if (var11) {
                        float var43 = (float)(var9 - var16.get(var31).startMs()) / 250.0f;
                        float var44 = Math.max(0.0f, Math.min(1.0f, var43));
                        var44 = var44 * var44 * (3.0f - 2.0f * var44);
                        var39 = var7 * var44;
                        var40 = (1.0f - var44) * 4.0f;
                    } else {
                        float var54 = Math.max(0.0f, Math.min(1.0f, (var8 - (float)var31 * var30) / 0.28f));
                        var54 = var54 * var54 * (3.0f - 2.0f * var54);
                        float var59 = Math.max(0.0f, Math.min(1.0f, var42));
                        var39 = var7 * var54 * (1.0f - var59);
                        var40 = (1.0f - var54) * 4.0f;
                        var41 = var59;
                    }
                }
                float var53 = -1.0f;
                if (var12.wordDust && var16 != null && var31 < var16.size() && var1.dyingNanos < 0L && (var60 = (var56 = var16.get(var31)).endMs() - var56.startMs()) >= 1200L && var9 >= var56.startMs() && var9 < var56.endMs()) {
                    var53 = (float)(var9 - var56.startMs()) / (float)var60;
                    if (!var11) {
                        var39 = Math.max(var39, var7 * Math.min(1.0f, (var8 - (float)var31 * var30) / 0.28f));
                        var41 = 0.0f;
                    }
                }
                float var57 = -1.0f;
                if (var17 && var18 >= 0.0f) {
                    var57 = var18 - (float)var31 * var19;
                    if (var57 >= 0.0f) {
                        var39 *= Math.max(0.0f, 1.0f - var57 / 0.22f);
                    }
                } else if (var17 && !var11 && var41 >= 0.6f) {
                    var57 = 0.0f;
                }
                float var61 = var1.beatPulse * Math.max(0.0f, Math.min(1.0f, (var39 / Math.max(0.05f, var7) - 0.55f) / 0.4f)) * Math.max(0.0f, Math.min(1.0f, (var7 - 0.3f) / 0.5f));
                int var45 = var29 && var27 > 1 ? SkyText.mix(var4, var28, (float)var31 / (float)(var27 - 1)) : var4;
                int var46 = var61 > 0.01f ? SkyText.mix(var45, 0xFFFFFF, 0.3f * var61) : var45;
                float var47 = var38.scale * (1.0f + var41 * 0.25f) * (var53 >= 0.0f ? 1.0f + 0.3f * var53 : 1.0f) * (1.0f + 0.07f * var61);
                float var48 = var26 + (var23 - var23 * var38.scale) + var40 - var41 * 5.0f - (var53 >= 0.0f ? var53 * 2.0f : 0.0f);
                if (var12.arc && var34 > 20.0f) {
                    float var49 = Math.max(0.0f, Math.min(1.0f, (var51 - var52 + (float)var38.width * var38.scale / 2.0f) / var34));
                    float var50 = 2.0f * var49 - 1.0f;
                    var48 -= 4.0f * (1.0f - var50 * var50) - 1.5f * var50;
                }
                float var62 = var51 - (var47 - var38.scale) * (float)var38.width / 2.0f;
                if (var57 >= 0.0f && var31 < var1.dusted.length && !var1.dusted[var31]) {
                    var1.dusted[var31] = true;
                    int var63 = Math.max(6, Math.min(36, var38.width / 2));
                    SkyText.dustRect(var1, var13, var62, var48, (float)var38.width * var47, 8.0f * var47, var45, var63, false, 1.0f);
                }
                if (var39 > 0.5f && var16 != null && var31 < var16.size() && var9 >= var16.get(var31).startMs() && var9 < var16.get(var31).endMs() + 150L) {
                    var1.sparkX = var62;
                    var1.sparkY = var48;
                    var1.sparkW = (float)var38.width * var47;
                    var1.sparkH = 8.0f * var47;
                    var1.sparkNanos = var14;
                }
                if (var39 > 0.02f) {
                    Matrix4f var64 = new Matrix4f((Matrix4fc)var2).translate(var62, var48, 0.0f).scale(var47, var47, var47);
                    if (var53 >= 0.0f) {
                        float keepGlow = glowAmt;
                        glowAmt = Math.min(1.0f, Math.max(glowAmt, 0.35f) + 0.45f * var53);
                        SkyText.drawWord(var0, var38.comp, var64, var3, SkyText.mix(var45, 0xFFFFFF, 0.2f + 0.3f * var53), var5, var39, var12.textEffect);
                        glowAmt = keepGlow;
                        if (var14 - var1.lastSparkNanos > 90000000L) {
                            var1.lastSparkNanos = var14;
                            SkyText.dustRect(var1, var13, var62, var48 - 2.0f, (float)var38.width * var47, 10.0f * var47, 0xFFFFFF, 2, true, 0.6f);
                        }
                    } else {
                        SkyText.drawWord(var0, var38.comp, var64, var3, var46, var5, var39, var12.textEffect);
                    }
                }
                var51 += (float)var38.width * var38.scale + var24;
                ++var31;
            }
            var26 += var23 + 2.0f;
        }
    }

    static int mix(int var0, int var1, float var2) {
        var2 = Math.max(0.0f, Math.min(1.0f, var2));
        int var3 = Math.round((float)(var0 >> 16 & 0xFF) * (1.0f - var2) + (float)(var1 >> 16 & 0xFF) * var2);
        int var4 = Math.round((float)(var0 >> 8 & 0xFF) * (1.0f - var2) + (float)(var1 >> 8 & 0xFF) * var2);
        int var5 = Math.round((float)(var0 & 0xFF) * (1.0f - var2) + (float)(var1 & 0xFF) * var2);
        return var3 << 16 | var4 << 8 | var5;
    }

    static void dustRect(Placed var0, float var1, float var2, float var3, float var4, float var5, int var6, int var7, boolean var8, float var9) {
        Minecraft var10 = Minecraft.getInstance();
        ClientLevel var11 = var10.level;
        if (var11 != null && var0.anchor != null && var7 > 0) {
            Quaternionf var12 = new Quaternionf().rotationY(var0.yawRad).rotateX(var0.pitchRad).rotateZ((float)Math.toRadians(var0.tiltDeg));
            Vector3f var13 = var12.transform(new Vector3f(1.0f, 0.0f, 0.0f));
            Vector3f var14 = var12.transform(new Vector3f(0.0f, 1.0f, 0.0f));
            double var15 = Math.max(4.0, var0.anchor.distanceTo(lastCamPos));
            double var17 = Math.max(0.35, Math.min(4.0, 16.0 * (double)var1 * (double)var9));
            double var19 = Math.max(0.4, Math.min(2.5, var15 / 25.0));
            float var21 = var2 + var4 / 2.0f;
            float var22 = var3 + var5 / 2.0f;
            for (int var23 = 0; var23 < var7; ++var23) {
                float var24 = var2 + RNG.nextFloat() * var4;
                float var25 = var3 + RNG.nextFloat() * var5;
                double var26 = var0.anchor.x + (double)((var13.x() * var24 - var14.x() * var25) * var1);
                double var28 = var0.anchor.y + (double)((var13.y() * var24 - var14.y() * var25) * var1);
                double var30 = var0.anchor.z + (double)((var13.z() * var24 - var14.z() * var25) * var1);
                if (var8) {
                    double var38 = var24 - var21;
                    double var40 = -(var25 - var22);
                    double var42 = Math.max(1.0, Math.sqrt(var38 * var38 + var40 * var40));
                    double var44 = var19 * (0.6 + RNG.nextDouble() * 0.8);
                    var32 = ((double)var13.x() * var38 + (double)var14.x() * var40) / var42 * var44;
                    var34 = ((double)var13.y() * var38 + (double)var14.y() * var40) / var42 * var44;
                    var36 = ((double)var13.z() * var38 + (double)var14.z() * var40) / var42 * var44;
                } else {
                    double var49 = (0.3 + RNG.nextDouble() * 0.6) * var19;
                    double var51 = (0.2 + RNG.nextDouble() * 0.5) * var19;
                    var32 = (double)var13.x() * var49 + (double)var14.x() * var51;
                    var34 = (double)var13.y() * var49 + (double)var14.y() * var51;
                    var36 = (double)var13.z() * var49 + (double)var14.z() * var51;
                }
                float var50 = (float)(var17 * (0.7 + RNG.nextDouble() * 0.5));
                var11.addParticle((ParticleOptions)new DustParticleOptions(var6 & 0xFFFFFF, Math.min(4.0f, var50)), true, true, var26, var28, var30, var32 += RNG.nextGaussian() * 0.15 * var19, var34 += RNG.nextGaussian() * 0.15 * var19, var36 += RNG.nextGaussian() * 0.15 * var19);
            }
        }
    }

    private static void beatFrame(LyricsConfig var0, long var1, long var3) {
        if (!BeatSync.active()) {
            lastBeatIdx = Long.MIN_VALUE;
        } else {
            long var5 = BeatSync.index(var1);
            if (lastBeatIdx == Long.MIN_VALUE || var5 < lastBeatIdx || var5 - lastBeatIdx > 2L) {
                lastBeatIdx = var5;
            } else if (var5 != lastBeatIdx) {
                Lyrics var10;
                lastBeatIdx = var5;
                boolean var7 = Math.floorMod(var5, 4L) == 0L;
                boolean var8 = false;
                MediaWatcher.Track var9 = MediaWatcher.track();
                Lyrics lyrics = var10 = var9 != null ? LyricsFetcher.get(var9) : null;
                if (var10 != null && var10.synced && !var10.lines.isEmpty()) {
                    int var11 = var10.indexAt(var3);
                    boolean bl = var8 = var11 >= 0 && var10.isHook(var11);
                }
                float var29 = "epic".equals(var0.fxLevel) ? 1.5f : ("subtle".equals(var0.fxLevel) ? 0.5f : 1.0f);
                Placed var12 = null;
                for (Placed var14 : placed) {
                    if (var14.dyingNanos >= 0L || var14.ground || !(var14.nearFade > 0.3f) || !(var14.relocAlpha > 0.5f)) continue;
                    var12 = var14;
                }
                if (var12 != null && var1 - var12.sparkNanos < 120000000L) {
                    float var30 = SkyText.px(var0, var12) * var12.sizeMul;
                    int var32 = Math.round((float)(var8 ? 8 : 4) * (var7 ? 1.6f : 1.0f) * var29);
                    SkyText.dustRect(var12, var30, var12.sparkX, var12.sparkY, var12.sparkW, var12.sparkH, SkyText.mix(lastColor, 0xFFFFFF, 0.35f), var32, true, 0.7f);
                }
                Minecraft var31 = Minecraft.getInstance();
                if (var31.player != null && var31.level != null && (var8 ? var7 : Math.floorMod(var5, 8L) == 0L) && var29 > 0.6f) {
                    boolean var15;
                    Vec3 var33 = var31.player.getPosition(1.0f);
                    boolean bl = var15 = CinemaLyrics.cinema() && lastCamPos != null && Math.abs(SkyText.lastCamPos.y - var33.y) < 3.0 && (SkyText.lastCamPos.x - var33.x) * (SkyText.lastCamPos.x - var33.x) + (SkyText.lastCamPos.z - var33.z) * (SkyText.lastCamPos.z - var33.z) < 64.0 && lastCamPos.distanceTo(var33.add(0.0, 1.6, 0.0)) > 0.5;
                    if (var15) {
                        return;
                    }
                    Vec3 var16 = MusicFx.ahead(var31.player.getPosition(1.0f), 0.25).add(0.0, 0.15, 0.0);
                    int var17 = Math.round((float)(var8 ? 32 : 20) * var29);
                    double var18 = var8 ? 0.45 : 0.32;
                    double var20 = RNG.nextDouble() * Math.PI * 2.0;
                    for (int var22 = 0; var22 < var17; ++var22) {
                        double var23 = var20 + (double)var22 * Math.PI * 2.0 / (double)var17;
                        double var25 = Math.cos(var23);
                        double var27 = Math.sin(var23);
                        var31.level.addParticle((ParticleOptions)ParticleTypes.FIREWORK, true, true, var16.x + var25 * 0.6, var16.y, var16.z + var27 * 0.6, var25 * var18, 0.02, var27 * var18);
                    }
                }
            }
        }
    }

    public static void testIntro() {
        lastTrackKey = "#test";
    }

    private static void checkSongChange(LyricsConfig var0, Camera var1, long var2, boolean var4) {
        String var6;
        MediaWatcher.Track var5 = MediaWatcher.track();
        if (var5 != null && !var5.title().isEmpty() && !(var6 = var5.key()).equals(lastTrackKey)) {
            boolean var7 = lastTrackKey == null;
            lastTrackKey = var6;
            if (var0.songIntro && var0.enabled && var0.skyEnabled && !var4 && MediaWatcher.isPlaying() && (!var0.skyOnlyCinema || CinemaLyrics.cinema()) && (!var7 || MediaWatcher.positionMs() <= 20000L)) {
                for (Placed var9 : placed) {
                    if (var9.dyingNanos < 0L) {
                        var9.dyingNanos = var2;
                    }
                    var9.dissolve = true;
                    var9.flying = false;
                }
                if (!var7) {
                    NowPlayingHud.flash();
                }
                Minecraft var16 = Minecraft.getInstance();
                String var17 = var5.title().length() > 34 ? var5.title().substring(0, 33) + "\u2026" : var5.title();
                Style var10 = SkyText.style(var0).withBold(Boolean.valueOf(true));
                Placed var11 = new Placed(new Lyrics.Line(0L, var17, null), 1, var2);
                titleSub = var5.artist().isEmpty() ? "" : "\u266a " + (String)(var5.artist().length() > 40 ? var5.artist().substring(0, 39) + "\u2026" : var5.artist());
                float var12 = (float)var16.font.width((FormattedText)Component.literal((String)var17).setStyle(var10)) * 1.7f;
                float var13 = titleSub.isEmpty() ? 0.0f : (float)var16.font.width((FormattedText)Component.literal((String)titleSub).setStyle(SkyText.style(var0))) * 0.8f;
                var11.halfWpx = Math.max(var12, var13) / 2.0f;
                var11.halfHpx = (15.3f + (titleSub.isEmpty() ? 0.0f : 11.200001f)) / 2.0f;
                var11.latDeg = 0.0;
                var11.pitchUp = Math.min(60.0, (double)var0.skyHeight + 10.0);
                var11.wantDist = var0.skyDistance;
                var11.cinema = CinemaLyrics.cinema();
                Vec3 var14 = var1.position();
                Visibility.Spot var15 = !var0.keepVisible && !var11.cinema ? new Visibility.Spot(var14.add(Visibility.dir(var1.yRot(), var11.pitchUp).scale(var11.wantDist)), var11.wantDist, true) : SkyText.findSpot(var11, var1, var0);
                var11.anchor = var15.anchor();
                var11.target = var15.anchor();
                var11.sizeMul = (float)(var15.distance() / (double)var0.skyDistance);
                SkyText.face(var11, var14);
                title = var11;
                titleOutNanos = 0L;
                titleDust = new boolean[var17.codePointCount(0, var17.length())];
                titleSubDust = false;
            }
        }
    }

    private static void renderTitle(Minecraft var0, Camera var1, PoseStack var2, Sink var3, LyricsConfig var4, Style var5, int var6, int var7, long var8, float var10, long var11) {
        float var35;
        Placed var13 = title;
        float var14 = (float)(var8 - var13.bornNanos) / 1.0E9f;
        if (titleOutNanos == 0L) {
            boolean var15;
            boolean bl = var15 = var14 > 5.0f || !MediaWatcher.isPlaying() || !var4.enabled || !var4.skyEnabled;
            if (!var15 && var14 > 2.4f) {
                Lyrics var17;
                MediaWatcher.Track var16 = MediaWatcher.track();
                Lyrics lyrics = var17 = var16 != null ? LyricsFetcher.get(var16) : null;
                if (var17 != null && var17.synced) {
                    for (Lyrics.Line var19 : var17.lines) {
                        if (var19.text().isBlank()) continue;
                        var15 = var19.startMs() - var11 < 600L;
                        break;
                    }
                }
            }
            if (var15) {
                titleOutNanos = var8;
            }
        }
        float f = var35 = titleOutNanos == 0L ? -1.0f : (float)(var8 - titleOutNanos) / 1.0E9f;
        if (var35 > 1.6f) {
            title = null;
        } else {
            Vec3 var36 = var1.position();
            SkyText.face(var13, var36);
            float var37 = 0.13f * var4.skyScale * var13.sizeMul * (1.0f + 0.06f * (titleOutNanos == 0L ? var10 : 0.0f));
            float var38 = skyFade * openSky;
            if (!(var38 < 0.02f)) {
                Style var39 = var5.withBold(Boolean.valueOf(true));
                Font var20 = var0.font;
                String var21 = var13.text;
                int[] var22 = var21.codePoints().toArray();
                float var23 = (float)var20.width((FormattedText)Component.literal((String)var21).setStyle(var39)) * 1.7f;
                float var24 = -var13.halfHpx;
                var2.pushPose();
                var2.translate(var13.anchor.x - var36.x, var13.anchor.y - var36.y, var13.anchor.z - var36.z);
                var2.mulPose((Quaternionfc)new Quaternionf().rotationY(var13.yawRad));
                var2.mulPose((Quaternionfc)new Quaternionf().rotationX(var13.pitchRad));
                var2.scale(var37, -var37, var37);
                Matrix4f var25 = var2.last().pose();
                float var26 = -var23 / 2.0f;
                for (int var27 = 0; var27 < var22.length; ++var27) {
                    float var32;
                    MutableComponent var28 = Component.literal((String)new String(var22, var27, 1)).setStyle(var39);
                    float var29 = (float)var20.width((FormattedText)var28) * 1.7f;
                    float var30 = Math.max(0.0f, Math.min(1.0f, (var14 - 0.15f - (float)var27 * 0.045f) / 0.22f));
                    var30 = var30 * var30 * (3.0f - 2.0f * var30);
                    float var31 = var38 * var30;
                    if (var35 >= 0.0f && (var32 = var35 - (float)var27 * 0.025f) >= 0.0f) {
                        if (!titleDust[var27] && var4.wordDust) {
                            SkyText.titleDust[var27] = true;
                            SkyText.dustRect(var13, var37, var26, var24, var29, 15.3f, var6, 10, false, 1.2f);
                        }
                        var31 *= Math.max(0.0f, 1.0f - var32 / 0.2f);
                    }
                    if (var31 > 0.02f) {
                        float var46 = 1.0f + (1.0f - var30) * 0.9f;
                        float var33 = 1.7f * var46;
                        Matrix4f var34 = new Matrix4f((Matrix4fc)var25).translate(var26 + var29 / 2.0f - var29 * var46 / 2.0f, var24 + 15.3f * (1.0f - var46) / 2.0f, 0.0f).scale(var33, var33, var33);
                        SkyText.drawWord(var20, (Component)var28, var34, var3, var6, var7, var31, var4.textEffect);
                    }
                    var26 += var29;
                }
                if (!titleSub.isEmpty()) {
                    float var48;
                    float var40 = 0.15f + (float)var22.length * 0.045f + 0.1f;
                    float var41 = Math.max(0.0f, Math.min(1.0f, (var14 - var40) / 0.4f));
                    float var42 = var38 * var41 * 0.9f;
                    MutableComponent var44 = Component.literal((String)titleSub).setStyle(var5);
                    float var45 = (float)var20.width((FormattedText)var44) * 0.8f;
                    float var47 = var24 + 15.3f + 4.0f + (1.0f - var41) * 3.0f;
                    if (var35 >= 0.0f && (var48 = var35 - Math.min(0.5f, (float)var22.length * 0.025f)) >= 0.0f) {
                        if (!titleSubDust && var4.wordDust) {
                            titleSubDust = true;
                            SkyText.dustRect(var13, var37, -var45 / 2.0f, var47, var45, 7.2000003f, var6, Math.max(8, Math.min(40, Math.round(var45 / 3.0f))), false, 0.8f);
                        }
                        var42 *= Math.max(0.0f, 1.0f - var48 / 0.2f);
                    }
                    if (var42 > 0.02f) {
                        Matrix4f var49 = new Matrix4f((Matrix4fc)var25).translate(-var45 / 2.0f, var47, 0.0f).scale(0.8f, 0.8f, 0.8f);
                        SkyText.drawWord(var20, (Component)var44, var49, var3, SkyText.mix(var6, 0xFFFFFF, 0.2f), var7, var42, var4.textEffect);
                    }
                }
                var2.popPose();
            }
        }
    }

    static void drawPlate(Font var0, Component var1, Matrix4f var2, Sink var3, int var4, int var5, float var6, float var7, boolean var8) {
        int var9 = Math.max(5, Math.round(var6 * 255.0f));
        if (var7 > 0.01f) {
            int var10 = var9 << 24 | var5 & 0xFFFFFF;
            Matrix4f var11 = var8 ? new Matrix4f((Matrix4fc)var2).translate(0.0f, 0.0f, -0.6f) : var2;
            int var12 = var7 <= 1.2f ? 8 : 12;
            for (int var13 = 0; var13 < var12; ++var13) {
                double var14 = (double)var13 * Math.PI * 2.0 / (double)var12;
                Sink.draw(var0, var1, (float)(Math.cos(var14) * (double)var7), (float)(Math.sin(var14) * (double)var7), var10, false, var11, var3, Font.DisplayMode.NORMAL, 0, 0xF000F0);
            }
        }
        Sink.draw(var0, var1, 0.0f, 0.0f, var9 << 24 | var4 & 0xFFFFFF, false, var2, var3, var7 > 0.01f && !var8 ? Font.DisplayMode.POLYGON_OFFSET : Font.DisplayMode.NORMAL, 0, 0xF000F0);
    }

    static void drawWord(Font var0, Component var1, Matrix4f var2, Sink var3, int var4, int var5, float var6, String var7) {
        int var8 = Math.max(5, Math.round(var6 * 255.0f));
        int var9 = var8 << 24 | var4 & 0xFFFFFF;
        if (glowAmt > 0.02f) {
            Object cached = WIDTHS.get(var1);
            if (cached == null) {
                if (WIDTHS.size() > 256) {
                    WIDTHS.clear();
                }
                cached = var0.width((FormattedText)var1);
                WIDTHS.put(var1, (Integer)cached);
            }
            float w = ((Integer)cached).intValue();
            GlowQuad.draw(var3, var2, -1.0f, -1.0f, w + 1.0f, 8.0f, 4.5f, glowCol, var6 * glowAmt * 0.55f, seeThrough);
        }
        if (!seeThrough) {
            switch (var7) {
                case "outline": {
                    Sink.outline(var0, var1.getVisualOrderText(), 0.0f, 0.0f, var9, var8 << 24 | var5 & 0xFFFFFF, var2, var3, 0xF000F0);
                    break;
                }
                case "shadow": {
                    int shadow = var9 & 0xFF000000 | var9 >> 2 & 0x3F3F3F;
                    Sink.draw(var0, var1, 1.0f, 1.0f, shadow, false, var2, var3, Font.DisplayMode.NORMAL, 0, 0xF000F0);
                    Matrix4f front = new Matrix4f((Matrix4fc)var2).translate(0.0f, 0.0f, 0.8f);
                    Sink.draw(var0, var1, 0.0f, 0.0f, var9, false, front, var3, Font.DisplayMode.NORMAL, 0, 0xF000F0);
                    break;
                }
                default: {
                    Sink.draw(var0, var1, 0.0f, 0.0f, var9, false, var2, var3, Font.DisplayMode.NORMAL, 0, 0xF000F0);
                    break;
                }
            }
        } else {
            Font.DisplayMode var11 = Font.DisplayMode.SEE_THROUGH;
            if ("outline".equals(var7)) {
                int var12 = var8 << 24 | var5 & 0xFFFFFF;
                Sink.draw(var0, var1, -0.6f, -0.6f, var12, false, var2, var3, var11, 0, 0xF000F0);
                Sink.draw(var0, var1, 0.6f, -0.6f, var12, false, var2, var3, var11, 0, 0xF000F0);
                Sink.draw(var0, var1, -0.6f, 0.6f, var12, false, var2, var3, var11, 0, 0xF000F0);
                Sink.draw(var0, var1, 0.6f, 0.6f, var12, false, var2, var3, var11, 0, 0xF000F0);
            }
            Sink.draw(var0, var1, 0.0f, 0.0f, var9, "shadow".equals(var7), var2, var3, var11, 0, 0xF000F0);
        }
    }

    static {
        openSkyLast = true;
        skyFade = 1.0f;
        lastColor = 0xFFFFFF;
        lastCamPos = Vec3.ZERO;
        titleSub = "";
        camVel = Vec3.ZERO;
        lastBeatIdx = Long.MIN_VALUE;
        glowCol = 0xFFFFFF;
        WIDTHS = new IdentityHashMap();
    }

    static final class Placed {
        boolean dustLock;
        float dieCap = 1.0f;
        boolean airPassed;
        Vec3 airDir;
        long outSince;
        int insideHits;
        final Lyrics.Line line;
        final String text;
        final int side;
        final long bornNanos;
        float sizeMul;
        float baseSizeMul;
        float targetSizeMul;
        Vec3 anchor;
        Vec3 target;
        float yawRad;
        float pitchRad;
        float tiltDeg;
        double latDeg;
        double pitchUp;
        double wantDist;
        float halfWpx;
        float halfHpx;
        boolean groundTimeFade;
        long fastFadeNanos;
        boolean ground;
        double groundSizeMul = 1.0;
        Vec3 pathDir = new Vec3(0.0, 0.0, 1.0);
        double relYaw;
        double relPitch;
        double relDist;
        float nearFade = 1.0f;
        int routeSide;
        double lift;
        long routeNanos;
        long blockCheckNanos;
        boolean inBlock;
        float blockFade = 1.0f;
        boolean locked;
        boolean steep;
        float relocAlpha = 1.0f;
        int relocPhase;
        long occludedSince;
        long lastRelocNanos;
        boolean cinema;
        long lastCheckNanos;
        float moveFade = 1.0f;
        long dyingNanos = -1L;
        boolean flying;
        boolean passed;
        int passSide;
        long passedNanos;
        float carry;
        boolean air;
        long endMs;
        double airMove;
        long airOutSince;
        boolean[] dusted;
        boolean dissolve;
        long lastSparkNanos;
        float sparkX;
        float sparkY;
        float sparkW;
        float sparkH;
        long sparkNanos;
        float beatPulse;
        long arriveMs;
        double dirX;
        double dirY;
        double dirZ;
        double vel;

        Placed(Lyrics.Line var1, int var2, long var3) {
            this.line = var1;
            this.text = var1.text();
            this.side = var2;
            this.bornNanos = var3;
        }
    }

    record Word(Component comp, float scale, int width) {
    }

    record Layout(List<List<Word>> rows, int words) {
    }
}

