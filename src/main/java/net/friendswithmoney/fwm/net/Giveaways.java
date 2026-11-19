/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.client.Minecraft
 */
package net.friendswithmoney.fwm.net;

import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.loader.api.FabricLoader;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.net.FwmApi;
import net.friendswithmoney.fwm.net.ModId;
import net.friendswithmoney.fwm.net.Presence;
import net.friendswithmoney.fwm.ui.FwmNotice;
import net.friendswithmoney.fwm.ui.Lang;
import net.minecraft.client.Minecraft;

public final class Giveaways {
    private static final Gson GSON = new Gson();
    private static final long POLL_MS = 60000L;
    private static final long[] BACKOFF_MS = new long[]{120000L, 300000L, 600000L};
    private static volatile List<Active> active = List.of();
    private static volatile List<Recent> recent = List.of();
    private static volatile long clockOffset;
    private static volatile boolean loaded;
    private static long nextPoll;
    private static int failures;
    private static boolean running;
    private static Seen seen;
    private static final Map<String, String> JOIN_RESULT;
    private static final Set<String> JOINING;
    public static final long CARD_MS = 30000L;

    private Giveaways() {
    }

    public static List<Active> active() {
        return active;
    }

    public static List<Recent> recent() {
        return recent;
    }

    public static boolean loaded() {
        return loaded;
    }

    public static long now() {
        return System.currentTimeMillis() + clockOffset;
    }

    public static int open() {
        if (!Features.allowedHere("fwm.news.giveaway")) {
            return 0;
        }
        long now = Giveaways.now();
        int n = 0;
        for (Active a : active) {
            if (a.joined || a.endsAt <= now) continue;
            ++n;
        }
        return n;
    }

    public static String joinResult(String id) {
        return JOIN_RESULT.get(id);
    }

    public static boolean joining(String id) {
        return JOINING.contains(id);
    }

    public static void tick(Minecraft client) {
        if (client.player == null || !Presence.onHugo(client)) {
            return;
        }
        long now = System.currentTimeMillis();
        if (running || now < nextPoll) {
            return;
        }
        Giveaways.poll(client.player.getUUID());
    }

    public static void refresh() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || running) {
            return;
        }
        Giveaways.poll(client.player.getUUID());
    }

    private static void poll(UUID uuid) {
        running = true;
        long sent = System.currentTimeMillis();
        FwmApi.get("/api/mod/giveaways?uuid=" + uuid.toString().replace("-", ""), Answer.class).whenComplete((answer, error) -> {
            running = false;
            long back = System.currentTimeMillis();
            if (error != null || answer == null) {
                nextPoll = back + BACKOFF_MS[Math.min(failures, BACKOFF_MS.length - 1)];
                ++failures;
                return;
            }
            failures = 0;
            nextPoll = back + 60000L;
            if (answer.now > 0L) {
                clockOffset = answer.now - (sent + back) / 2L;
            }
            List<Active> a = answer.active == null ? List.of() : List.copyOf(answer.active);
            List<Recent> r = answer.recent == null ? List.of() : List.copyOf(answer.recent);
            active = a;
            recent = r;
            loaded = true;
            Giveaways.announce(a, r);
        });
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("fwm-giveaways.json");
    }

    private static Seen seen() {
        if (seen != null) {
            return seen;
        }
        try {
            if (Files.exists(Giveaways.file(), new LinkOption[0])) {
                seen = (Seen)GSON.fromJson(Files.readString(Giveaways.file(), StandardCharsets.UTF_8), Seen.class);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (seen == null) {
            seen = new Seen();
        }
        if (Giveaways.seen.announced == null) {
            Giveaways.seen.announced = new LinkedHashSet<String>();
        }
        if (Giveaways.seen.results == null) {
            Giveaways.seen.results = new LinkedHashSet<String>();
        }
        return seen;
    }

    private static void announce(List<Active> a, List<Recent> r) {
        Seen s = Giveaways.seen();
        ArrayList<Card> toasts = new ArrayList<Card>();
        for (Active active : a) {
            if (!s.announced.add(active.id)) continue;
            toasts.add(new Card(Lang.t("NEUES GIVEAWAY", "NEW GIVEAWAY"), active.title, Lang.t("Preis: ", "Prize: ") + active.prize + " \u00b7 " + Lang.t("Info (i) \u00f6ffnen zum Teilnehmen", "open Info (i) to join"), false));
        }
        for (Recent recent : r) {
            s.announced.add(recent.id);
            if (!recent.joined || !s.results.add(recent.id) || !"drawn".equals(recent.status)) continue;
            toasts.add(recent.won ? new Card(Lang.t("DU HAST GEWONNEN!", "YOU WON!"), recent.title, Lang.t("Melde dich bei FWM.", "Contact FWM."), true) : new Card(Lang.t("AUSGELOST", "DRAWN"), recent.title, Lang.t("Gewinner: ", "Winner: ") + recent.winnerName, false));
        }
        Giveaways.trim(s.announced);
        Giveaways.trim(s.results);
        if (toasts.isEmpty()) {
            return;
        }
        try {
            Files.writeString(Giveaways.file(), (CharSequence)GSON.toJson((Object)s), StandardCharsets.UTF_8, new OpenOption[0]);
        }
        catch (Exception exception) {
            // empty catch block
        }
        for (Card card : toasts) {
            FwmNotice.show(card.head(), card.title(), card.line(), 15911244, 30000L);
        }
    }

    private static void trim(Set<String> set) {
        while (set.size() > 200) {
            set.remove(set.iterator().next());
        }
    }

    public static void join(String id) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || !JOINING.add(id)) {
            return;
        }
        ModId.identifyThen(() -> Giveaways.send(id));
    }

    private static void send(String id) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            JOINING.remove(id);
            return;
        }
        JoinBody body = new JoinBody();
        body.uuid = client.player.getUUID().toString().replace("-", "");
        body.name = client.player.getGameProfile().name();
        body.modId = ModId.get();
        FwmApi.post("/api/mod/giveaways/" + id + "/join", body, null, JoinAnswer.class).whenComplete((answer, error) -> {
            JOINING.remove(id);
            String result = error != null || answer == null || answer.result == null ? "error" : answer.result;
            JOIN_RESULT.put(id, result);
            if ("joined".equals(result) || "already_joined".equals(result)) {
                ArrayList<Active> now = new ArrayList<Active>(active);
                for (Active a : now) {
                    if (!a.id.equals(id)) continue;
                    a.joined = true;
                    if (answer.participants <= 0) continue;
                    a.participants = answer.participants;
                }
                active = List.copyOf(now);
            }
        });
    }

    public static String explain(String result) {
        if (result == null) {
            return null;
        }
        return switch (result) {
            case "joined", "already_joined" -> null;
            case "already_joined_modid" -> Lang.t("Mit diesem PC nimmt schon ein anderer Account teil.", "Another account on this PC has already joined.");
            case "ended" -> Lang.t("Das Giveaway ist schon vorbei.", "This giveaway has already ended.");
            case "not_found" -> Lang.t("Das Giveaway gibt es nicht mehr.", "This giveaway no longer exists.");
            case "unknown_modid" -> Lang.t("Deine Mod-ID ist noch nicht angemeldet \u2013 kurz neu auf HugoSMP joinen.", "Your mod ID is not registered yet \u2013 rejoin HugoSMP once.");
            case "slow_down" -> Lang.t("Zu viele Versuche, kurz warten.", "Too many tries, wait a moment.");
            default -> Lang.t("Keine Verbindung zum Server.", "No connection to the server.");
        };
    }

    static {
        JOIN_RESULT = new ConcurrentHashMap<String, String>();
        JOINING = ConcurrentHashMap.newKeySet();
    }

    public static final class Active {
        public String id;
        public String title;
        public String prize;
        public String description;
        public long startsAt;
        public long endsAt;
        public int participants;
        public boolean joined;
    }

    static final class Answer {
        long now;
        List<Active> active;
        List<Recent> recent;

        Answer() {
        }
    }

    static final class Seen {
        Set<String> announced = new LinkedHashSet<String>();
        Set<String> results = new LinkedHashSet<String>();

        Seen() {
        }
    }

    public record Card(String head, String title, String line, boolean won) {
    }

    public static final class Recent {
        public String id;
        public String title;
        public String prize;
        public long endedAt;
        public String status;
        public String winnerName;
        public boolean won;
        public boolean joined;
    }

    static final class JoinBody {
        String uuid;
        String name;
        String modId;

        JoinBody() {
        }
    }

    static final class JoinAnswer {
        String result;
        int participants;

        JoinAnswer() {
        }
    }
}

