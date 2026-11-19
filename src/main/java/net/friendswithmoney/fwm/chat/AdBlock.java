/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.chat.Component
 */
package net.friendswithmoney.fwm.chat;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.friendswithmoney.fwm.chat.Messages;
import net.friendswithmoney.fwm.chat.RealNames;
import net.friendswithmoney.fwm.chat.ServerLine;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.net.ClanTags;
import net.friendswithmoney.fwm.net.FakeList;
import net.friendswithmoney.fwm.net.Members;
import net.friendswithmoney.fwm.net.ServerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class AdBlock {
    private static final Pattern STREAM = Pattern.compile("\\b(live|twitch|kick\\.com|youtube|yt|stream|giveaway|verlosung|gewinnspiel|tiktok)\\b");
    private static final Pattern STREAM_SPACED = Pattern.compile("t\\s+w\\s+i\\s+t\\s+c\\s+h|k\\s+i\\s+c\\s+k\\b");
    private static final Pattern TRADE = Pattern.compile("\\b(kaufe|verkaufe|vk|wts|wtb|ankauf|tausche|selling|buying|biete)\\b");
    private static final Pattern TPA_AD = Pattern.compile("^\\W*tpa?\\b|\\btpa?\\s+(fur|for|vor|zu|ich|mir|me|bei|in|um|wer)\\b|\\btpauto\\b|\\btpa?\\b.{0,25}\\b(mir|me|farm|clan|pvp|ffa|free|gratis|hier|here|lootdrop|loot)\\b|\\b(mir|me)\\b.{0,10}\\btpa?\\b");
    private static final Pattern LINK = Pattern.compile("(https?://|www\\.|discord\\.gg|discord\\.com/invite|\\b[a-z0-9-]{2,}\\.(net|com|de|gg|eu|org|io|xyz|me)\\b|\\b\\d{1,3}(\\.\\d{1,3}){3}\\b)");
    private static final Pattern SERVER_AD = Pattern.compile("(unlock the exclusive|get kits, ranks and keys|in the windsmp store|is waiting for an opponent|store\\.[a-z]+\\.(net|com))");
    private static final Pattern WIND_SPEAKER = Pattern.compile("^<(?:[A-Z]+ )?([A-Za-z0-9_.*]{2,17})> ");
    private static int today;
    private static String day;
    private static final String SMALL_CAPS = "\u1d00\u0299\u1d04\u1d05\u1d07\ua730\u0262\u029c\u026a\u1d0a\u1d0b\u029f\u1d0d\u0274\u1d0f\u1d18\u01eb\u0280\ua731\u1d1b\u1d1c\u1d20\u1d21x\u028f\u1d22";
    static final List<String> CATEGORIES;

    private AdBlock() {
    }

    public static int hiddenToday() {
        AdBlock.roll();
        return today;
    }

    private static void roll() {
        String now = LocalDate.now().toString();
        if (!now.equals(day)) {
            day = now;
            today = 0;
        }
    }

    public static boolean hide(Component message, String sender, boolean signed) {
        Matcher m;
        if (message == null || !Features.isOn("fwm.tab.adblock")) {
            return false;
        }
        String flat = message.getString();
        if (flat == null || flat.isBlank()) {
            return false;
        }
        if (Messages.parse(message) != null) {
            return false;
        }
        String speaker = sender;
        if (speaker == null) {
            speaker = RealNames.speaker(flat);
        }
        if (speaker == null && (m = WIND_SPEAKER.matcher(flat)).find()) {
            speaker = m.group(1);
        }
        String clean = AdBlock.clean(flat);
        if (speaker == null) {
            if (signed) {
                return false;
            }
            if (V2.INSTANCE.adServer && SERVER_AD.matcher(clean).find()) {
                return AdBlock.count();
            }
            return false;
        }
        if (AdBlock.exempt(speaker)) {
            return false;
        }
        if (V2.INSTANCE.adFakes && AdBlock.speakerWearsFake(flat, speaker)) {
            return AdBlock.count();
        }
        String text = clean;
        int colon = text.indexOf(": ");
        if (!signed && colon >= 0 && colon < 60) {
            text = text.substring(colon + 2);
        }
        V2 c = V2.INSTANCE;
        if (c.adStream && (STREAM.matcher(text).find() || STREAM_SPACED.matcher(text).find())) {
            return AdBlock.count();
        }
        if (c.adTrade && TRADE.matcher(text).find()) {
            return AdBlock.count();
        }
        if (c.adTpa && TPA_AD.matcher(text).find()) {
            return AdBlock.count();
        }
        for (String word : c.adWords) {
            String w = AdBlock.clean(word).trim();
            if (w.isEmpty() || !text.contains(w)) continue;
            return AdBlock.count();
        }
        return false;
    }

    private static boolean speakerWearsFake(String flat, String speaker) {
        String head;
        int at = flat.indexOf(speaker);
        String string = head = at > 0 ? flat.substring(0, at) : "";
        if (head.isBlank()) {
            return false;
        }
        return ClanTags.fakeIn(head) != null;
    }

    private static boolean count() {
        AdBlock.roll();
        ++today;
        return true;
    }

    private static boolean exempt(String speaker) {
        String name = ServerLine.strip(speaker).trim();
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && name.equalsIgnoreCase(client.player.getGameProfile().name())) {
            return true;
        }
        if (FakeList.realLabel(name) != null) {
            return true;
        }
        if ("HydraVB".equals(name)) {
            return true;
        }
        return V2.INSTANCE.adKeepMembers && ServerContext.hugo() && Members.isMember(name);
    }

    static String clean(String s) {
        StringBuilder b = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); ++i) {
            char ch = s.charAt(i);
            if (ch == '\u00a7' && i + 1 < s.length()) {
                ++i;
                continue;
            }
            if (ch == '\u200b' || ch == '\u200c' || ch == '\u200d' || ch == '\u2060' || ch == '\ufeff') continue;
            int small = SMALL_CAPS.indexOf(ch);
            b.append(small >= 0 ? (char)(97 + small) : ch);
        }
        String n = Normalizer.normalize(b.toString(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.toLowerCase(Locale.ROOT);
    }

    static {
        day = "";
        CATEGORIES = List.of("stream", "trade", "tpa", "links", "server", "words");
    }
}

