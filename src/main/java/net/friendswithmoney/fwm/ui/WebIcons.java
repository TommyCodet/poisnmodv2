/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.NativeImage
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.resources.Identifier
 */
package net.friendswithmoney.fwm.ui;

import com.mojang.blaze3d.platform.NativeImage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import net.fabricmc.loader.api.FabricLoader;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.net.FwmApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

public final class WebIcons {
    private static final int SIZE = 16;
    private static final Map<String, Identifier> READY = new HashMap<String, Identifier>();
    private static final Set<String> PENDING = new HashSet<String>();
    private static final Set<String> FAILED = new HashSet<String>();

    private WebIcons() {
    }

    public static Identifier get(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        Identifier ready = READY.get(path);
        if (ready != null || PENDING.contains(path) || FAILED.contains(path)) {
            return ready;
        }
        PENDING.add(path);
        WebIcons.load(path);
        return null;
    }

    private static void load(String path) {
        Minecraft client = Minecraft.getInstance();
        Path file = WebIcons.cacheFile(path);
        FwmApi.executor().execute(() -> {
            byte[] local = null;
            try {
                if (Files.exists(file, new LinkOption[0])) {
                    local = Files.readAllBytes(file);
                }
            }
            catch (Exception e) {
                local = null;
            }
            if (local != null) {
                WebIcons.finish(client, path, WebIcons.shrink(local));
                return;
            }
            String url = FwmConfig.INSTANCE.websiteUrl.replaceAll("/+$", "") + (String)(path.startsWith("/") ? path : "/" + path);
            FwmApi.getBytes(url).whenCompleteAsync((bytes, error) -> {
                if (error != null || bytes == null) {
                    System.err.println("[FWM] Icon " + path + " failed: " + (error == null ? "empty" : error.getMessage()));
                    WebIcons.finish(client, path, null);
                    return;
                }
                int[] pixels = WebIcons.shrink(bytes);
                if (pixels != null) {
                    try {
                        Files.createDirectories(file.getParent(), new FileAttribute[0]);
                        Files.write(file, bytes, new OpenOption[0]);
                    }
                    catch (Exception e) {
                        System.err.println("[FWM] Could not write " + String.valueOf(file) + ": " + e.getMessage());
                    }
                }
                WebIcons.finish(client, path, pixels);
            }, FwmApi.executor());
        });
    }

    private static void finish(Minecraft client, String path, int[] pixels) {
        client.execute(() -> {
            PENDING.remove(path);
            if (pixels == null) {
                FAILED.add(path);
                return;
            }
            try {
                NativeImage image = new NativeImage(16, 16, false);
                for (int y = 0; y < 16; ++y) {
                    for (int x = 0; x < 16; ++x) {
                        image.setPixel(x, y, pixels[y * 16 + x]);
                    }
                }
                Identifier id = Identifier.fromNamespaceAndPath((String)"fwm", (String)("webicon/" + WebIcons.slug(path)));
                client.getTextureManager().register(id, (AbstractTexture)new DynamicTexture(() -> "fwm web icon " + path, image));
                READY.put(path, id);
            }
            catch (Exception e) {
                System.err.println("[FWM] Icon " + path + " could not be loaded: " + e.getMessage());
                FAILED.add(path);
            }
        });
    }

    private static int[] shrink(byte[] png) {
        try {
            BufferedImage src = ImageIO.read(new ByteArrayInputStream(png));
            if (src == null) {
                return null;
            }
            int w = src.getWidth();
            int h = src.getHeight();
            int[] out = new int[256];
            for (int ty = 0; ty < 16; ++ty) {
                int y0 = ty * h / 16;
                int y1 = Math.max(y0 + 1, (ty + 1) * h / 16);
                for (int tx = 0; tx < 16; ++tx) {
                    int x0 = tx * w / 16;
                    int x1 = Math.max(x0 + 1, (tx + 1) * w / 16);
                    long a = 0L;
                    long r = 0L;
                    long g = 0L;
                    long b = 0L;
                    int n = 0;
                    for (int y = y0; y < y1; ++y) {
                        for (int x = x0; x < x1; ++x) {
                            int argb = src.getRGB(x, y);
                            int alpha = argb >>> 24;
                            a += (long)alpha;
                            r += (long)(argb >> 16 & 0xFF) * (long)alpha;
                            g += (long)(argb >> 8 & 0xFF) * (long)alpha;
                            b += (long)(argb & 0xFF) * (long)alpha;
                            ++n;
                        }
                    }
                    if (a == 0L) continue;
                    int alpha = (int)(a / (long)n);
                    out[ty * 16 + tx] = alpha << 24 | (int)(r / a) << 16 | (int)(g / a) << 8 | (int)(b / a);
                }
            }
            return out;
        }
        catch (Exception e) {
            return null;
        }
    }

    private static String slug(String path) {
        String name = path.toLowerCase(Locale.ROOT).replaceAll("^/+", "").replaceAll("[^a-z0-9/._-]", "_");
        return name.isEmpty() ? "unknown" : name;
    }

    private static Path cacheFile(String path) {
        return FabricLoader.getInstance().getConfigDir().resolve("fwm-icons").resolve(WebIcons.slug(path).replace('/', '_'));
    }
}

