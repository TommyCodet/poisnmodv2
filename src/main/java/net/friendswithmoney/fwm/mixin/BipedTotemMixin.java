/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.HumanoidModel
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.renderer.entity.state.HumanoidRenderState
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.HumanoidArm
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.friendswithmoney.fwm.mixin;

import net.friendswithmoney.fwm.game.PvpFx;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={HumanoidModel.class})
public abstract class BipedTotemMixin {
    @Inject(method={"setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V"}, at={@At(value="TAIL")})
    private void fwm$holdUp(HumanoidRenderState state, CallbackInfo ci) {
        boolean right;
        float f;
        if (!(state instanceof PvpFx.TotemArms)) {
            return;
        }
        PvpFx.TotemArms holder = (PvpFx.TotemArms)state;
        int arms = holder.fwm$totemArms();
        if (arms == 0) {
            return;
        }
        HumanoidModel model = (HumanoidModel)this;
        float pitch = -1.5708f + model.head.xRot;
        float yaw = model.head.yRot + model.body.yRot;
        if ((arms & 1) != 0) {
            model.rightArm.xRot = pitch;
            model.rightArm.yRot = -0.1f + yaw;
            model.rightArm.zRot = 0.0f;
        }
        if ((arms & 2) != 0) {
            model.leftArm.xRot = pitch;
            model.leftArm.yRot = 0.1f + yaw;
            model.leftArm.zRot = 0.0f;
        }
        if ((f = state.attackTime) <= 0.0f) {
            return;
        }
        boolean bl = right = state.attackArm == HumanoidArm.RIGHT;
        if (right ? (arms & 1) == 0 : (arms & 2) == 0) {
            return;
        }
        ModelPart arm = right ? model.rightArm : model.leftArm;
        float g = 1.0f - f;
        g = 1.0f - g * g * g * g;
        float lift = Mth.sin((double)(g * (float)Math.PI));
        float dip = Mth.sin((double)(f * (float)Math.PI)) * -(model.head.xRot - 0.7f) * 0.75f;
        arm.xRot -= lift * 1.2f + dip;
        arm.yRot += model.body.yRot * 2.0f;
        arm.zRot += Mth.sin((double)(f * (float)Math.PI)) * -0.4f;
    }
}

