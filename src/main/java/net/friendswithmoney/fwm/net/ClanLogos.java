/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.NativeImage
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.client.Minecraft
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.packs.repository.PackRepository
 */
package net.friendswithmoney.fwm.net;

import com.mojang.blaze3d.platform.NativeImage;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import net.fabricmc.loader.api.FabricLoader;
import net.friendswithmoney.fwm.antileak.AntiLeakPack;
import net.friendswithmoney.fwm.config.ConfigManager;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.net.FwmApi;
import net.friendswithmoney.fwm.net.TrustedClans;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.repository.PackRepository;

public final class ClanLogos {
    public static final String FOLDER = "FWM Clan-Logos";
    private static final String PACK_ID = "file/FWM Clan-Logos";
    private static final Map<String, Long> READY = new ConcurrentHashMap<String, Long>();
    private static final Set<String> LOADING = ConcurrentHashMap.newKeySet();
    private static volatile Set<String> active = Set.of();
    private static boolean checkedAtStart;
    private static volatile long nextIcon;

    private ClanLogos() {
    }

    private static Path cacheDir() {
        return FabricLoader.getInstance().getConfigDir().resolve("fwm-clan-logos");
    }

    public static Identifier font(String id) {
        if (id == null) {
            return null;
        }
        if (active.contains(id)) {
            return Identifier.fromNamespaceAndPath((String)"fwm", (String)("clan_" + id));
        }
        if ("mrket".equals(id)) {
            return Identifier.fromNamespaceAndPath((String)"fwm", (String)"market");
        }
        return null;
    }

    private static long versionOf(String etag) {
        if (etag == null || etag.isBlank()) {
            return 0L;
        }
        long h = 1125899906842597L;
        for (int i = 0; i < etag.length(); ++i) {
            h = 31L * h + (long)etag.charAt(i);
        }
        return h & Long.MAX_VALUE | 1L;
    }

    private static byte[] shrink(byte[] png) throws IOException {
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(png));
        if (img == null) {
            return null;
        }
        int target = 30;
        BufferedImage cur = img;
        int w = img.getWidth();
        int h = img.getHeight();
        int side = Math.max(w, h);
        BufferedImage square = new BufferedImage(side, side, 2);
        Graphics2D g0 = square.createGraphics();
        g0.drawImage((Image)cur, (side - w) / 2, (side - h) / 2, null);
        g0.dispose();
        cur = square;
        while (cur.getWidth() / 2 >= target) {
            int n = cur.getWidth() / 2;
            BufferedImage next = new BufferedImage(n, n, 2);
            Graphics2D g = next.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(cur, 0, 0, n, n, null);
            g.dispose();
            cur = next;
        }
        BufferedImage out = new BufferedImage(target, target, 2);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(cur, 0, 0, target, target, null);
        g.dispose();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write((RenderedImage)out, "png", bytes);
        return bytes.toByteArray();
    }

    static void tick(Minecraft client, List<TrustedClans.Clan> clanList, List<TrustedClans.Player> playerList) {
        String string;
        ArrayList<Logo> clans = new ArrayList<Logo>();
        for (TrustedClans.Clan clan : clanList) {
            if (clan.iconUrl != null) {
                clans.add(new Logo(clan.id, clan.iconUrl, ClanLogos.versionOf(clan.iconEtag), clan.iconEtag));
                continue;
            }
            clans.add(new Logo(clan.id, clan.logo, clan.logoVersion, null));
        }
        for (TrustedClans.Player player : playerList) {
            if (player.uuid == null) continue;
            clans.add(new Logo("p_" + player.uuid, player.logo, player.logoVersion, null));
        }
        for (Logo logo : clans) {
            Long have;
            if (logo.logoVersion() <= 0L || logo.logo() == null || logo.logo().isBlank() || logo.id() == null || !logo.id().matches("[a-z0-9_-]{2,24}|p_[0-9a-f]{32}") || (have = READY.get(logo.id())) != null && have.longValue() == logo.logoVersion()) continue;
            Path file = ClanLogos.cacheDir().resolve(logo.id() + "-" + logo.logoVersion() + ".png");
            if (Files.exists(file, new LinkOption[0])) {
                READY.put(logo.id(), logo.logoVersion());
                continue;
            }
            boolean bot = logo.logo().startsWith("http");
            if (bot && System.currentTimeMillis() < nextIcon || !LOADING.add(logo.id())) continue;
            String logoId = logo.id();
            long version = logo.logoVersion();
            if (bot) {
                nextIcon = System.currentTimeMillis() + 1000L;
                Path etagFile = ClanLogos.cacheDir().resolve(logoId + ".etag");
                String had = null;
                try {
                    if (Files.exists(etagFile, new LinkOption[0])) {
                        had = Files.readString(etagFile, StandardCharsets.UTF_8).trim();
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
                String sent = had;
                FwmApi.request("GET", logo.logo(), sent, null, null, 0x200000L).whenComplete((got, error) -> {
                    try {
                        if (error != null) {
                            return;
                        }
                        if (got.status() == 429) {
                            nextIcon = System.currentTimeMillis() + Math.max(60L, got.retryAfterSeconds()) * 1000L;
                            return;
                        }
                        Files.createDirectories(ClanLogos.cacheDir(), new FileAttribute[0]);
                        Path last = ClanLogos.cacheDir().resolve(logoId + "-last.png");
                        if (got.status() == 304 && Files.exists(last, new LinkOption[0])) {
                            Files.copy(last, file, StandardCopyOption.REPLACE_EXISTING);
                            READY.put(logoId, version);
                            return;
                        }
                        byte[] bytes = got.body();
                        if (got.status() != 200 || bytes == null || bytes.length < 8 || (bytes[0] & 0xFF) != 137 || bytes[1] != 80) {
                            return;
                        }
                        byte[] small = ClanLogos.shrink(bytes);
                        if (small == null) {
                            return;
                        }
                        Files.write(file, small, new OpenOption[0]);
                        Files.write(last, small, new OpenOption[0]);
                        if (got.etag() != null) {
                            Files.writeString(etagFile, (CharSequence)got.etag(), StandardCharsets.UTF_8, new OpenOption[0]);
                        }
                        READY.put(logoId, version);
                    }
                    catch (Exception exception) {
                    }
                    finally {
                        LOADING.remove(logoId);
                    }
                });
                continue;
            }
            String url = FwmConfig.INSTANCE.apiBaseUrl.replaceAll("/+$", "") + logo.logo();
            FwmApi.getBytes(url).whenComplete((bytes, error) -> {
                try {
                    if (error == null && bytes != null && ((byte[])bytes).length > 8 && ((byte[])bytes).length < 64000 && (bytes[0] & 0xFF) == 137 && bytes[1] == 80) {
                        Files.createDirectories(ClanLogos.cacheDir(), new FileAttribute[0]);
                        Files.write(file, bytes, new OpenOption[0]);
                        READY.put(logoId, version);
                    }
                }
                catch (Exception exception) {
                }
                finally {
                    LOADING.remove(logoId);
                }
            });
        }
        if (client.gui.overlay() != null || client.gui.screen() != null) {
            return;
        }
        TreeMap<String, Long> wanted = new TreeMap<String, Long>();
        for (Logo c : clans) {
            Long have = READY.get(c.id());
            if (have == null || c.logoVersion() <= 0L || have.longValue() != c.logoVersion()) continue;
            wanted.put(c.id(), have);
        }
        String string2 = string = wanted.isEmpty() ? "" : "v2|" + AntiLeakPack.packFormat() + "|" + String.valueOf(wanted);
        if (!checkedAtStart) {
            checkedAtStart = true;
            if (string.equals(FwmConfig.INSTANCE.clanLogosApplied) && Files.isDirectory(ClanLogos.folder(client), new LinkOption[0])) {
                active = Set.copyOf(wanted.keySet());
                return;
            }
        }
        if (string.equals(FwmConfig.INSTANCE.clanLogosApplied)) {
            return;
        }
        if (!wanted.isEmpty() && wanted.size() < ClanLogos.countWithLogo(clans)) {
            return;
        }
        ClanLogos.apply(client, wanted, string);
    }

    private static int countWithLogo(List<Logo> clans) {
        int n = 0;
        for (Logo c : clans) {
            if (c.logoVersion() <= 0L) continue;
            ++n;
        }
        return n;
    }

    private static void copyCropped(Path from, Path to) throws IOException {
        try (InputStream in = Files.newInputStream(from, new OpenOption[0]);
             NativeImage img = NativeImage.read((InputStream)in);){
            int y;
            int w = img.getWidth();
            int h = img.getHeight();
            int left = w;
            int right = -1;
            block19: for (int x = 0; x < w; ++x) {
                for (y = 0; y < h; ++y) {
                    if (img.getPixel(x, y) >>> 24 <= 16) continue;
                    left = Math.min(left, x);
                    right = Math.max(right, x);
                    continue block19;
                }
            }
            if (right < 0 || left == 0 && right == w - 1) {
                Files.copy(from, to, new CopyOption[0]);
                return;
            }
            try (NativeImage out = new NativeImage(right - left + 1, h, true);){
                for (y = 0; y < h; ++y) {
                    for (int x = left; x <= right; ++x) {
                        out.setPixel(x - left, y, img.getPixel(x, y));
                    }
                }
                out.writeToFile(to);
            }
        }
        catch (RuntimeException e) {
            Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static Path folder(Minecraft client) {
        return client.getResourcePackDirectory().resolve(FOLDER);
    }

    private static void apply(Minecraft client, Map<String, Long> wanted, String key) {
        Path dir = ClanLogos.folder(client);
        PackRepository manager = client.getResourcePackRepository();
        boolean wasEnabled = manager.getSelectedIds().contains(PACK_ID);
        boolean on = !wanted.isEmpty();
        try {
            AntiLeakPack.delete(dir);
            if (on) {
                Path fonts = dir.resolve("assets/fwm/font");
                Path pics = dir.resolve("assets/fwm/textures/font");
                Files.createDirectories(fonts, new FileAttribute[0]);
                Files.createDirectories(pics, new FileAttribute[0]);
                Files.writeString(dir.resolve("pack.mcmeta"), (CharSequence)AntiLeakPack.mcmeta(), StandardCharsets.UTF_8, new OpenOption[0]);
                for (Map.Entry<String, Long> e : wanted.entrySet()) {
                    String id = e.getKey();
                    ClanLogos.copyCropped(ClanLogos.cacheDir().resolve(id + "-" + String.valueOf(e.getValue()) + ".png"), pics.resolve("clan_" + id + ".png"));
                    Files.writeString(fonts.resolve("clan_" + id + ".json"), (CharSequence)("{\"providers\":[{\"type\":\"bitmap\",\"file\":\"fwm:font/clan_" + id + ".png\",\"ascent\":8,\"height\":10,\"chars\":[\"\\ue000\"]}]}"), StandardCharsets.UTF_8, new OpenOption[0]);
                }
                manager.reload();
                if (!wasEnabled && !manager.addPack(PACK_ID)) {
                    System.err.println("[FWM] Clan logo pack not found after writing it: " + String.valueOf(dir));
                    return;
                }
            } else {
                manager.removePack(PACK_ID);
            }
            client.options.updateResourcePacks(manager);
            if (on && wasEnabled) {
                client.reloadResourcePacks();
            }
            active = Set.copyOf(wanted.keySet());
            FwmConfig.INSTANCE.clanLogosApplied = key;
            ConfigManager.save();
        }
        catch (Exception e) {
            System.err.println("[FWM] Clan logos could not be applied: " + String.valueOf(e));
        }
    }

    private record Logo(String id, String logo, long logoVersion, String etag) {
    }
}

