/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.NativeImage
 */
package net.friendswithmoney.fwm.textures;

import com.mojang.blaze3d.platform.NativeImage;
import java.awt.Color;
import java.util.List;
import net.friendswithmoney.fwm.ui.Lang;

public final class FwmStyles {
    public static final String PREFIX = "fwm:";
    public static final List<Style> ALL = List.of(new Style("mint", "Mint", "Mint", Kind.MAP, 932656, 4177813, 13238250), new Style("petrol", "Petrol", "Petrol", Kind.MAP, 666150, 3112812, 10478543), new Style("gold", "FWM Gold", "FWM Gold", Kind.MAP, 3809792, 13207066, 16771494), new Style("blood", "Blood", "Blood", Kind.MAP, 1966084, 10095126, 0xFF6B6B), new Style("sakura", "Sakura", "Sakura", Kind.MAP, 3805224, 15105968, 16770032), new Style("ocean", "Ozean", "Ocean", Kind.MAP, 268858, 2063318, 11071743), new Style("frost", "Frost", "Frost", Kind.MAP, 2767442, 10275056, 0xFFFFFF), new Style("shadow", "Schatten", "Shadow", Kind.MAP, 328968, 0x262636, 8158358), new Style("sand", "Sand", "Sand", Kind.MAP, 3811870, 13608076, 16247773), new Style("neon", "Neon", "Neon", Kind.NEON, 0x10001E, 16722902, 2881023), new Style("rainbow", "Regenbogen", "Rainbow", Kind.RAINBOW, 0, 0, 0), new Style("glow", "Glow-Rand", "Glow edge", Kind.GLOW, 0, 8253638, 0));

    private FwmStyles() {
    }

    public static List<Style> forCategory(String category) {
        return List.of();
    }

    public static boolean isStyle(String choice) {
        return choice != null && choice.startsWith(PREFIX);
    }

    public static Style byKey(String choice) {
        if (!FwmStyles.isStyle(choice)) {
            return null;
        }
        String id = choice.substring(PREFIX.length());
        for (Style s : ALL) {
            if (!s.id().equals(id)) continue;
            return s;
        }
        return null;
    }

    public static NativeImage apply(Style style, NativeImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        NativeImage out = new NativeImage(w, h, true);
        for (int y = 0; y < h; ++y) {
            for (int x = 0; x < w; ++x) {
                int argb = src.getPixel(x, y);
                int a = argb >>> 24;
                if (a == 0) {
                    out.setPixel(x, y, 0);
                    continue;
                }
                int r = argb >> 16 & 0xFF;
                int g = argb >> 8 & 0xFF;
                int b = argb & 0xFF;
                float l = (0.299f * (float)r + 0.587f * (float)g + 0.114f * (float)b) / 255.0f;
                int rgb = switch (style.kind().ordinal()) {
                    default -> throw new MatchException(null, null);
                    case 0 -> FwmStyles.map(style, FwmStyles.contrast(l));
                    case 3 -> FwmStyles.neon(style, l, r, g, b);
                    case 1 -> FwmStyles.rainbow(x, y, w, h, l);
                    case 2 -> argb & 0xFFFFFF;
                };
                out.setPixel(x, y, a << 24 | rgb & 0xFFFFFF);
            }
        }
        if (style.kind() == Kind.GLOW) {
            FwmStyles.glowEdge(src, out, style.mid());
        }
        return out;
    }

    private static float contrast(float l) {
        float c = (l - 0.5f) * 1.25f + 0.5f;
        return Math.max(0.0f, Math.min(1.0f, c));
    }

    private static int map(Style s, float l) {
        return l < 0.5f ? FwmStyles.mix(s.dark(), s.mid(), l * 2.0f) : FwmStyles.mix(s.mid(), s.light(), (l - 0.5f) * 2.0f);
    }

    private static int neon(Style s, float l, int r, int g, int b) {
        float t = ((float)b + (float)g * 0.5f) / ((float)(r + g + b) + 1.0f);
        int bright = FwmStyles.mix(s.mid(), s.light(), Math.min(1.0f, t * 1.6f));
        return FwmStyles.mix(s.dark(), bright, Math.min(1.0f, 0.25f + l * 1.1f));
    }

    private static int rainbow(int x, int y, int w, int h, float l) {
        float hue = (float)(x + y) / (float)Math.max(1, w + h) % 1.0f;
        int c = Color.HSBtoRGB(hue, 0.75f, 0.35f + 0.65f * l);
        return c & 0xFFFFFF;
    }

    private static void glowEdge(NativeImage src, NativeImage out, int glow) {
        int w = src.getWidth();
        int h = src.getHeight();
        for (int y = 0; y < h; ++y) {
            for (int x = 0; x < w; ++x) {
                boolean edge;
                if (src.getPixel(x, y) >>> 24 == 0) continue;
                boolean bl = edge = x == 0 || y == 0 || x == w - 1 || y == h - 1 || src.getPixel(x - 1, y) >>> 24 == 0 || src.getPixel(x + 1, y) >>> 24 == 0 || src.getPixel(x, y - 1) >>> 24 == 0 || src.getPixel(x, y + 1) >>> 24 == 0;
                if (!edge) continue;
                int a = out.getPixel(x, y) >>> 24;
                out.setPixel(x, y, a << 24 | glow);
            }
        }
    }

    private static int mix(int a, int b, float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        int r = Math.round((float)(a >> 16 & 0xFF) + (float)((b >> 16 & 0xFF) - (a >> 16 & 0xFF)) * t);
        int g = Math.round((float)(a >> 8 & 0xFF) + (float)((b >> 8 & 0xFF) - (a >> 8 & 0xFF)) * t);
        int bl = Math.round((float)(a & 0xFF) + (float)((b & 0xFF) - (a & 0xFF)) * t);
        return r << 16 | g << 8 | bl;
    }

    public record Style(String id, String de, String en, Kind kind, int dark, int mid, int light) {
        public String label() {
            return Lang.t(this.de, this.en);
        }

        public String key() {
            return FwmStyles.PREFIX + this.id;
        }
    }

    public static enum Kind {
        MAP,
        RAINBOW,
        GLOW,
        NEON;

    }
}

