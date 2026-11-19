/*
 * Decompiled with CFR 0.152.
 */
package net.friendswithmoney.fwm.stream.fx;

import net.friendswithmoney.fwm.stream.fx.FxAnim;
import net.friendswithmoney.fwm.stream.fx.FxCanvas;
import net.friendswithmoney.fwm.stream.fx.Play;

final class ChatLineFx
extends FxAnim {
    ChatLineFx() {
        super("chat", "Nur Chatzeile", "Chat line only", 3500, "Eine Zeile im Chat, sonst nichts. Ideal f\u00fcr Small Donations.", "One line in the chat, nothing else. Good for small change.");
    }

    @Override
    public void render(FxCanvas c, Play p, float t) {
        if (!c.preview) {
            return;
        }
        float a = ChatLineFx.window(t, 0.0f, 220.0f, 3000.0f, 450.0f);
        if (a <= 0.004f) {
            return;
        }
        float rise = (1.0f - ChatLineFx.easeOutQuint(ChatLineFx.prog(t, 0.0f, 380.0f))) * 7.0f;
        String tag = "[FWM] ";
        float size = 8.0f;
        float tagW = c.width(tag, size, false) + c.width(" ", size, false);
        float w = tagW + c.width(p.line, size, false) + 8.0f;
        float h = 12.0f;
        float x = 4.0f;
        float y = c.sh - 52.0f + rise;
        c.fx();
        c.rect(x, y, w, h, FxCanvas.withAlpha(0, Math.round(128.0f * a)));
        float hl = 1.0f - ChatLineFx.smooth(ChatLineFx.prog(t, 250.0f, 900.0f));
        c.rect(x, y, 1.5f, h, FxCanvas.alpha(p.tier.color | 0xFF000000, a * (0.35f + 0.65f * hl)));
        c.fxEnd();
        float ty = y + (h - c.capH(size)) / 2.0f;
        c.text(tag, x + 5.0f, ty, size, FxCanvas.alpha(p.tier.color | 0xFF000000, a), false);
        c.text(p.line, x + 5.0f + tagW, ty, size, FxCanvas.alpha(-1, a), false);
    }
}

