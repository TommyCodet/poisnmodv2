/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.mojang.blaze3d.PrimitiveTopology
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  com.mojang.blaze3d.pipeline.RenderTarget
 *  com.mojang.blaze3d.platform.NativeImage
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.systems.RenderSystem$AutoStorageIndexBuffer
 *  com.mojang.blaze3d.textures.FilterMode
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.ByteBufferBuilder
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.MeshData
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.level.Level
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 *  org.joml.Vector4f
 */
package net.friendswithmoney.fwm.textures;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.textures.TextureMixer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class FwmSky {
    private static final int FACES = 6;
    private static final int MAX_FACE = 512;
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath((String)"fwm", (String)"sky/custom");
    private static DynamicTexture texture;
    private static String loaded;
    private static volatile String loading;
    private static GpuBuffer cube;
    private static final Map<String, BufferedImage> THUMBS;
    private static final Set<String> NONE;
    private static final Set<String> PENDING;
    private static final int GUTTER = 4;
    private static final int BLEND = 4;
    private static final float CROP = 1.5f;
    private static volatile int faceSize;
    private static final float FEATHER = 0.05f;

    private FwmSky() {
    }

    public static List<String> skies() {
        ArrayList<String> out = new ArrayList<String>();
        for (String pack : TextureMixer.packs()) {
            if (NONE.contains(pack)) continue;
            if (THUMBS.containsKey(pack) || FwmSky.hasSky(pack)) {
                out.add(pack);
                continue;
            }
            NONE.add(pack);
        }
        return out;
    }

    public static void rescan() {
        NONE.clear();
    }

    private static boolean hasSky(String pack) {
        boolean bl;
        TextureMixer.Source s = new TextureMixer.Source(pack);
        try {
            bl = FwmSky.optifine(s.root) != null || FwmSky.fabric(s.root) != null;
        }
        catch (Throwable throwable) {
            try {
                try {
                    s.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (Exception e) {
                return false;
            }
        }
        s.close();
        return bl;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static Path optifine(Path root) {
        String[] stringArray = new String[]{"optifine", "mcpatcher"};
        int n = stringArray.length;
        int n2 = 0;
        while (n2 < n) {
            String kind = stringArray[n2];
            Path dir = root.resolve("assets/minecraft/" + kind + "/sky/world0");
            if (Files.isDirectory(dir, new LinkOption[0])) {
                if (Files.exists(dir.resolve("sky1.png"), new LinkOption[0])) {
                    return dir.resolve("sky1.png");
                }
                try (Stream<Path> list = Files.walk(dir, 2, new FileVisitOption[0]);){
                    Path best = list.filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".png")).filter(p -> FwmSky.skyScore(p) >= 0).max(Comparator.comparingInt(FwmSky::skyScore).thenComparing(Path::toString, Comparator.reverseOrder())).orElse(null);
                    if (best != null) {
                        Path path = best;
                        return path;
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            ++n2;
        }
        return null;
    }

    private static int skyScore(Path p) {
        String n = p.toString().toLowerCase(Locale.ROOT).replace(File.separatorChar, '/');
        String file = p.getFileName().toString().toLowerCase(Locale.ROOT);
        for (String skip : new String[]{"rainbow", "starfield", "overlay", "fog"}) {
            if (!file.contains(skip)) continue;
            return -1;
        }
        if (file.matches("(sun|moon|stars?|base)[0-9]*[.]png")) {
            return -1;
        }
        int score = 0;
        if (n.contains("/day/") || file.contains("day") || file.contains("noon")) {
            score += 4;
        }
        if (file.contains("cloud")) {
            score += 3;
        }
        if (file.contains("sky")) {
            score += 2;
        }
        if (file.contains("night") || file.contains("sunset") || file.contains("sunrise")) {
            ++score;
        }
        return score;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static Path[] fabric(Path root) {
        Path assets = root.resolve("assets");
        if (!Files.isDirectory(assets, new LinkOption[0])) {
            return null;
        }
        try (Stream<Path> walk = Files.walk(assets, 3, new FileVisitOption[0]);){
            Iterator<Path> iterator = walk.filter(p -> p.getFileName().toString().endsWith(".json") && p.getParent() != null && p.getParent().getFileName().toString().equals("sky")).sorted().toList().iterator();
            while (iterator.hasNext()) {
                Path json = iterator.next();
                try {
                    JsonObject o = JsonParser.parseString((String)Files.readString(json, StandardCharsets.UTF_8)).getAsJsonObject();
                    if (!o.has("textures") || !o.get("textures").isJsonObject()) continue;
                    JsonObject t = o.getAsJsonObject("textures");
                    String[] keys = new String[]{"north", "east", "south", "west", "top", "bottom"};
                    Path[] out = new Path[6];
                    boolean all = true;
                    for (int i = 0; i < 6; ++i) {
                        if (!t.has(keys[i])) {
                            all = false;
                            break;
                        }
                        Identifier id = Identifier.tryParse((String)t.get(keys[i]).getAsString());
                        if (id == null) {
                            all = false;
                            break;
                        }
                        out[i] = root.resolve("assets/" + id.getNamespace() + "/" + id.getPath());
                        if (Files.exists(out[i], new LinkOption[0])) continue;
                        all = false;
                        break;
                    }
                    if (!all) continue;
                    Path[] pathArray = out;
                    return pathArray;
                }
                catch (Exception exception) {}
            }
            return null;
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    static BufferedImage[] faces(String pack) {
        try (TextureMixer.Source s = new TextureMixer.Source(pack);){
            Path[] six;
            Path sheet = FwmSky.optifine(s.root);
            if (sheet != null) {
                BufferedImage img;
                try (BufferedImage[] in = Files.newInputStream(sheet, new OpenOption[0]);){
                    img = ImageIO.read((InputStream)in);
                }
                if (img != null) {
                    in = FwmSky.fromSheet(img);
                    return in;
                }
            }
            if ((six = FwmSky.fabric(s.root)) == null) return null;
            BufferedImage[] out = new BufferedImage[6];
            for (int i = 0; i < 6; ++i) {
                try (InputStream in = Files.newInputStream(six[i], new OpenOption[0]);){
                    out[i] = ImageIO.read(in);
                }
                if (out[i] != null) continue;
                BufferedImage[] bufferedImageArray = null;
                return bufferedImageArray;
            }
            BufferedImage[] bufferedImageArray = out;
            return bufferedImageArray;
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    private static BufferedImage[] fromSheet(BufferedImage sheet) {
        int fw = sheet.getWidth() / 3;
        int fh = sheet.getHeight() / 2;
        if (fw <= 0 || fh <= 0) {
            return null;
        }
        int[][] at = new int[][]{{1, 1}, {2, 1}, {2, 0}, {0, 1}, {1, 0}, {0, 0}};
        BufferedImage[] out = new BufferedImage[6];
        for (int i = 0; i < 6; ++i) {
            out[i] = sheet.getSubimage(at[i][0] * fw, at[i][1] * fh, fw, fh);
        }
        return out;
    }

    public static BufferedImage thumb(String pack) {
        BufferedImage have = THUMBS.get(pack);
        if (have != null || NONE.contains(pack)) {
            return have;
        }
        if (PENDING.add(pack)) {
            Thread t = new Thread(() -> {
                BufferedImage[] f = FwmSky.faces(pack);
                if (f == null) {
                    NONE.add(pack);
                } else {
                    int h = 72;
                    int w = 72;
                    BufferedImage strip = new BufferedImage(w * 3, h, 2);
                    Graphics2D g = strip.createGraphics();
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g.drawImage(f[3], 0, 0, w, h, null);
                    g.drawImage(f[0], w, 0, w, h, null);
                    g.drawImage(f[1], w * 2, 0, w, h, null);
                    g.dispose();
                    THUMBS.put(pack, strip);
                }
                PENDING.remove(pack);
            }, "FWM sky thumb");
            t.setDaemon(true);
            t.start();
        }
        return null;
    }

    public static boolean active() {
        if (Features.switchedOff("fwm.tab.sky")) {
            return false;
        }
        String pack = V2.INSTANCE.skyPack;
        if (pack == null || pack.isEmpty()) {
            return false;
        }
        if (!pack.equals(loaded)) {
            if (loading == null) {
                FwmSky.load(pack);
            }
            return false;
        }
        return texture != null;
    }

    public static boolean inEnd() {
        return V2.INSTANCE.skyEnd && FwmSky.active();
    }

    public static boolean sunMoon() {
        return V2.INSTANCE.skySunMoon;
    }

    public static void reload() {
        loaded = "";
    }

    private static void load(String pack) {
        loading = pack;
        Thread t = new Thread(() -> {
            BufferedImage[] f = FwmSky.faces(pack);
            NativeImage image = null;
            if (f != null) {
                int size = 512;
                for (BufferedImage face : f) {
                    size = Math.min(size, Math.max(face.getWidth(), face.getHeight()));
                }
                size = Math.max(16, size);
                int[][] px = new int[6][];
                for (int i = 0; i < 6; ++i) {
                    BufferedImage scaled = new BufferedImage(size, size, 2);
                    Graphics2D g = scaled.createGraphics();
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g.drawImage(f[i], 0, 0, size, size, null);
                    g.dispose();
                    px[i] = scaled.getRGB(0, 0, size, size, null, 0, size);
                }
                image = FwmSky.seamless(px, size);
            }
            NativeImage done = image;
            int doneSize = f == null ? 0 : Math.max(16, Math.min(512, FwmSky.minSize(f)));
            Minecraft.getInstance().execute(() -> {
                if (texture != null) {
                    Minecraft.getInstance().getTextureManager().release(TEXTURE);
                    texture = null;
                }
                if (done != null) {
                    faceSize = doneSize;
                    if (cube != null) {
                        cube.close();
                        cube = null;
                    }
                    texture = new DynamicTexture(() -> "fwm sky", done);
                    Minecraft.getInstance().getTextureManager().register(TEXTURE, (AbstractTexture)texture);
                }
                loaded = pack;
                loading = null;
            });
        }, "FWM sky");
        t.setDaemon(true);
        t.start();
    }

    private static GpuBuffer cube() {
        if (cube != null) {
            return cube;
        }
        int quads = 12;
        try (ByteBufferBuilder alloc = ByteBufferBuilder.exactlySized((int)(quads * 4 * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize()));){
            BufferBuilder b = new BufferBuilder(alloc, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            int size = Math.max(16, faceSize);
            int cell = size + 8;
            float texW = (float)cell * 6.0f;
            float texH = cell;
            float[][] corners = new float[][]{{-1.0f, -1.0f}, {-1.0f, 1.0f}, {1.0f, 1.0f}, {1.0f, -1.0f}};
            for (int f = 0; f < 6; ++f) {
                int k;
                float u0 = (float)(f * cell + 4) / texW;
                float u1 = (float)(f * cell + 4 + size) / texW;
                float v0 = 4.0f / texH;
                float v1 = (float)(4 + size) / texH;
                float[][] v = new float[4][];
                float[][] uv = new float[4][];
                for (k = 0; k < 4; ++k) {
                    float a = corners[k][0];
                    float c = corners[k][1];
                    v[k] = FwmSky.dir(f, a, c);
                    uv[k] = new float[]{a < 0.0f ? u0 : u1, c < 0.0f ? v0 : v1};
                }
                for (k = 0; k < 4; ++k) {
                    b.addVertex(v[k][0] * 100.0f, v[k][1] * 100.0f, v[k][2] * 100.0f).setUv(uv[k][0], uv[k][1]).setColor(-1);
                }
                for (k = 3; k >= 0; --k) {
                    b.addVertex(v[k][0] * 100.0f, v[k][1] * 100.0f, v[k][2] * 100.0f).setUv(uv[k][0], uv[k][1]).setColor(-1);
                }
            }
            try (MeshData built = b.buildOrThrow();){
                cube = RenderSystem.getDevice().createBuffer(() -> "FWM sky cube", 40, built.vertexBuffer());
            }
        }
        return cube;
    }

    private static int minSize(BufferedImage[] f) {
        int size = 512;
        for (BufferedImage face : f) {
            size = Math.min(size, Math.max(face.getWidth(), face.getHeight()));
        }
        return size;
    }

    private static NativeImage seamless(int[][] px, int size) {
        int cell = size + 8;
        NativeImage img = new NativeImage(cell * 6, cell, false);
        int[] face = new int[size * size];
        float step = 2.0f / (float)size;
        int feather = Math.max(4, Math.round((float)size * 0.05f));
        for (int f = 0; f < 6; ++f) {
            int x;
            int y;
            for (y = 0; y < size; ++y) {
                for (x = 0; x < size; ++x) {
                    float a = ((float)x + 0.5f) * step - 1.0f;
                    float b = ((float)y + 0.5f) * step - 1.0f;
                    int own = FwmSky.sample(px, size, FwmSky.dir(f, a, b));
                    face[y * size + x] = FwmSky.feathered(px, size, f, a, b, x, y, own, feather, step);
                }
            }
            for (y = -4; y < size + 4; ++y) {
                for (x = -4; x < size + 4; ++x) {
                    int cx = Math.max(0, Math.min(size - 1, x));
                    int cy = Math.max(0, Math.min(size - 1, y));
                    img.setPixel(f * cell + 4 + x, 4 + y, 0xFF000000 | face[cy * size + cx]);
                }
            }
        }
        return img;
    }

    private static int feathered(int[][] px, int size, int f, float a, float b, int x, int y, int own, int feather, float step) {
        float r = own >> 16 & 0xFF;
        float g = own >> 8 & 0xFF;
        float bl = own & 0xFF;
        int[] dist = new int[]{x, size - 1 - x, y, size - 1 - y};
        for (int side = 0; side < 4; ++side) {
            float eb;
            float ea;
            int e = dist[side];
            if (e >= feather) continue;
            float f2 = side == 0 ? -1.0f : (ea = side == 1 ? 1.0f : a);
            float f3 = side == 2 ? -1.0f : (eb = side == 3 ? 1.0f : b);
            float ia = ea - (float)(side == 0 ? -1 : (side == 1 ? 1 : 0)) * step * 0.5f;
            float ib = eb - (float)(side == 2 ? -1 : (side == 3 ? 1 : 0)) * step * 0.5f;
            float oa = ea + (float)(side == 0 ? -1 : (side == 1 ? 1 : 0)) * step * 0.5f;
            float ob = eb + (float)(side == 2 ? -1 : (side == 3 ? 1 : 0)) * step * 0.5f;
            int cin = FwmSky.sample(px, size, FwmSky.dir(f, ia, ib));
            int cout = FwmSky.sample(px, size, FwmSky.dir(f, oa, ob));
            float mr = (float)((cin >> 16 & 0xFF) + (cout >> 16 & 0xFF)) * 0.5f;
            float mg = (float)((cin >> 8 & 0xFF) + (cout >> 8 & 0xFF)) * 0.5f;
            float mb = (float)((cin & 0xFF) + (cout & 0xFF)) * 0.5f;
            float t = 1.0f - ((float)e + 0.5f) / (float)feather;
            t = t * t * (3.0f - 2.0f * t);
            r += (mr - (float)(cin >> 16 & 0xFF)) * t;
            g += (mg - (float)(cin >> 8 & 0xFF)) * t;
            bl += (mb - (float)(cin & 0xFF)) * t;
        }
        int ri = Math.max(0, Math.min(255, Math.round(r)));
        int gi = Math.max(0, Math.min(255, Math.round(g)));
        int bi = Math.max(0, Math.min(255, Math.round(bl)));
        return ri << 16 | gi << 8 | bi;
    }

    private static int sample(int[][] px, int size, float[] d) {
        return FwmSky.sample(px, size, d[0], d[1], d[2]);
    }

    private static void add(float[] acc, int[][] px, int size, float[] d, float w) {
        int c = FwmSky.sample(px, size, d[0], d[1], d[2]);
        acc[0] = acc[0] + (float)(c >> 16 & 0xFF) * w;
        acc[1] = acc[1] + (float)(c >> 8 & 0xFF) * w;
        acc[2] = acc[2] + (float)(c & 0xFF) * w;
    }

    private static int sample(int[][] px, int size, float x, float y, float z) {
        float b;
        float a;
        int f;
        float ax = Math.abs(x);
        float ay = Math.abs(y);
        float az = Math.abs(z);
        if (az >= ax && az >= ay) {
            if (z < 0.0f) {
                f = 0;
                a = x / az;
                b = -y / az;
            } else {
                f = 2;
                a = -x / az;
                b = -y / az;
            }
        } else if (ax >= ay) {
            if (x > 0.0f) {
                f = 1;
                a = z / ax;
                b = -y / ax;
            } else {
                f = 3;
                a = -z / ax;
                b = -y / ax;
            }
        } else if (y > 0.0f) {
            f = 4;
            a = x / ay;
            b = z / ay;
        } else {
            f = 5;
            a = x / ay;
            b = -z / ay;
        }
        float span = (float)(size - 1) - 3.0f;
        float fx = 1.5f + (a + 1.0f) * 0.5f * span;
        float fy = 1.5f + (b + 1.0f) * 0.5f * span;
        int x0 = (int)Math.floor(fx);
        int y0 = (int)Math.floor(fy);
        float tx = fx - (float)x0;
        float ty = fy - (float)y0;
        int c00 = FwmSky.at(px[f], size, x0, y0);
        int c10 = FwmSky.at(px[f], size, x0 + 1, y0);
        int c01 = FwmSky.at(px[f], size, x0, y0 + 1);
        int c11 = FwmSky.at(px[f], size, x0 + 1, y0 + 1);
        int out = 0;
        for (int sh = 0; sh <= 16; sh += 8) {
            float top = (float)(c00 >> sh & 0xFF) * (1.0f - tx) + (float)(c10 >> sh & 0xFF) * tx;
            float bot = (float)(c01 >> sh & 0xFF) * (1.0f - tx) + (float)(c11 >> sh & 0xFF) * tx;
            out |= Math.round(top * (1.0f - ty) + bot * ty) << sh;
        }
        return out;
    }

    private static int at(int[] p, int size, int x, int y) {
        x = Math.max(0, Math.min(size - 1, x));
        y = Math.max(0, Math.min(size - 1, y));
        return p[y * size + x];
    }

    private static float[] dir(int f, float a, float b) {
        float[] fArray;
        switch (f) {
            case 0: {
                float[] fArray2 = new float[3];
                fArray2[0] = a;
                fArray2[1] = -b;
                fArray = fArray2;
                fArray2[2] = -1.0f;
                break;
            }
            case 1: {
                float[] fArray3 = new float[3];
                fArray3[0] = 1.0f;
                fArray3[1] = -b;
                fArray = fArray3;
                fArray3[2] = a;
                break;
            }
            case 2: {
                float[] fArray4 = new float[3];
                fArray4[0] = -a;
                fArray4[1] = -b;
                fArray = fArray4;
                fArray4[2] = 1.0f;
                break;
            }
            case 3: {
                float[] fArray5 = new float[3];
                fArray5[0] = -1.0f;
                fArray5[1] = -b;
                fArray = fArray5;
                fArray5[2] = -a;
                break;
            }
            case 4: {
                float[] fArray6 = new float[3];
                fArray6[0] = a;
                fArray6[1] = 1.0f;
                fArray = fArray6;
                fArray6[2] = b;
                break;
            }
            default: {
                float[] fArray7 = new float[3];
                fArray7[0] = a;
                fArray7[1] = -1.0f;
                fArray = fArray7;
                fArray7[2] = -b;
            }
        }
        return fArray;
    }

    public static void render(RenderTarget target) {
        if (texture == null) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        float light = 1.0f;
        if (V2.INSTANCE.skyNight && client.level != null && client.level.dimension() == Level.OVERWORLD) {
            double day = (double)(client.level.getOverworldClockTime() % 24000L) / 24000.0;
            double angle = (day - 0.25) * Math.PI * 2.0;
            float sun = (float)Math.max(0.0, Math.min(1.0, Math.cos(angle) * 2.0 + 0.5));
            light = 0.22f + 0.78f * sun;
        }
        Matrix4f view = RenderSystem.getModelViewMatrixCopy();
        if (V2.INSTANCE.skySpin != 0) {
            double minutes = (double)(System.currentTimeMillis() % 3600000L) / 60000.0;
            view.rotateY((float)Math.toRadians(minutes * (double)V2.INSTANCE.skySpin));
        }
        GpuBufferSlice uniforms = RenderSystem.getDynamicUniforms().writeTransform(view, new Vector4f(light, light, light, 1.0f), new Vector3f(), new Matrix4f());
        RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer((PrimitiveTopology)PrimitiveTopology.QUADS);
        GpuBuffer index = indices.getBuffer(72);
        if (target == null) {
            target = client.gameRenderer.mainRenderTarget();
        }
        GpuTextureView color = target.getColorTextureView();
        GpuTextureView depth = target.getDepthTextureView();
        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "FWM sky", color, Optional.empty(), depth, OptionalDouble.empty());){
            pass.setPipeline(RenderPipelines.END_SKY);
            RenderSystem.bindDefaultUniforms((RenderPass)pass);
            pass.setUniform("DynamicTransforms", uniforms);
            pass.bindTexture("Sampler0", texture.getTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
            pass.setVertexBuffer(0, FwmSky.cube().slice());
            pass.setIndexBuffer(index, indices.type());
            pass.drawIndexed(72, 1, 0, 0, 0);
        }
    }

    static {
        loaded = "";
        loading = null;
        THUMBS = new ConcurrentHashMap<String, BufferedImage>();
        NONE = ConcurrentHashMap.newKeySet();
        PENDING = ConcurrentHashMap.newKeySet();
        faceSize = 512;
    }
}

