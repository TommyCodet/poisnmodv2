/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
 *  net.fabricmc.fabric.api.client.command.v2.ClientCommands
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.multiplayer.ServerData
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.network.chat.ClickEvent
 *  net.minecraft.network.chat.ClickEvent$RunCommand
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.HoverEvent
 *  net.minecraft.network.chat.HoverEvent$ShowText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  org.joml.Matrix3x2fStack
 */
package net.friendswithmoney.fwm.game;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.friendswithmoney.fwm.auction.Payments;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.ui.FwmHud;
import net.friendswithmoney.fwm.ui.FwmNotice;
import net.friendswithmoney.fwm.ui.FwmTheme;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.LauncherGfx;
import net.friendswithmoney.fwm.ui.gfx.Shapes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.joml.Matrix3x2fStack;

public final class Todos {
    public static final String ONCE = "ONCE";
    public static final String DAILY = "DAILY";
    public static final String EVERY = "EVERY";
    public static final String SCOPE_ALL = "ALL";
    public static final String SCOPE_HERE = "HERE";
    public static final int NAME_MAX = 32;
    static final long HOUR = 3600000L;
    static final long DAY = 86400000L;
    static final long REMIND_GAP = 3600000L;
    static final long JOIN_DELAY = 5000L;
    static final long JOIN_GAP = 600000L;
    public static final String[] ICON_KEYS = new String[]{"paper", "emerald", "chest", "spawner", "wheat", "diamond", "gold_ingot", "clock", "book", "iron_pickaxe", "totem", "experience_bottle"};
    private static final Item[] ICON_ITEMS = new Item[]{Items.PAPER, Items.EMERALD, Items.CHEST, Items.SPAWNER, Items.WHEAT, Items.DIAMOND, Items.GOLD_INGOT, Items.CLOCK, Items.BOOK, Items.IRON_PICKAXE, Items.TOTEM_OF_UNDYING, Items.EXPERIENCE_BOTTLE};
    private static final ItemStack[] ICON_STACKS = new ItemStack[ICON_ITEMS.length];
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Data data = new Data();
    private static boolean loaded;
    public static long fakeNow;
    private static long lastSecond;
    private static long joinAt;
    private static long lastJoinNote;
    private static long dayStart;
    private static long dayEnd;
    private static final int HUD_MAX = 12;
    private static final String[] hudName;
    private static final String[] hudInfo;
    private static final boolean[] hudOpen;
    private static int hudLines;
    private static String hudHead;
    private static int hudDone;
    private static int hudTotal;
    private static boolean hudVisible;
    private static final int W = 168;
    private static final int PAD = 7;
    private static final int ROW = 10;
    private static final int GLASS_TOP = -1005310936;
    private static final int GLASS_BOTTOM = -804645356;
    private static final int RIM = 0x30FFFFFF;
    private static final int TRACK = 0x1AFFFFFF;
    private static final int TEXT = -1116943;
    private static final int SUB = -6902107;
    private static final int DIM = -2136556108;
    private static final int DONE_NAME = -1869173083;
    private static final int MINT = -8523578;
    private static final int GOLD = -670398;

    private Todos() {
    }

    public static void register() {
        Todos.load();
        ClientTickEvents.END_CLIENT_TICK.register(Todos::tick);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            joinAt = System.currentTimeMillis();
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)ClientCommands.literal((String)"fwmtodo").then(ClientCommands.literal((String)"done").then(ClientCommands.argument((String)"id", (ArgumentType)StringArgumentType.word()).executes(c -> {
            Todos.button(StringArgumentType.getString((CommandContext)c, (String)"id"), false);
            return 1;
        })))).then(ClientCommands.literal((String)"later").then(ClientCommands.argument((String)"id", (ArgumentType)StringArgumentType.word()).executes(c -> {
            Todos.button(StringArgumentType.getString((CommandContext)c, (String)"id"), true);
            return 1;
        })))));
    }

    public static Settings settings() {
        if (!loaded) {
            Todos.load();
        }
        return Todos.data.settings;
    }

    public static List<Todo> all() {
        if (!loaded) {
            Todos.load();
        }
        return Todos.data.todos;
    }

    public static long now() {
        return fakeNow != 0L ? fakeNow : System.currentTimeMillis();
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("fwm").resolve("todos.json");
    }

    public static void load() {
        loaded = true;
        try {
            Path f = Todos.file();
            if (!Files.exists(f, new LinkOption[0])) {
                return;
            }
            Data read = (Data)GSON.fromJson(Files.readString(f, StandardCharsets.UTF_8), Data.class);
            if (read == null) {
                return;
            }
            if (read.settings == null) {
                read.settings = new Settings();
            }
            if (read.todos == null) {
                read.todos = new ArrayList<Todo>();
            }
            read.todos.removeIf(t -> t == null);
            for (Todo t2 : read.todos) {
                Todos.sanitize(t2);
            }
            data = read;
        }
        catch (Exception e) {
            System.err.println("[FWM] todos.json could not be read: " + String.valueOf(e));
        }
    }

    public static void save() {
        try {
            Path f = Todos.file();
            Files.createDirectories(f.getParent(), new FileAttribute[0]);
            Path tmp = f.resolveSibling("todos.json.tmp");
            Files.writeString(tmp, (CharSequence)GSON.toJson((Object)data), StandardCharsets.UTF_8, new OpenOption[0]);
            Files.move(tmp, f, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (Exception e) {
            System.err.println("[FWM] todos.json could not be saved: " + String.valueOf(e));
        }
    }

    public static void sanitize(Todo t) {
        if (t.id == null || t.id.isBlank()) {
            t.id = Todos.newId();
        }
        if (t.name == null) {
            t.name = "";
        }
        if (t.name.length() > 32) {
            t.name = t.name.substring(0, 32);
        }
        if (!ONCE.equals(t.type) && !EVERY.equals(t.type)) {
            t.type = DAILY;
        }
        t.hours = Math.max(1, Math.min(168, t.hours));
        t.resetTime = Math.floorMod(t.resetTime, 1440);
        if (t.remindAt >= 1440) {
            t.remindAt = Math.floorMod(t.remindAt, 1440);
        }
        if (t.remindAt < -1) {
            t.remindAt = -1;
        }
        if (!SCOPE_HERE.equals(t.server)) {
            t.server = SCOPE_ALL;
        }
        if (t.serverAddr == null) {
            t.serverAddr = "";
        }
        if (t.icon == null || Todos.iconIndex(t.icon) < 0) {
            t.icon = "paper";
        }
        if (t.streak < 0) {
            t.streak = 0;
        }
    }

    private static String newId() {
        return Long.toString(System.currentTimeMillis(), 36) + Integer.toString((int)(Math.random() * 1296.0), 36);
    }

    public static Todo create(String name, String type, int hours) {
        Todo t = new Todo();
        t.id = Todos.newId();
        t.name = name;
        t.type = type;
        t.hours = hours;
        t.created = Todos.now();
        Todos.sanitize(t);
        Todos.all().add(t);
        Todos.save();
        return t;
    }

    public static Todo template(int which) {
        Todo t;
        switch (which) {
            case 0: {
                t = Todos.create(Lang.t("T\u00e4glich voten", "Vote daily"), DAILY, 24);
                t.icon = "emerald";
                break;
            }
            case 1: {
                t = Todos.create(Lang.t("Kit abholen", "Claim kit"), EVERY, 24);
                t.icon = "chest";
                break;
            }
            case 2: {
                t = Todos.create(Lang.t("Farm leeren", "Empty the farm"), EVERY, 6);
                t.icon = "wheat";
                break;
            }
            default: {
                t = Todos.create(Lang.t("Spawner verkaufen", "Sell spawners"), DAILY, 24);
                t.icon = "spawner";
                t.remindAt = 1080;
            }
        }
        Todos.save();
        return t;
    }

    public static void delete(Todo t) {
        Todos.all().remove(t);
        Todos.save();
    }

    public static void move(Todo t, int toIndex) {
        List<Todo> list = Todos.all();
        int from = list.indexOf(t);
        if (from < 0) {
            return;
        }
        list.remove(from);
        list.add(Math.max(0, Math.min(list.size(), toIndex)), t);
        Todos.save();
    }

    public static void markDone(Todo t, boolean fx) {
        long now = Todos.now();
        if (!Todos.isOpen(t, now)) {
            return;
        }
        t.undoDone = t.lastDone;
        t.undoStreak = t.streak;
        if (DAILY.equals(t.type)) {
            long reset = Todos.lastReset(t, now);
            t.streak = t.lastDone >= reset - 86400000L ? t.streak + 1 : 1;
        }
        t.lastDone = now;
        t.snoozeUntil = 0L;
        if (fx) {
            t.fxAt = System.currentTimeMillis();
        }
        Todos.save();
    }

    public static void undo(Todo t) {
        if (t.undoDone >= 0L) {
            t.lastDone = t.undoDone;
            t.streak = t.undoStreak;
        } else {
            t.lastDone = 0L;
            if (DAILY.equals(t.type)) {
                t.streak = Math.max(0, t.streak - 1);
            }
        }
        t.undoDone = -1L;
        t.fxAt = 0L;
        Todos.save();
    }

    public static void snooze(Todo t) {
        t.snoozeUntil = Todos.now() + 3600000L;
        Todos.save();
    }

    public static void setScope(Todo t, String scope) {
        t.server = scope;
        if (SCOPE_HERE.equals(scope)) {
            t.serverAddr = Todos.currentAddress();
        }
        Todos.save();
    }

    private static void refreshDay(long now) {
        if (now >= dayStart && now < dayEnd) {
            return;
        }
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate();
        dayStart = today.atStartOfDay(zone).toInstant().toEpochMilli();
        dayEnd = today.plusDays(1L).atStartOfDay(zone).toInstant().toEpochMilli();
    }

    static long todayAt(int minute, long now) {
        Todos.refreshDay(now);
        return dayStart + (long)minute * 60000L;
    }

    public static long lastReset(Todo t, long now) {
        long at = Todos.todayAt(t.resetTime, now);
        return at > now ? at - 86400000L : at;
    }

    public static boolean isOpen(Todo t, long now) {
        return switch (t.type) {
            case ONCE -> {
                if (t.lastDone == 0L) {
                    yield true;
                }
                yield false;
            }
            case EVERY -> {
                if (t.lastDone == 0L || now >= t.lastDone + (long)t.hours * 3600000L) {
                    yield true;
                }
                yield false;
            }
            default -> t.lastDone < Todos.lastReset(t, now);
        };
    }

    static long openSince(Todo t, long now) {
        long since = switch (t.type) {
            case ONCE -> t.created;
            case EVERY -> {
                if (t.lastDone == 0L) {
                    yield t.created;
                }
                yield t.lastDone + (long)t.hours * 3600000L;
            }
            default -> Todos.lastReset(t, now);
        };
        return Math.max(since, t.created);
    }

    public static long reopensAt(Todo t, long now) {
        return switch (t.type) {
            case ONCE -> -1L;
            case EVERY -> t.lastDone + (long)t.hours * 3600000L;
            default -> Todos.lastReset(t, now) + 86400000L;
        };
    }

    public static int streakNow(Todo t, long now) {
        if (!DAILY.equals(t.type) || t.streak <= 0) {
            return 0;
        }
        return t.lastDone >= Todos.lastReset(t, now) - 86400000L ? t.streak : 0;
    }

    public static String duration(long ms) {
        long min = Math.max(0L, (ms + 59999L) / 60000L);
        long h = min / 60L;
        long m = min % 60L;
        if (h >= 48L) {
            return h / 24L + Lang.t(" T ", " d ") + h % 24L + " h";
        }
        if (h > 0L) {
            return h + " h " + m + " min";
        }
        return m + " min";
    }

    public static String hhmm(int minute) {
        return String.format(Locale.ROOT, "%02d:%02d", minute / 60, minute % 60);
    }

    public static String kind(Todo t) {
        return switch (t.type) {
            case ONCE -> Lang.t("einmal", "once");
            case EVERY -> Lang.t("alle ", "every ") + t.hours + " h";
            default -> t.resetTime == 0 ? Lang.t("t\u00e4glich", "daily") : Lang.t("t\u00e4glich ", "daily ") + Todos.hhmm(t.resetTime);
        };
    }

    static String currentAddress() {
        Minecraft client = Minecraft.getInstance();
        ServerData entry = client == null ? null : client.getCurrentServer();
        return entry == null || entry.ip == null ? "singleplayer" : entry.ip;
    }

    public static boolean appliesHere(Todo t) {
        if (SCOPE_HERE.equals(t.server)) {
            Minecraft client = Minecraft.getInstance();
            ServerData entry = client.getCurrentServer();
            String here = entry == null || entry.ip == null ? "singleplayer" : entry.ip;
            return here.equalsIgnoreCase(t.serverAddr);
        }
        return true;
    }

    public static ItemStack icon(Todo t) {
        int i = Todos.iconIndex(t.icon);
        return Todos.iconStack(i < 0 ? 0 : i);
    }

    public static ItemStack iconStack(int i) {
        if (ICON_STACKS[i] == null) {
            Todos.ICON_STACKS[i] = new ItemStack((ItemLike)ICON_ITEMS[i]);
        }
        return ICON_STACKS[i];
    }

    public static int iconIndex(String key) {
        for (int i = 0; i < ICON_KEYS.length; ++i) {
            if (!ICON_KEYS[i].equals(key)) continue;
            return i;
        }
        return -1;
    }

    private static boolean active(Minecraft client) {
        return client.player != null && Features.isOn("fwm.tab.todos");
    }

    private static void tick(Minecraft client) {
        long real = System.currentTimeMillis();
        if (real - lastSecond < 1000L) {
            return;
        }
        lastSecond = real;
        if (!loaded) {
            Todos.load();
        }
        if (!Todos.active(client)) {
            hudVisible = false;
            return;
        }
        Todos.check(Todos.now());
    }

    public static void check(long now) {
        boolean joining;
        List<Todo> list = Todos.data.todos;
        Settings s = Todos.data.settings;
        boolean bl = joining = joinAt > 0L && System.currentTimeMillis() - joinAt < 5000L;
        if (joinAt > 0L && !joining) {
            int open;
            joinAt = 0L;
            if (s.joinNote && System.currentTimeMillis() - lastJoinNote >= 600000L && (open = Todos.openHere(now)) > 0) {
                lastJoinNote = System.currentTimeMillis();
                Todos.joinNote(open);
            }
        }
        if (s.reminders && !joining) {
            boolean changed = false;
            for (int i = 0; i < list.size(); ++i) {
                Todo t = list.get(i);
                if (!Todos.appliesHere(t) || !Todos.isOpen(t, now) || !Todos.dueReminder(t, now)) continue;
                t.lastRemind = now;
                t.snoozeUntil = 0L;
                Todos.remind(t, now);
                changed = true;
            }
            if (changed) {
                Todos.save();
            }
        }
        if (s.hud) {
            Todos.buildHud(now);
        } else {
            hudVisible = false;
        }
    }

    static boolean dueReminder(Todo t, long now) {
        if (now - t.lastRemind < 3600000L) {
            return false;
        }
        if (t.snoozeUntil > 0L) {
            return now >= t.snoozeUntil;
        }
        long since = Todos.openSince(t, now);
        if (t.remindWhenOpen && !ONCE.equals(t.type) && t.lastDone > 0L && t.lastRemind < since) {
            return true;
        }
        if (t.remindAt >= 0) {
            long at = Todos.todayAt(t.remindAt, now);
            if (at > now) {
                at -= 86400000L;
            }
            return at >= since && t.lastRemind < at;
        }
        return false;
    }

    public static int openHere(long now) {
        int n = 0;
        List<Todo> list = Todos.all();
        for (int i = 0; i < list.size(); ++i) {
            Todo t = list.get(i);
            if (!Todos.appliesHere(t) || !Todos.isOpen(t, now)) continue;
            ++n;
        }
        return n;
    }

    private static void remind(Todo t, long now) {
        Minecraft client = Minecraft.getInstance();
        int streak = Todos.streakNow(t, now);
        Object line = streak > 0 ? Lang.t(streak + " Tage am St\u00fcck \u2013 nicht abrei\u00dfen lassen!", streak + " days in a row \u2013 keep it going!") : Lang.t("Noch offen \u00b7 ", "Still open \u00b7 ") + Todos.kind(t);
        FwmNotice.show(Lang.t("FWM \u00b7 To-Do", "FWM \u00b7 To-do"), t.name, (String)line, FwmTheme.ACCENT & 0xFFFFFF, 6000L);
        Todos.playSound(client, 1.2f);
        if (Todos.data.settings.chatLine && client.gui != null) {
            MutableComponent msg = Component.literal((String)"[FWM] ").withStyle(ChatFormatting.GOLD).append((Component)Component.literal((String)Lang.t("To-Do offen: ", "To-do open: ")).withStyle(ChatFormatting.GRAY)).append((Component)Component.literal((String)t.name).withStyle(new ChatFormatting[]{ChatFormatting.WHITE, ChatFormatting.BOLD}));
            if (streak > 0) {
                msg.append((Component)Component.literal((String)(" (" + streak + Lang.t(" Tage", " days") + ")")).withStyle(ChatFormatting.GOLD));
            }
            msg.append(Todos.buttons(t));
            client.gui.hud.getChat().addClientSystemMessage((Component)msg);
        }
    }

    private static Component buttons(Todo t) {
        String done = "/fwmtodo done " + t.id;
        String later = "/fwmtodo later " + t.id;
        return Component.literal((String)" ").append((Component)Component.literal((String)("[" + Lang.t("Erledigt", "Done") + "]")).withStyle(new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}).withStyle(st -> st.withClickEvent((ClickEvent)new ClickEvent.RunCommand(done)).withHoverEvent((HoverEvent)new HoverEvent.ShowText((Component)Component.literal((String)Lang.t("Als erledigt abhaken", "Tick it off")))))).append((Component)Component.literal((String)" ")).append((Component)Component.literal((String)("[" + Lang.t("Sp\u00e4ter +1 h", "Later +1 h") + "]")).withStyle(ChatFormatting.AQUA).withStyle(st -> st.withClickEvent((ClickEvent)new ClickEvent.RunCommand(later)).withHoverEvent((HoverEvent)new HoverEvent.ShowText((Component)Component.literal((String)Lang.t("In einer Stunde nochmal erinnern", "Remind me again in an hour"))))));
    }

    private static void joinNote(int open) {
        Minecraft client = Minecraft.getInstance();
        String text = open == 1 ? Lang.t("1 To-Do offen", "1 to-do open") : Lang.t(open + " To-Dos offen", open + " to-dos open");
        StringBuilder names = new StringBuilder();
        long now = Todos.now();
        List<Todo> list = Todos.all();
        int shown = 0;
        for (int i = 0; i < list.size() && shown < 3; ++i) {
            Todo t = list.get(i);
            if (!Todos.appliesHere(t) || !Todos.isOpen(t, now)) continue;
            if (shown++ > 0) {
                names.append(", ");
            }
            names.append(t.name);
        }
        if (open > 3) {
            names.append(", \u2026");
        }
        FwmNotice.show("FWM", text, names.toString(), FwmTheme.ACCENT & 0xFFFFFF, 5000L);
        if (client.gui != null) {
            client.gui.hud.getChat().addClientSystemMessage((Component)Component.literal((String)"[FWM] ").withStyle(ChatFormatting.GOLD).append((Component)Component.literal((String)text).withStyle(ChatFormatting.GREEN)).append((Component)Component.literal((String)(": " + String.valueOf(names))).withStyle(ChatFormatting.GRAY)));
        }
        Todos.playSound(client, 1.0f);
    }

    static void playSound(Minecraft client, float pitch) {
        float vol = (float)Todos.data.settings.volume / 100.0f;
        if (vol <= 0.0f || client.getSoundManager() == null) {
            return;
        }
        client.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)((SoundEvent)SoundEvents.NOTE_BLOCK_CHIME.value()), (float)pitch, (float)(vol * 0.7f)));
    }

    public static void tickSound() {
        Minecraft client = Minecraft.getInstance();
        float vol = (float)Todos.data.settings.volume / 100.0f;
        if (vol <= 0.0f) {
            return;
        }
        client.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)((SoundEvent)SoundEvents.NOTE_BLOCK_BELL.value()), (float)1.6f, (float)(vol * 0.5f)));
    }

    static void button(String id, boolean later) {
        String msg;
        Minecraft client = Minecraft.getInstance();
        Todo t = Todos.byId(id);
        if (t == null) {
            return;
        }
        if (later) {
            Todos.snooze(t);
            msg = Lang.t("Erinnere dich in 1 h nochmal an ", "Reminding you again in 1 h: ") + t.name;
        } else if (Todos.isOpen(t, Todos.now())) {
            Todos.markDone(t, true);
            int streak = Todos.streakNow(t, Todos.now());
            msg = "\u2714 " + t.name + Lang.t(" erledigt", " done") + (String)(streak > 1 ? " (" + streak + Lang.t(" Tage am St\u00fcck)", " days in a row)") : "");
            Todos.tickSound();
        } else {
            msg = t.name + Lang.t(" ist schon erledigt", " is already done");
        }
        if (client.gui != null) {
            client.gui.hud.getChat().addClientSystemMessage((Component)Component.literal((String)"[FWM] ").withStyle(ChatFormatting.GOLD).append((Component)Component.literal((String)msg).withStyle(ChatFormatting.GREEN)));
        }
    }

    public static Todo byId(String id) {
        List<Todo> list = Todos.all();
        for (int i = 0; i < list.size(); ++i) {
            if (!list.get((int)i).id.equals(id)) continue;
            return list.get(i);
        }
        return null;
    }

    static boolean spokenByPlayer(String line) {
        int colon = line.indexOf(58);
        if (colon > 0 && Payments.isName(Todos.lastPlainWord(line.substring(0, colon)))) {
            return true;
        }
        int open = line.indexOf(60);
        int close = line.indexOf(62);
        return open >= 0 && close > open + 1 && Payments.isName(line.substring(open + 1, close).trim());
    }

    private static String lastPlainWord(String left) {
        StringBuilder plain = new StringBuilder();
        int depth = 0;
        for (int i = 0; i < left.length(); ++i) {
            char c = left.charAt(i);
            if (c == '[' || c == '(' || c == '<') {
                ++depth;
                continue;
            }
            if (c == ']' || c == ')' || c == '>') {
                if (depth <= 0) continue;
                --depth;
                continue;
            }
            if (depth != 0) continue;
            plain.append(c);
        }
        String[] words = plain.toString().trim().split(" ");
        return words.length == 0 ? "" : words[words.length - 1].trim();
    }

    private static void buildHud(long now) {
        Settings s = Todos.data.settings;
        List<Todo> list = Todos.data.todos;
        int total = 0;
        int open = 0;
        int lines = 0;
        int max = Math.max(1, Math.min(12, s.hudMaxLines));
        for (int pass = 0; pass < 2; ++pass) {
            for (int i = 0; i < list.size(); ++i) {
                Todo t = list.get(i);
                if (!Todos.appliesHere(t)) continue;
                boolean isOpen = Todos.isOpen(t, now);
                if (pass == 0) {
                    ++total;
                    if (isOpen) {
                        ++open;
                    }
                }
                if (isOpen != (pass == 0) || !isOpen && s.hudOnlyOpen || !isOpen && ONCE.equals(t.type) || lines >= max) continue;
                Todos.hudName[lines] = t.name;
                Todos.hudOpen[lines] = isOpen;
                Todos.hudInfo[lines] = isOpen ? (EVERY.equals(t.type) && t.lastDone > 0L ? Lang.t("wieder da!", "back!") : Todos.kind(t)) : Lang.t("in ", "in ") + Todos.shortDuration(Todos.reopensAt(t, now) - now);
                ++lines;
            }
        }
        hudLines = lines;
        hudHead = total - open + "/" + total;
        hudDone = total - open;
        hudTotal = total;
        hudVisible = total > 0 && (!s.hudHideWhenDone || open != 0) && lines > 0;
    }

    static String shortDuration(long ms) {
        long min = Math.max(0L, (ms + 59999L) / 60000L);
        if (min >= 60L) {
            return min / 60L + " h " + String.format(Locale.ROOT, "%02d", min % 60L);
        }
        return min + " min";
    }

    public static void drawHud(GuiGraphicsExtractor ctx, Minecraft client) {
        if (!(hudVisible && client.player != null && Features.isOn("fwm.tab.todos") && Todos.data.settings.hud)) {
            return;
        }
        int rows = hudLines;
        float barY = 21.0f;
        float listTop = barY + 4.0f;
        int h = Math.round(listTop + (float)(rows * 10) + 4.0f);
        float scale = FwmHud.scaleOf("todos");
        int[] at = FwmHud.placeCard(ctx, "todos", Math.round(168.0f * scale), Math.round((float)h * scale), 6, 60);
        Matrix3x2fStack m = ctx.pose();
        m.pushMatrix();
        m.translate((float)at[0], (float)at[1]);
        m.scale(scale, scale);
        Shapes.roundGradient(ctx, 0.0f, 0.0f, 168.0f, h, 9.0f, -1005310936, -804645356);
        Shapes.ring(ctx, 0.0f, 0.0f, 168.0f, h, 9.0f, 0x30FFFFFF);
        boolean allDone = hudTotal > 0 && hudDone >= hudTotal;
        int accent = allDone ? -670398 : -8523578;
        Shapes.ring(ctx, 7.0f, 7.0f, 10.0f, 10.0f, 3.0f, accent);
        Shapes.icon(ctx, "check", 8.0f, 8.0f, 8.0f, accent);
        LauncherGfx.text(ctx, "To-Dos", 21.0f, 7.5f, -1116943, 8.0f, true);
        float pw = LauncherGfx.width(hudHead, 6.0f, true) + 10.0f;
        Shapes.round(ctx, 161.0f - pw, 6.5f, pw, 11.0f, 5.5f, 0x16FFFFFF);
        Shapes.ring(ctx, 161.0f - pw, 6.5f, pw, 11.0f, 5.5f, 0x22FFFFFF);
        LauncherGfx.text(ctx, hudHead, 161.0f - pw + 5.0f, 8.5f, accent, 6.0f, true);
        float barW = 154.0f;
        Shapes.round(ctx, 7.0f, barY, barW, 2.0f, 1.0f, 0x1AFFFFFF);
        if (hudDone > 0 && hudTotal > 0) {
            float part = Math.min(1.0f, (float)hudDone / (float)hudTotal);
            Shapes.round(ctx, 7.0f, barY, Math.max(2.0f, barW * part), 2.0f, 1.0f, accent);
        }
        float nameX = 20.0f;
        for (int i = 0; i < rows; ++i) {
            boolean open = hudOpen[i];
            String info = hudInfo[i];
            boolean back = open && info.endsWith("!");
            float cy = listTop + (float)(i * 10) + 5.0f;
            if (open) {
                Shapes.ring(ctx, 7.0f, cy - 4.0f, 8.0f, 8.0f, 4.0f, back ? -1057635006 : 0x70FFFFFF);
            } else {
                Shapes.circle(ctx, 11.0f, cy, 4.0f, -1602359098);
                Shapes.icon(ctx, "check", 8.0f, cy - 3.0f, 6.0f, -16045536);
            }
            float iw = LauncherGfx.width(info, 6.5f, back);
            String name = LauncherGfx.fit(hudName[i], 7.0f, false, 161.0f - iw - 8.0f - nameX);
            LauncherGfx.text(ctx, name, nameX, cy - 3.5f, open ? -1116943 : -1869173083, 7.0f, false);
            LauncherGfx.text(ctx, info, 161.0f - iw, cy - 3.5f, !open ? -2136556108 : (back ? -670398 : -6902107), 6.5f, back);
        }
        m.popMatrix();
    }

    static {
        dayStart = Long.MIN_VALUE;
        dayEnd = Long.MIN_VALUE;
        hudName = new String[12];
        hudInfo = new String[12];
        hudOpen = new boolean[12];
        hudHead = "";
    }

    private static final class Data {
        Settings settings = new Settings();
        List<Todo> todos = new ArrayList<Todo>();

        private Data() {
        }
    }

    public static final class Settings {
        public boolean hud = true;
        public boolean hudOnlyOpen = false;
        public int hudMaxLines = 5;
        public boolean hudHideWhenDone = true;
        public boolean reminders = true;
        public boolean joinNote = true;
        public int volume = 40;
        public boolean chatLine = true;
    }

    public static final class Todo {
        public String id;
        public String name = "";
        public String icon = "paper";
        public String type = "DAILY";
        public int hours = 24;
        public int resetTime = 0;
        public int remindAt = -1;
        public boolean remindWhenOpen = true;
        public String server = "ALL";
        public String serverAddr = "";
        public long lastDone;
        public int streak;
        public long snoozeUntil;
        public long lastRemind;
        public long created;
        transient long undoDone = -1L;
        transient int undoStreak;
        public transient long fxAt;
    }
}

