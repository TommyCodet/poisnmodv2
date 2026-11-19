/*
 * Decompiled with CFR 0.152.
 */
package net.friendswithmoney.fwm.stream.fx;

import net.friendswithmoney.fwm.stream.fx.FxAnim;
import net.friendswithmoney.fwm.stream.fx.FxCanvas;
import net.friendswithmoney.fwm.stream.fx.Play;

final class TickerFx
extends FxAnim {
    TickerFx() {
        super("ticker", "Laufschrift", "Ticker", 6000, "Name und Betrag laufen als Band quer \u00fcbers Bild. Dezent, gut im Kampf.", "Name and amount run across the screen on a band. Quiet, good in a fight.");
    }

    @Override
    public void render(FxCanvas c, Play p, float t) {
        float open = TickerFx.easeOutQuint(TickerFx.prog(t, 0.0f, 420.0f)) * (1.0f - TickerFx.easeInCubic(TickerFx.prog(t, 5600.0f, 380.0f)));
        float a = TickerFx.smooth(TickerFx.prog(t, 0.0f, 260.0f)) * (1.0f - TickerFx.smooth(TickerFx.prog(t, 5640.0f, 360.0f)));
        if (a <= 0.004f || open <= 0.004f) {
            return;
        }
        int col = p.tier.color | 0xFF000000;
        float bandH = 22.0f;
        float cy = 6.0f + bandH / 2.0f;
        float h = bandH * open;
        float y = cy - h / 2.0f;
        float size = 8.0f;
        c.fx();
        c.rectV(0.0f, y, c.sw, h, FxCanvas.alpha(-434757861, a), FxCanvas.alpha(-435482354, a));
        float hl = c.hair();
        c.rect(0.0f, y, c.sw, hl, FxCanvas.withAlpha(0xFFFFFF, Math.round(34.0f * a)));
        int edge = FxCanvas.alpha(col, 0.85f * a);
        c.rectH(0.0f, y + h - hl * 2.0f, c.sw * 0.5f, hl * 2.0f, col & 0xFFFFFF, edge);
        c.rectH(c.sw * 0.5f, y + h - hl * 2.0f, c.sw * 0.5f, hl * 2.0f, edge, col & 0xFFFFFF);
        c.fxEnd();
        c.layerUp();
        float labelIn = TickerFx.easeOutQuint(TickerFx.prog(t, 140.0f, 520.0f));
        float la = a * TickerFx.smooth(TickerFx.prog(t, 140.0f, 320.0f));
        float lw = c.width(p.title, size, true) + 14.0f;
        float lh = 14.0f;
        float lx = 10.0f - (1.0f - labelIn) * 18.0f;
        float textLeft = 10.0f + lw + 12.0f;
        if (la > 0.004f) {
            c.round(lx, cy - lh / 2.0f, lw, lh, 4.0f, FxCanvas.alpha(col, la));
        }
        c.layerUp();
        if (la > 0.004f && open > 0.6f) {
            c.text(p.title, lx + 7.0f, cy - c.capH(size) / 2.0f, size, FxCanvas.alpha(-16248307, la), true);
        }
        if (open < 0.55f) {
            return;
        }
        String[] g = p.tickGlyphs;
        float total = 0.0f;
        for (int i = 0; i < g.length; ++i) {
            total += TickerFx.glyphW(c, p, i, size);
        }
        float travel = c.sw - textLeft + total;
        float x = c.sw - travel * TickerFx.prog(t, 380.0f, 5150.0f);
        float ty = cy - c.capH(size) / 2.0f;
        float fadeL = textLeft;
        float fadeR = c.sw;
        for (int i = 0; i < g.length; ++i) {
            float gw = TickerFx.glyphW(c, p, i, size);
            if (x + gw > fadeL && x < fadeR) {
                float mid = x + gw / 2.0f;
                float ea = TickerFx.smooth((mid - fadeL) / 26.0f) * TickerFx.smooth((fadeR - mid) / 40.0f);
                byte role = p.tickRole[i];
                int gc = role == 0 ? -722698 : (role == 2 ? col : (role == 3 ? FxCanvas.alpha(col, 0.8f) : (role == 1 ? -925047072 : -419430401)));
                c.text(g[i], x, ty, size, FxCanvas.alpha(gc, ea * a), role == 0 || role == 2);
            }
            x += gw;
        }
    }

    private static float glyphW(FxCanvas c, Play p, int i, float size) {
        byte role = p.tickRole[i];
        return c.width(p.tickGlyphs[i], size, role == 0 || role == 2) + c.fs(size) / 8.0f;
    }
}

