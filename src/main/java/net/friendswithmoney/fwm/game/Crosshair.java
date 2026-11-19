/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.event.player.AttackEntityCallback
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 */
package net.friendswithmoney.fwm.game;

import java.util.List;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.net.ServerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class Crosshair {
    public static final int N = 21;
    public static final int MID = 10;
    private static final long FLASH_MS = 160L;
    private static long hitAt;

    private Crosshair() {
    }

    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClientSide() && entity instanceof Player) {
                hitAt = System.currentTimeMillis();
            }
            return InteractionResult.PASS;
        });
    }

    public static boolean render(GuiGraphicsExtractor ctx) {
        if (!Features.isOn("fwm.tab.crosshair")) {
            return false;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || !client.options.getCameraType().isFirstPerson()) {
            return false;
        }
        int cx = (ctx.guiWidth() - 15) / 2 + 7;
        int cy = (ctx.guiHeight() - 15) / 2 + 7;
        Crosshair.draw(ctx, Crosshair.grid(), cx - 10, cy - 10, 1, Crosshair.color(client));
        return true;
    }

    public static void draw(GuiGraphicsExtractor ctx, boolean[] g, int x, int y, int px, int color) {
        for (int r = 0; r < 21; ++r) {
            int c = 0;
            while (c < 21) {
                if (!g[r * 21 + c]) {
                    ++c;
                    continue;
                }
                int start = c;
                while (c < 21 && g[r * 21 + c]) {
                    ++c;
                }
                ctx.fill(x + start * px, y + r * px, x + c * px, y + (r + 1) * px, color);
            }
        }
    }

    public static boolean[] grid() {
        boolean[] drawn;
        V2 c = V2.INSTANCE;
        if (c.crosshairCustom && (drawn = Crosshair.parse(c.crosshairPixels)) != null) {
            return drawn;
        }
        return Crosshair.shape();
    }

    public static boolean[] shape() {
        V2 c = V2.INSTANCE;
        boolean[] g = new boolean[441];
        int size = Math.max(1, c.crosshairSize);
        int t = Math.max(1, c.crosshairThickness);
        int gap = Math.max(0, c.crosshairGap);
        int lo = 10 - (t - 1) / 2;
        int hi = lo + t - 1;
        for (int i = gap + 1; i <= gap + size; ++i) {
            for (int k = lo; k <= hi; ++k) {
                Crosshair.set(g, k, 10 - i);
                Crosshair.set(g, k, 10 + i);
                Crosshair.set(g, 10 - i, k);
                Crosshair.set(g, 10 + i, k);
            }
        }
        if (c.crosshairDot || gap == 0) {
            for (int a = lo; a <= hi; ++a) {
                for (int b = lo; b <= hi; ++b) {
                    Crosshair.set(g, a, b);
                }
            }
        }
        return g;
    }

    private static void set(boolean[] g, int col, int row) {
        if (col >= 0 && col < 21 && row >= 0 && row < 21) {
            g[row * 21 + col] = true;
        }
    }

    public static boolean[] parse(String s) {
        if (s == null || s.length() != 441 || s.indexOf(49) < 0) {
            return null;
        }
        boolean[] g = new boolean[441];
        for (int i = 0; i < g.length; ++i) {
            g[i] = s.charAt(i) == '1';
        }
        return g;
    }

    public static String write(boolean[] g) {
        StringBuilder b = new StringBuilder(441);
        for (boolean on : g) {
            b.append(on ? (char)'1' : '0');
        }
        return b.toString();
    }

    public static int color(Minecraft client) {
        Player p;
        Entity entity;
        V2 c = V2.INSTANCE;
        if (c.crosshairHitFlash && System.currentTimeMillis() - hitAt < 160L) {
            return c.crosshairHit;
        }
        if (c.crosshairTargetColor && ServerContext.current() != ServerContext.Server.DONUT && (entity = client.crosshairPickEntity) instanceof Player && !(p = (Player)entity).isSpectator() && !p.isInvisible() && client.player != null && !p.isAlliedTo((Entity)client.player)) {
            return c.hitResult;
        }
        return Crosshair.baseColor();
    }

    public static int baseColor() {
        V2 c = V2.INSTANCE;
        List<Integer> colors = c.crosshairColors;
        if (colors.isEmpty()) {
            return -1;
        }
        if (!c.crosshairGradient || colors.size() < 2) {
            return colors.get(Math.max(0, Math.min(colors.size() - 1, c.crosshairSelected)));
        }
        float period = Math.max(0.5f, c.crosshairGradientSeconds) * 1000.0f;
        float pos = (float)(System.currentTimeMillis() % (long)period) / period * (float)colors.size();
        int i = (int)pos;
        return Crosshair.mix(colors.get(i % colors.size()), colors.get((i + 1) % colors.size()), pos - (float)i);
    }

    private static int mix(int a, int b, float t) {
        int ar = a >> 16 & 0xFF;
        int ag = a >> 8 & 0xFF;
        int ab = a & 0xFF;
        int br = b >> 16 & 0xFF;
        int bg = b >> 8 & 0xFF;
        int bb = b & 0xFF;
        return 0xFF000000 | Math.round((float)ar + (float)(br - ar) * t) << 16 | Math.round((float)ag + (float)(bg - ag) * t) << 8 | Math.round((float)ab + (float)(bb - ab) * t);
    }
}

