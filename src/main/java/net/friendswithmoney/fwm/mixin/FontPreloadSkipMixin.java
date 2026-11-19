/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.font.GlyphProvider$Conditional
 *  net.minecraft.client.gui.font.FontManager
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.friendswithmoney.fwm.mixin;

import com.mojang.blaze3d.font.GlyphProvider;
import java.util.List;
import net.friendswithmoney.fwm.lyrics.LazySkyFonts;
import net.minecraft.client.gui.font.FontManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={FontManager.class})
public abstract class FontPreloadSkipMixin {
    @Inject(method={"finalizeProviderLoading"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void fwm$skipPreload(List<GlyphProvider.Conditional> list, GlyphProvider.Conditional fallback, CallbackInfo ci) {
        if (LazySkyFonts.EAGER) {
            return;
        }
        list.add(0, fallback);
        ci.cancel();
    }
}

