/*
 * Decompiled with CFR 0.152.
 */
package net.friendswithmoney.fwm.config;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.friendswithmoney.fwm.config.ConfigManager;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.net.ModBan;
import net.friendswithmoney.fwm.net.ServerContext;

public final class Features {
    public static final String SELLMACRO = "fwm.tab.sellmacro";
    public static final String SELLSTATS = "fwm.tab.sellmacro";
    public static final String SPAWNER = "fwm.tab.spawner";
    public static final String MEMBERS = "fwm.tab.members";
    public static final String MESSAGES = "fwm.tab.messages";
    public static final String TOOLGUARD = "fwm.tab.toolguard";
    public static final String UNITPRICE = "fwm.feature.unitprice";
    public static final String AHPRICE = "fwm.feature.ahprice";
    public static final String REALNAME = "fwm.feature.realname";
    public static final String SECONDCHAT = "fwm.tab.secondchat";
    public static final String AUCTION = "fwm.tab.auction";
    public static final String COLORCODES = "fwm.tab.colorcodes";
    public static final String TPAWARN = "fwm.feature.tpawarn";
    public static final String MARKET = "fwm.tab.market";
    public static final String RTPMAP = "fwm.tab.rtp";
    public static final String RTPMINIMAP = "fwm.feature.rtpminimap";
    public static final String FIGHTKIT = "fwm.tab.fightkit";
    public static final String WAYPOINTS = "fwm.tab.waypoints";
    public static final String PRICEPING = "fwm.feature.priceping";
    public static final String ANTILEAK = "fwm.feature.antileak";
    public static final String AUTOTPA = "fwm.tab.autotpa";
    public static final String SPAWNERMACRO = "fwm.tab.spawnermacro";
    public static final String MONEY = "fwm.tab.money";
    public static final String INVVALUE = "fwm.feature.invvalue";
    public static final String EMOTES = "fwm.tab.emotes";
    public static final String TEXTURES = "fwm.tab.textures";
    public static final String ADBLOCK = "fwm.tab.adblock";
    public static final String TPAGUARD = "fwm.tab.tpaguard";
    public static final String TOTEM = "fwm.tab.totem";
    public static final String CROSSHAIR = "fwm.tab.crosshair";
    public static final String SOUNDS = "fwm.tab.sounds";
    public static final String CALC = "fwm.tab.calc";
    public static final String ARMORWARN = "fwm.tab.armorwarn";
    public static final String SKYLYRICS = "fwm.tab.skylyrics";
    public static final String SELLSTATS_MANUAL = "fwm.tab.sellstats";
    public static final String TODOS = "fwm.tab.todos";
    public static final String DONATIONS = "fwm.tab.donations";
    public static final String PRICETREND = "fwm.feature.pricetrend";
    private static final List<String> DEFAULT_OFF = List.of("fwm.feature.rtpminimap", "fwm.feature.antileak", "fwm.tab.waypoints", "fwm.tab.autotpa", "fwm.tab.spawnermacro", "fwm.feature.invvalue", "fwm.tab.totem", "fwm.tab.crosshair", "fwm.tab.skylyrics");
    public static final List<String> SWITCHABLE = List.of("fwm.tab.sellmacro", "fwm.tab.messages", "fwm.tab.toolguard", "fwm.feature.unitprice", "fwm.feature.ahprice", "fwm.feature.realname", "fwm.feature.tpawarn", "fwm.tab.secondchat", "fwm.tab.auction", "fwm.tab.colorcodes", "fwm.tab.market", "fwm.feature.priceping", "fwm.tab.rtp", "fwm.feature.rtpminimap", "fwm.feature.antileak", "fwm.tab.fightkit", "fwm.tab.waypoints", "fwm.tab.autotpa", "fwm.tab.spawnermacro", "fwm.tab.money", "fwm.feature.invvalue", "fwm.tab.emotes", "fwm.tab.adblock", "fwm.tab.totem", "fwm.tab.crosshair", "fwm.tab.sounds", "fwm.tab.calc", "fwm.tab.armorwarn", "fwm.tab.totempop", "fwm.tab.skylyrics", "fwm.tab.todos", "fwm.tab.donations", "fwm.feature.pricetrend");
    private static final Set<ServerContext.Server> EVERYWHERE = EnumSet.of(ServerContext.Server.HUGO, ServerContext.Server.DONUT, ServerContext.Server.WIND, ServerContext.Server.OTHER);
    private static final Set<ServerContext.Server> HUGO_DONUT_WIND = EnumSet.of(ServerContext.Server.HUGO, ServerContext.Server.DONUT, ServerContext.Server.WIND);
    private static final Set<ServerContext.Server> HUGO_WIND = EnumSet.of(ServerContext.Server.HUGO, ServerContext.Server.WIND);
    private static final Map<String, Set<ServerContext.Server>> WHERE = new HashMap<String, Set<ServerContext.Server>>();
    private static final Set<String> SP_WORKS;
    private static final Set<String> ALWAYS;

    private Features() {
    }

    public static boolean canSwitch(String key) {
        return SWITCHABLE.contains(key);
    }

    public static boolean allowedHere(String key) {
        ServerContext.Server server = ServerContext.current();
        if (server == ServerContext.Server.NONE) {
            return true;
        }
        if (ALWAYS.contains(key)) {
            return true;
        }
        if (server == ServerContext.Server.SINGLE) {
            return V2.INSTANCE.singleplayerOn && SP_WORKS.contains(key);
        }
        Set<ServerContext.Server> where = WHERE.get(key);
        return where == null ? server == ServerContext.Server.HUGO : where.contains((Object)server);
    }

    public static int hiddenHere(List<String> keys) {
        int n = 0;
        for (String key : keys) {
            if (Features.allowedHere(key)) continue;
            ++n;
        }
        return n;
    }

    public static boolean isOn(String key) {
        if (ModBan.locked()) {
            return false;
        }
        if (!Features.allowedHere(key)) {
            return false;
        }
        if (!Features.canSwitch(key)) {
            return true;
        }
        if (DEFAULT_OFF.contains(key)) {
            return FwmConfig.INSTANCE.enabledFeatures.contains(key);
        }
        return !FwmConfig.INSTANCE.disabledFeatures.contains(key);
    }

    public static boolean switchedOff(String key) {
        List<String> hidden = V2.INSTANCE.hiddenTiles;
        if (hidden != null && hidden.contains(key)) {
            return true;
        }
        if (!Features.canSwitch(key)) {
            return false;
        }
        if (DEFAULT_OFF.contains(key)) {
            return !FwmConfig.INSTANCE.enabledFeatures.contains(key);
        }
        return FwmConfig.INSTANCE.disabledFeatures.contains(key);
    }

    public static void set(String key, boolean on) {
        if (!Features.canSwitch(key)) {
            return;
        }
        if (DEFAULT_OFF.contains(key)) {
            if (on) {
                if (!FwmConfig.INSTANCE.enabledFeatures.contains(key)) {
                    FwmConfig.INSTANCE.enabledFeatures.add(key);
                }
            } else {
                FwmConfig.INSTANCE.enabledFeatures.remove(key);
            }
            ConfigManager.save();
            return;
        }
        if (on) {
            FwmConfig.INSTANCE.disabledFeatures.remove(key);
        } else if (!FwmConfig.INSTANCE.disabledFeatures.contains(key)) {
            FwmConfig.INSTANCE.disabledFeatures.add(key);
        }
        ConfigManager.save();
    }

    public static void toggle(String key) {
        Features.set(key, !Features.isOn(key));
    }

    static {
        for (String key : List.of(SECONDCHAT, ANTILEAK, "fwm.tab.antileak", TEXTURES, "fwm.tab.news", "fwm.tab.settings", "fwm.tab.extras", "fwm.tab.privacy", ADBLOCK, TPAGUARD, TOTEM, CROSSHAIR, SOUNDS, CALC, ARMORWARN, "fwm.tab.mapart", "fwm.tab.sky", "fwm.tab.totemstudio", "fwm.tab.armortrim", "fwm.tab.hitcolor", "fwm.tab.totempop", "fwm.tab.totemhold", SKYLYRICS, TODOS)) {
            WHERE.put(key, EVERYWHERE);
        }
        WHERE.put(EMOTES, EnumSet.of(ServerContext.Server.HUGO, ServerContext.Server.WIND, ServerContext.Server.OTHER));
        WHERE.put(TOOLGUARD, HUGO_DONUT_WIND);
        WHERE.put(FIGHTKIT, HUGO_DONUT_WIND);
        WHERE.put(AUCTION, HUGO_DONUT_WIND);
        WHERE.put("fwm.tab.giveaway", HUGO_DONUT_WIND);
        WHERE.put("fwm.news.giveaway", EnumSet.of(ServerContext.Server.HUGO));
        WHERE.put(MONEY, HUGO_DONUT_WIND);
        WHERE.put(DONATIONS, HUGO_DONUT_WIND);
        WHERE.put(MESSAGES, HUGO_WIND);
        WHERE.put(SELLSTATS_MANUAL, EnumSet.noneOf(ServerContext.Server.class));
        WHERE.put(TPAWARN, EnumSet.of(ServerContext.Server.HUGO));
        SP_WORKS = Set.of(ANTILEAK, "fwm.tab.antileak", TEXTURES, COLORCODES, "fwm.tab.settings", "fwm.tab.news", "fwm.tab.privacy", CROSSHAIR, SOUNDS, CALC, ARMORWARN, TOTEM, "fwm.tab.mapart", "fwm.tab.sky", "fwm.tab.totemstudio", "fwm.tab.armortrim", "fwm.tab.hitcolor", "fwm.tab.totempop", "fwm.tab.totemhold", SKYLYRICS, TODOS);
        ALWAYS = Set.of("fwm.tab.settings", "fwm.tab.privacy", "fwm.tab.news");
    }
}

