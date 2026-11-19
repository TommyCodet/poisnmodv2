/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.particle.Particle
 *  net.minecraft.client.particle.SingleQuadParticle
 *  net.minecraft.client.particle.SpriteSet
 *  net.minecraft.client.particle.TotemParticle
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.friendswithmoney.fwm.mixin;

import java.util.Random;
import net.friendswithmoney.fwm.game.PvpFx;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TotemParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={TotemParticle.class})
public abstract class TotemParticleMixin {
    private static final Random FWM_RANDOM = new Random();

    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void fwm$popColor(ClientLevel world, double x, double y, double z, double vx, double vy, double vz, SpriteSet sprites, CallbackInfo ci) {
        if (!PvpFx.keepParticle(FWM_RANDOM)) {
            ((Particle)this).remove();
            return;
        }
        if (!PvpFx.totemPop()) {
            return;
        }
        float[] c = PvpFx.totemParticleColor(FWM_RANDOM);
        ((SingleQuadParticle)this).setColor(c[0], c[1], c[2]);
    }
}

