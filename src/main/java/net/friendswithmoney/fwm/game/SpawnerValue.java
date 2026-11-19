/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.ItemLore
 */
package net.friendswithmoney.fwm.game;

import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.friendswithmoney.fwm.auction.Money;
import net.friendswithmoney.fwm.net.SpawnerData;
import net.friendswithmoney.fwm.net.Spawners;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

public final class SpawnerValue {
    private static final String SMALL = "\u1d00\u0299\u1d04\u1d05\u1d07\ua730\u0262\u029c\u026a\u1d0a\u1d0b\u029f\u1d0d\u0274\u1d0f\u1d18\u01eb\u0280\ua731\u1d1b\u1d1c\u1d20\u1d21\u028f\u1d22";
    private static final String PLAIN = "abcdefghijklmnopqrstuvwyz";
    private static final Pattern AMOUNT = Pattern.compile("(?:anzahl|amount|menge)\\s*:?\\s*x?\\s*(\\d{1,5})");

    private SpawnerValue() {
    }

    public static long cents(ItemStack stack) {
        String kind = SpawnerValue.kind(stack);
        if (kind == null) {
            return -1L;
        }
        SpawnerData.Item item = SpawnerValue.item(kind);
        if (item == null || item.buyPrice == null) {
            return -1L;
        }
        long cents = Money.cents(item.buyPrice);
        return cents > 0L ? cents * (long)SpawnerValue.perItem(stack) : -1L;
    }

    static String plain(String text) {
        String s = text.replaceAll("\u00a7.", "").toLowerCase(Locale.ROOT);
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            int at = SMALL.indexOf(c);
            out.append(at >= 0 ? PLAIN.charAt(at) : c);
        }
        return out.toString();
    }

    public static int perItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 1;
        }
        ItemLore lore = (ItemLore)stack.get(DataComponents.LORE);
        if (lore == null) {
            return 1;
        }
        for (Component line : lore.lines()) {
            Matcher m = AMOUNT.matcher(SpawnerValue.plain(line.getString()));
            if (!m.find()) continue;
            return Math.max(1, Integer.parseInt(m.group(1)));
        }
        return 1;
    }

    public static SpawnerData.Item itemFor(ItemStack stack) {
        String kind = SpawnerValue.kind(stack);
        return kind == null ? null : SpawnerValue.item(kind);
    }

    static String kind(ItemStack stack) {
        String kind;
        block3: {
            Component line;
            boolean spawner;
            if (stack == null || stack.isEmpty()) {
                return null;
            }
            String text = stack.getHoverName().getString();
            boolean bl = spawner = "spawner".equals(BuiltInRegistries.ITEM.getKey((Object)stack.getItem()).getPath()) || SpawnerValue.plain(text).contains("spawner");
            if (!spawner) {
                return null;
            }
            ItemLore lore = (ItemLore)stack.get(DataComponents.LORE);
            kind = SpawnerValue.canon(text);
            if (kind != null || lore == null) break block3;
            Iterator iterator = lore.lines().iterator();
            while (iterator.hasNext() && (kind = SpawnerValue.canon((line = (Component)iterator.next()).getString())) == null) {
            }
        }
        return kind;
    }

    static String canon(String name) {
        if (name == null) {
            return null;
        }
        String s = SpawnerValue.plain(name);
        if (s.contains("wither")) {
            return null;
        }
        if (s.contains("h\u00f6hlenspinne") || s.contains("cave")) {
            return null;
        }
        if (s.contains("zombie") && s.contains("piglin") || s.contains("piglin")) {
            return "piglin";
        }
        if (s.contains("skel")) {
            return "skeleton";
        }
        if (s.contains("creeper")) {
            return "creeper";
        }
        if (s.contains("iron") || s.contains("eisen")) {
            return "iron_golem";
        }
        if (s.contains("blaze") || s.contains("lohe")) {
            return "blaze";
        }
        if (s.contains("spider") || s.contains("spinne")) {
            return "spider";
        }
        if (s.contains("cow") || s.contains("kuh")) {
            return "cow";
        }
        return null;
    }

    private static SpawnerData.Item item(String kind) {
        SpawnerData data = Spawners.data();
        if (data == null || data.items == null) {
            return null;
        }
        for (SpawnerData.Item item : data.items) {
            if (!kind.equals(SpawnerValue.canon(item.name))) continue;
            return item;
        }
        return null;
    }
}

