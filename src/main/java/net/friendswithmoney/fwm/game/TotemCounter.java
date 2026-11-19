/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  org.joml.Matrix3x2fStack
 */
package net.friendswithmoney.fwm.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.net.ServerContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.joml.Matrix3x2fStack;

public final class TotemCounter {
    private static final Identifier ID = Identifier.fromNamespaceAndPath((String)"fwm", (String)"totem_counter");
    private static final Map<UUID, Integer> POPS = new HashMap<UUID, Integer>();
    private static final Map<UUID, String> NAMES = new HashMap<UUID, String>();
    private static ClientPacketListener connection;
    private static long stockAt;
    private static Object stockOf;
    private static int stockCached;

    private TotemCounter() {
    }

    public static void register() {
    }

    public static void onStatus(Entity entity, byte status) {
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player)entity;
        UUID id = player.getUUID();
        if (status == 35) {
            POPS.merge(id, 1, Integer::sum);
            NAMES.put(id, player.getGameProfile().name());
        } else if (status == 3) {
            POPS.remove(id);
        }
    }

    public static void reset() {
        POPS.clear();
        NAMES.clear();
    }

    private static boolean othersShown() {
        return Features.isOn("fwm.tab.totem") && ServerContext.current() != ServerContext.Server.DONUT;
    }

    public static int pops(UUID id) {
        Integer n = POPS.get(id);
        return n == null ? 0 : n;
    }

    public static List<Map.Entry<String, Integer>> top(int limit) {
        ArrayList<Map.Entry<String, Integer>> out = new ArrayList<Map.Entry<String, Integer>>();
        for (Map.Entry<UUID, Integer> e : POPS.entrySet()) {
            out.add(Map.entry(NAMES.getOrDefault(e.getKey(), "?"), e.getValue()));
        }
        out.sort((a, b) -> (Integer)b.getValue() - (Integer)a.getValue());
        return out.size() > limit ? out.subList(0, limit) : out;
    }

    public static Component decorate(UUID id, Component name, boolean tab) {
        if (name == null || id == null || !TotemCounter.othersShown()) {
            return name;
        }
        if (tab ? !V2.INSTANCE.totemTabList : !V2.INSTANCE.totemNameTag) {
            return name;
        }
        Minecraft client = Minecraft.getInstance();
        if (!V2.INSTANCE.totemOwnPops && client.player != null && id.equals(client.player.getUUID())) {
            return name;
        }
        int n = TotemCounter.pops(id);
        if (n <= 0) {
            return name;
        }
        ChatFormatting color = n >= 5 ? ChatFormatting.RED : (n >= 2 ? ChatFormatting.GOLD : ChatFormatting.YELLOW);
        return name.copy().append((Component)Component.literal((String)(" \u2726" + n)).withStyle(color));
    }

    public static int stock() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return 0;
        }
        long now = System.currentTimeMillis();
        if (now - stockAt < 100L && stockOf == client.player) {
            return stockCached;
        }
        stockAt = now;
        stockOf = client.player;
        stockCached = TotemCounter.countStock(client);
        return stockCached;
    }

    private static int countStock(Minecraft client) {
        ItemStack off;
        int n = 0;
        Inventory inv = client.player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack s = inv.getItem(i);
            if (!s.is((Object)Items.TOTEM_OF_UNDYING)) continue;
            n += s.getCount();
        }
        if (!V2.INSTANCE.totemOffhand && (off = client.player.getOffhandItem()).is((Object)Items.TOTEM_OF_UNDYING)) {
            n -= off.getCount();
        }
        return Math.max(0, n);
    }

    public static void tick(Minecraft client) {
        if (client.getConnection() != connection) {
            connection = client.getConnection();
            TotemCounter.reset();
        }
    }

    private static void renderCard(GuiGraphicsExtractor ctx) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.gui.hud.isHidden() || !Features.isOn("fwm.tab.totem") || !V2.INSTANCE.totemCard) {
            return;
        }
        int n = TotemCounter.stock();
        int color = n == 0 ? -43691 : (n <= V2.INSTANCE.totemLow ? -12288 : -11141291);
        float scale = Math.max(0.5f, Math.min(3.0f, V2.INSTANCE.totemCardScale));
        int w = ctx.guiWidth();
        int h = ctx.guiHeight();
        float x = V2.INSTANCE.totemCardX < 0.0f ? 4.0f : V2.INSTANCE.totemCardX * (float)w;
        float y = V2.INSTANCE.totemCardY < 0.0f ? (float)h / 2.0f - 10.0f : V2.INSTANCE.totemCardY * (float)h;
        Matrix3x2fStack m = ctx.pose();
        m.pushMatrix();
        m.translate(x, y);
        m.scale(scale, scale);
        String text = String.valueOf(n);
        int cw = 22 + client.font.width(text);
        ctx.fill(0, 0, cw, 20, -2013265920);
        ctx.fill(0, 0, 1, 20, color);
        ctx.item(new ItemStack((ItemLike)Items.TOTEM_OF_UNDYING), 3, 2);
        ctx.text(client.font, (Component)Component.literal((String)text), 20, 6, color, true);
        m.popMatrix();
    }
}

