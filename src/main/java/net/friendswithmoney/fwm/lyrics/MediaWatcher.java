/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package net.friendswithmoney.fwm.lyrics;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Locale;
import net.friendswithmoney.fwm.lyrics.BeatSync;
import net.friendswithmoney.fwm.lyrics.LyricsConfig;
import net.friendswithmoney.fwm.lyrics.SkyLyrics;
import net.friendswithmoney.fwm.lyrics.WinMedia;

public final class MediaWatcher {
    private static volatile Track track;
    private static volatile boolean playing;
    private static volatile boolean active;
    private static volatile String coverPath;
    private static volatile int coverId;
    private static volatile long basePosMs;
    private static volatile long baseNanos;
    private static volatile long lastMsgNanos;
    private static Process process;
    private static Thread reader;
    private static volatile boolean stopping;
    private static volatile long holdPlayingUntil;
    private static volatile long holdPosUntil;
    private static volatile boolean forceSnap;
    private static volatile long lastDriftMs;
    private static volatile long lastFixNanos;
    private static volatile long lastFixMs;
    private static volatile String syncSource;
    private static volatile float volume;
    private static volatile long holdVolUntil;
    private static volatile long lastVolSendNanos;

    private MediaWatcher() {
    }

    public static boolean supported() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    public static synchronized void start() {
        if (!MediaWatcher.supported()) {
            SkyLyrics.LOG.warn("Sky Lyrics: Song-Erkennung gibt es nur unter Windows.");
        } else {
            stopping = false;
            if (WinMedia.start()) {
                SkyLyrics.LOG.info("Sky Lyrics: Medienanzeige direkt \u00fcber WinRT verbunden (kein PowerShell)");
                return;
            }
            reader = new Thread(MediaWatcher::runLoop, "SkyLyrics-Media");
            reader.setDaemon(true);
            reader.start();
        }
    }

    public static String sourceApp() {
        int dot;
        String s;
        String a = WinMedia.sourceApp;
        if (a == null || a.isEmpty()) {
            return "";
        }
        int bang = a.indexOf(33);
        String string = s = bang > 0 ? a.substring(bang + 1) : a;
        if (s.toLowerCase(Locale.ROOT).endsWith(".exe")) {
            s = s.substring(0, s.length() - 4);
        }
        if ((dot = s.lastIndexOf(46)) > 0 && dot < s.length() - 1 && !s.contains(" ")) {
            s = s.substring(dot + 1);
        }
        return s;
    }

    public static synchronized void stop() {
        stopping = true;
        WinMedia.stop();
        active = false;
        if (process != null) {
            process.destroyForcibly();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Converted monitor instructions to comments
     * Lifted jumps to return sites
     */
    private static void runLoop() {
        int var0 = 2000;
        while (!stopping) {
            long var1 = System.currentTimeMillis();
            try {
                Path var3 = MediaWatcher.extractScript();
                Path var4 = LyricsConfig.DIR.resolve("cover");
                Files.createDirectories(var4, new FileAttribute[0]);
                ProcessBuilder var5 = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-File", var3.toString(), "-CoverDir", var4.toString(), "-ParentPid", String.valueOf(ProcessHandle.current().pid()), "-CmdDir", MediaWatcher.cmdDir().toString(), "-OnlyMusic", SkyLyrics.cfg != null && !SkyLyrics.cfg.onlyMusicApps ? "0" : "1", "-AllowBrowser", SkyLyrics.cfg != null && SkyLyrics.cfg.allowBrowser ? "1" : "0");
                var5.redirectError(ProcessBuilder.Redirect.to(LyricsConfig.DIR.resolve("media-error.log").toFile()));
                Class<MediaWatcher> clazz = MediaWatcher.class;
                // MONITORENTER : net.friendswithmoney.fwm.lyrics.MediaWatcher.class
                process = var5.start();
                // MONITOREXIT : clazz
                try (BufferedReader var7 = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));){
                    String var15;
                    while ((var15 = var7.readLine()) != null && !stopping) {
                        MediaWatcher.handle(var15);
                    }
                }
            }
            catch (Exception var14) {
                SkyLyrics.LOG.warn("Sky Lyrics: Medien-Skript Fehler", (Throwable)var14);
            }
            active = false;
            if (stopping) {
                return;
            }
            if (System.currentTimeMillis() - var1 > 60000L) {
                var0 = 2000;
            }
            try {
                Thread.sleep(var0);
            }
            catch (InterruptedException var12) {
                return;
            }
            var0 = Math.min(var0 * 2, 60000);
        }
    }

    private static Path extractScript() throws Exception {
        Files.createDirectories(LyricsConfig.DIR, new FileAttribute[0]);
        Path var0 = LyricsConfig.DIR.resolve("media.ps1");
        try (InputStream var1 = MediaWatcher.class.getResourceAsStream("/assets/fwm/skylyrics/media.ps1");){
            if (var1 == null) {
                throw new IllegalStateException("media.ps1 fehlt in der Mod");
            }
            Files.copy(var1, var0, StandardCopyOption.REPLACE_EXISTING);
        }
        return var0;
    }

    static void handle(String var0) {
        if ((var0 = var0.replace("\ufeff", "").trim()).startsWith("{")) {
            long var2;
            JsonObject var1;
            try {
                var1 = JsonParser.parseString((String)var0).getAsJsonObject();
            }
            catch (Exception var22) {
                return;
            }
            lastMsgNanos = var2 = System.nanoTime();
            if (var1.has("active") && var1.get("active").getAsBoolean()) {
                int var19;
                boolean var15;
                String var4 = MediaWatcher.str(var1, "title");
                String var5 = MediaWatcher.str(var1, "artist");
                String var6 = MediaWatcher.str(var1, "album");
                String var7 = MediaWatcher.str(var1, "app");
                String var8 = MediaWatcher.str(var1, "genres");
                long var9 = var1.has("dur") ? var1.get("dur").getAsLong() : 0L;
                long var11 = var1.has("pos") ? var1.get("pos").getAsLong() : 0L;
                boolean var13 = var1.has("playing") && var1.get("playing").getAsBoolean();
                Track var14 = track;
                boolean bl = var15 = var14 == null || !var14.title.equals(var4) || !var14.artist.equals(var5);
                if (var15 || var14.durationMs != var9 || !var14.genres.equals(var8)) {
                    track = new Track(var4, var5, var6, var7, var9, var8);
                }
                long var16 = MediaWatcher.positionMs();
                syncSource = MediaWatcher.str(var1, "src");
                if (var1.has("seekFail") && var1.get("seekFail").getAsBoolean()) {
                    holdPosUntil = 0L;
                }
                try {
                    BeatSync.update(var1.has("bpm") ? var1.get("bpm").getAsDouble() : 0.0, var1.has("beat") ? var1.get("beat").getAsLong() : -1L, var1.has("lvl") ? var1.get("lvl").getAsDouble() : 0.0, var2);
                }
                catch (Exception exception) {
                    // empty catch block
                }
                if (var1.has("vol") && System.nanoTime() > holdVolUntil) {
                    try {
                        volume = var1.get("vol").getAsFloat();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                if (!var15 && var13 && playing && System.nanoTime() >= holdPosUntil) {
                    lastDriftMs = var11 - var16;
                } else if (var15) {
                    lastDriftMs = 0L;
                }
                if (forceSnap) {
                    forceSnap = false;
                    if (!var15 && Math.abs(var11 - var16) > 150L) {
                        lastFixNanos = var2;
                        lastFixMs = var11 - var16;
                    }
                    basePosMs = var11;
                    baseNanos = var2;
                    holdPosUntil = 0L;
                } else if (var15 || System.nanoTime() >= holdPosUntil || Math.abs(var16 - var11) <= 1500L) {
                    if (!var15 && var13 && var13 == playing && Math.abs(var16 - var11) <= 1200L) {
                        long var18;
                        basePosMs = var18 = var16 + (var11 - var16) / 8L;
                        baseNanos = var2;
                    } else {
                        if (!var15 && var13 && playing && Math.abs(var11 - var16) > 1200L) {
                            lastFixNanos = var2;
                            lastFixMs = var11 - var16;
                        }
                        basePosMs = var11;
                        baseNanos = var2;
                    }
                }
                if (System.nanoTime() > holdPlayingUntil) {
                    playing = var13;
                }
                active = !var4.isEmpty();
                String var24 = MediaWatcher.str(var1, "cover");
                int n = var19 = var1.has("coverId") ? var1.get("coverId").getAsInt() : -1;
                if (var15) {
                    coverPath = "";
                    coverId = -1;
                }
                if (!var24.isEmpty() && var19 != coverId) {
                    coverPath = var24;
                    coverId = var19;
                }
            } else {
                active = false;
                playing = false;
            }
        }
    }

    private static String str(JsonObject var0, String var1) {
        return var0.has(var1) && !var0.get(var1).isJsonNull() ? var0.get(var1).getAsString().trim() : "";
    }

    private static Path cmdDir() throws IOException {
        Path var0 = LyricsConfig.DIR.resolve("cmd");
        Files.createDirectories(var0, new FileAttribute[0]);
        return var0;
    }

    public static void command(String var0) {
        if (MediaWatcher.supported() && WinMedia.running()) {
            WinMedia.command(var0);
            if ("toggle".equals(var0) && active) {
                basePosMs = MediaWatcher.positionMs();
                baseNanos = System.nanoTime();
                playing = !playing;
                holdPlayingUntil = System.nanoTime() + 900000000L;
            }
        } else if (MediaWatcher.supported()) {
            try {
                Path var1 = MediaWatcher.cmdDir();
                Path var2 = var1.resolve("tmp_" + System.nanoTime() + ".part");
                Files.writeString(var2, (CharSequence)var0, StandardCharsets.UTF_8, new OpenOption[0]);
                Files.move(var2, var1.resolve("cmd_" + System.nanoTime() + ".txt"), StandardCopyOption.ATOMIC_MOVE);
                if ("toggle".equals(var0) && active) {
                    basePosMs = MediaWatcher.positionMs();
                    baseNanos = System.nanoTime();
                    playing = !playing;
                    holdPlayingUntil = System.nanoTime() + 900000000L;
                }
            }
            catch (Exception var3) {
                SkyLyrics.LOG.warn("Sky Lyrics: Medienbefehl fehlgeschlagen", (Throwable)var3);
            }
        }
    }

    public static void resync() {
        forceSnap = true;
        holdPosUntil = 0L;
        MediaWatcher.command("sync");
    }

    public static long driftMs() {
        return lastDriftMs;
    }

    public static long sinceFixMs() {
        return lastFixNanos == 0L ? Long.MAX_VALUE : (System.nanoTime() - lastFixNanos) / 1000000L;
    }

    public static long lastFixMs() {
        return lastFixMs;
    }

    public static String syncSource() {
        return syncSource;
    }

    public static float volume() {
        return active ? volume : -1.0f;
    }

    public static void setVolume(float var0, boolean var1) {
        volume = var0 = Math.max(0.0f, Math.min(1.0f, var0));
        holdVolUntil = System.nanoTime() + 2500000000L;
        long var2 = System.nanoTime();
        if (var1 || var2 - lastVolSendNanos > 120000000L) {
            lastVolSendNanos = var2;
            MediaWatcher.command("vol:" + String.format(Locale.ROOT, "%.3f", Float.valueOf(var0)));
        }
    }

    public static void seek(long var0) {
        Track var2 = track;
        if (var2 != null && var2.durationMs() > 0L) {
            var0 = Math.max(0L, Math.min(var2.durationMs() - 500L, var0));
        }
        MediaWatcher.command("seek:" + Math.max(0L, var0));
        basePosMs = Math.max(0L, var0);
        baseNanos = System.nanoTime();
        holdPosUntil = System.nanoTime() + 3500000000L;
    }

    public static Track track() {
        return active ? track : null;
    }

    public static boolean isPlaying() {
        return active && playing;
    }

    public static String coverPath() {
        return coverPath;
    }

    public static int coverId() {
        return coverId;
    }

    public static boolean alive() {
        return System.nanoTime() - lastMsgNanos < 5000000000L;
    }

    public static long positionMs() {
        Track var2;
        long var0 = basePosMs;
        if (playing) {
            var0 += (System.nanoTime() - baseNanos) / 1000000L;
        }
        if ((var2 = track) != null && var2.durationMs > 0L) {
            var0 = Math.min(var0, var2.durationMs);
        }
        return Math.max(0L, var0);
    }

    static {
        coverPath = "";
        coverId = -1;
        syncSource = "";
        volume = -1.0f;
    }

    public record Track(String title, String artist, String album, String app, long durationMs, String genres) {
        public String key() {
            return this.artist + "|" + this.title;
        }
    }
}

