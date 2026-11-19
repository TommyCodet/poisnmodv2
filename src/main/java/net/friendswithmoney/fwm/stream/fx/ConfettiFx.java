/*
 * Decompiled with CFR 0.152.
 */
package net.friendswithmoney.fwm.stream.fx;

import net.friendswithmoney.fwm.stream.fx.FxAnim;
import net.friendswithmoney.fwm.stream.fx.FxCanvas;
import net.friendswithmoney.fwm.stream.fx.Play;

final class ConfettiFx
extends FxAnim {
    private static final int PIECES = 220;

    ConfettiFx() {
        super("confetti", "Konfetti", "Confetti", 6000, "Konfetti-Kanonen von beiden Seiten, Karte in der Mitte.", "Confetti cannons from both sides, the card in the middle.");
    }

    @Override
    public float impactMs() {
        return 300.0f;
    }

    @Override
    public void sounds(Play p, float t0, float t1) {
        ConfettiFx.cue(p, t0, t1, 60.0f, "firework_twinkle", 1.2f, 0.5f);
        ConfettiFx.cue(p, t0, t1, 340.0f, "firework_twinkle", 1.0f, 0.5f);
    }

    @Override
    public void render(FxCanvas c, Play p, float t) {
        float end = 1.0f - ConfettiFx.smooth(ConfettiFx.prog(t, 5350.0f, 600.0f));
        c.fx();
        for (int v = 0; v < 2; ++v) {
            float ps = (t - (float)(v == 0 ? 40 : 320)) / 1000.0f;
            if (ps < 0.0f || ps > 0.45f) continue;
            float pa = 1.0f - ConfettiFx.smooth(ps / 0.45f);
            float pr = 30.0f + 90.0f * ConfettiFx.easeOut(ps / 0.45f);
            c.glow(0.0f, c.sh, pr, FxCanvas.withAlpha(0xFFFFFF, Math.round(120.0f * pa)));
            c.glow(c.sw, c.sh, pr, FxCanvas.withAlpha(0xFFFFFF, Math.round(120.0f * pa)));
        }
        ConfettiFx.pieces(c, p, t, end, false);
        c.fxEnd();
        c.layerUp();
        float a = ConfettiFx.window(t, 160.0f, 280.0f, 5420.0f, 520.0f);
        float pop = ConfettiFx.easeOutBack(ConfettiFx.prog(t, 160.0f, 560.0f), 1.8f);
        float s = ConfettiFx.lerp(0.85f, 1.0f, pop) - 0.03f * ConfettiFx.easeInCubic(ConfettiFx.prog(t, 5420.0f, 520.0f));
        ConfettiFx.donorCard(c, p, c.sw / 2.0f, 118.0f - (1.0f - ConfettiFx.easeOutQuint(ConfettiFx.prog(t, 160.0f, 600.0f))) * 10.0f, t, 350.0f, 1000.0f, a, s, true, 24.0f, p.tier.color);
        c.layerUp();
        c.fx();
        ConfettiFx.pieces(c, p, t, end, true);
        c.fxEnd();
    }

    private static void pieces(FxCanvas c, Play p, float t, float end, boolean front) {
        if (end <= 0.004f) {
            return;
        }
        float k = 2.2f;
        float g = 150.0f;
        for (int i = 0; i < 220; ++i) {
            float w;
            if (i % 4 == 0 != front) continue;
            int b = i * 8;
            boolean left = i % 2 == 0;
            float born = (float)((i >> 1) % 2 == 0 ? 40 : 320) + p.rnd(b, 0.0f, 120.0f);
            float s = (t - born) / 1000.0f;
            if (s < 0.0f) continue;
            float ang = (float)Math.toRadians(p.rnd(b + 1, 52.0f, 80.0f));
            float v0 = p.rnd(b + 2, 480.0f, 830.0f);
            float vx0 = (float)(left ? 1 : -1) * (float)Math.cos(ang) * v0;
            float vy0 = -((float)Math.sin(ang)) * v0;
            float x = (left ? 0.0f : c.sw) + ConfettiFx.dragX(vx0, k, s);
            float y = c.sh + 4.0f + ConfettiFx.dragY(vy0, g, k, s);
            float settle = ConfettiFx.smooth((s - 0.5f) / 0.8f);
            if (y > c.sh + 12.0f || (x += settle * p.rnd(b + 3, 6.0f, 16.0f) * (float)Math.sin(s * p.rnd(b + 4, 2.2f, 4.0f) + (float)i)) < -20.0f || x > c.sw + 20.0f) continue;
            int shape = i % 10;
            float f = shape < 7 ? p.rnd(b + 5, 6.0f, 9.5f) : (w = shape < 9 ? 4.6f : p.rnd(b + 5, 11.0f, 15.0f));
            float h = shape < 7 ? p.rnd(b + 6, 3.0f, 4.4f) : (shape < 9 ? 4.6f : 2.0f);
            float flip = (float)Math.cos(s * p.rnd(b + 7, 5.0f, 11.0f) + (float)i * 1.3f);
            float rot = p.rnd(b, 0.0f, 6.28f) + s * p.rnd(b + 3, -4.0f, 4.0f);
            int pc = ConfettiFx.tone(p, i);
            float shade = flip < 0.0f ? 0.3f : 0.0f;
            pc = FxCanvas.mix(pc, -16777216, shade += (1.0f - Math.abs(flip)) * 0.25f);
            float hh = Math.max(0.5f, h * Math.abs(flip));
            c.rectRot(x, y, w, hh, rot, FxCanvas.alpha(pc, end));
        }
    }

    private static int tone(Play p, int i) {
        switch (i % 9) {
            case 0: 
            case 1: 
            case 2: {
                return p.tones[0];
            }
            case 3: 
            case 4: {
                return p.tones[1];
            }
            case 5: {
                return p.tones[3];
            }
            case 6: {
                return p.tones[4];
            }
            case 7: {
                return -1;
            }
        }
        return p.tones[2];
    }
}

