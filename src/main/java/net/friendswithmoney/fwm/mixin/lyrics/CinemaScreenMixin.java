/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.friendswithmoney.fwm.mixin.lyrics;

import net.friendswithmoney.fwm.lyrics.AfkCinema;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Screen.class})
public class CinemaScreenMixin {
    @Inject(method={"extractRenderStateWithTooltipAndSubtitles"}, at={@At(value="HEAD")}, cancellable=true)
    private void fwm$cinemaHide(GuiGraphicsExtractor ctx, int mx, int my, float delta, CallbackInfo ci) {
        if (AfkCinema.isActive() && AfkCinema.isAfkScreen((Screen)this)) {
            ci.cancel();
        }
    }
}

