/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 */
package net.friendswithmoney.fwm.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.friendswithmoney.fwm.auction.Money;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.game.SpawnerValue;
import net.friendswithmoney.fwm.net.ServerContext;
import net.friendswithmoney.fwm.net.SpawnerData;
import net.friendswithmoney.fwm.net.Spawners;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.util.Fmt;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class SpawnerTooltip {
    private SpawnerTooltip() {
    }

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> SpawnerTooltip.append(stack, lines));
    }

    static boolean isSpawner(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return "spawner".equals(BuiltInRegistries.ITEM.getKey((Object)stack.getItem()).getPath()) || stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains("spawner");
    }

    private static void append(ItemStack stack, List<Component> lines) {
        long fwmSell;
        if (!V2.INSTANCE.spawnerTooltip || !ServerContext.hugo()) {
            return;
        }
        if (!SpawnerTooltip.isSpawner(stack) || lines == null) {
            return;
        }
        SpawnerData.Item item = SpawnerValue.itemFor(stack);
        int count = stack.getCount() * SpawnerValue.perItem(stack);
        int at = Math.min(lines.size(), 1);
        if (item == null) {
            boolean loading = Spawners.data() == null;
            lines.add(at, (Component)Component.literal((String)(loading ? Lang.t("FWM-Ankauf: l\u00e4dt \u2026", "FWM buy price: loading \u2026") : Lang.t("FWM-Ankauf: unbekannt", "FWM buy price: unknown"))).withStyle(ChatFormatting.GRAY));
            return;
        }
        ArrayList<Object> add = new ArrayList<Object>();
        long fwmBuy = item.buyPrice == null ? -1L : Money.cents(item.buyPrice);
        long l = fwmSell = item.sellPrice == null ? -1L : Money.cents(item.sellPrice);
        if (fwmBuy > 0L) {
            add.add(SpawnerTooltip.line(Lang.t("FWM-Ankauf: ", "FWM buys: "), fwmBuy, count, ChatFormatting.GREEN));
        }
        if (fwmSell > 0L) {
            add.add(SpawnerTooltip.line(Lang.t("FWM-Verkauf: ", "FWM sells: "), fwmSell, count, ChatFormatting.GOLD));
        }
        if (add.isEmpty()) {
            add.add(Component.literal((String)Lang.t("FWM-Ankauf: unbekannt", "FWM buy price: unknown")).withStyle(ChatFormatting.GRAY));
        }
        lines.addAll(at, add);
    }

    private static Component line(String label, long cents, int count, ChatFormatting color) {
        String each = Fmt.compactMoney(cents);
        String text = count > 1 ? each + " \u00d7 " + count + " = " + Fmt.compactMoney(cents * (long)count) : each;
        return Component.literal((String)label).withStyle(ChatFormatting.GRAY).append((Component)Component.literal((String)text).withStyle(color));
    }
}

