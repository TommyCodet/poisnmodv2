/*
 * Decompiled with CFR 0.152.
 */
package net.friendswithmoney.fwm.stream.fx;

import net.friendswithmoney.fwm.stream.fx.FxAnim;
import net.friendswithmoney.fwm.stream.fx.FxCanvas;
import net.friendswithmoney.fwm.stream.fx.Play;

final class GlassCardFx
extends FxAnim {
    GlassCardFx() {
        super("card", "Glas-Karte", "Glass card", 4200, "Karte gleitet oben rein, Betrag z\u00e4hlt hoch, Glanz l\u00e4uft dr\u00fcber.", "A card slides in at the top, the amount counts up, a sheen runs across.");
    }

    @Override
    public void render(FxCanvas c, Play p, float t) {
        float a = GlassCardFx.smooth(GlassCardFx.prog(t, 0.0f, 300.0f)) * (1.0f - GlassCardFx.smooth(GlassCardFx.prog(t, 3780.0f, 380.0f)));
        if (a <= 0.004f) {
            return;
        }
        int col = p.tier.color | 0xFF000000;
        float head = 40.0f;
        float lineSize = 8.0f;
        float amtSize = 24.0f;
        float gap = 14.0f;
        float padX = 16.0f;
        float padY = 14.0f;
        float lineW = c.width(p.line, lineSize, false);
        float amtW = c.width(p.amountText, amtSize, true) * 1.1f;
        float w = Math.max(280.0f, padX * 2.0f + head + gap + Math.max(lineW, amtW));
        float h = padY * 2.0f + head;
        float drop = (1.0f - GlassCardFx.easeOutBack(GlassCardFx.prog(t, 0.0f, 620.0f), 1.3f)) * -20.0f;
        float lift = GlassCardFx.easeInCubic(GlassCardFx.prog(t, 3760.0f, 420.0f)) * -14.0f;
        float sc = GlassCardFx.lerp(0.96f, 1.0f, GlassCardFx.easeOutQuint(GlassCardFx.prog(t, 0.0f, 520.0f))) - 0.02f * GlassCardFx.easeInCubic(GlassCardFx.prog(t, 3760.0f, 420.0f));
        float x = -w / 2.0f;
        float y = -h / 2.0f;
        c.push();
        c.translate(c.sw / 2.0f, 16.0f + h / 2.0f + drop + lift);
        c.scale(sc, sc);
        c.fx();
        c.glow(0.0f, 4.0f, w * 0.62f, h * 1.1f, FxCanvas.alpha(col, 0.16f * a));
        c.fxEnd();
        c.layerUp();
        c.panel(x, y, w, h, 10.0f, a, col);
        c.layerUp();
        c.fx();
        float lw = w * 0.4f;
        float lh = Math.max(c.hair(), 1.0f);
        c.rectH(-lw, y, lw, lh, col & 0xFFFFFF, FxCanvas.alpha(col, a));
        c.rectH(0.0f, y, lw, lh, FxCanvas.alpha(col, a), col & 0xFFFFFF);
        c.fxEnd();
        float s = GlassCardFx.prog(t, 1150.0f, 1000.0f);
        if (s > 0.0f && s < 1.0f) {
            float e = GlassCardFx.easeInOut(s);
            float band = 46.0f;
            float skew = h * 0.55f;
            float bx = x - band - skew + e * (w + band * 2.0f + skew);
            int clear = 0xFFFFFF;
            int lit = FxCanvas.withAlpha(0xFFFFFF, Math.round(30.0f * a * (float)Math.sin(Math.PI * (double)s)));
            c.clip(x + 1.0f, y + 1.0f, w - 2.0f, h - 2.0f);
            c.fx();
            c.quad(bx, y + h, clear, bx + skew, y, clear, bx + skew + band / 2.0f, y, lit, bx + band / 2.0f, y + h, lit);
            c.quad(bx + band / 2.0f, y + h, lit, bx + skew + band / 2.0f, y, lit, bx + skew + band, y, clear, bx + band, y + h, clear);
            c.fxEnd();
            c.unclip();
        }
        float hx = x + padX;
        float hy = y + padY;
        c.head(p.donor, hx, hy, head, a);
        float tx = hx + head + gap;
        float block = c.capH(lineSize) + 8.0f + c.capH(amtSize);
        float ty = y + (h - block) / 2.0f;
        c.text(p.line, tx, ty, lineSize, FxCanvas.alpha(-925047072, a), false);
        GlassCardFx.amount(c, p, tx, ty + c.capH(lineSize) + 8.0f, amtSize, col, a, t, 260.0f, 900.0f, false);
        c.pop();
    }
}

