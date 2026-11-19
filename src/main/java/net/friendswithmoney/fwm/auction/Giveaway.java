/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.network.chat.ClickEvent
 *  net.minecraft.network.chat.ClickEvent$SuggestCommand
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.HoverEvent
 *  net.minecraft.network.chat.HoverEvent$ShowText
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package net.friendswithmoney.fwm.auction;

import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import net.friendswithmoney.fwm.auction.StreamerStore;
import net.friendswithmoney.fwm.chat.Messages;
import net.friendswithmoney.fwm.ui.FwmGfx;
import net.friendswithmoney.fwm.util.Fmt;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public final class Giveaway {
    private static final String FILE = "fwm-giveaway-state.json";
    private static final long AFTERGLOW_MS = 60000L;
    public static final long DRAW_MS = 4500L;
    private static ItemStack item = ItemStack.EMPTY;
    private static long money;
    private static String word;
    private static int limit;
    private static long durationMs;
    private static long startedAt;
    private static long pausedAt;
    private static long pausedTotal;
    private static Status status;
    private static final List<Participant> participants;
    private static String winner;
    private static long drawnAt;
    private static boolean restored;

    private Giveaway() {
    }

    public static void hold(ItemStack stack) {
        if (status == Status.RUNNING || status == Status.PAUSED) {
            return;
        }
        item = stack == null ? ItemStack.EMPTY : stack.copy();
    }

    public static void start(ItemStack stack, String joinWord, int maxParticipants, int seconds) {
        Giveaway.start(stack, 0L, joinWord, maxParticipants, seconds);
    }

    public static void start(ItemStack stack, long dollars, String joinWord, int maxParticipants, int seconds) {
        money = Math.max(0L, dollars);
        item = money > 0L || stack == null ? ItemStack.EMPTY : stack.copy();
        word = joinWord == null ? "" : joinWord.trim();
        limit = Math.max(1, maxParticipants);
        durationMs = (long)Math.max(1, seconds) * 1000L;
        startedAt = System.currentTimeMillis();
        pausedAt = 0L;
        pausedTotal = 0L;
        participants.clear();
        winner = null;
        drawnAt = 0L;
        status = Status.RUNNING;
        Giveaway.save();
    }

    public static void pause() {
        if (status != Status.RUNNING) {
            return;
        }
        status = Status.PAUSED;
        pausedAt = System.currentTimeMillis();
        Giveaway.save();
    }

    public static void resume() {
        if (status != Status.PAUSED) {
            return;
        }
        pausedTotal += System.currentTimeMillis() - pausedAt;
        pausedAt = 0L;
        status = Status.RUNNING;
        Giveaway.save();
    }

    public static void draw() {
        if (status != Status.RUNNING && status != Status.PAUSED) {
            return;
        }
        if (status == Status.PAUSED) {
            Giveaway.resume();
        }
        status = Status.ENDED;
        drawnAt = System.currentTimeMillis();
        String string = winner = participants.isEmpty() ? null : participants.get(ThreadLocalRandom.current().nextInt(participants.size())).name();
        if (winner != null && money > 0L) {
            Giveaway.notifyPay(winner, participants.size());
        } else {
            Giveaway.notify((String)(winner == null ? "Giveaway beendet \u2013 es gab keine Teilnehmer." : "Gewinner: " + winner + " (" + participants.size() + " Teilnehmer). \u00dcbergib das Item manuell."));
        }
        Giveaway.save();
    }

    public static void cancel() {
        status = Status.NONE;
        participants.clear();
        winner = null;
        drawnAt = 0L;
        Giveaway.save();
    }

    public static void reset() {
        Giveaway.cancel();
    }

    public static Status status() {
        return status;
    }

    public static boolean active() {
        return status == Status.RUNNING || status == Status.PAUSED;
    }

    public static ItemStack item() {
        return item;
    }

    public static long money() {
        return money;
    }

    public static String prizeName() {
        if (money > 0L) {
            return Fmt.dollars(money * 100L);
        }
        if (item.isEmpty()) {
            return "\u2013";
        }
        return (String)(item.getCount() > 1 ? item.getCount() + "x " : "") + item.getHoverName().getString();
    }

    public static ItemStack displayStack() {
        if (money <= 0L) {
            return item;
        }
        ItemStack gold = new ItemStack((ItemLike)Items.GOLD_INGOT);
        gold.set(DataComponents.CUSTOM_NAME, (Object)Component.literal((String)Giveaway.prizeName()).withStyle(ChatFormatting.GOLD));
        return gold;
    }

    public static String word() {
        return word;
    }

    public static int limit() {
        return limit;
    }

    public static List<Participant> participants() {
        return new ArrayList<Participant>(participants);
    }

    public static String winner() {
        return winner;
    }

    public static long drawnAt() {
        return drawnAt;
    }

    public static boolean drawing() {
        return status == Status.ENDED && winner != null && System.currentTimeMillis() - drawnAt < 4500L;
    }

    public static long remainingMs() {
        long now = status == Status.PAUSED ? pausedAt : System.currentTimeMillis();
        return Math.max(0L, startedAt + pausedTotal + durationMs - now);
    }

    public static float progress() {
        if (durationMs <= 0L) {
            return 0.0f;
        }
        return Math.max(0.0f, Math.min(1.0f, (float)Giveaway.remainingMs() / (float)durationMs));
    }

    public static boolean visible() {
        return Giveaway.active() || status == Status.ENDED && System.currentTimeMillis() < drawnAt + 60000L;
    }

    public static String joinCommand() {
        return "/msg " + FwmGfx.playerName() + " " + word;
    }

    public static void accept(Component message) {
        if (status != Status.RUNNING || message == null || word.isEmpty()) {
            return;
        }
        Messages.Parsed dm = Messages.parse(message);
        if (dm == null || dm.sent()) {
            return;
        }
        String from = dm.partner();
        String text = dm.text();
        if (!text.trim().toLowerCase(Locale.ROOT).equals(word.toLowerCase(Locale.ROOT))) {
            return;
        }
        for (Participant p : participants) {
            if (!p.name().equalsIgnoreCase(from)) continue;
            return;
        }
        participants.add(new Participant(from, System.currentTimeMillis()));
        Giveaway.save();
    }

    private static void notifyPay(String name, int entrants) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }
        String command = "/pay " + name + " " + money;
        client.player.sendSystemMessage((Component)Component.literal((String)"[Giveaway] ").withStyle(ChatFormatting.LIGHT_PURPLE).append((Component)Component.literal((String)("Gewinner: " + name + " (" + entrants + " Teilnehmer). Preis: " + Giveaway.prizeName() + " ")).withStyle(ChatFormatting.WHITE)).append((Component)Component.literal((String)"[Auszahlen]").withStyle(new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}).withStyle(s -> s.withClickEvent((ClickEvent)new ClickEvent.SuggestCommand(command)).withHoverEvent((HoverEvent)new HoverEvent.ShowText((Component)Component.literal((String)command))))));
    }

    private static void notify(String text) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }
        client.player.sendSystemMessage((Component)Component.literal((String)"[Giveaway] ").withStyle(ChatFormatting.LIGHT_PURPLE).append((Component)Component.literal((String)text).withStyle(ChatFormatting.WHITE)));
    }

    public static void tick(Minecraft client) {
        if (client.level == null || client.player == null) {
            return;
        }
        if (!restored) {
            restored = true;
            Giveaway.load();
        }
        if (status == Status.RUNNING && Giveaway.remainingMs() <= 0L) {
            Giveaway.draw();
        }
    }

    private static void save() {
        if (status == Status.NONE || item.isEmpty() && money <= 0L) {
            StreamerStore.save(FILE, null);
            return;
        }
        State state = new State();
        state.money = money;
        if (money <= 0L) {
            state.item = StreamerStore.encode(item);
            if (state.item == null) {
                return;
            }
        }
        state.word = word;
        state.limit = limit;
        state.durationMs = durationMs;
        state.startedAt = startedAt;
        state.pausedAt = pausedAt;
        state.pausedTotal = pausedTotal;
        state.status = status;
        state.participants = new ArrayList<Participant>(participants);
        state.winner = winner;
        state.drawnAt = drawnAt;
        StreamerStore.save(FILE, state);
    }

    private static void load() {
        ItemStack stack;
        State state = StreamerStore.load(FILE, State.class);
        if (state == null || state.item == null && state.money <= 0L || state.status == null || state.status == Status.NONE) {
            return;
        }
        if (state.status == Status.ENDED && System.currentTimeMillis() > state.drawnAt + 86400000L) {
            StreamerStore.save(FILE, null);
            return;
        }
        ItemStack itemStack = stack = state.money > 0L ? ItemStack.EMPTY : StreamerStore.decode(state.item);
        if (stack.isEmpty() && state.money <= 0L) {
            return;
        }
        item = stack;
        money = Math.max(0L, state.money);
        word = state.word == null ? "" : state.word;
        limit = state.limit;
        durationMs = state.durationMs;
        startedAt = state.startedAt;
        pausedAt = state.pausedAt;
        pausedTotal = state.pausedTotal;
        status = state.status;
        participants.clear();
        if (state.participants != null) {
            participants.addAll(state.participants);
        }
        winner = state.winner;
        drawnAt = state.drawnAt;
    }

    static {
        word = "";
        status = Status.NONE;
        participants = new ArrayList<Participant>();
    }

    public static enum Status {
        NONE,
        RUNNING,
        PAUSED,
        ENDED;

    }

    public record Participant(String name, long joinedAt) {
    }

    private static class State {
        JsonElement item;
        long money;
        String word;
        int limit;
        long durationMs;
        long startedAt;
        long pausedAt;
        long pausedTotal;
        Status status;
        List<Participant> participants;
        String winner;
        long drawnAt;

        private State() {
        }
    }
}

