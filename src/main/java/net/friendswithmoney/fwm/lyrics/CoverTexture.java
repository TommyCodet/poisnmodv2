/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.NativeImage
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.resources.Identifier
 */
package net.friendswithmoney.fwm.lyrics;

import com.mojang.blaze3d.platform.NativeImage;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import net.friendswithmoney.fwm.lyrics.CoverFetcher;
import net.friendswithmoney.fwm.lyrics.MediaWatcher;
import net.friendswithmoney.fwm.lyrics.SkyLyrics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

public final class CoverTexture {
    public static final Identifier ID = Identifier.fromNamespaceAndPath((String)"skylyrics", (String)"cover");
    private static final int SIZE = 64;
    private static int loadedId = -2;
    private static String loadedTrack = "";
    private static boolean ready;
    private static volatile int dominant;

    private CoverTexture() {
    }

    public static int dominantColor() {
        return ready ? dominant : -1;
    }

    public static boolean update(String var0) {
        String var3;
        if (!var0.equals(loadedTrack)) {
            loadedTrack = var0;
            ready = false;
            dominant = -1;
            loadedId = -2;
        }
        int var1 = MediaWatcher.coverId();
        String var2 = MediaWatcher.coverPath();
        if (var2.isEmpty() && (var3 = CoverFetcher.path(MediaWatcher.track())) != null) {
            var2 = var3;
            var1 = -1000 - (var3.hashCode() & 0xFFFF);
        }
        if (var1 != loadedId && !var2.isEmpty()) {
            loadedId = var1;
            try {
                BufferedImage var13;
                try (InputStream var4 = Files.newInputStream(Path.of(var2, new String[0]), new OpenOption[0]);){
                    var13 = ImageIO.read(var4);
                }
                if (var13 == null) {
                    return ready;
                }
                int var14 = Math.min(var13.getWidth(), var13.getHeight());
                BufferedImage var5 = var13.getSubimage((var13.getWidth() - var14) / 2, (var13.getHeight() - var14) / 2, var14, var14);
                BufferedImage var6 = new BufferedImage(64, 64, 2);
                Graphics2D var7 = var6.createGraphics();
                var7.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                var7.drawImage(var5, 0, 0, 64, 64, null);
                var7.dispose();
                dominant = CoverTexture.computeDominant(var6);
                ByteArrayOutputStream var8 = new ByteArrayOutputStream();
                ImageIO.write((RenderedImage)var6, "png", var8);
                NativeImage var9 = NativeImage.read((byte[])var8.toByteArray());
                Minecraft.getInstance().getTextureManager().register(ID, (AbstractTexture)new DynamicTexture(() -> "skylyrics_cover", var9));
                ready = true;
            }
            catch (Exception var12) {
                SkyLyrics.LOG.debug("Cover konnte nicht geladen werden", (Throwable)var12);
            }
            return ready;
        }
        return ready;
    }

    private static int computeDominant(BufferedImage var0) {
        double var1 = 0.0;
        double var3 = 0.0;
        double var5 = 0.0;
        double var7 = 0.0;
        float[] var9 = new float[3];
        for (int var10 = 0; var10 < var0.getHeight(); var10 = (int)((byte)(var10 + 2))) {
            for (int var11 = 0; var11 < var0.getWidth(); var11 = (int)((byte)(var11 + 2))) {
                int var12 = var0.getRGB(var11, var10);
                int var13 = var12 >> 16 & 0xFF;
                int var14 = var12 >> 8 & 0xFF;
                int var15 = var12 & 0xFF;
                Color.RGBtoHSB(var13, var14, var15, var9);
                double var16 = (double)(var9[1] * var9[1]) * Math.max(0.0, (double)var9[2] - 0.15);
                var1 += (double)var13 * var16;
                var3 += (double)var14 * var16;
                var5 += (double)var15 * var16;
                var7 += var16;
            }
        }
        if (var7 < 0.5) {
            return -1;
        }
        int var18 = (int)(var1 / var7);
        int var19 = (int)(var3 / var7);
        int var20 = (int)(var5 / var7);
        Color.RGBtoHSB(var18, var19, var20, var9);
        return Color.HSBtoRGB(var9[0], Math.min(1.0f, var9[1] * 1.2f + 0.1f), Math.max(0.75f, var9[2])) & 0xFFFFFF;
    }

    static {
        dominant = -1;
    }
}

