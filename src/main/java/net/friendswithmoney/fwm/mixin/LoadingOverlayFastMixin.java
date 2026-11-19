/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.LoadingOverlay
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.Constant
 *  org.spongepowered.asm.mixin.injection.ModifyConstant
 */
package net.friendswithmoney.fwm.mixin;

import net.minecraft.client.gui.screens.LoadingOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value={LoadingOverlay.class})
public abstract class LoadingOverlayFastMixin {
    @Unique
    private static final boolean FWM_VANILLA = Boolean.getBoolean("fwm.vanillaSplash");

    @ModifyConstant(method={"extractRenderState", "render"}, constant={@Constant(floatValue=1000.0f)}, require=0)
    private float fwm$completeFade(float ms) {
        return FWM_VANILLA ? ms : 250.0f;
    }

    @ModifyConstant(method={"extractRenderState", "render"}, constant={@Constant(floatValue=500.0f)}, require=0)
    private float fwm$startFade(float ms) {
        return FWM_VANILLA ? ms : 200.0f;
    }

    @ModifyConstant(method={"isReadyToFadeOut"}, constant={@Constant(longValue=1000L)}, require=0)
    private long fwm$grace(long ms) {
        return FWM_VANILLA ? ms : 300L;
    }
}

