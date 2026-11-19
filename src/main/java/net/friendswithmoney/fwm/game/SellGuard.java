/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
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
 *  net.minecraft.world.item.ItemStack
 */
package net.friendswithmoney.fwm.game;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.net.MarketPrices;
import net.friendswithmoney.fwm.net.ServerContext;
import net.friendswithmoney.fwm.shop.MarketKey;
import net.friendswithmoney.fwm.ui.FwmNotice;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.util.Fmt;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

public final class SellGuard {
    private static final Set<String> VERBS = Set.of("ah", "auction", "auctionhouse", "auktion", "ahs");
    private static final Pattern PRICE = Pattern.compile("^\\$?([0-9][0-9.,]*)([kKmMbB]?)$");
    private static final long CONFIRM_MS = 60000L;
    private static String confirmed;
    private static long confirmedAt;

    private SellGuard() {
    }

    public static void register() {
        ClientSendMessageEvents.ALLOW_COMMAND.register(SellGuard::allow);
    }

    static boolean allow(String command) {
        boolean high;
        double orders;
        if (command == null || !V2.INSTANCE.sellGuard) {
            return true;
        }
        if (ServerContext.current() != ServerContext.Server.HUGO) {
            return true;
        }
        String[] p = command.trim().split("\\s+");
        if (p.length < 3) {
            return true;
        }
        if (!VERBS.contains(p[0].toLowerCase(Locale.ROOT)) || !p[1].equalsIgnoreCase("sell")) {
            return true;
        }
        double price = SellGuard.parsePrice(p[2]);
        if (price <= 0.0) {
            return true;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return true;
        }
        ItemStack hand = client.player.getMainHandItem();
        if (hand == null || hand.isEmpty()) {
            return true;
        }
        Double[] prices = MarketPrices.get(MarketKey.of(hand));
        if (prices == null || prices.length < 2) {
            return true;
        }
        double ah = MarketPrices.ahForValue(prices);
        double each = Math.max(ah, orders = prices[1] == null ? 0.0 : prices[1]);
        if (each <= 0.0) {
            return true;
        }
        double value = each * (double)hand.getCount();
        boolean low = price < value * (double)V2.INSTANCE.sellGuardLowPct / 100.0;
        boolean bl = high = V2.INSTANCE.sellGuardHighX > 0 && price > value * (double)V2.INSTANCE.sellGuardHighX;
        if (!low && !high) {
            return true;
        }
        long now = System.currentTimeMillis();
        if (command.equals(confirmed) && now - confirmedAt < 60000L) {
            confirmed = null;
            return true;
        }
        confirmed = command;
        confirmedAt = now;
        SellGuard.warn(command, price, value, low, hand);
        return false;
    }

    static double parsePrice(String raw) {
        double v;
        Matcher m = PRICE.matcher(raw.trim());
        if (!m.matches()) {
            return -1.0;
        }
        String num = m.group(1);
        String unit = m.group(2).toLowerCase(Locale.ROOT);
        if (num.matches("\\d{1,3}([.,]\\d{3})+")) {
            num = num.replaceAll("[.,]", "");
        } else if ((num = num.replace(',', '.')).indexOf(46) != num.lastIndexOf(46)) {
            return -1.0;
        }
        try {
            v = Double.parseDouble(num);
        }
        catch (NumberFormatException e) {
            return -1.0;
        }
        return switch (unit) {
            case "k" -> v * 1000.0;
            case "m" -> v * 1000000.0;
            case "b" -> v * 1.0E9;
            default -> v;
        };
    }

    private static String money(double dollars) {
        return Fmt.compactMoney(Math.round(dollars * 100.0));
    }

    private static void warn(String command, double price, double value, boolean low, ItemStack hand) {
        Minecraft client = Minecraft.getInstance();
        MutableComponent send = Component.literal((String)(" [" + Lang.t("Trotzdem verkaufen", "Sell anyway") + "]")).withStyle(new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}).withStyle(s -> s.withClickEvent((ClickEvent)new ClickEvent.RunCommand("/" + command)).withHoverEvent((HoverEvent)new HoverEvent.ShowText((Component)Component.literal((String)("/" + command)))));
        String what = (String)(hand.getCount() > 1 ? hand.getCount() + "x " : "") + hand.getHoverName().getString();
        String msg = low ? Lang.t("Wirklich " + what + " f\u00fcr " + SellGuard.money(price) + " verkaufen? \u00dcblich sind ca. " + SellGuard.money(value) + ".", "Really sell " + what + " for " + SellGuard.money(price) + "? It is usually worth about " + SellGuard.money(value) + ".") : Lang.t("Wirklich " + what + " f\u00fcr " + SellGuard.money(price) + " einstellen? \u00dcblich sind ca. " + SellGuard.money(value) + " \u2013 eine Null zu viel?", "Really list " + what + " for " + SellGuard.money(price) + "? It is usually worth about " + SellGuard.money(value) + " \u2013 one zero too many?");
        MutableComponent line = Component.literal((String)"[FWM] ").withStyle(ChatFormatting.GOLD).append((Component)Component.literal((String)(low ? Lang.t("Sehr billig \u2013 ", "Very cheap \u2013 ") : Lang.t("Sehr teuer \u2013 ", "Very expensive \u2013 "))).withStyle(new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.BOLD})).append((Component)Component.literal((String)msg).withStyle(ChatFormatting.YELLOW)).append((Component)send);
        client.gui.hud.getChat().addClientSystemMessage((Component)line);
        FwmNotice.show("FWM", Lang.t("Verkauf angehalten", "Sale held"), Lang.t("Preis ", "Price ") + SellGuard.money(price) + Lang.t(" \u2013 \u00fcblich ", " \u2013 usually ") + SellGuard.money(value), 16103723, 5000L);
        if (V2.INSTANCE.guardSound) {
            client.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)((SoundEvent)SoundEvents.NOTE_BLOCK_BASS.value()), (float)0.6f));
        }
    }
}

