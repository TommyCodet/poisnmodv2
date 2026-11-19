/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
 *  net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.KeyMapping$Category
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.Identifier
 */
package net.friendswithmoney.fwm.stream;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.friendswithmoney.fwm.auction.Auction;
import net.friendswithmoney.fwm.auction.Giveaway;
import net.friendswithmoney.fwm.auction.Payments;
import net.friendswithmoney.fwm.chat.ServerLine;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.stream.DonationBoard;
import net.friendswithmoney.fwm.stream.DonationFx;
import net.friendswithmoney.fwm.stream.DonationStore;
import net.friendswithmoney.fwm.stream.DonationTier;
import net.friendswithmoney.fwm.stream.fx.Play;
import net.friendswithmoney.fwm.ui.FwmGfx;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.panel.DonationStudioScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class Donations {
    private static final long AUCTION_LATE_MS = 600000L;
    private static final long SETTLE_MS = 3000L;
    private static final long FIGHT_MS = 10000L;
    private static final Map<String, Pending> PENDING = new LinkedHashMap<String, Pending>();
    private static final List<Item> QUEUE = new ArrayList<Item>();
    private static long seq;
    private static long lastHurt;
    private static boolean wasStreaming;
    private static boolean registered;
    public static KeyMapping SKIP;
    public static volatile boolean ignoreFocus;
    public static String lastCounted;

    private Donations() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        DonationStore.load();
        SKIP = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("FWM: Donation \u00fcberspringen", InputConstants.Type.KEYSYM, -1, KeyMapping.Category.MISC));
        ClientTickEvents.END_CLIENT_TICK.register(Donations::tick);
        HudElementRegistry.addLast((Identifier)Identifier.fromNamespaceAndPath((String)"fwm", (String)"donation_fx"), (ctx, counter) -> DonationFx.renderHud(ctx));
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> ScreenEvents.afterExtract((Screen)screen).register((s, ctx, mx, my, delta) -> DonationFx.renderOverScreen(ctx)));
        ScreenEvents.BEFORE_INIT.register((client, screen, w, h) -> {
            if (Features.isOn("fwm.tab.donations")) {
                DonationStudioScreen.maybeSnapshot(screen);
            }
        });
        DonationBoard.register();
    }

    public static void accept(Component message) {
        if (message == null) {
            return;
        }
        if (!Features.isOn("fwm.tab.donations")) {
            return;
        }
        if (!ServerLine.fromServer(message)) {
            return;
        }
        Payments.Payment paid = Payments.read(ServerLine.plain(message), Donations.self());
        if (paid == null || paid.amount() <= 0L) {
            return;
        }
        Result r = Donations.offer(paid.name(), paid.amount(), System.currentTimeMillis());
        if (r != Result.COUNTED) {
            System.out.println("[FWM] Donation ignored (" + String.valueOf((Object)r) + "): " + paid.name());
        }
    }

    private static String self() {
        String name = FwmGfx.playerName();
        return name.isEmpty() ? null : name;
    }

    public static boolean live() {
        if (!Features.isOn("fwm.tab.donations")) {
            return false;
        }
        return !DonationStore.get().onlyStreamer || Donations.streaming();
    }

    public static boolean streaming() {
        return DonationStore.get().streamLive || FwmConfig.INSTANCE.moneyHideAmounts;
    }

    public static Result busy(long now) {
        if (Auction.running()) {
            return Result.AUCTION;
        }
        if (!Auction.item().isEmpty() && Auction.endsAt() > 0L && now < Auction.endsAt() + 600000L) {
            return Result.AUCTION;
        }
        if (Giveaway.active()) {
            return Result.GIVEAWAY;
        }
        return null;
    }

    public static Result offer(String name, long cents, long now) {
        if (name == null || name.isBlank() || cents <= 0L) {
            return Result.INVALID;
        }
        if (!Features.isOn("fwm.tab.donations")) {
            return Result.OFF;
        }
        if (DonationStore.get().onlyStreamer && !Donations.streaming()) {
            return Result.NOT_STREAMING;
        }
        Result busy = Donations.busy(now);
        if (busy != null) {
            return busy;
        }
        String me = Donations.self();
        if (me != null && me.equalsIgnoreCase(name)) {
            return Result.SELF;
        }
        if (DonationStore.blocked(name)) {
            return Result.BLOCKED;
        }
        String key = name.toLowerCase(Locale.ROOT);
        Pending p = PENDING.get(key);
        long window = (long)DonationStore.get().mergeSeconds * 1000L;
        if (p != null && now - p.first <= window) {
            p.sum += cents;
            ++p.count;
            p.last = now;
        } else {
            if (p != null) {
                Donations.flush(p, now);
            }
            p = new Pending();
            p.name = name;
            p.sum = cents;
            p.count = 1;
            p.first = now;
            p.last = now;
            PENDING.put(key, p);
        }
        if (window <= 0L) {
            PENDING.remove(key);
            Donations.flush(p, now);
        }
        return Result.COUNTED;
    }

    public static void flushDue(long now) {
        if (PENDING.isEmpty()) {
            return;
        }
        long window = (long)DonationStore.get().mergeSeconds * 1000L;
        long settle = Math.min(window, 3000L);
        Iterator<Pending> it = PENDING.values().iterator();
        while (it.hasNext()) {
            Pending p = it.next();
            if (now - p.first <= window && now - p.last <= settle) continue;
            it.remove();
            Donations.flush(p, now);
        }
    }

    private static void flush(Pending p, long now) {
        DonationTier tier = DonationStore.tierFor(p.sum);
        long goalBefore = Donations.goalProgress();
        long total = DonationStore.record(p.name, p.sum, p.count, tier == null ? "" : tier.name, now);
        lastCounted = p.name + " " + p.sum + " x" + p.count + " -> " + (tier == null ? "-" : tier.name);
        if (tier != null) {
            Item item = new Item();
            item.name = p.name;
            item.amount = p.sum;
            item.count = p.count;
            item.total = total;
            item.tier = tier.copy();
            item.at = now;
            Donations.enqueue(item);
        } else if (DonationStore.get().gapChat) {
            DonationTier gap = new DonationTier();
            gap.text = "+{amount} from {name}";
            gap.sound = "none";
            Item item = new Item();
            item.name = p.name;
            item.amount = p.sum;
            item.count = p.count;
            item.total = total;
            item.tier = gap;
            DonationFx.chatLine(item);
        }
        Donations.checkGoal(goalBefore);
    }

    private static void enqueue(Item item) {
        item.seq = seq++;
        if ("chat".equals(item.tier.anim)) {
            DonationFx.chatLine(item);
            return;
        }
        if (QUEUE.size() >= 10) {
            Minecraft client = Minecraft.getInstance();
            if (client.gui != null) {
                client.gui.hud.getChat().addClientSystemMessage((Component)Component.literal((String)"[FWM] ").withStyle(ChatFormatting.GREEN).append((Component)Component.literal((String)(item.name + " " + Play.money(item.amount) + Lang.t(" (Warteschlange voll)", " (queue full)"))).withStyle(ChatFormatting.WHITE)));
            }
            return;
        }
        QUEUE.add(item);
        QUEUE.sort((a, b) -> {
            if (a.goal != b.goal) {
                return a.goal ? -1 : 1;
            }
            int c = Long.compare(DonationStore.weight(b.tier), DonationStore.weight(a.tier));
            return c != 0 ? c : Long.compare(a.seq, b.seq);
        });
    }

    public static String queueOrder() {
        StringBuilder sb = new StringBuilder();
        for (Item i : QUEUE) {
            sb.append(i.name).append('/').append(i.tier.name).append(' ');
        }
        return sb.toString().trim();
    }

    public static int queued() {
        return QUEUE.size();
    }

    public static void clearQueue() {
        QUEUE.clear();
        PENDING.clear();
    }

    public static void test(DonationTier tier, long amount) {
        Item item = new Item();
        item.name = DonationStore.get().testName;
        item.amount = amount > 0L ? amount : tier.sampleAmount();
        item.count = 1;
        item.total = item.amount * 3L;
        item.tier = tier.copy();
        item.tier.on = true;
        item.test = true;
        item.at = System.currentTimeMillis();
        DonationFx.skip();
        if ("chat".equals(item.tier.anim)) {
            DonationFx.chatLine(item);
            return;
        }
        QUEUE.add(0, item);
    }

    public static void testAnim(DonationTier tier, String animId, long amount) {
        DonationTier t = tier.copy();
        t.anim = animId;
        Donations.test(t, amount);
    }

    private static void tick(Minecraft client) {
        boolean fight;
        boolean streaming;
        DonationStore.tick();
        long now = System.currentTimeMillis();
        while (SKIP != null && SKIP.consumeClick()) {
            DonationFx.skip();
        }
        if (client.player != null && client.player.hurtTime > 0) {
            lastHurt = now;
        }
        if ((streaming = Donations.streaming()) && !wasStreaming && Features.isOn("fwm.tab.donations")) {
            DonationStore.resetSession();
        }
        wasStreaming = streaming;
        Donations.flushDue(now);
        DonationFx.tick(now);
        if (DonationFx.playing() || QUEUE.isEmpty()) {
            return;
        }
        if (client.level == null || !client.isWindowActive() && !ignoreFocus) {
            return;
        }
        Item next = QUEUE.remove(0);
        String anim = next.tier.anim;
        boolean bl = fight = now - lastHurt < 10000L;
        if (fight && DonationStore.get().fightSmall && !next.test && !"chat".equals(anim) && !"toast".equals(anim)) {
            anim = DonationStore.get().fightAnim;
        }
        DonationFx.start(next, anim);
    }

    public static long goalProgress() {
        long from = DonationStore.get().goalStart;
        long sum = 0L;
        for (DonationStore.Entry e : DonationStore.get().history) {
            if (e.time < from) continue;
            sum += e.amount;
        }
        return sum;
    }

    private static void checkGoal(long before) {
        DonationStore.Data d = DonationStore.get();
        if (d.goal <= 0L || d.goalDone) {
            return;
        }
        long after = Donations.goalProgress();
        if (before < d.goal && after >= d.goal) {
            d.goalDone = true;
            DonationStore.changed();
            Donations.celebrateGoal(false);
        }
    }

    public static void celebrateGoal(boolean test) {
        DonationStore.Data d = DonationStore.get();
        DonationTier t = new DonationTier();
        t.name = "GOAL REACHED";
        t.anim = "fireworks";
        t.color = -670398;
        t.sound = "totem";
        t.item = "minecraft:totem_of_undying";
        t.text = d.goalName == null || d.goalName.isBlank() ? "Goal reached: {amount}!" : d.goalName + ": {amount}!";
        Item item = new Item();
        String me = Donations.self();
        item.name = me == null ? "Steve" : me;
        item.amount = Math.max(d.goal, 100L);
        item.tier = t;
        item.goal = true;
        item.test = test;
        item.at = System.currentTimeMillis();
        item.seq = seq++;
        QUEUE.add(0, item);
    }

    static {
        lastCounted = "";
    }

    public static enum Result {
        COUNTED,
        OFF,
        NOT_STREAMING,
        AUCTION,
        GIVEAWAY,
        BLOCKED,
        SELF,
        INVALID;

    }

    private static final class Pending {
        String name;
        long sum;
        int count;
        long first;
        long last;

        private Pending() {
        }
    }

    static final class Item {
        String name;
        long amount;
        long total;
        int count = 1;
        DonationTier tier;
        boolean test;
        long at;
        long seq;
        boolean goal;

        Item() {
        }
    }
}

