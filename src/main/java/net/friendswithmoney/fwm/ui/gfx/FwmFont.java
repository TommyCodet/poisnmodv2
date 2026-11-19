/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.font.GlyphInfo
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.platform.NativeImage
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.Font$DisplayMode
 *  net.minecraft.client.gui.Font$Provider
 *  net.minecraft.client.gui.GlyphSource
 *  net.minecraft.client.gui.font.GlyphRenderTypes
 *  net.minecraft.client.gui.font.TextRenderable$Styled
 *  net.minecraft.client.gui.font.glyphs.BakedGlyph
 *  net.minecraft.client.gui.font.glyphs.EffectGlyph
 *  net.minecraft.client.renderer.rendertype.RenderType
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.network.chat.FontDescription
 *  net.minecraft.network.chat.Style
 *  net.minecraft.resources.Identifier
 *  net.minecraft.util.RandomSource
 *  org.joml.Matrix4fc
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.friendswithmoney.fwm.ui.gfx;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.InputStream;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.mixin.TextRendererAccessor;
import net.friendswithmoney.fwm.ui.gfx.Shapes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EffectGlyph;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4fc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FwmFont {
    private static final Logger LOG = LoggerFactory.getLogger((String)"FWM font");
    public static final float BODY = 7.5f;
    private static final int ATLAS = 1024;
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath((String)"fwm", (String)"ui_font_atlas");
    private static Font base;
    private static boolean failed;
    private static DynamicTexture atlas;
    private static GlyphRenderTypes layers;
    private static int shelfX;
    private static int shelfY;
    private static int shelfH;
    private static final Long2ObjectOpenHashMap<Slot> SLOTS;
    private static final Int2ObjectOpenHashMap<float[]> ADVANCE;
    private static final Int2ObjectOpenHashMap<net.minecraft.client.gui.Font> RENDERERS;
    private static final Int2ObjectOpenHashMap<Provider> PROVIDERS;
    private static final FontRenderContext FRC;
    private static final float WORLD_PX = 6.0f;
    private static final Int2ObjectOpenHashMap<Provider> WORLD;
    private static final float BOLD_STROKE = 0.045f;

    private FwmFont() {
    }

    public static net.minecraft.client.gui.Font ui() {
        return FwmFont.renderer(7.5f);
    }

    public static net.minecraft.client.gui.Font renderer(float size) {
        Minecraft client = Minecraft.getInstance();
        if (!FwmFont.enabled()) {
            return client.font;
        }
        int key = Math.round(size * 4.0f);
        net.minecraft.client.gui.Font have = (net.minecraft.client.gui.Font)RENDERERS.get(key);
        if (have != null) {
            return have;
        }
        final float s = (float)key / 4.0f;
        net.minecraft.client.gui.Font made = new net.minecraft.client.gui.Font(new Font.Provider(){

            public GlyphSource glyphs(FontDescription source) {
                if (FontDescription.DEFAULT.equals((Object)source) && FwmFont.enabled()) {
                    return FwmFont.provider(s);
                }
                return FwmFont.vanilla().glyphs(source);
            }

            public EffectGlyph effect() {
                return FwmFont.vanilla().effect();
            }
        });
        RENDERERS.put(key, (Object)made);
        return made;
    }

    public static boolean enabled() {
        V2 v2 = V2.INSTANCE;
        if (failed || !v2.smoothFont || v2.designMinecraft) {
            return false;
        }
        return FwmFont.font() != null;
    }

    private static Font.Provider vanilla() {
        return ((TextRendererAccessor)Minecraft.getInstance().font).fwm$fonts();
    }

    private static Font font() {
        if (base != null || failed) {
            return base;
        }
        try (InputStream in = FwmFont.class.getResourceAsStream("/assets/fwm/ui/Inter.ttf");){
            if (in == null) {
                throw new IllegalStateException("Inter.ttf missing");
            }
            base = Font.createFont(0, in);
        }
        catch (Throwable t) {
            failed = true;
            LOG.warn("FWM: outline font unavailable, using Minecraft's font", t);
        }
        return base;
    }

    private static Provider provider(float size) {
        int key = Math.round(size * 4.0f);
        Provider p = (Provider)PROVIDERS.get(key);
        if (p == null) {
            p = new Provider((float)key / 4.0f, false);
            PROVIDERS.put(key, (Object)p);
        }
        return p;
    }

    public static Identifier worldFont(float size) {
        return Identifier.fromNamespaceAndPath((String)"fwm", (String)("holo/" + Math.round(size * 4.0f)));
    }

    public static GlyphSource worldGlyphs(Identifier id) {
        int key;
        if (!"fwm".equals(id.getNamespace()) || !id.getPath().startsWith("holo/")) {
            return null;
        }
        if (!FwmFont.enabled()) {
            return null;
        }
        try {
            key = Integer.parseInt(id.getPath().substring(5));
        }
        catch (NumberFormatException e) {
            return null;
        }
        Provider p = (Provider)WORLD.get(key);
        if (p == null) {
            p = new Provider((float)key / 4.0f, true);
            WORLD.put(key, (Object)p);
        }
        return p;
    }

    private static float[] advance(int cp) {
        float[] a = (float[])ADVANCE.get(cp);
        if (a != null) {
            return a;
        }
        Font f = FwmFont.font().deriveFont(64.0f);
        GlyphVector gv = f.createGlyphVector(FRC, new String(Character.toChars(cp)));
        float adv = gv.getGlyphMetrics(0).getAdvanceX() / 64.0f;
        a = new float[]{adv, adv + 0.045f};
        ADVANCE.put(cp, (Object)a);
        return a;
    }

    private static float baseline(float size) {
        return 3.5f + size * 0.727f / 2.0f;
    }

    private static DynamicTexture atlas() {
        if (atlas != null) {
            return atlas;
        }
        try {
            atlas = new DynamicTexture(() -> "fwm ui font", 1024, 1024, true);
            Minecraft.getInstance().getTextureManager().register(TEXTURE, (AbstractTexture)atlas);
            layers = GlyphRenderTypes.createForColorTexture((Identifier)TEXTURE);
        }
        catch (Throwable t) {
            failed = true;
            atlas = null;
            LOG.warn("FWM: font atlas could not be made", t);
        }
        return atlas;
    }

    private static Slot slot(int cp, boolean bold, float px) {
        int q = Math.min(384, Math.max(4, Math.round(px * 4.0f)));
        long key = (long)cp << 12 | (long)q << 1 | (long)(bold ? 1 : 0);
        Slot s = (Slot)SLOTS.get(key);
        if (s != null) {
            return s;
        }
        s = FwmFont.rasterise(cp, bold, (float)q / 4.0f);
        if (s != null) {
            SLOTS.put(key, (Object)s);
        }
        return s;
    }

    private static Slot rasterise(int cp, boolean bold, float px) {
        if (FwmFont.atlas() == null) {
            return null;
        }
        Font f = FwmFont.font().deriveFont(px);
        GlyphVector gv = f.createGlyphVector(FRC, new String(Character.toChars(cp)));
        Shape outline = gv.getGlyphOutline(0);
        Rectangle2D b = outline.getBounds2D();
        float stroke = bold ? px * 0.045f : 0.0f;
        int dx = (int)Math.floor(b.getX() - (double)stroke) - 1;
        int dy = (int)Math.floor(b.getY() - (double)stroke) - 1;
        int w = (int)Math.ceil(b.getMaxX() + (double)stroke) + 1 - dx;
        int h = (int)Math.ceil(b.getMaxY() + (double)stroke) + 1 - dy;
        if (w <= 0 || h <= 0 || w > 256 || h > 256) {
            return null;
        }
        if (shelfX + w > 1024) {
            shelfX = 0;
            shelfY += shelfH + 1;
            shelfH = 0;
        }
        if (shelfY + h > 1024) {
            FwmFont.reset();
        }
        int u = shelfX;
        int v = shelfY;
        shelfX += w + 1;
        shelfH = Math.max(shelfH, h);
        BufferedImage img = new BufferedImage(w, h, 10);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setColor(Color.WHITE);
        g2.translate(-dx, -dy);
        g2.fill(outline);
        if (bold) {
            g2.setStroke(new BasicStroke(stroke, 1, 1));
            g2.draw(outline);
        }
        g2.dispose();
        NativeImage target = atlas.getPixels();
        if (target == null) {
            return null;
        }
        WritableRaster raster = img.getRaster();
        try (NativeImage piece = new NativeImage(w, h, true);){
            for (int yy = 0; yy < h; ++yy) {
                for (int xx = 0; xx < w; ++xx) {
                    int c = raster.getSample(xx, yy, 0);
                    int a = c == 0 ? 0 : Math.min(255, Math.round((float)Math.pow((float)c / 255.0f, 0.85f) * 255.0f));
                    target.setPixel(u + xx, v + yy, a << 24 | 0xFFFFFF);
                    piece.setPixel(xx, yy, a << 24 | 0xFFFFFF);
                }
            }
            RenderSystem.getDevice().createCommandEncoder().writeToTexture(atlas.getTexture(), piece, 0, 0, u, v);
        }
        return new Slot(u, v, w, h, dx, dy);
    }

    private static void reset() {
        SLOTS.clear();
        shelfH = 0;
        shelfY = 0;
        shelfX = 0;
        NativeImage img = atlas.getPixels();
        if (img != null) {
            img.fillRect(0, 0, 1024, 1024, 0);
        }
    }

    static {
        SLOTS = new Long2ObjectOpenHashMap();
        ADVANCE = new Int2ObjectOpenHashMap();
        RENDERERS = new Int2ObjectOpenHashMap();
        PROVIDERS = new Int2ObjectOpenHashMap();
        FRC = new FontRenderContext(new AffineTransform(), true, true);
        WORLD = new Int2ObjectOpenHashMap();
    }

    private static final class Provider
    implements GlyphSource {
        final float size;
        final boolean world;
        final Int2ObjectOpenHashMap<Glyph> glyphs = new Int2ObjectOpenHashMap();

        Provider(float size, boolean world) {
            this.size = size;
            this.world = world;
        }

        public BakedGlyph getGlyph(int cp) {
            Glyph g = (Glyph)this.glyphs.get(cp);
            if (g != null) {
                return g;
            }
            Font f = FwmFont.font();
            if (f == null || cp != 32 && !f.canDisplay(cp)) {
                return FwmFont.vanilla().glyphs((FontDescription)FontDescription.DEFAULT).getGlyph(cp);
            }
            g = new Glyph(cp, this.size, this.world);
            this.glyphs.put(cp, (Object)g);
            return g;
        }

        public BakedGlyph getRandomGlyph(RandomSource random, int width) {
            return this.getGlyph(97 + random.nextInt(26));
        }
    }

    private record Slot(int u, int v, int w, int h, int dx, int dy) {
    }

    private record Rect(Glyph glyph, float x, float y, int color, int shadowColor, boolean bold, float shadowOffset, Style style) implements TextRenderable.Styled
    {
        public void render(Matrix4fc pose, VertexConsumer buffer, int light, boolean seeThrough) {
            if (this.glyph.world) {
                Slot slot = FwmFont.slot(this.glyph.cp, this.bold, this.glyph.size * 6.0f);
                if (slot == null) {
                    return;
                }
                float k = 6.0f;
                float x0 = this.x + (float)slot.dx() / k;
                float y0 = this.y + FwmFont.baseline(this.glyph.size) + (float)slot.dy() / k;
                float x1 = x0 + (float)slot.w() / k;
                float y1 = y0 + (float)slot.h() / k;
                float u0 = (float)slot.u() / 1024.0f;
                float v0 = (float)slot.v() / 1024.0f;
                float u1 = (float)(slot.u() + slot.w()) / 1024.0f;
                float v1 = (float)(slot.v() + slot.h()) / 1024.0f;
                buffer.addVertex(pose, x0, y0, 0.0f).setColor(this.color).setUv(u0, v0).setLight(light);
                buffer.addVertex(pose, x0, y1, 0.0f).setColor(this.color).setUv(u0, v1).setLight(light);
                buffer.addVertex(pose, x1, y1, 0.0f).setColor(this.color).setUv(u1, v1).setLight(light);
                buffer.addVertex(pose, x1, y0, 0.0f).setColor(this.color).setUv(u1, v0).setLight(light);
                return;
            }
            float g = Shapes.gui();
            float scale = (float)Math.sqrt(pose.m00() * pose.m00() + pose.m01() * pose.m01());
            float px = this.glyph.size * scale * g;
            if (px < 1.0f) {
                return;
            }
            Slot slot = FwmFont.slot(this.glyph.cp, this.bold, px);
            if (slot == null) {
                return;
            }
            float lx = this.x;
            float ly = this.y + FwmFont.baseline(this.glyph.size);
            float sx = (pose.m00() * lx + pose.m10() * ly + pose.m30()) * g;
            float sy = (pose.m01() * lx + pose.m11() * ly + pose.m31()) * g;
            if (this.shadowColor != 0) {
                int off = Math.max(1, Math.round(this.shadowOffset * scale * g));
                Rect.quad(buffer, slot, Math.round(sx) + off, Math.round(sy) + off, g, Rect.fade(this.shadowColor), light);
            }
            Rect.quad(buffer, slot, Math.round(sx), Math.round(sy), g, Rect.fade(this.color), light);
        }

        private static void quad(VertexConsumer b, Slot s, int penX, int penY, float g, int color, int light) {
            float x0 = (float)(penX + s.dx()) / g;
            float y0 = (float)(penY + s.dy()) / g;
            float x1 = x0 + (float)s.w() / g;
            float y1 = y0 + (float)s.h() / g;
            float u0 = (float)s.u() / 1024.0f;
            float v0 = (float)s.v() / 1024.0f;
            float u1 = (float)(s.u() + s.w()) / 1024.0f;
            float v1 = (float)(s.v() + s.h()) / 1024.0f;
            b.addVertex(x0, y0, 0.0f).setColor(color).setUv(u0, v0).setLight(light);
            b.addVertex(x0, y1, 0.0f).setColor(color).setUv(u0, v1).setLight(light);
            b.addVertex(x1, y1, 0.0f).setColor(color).setUv(u1, v1).setLight(light);
            b.addVertex(x1, y0, 0.0f).setColor(color).setUv(u1, v0).setLight(light);
        }

        private static int fade(int c) {
            return c;
        }

        public RenderType renderType(Font.DisplayMode mode) {
            return layers.select(mode);
        }

        public GpuTextureView textureView() {
            return atlas.getTextureView();
        }

        public RenderPipeline guiPipeline() {
            return layers.guiPipeline();
        }

        public float left() {
            return this.x;
        }

        public float top() {
            return this.y;
        }

        public float right() {
            return this.x + this.glyph.boldAdvance + 1.0f;
        }

        public float bottom() {
            return this.y + 9.0f;
        }
    }

    private static final class Glyph
    implements BakedGlyph,
    GlyphInfo {
        final int cp;
        final float size;
        final boolean world;
        final float advance;
        final float boldAdvance;

        Glyph(int cp, float size, boolean world) {
            this.cp = cp;
            this.size = size;
            this.world = world;
            float[] a = FwmFont.advance(cp);
            this.advance = a[0] * size;
            this.boldAdvance = a[1] * size;
        }

        public GlyphInfo info() {
            return this;
        }

        public float getAdvance() {
            return this.advance;
        }

        public float getAdvance(boolean bold) {
            return bold ? this.boldAdvance : this.advance;
        }

        public float getBoldOffset() {
            return 0.0f;
        }

        public float getShadowOffset() {
            return 0.6f;
        }

        public TextRenderable.Styled createGlyph(float x, float y, int color, int shadowColor, Style style, float boldOffset, float shadowOffset) {
            if (this.cp == 32 || FwmFont.atlas() == null) {
                return null;
            }
            return new Rect(this, x, y, color, shadowColor, style.isBold(), shadowOffset, style);
        }
    }
}

