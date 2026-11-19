/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.Options
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.ChatComponent
 *  net.minecraft.client.multiplayer.chat.GuiMessageTag
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.friendswithmoney.fwm.mixin;

import net.friendswithmoney.fwm.chat.DevHighlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets={"net.minecraft.client.gui.components.ChatComponent$DrawingBackgroundGraphicsAccess"})
public abstract class ChatBackendMixin {
    @Shadow
    @Final
    private GuiGraphicsExtractor graphics;

    @Inject(method={"handleTag"}, at={@At(value="HEAD")})
    private void fwm$devTint(int x1, int y1, int x2, int y2, float opacity, GuiMessageTag indicator, CallbackInfo ci) {
        if (indicator != DevHighlight.INDICATOR) {
            return;
        }
        Options options = Minecraft.getInstance().options;
        double scale = Math.max(0.01, (Double)options.chatScale().get());
        int width = (int)Math.ceil((double)ChatComponent.getWidth((double)((Double)options.chatWidth().get())) / scale);
        this.graphics.fill(x2, y1, x2 + width + 6, y2, DevHighlight.tint(opacity));
    }
}

