/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.ClientClockManager
 *  net.minecraft.core.Holder
 *  net.minecraft.world.clock.WorldClock
 *  net.minecraft.world.clock.WorldClocks
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.friendswithmoney.fwm.mixin.lyrics;

import net.friendswithmoney.fwm.lyrics.GenreTime;
import net.minecraft.client.ClientClockManager;
import net.minecraft.core.Holder;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ClientClockManager.class})
public abstract class WorldTimeMixin {
    @Inject(method={"getTotalTicks"}, at={@At(value="RETURN")}, cancellable=true)
    private void skylyrics$genreTime(Holder<WorldClock> clock, CallbackInfoReturnable<Long> cir) {
        if (clock == null || !clock.is(WorldClocks.OVERWORLD)) {
            return;
        }
        long real = cir.getReturnValueJ();
        long shown = GenreTime.override(real);
        if (shown != real) {
            cir.setReturnValue((Object)shown);
        }
    }
}

