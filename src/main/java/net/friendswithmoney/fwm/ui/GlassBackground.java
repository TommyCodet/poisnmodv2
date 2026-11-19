/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.NativeImage
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.resources.Identifier
 */
package net.friendswithmoney.fwm.ui;

import com.mojang.blaze3d.platform.NativeImage;
import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

public final class GlassBackground {
    private static final LinkedHashMap<String, Identifier> CACHE = new LinkedHashMap(8, 0.75f, true);
    private static final int KEEP = 8;

    private GlassBackground() {
    }

    public static void draw(GuiGraphicsExtractor ctx, int w, int h, int radius, float pixelsPerUnit, int top, int bottom, int glowA, int glowAAlpha, int glowB, int glowBAlpha) {
        int pw = Math.max(1, Math.round((float)w * pixelsPerUnit));
        int ph = Math.max(1, Math.round((float)h * pixelsPerUnit));
        FwmConfig c = FwmConfig.INSTANCE;
        String want = pw + "x" + ph + "|" + radius + "|" + top + "|" + bottom + "|" + glowA + "|" + glowB + "|" + c.menuHue + "|" + c.menuSat + "|" + c.tintHue + "|" + c.tintSat;
        Identifier id = CACHE.get(want);
        if (id == null) {
            id = GlassBackground.bake(want, pw, ph, (float)radius * pixelsPerUnit, pixelsPerUnit, GlassBackground.tinted(top, c.tintHue, c.tintSat), GlassBackground.themed(bottom), GlassBackground.themed(glowA | 0xFF000000), glowAAlpha, GlassBackground.themed(glowB | 0xFF000000), glowBAlpha, w, h);
        }
        ctx.blit(RenderPipelines.GUI_TEXTURED, id, 0, 0, 0.0f, 0.0f, w, h, pw, ph, pw, ph);
    }

    static int themed(int argb) {
        FwmConfig c = FwmConfig.INSTANCE;
        return GlassBackground.tinted(argb, c.menuHue, c.menuSat);
    }

    private static int tinted(int argb, float hue, float sat) {
        if (hue < 0.0f) {
            return argb;
        }
        float[] hsv = Color.RGBtoHSB(argb >> 16 & 0xFF, argb >> 8 & 0xFF, argb & 0xFF, null);
        float s = hsv[1] * Math.max(0.35f, Math.min(1.2f, sat));
        int rgb = Color.HSBtoRGB(hue, Math.min(1.0f, s), hsv[2]) & 0xFFFFFF;
        return argb & 0xFF000000 | rgb;
    }

    private static Identifier bake(String want, int pw, int ph, float r, float k, int top, int bottom, int glowA, int alphaA, int glowB, int alphaB, int w, int h) {
        NativeImage img = new NativeImage(pw, ph, true);
        Random noise = new Random(7L);
        float ax = (float)(w - 90) * k;
        float ay = -30.0f * k;
        float ar = 300.0f * k;
        float bx = -20.0f * k;
        float by = (float)(h + 30) * k;
        float br = 240.0f * k;
        for (int y = 0; y < ph; ++y) {
            float t = ph <= 1 ? 0.0f : (float)y / (float)(ph - 1);
            float[] base = GlassBackground.mix(top, bottom, t);
            for (int x = 0; x < pw; ++x) {
                float cover = GlassBackground.cover((float)x + 0.5f, (float)y + 0.5f, pw, ph, r);
                if (cover <= 0.0f) {
                    img.setPixel(x, y, 0);
                    continue;
                }
                float[] col = (float[])base.clone();
                GlassBackground.over(col, glowA, (float)alphaA / 255.0f * GlassBackground.glow(x, y, ax, ay, ar));
                GlassBackground.over(col, glowB, (float)alphaB / 255.0f * GlassBackground.glow(x, y, bx, by, br));
                float d = (noise.nextFloat() - 0.5f) / 255.0f;
                int a = GlassBackground.clamp(Math.round((col[0] + d) * cover * 255.0f));
                int rr = GlassBackground.clamp(Math.round((col[1] + d) * 255.0f));
                int gg = GlassBackground.clamp(Math.round((col[2] + d) * 255.0f));
                int bb = GlassBackground.clamp(Math.round((col[3] + d) * 255.0f));
                img.setPixel(x, y, a << 24 | rr << 16 | gg << 8 | bb);
            }
        }
        Minecraft client = Minecraft.getInstance();
        if (CACHE.size() >= 8) {
            Map.Entry<String, Identifier> eldest = CACHE.entrySet().iterator().next();
            client.getTextureManager().release(eldest.getValue());
            CACHE.remove(eldest.getKey());
        }
        Identifier id = Identifier.fromNamespaceAndPath((String)"fwm", (String)("glass/" + Integer.toHexString(want.hashCode())));
        client.getTextureManager().register(id, (AbstractTexture)new DynamicTexture(() -> "fwm glass", img));
        CACHE.put(want, id);
        return id;
    }

    private static float cover(float x, float y, int w, int h, float r) {
        float cx = Math.max(r, Math.min((float)w - r, x));
        float cy = Math.max(r, Math.min((float)h - r, y));
        float dx = x - cx;
        float dy = y - cy;
        float dist = (float)Math.sqrt(dx * dx + dy * dy);
        return Math.max(0.0f, Math.min(1.0f, r - dist + 0.5f));
    }

    private static float glow(float x, float y, float cx, float cy, float radius) {
        float dx = x - cx;
        float dy = y - cy;
        float d = (float)Math.min(1.0, Math.sqrt(dx * dx + dy * dy) / (double)radius);
        float f = 1.0f - d;
        return f * f * (3.0f - 2.0f * f);
    }

    private static float[] mix(int a, int b, float t) {
        float[] out = new float[4];
        for (int i = 0; i < 4; ++i) {
            int shift = 24 - 8 * i;
            float ca = (float)(a >>> shift & 0xFF) / 255.0f;
            float cb = (float)(b >>> shift & 0xFF) / 255.0f;
            out[i] = ca + (cb - ca) * t;
        }
        return out;
    }

    private static void over(float[] col, int rgb, float s) {
        if (s <= 0.0f) {
            return;
        }
        float r = (float)(rgb >> 16 & 0xFF) / 255.0f;
        float g = (float)(rgb >> 8 & 0xFF) / 255.0f;
        float b = (float)(rgb & 0xFF) / 255.0f;
        float a = col[0];
        float na = a + s * (1.0f - a);
        if (na <= 0.0f) {
            return;
        }
        col[1] = (r * s + col[1] * a * (1.0f - s)) / na;
        col[2] = (g * s + col[2] * a * (1.0f - s)) / na;
        col[3] = (b * s + col[3] * a * (1.0f - s)) / na;
        col[0] = na;
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}

