/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.NativeImage
 */
package net.friendswithmoney.fwm.textures;

import com.mojang.blaze3d.platform.NativeImage;
import net.friendswithmoney.fwm.textures.CrystalRender;
import net.friendswithmoney.fwm.textures.GlintFigure;

final class IsoRender {
    private static final float COS = 0.8660254f;
    private static final int K = 2;

    private IsoRender() {
    }

    static NativeImage render(String categoryId, String path, NativeImage src) {
        return IsoRender.render(categoryId, path, src, null);
    }

    static NativeImage render(String categoryId, String path, NativeImage src, NativeImage top) {
        int frameH;
        int w = src.getWidth();
        int h = src.getHeight();
        int n = frameH = h > w && h % w == 0 ? w : h;
        if (path.contains("textures/misc/enchanted_glint")) {
            return GlintFigure.strip(src);
        }
        if (path.endsWith("textures/entity/end_crystal/end_crystal.png") && w >= 64 && w == h * 2) {
            return CrystalRender.strip(src);
        }
        if (path.endsWith("/cobweb.png") && w <= 64 && frameH <= 64) {
            return IsoRender.trim(IsoRender.item(src, w, frameH));
        }
        if (path.contains("/block/") && w == frameH && top != null) {
            return IsoRender.trim(IsoRender.cubeWithTop(src, w, frameH, top));
        }
        if (path.contains("/block/") && w == frameH) {
            return IsoRender.trim(IsoRender.cube(src, 0, 0, w, frameH));
        }
        if (path.contains("/item/") && w <= 64 && frameH <= 64) {
            return IsoRender.trim(IsoRender.item(src, w, frameH));
        }
        return null;
    }

    private static NativeImage trim(NativeImage img) {
        int minX = img.getWidth();
        int minY = img.getHeight();
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < img.getHeight(); ++y) {
            for (int x = 0; x < img.getWidth(); ++x) {
                if (img.getPixel(x, y) >>> 24 < 8) continue;
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }
        if (maxX < 0) {
            return img;
        }
        int w = maxX - minX + 3;
        int h = maxY - minY + 3;
        NativeImage out = new NativeImage(w, h, true);
        for (int y = minY; y <= maxY; ++y) {
            for (int x = minX; x <= maxX; ++x) {
                out.setPixel(x - minX + 1, y - minY + 1, img.getPixel(x, y));
            }
        }
        img.close();
        return out;
    }

    private static NativeImage cube(NativeImage src, int sx, int sy, int sw, int sh) {
        int size = 144;
        NativeImage out = new NativeImage(size, size + 12, true);
        IsoRender.shadow(out, (float)size / 2.0f, size + 2, 52.0f, 10.0f);
        float edge = 68.0f;
        float bx = (float)size / 2.0f;
        float by = size - 4;
        IsoRender.box(out, src, bx, by, edge, edge, edge, new int[]{sx, sy, sw, sh}, new int[]{sx, sy, sw, sh}, new int[]{sx, sy, sw, sh}, 1.0f);
        return out;
    }

    private static NativeImage cross(NativeImage src, int w, int h) {
        int size = 144;
        NativeImage out = new NativeImage(size, size + 12, true);
        IsoRender.shadow(out, (float)size / 2.0f, size + 2, 44.0f, 8.0f);
        float edge = 80.0f;
        float bx = (float)size / 2.0f;
        float by = size - 8;
        float[] a = new float[]{-0.8660254f * edge, -0.5f * edge};
        float[] b = new float[]{0.8660254f * edge, -0.5f * edge};
        int half = Math.max(1, w / 2);
        float ax = bx + a[0] / 2.0f;
        float ay = by + a[1] / 2.0f;
        float cx = bx + b[0] / 2.0f;
        float cy = by + b[1] / 2.0f;
        IsoRender.face(out, src, new int[]{half, 0, w - half, h}, ax + b[0] / 2.0f, ay + b[1] / 2.0f - edge, b[0] / 2.0f, b[1] / 2.0f, 0.0f, edge, 0.8f, 1.0f);
        IsoRender.face(out, src, new int[]{half, 0, w - half, h}, cx + a[0] / 2.0f, cy + a[1] / 2.0f - edge, a[0] / 2.0f, a[1] / 2.0f, 0.0f, edge, 0.9f, 1.0f);
        IsoRender.face(out, src, new int[]{0, 0, half, h}, ax, ay - edge, b[0] / 2.0f, b[1] / 2.0f, 0.0f, edge, 0.8f, 1.0f);
        IsoRender.face(out, src, new int[]{0, 0, half, h}, cx, cy - edge, a[0] / 2.0f, a[1] / 2.0f, 0.0f, edge, 0.9f, 1.0f);
        return out;
    }

    private static NativeImage cubeWithTop(NativeImage src, int sw, int sh, NativeImage top) {
        int size = 144;
        NativeImage out = new NativeImage(size, size + 12, true);
        IsoRender.shadow(out, (float)size / 2.0f, size + 2, 52.0f, 10.0f);
        float edge = 68.0f;
        float bx = (float)size / 2.0f;
        float by = size - 4;
        int tw = top.getWidth();
        int th = top.getHeight() > tw && top.getHeight() % tw == 0 ? tw : top.getHeight();
        float[] a = new float[]{-0.8660254f * edge, -0.5f * edge};
        float[] b = new float[]{0.8660254f * edge, -0.5f * edge};
        int[] side = new int[]{0, 0, sw, sh};
        IsoRender.face(out, src, side, bx + a[0], by + a[1] - edge, -a[0], -a[1], 0.0f, edge, 0.78f, 1.0f);
        IsoRender.face(out, src, side, bx, by - edge, b[0], b[1], 0.0f, edge, 0.6f, 1.0f);
        IsoRender.face(out, top, new int[]{0, 0, tw, th}, bx + a[0] + b[0], by + a[1] + b[1] - edge, -b[0], -b[1], -a[0], -a[1], 1.0f, 1.0f);
        return out;
    }

    private static NativeImage item(NativeImage src, int w, int h) {
        int scale = Math.max(1, 128 / Math.max(w, h));
        int ow = w * scale;
        int oh = h * scale;
        NativeImage out = new NativeImage(ow + 8, oh + 12, true);
        IsoRender.shadow(out, (float)(ow + 8) / 2.0f, oh + 7, (float)ow * 0.36f, 3.5f);
        for (int y = 0; y < oh; ++y) {
            for (int x = 0; x < ow; ++x) {
                int c = src.getPixel(x / scale, y / scale);
                if (c >>> 24 == 0) continue;
                IsoRender.blend(out, x + 4, y + 2, c);
            }
        }
        return out;
    }

    private static int[] face(int u, int x, int y, int w, int h) {
        return new int[]{x * u, y * u, w * u, h * u};
    }

    private static void box(NativeImage out, NativeImage src, float bx, float by, float width, float depth, float height, int[] top, int[] front, int[] side, float opacity) {
        float[] a = new float[]{-0.8660254f * depth, -0.5f * depth};
        float[] b = new float[]{0.8660254f * width, -0.5f * width};
        IsoRender.face(out, src, side, bx + a[0], by + a[1] - height, -a[0], -a[1], 0.0f, height, 0.78f, opacity);
        IsoRender.face(out, src, front, bx, by - height, b[0], b[1], 0.0f, height, 0.6f, opacity);
        IsoRender.face(out, src, top, bx + a[0] + b[0], by + a[1] + b[1] - height, -b[0], -b[1], -a[0], -a[1], 1.0f, opacity);
    }

    private static void face(NativeImage out, NativeImage src, int[] r, float ox, float oy, float ux, float uy, float vx, float vy, float shade, float opacity) {
        if (r[2] <= 0 || r[3] <= 0) {
            return;
        }
        float det = ux * vy - uy * vx;
        if (Math.abs(det) < 1.0E-4f) {
            return;
        }
        int minX = (int)Math.floor(Math.min(Math.min(ox, ox + ux), Math.min(ox + vx, ox + ux + vx)));
        int maxX = (int)Math.ceil(Math.max(Math.max(ox, ox + ux), Math.max(ox + vx, ox + ux + vx)));
        int minY = (int)Math.floor(Math.min(Math.min(oy, oy + uy), Math.min(oy + vy, oy + uy + vy)));
        int maxY = (int)Math.ceil(Math.max(Math.max(oy, oy + uy), Math.max(oy + vy, oy + uy + vy)));
        for (int py = Math.max(0, minY); py < Math.min(out.getHeight(), maxY); ++py) {
            for (int px = Math.max(0, minX); px < Math.min(out.getWidth(), maxX); ++px) {
                int c;
                int alpha;
                float dx = (float)px + 0.5f - ox;
                float dy = (float)py + 0.5f - oy;
                float s = (dx * vy - dy * vx) / det;
                float t = (ux * dy - uy * dx) / det;
                if (s < 0.0f || s >= 1.0f || t < 0.0f || t >= 1.0f) continue;
                int tx = r[0] + Math.min(r[2] - 1, (int)(s * (float)r[2]));
                int ty = r[1] + Math.min(r[3] - 1, (int)(t * (float)r[3]));
                if (tx >= src.getWidth() || ty >= src.getHeight() || (alpha = Math.round((float)((c = src.getPixel(tx, ty)) >>> 24) * opacity)) == 0) continue;
                int red = Math.round((float)(c >> 16 & 0xFF) * shade);
                int green = Math.round((float)(c >> 8 & 0xFF) * shade);
                int blue = Math.round((float)(c & 0xFF) * shade);
                IsoRender.blend(out, px, py, alpha << 24 | red << 16 | green << 8 | blue);
            }
        }
    }

    private static void shadow(NativeImage out, float cx, float cy, float rx, float ry) {
        for (int y = (int)(cy - ry); y <= (int)(cy + ry); ++y) {
            for (int x = (int)(cx - rx); x <= (int)(cx + rx); ++x) {
                float dy;
                float dx;
                float d;
                if (x < 0 || y < 0 || x >= out.getWidth() || y >= out.getHeight() || (d = (dx = ((float)x - cx) / rx) * dx + (dy = ((float)y - cy) / ry) * dy) >= 1.0f) continue;
                IsoRender.blend(out, x, y, Math.round(90.0f * (1.0f - d)) << 24);
            }
        }
    }

    private static void blend(NativeImage out, int x, int y, int argb) {
        int a = argb >>> 24;
        if (a == 0) {
            return;
        }
        if (a == 255) {
            out.setPixel(x, y, argb);
            return;
        }
        int under = out.getPixel(x, y);
        int ua = under >>> 24;
        int na = a + ua * (255 - a) / 255;
        if (na == 0) {
            return;
        }
        int r = ((argb >> 16 & 0xFF) * a + (under >> 16 & 0xFF) * ua * (255 - a) / 255) / na;
        int g = ((argb >> 8 & 0xFF) * a + (under >> 8 & 0xFF) * ua * (255 - a) / 255) / na;
        int b = ((argb & 0xFF) * a + (under & 0xFF) * ua * (255 - a) / 255) / na;
        out.setPixel(x, y, na << 24 | r << 16 | g << 8 | b);
    }
}

