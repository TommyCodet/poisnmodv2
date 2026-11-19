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

final class GoldRainFx
extends FxAnim {
    private static final int[] BRIGHT = new int[]{0, 1, 2, 4};
    private static final int DROPS = 40;
    private static ItemStack[] drops;

    GoldRainFx() {
        super("goldrain", "Goldregen", "Gold rain", 7000, "Gro\u00dfe Karte mit pulsierendem Rand, Titel springt rein, Gold f\u00e4llt vom Himmel.", "Large card with a pulsing rim, the title drops in, gold falls from the sky.");
    }

    @Override
    public void sounds(Play p, float t0, float t1) {
        GoldRainFx.cue(p, t0, t1, 650.0f, "firework_blast", 1.0f, 0.9f);
        GoldRainFx.cue(p, t0, t1, 800.0f, "bell", 1.0f, 0.8f);
        GoldRainFx.cue(p, t0, t1, 800.0f, "bell", 1.26f, 0.7f);
        GoldRainFx.cue(p, t0, t1, 800.0f, "bell", 1.5f, 0.6f);
    }

    @Override
    public float impactMs() {
        return 650.0f;
    }

    @Override
    public void render(FxCanvas c, Play p, float t) {
        if (drops == null) {
            drops = new ItemStack[]{new ItemStack((ItemLike)Items.GOLD_INGOT), new ItemStack((ItemLike)Items.DIAMOND), new ItemStack((ItemLike)Items.EMERALD), new ItemStack((ItemLike)Items.GOLD_NUGGET)};
        }
        int col = p.tier.color | 0xFF000000;
        float cx = c.sw / 2.0f;
        float out = GoldRainFx.smooth(GoldRainFx.prog(t, 6380.0f, 560.0f));
        float env = GoldRainFx.smooth(GoldRainFx.prog(t, 520.0f, 160.0f)) * (1.0f - GoldRainFx.smooth(GoldRainFx.prog(t, 2200.0f, 1600.0f)));
        if (env > 0.004f) {
            float pulse = 0.55f + 0.45f * (float)Math.cos((double)((t - 680.0f) / 520.0f) * Math.PI * 2.0);
            float ea = env * (0.35f + 0.65f * pulse) * 0.55f;
            float d = 34.0f + 10.0f * pulse;
            int e = FxCanvas.alpha(col, ea);
            int clear = col & 0xFFFFFF;
            c.fx();
            c.rectV(0.0f, 0.0f, c.sw, d, e, clear);
            c.rectV(0.0f, c.sh - d, c.sw, d, clear, e);
            c.rectH(0.0f, 0.0f, d, c.sh, e, clear);
            c.rectH(c.sw - d, 0.0f, d, c.sh, clear, e);
            c.fxEnd();
        }
        for (int layer = 0; layer < 3; ++layer) {
            for (int i = 0; i < 40; ++i) {
                float speed;
                float size;
                float y;
                float born;
                float s;
                float z = p.rnd(i * 9, 0.0f, 1.0f);
                if ((int)Math.min(2.0f, z * 3.0f) != layer || (s = (t - (born = 380.0f + (float)Math.pow(p.rnd(i * 9 + 1, 0.0f, 1.0f), 1.4) * 2600.0f)) / 1000.0f) < 0.0f || (y = -(size = GoldRainFx.lerp(11.0f, 28.0f, z * z)) + (speed = GoldRainFx.lerp(90.0f, 230.0f, z)) * s + 18.0f * s * s) > c.sh + size) continue;
                float sway = (float)Math.sin(s * p.rnd(i * 9 + 2, 1.2f, 2.4f) + p.rnd(i * 9 + 3, 0.0f, 6.28f)) * GoldRainFx.lerp(3.0f, 9.0f, z);
                float x = p.rnd(i * 9 + 4, 0.03f, 0.97f) * c.sw + sway;
                float spin = p.rnd(i * 9 + 5, 0.0f, 6.28f) + s * p.rnd(i * 9 + 6, 1.6f, 3.6f) * (float)(i % 2 == 0 ? 1 : -1);
                float tilt = (float)Math.sin(s * 1.7f + (float)i) * 0.35f;
                c.item(drops[i % 4 == 3 ? (i % 8 == 3 ? 3 : 0) : i % 3], x, y, size, (float)Math.cos(spin), tilt);
            }
        }
        c.layerUp();
        float a = GoldRainFx.smooth(GoldRainFx.prog(t, 0.0f, 260.0f)) * (1.0f - out);
        if (a <= 0.004f) {
            return;
        }
        float pop = GoldRainFx.easeOutBack(GoldRainFx.prog(t, 0.0f, 560.0f), 1.6f);
        float sc = GoldRainFx.lerp(0.86f, 1.0f, pop) * (1.0f - 0.04f * GoldRainFx.easeInCubic(GoldRainFx.prog(t, 6380.0f, 560.0f)));
        float titleSize = 24.0f;
        float head = 32.0f;
        float lineSize = 8.0f;
        float amtSize = 24.0f;
        float titleW = GoldRainFx.titleWidth(c, p, titleSize);
        float lineW = c.width(p.line, lineSize, false);
        float amtW = c.width(p.amountText, amtSize, true) * 1.1f;
        float rowW = head + 12.0f + Math.max(lineW, amtW);
        float w = Math.max(340.0f, Math.max(titleW, rowW) + 56.0f);
        float h = 18.0f + c.capH(titleSize) + 18.0f + head + 18.0f;
        float y0 = 22.0f - GoldRainFx.easeInCubic(GoldRainFx.prog(t, 6380.0f, 560.0f)) * 10.0f;
        c.push();
        c.translate(cx, y0 + h / 2.0f);
        c.scale(sc, sc);
        float x = -w / 2.0f;
        float y = -h / 2.0f;
        float breath = 0.5f + 0.5f * (float)Math.sin((double)((t - 700.0f) / 1400.0f) * Math.PI * 2.0);
        float flare = 1.0f - GoldRainFx.smooth(GoldRainFx.prog(t, 650.0f, 700.0f));
        c.fx();
        c.glow(0.0f, 0.0f, w * 0.7f + 30.0f * flare, h * 1.25f + 20.0f * flare, FxCanvas.alpha(col, a * (0.14f + 0.06f * breath + 0.25f * flare * GoldRainFx.smooth(GoldRainFx.prog(t, 560.0f, 90.0f)))));
        c.fxEnd();
        c.layerUp();
        c.panel(x, y, w, h, 12.0f, a, col);
        int rim = FxCanvas.mix(col, -1, 0.25f * breath + 0.5f * flare);
        c.ring(x, y, w, h, 12.0f, FxCanvas.alpha(rim, a * (0.75f + 0.25f * breath)), 1.0f);
        c.ring(x - 2.0f, y - 2.0f, w + 4.0f, h + 4.0f, 14.0f, FxCanvas.alpha(col, a * (0.1f + 0.16f * breath)), 1.5f);
        c.layerUp();
        String[] chars = p.titleChars;
        float lx = -titleW / 2.0f;
        float ty = y + 18.0f;
        float shine = (t - 1500.0f) % 2600.0f / 2600.0f;
        for (int i = 0; i < chars.length; ++i) {
            float gw = c.width(chars[i], titleSize, true);
            float adv = gw + c.fs(titleSize) / 8.0f + 1.5f;
            float lf = GoldRainFx.prog(t, 120 + i * 36, 440.0f);
            float la = GoldRainFx.smooth(lf * 2.5f) * a;
            if (la > 0.004f) {
                float dy = (1.0f - GoldRainFx.easeOutBack(lf, 1.9f)) * -20.0f;
                float pos = chars.length <= 1 ? 0.0f : (float)i / (float)(chars.length - 1);
                float sh = t < 1500.0f ? 0.0f : (float)Math.exp(-Math.pow((pos - (shine * 1.6f - 0.3f)) * 7.0f, 2.0));
                int lc = FxCanvas.mix(col, -1, 0.12f + 0.55f * sh);
                c.text(chars[i], lx, ty + dy, titleSize, FxCanvas.alpha(lc, la), true);
            }
            lx += adv;
        }
        float ra = a * GoldRainFx.smooth(GoldRainFx.prog(t, 520.0f, 320.0f));
        float rowY = ty + c.capH(titleSize) + 18.0f + (1.0f - GoldRainFx.easeOutQuint(GoldRainFx.prog(t, 520.0f, 500.0f))) * 8.0f;
        float rx = -rowW / 2.0f;
        c.head(p.donor, rx, rowY, head, ra);
        float tx = rx + head + 12.0f;
        float block = c.capH(lineSize) + 7.0f + c.capH(amtSize);
        float by = rowY + (head - block) / 2.0f;
        c.text(p.line, tx, by, lineSize, FxCanvas.alpha(-925047072, ra), false);
        GoldRainFx.amount(c, p, tx, by + c.capH(lineSize) + 7.0f, amtSize, -1, ra, t, 650.0f, 1300.0f, false);
        c.pop();
        float gs = (t - 640.0f) / 1000.0f;
        if (gs >= 0.0f && gs < 1.1f) {
            c.fx();
            float gcy = y0 + h / 2.0f;
            for (int i = 0; i < 32; ++i) {
                float life = p.rnd(950 + i, 0.6f, 1.1f);
                float f = gs / life;
                if (f >= 1.0f) continue;
                float th = p.rnd(900 + i, 0.0f, 6.2832f);
                float ex = (float)Math.cos(th);
                float ey = (float)Math.sin(th);
                float v0 = p.rnd(990 + i, 90.0f, 220.0f);
                float d = GoldRainFx.dragX(v0, 3.2f, gs);
                float px = cx + ex * (w / 2.0f * sc + d);
                float py = gcy + ey * (h / 2.0f * sc + d * 0.7f) + 25.0f * gs * gs;
                float la = (1.0f - f) * (1.0f - f);
                int gc = p.tones[BRIGHT[i % 4]] | 0xFF000000;
                float tl = Math.min(12.0f, GoldRainFx.dragV(v0, 0.0f, 3.2f, gs) * 0.05f);
                c.streak(px - ex * tl, py - ey * tl * 0.7f, px, py, 0.0f, 2.0f, gc & 0xFFFFFF, FxCanvas.alpha(gc, la));
                c.disc(px, py, 1.1f, FxCanvas.alpha(-1, la));
            }
            c.fxEnd();
        }
    }

    private static float titleWidth(FxCanvas c, Play p, float size) {
        float w = 0.0f;
        for (String ch : p.titleChars) {
            w += c.width(ch, size, true) + c.fs(size) / 8.0f + 1.5f;
        }
        return Math.max(0.0f, w - c.fs(size) / 8.0f - 1.5f);
    }
}

