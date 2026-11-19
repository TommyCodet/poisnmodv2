/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.client.sounds.SoundEngine
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.friendswithmoney.fwm.mixin;

import net.friendswithmoney.fwm.game.SoundMuffler;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={SoundEngine.class})
public abstract class SoundVolumeMixin {
    @Unique
    private static SoundInstance fwm$playing;

    @Inject(method={"play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;"}, at={@At(value="HEAD")})
    private void fwm$remember(SoundInstance sound, CallbackInfoReturnable<?> ci) {
        fwm$playing = sound;
    }

    @ModifyArg(method={"play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/sounds/SoundEngine;calculateVolume(FLnet/minecraft/sounds/SoundSource;)F"), index=0)
    private float fwm$muffleStart(float volume) {
        SoundInstance sound = fwm$playing;
        fwm$playing = null;
        return volume * SoundMuffler.factor(sound);
    }

    @Inject(method={"calculateVolume(Lnet/minecraft/client/resources/sounds/SoundInstance;)F"}, at={@At(value="RETURN")}, cancellable=true)
    private void fwm$muffle(SoundInstance sound, CallbackInfoReturnable<Float> cir) {
        float factor = SoundMuffler.factor(sound);
        if (factor != 1.0f) {
            cir.setReturnValue((Object)Float.valueOf(((Float)cir.getReturnValue()).floatValue() * factor));
        }
    }
}

