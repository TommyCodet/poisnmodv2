/*
 * Decompiled with CFR 0.152.
 */
package net.friendswithmoney.fwm.stream.fx;

import net.friendswithmoney.fwm.stream.fx.FxAnim;
import net.friendswithmoney.fwm.stream.fx.FxCanvas;
import net.friendswithmoney.fwm.stream.fx.Play;

final class LightningFx
extends FxAnim {
    private static final float STRIKE = 140.0f;
    private static final int SEG = 12;
    private static final int BRANCH = 5;
    private final float[] bx = new float[13];
    private final float[] by = new float[13];

    LightningFx() {
        super("lightning", "Blitz", "Lightning", 5200, "Wei\u00dfer Blitz, Donner, der Name schl\u00e4gt mit R\u00fctteln ein.", "A white flash, thunder, the name strikes in with a shake.");
    }

    @Override
    public float impactMs() {
        return 300.0f;
    }

    @Override
    public void shake(Play p, float t, float[] out) {
        LightningFx.dampedShake(t, 180.0f, 4.2f, 150.0f, out);
    }

    @Override
    public void render(FxCanvas c, Play p, float t) {
        float ra;
        float crackle;
        float ny;
        float nx;
        int i;
        float py;
        float px;
        float on;
        int col = p.tier.color | 0xFF000000;
        int light = p.tones[2];
        float cx = c.sw / 2.0f;
        float fade = 1.0f - LightningFx.smooth(LightningFx.prog(t, 4700.0f, 480.0f));
        float nameY = 110.0f;
        c.fx();
        float dim = LightningFx.smooth(LightningFx.prog(t, 0.0f, 140.0f)) * (1.0f - LightningFx.smooth(LightningFx.prog(t, 4650.0f, 520.0f)));
        if (dim > 0.004f) {
            c.vignette(0.3f * dim, 0.68f * dim, 132106);
        }
        float f = t < 140.0f ? 0.0f : (t < 210.0f ? 1.0f : (t < 250.0f ? 0.18f : (on = t < 420.0f ? 1.0f : 1.0f - LightningFx.smooth((t - 420.0f) / 320.0f))));
        if (on > 0.004f) {
            float x0 = cx + p.rnd(5, -70.0f, 70.0f);
            float x1 = cx;
            float y0 = -12.0f;
            float y1 = nameY - 10.0f;
            for (int i2 = 0; i2 <= 12; ++i2) {
                float f2 = (float)i2 / 12.0f;
                float jit = i2 == 0 || i2 == 12 ? 0.0f : p.rnd(10 + i2, -1.0f, 1.0f) * 26.0f * (float)Math.sin((double)f2 * Math.PI) + 4.0f;
                this.bx[i2] = LightningFx.lerp(x0, x1, f2) + jit;
                this.by[i2] = LightningFx.lerp(y0, y1, f2);
            }
            LightningFx.bolt(c, this.bx, this.by, 12, 1.0f, on, col);
            for (int b = 0; b < 2; ++b) {
                int from = b == 0 ? 4 : 7;
                float dir = b == 0 ? -1.0f : 1.0f;
                px = this.bx[from];
                py = this.by[from];
                for (i = 1; i <= 5; ++i) {
                    nx = px + dir * p.rnd(40 + b * 10 + i, 3.0f, 20.0f) + p.rnd(120 + b * 10 + i, -7.0f, 7.0f);
                    ny = py + p.rnd(60 + b * 10 + i, 7.0f, 17.0f);
                    float bw = 1.0f - (float)i / 6.0f;
                    LightningFx.seg(c, px, py, nx, ny, 0.55f * bw, on * bw, col);
                    px = nx;
                    py = ny;
                }
            }
            c.glow(x1, y1, 110.0f, FxCanvas.alpha(light, 0.45f * on));
        }
        if ((crackle = (1.0f - LightningFx.smooth(LightningFx.prog(t, 600.0f, 1500.0f))) * LightningFx.smooth(LightningFx.prog(t, 260.0f, 80.0f))) > 0.004f) {
            int step = (int)(t / 70.0f);
            for (int a = 0; a < 3; ++a) {
                int sd = step * 7 + a * 31 & 0x1FF;
                if (p.r[sd] < 0.35f) continue;
                float ang = p.rnd(sd + 1, 0.0f, 6.2832f);
                float ax = cx + (float)Math.cos(ang) * p.rnd(sd + 2, 70.0f, 130.0f);
                float ay = nameY + 14.0f + (float)Math.sin(ang) * p.rnd(sd + 3, 14.0f, 26.0f);
                px = ax;
                py = ay;
                for (i = 0; i < 4; ++i) {
                    nx = px + p.rnd(sd + 4 + i, -9.0f, 9.0f);
                    ny = py + p.rnd(sd + 8 + i, -7.0f, 7.0f);
                    LightningFx.seg(c, px, py, nx, ny, 0.45f, crackle, col);
                    px = nx;
                    py = ny;
                }
            }
        }
        float f1 = t < 140.0f ? 0.0f : (float)Math.exp(-(t - 140.0f) / 70.0f);
        float f2 = t < 250.0f ? 0.0f : (float)Math.exp(-(t - 250.0f) / 110.0f) * 0.55f;
        float flash = Math.min(0.85f, 0.85f * f1 + f2);
        c.fxEnd();
        float nf = LightningFx.prog(t, 150.0f, 340.0f);
        if (nf > 0.0f) {
            float size = 32.0f;
            if (c.width(p.donor, size, true) > c.sw - 60.0f) {
                size = 24.0f;
            }
            float s = LightningFx.lerp(2.1f, 1.0f, LightningFx.easeOutQuint(nf)) * (1.0f + 0.03f * LightningFx.easeInCubic(LightningFx.prog(t, 4700.0f, 480.0f)));
            float a = LightningFx.smooth(nf * 4.0f) * fade;
            float w = c.width(p.donor, size, true);
            c.fx();
            float pulse = 0.5f + 0.5f * (float)Math.sin((double)(t / 900.0f) * Math.PI * 2.0);
            c.glow(cx, nameY + c.capH(size) / 2.0f, w * 0.8f + 40.0f, 46.0f, FxCanvas.alpha(col, (0.42f + 0.1f * pulse) * a));
            c.fxEnd();
            c.push();
            c.translate(cx, nameY + c.capH(size) / 2.0f - LightningFx.easeInCubic(LightningFx.prog(t, 4700.0f, 480.0f)) * 8.0f);
            c.scale(s, s);
            c.text(p.donor, -w / 2.0f, -c.capH(size) / 2.0f, size, FxCanvas.alpha(-1, a), true);
            c.pop();
        }
        if ((ra = LightningFx.smooth(LightningFx.prog(t, 520.0f, 380.0f)) * fade) > 0.004f) {
            float rise = (1.0f - LightningFx.easeOutQuint(LightningFx.prog(t, 520.0f, 600.0f))) * 10.0f + LightningFx.easeInCubic(LightningFx.prog(t, 4700.0f, 480.0f)) * -8.0f;
            float pw = Math.max(c.width(p.line, 8.0f, false), c.width(p.amountText, 24.0f, true) * 1.1f) + 40.0f;
            float ph = 14.0f + c.capH(24.0f) + 8.0f + c.capH(8.0f) + 14.0f;
            float py2 = 156.0f + rise;
            c.layerUp();
            c.panel(cx - pw / 2.0f, py2, pw, ph, 9.0f, ra, col);
            c.layerUp();
            LightningFx.amount(c, p, cx, py2 + 14.0f, 24.0f, col, ra, t, 620.0f, 1000.0f, true);
            c.textC(p.line, cx, py2 + 14.0f + c.capH(24.0f) + 8.0f, 8.0f, FxCanvas.alpha(-925047072, ra), false);
        }
        if (flash > 0.004f) {
            c.layerUp();
            c.fx();
            c.rect(-24.0f, -24.0f, c.sw + 48.0f, c.sh + 48.0f, FxCanvas.withAlpha(0xF4F8FF, Math.round(255.0f * flash)));
            c.fxEnd();
        }
    }

    private static void bolt(FxCanvas c, float[] x, float[] y, int n, float width, float a, int col) {
        for (int i = 0; i < n; ++i) {
            float taper = 1.0f - (float)i / (float)n * 0.35f;
            LightningFx.seg(c, x[i], y[i], x[i + 1], y[i + 1], width * taper, a, col);
        }
    }

    private static void seg(FxCanvas c, float x0, float y0, float x1, float y1, float w, float a, int col) {
        int halo = FxCanvas.alpha(col, 0.16f * a);
        int body = FxCanvas.alpha(FxCanvas.mix(col, -1, 0.3f), 0.85f * a);
        int core = FxCanvas.withAlpha(0xFFFFFF, Math.round(255.0f * a));
        float len = (float)Math.hypot(x1 - x0, y1 - y0);
        c.glow((x0 + x1) / 2.0f, (y0 + y1) / 2.0f, len * 0.5f + 14.0f * w, len * 0.5f + 14.0f * w, halo);
        c.streak(x0, y0, x1, y1, 7.0f * w, 7.0f * w, body, body);
        c.disc(x1, y1, 3.5f * w, body);
        c.streak(x0, y0, x1, y1, 2.6f * w + 0.5f, 2.6f * w + 0.5f, core, core);
        c.disc(x1, y1, 1.3f * w + 0.25f, core);
    }
}

