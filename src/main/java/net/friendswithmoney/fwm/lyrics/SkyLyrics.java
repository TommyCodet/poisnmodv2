/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
 *  net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.KeyMapping$Category
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.chat.Component
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.friendswithmoney.fwm.lyrics;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.lyrics.AfkCinema;
import net.friendswithmoney.fwm.lyrics.GenreTime;
import net.friendswithmoney.fwm.lyrics.GenreWeather;
import net.friendswithmoney.fwm.lyrics.LyricsConfig;
import net.friendswithmoney.fwm.lyrics.LyricsTime;
import net.friendswithmoney.fwm.lyrics.MediaWatcher;
import net.friendswithmoney.fwm.lyrics.MusicFx;
import net.friendswithmoney.fwm.lyrics.PvpLyrics;
import net.friendswithmoney.fwm.lyrics.SkyText;
import net.friendswithmoney.fwm.lyrics.SongOffsets;
import net.friendswithmoney.fwm.lyrics.T;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SkyLyrics {
    public static final Logger LOG = LoggerFactory.getLogger((String)"FWM Sky Lyrics");
    public static LyricsConfig cfg;
    private static boolean skyBroken;
    private static boolean fxBroken;
    private static boolean running;
    private static KeyMapping toggleSky;
    private static KeyMapping togglePill;
    private static KeyMapping later;
    private static KeyMapping earlier;
    private static KeyMapping playPause;
    private static KeyMapping nextSong;
    private static KeyMapping prevSong;

    private SkyLyrics() {
    }

    public static boolean active() {
        return cfg != null && MediaWatcher.supported() && Features.isOn("fwm.tab.skylyrics") && SkyLyrics.cfg.consent != null && !SkyLyrics.cfg.consent.isEmpty();
    }

    public static boolean online() {
        return cfg != null && "online".equals(SkyLyrics.cfg.consent);
    }

    public static void init() {
        cfg = LyricsConfig.load();
        KeyMapping.Category cat = KeyMapping.Category.MISC;
        int none = InputConstants.UNKNOWN.getValue();
        toggleSky = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("FWM: Sky Lyrics im Himmel an/aus", InputConstants.Type.KEYSYM, none, cat));
        togglePill = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("FWM: Sky Lyrics Leiste an/aus", InputConstants.Type.KEYSYM, none, cat));
        earlier = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("FWM: Lyrics fr\u00fcher (-250 ms)", InputConstants.Type.KEYSYM, none, cat));
        later = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("FWM: Lyrics sp\u00e4ter (+250 ms)", InputConstants.Type.KEYSYM, none, cat));
        playPause = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("FWM: Musik Play/Pause", InputConstants.Type.KEYSYM, none, cat));
        nextSong = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("FWM: Musik n\u00e4chster Song", InputConstants.Type.KEYSYM, none, cat));
        prevSong = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("FWM: Musik vorheriger Song", InputConstants.Type.KEYSYM, none, cat));
        ClientTickEvents.END_CLIENT_TICK.register(SkyLyrics::tick);
        PvpLyrics.register();
        AfkCinema.init();
        LevelRenderEvents.AFTER_SOLID_FEATURES.register(ctx -> {
            if (skyBroken || !SkyLyrics.active()) {
                return;
            }
            try {
                SkyText.renderWorld(ctx);
            }
            catch (Throwable t) {
                skyBroken = true;
                LOG.error("Sky Lyrics: Himmelstext wegen Fehler abgeschaltet", t);
            }
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(c -> MediaWatcher.stop());
        Runtime.getRuntime().addShutdownHook(new Thread(MediaWatcher::stop, "FWM-SkyLyrics-Stop"));
    }

    private static void syncRunning() {
        boolean want;
        SkyLyrics.cfg.enabled = want = SkyLyrics.active();
        if (want != running) {
            running = want;
            if (want) {
                MediaWatcher.start();
            } else {
                MediaWatcher.stop();
                SkyText.reset();
            }
        }
    }

    public static void setEnabled(boolean on) {
        SkyLyrics.cfg.enabled = on;
        cfg.save();
        SkyLyrics.syncRunning();
    }

    private static void tick(Minecraft mc) {
        SkyLyrics.syncRunning();
        if (!running) {
            GenreWeather.tick(mc);
            GenreTime.tick(mc);
            return;
        }
        try {
            GenreWeather.tick(mc);
            GenreTime.tick(mc);
        }
        catch (Throwable t) {
            LOG.error("Sky Lyrics: Genre-Wetter Fehler", t);
        }
        try {
            PvpLyrics.tick(mc);
        }
        catch (Throwable t) {
            LOG.error("Sky Lyrics: PvP-Lyrics Fehler", t);
        }
        if (!fxBroken) {
            try {
                MusicFx.tick(mc);
            }
            catch (Throwable t) {
                fxBroken = true;
                LOG.error("Sky Lyrics: Musik-Effekte wegen Fehler abgeschaltet", t);
            }
        }
        while (toggleSky.consumeClick()) {
            SkyLyrics.cfg.skyEnabled = !SkyLyrics.cfg.skyEnabled;
            cfg.save();
            SkyLyrics.msg(mc, T.tr("Lyrics im Himmel ") + (SkyLyrics.cfg.skyEnabled ? T.tr("\u00a7aan") : T.tr("\u00a7caus")));
        }
        while (togglePill.consumeClick()) {
            SkyLyrics.cfg.pillEnabled = !SkyLyrics.cfg.pillEnabled;
            cfg.save();
            SkyLyrics.msg(mc, "Now-Playing " + (SkyLyrics.cfg.pillEnabled ? T.tr("\u00a7aan") : T.tr("\u00a7caus")));
        }
        while (later.consumeClick()) {
            SkyLyrics.shift(mc, 250);
        }
        while (earlier.consumeClick()) {
            SkyLyrics.shift(mc, -250);
        }
        while (playPause.consumeClick()) {
            MediaWatcher.command("toggle");
        }
        while (nextSong.consumeClick()) {
            MediaWatcher.command("next");
            SkyLyrics.msg(mc, T.tr("N\u00e4chster Song"));
        }
        while (prevSong.consumeClick()) {
            MediaWatcher.command("prev");
            SkyLyrics.msg(mc, T.tr("Vorheriger Song"));
        }
    }

    public static void shift(Minecraft mc, int ms) {
        MediaWatcher.Track track = MediaWatcher.track();
        if (track != null) {
            SongOffsets.add(track.key(), ms);
        } else {
            SkyLyrics.cfg.offsetMs = Math.max(-10000, Math.min(10000, SkyLyrics.cfg.offsetMs + ms));
            cfg.save();
        }
        int off = LyricsTime.offset(track);
        SkyLyrics.msg(mc, T.tr("Lyrics-Versatz") + (track != null ? T.tr(" (dieser Song)") : "") + ": \u00a7f" + (off > 0 ? "+" : "") + off + " ms");
    }

    private static void msg(Minecraft mc, String s) {
        if (mc.player != null) {
            mc.player.sendOverlayMessage((Component)Component.literal((String)("\u00a7a[FWM Sky Lyrics] \u00a77" + s)));
        }
    }
}

