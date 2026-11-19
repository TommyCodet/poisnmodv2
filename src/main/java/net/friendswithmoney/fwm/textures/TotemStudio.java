/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.client.Minecraft
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.packs.PackType
 *  net.minecraft.server.packs.resources.IoSupplier
 */
package net.friendswithmoney.fwm.textures;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import net.friendswithmoney.fwm.antileak.AntiLeakPack;
import net.friendswithmoney.fwm.textures.TextureMixer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;

public final class TotemStudio {
    public static final String PREFIX = "FWM Totem ";
    private static final int OUTLINE = -15329766;
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8L)).followRedirects(HttpClient.Redirect.NORMAL).build();
    public static final float[][] FIGURE = new float[][]{{6.0f, 12.0f, 6.0f, 10.0f, 16.0f, 10.0f, 0.0f, 0.0f, 8.0f, 8.0f, 8.0f, 0.0f}, {6.0f, 6.0f, 7.0f, 10.0f, 12.0f, 9.0f, 16.0f, 16.0f, 8.0f, 12.0f, 4.0f, 0.0f}, {4.0f, 6.0f, 7.0f, 6.0f, 12.0f, 9.0f, 40.0f, 16.0f, 4.0f, 12.0f, 4.0f, 0.0f}, {10.0f, 6.0f, 7.0f, 12.0f, 12.0f, 9.0f, 32.0f, 48.0f, 4.0f, 12.0f, 4.0f, 0.0f}, {6.0f, 0.0f, 7.0f, 8.0f, 6.0f, 9.0f, 0.0f, 16.0f, 4.0f, 12.0f, 4.0f, 0.0f}, {8.0f, 0.0f, 7.0f, 10.0f, 6.0f, 9.0f, 16.0f, 48.0f, 4.0f, 12.0f, 4.0f, 0.0f}, {6.0f, 12.0f, 6.0f, 10.0f, 16.0f, 10.0f, 32.0f, 0.0f, 8.0f, 8.0f, 8.0f, 0.25f}, {6.0f, 6.0f, 7.0f, 10.0f, 12.0f, 9.0f, 16.0f, 32.0f, 8.0f, 12.0f, 4.0f, 0.13f}, {4.0f, 6.0f, 7.0f, 6.0f, 12.0f, 9.0f, 40.0f, 32.0f, 4.0f, 12.0f, 4.0f, 0.13f}, {10.0f, 6.0f, 7.0f, 12.0f, 12.0f, 9.0f, 48.0f, 48.0f, 4.0f, 12.0f, 4.0f, 0.13f}, {6.0f, 0.0f, 7.0f, 8.0f, 6.0f, 9.0f, 0.0f, 32.0f, 4.0f, 12.0f, 4.0f, 0.13f}, {8.0f, 0.0f, 7.0f, 10.0f, 6.0f, 9.0f, 0.0f, 48.0f, 4.0f, 12.0f, 4.0f, 0.13f}};

    private TotemStudio() {
    }

    public static BufferedImage fetchSkin(String name) throws Exception {
        String clean = name.trim().replaceAll("[^A-Za-z0-9_]", "");
        if (clean.isEmpty()) {
            return null;
        }
        HttpResponse<String> who = HTTP.send(HttpRequest.newBuilder(URI.create("https://api.mojang.com/users/profiles/minecraft/" + clean)).timeout(Duration.ofSeconds(8L)).GET().build(), HttpResponse.BodyHandlers.ofString());
        if (who.statusCode() != 200) {
            return null;
        }
        String id = JsonParser.parseString((String)who.body()).getAsJsonObject().get("id").getAsString();
        HttpResponse<String> profile = HTTP.send(HttpRequest.newBuilder(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + id)).timeout(Duration.ofSeconds(8L)).GET().build(), HttpResponse.BodyHandlers.ofString());
        if (profile.statusCode() != 200) {
            return null;
        }
        JsonObject p = JsonParser.parseString((String)profile.body()).getAsJsonObject();
        for (JsonElement e : p.getAsJsonArray("properties")) {
            JsonObject prop = e.getAsJsonObject();
            if (!prop.get("name").getAsString().equals("textures")) continue;
            String json = new String(Base64.getDecoder().decode(prop.get("value").getAsString()), StandardCharsets.UTF_8);
            JsonObject tex = JsonParser.parseString((String)json).getAsJsonObject().getAsJsonObject("textures");
            if (tex == null || !tex.has("SKIN")) {
                return null;
            }
            String url = tex.getAsJsonObject("SKIN").get("url").getAsString();
            if (!url.startsWith("http://textures.minecraft.net/") && !url.startsWith("https://textures.minecraft.net/")) {
                return null;
            }
            HttpResponse<InputStream> png = HTTP.send(HttpRequest.newBuilder(URI.create(url.replace("http://", "https://"))).timeout(Duration.ofSeconds(8L)).GET().build(), HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream in = png.body();){
                BufferedImage bufferedImage = TotemStudio.normalize(ImageIO.read(in));
                return bufferedImage;
            }
        }
        return null;
    }

    public static BufferedImage normalize(BufferedImage skin) {
        if (skin == null || skin.getWidth() != 64 || skin.getHeight() != 64 && skin.getHeight() != 32) {
            return null;
        }
        BufferedImage out = new BufferedImage(64, 64, 2);
        Graphics2D g = out.createGraphics();
        g.drawImage((Image)skin, 0, 0, null);
        g.dispose();
        if (skin.getHeight() == 32) {
            TotemStudio.mirrorLimb(out, 0, 16, 16, 48);
            TotemStudio.mirrorLimb(out, 40, 16, 32, 48);
        }
        return out;
    }

    private static void mirrorLimb(BufferedImage img, int su, int sv, int du, int dv) {
        for (int y = 0; y < 16; ++y) {
            for (int x = 0; x < 16; ++x) {
                img.setRGB(du + x, dv + y, 0);
            }
        }
        for (int part = 0; part < 2; ++part) {
            for (int y = 0; y < 4; ++y) {
                for (int x = 0; x < 4; ++x) {
                    img.setRGB(du + 4 + part * 4 + x, dv + y, img.getRGB(su + 4 + part * 4 + 3 - x, sv + y));
                }
            }
        }
        int[] order = new int[]{2, 1, 0, 3};
        for (int side = 0; side < 4; ++side) {
            int from = order[side];
            for (int y = 0; y < 12; ++y) {
                for (int x = 0; x < 4; ++x) {
                    img.setRGB(du + side * 4 + x, dv + 4 + y, img.getRGB(su + from * 4 + 3 - x, sv + 4 + y));
                }
            }
        }
    }

    public static int[] face(BufferedImage skin) {
        int[] out = new int[64];
        for (int y = 0; y < 8; ++y) {
            for (int x = 0; x < 8; ++x) {
                int c = skin.getRGB(8 + x, 8 + y) | 0xFF000000;
                int hat = skin.getRGB(40 + x, 8 + y);
                out[y * 8 + x] = hat >>> 24 > 127 ? hat | 0xFF000000 : c;
            }
        }
        return out;
    }

    public static int[] fromSkin(BufferedImage skin, int size, boolean hat, boolean outline) {
        int[] out = new int[size * size];
        int[][] parts = new int[][]{{5, 14, 3, 2, 4, 20, 4, 12, 4, 36}, {8, 14, 3, 2, 20, 52, 4, 12, 4, 52}, {5, 8, 6, 6, 20, 20, 8, 12, 20, 36}, {3, 8, 2, 4, 44, 20, 4, 12, 44, 36}, {11, 8, 2, 4, 36, 52, 4, 12, 52, 52}, {4, 0, 8, 8, 8, 8, 8, 8, 40, 8}};
        float s = (float)size / 16.0f;
        for (int[] p : parts) {
            int dx0 = Math.round((float)p[0] * s);
            int dy0 = Math.round((float)p[1] * s);
            int dw = Math.round((float)(p[0] + p[2]) * s) - dx0;
            int dh = Math.round((float)(p[1] + p[3]) * s) - dy0;
            for (int y = 0; y < dh; ++y) {
                for (int x = 0; x < dw; ++x) {
                    int o;
                    int sx = (int)(((float)x + 0.5f) / (float)dw * (float)p[6]);
                    int sy = (int)(((float)y + 0.5f) / (float)dh * (float)p[7]);
                    int c = skin.getRGB(p[4] + sx, p[5] + sy) | 0xFF000000;
                    if (hat && (o = skin.getRGB(p[8] + sx, p[9] + sy)) >>> 24 > 127) {
                        c = o | 0xFF000000;
                    }
                    out[(dy0 + y) * size + dx0 + x] = c;
                }
            }
        }
        if (outline) {
            TotemStudio.outline(out, size);
        }
        return out;
    }

    public static int[] fromPicture(BufferedImage src, int size, boolean removeBg, int colors, boolean outline) {
        int h;
        int w = src.getWidth();
        if (Math.max(w, h = src.getHeight()) > 512) {
            float f = 512.0f / (float)Math.max(w, h);
            BufferedImage small = new BufferedImage(Math.max(1, Math.round((float)w * f)), Math.max(1, Math.round((float)h * f)), 2);
            Graphics2D g = small.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(src, 0, 0, small.getWidth(), small.getHeight(), null);
            g.dispose();
            src = small;
            w = src.getWidth();
            h = src.getHeight();
        }
        int[] px = src.getRGB(0, 0, w, h, null, 0, w);
        if (removeBg && !TotemStudio.seeThrough(px, w, h)) {
            TotemStudio.flood(px, w, h);
        }
        int x0 = w;
        int y0 = h;
        int x1 = -1;
        int y1 = -1;
        for (int y = 0; y < h; ++y) {
            for (int x = 0; x < w; ++x) {
                if (px[y * w + x] >>> 24 <= 24) continue;
                x0 = Math.min(x0, x);
                y0 = Math.min(y0, y);
                x1 = Math.max(x1, x);
                y1 = Math.max(y1, y);
            }
        }
        int[] out = new int[size * size];
        if (x1 < 0) {
            return out;
        }
        int cw = x1 - x0 + 1;
        int ch = y1 - y0 + 1;
        int room = outline ? size - 2 : size;
        float scale = Math.min((float)room / (float)cw, (float)room / (float)ch);
        int tw = Math.max(1, Math.round((float)cw * scale));
        int th = Math.max(1, Math.round((float)ch * scale));
        int ox = (size - tw) / 2;
        int oy = (size - th) / 2;
        for (int ty = 0; ty < th; ++ty) {
            for (int tx = 0; tx < tw; ++tx) {
                int alpha;
                int sx0 = x0 + (int)Math.floor((float)tx / scale);
                int sx1 = Math.max(sx0 + 1, x0 + (int)Math.floor((float)(tx + 1) / scale));
                int sy0 = y0 + (int)Math.floor((float)ty / scale);
                int sy1 = Math.max(sy0 + 1, y0 + (int)Math.floor((float)(ty + 1) / scale));
                long a = 0L;
                long r = 0L;
                long gr = 0L;
                long b = 0L;
                long n = 0L;
                for (int sy = sy0; sy < Math.min(sy1, h); ++sy) {
                    for (int sx = sx0; sx < Math.min(sx1, w); ++sx) {
                        int c = px[sy * w + sx];
                        int al = c >>> 24;
                        a += (long)al;
                        r += (long)(c >> 16 & 0xFF) * (long)al;
                        gr += (long)(c >> 8 & 0xFF) * (long)al;
                        b += (long)(c & 0xFF) * (long)al;
                        ++n;
                    }
                }
                if (n == 0L || a == 0L || (alpha = (int)(a / n)) < 110) continue;
                out[(oy + ty) * size + ox + tx] = 0xFF000000 | (int)(r / a) << 16 | (int)(gr / a) << 8 | (int)(b / a);
            }
        }
        if (colors > 0) {
            TotemStudio.reduce(out, colors);
        }
        if (outline) {
            TotemStudio.outline(out, size);
        }
        return out;
    }

    private static boolean seeThrough(int[] px, int w, int h) {
        int[] corners;
        for (int c : corners = new int[]{px[0], px[w - 1], px[(h - 1) * w], px[h * w - 1]}) {
            if (c >>> 24 >= 128) continue;
            return true;
        }
        return false;
    }

    private static void flood(int[] px, int w, int h) {
        int[] corners = new int[]{px[0], px[w - 1], px[(h - 1) * w], px[h * w - 1]};
        boolean[] seen = new boolean[w * h];
        ArrayDeque<Integer> todo = new ArrayDeque<Integer>();
        for (int x = 0; x < w; ++x) {
            todo.add(x);
            todo.add((h - 1) * w + x);
        }
        for (int y = 0; y < h; ++y) {
            todo.add(y * w);
            todo.add(y * w + w - 1);
        }
        while (!todo.isEmpty()) {
            int i = (Integer)todo.poll();
            if (seen[i]) continue;
            seen[i] = true;
            if (!TotemStudio.nearAny(px[i], corners)) continue;
            px[i] = 0;
            int x = i % w;
            int y = i / w;
            if (x > 0) {
                todo.add(i - 1);
            }
            if (x < w - 1) {
                todo.add(i + 1);
            }
            if (y > 0) {
                todo.add(i - w);
            }
            if (y >= h - 1) continue;
            todo.add(i + w);
        }
    }

    private static boolean nearAny(int c, int[] refs) {
        for (int r : refs) {
            int dr = (c >> 16 & 0xFF) - (r >> 16 & 0xFF);
            int dg = (c >> 8 & 0xFF) - (r >> 8 & 0xFF);
            int db = (c & 0xFF) - (r & 0xFF);
            if (dr * dr + dg * dg + db * db >= 1764) continue;
            return true;
        }
        return false;
    }

    private static void reduce(int[] px, int k) {
        ArrayList<Integer> idx = new ArrayList<Integer>();
        for (int i = 0; i < px.length; ++i) {
            if (px[i] >>> 24 == 0) continue;
            idx.add(i);
        }
        if (idx.size() <= k) {
            return;
        }
        float[][] c = new float[k][3];
        for (int j = 0; j < k; ++j) {
            int p = px[(Integer)idx.get(j * idx.size() / k)];
            c[j] = new float[]{p >> 16 & 0xFF, p >> 8 & 0xFF, p & 0xFF};
        }
        int[] of = new int[idx.size()];
        for (int round = 0; round < 8; ++round) {
            float[][] sum = new float[k][3];
            int[] n = new int[k];
            for (int q = 0; q < idx.size(); ++q) {
                int p = px[(Integer)idx.get(q)];
                float r = p >> 16 & 0xFF;
                float g = p >> 8 & 0xFF;
                float b = p & 0xFF;
                int best = 0;
                float bd = Float.MAX_VALUE;
                for (int j = 0; j < k; ++j) {
                    float d = (r - c[j][0]) * (r - c[j][0]) + (g - c[j][1]) * (g - c[j][1]) + (b - c[j][2]) * (b - c[j][2]);
                    if (!(d < bd)) continue;
                    bd = d;
                    best = j;
                }
                of[q] = best;
                float[] fArray = sum[best];
                fArray[0] = fArray[0] + r;
                float[] fArray2 = sum[best];
                fArray2[1] = fArray2[1] + g;
                float[] fArray3 = sum[best];
                fArray3[2] = fArray3[2] + b;
                int n2 = best;
                n[n2] = n[n2] + 1;
            }
            for (int j = 0; j < k; ++j) {
                if (n[j] <= 0) continue;
                c[j] = new float[]{sum[j][0] / (float)n[j], sum[j][1] / (float)n[j], sum[j][2] / (float)n[j]};
            }
        }
        for (int q = 0; q < idx.size(); ++q) {
            float[] cc = c[of[q]];
            px[((Integer)idx.get((int)q)).intValue()] = 0xFF000000 | Math.round(cc[0]) << 16 | Math.round(cc[1]) << 8 | Math.round(cc[2]);
        }
    }

    public static void outline(int[] px, int size) {
        int[] copy = (int[])px.clone();
        for (int y = 0; y < size; ++y) {
            for (int x = 0; x < size; ++x) {
                boolean next;
                if (copy[y * size + x] >>> 24 != 0) continue;
                boolean bl = next = x > 0 && copy[y * size + x - 1] >>> 24 != 0 || x < size - 1 && copy[y * size + x + 1] >>> 24 != 0 || y > 0 && copy[(y - 1) * size + x] >>> 24 != 0 || y < size - 1 && copy[(y + 1) * size + x] >>> 24 != 0;
                if (!next) continue;
                px[y * size + x] = -15329766;
            }
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static int[] vanilla(int size) {
        try {
            IoSupplier supplier = Minecraft.getInstance().getVanillaPackResources().getResource(PackType.CLIENT_RESOURCES, Identifier.withDefaultNamespace((String)"textures/item/totem_of_undying.png"));
            if (supplier == null) return new int[size * size];
            try (InputStream in = (InputStream)supplier.get();){
                BufferedImage img = ImageIO.read(in);
                if (img == null) return new int[size * size];
                int[] nArray = TotemStudio.resize(img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth()), img.getWidth(), size);
                return nArray;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return new int[size * size];
    }

    public static int[] resize(int[] px, int from, int to) {
        if (from == to) {
            return (int[])px.clone();
        }
        int[] out = new int[to * to];
        for (int y = 0; y < to; ++y) {
            for (int x = 0; x < to; ++x) {
                out[y * to + x] = px[y * from / to * from + x * from / to];
            }
        }
        return out;
    }

    public static String folder(String title) {
        String t;
        String string = t = title == null ? "" : title.trim();
        if (t.toLowerCase(Locale.ROOT).startsWith("fwm totem")) {
            t = t.substring(9).trim();
        }
        if ((t = t.replaceAll("[^A-Za-z0-9 _-]", "").trim()).isEmpty()) {
            t = "Eigenes";
        }
        if (t.length() > 32) {
            t = t.substring(0, 32);
        }
        return PREFIX + t;
    }

    public static String write(String title, int[] px, int size, BufferedImage skin3d) throws IOException {
        String name = TotemStudio.folder(title);
        Path dir = Minecraft.getInstance().getResourcePackDirectory().resolve(name).normalize();
        if (!dir.startsWith(Minecraft.getInstance().getResourcePackDirectory())) {
            throw new IOException("bad name");
        }
        Files.createDirectories(dir, new FileAttribute[0]);
        Files.writeString(dir.resolve("pack.mcmeta"), (CharSequence)AntiLeakPack.mcmeta(), StandardCharsets.UTF_8, new OpenOption[0]);
        Path item = dir.resolve("assets/minecraft/textures/item/totem_of_undying.png");
        Files.createDirectories(item.getParent(), new FileAttribute[0]);
        BufferedImage img = new BufferedImage(size, size, 2);
        img.setRGB(0, 0, size, size, px, 0, size);
        ImageIO.write((RenderedImage)img, "png", item.toFile());
        Path skinPng = dir.resolve("assets/minecraft/textures/item/totem_of_undying_skin.png");
        Path model = dir.resolve("assets/minecraft/models/item/totem_of_undying.json");
        if (skin3d != null) {
            ImageIO.write((RenderedImage)skin3d, "png", skinPng.toFile());
            Files.createDirectories(model.getParent(), new FileAttribute[0]);
            Files.writeString(model, (CharSequence)TotemStudio.model3d(), StandardCharsets.UTF_8, new OpenOption[0]);
        } else {
            Files.deleteIfExists(skinPng);
            Files.deleteIfExists(model);
        }
        Path png = dir.resolve("pack.png");
        BufferedImage icon = new BufferedImage(64, 64, 2);
        Graphics2D g = icon.createGraphics();
        g.drawImage(img, 0, 0, 64, 64, null);
        g.dispose();
        ImageIO.write((RenderedImage)icon, "png", png.toFile());
        TextureMixer.forget(name);
        return name;
    }

    public static List<String> made() {
        ArrayList found = new ArrayList();
        try (Stream<Path> list = Files.list(Minecraft.getInstance().getResourcePackDirectory());){
            list.filter(p -> p.getFileName().toString().startsWith(PREFIX) && Files.isDirectory(p, new LinkOption[0])).forEach(found::add);
        }
        catch (Exception exception) {
            // empty catch block
        }
        found.sort((a, b) -> {
            try {
                return Files.getLastModifiedTime(b, new LinkOption[0]).compareTo(Files.getLastModifiedTime(a, new LinkOption[0]));
            }
            catch (Exception e) {
                return 0;
            }
        });
        return found.stream().map(p -> p.getFileName().toString()).toList();
    }

    public static BufferedImage picture(String pack) {
        try {
            Path p = Minecraft.getInstance().getResourcePackDirectory().resolve(pack).resolve("assets/minecraft/textures/item/totem_of_undying.png");
            return Files.exists(p, new LinkOption[0]) ? ImageIO.read(p.toFile()) : null;
        }
        catch (Exception e) {
            return null;
        }
    }

    public static int[][] faces(int u, int v, int w, int h, int d) {
        return new int[][]{{u + d, v, u + d + w, v + d}, {u + d + w, v + d, u + d + w + w, v}, {u, v + d, u + d, v + d + h}, {u + d, v + d, u + d + w, v + d + h}, {u + d + w, v + d, u + d + w + d, v + d + h}, {u + d + w + d, v + d, u + 2 * (d + w), v + d + h}};
    }

    private static String model3d() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"gui_light\": \"front\",\n  \"textures\": { \"skin\": \"minecraft:item/totem_of_undying_skin\", \"particle\": \"minecraft:item/totem_of_undying\" },\n");
        sb.append("  \"elements\": [\n");
        String[] names = new String[]{"up", "down", "west", "south", "east", "north"};
        for (int i = 0; i < FIGURE.length; ++i) {
            float[] b = FIGURE[i];
            float g = b[11];
            sb.append("    { \"from\": [").append(TotemStudio.f(b[0] - g)).append(", ").append(TotemStudio.f(b[1] - g)).append(", ").append(TotemStudio.f(b[2] - g)).append("], \"to\": [").append(TotemStudio.f(b[3] + g)).append(", ").append(TotemStudio.f(b[4] + g)).append(", ").append(TotemStudio.f(b[5] + g)).append("], \"shade\": false, \"light_emission\": 10, \"faces\": {");
            int[][] fc = TotemStudio.faces((int)b[6], (int)b[7], (int)b[8], (int)b[9], (int)b[10]);
            for (int k = 0; k < 6; ++k) {
                sb.append(k == 0 ? " " : ", ").append('\"').append(names[k]).append("\": { \"uv\": [").append(TotemStudio.f((float)fc[k][0] / 4.0f)).append(", ").append(TotemStudio.f((float)fc[k][1] / 4.0f)).append(", ").append(TotemStudio.f((float)fc[k][2] / 4.0f)).append(", ").append(TotemStudio.f((float)fc[k][3] / 4.0f)).append("], \"texture\": \"#skin\" }");
            }
            sb.append(" } }").append(i < FIGURE.length - 1 ? ",\n" : "\n");
        }
        sb.append("  ],\n");
        sb.append("  \"display\": {\n    \"gui\": { \"rotation\": [10, -28, 0], \"translation\": [0, 0, 0], \"scale\": [1, 1, 1] },\n    \"ground\": { \"rotation\": [0, 0, 0], \"translation\": [0, 2, 0], \"scale\": [0.5, 0.5, 0.5] },\n    \"fixed\": { \"rotation\": [0, 180, 0], \"translation\": [0, 0, 0], \"scale\": [1, 1, 1] },\n    \"head\": { \"rotation\": [0, 180, 0], \"translation\": [0, 13, 7], \"scale\": [1, 1, 1] },\n    \"thirdperson_righthand\": { \"rotation\": [0, 0, 0], \"translation\": [0, 3, 1], \"scale\": [0.55, 0.55, 0.55] },\n    \"thirdperson_lefthand\": { \"rotation\": [0, 0, 0], \"translation\": [0, 3, 1], \"scale\": [0.55, 0.55, 0.55] },\n    \"firstperson_righthand\": { \"rotation\": [0, 0, 0], \"translation\": [1, 2, -1], \"scale\": [0.38, 0.38, 0.38] },\n    \"firstperson_lefthand\": { \"rotation\": [0, 0, 0], \"translation\": [1, 2, -1], \"scale\": [0.38, 0.38, 0.38] }\n  }\n}\n");
        return sb.toString();
    }

    private static String f(float v) {
        return v == (float)Math.round(v) ? String.valueOf(Math.round(v)) : String.valueOf((float)Math.round(v * 1000.0f) / 1000.0f);
    }

    private record AABB(float x0, float y0, float z0, float x1, float y1, float z1, int u, int v, int w, int h, int d) {
    }
}

