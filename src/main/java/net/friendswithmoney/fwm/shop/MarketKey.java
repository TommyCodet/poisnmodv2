/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.alchemy.PotionContents
 *  net.minecraft.world.item.component.ItemLore
 *  net.minecraft.world.item.enchantment.ItemEnchantments
 */
package net.friendswithmoney.fwm.shop;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import net.friendswithmoney.fwm.net.MarketPrices;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class MarketKey {
    private static final Map<String, String[]> KNOWN = Map.of("soul_lantern#mara", new String[]{"soul_lantern", "Original: Soul Lantern", "Nacht der Mara"});

    private MarketKey() {
    }

    public static String of(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        String event = MarketKey.eventItem(stack);
        if (event != null) {
            return event;
        }
        String custom = MarketKey.serverItem(stack);
        if (custom != null) {
            return custom;
        }
        Identifier id = BuiltInRegistries.ITEM.getKey((Object)stack.getItem());
        String path = id.getPath();
        if (path.equals("enchanted_book")) {
            return MarketKey.book(stack);
        }
        if (path.equals("potion") || path.equals("splash_potion") || path.equals("tipped_arrow")) {
            String potion = MarketKey.potion(stack);
            return potion == null ? null : path + "#" + potion;
        }
        return path;
    }

    static String eventItem(ItemStack stack) {
        ItemLore lore = (ItemLore)stack.get(DataComponents.LORE);
        if (lore == null || lore.lines().isEmpty()) {
            return null;
        }
        StringBuilder all = new StringBuilder();
        boolean original = false;
        boolean edition = false;
        for (Component line : lore.lines()) {
            String plain = line.getString().replaceAll("\u00a7.", "").trim();
            all.append(plain).append(' ');
            if (plain.startsWith("Original:")) {
                original = true;
            }
            if (!plain.matches("(?i)^(Nr\\.|No\\.|#)\\s*\\d+.*")) continue;
            edition = true;
        }
        String path = BuiltInRegistries.ITEM.getKey((Object)stack.getItem()).getPath();
        String text = all.toString();
        for (Map.Entry entry : KNOWN.entrySet()) {
            String[] need = (String[])entry.getValue();
            if (!need[0].equals(path)) continue;
            boolean ok = true;
            for (int i = 1; i < need.length; ++i) {
                if (text.contains(need[i])) continue;
                ok = false;
                break;
            }
            if (!ok) continue;
            return (String)entry.getKey();
        }
        if (!original || !edition) {
            return null;
        }
        String name = stack.getHoverName().getString().replaceAll("\u00a7.", "").toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\u00e4\u00f6\u00fc\u00df]+", "_").replaceAll("^_|_$", "");
        return path + "#event_" + name;
    }

    private static String serverItem(ItemStack stack) {
        Component item;
        ArrayList<String> names = new ArrayList<String>();
        names.add(stack.getHoverName().getString());
        Component custom = (Component)stack.get(DataComponents.CUSTOM_NAME);
        if (custom != null) {
            names.add(custom.getString());
        }
        if ((item = (Component)stack.get(DataComponents.ITEM_NAME)) != null) {
            names.add(item.getString());
        }
        for (String name : names) {
            String plain = name.replaceAll("\u00a7.", "").trim();
            for (String candidate : new String[]{plain, plain.replaceFirst("^\\d+\\s*[xX\u00d7]\\s*", ""), plain.contains(":") ? plain.substring(plain.lastIndexOf(58) + 1) : null}) {
                String key;
                if (candidate == null || candidate.isBlank() || (key = MarketPrices.keyForName(candidate)) == null) continue;
                return key;
            }
        }
        return null;
    }

    private static String book(ItemStack stack) {
        ItemEnchantments stored = (ItemEnchantments)stack.get(DataComponents.STORED_ENCHANTMENTS);
        if (stored == null || stored.size() != 1) {
            return null;
        }
        Iterator iterator = stored.keySet().iterator();
        if (iterator.hasNext()) {
            Holder entry = (Holder)iterator.next();
            String name = entry.unwrapKey().map(key -> key.identifier().getPath()).orElse(null);
            if (name == null) {
                return null;
            }
            return "enchanted_book#" + name + "_" + stored.getLevel(entry);
        }
        return null;
    }

    private static String potion(ItemStack stack) {
        PotionContents contents = (PotionContents)stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null || contents.potion().isEmpty()) {
            return null;
        }
        Holder potion = (Holder)contents.potion().get();
        String name = potion.unwrapKey().map(key -> key.identifier().getPath()).orElse(null);
        if (name == null) {
            return null;
        }
        return name.replaceFirst("^(long|strong)_", "");
    }
}

