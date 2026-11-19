/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.entity.player.AvatarRenderer
 *  net.minecraft.client.renderer.entity.state.AvatarRenderState
 *  net.minecraft.world.entity.Avatar
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.item.Items
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.friendswithmoney.fwm.mixin;

import net.friendswithmoney.fwm.game.PvpFx;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={AvatarRenderer.class})
public abstract class PlayerRendererTotemMixin {
    @Inject(method={"extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V"}, at={@At(value="TAIL")})
    private void fwm$totemArms(Avatar player, AvatarRenderState state, float tickDelta, CallbackInfo ci) {
        int arms = 0;
        if (PvpFx.holdUp()) {
            boolean rightMain;
            boolean bl = rightMain = player.getMainArm() == HumanoidArm.RIGHT;
            if (player.getMainHandItem().is((Object)Items.TOTEM_OF_UNDYING)) {
                arms |= rightMain ? 1 : 2;
            }
            if (player.getOffhandItem().is((Object)Items.TOTEM_OF_UNDYING)) {
                arms |= rightMain ? 2 : 1;
            }
        }
        ((PvpFx.TotemArms)state).fwm$setTotemArms(arms);
    }
}

