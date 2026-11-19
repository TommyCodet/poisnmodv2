/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GlyphSource
 *  net.minecraft.network.chat.FontDescription
 *  net.minecraft.network.chat.FontDescription$Resource
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.friendswithmoney.fwm.mixin;

import net.friendswithmoney.fwm.seventv.EmoteGlyphs;
import net.friendswithmoney.fwm.ui.gfx.FwmFont;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.network.chat.FontDescription;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets={"net.minecraft.client.gui.font.FontManager$CachedFontProvider"})
public abstract class FontsMixin {
    @Inject(method={"getGlyphSource"}, at={@At(value="HEAD")}, cancellable=true)
    private void fwm$emoteGlyphs(FontDescription source, CallbackInfoReturnable<GlyphSource> cir) {
        if (source instanceof FontDescription.Resource) {
            GlyphSource holo;
            FontDescription.Resource font = (FontDescription.Resource)source;
            GlyphSource emote = EmoteGlyphs.sourceFor(font.id());
            if (emote != null) {
                cir.setReturnValue((Object)emote);
            }
            if ((holo = FwmFont.worldGlyphs(font.id())) != null) {
                cir.setReturnValue((Object)holo);
            }
        }
    }
}

