/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.NativeImage
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.Identifier
 *  net.minecraft.sounds.SoundEvent
 */
package net.friendswithmoney.fwm.ui;

import com.mojang.blaze3d.platform.NativeImage;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.net.ServerContext;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.LauncherGfx;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class IntroScreen
extends Screen {
    private static final int FPS = 20;
    private static final int W = 960;
    private static final int H = 540;
    private static final Identifier SOUND = Identifier.fromNamespaceAndPath((String)"fwm", (String)"intro");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath((String)"fwm", (String)"intro/frame");
    private final Screen parent;
    private final String id;
    private final Identifier soundId;
    private final int frames;
    private final BlockingQueue<Frame> ready = new ArrayBlockingQueue<Frame>(12);
    private volatile boolean stop;
    private DynamicTexture texture;
    private SimpleSoundInstance sound;
    private long startedAt;
    private int shown = -1;
    private static final Map<String, String> TAGS = Map.of("hugo", "2.1", "wind", "wind-1");
    public static int joinCalm;
    public static boolean joinAsked;

    public IntroScreen(Screen parent) {
        this(parent, "hugo");
    }

    public IntroScreen(Screen parent, String id) {
        super((Component)Component.literal((String)"FWM Intro"));
        this.parent = parent;
        this.id = id;
        this.soundId = Identifier.fromNamespaceAndPath((String)"fwm", (String)("hugo".equals(id) ? "intro" : "intro_" + id));
        this.frames = IntroScreen.countFrames(id);
    }

    public static boolean available() {
        return IntroScreen.available("hugo");
    }

    public static boolean available(String id) {
        return IntroScreen.class.getResource(IntroScreen.path(id, 1)) != null;
    }

    private static String path(String id, int index) {
        return "hugo".equals(id) ? String.format("/assets/fwm/intro/%04d.jpg", index) : String.format("/assets/fwm/intro_" + id + "/%04d.jpg", index);
    }

    private static int countFrames(String id) {
        int n = 0;
        while (IntroScreen.class.getResource(IntroScreen.path(id, n + 1)) != null) {
            ++n;
        }
        return n;
    }

    protected void init() {
        if (this.texture != null) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        this.texture = new DynamicTexture(() -> "fwm intro", 960, 540, false);
        client.getTextureManager().register(TEXTURE, (AbstractTexture)this.texture);
        Thread reader = new Thread(this::read, "FWM intro");
        reader.setDaemon(true);
        reader.start();
        this.sound = SimpleSoundInstance.forUI((SoundEvent)SoundEvent.createVariableRangeEvent((Identifier)this.soundId), (float)1.0f, (float)1.0f);
        client.getSoundManager().play((SoundInstance)this.sound);
        this.startedAt = System.currentTimeMillis();
    }

    private void read() {
        for (int i = 1; i <= this.frames && !this.stop; ++i) {
            try (InputStream in = IntroScreen.class.getResourceAsStream(IntroScreen.path(this.id, i));){
                if (in == null) break;
                BufferedImage jpg = ImageIO.read(in);
                if (jpg == null) continue;
                int w = Math.min(960, jpg.getWidth());
                int h = Math.min(540, jpg.getHeight());
                int[] argb = jpg.getRGB(0, 0, w, h, null, 0, w);
                NativeImage image = new NativeImage(960, 540, false);
                for (int y = 0; y < h; ++y) {
                    for (int x = 0; x < w; ++x) {
                        image.setPixel(x, y, 0xFF000000 | argb[y * w + x]);
                    }
                }
                while (!this.stop && !this.ready.offer(new Frame(i, image), 50L, TimeUnit.MILLISECONDS)) {
                }
                if (!this.stop) continue;
                image.close();
                continue;
            }
            catch (Exception e) {
                System.err.println("[FWM] Intro frame " + i + " could not be read: " + String.valueOf(e));
            }
        }
    }

    public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        Frame next;
        ctx.fill(0, 0, this.width, this.height, -16777216);
        int want = 1 + (int)((System.currentTimeMillis() - this.startedAt) * 20L / 1000L);
        if (this.frames == 0 || want > this.frames + 10) {
            this.onClose();
            return;
        }
        while ((next = (Frame)this.ready.peek()) != null && next.index() <= want) {
            this.ready.poll();
            if (this.texture.getPixels() != null) {
                this.texture.getPixels().copyFrom(next.image());
                this.shown = next.index();
            }
            next.image().close();
        }
        if (this.shown > 0) {
            this.texture.upload();
            float s = Math.min((float)this.width / 960.0f, (float)this.height / 540.0f);
            int dw = Math.round(960.0f * s);
            int dh = Math.round(540.0f * s);
            int dx = (this.width - dw) / 2;
            int dy = (this.height - dh) / 2;
            ctx.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, dx, dy, 0.0f, 0.0f, dw, dh, 960, 540, 960, 540);
        }
        String skip = Lang.t("\u00dcberspringen  \u203a", "Skip  \u203a");
        int bw = this.font.width(skip) + 16;
        int bx = this.width - bw - 12;
        int by = this.height - 28;
        boolean over = mouseX >= bx && mouseX < bx + bw && mouseY >= by && mouseY < by + 18;
        LauncherGfx.round(ctx, bx, by, bw, 18, 6, over ? -864161594 : -1728053248);
        LauncherGfx.ring(ctx, bx, by, bw, 18, 6, -2005012282);
        ctx.text(this.font, skip, bx + 8, by + 5, over ? -16045536 : -1379602, false);
        float done = Math.min(1.0f, (float)(want - 1) / (float)Math.max(1, this.frames));
        ctx.fill(0, this.height - 2, Math.round((float)this.width * done), this.height, -864161594);
    }

    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        String skip = Lang.t("\u00dcberspringen  \u203a", "Skip  \u203a");
        int bw = this.font.width(skip) + 16;
        int bx = this.width - bw - 12;
        int by = this.height - 28;
        if (click.x() >= (double)bx && click.x() < (double)(bx + bw) && click.y() >= (double)by && click.y() < (double)(by + 18)) {
            this.onClose();
            return true;
        }
        return true;
    }

    public boolean keyPressed(KeyEvent input) {
        if (input.key() == 256 || input.key() == 32 || input.key() == 257) {
            this.onClose();
            return true;
        }
        return true;
    }

    public void onClose() {
        Frame f;
        if (this.stop) {
            return;
        }
        this.stop = true;
        Minecraft client = Minecraft.getInstance();
        if (this.sound != null) {
            client.getSoundManager().stop((SoundInstance)this.sound);
        }
        while ((f = (Frame)this.ready.poll()) != null) {
            f.image().close();
        }
        client.getTextureManager().release(TEXTURE);
        client.gui.setScreen(this.parent);
    }

    public boolean isPauseScreen() {
        return false;
    }

    public boolean shouldCloseOnEsc() {
        return true;
    }

    public static String forServer(ServerContext.Server server) {
        if (server == ServerContext.Server.HUGO) {
            return "hugo";
        }
        if (server == ServerContext.Server.WIND) {
            return "wind";
        }
        if (server == ServerContext.Server.DONUT) {
            return "donut";
        }
        return null;
    }

    public static boolean seen(String id) {
        String tag = TAGS.getOrDefault(id, "1");
        V2 v2 = V2.INSTANCE;
        if ("hugo".equals(id) && "2.1".equals(tag) && v2.introSeen != null && v2.introSeen.startsWith("2.1")) {
            return true;
        }
        String all = v2.introSeenServers == null ? "" : v2.introSeenServers;
        return Arrays.asList(all.split(",")).contains(id + ":" + tag);
    }

    public static void markSeen(String id) {
        V2 v2 = V2.INSTANCE;
        String entry = id + ":" + TAGS.getOrDefault(id, "1");
        String all = v2.introSeenServers == null ? "" : v2.introSeenServers;
        v2.introSeenServers = all.isEmpty() ? entry : all + "," + entry;
        V2.save();
    }

    public static boolean blocking(Object screen) {
        if (screen == null) {
            return false;
        }
        if (screen instanceof IntroScreen) {
            return true;
        }
        String n = screen.getClass().getName();
        return n.contains("Terrain") || n.contains("Loading") || n.contains("Progress") || n.contains("Confirm") || n.contains("Connect") || n.contains("ReceivingLevel") || n.contains("Download") || n.contains("Reload");
    }

    public static String forMenu() {
        String id = IntroScreen.forServer(ServerContext.current());
        return id != null && IntroScreen.available(id) ? id : "hugo";
    }

    private record Frame(int index, NativeImage image) {
    }
}

