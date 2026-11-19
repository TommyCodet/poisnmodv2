/*
 * Decompiled with CFR 0.152.
 */
package net.friendswithmoney.fwm.stream.fx;

import java.util.List;
import net.friendswithmoney.fwm.stream.DonationSounds;
import net.friendswithmoney.fwm.stream.fx.ChatLineFx;
import net.friendswithmoney.fwm.stream.fx.ConfettiFx;
import net.friendswithmoney.fwm.stream.fx.DragonFx;
import net.friendswithmoney.fwm.stream.fx.FireworksFx;
import net.friendswithmoney.fwm.stream.fx.FxCanvas;
import net.friendswithmoney.fwm.stream.fx.GlassCardFx;
import net.friendswithmoney.fwm.stream.fx.GoldRainFx;
import net.friendswithmoney.fwm.stream.fx.LightningFx;
import net.friendswithmoney.fwm.stream.fx.Play;
import net.friendswithmoney.fwm.stream.fx.TickerFx;
import net.friendswithmoney.fwm.stream.fx.ToastFx;
import net.friendswithmoney.fwm.stream.fx.TotemFx;
import net.friendswithmoney.fwm.ui.Lang;

public abstract class FxAnim {
    public final String id;
    private final String nameDe;
    private final String nameEn;
    private final String descDe;
    private final String descEn;
    public final int defMs;
    public static final List<FxAnim> ALL = List.of(new ChatLineFx(), new ToastFx(), new GlassCardFx(), new GoldRainFx(), new DragonFx(), new FireworksFx(), new TotemFx(), new LightningFx(), new TickerFx(), new ConfettiFx());
    protected static final int GLASS = -435087340;
    protected static final int INK = -722698;
    protected static final int INK_DIM = -925047072;

    protected FxAnim(String id, String nameDe, String nameEn, int defMs, String descDe, String descEn) {
        this.id = id;
        this.nameDe = nameDe;
        this.nameEn = nameEn;
        this.defMs = defMs;
        this.descDe = descDe;
        this.descEn = descEn;
    }

    public String name() {
        return Lang.t(this.nameDe, this.nameEn);
    }

    public String desc() {
        return Lang.t(this.descDe, this.descEn);
    }

    public abstract void render(FxCanvas var1, Play var2, float var3);

    public void sounds(Play p, float t0, float t1) {
    }

    public float impactMs() {
        return 600.0f;
    }

    public void onStart(Play p) {
    }

    public void shake(Play p, float t, float[] out) {
        out[0] = 0.0f;
        out[1] = 0.0f;
    }

    public static FxAnim byId(String id) {
        for (FxAnim a : ALL) {
            if (!a.id.equals(id)) continue;
            return a;
        }
        return ALL.get(2);
    }

    public static int indexOf(String id) {
        for (int i = 0; i < ALL.size(); ++i) {
            if (!FxAnim.ALL.get((int)i).id.equals(id)) continue;
            return i;
        }
        return 2;
    }

    protected static float clamp(float v) {
        return v < 0.0f ? 0.0f : (v > 1.0f ? 1.0f : v);
    }

    protected static float prog(float t, float start, float len) {
        return FxAnim.clamp((t - start) / Math.max(1.0f, len));
    }

    protected static float lerp(float a, float b, float f) {
        return a + (b - a) * f;
    }

    protected static float easeOut(float f) {
        f = FxAnim.clamp(f);
        return 1.0f - (1.0f - f) * (1.0f - f) * (1.0f - f);
    }

    protected static float easeIn(float f) {
        f = FxAnim.clamp(f);
        return f * f;
    }

    protected static float easeInOut(float f) {
        return (f = FxAnim.clamp(f)) < 0.5f ? 4.0f * f * f * f : 1.0f - (float)Math.pow(-2.0f * f + 2.0f, 3.0) / 2.0f;
    }

    protected static float keys(float f, float[] keys) {
        if (f <= keys[0]) {
            return keys[1];
        }
        for (int i = 2; i < keys.length; i += 2) {
            if (!(f <= keys[i])) continue;
            float b = keys[i];
            float a = keys[i - 2];
            float k = b <= a ? 1.0f : (f - a) / (b - a);
            return keys[i - 1] + (keys[i + 1] - keys[i - 1]) * k;
        }
        return keys[keys.length - 1];
    }

    protected static void cue(Play p, float t0, float t1, float at, String sound, float pitch, float volume) {
        if (!p.sound || !p.tier.animSounds) {
            return;
        }
        if (at > t0 && at <= t1) {
            DonationSounds.play(sound, pitch, volume * Math.max(0.3f, p.tier.volume));
        }
    }

    protected static float easeOutQuint(float f) {
        f = 1.0f - FxAnim.clamp(f);
        return 1.0f - f * f * f * f * f;
    }

    protected static float easeInCubic(float f) {
        f = FxAnim.clamp(f);
        return f * f * f;
    }

    protected static float easeOutBack(float f, float s) {
        f = FxAnim.clamp(f) - 1.0f;
        return 1.0f + f * f * ((s + 1.0f) * f + s);
    }

    protected static float easeInBack(float f, float s) {
        f = FxAnim.clamp(f);
        return f * f * ((s + 1.0f) * f - s);
    }

    protected static float smooth(float f) {
        f = FxAnim.clamp(f);
        return f * f * (3.0f - 2.0f * f);
    }

    protected static float spring(float ms, float hz, float damp) {
        if (ms <= 0.0f) {
            return 1.0f;
        }
        float s = ms / 1000.0f;
        return (float)(Math.exp(-damp * s) * Math.cos(Math.PI * 2 * (double)hz * (double)s));
    }

    protected static float window(float t, float inAt, float inLen, float outAt, float outLen) {
        return FxAnim.smooth(FxAnim.prog(t, inAt, inLen)) * (1.0f - FxAnim.smooth(FxAnim.prog(t, outAt, outLen)));
    }

    protected static float dragX(float v0, float k, float s) {
        return v0 / k * (1.0f - (float)Math.exp(-k * s));
    }

    protected static float dragY(float v0, float g, float k, float s) {
        float e = 1.0f - (float)Math.exp(-k * s);
        return (v0 - g / k) / k * e + g / k * s;
    }

    protected static float dragV(float v0, float g, float k, float s) {
        float e = (float)Math.exp(-k * s);
        return v0 * e + g / k * (1.0f - e);
    }

    protected static void dampedShake(float t, float at, float amp, float calm, float[] out) {
        float d = t - at;
        if (d < 0.0f || d > calm * 3.5f) {
            out[1] = 0.0f;
            out[0] = 0.0f;
            return;
        }
        float s = d / 1000.0f;
        float env = amp * (float)Math.exp(-d / calm) * FxAnim.smooth(d / 24.0f);
        out[0] = env * (float)Math.sin(119.38052083641213 * (double)s);
        out[1] = env * 0.7f * (float)Math.sin(87.96459430051421 * (double)s + 1.3);
    }

    protected static float donorCard(FxCanvas c, Play p, float cx, float y, float t, float countStart, float countLen, float a, float s, boolean title, float amtSize, int amtColor) {
        float head = 32.0f;
        float lineSize = 8.0f;
        float gap = 12.0f;
        float padX = 16.0f;
        float padY = 14.0f;
        float lineW = c.width(p.line, lineSize, false);
        float amtW = c.width(p.amountText, amtSize, true) * 1.1f;
        float titleW = title ? c.width(p.title, lineSize, true) : 0.0f;
        float textW = Math.max(lineW, amtW);
        float w = Math.max(padX * 2.0f + head + gap + textW, padX * 2.0f + titleW);
        w = Math.max(w, 200.0f);
        float titleH = title ? c.capH(lineSize) + 10.0f : 0.0f;
        float blockH = c.capH(lineSize) + 7.0f + c.capH(amtSize);
        float rowH = Math.max(head, blockH);
        float h = padY * 2.0f + titleH + rowH;
        if (a <= 0.004f || s <= 0.01f) {
            return h;
        }
        c.push();
        c.translate(cx, y + h / 2.0f);
        c.scale(s, s);
        c.translate(-w / 2.0f, -h / 2.0f);
        c.panel(0.0f, 0.0f, w, h, 9.0f, a, p.tier.color);
        c.layerUp();
        int col = p.tier.color | 0xFF000000;
        c.fx();
        float lw = w * 0.42f;
        float lh = Math.max(c.hair(), 1.0f);
        c.rectH(w / 2.0f - lw, 0.0f, lw, lh, col & 0xFFFFFF, FxCanvas.alpha(col, a));
        c.rectH(w / 2.0f, 0.0f, lw, lh, FxCanvas.alpha(col, a), col & 0xFFFFFF);
        c.fxEnd();
        float top = padY;
        if (title) {
            c.textC(p.title, w / 2.0f, top, lineSize, FxCanvas.alpha(col, a), true);
            top += titleH;
        }
        float rowW = head + gap + textW;
        float rx = title ? (w - rowW) / 2.0f : padX;
        c.head(p.donor, rx, top + (rowH - head) / 2.0f, head, a);
        float tx = rx + head + gap;
        float ty = top + (rowH - blockH) / 2.0f;
        c.text(p.line, tx, ty, lineSize, FxCanvas.alpha(-925047072, a), false);
        FxAnim.amount(c, p, tx, ty + c.capH(lineSize) + 7.0f, amtSize, amtColor, a, t, countStart, countLen, false);
        c.pop();
        return h;
    }

    protected static void amount(FxCanvas c, Play p, float x, float y, float size, int color, float a, float t, float countStart, float countLen, boolean centred) {
        if (a <= 0.004f) {
            return;
        }
        String s = p.counted(FxAnim.prog(t, countStart, countLen));
        float wFinal = c.width(p.amountText, size, true);
        float land = t - (countStart + countLen);
        float pop = land < 0.0f ? 0.0f : 1.0f - FxAnim.easeOut(land / 450.0f);
        float sc = 1.0f + 0.08f * pop;
        int col = FxCanvas.mix(color | 0xFF000000, -1, 0.6f * pop);
        float ch = c.capH(size);
        c.push();
        c.translate(x, y + ch / 2.0f);
        if (sc != 1.0f) {
            c.scale(sc, sc);
        }
        float lx = centred ? wFinal / 2.0f - c.width(s, size, true) : 0.0f;
        c.text(s, lx, -ch / 2.0f, size, FxCanvas.alpha(col, a), true);
        c.pop();
    }

    protected static void nameCard(FxCanvas c, Play p, float cx, float y, boolean big, float t, float countStart, float countLen, float a, float s) {
        FxAnim.donorCard(c, p, cx, y, t, countStart, countLen, a, s, big, big ? 24.0f : 16.0f, p.tier.color);
    }

    protected static void burst(FxCanvas c, Play p, float t, float x, float y, int n, int seed, int[] colors, float dist, float start, float len, float size) {
        float f = (t - start) / len;
        if (f < 0.0f || f > 1.0f) {
            return;
        }
        float s = (t - start) / 1000.0f;
        float k = 3.4f;
        float life = 1.0f - f;
        for (int i = 0; i < n; ++i) {
            float ang = (float)(Math.PI * 2 * (double)i / (double)n) + p.rnd(seed + i * 2, -0.25f, 0.25f);
            float v0 = p.rnd(seed + i * 2 + 1, 0.55f, 1.0f) * dist * k;
            float ca = (float)Math.cos(ang);
            float sa = (float)Math.sin(ang);
            float d = FxAnim.dragX(v0, k, s);
            float px = x + ca * d;
            float py = y + sa * d + 30.0f * s * s;
            float sp = FxAnim.dragV(v0, 0.0f, k, s);
            float tail = Math.min(16.0f, sp * 0.028f);
            int col = colors[i % colors.length] | 0xFF000000;
            int head = FxCanvas.alpha(col, life * life * (2.0f - life));
            c.streak(px - ca * tail, py - sa * tail, px, py, 0.0f, size * 0.7f, col & 0xFFFFFF, head);
            c.disc(px, py, size * 0.45f * (0.5f + 0.5f * life), head);
        }
    }
}

