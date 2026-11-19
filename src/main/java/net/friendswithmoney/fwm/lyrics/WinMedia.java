/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.sun.jna.Function
 *  com.sun.jna.Library
 *  com.sun.jna.Native
 *  com.sun.jna.Pointer
 *  com.sun.jna.WString
 *  com.sun.jna.platform.win32.Guid$GUID
 *  com.sun.jna.platform.win32.Ole32
 *  com.sun.jna.ptr.ByteByReference
 *  com.sun.jna.ptr.FloatByReference
 *  com.sun.jna.ptr.IntByReference
 *  com.sun.jna.ptr.LongByReference
 *  com.sun.jna.ptr.PointerByReference
 */
package net.friendswithmoney.fwm.lyrics;

import com.google.gson.JsonObject;
import com.sun.jna.Function;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.friendswithmoney.fwm.lyrics.LyricsConfig;
import net.friendswithmoney.fwm.lyrics.MediaWatcher;
import net.friendswithmoney.fwm.lyrics.SkyLyrics;

final class WinMedia {
    private static Combase rt;
    private static final Guid.GUID IID_MANAGER_STATICS;
    private static final Guid.GUID IID_ASYNC_INFO;
    private static final Guid.GUID IID_RANDOM_ACCESS_STREAM;
    private static final Guid.GUID IID_INPUT_STREAM;
    private static final Guid.GUID IID_BUFFER_FACTORY;
    private static final Guid.GUID IID_BUFFER_BYTE_ACCESS;
    private static final Guid.GUID IID_BUFFER;
    private static final Guid.GUID CLSID_MM_DEVICE_ENUMERATOR;
    private static final Guid.GUID IID_MM_DEVICE_ENUMERATOR;
    private static final Guid.GUID IID_AUDIO_SESSION_MANAGER2;
    private static final Guid.GUID IID_AUDIO_SESSION_CONTROL2;
    private static final Guid.GUID IID_SIMPLE_AUDIO_VOLUME;
    private static final Guid.GUID IID_AUDIO_METER;
    private static volatile boolean running;
    private static Thread mediaThread;
    private static Thread beatThread;
    private static final ConcurrentLinkedQueue<String> COMMANDS;
    private static Pointer manager;
    private static long clkPos;
    private static long clkAt;
    private static volatile boolean clkPlaying;
    private static String clkKey;
    private static long seekAt;
    private static boolean seekFail;
    private static long lastUpd;
    private static double lastRaw;
    private static boolean forceSync;
    private static String lastKey;
    private static int coverN;
    private static int coverTries;
    private static int coverWait;
    private static String coverPath;
    private static float vol;
    private static int volTick;
    static volatile String audioKey;
    private static int propsTick;
    private static String propsApp;
    private static String cachedTitle;
    private static String cachedArtist;
    private static String cachedAlbum;
    private static String cachedGenres;
    private static String lastMusicApp;
    static volatile String sourceApp;
    private static final String[] MUSIC_APPS;
    private static final String[] BROWSERS;
    private static final long T0;

    WinMedia() {
    }

    private static int call(Pointer obj, int index, Object ... args) {
        Pointer vtable = obj.getPointer(0L);
        Function f = Function.getFunction((Pointer)vtable.getPointer((long)index * (long)Native.POINTER_SIZE), (int)63);
        Object[] all = new Object[args.length + 1];
        all[0] = obj;
        System.arraycopy(args, 0, all, 1, args.length);
        return f.invokeInt(all);
    }

    private static void release(Pointer p) {
        if (p != null) {
            WinMedia.call(p, 2, new Object[0]);
        }
    }

    private static Pointer qi(Pointer p, Guid.GUID iid) {
        if (p == null) {
            return null;
        }
        PointerByReference out = new PointerByReference();
        return WinMedia.call(p, 0, iid, out) == 0 ? out.getValue() : null;
    }

    private static Pointer hstring(String s) {
        PointerByReference out = new PointerByReference();
        rt.WindowsCreateString(new WString(s), s.length(), out);
        return out.getValue();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static String str(Pointer h) {
        if (h == null) {
            return "";
        }
        try {
            IntByReference len = new IntByReference();
            Pointer buf = rt.WindowsGetStringRawBuffer(h, len);
            String string = buf == null || len.getValue() == 0 ? "" : new String(buf.getCharArray(0L, len.getValue()));
            return string;
        }
        finally {
            rt.WindowsDeleteString(h);
        }
    }

    private static String getString(Pointer obj, int index) {
        PointerByReference out = new PointerByReference();
        return WinMedia.call(obj, index, out) == 0 ? WinMedia.str(out.getValue()) : "";
    }

    private static Pointer getObject(Pointer obj, int index) {
        PointerByReference out = new PointerByReference();
        return WinMedia.call(obj, index, out) == 0 ? out.getValue() : null;
    }

    private static long getLong(Pointer obj, int index) {
        LongByReference out = new LongByReference();
        return WinMedia.call(obj, index, out) == 0 ? out.getValue() : 0L;
    }

    private static int getInt(Pointer obj, int index) {
        IntByReference out = new IntByReference();
        return WinMedia.call(obj, index, out) == 0 ? out.getValue() : -1;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static boolean waitAsync(Pointer op) {
        Pointer info = WinMedia.qi(op, IID_ASYNC_INFO);
        if (info == null) {
            return false;
        }
        try {
            long end = System.currentTimeMillis() + 4000L;
            IntByReference st = new IntByReference();
            while (true) {
                if (WinMedia.call(info, 7, st) != 0) {
                    boolean bl = false;
                    return bl;
                }
                if (st.getValue() != 0) {
                    boolean bl = st.getValue() == 1;
                    return bl;
                }
                if (System.currentTimeMillis() > end) {
                    WinMedia.call(info, 9, new Object[0]);
                    boolean bl = false;
                    return bl;
                }
                Thread.sleep(3L);
                continue;
                break;
            }
        }
        catch (InterruptedException e) {
            boolean bl = false;
            return bl;
        }
        finally {
            WinMedia.release(info);
        }
    }

    private static Pointer awaitObject(Pointer op) {
        if (op == null) {
            return null;
        }
        try {
            if (!WinMedia.waitAsync(op)) {
                Pointer pointer = null;
                return pointer;
            }
            PointerByReference out = new PointerByReference();
            Pointer pointer = WinMedia.call(op, 8, out) == 0 ? out.getValue() : null;
            return pointer;
        }
        finally {
            WinMedia.release(op);
        }
    }

    private static boolean awaitBool(Pointer op) {
        if (op == null) {
            return false;
        }
        try {
            if (!WinMedia.waitAsync(op)) {
                boolean bl = false;
                return bl;
            }
            ByteByReference out = new ByteByReference();
            boolean bl = WinMedia.call(op, 8, out) == 0 && out.getValue() != 0;
            return bl;
        }
        finally {
            WinMedia.release(op);
        }
    }

    static void command(String cmd) {
        COMMANDS.add(cmd);
    }

    static boolean running() {
        return running;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static synchronized boolean start() {
        if (running) {
            return true;
        }
        if (Boolean.getBoolean("fwm.noWinRT")) {
            return false;
        }
        try {
            if (rt == null) {
                rt = (Combase)Native.load((String)"combase", Combase.class);
            }
        }
        catch (Throwable t) {
            SkyLyrics.LOG.warn("Sky Lyrics: JNA/WinRT nicht verf\u00fcgbar, nutze PowerShell", t);
            return false;
        }
        Object[] result = new Object[1];
        Object lock = new Object();
        running = true;
        mediaThread = new Thread(() -> WinMedia.mediaLoop(result, lock), "FWM-Media");
        mediaThread.setDaemon(true);
        Object object = lock;
        synchronized (object) {
            mediaThread.start();
            try {
                lock.wait(6000L);
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
        }
        if (!Boolean.TRUE.equals(result[0])) {
            running = false;
            SkyLyrics.LOG.warn("Sky Lyrics: WinRT-Medienanzeige nicht erreichbar, nutze PowerShell");
            return false;
        }
        beatThread = new Thread(Beat::run, "FWM-Beat");
        beatThread.setDaemon(true);
        beatThread.setPriority(6);
        beatThread.start();
        return true;
    }

    static synchronized void stop() {
        running = false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Pointer requestManager() {
        Pointer cls = WinMedia.hstring("Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager");
        PointerByReference statics = new PointerByReference();
        try {
            if (rt.RoGetActivationFactory(cls, IID_MANAGER_STATICS, statics) != 0) {
                Pointer pointer = null;
                return pointer;
            }
        }
        finally {
            rt.WindowsDeleteString(cls);
        }
        Pointer s = statics.getValue();
        try {
            Pointer op = WinMedia.getObject(s, 6);
            Pointer pointer = WinMedia.awaitObject(op);
            return pointer;
        }
        finally {
            WinMedia.release(s);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void mediaLoop(Object[] result, Object lock) {
        boolean ok = false;
        try {
            rt.RoInitialize(1);
            manager = WinMedia.requestManager();
            ok = manager != null;
        }
        catch (Throwable t) {
            SkyLyrics.LOG.warn("Sky Lyrics: WinRT-Start fehlgeschlagen", t);
        }
        Object t = lock;
        synchronized (t) {
            result[0] = ok;
            lock.notifyAll();
        }
        if (!ok) {
            return;
        }
        long t0 = System.nanoTime();
        while (running) {
            long loopStart = System.nanoTime();
            try {
                WinMedia.runCommands();
                WinMedia.poll((System.nanoTime() - t0) / 1000000L);
            }
            catch (Throwable t2) {
                SkyLyrics.LOG.debug("Sky Lyrics: Medienabfrage Fehler", t2);
                WinMedia.emitInactive();
                try {
                    WinMedia.release(manager);
                    manager = WinMedia.requestManager();
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
            seekFail = false;
            long took = (System.nanoTime() - loopStart) / 1000000L;
            long wait = clkPlaying ? 200L : 500L;
            try {
                Thread.sleep(Math.max(20L, wait - took));
            }
            catch (InterruptedException e) {
                break;
            }
        }
        WinMedia.release(manager);
        manager = null;
    }

    static boolean allowedApp(String app) {
        String a;
        LyricsConfig c = SkyLyrics.cfg;
        if (c == null || !c.onlyMusicApps) {
            return true;
        }
        String string = a = app == null ? "" : app.toLowerCase(Locale.ROOT);
        if (a.isEmpty()) {
            return false;
        }
        for (String b : BROWSERS) {
            if (!a.contains(b)) continue;
            return c.allowBrowser;
        }
        if (a.contains("twitch") || a.contains("tiktok") || a.contains("discord") || a.contains("obs")) {
            return false;
        }
        for (String m : MUSIC_APPS) {
            if (!a.contains(m)) continue;
            return true;
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Pointer currentSession() {
        if (manager == null) {
            return null;
        }
        LyricsConfig c = SkyLyrics.cfg;
        if (c == null || !c.onlyMusicApps) {
            Pointer s = WinMedia.getObject(manager, 6);
            sourceApp = s == null ? "" : WinMedia.getString(s, 6);
            return s;
        }
        Pointer list = WinMedia.getObject(manager, 7);
        if (list == null) {
            return null;
        }
        Pointer best = null;
        int bestScore = -1;
        String bestApp = "";
        try {
            int n = WinMedia.getInt(list, 7);
            for (int i = 0; i < n; ++i) {
                int score;
                Pointer sess;
                PointerByReference out = new PointerByReference();
                if (WinMedia.call(list, 6, i, out) != 0 || (sess = out.getValue()) == null) continue;
                String app = WinMedia.getString(sess, 6);
                if (!WinMedia.allowedApp(app)) {
                    WinMedia.release(sess);
                    continue;
                }
                Pointer pb = WinMedia.getObject(sess, 9);
                boolean playing = pb != null && WinMedia.getInt(pb, 7) == 4;
                WinMedia.release(pb);
                int n2 = playing ? 2 : (score = app.equals(lastMusicApp) ? 1 : 0);
                if (score > bestScore) {
                    WinMedia.release(best);
                    best = sess;
                    bestScore = score;
                    bestApp = app;
                    continue;
                }
                WinMedia.release(sess);
            }
        }
        finally {
            WinMedia.release(list);
        }
        if (best != null) {
            lastMusicApp = bestApp;
        }
        sourceApp = bestApp;
        return best;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    private static void runCommands() {
lbl1:
        // 15 sources

        block21: while ((cmd = WinMedia.COMMANDS.poll()) != null) {
            s = WinMedia.currentSession();
            if (s == null) continue;
            try {
                var2_2 = cmd;
                var3_4 = -1;
                switch (var2_2.hashCode()) {
                    case -868304044: {
                        if (!var2_2.equals("toggle")) break;
                        var3_4 = 0;
                        break;
                    }
                    case 3443508: {
                        if (!var2_2.equals("play")) break;
                        var3_4 = 1;
                        break;
                    }
                    case 106440182: {
                        if (!var2_2.equals("pause")) break;
                        var3_4 = 2;
                        break;
                    }
                    case 3377907: {
                        if (!var2_2.equals("next")) break;
                        var3_4 = 3;
                        break;
                    }
                    case 3449395: {
                        if (!var2_2.equals("prev")) break;
                        var3_4 = 4;
                        break;
                    }
                    case 3545755: {
                        if (!var2_2.equals("sync")) break;
                        var3_4 = 5;
                    }
                }
                switch (var3_4) {
                    case 0: {
                        WinMedia.awaitBool(WinMedia.getObject(s, 20));
                        ** break;
                    }
                    case 1: {
                        WinMedia.awaitBool(WinMedia.getObject(s, 10));
                        ** break;
                    }
                    case 2: {
                        WinMedia.awaitBool(WinMedia.getObject(s, 11));
                        ** break;
                    }
                    case 3: {
                        WinMedia.awaitBool(WinMedia.getObject(s, 16));
                        ** break;
                    }
                    case 4: {
                        WinMedia.awaitBool(WinMedia.getObject(s, 17));
                        ** break;
                    }
                    case 5: {
                        WinMedia.forceSync = true;
                        ** break;
                    }
                    default: {
                        if (cmd.startsWith("vol:")) {
                            lv = Float.parseFloat(cmd.substring(4));
                            r = Audio.volume(WinMedia.getString(s, 6), lv);
                            if (!(r >= 0.0f)) continue block21;
                            WinMedia.vol = r;
                            ** break;
                        }
                        if (!cmd.startsWith("seek:")) continue block21;
                        ms = Long.parseLong(cmd.substring(5));
                        op = new PointerByReference();
                        v0 = done = WinMedia.call(s, 24, new Object[]{ms * 10000L, op}) == 0 && WinMedia.awaitBool(op.getValue()) != false;
                        if (done) {
                            WinMedia.clkPos = ms;
                            WinMedia.clkAt = WinMedia.nowMs();
                            WinMedia.seekAt = WinMedia.nowMs();
                            ** break;
                        }
                        WinMedia.seekFail = true;
                        continue block21;
                    }
                }
            }
            catch (Throwable t) {
                SkyLyrics.LOG.debug("Sky Lyrics: Befehl {} fehlgeschlagen", (Object)cmd, (Object)t);
            }
            finally {
                WinMedia.release(s);
            }
        }
    }

    private static long nowMs() {
        return (System.nanoTime() - T0) / 1000000L;
    }

    private static void emitInactive() {
        JsonObject o = new JsonObject();
        o.addProperty("active", Boolean.valueOf(false));
        MediaWatcher.handle(o.toString());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    private static void poll(long sinceStart) {
        block26: {
            block25: {
                s = WinMedia.currentSession();
                if (s == null) {
                    WinMedia.emitInactive();
                    return;
                }
                props = null;
                timeline = null;
                playback = null;
                try {
                    app = WinMedia.getString(s, 6);
                    timeline = WinMedia.getObject(s, 8);
                    playback = WinMedia.getObject(s, 9);
                    if (timeline != null && playback != null) break block25;
                    WinMedia.emitInactive();
                }
                catch (Throwable var34_27) {
                    WinMedia.release(props);
                    WinMedia.release(timeline);
                    WinMedia.release(playback);
                    WinMedia.release(s);
                    throw var34_27;
                }
                WinMedia.release(props);
                WinMedia.release(timeline);
                WinMedia.release(playback);
                WinMedia.release(s);
                return;
            }
            updNow = WinMedia.getLong(timeline, 11);
            v0 = refresh = ++WinMedia.propsTick >= 3 || app.equals(WinMedia.propsApp) == false || updNow != WinMedia.lastUpd || WinMedia.cachedTitle == null;
            if (!refresh) ** GOTO lbl46
            WinMedia.propsTick = 0;
            props = WinMedia.awaitObject(WinMedia.getObject(s, 7));
            if (props != null) break block26;
            WinMedia.emitInactive();
            WinMedia.release(props);
            WinMedia.release(timeline);
            WinMedia.release(playback);
            WinMedia.release(s);
            return;
        }
        WinMedia.propsApp = app;
        WinMedia.cachedTitle = WinMedia.getString(props, 6);
        WinMedia.cachedArtist = WinMedia.getString(props, 9);
        WinMedia.cachedAlbum = WinMedia.getString(props, 10);
        WinMedia.cachedGenres = WinMedia.genres(props);
lbl46:
        // 2 sources

        title = WinMedia.cachedTitle;
        artist = WinMedia.cachedArtist;
        album = WinMedia.cachedAlbum;
        genres = WinMedia.cachedGenres;
        playing = WinMedia.getInt(playback, 7) == 4;
        WinMedia.audioKey = app;
        if (++WinMedia.volTick >= 6) {
            WinMedia.volTick = 0;
            try {
                WinMedia.vol = Audio.volume(app, -1.0f);
            }
            catch (Throwable t) {
                WinMedia.vol = -1.0f;
            }
        }
        key = artist + "|" + title;
        raw = (double)WinMedia.getLong(timeline, 10) / 10000.0;
        upd = WinMedia.getLong(timeline, 11);
        appPos = raw;
        if (playing && upd > 0L) {
            nowFt = System.currentTimeMillis() * 10000L + 116444736000000000L;
            appPos += (double)(nowFt - upd) / 10000.0;
        }
        now = WinMedia.nowMs();
        own = WinMedia.clkPos + (WinMedia.clkPlaying != false ? now - WinMedia.clkAt : 0L);
        newSong = key.equals(WinMedia.clkKey) == false;
        v1 = fresh = WinMedia.forceSync != false || newSong != false || upd != WinMedia.lastUpd || Math.abs(raw - WinMedia.lastRaw) > 1.0;
        if (WinMedia.seekAt >= 0L) {
            if (now - WinMedia.seekAt >= 3000L) {
                WinMedia.seekAt = -1L;
            } else if (fresh && !WinMedia.forceSync && Math.abs(appPos - own) > 1500.0) {
                fresh = false;
            }
        }
        if (newSong && !WinMedia.clkKey.isEmpty() && upd == WinMedia.lastUpd && playing) {
            WinMedia.clkPos = 0L;
            src = "new";
        } else if (fresh) {
            WinMedia.clkPos = Math.round(appPos);
            src = "app";
        } else {
            WinMedia.clkPos = Math.round(own);
            src = "clock";
        }
        WinMedia.clkAt = now;
        WinMedia.clkPlaying = playing;
        WinMedia.clkKey = key;
        WinMedia.lastUpd = upd;
        WinMedia.lastRaw = raw;
        WinMedia.forceSync = false;
        dur = (WinMedia.getLong(timeline, 7) - WinMedia.getLong(timeline, 6)) / 10000L;
        pos = WinMedia.clkPos;
        if (dur > 0L && pos > dur) {
            pos = dur;
        }
        if (pos < 0L) {
            pos = 0L;
        }
        if (!key.equals(WinMedia.lastKey)) {
            WinMedia.lastKey = key;
            WinMedia.coverPath = "";
            WinMedia.coverTries = 0;
            WinMedia.coverWait = 0;
        }
        if (WinMedia.coverPath.isEmpty() && WinMedia.coverTries < 60 && props != null) {
            if (WinMedia.coverWait > 0) {
                --WinMedia.coverWait;
            } else {
                ++WinMedia.coverTries;
                WinMedia.coverWait = 4;
                try {
                    WinMedia.coverPath = WinMedia.saveCover(props);
                }
                catch (Throwable t) {
                    WinMedia.coverPath = "";
                }
            }
        }
        o = new JsonObject();
        o.addProperty("active", Boolean.valueOf(true));
        o.addProperty("app", app);
        o.addProperty("title", title);
        o.addProperty("artist", artist);
        o.addProperty("album", album);
        o.addProperty("genres", genres);
        o.addProperty("playing", Boolean.valueOf(playing));
        o.addProperty("pos", (Number)pos);
        o.addProperty("src", src);
        o.addProperty("seekFail", Boolean.valueOf(WinMedia.seekFail));
        o.addProperty("vol", (Number)((double)Math.round(WinMedia.vol * 1000.0f) / 1000.0));
        o.addProperty("bpm", (Number)((double)Math.round(Beat.bpm * 100.0) / 100.0));
        o.addProperty("beat", (Number)Beat.beatAgoMs());
        o.addProperty("lvl", (Number)((double)Math.round(Beat.level * 1000.0) / 1000.0));
        o.addProperty("dur", (Number)dur);
        o.addProperty("cover", WinMedia.coverPath);
        o.addProperty("coverId", (Number)WinMedia.coverN);
        MediaWatcher.handle(o.toString());
        WinMedia.release(props);
        WinMedia.release(timeline);
        WinMedia.release(playback);
        WinMedia.release(s);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static String genres(Pointer props) {
        Pointer vec = WinMedia.getObject(props, 12);
        if (vec == null) {
            return "";
        }
        try {
            int n = WinMedia.getInt(vec, 7);
            ArrayList<String> out = new ArrayList<String>();
            for (int i = 0; i < n && i < 8; ++i) {
                String g;
                PointerByReference h = new PointerByReference();
                if (WinMedia.call(vec, 6, i, h) != 0 || (g = WinMedia.str(h.getValue())).isBlank()) continue;
                out.add(g);
            }
            String string = String.join((CharSequence)", ", out);
            return string;
        }
        finally {
            WinMedia.release(vec);
        }
    }

    /*
     * Exception decompiling
     */
    private static String saveCover(Pointer props) throws Exception {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Started 4 blocks at once
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    static {
        IID_MANAGER_STATICS = new Guid.GUID("{2050C4EE-11A0-57DE-AED7-C97C70338245}");
        IID_ASYNC_INFO = new Guid.GUID("{00000036-0000-0000-C000-000000000046}");
        IID_RANDOM_ACCESS_STREAM = new Guid.GUID("{905A0FE1-BC53-11DF-8C49-001E4FC686DA}");
        IID_INPUT_STREAM = new Guid.GUID("{905A0FE2-BC53-11DF-8C49-001E4FC686DA}");
        IID_BUFFER_FACTORY = new Guid.GUID("{71AF914D-C10F-484B-BC50-14BC623B3A27}");
        IID_BUFFER_BYTE_ACCESS = new Guid.GUID("{905A0FEF-BC53-11DF-8C49-001E4FC686DA}");
        IID_BUFFER = new Guid.GUID("{905A0FE0-BC53-11DF-8C49-001E4FC686DA}");
        CLSID_MM_DEVICE_ENUMERATOR = new Guid.GUID("{BCDE0395-E52F-467C-8E3D-C4579291692E}");
        IID_MM_DEVICE_ENUMERATOR = new Guid.GUID("{A95664D2-9614-4F35-A746-DE8DB63617E6}");
        IID_AUDIO_SESSION_MANAGER2 = new Guid.GUID("{77AA99A0-1BD6-484F-8BC7-2C654C9A9B6F}");
        IID_AUDIO_SESSION_CONTROL2 = new Guid.GUID("{BFB7FF88-7239-4FC9-8FA2-07C950BE9C6D}");
        IID_SIMPLE_AUDIO_VOLUME = new Guid.GUID("{87CE5498-68D6-44E5-9215-6DA47EF883D8}");
        IID_AUDIO_METER = new Guid.GUID("{C02216F6-8C67-4B5B-9D00-D008E73E0064}");
        COMMANDS = new ConcurrentLinkedQueue();
        clkKey = "";
        seekAt = -1L;
        lastUpd = Long.MIN_VALUE;
        lastRaw = -1.0;
        lastKey = "";
        coverPath = "";
        vol = -1.0f;
        audioKey = "";
        propsApp = "";
        cachedArtist = "";
        cachedAlbum = "";
        cachedGenres = "";
        lastMusicApp = "";
        sourceApp = "";
        MUSIC_APPS = new String[]{"spotify", "applemusic", "apple.music", "itunes", "amazon", "deezer", "tidal", "youtubemusic", "youtube music", "ytmdesktop", "th-ch.youtube", "foobar", "musicbee", "aimp", "zunemusic", "mediaplayer", "wmplayer", "winamp", "vlc", "soundcloud", "qobuz", "napster", "pandora", "audacious", "clementine", "strawberry", "dopamine", "plexamp", "cider", "nuclear", "music"};
        BROWSERS = new String[]{"chrome", "msedge", "edge", "firefox", "opera", "brave", "vivaldi", "arc", "browser", "thorium", "librewolf"};
        T0 = System.nanoTime();
    }

    static interface Combase
    extends Library {
        public int RoInitialize(int var1);

        public int WindowsCreateString(WString var1, int var2, PointerByReference var3);

        public int WindowsDeleteString(Pointer var1);

        public Pointer WindowsGetStringRawBuffer(Pointer var1, IntByReference var2);

        public int RoGetActivationFactory(Pointer var1, Guid.GUID var2, PointerByReference var3);

        public int RoActivateInstance(Pointer var1, PointerByReference var2);
    }

    static final class Audio {
        private Audio() {
        }

        private static String processName(int pid) {
            return ProcessHandle.of(pid).flatMap(h -> h.info().command()).map(c -> {
                String n = Path.of(c, new String[0]).getFileName().toString().toLowerCase(Locale.ROOT);
                return n.endsWith(".exe") ? n.substring(0, n.length() - 4) : n;
            }).orElse("");
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        static List<Pointer> sessions(String key) {
            PointerByReference e;
            Pointer se;
            Pointer mgr;
            Pointer device;
            Pointer enumerator;
            ArrayList<Pointer> out;
            block11: {
                PointerByReference m;
                block10: {
                    PointerByReference d;
                    block9: {
                        out = new ArrayList<Pointer>();
                        if (key == null || key.isEmpty()) {
                            return out;
                        }
                        key = key.toLowerCase(Locale.ROOT);
                        PointerByReference en = new PointerByReference();
                        if (Ole32.INSTANCE.CoCreateInstance(CLSID_MM_DEVICE_ENUMERATOR, null, 23, IID_MM_DEVICE_ENUMERATOR, en).intValue() != 0) {
                            return out;
                        }
                        enumerator = en.getValue();
                        device = null;
                        mgr = null;
                        se = null;
                        d = new PointerByReference();
                        if (WinMedia.call(enumerator, 4, 0, 1, d) == 0) break block9;
                        ArrayList<Pointer> arrayList = out;
                        WinMedia.release(se);
                        WinMedia.release(mgr);
                        WinMedia.release(device);
                        WinMedia.release(enumerator);
                        return arrayList;
                    }
                    device = d.getValue();
                    m = new PointerByReference();
                    if (WinMedia.call(device, 3, IID_AUDIO_SESSION_MANAGER2, 23, Pointer.NULL, m) == 0) break block10;
                    ArrayList<Pointer> arrayList = out;
                    WinMedia.release(se);
                    WinMedia.release(mgr);
                    WinMedia.release(device);
                    WinMedia.release(enumerator);
                    return arrayList;
                }
                mgr = m.getValue();
                e = new PointerByReference();
                if (WinMedia.call(mgr, 5, e) == 0) break block11;
                ArrayList<Pointer> arrayList = out;
                WinMedia.release(se);
                WinMedia.release(mgr);
                WinMedia.release(device);
                WinMedia.release(enumerator);
                return arrayList;
            }
            try {
                se = e.getValue();
                int n = WinMedia.getInt(se, 3);
                for (int i = 0; i < n; ++i) {
                    String name;
                    PointerByReference c = new PointerByReference();
                    if (WinMedia.call(se, 4, i, c) != 0 || c.getValue() == null) continue;
                    Pointer ctl = c.getValue();
                    Pointer ctl2 = WinMedia.qi(ctl, IID_AUDIO_SESSION_CONTROL2);
                    WinMedia.release(ctl);
                    if (ctl2 == null) continue;
                    IntByReference pid = new IntByReference();
                    String string = name = WinMedia.call(ctl2, 14, pid) == 0 && pid.getValue() != 0 ? Audio.processName(pid.getValue()) : "";
                    if (name.length() >= 3 && key.contains(name)) {
                        out.add(ctl2);
                        continue;
                    }
                    WinMedia.release(ctl2);
                }
            }
            catch (Throwable throwable) {
                WinMedia.release(se);
                WinMedia.release(mgr);
                WinMedia.release(device);
                WinMedia.release(enumerator);
                throw throwable;
            }
            WinMedia.release(se);
            WinMedia.release(mgr);
            WinMedia.release(device);
            WinMedia.release(enumerator);
            return out;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        static float volume(String key, float set) {
            float result = -1.0f;
            for (Pointer ctl : Audio.sessions(key)) {
                Pointer v = WinMedia.qi(ctl, IID_SIMPLE_AUDIO_VOLUME);
                WinMedia.release(ctl);
                if (v == null) continue;
                try {
                    if (set >= 0.0f) {
                        WinMedia.call(v, 3, Float.valueOf(Math.max(0.0f, Math.min(1.0f, set))), Pointer.NULL);
                    }
                    FloatByReference lv = new FloatByReference();
                    if (WinMedia.call(v, 4, lv) != 0) continue;
                    result = Math.max(result, lv.getValue());
                }
                finally {
                    WinMedia.release(v);
                }
            }
            return result;
        }

        static Pointer meter(String key) {
            Pointer found = null;
            for (Pointer ctl : Audio.sessions(key)) {
                if (found == null) {
                    found = WinMedia.qi(ctl, IID_AUDIO_METER);
                }
                WinMedia.release(ctl);
            }
            return found;
        }
    }

    static final class Beat {
        static volatile double bpm;
        static volatile double level;
        private static long anchor;
        private static long lastLock;
        private static final Object LK;
        private static final long START;
        private static final List<Long> onsets;
        private static final int BIN = 10;
        private static final int N = 900;
        private static final double[] env;
        private static long envBin;
        private static int envFill;
        private static double eFast;
        private static double eSlow;
        private static long lastAc;
        private static double acConf;

        private Beat() {
        }

        private static long now() {
            return (System.nanoTime() - START) / 1000000L;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        static long beatAgoMs() {
            Object object = LK;
            synchronized (object) {
                return anchor == 0L || bpm <= 0.0 ? -1L : Beat.now() - anchor;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        static void run() {
            try {
                Ole32.INSTANCE.CoInitializeEx(null, 0);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            Pointer meter = null;
            String lastKey = null;
            long lastFind = -100000L;
            double fast = 0.0;
            double slow = 0.0;
            double prevFast = 0.0;
            double maxSlow = 1.0E-4;
            boolean armed = true;
            long lastOnset = 0L;
            FloatByReference peak = new FloatByReference();
            while (running) {
                try {
                    if (SkyLyrics.cfg == null || !SkyLyrics.cfg.beatSync || !clkPlaying) {
                        level = 0.0;
                        Thread.sleep(500L);
                        continue;
                    }
                    long now = Beat.now();
                    String key = audioKey;
                    if (!key.equals(lastKey) || now - lastFind > (long)(meter == null ? 2000 : 6000)) {
                        lastKey = key;
                        lastFind = now;
                        WinMedia.release(meter);
                        meter = Audio.meter(key);
                    }
                    if (meter == null) {
                        level = 0.0;
                        Thread.sleep(250L);
                        continue;
                    }
                    if (WinMedia.call(meter, 3, peak) != 0) {
                        WinMedia.release(meter);
                        meter = null;
                        continue;
                    }
                    double p = peak.getValue();
                    fast += (p - fast) * 0.5;
                    slow += (p - slow) * 0.02;
                    maxSlow = Math.max(slow, maxSlow * 0.9995);
                    level = Math.min(1.0, slow / Math.max(1.0E-4, maxSlow));
                    Beat.feed(now, p);
                    if (now - lastAc > 1000L) {
                        lastAc = now;
                        Beat.autocorr(now);
                    }
                    double rise = fast - prevFast;
                    prevFast = fast;
                    if (armed && slow > 3.0E-4 && fast > slow * 1.25 && rise > slow * 0.06 && now - lastOnset > 230L) {
                        armed = false;
                        lastOnset = now;
                        Beat.onset(now);
                    }
                    if (fast < slow * 1.05) {
                        armed = true;
                    }
                    if (slow < 3.0E-4 && now - lastOnset > 4000L) {
                        Object object = LK;
                        synchronized (object) {
                            bpm = 0.0;
                            onsets.clear();
                        }
                        envFill = 0;
                    }
                    Thread.sleep(10L);
                }
                catch (InterruptedException e) {
                    break;
                }
                catch (Throwable t) {
                    WinMedia.release(meter);
                    meter = null;
                    try {
                        Thread.sleep(500L);
                    }
                    catch (InterruptedException e) {
                        break;
                    }
                }
            }
            WinMedia.release(meter);
        }

        private static void feed(long now, double p) {
            double e = Math.log10(p + 1.0E-5);
            if (Double.isNaN(eFast)) {
                eFast = e;
                eSlow = e;
            }
            eFast += (e - eFast) * 0.6;
            eSlow += (e - eSlow) * 0.06;
            double d = Math.max(0.0, eFast - eSlow);
            long bin = now / 10L;
            if (envBin < 0L) {
                envBin = bin - 1L;
            }
            if (bin - envBin > 900L) {
                envBin = bin - 1L;
                envFill = 0;
            }
            while (envBin < bin) {
                Beat.env[(int)(++Beat.envBin % 900L)] = d;
                if (envFill >= 900) continue;
                ++envFill;
            }
            Beat.env[(int)(bin % 900L)] = Math.max(env[(int)(bin % 900L)], d);
        }

        private static double at(long b) {
            return env[(int)((b % 900L + 900L) % 900L)];
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private static void autocorr(long now) {
            double nb;
            double sd;
            if (envFill < 500) {
                return;
            }
            int n = envFill;
            long end = envBin;
            double mean = 0.0;
            for (int i = 0; i < n; ++i) {
                mean += Beat.at(end - (long)i);
            }
            mean /= (double)n;
            int minL = 33;
            int maxL = 86;
            double[] v = new double[2 * maxL + 2];
            for (int l = minL; l <= 2 * maxL && l < n - 50; ++l) {
                double sum = 0.0;
                int cnt = 0;
                int i = 0;
                while (i + l < n) {
                    sum += (Beat.at(end - (long)i) - mean) * (Beat.at(end - (long)i - (long)l) - mean);
                    ++cnt;
                    ++i;
                }
                v[l] = cnt > 0 ? sum / (double)cnt : 0.0;
            }
            int best = -1;
            double bestV = -1.7976931348623157E308;
            double avg = 0.0;
            double sq = 0.0;
            int m = 0;
            for (int l = minL; l <= maxL; ++l) {
                double sc = v[l] + 0.5 * (2 * l < v.length ? v[2 * l] : 0.0);
                avg += sc;
                sq += sc * sc;
                ++m;
                if (!(sc > bestV)) continue;
                bestV = sc;
                best = l;
            }
            if (best < 0 || m == 0) {
                return;
            }
            if ((acConf = (bestV - (avg /= (double)m)) / (sd = Math.sqrt(Math.max(1.0E-12, sq / (double)m - avg * avg)))) < 2.5 || bestV <= 0.0) {
                return;
            }
            double a = v[best - 1];
            double b = v[best];
            double c = v[best + 1];
            double off = a - 2.0 * b + c != 0.0 ? 0.5 * (a - c) / (a - 2.0 * b + c) : 0.0;
            double period = ((double)best + Math.max(-0.5, Math.min(0.5, off))) * 10.0;
            for (nb = 60000.0 / period; nb < 80.0; nb *= 2.0) {
            }
            while (nb >= 170.0) {
                nb /= 2.0;
            }
            double pp = 60000.0 / nb / 10.0;
            int steps = (int)Math.floor(pp);
            double bestPh = 0.0;
            double bestS = -1.7976931348623157E308;
            for (int ph = 0; ph < steps; ++ph) {
                long idx;
                double s2 = 0.0;
                for (int k = 0; k < 8 && end - (idx = end - (long)ph - Math.round((double)k * pp)) < (long)n; ++k) {
                    s2 += Beat.at(idx) + 0.5 * Beat.at(idx - 1L);
                }
                if (!(s2 > bestS)) continue;
                bestS = s2;
                bestPh = ph;
            }
            Object object = LK;
            synchronized (object) {
                bpm = bpm <= 0.0 || Math.abs(nb - bpm) > 4.0 ? nb : bpm * 0.7 + nb * 0.3;
                long beatAt = now - (long)(bestPh * 10.0) - 20L;
                if (anchor == 0L || now - lastLock > 1500L) {
                    anchor = beatAt;
                } else {
                    double per = 60000.0 / bpm;
                    double k2 = Math.round((double)(beatAt - anchor) / per);
                    double err = (double)beatAt - ((double)anchor + k2 * per);
                    anchor = (long)((double)anchor + k2 * per + err * 0.3);
                }
                lastLock = now;
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private static void onset(long now) {
            Object object = LK;
            synchronized (object) {
                onsets.add(now);
                while (!onsets.isEmpty() && now - onsets.get(0) > 8000L) {
                    onsets.remove(0);
                }
                if (onsets.size() >= 6) {
                    double[] hist = new double[200];
                    double total = 0.0;
                    for (int i = 0; i < onsets.size(); ++i) {
                        for (int j = i + 1; j < onsets.size() && j <= i + 4; ++j) {
                            double b;
                            double dt = onsets.get(j) - onsets.get(i);
                            if (dt < 250.0 || dt > 2000.0) continue;
                            for (b = 60000.0 / dt; b < 80.0; b *= 2.0) {
                            }
                            while (b >= 170.0) {
                                b /= 2.0;
                            }
                            int bi = (int)Math.round(b);
                            if (bi < 2 || bi > 197) continue;
                            int n = bi;
                            hist[n] = hist[n] + 1.0;
                            int n2 = bi - 1;
                            hist[n2] = hist[n2] + 0.5;
                            int n3 = bi + 1;
                            hist[n3] = hist[n3] + 0.5;
                            total += 2.0;
                        }
                    }
                    int best = 0;
                    for (int i = 1; i < 199; ++i) {
                        if (!(hist[i] > hist[best])) continue;
                        best = i;
                    }
                    if (best > 0 && hist[best] >= 3.0 && total > 0.0 && acConf < 2.5) {
                        double w = hist[best - 1] + hist[best] + hist[best + 1];
                        double nb = ((double)(best - 1) * hist[best - 1] + (double)best * hist[best] + (double)(best + 1) * hist[best + 1]) / w;
                        double d = bpm = bpm <= 0.0 || Math.abs(nb - bpm) > 4.0 ? nb : bpm * 0.8 + nb * 0.2;
                    }
                }
                if (bpm > 0.0) {
                    double period = 60000.0 / bpm;
                    if (anchor == 0L) {
                        anchor = now;
                        lastLock = now;
                        return;
                    }
                    double k = Math.round((double)(now - anchor) / period);
                    double grid = (double)anchor + k * period;
                    double err = (double)now - grid;
                    if (Math.abs(err) < period * 0.25) {
                        anchor = (long)(grid + err * 0.35);
                        lastLock = now;
                    } else if (now - lastLock > 2500L) {
                        anchor = now;
                        lastLock = now;
                    }
                }
            }
        }

        static {
            LK = new Object();
            START = System.nanoTime();
            onsets = new ArrayList<Long>();
            env = new double[900];
            envBin = -1L;
            eFast = Double.NaN;
            eSlow = Double.NaN;
        }
    }
}

