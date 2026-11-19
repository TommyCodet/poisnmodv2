/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.MouseHandler
 *  net.minecraft.client.input.MouseButtonInfo
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.friendswithmoney.fwm.mixin.lyrics;

import net.friendswithmoney.fwm.lyrics.AfkCinema;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={MouseHandler.class})
public class CinemaMouseMixin {
    @Unique
    private double fwm$lastX = Double.NaN;
    @Unique
    private double fwm$lastY = Double.NaN;

    @Inject(method={"onButton"}, at={@At(value="HEAD")})
    private void fwm$cinemaButton(long window, MouseButtonInfo input, int action, CallbackInfo ci) {
        if (action != 0) {
            AfkCinema.onButton(input.button());
        }
    }

    @Inject(method={"onScroll"}, at={@At(value="HEAD")})
    private void fwm$cinemaScroll(long window, double h, double v, CallbackInfo ci) {
        AfkCinema.onLook();
    }

    @Inject(method={"onMove"}, at={@At(value="HEAD")})
    private void fwm$cinemaMove(long window, double x, double y, CallbackInfo ci) {
        if (Double.isNaN(this.fwm$lastX)) {
            this.fwm$lastX = x;
            this.fwm$lastY = y;
            return;
        }
        double dx = x - this.fwm$lastX;
        double dy = y - this.fwm$lastY;
        if (!Minecraft.getInstance().isWindowActive()) {
            this.fwm$lastX = x;
            this.fwm$lastY = y;
        } else if (dx * dx + dy * dy > 9.0) {
            this.fwm$lastX = x;
            this.fwm$lastY = y;
            AfkCinema.onLook();
        }
    }
}

