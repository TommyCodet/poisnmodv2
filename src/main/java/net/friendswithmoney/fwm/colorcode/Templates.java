/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  net.fabricmc.loader.api.FabricLoader
 */
package net.friendswithmoney.fwm.colorcode;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.friendswithmoney.fwm.colorcode.ColorCode;

public final class Templates {
    public static final int MAX = 50;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static List<Template> list;

    private Templates() {
    }

    public static List<Template> all() {
        if (list == null) {
            Templates.load();
        }
        return list;
    }

    public static Added add(ColorCode code, ColorCode.Mode mode) {
        String serialized = code.serialize();
        if (code.text().isBlank()) {
            return Added.EMPTY;
        }
        for (Template t : Templates.all()) {
            if (!serialized.equals(t.code) || mode != t.mode()) continue;
            return Added.EXISTS;
        }
        if (Templates.all().size() >= 50) {
            return Added.FULL;
        }
        Template t = new Template();
        t.name = code.text().trim();
        t.code = serialized;
        t.mode = mode.name();
        Templates.all().add(t);
        Templates.save();
        return Added.OK;
    }

    public static void remove(Template template) {
        Templates.all().remove(template);
        Templates.save();
    }

    private static void load() {
        list = new ArrayList<Template>();
        Path path = Templates.file();
        if (!Files.exists(path, new LinkOption[0])) {
            return;
        }
        try {
            Snapshot snapshot = (Snapshot)GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), Snapshot.class);
            if (snapshot != null && snapshot.templates != null) {
                for (Template t : snapshot.templates) {
                    if (t == null || t.code == null || list.size() >= 50) continue;
                    list.add(t);
                }
            }
        }
        catch (Exception e) {
            System.err.println("[FWM] Could not read " + String.valueOf(path) + ": " + e.getMessage());
        }
    }

    private static void save() {
        Path path = Templates.file();
        try {
            Snapshot snapshot = new Snapshot();
            snapshot.templates = list;
            Files.createDirectories(path.getParent(), new FileAttribute[0]);
            Files.writeString(path, (CharSequence)GSON.toJson((Object)snapshot), StandardCharsets.UTF_8, new OpenOption[0]);
        }
        catch (Exception e) {
            System.err.println("[FWM] Could not write " + String.valueOf(path) + ": " + e.getMessage());
        }
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("fwm-farbcodes.json");
    }

    public static enum Added {
        OK,
        FULL,
        EXISTS,
        EMPTY;

    }

    public static class Template {
        public String name;
        public String code;
        public String mode;

        public ColorCode.Mode mode() {
            return "SIGN".equals(this.mode) ? ColorCode.Mode.SIGN : ColorCode.Mode.RENAME;
        }
    }

    private static class Snapshot {
        List<Template> templates;

        private Snapshot() {
        }
    }
}

