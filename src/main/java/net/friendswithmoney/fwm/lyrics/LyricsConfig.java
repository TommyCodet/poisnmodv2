/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  net.fabricmc.loader.api.FabricLoader
 */
package net.friendswithmoney.fwm.lyrics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import net.fabricmc.loader.api.FabricLoader;
import net.friendswithmoney.fwm.lyrics.FontPack;
import net.friendswithmoney.fwm.lyrics.SkyLyrics;

public final class LyricsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Path DIR = FabricLoader.getInstance().getConfigDir().resolve("fwm").resolve("skylyrics");
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("fwm").resolve("skylyrics.json");
    public boolean enabled = false;
    public boolean onlyMusicApps = true;
    public boolean allowBrowser = false;
    public String consent = "";
    public boolean netGenre = true;
    public boolean netCover = true;
    public String mode = "build";
    public boolean speedAdapt = true;
    public boolean genreWeather = true;
    public String fontStyle = "outfit";
    public boolean bold = true;
    public boolean italic = false;
    public String textEffect = "shadow";
    public int pillOpacity = 82;
    public boolean skyEnabled = true;
    public boolean pillEnabled = false;
    public float skyHeight = 20.0f;
    public float skyDistance = 48.0f;
    public float skyScale = 2.6f;
    public float skyTilt = -6.0f;
    public boolean onlyUnderOpenSky = true;
    public boolean skyOnlyCinema = false;
    public boolean wordByWord = true;
    public String colorMode = "custom";
    public String darkTextColor = "#1E2440";
    public String lightTextColor = "#FFF8E8";
    public String customColor = "#7DF0C6";
    public String accentColor = "#7DF0C6";
    public String color2 = "#F472B6";
    public boolean gradient = false;
    public int glow = 40;
    public boolean arc = false;
    public String look = "fwm_mint";
    public String pillPosition = "top";
    public int offsetMs = 0;
    public boolean randomPlacement = true;
    public boolean keepVisible = true;
    public boolean musicFx = false;
    public String fxLevel = "normal";
    public boolean pvpEdit = true;
    public boolean fxLightning = false;
    public boolean fxNotes = false;
    public boolean fxPillars = false;
    public boolean fxStars = true;
    public float pvpScale = 1.0f;
    public float pvpOutline = 1.0f;
    public boolean beatSync = true;
    public boolean wordDust = true;
    public boolean songIntro = true;
    public boolean genreTime = true;
    public boolean worldSync = false;
    public int fwmDefaults = 0;

    public static LyricsConfig load() {
        try {
            LyricsConfig var0;
            if (Files.exists(FILE, new LinkOption[0]) && (var0 = (LyricsConfig)GSON.fromJson(Files.readString(FILE), LyricsConfig.class)) != null) {
                var0.clamp();
                var0.save();
                return var0;
            }
        }
        catch (Exception var1) {
            SkyLyrics.LOG.warn("skylyrics.json kaputt, nehme Standardwerte", (Throwable)var1);
        }
        LyricsConfig var2 = new LyricsConfig();
        var2.save();
        return var2;
    }

    public void clamp() {
        if (this.fwmDefaults < 1) {
            this.musicFx = false;
            this.fxNotes = false;
            this.fxPillars = false;
            this.fxLightning = false;
            this.pillEnabled = false;
            this.fwmDefaults = 1;
        }
        this.skyHeight = Math.max(0.0f, Math.min(70.0f, this.skyHeight));
        this.skyScale = Math.max(0.8f, Math.min(6.0f, this.skyScale));
        if (this.skyDistance <= 0.0f) {
            this.skyDistance = 48.0f;
        }
        this.skyDistance = Math.max(12.0f, Math.min(160.0f, this.skyDistance));
        this.skyTilt = Math.max(-30.0f, Math.min(30.0f, this.skyTilt));
        this.offsetMs = Math.max(-10000, Math.min(10000, this.offsetMs));
        if (this.colorMode == null) {
            this.colorMode = "auto";
        }
        if (this.pillPosition == null) {
            this.pillPosition = "top";
        }
        if (this.mode == null || !this.mode.equals("vanish") && !this.mode.equals("flyby")) {
            this.mode = "build";
        }
        if (this.fontStyle == null || FontPack.CHOICES.stream().noneMatch(var1 -> var1.id().equals(this.fontStyle))) {
            this.fontStyle = "default";
        }
        if (this.textEffect == null || !this.textEffect.equals("shadow") && !this.textEffect.equals("outline")) {
            this.textEffect = "none";
        }
        if (this.customColor == null) {
            this.customColor = "#FFD27A";
        }
        if (this.darkTextColor == null) {
            this.darkTextColor = "#1E2440";
        }
        if (this.lightTextColor == null) {
            this.lightTextColor = "#FFF8E8";
        }
        this.pillOpacity = Math.max(20, Math.min(100, this.pillOpacity));
        if (this.accentColor == null) {
            this.accentColor = "#A78BFA";
        }
        if (this.fxLevel == null || !this.fxLevel.equals("subtle") && !this.fxLevel.equals("epic")) {
            this.fxLevel = "normal";
        }
        this.pvpScale = Math.max(0.5f, Math.min(2.0f, this.pvpScale));
        this.pvpOutline = Math.max(0.0f, Math.min(3.0f, this.pvpOutline));
        this.glow = Math.max(0, Math.min(100, this.glow));
        if (this.color2 == null) {
            this.color2 = "#F472B6";
        }
        if (this.look == null) {
            this.look = "";
        }
    }

    public void resetToDefaults() {
        boolean keepOn = this.enabled;
        String keepConsent = this.consent;
        LyricsConfig.copyDefaults(this, new LyricsConfig());
        this.enabled = keepOn;
        this.consent = keepConsent;
        this.save();
    }

    public static <T> void copyDefaults(T var0, T var1) {
        for (Field var5 : var0.getClass().getFields()) {
            if (Modifier.isStatic(var5.getModifiers()) || Modifier.isFinal(var5.getModifiers())) continue;
            try {
                var5.set(var0, var5.get(var1));
            }
            catch (IllegalAccessException illegalAccessException) {
                // empty catch block
            }
        }
    }

    public void save() {
        try {
            Files.createDirectories(FILE.getParent(), new FileAttribute[0]);
            Files.writeString(FILE, (CharSequence)GSON.toJson((Object)this), new OpenOption[0]);
        }
        catch (Exception var2) {
            SkyLyrics.LOG.warn("Konnte skylyrics.json nicht speichern", (Throwable)var2);
        }
    }

    public boolean buildMode() {
        return !"vanish".equals(this.mode);
    }

    public boolean flybyMode() {
        return "flyby".equals(this.mode);
    }

    public static String hex(int var0) {
        return String.format("#%06X", var0 & 0xFFFFFF);
    }

    public static int rgb(String var0, int var1) {
        try {
            String var2 = var0.trim();
            if (var2.startsWith("#")) {
                var2 = var2.substring(1);
            }
            return Integer.parseInt(var2, 16) & 0xFFFFFF;
        }
        catch (Exception var3) {
            return var1;
        }
    }
}

