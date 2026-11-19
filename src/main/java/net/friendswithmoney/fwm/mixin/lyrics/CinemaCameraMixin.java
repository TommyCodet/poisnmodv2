/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Camera
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.Minecraft
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.friendswithmoney.fwm.mixin.lyrics;

import net.friendswithmoney.fwm.lyrics.AfkCinema;
import net.friendswithmoney.fwm.lyrics.CinemaShots;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Camera.class})
public abstract class CinemaCameraMixin {
    @Shadow
    private Entity entity;

    @Shadow
    protected abstract void setPosition(Vec3 var1);

    @Shadow
    protected abstract void setRotation(float var1, float var2);

    @Inject(method={"update"}, at={@At(value="TAIL")})
    private void fwm$cinema(DeltaTracker tracker, CallbackInfo ci) {
        if (AfkCinema.isActive() && this.entity != null && this.entity == Minecraft.getInstance().player) {
            CinemaShots.Pose pose = CinemaShots.compute(this.entity, ((Camera)this).getCameraEntityPartialTicks(tracker));
            this.setRotation(pose.yRot(), pose.xRot());
            this.setPosition(pose.pos());
        }
    }
}

