/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.util.Mth
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.decoration.ArmorStand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package net.friendswithmoney.fwm.lyrics;

import java.util.ArrayList;
import java.util.Random;
import net.friendswithmoney.fwm.lyrics.AfkCinema;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class CinemaShots {
    private static final Shot[] ALL = new Shot[]{Shot.player("Vorne", -6.0f, 6.0f, 3.4f, 2.6f, 1.65f, 1.55f, 1.5f), Shot.player("Seite", 80.0f, 100.0f, 3.0f, 3.0f, 1.1f, 1.3f, 1.15f), new Shot("Landschaft", Kind.VISTA, -12.0f, 12.0f, 0.0f, 2.0f, 1.6f, 2.0f, 0.0f), new Shot("\u00dcber die Schulter", Kind.MOB_SHOULDER, 0.0f, 0.0f, 2.7f, 2.3f, 0.35f, 0.3f, 0.0f), Shot.player("Hinten", 170.0f, 190.0f, 3.2f, 3.9f, 2.4f, 1.7f, 1.2f), Shot.player("Kran", 25.0f, 40.0f, 2.6f, 4.8f, 0.5f, 4.5f, 1.2f), new Shot("Himmelsblick", Kind.SKY, -10.0f, 10.0f, 2.2f, 2.4f, 0.9f, 1.0f, 0.0f), Shot.player("Unten", 280.0f, 260.0f, 2.7f, 2.9f, 0.25f, 0.4f, 1.4f), new Shot("Begegnung", Kind.MOB_CAM, -0.7f, 0.7f, 0.0f, 0.0f, 0.7f, 0.9f, 0.0f), new Shot("Drohne", Kind.DRONE, -8.0f, 8.0f, 8.0f, 11.0f, 6.0f, 11.0f, 0.0f), Shot.player("Held", -15.0f, 15.0f, 2.4f, 2.6f, 0.15f, 0.25f, 1.75f), new Shot("Vorbeiflug", Kind.FLYPAST, 0.0f, 0.0f, 4.5f, 4.5f, 1.5f, 1.7f, 0.0f), Shot.player("Orbit", 200.0f, 260.0f, 5.2f, 5.0f, 2.3f, 2.0f, 1.0f), Shot.player("Spirale", 0.0f, 90.0f, 6.0f, 3.8f, 4.5f, 1.6f, 1.3f), Shot.player("Heranfahrt", -3.0f, 3.0f, 6.0f, 2.6f, 1.7f, 1.62f, 1.6f), Shot.player("R\u00fcckzug", 20.0f, 30.0f, 2.4f, 7.0f, 1.6f, 4.5f, 1.4f), Shot.player("Bodenkreis", 60.0f, 0.0f, 3.0f, 3.2f, 0.2f, 0.25f, 1.0f), new Shot("Vogelperspektive", Kind.TOP, 0.0f, 30.0f, 0.0f, 0.0f, 10.0f, 12.0f, 0.0f)};
    private static Shot[] shots = ALL;
    private static int lastShot = -1;
    private static double smoothDist = -1.0;
    private static float viewYaw;
    private static LivingEntity focus;
    private static Vec3 focusDir;
    private static Vec3 focusLook;
    private static Vec3 flightDir;

    public static int currentShot() {
        return lastShot;
    }

    private static long slot(long var0) {
        return var0 / CinemaShots.shotMs();
    }

    private static int shotIndex(long var0) {
        int var2 = shots.length;
        if (var2 <= 1) {
            return 0;
        }
        long var3 = var0 / (long)var2;
        int[] var5 = CinemaShots.order(var3);
        return var5[(int)(var0 % (long)var2)];
    }

    private static int[] order(long var0) {
        int[] var9;
        int var2 = shots.length;
        int[] var3 = new int[var2];
        int var4 = 0;
        while (var4 < var2) {
            var3[var4] = var4++;
        }
        Random var8 = new Random(AfkCinema.startMs() * 31L + var0 * 7919L);
        for (int var5 = var2 - 1; var5 > 0; --var5) {
            int var6 = var8.nextInt(var5 + 1);
            int var7 = var3[var5];
            var3[var5] = var3[var6];
            var3[var6] = var7;
        }
        if (var0 > 0L && var3[0] == (var9 = CinemaShots.order(var0 - 1L))[var2 - 1]) {
            int var10 = var3[0];
            var3[0] = var3[var2 - 1];
            var3[var2 - 1] = var10;
        }
        return var3;
    }

    public static void reset() {
        ArrayList<Shot> var0 = new ArrayList<Shot>();
        for (Shot var4 : ALL) {
            boolean var5;
            boolean bl = var5 = var4.kind == Kind.MOB_SHOULDER || var4.kind == Kind.MOB_CAM;
            if (var5) {
                if (!AfkCinema.cfg.mobShots) continue;
                var0.add(var4);
                continue;
            }
            if (var4.kind != Kind.PLAYER && !AfkCinema.cfg.environmentShots) continue;
            var0.add(var4);
        }
        shots = var0.toArray(new Shot[0]);
        lastShot = -1;
        smoothDist = -1.0;
        focus = null;
        focusDir = null;
        focusLook = null;
    }

    static long shotMs() {
        return (long)(AfkCinema.cfg.shotSeconds * 1000.0f);
    }

    private static float ease(float var0) {
        var0 = Mth.clamp((float)var0, (float)0.0f, (float)1.0f);
        return (float)(0.5 - 0.5 * Math.cos(Math.PI * (double)var0));
    }

    private static Vec3 dirOf(float var0) {
        double var1 = Math.toRadians(var0);
        return new Vec3(-Math.sin(var1), 0.0, Math.cos(var1));
    }

    private static double freeDist(Level var0, Vec3 var1, Vec3 var2, Entity var3) {
        BlockHitResult var4 = var0.clip(new ClipContext(var1, var2, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, var3));
        return var4.getType() == HitResult.Type.MISS ? var1.distanceTo(var2) : var4.getLocation().distanceTo(var1);
    }

    private static boolean sees(Level var0, Vec3 var1, Vec3 var2, Entity var3) {
        BlockHitResult var4 = var0.clip(new ClipContext(var1, var2, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, var3));
        return var4.getType() == HitResult.Type.MISS;
    }

    private static float bestViewYaw(Level var0, Entity var1, Vec3 var2) {
        Vec3 var3 = var2.add(0.0, 1.5, 0.0);
        float var4 = var1.getYRot();
        double var5 = -1.0;
        for (int var7 = 0; var7 < 24; ++var7) {
            double var12;
            float var8 = (float)var7 * 15.0f;
            Vec3 var9 = CinemaShots.dirOf(var8);
            double var10 = CinemaShots.freeDist(var0, var3, var3.add(var9.scale(64.0)).add(0.0, 3.0, 0.0), var1);
            double var14 = var10 + (var12 = CinemaShots.freeDist(var0, var3, var3.add(var9.scale(24.0)).add(0.0, -2.0, 0.0), var1)) * 0.5;
            if (!(var14 > var5 + 0.01)) continue;
            var5 = var14;
            var4 = var8;
        }
        return var4;
    }

    private static LivingEntity findFocus(Level var0, Entity var1, Vec3 var2) {
        LivingEntity var3 = null;
        double var4 = Double.MAX_VALUE;
        for (LivingEntity var7 : var0.getEntitiesOfClass(LivingEntity.class, var1.getBoundingBox().inflate(20.0), var1x -> var1x != var1 && var1x.isAlive() && !(var1x instanceof ArmorStand) && !(var1x instanceof Player) && !var1x.isInvisible() && !var1x.hasEffect(MobEffects.INVISIBILITY))) {
            Vec3 var10;
            double var8 = var7.distanceToSqr(var1);
            if (var8 < 9.0 || var8 > 256.0 || var8 >= var4 || !CinemaShots.sees(var0, var2, var10 = var7.getEyePosition(), var1)) continue;
            var4 = var8;
            var3 = var7;
        }
        return var3;
    }

    private static boolean focusOk(Entity var0) {
        return focus != null && focusDir != null && focusLook != null && focus.isAlive() && !focus.isRemoved() && focus.distanceToSqr(var0) < 484.0;
    }

    public static Pose compute(Entity var0, float var1) {
        long var2 = System.currentTimeMillis() - AfkCinema.startMs();
        return CinemaShots.pose(var0, var1, var2, false);
    }

    public static Vec3 previewCamera(Entity var0, long var1) {
        if (lastShot >= 0 && shots.length != 0) {
            long var3 = System.currentTimeMillis() - AfkCinema.startMs() + var1;
            return (int)CinemaShots.slot(var3) != lastShot ? null : CinemaShots.pose(var0, 1.0f, var3, true).pos();
        }
        return null;
    }

    public static Pose previewPose(Entity var0, long var1) {
        if (lastShot >= 0 && shots.length != 0) {
            long var3 = System.currentTimeMillis() - AfkCinema.startMs() + var1;
            return (int)CinemaShots.slot(var3) != lastShot ? null : CinemaShots.pose(var0, 1.0f, var3, true);
        }
        return null;
    }

    public static long remainingMs() {
        long var0 = System.currentTimeMillis() - AfkCinema.startMs();
        return CinemaShots.shotMs() - var0 % CinemaShots.shotMs();
    }

    private static Pose pose(Entity var0, float var1, long var2, boolean var4) {
        Vec3 var25;
        double var53;
        BlockHitResult var52;
        double var47;
        Vec3 var17;
        Vec3 var16;
        Vec3 var15;
        if (shots.length == 0) {
            CinemaShots.reset();
        }
        int var5 = (int)CinemaShots.slot(var2);
        float var6 = CinemaShots.ease((float)(var2 % CinemaShots.shotMs()) / (float)CinemaShots.shotMs());
        Shot var7 = shots[CinemaShots.shotIndex(var5)];
        ClientLevel var8 = Minecraft.getInstance().level;
        Vec3 var9 = var0.getPosition(var1);
        float var10 = var0.getBbHeight() / 1.8f;
        Vec3 var11 = var9.add(0.0, 1.62 * (double)var10, 0.0);
        if (!var4 && var5 != lastShot) {
            lastShot = var5;
            smoothDist = -1.0;
            focus = null;
            if (var8 != null) {
                if (var7.kind == Kind.VISTA || var7.kind == Kind.DRONE) {
                    viewYaw = CinemaShots.bestViewYaw((Level)var8, var0, var11);
                }
                if ((var7.kind == Kind.MOB_SHOULDER || var7.kind == Kind.MOB_CAM) && (focus = CinemaShots.findFocus((Level)var8, var0, var11)) != null) {
                    Vec3 var12 = focus.getPosition(var1).add(0.0, (double)focus.getEyeHeight(), 0.0);
                    Vec3 var13 = var12.subtract(var11);
                    Vec3 var14 = new Vec3(var13.x, 0.0, var13.z);
                    focusDir = var14.lengthSqr() < 1.0E-4 ? CinemaShots.dirOf(AfkCinema.baseYaw()) : var14.normalize();
                    focusLook = var12;
                }
            }
        }
        if (CinemaShots.flying(var0)) {
            return CinemaShots.flightPose(var0, var9, var10, var5, var6, var4, (Level)var8);
        }
        flightDir = null;
        Kind var31 = var7.kind;
        if (!(var31 != Kind.MOB_SHOULDER && var31 != Kind.MOB_CAM || CinemaShots.focusOk(var0))) {
            var31 = Kind.PLAYER;
            var7 = Shot.player("Orbit", 120.0f, 200.0f, 4.5f, 4.0f, 2.0f, 1.6f, 1.1f);
        }
        float var32 = 0.0f;
        float var33 = 0.0f;
        switch (var31.ordinal()) {
            case 1: {
                Vec3 var41 = CinemaShots.dirOf(viewYaw);
                var15 = var11;
                var16 = var11.add(0.0, (double)Mth.lerp((float)var6, (float)var7.camHFrom, (float)var7.camHTo), 0.0).add(var41.scale((double)Mth.lerp((float)var6, (float)var7.distFrom, (float)var7.distTo)));
                var17 = null;
                var32 = viewYaw + Mth.lerp((float)var6, (float)var7.angFrom, (float)var7.angTo);
                var33 = Mth.lerp((float)var6, (float)7.0f, (float)3.0f);
                break;
            }
            case 2: {
                float var40 = viewYaw + Mth.lerp((float)var6, (float)var7.angFrom, (float)var7.angTo);
                Vec3 var46 = CinemaShots.dirOf(var40).scale((double)(-Mth.lerp((float)var6, (float)var7.distFrom, (float)var7.distTo)));
                var15 = var11;
                var16 = var9.add(var46).add(0.0, (double)Mth.lerp((float)var6, (float)var7.camHFrom, (float)var7.camHTo), 0.0);
                var17 = var9.add(CinemaShots.dirOf(viewYaw).scale((double)Mth.lerp((float)var6, (float)6.0f, (float)14.0f)));
                break;
            }
            case 3: {
                float var39 = AfkCinema.baseYaw();
                var15 = var11;
                var16 = var11.add(CinemaShots.dirOf(var39).scale((double)(-Mth.lerp((float)var6, (float)var7.distFrom, (float)var7.distTo)))).add(0.0, (double)Mth.lerp((float)var6, (float)var7.camHFrom, (float)var7.camHTo) - 1.0, 0.0);
                var17 = null;
                var32 = var39 + Mth.lerp((float)var6, (float)var7.angFrom, (float)var7.angTo);
                var33 = Mth.lerp((float)var6, (float)-28.0f, (float)-38.0f);
                break;
            }
            case 4: {
                var15 = var11;
                var16 = var9.add(0.0, (double)Mth.lerp((float)var6, (float)var7.camHFrom, (float)var7.camHTo), 0.0).add(CinemaShots.dirOf(AfkCinema.baseYaw()).scale(-0.05));
                var17 = null;
                var32 = AfkCinema.baseYaw() + Mth.lerp((float)var6, (float)var7.angFrom, (float)var7.angTo);
                var33 = 89.0f;
                break;
            }
            case 5: {
                float var38 = AfkCinema.baseYaw();
                Vec3 var45 = CinemaShots.dirOf(var38);
                Vec3 var48 = new Vec3(-var45.z, 0.0, var45.x);
                var15 = var11;
                var16 = var9.add(var45.scale((double)var7.distFrom)).add(var48.scale((double)Mth.lerp((float)var6, (float)-5.0f, (float)5.0f))).add(0.0, (double)Mth.lerp((float)var6, (float)var7.camHFrom, (float)var7.camHTo), 0.0);
                var17 = var9.add(0.0, 1.2 * (double)var10, 0.0);
                break;
            }
            case 6: {
                if (!var4) {
                    Vec3 var36 = focus.getPosition(var1).add(0.0, (double)focus.getEyeHeight(), 0.0);
                    focusLook = focusLook.add(var36.subtract(focusLook).scale(0.06));
                }
                Vec3 var37 = focusDir;
                Vec3 var44 = new Vec3(-var37.z, 0.0, var37.x);
                var15 = var11;
                var16 = var11.subtract(var37.scale((double)Mth.lerp((float)var6, (float)var7.distFrom, (float)var7.distTo))).add(var44.scale(0.85)).add(0.0, (double)Mth.lerp((float)var6, (float)var7.camHFrom, (float)var7.camHTo), 0.0);
                var17 = focusLook;
                break;
            }
            case 7: {
                if (!var4) {
                    Vec3 var34 = focus.getPosition(var1).add(0.0, (double)focus.getEyeHeight(), 0.0);
                    focusLook = focusLook.add(var34.subtract(focusLook).scale(0.06));
                }
                Vec3 var35 = focusDir;
                Vec3 var43 = new Vec3(-var35.z, 0.0, var35.x);
                Vec3 var20 = var11.add(focusLook).scale(0.5).add(0.0, -0.3 * (double)var10, 0.0);
                double var49 = Math.sqrt(Math.pow(CinemaShots.focusLook.x - var11.x, 2.0) + Math.pow(CinemaShots.focusLook.z - var11.z, 2.0));
                double var51 = Math.max(3.5, var49 * 0.9 + 2.0);
                var15 = var20;
                var16 = var20.add(var43.scale(var51)).add(var35.scale((double)Mth.lerp((float)var6, (float)var7.angFrom, (float)var7.angTo))).add(0.0, (double)Mth.lerp((float)var6, (float)var7.camHFrom, (float)var7.camHTo), 0.0);
                var17 = var20;
                break;
            }
            default: {
                var15 = var9.add(0.0, (double)(var7.lookH * var10), 0.0);
                float var18 = AfkCinema.baseYaw() + Mth.lerp((float)var6, (float)var7.angFrom, (float)var7.angTo);
                double var19 = Math.toRadians(var18);
                double var21 = Mth.lerp((float)var6, (float)var7.distFrom, (float)var7.distTo);
                double var23 = Mth.lerp((float)var6, (float)var7.camHFrom, (float)var7.camHTo) * var10;
                var16 = new Vec3(var9.x - Math.sin(var19) * var21, var9.y + var23, var9.z + Math.cos(var19) * var21);
                var17 = var15;
            }
        }
        Vec3 var42 = var16.subtract(var15);
        double var50 = var47 = var42.length();
        if (var8 != null && var47 > 0.01 && (var52 = var8.clip(new ClipContext(var15, var16, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, var0))).getType() != HitResult.Type.MISS) {
            var50 = Math.max(0.3, var52.getLocation().distanceTo(var15) - 0.25);
        }
        if (var4) {
            var53 = var50;
        } else {
            smoothDist = !(smoothDist < 0.0) && !(var50 < smoothDist) ? (smoothDist += (var50 - smoothDist) * 0.06) : var50;
            var53 = smoothDist;
        }
        Vec3 vec3 = var25 = var47 > 0.01 ? var15.add(var42.scale(Math.min(var53, var47) / var47)) : var16;
        if (var17 == null) {
            return new Pose(var25, var32, var33);
        }
        Vec3 var26 = var17.subtract(var25);
        double var27 = Math.sqrt(var26.x * var26.x + var26.z * var26.z);
        float var29 = (float)(Mth.atan2((double)var26.z, (double)var26.x) * 180.0 / 3.1415927410125732) - 90.0f;
        float var30 = (float)(-(Mth.atan2((double)var26.y, (double)var27) * 180.0 / 3.1415927410125732));
        return new Pose(var25, var29, var30);
    }

    public static boolean flying(Entity var0) {
        LivingEntity var1;
        return var0 instanceof LivingEntity && (var1 = (LivingEntity)var0).isFallFlying() ? true : !var0.onGround() && var0.getDeltaMovement().length() > 0.7;
    }

    private static Pose flightPose(Entity var0, Vec3 var1, float var2, int var3, float var4, boolean var5, Level var6) {
        BlockHitResult var18;
        Vec3 var13;
        Vec3 var8;
        Vec3 var7 = var0.getDeltaMovement();
        Vec3 vec3 = var8 = var7.lengthSqr() > 1.0E-4 ? var7.normalize() : CinemaShots.dirOf(var0.getYRot());
        if (flightDir == null) {
            flightDir = var8;
        } else if (!var5) {
            flightDir = flightDir.add(var8.subtract(flightDir).scale(0.06)).normalize();
        }
        Vec3 var9 = flightDir;
        Vec3 var10 = new Vec3(0.0, 1.0, 0.0);
        Vec3 var11 = var9.cross(var10);
        var11 = var11.lengthSqr() < 1.0E-4 ? CinemaShots.dirOf(var0.getYRot() + 90.0f) : var11.normalize();
        Vec3 var12 = var1.add(0.0, 0.6 * (double)var2, 0.0);
        Vec3 var14 = switch (Math.floorMod(var3, 4)) {
            case 0 -> {
                var13 = var12.subtract(var9.scale(7.0)).add(0.0, 2.2, 0.0);
                yield var12.add(var9.scale(8.0));
            }
            case 1 -> {
                var13 = var12.add(var11.scale(6.0)).add(var9.scale(-1.5 + 3.0 * (double)var4)).add(0.0, 0.8, 0.0);
                yield var12.add(var9.scale(3.0));
            }
            case 2 -> {
                var13 = var12.subtract(var9.scale(13.0)).add(0.0, 6.0, 0.0);
                yield var12.add(var9.scale(14.0));
            }
            default -> {
                var13 = var12.subtract(var9.scale(4.5)).subtract(var11.scale(2.2)).add(0.0, 0.6, 0.0);
                yield var12.add(var9.scale(10.0)).add(0.0, 0.5, 0.0);
            }
        };
        Vec3 var15 = var13.subtract(var12);
        double var16 = var15.length();
        if (var6 != null && var16 > 0.01 && (var18 = var6.clip(new ClipContext(var12, var13, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, var0))).getType() != HitResult.Type.MISS) {
            var13 = var12.add(var15.scale(Math.max(0.3, var18.getLocation().distanceTo(var12) - 0.25) / var16));
        }
        Vec3 var24 = var14.subtract(var13);
        double var19 = Math.sqrt(var24.x * var24.x + var24.z * var24.z);
        float var21 = (float)(Mth.atan2((double)var24.z, (double)var24.x) * 180.0 / 3.1415927410125732) - 90.0f;
        float var22 = (float)(-(Mth.atan2((double)var24.y, (double)var19) * 180.0 / 3.1415927410125732));
        return new Pose(var13, var21, var22);
    }

    public static float fade() {
        long var0 = System.currentTimeMillis() - AfkCinema.startMs();
        long var2 = CinemaShots.shotMs();
        long var4 = var0 % var2;
        float var6 = 0.0f;
        if (var0 < 1200L) {
            var6 = 1.0f - (float)var0 / 1200.0f;
        } else if (var4 < 650L) {
            var6 = 1.0f - (float)var4 / 650.0f;
        }
        if (var2 - var4 < 650L) {
            var6 = Math.max(var6, 1.0f - (float)(var2 - var4) / 650.0f);
        }
        var6 = Mth.clamp((float)var6, (float)0.0f, (float)1.0f);
        return var6 * var6 * (3.0f - 2.0f * var6);
    }

    private record Shot(String name, Kind kind, float angFrom, float angTo, float distFrom, float distTo, float camHFrom, float camHTo, float lookH) {
        static Shot player(String var0, float var1, float var2, float var3, float var4, float var5, float var6, float var7) {
            return new Shot(var0, Kind.PLAYER, var1, var2, var3, var4, var5, var6, var7);
        }
    }

    private static enum Kind {
        PLAYER,
        VISTA,
        DRONE,
        SKY,
        TOP,
        FLYPAST,
        MOB_SHOULDER,
        MOB_CAM;

    }

    public record Pose(Vec3 pos, float yRot, float xRot) {
    }
}

