/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.NativeImage
 *  net.minecraft.client.Minecraft
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.packs.PackType
 *  net.minecraft.server.packs.resources.IoSupplier
 */
package net.friendswithmoney.fwm.textures;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.InputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;

final class GlintFigure {
    static final int FRAMES = 16;
    private static final int PX = 4;
    private static final int FW = 16;
    private static final int FH = 32;

    private GlintFigure() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static NativeImage strip(NativeImage glint) {
        NativeImage skin = GlintFigure.vanilla("textures/entity/player/wide/steve.png");
        NativeImage armour = GlintFigure.vanilla("textures/entity/equipment/humanoid/diamond.png");
        NativeImage legs = GlintFigure.vanilla("textures/entity/equipment/humanoid_leggings/diamond.png");
        if (skin == null || armour == null) {
            return null;
        }
        try {
            int w = 64;
            int h = 128;
            int[] base = new int[512];
            boolean[] armoured = new boolean[512];
            GlintFigure.front(base, null, skin, 8, 8, 8, 8, 4, 0, false);
            GlintFigure.front(base, null, skin, 20, 20, 8, 12, 4, 8, false);
            GlintFigure.front(base, null, skin, 44, 20, 4, 12, 0, 8, false);
            GlintFigure.front(base, null, skin, 44, 20, 4, 12, 12, 8, true);
            GlintFigure.front(base, null, skin, 4, 20, 4, 12, 4, 20, false);
            GlintFigure.front(base, null, skin, 4, 20, 4, 12, 8, 20, true);
            GlintFigure.front(base, armoured, armour, 8, 8, 8, 8, 4, 0, false);
            GlintFigure.front(base, armoured, armour, 20, 20, 8, 12, 4, 8, false);
            GlintFigure.front(base, armoured, armour, 44, 20, 4, 12, 0, 8, false);
            GlintFigure.front(base, armoured, armour, 44, 20, 4, 12, 12, 8, true);
            if (legs != null) {
                GlintFigure.front(base, armoured, legs, 4, 20, 4, 12, 4, 20, false);
                GlintFigure.front(base, armoured, legs, 4, 20, 4, 12, 8, 20, true);
            }
            GlintFigure.front(base, armoured, armour, 4, 26, 4, 6, 4, 26, false);
            GlintFigure.front(base, armoured, armour, 4, 26, 4, 6, 8, 26, true);
            NativeImage out = new NativeImage(w, h * 16, true);
            int gw = glint == null ? 0 : glint.getWidth();
            int gh = glint == null ? 0 : glint.getHeight();
            for (int f = 0; f < 16; ++f) {
                float shift = (float)f / 16.0f;
                for (int y = 0; y < h; ++y) {
                    for (int x = 0; x < w; ++x) {
                        int c = base[y / 4 * 16 + x / 4];
                        if (c >>> 24 == 0) continue;
                        if (glint != null && armoured[y / 4 * 16 + x / 4]) {
                            int gx = Math.floorMod((int)(((float)x / (float)w * 0.5f + shift) * (float)gw), gw);
                            int gy = Math.floorMod((int)(((float)y / (float)h * 0.5f + shift * 0.6f) * (float)gh), gh);
                            c = GlintFigure.add(c, glint.getPixel(gx, gy));
                        }
                        out.setPixel(x, f * h + y, c);
                    }
                }
            }
            NativeImage nativeImage = out;
            return nativeImage;
        }
        finally {
            skin.close();
            armour.close();
            if (legs != null) {
                legs.close();
            }
        }
    }

    private static void front(int[] base, boolean[] armoured, NativeImage tex, int u, int v, int fw, int fh, int dx, int dy, boolean mirror) {
        float sx = (float)tex.getWidth() / 64.0f;
        float sy = (float)tex.getHeight() / (tex.getHeight() >= tex.getWidth() ? 64.0f : 32.0f);
        for (int y = 0; y < fh; ++y) {
            for (int x = 0; x < fw; ++x) {
                int i;
                int c;
                int tx = (int)((float)(u + (mirror ? fw - 1 - x : x)) * sx);
                int ty = (int)((float)(v + y) * sy);
                if (tx >= tex.getWidth() || ty >= tex.getHeight() || (c = tex.getPixel(tx, ty)) >>> 24 < 16 || (i = (dy + y) * 16 + dx + x) < 0 || i >= base.length) continue;
                base[i] = 0xFF000000 | c & 0xFFFFFF;
                if (armoured == null) continue;
                armoured[i] = true;
            }
        }
    }

    private static int add(int c, int g) {
        float k = Math.min(1.0f, (float)(g >>> 24 & 0xFF) / 255.0f * 2.0f);
        int r = Math.min(255, (c >> 16 & 0xFF) + Math.round((float)(g >> 16 & 0xFF) * k));
        int gg = Math.min(255, (c >> 8 & 0xFF) + Math.round((float)(g >> 8 & 0xFF) * k));
        int b = Math.min(255, (c & 0xFF) + Math.round((float)(g & 0xFF) * k));
        return 0xFF000000 | r << 16 | gg << 8 | b;
    }

    private static NativeImage vanilla(String path) {
        NativeImage nativeImage;
        block9: {
            IoSupplier supplier = Minecraft.getInstance().getVanillaPackResources().getResource(PackType.CLIENT_RESOURCES, Identifier.withDefaultNamespace((String)path));
            if (supplier == null) {
                return null;
            }
            InputStream in = (InputStream)supplier.get();
            try {
                nativeImage = NativeImage.read((InputStream)in);
                if (in == null) break block9;
            }
            catch (Throwable throwable) {
                try {
                    if (in != null) {
                        try {
                            in.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (Exception e) {
                    return null;
                }
            }
            in.close();
        }
        return nativeImage;
    }
}

