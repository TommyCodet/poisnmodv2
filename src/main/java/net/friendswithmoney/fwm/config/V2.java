/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  net.fabricmc.loader.api.FabricLoader
 */
package net.friendswithmoney.fwm.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;

public final class V2 {
    public static V2 INSTANCE = new V2();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public boolean singleplayerOn = false;
    public boolean chatJsonLog = false;
    public boolean modIdConsent = false;
    public int animations = 2;
    public String updateAnnounced = "";
    public boolean keysMoved = false;
    public boolean whatsNewSeen = false;
    public List<String> marketWatch = new ArrayList<String>();
    public boolean marketWatchOnly = false;
    public List<String> hiddenTiles = new ArrayList<String>();
    public boolean showHidden = true;
    public List<String> invseeHiddenItems = new ArrayList<String>();
    public boolean invseeShowZero = false;
    public Set<String> textureOff = new HashSet<String>();
    public boolean designMinecraft = false;
    public boolean showHiddenMigrated = false;
    public boolean smoothFont = true;
    public boolean menuBlur = true;
    public boolean hitColorOn = false;
    public int hitColor = 8253638;
    public int hitAlpha = 178;
    public boolean totemPopOn = false;
    public int totemColorA = 0xFFD000;
    public int totemColorB = 8253638;
    public boolean totemHoldUp = false;
    public boolean popChamsOn = true;
    public int popChamsColor = 10181887;
    public int popChamsTenths = 10;
    public boolean popParticles = true;
    public int popParticleAmount = 100;
    public int popChamsMode = 2;
    public int popChamsFillAlpha = 38;
    public int popChamsEdgeColor = -1;
    public int popChamsEdge = 2;
    public String skyPack = "";
    public boolean skySunMoon = false;
    public boolean skyNight = true;
    public boolean skyEnd = true;
    public int skySpin = 0;
    public boolean skyMigrated = false;
    public String totemSkin = "";
    public int totemSize = 16;
    public boolean totemHat = true;
    public boolean totemOutline = false;
    public boolean totem3d = false;
    public String introSeen = "";
    public String introSeenServers = "";
    public boolean hologram = true;
    public boolean spawnerTooltip = true;
    public List<String> favourites = new ArrayList<String>();
    public Map<String, String> pageKeys = new LinkedHashMap<String, String>();
    public boolean menuScrollShown = false;
    public boolean adStream = true;
    public boolean adTrade = true;
    public boolean adTpa = true;
    public boolean adLinks = true;
    public boolean adServer = true;
    public boolean adToSecondChat = false;
    public boolean adFakes = true;
    public boolean adKeepMembers = true;
    public List<String> adWords = new ArrayList<String>();
    public boolean guardTpa = true;
    public boolean guardPay = true;
    public boolean guardSound = true;
    public boolean sellGuard = true;
    public int sellGuardLowPct = 50;
    public int sellGuardHighX = 5;
    public boolean trendGraph = true;
    public boolean totemCard = true;
    public boolean totemOffhand = true;
    public int totemLow = 2;
    public boolean totemNameTag = true;
    public boolean totemTabList = true;
    public boolean totemOwnPops = true;
    public float totemCardX = -1.0f;
    public float totemCardY = -1.0f;
    public float totemCardScale = 1.0f;
    public List<Integer> crosshairColors = new ArrayList<Integer>(List.of(Integer.valueOf(-1)));
    public int crosshairSelected = 0;
    public boolean crosshairGradient = false;
    public float crosshairGradientSeconds = 3.0f;
    public boolean crosshairTargetColor = true;
    public int hitResult = -43691;
    public boolean crosshairHitFlash = true;
    public int crosshairHit = -12288;
    public int crosshairSize = 5;
    public int crosshairThickness = 1;
    public int crosshairGap = 1;
    public boolean crosshairDot = false;
    public boolean crosshairCustom = false;
    public String crosshairPixels = "";
    public boolean crosshairMirror = true;
    public Map<String, Integer> soundVolumes = new HashMap<String, Integer>();
    public boolean calcOpen = false;
    public String calcInput = "";
    public String calcLast = "";
    public boolean armorHelmet = true;
    public boolean armorChest = true;
    public boolean armorLegs = true;
    public boolean armorBoots = true;
    public boolean armorOffhand = true;
    public boolean armorAt20 = true;
    public boolean armorAt10 = true;
    public boolean armorAt5 = true;
    public boolean armorSound = true;
    public List<String> auctionBlacklist = new ArrayList<String>();
    public List<SellPreset> sellPresets = new ArrayList<SellPreset>();
    public String sellPresetActive = "";
    public Map<String, String> textureStyles = new HashMap<String, String>();
    public Map<String, String> textureApplied = new HashMap<String, String>();
    public int texturePreview = 2;

    private V2() {
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("fwm").resolve("v2.json");
    }

    public static void load() {
        try {
            Path file = V2.file();
            if (!Files.exists(file, new LinkOption[0])) {
                return;
            }
            V2 read = (V2)GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), V2.class);
            if (read != null) {
                INSTANCE = read.fixed();
            }
        }
        catch (Exception e) {
            System.err.println("[FWM] Could not read v2.json, using defaults: " + e.getMessage());
        }
    }

    private V2 fixed() {
        if (this.favourites == null) {
            this.favourites = new ArrayList<String>();
        }
        if (this.pageKeys == null) {
            this.pageKeys = new LinkedHashMap<String, String>();
        }
        if (this.adWords == null) {
            this.adWords = new ArrayList<String>();
        }
        if (this.crosshairColors == null || this.crosshairColors.isEmpty()) {
            this.crosshairColors = new ArrayList<Integer>(List.of(Integer.valueOf(-1)));
        }
        if (this.soundVolumes == null) {
            this.soundVolumes = new HashMap<String, Integer>();
        }
        if (this.auctionBlacklist == null) {
            this.auctionBlacklist = new ArrayList<String>();
        }
        if (this.sellPresets == null) {
            this.sellPresets = new ArrayList<SellPreset>();
        }
        if (this.textureStyles == null) {
            this.textureStyles = new HashMap<String, String>();
        }
        if (this.textureApplied == null) {
            this.textureApplied = new HashMap<String, String>();
        }
        if (this.textureOff == null) {
            this.textureOff = new HashSet<String>();
        }
        if (!this.showHiddenMigrated) {
            this.showHidden = true;
            this.showHiddenMigrated = true;
        }
        if (this.calcInput == null) {
            this.calcInput = "";
        }
        if (this.crosshairPixels == null) {
            this.crosshairPixels = "";
        }
        if (this.calcLast == null) {
            this.calcLast = "";
        }
        if (this.sellPresetActive == null) {
            this.sellPresetActive = "";
        }
        if (this.updateAnnounced == null) {
            this.updateAnnounced = "";
        }
        this.crosshairSelected = Math.max(0, Math.min(this.crosshairColors.size() - 1, this.crosshairSelected));
        return this;
    }

    public static void save() {
        try {
            Path file = V2.file();
            Files.createDirectories(file.getParent(), new FileAttribute[0]);
            Path tmp = file.resolveSibling("v2.json.tmp");
            Files.writeString(tmp, (CharSequence)GSON.toJson((Object)INSTANCE), StandardCharsets.UTF_8, new OpenOption[0]);
            try {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            }
            catch (Exception atomicNotSupported) {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (Exception e) {
            System.err.println("[FWM] Could not write v2.json: " + e.getMessage());
        }
    }

    public static final class Timings {
        public int itemDelay;
        public int stacksPerTick;
        public int buttonDelay;
        public int reopenDelay;
    }

    public static final class SellPreset {
        public String name;
        public List<String> items = new ArrayList<String>();
        public Timings timings;
    }
}

