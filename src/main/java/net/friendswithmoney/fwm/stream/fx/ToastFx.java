/*
 * Decompiled with CFR 0.152.
 */
package net.friendswithmoney.fwm.stream.fx;

import net.friendswithmoney.fwm.stream.fx.FxAnim;
import net.friendswithmoney.fwm.stream.fx.FxCanvas;
import net.friendswithmoney.fwm.stream.fx.Play;

final class ToastFx
extends FxAnim {
    ToastFx() {
        super("toast", "Kleiner Toast", "Small toast", 3200, "Kleine Karte oben rechts, st\u00f6rt nicht.", "A small card top right, out of the way.");
    }

    @Override
    public void render(FxCanvas c, Play p, float t) {
        float in = ToastFx.easeOutQuint(ToastFx.prog(t, 0.0f, 520.0f));
        float out = ToastFx.easeInCubic(ToastFx.prog(t, 2780.0f, 400.0f));
        float a = ToastFx.smooth(ToastFx.prog(t, 0.0f, 260.0f)) * (1.0f - ToastFx.smooth(ToastFx.prog(t, 2800.0f, 380.0f)));
        if (a <= 0.004f) {
            return;
        }
        int col = p.tier.color | 0xFF000000;
        float head = 24.0f;
        float nameSize = 8.0f;
        float amtSize = 16.0f;
        float gap = 10.0f;
        float pad = 10.0f;
        float textW = Math.max(c.width(p.donor, nameSize, false), c.width(p.amountText, amtSize, true) * 1.08f);
        float w = pad + head + gap + textW + pad + 4.0f;
        float h = pad + head + pad;
        float x = c.sw - 10.0f - w + (1.0f - in) * 34.0f + out * 34.0f;
        float y = 10.0f;
        c.panel(x, y, w, h, 8.0f, a, col);
        float left = 1.0f - ToastFx.prog(t, 450.0f, 2350.0f);
        float bw = (w - 20.0f) * left;
        c.round(x + 10.0f, y + h - 3.0f, w - 20.0f, 1.0f, 0.5f, FxCanvas.withAlpha(0xFFFFFF, Math.round(20.0f * a)));
        if (bw > 0.5f) {
            c.round(x + 10.0f, y + h - 3.0f, bw, 1.0f, 0.5f, FxCanvas.alpha(col, 0.75f * a));
        }
        c.layerUp();
        c.head(p.donor, x + pad, y + pad, head, a);
        float tx = x + pad + head + gap;
        float block = c.capH(nameSize) + 5.0f + c.capH(amtSize);
        float ty = y + (h - block) / 2.0f;
        c.text(p.donor, tx, ty, nameSize, FxCanvas.alpha(-925047072, a), false);
        ToastFx.amount(c, p, tx, ty + c.capH(nameSize) + 5.0f, amtSize, col, a, t, 180.0f, 750.0f, false);
    }
}

