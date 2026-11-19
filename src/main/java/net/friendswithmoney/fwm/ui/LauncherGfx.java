/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.NativeImage
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.item.ItemStack
 *  org.joml.Matrix3x2fStack
 */
package net.friendswithmoney.fwm.ui;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import net.friendswithmoney.fwm.ui.FwmIcons;
import net.friendswithmoney.fwm.ui.FwmTheme;
import net.friendswithmoney.fwm.ui.gfx.FwmFont;
import net.friendswithmoney.fwm.ui.gfx.Shapes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

public final class LauncherGfx {
    private static final int GLOW_SIZE = 512;
    private static final Map<Integer, Identifier> GLOWS = new HashMap<Integer, Identifier>();
    private static final HashMap<String, String> FITS = new HashMap();

    private LauncherGfx() {
    }

    private static int inset(int row, int h, int r) {
        int fromEdge = Math.min(row, h - 1 - row);
        if (fromEdge >= r) {
            return 0;
        }
        double dy = (double)(r - fromEdge) - 0.5;
        return (int)Math.round((double)r - Math.sqrt(Math.max(0.0, (double)r * (double)r - dy * dy)));
    }

    public static void round(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int r, int color) {
        Shapes.round(ctx, x, y, w, h, r, color);
    }

    public static void round(GuiGraphicsExtractor ctx, float x, float y, float w, float h, float r, int color) {
        Shapes.round(ctx, x, y, w, h, r, color);
    }

    public static void card(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int r) {
        LauncherGfx.roundGradient(ctx, x, y, w, h, r, FwmTheme.GLASS_TOP, FwmTheme.GLASS_BOTTOM);
        LauncherGfx.ring(ctx, x, y, w, h, r, FwmTheme.EDGE);
        ctx.fill(x + r, y + 1, x + w - r, y + 2, FwmTheme.SHEEN);
    }

    public static void roundGradient(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int r, int top, int bottom) {
        Shapes.roundGradient(ctx, x, y, w, h, r, top, bottom);
    }

    public static void glow(GuiGraphicsExtractor ctx, int cx, int cy, int radius, int rgb, int alpha) {
        int key = (alpha & 0xFF) << 24 | rgb & 0xFFFFFF;
        Identifier id = GLOWS.computeIfAbsent(key, k -> {
            int size = 512;
            NativeImage img = new NativeImage(size, size, true);
            Random noise = new Random(k.intValue());
            for (int y = 0; y < size; ++y) {
                for (int x = 0; x < size; ++x) {
                    double dx = ((double)x + 0.5) / (double)size * 2.0 - 1.0;
                    double dy = ((double)y + 0.5) / (double)size * 2.0 - 1.0;
                    double d = Math.min(1.0, Math.sqrt(dx * dx + dy * dy));
                    double f = 1.0 - d;
                    double exact = (double)alpha * f * f * (3.0 - 2.0 * f);
                    int a = exact <= 0.0 ? 0 : Math.max(0, Math.min(255, (int)Math.floor(exact + noise.nextDouble())));
                    img.setPixel(x, y, a << 24 | rgb & 0xFFFFFF);
                }
            }
            Identifier made = Identifier.fromNamespaceAndPath((String)"fwm", (String)("glow/" + Integer.toHexString(k)));
            Minecraft.getInstance().getTextureManager().register(made, (AbstractTexture)new DynamicTexture(() -> "fwm glow", img));
            return made;
        });
        ctx.blit(RenderPipelines.GUI_TEXTURED, id, cx - radius, cy - radius, 0.0f, 0.0f, radius * 2, radius * 2, 512, 512, 512, 512);
    }

    public static void glowInRound(GuiGraphicsExtractor ctx, int w, int h, int r, int cx, int cy, int radius, int rgb, int alpha) {
        int top = Math.max(0, cy - radius);
        int bottom = Math.min(h, cy + radius);
        int left = cx - radius;
        int right = cx + radius;
        if (left >= w || right <= 0 || top >= h || bottom <= 0) {
            return;
        }
        int y0 = Math.max(top, r);
        int y1 = Math.min(bottom, h - r);
        if (y1 > y0) {
            ctx.enableScissor(1, y0, w - 1, y1);
            LauncherGfx.glow(ctx, cx, cy, radius, rgb, alpha);
            ctx.disableScissor();
        }
        for (int row = 0; row < h; ++row) {
            if (row >= r && row < h - r || row < top || row >= bottom) continue;
            int in = LauncherGfx.inset(row, h, r) + 1;
            ctx.enableScissor(in, row, w - in, row + 1);
            LauncherGfx.glow(ctx, cx, cy, radius, rgb, alpha);
            ctx.disableScissor();
        }
    }

    public static void item(GuiGraphicsExtractor ctx, ItemStack stack, int x, int y, float scale) {
        Matrix3x2fStack m = ctx.pose();
        m.pushMatrix();
        m.translate((float)x, (float)y);
        m.scale(scale, scale);
        ctx.item(stack, 0, 0);
        m.popMatrix();
    }

    private static int mix(int a, int b, float t) {
        int out = 0;
        for (int shift = 0; shift <= 24; shift += 8) {
            int ca = a >>> shift & 0xFF;
            int cb = b >>> shift & 0xFF;
            out |= (Math.round((float)ca + (float)(cb - ca) * t) & 0xFF) << shift;
        }
        return out;
    }

    public static void infoGlyph(GuiGraphicsExtractor ctx, int x, int y, int color) {
        ctx.fill(x + 6, y + 3, x + 8, y + 5, color);
        ctx.fill(x + 6, y + 6, x + 8, y + 11, color);
    }

    public static void ring(GuiGraphicsExtractor ctx, int x, int y, int w, int h, int r, int color) {
        Shapes.ring(ctx, x, y, w, h, r, color);
    }

    public static void ring(GuiGraphicsExtractor ctx, float x, float y, float w, float h, float r, int color) {
        Shapes.ring(ctx, x, y, w, h, r, color);
    }

    public static void toggle(GuiGraphicsExtractor ctx, int x, int y, boolean on, int onColor, int offColor) {
        LauncherGfx.toggle(ctx, (float)x, (float)y, on ? 1.0f : 0.0f, onColor, offColor);
    }

    public static void toggle(GuiGraphicsExtractor ctx, float x, float y, float t, int onColor, int offColor) {
        float w = 22.0f;
        float h = 12.0f;
        t = Math.max(0.0f, Math.min(1.0f, t));
        Shapes.round(ctx, x, y, w, h, h / 2.0f, Shapes.mix(offColor, onColor, t));
        float knob = h - 3.0f;
        float kx = x + 1.5f + t * (w - knob - 3.0f);
        Shapes.circle(ctx, kx + knob / 2.0f, y + 1.5f + knob / 2.0f + 0.5f, knob / 2.0f + 0.3f, 0x33000000);
        Shapes.circle(ctx, kx + knob / 2.0f, y + 1.5f + knob / 2.0f, knob / 2.0f, t > 0.5f ? -16045536 : -1);
    }

    public static void icon(GuiGraphicsExtractor ctx, String key, int x, int y, float scale, int color) {
        Matrix3x2fStack m = ctx.pose();
        m.pushMatrix();
        m.translate((float)x, (float)y);
        m.scale(scale, scale);
        FwmIcons.draw(ctx, key, 0, 0, color);
        m.popMatrix();
    }

    public static void text(GuiGraphicsExtractor ctx, Font font, String s, int x, int y, int color) {
        ctx.text(font, (Component)Component.literal((String)s), x, y, color, false);
    }

    public static void bold(GuiGraphicsExtractor ctx, Font font, String s, int x, int y, int color) {
        ctx.text(font, (Component)Component.literal((String)s).withStyle(ChatFormatting.BOLD), x, y, color, false);
    }

    public static void big(GuiGraphicsExtractor ctx, Font font, String s, int x, int y, float scale, int color, boolean bold) {
        Matrix3x2fStack m = ctx.pose();
        m.pushMatrix();
        m.translate((float)x, (float)y);
        m.scale(scale, scale);
        MutableComponent t = bold ? Component.literal((String)s).withStyle(ChatFormatting.BOLD) : Component.literal((String)s);
        ctx.text(font, (Component)t, 0, 0, color, false);
        m.popMatrix();
    }

    public static void text(GuiGraphicsExtractor ctx, String s, float x, float y, int color, float size, boolean bold) {
        if ((color = Shapes.faded(color)) >>> 24 < 4) {
            return;
        }
        MutableComponent t = bold ? Component.literal((String)s).withStyle(ChatFormatting.BOLD) : Component.literal((String)s);
        Matrix3x2fStack m = ctx.pose();
        m.pushMatrix();
        if (FwmFont.enabled()) {
            m.translate(x, y);
            ctx.text(FwmFont.renderer(size), (Component)t, 0, 0, color, false);
        } else {
            float k = size / 8.0f;
            m.translate(x, y + 3.5f - 3.5f * k);
            m.scale(k, k);
            ctx.text(Minecraft.getInstance().font, (Component)t, 0, 0, color, false);
        }
        m.popMatrix();
    }

    public static float width(String s, float size, boolean bold) {
        MutableComponent t;
        MutableComponent mutableComponent = t = bold ? Component.literal((String)s).withStyle(ChatFormatting.BOLD) : Component.literal((String)s);
        if (FwmFont.enabled()) {
            return FwmFont.renderer(size).width((FormattedText)t);
        }
        return (float)Minecraft.getInstance().font.width((FormattedText)t) * size / 8.0f;
    }

    public static String fit(String s, float size, boolean bold, float width) {
        String key = s + "\u0000" + size + (bold ? (char)'b' : 'r') + Math.round(width) + FwmFont.enabled();
        String have = FITS.get(key);
        if (have != null) {
            return have;
        }
        Object out = s;
        if (LauncherGfx.width(s, size, bold) > width) {
            String cut = s;
            while (cut.length() > 1 && LauncherGfx.width(cut + "\u2026", size, bold) > width) {
                cut = cut.substring(0, cut.length() - 1);
            }
            out = cut.stripTrailing() + "\u2026";
        }
        if (FITS.size() > 4000) {
            FITS.clear();
        }
        FITS.put(key, (String)out);
        return out;
    }

    public static String trim(Font font, String s, int width) {
        return font.width(s) <= width ? s : font.plainSubstrByWidth(s, Math.max(0, width - 6)) + "\u2026";
    }
}

