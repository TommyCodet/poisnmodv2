/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package net.friendswithmoney.fwm.stream.fx;

import net.friendswithmoney.fwm.stream.fx.FxAnim;
import net.friendswithmoney.fwm.stream.fx.FxCanvas;
import net.friendswithmoney.fwm.stream.fx.Play;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

final class DragonFx
extends FxAnim {
    private static final int[] BRIGHT = new int[]{0, 1, 2, 4};
    private static final float IMPACT = 650.0f;
    private static final float END = 10250.0f;
    private static final int SPARKS = 56;
    private static final int EMBERS = 34;
    private static ItemStack fallback;

    DragonFx() {
        super("dragon", "Drachen-Einschlag", "Dragon strike", 11000, "Bild wird dunkel, riesiges 3D-Item schl\u00e4gt ein, Strahlen drehen sich, Titel wackelt.", "The screen darkens, a huge item strikes, rays turn, the title wobbles.");
    }

    @Override
    public float impactMs() {
        return 1200.0f;
    }

    @Override
    public void shake(Play p, float t, float[] out) {
        DragonFx.dampedShake(t, 650.0f, 3.6f, 170.0f, out);
    }

    @Override
    public void render(FxCanvas c, Play p, float t) {
        float ri;
        float emberA;
        float spin;
        float size;
        float since;
        int col = p.tier.color | 0xFF000000;
        int gold = p.tones[4];
        float cx = c.sw / 2.0f;
        float iy = 112.0f;
        float ending = DragonFx.smooth(DragonFx.prog(t, 10250.0f, 650.0f));
        float fade = 1.0f - ending;
        float dark = DragonFx.smooth(DragonFx.prog(t, 0.0f, 450.0f)) * (1.0f - DragonFx.smooth(DragonFx.prog(t, 10400.0f, 600.0f)));
        float raysA = DragonFx.smooth(DragonFx.prog(t, 610.0f, 520.0f)) * fade;
        c.fx();
        if (dark > 0.004f) {
            c.vignette(0.42f * dark, 0.86f * dark, 0);
        }
        if (raysA > 0.004f) {
            float half;
            float ang;
            int i;
            float sec = t / 1000.0f;
            for (i = 0; i < 12; ++i) {
                ang = (float)(Math.PI * 2 * (double)i / 12.0) + sec * 0.13f + p.rnd(20 + i, -0.12f, 0.12f);
                half = p.rnd(40 + i, 0.07f, 0.16f);
                float len = p.rnd(60 + i, 230.0f, 330.0f);
                c.ray(cx, iy, ang, half, 22.0f, len, FxCanvas.alpha(col, 0.55f * raysA));
            }
            for (i = 0; i < 9; ++i) {
                ang = (float)(Math.PI * 2 * (double)((float)i + 0.5f) / 9.0) - sec * 0.085f;
                half = p.rnd(80 + i, 0.05f, 0.09f);
                c.ray(cx, iy, ang, half, 26.0f, p.rnd(90 + i, 170.0f, 250.0f), FxCanvas.alpha(gold, 0.3f * raysA));
            }
            float breath = 0.5f + 0.5f * (float)Math.sin((double)((t - 650.0f) / 1900.0f) * Math.PI * 2.0);
            c.glow(cx, iy, 96.0f + 10.0f * breath, FxCanvas.alpha(col, (0.38f + 0.12f * breath) * raysA));
        }
        if ((since = t - 650.0f) >= 0.0f && since < 1100.0f) {
            float fl = 1.0f - DragonFx.smooth(since / 320.0f);
            c.glow(cx, iy, 170.0f + since * 0.15f, FxCanvas.withAlpha(0xFFFFFF, Math.round(190.0f * fl)));
            float w1 = DragonFx.easeOut(since / 950.0f);
            c.wave(cx, iy, DragonFx.lerp(28.0f, 300.0f, w1), DragonFx.lerp(30.0f, 6.0f, w1), FxCanvas.alpha(FxCanvas.mix(col, -1, 0.35f), 0.85f * (1.0f - DragonFx.smooth(since / 950.0f))));
            float s2 = since - 90.0f;
            if (s2 > 0.0f) {
                float w2 = DragonFx.easeOut(s2 / 750.0f);
                c.wave(cx, iy, DragonFx.lerp(20.0f, 210.0f, w2), DragonFx.lerp(12.0f, 2.0f, w2), FxCanvas.withAlpha(0xFFFFFF, Math.round(170.0f * (1.0f - DragonFx.smooth(s2 / 750.0f)))));
            }
        }
        c.fxEnd();
        ItemStack stack = p.item;
        if (stack == null || stack.isEmpty()) {
            if (fallback == null) {
                fallback = new ItemStack((ItemLike)Items.DRAGON_HEAD);
            }
            stack = fallback;
        }
        float bob = 0.0f;
        if (t < 650.0f) {
            float f = DragonFx.clamp(t / 650.0f);
            size = DragonFx.lerp(6.0f, 120.0f, (float)Math.pow(f, 2.6));
            spin = (1.0f - DragonFx.easeOut(f)) * (float)Math.PI * 4.0f;
        } else {
            float base = 96.0f;
            size = base * (1.0f + 0.25f * DragonFx.spring(since, 2.4f, 6.5f));
            spin = 0.32f * (float)Math.sin((double)((t - 650.0f) / 4200.0f) * Math.PI * 2.0);
            float twirl = DragonFx.prog(t, 5600.0f, 900.0f);
            if (twirl > 0.0f && twirl < 1.0f) {
                spin += DragonFx.easeInOut(twirl) * (float)Math.PI * 2.0f;
            }
            bob = 3.0f * (float)Math.sin((double)((t - 650.0f) / 2600.0f) * Math.PI * 2.0) * DragonFx.smooth(DragonFx.prog(t, 950.0f, 600.0f));
            float rec = DragonFx.prog(t, 10000.0f, 700.0f);
            if (rec > 0.0f) {
                size *= 1.0f - DragonFx.easeInCubic(rec);
                bob -= DragonFx.easeInCubic(rec) * 14.0f;
            }
        }
        if (size > 0.5f) {
            c.item(stack, cx, iy + bob, size, (float)Math.cos(spin));
        }
        c.layerUp();
        c.fx();
        if (since >= 0.0f && since < 1600.0f) {
            float s = since / 1000.0f;
            for (int i = 0; i < 56; ++i) {
                float life = p.rnd(300 + i, 0.75f, 1.5f);
                float f = s / life;
                if (f >= 1.0f) continue;
                float ang = p.rnd(400 + i, 0.0f, 6.2832f);
                float v0 = p.rnd(500 + i, 170.0f, 440.0f);
                float k = 3.0f;
                float g = 120.0f;
                float ca = (float)Math.cos(ang);
                float sa = (float)Math.sin(ang);
                float x = cx + ca * DragonFx.dragX(v0, k, s);
                float y = iy + DragonFx.dragY(v0 * sa, g, k, s);
                float vx = ca * DragonFx.dragV(v0, 0.0f, k, s);
                float vy = DragonFx.dragV(v0 * sa, g, k, s);
                float sp = (float)Math.sqrt(vx * vx + vy * vy);
                float tl = Math.min(26.0f, sp * 0.045f);
                float sz = p.rnd(600 + i, 1.2f, 3.0f);
                int pc = p.tones[BRIGHT[i % 4]] | 0xFF000000;
                float la = (1.0f - f) * (1.0f - f);
                c.streak(x - vx / sp * tl, y - vy / sp * tl, x, y, 0.0f, sz, pc & 0xFFFFFF, FxCanvas.alpha(pc, la));
                c.disc(x, y, sz * 0.6f, FxCanvas.alpha(-1, la));
            }
        }
        if ((emberA = DragonFx.smooth(DragonFx.prog(t, 1000.0f, 900.0f)) * fade) > 0.004f) {
            for (int i = 0; i < 34; ++i) {
                float speed = p.rnd(700 + i, 14.0f, 34.0f);
                float span = c.sh + 40.0f;
                float yy = c.sh + 20.0f - (t / 1000.0f * speed + p.rnd(740 + i, 0.0f, span)) % span;
                float xx = p.rnd(780 + i, 0.08f, 0.92f) * c.sw + 10.0f * (float)Math.sin(t / 1000.0f * p.rnd(820 + i, 0.6f, 1.4f) + (float)i);
                float tw = 0.55f + 0.45f * (float)Math.sin(t / 1000.0f * p.rnd(860 + i, 2.0f, 5.0f) + (float)i * 1.7f);
                float edge = DragonFx.smooth((c.sh + 20.0f - yy) / 60.0f) * DragonFx.smooth((yy + 20.0f) / 80.0f);
                float ea = emberA * tw * edge;
                int ec = i % 3 == 0 ? gold : col;
                float r = p.rnd(900 + i, 0.8f, 1.9f);
                c.glow(xx, yy, r * 5.0f, FxCanvas.alpha(ec, 0.3f * ea));
                c.disc(xx, yy, r, FxCanvas.alpha(FxCanvas.mix(ec, -1, 0.4f), 0.9f * ea));
            }
        }
        c.fxEnd();
        c.layerUp();
        float ti = DragonFx.prog(t, 880.0f, 560.0f);
        if (ti > 0.0f && fade > 0.004f) {
            String[] chars = p.titleChars;
            float ts = 24.0f;
            float tw = DragonFx.lettersWidth(c, chars, ts);
            if (tw > c.sw - 40.0f) {
                ts = 16.0f;
                tw = DragonFx.lettersWidth(c, chars, ts);
            }
            float sc = DragonFx.lerp(0.35f, 1.0f, DragonFx.easeOutBack(ti, 2.4f));
            sc *= 1.0f + 0.022f * (float)Math.sin((double)((t - 1440.0f) / 1800.0f) * Math.PI * 2.0) * DragonFx.smooth(DragonFx.prog(t, 1440.0f, 500.0f));
            sc *= 1.0f + 0.06f * DragonFx.easeInCubic(DragonFx.prog(t, 10250.0f, 600.0f));
            float ta = DragonFx.smooth(ti * 3.0f) * fade;
            float ty = 190.0f;
            float shine = (t - 1700.0f) % 2800.0f / 2800.0f;
            c.push();
            c.translate(cx, ty + c.capH(ts) / 2.0f);
            c.scale(sc, sc);
            float lx = -tw / 2.0f;
            int n = Math.max(1, chars.length - 1);
            float adv = c.fs(ts) / 8.0f + 1.0f;
            for (int i = 0; i < chars.length; ++i) {
                float pos = (float)i / (float)n;
                float g = 0.5f - 0.5f * (float)Math.cos((double)pos * Math.PI * 2.0);
                int lc = FxCanvas.mix(col, gold, g);
                float sh = t < 1700.0f ? 0.0f : (float)Math.exp(-Math.pow((pos - (shine * 1.6f - 0.3f)) * 6.0f, 2.0));
                lc = FxCanvas.mix(lc, -1, 0.6f * sh);
                c.text(chars[i], lx, -c.capH(ts) / 2.0f, ts, FxCanvas.alpha(lc, ta), true);
                lx += c.width(chars[i], ts, true) + adv;
            }
            c.pop();
        }
        if ((ri = DragonFx.prog(t, 1350.0f, 520.0f)) > 0.0f) {
            float a = DragonFx.smooth(ri * 1.6f) * fade;
            float rise = (1.0f - DragonFx.easeOutQuint(ri)) * 14.0f + ending * 8.0f;
            DragonFx.donorCard(c, p, cx, 226.0f + rise, t, 1450.0f, 2200.0f, a, 1.0f, false, 24.0f, -670398 == gold ? gold : FxCanvas.mix(gold, -1, 0.1f));
        }
    }

    private static float lettersWidth(FxCanvas c, String[] chars, float size) {
        float w = 0.0f;
        float adv = c.fs(size) / 8.0f + 1.0f;
        for (String ch : chars) {
            w += c.width(ch, size, true) + adv;
        }
        return Math.max(0.0f, w - adv);
    }
}

