/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderTarget
 *  net.minecraft.client.renderer.SkyRenderer
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.friendswithmoney.fwm.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.friendswithmoney.fwm.textures.FwmSky;
import net.minecraft.client.renderer.SkyRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={SkyRenderer.class})
public abstract class SkyRenderingMixin {
    @Shadow
    @Final
    private RenderTarget renderTarget;

    @Inject(method={"renderSkyDisc"}, at={@At(value="HEAD")}, cancellable=true)
    private void fwm$topSky(int color, CallbackInfo ci) {
        if (!FwmSky.active()) {
            return;
        }
        FwmSky.render(this.renderTarget);
        ci.cancel();
    }

    @Inject(method={"renderSunriseAndSunset"}, at={@At(value="HEAD")}, cancellable=true)
    private void fwm$glow(CallbackInfo ci) {
        if (FwmSky.active() && !FwmSky.sunMoon()) {
            ci.cancel();
        }
    }

    @Inject(method={"renderSunMoonAndStars"}, at={@At(value="HEAD")}, cancellable=true)
    private void fwm$bodies(CallbackInfo ci) {
        if (FwmSky.active() && !FwmSky.sunMoon()) {
            ci.cancel();
        }
    }

    @Inject(method={"renderDarkDisc"}, at={@At(value="HEAD")}, cancellable=true)
    private void fwm$dark(CallbackInfo ci) {
        if (FwmSky.active()) {
            ci.cancel();
        }
    }

    @Inject(method={"renderEndSky"}, at={@At(value="HEAD")}, cancellable=true)
    private void fwm$endSky(CallbackInfo ci) {
        if (!FwmSky.inEnd()) {
            return;
        }
        FwmSky.render(this.renderTarget);
        ci.cancel();
    }
}

