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
package net.friendswithmoney.fwm.ui.gfx;

import com.mojang.blaze3d.platform.NativeImage;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

public final class ImageTex {
    private static final Map<String, Entry> MAP = new HashMap<String, Entry>();
    private static int next;

    private ImageTex() {
    }

    public static void draw(GuiGraphicsExtractor ctx, String key, BufferedImage img, int x, int y, int w, int h) {
        if (img == null) {
            return;
        }
        Entry e = MAP.get(key);
        if (e == null || e.source != img) {
            int[] px = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
            e = ImageTex.put(key, px, img.getWidth(), img.getHeight(), img);
        }
        ctx.blit(RenderPipelines.GUI_TEXTURED, e.id, x, y, 0.0f, 0.0f, w, h, e.w, e.h, e.w, e.h);
    }

    public static void draw(GuiGraphicsExtractor ctx, String key, int[] argb, int pw, int ph, int version, int x, int y, int w, int h) {
        Entry e = MAP.get(key);
        Integer v = version;
        if (e == null || !v.equals(e.source) || e.w != pw || e.h != ph) {
            if (e != null && e.w == pw && e.h == ph) {
                NativeImage img = e.tex.getPixels();
                if (img != null) {
                    for (int yy = 0; yy < ph; ++yy) {
                        for (int xx = 0; xx < pw; ++xx) {
                            img.setPixel(xx, yy, argb[yy * pw + xx]);
                        }
                    }
                    e.tex.upload();
                    e = new Entry(e.id, e.tex, pw, ph, v);
                    MAP.put(key, e);
                }
            } else {
                e = ImageTex.put(key, argb, pw, ph, v);
            }
        }
        ctx.blit(RenderPipelines.GUI_TEXTURED, e.id, x, y, 0.0f, 0.0f, w, h, e.w, e.h, e.w, e.h);
    }

    private static Entry put(String key, int[] argb, int w, int h, Object source) {
        Entry old = MAP.remove(key);
        if (old != null) {
            Minecraft.getInstance().getTextureManager().release(old.id);
        }
        NativeImage img = new NativeImage(w, h, false);
        for (int y = 0; y < h; ++y) {
            for (int x = 0; x < w; ++x) {
                img.setPixel(x, y, argb[y * w + x]);
            }
        }
        Identifier id = Identifier.fromNamespaceAndPath((String)"fwm", (String)("dyn/" + next++));
        DynamicTexture tex = new DynamicTexture(() -> "fwm " + key, img);
        Minecraft.getInstance().getTextureManager().register(id, (AbstractTexture)tex);
        Entry e = new Entry(id, tex, w, h, source);
        MAP.put(key, e);
        return e;
    }

    public static void release(String prefix) {
        Iterator<Map.Entry<String, Entry>> it = MAP.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Entry> e = it.next();
            if (!e.getKey().startsWith(prefix)) continue;
            Minecraft.getInstance().getTextureManager().release(e.getValue().id);
            it.remove();
        }
    }

    private record Entry(Identifier id, DynamicTexture tex, int w, int h, Object source) {
    }
}

