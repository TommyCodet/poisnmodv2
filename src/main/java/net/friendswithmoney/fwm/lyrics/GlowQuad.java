/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.NativeImage
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.rendertype.RenderTypes
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.Identifier
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 */
package net.friendswithmoney.fwm.lyrics;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.Arrays;
import net.friendswithmoney.fwm.lyrics.Sink;
import net.friendswithmoney.fwm.lyrics.SkyLyrics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector3f;

final class GlowQuad {
    static final Identifier ID = Identifier.fromNamespaceAndPath((String)"fwm", (String)"sky_glow");
    private static final int W = 192;
    private static final int H = 64;
    private static boolean ready;
    private static boolean failed;
    private static float[] queue;
    private static int[] colors;
    private static int queued;
    private static final Vector3f TMP;

    private GlowQuad() {
    }

    private static boolean ensure() {
        if (ready) {
            return true;
        }
        if (failed) {
            return false;
        }
        try {
            NativeImage img = new NativeImage(192, 64, false);
            float rx = 96.0f;
            float ry = 32.0f;
            float coreX = rx - ry;
            for (int y = 0; y < 64; ++y) {
                for (int x = 0; x < 192; ++x) {
                    float px = (float)x + 0.5f - rx;
                    float py = (float)y + 0.5f - ry;
                    float dx = Math.max(0.0f, Math.abs(px) - coreX);
                    float d = (float)Math.sqrt(dx * dx + py * py) / ry;
                    float a = d >= 1.0f ? 0.0f : (float)(Math.exp(-3.2 * (double)d * (double)d) * (double)(1.0f - d * d) * (double)(1.0f - d * d));
                    int alpha = Math.round(a * 255.0f);
                    img.setPixel(x, y, alpha << 24 | 0xFFFFFF);
                }
            }
            Minecraft.getInstance().getTextureManager().register(ID, (AbstractTexture)new DynamicTexture(() -> "fwm sky glow", img));
            ready = true;
        }
        catch (Throwable t) {
            failed = true;
            SkyLyrics.LOG.warn("Sky Lyrics: Leucht-Textur nicht erstellt", t);
        }
        return ready;
    }

    static void draw(Sink vcp, Matrix4f m, float x0, float y0, float x1, float y1, float pad, int rgb, float alpha, boolean seeThrough) {
        if (alpha <= 0.01f) {
            return;
        }
        int a = Math.max(0, Math.min(255, Math.round(alpha * 255.0f)));
        if (queued == colors.length) {
            colors = Arrays.copyOf(colors, queued * 2);
            queue = Arrays.copyOf(queue, queued * 2 * 16);
        }
        float ax = x0 - pad * 1.6f;
        float bx = x1 + pad * 1.6f;
        float ay = y0 - pad;
        float by = y1 + pad;
        float[] xs = new float[]{ax, ax, bx, bx};
        float[] ys = new float[]{ay, by, by, ay};
        int o = queued * 16;
        for (int k = 0; k < 4; ++k) {
            m.transformPosition(xs[k], ys[k], -0.4f, TMP);
            GlowQuad.queue[o + k * 4] = GlowQuad.TMP.x;
            GlowQuad.queue[o + k * 4 + 1] = GlowQuad.TMP.y;
            GlowQuad.queue[o + k * 4 + 2] = GlowQuad.TMP.z;
        }
        GlowQuad.colors[GlowQuad.queued++] = a << 24 | rgb & 0xFFFFFF;
    }

    static void flush(Sink vcp) {
        if (queued == 0) {
            return;
        }
        if (vcp == null || !GlowQuad.ensure()) {
            queued = 0;
            return;
        }
        int ov = OverlayTexture.NO_OVERLAY;
        float[] us = new float[]{0.0f, 0.0f, 1.0f, 1.0f};
        float[] vs = new float[]{0.0f, 1.0f, 1.0f, 0.0f};
        int n = queued;
        float[] qs = Arrays.copyOf(queue, n * 16);
        int[] cs = Arrays.copyOf(colors, n);
        vcp.quads(RenderTypes.beaconBeam((Identifier)ID, (boolean)true), c -> {
            for (int q = 0; q < n; ++q) {
                int o = q * 16;
                for (int k = 0; k < 4; ++k) {
                    c.addVertex(qs[o + k * 4], qs[o + k * 4 + 1], qs[o + k * 4 + 2]).setColor(cs[q]).setUv(us[k], vs[k]).setOverlay(ov).setLight(0xF000F0).setNormal(0.0f, 0.0f, 1.0f);
                }
            }
        });
        queued = 0;
    }

    static {
        queue = new float[1024];
        colors = new int[64];
        TMP = new Vector3f();
    }
}

