/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 */
package net.friendswithmoney.fwm.auction;

import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.friendswithmoney.fwm.auction.AuctionStats;
import net.friendswithmoney.fwm.auction.Hologram;
import net.friendswithmoney.fwm.auction.Payments;
import net.friendswithmoney.fwm.auction.StreamerStore;
import net.friendswithmoney.fwm.chat.ServerLine;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.ui.FwmGfx;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class Auction {
    private static final int HISTORY = 6;
    private static final long AFTERGLOW_MS = 60000L;
    private static final long LATE_MS = 600000L;
    private static final String FILE = "fwm-auction-state.json";
    private static ItemStack item = ItemStack.EMPTY;
    private static long startBid;
    private static long minStep;
    private static long endsAt;
    private static long durationMs;
    private static final List<Bid> bids;
    private static final List<Bid> late;
    private static final List<Bid> blocked;
    private static int fakes;
    private static int extendedTotal;
    private static final Map<String, Long> paidBy;
    private static boolean missing;
    private static long extendedAt;
    private static int extendedBy;
    private static boolean restored;
    private static int ticks;

    private Auction() {
    }

    public static void start(ItemStack stack, int seconds, long startBidCents, long minStepCents) {
        Auction.keepResult();
        item = stack == null ? ItemStack.EMPTY : stack.copy();
        startBid = Math.max(0L, startBidCents);
        minStep = Math.max(0L, minStepCents);
        durationMs = (long)Math.max(1, seconds) * 1000L;
        endsAt = System.currentTimeMillis() + durationMs;
        bids.clear();
        late.clear();
        blocked.clear();
        fakes = 0;
        extendedTotal = 0;
        paidBy.clear();
        missing = false;
        Auction.save();
        Hologram.moveHere();
    }

    public static void hold(ItemStack stack) {
        if (Auction.running()) {
            return;
        }
        Auction.clearResult();
        item = stack == null ? ItemStack.EMPTY : stack.copy();
    }

    public static void stop() {
        if (endsAt > System.currentTimeMillis()) {
            endsAt = System.currentTimeMillis();
        }
        Auction.save();
    }

    public static void clearResult() {
        Auction.keepResult();
        endsAt = 0L;
        bids.clear();
        late.clear();
        blocked.clear();
        fakes = 0;
        extendedTotal = 0;
        paidBy.clear();
        missing = false;
        Auction.save();
    }

    private static void keepResult() {
        if (endsAt <= 0L || System.currentTimeMillis() < endsAt) {
            return;
        }
        String name = item == null || item.isEmpty() ? "" : item.getHoverName().getString();
        ArrayList<Bid> filtered = new ArrayList<Bid>(late);
        filtered.addAll(blocked);
        AuctionStats.record(endsAt, name, Auction.leader(), Auction.price(), Auction.earnings(), new ArrayList<Bid>(bids), filtered);
    }

    public static void clear() {
        Auction.clearResult();
        item = ItemStack.EMPTY;
    }

    public static boolean running() {
        return !item.isEmpty() && System.currentTimeMillis() < endsAt;
    }

    public static boolean hasResult() {
        return !item.isEmpty() && endsAt > 0L && System.currentTimeMillis() >= endsAt;
    }

    public static boolean visible() {
        return !item.isEmpty() && endsAt > 0L && System.currentTimeMillis() < endsAt + 60000L;
    }

    public static ItemStack item() {
        return item;
    }

    public static long remainingMs() {
        return Math.max(0L, endsAt - System.currentTimeMillis());
    }

    public static float progress() {
        if (durationMs <= 0L) {
            return 0.0f;
        }
        return Math.max(0.0f, Math.min(1.0f, (float)Auction.remainingMs() / (float)durationMs));
    }

    public static String leader() {
        Bid top = Auction.top();
        return top == null ? null : top.name();
    }

    public static long price() {
        Bid top = Auction.top();
        return top == null ? startBid : top.amount();
    }

    private static Bid top() {
        List<Bid> ranking = Auction.ranking();
        if (ranking.isEmpty() || ranking.get(0).amount() < startBid) {
            return null;
        }
        return ranking.get(0);
    }

    public static List<Bid> ranking() {
        ArrayList<Bid> out = new ArrayList<Bid>();
        if (FwmConfig.INSTANCE.auctionStackPayments) {
            HashMap<String, Integer> at = new HashMap<String, Integer>();
            for (Bid paid : bids) {
                String key = paid.name().toLowerCase(Locale.ROOT);
                Integer index = (Integer)at.get(key);
                if (index == null) {
                    at.put(key, out.size());
                    out.add(paid);
                    continue;
                }
                Bid sum = (Bid)out.get(index);
                out.set(index, new Bid(sum.name(), sum.amount() + paid.amount(), paid.time()));
            }
        } else {
            out.addAll(bids);
        }
        out.sort((a, b) -> a.amount() != b.amount() ? Long.compare(b.amount(), a.amount()) : Long.compare(a.time(), b.time()));
        return out;
    }

    public static List<Bid> late() {
        return new ArrayList<Bid>(late);
    }

    public static long startBid() {
        return startBid;
    }

    public static long minStep() {
        return minStep;
    }

    public static long nextMinimum() {
        if (Auction.top() == null) {
            return startBid;
        }
        return Auction.price() + Math.max(1L, minStep);
    }

    public static List<Bid> bids() {
        List<Bid> ranking = Auction.ranking();
        return new ArrayList<Bid>(ranking.subList(0, Math.min(6, ranking.size())));
    }

    public static List<Bid> allBids() {
        return Auction.ranking();
    }

    public static int bidCount() {
        return bids.size();
    }

    public static boolean itemMissing() {
        return missing;
    }

    public static long earnings() {
        long sum = 0L;
        Iterator<Object> iterator = paidBy.values().iterator();
        while (iterator.hasNext()) {
            long paid = iterator.next();
            sum += paid;
        }
        for (Bid paid : late) {
            sum += paid.amount();
        }
        for (Bid paid : blocked) {
            sum += paid.amount();
        }
        return sum;
    }

    public static List<Bid> blocked() {
        return new ArrayList<Bid>(blocked);
    }

    public static long refundDue() {
        long sum = 0L;
        for (Bid paid : blocked) {
            sum += paid.amount();
        }
        return sum;
    }

    public static int fakePayments() {
        return fakes;
    }

    public static int extendedTotal() {
        return extendedTotal;
    }

    public static int bidderCount() {
        return paidBy.size();
    }

    public static long endsAt() {
        return endsAt;
    }

    public static String plain(long cents) {
        return cents % 100L == 0L ? String.valueOf(cents / 100L) : String.format(Locale.ROOT, "%.2f", (double)cents / 100.0);
    }

    public static void accept(Component message) {
        boolean newHigh;
        boolean live;
        if (message == null || item.isEmpty() || endsAt <= 0L) {
            return;
        }
        long now = System.currentTimeMillis();
        boolean bl = live = now < endsAt;
        if (!live && now > endsAt + 600000L) {
            return;
        }
        if (!ServerLine.fromServer(message)) {
            Payments.Payment fake = Payments.read(message.getString(), Auction.self());
            if (fake != null && fake.amount() > 0L) {
                ++fakes;
                Auction.save();
            }
            return;
        }
        Payments.Payment paid = Payments.read(ServerLine.plain(message), Auction.self());
        if (paid == null || paid.amount() <= 0L) {
            return;
        }
        for (String blocked : V2.INSTANCE.auctionBlacklist) {
            if (!blocked.equalsIgnoreCase(paid.name())) continue;
            Auction.blocked.add(new Bid(paid.name(), paid.amount(), now));
            Auction.save();
            return;
        }
        if (!live) {
            late.add(new Bid(paid.name(), paid.amount(), now));
            Auction.save();
            return;
        }
        String leaderBefore = Auction.leader();
        long priceBefore = Auction.price();
        paidBy.merge(paid.name().toLowerCase(Locale.ROOT), paid.amount(), Long::sum);
        bids.add(new Bid(paid.name(), paid.amount(), now));
        boolean bl2 = newHigh = Auction.leader() != null && (leaderBefore == null || Auction.price() > priceBefore);
        if (newHigh) {
            Auction.extendIfLate(now);
        }
        Auction.save();
    }

    static void extendIfLate(long now) {
        FwmConfig config = FwmConfig.INSTANCE;
        if (!config.auctionAntiSnipe || endsAt - now > 3000L) {
            return;
        }
        int seconds = Math.max(1, config.auctionAntiSnipeSeconds);
        endsAt += (long)seconds * 1000L;
        durationMs += (long)seconds * 1000L;
        extendedAt = now;
        extendedBy = seconds;
        extendedTotal += seconds;
    }

    public static int recentExtension() {
        return System.currentTimeMillis() - extendedAt < 2000L ? extendedBy : 0;
    }

    private static String self() {
        String name = FwmGfx.playerName();
        return name.isEmpty() ? null : name;
    }

    private static void notify(String text) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }
        client.player.sendSystemMessage((Component)Component.literal((String)"[Auktion] ").withStyle(ChatFormatting.GOLD).append((Component)Component.literal((String)text).withStyle(ChatFormatting.WHITE)));
    }

    public static void tick(Minecraft client) {
        boolean now;
        if (client.level == null || client.player == null) {
            return;
        }
        if (!restored) {
            restored = true;
            Auction.load();
        }
        if (!Auction.running() || ++ticks % 20 != 0) {
            return;
        }
        boolean bl = now = StreamerStore.carried(item) < item.getCount();
        if (now && !missing) {
            Auction.notify("Achtung: Das versteigerte Item fehlt im Inventar!");
        }
        missing = now;
    }

    private static void save() {
        if (item.isEmpty() || endsAt <= 0L) {
            StreamerStore.save(FILE, null);
            return;
        }
        State state = new State();
        state.item = StreamerStore.encode(item);
        if (state.item == null) {
            return;
        }
        state.startBid = startBid;
        state.minStep = minStep;
        state.endsAt = endsAt;
        state.durationMs = durationMs;
        state.payments = new ArrayList<Bid>(bids);
        state.late = new ArrayList<Bid>(late);
        state.blocked = new ArrayList<Bid>(blocked);
        state.fakes = fakes;
        state.extendedTotal = extendedTotal;
        state.paidBy = new HashMap<String, Long>(paidBy);
        StreamerStore.save(FILE, state);
    }

    private static void load() {
        State state = StreamerStore.load(FILE, State.class);
        if (state == null || state.item == null) {
            return;
        }
        if (System.currentTimeMillis() > state.endsAt + 86400000L) {
            StreamerStore.save(FILE, null);
            return;
        }
        ItemStack stack = StreamerStore.decode(state.item);
        if (stack.isEmpty()) {
            return;
        }
        item = stack;
        startBid = state.startBid;
        minStep = state.minStep;
        endsAt = state.endsAt;
        durationMs = state.durationMs;
        bids.clear();
        late.clear();
        paidBy.clear();
        if (state.payments != null) {
            bids.addAll(state.payments);
        } else if (state.bids != null) {
            bids.addAll(state.bids);
        }
        if (state.late != null) {
            late.addAll(state.late);
        }
        blocked.clear();
        if (state.blocked != null) {
            blocked.addAll(state.blocked);
        }
        fakes = state.fakes;
        extendedTotal = state.extendedTotal;
        if (state.paidBy != null) {
            paidBy.putAll(state.paidBy);
        }
    }

    public static void start(ItemStack stack, int seconds, long startBidCents) {
        Auction.start(stack, seconds, startBidCents, 0L);
    }

    static {
        bids = new ArrayList<Bid>();
        late = new ArrayList<Bid>();
        blocked = new ArrayList<Bid>();
        paidBy = new HashMap<String, Long>();
    }

    public record Bid(String name, long amount, long time) {
    }

    private static class State {
        JsonElement item;
        long startBid;
        long minStep;
        long endsAt;
        long durationMs;
        List<Bid> bids;
        List<Bid> payments;
        List<Bid> late;
        List<Bid> blocked;
        int fakes;
        int extendedTotal;
        Map<String, Long> paidBy;

        private State() {
        }
    }
}

