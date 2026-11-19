/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.fabricmc.fabric.api.event.player.AttackEntityCallback
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.core.particles.DustParticleOptions
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Style
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.decoration.ArmorStand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 */
package net.friendswithmoney.fwm.lyrics;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.friendswithmoney.fwm.lyrics.CinemaLyrics;
import net.friendswithmoney.fwm.lyrics.Lyrics;
import net.friendswithmoney.fwm.lyrics.LyricsConfig;
import net.friendswithmoney.fwm.lyrics.LyricsFetcher;
import net.friendswithmoney.fwm.lyrics.MediaWatcher;
import net.friendswithmoney.fwm.lyrics.Sink;
import net.friendswithmoney.fwm.lyrics.SkyText;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public final class PvpLyrics {
    private static final long COMBAT_MS = 9000L;
    private static LivingEntity target;
    private static long lastHitMs;
    private static int prevHurtTime;
    private static float fade;
    private static int side;
    private static Lyrics.Line shownLine;
    private static long lineBornNanos;
    private static Vec3 lastPos;
    private static float lastWidth;
    private static float lastHeight;
    private static long maceUntilNanos;
    private static long slamNanos;
    private static float slamStrength;
    private static boolean slamFxDone;
    private static float viewBlend;
    private static final double RANGE = 24.0;
    private static final double MACE_RANGE = 40.0;
    private static boolean targetSeen;
    private static int losTick;

    private PvpLyrics() {
    }

    public static void register() {
        AttackEntityCallback.EVENT.register((var0, var1, var2, var3, var4) -> {
            LivingEntity var5;
            if (var1.isClientSide() && var3 instanceof LivingEntity && PvpLyrics.valid(var5 = (LivingEntity)var3, var0)) {
                target = var5;
                lastHitMs = System.currentTimeMillis();
                if (var0.fallDistance > 1.5) {
                    slamNanos = System.nanoTime();
                    slamStrength = (float)Math.max(0.35, Math.min(1.0, var0.fallDistance / 15.0));
                    slamFxDone = false;
                    maceUntilNanos = System.nanoTime() + 3000000000L;
                }
            }
            return InteractionResult.PASS;
        });
    }

    private static boolean valid(LivingEntity var0, Player var1) {
        Player p;
        if (var0 == var1 || !var0.isAlive() || var0.isRemoved() || var0 instanceof ArmorStand) {
            return false;
        }
        if (!(var0 instanceof Player) || (p = (Player)var0).isSpectator()) {
            return false;
        }
        if (p.isInvisible() || p.hasEffect(MobEffects.INVISIBILITY)) {
            return false;
        }
        ClientPacketListener net = Minecraft.getInstance().getConnection();
        return net == null || net.getPlayerInfo(p.getUUID()) != null;
    }

    private static boolean lineOfSight(LocalPlayer me, LivingEntity t) {
        Vec3 eye = me.getEyePosition();
        for (double h : new double[]{(double)t.getBbHeight() * 0.9, (double)t.getBbHeight() * 0.5}) {
            Vec3 to = t.position().add(0.0, h, 0.0);
            BlockHitResult hit = me.level().clip(new ClipContext(eye, to, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, (Entity)me));
            if (hit != null && hit.getType() != HitResult.Type.MISS) continue;
            return true;
        }
        return false;
    }

    private static void drop() {
        target = null;
        targetSeen = false;
        fade = 0.0f;
        shownLine = null;
        lastPos = null;
    }

    public static void tick(Minecraft var0) {
        LocalPlayer var1 = var0.player;
        if (var1 != null && var0.level != null) {
            if (var1.hurtTime > prevHurtTime && var1.hurtTime > 0) {
                LivingEntity var2 = null;
                double var3 = Double.MAX_VALUE;
                for (LivingEntity var7 : var0.level.getEntitiesOfClass(LivingEntity.class, var1.getBoundingBox().inflate(6.0), var1x -> PvpLyrics.valid(var1x, (Player)var1))) {
                    double var8 = var7.distanceToSqr((Entity)var1);
                    if (!(var8 < var3)) continue;
                    var3 = var8;
                    var2 = var7;
                }
                if (var2 != null) {
                    if (target == null || target == var2 || !target.isAlive()) {
                        target = var2;
                    }
                    lastHitMs = System.currentTimeMillis();
                }
            }
            prevHurtTime = var1.hurtTime;
            if (target != null) {
                boolean var12;
                boolean var4;
                double var10 = var1.getDeltaMovement().y * 20.0;
                if (var1.isFallFlying() || var1.fallDistance > 3.0 || Math.abs(var10) > 12.0 || Math.abs(var1.getY() - target.getY()) > 6.0) {
                    maceUntilNanos = System.nanoTime() + 2500000000L;
                }
                double var11 = (var4 = PvpLyrics.mace()) ? 40.0 : 24.0;
                boolean bl = var12 = !target.isAlive() || target.isRemoved() || target.level() != var0.level || target.distanceToSqr((Entity)var1) > var11 * var11;
                if (!PvpLyrics.valid(target, (Player)var1)) {
                    PvpLyrics.drop();
                    return;
                }
                if (var12 || System.currentTimeMillis() - lastHitMs > (var4 ? 12000L : 8000L)) {
                    PvpLyrics.drop();
                    return;
                }
                if (++losTick >= 4 || !targetSeen) {
                    losTick = 0;
                    boolean seen = PvpLyrics.lineOfSight(var1, target);
                    if (!seen && targetSeen) {
                        fade = 0.0f;
                        shownLine = null;
                        lastPos = null;
                    }
                    targetSeen = seen;
                }
            }
        } else {
            target = null;
            prevHurtTime = 0;
        }
    }

    public static boolean active(LyricsConfig var0) {
        return var0.enabled && var0.pvpEdit && target != null && targetSeen && MediaWatcher.isPlaying() && MediaWatcher.track() != null && !CinemaLyrics.cinema();
    }

    public static boolean mace() {
        return System.nanoTime() < maceUntilNanos;
    }

    public static boolean fading() {
        return fade > 0.01f;
    }

    public static boolean inCombat() {
        return target != null;
    }

    static void render(Minecraft var0, Camera var1, PoseStack var2, Sink var3, LyricsConfig var4, long var5, long var7, float var9) {
        boolean var10 = PvpLyrics.active(var4);
        if ((fade += ((var10 ? 1.0f : 0.0f) - fade) * Math.min(1.0f, var9 * (var10 ? 7.0f : 4.0f))) < 0.01f) {
            shownLine = null;
        } else if (!(target == null || var0.player == null || PvpLyrics.valid(target, (Player)var0.player) && targetSeen)) {
            PvpLyrics.drop();
        } else if (target == null && lastPos != null && !var10) {
            fade = 0.0f;
            lastPos = null;
            shownLine = null;
        } else {
            int var13;
            Lyrics var12;
            MediaWatcher.Track var71;
            if (target != null) {
                float var11 = var0.getDeltaTracker().getGameTimeDeltaPartialTick(false);
                lastPos = target.getPosition(var11);
                lastWidth = target.getBbWidth();
                lastHeight = target.getBbHeight();
            }
            if (lastPos != null && (var71 = MediaWatcher.track()) != null && (var12 = LyricsFetcher.get(var71)) != null && var12.synced && !var12.lines.isEmpty() && (var13 = var12.indexAt(var5)) >= 0 && var5 < var12.endOf(var13, var71.durationMs()) && !var12.lines.get(var13).text().isBlank()) {
                float var73;
                float var72;
                Lyrics.Line var14 = var12.lines.get(var13);
                if (var14 != shownLine) {
                    shownLine = var14;
                    lineBornNanos = var7;
                    side = -side;
                }
                Font var15 = var0.font;
                String var16 = var4.fontStyle == null ? "default" : var4.fontStyle;
                boolean var17 = var16.equals("default") || var16.equals("uniform");
                Style var18 = var17 ? SkyText.style(var4).withBold(Boolean.valueOf(true)) : SkyText.style(var4);
                SkyText.Layout var19 = SkyText.layout(var15, var14.text(), var18);
                float var20 = SkyText.layoutHalfWidth(var15, var14.text(), var18);
                Vec3 var21 = var1.position();
                Vec3 var22 = lastPos.add(0.0, (double)lastHeight * 0.78, 0.0);
                double var23 = Math.max(1.0, var21.distanceTo(var22));
                boolean var25 = PvpLyrics.mace();
                float var26 = (float)(0.021 * Math.max(1.0, Math.min(var25 ? 18.0 : 3.5, var23 / 4.0)) * (double)var4.pvpScale);
                Vec3 var27 = SkyText.right(var1.yRot());
                double var28 = (double)lastWidth / 2.0 + 0.45 + (double)(var20 * var26);
                Vec3 var30 = var22.add(var27.scale((double)side * var28));
                double var31 = Math.toRadians(var1.yRot());
                double var33 = Math.toRadians(var1.xRot());
                Vec3 var35 = new Vec3(-Math.sin(var31) * Math.cos(var33), -Math.sin(var33), Math.cos(var31) * Math.cos(var33));
                Vec3 var36 = new Vec3(-Math.sin(var31) * Math.sin(var33), Math.cos(var33), Math.cos(var31) * Math.sin(var33));
                Vec3 var37 = var30.subtract(var21);
                double var38 = Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, var37.normalize().dot(var35)))));
                float var40 = 0.0f;
                if ((viewBlend += (var40 - viewBlend) * Math.min(1.0f, var9 * 5.0f)) > 0.001f) {
                    float var41 = 0.0336f * var4.pvpScale;
                    Vec3 var42 = var21.add(var35.scale(7.0)).add(var36.scale(-1.6)).add(var27.scale((double)side * 0.8));
                    var30 = var30.add(var42.subtract(var30).scale((double)viewBlend));
                    var26 += (var41 - var26) * viewBlend;
                }
                if (var25) {
                    var72 = (float)Math.toRadians(180.0 - (double)var1.yRot());
                    var73 = (float)Math.toRadians(-var1.xRot());
                } else {
                    double var43 = var21.x - var30.x;
                    double var45 = var21.z - var30.z;
                    var72 = (float)Math.atan2(var43, var45);
                    var73 = (float)Math.atan2(var30.y - var21.y, Math.max(0.3, Math.sqrt(var43 * var43 + var45 * var45)));
                }
                float var74 = (float)(var7 - lineBornNanos) / 1.0E9f;
                float var44 = 1.0f + 0.18f * Math.max(0.0f, 1.0f - var74 / 0.25f);
                float var76 = (float)(var7 - slamNanos) / 1.0E9f;
                float var46 = slamNanos != 0L && var76 < 1.2f ? slamStrength * (float)Math.exp((double)(-var76) * 5.0) : 0.0f;
                var44 += 0.9f * var46;
                float var47 = var46 * 9.0f * (float)Math.sin((double)var76 * 55.0);
                if (!slamFxDone && lastPos != null) {
                    slamFxDone = true;
                    PvpLyrics.slamParticles(var0, lastPos, LyricsConfig.rgb(var4.accentColor, 10980346), slamStrength);
                }
                int var48 = LyricsConfig.rgb(var4.accentColor, 10980346);
                var2.pushPose();
                var2.translate(var30.x - var21.x, var30.y - var21.y, var30.z - var21.z);
                var2.mulPose((Quaternionfc)new Quaternionf().rotationY(var72));
                var2.mulPose((Quaternionfc)new Quaternionf().rotationX(var73));
                var2.mulPose((Quaternionfc)new Quaternionf().rotationZ((float)Math.toRadians(-4.0 * (double)side + (double)var47)));
                var2.scale(var26 * var44, -var26 * var44, var26 * var44);
                Matrix4f var49 = var2.last().pose();
                List<Lyrics.Word> var50 = var14.words();
                float var51 = Lyrics.wordPosition(var14, var5);
                List<List<SkyText.Word>> var52 = var19.rows();
                float var53 = 10.0f;
                float var54 = -((float)var52.size() * var53) / 2.0f;
                int var55 = 0;
                for (List list : var52) {
                    float var58 = -3.0f;
                    for (SkyText.Word var60 : list) {
                        var58 += (float)var60.width() + 3.0f;
                    }
                    float var77 = -var58 / 2.0f;
                    for (SkyText.Word var61 : list) {
                        float var62;
                        float f = var62 = var50 != null && var55 < var50.size() ? var51 - (float)var55 : 1.0f;
                        if (var62 > 0.0f) {
                            int var67;
                            float var63 = Math.min(1.0f, var62 * 5.0f);
                            float var64 = 1.0f + 0.6f * (1.0f - var63) - 0.12f * (float)Math.sin((double)Math.min(1.0f, var62 * 3.0f) * Math.PI) * (1.0f - var63 * 0.5f);
                            boolean var65 = var62 < 1.0f;
                            int var66 = Math.max(1, var19.words());
                            int n = var67 = var4.gradient && var66 > 1 ? SkyText.mix(SkyText.lastColor, LyricsConfig.rgb(var4.color2, 16020150), (float)var55 / (float)(var66 - 1)) : SkyText.lastColor;
                            int var68 = var65 ? SkyText.mix(var67, 0xFFFFFF, var46 > 0.15f ? Math.max(0.3f, var46) : 0.3f) : var67;
                            float var69 = fade * Math.min(1.0f, 0.25f + var63);
                            Matrix4f var70 = new Matrix4f((Matrix4fc)var49).translate(var77 + (float)var61.width() / 2.0f - (float)var61.width() * var64 / 2.0f, var54 + var53 / 2.0f - var53 * var64 / 2.0f, 0.0f).scale(var64, var64, var64);
                            SkyText.drawPlate(var15, var61.comp(), var70, var3, var68, 0, var69, var4.pvpOutline * (var17 ? 1.0f : 0.6f), !var17);
                        }
                        var77 += (float)var61.width() + 3.0f;
                        ++var55;
                    }
                    var54 += var53;
                }
                var2.popPose();
            }
        }
    }

    private static void slamParticles(Minecraft var0, Vec3 var1, int var2, float var3) {
        if (var0.level != null) {
            int var4 = Math.round(24.0f + 30.0f * var3);
            double var5 = 0.35 + 0.35 * (double)var3;
            for (int var7 = 0; var7 < var4; ++var7) {
                double var8 = (double)var7 * Math.PI * 2.0 / (double)var4;
                double var10 = Math.cos(var8);
                double var12 = Math.sin(var8);
                var0.level.addParticle((ParticleOptions)ParticleTypes.FIREWORK, true, true, var1.x + var10 * 0.4, var1.y + 0.15, var1.z + var12 * 0.4, var10 * var5, 0.03, var12 * var5);
            }
            for (int var14 = 0; var14 < Math.round(14.0f * var3) + 6; ++var14) {
                double var15 = Math.random() * Math.PI * 2.0;
                double var16 = 0.15 + Math.random() * 0.5;
                var0.level.addParticle((ParticleOptions)new DustParticleOptions(var2 & 0xFFFFFF, 1.6f), true, true, var1.x, var1.y + 0.6, var1.z, Math.cos(var15) * 3.0, var16 * 6.0, Math.sin(var15) * 3.0);
            }
        }
    }

    static {
        side = 1;
        lastWidth = 0.6f;
        lastHeight = 1.8f;
        slamFxDone = true;
    }
}

