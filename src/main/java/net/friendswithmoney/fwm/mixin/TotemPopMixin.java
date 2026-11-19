/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.network.protocol.game.ClientboundEntityEventPacket
 *  net.minecraft.world.level.Level
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.friendswithmoney.fwm.mixin;

import net.friendswithmoney.fwm.game.PvpFx;
import net.friendswithmoney.fwm.game.TotemCounter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPacketListener.class})
public abstract class TotemPopMixin {
    @Inject(method={"handleEntityEvent"}, at={@At(value="TAIL")})
    private void fwm$totemPop(ClientboundEntityEventPacket packet, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return;
        }
        byte status = packet.getEventId();
        if (status != 35 && status != 3) {
            return;
        }
        TotemCounter.onStatus(packet.getEntity((Level)client.level), status);
        if (status == 35) {
            PvpFx.onPop(packet.getEntity((Level)client.level));
        }
    }
}

