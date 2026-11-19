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
package net.friendswithmoney.fwm.seventv;

import com.mojang.blaze3d.platform.NativeImage;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import net.friendswithmoney.fwm.seventv.Emote;
import net.friendswithmoney.fwm.seventv.EmoteApi;
import net.friendswithmoney.fwm.seventv.SevenTv;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class EmoteTexture {
    private static final int FRAME_H = 56;
    private static final int MAX_FRAME_W = 224;
    private static final int MAX_FRAMES = 80;
    private static final Map<String, EmoteTexture> CACHE = new ConcurrentHashMap<String, EmoteTexture>();
    public final Emote emote;
    public final Identifier id;
    public volatile State state = State.LOADING;
    public volatile int frameW = 56;
    public volatile int frameH = 56;
    public volatile int frames = 1;
    private volatile int[] frameEnds = new int[]{100};
    public volatile long hash;

    private EmoteTexture(Emote emote) {
        this.emote = emote;
        this.id = Identifier.fromNamespaceAndPath((String)"fwm", (String)("emotes/" + emote.key()));
    }

    public static EmoteTexture get(Emote emote) {
        return CACHE.computeIfAbsent(emote.key(), k -> {
            EmoteTexture t = new EmoteTexture(emote);
            t.load();
            return t;
        });
    }

    public static EmoteTexture byKey(String key) {
        return CACHE.get(key);
    }

    public static EmoteTexture peek(Emote emote) {
        return CACHE.get(emote.key());
    }

    public float aspect() {
        return this.frameH == 0 ? 1.0f : (float)this.frameW / (float)this.frameH;
    }

    public int currentFrame() {
        int n = this.frames;
        if (n <= 1) {
            return 0;
        }
        int[] ends = this.frameEnds;
        int total = ends[ends.length - 1];
        if (total <= 0) {
            return 0;
        }
        long t = System.currentTimeMillis() % (long)total;
        for (int i = 0; i < ends.length; ++i) {
            if (t >= (long)ends[i]) continue;
            return i;
        }
        return n - 1;
    }

    public float v0() {
        return (float)this.currentFrame() / (float)this.frames;
    }

    public float v1() {
        return (float)(this.currentFrame() + 1) / (float)this.frames;
    }

    private void load() {
        String[] urls = this.emote.imageUrls();
        this.tryUrl(urls, 0);
    }

    private void tryUrl(String[] urls, int idx) {
        if (idx >= urls.length) {
            this.state = State.FAILED;
            return;
        }
        ((CompletableFuture)EmoteApi.download(urls[idx]).thenApplyAsync(this::decode, (Executor)EmoteApi.IO)).whenComplete((dec, err) -> {
            if (err != null || dec == null) {
                if (idx + 1 >= urls.length) {
                    SevenTv.LOG.debug("Emote {} konnte nicht geladen werden: {}", (Object)this.emote.name(), (Object)String.valueOf(err));
                }
                this.tryUrl(urls, idx + 1);
                return;
            }
            Minecraft.getInstance().execute(() -> this.upload((Decoded)dec));
        });
    }

    private Decoded decode(byte[] data) {
        try {
            ArrayList<BufferedImage> frameImgs = new ArrayList<BufferedImage>();
            ArrayList<Integer> delays = new ArrayList<Integer>();
            EmoteTexture.readFrames(data, frameImgs, delays);
            if (frameImgs.isEmpty()) {
                return null;
            }
            if (frameImgs.size() > 80) {
                ArrayList<BufferedImage> f2 = new ArrayList<BufferedImage>();
                ArrayList<Integer> d2 = new ArrayList<Integer>();
                double step = (double)frameImgs.size() / 80.0;
                for (int i = 0; i < 80; ++i) {
                    int from = (int)Math.floor((double)i * step);
                    int to = (int)Math.floor((double)(i + 1) * step);
                    int sum = 0;
                    for (int j = from; j < Math.max(to, from + 1) && j < delays.size(); ++j) {
                        sum += ((Integer)delays.get(j)).intValue();
                    }
                    f2.add((BufferedImage)frameImgs.get(from));
                    d2.add(sum);
                }
                frameImgs = f2;
                delays = d2;
            }
            BufferedImage first = (BufferedImage)frameImgs.get(0);
            int srcW = first.getWidth();
            int srcH = first.getHeight();
            int fh = 56;
            int fw = Math.max(1, Math.min(224, Math.round((float)srcW * ((float)fh / (float)srcH))));
            int n = frameImgs.size();
            BufferedImage strip = new BufferedImage(fw, fh * n, 2);
            Graphics2D g = strip.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setComposite(AlphaComposite.Src);
            for (int i = 0; i < n; ++i) {
                g.drawImage((Image)frameImgs.get(i), 0, i * fh, fw, fh, null);
            }
            g.dispose();
            int[] ends = new int[n];
            int acc = 0;
            for (int i = 0; i < n; ++i) {
                int d;
                int n2 = d = i < delays.size() ? (Integer)delays.get(i) : 100;
                if (d < 20) {
                    d = 100;
                }
                ends[i] = acc += d;
            }
            NativeImage img = new NativeImage(strip.getWidth(), strip.getHeight(), true);
            for (int y = 0; y < strip.getHeight(); ++y) {
                for (int x = 0; x < strip.getWidth(); ++x) {
                    img.setPixel(x, y, strip.getRGB(x, y));
                }
            }
            return new Decoded(img, fw, fh, n, ends, EmoteTexture.averageHash(first));
        }
        catch (Throwable t) {
            SevenTv.LOG.debug("Dekodieren von {} fehlgeschlagen: {}", (Object)this.emote.name(), (Object)t.toString());
            return null;
        }
    }

    private void upload(Decoded d) {
        try {
            NativeImage img = d.image();
            DynamicTexture tex = new DynamicTexture(() -> "fwm:emote/" + this.emote.key(), img);
            Minecraft.getInstance().getTextureManager().register(this.id, (AbstractTexture)tex);
            this.frameW = d.frameW;
            this.frameH = d.frameH;
            this.frames = d.frames;
            this.frameEnds = d.ends;
            this.hash = d.hash;
            this.state = State.READY;
            SevenTv.onEmoteLoaded(this.emote.key());
        }
        catch (Throwable t) {
            SevenTv.LOG.warn("Textur-Upload f\u00fcr {} fehlgeschlagen", (Object)this.emote.name(), (Object)t);
            this.state = State.FAILED;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void readFrames(byte[] data, List<BufferedImage> out, List<Integer> delays) throws Exception {
        try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(data));){
            Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (!readers.hasNext()) {
                throw new IllegalStateException("Unbekanntes Bildformat");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(in, false);
                String format = reader.getFormatName().toLowerCase();
                if (!format.equals("gif")) {
                    BufferedImage img = reader.read(0);
                    out.add(EmoteTexture.toArgb(img));
                    delays.add(100);
                    return;
                }
                EmoteTexture.readGif(reader, out, delays);
            }
            finally {
                reader.dispose();
            }
        }
    }

    private static void readGif(ImageReader reader, List<BufferedImage> out, List<Integer> delays) throws Exception {
        int count;
        try {
            count = reader.getNumImages(true);
        }
        catch (Exception e) {
            count = 1;
        }
        int canvasW = -1;
        int canvasH = -1;
        try {
            Node root;
            Node lsd;
            IIOMetadata sm = reader.getStreamMetadata();
            if (sm != null && (lsd = EmoteTexture.child(root = sm.getAsTree("javax_imageio_gif_stream_1.0"), "LogicalScreenDescriptor")) != null) {
                canvasW = EmoteTexture.intAttr(lsd, "logicalScreenWidth", -1);
                canvasH = EmoteTexture.intAttr(lsd, "logicalScreenHeight", -1);
            }
        }
        catch (Exception sm) {
            // empty catch block
        }
        BufferedImage canvas = null;
        BufferedImage restore = null;
        block16: for (int i = 0; i < count; ++i) {
            BufferedImage frame;
            try {
                frame = reader.read(i);
            }
            catch (Exception e) {
                break;
            }
            int fx = 0;
            int fy = 0;
            int delay = 10;
            String disposal = "none";
            try {
                Node gce;
                Node root = reader.getImageMetadata(i).getAsTree("javax_imageio_gif_image_1.0");
                Node desc = EmoteTexture.child(root, "ImageDescriptor");
                if (desc != null) {
                    fx = EmoteTexture.intAttr(desc, "imageLeftPosition", 0);
                    fy = EmoteTexture.intAttr(desc, "imageTopPosition", 0);
                }
                if ((gce = EmoteTexture.child(root, "GraphicControlExtension")) != null) {
                    delay = EmoteTexture.intAttr(gce, "delayTime", 10);
                    Node d = gce.getAttributes().getNamedItem("disposalMethod");
                    if (d != null) {
                        disposal = d.getNodeValue();
                    }
                }
            }
            catch (Exception root) {
                // empty catch block
            }
            if (canvas == null) {
                int w = canvasW > 0 ? canvasW : frame.getWidth() + fx;
                int h = canvasH > 0 ? canvasH : frame.getHeight() + fy;
                canvas = new BufferedImage(w, h, 2);
            }
            if (disposal.equals("restoreToPrevious")) {
                restore = EmoteTexture.copy(canvas);
            }
            Graphics2D g = canvas.createGraphics();
            g.drawImage((Image)frame, fx, fy, null);
            g.dispose();
            out.add(EmoteTexture.copy(canvas));
            delays.add(delay * 10);
            switch (disposal) {
                case "restoreToBackgroundColor": {
                    Graphics2D g2 = canvas.createGraphics();
                    g2.setComposite(AlphaComposite.Clear);
                    g2.fillRect(fx, fy, frame.getWidth(), frame.getHeight());
                    g2.dispose();
                    continue block16;
                }
                case "restoreToPrevious": {
                    if (restore == null) continue block16;
                    canvas = restore;
                    continue block16;
                }
            }
        }
    }

    private static BufferedImage toArgb(BufferedImage img) {
        if (img.getType() == 2) {
            return img;
        }
        BufferedImage c = new BufferedImage(img.getWidth(), img.getHeight(), 2);
        Graphics2D g = c.createGraphics();
        g.drawImage((Image)img, 0, 0, null);
        g.dispose();
        return c;
    }

    private static BufferedImage copy(BufferedImage src) {
        BufferedImage c = new BufferedImage(src.getWidth(), src.getHeight(), 2);
        Graphics2D g = c.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage((Image)src, 0, 0, null);
        g.dispose();
        return c;
    }

    private static Node child(Node parent, String name) {
        if (parent == null) {
            return null;
        }
        for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (!name.equals(n.getNodeName())) continue;
            return n;
        }
        return null;
    }

    private static int intAttr(Node node, String name, int def) {
        NamedNodeMap attrs = node.getAttributes();
        if (attrs == null) {
            return def;
        }
        Node a = attrs.getNamedItem(name);
        if (a == null) {
            return def;
        }
        try {
            return Integer.parseInt(a.getNodeValue());
        }
        catch (NumberFormatException e) {
            return def;
        }
    }

    private static long averageHash(BufferedImage img) {
        BufferedImage small = new BufferedImage(8, 8, 2);
        Graphics2D g = small.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, 8, 8, null);
        g.dispose();
        int[] lum = new int[64];
        long sum = 0L;
        for (int i = 0; i < 64; ++i) {
            int l;
            int p = small.getRGB(i % 8, i / 8);
            int a = p >>> 24 & 0xFF;
            int r = p >> 16 & 0xFF;
            int gg = p >> 8 & 0xFF;
            int b = p & 0xFF;
            lum[i] = l = (r * 299 + gg * 587 + b * 114) / 1000 * a / 255;
            sum += (long)l;
        }
        long avg = sum / 64L;
        long h = 0L;
        for (int i = 0; i < 64; ++i) {
            if ((long)lum[i] <= avg) continue;
            h |= 1L << i;
        }
        return h == 0L ? 1L : h;
    }

    public static enum State {
        LOADING,
        READY,
        FAILED;

    }

    private record Decoded(NativeImage image, int frameW, int frameH, int frames, int[] ends, long hash) {
    }
}

