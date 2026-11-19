/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.SharedConstants
 *  net.minecraft.client.Minecraft
 */
package net.friendswithmoney.fwm.game;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import javax.imageio.ImageIO;
import net.friendswithmoney.fwm.ui.FwmGfx;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;

public final class Mapart {
    private static final Colour[] COLOURS = new Colour[]{Mapart.c("GRASS", 127, 178, 56, "grass_block"), Mapart.c("SAND", 247, 233, 163, "birch_planks"), Mapart.c("FIRE", 255, 0, 0, "redstone_block"), Mapart.c("ICE", 160, 160, 255, "packed_ice"), Mapart.c("METAL", 167, 167, 167, "iron_block"), Mapart.c("SNOW", 255, 255, 255, "white_concrete"), Mapart.c("CLAY", 164, 168, 184, "clay"), Mapart.c("DIRT", 151, 109, 77, "granite"), Mapart.c("STONE", 112, 112, 112, "cobblestone"), Mapart.c("WOOD", 143, 119, 72, "oak_planks"), Mapart.c("QUARTZ", 255, 252, 245, "diorite"), Mapart.c("ORANGE", 216, 127, 51, "orange_concrete"), Mapart.c("MAGENTA", 178, 76, 216, "magenta_concrete"), Mapart.c("LIGHT_BLUE", 102, 153, 216, "light_blue_concrete"), Mapart.c("YELLOW", 229, 229, 51, "yellow_concrete"), Mapart.c("LIME", 127, 204, 25, "lime_concrete"), Mapart.c("PINK", 242, 127, 165, "pink_concrete"), Mapart.c("GRAY", 76, 76, 76, "gray_concrete"), Mapart.c("LIGHT_GRAY", 153, 153, 153, "light_gray_concrete"), Mapart.c("CYAN", 76, 127, 153, "cyan_concrete"), Mapart.c("PURPLE", 127, 63, 178, "purple_concrete"), Mapart.c("BLUE", 51, 76, 178, "blue_concrete"), Mapart.c("BROWN", 102, 76, 51, "brown_concrete"), Mapart.c("GREEN", 102, 127, 51, "green_concrete"), Mapart.c("RED", 153, 51, 51, "red_concrete"), Mapart.c("BLACK", 25, 25, 25, "black_concrete"), Mapart.c("GOLD", 250, 238, 77, "gold_block"), Mapart.c("DIAMOND", 92, 219, 213, "prismarine_bricks"), Mapart.c("LAPIS", 74, 128, 255, "lapis_block"), Mapart.c("PODZOL", 129, 86, 49, "spruce_planks"), Mapart.c("NETHER", 112, 2, 0, "netherrack"), Mapart.c("T_WHITE", 209, 177, 161, "white_terracotta"), Mapart.c("T_ORANGE", 159, 82, 36, "orange_terracotta"), Mapart.c("T_MAGENTA", 149, 87, 108, "magenta_terracotta"), Mapart.c("T_LIGHT_BLUE", 112, 108, 138, "light_blue_terracotta"), Mapart.c("T_YELLOW", 186, 133, 36, "yellow_terracotta"), Mapart.c("T_LIME", 103, 117, 53, "lime_terracotta"), Mapart.c("T_PINK", 160, 77, 78, "pink_terracotta"), Mapart.c("T_GRAY", 57, 41, 35, "gray_terracotta"), Mapart.c("T_LIGHT_GRAY", 135, 107, 98, "light_gray_terracotta"), Mapart.c("T_CYAN", 87, 92, 92, "cyan_terracotta"), Mapart.c("T_PURPLE", 122, 73, 88, "purple_terracotta"), Mapart.c("T_BLUE", 76, 62, 92, "blue_terracotta"), Mapart.c("T_BROWN", 76, 50, 35, "brown_terracotta"), Mapart.c("T_GREEN", 76, 82, 42, "green_terracotta"), Mapart.c("T_RED", 142, 60, 46, "red_terracotta"), Mapart.c("T_BLACK", 37, 22, 16, "black_terracotta"), Mapart.c("CRIMSON_STEM", 148, 63, 97, "crimson_planks"), Mapart.c("WARPED_STEM", 58, 142, 140, "warped_planks"), Mapart.c("DEEPSLATE", 100, 100, 100, "cobbled_deepslate"), Mapart.c("RAW_IRON", 216, 175, 147, "raw_iron_block")};
    private static final double FLAT = 0.8627450980392157;
    private static final double[] SHADES = new double[]{0.7058823529411765, 0.8627450980392157, 1.0};

    private static Colour c(String name, int r, int g, int b, String block) {
        return new Colour(name, r << 16 | g << 8 | b, block);
    }

    private Mapart() {
    }

    public static Result make(Path picture, int mapsX, int mapsY, boolean dither, boolean gold, boolean carpet, boolean threeD, Look look) throws IOException {
        BufferedImage src;
        if (carpet) {
            threeD = false;
        }
        if (look == null) {
            look = Look.NONE;
        }
        if ((src = ImageIO.read(picture.toFile())) == null) {
            throw new IOException("not a picture");
        }
        int w = mapsX * 128;
        int h = mapsY * 128;
        BufferedImage scaled = new BufferedImage(w, h, 1);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        double want = (double)w / (double)h;
        double have = (double)src.getWidth() / (double)src.getHeight();
        int sx = 0;
        int sy = 0;
        int sw = src.getWidth();
        int sh = src.getHeight();
        if (have > want) {
            sw = (int)Math.round((double)sh * want);
            sx = (src.getWidth() - sw) / 2;
        } else if (have < want) {
            sh = (int)Math.round((double)sw / want);
            sy = (src.getHeight() - sh) / 2;
        }
        g.drawImage(src, 0, 0, w, h, sx, sy, sx + sw, sy + sh, null);
        g.dispose();
        ArrayList<Colour> pal = new ArrayList<Colour>();
        if (carpet) {
            for (Colour col : COLOURS) {
                if (!col.block().endsWith("_concrete")) continue;
                pal.add(new Colour(col.name(), col.rgb(), col.block().replace("_concrete", "_carpet")));
            }
        } else {
            for (Colour col : COLOURS) {
                if (!gold && col.block().equals("gold_block")) continue;
                pal.add(col);
            }
        }
        ArrayList<Tone> tones = new ArrayList<Tone>();
        for (Colour col : pal) {
            if (threeD) {
                for (int s = 0; s < 3; ++s) {
                    tones.add(new Tone(col, s));
                }
                continue;
            }
            tones.add(new Tone(col, 1));
        }
        double[][] rgb = new double[tones.size()][3];
        double[][] lab = new double[tones.size()][];
        for (int i = 0; i < tones.size(); ++i) {
            int v = ((Tone)tones.get(i)).colour().rgb();
            double k = SHADES[((Tone)tones.get(i)).shade()];
            rgb[i] = new double[]{Math.floor((double)(v >> 16 & 0xFF) * k), Math.floor((double)(v >> 8 & 0xFF) * k), Math.floor((double)(v & 0xFF) * k)};
            lab[i] = Mapart.lab(rgb[i]);
        }
        double[][][] buf = new double[h][w][3];
        for (int y = 0; y < h; ++y) {
            for (int x = 0; x < w; ++x) {
                int v = scaled.getRGB(x, y);
                buf[y][x] = Mapart.adjust(v >> 16 & 0xFF, v >> 8 & 0xFF, v & 0xFF, look);
            }
        }
        int[][] idx = new int[h][w];
        for (int y = 0; y < h; ++y) {
            for (int x = 0; x < w; ++x) {
                double[] px = Mapart.clamp(buf[y][x]);
                double[] l = Mapart.lab(px);
                int best = 0;
                double d = Double.MAX_VALUE;
                for (int i = 0; i < lab.length; ++i) {
                    double d2 = Mapart.sq(l[0] - lab[i][0]) + Mapart.sq(l[1] - lab[i][1]) + Mapart.sq(l[2] - lab[i][2]);
                    if (!(d2 < d)) continue;
                    d = d2;
                    best = i;
                }
                idx[y][x] = best;
                if (!dither) continue;
                for (int k = 0; k < 3; ++k) {
                    double err = (px[k] - rgb[best][k]) * 0.85;
                    if (x + 1 < w) {
                        double[] dArray = buf[y][x + 1];
                        int n = k;
                        dArray[n] = dArray[n] + err * 7.0 / 16.0;
                    }
                    if (y + 1 >= h) continue;
                    if (x > 0) {
                        double[] dArray = buf[y + 1][x - 1];
                        int n = k;
                        dArray[n] = dArray[n] + err * 3.0 / 16.0;
                    }
                    double[] dArray = buf[y + 1][x];
                    int n = k;
                    dArray[n] = dArray[n] + err * 5.0 / 16.0;
                    if (x + 1 >= w) continue;
                    double[] dArray2 = buf[y + 1][x + 1];
                    int n2 = k;
                    dArray2[n2] = dArray2[n2] + err / 16.0;
                }
            }
        }
        BufferedImage preview = new BufferedImage(w, h, 1);
        LinkedHashMap<String, Integer> counts = new LinkedHashMap<String, Integer>();
        for (int y = 0; y < h; ++y) {
            for (int x = 0; x < w; ++x) {
                double[] c = rgb[idx[y][x]];
                preview.setRGB(x, y, (int)c[0] << 16 | (int)c[1] << 8 | (int)c[2]);
                counts.merge(((Tone)tones.get(idx[y][x])).colour().block(), 1, Integer::sum);
            }
        }
        ArrayList sorted = new ArrayList(counts.entrySet());
        sorted.sort((a, b) -> (Integer)b.getValue() - (Integer)a.getValue());
        LinkedHashMap<String, Integer> materials = new LinkedHashMap<String, Integer>();
        for (Map.Entry entry : sorted) {
            materials.put((String)entry.getKey(), (Integer)entry.getValue());
        }
        boolean three = threeD;
        boolean bl = carpet;
        Saver saver = () -> Mapart.write(picture, mapsX, mapsY, carpets, three, tones, idx, w, h, preview, materials);
        return new Result(preview, materials, null, w * h, mapsX, mapsY, saver);
    }

    private static Result write(Path picture, int mapsX, int mapsY, boolean carpet, boolean threeD, List<Tone> tones, int[][] idx, int w, int h, BufferedImage preview, Map<String, Integer> materials) throws IOException {
        String base = picture.getFileName().toString().replaceAll("\\.[A-Za-z]+$", "").replaceAll("[^A-Za-z0-9 _-]", "");
        String name = "FWM Mapart " + (base.isBlank() ? "Bild" : base) + " " + mapsX + "x" + mapsY + (carpet ? " Teppich" : (threeD ? " 3D" : ""));
        Path dir = Minecraft.getInstance().gameDirectory.toPath().resolve("schematics");
        Files.createDirectories(dir, new FileAttribute[0]);
        Path schematic = dir.resolve(name + ".litematic");
        int height = Mapart.writeLitematic(tones, idx, w, h, name, schematic, threeD);
        ImageIO.write((RenderedImage)preview, "png", dir.resolve(name + ".png").toFile());
        StringBuilder list = new StringBuilder(name + "\nGesamt: " + w * h + (carpet ? " Teppiche" : " Bl\u00f6cke") + " + " + w + " Cobblestone (St\u00fctzreihe im Norden)" + (carpet ? "\nTeppiche brauchen einen Block darunter: auf flachem Boden bauen." : "") + (String)(threeD ? "\n3D (Treppe): bis zu " + height + " Bl\u00f6cke hoch. Bl\u00f6cke schweben teils, Litematica setzt sie richtig." : "") + "\n\n");
        for (Map.Entry<String, Integer> e : materials.entrySet()) {
            int n = e.getValue();
            list.append(String.format("%-26s %7d  = %4d Stacks + %2d  (~%.1f Shulker)%n", e.getKey(), n, n / 64, n % 64, (double)n / 1728.0));
        }
        Files.writeString(dir.resolve(name + " Materialliste.txt"), (CharSequence)list.toString(), StandardCharsets.UTF_8, new OpenOption[0]);
        return new Result(preview, materials, schematic, w * h, mapsX, mapsY, null);
    }

    private static double[] adjust(double r, double g, double b, Look look) {
        double c = (double)look.contrast() / 100.0;
        double br = (double)look.brightness() * 2.55;
        double s = (double)look.saturation() / 100.0;
        double[] v = new double[]{r, g, b};
        for (int i = 0; i < 3; ++i) {
            v[i] = (v[i] - 128.0) * c + 128.0 + br;
        }
        double grey = 0.299 * v[0] + 0.587 * v[1] + 0.114 * v[2];
        for (int i = 0; i < 3; ++i) {
            v[i] = grey + (v[i] - grey) * s;
        }
        return v;
    }

    private static double sq(double v) {
        return v * v;
    }

    private static double[] clamp(double[] c) {
        return new double[]{Math.max(0.0, Math.min(255.0, c[0])), Math.max(0.0, Math.min(255.0, c[1])), Math.max(0.0, Math.min(255.0, c[2]))};
    }

    private static double[] lab(double[] rgb) {
        double[] c = new double[3];
        for (int i = 0; i < 3; ++i) {
            double v = rgb[i] / 255.0;
            c[i] = v > 0.04045 ? Math.pow((v + 0.055) / 1.055, 2.4) : v / 12.92;
        }
        double x = (c[0] * 0.4124 + c[1] * 0.3576 + c[2] * 0.1805) / 0.9505;
        double y = c[0] * 0.2126 + c[1] * 0.7152 + c[2] * 0.0722;
        double z = (c[0] * 0.0193 + c[1] * 0.1192 + c[2] * 0.9505) / 1.089;
        double fx = Mapart.f(x);
        double fy = Mapart.f(y);
        double fz = Mapart.f(z);
        return new double[]{116.0 * fy - 16.0, 500.0 * (fx - fy), 200.0 * (fy - fz)};
    }

    private static double f(double t) {
        return t > 0.008856 ? Math.cbrt(t) : 7.787 * t + 0.13793103448275862;
    }

    private static int writeLitematic(List<Tone> tones, int[][] idx, int w, int h, String name, Path path, boolean threeD) throws IOException {
        ArrayList<Object> blocks = new ArrayList<Object>();
        blocks.add("minecraft:air");
        LinkedHashMap<Integer, Integer> remap = new LinkedHashMap<Integer, Integer>();
        for (int y = 0; y < h; ++y) {
            for (int x = 0; x < w; ++x) {
                int i = idx[y][x];
                if (remap.containsKey(i)) continue;
                String b = "minecraft:" + tones.get(i).colour().block();
                int at = blocks.indexOf(b);
                if (at < 0) {
                    blocks.add(b);
                    at = blocks.size() - 1;
                }
                remap.put(i, at);
            }
        }
        int cobble = blocks.indexOf("minecraft:cobblestone");
        if (cobble < 0) {
            blocks.add("minecraft:cobblestone");
            cobble = blocks.size() - 1;
        }
        int sx = w;
        int sz = h + 1;
        int[][] top = new int[sz][sx];
        int sy = 1;
        for (int x = 0; x < sx; ++x) {
            int i;
            if (!threeD) continue;
            int[] node = new int[sz];
            int[] rel = new int[sz];
            int n = 0;
            for (int z = 1; z < sz; ++z) {
                int d = tones.get(idx[z - 1][x]).shade() - 1;
                if (d == 0) {
                    node[z] = n;
                    continue;
                }
                node[z] = ++n;
                rel[n] = d;
            }
            int[] up = new int[n + 1];
            int[] down = new int[n + 1];
            for (i = 1; i <= n; ++i) {
                up[i] = rel[i] > 0 ? up[i - 1] + 1 : 0;
            }
            for (i = n - 1; i >= 0; --i) {
                down[i] = rel[i + 1] < 0 ? down[i + 1] + 1 : 0;
            }
            for (int z = 0; z < sz; ++z) {
                int i2 = node[z];
                top[z][x] = Math.max(up[i2], down[i2]);
                sy = Math.max(sy, top[z][x] + 1);
            }
        }
        long total = (long)sx * (long)sy * (long)sz;
        int bits = Math.max(2, 32 - Integer.numberOfLeadingZeros(blocks.size() - 1));
        long[] longs = new long[(int)Math.ceil((double)total * (double)bits / 64.0)];
        for (int z = 0; z < sz; ++z) {
            for (int x = 0; x < sx; ++x) {
                long v = z == 0 ? (long)cobble : (long)((Integer)remap.get(idx[z - 1][x])).intValue();
                long i = ((long)top[z][x] * (long)sz + (long)z) * (long)sx + (long)x;
                long s = i * (long)bits;
                int a = (int)(s >> 6);
                int o = (int)(s & 0x3FL);
                int n = a;
                longs[n] = longs[n] | v << o;
                if (o + bits <= 64) continue;
                int n2 = a + 1;
                longs[n2] = longs[n2] | v >>> 64 - o;
            }
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        long now = System.currentTimeMillis();
        out.writeByte(10);
        out.writeUTF("");
        Mapart.intTag(out, "MinecraftDataVersion", Mapart.dataVersion());
        Mapart.intTag(out, "Version", 6);
        Mapart.compound(out, "Metadata");
        Mapart.stringTag(out, "Name", name);
        Mapart.stringTag(out, "Author", FwmGfx.playerName());
        Mapart.stringTag(out, "Description", threeD ? "FWM Mapart, 3D-Treppe, Norden = oben" : "FWM Mapart, flach, Norden = oben");
        Mapart.intTag(out, "RegionCount", 1);
        Mapart.intTag(out, "TotalBlocks", sx * sz);
        Mapart.intTag(out, "TotalVolume", (int)Math.min(Integer.MAX_VALUE, total));
        Mapart.longTag(out, "TimeCreated", now);
        Mapart.longTag(out, "TimeModified", now);
        Mapart.vec(out, "EnclosingSize", sx, sy, sz);
        out.writeByte(0);
        Mapart.compound(out, "Regions");
        Mapart.compound(out, "mapart");
        Mapart.vec(out, "Position", 0, 0, 0);
        Mapart.vec(out, "Size", sx, sy, sz);
        out.writeByte(9);
        out.writeUTF("BlockStatePalette");
        out.writeByte(10);
        out.writeInt(blocks.size());
        for (String string : blocks) {
            Mapart.stringTag(out, "Name", string);
            out.writeByte(0);
        }
        out.writeByte(12);
        out.writeUTF("BlockStates");
        out.writeInt(longs.length);
        for (String l : (String[])longs) {
            out.writeLong((long)l);
        }
        for (String list : new String[]{"TileEntities", "Entities", "PendingBlockTicks", "PendingFluidTicks"}) {
            out.writeByte(9);
            out.writeUTF(list);
            out.writeByte(10);
            out.writeInt(0);
        }
        out.writeByte(0);
        out.writeByte(0);
        out.writeByte(0);
        out.flush();
        try (GZIPOutputStream file = new GZIPOutputStream(Files.newOutputStream(path, new OpenOption[0]));){
            ((OutputStream)file).write(bytes.toByteArray());
        }
        return sy;
    }

    private static int dataVersion() {
        try {
            return SharedConstants.getCurrentVersion().dataVersion().version();
        }
        catch (Throwable t) {
            return 4671;
        }
    }

    private static void compound(DataOutputStream out, String name) throws IOException {
        out.writeByte(10);
        out.writeUTF(name);
    }

    private static void intTag(DataOutputStream out, String name, int v) throws IOException {
        out.writeByte(3);
        out.writeUTF(name);
        out.writeInt(v);
    }

    private static void longTag(DataOutputStream out, String name, long v) throws IOException {
        out.writeByte(4);
        out.writeUTF(name);
        out.writeLong(v);
    }

    private static void stringTag(DataOutputStream out, String name, String v) throws IOException {
        out.writeByte(8);
        out.writeUTF(name);
        out.writeUTF(v);
    }

    private static void vec(DataOutputStream out, String name, int x, int y, int z) throws IOException {
        Mapart.compound(out, name);
        Mapart.intTag(out, "x", x);
        Mapart.intTag(out, "y", y);
        Mapart.intTag(out, "z", z);
        out.writeByte(0);
    }

    private record Colour(String name, int rgb, String block) {
    }

    public record Look(int saturation, int brightness, int contrast) {
        public static final Look NONE = new Look(100, 0, 100);
    }

    private record Tone(Colour colour, int shade) {
    }

    @FunctionalInterface
    public static interface Saver {
        public Result save() throws IOException;
    }

    public record Result(BufferedImage preview, Map<String, Integer> materials, Path schematic, int blocks, int mapsX, int mapsY, Saver saver) {
        public Result save() throws IOException {
            return this.saver.save();
        }
    }
}

