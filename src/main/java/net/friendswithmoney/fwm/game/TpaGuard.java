/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.PlayerInfo
 *  net.minecraft.client.player.AbstractClientPlayer
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
 */
package net.friendswithmoney.fwm.game;

import java.util.Locale;
import java.util.Set;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.net.ClanTags;
import net.friendswithmoney.fwm.net.FakeList;
import net.friendswithmoney.fwm.ui.FwmNotice;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.MemberTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public final class TpaGuard {
    private static final Set<String> TPA = Set.of("tpa", "tpahere", "tpaccept", "tpask", "tpyes");
    private static final Set<String> PAY = Set.of("pay", "money pay", "eco pay");
    private static final long CONFIRM_MS = 60000L;
    private static String confirmed;
    private static long confirmedAt;

    private TpaGuard() {
    }

    public static void register() {
        ClientSendMessageEvents.ALLOW_COMMAND.register(TpaGuard::allow);
    }

    static boolean allow(String command) {
        if (command == null || !Features.isOn("fwm.tab.tpaguard")) {
            return true;
        }
        String[] p = command.trim().split("\\s+");
        if (p.length < 2) {
            return true;
        }
        Object verb = p[0].toLowerCase(Locale.ROOT);
        int nameAt = 1;
        if ((((String)verb).equals("money") || ((String)verb).equals("eco")) && p.length >= 3 && p[1].equalsIgnoreCase("pay")) {
            verb = (String)verb + " pay";
            nameAt = 2;
        }
        boolean tpa = TPA.contains(verb);
        boolean pay = PAY.contains(verb);
        if (!tpa && !pay) {
            return true;
        }
        if (tpa && !V2.INSTANCE.guardTpa) {
            return true;
        }
        if (pay && !V2.INSTANCE.guardPay) {
            return true;
        }
        String target = p[nameAt];
        if (!TpaGuard.isFake(target)) {
            return true;
        }
        long now = System.currentTimeMillis();
        if (command.equals(confirmed) && now - confirmedAt < 60000L) {
            confirmed = null;
            return true;
        }
        confirmed = command;
        confirmedAt = now;
        TpaGuard.warn(target, command);
        return false;
    }

    static boolean isFake(String name) {
        PlayerInfo entry;
        if (name == null || name.isBlank()) {
            return false;
        }
        if (FakeList.isFakePlayer(name)) {
            return true;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.getConnection() != null && (entry = client.getConnection().getPlayerInfo(name)) != null && entry.getTabListDisplayName() != null && ClanTags.fakeIn(entry.getTabListDisplayName().getString()) != null) {
            return true;
        }
        if (client.level != null) {
            for (AbstractClientPlayer player : client.level.players()) {
                if (!name.equalsIgnoreCase(player.getGameProfile().name())) continue;
                return MemberTags.wearsFake(player);
            }
        }
        return false;
    }

    private static void warn(String target, String command) {
        Minecraft client = Minecraft.getInstance();
        MutableComponent send = Component.literal((String)(" [" + Lang.t("Trotzdem senden", "Send anyway") + "]")).withStyle(new ChatFormatting[]{ChatFormatting.GREEN, ChatFormatting.BOLD}).withStyle(s -> s.withClickEvent((ClickEvent)new ClickEvent.RunCommand("/" + command)).withHoverEvent((HoverEvent)new HoverEvent.ShowText((Component)Component.literal((String)("/" + command)))));
        MutableComponent line = Component.literal((String)"[FWM] ").withStyle(ChatFormatting.GOLD).append((Component)Component.literal((String)Lang.t("Achtung Faker \u2013 ", "Watch out, fake \u2013 ")).withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD})).append((Component)Component.literal((String)(target + Lang.t(" ist kein echtes Mitglied. Trotzdem senden?", " is not a real member. Send anyway?"))).withStyle(ChatFormatting.RED)).append((Component)send);
        client.gui.hud.getChat().addClientSystemMessage((Component)line);
        FwmNotice.show("FWM", Lang.t("Faker erkannt", "Fake detected"), Lang.t("Aktion angehalten: /", "Action held: /") + command, 0xFF5555, 5000L);
        if (V2.INSTANCE.guardSound) {
            client.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)((SoundEvent)SoundEvents.NOTE_BLOCK_BASS.value()), (float)0.6f));
        }
    }
}

