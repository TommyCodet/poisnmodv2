/*
 * Decompiled with CFR 0.152.
 */
package net.friendswithmoney.fwm.lyrics;

import java.util.List;
import net.friendswithmoney.fwm.lyrics.FontPack;
import net.friendswithmoney.fwm.lyrics.LyricsConfig;

public final class Look {
    public static final List<Preset> ALL = List.of(new Preset("fwm_mint", "FWM Mint", "outfit", true, false, "#7DF0C6", "#7DF0C6", 40, "shadow", false), new Preset("fwm_gold", "FWM Gold", "bricolage", true, false, "#F5C542", "#F5C542", 35, "shadow", false), new Preset("clean", "Klar", "outfit", true, false, "#FFFFFF", "#FFFFFF", 0, "shadow", false), new Preset("neon", "Neon", "outfit", true, false, "#FF4FD8", "#FF4FD8", 85, "none", false), new Preset("sunset", "Sonnenuntergang", "outfit", true, false, "#FFB36B", "#FF4F8B", 30, "shadow", false), new Preset("ice", "Eis", "italiana", false, false, "#BFEFFF", "#6FD3FF", 55, "none", false), new Preset("fight", "Kampf", "bigshoulders", true, false, "#FFFFFF", "#FF3B3B", 0, "outline", false), new Preset("pixel", "Pixel", "pixelify", true, false, "#FFFFFF", "#FFFFFF", 0, "shadow", false), new Preset("dreamy", "Vertr\u00e4umt", "handwriting", false, false, "#FBCFE8", "#93C5FD", 50, "none", true), new Preset("royal", "Royal", "youngserif", false, false, "#FEF3C7", "#D4A017", 35, "none", true));

    private Look() {
    }

    public static void apply(LyricsConfig var0, Preset var1) {
        var0.look = var1.id();
        var0.fontStyle = FontPack.CHOICES.stream().anyMatch(var1x -> var1x.id().equals(var1.font())) ? var1.font() : "default";
        var0.bold = var1.bold();
        var0.italic = var1.italic();
        var0.colorMode = "custom";
        var0.customColor = var1.c1();
        var0.color2 = var1.c2();
        var0.gradient = !var1.c1().equalsIgnoreCase(var1.c2());
        var0.glow = var1.glow();
        var0.textEffect = var1.effect();
        var0.arc = var1.arc();
        var0.save();
    }

    public static boolean active(LyricsConfig var0, Preset var1) {
        return var1.id().equals(var0.look) && var1.font().equals(var0.fontStyle) && "custom".equals(var0.colorMode) && var1.c1().equalsIgnoreCase(var0.customColor);
    }

    public record Preset(String id, String name, String font, boolean bold, boolean italic, String c1, String c2, int glow, String effect, boolean arc) {
    }
}

