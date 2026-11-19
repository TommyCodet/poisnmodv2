/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.NativeImage
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
 *  net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
 *  net.minecraft.client.Camera
 *  net.minecraft.client.CameraType
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.SubmitNodeCollector
 *  net.minecraft.client.renderer.rendertype.RenderTypes
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 */
package net.friendswithmoney.fwm.game;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.V2;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public final class PvpFx {
    public static final String HIT = "fwm.tab.hitcolor";
    public static final String POP = "fwm.tab.totempop";
    public static final String HOLD = "fwm.tab.totemhold";
    private static DynamicTexture overlay;
    private static final int VANILLA_HIT = -1291911168;
    private static final List<Ghost> GHOSTS;
    private static final float PX = 0.05625f;
    public static final float[][] PARTS;

    private PvpFx() {
    }

    public static void register() {
        LevelRenderEvents.AFTER_SOLID_FEATURES.register(PvpFx::renderGhosts);
    }

    public static boolean hitColor() {
        return Features.isOn(HIT) && V2.INSTANCE.hitColorOn;
    }

    public static boolean totemPop() {
        return Features.isOn(POP) && V2.INSTANCE.totemPopOn;
    }

    public static boolean popChams() {
        return Features.isOn(POP) && V2.INSTANCE.popChamsOn;
    }

    public static boolean holdUp() {
        return Features.isOn(HOLD) && V2.INSTANCE.totemHoldUp;
    }

    public static void attach(DynamicTexture texture) {
        overlay = texture;
        PvpFx.applyHitColor();
    }

    public static void applyHitColor() {
        if (overlay == null) {
            return;
        }
        NativeImage image = overlay.getPixels();
        if (image == null) {
            return;
        }
        V2 c = V2.INSTANCE;
        int alpha = Math.max(20, Math.min(255, c.hitAlpha));
        int color = PvpFx.hitColor() ? alpha << 24 | c.hitColor & 0xFFFFFF : -1291911168;
        for (int y = 0; y < 8; ++y) {
            for (int x = 0; x < 16; ++x) {
                image.setPixel(x, y, color);
            }
        }
        overlay.upload();
    }

    public static float[] totemParticleColor(Random random) {
        V2 c = V2.INSTANCE;
        int rgb = random.nextBoolean() ? c.totemColorA : c.totemColorB;
        float shade = 0.8f + random.nextFloat() * 0.2f;
        return new float[]{(float)(rgb >> 16 & 0xFF) / 255.0f * shade, (float)(rgb >> 8 & 0xFF) / 255.0f * shade, (float)(rgb & 0xFF) / 255.0f * shade};
    }

    public static boolean keepParticle(Random random) {
        if (!Features.isOn(POP)) {
            return true;
        }
        V2 c = V2.INSTANCE;
        if (!c.popParticles) {
            return false;
        }
        int amount = Math.max(0, Math.min(100, c.popParticleAmount));
        return amount >= 100 || random.nextInt(100) < amount;
    }

    public static int edgeColor() {
        V2 c = V2.INSTANCE;
        return (c.popChamsEdgeColor < 0 ? c.popChamsColor : c.popChamsEdgeColor) & 0xFFFFFF;
    }

    public static int fillAlpha() {
        return Math.round((float)Math.max(0, Math.min(100, V2.INSTANCE.popChamsFillAlpha)) * 2.55f);
    }

    public static float edgeHalf() {
        return (float)Math.max(1, Math.min(5, V2.INSTANCE.popChamsEdge)) * 0.125f;
    }

    public static void onPop(Entity entity) {
        if (!PvpFx.popChams() || !(entity instanceof Player)) {
            return;
        }
        Player player = (Player)entity;
        Minecraft client = Minecraft.getInstance();
        if (player == client.player && client.options.getCameraType() == CameraType.FIRST_PERSON) {
            return;
        }
        if (GHOSTS.size() > 32) {
            GHOSTS.remove(0);
        }
        GHOSTS.add(new Ghost(player.getX(), player.getY(), player.getZ(), player.yBodyRot, System.currentTimeMillis()));
    }

    private static void renderGhosts(LevelRenderContext ctx) {
        if (GHOSTS.isEmpty()) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.gui.hud.isHidden()) {
            GHOSTS.clear();
            return;
        }
        long now = System.currentTimeMillis();
        long life = Math.max(200L, (long)V2.INSTANCE.popChamsTenths * 100L);
        GHOSTS.removeIf(g -> now - g.at() > life);
        if (GHOSTS.isEmpty()) {
            return;
        }
        PoseStack matrices = ctx.poseStack();
        SubmitNodeCollector queue = ctx.submitNodeCollector();
        Camera camera = client.gameRenderer.mainCamera();
        if (matrices == null || queue == null || camera == null) {
            return;
        }
        Vec3 cam = camera.position();
        int rgb = V2.INSTANCE.popChamsColor & 0xFFFFFF;
        int edgeRgb = PvpFx.edgeColor();
        int mode = V2.INSTANCE.popChamsMode;
        boolean drawFill = mode != 1;
        boolean drawEdge = mode != 0;
        int fillA = PvpFx.fillAlpha();
        float t = PvpFx.edgeHalf();
        int light = 0xF000F0;
        for (Ghost g2 : GHOSTS) {
            float fade = 1.0f - (float)(now - g2.at()) / (float)life;
            int fill = drawFill ? Math.round((float)fillA * fade) << 24 | rgb : 0;
            int edge = drawEdge ? Math.round(224.0f * fade) << 24 | edgeRgb : 0;
            matrices.pushPose();
            matrices.translate(g2.x() - cam.x, g2.y() - cam.y, g2.z() - cam.z);
            matrices.mulPose((Quaternionfc)new Quaternionf().rotationY((float)Math.toRadians(-g2.yaw())));
            matrices.scale(0.05625f, 0.05625f, 0.05625f);
            queue.submitCustomGeometry(matrices, RenderTypes.textBackground(), (entry, consumer) -> {
                Matrix4f m = entry.pose();
                for (float[] p : PARTS) {
                    PvpFx.part(consumer, m, p[0], p[1], p[2], p[3], p[4], p[5], fill, edge, t, light);
                }
            });
            matrices.popPose();
        }
    }

    private static void part(VertexConsumer c, Matrix4f m, float x0, float y0, float z0, float x1, float y1, float z1, int fill, int edge, float t, int light) {
        if (fill >>> 24 > 0) {
            PvpFx.box(c, m, x0, y0, z0, x1, y1, z1, fill, light);
        }
        if (edge >>> 24 == 0) {
            return;
        }
        float[] xs = new float[]{x0, x1};
        float[] ys = new float[]{y0, y1};
        float[] zs = new float[]{z0, z1};
        for (float y : ys) {
            for (float z : zs) {
                PvpFx.box(c, m, x0, y - t, z - t, x1, y + t, z + t, edge, light);
            }
        }
        for (float x : xs) {
            for (float z : zs) {
                PvpFx.box(c, m, x - t, y0, z - t, x + t, y1, z + t, edge, light);
            }
        }
        for (float x : xs) {
            for (float y : ys) {
                PvpFx.box(c, m, x - t, y - t, z0, x + t, y + t, z1, edge, light);
            }
        }
    }

    private static void box(VertexConsumer c, Matrix4f m, float x0, float y0, float z0, float x1, float y1, float z1, int argb, int light) {
        PvpFx.face(c, m, x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, argb, light);
        PvpFx.face(c, m, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, argb, light);
        PvpFx.face(c, m, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, argb, light);
        PvpFx.face(c, m, x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0, argb, light);
        PvpFx.face(c, m, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, argb, light);
        PvpFx.face(c, m, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, argb, light);
    }

    private static void face(VertexConsumer c, Matrix4f m, float ax, float ay, float az, float bx, float by, float bz, float cx, float cy, float cz, float dx, float dy, float dz, int argb, int light) {
        c.addVertex((Matrix4fc)m, ax, ay, az).setColor(argb).setLight(light);
        c.addVertex((Matrix4fc)m, bx, by, bz).setColor(argb).setLight(light);
        c.addVertex((Matrix4fc)m, cx, cy, cz).setColor(argb).setLight(light);
        c.addVertex((Matrix4fc)m, dx, dy, dz).setColor(argb).setLight(light);
        c.addVertex((Matrix4fc)m, dx, dy, dz).setColor(argb).setLight(light);
        c.addVertex((Matrix4fc)m, cx, cy, cz).setColor(argb).setLight(light);
        c.addVertex((Matrix4fc)m, bx, by, bz).setColor(argb).setLight(light);
        c.addVertex((Matrix4fc)m, ax, ay, az).setColor(argb).setLight(light);
    }

    static {
        GHOSTS = new ArrayList<Ghost>();
        PARTS = new float[][]{{-4.0f, 0.0f, -2.0f, 0.0f, 12.0f, 2.0f}, {0.0f, 0.0f, -2.0f, 4.0f, 12.0f, 2.0f}, {-4.0f, 12.0f, -2.0f, 4.0f, 24.0f, 2.0f}, {-8.0f, 12.0f, -2.0f, -4.0f, 24.0f, 2.0f}, {4.0f, 12.0f, -2.0f, 8.0f, 24.0f, 2.0f}, {-4.0f, 24.0f, -4.0f, 4.0f, 32.0f, 4.0f}};
    }

    private record Ghost(double x, double y, double z, float yaw, long at) {
    }

    public static interface TotemArms {
        public int fwm$totemArms();

        public void fwm$setTotemArms(int var1);
    }
}

