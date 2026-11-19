/*
 * Decompiled with CFR 0.152.
 */
package net.friendswithmoney.fwm.stream.fx;

import net.friendswithmoney.fwm.stream.fx.FxAnim;
import net.friendswithmoney.fwm.stream.fx.FxCanvas;
import net.friendswithmoney.fwm.stream.fx.Play;

final class FireworksFx
extends FxAnim {
    private static final int ROCKETS = 7;
    private static final float[] LAUNCH = new float[]{0.0f, 360.0f, 760.0f, 1250.0f, 1700.0f, 2250.0f, 2800.0f};
    private static final int SPARKS = 56;

    FireworksFx() {
        super("fireworks", "Feuerwerk", "Fireworks", 6200, "Raketen steigen auf und explodieren in der Stufenfarbe.", "Rockets rise and burst in the tier colour.");
    }

    @Override
    public float impactMs() {
        return 650.0f;
    }

    @Override
    public void sounds(Play p, float t0, float t1) {
        int i;
        for (i = 1; i < 7; ++i) {
            FireworksFx.cue(p, t0, t1, LAUNCH[i], "firework_launch", 1.0f + (float)i * 0.04f, 0.45f);
        }
        for (i = 0; i < 7; ++i) {
            FireworksFx.cue(p, t0, t1, LAUNCH[i] + FireworksFx.flight(p, i), i % 2 == 0 ? "firework_blast" : "firework_twinkle", 1.0f, 0.6f);
        }
    }

    private static float flight(Play p, int i) {
        return p.rnd(600 + i * 8 + 4, 620.0f, 780.0f);
    }

    @Override
    public void render(FxCanvas c, Play p, float t) {
        int col = p.tier.color | 0xFF000000;
        float cx = c.sw / 2.0f;
        float dim = FireworksFx.window(t, 0.0f, 500.0f, 5400.0f, 700.0f);
        c.fx();
        if (dim > 0.004f) {
            c.vignette(0.1f * dim, 0.45f * dim, 0);
        }
        for (int i = 0; i < 7; ++i) {
            float bt;
            float start = LAUNCH[i];
            float fl = FireworksFx.flight(p, i);
            float side = i % 2 == 0 ? -1.0f : 1.0f;
            float ex = cx + side * p.rnd(600 + i * 8, 70.0f, c.sw * 0.42f);
            float ey = p.rnd(600 + i * 8 + 1, 42.0f, 125.0f);
            float sx = ex + p.rnd(600 + i * 8 + 2, -40.0f, 40.0f);
            float rf = (t - start) / fl;
            if (rf >= 0.0f && rf < 1.0f) {
                FireworksFx.rocket(c, sx, ex, ey, rf);
            }
            if (!((bt = t - start - fl) >= 0.0f) || !(bt < 2200.0f)) continue;
            FireworksFx.burstOf(c, p, i, ex, ey, bt / 1000.0f, col);
        }
        c.fxEnd();
        c.layerUp();
        float a = FireworksFx.window(t, 420.0f, 320.0f, 5560.0f, 520.0f);
        float pop = FireworksFx.easeOutBack(FireworksFx.prog(t, 420.0f, 560.0f), 1.7f);
        float s = FireworksFx.lerp(0.88f, 1.0f, pop) - 0.03f * FireworksFx.easeInCubic(FireworksFx.prog(t, 5560.0f, 520.0f));
        float y = c.sh / 2.0f - 46.0f - (1.0f - FireworksFx.easeOutQuint(FireworksFx.prog(t, 420.0f, 600.0f))) * -10.0f;
        FireworksFx.donorCard(c, p, cx, y, t, 650.0f, 1100.0f, a, s, true, 24.0f, col);
    }

    private static void rocket(FxCanvas c, float sx, float ex, float ey, float f) {
        float e = FireworksFx.easeOut(f);
        float x = FireworksFx.lerp(sx, ex, e);
        float y = FireworksFx.lerp(c.sh + 6.0f, ey, e);
        float pe = FireworksFx.easeOut(Math.max(0.0f, f - 0.14f));
        float tx = FireworksFx.lerp(sx, ex, pe);
        float ty = FireworksFx.lerp(c.sh + 6.0f, ey, pe);
        float a = 1.0f - FireworksFx.smooth((f - 0.85f) / 0.15f) * 0.6f;
        c.streak(tx, ty, x, y, 0.0f, 3.0f, 16765562, FxCanvas.withAlpha(16770984, Math.round(210.0f * a)));
        c.glow(x, y, 13.0f, FxCanvas.withAlpha(16765562, Math.round(130.0f * a)));
        c.disc(x, y, 1.9f, FxCanvas.withAlpha(0xFFFFFF, Math.round(255.0f * a)));
    }

    private static void burstOf(FxCanvas c, Play p, int i, float bx, float by, float s, int col) {
        float g;
        int hue;
        int kind = i % 3;
        int base = 100 + i * 120;
        float flash = 1.0f - FireworksFx.smooth(s / 0.28f);
        int n = kind == 2 ? p.tones[4] : (hue = i % 2 == 0 ? col : p.tones[1]);
        if (flash > 0.0f) {
            c.glow(bx, by, 70.0f, FxCanvas.alpha(hue, 0.55f * flash));
            c.glow(bx, by, 24.0f, FxCanvas.withAlpha(0xFFFFFF, Math.round(230.0f * flash)));
        }
        float k = kind == 2 ? 1.8f : 2.3f;
        float f = g = kind == 2 ? 90.0f : 50.0f;
        float speed = kind == 1 ? 230.0f : (kind == 2 ? 200.0f : 270.0f);
        int n2 = kind == 1 ? 40 : 56;
        float haze = (1.0f - FireworksFx.smooth(s / 1.2f)) * FireworksFx.smooth(s / 0.08f);
        if (haze > 0.0f) {
            c.glow(bx, by, 95.0f, FxCanvas.alpha(hue, 0.13f * haze));
        }
        for (int j = 0; j < n2; ++j) {
            float life = kind == 2 ? p.rnd(base + j, 1.6f, 2.1f) : p.rnd(base + j, 1.1f, 1.6f);
            float f2 = s / life;
            if (f2 >= 1.0f) continue;
            float ang = kind == 1 ? (float)(Math.PI * 2 * (double)j / (double)n2) : p.rnd(base + 50 + j, 0.0f, 6.2832f);
            float v0 = kind == 1 ? speed : speed * (float)Math.sqrt(p.rnd(base + 90 + j, 0.15f, 1.0f));
            float ca = (float)Math.cos(ang);
            float sa = (float)Math.sin(ang);
            float x = bx + ca * FireworksFx.dragX(v0, k, s);
            float y = by + FireworksFx.dragY(v0 * sa, g, k, s);
            float vx = ca * FireworksFx.dragV(v0, 0.0f, k, s);
            float vy = FireworksFx.dragV(v0 * sa, g, k, s);
            float sp = (float)Math.sqrt(vx * vx + vy * vy) + 0.001f;
            float tl = Math.min(kind == 2 ? 30.0f : 18.0f, sp * (kind == 2 ? 0.16f : 0.07f));
            float la = (1.0f - f2) * (1.0f - f2 * 0.5f);
            if (kind == 0 && f2 > 0.55f) {
                la *= 0.55f + 0.45f * (float)Math.sin(s * 38.0f + (float)j * 2.1f);
            }
            int pc = kind == 2 ? (j % 3 == 0 ? -3646 : p.tones[4]) : (j % 4 == 0 ? -1 : (j % 2 == 0 ? hue : p.tones[2]));
            float sz = p.rnd(base + 30 + j, 1.9f, 3.1f);
            c.streak(x - vx / sp * tl, y - vy / sp * tl, x, y, 0.0f, sz, pc & 0xFFFFFF, FxCanvas.alpha(pc, 0.85f * la));
            if (j % 2 == 0) {
                c.glow(x, y, sz * 3.2f, FxCanvas.alpha(pc, 0.22f * la));
            }
            c.disc(x, y, sz * 0.55f, FxCanvas.alpha(FxCanvas.mix(pc, -1, 0.5f), la));
        }
    }
}

