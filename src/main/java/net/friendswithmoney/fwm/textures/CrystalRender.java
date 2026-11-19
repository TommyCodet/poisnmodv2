/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.NativeImage
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.friendswithmoney.fwm.textures;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

final class CrystalRender {
    static final int FRAMES = 48;
    static final int SIZE = 176;
    static final int FRAME_MS = 100;
    private static final float TICKS_PER_FRAME = 2.0f;
    private static final int SS = 2;
    private static final float SIN45 = (float)Math.sin(0.7853981633974483);
    private static final Vector3f LIGHT = new Vector3f(-0.35f, 1.0f, 0.55f).normalize();

    private CrystalRender() {
    }

    static NativeImage strip(NativeImage tex) {
        int unit = Math.max(1, tex.getWidth() / 64);
        ArrayList<List<Quad>> frames = new ArrayList<List<Quad>>();
        for (int f = 0; f < 48; ++f) {
            frames.add(CrystalRender.scene((float)f * 2.0f));
        }
        Matrix4f view = new Matrix4f().rotateX((float)Math.toRadians(22.0)).rotateY((float)Math.toRadians(-38.0));
        float minX = Float.MAX_VALUE;
        float maxX = -3.4028235E38f;
        float minY = Float.MAX_VALUE;
        float maxY = -3.4028235E38f;
        for (List list : frames) {
            for (Quad q : list) {
                for (Vector3f p : q.p) {
                    Vector3f v = view.transformPosition(new Vector3f((Vector3fc)p));
                    minX = Math.min(minX, v.x);
                    maxX = Math.max(maxX, v.x);
                    minY = Math.min(minY, v.y);
                    maxY = Math.max(maxY, v.y);
                }
            }
        }
        int big = 352;
        float f = (float)big * 0.03f;
        float scale = Math.min(((float)big - 2.0f * f) / (maxX - minX), ((float)big - 2.0f * f) / (maxY - minY));
        float ox = (float)big / 2.0f - (minX + maxX) / 2.0f * scale;
        float oy = (float)big / 2.0f + (minY + maxY) / 2.0f * scale;
        NativeImage out = new NativeImage(176, 8448, true);
        for (int f2 = 0; f2 < 48; ++f2) {
            float[] rgba = new float[big * big * 4];
            float[] depth = new float[big * big];
            Arrays.fill(depth, Float.NEGATIVE_INFINITY);
            Vector3f ground = view.transformPosition(new Vector3f(0.0f, -1.0f, 0.0f));
            CrystalRender.shadow(rgba, big, ox + ground.x * scale, oy - ground.y * scale, scale);
            ArrayList opaque = new ArrayList();
            ArrayList<Quad> glass = new ArrayList<Quad>();
            for (Quad q : (List)frames.get(f2)) {
                (q.glass ? glass : opaque).add(q);
            }
            for (Quad q : opaque) {
                CrystalRender.draw(rgba, depth, big, tex, unit, view, q, scale, ox, oy, true);
            }
            glass.sort((a, b) -> Float.compare(CrystalRender.centre(view, a), CrystalRender.centre(view, b)));
            for (Quad q : glass) {
                CrystalRender.draw(rgba, depth, big, tex, unit, view, q, scale, ox, oy, false);
            }
            CrystalRender.halve(rgba, big, out, f2 * 176);
        }
        return out;
    }

    private static List<Quad> scene(float age) {
        ArrayList<Quad> quads = new ArrayList<Quad>();
        float spin = age * 3.0f;
        float wave = (float)Math.sin((double)(age * 2.0f) * Math.PI * 4.0 / 120.0) / 2.0f + 0.5f;
        float bob = (wave * wave + wave) * 0.4f - 1.4f;
        Matrix4f root = new Matrix4f().scale(2.0f).translate(0.0f, -0.5f, 0.0f);
        CrystalRender.box(quads, root, -6.0f, 0.0f, -6.0f, 12.0f, 4.0f, 12.0f, 0.0f, 16.0f, false);
        Quaternionf tilt = new Quaternionf().setAngleAxis((float)Math.toRadians(60.0), SIN45, 0.0f, SIN45);
        Matrix4f glass = new Matrix4f((Matrix4fc)root).rotateY((float)Math.toRadians(spin)).translate(0.0f, 1.5f + bob / 2.0f, 0.0f).rotate((Quaternionfc)tilt);
        CrystalRender.box(quads, glass, -4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f, 0.0f, 0.0f, true);
        Matrix4f inner = new Matrix4f((Matrix4fc)glass).scale(0.875f).rotate((Quaternionfc)tilt).rotateY((float)Math.toRadians(spin));
        CrystalRender.box(quads, inner, -4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f, 0.0f, 0.0f, true);
        Matrix4f core = new Matrix4f((Matrix4fc)inner).scale(0.875f).rotate((Quaternionfc)tilt).rotateY((float)Math.toRadians(spin));
        CrystalRender.box(quads, core, -4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f, 32.0f, 0.0f, false);
        return quads;
    }

    private static void box(List<Quad> out, Matrix4f m, float x, float y, float z, float w, float h, float d, float u, float v, boolean glass) {
        float x0 = x / 16.0f;
        float y0 = y / 16.0f;
        float z0 = z / 16.0f;
        float x1 = (x + w) / 16.0f;
        float y1 = (y + h) / 16.0f;
        float z1 = (z + d) / 16.0f;
        out.add(CrystalRender.quad(m, glass, u + d, v, u + d + w, v + d, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1));
        out.add(CrystalRender.quad(m, glass, u + d + w, v, u + d + w + w, v + d, x0, y0, z1, x1, y0, z1, x1, y0, z0, x0, y0, z0));
        out.add(CrystalRender.quad(m, glass, u + d, v + d, u + d + w, v + d + h, x1, y1, z0, x0, y1, z0, x0, y0, z0, x1, y0, z0));
        out.add(CrystalRender.quad(m, glass, u + d + w + d, v + d, u + d + w + d + w, v + d + h, x0, y1, z1, x1, y1, z1, x1, y0, z1, x0, y0, z1));
        out.add(CrystalRender.quad(m, glass, u, v + d, u + d, v + d + h, x0, y1, z0, x0, y1, z1, x0, y0, z1, x0, y0, z0));
        out.add(CrystalRender.quad(m, glass, u + d + w, v + d, u + d + w + d, v + d + h, x1, y1, z1, x1, y1, z0, x1, y0, z0, x1, y0, z1));
    }

    private static Quad quad(Matrix4f m, boolean glass, float u0, float v0, float u1, float v1, float ... c) {
        Vector3f[] p = new Vector3f[4];
        for (int i = 0; i < 4; ++i) {
            p[i] = m.transformPosition(new Vector3f(c[i * 3], c[i * 3 + 1], c[i * 3 + 2]));
        }
        return new Quad(p, u0, v0, u1, v1, glass);
    }

    private static float centre(Matrix4f view, Quad q) {
        float z = 0.0f;
        for (Vector3f p : q.p) {
            z += view.transformPosition((Vector3f)new Vector3f((Vector3fc)p)).z;
        }
        return z;
    }

    private static void draw(float[] rgba, float[] depth, int size, NativeImage tex, int unit, Matrix4f view, Quad q, float scale, float ox, float oy, boolean writeDepth) {
        float[] sx = new float[4];
        float[] sy = new float[4];
        float[] sz = new float[4];
        for (int i = 0; i < 4; ++i) {
            Vector3f v = view.transformPosition(new Vector3f((Vector3fc)q.p[i]));
            sx[i] = ox + v.x * scale;
            sy[i] = oy - v.y * scale;
            sz[i] = v.z;
        }
        Vector3f n = new Vector3f((Vector3fc)q.p[1]).sub((Vector3fc)q.p[0]).cross((Vector3fc)new Vector3f((Vector3fc)q.p[3]).sub((Vector3fc)q.p[0])).normalize();
        float light = 0.62f + 0.38f * Math.abs(n.dot((Vector3fc)LIGHT));
        float[] us = new float[]{q.u0, q.u1, q.u1, q.u0};
        float[] vs = new float[]{q.v0, q.v0, q.v1, q.v1};
        CrystalRender.tri(rgba, depth, size, tex, unit, sx, sy, sz, us, vs, 0, 1, 2, light, writeDepth);
        CrystalRender.tri(rgba, depth, size, tex, unit, sx, sy, sz, us, vs, 0, 2, 3, light, writeDepth);
    }

    private static void tri(float[] rgba, float[] depth, int size, NativeImage tex, int unit, float[] x, float[] y, float[] z, float[] u, float[] v, int a, int b, int c, float light, boolean writeDepth) {
        float area = (x[b] - x[a]) * (y[c] - y[a]) - (x[c] - x[a]) * (y[b] - y[a]);
        if (Math.abs(area) < 1.0E-6f) {
            return;
        }
        int minX = Math.max(0, (int)Math.floor(Math.min(x[a], Math.min(x[b], x[c]))));
        int maxX = Math.min(size - 1, (int)Math.ceil(Math.max(x[a], Math.max(x[b], x[c]))));
        int minY = Math.max(0, (int)Math.floor(Math.min(y[a], Math.min(y[b], y[c]))));
        int maxY = Math.min(size - 1, (int)Math.ceil(Math.max(y[a], Math.max(y[b], y[c]))));
        int tw = tex.getWidth();
        int th = tex.getHeight();
        for (int py = minY; py <= maxY; ++py) {
            for (int px = minX; px <= maxX; ++px) {
                int iy;
                int i;
                float zz;
                float cx = (float)px + 0.5f;
                float cy = (float)py + 0.5f;
                float w0 = ((x[b] - cx) * (y[c] - cy) - (x[c] - cx) * (y[b] - cy)) / area;
                float w1 = ((x[c] - cx) * (y[a] - cy) - (x[a] - cx) * (y[c] - cy)) / area;
                float w2 = 1.0f - w0 - w1;
                if (w0 < 0.0f || w1 < 0.0f || w2 < 0.0f || (zz = w0 * z[a] + w1 * z[b] + w2 * z[c]) < depth[i = py * size + px]) continue;
                float tu = (w0 * u[a] + w1 * u[b] + w2 * u[c]) * (float)unit;
                float tv = (w0 * v[a] + w1 * v[b] + w2 * v[c]) * (float)unit;
                int ix = Math.min(tw - 1, Math.max(0, (int)tu));
                int argb = tex.getPixel(ix, iy = Math.min(th - 1, Math.max(0, (int)tv)));
                float alpha = (float)(argb >>> 24) / 255.0f;
                if (alpha < 0.04f) continue;
                float r = (float)(argb >> 16 & 0xFF) / 255.0f * light;
                float g = (float)(argb >> 8 & 0xFF) / 255.0f * light;
                float bl = (float)(argb & 0xFF) / 255.0f * light;
                int o = i * 4;
                rgba[o] = r * alpha + rgba[o] * (1.0f - alpha);
                rgba[o + 1] = g * alpha + rgba[o + 1] * (1.0f - alpha);
                rgba[o + 2] = bl * alpha + rgba[o + 2] * (1.0f - alpha);
                rgba[o + 3] = alpha + rgba[o + 3] * (1.0f - alpha);
                if (!writeDepth || !(alpha > 0.9f)) continue;
                depth[i] = zz;
            }
        }
    }

    private static void shadow(float[] rgba, int size, float cx, float cy, float scale) {
        float rx = 1.15f * scale;
        float ry = 0.32f * scale;
        for (int py = 0; py < size; ++py) {
            for (int px = 0; px < size; ++px) {
                float dx = ((float)px + 0.5f - cx) / rx;
                float dy = ((float)py + 0.5f - cy) / ry;
                float d = dx * dx + dy * dy;
                if (d >= 1.0f) continue;
                float a = 0.32f * (1.0f - d);
                int o = (py * size + px) * 4;
                rgba[o + 3] = a + rgba[o + 3] * (1.0f - a);
                int n = o;
                rgba[n] = rgba[n] * (1.0f - a);
                int n2 = o + 1;
                rgba[n2] = rgba[n2] * (1.0f - a);
                int n3 = o + 2;
                rgba[n3] = rgba[n3] * (1.0f - a);
            }
        }
    }

    private static void halve(float[] rgba, int big, NativeImage out, int top) {
        for (int y = 0; y < 176; ++y) {
            for (int x = 0; x < 176; ++x) {
                float r = 0.0f;
                float g = 0.0f;
                float b = 0.0f;
                float a = 0.0f;
                for (int sy = 0; sy < 2; ++sy) {
                    for (int sx = 0; sx < 2; ++sx) {
                        int o = ((y * 2 + sy) * big + x * 2 + sx) * 4;
                        r += rgba[o];
                        g += rgba[o + 1];
                        b += rgba[o + 2];
                        a += rgba[o + 3];
                    }
                }
                float n = 4.0f;
                if ((a /= n) <= 0.003f) {
                    out.setPixel(x, top + y, 0);
                    continue;
                }
                int ia = Math.round(Math.min(1.0f, a) * 255.0f);
                int ir = Math.round(Math.min(1.0f, r / n / a) * 255.0f);
                int ig = Math.round(Math.min(1.0f, g / n / a) * 255.0f);
                int ib = Math.round(Math.min(1.0f, b / n / a) * 255.0f);
                out.setPixel(x, top + y, ia << 24 | ir << 16 | ig << 8 | ib);
            }
        }
    }

    private record Quad(Vector3f[] p, float u0, float v0, float u1, float v1, boolean glass) {
    }
}

