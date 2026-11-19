/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.font.GlyphProvider$Conditional
 *  net.minecraft.client.gui.font.FontManager
 *  net.minecraft.client.gui.font.FontOption
 *  net.minecraft.client.gui.font.FontSet
 *  net.minecraft.resources.Identifier
 *  net.minecraft.util.profiling.ProfilerFiller
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Coerce
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.friendswithmoney.fwm.mixin;

import com.mojang.blaze3d.font.GlyphProvider;
import java.util.List;
import java.util.Set;
import net.friendswithmoney.fwm.lyrics.LazySkyFonts;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontOption;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.Identifier;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={FontManager.class})
public abstract class LazySkyFontsMixin {
    @Inject(method={"createFontSet"}, at={@At(value="HEAD")})
    private void fwm$which(Identifier id, List<GlyphProvider.Conditional> providers, Set<FontOption> options, CallbackInfoReturnable<FontSet> cir) {
        LazySkyFonts.begin(id);
    }

    @Inject(method={"createFontSet"}, at={@At(value="RETURN")})
    private void fwm$done(Identifier id, List<GlyphProvider.Conditional> providers, Set<FontOption> options, CallbackInfoReturnable<FontSet> cir) {
        LazySkyFonts.end();
    }

    @Inject(method={"apply"}, at={@At(value="RETURN")})
    private void fwm$report(@Coerce Object preparation, ProfilerFiller profiler, CallbackInfo ci) {
        LazySkyFonts.report();
    }
}

