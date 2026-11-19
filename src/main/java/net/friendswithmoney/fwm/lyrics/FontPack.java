/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.FontDescription
 *  net.minecraft.network.chat.FontDescription$Resource
 *  net.minecraft.network.chat.Style
 *  net.minecraft.resources.Identifier
 */
package net.friendswithmoney.fwm.lyrics;

import java.util.List;
import net.friendswithmoney.fwm.lyrics.LyricsConfig;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public final class FontPack {
    public static final List<Choice> CHOICES = List.of(new Choice("default", "Minecraft"), new Choice("uniform", "Glatt"), new Choice("outfit", "Outfit"), new Choice("montserrat", "Montserrat"), new Choice("poppins", "Poppins"), new Choice("bricolage", "Bricolage"), new Choice("righteous", "Righteous"), new Choice("russo", "Russo One"), new Choice("bebas", "Bebas Neue"), new Choice("bigshoulders", "Big Shoulders"), new Choice("tektur", "Tektur"), new Choice("boldonse", "Boldonse"), new Choice("bangers", "Bangers"), new Choice("erica", "Erica One"), new Choice("smooch", "Smooch Sans"), new Choice("poiret", "Poiret One"), new Choice("italiana", "Italiana"), new Choice("playfair", "Playfair Display"), new Choice("youngserif", "Young Serif"), new Choice("gloock", "Gloock"), new Choice("instrument", "Instrument Serif"), new Choice("lora", "Lora Kursiv"), new Choice("lobster", "Lobster"), new Choice("pacifico", "Pacifico"), new Choice("caveat", "Caveat"), new Choice("handwriting", "Handschrift"), new Choice("marker", "Permanent Marker"), new Choice("pixelify", "Pixelify"), new Choice("silkscreen", "Silkscreen"), new Choice("pressstart", "Press Start 2P"));

    private FontPack() {
    }

    public static Choice find(String id) {
        for (Choice c : CHOICES) {
            if (!c.id.equals(id)) continue;
            return c;
        }
        return CHOICES.get(0);
    }

    public static Choice next(String id) {
        int i = CHOICES.indexOf(FontPack.find(id));
        return CHOICES.get((i + 1) % CHOICES.size());
    }

    private static Identifier font(Choice c) {
        return switch (c.id) {
            case "default" -> null;
            case "uniform" -> Identifier.withDefaultNamespace((String)"uniform");
            default -> Identifier.fromNamespaceAndPath((String)"fwm", (String)("sky_" + c.id));
        };
    }

    public static Style style(LyricsConfig cfg) {
        Style s;
        Choice choice = FontPack.find(cfg.fontStyle);
        Identifier id = FontPack.font(choice);
        Style style = s = id == null ? Style.EMPTY : Style.EMPTY.withFont((FontDescription)new FontDescription.Resource(id));
        if (cfg.bold && (id == null || "uniform".equals(choice.id))) {
            s = s.withBold(Boolean.valueOf(true));
        }
        if (cfg.italic) {
            s = s.withItalic(Boolean.valueOf(true));
        }
        return s;
    }

    public static Style fontOnly(Choice c) {
        Identifier id = FontPack.font(c);
        return id == null ? Style.EMPTY : Style.EMPTY.withFont((FontDescription)new FontDescription.Resource(id));
    }

    public static List<Choice> usable() {
        return CHOICES;
    }

    public record Choice(String id, String name) {
    }
}

