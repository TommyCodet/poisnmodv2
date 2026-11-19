/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.entity.state.AvatarRenderState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 */
package net.friendswithmoney.fwm.mixin;

import net.friendswithmoney.fwm.game.PvpFx;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={AvatarRenderState.class})
public abstract class PlayerRenderStateMixin
implements PvpFx.TotemArms {
    @Unique
    private int fwm$totemArms;

    @Override
    public int fwm$totemArms() {
        return this.fwm$totemArms;
    }

    @Override
    public void fwm$setTotemArms(int arms) {
        this.fwm$totemArms = arms;
    }
}

