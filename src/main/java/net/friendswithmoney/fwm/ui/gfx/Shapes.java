/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.platform.NativeImage
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.GpuSampler
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.navigation.ScreenRectangle
 *  net.minecraft.client.gui.render.TextureSetup
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.client.renderer.state.gui.GuiElementRenderState
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.resources.Identifier
 *  org.joml.Matrix3x2f
 *  org.joml.Matrix3x2fStack
 *  org.joml.Matrix3x2fc
 */
package net.friendswithmoney.fwm.ui.gfx;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.friendswithmoney.fwm.config.V2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix3x2fc;

public final class Shapes {
    public static float alpha = 1.0f;
    private static final int MAX_R = 160;
    private static final int ATLAS = 1024;
    private static final Identifier ATLAS_ID = Identifier.fromNamespaceAndPath((String)"fwm", (String)"ui_shape_atlas");
    private static DynamicTexture atlas;
    private static int shelfX;
    private static int shelfY;
    private static int shelfH;
    private static Region white;
    private static final Map<Long, Region> CORNERS;
    private static float[] quads;
    private static int quadCount;
    private static TextureSetup setup;
    static final float[] FRAME;
    private static final Map<String, Region> ICONS;
    private static final float W = 1.05f;

    private Shapes() {
    }

    private static boolean ready() {
        if (atlas != null) {
            return true;
        }
        try {
            atlas = new DynamicTexture(() -> "fwm shapes", 1024, 1024, true);
            Minecraft.getInstance().getTextureManager().register(ATLAS_ID, (AbstractTexture)atlas);
            Shapes.clearAtlas();
            return true;
        }
        catch (Throwable t) {
            atlas = null;
            return false;
        }
    }

    private static void clearAtlas() {
        NativeImage img = atlas.getPixels();
        if (img != null) {
            img.fillRect(0, 0, 1024, 1024, 0);
        }
        CORNERS.clear();
        ICONS.clear();
        shelfH = 0;
        shelfY = 0;
        shelfX = 0;
        white = null;
        setup = null;
        Region w = Shapes.alloc(4, 4);
        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 4; ++x) {
                img.setPixel(w.u() + x, w.v() + y, -1);
            }
        }
        Shapes.upload(w);
        white = w;
    }

    private static Region alloc(int w, int h) {
        if (shelfX + w > 1024) {
            shelfX = 0;
            shelfY += shelfH + 1;
            shelfH = 0;
        }
        if (shelfY + h > 1024) {
            Shapes.clearAtlas();
            if (shelfX + w > 1024) {
                shelfX = 0;
                shelfY += shelfH + 1;
                shelfH = 0;
            }
        }
        Region r = new Region(shelfX, shelfY, w, h);
        shelfX += w + 1;
        shelfH = Math.max(shelfH, h);
        return r;
    }

    private static void upload(Region r) {
        NativeImage src = atlas.getPixels();
        if (src == null || atlas.getTexture() == null) {
            return;
        }
        try (NativeImage piece = new NativeImage(r.w(), r.h(), false);){
            src.copyRect(piece, r.u(), r.v(), 0, 0, r.w(), r.h(), false, false);
            RenderSystem.getDevice().createCommandEncoder().writeToTexture(atlas.getTexture(), piece, 0, 0, r.u(), r.v());
        }
    }

    private static void fillPx(GuiGraphicsExtractor ctx, int x0, int y0, int x1, int y1, int color) {
        if (x1 <= x0 || y1 <= y0) {
            return;
        }
        if (white == null) {
            ctx.fill(x0, y0, x1, y1, color);
            return;
        }
        float u = ((float)white.u() + 1.5f) / 1024.0f;
        float v = ((float)white.v() + 1.5f) / 1024.0f;
        Shapes.quad(x0, y0, x1, y1, u, v, u, v, color);
    }

    private static void quad(float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1, int color) {
        int i = quadCount * 9;
        if (i + 9 > quads.length) {
            quads = Arrays.copyOf(quads, quads.length * 2);
        }
        Shapes.quads[i] = x0;
        Shapes.quads[i + 1] = y0;
        Shapes.quads[i + 2] = x1;
        Shapes.quads[i + 3] = y1;
        Shapes.quads[i + 4] = u0;
        Shapes.quads[i + 5] = v0;
        Shapes.quads[i + 6] = u1;
        Shapes.quads[i + 7] = v1;
        Shapes.quads[i + 8] = Float.intBitsToFloat(color);
        ++quadCount;
    }

    private static void flush(GuiGraphicsExtractor ctx) {
        int n = quadCount;
        quadCount = 0;
        if (n == 0 || atlas == null) {
            return;
        }
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -3.4028235E38f;
        float maxY = -3.4028235E38f;
        for (int q = 0; q < n; ++q) {
            int i = q * 9;
            minX = Math.min(minX, quads[i]);
            minY = Math.min(minY, quads[i + 1]);
            maxX = Math.max(maxX, quads[i + 2]);
            maxY = Math.max(maxY, quads[i + 3]);
        }
        Matrix3x2f pose = new Matrix3x2f((Matrix3x2fc)ctx.pose());
        ScreenRectangle scissor = ctx.scissorStack.peek();
        ScreenRectangle bounds = new ScreenRectangle((int)Math.floor(minX), (int)Math.floor(minY), Math.max(1, (int)Math.ceil(maxX - minX)), Math.max(1, (int)Math.ceil(maxY - minY))).transformMaxBounds((Matrix3x2fc)pose);
        if (scissor != null && (bounds = scissor.intersection(bounds)) == null) {
            return;
        }
        if (setup == null) {
            setup = TextureSetup.singleTexture((GpuTextureView)atlas.getTextureView(), (GpuSampler)atlas.getSampler());
        }
        ctx.guiRenderState.addGuiElement((GuiElementRenderState)new Batch(setup, pose, Arrays.copyOf(quads, n * 9), n, scissor, bounds));
    }

    public static void quads(GuiGraphicsExtractor ctx, float[] pts, int[] colors, int n) {
        int k;
        if (n <= 0 || !Shapes.ready() || white == null) {
            return;
        }
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -3.4028235E38f;
        float maxY = -3.4028235E38f;
        for (int i = 0; i < n * 8; i += 2) {
            minX = Math.min(minX, pts[i]);
            maxX = Math.max(maxX, pts[i]);
            minY = Math.min(minY, pts[i + 1]);
            maxY = Math.max(maxY, pts[i + 1]);
        }
        int[] faded = new int[n];
        for (k = 0; k < n; ++k) {
            faded[k] = Shapes.fade(colors[k]);
        }
        pts = Arrays.copyOf(pts, n * 8);
        for (k = 0; k < n; ++k) {
            int i = k * 8;
            float area = 0.0f;
            for (int j = 0; j < 4; ++j) {
                int a0 = i + j * 2;
                int a1 = i + (j + 1) % 4 * 2;
                area += pts[a0] * pts[a1 + 1] - pts[a1] * pts[a0 + 1];
            }
            if (!(area > 0.0f)) continue;
            float tx = pts[i + 2];
            float ty = pts[i + 3];
            pts[i + 2] = pts[i + 6];
            pts[i + 3] = pts[i + 7];
            pts[i + 6] = tx;
            pts[i + 7] = ty;
        }
        Matrix3x2f pose = new Matrix3x2f((Matrix3x2fc)ctx.pose());
        ScreenRectangle scissor = ctx.scissorStack.peek();
        ScreenRectangle bounds = new ScreenRectangle((int)Math.floor(minX), (int)Math.floor(minY), Math.max(1, (int)Math.ceil(maxX - minX)), Math.max(1, (int)Math.ceil(maxY - minY))).transformMaxBounds((Matrix3x2fc)pose);
        if (scissor != null && (bounds = scissor.intersection(bounds)) == null) {
            return;
        }
        if (setup == null) {
            setup = TextureSetup.singleTexture((GpuTextureView)atlas.getTextureView(), (GpuSampler)atlas.getSampler());
        }
        float u = ((float)white.u() + 1.5f) / 1024.0f;
        float v = ((float)white.v() + 1.5f) / 1024.0f;
        ctx.guiRenderState.addGuiElement((GuiElementRenderState)new FreeQuads(setup, pose, Arrays.copyOf(pts, n * 8), faded, n, u, v, scissor, bounds));
    }

    static float gui() {
        Minecraft c = Minecraft.getInstance();
        return c == null || c.getWindow() == null ? 1.0f : (float)c.getWindow().getGuiScale();
    }

    static void frame(GuiGraphicsExtractor ctx) {
        Matrix3x2fStack m = ctx.pose();
        float g = Shapes.gui();
        Shapes.FRAME[0] = m.m00() * g;
        Shapes.FRAME[1] = m.m11() * g;
        Shapes.FRAME[2] = m.m20() * g;
        Shapes.FRAME[3] = m.m21() * g;
    }

    public static float pixels(GuiGraphicsExtractor ctx) {
        Shapes.frame(ctx);
        return Math.abs(FRAME[0]);
    }

    static int px(float x) {
        return Math.round(x * FRAME[0] + FRAME[2]);
    }

    static int py(float y) {
        return Math.round(y * FRAME[1] + FRAME[3]);
    }

    static void enterPixels(GuiGraphicsExtractor ctx) {
        float g = Shapes.gui();
        Matrix3x2fStack m = ctx.pose();
        m.pushMatrix();
        m.set(1.0f / g, 0.0f, 0.0f, 1.0f / g, 0.0f, 0.0f);
    }

    public static int faded(int color) {
        return Shapes.fade(color);
    }

    static int fade(int color) {
        if (alpha >= 1.0f) {
            return color;
        }
        int a = Math.round((float)(color >>> 24 & 0xFF) * Math.max(0.0f, alpha));
        return a << 24 | color & 0xFFFFFF;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void round(GuiGraphicsExtractor ctx, float x, float y, float w, float h, float r, int color) {
        if (w <= 0.0f || h <= 0.0f || color >>> 24 == 0) {
            return;
        }
        Shapes.frame(ctx);
        int x0 = Shapes.px(x);
        int y0 = Shapes.py(y);
        int x1 = Shapes.px(x + w);
        int y1 = Shapes.py(y + h);
        if (x1 <= x0 || y1 <= y0) {
            return;
        }
        int R = Shapes.radius(r, x1 - x0, y1 - y0);
        color = Shapes.fade(color);
        if (!Shapes.ready()) {
            return;
        }
        Shapes.enterPixels(ctx);
        try {
            Shapes.fillRound(ctx, x0, y0, x1, y1, R, color);
        }
        finally {
            Shapes.flush(ctx);
            ctx.pose().popMatrix();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void roundGradient(GuiGraphicsExtractor ctx, float x, float y, float w, float h, float r, int top, int bottom) {
        if (w <= 0.0f || h <= 0.0f) {
            return;
        }
        if (top == bottom) {
            Shapes.round(ctx, x, y, w, h, r, top);
            return;
        }
        Shapes.frame(ctx);
        int x0 = Shapes.px(x);
        int y0 = Shapes.py(y);
        int x1 = Shapes.px(x + w);
        int y1 = Shapes.py(y + h);
        if (x1 <= x0 || y1 <= y0) {
            return;
        }
        int H = y1 - y0;
        int R = Shapes.radius(r, x1 - x0, H);
        top = Shapes.fade(top);
        bottom = Shapes.fade(bottom);
        float a = (float)R / (float)H;
        int cTop = Shapes.mix(top, bottom, a * 0.5f);
        int cBot = Shapes.mix(top, bottom, 1.0f - a * 0.5f);
        int cA = Shapes.mix(top, bottom, a);
        int cB = Shapes.mix(top, bottom, 1.0f - a);
        if (!Shapes.ready()) {
            return;
        }
        Shapes.enterPixels(ctx);
        try {
            if (R > 0) {
                Region disc = Shapes.corner(R, 0);
                Shapes.corner(ctx, disc, x0, y0, R, 0, 0, cTop);
                Shapes.corner(ctx, disc, x1 - R, y0, R, R, 0, cTop);
                Shapes.corner(ctx, disc, x0, y1 - R, R, 0, R, cBot);
                Shapes.corner(ctx, disc, x1 - R, y1 - R, R, R, R, cBot);
                if (x1 - R > x0 + R) {
                    ctx.fillGradient(x0 + R, y0, x1 - R, y0 + R, top, cA);
                    ctx.fillGradient(x0 + R, y1 - R, x1 - R, y1, cB, bottom);
                }
            }
            if (y1 - R > y0 + R) {
                ctx.fillGradient(x0, y0 + R, x1, y1 - R, cA, cB);
            }
        }
        finally {
            Shapes.flush(ctx);
            ctx.pose().popMatrix();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void ring(GuiGraphicsExtractor ctx, float x, float y, float w, float h, float r, int color, float stroke) {
        if (w <= 0.0f || h <= 0.0f || color >>> 24 == 0) {
            return;
        }
        Shapes.frame(ctx);
        int x0 = Shapes.px(x);
        int y0 = Shapes.py(y);
        int x1 = Shapes.px(x + w);
        int y1 = Shapes.py(y + h);
        if (x1 <= x0 || y1 <= y0) {
            return;
        }
        int W = x1 - x0;
        int H = y1 - y0;
        int R = Shapes.radius(r, W, H);
        int S = Math.max(1, Math.round(stroke * Math.abs(FRAME[0])));
        S = Math.min(S, Math.max(1, Math.min(W, H) / 2));
        color = Shapes.fade(color);
        if (!Shapes.ready()) {
            return;
        }
        Shapes.enterPixels(ctx);
        try {
            if (R <= 0) {
                Shapes.fillPx(ctx, x0, y0, x1, y0 + S, color);
                Shapes.fillPx(ctx, x0, y1 - S, x1, y1, color);
                Shapes.fillPx(ctx, x0, y0 + S, x0 + S, y1 - S, color);
                Shapes.fillPx(ctx, x1 - S, y0 + S, x1, y1 - S, color);
                return;
            }
            Region ring = Shapes.corner(R, Math.min(S, R));
            Shapes.corner(ctx, ring, x0, y0, R, 0, 0, color);
            Shapes.corner(ctx, ring, x1 - R, y0, R, R, 0, color);
            Shapes.corner(ctx, ring, x0, y1 - R, R, 0, R, color);
            Shapes.corner(ctx, ring, x1 - R, y1 - R, R, R, R, color);
            if (x1 - R > x0 + R) {
                Shapes.fillPx(ctx, x0 + R, y0, x1 - R, y0 + S, color);
                Shapes.fillPx(ctx, x0 + R, y1 - S, x1 - R, y1, color);
            }
            if (y1 - R > y0 + R) {
                Shapes.fillPx(ctx, x0, y0 + R, x0 + S, y1 - R, color);
                Shapes.fillPx(ctx, x1 - S, y0 + R, x1, y1 - R, color);
            }
        }
        finally {
            Shapes.flush(ctx);
            ctx.pose().popMatrix();
        }
    }

    public static void ring(GuiGraphicsExtractor ctx, float x, float y, float w, float h, float r, int color) {
        Shapes.ring(ctx, x, y, w, h, r, color, 0.5f);
    }

    public static void circle(GuiGraphicsExtractor ctx, float cx, float cy, float radius, int color) {
        Shapes.round(ctx, cx - radius, cy - radius, radius * 2.0f, radius * 2.0f, radius, color);
    }

    public static void shadow(GuiGraphicsExtractor ctx, float x, float y, float w, float h, float r, float spread, int color) {
        int steps;
        int a = color >>> 24 & 0xFF;
        if (a == 0) {
            return;
        }
        for (int i = steps = 4; i >= 1; --i) {
            float grow = spread * (float)i / (float)steps;
            int ca = Math.round((float)a * (1.0f - ((float)i - 0.5f) / (float)steps) / (float)steps * 2.0f);
            Shapes.round(ctx, x - grow, y - grow + spread * 0.35f, w + grow * 2.0f, h + grow * 2.0f, r + grow, Math.min(255, ca) << 24 | color & 0xFFFFFF);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void icon(GuiGraphicsExtractor ctx, String name, float x, float y, float size, int color) {
        if (color >>> 24 == 0) {
            return;
        }
        Shapes.frame(ctx);
        int x0 = Shapes.px(x);
        int y0 = Shapes.py(y);
        int S = Math.max(4, Math.min(160, Math.round(size * Math.abs(FRAME[0]))));
        if (!Shapes.ready()) {
            return;
        }
        Region id = Shapes.iconTexture(name, S);
        if (id == null) {
            return;
        }
        color = Shapes.fade(color);
        Shapes.enterPixels(ctx);
        try {
            ctx.blit(RenderPipelines.GUI_TEXTURED, ATLAS_ID, x0, y0, (float)id.u(), (float)id.v(), S, S, 1024, 1024, color);
        }
        finally {
            Shapes.flush(ctx);
            ctx.pose().popMatrix();
        }
    }

    private static Region iconTexture(String name, int S) {
        String key = name + "_" + S;
        Region have = ICONS.get(key);
        if (have != null) {
            return have;
        }
        Region r = Shapes.alloc(S, S);
        NativeImage img = atlas.getPixels();
        int N = 4;
        for (int y = 0; y < S; ++y) {
            for (int x = 0; x < S; ++x) {
                int hits = 0;
                for (int sy = 0; sy < 4; ++sy) {
                    for (int sx = 0; sx < 4; ++sx) {
                        float u = ((float)x + ((float)sx + 0.5f) / 4.0f) / (float)S * 24.0f;
                        float v = ((float)y + ((float)sy + 0.5f) / 4.0f) / (float)S * 24.0f;
                        if (!Shapes.inside(name, u, v)) continue;
                        ++hits;
                    }
                }
                int a = Math.round((float)hits * 255.0f / 16.0f);
                img.setPixel(r.u() + x, r.v() + y, a << 24 | 0xFFFFFF);
            }
        }
        Shapes.upload(r);
        ICONS.put(key, r);
        return r;
    }

    private static boolean inside(String name, float u, float v) {
        switch (name) {
            case "heart": {
                return Shapes.heart(u, v, 1.0f);
            }
            case "heart_line": {
                return Shapes.heart(u, v, 1.0f) && !Shapes.heart(u, v, 0.78f);
            }
            case "star_line": {
                return Shapes.star(u, v, 10.5f) && !Shapes.star(u, v, 7.4f);
            }
            case "search": {
                return Shapes.ring(u, v, 11.0f, 11.0f, 7.0f, 1.05f) || Shapes.seg(u, v, 16.2f, 16.2f, 20.5f, 20.5f, 1.05f);
            }
            case "grid": {
                return Shapes.box(u, v, 3.0f, 3.0f, 8.0f, 8.0f) || Shapes.box(u, v, 13.0f, 3.0f, 8.0f, 8.0f) || Shapes.box(u, v, 3.0f, 13.0f, 8.0f, 8.0f) || Shapes.box(u, v, 13.0f, 13.0f, 8.0f, 8.0f);
            }
            case "chev_down": {
                return Shapes.seg(u, v, 6.0f, 9.0f, 12.0f, 15.0f, 1.05f) || Shapes.seg(u, v, 12.0f, 15.0f, 18.0f, 9.0f, 1.05f);
            }
            case "chev_up": {
                return Shapes.seg(u, v, 6.0f, 15.0f, 12.0f, 9.0f, 1.05f) || Shapes.seg(u, v, 12.0f, 9.0f, 18.0f, 15.0f, 1.05f);
            }
            case "check": {
                return Shapes.seg(u, v, 5.0f, 12.5f, 10.0f, 17.5f, 1.05f) || Shapes.seg(u, v, 10.0f, 17.5f, 19.5f, 7.0f, 1.05f);
            }
            case "plus": {
                return Shapes.seg(u, v, 12.0f, 5.0f, 12.0f, 19.0f, 1.05f) || Shapes.seg(u, v, 5.0f, 12.0f, 19.0f, 12.0f, 1.05f);
            }
            case "x": {
                return Shapes.seg(u, v, 6.0f, 6.0f, 18.0f, 18.0f, 1.05f) || Shapes.seg(u, v, 18.0f, 6.0f, 6.0f, 18.0f, 1.05f);
            }
            case "info": {
                return Shapes.ring(u, v, 12.0f, 12.0f, 9.0f, 1.05f) || Shapes.seg(u, v, 12.0f, 11.0f, 12.0f, 16.5f, 1.05f) || Shapes.dot(u, v, 12.0f, 7.8f, 1.35f);
            }
            case "help": {
                return Shapes.ring(u, v, 12.0f, 12.0f, 9.0f, 1.05f) || Shapes.dot(u, v, 12.0f, 16.8f, 1.3f) || Shapes.ring(u, v, 12.0f, 9.6f, 2.8f, 1.05f) && (!(u < 12.0f) || !(v > 9.6f)) || Shapes.seg(u, v, 12.0f, 12.4f, 12.0f, 13.6f, 1.05f);
            }
            case "keys": {
                return Shapes.frameBox(u, v, 2.5f, 6.0f, 19.0f, 12.0f, 2.5f) || Shapes.dot(u, v, 7.0f, 10.0f, 1.1f) || Shapes.dot(u, v, 12.0f, 10.0f, 1.1f) || Shapes.dot(u, v, 17.0f, 10.0f, 1.1f) || Shapes.seg(u, v, 8.0f, 14.2f, 16.0f, 14.2f, 1.05f);
            }
            case "hud": {
                return Shapes.frameBox(u, v, 3.0f, 4.5f, 18.0f, 15.0f, 2.5f) || Shapes.seg(u, v, 3.5f, 9.5f, 20.5f, 9.5f, 1.05f);
            }
            case "play": {
                return Shapes.tri(u, v, 8.0f, 5.0f, 8.0f, 19.0f, 19.0f, 12.0f);
            }
            case "discord": {
                float dx = Math.max(0.0f, Math.abs(u - 12.0f) - 5.5f);
                float dy = Math.max(0.0f, Math.abs(v - 12.5f) - 2.2f);
                return dx * dx + dy * dy <= 17.639997f && !Shapes.dot(u, v, 9.0f, 12.2f, 1.75f) && !Shapes.dot(u, v, 15.0f, 12.2f, 1.75f);
            }
            case "shield": {
                return Shapes.shield(u, v);
            }
            case "smile": {
                return Shapes.ring(u, v, 12.0f, 12.0f, 9.0f, 1.05f) || Shapes.dot(u, v, 9.0f, 9.8f, 1.35f) || Shapes.dot(u, v, 15.0f, 9.8f, 1.35f) || Shapes.ring(u, v, 12.0f, 12.2f, 4.6f, 1.05f) && v > 13.2f;
            }
        }
        return false;
    }

    private static boolean dot(float u, float v, float cx, float cy, float r) {
        float dx = u - cx;
        float dy = v - cy;
        return dx * dx + dy * dy <= r * r;
    }

    private static boolean ring(float u, float v, float cx, float cy, float r, float w) {
        float d = (float)Math.sqrt((u - cx) * (u - cx) + (v - cy) * (v - cy));
        return Math.abs(d - r) <= w;
    }

    private static boolean seg(float u, float v, float ax, float ay, float bx, float by, float w) {
        float ey;
        float dx = bx - ax;
        float dy = by - ay;
        float t = ((u - ax) * dx + (v - ay) * dy) / (dx * dx + dy * dy);
        float ex = ax + (t = Math.max(0.0f, Math.min(1.0f, t))) * dx - u;
        return ex * ex + (ey = ay + t * dy - v) * ey <= w * w;
    }

    private static boolean box(float u, float v, float x, float y, float w, float h) {
        return Shapes.frameBox(u, v, x, y, w, h, 1.5f);
    }

    private static boolean frameBox(float u, float v, float x, float y, float w, float h, float r) {
        return Shapes.inRound(u, v, x - 1.05f, y - 1.05f, w + 2.1f, h + 2.1f, r + 1.05f) && !Shapes.inRound(u, v, x + 1.05f, y + 1.05f, w - 2.1f, h - 2.1f, Math.max(0.0f, r - 1.05f));
    }

    private static boolean inRound(float u, float v, float x, float y, float w, float h, float r) {
        if (u < x || v < y || u > x + w || v > y + h) {
            return false;
        }
        float cx = Math.max(x + r, Math.min(x + w - r, u));
        float cy = Math.max(y + r, Math.min(y + h - r, v));
        return Shapes.dot(u, v, cx, cy, r);
    }

    private static boolean tri(float u, float v, float ax, float ay, float bx, float by, float cx, float cy) {
        float d1 = (u - bx) * (ay - by) - (ax - bx) * (v - by);
        float d2 = (u - cx) * (by - cy) - (bx - cx) * (v - cy);
        float d3 = (u - ax) * (cy - ay) - (cx - ax) * (v - ay);
        boolean neg = d1 < 0.0f || d2 < 0.0f || d3 < 0.0f;
        boolean pos = d1 > 0.0f || d2 > 0.0f || d3 > 0.0f;
        return !neg || !pos;
    }

    private static boolean heart(float u, float v, float k) {
        float x = (u - 12.0f) / (10.5f * k);
        float y = -(v - 12.6f) / (10.5f * k);
        float a = x * x + y * y - 0.55f;
        return a * a * a - x * x * y * y * y * 0.9f <= 0.0f;
    }

    private static boolean star(float u, float v, float r) {
        float cx = 12.0f;
        float cy = 12.8f;
        double ang = Math.atan2(v - cy, u - cx) + 1.5707963267948966;
        double seg = 1.2566370614359172;
        double a = (ang % seg + seg) % seg - seg / 2.0;
        double inner = (double)r * 0.45;
        double t = Math.abs(a) / (seg / 2.0);
        double edge = inner + ((double)r - inner) * (1.0 - t);
        double d = Math.sqrt((u - cx) * (u - cx) + (v - cy) * (v - cy));
        return d <= edge * (1.0 - 0.18 * t * (1.0 - t));
    }

    /*
     * Unable to fully structure code
     */
    private static boolean shield(float u, float v) {
        if (!(v >= 3.0f) || !(v <= 21.0f)) ** GOTO lbl-1000
        v0 = Math.abs(u - 12.0f);
        v1 = v < 12.0f ? 8.0f : 8.0f * (21.0f - v) / 9.0f;
        if (v0 <= v1) {
            v2 = true;
        } else lbl-1000:
        // 2 sources

        {
            v2 = outer = false;
        }
        if (!(v >= 5.2f) || !(v <= 18.6f)) ** GOTO lbl-1000
        v3 = Math.abs(u - 12.0f);
        v4 = v < 12.0f ? 5.9f : 5.9f * (18.6f - v) / 6.6f;
        if (v3 <= v4) {
            v5 = true;
        } else lbl-1000:
        // 2 sources

        {
            v5 = false;
        }
        inner = v5;
        return outer != false && inner == false;
    }

    static boolean pixelLook() {
        return V2.INSTANCE.designMinecraft;
    }

    private static int radius(float r, int w, int h) {
        if (Shapes.pixelLook()) {
            r = Math.min(r, 2.0f);
        }
        int R = Math.round(Math.max(0.0f, r) * Math.abs(FRAME[0]));
        return Math.max(0, Math.min(Math.min(R, 160), Math.min(w, h) / 2));
    }

    private static void fillRound(GuiGraphicsExtractor ctx, int x0, int y0, int x1, int y1, int R, int color) {
        if (R <= 0) {
            Shapes.fillPx(ctx, x0, y0, x1, y1, color);
            return;
        }
        Region disc = Shapes.corner(R, 0);
        Shapes.corner(ctx, disc, x0, y0, R, 0, 0, color);
        Shapes.corner(ctx, disc, x1 - R, y0, R, R, 0, color);
        Shapes.corner(ctx, disc, x0, y1 - R, R, 0, R, color);
        Shapes.corner(ctx, disc, x1 - R, y1 - R, R, R, R, color);
        if (x1 - R > x0 + R) {
            Shapes.fillPx(ctx, x0 + R, y0, x1 - R, y1, color);
        }
        if (y1 - R > y0 + R) {
            Shapes.fillPx(ctx, x0, y0 + R, x0 + R, y1 - R, color);
            Shapes.fillPx(ctx, x1 - R, y0 + R, x1, y1 - R, color);
        }
    }

    private static void corner(GuiGraphicsExtractor ctx, Region r, int x, int y, int R, int u, int v, int color) {
        float u0 = (float)(r.u() + u) / 1024.0f;
        float v0 = (float)(r.v() + v) / 1024.0f;
        Shapes.quad(x, y, x + R, y + R, u0, v0, u0 + (float)R / 1024.0f, v0 + (float)R / 1024.0f, color);
    }

    private static Region corner(int R, int stroke) {
        boolean hard = Shapes.pixelLook();
        long key = (long)R << 16 | (long)stroke << 1 | (hard ? 1L : 0L);
        Region have = CORNERS.get(key);
        if (have != null) {
            return have;
        }
        int size = 2 * R;
        Region r = Shapes.alloc(size, size);
        NativeImage img = atlas.getPixels();
        float outer = R;
        float inner = stroke <= 0 ? -1.0f : (float)(R - stroke);
        for (int y = 0; y < size; ++y) {
            for (int x = 0; x < size; ++x) {
                int hits = 0;
                for (int sy = 0; sy < 4; ++sy) {
                    float py = (float)y + ((float)sy + 0.5f) / 4.0f - (float)R;
                    for (int sx = 0; sx < 4; ++sx) {
                        float px = (float)x + ((float)sx + 0.5f) / 4.0f - (float)R;
                        float d = (float)Math.sqrt(px * px + py * py);
                        if (!(d <= outer) || !(d >= inner)) continue;
                        ++hits;
                    }
                }
                int a = hard ? (hits >= 8 ? 255 : 0) : Math.round((float)hits * 255.0f / 16.0f);
                img.setPixel(r.u() + x, r.v() + y, a << 24 | 0xFFFFFF);
            }
        }
        Shapes.upload(r);
        CORNERS.put(key, r);
        return r;
    }

    public static int mix(int a, int b, float t) {
        float f = Math.max(0.0f, Math.min(1.0f, t));
        int out = 0;
        for (int shift = 0; shift <= 24; shift += 8) {
            int ca = a >>> shift & 0xFF;
            int cb = b >>> shift & 0xFF;
            out |= (Math.round((float)ca + (float)(cb - ca) * f) & 0xFF) << shift;
        }
        return out;
    }

    static {
        CORNERS = new HashMap<Long, Region>();
        quads = new float[144];
        FRAME = new float[4];
        ICONS = new HashMap<String, Region>();
    }

    private record Region(int u, int v, int w, int h) {
    }

    private record Batch(TextureSetup textureSetup, Matrix3x2f pose, float[] q, int n, ScreenRectangle scissorArea, ScreenRectangle bounds) implements GuiElementRenderState
    {
        public void buildVertices(VertexConsumer vc) {
            for (int k = 0; k < this.n; ++k) {
                int i = k * 9;
                int c = Float.floatToRawIntBits(this.q[i + 8]);
                vc.addVertexWith2DPose((Matrix3x2fc)this.pose, this.q[i], this.q[i + 1]).setUv(this.q[i + 4], this.q[i + 5]).setColor(c);
                vc.addVertexWith2DPose((Matrix3x2fc)this.pose, this.q[i], this.q[i + 3]).setUv(this.q[i + 4], this.q[i + 7]).setColor(c);
                vc.addVertexWith2DPose((Matrix3x2fc)this.pose, this.q[i + 2], this.q[i + 3]).setUv(this.q[i + 6], this.q[i + 7]).setColor(c);
                vc.addVertexWith2DPose((Matrix3x2fc)this.pose, this.q[i + 2], this.q[i + 1]).setUv(this.q[i + 6], this.q[i + 5]).setColor(c);
            }
        }

        public RenderPipeline pipeline() {
            return RenderPipelines.GUI_TEXTURED;
        }
    }

    private record FreeQuads(TextureSetup textureSetup, Matrix3x2f pose, float[] p, int[] c, int n, float u, float v, ScreenRectangle scissorArea, ScreenRectangle bounds) implements GuiElementRenderState
    {
        public void buildVertices(VertexConsumer vc) {
            for (int k = 0; k < this.n; ++k) {
                int i = k * 8;
                vc.addVertexWith2DPose((Matrix3x2fc)this.pose, this.p[i], this.p[i + 1]).setUv(this.u, this.v).setColor(this.c[k]);
                vc.addVertexWith2DPose((Matrix3x2fc)this.pose, this.p[i + 2], this.p[i + 3]).setUv(this.u, this.v).setColor(this.c[k]);
                vc.addVertexWith2DPose((Matrix3x2fc)this.pose, this.p[i + 4], this.p[i + 5]).setUv(this.u, this.v).setColor(this.c[k]);
                vc.addVertexWith2DPose((Matrix3x2fc)this.pose, this.p[i + 6], this.p[i + 7]).setUv(this.u, this.v).setColor(this.c[k]);
            }
        }

        public RenderPipeline pipeline() {
            return RenderPipelines.GUI_TEXTURED;
        }
    }
}

