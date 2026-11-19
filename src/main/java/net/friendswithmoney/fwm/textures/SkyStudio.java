/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  org.lwjgl.PointerBuffer
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.util.tinyfd.TinyFileDialogs
 */
package net.friendswithmoney.fwm.textures;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import javax.imageio.ImageIO;
import net.friendswithmoney.fwm.antileak.AntiLeakPack;
import net.minecraft.client.Minecraft;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public final class SkyStudio {
    public static final String PREFIX = "FWM Himmel ";
    private static final int FACE = 512;

    private SkyStudio() {
    }

    public static String make(Path picture) throws IOException {
        BufferedImage src = ImageIO.read(picture.toFile());
        if (src == null) {
            return null;
        }
        String base = picture.getFileName().toString().replaceAll("\\.[A-Za-z]+$", "").replaceAll("[^A-Za-z0-9 _-]", "");
        String name = PREFIX + (base.isBlank() ? "Bild" : base);
        Path dir = Minecraft.getInstance().getResourcePackDirectory().resolve(name).normalize();
        Files.createDirectories(dir, new FileAttribute[0]);
        Files.writeString(dir.resolve("pack.mcmeta"), (CharSequence)AntiLeakPack.mcmeta(), StandardCharsets.UTF_8, new OpenOption[0]);
        Path endSky = dir.resolve("assets/minecraft/textures/environment/end_sky.png");
        Files.createDirectories(endSky.getParent(), new FileAttribute[0]);
        ImageIO.write((RenderedImage)SkyStudio.square(src, 512), "png", endSky.toFile());
        BufferedImage[] faces = SkyStudio.faces(src);
        BufferedImage sheet = new BufferedImage(1536, 1024, 2);
        int[][] at = new int[][]{{0, 0}, {1, 0}, {2, 0}, {0, 1}, {1, 1}, {2, 1}};
        int[] order = new int[]{5, 4, 2, 3, 0, 1};
        Graphics2D g = sheet.createGraphics();
        for (int i = 0; i < 6; ++i) {
            g.drawImage((Image)faces[order[i]], at[i][0] * 512, at[i][1] * 512, null);
        }
        g.dispose();
        for (String world : new String[]{"world0", "world1", "world-1"}) {
            Path of = dir.resolve("assets/minecraft/optifine/sky/" + world);
            Files.createDirectories(of, new FileAttribute[0]);
            ImageIO.write((RenderedImage)sheet, "png", of.resolve("sky1.png").toFile());
            Files.writeString(of.resolve("sky1.properties"), (CharSequence)"source=./sky1.png\nblend=replace\nrotate=false\n", StandardCharsets.UTF_8, new OpenOption[0]);
        }
        String[] names = new String[]{"north", "east", "south", "west", "top", "bottom"};
        Path fs = dir.resolve("assets/fwm/sky");
        Files.createDirectories(fs, new FileAttribute[0]);
        for (int i = 0; i < 6; ++i) {
            ImageIO.write((RenderedImage)faces[i], "png", fs.resolve(names[i] + ".png").toFile());
        }
        Path json = dir.resolve("assets/fabricskyboxes/sky/fwm_sky.json");
        Files.createDirectories(json.getParent(), new FileAttribute[0]);
        Files.writeString(json, (CharSequence)"{\n  \"schemaVersion\": 2,\n  \"type\": \"square-textured\",\n  \"properties\": { \"priority\": 10, \"fade\": { \"alwaysOn\": true } },\n  \"conditions\": { \"worlds\": [\"minecraft:overworld\", \"minecraft:the_end\", \"minecraft:the_nether\"] },\n  \"textures\": {\n    \"north\": \"fwm:sky/north.png\", \"south\": \"fwm:sky/south.png\",\n    \"east\": \"fwm:sky/east.png\", \"west\": \"fwm:sky/west.png\",\n    \"top\": \"fwm:sky/top.png\", \"bottom\": \"fwm:sky/bottom.png\"\n  }\n}\n", StandardCharsets.UTF_8, new OpenOption[0]);
        return name;
    }

    private static BufferedImage[] faces(BufferedImage src) {
        BufferedImage[] out = new BufferedImage[6];
        boolean panorama = (double)src.getWidth() >= (double)src.getHeight() * 1.7;
        for (int f = 0; f < 6; ++f) {
            BufferedImage face = new BufferedImage(512, 512, 2);
            for (int y = 0; y < 512; ++y) {
                for (int x = 0; x < 512; ++x) {
                    double[] dArray;
                    double a = ((double)x + 0.5) / 512.0 * 2.0 - 1.0;
                    double b = ((double)y + 0.5) / 512.0 * 2.0 - 1.0;
                    switch (f) {
                        case 0: {
                            double[] dArray2 = new double[3];
                            dArray2[0] = a;
                            dArray2[1] = -b;
                            dArray = dArray2;
                            dArray2[2] = -1.0;
                            break;
                        }
                        case 1: {
                            double[] dArray3 = new double[3];
                            dArray3[0] = 1.0;
                            dArray3[1] = -b;
                            dArray = dArray3;
                            dArray3[2] = a;
                            break;
                        }
                        case 2: {
                            double[] dArray4 = new double[3];
                            dArray4[0] = -a;
                            dArray4[1] = -b;
                            dArray = dArray4;
                            dArray4[2] = 1.0;
                            break;
                        }
                        case 3: {
                            double[] dArray5 = new double[3];
                            dArray5[0] = -1.0;
                            dArray5[1] = -b;
                            dArray = dArray5;
                            dArray5[2] = -a;
                            break;
                        }
                        case 4: {
                            double[] dArray6 = new double[3];
                            dArray6[0] = a;
                            dArray6[1] = 1.0;
                            dArray = dArray6;
                            dArray6[2] = b;
                            break;
                        }
                        default: {
                            double[] dArray7 = new double[3];
                            dArray7[0] = a;
                            dArray7[1] = -1.0;
                            dArray = dArray7;
                            dArray7[2] = -b;
                        }
                    }
                    double[] d = dArray;
                    face.setRGB(x, y, panorama ? SkyStudio.sampleEquirect(src, d) : SkyStudio.sampleFlat(src, f, a, b));
                }
            }
            out[f] = face;
        }
        return out;
    }

    private static int sampleEquirect(BufferedImage src, double[] d) {
        double len = Math.sqrt(d[0] * d[0] + d[1] * d[1] + d[2] * d[2]);
        double lon = Math.atan2(d[0], -d[2]);
        double lat = Math.asin(d[1] / len);
        int u = (int)((lon / (Math.PI * 2) + 0.5) * (double)(src.getWidth() - 1));
        int v = (int)((0.5 - lat / Math.PI) * (double)(src.getHeight() - 1));
        return 0xFF000000 | src.getRGB(Math.max(0, Math.min(src.getWidth() - 1, u)), Math.max(0, Math.min(src.getHeight() - 1, v)));
    }

    private static int sampleFlat(BufferedImage src, int face, double a, double b) {
        int w = src.getWidth();
        int h = src.getHeight();
        if (face == 4) {
            return 0xFF000000 | src.getRGB(w / 2, 0);
        }
        if (face == 5) {
            return -16052464;
        }
        int u = (int)((a + 1.0) / 2.0 * (double)(w - 1));
        int v = (int)((b + 1.0) / 2.0 * (double)(h - 1));
        return 0xFF000000 | src.getRGB(Math.max(0, Math.min(w - 1, u)), Math.max(0, Math.min(h - 1, v)));
    }

    private static BufferedImage square(BufferedImage src, int size) {
        BufferedImage out = new BufferedImage(size, size, 2);
        int side = Math.min(src.getWidth(), src.getHeight());
        int sx = (src.getWidth() - side) / 2;
        int sy = (src.getHeight() - side) / 2;
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, size, size, sx, sy, sx + side, sy + side, null);
        g.dispose();
        return out;
    }

    public static Path pick() {
        Path path;
        block8: {
            MemoryStack stack = MemoryStack.stackPush();
            try {
                PointerBuffer filters = stack.mallocPointer(3);
                filters.put(stack.UTF8((CharSequence)"*.png")).put(stack.UTF8((CharSequence)"*.jpg")).put(stack.UTF8((CharSequence)"*.jpeg")).flip();
                String chosen = TinyFileDialogs.tinyfd_openFileDialog((CharSequence)"Himmel-Bild w\u00e4hlen", null, (PointerBuffer)filters, (CharSequence)"Bilder (PNG, JPG)", (boolean)false);
                Path path2 = path = chosen == null ? null : Path.of(chosen, new String[0]);
                if (stack == null) break block8;
            }
            catch (Throwable throwable) {
                try {
                    if (stack != null) {
                        try {
                            stack.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (Throwable t) {
                    return null;
                }
            }
            stack.close();
        }
        return path;
    }
}

