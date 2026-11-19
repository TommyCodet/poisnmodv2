/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.NativeImage
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.resources.Identifier
 *  org.joml.Matrix3x2fStack
 */
package net.friendswithmoney.fwm.ui;

import com.mojang.blaze3d.platform.NativeImage;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import net.friendswithmoney.fwm.ui.gfx.Shapes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fStack;

public final class SharpIcons {
    public static final String COG = "icon_cog";
    public static final String REFRESH = "icon_refresh";
    private static final Map<String, Identifier> TEXTURES = new HashMap<String, Identifier>();
    private static final Map<String, BufferedImage> MASTERS = new HashMap<String, BufferedImage>();

    private SharpIcons() {
    }

    public static void draw(GuiGraphicsExtractor ctx, String name, int x, int y, int size, int color) {
        float gui = Math.max(0.25f, Shapes.pixels(ctx));
        int px = Math.max(4, Math.round((float)size * gui));
        Identifier id = SharpIcons.texture(name, px);
        if (id == null) {
            return;
        }
        Matrix3x2fStack matrices = ctx.pose();
        matrices.pushMatrix();
        matrices.translate((float)x, (float)y);
        matrices.scale(1.0f / gui, 1.0f / gui);
        ctx.blit(RenderPipelines.GUI_TEXTURED, id, 0, 0, 0.0f, 0.0f, px, px, px, px, px, px, color);
        matrices.popMatrix();
    }

    private static Identifier texture(String name, int px) {
        String key = name + "_" + px;
        Identifier ready = TEXTURES.get(key);
        if (ready != null) {
            return ready;
        }
        BufferedImage master = SharpIcons.master(name);
        if (master == null) {
            return null;
        }
        NativeImage image = new NativeImage(px, px, true);
        float[] alpha = SharpIcons.shrink(master, px);
        for (int y = 0; y < px; ++y) {
            for (int x = 0; x < px; ++x) {
                int a = Math.round(alpha[y * px + x] * 255.0f);
                image.setPixel(x, y, a << 24 | 0xFFFFFF);
            }
        }
        Identifier id = Identifier.fromNamespaceAndPath((String)"fwm", (String)("sharp/" + key));
        Minecraft.getInstance().getTextureManager().register(id, (AbstractTexture)new DynamicTexture(() -> "fwm icon " + key, image));
        TEXTURES.put(key, id);
        return id;
    }

    private static BufferedImage master(String name) {
        if (MASTERS.containsKey(name)) {
            return MASTERS.get(name);
        }
        BufferedImage image = null;
        try (InputStream in = SharpIcons.class.getResourceAsStream("/assets/fwm/textures/gui/" + name + ".png");){
            if (in != null) {
                image = ImageIO.read(in);
            }
        }
        catch (Exception e) {
            System.err.println("[FWM] Icon " + name + " could not be read: " + e.getMessage());
        }
        MASTERS.put(name, image);
        return image;
    }

    private static float[] shrink(BufferedImage src, int px) {
        int w = src.getWidth();
        int h = src.getHeight();
        double sx = (double)w / (double)px;
        double sy = (double)h / (double)px;
        float[] out = new float[px * px];
        for (int ty = 0; ty < px; ++ty) {
            double y0 = (double)ty * sy;
            double y1 = y0 + sy;
            for (int tx = 0; tx < px; ++tx) {
                double x0 = (double)tx * sx;
                double x1 = x0 + sx;
                double sum = 0.0;
                int y = (int)y0;
                while ((double)y < Math.min((double)h, Math.ceil(y1))) {
                    double wy = Math.min(y1, (double)(y + 1)) - Math.max(y0, (double)y);
                    if (!(wy <= 0.0)) {
                        int x = (int)x0;
                        while ((double)x < Math.min((double)w, Math.ceil(x1))) {
                            double wx = Math.min(x1, (double)(x + 1)) - Math.max(x0, (double)x);
                            if (!(wx <= 0.0)) {
                                sum += (double)(src.getRGB(x, y) >>> 24) / 255.0 * wx * wy;
                            }
                            ++x;
                        }
                    }
                    ++y;
                }
                out[ty * px + tx] = (float)(sum / (sx * sy));
            }
        }
        return out;
    }
}

