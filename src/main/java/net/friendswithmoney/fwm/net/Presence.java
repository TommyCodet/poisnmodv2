/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.client.multiplayer.PlayerInfo
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FontDescription
 *  net.minecraft.network.chat.FontDescription$Resource
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.Style
 *  net.minecraft.resources.Identifier
 */
package net.friendswithmoney.fwm.net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import net.friendswithmoney.fwm.chat.ServerLine;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.net.FwmApi;
import net.friendswithmoney.fwm.net.ServerContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public final class Presence {
    private static final long PING_MS = 300000L;
    private static final long PING_RETRY_MS = 60000L;
    private static final long BATCH_DELAY_MS = 500L;
    private static final long LOOKUP_GAP_MS = 2500L;
    private static final long NO_RECHECK_MS = 3600000L;
    private static final long FAIL_RETRY_MS = 60000L;
    private static final int MAX_BATCH = 200;
    private static final FontDescription LOGO_FONT = new FontDescription.Resource(Identifier.fromNamespaceAndPath((String)"fwm", (String)"badge_small"));
    private static final FontDescription TAB_FONT = new FontDescription.Resource(Identifier.fromNamespaceAndPath((String)"fwm", (String)"badge_tab"));
    private static volatile long nextPing;
    private static final Set<UUID> FWM;
    private static final Map<UUID, Long> ASKED;
    private static final Set<UUID> PENDING;
    private static volatile long pendingSince;
    private static volatile boolean lookupRunning;
    private static long lastLookup;
    private static ClientPacketListener connection;
    private static volatile List<String> onlineNames;
    private static final Pattern WORD_EDGE;
    private static final int TAB_ASKED = 100;
    private static volatile Map<UUID, Integer> tabRow;
    private static final Map<UUID, TabMemo> TAB_MEMO;
    private static final Pattern PLAYER_NAME;
    private static final Pattern RANK;
    private static final String MOJANG_BULK = "https://api.minecraftservices.com/minecraft/profile/lookup/bulk/byname";
    private static final int NAME_BATCH = 10;
    private static final long NAME_GAP_MS = 1500L;
    private static final long NAME_BACKOFF_MS = 30000L;
    private static final Map<String, UUID> NAME_IDS;
    private static final Set<String> NAME_ASKED;
    private static final Set<String> NAME_PENDING;
    private static volatile boolean nameLookupRunning;
    private static volatile long nextNameLookup;
    public static volatile boolean drawingWorld;
    static boolean testHugo;

    private Presence() {
    }

    public static Component decorate(UUID uuid, Component name) {
        if (uuid == null || name == null) {
            return name;
        }
        Presence.want(uuid);
        if (!FwmConfig.INSTANCE.presenceShowOthers || !FWM.contains(uuid)) {
            return name;
        }
        if (!Presence.onLogoServer(Minecraft.getInstance())) {
            return name;
        }
        return name.copy().append((Component)Presence.logo());
    }

    public static void tabRows(List<PlayerInfo> entries) {
        int n;
        int rows = n = entries.size();
        int columns = 1;
        while (rows > 20) {
            rows = (n + ++columns - 1) / columns;
        }
        HashMap<UUID, Integer> map = new HashMap<UUID, Integer>();
        for (int i = 0; i < n; ++i) {
            PlayerInfo entry = entries.get(i);
            if (entry.getProfile() == null || rows <= 0) continue;
            map.put(entry.getProfile().id(), i);
        }
        tabRow = map;
    }

    private static boolean asksInTab(UUID uuid) {
        Integer index = tabRow.get(uuid);
        return index != null && index < 100;
    }

    public static Component decorateTab(UUID uuid, Component name) {
        if (uuid == null || name == null) {
            return name;
        }
        long now = System.currentTimeMillis();
        TabMemo memo = TAB_MEMO.get(uuid);
        if (memo != null && memo.in() == name && now - memo.at() < 500L) {
            return memo.out();
        }
        Component out = Presence.decorateTabNow(uuid, name);
        if (TAB_MEMO.size() > 4000) {
            TAB_MEMO.clear();
        }
        TAB_MEMO.put(uuid, new TabMemo(name, out, now));
        return out;
    }

    private static Component decorateTabNow(UUID uuid, Component name) {
        boolean ask = Presence.asksInTab(uuid);
        if (ask) {
            Presence.want(uuid);
        }
        if (!FwmConfig.INSTANCE.presenceShowOthers) {
            return name;
        }
        if (!Presence.onLogoServer(Minecraft.getInstance())) {
            return name;
        }
        if (FWM.contains(uuid)) {
            return name.copy().append((Component)Presence.rowLogo());
        }
        String shown = ServerLine.strip(name.getString());
        String rowName = Presence.rowName(shown);
        if (rowName != null) {
            String key = rowName.toLowerCase(Locale.ROOT);
            UUID id = NAME_IDS.get(key);
            if (id != null) {
                if (ask) {
                    Presence.want(id);
                }
                if (FWM.contains(id)) {
                    return name.copy().append((Component)Presence.rowLogo());
                }
            } else if (ask && NAME_ASKED.add(key)) {
                NAME_PENDING.add(rowName);
            }
        }
        for (String player : onlineNames) {
            int at = shown.indexOf(player);
            while (at >= 0) {
                boolean endOk;
                boolean startOk = at == 0 || !WORD_EDGE.matcher(String.valueOf(shown.charAt(at - 1))).matches();
                int end = at + player.length();
                boolean bl = endOk = end >= shown.length() || !WORD_EDGE.matcher(String.valueOf(shown.charAt(end))).matches();
                if (startOk && endOk) {
                    return name.copy().append((Component)Presence.rowLogo());
                }
                at = shown.indexOf(player, at + 1);
            }
        }
        return name;
    }

    static String rowName(String shown) {
        String[] parts = shown.trim().split("\\s+");
        if (parts.length == 0) {
            return null;
        }
        String name = parts.length > 1 && RANK.matcher(parts[0]).matches() ? parts[1] : parts[0];
        return PLAYER_NAME.matcher(name).matches() ? name : null;
    }

    private static void lookupNames() {
        ArrayList<String> batch = new ArrayList<String>();
        for (String n : NAME_PENDING) {
            if (batch.size() >= 10) break;
            batch.add(n);
        }
        batch.forEach(NAME_PENDING::remove);
        if (batch.isEmpty()) {
            return;
        }
        nameLookupRunning = true;
        FwmApi.postUrl(MOJANG_BULK, batch, Profile[].class).whenComplete((answer, error) -> {
            nameLookupRunning = false;
            if (error != null || answer == null) {
                nextNameLookup = System.currentTimeMillis() + 30000L;
                for (String n : batch) {
                    NAME_ASKED.remove(n.toLowerCase(Locale.ROOT));
                }
                return;
            }
            for (Profile profile : answer) {
                UUID uuid = Presence.parse(profile.id);
                if (uuid == null || profile.name == null) continue;
                NAME_IDS.put(profile.name.toLowerCase(Locale.ROOT), uuid);
                Presence.want(uuid);
            }
        });
    }

    private static MutableComponent rowLogo() {
        return drawingWorld ? Presence.logo() : Presence.tabLogo();
    }

    public static MutableComponent tabLogo() {
        return Component.literal((String)" ").append((Component)Component.literal((String)"\ue000").setStyle(Style.EMPTY.withFont(TAB_FONT).withColor(ChatFormatting.WHITE)));
    }

    public static MutableComponent logo() {
        return Component.literal((String)" ").append((Component)Component.literal((String)"\ue000").setStyle(Style.EMPTY.withFont(LOGO_FONT).withColor(ChatFormatting.WHITE)));
    }

    public static void want(UUID uuid) {
        if (uuid == null || FWM.contains(uuid)) {
            return;
        }
        Long asked = ASKED.get(uuid);
        if (asked != null && System.currentTimeMillis() - asked < 3600000L) {
            return;
        }
        if (PENDING.add(uuid) && PENDING.size() == 1) {
            pendingSince = System.currentTimeMillis();
        }
    }

    public static boolean showOwn() {
        Minecraft client = Minecraft.getInstance();
        return client.player != null && FwmConfig.INSTANCE.presenceShowOthers && Presence.onLogoServer(client) && FWM.contains(client.player.getUUID());
    }

    public static boolean uses(UUID uuid) {
        return uuid != null && FWM.contains(uuid);
    }

    public static void tick(Minecraft client) {
        long now;
        if (client.player == null || client.level == null || !Presence.onLogoServer(client)) {
            return;
        }
        if (!FwmConfig.INSTANCE.presenceShowOthers) {
            return;
        }
        ClientPacketListener handler = client.getConnection();
        if (handler != connection) {
            connection = handler;
            FWM.clear();
            ASKED.clear();
            PENDING.clear();
        }
        if ((now = System.currentTimeMillis()) >= nextPing) {
            nextPing = now + 300000L;
            Presence.ping(client);
        }
        if (client.level.getGameTime() % 20L == 0L) {
            for (AbstractClientPlayer player : client.level.players()) {
                Presence.want(player.getUUID());
            }
            ArrayList<String> names = new ArrayList<String>();
            for (UUID id : handler.getOnlinePlayerIds()) {
                PlayerInfo entry;
                String n;
                if (!FWM.contains(id) || (n = (entry = handler.getPlayerInfo(id)) == null || entry.getProfile() == null ? null : entry.getProfile().name()) == null || n.length() < 3) continue;
                names.add(n);
            }
            onlineNames = List.copyOf(names);
        }
        if (!NAME_PENDING.isEmpty() && !nameLookupRunning && now >= nextNameLookup) {
            nextNameLookup = now + 1500L;
            Presence.lookupNames();
        }
        if (!PENDING.isEmpty() && !lookupRunning && now - pendingSince >= 500L && now - lastLookup >= 2500L) {
            lastLookup = now;
            Presence.lookup();
        }
    }

    public static boolean onHugo(Minecraft client) {
        if (testHugo) {
            return true;
        }
        return ServerContext.hugo();
    }

    public static boolean onLogoServer(Minecraft client) {
        if (testHugo) {
            return true;
        }
        ServerContext.Server server = ServerContext.current();
        return server == ServerContext.Server.HUGO || server == ServerContext.Server.DONUT || server == ServerContext.Server.WIND;
    }

    private static void ping(Minecraft client) {
        UUID self = client.player.getUUID();
        Report body = new Report();
        body.uuid = self.toString().replace("-", "");
        body.name = client.player.getGameProfile().name();
        FwmApi.post("/api/mod/presence", body, null, Ok.class).whenComplete((answer, error) -> {
            if (error != null || answer == null) {
                nextPing = System.currentTimeMillis() + 60000L;
                return;
            }
            FWM.add(self);
        });
    }

    private static void lookup() {
        ArrayList<UUID> batch = new ArrayList<UUID>();
        for (UUID uuid : PENDING) {
            if (batch.size() >= 200) break;
            batch.add(uuid);
        }
        batch.forEach(PENDING::remove);
        if (!PENDING.isEmpty()) {
            pendingSince = System.currentTimeMillis();
        }
        if (batch.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        Lookup body = new Lookup();
        body.uuids = new ArrayList<String>();
        for (UUID uuid : batch) {
            ASKED.put(uuid, now);
            body.uuids.add(uuid.toString().replace("-", ""));
        }
        lookupRunning = true;
        FwmApi.post("/api/mod/presence/lookup", body, null, LookupAnswer.class).whenComplete((answer, error) -> {
            lookupRunning = false;
            if (error != null || answer == null || answer.fwm == null) {
                long retry = System.currentTimeMillis() - 3600000L + 60000L;
                for (UUID uuid : batch) {
                    ASKED.put(uuid, retry);
                }
                return;
            }
            for (String id : answer.fwm) {
                UUID uuid = Presence.parse(id);
                if (uuid == null) continue;
                FWM.add(uuid);
            }
        });
    }

    private static UUID parse(String id) {
        if (id == null || id.length() != 32) {
            return null;
        }
        try {
            return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20));
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    static {
        FWM = ConcurrentHashMap.newKeySet();
        ASKED = new ConcurrentHashMap<UUID, Long>();
        PENDING = ConcurrentHashMap.newKeySet();
        onlineNames = List.of();
        WORD_EDGE = Pattern.compile("[A-Za-z0-9_]");
        tabRow = Map.of();
        TAB_MEMO = new ConcurrentHashMap<UUID, TabMemo>();
        PLAYER_NAME = Pattern.compile("[A-Za-z0-9_]{3,16}");
        RANK = Pattern.compile("[A-Z]+");
        NAME_IDS = new ConcurrentHashMap<String, UUID>();
        NAME_ASKED = ConcurrentHashMap.newKeySet();
        NAME_PENDING = ConcurrentHashMap.newKeySet();
    }

    private record TabMemo(Component in, Component out, long at) {
    }

    static final class Profile {
        String id;
        String name;

        Profile() {
        }
    }

    static final class Report {
        String uuid;
        String name;

        Report() {
        }
    }

    static final class Ok {
        boolean ok;

        Ok() {
        }
    }

    static final class Lookup {
        List<String> uuids;

        Lookup() {
        }
    }

    static final class LookupAnswer {
        List<String> fwm;

        LookupAnswer() {
        }
    }
}

