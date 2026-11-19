/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.util.Mth
 */
package net.friendswithmoney.fwm.lyrics;

import net.friendswithmoney.fwm.lyrics.AfkCinema;
import net.friendswithmoney.fwm.lyrics.CinemaLyricsHud;
import net.friendswithmoney.fwm.lyrics.CinemaShots;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;

public final class CinemaOverlay {
    public static void render(GuiGraphicsExtractor var0) {
        float var15;
        Minecraft var1 = Minecraft.getInstance();
        int var2 = var0.guiWidth();
        int var3 = var0.guiHeight();
        long var4 = System.currentTimeMillis() - AfkCinema.startMs();
        int var6 = 0;
        if (AfkCinema.cfg.letterbox) {
            float var7 = Mth.clamp((float)((float)var4 / 1000.0f), (float)0.0f, (float)1.0f);
            var7 = (float)(0.5 - 0.5 * Math.cos(Math.PI * (double)var7));
            var6 = Math.round((float)var3 * 0.11f * var7);
            var0.fill(0, 0, var2, var6, -16777216);
            var0.fill(0, var3 - var6, var2, var3, -16777216);
        }
        if ((var15 = CinemaShots.fade()) > 0.001f) {
            int var8 = Math.round(var15 * 255.0f);
            var0.fill(0, 0, var2, var3, var8 << 24);
        }
        try {
            CinemaLyricsHud.render(var0, var6, var3 - var6);
        }
        catch (Throwable var13) {
            AfkCinema.LOG.debug("Kino-Lyrics Fehler", var13);
        }
        if (AfkCinema.cfg.showAfkTimer && var4 > 1000L) {
            long var16 = (System.currentTimeMillis() - AfkCinema.afkSinceMs()) / 1000L;
            String var10 = String.format("AFK %02d:%02d", var16 / 60L, var16 % 60L);
            int var11 = var1.font.width(var10);
            int var12 = var6 > 12 ? var3 - var6 / 2 - 4 : var3 - 12;
            var0.text(var1.font, var10, var2 - var11 - 10, var12, -6645094, false);
        }
    }
}

