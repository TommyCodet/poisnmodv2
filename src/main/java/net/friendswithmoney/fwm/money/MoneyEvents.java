/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.network.chat.Component
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.ItemLore
 *  net.minecraft.world.scores.DisplaySlot
 *  net.minecraft.world.scores.Objective
 *  net.minecraft.world.scores.PlayerScoreEntry
 *  net.minecraft.world.scores.PlayerTeam
 *  net.minecraft.world.scores.Scoreboard
 *  net.minecraft.world.scores.Team
 */
package net.friendswithmoney.fwm.money;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.loader.api.FabricLoader;
import net.friendswithmoney.fwm.auction.Money;
import net.friendswithmoney.fwm.auction.Payments;
import net.friendswithmoney.fwm.chat.ServerLine;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.money.History;
import net.friendswithmoney.fwm.money.MoneyStore;
import net.friendswithmoney.fwm.net.Presence;
import net.friendswithmoney.fwm.net.ServerContext;
import net.friendswithmoney.fwm.ui.FwmNotice;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.util.Fmt;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;

public final class MoneyEvents {
    private static final long DUPLICATE_MS = 1500L;
    private static final int SIDEBAR_SETTLE_TICKS = 60;
    private static final Pattern DOLLAR = Pattern.compile("[$]\\s?([0-9][0-9.,]*)\\s*([kmbKMB])?");
    private static final Pattern ORDER = Pattern.compile("^(?:\\[HugoSMP\\]\\s*)?Geliefert:\\s*[0-9][0-9.,]*\\.?\\s*Auszahlung:\\s*[$](?<amount>[0-9][0-9.,]*)\\s*(?<unit>[kmbKMB])?");
    public static final Pattern AH_SOLD = Pattern.compile("^Auktion gekauft:\\s*(?<name>[.*]?[A-Za-z0-9_]{2,16})\\s+hat\\s+(?<count>[0-9]+)x\\s+(?<item>.+?)\\s+f(?:\u00fc|ue)r\\s+[$](?<amount>[0-9][0-9.,]*)\\s*(?<unit>[kmbKMB])?\\s*gekauft");
    public static final Pattern ORDER_FILLED = Pattern.compile("^Order beliefert:\\s*(?<name>[.*]?[A-Za-z0-9_]{2,16})\\s+hat\\s+(?<count>[0-9]+)x\\s+(?<item>.+?)\\s+f(?:\u00fc|ue)r\\s+[$](?<amount>[0-9][0-9.,]*)\\s*(?<unit>[kmbKMB])?\\s*geliefert");
    private static final Pattern WIND_SELL = Pattern.compile("^Sold container items for \\$([0-9.,]+[KMBTkmbt]?)\\.$");
    public static final Pattern DONUT_OWN_ORDER = Pattern.compile("^(You ordered .+|[A-Za-z0-9_.*]{2,17} delivered you .+|Your .+ order is complete!?)$");
    public static final Pattern DONUT_DELIVERED = Pattern.compile("^You delivered (.+?) and received \\$ ?([0-9][0-9.,]*[KMBTkmbt]?)\\.?$");
    public static final Pattern DONUT_BOUGHT = Pattern.compile("^You bought (.+?) for \\$ ?([0-9][0-9.,]*[KMBTkmbt]?)\\.?$");
    public static final Pattern WIND_ORDER_PAYMENT = Pattern.compile("^Order payment: \\+\\$ ?([0-9][0-9.,]*[KMBTkmbt]?)");
    private static final Pattern WIND_ORDER_CLAIMED = Pattern.compile("^Claimed [0-9][0-9.,]*[KMBkmb]? ordered item");
    private static final Pattern ORDER_PICKED = Pattern.compile("^\\[HugoSMP\\]\\s*Abgeholt:");
    private static final Pattern ORDER_DELIVERED = Pattern.compile("^(?:\\[HugoSMP\\]\\s*)?Geliefert:\\s*[0-9][0-9.,]*\\.?\\s*Auszahlung:");
    private static final Pattern AH_BOUGHT = Pattern.compile("Du hast die Auktion erfolgreich gekauft");
    private static final Pattern SELLER = Pattern.compile("^\\s*(?:Verk\u00e4ufer|Verkaeufer|Seller)\\s*:\\s*([.*]?[A-Za-z0-9_]{2,16})");
    private static final Pattern PRICE_LINE = Pattern.compile("(?:Preis|Price|Kosten|Sofortkauf)[^$]*[$]\\s*([0-9][0-9.,]*)\\s*([kmbKMB])?", 2);
    private static final Pattern ANY_DOLLAR = Pattern.compile("[$]\\s*([0-9][0-9.,]*)\\s*([kmbKMB])?");
    private static final long OFFER_MS = 60000L;
    private static Offer pendingOffer;
    private static final Pattern FEE_LINE;
    private static final Pattern AH_CREATED;
    private static final Pattern ORDER_CREATED;
    private static Fee pendingFee;
    private static final List<String> BALANCE_WORDS;
    private static final Map<String, Pattern> PATTERNS;
    private static String lastKey;
    private static long lastAt;
    private static long sidebarBase;
    private static long chatAtBase;
    private static long chatTotal;
    private static long sidebarSeen;
    private static int sidebarStill;
    private static int sidebarTimer;
    private static ClientPacketListener connection;
    private static boolean goalChecked;

    public static boolean isMarketLine(String plain) {
        if (plain == null) {
            return false;
        }
        return AH_BOUGHT.matcher(plain).find() || AH_CREATED.matcher(plain).find() || ORDER_CREATED.matcher(plain).find() || ORDER_DELIVERED.matcher(plain).find() || ORDER_PICKED.matcher(plain).find() || WIND_ORDER_CLAIMED.matcher(plain).find() || DONUT_DELIVERED.matcher(plain).find() || DONUT_BOUGHT.matcher(plain).find() || DONUT_OWN_ORDER.matcher(plain).find();
    }

    public static boolean shown(Component message, boolean actionBar) {
        if (message == null) {
            return true;
        }
        FwmConfig c = FwmConfig.INSTANCE;
        String line = ServerLine.plain(message).trim();
        if (actionBar) {
            if (!c.moneyShowAhSold && AH_SOLD.matcher(line).find()) {
                return false;
            }
            return c.moneyShowOrderFilled || !ORDER_FILLED.matcher(line).find();
        }
        if (!c.moneyShowAhBought && AH_BOUGHT.matcher(line).find() && ServerLine.fromServer(message)) {
            return false;
        }
        return c.moneyShowOrderDelivered || !ORDER_DELIVERED.matcher(line).find();
    }

    public static void offerClicked(ItemStack stack) {
        long cents;
        if (stack == null || stack.isEmpty()) {
            return;
        }
        ItemLore lore = (ItemLore)stack.get(DataComponents.LORE);
        if (lore == null) {
            return;
        }
        String seller = null;
        long price = -1L;
        long anyPrice = -1L;
        for (Component text : lore.lines()) {
            Matcher d;
            Matcher p;
            String s = ServerLine.strip(text.getString());
            Matcher who = SELLER.matcher(s);
            if (who.find()) {
                seller = who.group(1);
            }
            if ((p = PRICE_LINE.matcher(s)).find() && price < 0L) {
                price = Money.cents(p.group(1) + (p.group(2) == null ? "" : p.group(2)));
            }
            if (!(d = ANY_DOLLAR.matcher(s)).find() || anyPrice >= 0L) continue;
            anyPrice = Money.cents(d.group(1) + (d.group(2) == null ? "" : d.group(2)));
        }
        if (seller == null) {
            return;
        }
        long l = cents = price > 0L ? price : anyPrice;
        if (cents <= 0L) {
            return;
        }
        String item = (String)(stack.getCount() > 1 ? stack.getCount() + "x " : "1x ") + ServerLine.strip(stack.getHoverName().getString());
        pendingOffer = new Offer(cents, seller, MoneyEvents.clean(item), System.currentTimeMillis());
    }

    public static void menuClicked(AbstractContainerMenu handler) {
        if (handler == null) {
            return;
        }
        for (Slot slot : handler.slots) {
            ItemLore lore;
            ItemStack stack;
            if (slot.container instanceof Inventory || (stack = slot.getItem()).isEmpty() || (lore = (ItemLore)stack.get(DataComponents.LORE)) == null) continue;
            for (Component text : lore.lines()) {
                Matcher m = FEE_LINE.matcher(ServerLine.strip(text.getString()));
                if (!m.find()) continue;
                long cents = Money.cents(m.group(1) + (m.group(2) == null ? "" : m.group(2)));
                if (cents > 0L) {
                    pendingFee = new Fee(cents, MoneyEvents.clean(ServerLine.strip(stack.getHoverName().getString())), System.currentTimeMillis());
                }
                return;
            }
        }
    }

    private MoneyEvents() {
    }

    public static boolean active(Minecraft client) {
        if (!Features.isOn("fwm.tab.money") || client.player == null) {
            return false;
        }
        ServerContext.Server server = ServerContext.current();
        return Presence.onHugo(client) || server == ServerContext.Server.WIND || server == ServerContext.Server.DONUT;
    }

    public static void accept(Component message, boolean overlay) {
        Matcher order;
        Minecraft client = Minecraft.getInstance();
        if (message == null || !MoneyEvents.active(client)) {
            return;
        }
        String line = ServerLine.plain(message);
        if (line.isEmpty()) {
            return;
        }
        if (ServerContext.current() == ServerContext.Server.DONUT) {
            if (!ServerLine.fromServer(message)) {
                return;
            }
            String t = line.trim();
            Matcher in = Payments.DONUT_IN.matcher(t);
            if (in.find()) {
                MoneyEvents.book(MoneyStore.Kind.PAY_IN, Money.cents(in.group("amount")), in.group("name"), null, line);
                return;
            }
            Matcher out = Payments.DONUT_OUT.matcher(t);
            if (out.find()) {
                MoneyEvents.book(MoneyStore.Kind.PAY_OUT, Money.cents(out.group("amount")), out.group("name"), null, line);
                return;
            }
            Matcher delivered = DONUT_DELIVERED.matcher(t);
            if (delivered.find()) {
                MoneyEvents.book(MoneyStore.Kind.ORDER, Money.cents(delivered.group(2)), null, MoneyEvents.clean(delivered.group(1)), line);
                return;
            }
            Matcher bought = DONUT_BOUGHT.matcher(t);
            if (bought.find()) {
                MoneyEvents.book(MoneyStore.Kind.AH_BUY, Money.cents(bought.group(2)), null, MoneyEvents.clean(bought.group(1)), line);
            }
            return;
        }
        if (ServerContext.current() == ServerContext.Server.WIND) {
            String t = line.trim();
            if (overlay) {
                Matcher paid = WIND_ORDER_PAYMENT.matcher(t);
                if (paid.find()) {
                    MoneyEvents.book(MoneyStore.Kind.ORDER, Money.cents(paid.group(1)), null, null, line);
                }
                return;
            }
            if (!ServerLine.fromServer(message)) {
                return;
            }
            Matcher in = Payments.WIND_IN.matcher(t);
            if (in.find()) {
                MoneyEvents.book(MoneyStore.Kind.PAY_IN, Money.cents(in.group("amount")), in.group("name"), null, line);
                return;
            }
            Matcher out = Payments.WIND_OUT.matcher(t);
            if (out.find()) {
                MoneyEvents.book(MoneyStore.Kind.PAY_OUT, Money.cents(out.group("amount")), out.group("name"), null, line);
                return;
            }
            Matcher sold = WIND_SELL.matcher(t);
            if (sold.find()) {
                MoneyEvents.book(MoneyStore.Kind.SELL, Money.cents(sold.group(1)), null, null, line);
            }
            return;
        }
        if (!overlay && AH_BOUGHT.matcher(line).find() && ServerLine.fromServer(message)) {
            if (pendingOffer != null && System.currentTimeMillis() - pendingOffer.at() < 60000L) {
                MoneyEvents.book(MoneyStore.Kind.AH_BUY, pendingOffer.cents(), pendingOffer.seller(), pendingOffer.item(), line);
            }
            pendingOffer = null;
            return;
        }
        if (!overlay && (AH_CREATED.matcher(line).find() || ORDER_CREATED.matcher(line).find()) && ServerLine.fromServer(message)) {
            if (pendingFee != null && System.currentTimeMillis() - pendingFee.at() < 60000L) {
                MoneyEvents.book(MoneyStore.Kind.FEE, pendingFee.cents(), null, pendingFee.item(), line);
            }
            pendingFee = null;
            return;
        }
        if (line.indexOf(36) < 0) {
            return;
        }
        if (FwmConfig.INSTANCE.moneyLogLines) {
            MoneyEvents.log(line, overlay);
        }
        if (overlay) {
            Matcher sold = AH_SOLD.matcher(line.trim());
            if (sold.find()) {
                String unit = sold.group("unit");
                MoneyEvents.book(MoneyStore.Kind.AH_SELL, Money.cents(sold.group("amount") + (unit == null ? "" : unit)), sold.group("name"), MoneyEvents.clean(sold.group("count") + "x " + sold.group("item")), line);
                return;
            }
            Matcher filled = ORDER_FILLED.matcher(line.trim());
            if (filled.find()) {
                String unit = filled.group("unit");
                MoneyEvents.book(MoneyStore.Kind.ORDER_BUY, Money.cents(filled.group("amount") + (unit == null ? "" : unit)), filled.group("name"), MoneyEvents.clean(filled.group("count") + "x " + filled.group("item")), line);
                return;
            }
        }
        if ((order = ORDER.matcher(line.trim())).find()) {
            String unit = order.group("unit");
            MoneyEvents.book(MoneyStore.Kind.ORDER, Money.cents(order.group("amount") + (unit == null ? "" : unit)), null, null, line);
            return;
        }
        if (!overlay && !ServerLine.fromServer(message)) {
            return;
        }
        FwmConfig config = FwmConfig.INSTANCE;
        Matcher sell = MoneyEvents.pattern(config.sellMessagePattern).matcher(line);
        if (sell.find()) {
            MoneyEvents.book(MoneyStore.Kind.SELL, Money.cents(sell.group(1)), null, null, line);
            return;
        }
        if (MoneyEvents.tryList(config.moneyPayOutPatterns, MoneyStore.Kind.PAY_OUT, line)) {
            return;
        }
        if (MoneyEvents.tryList(config.moneyAhBuyPatterns, MoneyStore.Kind.AH_BUY, line)) {
            return;
        }
        if (MoneyEvents.tryList(config.moneyAhSellPatterns, MoneyStore.Kind.AH_SELL, line)) {
            return;
        }
        MoneyEvents.tryList(config.payPatterns, MoneyStore.Kind.PAY_IN, line);
    }

    private static boolean tryList(List<String> sources, MoneyStore.Kind kind, String line) {
        if (sources == null) {
            return false;
        }
        for (String source : sources) {
            String unit;
            long cents;
            String amount;
            Matcher m = MoneyEvents.pattern(source).matcher(line);
            if (!m.find() || (amount = MoneyEvents.group(m, "amount")) == null || (cents = Money.cents(amount + ((unit = MoneyEvents.group(m, "unit")) == null ? "" : unit))) <= 0L) continue;
            MoneyEvents.book(kind, cents, MoneyEvents.group(m, "name"), MoneyEvents.clean(MoneyEvents.group(m, "item")), line);
            return true;
        }
        return false;
    }

    private static void toast(MoneyStore.Kind kind, long cents, String player, String item) {
    }

    private static void book(MoneyStore.Kind kind, long cents, String player, String item, String line) {
        if (cents <= 0L) {
            return;
        }
        MoneyEvents.toast(kind, cents, player, item);
        long now = System.currentTimeMillis();
        String key = String.valueOf((Object)kind) + ":" + cents + ":" + player;
        if (key.equals(lastKey) && now - lastAt < 1500L) {
            return;
        }
        lastKey = key;
        lastAt = now;
        MoneyStore.book(kind, cents);
        History.add(kind, cents, player, item, line);
        chatTotal += kind.income ? cents : -cents;
        goalChecked = false;
    }

    public static void tick(Minecraft client) {
        ClientPacketListener handler = client.getConnection();
        if (handler != connection) {
            if (connection != null) {
                MoneyStore.left();
                History.save();
            }
            connection = handler;
            sidebarBase = -1L;
            sidebarSeen = -1L;
            if (handler != null && MoneyEvents.active(client)) {
                MoneyStore.joined();
            }
        }
        if (!MoneyEvents.active(client)) {
            return;
        }
        if (FwmConfig.INSTANCE.moneySidebarSync && ++sidebarTimer >= 10) {
            sidebarTimer = 0;
            MoneyEvents.sidebar(client);
        }
        if (!goalChecked) {
            goalChecked = true;
            MoneyEvents.checkGoal(client);
        }
        MoneyStore.tick();
        History.tick();
    }

    private static void sidebar(Minecraft client) {
        long balance = MoneyEvents.readBalance(client);
        if (balance < 0L) {
            return;
        }
        if (balance != sidebarSeen) {
            sidebarSeen = balance;
            sidebarStill = 0;
            return;
        }
        if ((sidebarStill += 10) < 60) {
            return;
        }
        if (sidebarBase < 0L) {
            sidebarBase = balance;
            chatAtBase = chatTotal;
            return;
        }
        if (balance == sidebarBase && chatTotal == chatAtBase) {
            return;
        }
        long expected = sidebarBase + (chatTotal - chatAtBase);
        long gap = balance - expected;
        long tolerance = Math.max(100L, Math.abs(balance) / 500L);
        if (Math.abs(gap) > tolerance) {
            MoneyStore.Kind kind = gap > 0L ? MoneyStore.Kind.OTHER_IN : MoneyStore.Kind.OTHER_OUT;
            MoneyStore.book(kind, Math.abs(gap));
            History.add(kind, Math.abs(gap), null, null, Lang.t("Kontostand", "Balance") + ": " + Fmt.money(balance));
            goalChecked = false;
        }
        sidebarBase = balance;
        chatAtBase = chatTotal;
    }

    private static long readBalance(Minecraft client) {
        if (client.level == null) {
            return -1L;
        }
        Scoreboard board = client.level.getScoreboard();
        Objective objective = board.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (objective == null) {
            return -1L;
        }
        long fallback = -1L;
        int dollarLines = 0;
        for (PlayerScoreEntry entry : board.listPlayerScores(objective)) {
            PlayerTeam team;
            String text;
            Matcher m;
            if (entry.isHidden() || !(m = DOLLAR.matcher(text = ServerLine.strip(PlayerTeam.formatNameForTeam((Team)(team = board.getPlayersTeam(entry.owner())), (Component)entry.ownerName()).getString()))).find()) continue;
            long cents = Money.cents(m.group(1) + (m.group(2) == null ? "" : m.group(2)));
            String lower = text.toLowerCase(Locale.ROOT);
            for (String word : BALANCE_WORDS) {
                if (!lower.contains(word)) continue;
                return cents;
            }
            ++dollarLines;
            fallback = cents;
        }
        return dollarLines == 1 ? fallback : -1L;
    }

    private static void checkGoal(Minecraft client) {
        long goal = FwmConfig.INSTANCE.moneyGoal * 100L;
        if (goal <= 0L || MoneyStore.todayNet() < goal) {
            return;
        }
        if (!MoneyStore.claimGoal()) {
            return;
        }
        if (client.player != null) {
            client.player.sendSystemMessage((Component)Component.literal((String)"[FWM] ").withStyle(ChatFormatting.GREEN).append((Component)Component.literal((String)(Lang.t("Tagesziel geschafft: ", "Daily goal reached: ") + Fmt.compactMoney(goal))).withStyle(ChatFormatting.GOLD)));
            if (FwmConfig.INSTANCE.moneyGoalSound) {
                client.player.playSound(SoundEvents.PLAYER_LEVELUP, 0.35f, 1.2f);
            }
        }
    }

    private static void log(String line, boolean overlay) {
        try {
            Path file = FabricLoader.getInstance().getConfigDir().resolve("fwm-money-log.txt");
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            Files.writeString(file, (CharSequence)(stamp + (overlay ? " [Actionbar] " : " ") + line + System.lineSeparator()), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static String clean(String item) {
        if (item == null) {
            return null;
        }
        String s = item.replaceAll("^\\s*\\d+\\s*[x\u00d7]\\s*", "").trim();
        return s.isEmpty() ? null : (s.length() > 40 ? s.substring(0, 40) : s);
    }

    private static String group(Matcher m, String name) {
        try {
            return m.group(name);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static Pattern pattern(String source) {
        if (source == null || source.isBlank()) {
            return Pattern.compile("(?!)");
        }
        return PATTERNS.computeIfAbsent(source, s -> {
            try {
                return Pattern.compile(s, 2);
            }
            catch (Exception e) {
                System.err.println("[FWM] Bad money pattern '" + s + "': " + e.getMessage());
                return Pattern.compile("(?!)");
            }
        });
    }

    private static /* synthetic */ void lambda$toast$0(String title, long cents, String item, String player) {
        FwmNotice.show(title.toUpperCase(Locale.ROOT), Fmt.money(cents), (item == null ? "" : item) + (String)(player == null ? "" : (item == null ? "" : " \u00b7 ") + player), 6088852, 10000L);
    }

    static {
        FEE_LINE = Pattern.compile("^\\s*(?:Einstellgeb\u00fchr|Erstellgeb\u00fchr|Einstellgebuehr|Erstellgebuehr)\\s*:\\s*[$]\\s*([0-9][0-9.,]*)\\s*([kmbKMB])?");
        AH_CREATED = Pattern.compile("^\\[HugoSMP\\]\\s*Auktion mit .* erstellt");
        ORDER_CREATED = Pattern.compile("^\\[HugoSMP\\]\\s*Order erstellt");
        BALANCE_WORDS = List.of("kontostand", "guthaben", "geld", "money", "balance", "bank");
        PATTERNS = new HashMap<String, Pattern>();
        lastKey = "";
        sidebarBase = -1L;
        sidebarSeen = -1L;
    }

    private record Offer(long cents, String seller, String item, long at) {
    }

    private record Fee(long cents, String item, long at) {
    }
}

