/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package net.friendswithmoney.fwm.antileak;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.imageio.ImageIO;

public final class AntiLeakPack {
    private static final List<String> DEEPSLATE = List.of("deepslate", "cobbled_deepslate");
    private static final Map<String, String> ORES = new LinkedHashMap<String, String>();
    private static final List<String> STONES = List.of("granite", "diorite", "andesite", "tuff", "gravel");
    private static final Map<String, Integer> COLORMAPS;
    private static final Gson GSON;
    private static JsonObject vanilla;

    private AntiLeakPack() {
    }

    public static int packFormat() {
        return AntiLeakPack.data().get("pack_format").getAsInt();
    }

    public static void write(Path dir, Settings settings) throws IOException {
        AntiLeakPack.delete(dir);
        Files.createDirectories(dir, new FileAttribute[0]);
        AntiLeakPack.writeText(dir.resolve("pack.mcmeta"), AntiLeakPack.mcmeta());
        AntiLeakPack.writeLogo(dir.resolve("pack.png"));
        Path states = dir.resolve("assets/minecraft/blockstates");
        Files.createDirectories(states, new FileAttribute[0]);
        for (Map.Entry<String, JsonObject> entry : AntiLeakPack.blockstates(settings).entrySet()) {
            AntiLeakPack.writeText(states.resolve(entry.getKey() + ".json"), GSON.toJson((JsonElement)entry.getValue()));
        }
        if (settings.bedrock()) {
            Path models = dir.resolve("assets/minecraft/models/block");
            Files.createDirectories(models, new FileAttribute[0]);
            if (settings.deepslate()) {
                AntiLeakPack.writeText(models.resolve("bedrock.json"), AntiLeakPack.stoneModel("cube_all"));
                AntiLeakPack.writeText(models.resolve("bedrock_mirrored.json"), AntiLeakPack.stoneModel("cube_mirrored_all"));
            } else {
                AntiLeakPack.writeText(models.resolve("bedrock.json"), AntiLeakPack.deepslateModel("cube_column"));
                AntiLeakPack.writeText(models.resolve("bedrock_mirrored.json"), AntiLeakPack.deepslateModel("cube_column_mirrored"));
            }
        }
        if (settings.biome()) {
            Path maps = dir.resolve("assets/minecraft/textures/colormap");
            Files.createDirectories(maps, new FileAttribute[0]);
            for (JsonElement name : AntiLeakPack.data().getAsJsonArray("colormaps")) {
                Integer color = COLORMAPS.get(name.getAsString());
                if (color == null) continue;
                AntiLeakPack.writeFlat(maps.resolve(name.getAsString() + ".png"), color);
            }
        }
    }

    public static void delete(Path dir) throws IOException {
        if (!Files.exists(dir, new LinkOption[0])) {
            return;
        }
        try (Stream<Path> walk = Files.walk(dir, new FileVisitOption[0]);){
            for (Path path : walk.sorted(Comparator.reverseOrder()).toList()) {
                if (!path.startsWith(dir)) continue;
                Files.deleteIfExists(path);
            }
        }
    }

    static Map<String, JsonObject> blockstates(Settings settings) {
        String deep;
        LinkedHashMap<String, JsonObject> out = new LinkedHashMap<String, JsonObject>();
        JsonObject states = AntiLeakPack.data().getAsJsonObject("blockstates");
        if (settings.rotation()) {
            for (String block : states.keySet()) {
                out.put(block, AntiLeakPack.unturned(states.getAsJsonObject(block)));
            }
        }
        String string = deep = settings.deepslate() ? "stone" : "deepslate";
        if (settings.bedrock()) {
            out.put("bedrock", AntiLeakPack.shownAs(deep, settings));
        }
        if (settings.ores()) {
            ORES.forEach((ore, host) -> out.put((String)ore, AntiLeakPack.shownAs(host.equals("deepslate") ? deep : host, settings)));
        }
        if (settings.deepslate()) {
            for (String block : DEEPSLATE) {
                out.put(block, AntiLeakPack.shownAs("stone", settings));
            }
        }
        if (settings.stones()) {
            for (String stone : STONES) {
                out.put(stone, AntiLeakPack.shownAs("stone", settings));
            }
        }
        return out;
    }

    private static JsonObject shownAs(String host, Settings settings) {
        JsonElement models;
        JsonObject variants = AntiLeakPack.data().getAsJsonObject("blockstates").getAsJsonObject(host).getAsJsonObject("variants");
        JsonElement jsonElement = variants.has("") ? variants.get("") : (models = variants.has("axis=y") ? variants.get("axis=y") : (JsonElement)((Map.Entry)variants.entrySet().iterator().next()).getValue());
        if (settings.rotation()) {
            models = AntiLeakPack.first(models);
        }
        JsonObject shown = new JsonObject();
        shown.add("", models.deepCopy());
        JsonObject state = new JsonObject();
        state.add("variants", (JsonElement)shown);
        return state;
    }

    private static JsonObject unturned(JsonObject vanillaState) {
        JsonObject variants = vanillaState.getAsJsonObject("variants");
        JsonObject fixed = new JsonObject();
        for (Map.Entry entry : variants.entrySet()) {
            fixed.add((String)entry.getKey(), AntiLeakPack.first((JsonElement)entry.getValue()).deepCopy());
        }
        JsonObject state = new JsonObject();
        state.add("variants", (JsonElement)fixed);
        return state;
    }

    private static JsonElement first(JsonElement models) {
        JsonArray list;
        if (models.isJsonArray() && !(list = models.getAsJsonArray()).isEmpty()) {
            return list.get(0);
        }
        return models;
    }

    public static String mcmeta() {
        int format = AntiLeakPack.packFormat();
        JsonObject pack = new JsonObject();
        if (format >= 65) {
            pack.addProperty("min_format", (Number)format);
            pack.addProperty("max_format", (Number)format);
        } else {
            pack.addProperty("pack_format", (Number)format);
        }
        pack.addProperty("description", "By FriendsWithMoney");
        JsonObject root = new JsonObject();
        root.add("pack", (JsonElement)pack);
        return GSON.toJson((JsonElement)root);
    }

    private static String stoneModel(String parent) {
        JsonObject textures = new JsonObject();
        textures.addProperty("all", "minecraft:block/stone");
        JsonObject model = new JsonObject();
        model.addProperty("parent", "minecraft:block/" + parent);
        model.add("textures", (JsonElement)textures);
        return GSON.toJson((JsonElement)model);
    }

    private static String deepslateModel(String parent) {
        JsonObject textures = new JsonObject();
        textures.addProperty("end", "minecraft:block/deepslate_top");
        textures.addProperty("side", "minecraft:block/deepslate");
        JsonObject model = new JsonObject();
        model.addProperty("parent", "minecraft:block/" + parent);
        model.add("textures", (JsonElement)textures);
        return GSON.toJson((JsonElement)model);
    }

    private static void writeLogo(Path file) throws IOException {
        try (InputStream in = AntiLeakPack.class.getResourceAsStream("/icon.png");){
            if (in != null) {
                Files.copy(in, file, new CopyOption[0]);
            }
        }
    }

    private static void writeText(Path file, String text) throws IOException {
        Files.writeString(file, (CharSequence)text, StandardCharsets.UTF_8, new OpenOption[0]);
    }

    private static void writeFlat(Path file, int argb) throws IOException {
        BufferedImage image = new BufferedImage(256, 256, 2);
        for (int y = 0; y < 256; ++y) {
            for (int x = 0; x < 256; ++x) {
                image.setRGB(x, y, argb);
            }
        }
        try (OutputStream out = Files.newOutputStream(file, new OpenOption[0]);){
            ImageIO.write((RenderedImage)image, "png", out);
        }
    }

    private static JsonObject data() {
        if (vanilla != null) {
            return vanilla;
        }
        try (InputStream in = AntiLeakPack.class.getResourceAsStream("/assets/fwm/antileak/variants.json");){
            if (in == null) {
                throw new IllegalStateException("variants.json missing from the jar");
            }
            vanilla = JsonParser.parseReader((Reader)new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        }
        catch (IOException e) {
            throw new IllegalStateException("variants.json unreadable", e);
        }
        return vanilla;
    }

    static {
        for (String ore : List.of("coal", "iron", "copper", "gold", "redstone", "lapis", "diamond", "emerald")) {
            ORES.put(ore + "_ore", "stone");
            ORES.put("deepslate_" + ore + "_ore", "deepslate");
        }
        ORES.put("nether_gold_ore", "netherrack");
        ORES.put("nether_quartz_ore", "netherrack");
        ORES.put("ancient_debris", "netherrack");
        COLORMAPS = Map.of("grass", -7226023, "foliage", -8934609, "dry_foliage", -5993892);
        GSON = new GsonBuilder().setPrettyPrinting().create();
    }

    public record Settings(boolean bedrock, boolean ores, boolean rotation, boolean stones, boolean biome, boolean deepslate) {
        public Settings(boolean bedrock, boolean ores, boolean rotation, boolean stones, boolean biome) {
            this(bedrock, ores, rotation, stones, biome, false);
        }
    }
}

