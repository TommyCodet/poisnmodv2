/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.font.GlyphProvider
 *  com.mojang.blaze3d.font.GlyphProvider$Conditional
 *  net.minecraft.client.gui.font.FontOption
 *  net.minecraft.client.gui.font.FontSet
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.friendswithmoney.fwm.mixin;

import com.mojang.blaze3d.font.GlyphProvider;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.friendswithmoney.fwm.lyrics.LazySkyFonts;
import net.minecraft.client.gui.font.FontOption;
import net.minecraft.client.gui.font.FontSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={FontSet.class})
public class FontSetLazyMixin {
    @Shadow
    private List<GlyphProvider.Conditional> allProviders;
    @Unique
    private Set<FontOption> fwm$pendingIndex;
    @Unique
    private boolean fwm$buildingIndex;

    @Shadow
    private List<GlyphProvider> selectProviders(List<GlyphProvider.Conditional> providers, Set<FontOption> options) {
        throw new AssertionError();
    }

    @Inject(method={"selectProviders"}, at={@At(value="HEAD")}, cancellable=true)
    private void fwm$skipWidthIndex(List<GlyphProvider.Conditional> providers, Set<FontOption> options, CallbackInfoReturnable<List<GlyphProvider>> cir) {
        if (this.fwm$buildingIndex) {
            return;
        }
        if (LazySkyFonts.EAGER) {
            LazySkyFonts.startTimer(System.nanoTime());
            return;
        }
        ArrayList<GlyphProvider> out = new ArrayList<GlyphProvider>();
        for (GlyphProvider.Conditional c : providers) {
            if (!c.filter().apply(options)) continue;
            out.add(c.provider());
        }
        this.fwm$pendingIndex = new HashSet<FontOption>(options);
        LazySkyFonts.skipped();
        cir.setReturnValue(out);
    }

    @Inject(method={"selectProviders"}, at={@At(value="RETURN")})
    private void fwm$time(List<GlyphProvider.Conditional> providers, Set<FontOption> options, CallbackInfoReturnable<List<GlyphProvider>> cir) {
        if (!this.fwm$buildingIndex) {
            LazySkyFonts.stopTimer();
        }
    }

    @Inject(method={"getRandomGlyph"}, at={@At(value="HEAD")})
    private void fwm$buildIndex(CallbackInfoReturnable<?> cir) {
        Set<FontOption> options = this.fwm$pendingIndex;
        if (options == null) {
            return;
        }
        this.fwm$pendingIndex = null;
        this.fwm$buildingIndex = true;
        try {
            this.selectProviders(this.allProviders, options);
        }
        finally {
            this.fwm$buildingIndex = false;
        }
    }
}

