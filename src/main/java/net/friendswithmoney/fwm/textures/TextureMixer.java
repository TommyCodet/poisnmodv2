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
 *  com.mojang.blaze3d.platform.NativeImage
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.packs.PackType
 *  net.minecraft.server.packs.VanillaPackResources
 *  net.minecraft.server.packs.repository.PackRepository
 *  net.minecraft.server.packs.resources.IoSupplier
 */
package net.friendswithmoney.fwm.textures;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.CallSite;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.friendswithmoney.fwm.antileak.AntiLeakPack;
import net.friendswithmoney.fwm.config.ConfigManager;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.textures.BuiltinPacks;
import net.friendswithmoney.fwm.textures.FwmStyles;
import net.friendswithmoney.fwm.textures.IsoRender;
import net.friendswithmoney.fwm.ui.Lang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.IoSupplier;

public final class TextureMixer {
    public static final String FOLDER = "FWM Mix";
    private static final String TMP = ".fwm-mix-tmp";
    public static final String PACK_ID = "file/FWM Mix";
    public static final List<String[]> GROUPS = List.of(new String[]{"crystalpvp", "Crystal PvP", "Crystal PvP"}, new String[]{"mace", "Mace", "Mace"}, new String[]{"weapons", "Waffen & Werkzeuge", "Weapons & tools"}, new String[]{"armor", "R\u00fcstung", "Armour"}, new String[]{"ranged", "Fernkampf", "Ranged"}, new String[]{"heal", "Heilung & Items", "Healing & items"}, new String[]{"world", "Welt", "World"}, new String[]{"ui", "Oberfl\u00e4che", "Interface"}, new String[]{"misc", "Sonstiges", "Other"}, new String[]{"rest", "Rest", "Everything else"});
    private static final String M = "assets/minecraft/";
    public static final List<Category> CATEGORIES = List.of(new Category("crystal", "End-Kristall", "End crystal", "crystalpvp", List.of("textures/entity/end_crystal/", "textures/item/end_crystal.png"), "assets/minecraft/textures/entity/end_crystal/end_crystal.png"), new Category("obsidian", "Obsidian", "Obsidian", "crystalpvp", List.of("textures/block/obsidian*", "textures/block/crying_obsidian*"), "assets/minecraft/textures/block/obsidian.png"), new Category("anchor", "Respawn-Anker", "Respawn anchor", "crystalpvp", List.of("textures/block/respawn_anchor*"), "assets/minecraft/textures/block/respawn_anchor_side4.png"), new Category("glowstone", "Glowstone", "Glowstone", "crystalpvp", List.of("textures/block/glowstone.png"), "assets/minecraft/textures/block/glowstone.png"), new Category("mace", "Streitkolben", "Mace", "mace", List.of("textures/item/mace.png"), "assets/minecraft/textures/item/mace.png"), new Category("windcharge", "Windladung", "Wind charge", "mace", List.of("textures/item/wind_charge.png", "textures/entity/projectiles/wind_charge.png"), "assets/minecraft/textures/item/wind_charge.png"), new Category("breezerod", "Breeze-Rute", "Breeze rod", "mace", List.of("textures/item/breeze_rod.png"), "assets/minecraft/textures/item/breeze_rod.png"), new Category("swords", "Schwerter", "Swords", "weapons", List.of("textures/item/*_sword.png"), "assets/minecraft/textures/item/diamond_sword.png"), new Category("axes", "\u00c4xte", "Axes", "weapons", List.of("textures/item/*_axe.png"), "assets/minecraft/textures/item/diamond_axe.png"), new Category("spears", "Speere", "Spears", "weapons", List.of("textures/item/*_spear*"), "assets/minecraft/textures/item/diamond_spear.png"), new Category("pickaxes", "Spitzhacken", "Pickaxes", "weapons", List.of("textures/item/*_pickaxe.png"), "assets/minecraft/textures/item/diamond_pickaxe.png"), new Category("shovels", "Schaufeln", "Shovels", "weapons", List.of("textures/item/*_shovel.png"), "assets/minecraft/textures/item/diamond_shovel.png"), new Category("helmets", "Helme", "Helmets", "armor", List.of("textures/item/*_helmet.png"), "assets/minecraft/textures/item/diamond_helmet.png"), new Category("elytra", "Elytra", "Elytra", "armor", List.of("textures/item/elytra*", "textures/entity/equipment/wings/", "textures/entity/elytra.png"), "assets/minecraft/textures/item/elytra.png"), new Category("leggings", "Hosen", "Leggings", "armor", List.of("textures/item/*_leggings.png", "textures/entity/equipment/humanoid_leggings/", "textures/models/armor/*layer_2*"), "assets/minecraft/textures/item/diamond_leggings.png"), new Category("boots", "Schuhe", "Boots", "armor", List.of("textures/item/*_boots.png"), "assets/minecraft/textures/item/diamond_boots.png"), new Category("chestplates", "Brustplatten", "Chestplates", "armor", List.of("textures/item/*_chestplate.png", "textures/entity/equipment/", "textures/trims/", "equipment/", "textures/models/armor/", "optifine/cit/armor"), "assets/minecraft/textures/item/diamond_chestplate.png"), new Category("bows", "B\u00f6gen", "Bows", "ranged", List.of("textures/item/bow*"), "assets/minecraft/textures/item/bow.png"), new Category("crossbow", "Armbrust", "Crossbow", "ranged", List.of("textures/item/crossbow*"), "assets/minecraft/textures/item/crossbow_standby.png"), new Category("arrows", "Pfeile", "Arrows", "ranged", List.of("textures/item/*arrow*", "textures/entity/projectiles/arrow*", "textures/entity/projectiles/spectral_arrow*", "textures/entity/projectiles/tipped_arrow*"), "assets/minecraft/textures/item/arrow.png"), new Category("trident", "Dreizack", "Trident", "ranged", List.of("textures/item/trident.png", "textures/entity/trident*"), "assets/minecraft/textures/item/trident.png"), new Category("totem", "Totem", "Totem", "heal", List.of("textures/item/totem", "models/item/totem", "optifine/cit/totem*"), "assets/minecraft/textures/item/totem_of_undying.png"), new Category("apples", "Gold\u00e4pfel", "Golden apples", "heal", List.of("textures/item/golden_apple.png"), "assets/minecraft/textures/item/golden_apple.png"), new Category("opapples", "OP-\u00c4pfel", "OP apples", "heal", List.of("textures/item/enchanted_golden_apple.png"), "assets/minecraft/textures/item/enchanted_golden_apple.png"), new Category("pearls", "Perlen", "Pearls", "heal", List.of("textures/item/ender_pearl.png"), "assets/minecraft/textures/item/ender_pearl.png"), new Category("potions", "Tr\u00e4nke", "Potions", "heal", List.of("textures/item/potion*", "textures/item/splash_potion*", "textures/item/lingering_potion*"), "assets/minecraft/textures/item/potion.png"), new Category("xp", "XP", "XP", "heal", List.of("textures/item/experience_bottle.png", "textures/entity/experience_orb.png"), "assets/minecraft/textures/item/experience_bottle.png"), new Category("cobweb", "Spinnweben", "Cobwebs", "world", List.of("textures/block/cobweb.png", "models/block/cobweb.json", "blockstates/cobweb.json"), "assets/minecraft/textures/block/cobweb.png"), new Category("tnt", "TNT", "TNT", "world", List.of("textures/block/tnt_*"), "assets/minecraft/textures/block/tnt_side.png"), new Category("fire", "Feuer", "Fire", "world", List.of("textures/block/fire_*", "textures/block/soul_fire_*"), "assets/minecraft/textures/block/fire_0.png"), new Category("sky", "Himmel", "Sky", "world", List.of("textures/environment/", "optifine/sky/", "mcpatcher/sky/", "sky/"), "assets/minecraft/textures/environment/clouds.png"), new Category("deepslate", "Deepslate", "Deepslate", "world", List.of("textures/block/deepslate*"), "assets/minecraft/textures/block/deepslate.png"), new Category("stone", "Stein", "Stone", "world", List.of("textures/block/stone*", "textures/block/cobblestone*"), "assets/minecraft/textures/block/stone.png"), new Category("snow", "Schnee", "Snow", "world", List.of("textures/block/snow*", "textures/block/powder_snow*"), "assets/minecraft/textures/block/snow.png"), new Category("ores", "Erze", "Ores", "world", List.of("textures/block/*_ore*"), "assets/minecraft/textures/block/diamond_ore.png"), new Category("inventory", "Inventar", "Inventory", "ui", List.of("textures/gui/container/", "textures/gui/sprites/container/"), "assets/minecraft/textures/gui/container/inventory.png"), new Category("hud", "Hotbar / HUD", "Hotbar / HUD", "ui", List.of("textures/gui/sprites/hud/", "textures/gui/sprites/boss_bar/", "textures/gui/icons.png", "textures/gui/widgets.png", "textures/gui/bars.png"), "assets/minecraft/textures/gui/sprites/hud/heart/full.png"), new Category("font", "Schrift", "Font", "ui", List.of("font/", "textures/font/"), "assets/minecraft/textures/font/ascii.png"), new Category("particles", "Partikel", "Particles", "ui", List.of("textures/particle/", "particles/"), "assets/minecraft/textures/particle/flame.png"), new Category("effects", "Effekte", "Effects", "ui", List.of("textures/misc/enchanted_glint*", "textures/mob_effect/", "textures/effect/"), "assets/minecraft/textures/misc/enchanted_glint_armor.png"), new Category("sounds", "Sounds", "Sounds", "misc", List.of("sounds.json", "sounds/"), null), new Category("menus", "Men\u00fcs (Rest)", "Menus (rest)", "rest", List.of("textures/gui/"), "assets/minecraft/textures/gui/sprites/container/slot_highlight_front.png"), new Category("blocks", "Bl\u00f6cke (Rest)", "Blocks (rest)", "rest", List.of("textures/block/", "models/block/", "blockstates/", "optifine/ctm/", "textures/blocks/"), "assets/minecraft/textures/block/grass_block_side.png"), new Category("items", "Items (Rest)", "Items (rest)", "rest", List.of("textures/item/", "models/item/", "items/", "optifine/cit/", "textures/items/"), "assets/minecraft/textures/item/stick.png"), new Category("mobs", "Mobs (Rest)", "Mobs (rest)", "rest", List.of("textures/entity/", "optifine/random/", "optifine/mob/"), "assets/minecraft/textures/entity/creeper/creeper.png"), new Category("rest", "Rest", "Everything else", "rest", List.of(), null));
    private static final Map<String, String> OLD_PARENT = Map.ofEntries(Map.entry("anchor", "blocks"), Map.entry("fire", "blocks"), Map.entry("deepslate", "blocks"), Map.entry("stone", "blocks"), Map.entry("snow", "blocks"), Map.entry("obsidian", "blocks"), Map.entry("ores", "blocks"), Map.entry("totem", "items"), Map.entry("swords", "items"), Map.entry("bows", "items"), Map.entry("potions", "items"), Map.entry("pearls", "items"), Map.entry("apples", "items"), Map.entry("opapples", "items"), Map.entry("crystal", "mobs"), Map.entry("inventory", "menus"));
    private static final Map<String, Pattern> GLOBS = new ConcurrentHashMap<String, Pattern>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static volatile boolean building;
    private static volatile float progress;
    private static volatile String message;
    public static final Set<String> OFFERED;
    private static final Map<String, List<String>> SPLIT;
    private static final String ALL_WORLDS = "\"worlds\": [\"minecraft:overworld\", \"minecraft:the_end\", \"minecraft:the_nether\"]";
    private static final Map<String, Map<String, String>> CONTENT;
    private static final Set<String> SCANNING;
    private static final Map<String, Preview> PREVIEWS;
    private static final Set<String> MISSING;
    private static final Set<String> PENDING;
    private static volatile int previewGeneration;
    private static final ExecutorService PREVIEW_WORKER;
    private static int previewCount;
    private static final List<String> HUD_ALTS;
    private static int budget;
    private static NativeImage nextTop;
    private static int guardTicks;

    public static void migrate() {
        FwmConfig config = FwmConfig.INSTANCE;
        TextureMixer.mergeSky(config.textureMix);
        TextureMixer.mergeSky(V2.INSTANCE.textureApplied);
        if (config.textureMixVersion < 3) {
            Set<String> off;
            TextureMixer.split(config.textureMix);
            TextureMixer.split(V2.INSTANCE.textureApplied);
            if (config.textureThemes != null) {
                for (FwmConfig.TextureTheme textureTheme : config.textureThemes) {
                    TextureMixer.split(textureTheme.mix);
                }
            }
            if ((off = V2.INSTANCE.textureOff) != null) {
                for (Map.Entry<String, List<String>> entry : SPLIT.entrySet()) {
                    if (!off.remove(entry.getKey()) && !off.contains(entry.getKey())) continue;
                    off.addAll((Collection<String>)entry.getValue());
                }
            }
            if (config.textureMixVersion >= 2) {
                config.textureMixVersion = 3;
                ConfigManager.save();
                return;
            }
        }
        if (config.textureMixVersion >= 2) {
            return;
        }
        for (Map.Entry<String, String> entry : OLD_PARENT.entrySet()) {
            String string = config.textureMix.get(entry.getValue());
            if (string == null || string.isEmpty() || config.textureMix.containsKey(entry.getKey())) continue;
            config.textureMix.put(entry.getKey(), string);
        }
        config.textureMixVersion = 3;
        ConfigManager.save();
    }

    private static void mergeSky(Map<String, String> mix) {
        if (mix == null || !mix.containsKey("skyend")) {
            return;
        }
        String end = mix.remove("skyend");
        if (end != null && !end.isEmpty() && !mix.containsKey("sky")) {
            mix.put("sky", end);
        }
        ConfigManager.save();
    }

    public static Category category(String id) {
        for (Category c : CATEGORIES) {
            if (!c.id().equals(id)) continue;
            return c;
        }
        return CATEGORIES.get(0);
    }

    private static boolean fits(String inside, String prefix) {
        if (prefix.indexOf(42) < 0) {
            return inside.startsWith(prefix);
        }
        Pattern glob = GLOBS.computeIfAbsent(prefix, g -> Pattern.compile(Arrays.stream(g.split("\\*", -1)).map(Pattern::quote).reduce((x, y) -> x + "[^/]*" + y).orElse("") + (g.endsWith("/") ? ".*" : "(/.*)?")));
        return glob.matcher(inside).matches();
    }

    private TextureMixer() {
    }

    public static void register() {
    }

    public static boolean building() {
        return building;
    }

    public static float progress() {
        return progress;
    }

    public static String message() {
        return message;
    }

    private static Path packDir() {
        return Minecraft.getInstance().getResourcePackDirectory();
    }

    public static List<String> packs() {
        ArrayList<String> out = new ArrayList<String>(BuiltinPacks.ids());
        try (Stream<Path> list2 = Files.list(TextureMixer.packDir());){
            list2.forEach(path -> {
                boolean zip;
                String name = path.getFileName().toString();
                if (name.equals(FOLDER) || name.equals(TMP) || name.equals("FWM Anti-Leak") || name.equals("FWM Clan-Logos") || name.startsWith(".")) {
                    return;
                }
                boolean folder = Files.isDirectory(path, new LinkOption[0]) && Files.exists(path.resolve("pack.mcmeta"), new LinkOption[0]);
                boolean bl = zip = !Files.isDirectory(path, new LinkOption[0]) && name.toLowerCase(Locale.ROOT).endsWith(".zip");
                if (folder || zip) {
                    out.add(name);
                }
            });
        }
        catch (IOException list2) {
            // empty catch block
        }
        ArrayList own = new ArrayList(out.subList(BuiltinPacks.ids().size(), out.size()));
        own.sort(String.CASE_INSENSITIVE_ORDER);
        ArrayList<String> sorted = new ArrayList<String>(BuiltinPacks.ids());
        sorted.addAll(own);
        return sorted;
    }

    public static List<String> DISPLAY_ORDER() {
        return List.of("crystal", "obsidian", "anchor", "glowstone", "mace", "windcharge", "breezerod", "swords", "axes", "spears", "pickaxes", "shovels", "helmets", "chestplates", "leggings", "boots", "elytra", "bows", "arrows", "crossbow", "trident", "totem", "apples", "opapples", "pearls", "potions", "xp", "cobweb", "fire", "tnt", "inventory", "hud", "particles", "effects");
    }

    private static void split(Map<String, String> mix) {
        if (mix == null) {
            return;
        }
        for (Map.Entry<String, List<String>> e : SPLIT.entrySet()) {
            String pack = mix.get(e.getKey());
            if (pack == null) continue;
            if (!e.getKey().equals("bows")) {
                mix.remove(e.getKey());
            }
            for (String to : e.getValue()) {
                mix.putIfAbsent(to, pack);
            }
        }
    }

    public static boolean offered(String category) {
        return OFFERED.contains(category);
    }

    public static String chosen(String category) {
        if (!TextureMixer.offered(category)) {
            return "";
        }
        String pack = FwmConfig.INSTANCE.textureMix.get(category);
        return pack == null ? "" : pack;
    }

    public static boolean active(String category) {
        Set<String> off = V2.INSTANCE.textureOff;
        return off == null || !off.contains(category);
    }

    public static void setActive(String category, boolean on) {
        V2 v2 = V2.INSTANCE;
        if (v2.textureOff == null) {
            v2.textureOff = new HashSet<String>();
        }
        if (on) {
            v2.textureOff.remove(category);
        } else {
            v2.textureOff.add(category);
        }
        V2.save();
    }

    public static String effective(String category) {
        String pack = TextureMixer.active(category) ? TextureMixer.chosen(category) : "";
        return TextureMixer.studioOff(pack) ? "" : pack;
    }

    private static Map<String, String> effectiveMix() {
        LinkedHashMap<String, String> out = new LinkedHashMap<String, String>();
        for (Map.Entry<String, String> e : FwmConfig.INSTANCE.textureMix.entrySet()) {
            if (e.getValue() == null || e.getValue().isEmpty() || !TextureMixer.active(e.getKey()) || TextureMixer.studioOff(e.getValue())) continue;
            out.put(e.getKey(), e.getValue());
        }
        return out;
    }

    public static void choose(String category, String pack) {
        if (pack == null || pack.isEmpty()) {
            FwmConfig.INSTANCE.textureMix.remove(category);
        } else {
            FwmConfig.INSTANCE.textureMix.put(category, pack);
        }
        ConfigManager.save();
    }

    static String classify(String path) {
        if (!path.startsWith("assets/")) {
            return null;
        }
        String rest = path.substring("assets/".length());
        int slash = rest.indexOf(47);
        if (slash < 0) {
            return null;
        }
        String namespace = rest.substring(0, slash);
        String inside = rest.substring(slash + 1);
        if (namespace.startsWith(".")) {
            return null;
        }
        if (!namespace.equals("minecraft") && !namespace.equals("realms")) {
            return "sky";
        }
        for (Category category : CATEGORIES) {
            for (String prefix : category.prefixes()) {
                String model;
                if (TextureMixer.fits(inside, prefix)) {
                    return category.id();
                }
                if (!prefix.startsWith("textures/item/") || !TextureMixer.fits(inside, "models/item/" + (model = prefix.substring("textures/item/".length()).replace(".png", ".json"))) && !TextureMixer.fits(inside, "items/" + model)) continue;
                return category.id();
            }
        }
        return "rest";
    }

    public static void build(Minecraft client) {
        Map<String, String> mix = TextureMixer.effectiveMix();
        if (mix.isEmpty() && !FwmConfig.INSTANCE.textureMix.isEmpty()) {
            TextureMixer.remove(client);
            return;
        }
        TextureMixer.build(client, mix);
    }

    public static void buildCategory(Minecraft client, String category) {
        if (building) {
            return;
        }
        LinkedHashMap<String, String> choice = new LinkedHashMap<String, String>(V2.INSTANCE.textureApplied);
        String now = TextureMixer.effective(category);
        if (now.isEmpty()) {
            choice.remove(category);
        } else {
            choice.put(category, now);
        }
        choice.values().removeIf(v -> v == null || v.isEmpty());
        if (choice.isEmpty()) {
            TextureMixer.remove(client);
            return;
        }
        TextureMixer.build(client, choice);
    }

    private static void build(Minecraft client, Map<String, String> wanted) {
        if (building) {
            return;
        }
        LinkedHashMap<String, String> choice = new LinkedHashMap<String, String>(wanted);
        choice.values().removeIf(v -> v == null || v.isEmpty());
        if (choice.isEmpty()) {
            message = Lang.t("W\u00e4hle zuerst f\u00fcr mindestens eine Kategorie ein Pack.", "Choose a pack for at least one category first.");
            return;
        }
        building = true;
        progress = 0.0f;
        message = Lang.t("Baue Mix \u2026", "Building mix \u2026");
        Thread thread = new Thread(() -> {
            try {
                TextureMixer.write(choice);
                client.execute(() -> {
                    building = false;
                    TextureMixer.apply(client, new LinkedHashSet<String>(choice.values()));
                    V2.INSTANCE.textureApplied = new HashMap<String, String>(choice);
                    V2.save();
                });
            }
            catch (Exception e) {
                System.err.println("[FWM] Texture mix failed: " + String.valueOf(e));
                building = false;
                message = Lang.t("Das hat nicht geklappt. Ein Pack ist evtl. kaputt oder noch offen.", "That did not work. A pack may be broken or still open.");
            }
        }, "FWM texture mixer");
        thread.setDaemon(true);
        thread.start();
    }

    private static void copyDependencies(Path root, Path tmp, List<String> start) {
        ArrayDeque<String> todo = new ArrayDeque<String>(start);
        HashSet<String> seen = new HashSet<String>(start);
        int guard = 0;
        while (!todo.isEmpty() && guard++ < 5000) {
            JsonElement json;
            String rel = todo.poll();
            try {
                json = JsonParser.parseString((String)Files.readString(TextureMixer.safe(tmp, rel), StandardCharsets.UTF_8));
            }
            catch (Exception e) {
                continue;
            }
            ArrayList<String> refs = new ArrayList<String>();
            TextureMixer.collectRefs(json, null, refs);
            for (String ref : refs) {
                for (String dep : TextureMixer.candidates(ref)) {
                    if (!seen.add(dep)) continue;
                    try {
                        Path out;
                        Path from = root.resolve(dep);
                        if (!Files.isRegularFile(from, new LinkOption[0]) || Files.exists(out = TextureMixer.safe(tmp, dep), new LinkOption[0])) continue;
                        Files.createDirectories(out.getParent(), new FileAttribute[0]);
                        Files.copy(from, out, new CopyOption[0]);
                        if (dep.endsWith(".json")) {
                            todo.add(dep);
                        }
                        Path meta = root.resolve(dep + ".mcmeta");
                        if (!dep.endsWith(".png") || !Files.isRegularFile(meta, new LinkOption[0])) continue;
                        Files.copy(meta, TextureMixer.safe(tmp, dep + ".mcmeta"), new CopyOption[0]);
                    }
                    catch (Exception exception) {}
                }
            }
        }
    }

    private static void collectRefs(JsonElement e, String key, List<String> out) {
        if (e == null) {
            return;
        }
        if (e.isJsonObject()) {
            for (Map.Entry m : e.getAsJsonObject().entrySet()) {
                if (((String)m.getKey()).equals("textures") && ((JsonElement)m.getValue()).isJsonObject()) {
                    for (Map.Entry t : ((JsonElement)m.getValue()).getAsJsonObject().entrySet()) {
                        if (!((JsonElement)t.getValue()).isJsonPrimitive()) continue;
                        out.add("tex:" + ((JsonElement)t.getValue()).getAsString());
                    }
                    continue;
                }
                TextureMixer.collectRefs((JsonElement)m.getValue(), (String)m.getKey(), out);
            }
        } else if (e.isJsonArray()) {
            for (JsonElement x : e.getAsJsonArray()) {
                TextureMixer.collectRefs(x, key, out);
            }
        } else if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isString() && ("parent".equals(key) || "model".equals(key))) {
            out.add("model:" + e.getAsString());
        }
    }

    private static List<String> candidates(String ref) {
        boolean tex = ref.startsWith("tex:");
        String id = ref.substring(ref.indexOf(58) + 1);
        if (id.startsWith("#") || id.isEmpty()) {
            return List.of();
        }
        String ns = "minecraft";
        String path = id;
        int colon = id.indexOf(58);
        if (colon >= 0) {
            ns = id.substring(0, colon);
            path = id.substring(colon + 1);
        }
        if (path.contains("..")) {
            return List.of();
        }
        if (tex) {
            return List.of("assets/" + ns + "/textures/" + path + ".png");
        }
        return List.of("assets/" + ns + "/models/" + path + ".json", "assets/" + ns + "/items/" + path + ".json");
    }

    private static void mirrorSky(Path tmp) throws IOException {
        for (String kind : List.of("optifine", "mcpatcher")) {
            Path sky = tmp.resolve(M + kind + "/sky");
            Path world0 = sky.resolve("world0");
            if (!Files.isDirectory(world0, new LinkOption[0])) continue;
            for (String dim : List.of("world1", "world-1")) {
                Path target = sky.resolve(dim).normalize();
                if (!target.startsWith(tmp.normalize())) continue;
                if (Files.exists(target, new LinkOption[0])) {
                    try (Stream<Path> old = Files.walk(target, new FileVisitOption[0]);){
                        for (Path p2 : old.sorted(Comparator.reverseOrder()).toList()) {
                            Files.deleteIfExists(p2);
                        }
                    }
                }
                Stream<Path> walk = Files.walk(world0, new FileVisitOption[0]);
                try {
                    for (Path p2 : walk.toList()) {
                        Path out = target.resolve(world0.relativize(p2).toString());
                        if (Files.isDirectory(p2, new LinkOption[0])) {
                            Files.createDirectories(out, new FileAttribute[0]);
                            continue;
                        }
                        Files.copy(p2, out, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
                finally {
                    if (walk == null) continue;
                    walk.close();
                }
            }
        }
        Path fsb = tmp.resolve("assets/fabricskyboxes");
        if (Files.isDirectory(fsb, new LinkOption[0])) {
            List<Path> jsons;
            try (Stream<Path> walk = Files.walk(fsb, new FileVisitOption[0]);){
                jsons = walk.filter(p -> p.toString().endsWith(".json")).toList();
            }
            for (Path json : jsons) {
                String text = Files.readString(json, StandardCharsets.UTF_8);
                String all = text.replaceAll("\"worlds\"\\s*:\\s*\\[[^\\]]*\\]", Matcher.quoteReplacement(ALL_WORLDS));
                if (all.equals(text)) continue;
                Files.writeString(json, (CharSequence)all, StandardCharsets.UTF_8, new OpenOption[0]);
            }
        }
    }

    private static void write(Map<String, String> choice) throws IOException {
        Path base = TextureMixer.packDir();
        Path tmp = base.resolve(TMP);
        Path target = base.resolve(FOLDER);
        TextureMixer.deleteTree(tmp);
        Files.createDirectories(tmp, new FileAttribute[0]);
        HashMap<String, FwmStyles.Style> styled = new HashMap<String, FwmStyles.Style>();
        for (Map.Entry<String, String> e : choice.entrySet()) {
            FwmStyles.Style style = FwmStyles.byKey(e.getValue());
            if (style == null) continue;
            styled.put(e.getKey(), style);
        }
        if (!styled.isEmpty()) {
            TextureMixer.writeStyles(tmp, styled);
        }
        LinkedHashSet<String> packs = new LinkedHashSet<String>(choice.values());
        packs.removeIf(FwmStyles::isStyle);
        LinkedHashMap<String, JsonArray> atlases = new LinkedHashMap<String, JsonArray>();
        HashMap<String, Integer> formats = new HashMap<String, Integer>();
        int done = 0;
        for (String string : packs) {
            Source source = new Source(string);
            try {
                List<Path> files;
                Path assets = source.root.resolve("assets");
                if (!Files.isDirectory(assets, new LinkOption[0])) continue;
                try (Stream<Path> walk = Files.walk(assets, new FileVisitOption[0]);){
                    files = walk.filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).toList();
                }
                ArrayList<String> copiedJson = new ArrayList<String>();
                for (Path file : files) {
                    String rel = source.root.relativize(file).toString().replace('\\', '/');
                    if (rel.startsWith("/")) {
                        rel = rel.substring(1);
                    }
                    if (rel.matches("assets/[^/]+/atlases/[^/]+\\.json")) {
                        TextureMixer.mergeAtlas(atlases, rel, file);
                        continue;
                    }
                    String category = TextureMixer.classify(rel);
                    if (category == null || !string.equals(choice.get(category))) continue;
                    Path out = TextureMixer.safe(tmp, rel);
                    Files.createDirectories(out.getParent(), new FileAttribute[0]);
                    Files.copy(file, out, StandardCopyOption.REPLACE_EXISTING);
                    if (!rel.endsWith(".json")) continue;
                    copiedJson.add(rel);
                }
                TextureMixer.copyDependencies(source.root, tmp, copiedJson);
                formats.put(string, TextureMixer.packFormat(source.root));
            }
            finally {
                source.close();
                continue;
            }
            progress = (float)(++done) / (float)(packs.size() + 1);
        }
        if (choice.containsKey("sky")) {
            TextureMixer.mirrorSky(tmp);
        }
        for (Map.Entry entry : atlases.entrySet()) {
            JsonObject root = new JsonObject();
            root.add("sources", (JsonElement)entry.getValue());
            Path out = TextureMixer.safe(tmp, (String)entry.getKey());
            Files.createDirectories(out.getParent(), new FileAttribute[0]);
            Files.writeString(out, (CharSequence)GSON.toJson((JsonElement)root), StandardCharsets.UTF_8, new OpenOption[0]);
        }
        Files.writeString(tmp.resolve("pack.mcmeta"), (CharSequence)AntiLeakPack.mcmeta(), StandardCharsets.UTF_8, new OpenOption[0]);
        try (InputStream logo = TextureMixer.class.getResourceAsStream("/assets/fwm/textures/gui/logo.png");){
            if (logo != null) {
                Files.copy(logo, tmp.resolve("pack.png"), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        TextureMixer.deleteTree(target);
        try {
            Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE);
        }
        catch (IOException e) {
            Files.move(tmp, target, new CopyOption[0]);
        }
        int current = AntiLeakPack.packFormat();
        ArrayList arrayList = new ArrayList();
        formats.forEach((pack, format) -> {
            if (format > 0 && format < current) {
                older.add(pack);
            }
        });
        progress = 1.0f;
        message = arrayList.isEmpty() ? Lang.t("Mix gebaut.", "Mix built.") : Lang.t("Mix gebaut. Pack f\u00fcr \u00e4ltere Version: ", "Mix built. Pack for an older version: ") + String.join((CharSequence)", ", arrayList);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void writeStyles(Path tmp, Map<String, FwmStyles.Style> styled) throws IOException {
        VanillaPackResources vanilla = Minecraft.getInstance().getVanillaPackResources();
        PackType type = PackType.CLIENT_RESOURCES;
        ArrayList ids = new ArrayList();
        vanilla.listResources(type, "minecraft", "textures", (id, supplier) -> {
            if (id.getPath().endsWith(".png")) {
                ids.add(id);
            }
        });
        for (Identifier id2 : ids) {
            NativeImage src;
            IoSupplier supplier2;
            String rel = M + id2.getPath();
            FwmStyles.Style style = styled.get(TextureMixer.classify(rel));
            if (style == null || (supplier2 = vanilla.getResource(type, id2)) == null) continue;
            try (InputStream in = (InputStream)supplier2.get();){
                src = NativeImage.read((InputStream)in);
            }
            catch (Exception e) {
                continue;
            }
            Path out = TextureMixer.safe(tmp, rel);
            Files.createDirectories(out.getParent(), new FileAttribute[0]);
            try (NativeImage done = FwmStyles.apply(style, src);){
                done.writeToFile(out);
            }
            finally {
                src.close();
            }
            IoSupplier meta = vanilla.getResource(type, Identifier.withDefaultNamespace((String)(id2.getPath() + ".mcmeta")));
            if (meta == null) continue;
            try {
                InputStream in = (InputStream)meta.get();
                try {
                    Files.copy(in, TextureMixer.safe(tmp, rel + ".mcmeta"), StandardCopyOption.REPLACE_EXISTING);
                }
                finally {
                    if (in == null) continue;
                    in.close();
                }
            }
            catch (Exception exception) {}
        }
    }

    private static void mergeAtlas(Map<String, JsonArray> atlases, String rel, Path file) {
        try {
            JsonObject json = JsonParser.parseString((String)Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonArray sources = json.has("sources") ? json.getAsJsonArray("sources") : new JsonArray();
            JsonArray merged = atlases.computeIfAbsent(rel, k -> new JsonArray());
            LinkedHashSet<String> seen = new LinkedHashSet<String>();
            for (JsonElement have : merged) {
                seen.add(have.toString());
            }
            for (JsonElement add : sources) {
                if (!seen.add(add.toString())) continue;
                merged.add(add);
            }
        }
        catch (Exception e) {
            System.err.println("[FWM] Skipping broken atlas " + rel + ": " + e.getMessage());
        }
    }

    private static int packFormat(Path root) {
        try {
            Path meta = root.resolve("pack.mcmeta");
            if (!Files.exists(meta, new LinkOption[0])) {
                return 0;
            }
            JsonObject pack = JsonParser.parseString((String)Files.readString(meta, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("pack");
            if (pack == null) {
                return 0;
            }
            if (pack.has("pack_format")) {
                return pack.get("pack_format").getAsInt();
            }
            if (pack.has("min_format")) {
                JsonElement min = pack.get("min_format");
                return min.isJsonArray() ? min.getAsJsonArray().get(0).getAsInt() : min.getAsInt();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return 0;
    }

    private static Path safe(Path base, String rel) throws IOException {
        Path out = base.resolve(rel).normalize();
        if (!out.startsWith(base.normalize()) || rel.contains("..")) {
            throw new IOException("Path outside the mix folder: " + rel);
        }
        return out;
    }

    private static void deleteTree(Path dir) throws IOException {
        String name = dir.getFileName().toString();
        if (!name.equals(FOLDER) && !name.equals(TMP)) {
            throw new IOException("Refusing to delete " + String.valueOf(dir));
        }
        if (!Files.exists(dir, new LinkOption[0])) {
            return;
        }
        try (Stream<Path> walk = Files.walk(dir, new FileVisitOption[0]);){
            for (Path p : walk.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(p);
            }
        }
    }

    private static void apply(Minecraft client, Set<String> sources) {
        PackRepository manager = client.getResourcePackRepository();
        FwmConfig config = FwmConfig.INSTANCE;
        manager.reload();
        ArrayList<String> enabled = new ArrayList<String>(manager.getSelectedIds());
        boolean wasOn = enabled.contains(PACK_ID);
        if (config.textureMixBefore == null) {
            ArrayList<String> before = new ArrayList<String>(enabled);
            before.remove(PACK_ID);
            config.textureMixBefore = before;
        }
        enabled.remove(PACK_ID);
        if (config.textureMixDisableSources) {
            for (String pack : sources) {
                enabled.remove("file/" + pack);
            }
        }
        enabled.add(PACK_ID);
        manager.setSelected(enabled);
        client.options.updateResourcePacks(manager);
        if (wasOn) {
            client.reloadResourcePacks();
        }
        ConfigManager.save();
        message = message + Lang.t(" Aktiviert.", " Switched on.");
    }

    public static void remove(Minecraft client) {
        if (building) {
            return;
        }
        PackRepository manager = client.getResourcePackRepository();
        FwmConfig config = FwmConfig.INSTANCE;
        manager.reload();
        ArrayList<String> enabled = new ArrayList<String>(manager.getSelectedIds());
        enabled.remove(PACK_ID);
        if (config.textureMixBefore != null) {
            for (String id : config.textureMixBefore) {
                if (enabled.contains(id) || !manager.isAvailable(id)) continue;
                enabled.add(id);
            }
            config.textureMixBefore = null;
        }
        manager.setSelected(enabled);
        client.options.updateResourcePacks(manager);
        try {
            TextureMixer.deleteTree(TextureMixer.packDir().resolve(FOLDER));
        }
        catch (IOException e) {
            System.err.println("[FWM] Could not delete the mix: " + e.getMessage());
        }
        ConfigManager.save();
        V2.INSTANCE.textureApplied = new HashMap<String, String>();
        V2.save();
        message = Lang.t("Alles wieder Vanilla.", "Everything vanilla again.");
    }

    public static boolean active(Minecraft client) {
        return client.getResourcePackRepository().getSelectedIds().contains(PACK_ID);
    }

    public static Map<String, String> content(String pack) {
        Map<String, String> have = CONTENT.get(pack);
        if (have != null) {
            return have;
        }
        if (SCANNING.add(pack)) {
            Thread thread = new Thread(() -> {
                HashMap found;
                block13: {
                    found = new HashMap();
                    try (Source source = new Source(pack);){
                        Path assets = source.root.resolve("assets");
                        if (!Files.isDirectory(assets, new LinkOption[0])) break block13;
                        try (Stream<Path> walk = Files.walk(assets, new FileVisitOption[0]);){
                            walk.filter(x$0 -> Files.isRegularFile(x$0, new LinkOption[0])).forEach(file -> {
                                String category;
                                String rel = source.root.relativize((Path)file).toString().replace('\\', '/');
                                if (rel.startsWith("/")) {
                                    rel = rel.substring(1);
                                }
                                if ((category = TextureMixer.classify(rel)) == null) {
                                    return;
                                }
                                String before = (String)found.get(category);
                                boolean png = rel.endsWith(".png");
                                if (before == null || before.isEmpty() && png) {
                                    found.put(category, png ? rel : "");
                                } else if (png && category.equals("sky") && TextureMixer.skyScore(rel) > TextureMixer.skyScore(before)) {
                                    found.put(category, rel);
                                } else if (png && category.equals("chestplates") && TextureMixer.armorScore(rel) > TextureMixer.armorScore(before)) {
                                    found.put(category, rel);
                                }
                            });
                        }
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                CONTENT.put(pack, found);
                SCANNING.remove(pack);
            }, "FWM texture index");
            thread.setDaemon(true);
            thread.start();
        }
        return null;
    }

    private static int skyScore(String rel) {
        if (rel == null || !rel.endsWith(".png")) {
            return -1;
        }
        String r = rel.toLowerCase(Locale.ROOT);
        if (r.contains("star")) {
            return 2;
        }
        if (r.contains("/sky/world0/") || r.startsWith("assets/fabricskyboxes/") || !r.startsWith(M)) {
            return 3;
        }
        if (r.endsWith("end_sky.png")) {
            return 1;
        }
        if (r.endsWith("clouds.png")) {
            return 0;
        }
        return 1;
    }

    private static String modelTexture(Path root, String previewPath) {
        String name = previewPath.substring(previewPath.lastIndexOf(47) + 1).replace(".png", "");
        ArrayDeque<CallSite> todo = new ArrayDeque<CallSite>(List.of("assets/minecraft/items/" + name + ".json", "assets/minecraft/models/item/" + name + ".json"));
        HashSet<String> seen = new HashSet<String>();
        while (!todo.isEmpty() && seen.size() < 24) {
            JsonElement json;
            Path f;
            String rel = (String)((Object)todo.poll());
            if (!seen.add(rel) || !Files.isRegularFile(f = root.resolve(rel), new LinkOption[0])) continue;
            try {
                json = JsonParser.parseString((String)Files.readString(f, StandardCharsets.UTF_8));
            }
            catch (Exception e) {
                continue;
            }
            ArrayList<String> refs = new ArrayList<String>();
            TextureMixer.collectRefs(json, null, refs);
            if (json.isJsonObject() && json.getAsJsonObject().has("textures") && json.getAsJsonObject().get("textures").isJsonObject()) {
                JsonObject textures = json.getAsJsonObject().getAsJsonObject("textures");
                ArrayList<String> order = new ArrayList<String>();
                if (textures.has("layer0")) {
                    order.add("layer0");
                }
                for (String k : textures.keySet()) {
                    if (k.equals("layer0") || k.equals("particle")) continue;
                    order.add(k);
                }
                for (String k : order) {
                    if (!textures.get(k).isJsonPrimitive()) continue;
                    for (String c : TextureMixer.candidates("tex:" + textures.get(k).getAsString())) {
                        if (!Files.isRegularFile(root.resolve(c), new LinkOption[0])) continue;
                        return c;
                    }
                }
            }
            for (String ref : refs) {
                if (!ref.startsWith("model:")) continue;
                todo.addAll(TextureMixer.candidates(ref));
            }
        }
        if (name.equals("enchanted_golden_apple") && Files.isRegularFile(root.resolve("assets/minecraft/textures/item/golden_apple.png"), new LinkOption[0])) {
            return "assets/minecraft/textures/item/golden_apple.png";
        }
        return null;
    }

    private static int armorScore(String rel) {
        if (rel == null || !rel.endsWith(".png")) {
            return -1;
        }
        String r = rel.toLowerCase(Locale.ROOT);
        if (r.contains("/textures/item/") && r.endsWith("_chestplate.png")) {
            return 4;
        }
        if (TextureMixer.wornChest(r)) {
            return 3;
        }
        return 1;
    }

    private static boolean wornChest(String r) {
        return r.contains("/equipment/humanoid/") && !r.contains("leggings") || r.contains("/models/armor/") && r.contains("layer_1");
    }

    public static int packsWith(List<String> packs, String category) {
        int n = 0;
        for (String pack : packs) {
            Map<String, String> c = TextureMixer.content(pack);
            if (c == null || !c.containsKey(category)) continue;
            ++n;
        }
        return n;
    }

    public static boolean noPicture(String pack, Category category) {
        String own;
        if (pack == null || pack.isEmpty() || FwmStyles.isStyle(pack)) {
            return false;
        }
        String path = category.preview();
        Map<String, String> content = CONTENT.get(pack);
        String string = own = content == null ? null : content.get(category.id());
        if (path == null) {
            path = own;
        }
        if (category.id().equals("sky") && own != null && TextureMixer.skyScore(own) >= 2) {
            path = own;
        }
        if (category.id().equals("chestplates") && own != null && TextureMixer.armorScore(own) == 3) {
            path = own;
        }
        if (path == null || path.isEmpty()) {
            return true;
        }
        return MISSING.contains(pack + "|" + path);
    }

    /*
     * Exception decompiling
     */
    public static Preview preview(String pack, Category category) {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Started 2 blocks at once
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

    private static int[] sheetCrop(String path) {
        if (path.endsWith("textures/gui/icons.png")) {
            return new int[]{52, 0, 9, 9};
        }
        if (path.endsWith("textures/gui/widgets.png")) {
            return new int[]{0, 0, 182, 22};
        }
        if (path.endsWith("optifine/sky/world0/sky1.png")) {
            return new int[]{85, 85, 85, 85};
        }
        if (TextureMixer.wornChest(path.toLowerCase(Locale.ROOT))) {
            return new int[]{80, 80, 32, 48};
        }
        return null;
    }

    public static void previewBudget(int n) {
        budget = n;
    }

    public static Preview vanillaPreview(Category category) {
        Preview preview;
        block14: {
            String path = category.preview();
            if (path != null && path.endsWith("enchanted_golden_apple.png")) {
                path = "assets/minecraft/textures/item/golden_apple.png";
            }
            if (path == null || !path.startsWith(M)) {
                return null;
            }
            String key = "|vanilla|" + path;
            Preview have = PREVIEWS.get(key);
            if (have != null || MISSING.contains(key)) {
                return have;
            }
            if (PENDING.contains(key)) {
                return null;
            }
            if (budget <= 0) {
                return null;
            }
            --budget;
            IoSupplier supplier = Minecraft.getInstance().getVanillaPackResources().getResource(PackType.CLIENT_RESOURCES, Identifier.withDefaultNamespace((String)path.substring(M.length())));
            if (supplier == null) {
                MISSING.add(key);
                return null;
            }
            nextTop = TextureMixer.anchorTop(path, null);
            InputStream in = (InputStream)supplier.get();
            try {
                preview = TextureMixer.register(key, in);
                if (in == null) break block14;
            }
            catch (Throwable throwable) {
                try {
                    if (in != null) {
                        try {
                            in.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (Exception e) {
                    MISSING.add(key);
                    return null;
                }
            }
            in.close();
        }
        return preview;
    }

    /*
     * Enabled aggressive exception aggregation
     */
    private static Preview stylePreview(FwmStyles.Style style, Category category) {
        if (style == null) {
            return null;
        }
        String path = category.preview();
        if (path == null || !path.startsWith(M)) {
            return null;
        }
        String key = style.key() + "|" + path;
        Preview have = PREVIEWS.get(key);
        if (have != null || MISSING.contains(key)) {
            return have;
        }
        if (PENDING.contains(key)) {
            return null;
        }
        if (budget <= 0) {
            return null;
        }
        --budget;
        try {
            IoSupplier supplier = Minecraft.getInstance().getVanillaPackResources().getResource(PackType.CLIENT_RESOURCES, Identifier.withDefaultNamespace((String)path.substring(M.length())));
            if (supplier == null) {
                MISSING.add(key);
                return null;
            }
            NativeImage top = TextureMixer.anchorTop(path, null);
            if (top != null) {
                nextTop = FwmStyles.apply(style, top);
                top.close();
            }
            try (InputStream in = (InputStream)supplier.get();){
                Preview preview;
                block21: {
                    NativeImage src = NativeImage.read((InputStream)in);
                    try {
                        preview = TextureMixer.registerImage(key, FwmStyles.apply(style, src));
                        if (src == null) break block21;
                    }
                    catch (Throwable throwable) {
                        if (src != null) {
                            try {
                                src.close();
                            }
                            catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    }
                    src.close();
                }
                return preview;
            }
        }
        catch (Exception e) {
            MISSING.add(key);
            return null;
        }
    }

    public static boolean pending() {
        HashMap<String, String> now = new HashMap<String, String>(TextureMixer.effectiveMix());
        return !now.equals(V2.INSTANCE.textureApplied);
    }

    public static boolean pending(String category) {
        String applied;
        String now = TextureMixer.effective(category);
        return !now.equals(applied = V2.INSTANCE.textureApplied.getOrDefault(category, ""));
    }

    public static void reset(Minecraft client) {
        FwmConfig.INSTANCE.textureMix.clear();
        ConfigManager.save();
        TextureMixer.remove(client);
    }

    private static Preview register(String key, InputStream in) throws IOException {
        return TextureMixer.registerImage(key, NativeImage.read((InputStream)in));
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static NativeImage anchorTop(String path, Path packFile) {
        if (path == null) return null;
        if (!path.endsWith("respawn_anchor_side4.png")) {
            return null;
        }
        try {
            if (packFile != null && Files.exists(packFile, new LinkOption[0])) {
                try (InputStream in = Files.newInputStream(packFile, new OpenOption[0]);){
                    NativeImage nativeImage = NativeImage.read((InputStream)in);
                    return nativeImage;
                }
            }
            IoSupplier supplier = Minecraft.getInstance().getVanillaPackResources().getResource(PackType.CLIENT_RESOURCES, Identifier.withDefaultNamespace((String)"textures/block/respawn_anchor_top.png"));
            if (supplier == null) {
                return null;
            }
            try (InputStream in = (InputStream)supplier.get();){
                NativeImage nativeImage = NativeImage.read((InputStream)in);
                return nativeImage;
            }
        }
        catch (Exception e) {
            return null;
        }
    }

    private static Preview registerImage(String key, NativeImage image) {
        NativeImage top = nextTop;
        nextTop = null;
        String path = key.substring(key.lastIndexOf(124) + 1);
        if (path.endsWith("textures/entity/end_crystal/end_crystal.png") || path.contains("textures/misc/enchanted_glint")) {
            if (!PENDING.add(key)) {
                image.close();
                if (top != null) {
                    top.close();
                }
                return null;
            }
            int generation = previewGeneration;
            NativeImage src = image;
            CompletableFuture.supplyAsync(() -> IsoRender.render(null, path, src, top), PREVIEW_WORKER).whenComplete((made, error) -> Minecraft.getInstance().execute(() -> {
                PENDING.remove(key);
                if (top != null) {
                    top.close();
                }
                if (generation != previewGeneration || error != null) {
                    src.close();
                    if (made != null) {
                        made.close();
                    }
                    if (error != null) {
                        MISSING.add(key);
                    }
                    return;
                }
                TextureMixer.finishPreview(key, src, made);
            }));
            return null;
        }
        NativeImage built = IsoRender.render(null, path, image, top);
        if (top != null) {
            top.close();
        }
        return TextureMixer.finishPreview(key, image, built);
    }

    private static Preview finishPreview(String key, NativeImage image, NativeImage built) {
        int frameH;
        if (built != null) {
            image.close();
            image = built;
        }
        int w = image.getWidth();
        int h = image.getHeight();
        int n = frameH = h > w && h % w == 0 ? w : h;
        if (built != null && key.contains("enchanted_glint")) {
            frameH = h / 16;
        }
        Identifier id = Identifier.fromNamespaceAndPath((String)"fwm", (String)("mixpreview/" + previewCount++));
        Minecraft.getInstance().getTextureManager().register(id, (AbstractTexture)new DynamicTexture(() -> "fwm mix preview", image));
        Preview preview = new Preview(id, w, frameH, h);
        PREVIEWS.put(key, preview);
        return preview;
    }

    public static void releasePreviews() {
        ++previewGeneration;
        for (Preview preview : PREVIEWS.values()) {
            Minecraft.getInstance().getTextureManager().release(preview.id());
        }
        PREVIEWS.clear();
        MISSING.clear();
    }

    public static void forget(String pack) {
        CONTENT.remove(pack);
        PREVIEWS.entrySet().removeIf(e -> {
            if (!((String)e.getKey()).startsWith(pack + "|")) {
                return false;
            }
            Minecraft.getInstance().getTextureManager().release(((Preview)e.getValue()).id());
            return true;
        });
        MISSING.removeIf(k -> k.startsWith(pack + "|"));
    }

    public static void clearPreviews() {
        for (Preview preview : PREVIEWS.values()) {
            Minecraft.getInstance().getTextureManager().release(preview.id());
        }
        PREVIEWS.clear();
        MISSING.clear();
        CONTENT.clear();
    }

    public static boolean favourite(String pack) {
        return FwmConfig.INSTANCE.textureFavorites.contains(pack);
    }

    public static void toggleFavourite(String pack) {
        List<String> favs = FwmConfig.INSTANCE.textureFavorites;
        if (!favs.remove(pack)) {
            favs.add(pack);
        }
        ConfigManager.save();
    }

    public static void saveTheme(String name) {
        String n;
        String string = n = name == null ? "" : name.trim();
        if (n.isEmpty()) {
            return;
        }
        FwmConfig.INSTANCE.textureThemes.removeIf(t -> n.equalsIgnoreCase(t.name));
        FwmConfig.TextureTheme theme = new FwmConfig.TextureTheme();
        theme.name = n;
        theme.mix = new LinkedHashMap<String, String>(FwmConfig.INSTANCE.textureMix);
        FwmConfig.INSTANCE.textureThemes.add(theme);
        ConfigManager.save();
    }

    public static void applyTheme(FwmConfig.TextureTheme theme) {
        FwmConfig.INSTANCE.textureMix.clear();
        if (theme != null && theme.mix != null) {
            FwmConfig.INSTANCE.textureMix.putAll(theme.mix);
        }
        ConfigManager.save();
    }

    static boolean studioOff(String pack) {
        return pack != null && pack.startsWith("FWM Totem ") && Features.switchedOff("fwm.tab.totemstudio");
    }

    public static void registerGuard() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (++guardTicks < 40 || building) {
                return;
            }
            guardTicks = 0;
            try {
                if (Features.switchedOff("fwm.tab.textures")) {
                    if (TextureMixer.active(client)) {
                        TextureMixer.remove(client);
                        client.reloadResourcePacks();
                    }
                    return;
                }
                Map<String, String> applied = V2.INSTANCE.textureApplied;
                if (applied == null || !TextureMixer.active(client)) {
                    return;
                }
                for (Map.Entry<String, String> e : new ArrayList<Map.Entry<String, String>>(applied.entrySet())) {
                    if (!TextureMixer.studioOff(e.getValue())) continue;
                    TextureMixer.buildCategory(client, e.getKey());
                    return;
                }
            }
            catch (Exception e) {
                System.err.println("[FWM] Texture guard: " + String.valueOf(e));
            }
        });
    }

    static {
        message = "";
        OFFERED = Set.copyOf(TextureMixer.DISPLAY_ORDER());
        SPLIT = Map.of("tools", List.of("axes", "pickaxes", "shovels"), "armor", List.of("helmets", "chestplates", "leggings", "boots"), "bows", List.of("bows", "crossbow", "arrows"));
        CONTENT = new ConcurrentHashMap<String, Map<String, String>>();
        SCANNING = ConcurrentHashMap.newKeySet();
        PREVIEWS = new ConcurrentHashMap<String, Preview>();
        MISSING = ConcurrentHashMap.newKeySet();
        PENDING = ConcurrentHashMap.newKeySet();
        PREVIEW_WORKER = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "FWM texture previews");
            t.setDaemon(true);
            t.setPriority(1);
            return t;
        });
        HUD_ALTS = List.of("assets/minecraft/textures/gui/sprites/hud/hotbar.png", "assets/minecraft/textures/gui/icons.png", "assets/minecraft/textures/gui/widgets.png");
        budget = Integer.MAX_VALUE;
    }

    public record Category(String id, String de, String en, String group, List<String> prefixes, String preview) {
        public String label() {
            return Lang.t(this.de, this.en);
        }

        public boolean tiled() {
            return this.preview != null && this.preview.contains("/block/");
        }
    }

    static final class Source
    implements AutoCloseable {
        final Path root;
        final FileSystem zip;

        Source(String name) throws IOException {
            Path path;
            Path path2 = path = BuiltinPacks.is(name) ? BuiltinPacks.file(name) : TextureMixer.packDir().resolve(name);
            if (Files.isDirectory(path, new LinkOption[0])) {
                this.root = path;
                this.zip = null;
            } else {
                this.zip = FileSystems.newFileSystem(path);
                this.root = this.zip.getPath("/", new String[0]);
            }
        }

        @Override
        public void close() throws IOException {
            if (this.zip != null) {
                this.zip.close();
            }
        }
    }

    public record Preview(Identifier id, int width, int frameHeight, int height) {
    }
}

