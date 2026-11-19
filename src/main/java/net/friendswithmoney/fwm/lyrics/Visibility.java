/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.BlockGetter
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
import java.util.Arrays;
import java.util.HashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class Visibility {
    private static final HashMap<Long, double[]> RAYS = new HashMap();
    private static Level rayWorld;
    private static long rayCleanAt;
    public static int raysCast;
    public static int raysCached;

    private Visibility() {
    }

    private static long key(Vec3 a, Vec3 b) {
        long[] parts;
        long h = 1469598103934665603L;
        for (long p : parts = new long[]{Math.round(a.x * 2.0), Math.round(a.y * 2.0), Math.round(a.z * 2.0), Math.round(b.x * 2.0), Math.round(b.y * 2.0), Math.round(b.z * 2.0)}) {
            h = (h ^ p) * 1099511628211L;
        }
        return h;
    }

    static double ray(Level world, Vec3 from, Vec3 to, Entity entity) {
        long k;
        double[] hit;
        long now = System.currentTimeMillis();
        if (world != rayWorld || RAYS.size() > 4096) {
            RAYS.clear();
            rayWorld = world;
        }
        if (now - rayCleanAt > 1000L) {
            rayCleanAt = now;
            RAYS.values().removeIf(v -> now - (long)v[1] > 250L);
        }
        if ((hit = RAYS.get(k = Visibility.key(from, to))) != null && now - (long)hit[1] <= 250L) {
            ++raysCached;
            return hit[0];
        }
        ++raysCast;
        ClipContext.Fluid fluid = Visibility.cameraInFluid(world, from) ? ClipContext.Fluid.NONE : ClipContext.Fluid.ANY;
        BlockHitResult r = world.clip(new ClipContext(from, to, ClipContext.Block.VISUAL, fluid, entity));
        double d = r != null && r.getType() != HitResult.Type.MISS ? r.getLocation().distanceTo(from) : -1.0;
        RAYS.put(k, new double[]{d, now});
        return d;
    }

    private static boolean cameraInFluid(Level var0, Vec3 var1) {
        return !var0.getFluidState(BlockPos.containing((Position)var1)).isEmpty();
    }

    public static double hitDistance(Level var0, Vec3 var1, Vec3 var2, Entity var3) {
        return Visibility.ray(var0, var1, var2, var3);
    }

    public static boolean clear(Level var0, Vec3 var1, Vec3 var2, Entity var3) {
        return Visibility.ray(var0, var1, var2, var3) < 0.0;
    }

    public static double freeDistance(Level var0, Vec3 var1, Vec3 var2, double var3, Entity var5) {
        Vec3 var6 = var1.add(var2.scale(var3));
        double var8 = Visibility.ray(var0, var1, var6, var5);
        return var8 >= 0.0 ? var8 : var3;
    }

    public static boolean visible(Level var0, Vec3 var1, Vec3 var2, double var3, double var5, Entity var7) {
        return Visibility.visible(var0, var1, var2, var3, var5, var7, true);
    }

    public static boolean visible(Level var0, Vec3 var1, Vec3 var2, double var3, double var5, Entity var7, boolean var8) {
        if (Visibility.intersects(var0, var1, var2, var3, var5)) {
            return false;
        }
        Vec3 var9 = var2.add(0.0, -var5, 0.0);
        if (!var0.getFluidState(BlockPos.containing((Position)var2)).isEmpty() || !var0.getFluidState(BlockPos.containing((Position)var9)).isEmpty()) {
            return false;
        }
        if (!Visibility.clear(var0, var1, var2, var7) || !Visibility.clear(var0, var1, var9, var7)) {
            return false;
        }
        if (var3 < 0.6) {
            return true;
        }
        Vec3[] var10 = Visibility.sidePoints(var1, var2, var3, var5);
        if (var10 == null) {
            return true;
        }
        boolean var11 = Visibility.clear(var0, var1, var10[0], var7);
        boolean var12 = Visibility.clear(var0, var1, var10[1], var7);
        if (var8 && var11 && var12) {
            return Visibility.fits(var0, var1, var2, var3, var5, var7);
        }
        return var8 ? var11 && var12 : var11 || var12;
    }

    public static boolean intersects(Level var0, Vec3 var1, Vec3 var2, double var3, double var5) {
        Vec3 var7 = var2.subtract(var1);
        Vec3 var8 = new Vec3(-var7.z, 0.0, var7.x);
        double var9 = var8.length();
        Vec3 var11 = var9 < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : var8.scale(1.0 / var9);
        int var12 = Math.max(2, Math.min(32, (int)Math.ceil(var3 * 2.0 / 0.8)));
        int var13 = Math.max(2, Math.min(8, (int)Math.ceil(var5 * 2.0 / 0.8)));
        for (int var14 = 0; var14 <= var12; ++var14) {
            double var15 = -var3 + 2.0 * var3 * (double)var14 / (double)var12;
            for (int var17 = 0; var17 <= var13; ++var17) {
                double var18 = -var5 + 2.0 * var5 * (double)var17 / (double)var13;
                Vec3 var20 = var2.add(var11.scale(var15)).add(0.0, var18, 0.0);
                BlockPos var21 = BlockPos.containing((Position)var20);
                if (var0.getBlockState(var21).getCollisionShape((BlockGetter)var0, var21).isEmpty() && var0.getFluidState(var21).isEmpty()) continue;
                return true;
            }
        }
        return false;
    }

    public static boolean fits(Level var0, Vec3 var1, Vec3 var2, double var3, double var5, Entity var7) {
        Vec3[] var14;
        if (Visibility.intersects(var0, var1, var2, var3, var5)) {
            return false;
        }
        Vec3 var8 = var2.subtract(var1);
        Vec3 var9 = new Vec3(-var8.z, 0.0, var8.x);
        double var10 = var9.length();
        Vec3 var12 = var10 < 1.0E-4 ? new Vec3(var3, 0.0, 0.0) : var9.scale(var3 / var10);
        Vec3 var13 = new Vec3(0.0, var5, 0.0);
        for (Vec3 var18 : var14 = new Vec3[]{var2.add(var12).add(var13), var2.subtract(var12).add(var13), var2.add(var12).subtract(var13), var2.subtract(var12).subtract(var13)}) {
            if (var0.getBlockState(BlockPos.containing((Position)var18)).getCollisionShape((BlockGetter)var0, BlockPos.containing((Position)var18)).isEmpty() && var0.getFluidState(BlockPos.containing((Position)var18)).isEmpty() && Visibility.clear(var0, var1, var18, var7)) continue;
            return false;
        }
        return true;
    }

    private static Vec3[] sidePoints(Vec3 var0, Vec3 var1, double var2, double var4) {
        Vec3 var6 = var1.subtract(var0);
        Vec3 var7 = new Vec3(-var6.z, 0.0, var6.x);
        double var8 = var7.length();
        if (var8 < 1.0E-4) {
            return null;
        }
        var7 = var7.scale(var2 * 0.75 / var8);
        Vec3 var10 = var1.add(0.0, -var4 * 0.5, 0.0);
        return new Vec3[]{var10.add(var7), var10.subtract(var7)};
    }

    public static int blocked(Level var0, Vec3 var1, Vec3 var2, double var3, double var5, Entity var7) {
        Vec3 var8 = var2.add(0.0, -var5, 0.0);
        if (var0.getFluidState(BlockPos.containing((Position)var2)).isEmpty() && var0.getFluidState(BlockPos.containing((Position)var8)).isEmpty()) {
            Vec3[] var10;
            int var9 = 0;
            if (!Visibility.clear(var0, var1, var2, var7)) {
                ++var9;
            }
            if (!Visibility.clear(var0, var1, var8, var7)) {
                ++var9;
            }
            if (!Visibility.clear(var0, var1, var2.add(0.0, var5 * 0.8, 0.0), var7)) {
                ++var9;
            }
            if ((var10 = Visibility.sidePoints(var1, var2, var3, var5)) != null) {
                if (!Visibility.clear(var0, var1, var10[0], var7)) {
                    ++var9;
                }
                if (!Visibility.clear(var0, var1, var10[1], var7)) {
                    ++var9;
                }
            }
            return var9;
        }
        return 5;
    }

    static boolean visibleFromAll(Level var0, Vec3[] var1, Vec3 var2, double var3, double var5, Entity var7, boolean var8) {
        if (!Visibility.visible(var0, var1[0], var2, var3, var5, var7, var8)) {
            return false;
        }
        Vec3 var9 = var2.add(0.0, -var5, 0.0);
        for (int var10 = 1; var10 < var1.length; ++var10) {
            if (Visibility.clear(var0, var1[var10], var2, var7) && Visibility.clear(var0, var1[var10], var9, var7)) continue;
            return false;
        }
        return true;
    }

    static boolean skyBehind(Level var0, Vec3 var1, Vec3 var2, double var3, Entity var5) {
        Vec3 var6 = var2.subtract(var1);
        double var7 = var6.length();
        if (var7 < 0.001) {
            return false;
        }
        Vec3 var9 = var6.scale(1.0 / var7);
        Vec3 var10 = var2.add(0.0, -var3, 0.0);
        return Visibility.clear(var0, var2, var2.add(var9.scale(160.0)), var5) && Visibility.clear(var0, var10, var10.add(var9.scale(160.0)), var5);
    }

    static Vec3 dir(double var0, double var2) {
        double var4 = Math.toRadians(var0);
        double var6 = Math.toRadians(var2);
        return new Vec3(-Math.sin(var4) * Math.cos(var6), Math.sin(var6), Math.cos(var4) * Math.cos(var6));
    }

    public static Spot find(Level var0, Vec3[] var1, double var2, double var4, double var6, double var8, double var10, double var12, double var14, Entity var16) {
        return Visibility.find(var0, var1, var2, var4, var6, var8, var10, var12, var14, var16, null);
    }

    public static Spot find(Level var0, Vec3[] var1, double var2, double var4, double var6, double var8, double var10, double var12, double var14, Entity var16, Frame var17) {
        return Visibility.find(var0, var1, var2, var4, var6, var8, var10, var12, var14, var16, var17, null);
    }

    public static Spot find(Level var0, Vec3[] var1, double var2, double var4, double var6, double var8, double var10, double var12, double var14, Entity var16, Frame var17, Filter var18) {
        Spot var55;
        Vec3 var19 = var1[0];
        double var20 = var2 - var14;
        double[][] var22 = new double[][]{{var14, var4, 1.0}, {var14, var4 + 8.0, 1.0}, {var14, var4 + 16.0, 1.0}, {-var14, var4, 1.0}, {-var14, var4 + 10.0, 1.0}, {var14 * 0.4, var4 + 12.0, 1.0}, {var14, var4 + 26.0, 1.0}, {-var14, var4 + 22.0, 1.0}, {var14 * 1.6, var4 + 6.0, 1.0}, {-var14 * 1.6, var4 + 6.0, 1.0}, {0.0, var4 + 34.0, 1.0}, {var14, var4, 0.7}, {var14, var4 + 15.0, 0.7}, {-var14, var4 + 10.0, 0.7}, {var14, var4 + 10.0, 0.5}, {0.0, var4 + 25.0, 0.5}, {var14, var4 + 48.0, 1.0}, {var14 * 2.0, var4, 1.0}, {-var14 * 2.0, var4, 1.0}, {var14 * 2.0, var4 + 10.0, 1.0}, {-var14 * 2.0, var4 + 10.0, 1.0}};
        ArrayList var23 = new ArrayList(Arrays.asList(var22));
        if (var17 != null) {
            double[] var27;
            double var24 = var17.camPitchUp();
            double[] var26 = new double[]{var14, -var14, 0.0, var17.maxYaw() * 0.45, -var17.maxYaw() * 0.45, var17.maxYaw() * 0.75, -var17.maxYaw() * 0.75};
            for (double var31 : var27 = new double[]{0.25, 0.5, 0.75, 0.0, -0.25}) {
                for (double var36 : var26) {
                    var23.add(new double[]{var36, var24 + var17.maxPitch() * var31, 1.0});
                }
            }
        }
        for (double[] var25 : var23) {
            double var101;
            double var32;
            double var59 = Math.max(-10.0, Math.min(78.0, var25[1]));
            double var70 = Math.max(var8, var6 * var25[2]);
            double var82 = var70 / Math.max(1.0, var6);
            if (var17 != null && !var17.contains(var20 + var25[0], var59, var32 = Math.toDegrees(Math.atan2(var10 * var82, var70)), var101 = Math.toDegrees(Math.atan2(var12 * var82, var70)))) continue;
            Vec3 var93 = var19.add(Visibility.dir(var20 + var25[0], var59).scale(var70));
            if (var18 != null && !var18.ok(var93, var10 * var82, var12 * var82) || !Visibility.skyBehind(var0, var19, var93, var12 * var82, var16) || !Visibility.visibleFromAll(var0, var1, var93, var10 * var82, var12 * var82, var16, true)) continue;
            return new Spot(var93, var70, true);
        }
        for (int var50 = 0; var50 < 1; ++var50) {
            boolean var52 = true;
            for (double[] var78 : var22) {
                double var38;
                double var107;
                double var83 = Math.max(-10.0, Math.min(78.0, var78[1]));
                double var94 = Math.max(var8, var6 * var78[2]);
                double var102 = var94 / Math.max(1.0, var6);
                if (var17 != null && !var17.contains(var20 + var78[0], var83, var107 = Math.toDegrees(Math.atan2(var10 * var102, var94)), var38 = Math.toDegrees(Math.atan2(var12 * var102, var94)))) continue;
                Vec3 var108 = var19.add(Visibility.dir(var20 + var78[0], var83).scale(var94));
                if (var18 != null && !var18.ok(var108, var10 * var102, var12 * var102) || !Visibility.visibleFromAll(var0, var1, var108, var10 * var102, var12 * var102, var16, var52)) continue;
                return new Spot(var108, var94, true);
            }
        }
        double[][] var51 = new double[][]{{var14, var4}, {0.0, var4}, {-var14, var4}, {var14 * 0.5, var4 + 8.0}, {-var14 * 0.5, var4 + 8.0}};
        for (double var72 : new double[]{0.7, 0.5, 0.35, 0.25, 0.17}) {
            for (double var97 : new double[]{1.0, 0.75, 0.55}) {
                double var104 = Math.max(2.5, var6 * var72);
                double var37 = var104 / Math.max(1.0, var6) * var97;
                for (double[] var42 : var51) {
                    double var47;
                    double var45;
                    double var43 = Math.max(-10.0, Math.min(78.0, var42[1]));
                    if (var17 != null && !var17.contains(var20 + var42[0], var43, var45 = Math.toDegrees(Math.atan2(var10 * var37, var104)), var47 = Math.toDegrees(Math.atan2(var12 * var37, var104)))) continue;
                    Vec3 var117 = var19.add(Visibility.dir(var20 + var42[0], var43).scale(var104));
                    if (var18 != null && !var18.ok(var117, var10 * var37, var12 * var37) || !var0.getFluidState(BlockPos.containing((Position)var117)).isEmpty() || !Visibility.fits(var0, var19, var117, var10 * var37, var12 * var37, var16) || !Visibility.clear(var0, var19, var117, var16)) continue;
                    return new Spot(var117, var104 * var97, true);
                }
            }
        }
        for (int var54 = 0; var54 < 1; ++var54) {
            boolean var62 = false;
            for (double[] var85 : var22) {
                double var113;
                double var110;
                double var89 = Math.max(-10.0, Math.min(78.0, var85[1]));
                double var98 = Math.max(var8, var6 * var85[2]);
                double var105 = var98 / Math.max(1.0, var6);
                if (var17 != null && !var17.contains(var20 + var85[0], var89, var110 = Math.toDegrees(Math.atan2(var10 * var105, var98)), var113 = Math.toDegrees(Math.atan2(var12 * var105, var98)))) continue;
                Vec3 var111 = var19.add(Visibility.dir(var20 + var85[0], var89).scale(var98));
                if (var18 != null && !var18.ok(var111, var10 * var105, var12 * var105) || !Visibility.visibleFromAll(var0, var1, var111, var10 * var105, var12 * var105, var16, var62)) continue;
                return new Spot(var111, var98, true);
            }
        }
        if (var1.length > 1 && (var55 = Visibility.find(var0, new Vec3[]{var19}, var2, var4, var6, var8, var10, var12, var14, var16, var17, var18)).clear()) {
            return var55;
        }
        if (var18 != null) {
            for (int var56 = 0; var56 < 2; ++var56) {
                for (double var80 : new double[]{1.0, 0.7, 0.5}) {
                    for (double[] var96 : var23) {
                        Vec3 var115;
                        double var116;
                        double var114;
                        double var99 = Math.max(-10.0, Math.min(78.0, var96[1]));
                        double var106 = Math.max(var8, var6 * var96[2] * var80);
                        double var112 = var106 / Math.max(1.0, var6);
                        if (var17 != null && !var17.contains(var20 + var96[0], var99, var114 = Math.toDegrees(Math.atan2(var10 * var112, var106)), var116 = Math.toDegrees(Math.atan2(var12 * var112, var106))) || !var0.getFluidState(BlockPos.containing((Position)(var115 = var19.add(Visibility.dir(var20 + var96[0], var99).scale(var106))))).isEmpty() || !var18.ok(var115, var10 * var112, var12 * var112) || var56 == 0 && (!Visibility.clear(var0, var19, var115, var16) || Visibility.intersects(var0, var19, var115, var10 * var112, var12 * var112))) continue;
                        return new Spot(var115, var106, false);
                    }
                }
            }
        }
        if (var17 == null) {
            double var58 = -1.0;
            Vec3 var69 = null;
            for (double var92 : new double[]{20.0, 35.0, 50.0, 10.0}) {
                Vec3 var100 = Visibility.dir(var2, Math.min(78.0, Math.max(5.0, var4) + var92));
                double var103 = Visibility.freeDistance(var0, var19, var100, var6, var16);
                double var109 = Math.max(2.5, var103 - 1.2 - var12);
                if (!(var109 > var58)) continue;
                var58 = var109;
                var69 = var19.add(var100.scale(var109));
            }
            return new Spot(var69, var58, false);
        }
        Vec3 var57 = Visibility.dir(var2, var4);
        double var64 = Visibility.freeDistance(var0, var19, var57, var6, var16);
        double var75 = Math.min(var6, var64 * 0.6);
        var75 = Math.max(2.5, var75);
        for (int var86 = 0; var86 < 4 && var75 > 3.0; var75 *= 0.7, ++var86) {
            double var91 = var75 / Math.max(1.0, var6);
            if (Visibility.visible(var0, var19, var19.add(var57.scale(var75)), var10 * var91, var12 * var91, var16, true)) break;
        }
        return new Spot(var19.add(var57.scale(var75)), var75, false);
    }

    public record Frame(double camYaw, double camPitchUp, double maxYaw, double maxPitch) {
        boolean contains(double var1, double var3, double var5, double var7) {
            double var9 = Mth.wrapDegrees((double)(var1 - this.camYaw));
            return Math.abs(var9) + var5 <= this.maxYaw && Math.abs(var3 - this.camPitchUp) + var7 <= this.maxPitch;
        }
    }

    public record Spot(Vec3 anchor, double distance, boolean clear) {
    }

    public static interface Filter {
        public boolean ok(Vec3 var1, double var2, double var4);
    }
}

