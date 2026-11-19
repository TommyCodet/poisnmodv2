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

final class TotemFx
extends FxAnim {
    private static final int[] SPARKS = new int[]{-9450694, -5840021, -861876, -2128, -12673446, -1522361};
    private static final int PARTS = 96;
    private static final float REST = 900.0f;
    private static final float LEAVE = 4050.0f;
    private static ItemStack totem;

    TotemFx() {
        super("totem", "Totem-Pop", "Totem pop", 5000, "Item dreht sich nach vorne wie beim Totem-Effekt, gr\u00fcn-gelbe Funken.", "The item turns to the front like the totem effect, green and yellow sparks.");
    }

    @Override
    public float impactMs() {
        return 900.0f;
    }

    @Override
    public void render(FxCanvas c, Play p, float t) {
        float spin;
        float size;
        float y;
        float ws;
        float cx = c.sw / 2.0f;
        float iy = 132.0f;
        float halo = TotemFx.smooth(TotemFx.prog(t, 500.0f, 500.0f)) * (1.0f - TotemFx.smooth(TotemFx.prog(t, 4050.0f, 450.0f)));
        c.fx();
        if (halo > 0.004f) {
            float br = 0.5f + 0.5f * (float)Math.sin((double)((t - 900.0f) / 1500.0f) * Math.PI * 2.0);
            c.glow(cx, iy, 92.0f + 8.0f * br, FxCanvas.withAlpha(13166698, Math.round(255.0f * (0.24f + 0.08f * br) * halo)));
            c.glow(cx, iy, 40.0f, FxCanvas.withAlpha(16775088, Math.round(45.9f * halo)));
        }
        if ((ws = t - 900.0f) >= 0.0f && ws < 900.0f) {
            float w = TotemFx.easeOut(ws / 900.0f);
            c.wave(cx, iy, TotemFx.lerp(30.0f, 150.0f, w), TotemFx.lerp(16.0f, 3.0f, w), FxCanvas.withAlpha(15332762, Math.round(200.0f * (1.0f - TotemFx.smooth(ws / 900.0f)))));
        }
        c.fxEnd();
        ItemStack stack = p.item;
        if (stack == null || stack.isEmpty() || stack.is((Object)Items.DRAGON_HEAD)) {
            if (totem == null) {
                totem = new ItemStack((ItemLike)Items.TOTEM_OF_UNDYING);
            }
            stack = totem;
        }
        if (t < 900.0f) {
            float f = t / 900.0f;
            e = TotemFx.easeOutQuint(f);
            y = TotemFx.lerp(c.sh + 40.0f, iy, TotemFx.easeOutBack(f, 1.1f));
            size = TotemFx.lerp(28.0f, 104.0f, e);
            spin = (1.0f - e) * (float)Math.PI * 5.0f;
        } else {
            y = iy + 2.5f * (float)Math.sin((double)((t - 900.0f) / 2200.0f) * Math.PI * 2.0);
            size = 92.0f + 12.0f * TotemFx.spring(t - 900.0f, 1.6f, 5.0f);
            spin = 0.22f * (float)Math.sin((double)((t - 900.0f) / 3000.0f) * Math.PI * 2.0);
            float lf = TotemFx.prog(t, 4050.0f, 650.0f);
            if (lf > 0.0f) {
                e = TotemFx.easeInBack(lf, 1.4f);
                size *= 1.0f - e;
                y -= TotemFx.easeInCubic(lf) * 60.0f;
                spin += TotemFx.easeInCubic(lf) * (float)Math.PI * 3.0f;
            }
        }
        if (size > 0.5f) {
            c.item(stack, cx, y, size, (float)Math.cos(spin));
        }
        c.layerUp();
        c.fx();
        for (int i = 0; i < 96; ++i) {
            float life;
            float born = i < 48 ? 260.0f + p.rnd(i * 6, 0.0f, 260.0f) : 860.0f + p.rnd(i * 6, 0.0f, 280.0f);
            float s = (t - born) / 1000.0f;
            float f = s / (life = p.rnd(i * 6 + 1, 1.1f, 2.2f));
            if (f < 0.0f || f >= 1.0f) continue;
            float ang = p.rnd(i * 6 + 2, 0.0f, 6.2832f);
            float v0 = p.rnd(i * 6 + 3, 120.0f, 360.0f);
            float k = 2.4f;
            float g = 70.0f;
            float vx0 = (float)Math.cos(ang) * v0;
            float vy0 = (float)Math.sin(ang) * v0 * 0.8f - 90.0f;
            float x = cx + TotemFx.dragX(vx0, k, s) + 6.0f * (float)Math.sin(s * 3.0f + (float)i);
            float yy = iy + TotemFx.dragY(vy0, g, k, s);
            float sz = p.rnd(i * 6 + 4, 2.6f, 5.2f) * (1.0f - f * 0.7f);
            float rot = p.rnd(i * 6 + 5, 0.0f, 6.28f) + s * (i % 2 == 0 ? 2.5f : -2.5f);
            int pc = SPARKS[i % SPARKS.length];
            float la = 1.0f - TotemFx.smooth((f - 0.55f) / 0.45f);
            if (i % 3 == 0) {
                c.glow(x, yy, sz * 3.2f, FxCanvas.alpha(pc, 0.25f * la));
            }
            c.rectRot(x, yy, sz, sz, rot, FxCanvas.alpha(pc, la));
        }
        c.fxEnd();
        c.layerUp();
        float a = TotemFx.window(t, 1050.0f, 320.0f, 4250.0f, 450.0f);
        float rise = (1.0f - TotemFx.easeOutQuint(TotemFx.prog(t, 1050.0f, 600.0f))) * 14.0f + TotemFx.easeInCubic(TotemFx.prog(t, 4250.0f, 450.0f)) * 8.0f;
        TotemFx.donorCard(c, p, cx, 222.0f + rise, t, 1150.0f, 1000.0f, a, 1.0f, false, 24.0f, p.tier.color);
    }
}

