/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityTypes
 *  net.minecraft.world.entity.LightningBolt
 *  net.minecraft.world.item.component.FireworkExplosion
 *  net.minecraft.world.item.component.FireworkExplosion$Shape
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.minecraft.world.phys.Vec3
 */
package net.friendswithmoney.fwm.lyrics;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.friendswithmoney.fwm.lyrics.CoverTexture;
import net.friendswithmoney.fwm.lyrics.GenreWeather;
import net.friendswithmoney.fwm.lyrics.Lyrics;
import net.friendswithmoney.fwm.lyrics.LyricsConfig;
import net.friendswithmoney.fwm.lyrics.LyricsFetcher;
import net.friendswithmoney.fwm.lyrics.LyricsTime;
import net.friendswithmoney.fwm.lyrics.MediaWatcher;
import net.friendswithmoney.fwm.lyrics.SkyLyrics;
import net.friendswithmoney.fwm.lyrics.SkyText;
import net.friendswithmoney.fwm.lyrics.SkyTone;
import net.friendswithmoney.fwm.lyrics.Visibility;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public final class MusicFx {
    private static final Random RNG = new Random();
    private static final List<Rocket> rockets = new ArrayList<Rocket>();
    private static final List<long[]> queue = new ArrayList<long[]>();
    private static final List<Job> jobs = new ArrayList<Job>();
    private static String lastKey = "";
    private static int lastIdx = -1;
    private static int lastWord = -1;
    private static int noteLine = -1;
    private static long lastVolleyMs;
    private static long nextStarMs;
    private static int nextBoltId;

    private MusicFx() {
    }

    static Vec3 ahead(Vec3 var0, double var1) {
        Vec3 var3 = SkyText.velocity();
        Vec3 var4 = new Vec3(var3.x, var3.y * 0.3, var3.z).scale(var1);
        if (var4.length() > 60.0) {
            var4 = var4.normalize().scale(60.0);
        }
        return var0.add(var4);
    }

    private static float effectYaw(Camera var0) {
        Vec3 var1 = SkyText.velocity();
        double var2 = Math.sqrt(var1.x * var1.x + var1.z * var1.z);
        return var2 > 3.0 ? (float)Math.toDegrees(Math.atan2(-var1.x, var1.z)) : var0.yRot();
    }

    private static int cooldownMs(LyricsConfig var0) {
        String var1;
        return switch (var1 = var0.fxLevel) {
            case "subtle" -> 20000;
            case "epic" -> 8000;
            default -> 12000;
        };
    }

    private static int rocketsFor(LyricsConfig var0, Lyrics.Moment var1) {
        String var3;
        int var2 = switch (var3 = var0.fxLevel) {
            case "subtle" -> 1;
            case "epic" -> 3;
            default -> 2;
        };
        return switch (var1) {
            case Lyrics.Moment.INTRO -> Math.max(1, var2 - 1);
            case Lyrics.Moment.FINALE -> var2 + 2;
            default -> var2;
        };
    }

    private static boolean anyOn(LyricsConfig var0) {
        return var0.musicFx || var0.fxLightning || var0.fxNotes || var0.fxPillars || var0.fxStars;
    }

    public static void tick(Minecraft var0) {
        ClientLevel var1 = var0.level;
        if (var1 != null && var0.player != null) {
            MusicFx.updateRockets(var1);
            Iterator<Job> var2 = jobs.iterator();
            while (var2.hasNext()) {
                Job var3 = var2.next();
                var3.tick(var1);
                if (++var3.age < var3.life) continue;
                var2.remove();
            }
            long var15 = System.currentTimeMillis();
            Iterator<long[]> var5 = queue.iterator();
            while (var5.hasNext()) {
                long[] var6 = var5.next();
                if (var15 < var6[0]) continue;
                var5.remove();
                MusicFx.launch(var0, var1, (int)var6[1]);
            }
            LyricsConfig var16 = SkyLyrics.cfg;
            MediaWatcher.Track var7 = MediaWatcher.track();
            if (var16.enabled && MusicFx.anyOn(var16) && var7 != null && MediaWatcher.isPlaying()) {
                Lyrics var8;
                if (!var7.key().equals(lastKey)) {
                    lastKey = var7.key();
                    lastIdx = -1;
                    lastWord = -1;
                }
                if (var16.fxStars) {
                    MusicFx.stars(var0, var1, var15);
                }
                if ((var8 = LyricsFetcher.get(var7)) != null && var8.synced && !var8.lines.isEmpty()) {
                    long var9 = LyricsTime.songMs();
                    int var11 = var8.indexAt(var9);
                    if (var11 != lastIdx) {
                        Lyrics.Moment var14;
                        boolean var12 = lastIdx >= 0 ? var11 == lastIdx + 1 : var11 >= 0 && var9 - var8.lines.get(var11).startMs() < 1500L;
                        int var13 = lastIdx;
                        lastIdx = var11;
                        lastWord = -1;
                        if (var12 && var11 >= 0 && var11 != var13 && (var14 = var8.momentAt(var11)) != null) {
                            MusicFx.onMoment(var0, var1, var16, var14);
                        }
                    }
                    if (var16.fxNotes && var11 >= 0 && var8.isHook(var11) && var9 < var8.endOf(var11, var7.durationMs())) {
                        Lyrics.Line var17 = var8.lines.get(var11);
                        int var18 = var17.words() == null ? 0 : var17.words().size();
                        int var19 = (int)Math.floor(Lyrics.wordPosition(var17, var9));
                        if (var19 != lastWord && var19 >= 0 && var19 < var18) {
                            lastWord = var19;
                            MusicFx.note(var0, var1, var19);
                        }
                    }
                }
            }
        } else {
            rockets.clear();
            queue.clear();
            jobs.clear();
        }
    }

    private static void onMoment(Minecraft var0, ClientLevel var1, LyricsConfig var2, Lyrics.Moment var3) {
        boolean var6;
        long var4 = System.currentTimeMillis();
        boolean bl = var6 = var3 == Lyrics.Moment.FINALE;
        if (var6 || var4 - lastVolleyMs >= (long)MusicFx.cooldownMs(var2)) {
            lastVolleyMs = var4;
            GenreWeather.Weather var7 = GenreWeather.weatherFor(GenreWeather.genre(MediaWatcher.track()));
            if (var2.musicFx) {
                int var8 = MusicFx.rocketsFor(var2, var3);
                for (int var9 = 0; var9 < var8; ++var9) {
                    queue.add(new long[]{var4 + (long)var9 * (180L + (long)RNG.nextInt(220)), var6 ? 2L : 1L});
                }
            }
            if (var2.fxLightning && var7 == GenreWeather.Weather.THUNDER && var3 != Lyrics.Moment.INTRO) {
                MusicFx.lightning(var0, var1);
                if (var6 || "epic".equals(var2.fxLevel)) {
                    queue.add(new long[]{var4 + 700L, -1L});
                }
            }
            if (var2.fxPillars && var6) {
                MusicFx.pillars(var0, var1);
            }
        }
    }

    public static void preview() {
        LyricsConfig var0 = SkyLyrics.cfg;
        Minecraft var1 = Minecraft.getInstance();
        long var2 = System.currentTimeMillis();
        for (int var4 = 0; var4 < 3; ++var4) {
            queue.add(new long[]{var2 + (long)var4 * 250L, 1L});
        }
        if (var1.level != null && var1.player != null) {
            if (var0.fxLightning) {
                queue.add(new long[]{var2 + 900L, -1L});
            }
            if (var0.fxPillars) {
                MusicFx.pillars(var1, var1.level);
            }
            if (var0.fxStars) {
                nextStarMs = 0L;
                MusicFx.shootingStar(var1, var1.level);
            }
            if (var0.fxNotes) {
                for (int var5 = 0; var5 < 4; ++var5) {
                    MusicFx.note(var1, var1.level, var5);
                }
            }
        }
    }

    private static void launch(Minecraft var0, ClientLevel var1, int var2) {
        if (var2 < 0) {
            MusicFx.lightning(var0, var1);
        } else {
            Camera var3 = var0.gameRenderer.mainCamera();
            Vec3 var4 = var3.position();
            Vec3 var5 = MusicFx.ahead(var4, 1.8);
            float var6 = MusicFx.effectYaw(var3);
            Vec3 var7 = SkyText.forward(var6);
            Vec3 var8 = SkyText.right(var6);
            Vec3 var9 = null;
            Vec3 var10 = null;
            for (int var11 = 0; var11 < 6; ++var11) {
                double var12 = 26.0 + RNG.nextDouble() * 18.0;
                double var14 = (RNG.nextDouble() * 2.0 - 1.0) * 22.0;
                Vec3 var16 = var5.add(var7.scale(var12)).add(var8.scale(var14)).add(0.0, -6.0, 0.0);
                Vec3 var17 = var16.add((RNG.nextDouble() - 0.5) * 4.0, 24.0 + RNG.nextDouble() * 10.0 + (double)var2 * 4.0, (RNG.nextDouble() - 0.5) * 4.0);
                var9 = var16;
                var10 = var17;
                if (Visibility.clear((Level)var1, var4, var17, (Entity)var0.player)) break;
            }
            int var18 = 22 + RNG.nextInt(6);
            Vec3 var19 = var10.subtract(var9).scale(1.0 / (double)var18);
            rockets.add(new Rocket(var9, var19, var18, MusicFx.explosion(var2)));
            var1.playLocalSound(var9.x, var9.y, var9.z, SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.AMBIENT, 3.0f, 1.0f, false);
        }
    }

    private static void updateRockets(ClientLevel var0) {
        Iterator<Rocket> var1 = rockets.iterator();
        while (var1.hasNext()) {
            Rocket var2 = var1.next();
            ++var2.ticks;
            var2.pos = var2.pos.add(var2.vel);
            var0.addParticle((ParticleOptions)ParticleTypes.FIREWORK, var2.pos.x, var2.pos.y, var2.pos.z, RNG.nextGaussian() * 0.02, -var2.vel.y * 0.15, RNG.nextGaussian() * 0.02);
            if (var2.ticks < var2.life) continue;
            var0.createFireworks(var2.pos.x, var2.pos.y, var2.pos.z, 0.0, 0.0, 0.0, var2.boom);
            var1.remove();
        }
    }

    private static List<FireworkExplosion> explosion(int var0) {
        boolean var9;
        boolean var8;
        FireworkExplosion.Shape var7;
        LyricsConfig var1 = SkyLyrics.cfg;
        GenreWeather.Weather var2 = GenreWeather.weatherFor(GenreWeather.genre(MediaWatcher.track()));
        int var3 = CoverTexture.dominantColor();
        int var4 = LyricsConfig.rgb(var1.accentColor, 10980346);
        IntArrayList var5 = new IntArrayList();
        IntArrayList var6 = new IntArrayList();
        if (var2 == GenreWeather.Weather.THUNDER) {
            var7 = RNG.nextInt(3) == 0 ? FireworkExplosion.Shape.BURST : FireworkExplosion.Shape.LARGE_BALL;
            var5.add(var3 >= 0 ? var3 : 16726832);
            var5.add(16749824);
            var5.add(var4);
            var6.add(0xFFFFFF);
            var8 = true;
            var9 = false;
        } else if (var2 == GenreWeather.Weather.RAIN) {
            var7 = FireworkExplosion.Shape.STAR;
            var5.add(var3 >= 0 ? var3 : 5949690);
            var5.add(12573183);
            var5.add(var4);
            var6.add(8365823);
            var8 = false;
            var9 = true;
        } else {
            var7 = RNG.nextBoolean() ? FireworkExplosion.Shape.BURST : FireworkExplosion.Shape.SMALL_BALL;
            var5.add(var3 >= 0 ? var3 : 16766474);
            var5.add(var4);
            var5.add(1947988);
            var5.add(16723285);
            var6.add(0xFFFFFF);
            var8 = RNG.nextBoolean();
            var9 = RNG.nextInt(3) == 0;
        }
        ArrayList<FireworkExplosion> var10 = new ArrayList<FireworkExplosion>();
        var10.add(new FireworkExplosion(var7, (IntList)var5, (IntList)var6, var8, var9));
        if (var0 > 1) {
            var10.add(new FireworkExplosion(FireworkExplosion.Shape.LARGE_BALL, (IntList)var5, (IntList)var6, true, true));
        }
        return var10;
    }

    private static void lightning(Minecraft var0, ClientLevel var1) {
        Camera var2 = var0.gameRenderer.mainCamera();
        Vec3 var3 = MusicFx.ahead(var2.position(), 1.0);
        float var4 = MusicFx.effectYaw(var2) + (RNG.nextFloat() * 2.0f - 1.0f) * 35.0f;
        Vec3 var5 = SkyText.forward(var4);
        double var6 = 35.0 + RNG.nextDouble() * 25.0;
        double var8 = var3.x + var5.x * var6;
        double var10 = var3.z + var5.z * var6;
        int var12 = var1.getHeight(Heightmap.Types.MOTION_BLOCKING, (int)Math.floor(var8), (int)Math.floor(var10));
        if (var12 <= var1.getMinY() + 1) {
            var12 = (int)Math.floor(var3.y);
        }
        LightningBolt var13 = new LightningBolt(EntityTypes.LIGHTNING_BOLT, (Level)var1);
        var13.setVisualOnly(true);
        var13.setId(nextBoltId--);
        var13.snapTo(var8, (double)var12, var10);
        var1.addEntity((Entity)var13);
    }

    private static void pillars(final Minecraft var0, ClientLevel var1) {
        Vec3 var2 = var0.player.getPosition(1.0f);
        int var3 = 6;
        double var4 = 6.0;
        final double var6 = RNG.nextDouble() * Math.PI;
        var1.playLocalSound(var2.x, var2.y, var2.z, SoundEvents.BEACON_ACTIVATE, SoundSource.AMBIENT, 1.2f, 1.4f, false);
        jobs.add(new Job(50){

            @Override
            void tick(ClientLevel var1) {
                if (var0.player != null) {
                    Vec3 var2x = MusicFx.ahead(var0.player.getPosition(1.0f), 0.4);
                    Vec3 var3x = SkyText.velocity().scale(0.05);
                    for (int var4x = 0; var4x < 6; ++var4x) {
                        double var5 = var6 + (double)var4x * Math.PI * 2.0 / 6.0 + (double)this.age * 0.02;
                        double var7 = var2x.x + Math.cos(var5) * 6.0;
                        double var9 = var2x.z + Math.sin(var5) * 6.0;
                        for (int var11 = 0; var11 < 2; ++var11) {
                            var1.addParticle((ParticleOptions)ParticleTypes.END_ROD, var7 + RNG.nextGaussian() * 0.08, var2x.y + RNG.nextDouble() * 1.5, var9 + RNG.nextGaussian() * 0.08, var3x.x, 0.35 + RNG.nextDouble() * 0.2, var3x.z);
                        }
                    }
                }
            }
        });
    }

    private static void note(Minecraft var0, ClientLevel var1, int var2) {
        Camera var3 = var0.gameRenderer.mainCamera();
        Vec3 var4 = MusicFx.ahead(var0.player.getPosition(1.0f), 0.5);
        float var5 = var3.yRot() + (float)(var2 % 2 == 0 ? -1 : 1) * (35.0f + RNG.nextFloat() * 25.0f);
        Vec3 var6 = SkyText.forward(var5);
        double var7 = 1.4 + RNG.nextDouble() * 0.8;
        double var9 = var4.x + var6.x * var7;
        double var11 = var4.y + 1.2 + RNG.nextDouble() * 0.9;
        double var13 = var4.z + var6.z * var7;
        var1.addParticle((ParticleOptions)ParticleTypes.NOTE, var9, var11, var13, (double)(var2 * 3 % 24) / 24.0, 0.0, 0.0);
    }

    private static void stars(Minecraft var0, ClientLevel var1, long var2) {
        if (var1.dimension() == Level.OVERWORLD && !(SkyTone.skyLuminance(var1, 1.0f) > 0.22f)) {
            if (nextStarMs == 0L) {
                nextStarMs = var2 + 8000L + (long)RNG.nextInt(12000);
            } else if (var2 >= nextStarMs) {
                nextStarMs = var2 + 18000L + (long)RNG.nextInt(22000);
                MusicFx.shootingStar(var0, var1);
            }
        }
    }

    private static void shootingStar(Minecraft var0, ClientLevel var1) {
        Camera var2 = var0.gameRenderer.mainCamera();
        Vec3 var3 = MusicFx.ahead(var2.position(), 0.8);
        double var4 = (double)MusicFx.effectYaw(var2) + (RNG.nextDouble() * 2.0 - 1.0) * 45.0;
        double var6 = 28.0 + RNG.nextDouble() * 20.0;
        final Vec3 var8 = var3.add(Visibility.dir(var4, var6).scale(32.0));
        Vec3 var9 = SkyText.right(var2.yRot()).scale(RNG.nextBoolean() ? 1.0 : -1.0);
        final Vec3 var10 = var9.add(0.0, -0.35, 0.0).normalize().scale(1.5);
        var1.playLocalSound(var8.x, var8.y, var8.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.AMBIENT, 1.5f, 1.6f + RNG.nextFloat() * 0.3f, false);
        jobs.add(new Job(16){
            Vec3 head;
            {
                super(var1);
                this.head = var8;
            }

            @Override
            void tick(ClientLevel var1) {
                Vec3 var2x = this.head.add(var10).add(SkyText.velocity().scale(0.05));
                for (int var3x = 0; var3x < 5; ++var3x) {
                    Vec3 var4x = this.head.add(var10.scale((double)var3x / 5.0));
                    var1.addParticle((ParticleOptions)ParticleTypes.FIREWORK, var4x.x, var4x.y, var4x.z, 0.0, 0.0, 0.0);
                }
                var1.addParticle((ParticleOptions)ParticleTypes.END_ROD, var2x.x, var2x.y, var2x.z, var10.x * 0.05, var10.y * 0.05, var10.z * 0.05);
                this.head = var2x;
            }
        });
    }

    static {
        nextBoltId = -2000000;
    }

    private static abstract class Job {
        int age;
        final int life;

        Job(int var1) {
            this.life = var1;
        }

        abstract void tick(ClientLevel var1);
    }

    private static final class Rocket {
        Vec3 pos;
        final Vec3 vel;
        int ticks;
        final int life;
        final List<FireworkExplosion> boom;

        Rocket(Vec3 var1, Vec3 var2, int var3, List<FireworkExplosion> var4) {
            this.pos = var1;
            this.vel = var2;
            this.life = var3;
            this.boom = var4;
        }
    }
}

