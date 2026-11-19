/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.platform.NativeImage
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.FilterMode
 *  com.mojang.blaze3d.textures.GpuSampler
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.PlayerFaceExtractor
 *  net.minecraft.client.gui.navigation.ScreenRectangle
 *  net.minecraft.client.gui.render.TextureSetup
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.client.renderer.state.gui.GuiElementRenderState
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  org.joml.Matrix3x2f
 *  org.joml.Matrix3x2fStack
 *  org.joml.Matrix3x2fc
 */
package net.friendswithmoney.fwm.stream.fx;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.friendswithmoney.fwm.ui.FwmGfx;
import net.friendswithmoney.fwm.ui.gfx.Shapes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix3x2fc;

public final class FxCanvas {
    public static final float STAGE_H = 360.0f;
    public GuiGraphicsExtractor ctx;
    public float sw;
    public float sh = 360.0f;
    public float k = 1.0f;
    public float px = 1.0f;
    public boolean preview;
    public boolean shake = true;
    private Font font;
    private float gui = 1.0f;
    public static final int GLASS_TOP = -434757861;
    public static final int GLASS_BOTTOM = -435482354;
    public static final int RIM = 0x30FFFFFF;
    public static final int SHEEN = 0x1EFFFFFF;
    private final Map<String, Component> bold = new HashMap<String, Component>();
    private static final Map<String, ItemStack> ITEMS = new HashMap<String, ItemStack>();
    private static final int TW = 128;
    private static final int TH = 64;
    private static final float GU0 = 0.00390625f;
    private static final float GV0 = 0.0078125f;
    private static final float GU1 = 0.49609375f;
    private static final float GV1 = 0.9921875f;
    private static final float DU0 = 0.51953125f;
    private static final float DV0 = 0.0078125f;
    private static final float DU1 = 0.76171875f;
    private static final float DV1 = 0.4921875f;
    private static final float WU = 0.8125f;
    private static final float WV = 0.0625f;
    private static TextureSetup fxSetup;
    private static boolean fxFailed;
    private float[] fxV = new float[32768];
    private int[] fxC = new int[8192];
    private int fxN;
    private boolean building;
    private float fxMinX;
    private float fxMinY;
    private float fxMaxX;
    private float fxMaxY;
    private boolean flipWinding;
    private static final float[] COS;
    private static final float[] SIN;

    public void begin(GuiGraphicsExtractor ctx, float x, float y, float w, float h, boolean preview) {
        this.ctx = ctx;
        this.preview = preview;
        this.k = h / 360.0f;
        this.sw = w / this.k;
        Minecraft mc = Minecraft.getInstance();
        this.font = mc.font;
        this.gui = mc.getWindow() == null ? 1.0f : (float)mc.getWindow().getGuiScale();
        Matrix3x2fStack m = ctx.pose();
        m.pushMatrix();
        m.translate(x, y);
        m.scale(this.k, this.k);
        this.px = Math.max(0.01f, Math.abs(m.m00()) * this.gui);
        this.fxN = 0;
        this.building = false;
    }

    public void end() {
        if (this.building) {
            this.fxEnd();
        }
        this.ctx.pose().popMatrix();
    }

    public float hair() {
        return 1.0f / this.px;
    }

    public float snap(float v) {
        return (float)Math.round(v * this.px) / this.px;
    }

    public void push() {
        this.ctx.pose().pushMatrix();
    }

    public void pop() {
        this.ctx.pose().popMatrix();
    }

    public void translate(float x, float y) {
        this.ctx.pose().translate(x, y);
    }

    public void scale(float x, float y) {
        this.ctx.pose().scale(x, y);
    }

    public void rotate(float radians) {
        this.ctx.pose().rotate(radians);
    }

    public void layerUp() {
        if (this.building) {
            this.fxEnd();
        }
        this.ctx.guiRenderState.up();
    }

    public static void newRootLayer(GuiGraphicsExtractor ctx) {
        ctx.nextStratum();
    }

    public void clip(float x, float y, float w, float h) {
        if (this.building) {
            this.fxEnd();
        }
        this.ctx.enableScissor((int)Math.floor(x), (int)Math.floor(y), (int)Math.ceil(x + w), (int)Math.ceil(y + h));
    }

    public void unclip() {
        if (this.building) {
            this.fxEnd();
        }
        this.ctx.disableScissor();
    }

    public void fill(float x, float y, float w, float h, int argb) {
        if (w <= 0.0f || h <= 0.0f || argb >>> 24 == 0) {
            return;
        }
        Matrix3x2fStack m = this.ctx.pose();
        m.pushMatrix();
        m.translate(x, y);
        m.scale(w / 64.0f, h / 64.0f);
        this.ctx.fill(0, 0, 64, 64, argb);
        m.popMatrix();
    }

    public void gradient(float x, float y, float w, float h, int top, int bottom) {
        if (w <= 0.0f || h <= 0.0f) {
            return;
        }
        Matrix3x2fStack m = this.ctx.pose();
        m.pushMatrix();
        m.translate(x, y);
        m.scale(w / 64.0f, h / 64.0f);
        this.ctx.fillGradient(0, 0, 64, 64, top, bottom);
        m.popMatrix();
    }

    public void round(float x, float y, float w, float h, float r, int argb) {
        Shapes.round(this.ctx, x, y, w, h, r, argb);
    }

    public void roundGradient(float x, float y, float w, float h, float r, int top, int bottom) {
        Shapes.roundGradient(this.ctx, x, y, w, h, r, top, bottom);
    }

    public void ring(float x, float y, float w, float h, float r, int argb, float stroke) {
        Shapes.ring(this.ctx, x, y, w, h, r, argb, stroke);
    }

    public void line(float x0, float y0, float x1, float y1, float width, int argb) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        float len = (float)Math.sqrt(dx * dx + dy * dy);
        if (len < 0.01f) {
            return;
        }
        this.push();
        this.translate(x0, y0);
        this.rotate((float)Math.atan2(dy, dx));
        this.fill(-width / 2.0f, -width / 2.0f, len + width, width, argb);
        this.pop();
    }

    public void panel(float x, float y, float w, float h, float r, float a, int accent) {
        if (a <= 0.004f || w <= 0.0f || h <= 0.0f) {
            return;
        }
        if (this.building) {
            this.fxEnd();
        }
        Shapes.roundGradient(this.ctx, x, y, w, h, r, FxCanvas.alpha(-434757861, a), FxCanvas.alpha(-435482354, a));
        int rim = FxCanvas.mix(-1, accent | 0xFF000000, 0.45f);
        Shapes.ring(this.ctx, x, y, w, h, r, FxCanvas.withAlpha(rim, Math.round(112.0f * a)), 0.01f);
        float hl = this.hair();
        Shapes.round(this.ctx, x + r * 0.8f, y + hl, w - r * 1.6f, hl, 0.0f, FxCanvas.alpha(0x1EFFFFFF, a));
    }

    private Component boldText(String s) {
        Component t = this.bold.get(s);
        if (t == null) {
            if (this.bold.size() > 512) {
                this.bold.clear();
            }
            t = Component.literal((String)s).withStyle(ChatFormatting.BOLD);
            this.bold.put(s, t);
        }
        return t;
    }

    public float fs(float size) {
        float perFontPx = size / 8.0f * this.px;
        if (perFontPx < 1.5f) {
            return size;
        }
        return (float)Math.round(perFontPx) * 8.0f / this.px;
    }

    public float width(String s, float size, boolean isBold) {
        if (s == null || s.isEmpty()) {
            return 0.0f;
        }
        int w = isBold ? this.font.width((FormattedText)this.boldText(s)) : this.font.width(s);
        return (float)(w - 1) * this.fs(size) / 8.0f;
    }

    public float capH(float size) {
        return 7.0f * this.fs(size) / 8.0f;
    }

    public void text(String s, float x, float y, float size, int argb, boolean isBold) {
        if (argb >>> 24 < 4 || s == null || s.isEmpty()) {
            return;
        }
        Matrix3x2fStack m = this.ctx.pose();
        m.pushMatrix();
        m.translate(x, y);
        if (Math.abs(m.m01()) < 1.0E-5f && Math.abs(m.m10()) < 1.0E-5f && Math.abs(m.m00()) > 1.0E-5f && Math.abs(m.m11()) > 1.0E-5f) {
            float tx = m.m20() * this.gui;
            float ty = m.m21() * this.gui;
            float dx = ((float)Math.round(tx) - tx) / this.gui;
            float dy = ((float)Math.round(ty) - ty) / this.gui;
            m.translate(dx / m.m00(), dy / m.m11());
        }
        float f = this.fs(size) / 8.0f;
        m.scale(f, f);
        if (isBold) {
            this.ctx.text(this.font, this.boldText(s), 0, 0, argb, false);
        } else {
            this.ctx.text(this.font, s, 0, 0, argb, false);
        }
        m.popMatrix();
    }

    public void textC(String s, float cx, float y, float size, int argb, boolean isBold) {
        this.text(s, cx - this.width(s, size, isBold) / 2.0f, y, size, argb, isBold);
    }

    public void textR(String s, float rx, float y, float size, int argb, boolean isBold) {
        this.text(s, rx - this.width(s, size, isBold), y, size, argb, isBold);
    }

    public void outlined(String s, float x, float y, float size, int argb, int edge, boolean isBold) {
        this.text(s, x, y, size, argb, isBold);
    }

    public void item(ItemStack stack, float cx, float cy, float size, float flip) {
        this.item(stack, cx, cy, size, flip, 0.0f);
    }

    public void item(ItemStack stack, float cx, float cy, float size, float flip, float radians) {
        if (stack == null || stack.isEmpty() || size <= 0.2f) {
            return;
        }
        float fx = Math.abs(flip) < 0.035f ? 0.035f : Math.abs(flip);
        Matrix3x2fStack m = this.ctx.pose();
        m.pushMatrix();
        m.translate(cx, cy);
        if (radians != 0.0f) {
            m.rotate(radians);
        }
        m.scale(size / 16.0f * fx, size / 16.0f);
        this.ctx.item(stack, -8, -8);
        m.popMatrix();
    }

    public void itemTurned(ItemStack stack, float cx, float cy, float size, float radians) {
        this.item(stack, cx, cy, size, 1.0f, radians);
    }

    public void head(String name, float x, float y, float size, float alpha) {
        int a = Math.round(Math.max(0.0f, Math.min(1.0f, alpha)) * 255.0f);
        if (a < 4) {
            return;
        }
        if (this.building) {
            this.fxEnd();
        }
        Matrix3x2fStack m = this.ctx.pose();
        m.pushMatrix();
        m.translate(x, y);
        m.scale(size / 8.0f, size / 8.0f);
        Identifier skin = FwmGfx.skinTexture(name);
        PlayerFaceExtractor.extractRenderState((GuiGraphicsExtractor)this.ctx, (Identifier)skin, (int)0, (int)0, (int)8, (boolean)true, (boolean)false, (int)(a << 24 | 0xFFFFFF));
        m.popMatrix();
    }

    public static ItemStack itemOf(String id) {
        ItemStack made;
        if (id == null || id.isEmpty()) {
            return new ItemStack((ItemLike)Items.DIAMOND);
        }
        ItemStack have = ITEMS.get(id);
        if (have != null) {
            return have;
        }
        try {
            Identifier key = Identifier.tryParse((String)id);
            Item item = key == null ? Items.DIAMOND : (Item)BuiltInRegistries.ITEM.getValue(key);
            made = new ItemStack((ItemLike)(item == null || item == Items.AIR ? Items.DIAMOND : item));
        }
        catch (RuntimeException e) {
            made = new ItemStack((ItemLike)Items.DIAMOND);
        }
        ITEMS.put(id, made);
        return made;
    }

    private static TextureSetup fxTexture() {
        if (fxSetup != null || fxFailed) {
            return fxSetup;
        }
        try {
            double d;
            double dy;
            double dx;
            int x;
            int y;
            NativeImage img = new NativeImage(128, 64, true);
            img.fillRect(0, 0, 128, 64, 0);
            for (y = 0; y < 64; ++y) {
                for (x = 0; x < 64; ++x) {
                    dx = ((double)x + 0.5 - 32.0) / 32.0;
                    dy = ((double)y + 0.5 - 32.0) / 32.0;
                    d = Math.sqrt(dx * dx + dy * dy);
                    double g = Math.exp(-d * d * 4.2);
                    double edge = d >= 1.0 ? 0.0 : 1.0 - FxCanvas.smooth(0.72, 1.0, d);
                    int a = (int)Math.round(255.0 * g * edge);
                    img.setPixel(x, y, Math.max(0, Math.min(255, a)) << 24 | 0xFFFFFF);
                }
            }
            for (y = 0; y < 32; ++y) {
                for (x = 0; x < 32; ++x) {
                    dx = (double)x + 0.5 - 16.0;
                    dy = (double)y + 0.5 - 16.0;
                    d = Math.sqrt(dx * dx + dy * dy);
                    double a = Math.max(0.0, Math.min(1.0, (15.0 - d) / 1.4 + 0.5));
                    img.setPixel(66 + x, y, (int)Math.round(a * 255.0) << 24 | 0xFFFFFF);
                }
            }
            for (y = 0; y < 8; ++y) {
                for (x = 0; x < 8; ++x) {
                    img.setPixel(100 + x, y, -1);
                }
            }
            DynamicTexture tex = new DynamicTexture(() -> "fwm donation fx", img);
            Minecraft.getInstance().getTextureManager().register(Identifier.fromNamespaceAndPath((String)"fwm", (String)"donation_fx"), (AbstractTexture)tex);
            fxSetup = TextureSetup.singleTexture((GpuTextureView)tex.getTextureView(), (GpuSampler)RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
        }
        catch (Throwable t) {
            fxFailed = true;
        }
        return fxSetup;
    }

    private static double smooth(double e0, double e1, double x) {
        double t = Math.max(0.0, Math.min(1.0, (x - e0) / (e1 - e0)));
        return t * t * (3.0 - 2.0 * t);
    }

    public void fx() {
        if (this.building) {
            this.fxEnd();
        }
        this.building = true;
        this.fxN = 0;
        this.fxMinY = Float.MAX_VALUE;
        this.fxMinX = Float.MAX_VALUE;
        this.fxMaxY = -3.4028235E38f;
        this.fxMaxX = -3.4028235E38f;
        Matrix3x2fStack m = this.ctx.pose();
        this.flipWinding = m.m00() * m.m11() - m.m01() * m.m10() < 0.0f;
    }

    public void fxEnd() {
        if (!this.building) {
            return;
        }
        this.building = false;
        int n = this.fxN;
        this.fxN = 0;
        TextureSetup tex = FxCanvas.fxTexture();
        if (n == 0 || tex == null) {
            return;
        }
        Matrix3x2f pose = new Matrix3x2f((Matrix3x2fc)this.ctx.pose());
        ScreenRectangle scissor = this.ctx.scissorStack.peek();
        ScreenRectangle bounds = new ScreenRectangle((int)Math.floor(this.fxMinX), (int)Math.floor(this.fxMinY), Math.max(1, (int)Math.ceil(this.fxMaxX - this.fxMinX)), Math.max(1, (int)Math.ceil(this.fxMaxY - this.fxMinY))).transformMaxBounds((Matrix3x2fc)pose);
        if (scissor != null && (bounds = scissor.intersection(bounds)) == null) {
            return;
        }
        float[] v = Arrays.copyOf(this.fxV, n * 16);
        int[] c = Arrays.copyOf(this.fxC, n * 4);
        this.ctx.guiRenderState.addGuiElement((GuiElementRenderState)new Mesh(tex, pose, v, c, n, scissor, bounds));
    }

    private void grow() {
        this.fxV = Arrays.copyOf(this.fxV, this.fxV.length * 2);
        this.fxC = Arrays.copyOf(this.fxC, this.fxC.length * 2);
    }

    private void quadRaw(float x0, float y0, float u0, float v0, int c0, float x1, float y1, float u1, float v1, int c1, float x2, float y2, float u2, float v2, int c2, float x3, float y3, float u3, float v3, int c3) {
        if (!this.building) {
            return;
        }
        if ((c0 | c1 | c2 | c3) >>> 24 == 0) {
            return;
        }
        if ((this.fxN + 1) * 16 > this.fxV.length) {
            this.grow();
        }
        float area = x0 * y1 - x1 * y0 + (x1 * y2 - x2 * y1) + (x2 * y3 - x3 * y2) + (x3 * y0 - x0 * y3);
        boolean swap = this.flipWinding ? area < 0.0f : area > 0.0f;
        int i = this.fxN * 16;
        int j = this.fxN * 4;
        float[] v = this.fxV;
        v[i] = x0;
        v[i + 1] = y0;
        v[i + 2] = u0;
        v[i + 3] = v0;
        if (swap) {
            v[i + 4] = x3;
            v[i + 5] = y3;
            v[i + 6] = u3;
            v[i + 7] = v3;
            v[i + 12] = x1;
            v[i + 13] = y1;
            v[i + 14] = u1;
            v[i + 15] = v1;
            this.fxC[j + 1] = c3;
            this.fxC[j + 3] = c1;
        } else {
            v[i + 4] = x1;
            v[i + 5] = y1;
            v[i + 6] = u1;
            v[i + 7] = v1;
            v[i + 12] = x3;
            v[i + 13] = y3;
            v[i + 14] = u3;
            v[i + 15] = v3;
            this.fxC[j + 1] = c1;
            this.fxC[j + 3] = c3;
        }
        v[i + 8] = x2;
        v[i + 9] = y2;
        v[i + 10] = u2;
        v[i + 11] = v2;
        this.fxC[j] = c0;
        this.fxC[j + 2] = c2;
        ++this.fxN;
        float minX = Math.min(Math.min(x0, x1), Math.min(x2, x3));
        float maxX = Math.max(Math.max(x0, x1), Math.max(x2, x3));
        float minY = Math.min(Math.min(y0, y1), Math.min(y2, y3));
        float maxY = Math.max(Math.max(y0, y1), Math.max(y2, y3));
        if (minX < this.fxMinX) {
            this.fxMinX = minX;
        }
        if (minY < this.fxMinY) {
            this.fxMinY = minY;
        }
        if (maxX > this.fxMaxX) {
            this.fxMaxX = maxX;
        }
        if (maxY > this.fxMaxY) {
            this.fxMaxY = maxY;
        }
    }

    public void quad(float x0, float y0, int c0, float x1, float y1, int c1, float x2, float y2, int c2, float x3, float y3, int c3) {
        this.quadRaw(x0, y0, 0.8125f, 0.0625f, c0, x1, y1, 0.8125f, 0.0625f, c1, x2, y2, 0.8125f, 0.0625f, c2, x3, y3, 0.8125f, 0.0625f, c3);
    }

    public void rect(float x, float y, float w, float h, int argb) {
        if (w <= 0.0f || h <= 0.0f) {
            return;
        }
        this.quad(x, y, argb, x, y + h, argb, x + w, y + h, argb, x + w, y, argb);
    }

    public void rectH(float x, float y, float w, float h, int left, int right) {
        if (w <= 0.0f || h <= 0.0f) {
            return;
        }
        this.quad(x, y, left, x, y + h, left, x + w, y + h, right, x + w, y, right);
    }

    public void rectV(float x, float y, float w, float h, int top, int bottom) {
        if (w <= 0.0f || h <= 0.0f) {
            return;
        }
        this.quad(x, y, top, x, y + h, bottom, x + w, y + h, bottom, x + w, y, top);
    }

    public void rectRot(float cx, float cy, float w, float h, float ang, int argb) {
        float c = (float)Math.cos(ang);
        float s = (float)Math.sin(ang);
        float ax = c * w / 2.0f;
        float ay = s * w / 2.0f;
        float bx = -s * h / 2.0f;
        float by = c * h / 2.0f;
        this.quad(cx - ax - bx, cy - ay - by, argb, cx - ax + bx, cy - ay + by, argb, cx + ax + bx, cy + ay + by, argb, cx + ax - bx, cy + ay - by, argb);
    }

    public void glow(float cx, float cy, float r, int argb) {
        this.glow(cx, cy, r, r, argb);
    }

    public void glow(float cx, float cy, float rx, float ry, int argb) {
        if (rx <= 0.05f || ry <= 0.05f || argb >>> 24 == 0) {
            return;
        }
        this.quadRaw(cx - rx, cy - ry, 0.00390625f, 0.0078125f, argb, cx - rx, cy + ry, 0.00390625f, 0.9921875f, argb, cx + rx, cy + ry, 0.49609375f, 0.9921875f, argb, cx + rx, cy - ry, 0.49609375f, 0.0078125f, argb);
    }

    public void disc(float cx, float cy, float r, int argb) {
        if (r <= 0.05f || argb >>> 24 == 0) {
            return;
        }
        float rr = r * 16.0f / 15.0f;
        this.quadRaw(cx - rr, cy - rr, 0.51953125f, 0.0078125f, argb, cx - rr, cy + rr, 0.51953125f, 0.4921875f, argb, cx + rr, cy + rr, 0.76171875f, 0.4921875f, argb, cx + rr, cy - rr, 0.76171875f, 0.0078125f, argb);
    }

    public void streak(float x0, float y0, float x1, float y1, float w0, float w1, int c0, int c1) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        float len = (float)Math.sqrt(dx * dx + dy * dy);
        if (len < 0.001f) {
            return;
        }
        float nx = -dy / len;
        float ny = dx / len;
        float h0 = w0 / 2.0f;
        float h1 = w1 / 2.0f;
        this.quad(x0 + nx * h0, y0 + ny * h0, c0, x0 - nx * h0, y0 - ny * h0, c0, x1 - nx * h1, y1 - ny * h1, c1, x1 + nx * h1, y1 + ny * h1, c1);
    }

    public void wave(float cx, float cy, float r, float th, int argb) {
        if (r <= 0.0f || th <= 0.0f || argb >>> 24 == 0) {
            return;
        }
        int clear = argb & 0xFFFFFF;
        float ri = Math.max(0.0f, r - th / 2.0f);
        float ro = r + th / 2.0f;
        for (int i = 0; i < 64; ++i) {
            float c0 = COS[i];
            float s0 = SIN[i];
            float c1 = COS[i + 1];
            float s1 = SIN[i + 1];
            this.quad(cx + c0 * ri, cy + s0 * ri, clear, cx + c1 * ri, cy + s1 * ri, clear, cx + c1 * r, cy + s1 * r, argb, cx + c0 * r, cy + s0 * r, argb);
            this.quad(cx + c0 * r, cy + s0 * r, argb, cx + c1 * r, cy + s1 * r, argb, cx + c1 * ro, cy + s1 * ro, clear, cx + c0 * ro, cy + s0 * ro, clear);
        }
    }

    public void ray(float cx, float cy, float ang, float half, float r0, float r1, int argb) {
        if (argb >>> 24 == 0) {
            return;
        }
        float ca = (float)Math.cos(ang - half);
        float sa = (float)Math.sin(ang - half);
        float cm = (float)Math.cos(ang);
        float sm = (float)Math.sin(ang);
        float cb = (float)Math.cos(ang + half);
        float sb = (float)Math.sin(ang + half);
        float rm = r0 + (r1 - r0) * 0.4f;
        int mid = FxCanvas.alpha(argb, 0.4f);
        int clear = argb & 0xFFFFFF;
        for (int side = 0; side < 2; ++side) {
            float ex = side == 0 ? ca : cb;
            float ey = side == 0 ? sa : sb;
            this.quad(cx + ex * r0, cy + ey * r0, clear, cx + ex * rm, cy + ey * rm, clear, cx + cm * rm, cy + sm * rm, mid, cx + cm * r0, cy + sm * r0, argb);
            this.quad(cx + ex * rm, cy + ey * rm, clear, cx + ex * r1, cy + ey * r1, clear, cx + cm * r1, cy + sm * r1, clear, cx + cm * rm, cy + sm * rm, mid);
        }
    }

    public void vignette(float inner, float outer, int rgb) {
        int gx = 10;
        int gy = 6;
        float m = 24.0f;
        float cw = (this.sw + m * 2.0f) / (float)gx;
        float ch = (this.sh + m * 2.0f) / (float)gy;
        for (int j = 0; j < gy; ++j) {
            for (int i = 0; i < gx; ++i) {
                float x0 = (float)i * cw - m;
                float y0 = (float)j * ch - m;
                float x1 = x0 + cw;
                float y1 = y0 + ch;
                this.quad(x0, y0, this.vig(x0, y0, inner, outer, rgb), x0, y1, this.vig(x0, y1, inner, outer, rgb), x1, y1, this.vig(x1, y1, inner, outer, rgb), x1, y0, this.vig(x1, y0, inner, outer, rgb));
            }
        }
    }

    private int vig(float x, float y, float inner, float outer, int rgb) {
        float dx = (x - this.sw / 2.0f) / (this.sw / 2.0f);
        float dy = (y - this.sh / 2.0f) / (this.sh / 2.0f);
        float d = (float)Math.sqrt(dx * dx * 0.8f + dy * dy * 1.2f);
        float t = Math.max(0.0f, Math.min(1.0f, (d - 0.15f) / 1.1f));
        t = t * t * (3.0f - 2.0f * t);
        float a = inner + (outer - inner) * t;
        return FxCanvas.withAlpha(rgb, Math.round(a * 255.0f));
    }

    public static int alpha(int argb, float a) {
        if (a >= 1.0f) {
            return argb;
        }
        if (a <= 0.0f) {
            return argb & 0xFFFFFF;
        }
        int al = Math.round((float)(argb >>> 24 & 0xFF) * a);
        return al << 24 | argb & 0xFFFFFF;
    }

    public static int withAlpha(int rgb, int a) {
        return Math.max(0, Math.min(255, a)) << 24 | rgb & 0xFFFFFF;
    }

    public static int mix(int a, int b, float t) {
        return Shapes.mix(a, b, Math.max(0.0f, Math.min(1.0f, t)));
    }

    static {
        COS = new float[65];
        SIN = new float[65];
        for (int i = 0; i <= 64; ++i) {
            FxCanvas.COS[i] = (float)Math.cos(Math.PI * 2 * (double)i / 64.0);
            FxCanvas.SIN[i] = (float)Math.sin(Math.PI * 2 * (double)i / 64.0);
        }
    }

    private record Mesh(TextureSetup textureSetup, Matrix3x2f pose, float[] v, int[] c, int n, ScreenRectangle scissorArea, ScreenRectangle bounds) implements GuiElementRenderState
    {
        public void buildVertices(VertexConsumer vc) {
            for (int k = 0; k < this.n * 4; ++k) {
                int i = k * 4;
                vc.addVertexWith2DPose((Matrix3x2fc)this.pose, this.v[i], this.v[i + 1]).setUv(this.v[i + 2], this.v[i + 3]).setColor(this.c[k]);
            }
        }

        public RenderPipeline pipeline() {
            return RenderPipelines.GUI_TEXTURED;
        }
    }
}

