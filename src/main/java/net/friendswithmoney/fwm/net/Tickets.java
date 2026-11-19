/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.chat.Component
 */
package net.friendswithmoney.fwm.net;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import net.friendswithmoney.fwm.net.FwmApi;
import net.friendswithmoney.fwm.net.ModAccount;
import net.friendswithmoney.fwm.ui.FwmNotice;
import net.friendswithmoney.fwm.ui.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class Tickets {
    private static final long POLL_MS = 60000L;
    private static volatile List<Spawner> spawners = List.of();
    private static volatile String spawnerError = "";
    private static volatile List<Ticket> tickets = List.of();
    private static volatile long nextPoll;
    private static volatile long pausedUntil;
    private static final Map<String, String> SEEN;
    private static final Map<String, List<Message>> MESSAGES;
    private static final Map<String, String> AFTER;
    private static final Set<String> LOADING;
    private static volatile String watched;
    private static long nextWatch;
    private static final long WATCH_MS = 15000L;

    private Tickets() {
    }

    public static List<Spawner> spawners() {
        return spawners;
    }

    public static String spawnerError() {
        return spawnerError;
    }

    public static List<Ticket> tickets() {
        return tickets;
    }

    public static List<Message> messages(String ticketId) {
        return MESSAGES.getOrDefault(ticketId, List.of());
    }

    public static boolean loading(String ticketId) {
        return LOADING.contains(ticketId);
    }

    public static void watch(String ticketId) {
        watched = ticketId;
        nextWatch = 0L;
    }

    public static void loadMessages(String ticketId, boolean quiet) {
        if (ticketId == null || !ModAccount.hasKey() || System.currentTimeMillis() < pausedUntil || !LOADING.add(ticketId)) {
            return;
        }
        String after = AFTER.get(ticketId);
        String path = "/mod/v1/tickets/" + ticketId + "/messages?limit=50" + (String)(after == null ? "" : "&after=" + after);
        ModAccount.call("GET", path, null).whenComplete((got, error) -> {
            LOADING.remove(ticketId);
            if (error != null || got == null) {
                return;
            }
            Tickets.pause(got);
            MessagePage page = ModAccount.parse(got, MessagePage.class);
            if (got.status() != 200 || page == null || page.messages == null) {
                return;
            }
            boolean first = !AFTER.containsKey(ticketId);
            ArrayList all = new ArrayList(MESSAGES.getOrDefault(ticketId, List.of()));
            HashSet<String> have = new HashSet<String>();
            for (Message m2 : all) {
                have.add(m2.id);
            }
            ArrayList<Message> fresh = new ArrayList<Message>();
            for (Message m3 : page.messages) {
                if (m3.id == null || !have.add(m3.id)) continue;
                fresh.add(m3);
            }
            all.addAll(fresh);
            all.sort(Comparator.comparing(m -> m.createdAt == null ? "" : m.createdAt));
            MESSAGES.put(ticketId, List.copyOf(all));
            if (page.pagination != null && page.pagination.nextAfter != null) {
                AFTER.put(ticketId, page.pagination.nextAfter);
            } else if (first) {
                AFTER.put(ticketId, all.isEmpty() ? "0" : ((Message)all.get((int)(all.size() - 1))).id);
            }
            if (!(quiet || first || fresh.isEmpty() || ticketId.equals(watched))) {
                Message last = (Message)fresh.get(fresh.size() - 1);
                String who = last.author == null ? "FWM" : (last.author.displayName != null ? last.author.displayName : last.author.username);
                Minecraft client = Minecraft.getInstance();
                client.execute(() -> FwmNotice.show(Lang.t("NEUE NACHRICHT IM TICKET", "NEW TICKET MESSAGE"), who, last.content == null ? "" : last.content, 6088852, 10000L));
            }
        });
    }

    public static boolean hasLive(String type) {
        for (Ticket t : tickets) {
            if (!type.equals(t.type) || !t.live()) continue;
            return true;
        }
        return false;
    }

    public static void loadSpawners() {
        if (!ModAccount.hasKey() || System.currentTimeMillis() < pausedUntil) {
            return;
        }
        ModAccount.call("GET", "/mod/v1/spawners", null).whenComplete((got, error) -> {
            if (error != null || got == null) {
                spawnerError = "Keine Verbindung.";
                return;
            }
            Tickets.pause(got);
            SpawnerList list = ModAccount.parse(got, SpawnerList.class);
            if (got.status() == 200 && list != null && list.spawners != null) {
                spawners = List.copyOf(list.spawners);
                spawnerError = "";
            } else {
                spawnerError = ModAccount.errorText(got);
            }
        });
    }

    public static void refresh() {
        if (!ModAccount.hasKey() || System.currentTimeMillis() < pausedUntil) {
            return;
        }
        nextPoll = System.currentTimeMillis() + 60000L;
        ModAccount.call("GET", "/mod/v1/tickets", null).whenComplete((got, error) -> {
            if (error != null || got == null) {
                return;
            }
            Tickets.pause(got);
            TicketList list = ModAccount.parse(got, TicketList.class);
            if (got.status() != 200 || list == null || list.tickets == null) {
                return;
            }
            Tickets.announce(list.tickets);
            tickets = List.copyOf(list.tickets);
            for (Ticket t : list.tickets) {
                if (!t.live() || t.id == null) continue;
                Tickets.loadMessages(t.id, !AFTER.containsKey(t.id));
            }
        });
    }

    private static void pause(FwmApi.Fetched got) {
        if (got.status() == 429) {
            pausedUntil = System.currentTimeMillis() + Math.max(60L, got.retryAfterSeconds()) * 1000L;
        }
    }

    public static CompletableFuture<String> openSpawner(boolean buy, Spawner spawner, int amount, String notes) {
        HashMap<String, Object> body = new HashMap<String, Object>();
        body.put("type", "spawner");
        body.put("action", buy ? "ankauf" : "verkauf");
        body.put("spawner", spawner.id != null ? spawner.id : spawner.name);
        body.put("amount", amount);
        if (notes != null && !notes.isBlank()) {
            body.put("notes", notes.trim());
        }
        return Tickets.open(body);
    }

    public static CompletableFuture<String> openMiddleman(String description) {
        HashMap<String, Object> body = new HashMap<String, Object>();
        body.put("type", "middleman");
        body.put("description", description.trim());
        return Tickets.open(body);
    }

    private static CompletableFuture<String> open(Map<String, Object> body) {
        return ModAccount.call("POST", "/mod/v1/tickets", body).handle((got, error) -> {
            if (error != null || got == null) {
                return "Keine Verbindung zur FWM-API.";
            }
            Tickets.pause(got);
            if (got.status() == 201) {
                Tickets.refresh();
                return null;
            }
            return ModAccount.errorText(got);
        });
    }

    public static void tick(Minecraft client) {
        if (!ModAccount.hasKey()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (watched != null && now >= nextWatch && now >= pausedUntil) {
            nextWatch = now + 15000L;
            Tickets.loadMessages(watched, true);
        }
        if (now < nextPoll || now < pausedUntil) {
            return;
        }
        boolean anyLive = false;
        for (Ticket t : tickets) {
            if (!t.live()) continue;
            anyLive = true;
        }
        if (!anyLive && nextPoll != 0L) {
            return;
        }
        Tickets.refresh();
    }

    private static void announce(List<Ticket> fresh) {
        boolean first = SEEN.isEmpty();
        for (Ticket t : fresh) {
            if (t.id == null) continue;
            String now = t.status + "/" + t.outcome;
            String before = SEEN.put(t.id, now);
            if (first || before == null || before.equals(now)) continue;
            String what = "spawner".equals(t.type) ? "Spawner-Ticket" : "Middleman-Ticket";
            String line = what + ": " + Tickets.statusText(t);
            Minecraft client = Minecraft.getInstance();
            client.execute(() -> {
                boolean good = "success".equals(t.status) || "success".equals(t.outcome);
                boolean bad = "failure".equals(t.status) || "failure".equals(t.outcome);
                FwmNotice.show(what.toUpperCase(Locale.ROOT), Tickets.statusText(t), "", good ? 6088852 : (bad ? 14709876 : 15911244), 10000L);
                if (client.gui != null) {
                    client.gui.hud.getChat().addClientSystemMessage((Component)Component.literal((String)"[FWM] ").withStyle(ChatFormatting.GREEN).append((Component)Component.literal((String)line).withStyle(ChatFormatting.GRAY)));
                }
            });
        }
    }

    public static String statusText(Ticket t) {
        String by;
        String string = by = t.claimedByMinecraftName == null || t.claimedByMinecraftName.isBlank() ? "" : " von " + t.claimedByMinecraftName;
        if (t.status == null) {
            return "?";
        }
        return switch (t.status) {
            case "open" -> "offen, wartet auf das Team";
            case "claimed" -> "\u00fcbernommen" + by;
            case "success" -> "erfolgreich abgeschlossen" + by;
            case "failure" -> "fehlgeschlagen" + by;
            case "closed" -> {
                if ("pending".equals(t.outcome)) {
                    yield "geschlossen (ohne Entscheidung)";
                }
                yield "geschlossen";
            }
            default -> t.status;
        };
    }

    static {
        SEEN = new HashMap<String, String>();
        MESSAGES = new ConcurrentHashMap<String, List<Message>>();
        AFTER = new ConcurrentHashMap<String, String>();
        LOADING = ConcurrentHashMap.newKeySet();
    }

    public static final class Ticket {
        public String id;
        public String type;
        public String status;
        public String outcome;
        public String claimedByMinecraftName;
        public String createdAt;
        public String updatedAt;
        public String closedAt;

        public boolean live() {
            return "open".equals(this.status) || "claimed".equals(this.status);
        }
    }

    public static final class Spawner {
        public String id;
        public String name;
        public boolean ankauf;
        public boolean verkauf;
        public String ankaufPreis;
        public String verkaufPreis;

        public boolean can(boolean buy) {
            String price;
            String string = price = buy ? this.ankaufPreis : this.verkaufPreis;
            return (buy ? this.ankauf : this.verkauf) && price != null && !price.isBlank() && !"0".equals(price.trim());
        }
    }

    static final class TicketList {
        boolean ok;
        List<Ticket> tickets = new ArrayList<Ticket>();

        TicketList() {
        }
    }

    static final class SpawnerList {
        boolean ok;
        List<Spawner> spawners = new ArrayList<Spawner>();

        SpawnerList() {
        }
    }

    static final class MessagePage {
        boolean ok;
        List<Message> messages = new ArrayList<Message>();
        Pagination pagination;

        MessagePage() {
        }
    }

    public static final class Message {
        public String id;
        public Author author;
        public String content;
        public String createdAt;
        public String editedAt;
    }

    static final class Pagination {
        String nextAfter;
        boolean hasMore;

        Pagination() {
        }
    }

    public static final class Author {
        public String discordId;
        public String username;
        public String displayName;
    }

    static final class Created {
        boolean ok;
        String ticketId;
        String status;
        String type;

        Created() {
        }
    }
}

