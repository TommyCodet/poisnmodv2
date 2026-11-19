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
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import net.fabricmc.loader.api.FabricLoader;
import net.friendswithmoney.fwm.lyrics.AfkCinema;

public final class AfkConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("fwm").resolve("afkcinema.json");
    public boolean enabled = false;
    public int delaySeconds = 30;
    public float shotSeconds = 10.0f;
    public int configVersion = 0;
    public boolean hideHud = true;
    public boolean letterbox = true;
    public boolean showAfkTimer = true;
    public boolean environmentShots = true;
    public boolean afkInChat = true;
    public boolean mobShots = true;

    public static AfkConfig load() {
        try {
            AfkConfig var0;
            if (Files.exists(FILE, new LinkOption[0]) && (var0 = (AfkConfig)GSON.fromJson(Files.readString(FILE), AfkConfig.class)) != null) {
                var0.clamp();
                var0.save();
                return var0;
            }
        }
        catch (Exception var1) {
            AfkCinema.LOG.warn("afkcinema.json kaputt, nehme Standardwerte", (Throwable)var1);
        }
        AfkConfig var2 = new AfkConfig();
        var2.save();
        return var2;
    }

    private void clamp() {
        if (this.configVersion < 2) {
            if (this.shotSeconds == 7.0f) {
                this.shotSeconds = 10.0f;
            }
            this.configVersion = 2;
        }
        if (this.delaySeconds < 5) {
            this.delaySeconds = 5;
        }
        if (this.shotSeconds < 3.0f) {
            this.shotSeconds = 3.0f;
        }
        if (this.shotSeconds > 30.0f) {
            this.shotSeconds = 30.0f;
        }
    }

    public void save() {
        try {
            Files.createDirectories(FILE.getParent(), new FileAttribute[0]);
            Files.writeString(FILE, (CharSequence)GSON.toJson((Object)this), new OpenOption[0]);
        }
        catch (Exception var2) {
            AfkCinema.LOG.warn("Konnte afkcinema.json nicht speichern", (Throwable)var2);
        }
    }
}

