/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.reflect.TypeToken
 */
package net.friendswithmoney.fwm.lyrics;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.friendswithmoney.fwm.lyrics.LyricsConfig;
import net.friendswithmoney.fwm.lyrics.SkyLyrics;

public final class SongOffsets {
    private static final Path FILE = LyricsConfig.DIR.resolve("song-offsets.json");
    private static final Gson GSON = new Gson();
    private static Map<String, Integer> map;

    private SongOffsets() {
    }

    private static synchronized Map<String, Integer> map() {
        if (map == null) {
            map = new ConcurrentHashMap<String, Integer>();
            try {
                Map var0;
                if (Files.exists(FILE, new LinkOption[0]) && (var0 = (Map)GSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), new TypeToken<Map<String, Integer>>(){}.getType())) != null) {
                    map.putAll(var0);
                }
            }
            catch (Exception var1) {
                SkyLyrics.LOG.warn("Sky Lyrics: song-offsets.json kaputt, wird neu angelegt", (Throwable)var1);
            }
        }
        return map;
    }

    public static int get(String var0) {
        Integer var1 = SongOffsets.map().get(var0);
        return var1 == null ? 0 : var1;
    }

    public static synchronized void add(String var0, int var1) {
        int var2 = Math.max(-15000, Math.min(15000, SongOffsets.get(var0) + var1));
        if (var2 == 0) {
            SongOffsets.map().remove(var0);
        } else {
            SongOffsets.map().put(var0, var2);
        }
        try {
            Files.createDirectories(FILE.getParent(), new FileAttribute[0]);
            Files.writeString(FILE, (CharSequence)GSON.toJson(SongOffsets.map()), StandardCharsets.UTF_8, new OpenOption[0]);
        }
        catch (Exception var4) {
            SkyLyrics.LOG.warn("Sky Lyrics: Song-Versatz speichern fehlgeschlagen", (Throwable)var4);
        }
    }
}

