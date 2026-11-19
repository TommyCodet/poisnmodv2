/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.Hud
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.friendswithmoney.fwm.mixin.lyrics;

import net.friendswithmoney.fwm.lyrics.NowPlayingHud;
import net.friendswithmoney.fwm.lyrics.SkyLyrics;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Hud.class})
public class GuiMixin {
    @Unique
    private static boolean skylyrics$pillBroken;

    @Inject(method={"extractRenderState"}, at={@At(value="TAIL")})
    private void skylyrics$pill(GuiGraphicsExtractor var1, DeltaTracker var2, CallbackInfo var3) {
        if (!skylyrics$pillBroken) {
            try {
                NowPlayingHud.render(var1);
            }
            catch (Throwable var5) {
                skylyrics$pillBroken = true;
                SkyLyrics.LOG.error("Sky Lyrics: Now-Playing-Anzeige deaktiviert wegen Fehler", var5);
            }
        }
    }
}

