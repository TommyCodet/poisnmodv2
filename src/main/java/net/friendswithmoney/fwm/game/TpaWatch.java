/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.chat.Component
 */
package net.friendswithmoney.fwm.game;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.friendswithmoney.fwm.chat.ServerLine;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class TpaWatch {
    private static final Pattern REQUEST = Pattern.compile("^(?<cmd>tpa|tpask|tpahere|tpaher)\\s+(?<name>[.*]?[A-Za-z0-9_]{2,16})\\s*$", 2);
    private static final List<String> CANCEL = List.of("tpadelete", "tpacancel", "tpcancel", "tpdelete");
    private static final long ANSWER_MS = 5000L;
    private static final Pattern INCOMING = Pattern.compile("hat dir eine Teleportations-Anfrage|hat gefragt, ob du|Du hast die Anfrage von|keine Anfrage von", 2);
    private static Kind kind;
    private static String target;
    private static long sentAt;
    private static Kind askedKind;
    private static String askedTarget;
    private static long askedAt;
    private static final Map<String, Pattern> PATTERNS;

    private TpaWatch() {
    }

    public static void register() {
        ClientSendMessageEvents.COMMAND.register(TpaWatch::onCommand);
    }

    public static boolean active() {
        return target != null;
    }

    public static Kind kind() {
        return kind;
    }

    public static String target() {
        return target;
    }

    public static int remaining() {
        if (target == null) {
            return 0;
        }
        long gone = (System.currentTimeMillis() - sentAt) / 1000L;
        return (int)Math.max(0L, (long)TpaWatch.timeout() - gone);
    }

    public static void clear() {
        kind = null;
        target = null;
        sentAt = 0L;
    }

    private static int timeout() {
        return Math.max(5, FwmConfig.INSTANCE.tpaTimeoutSeconds);
    }

    static void onCommand(String command) {
        if (!Features.isOn("fwm.feature.tpawarn")) {
            return;
        }
        if (command == null) {
            return;
        }
        String line = command.trim();
        String lower = line.toLowerCase(Locale.ROOT);
        for (String word : CANCEL) {
            if (!lower.equals(word) && !lower.startsWith(word + " ")) continue;
            TpaWatch.clear();
            askedTarget = null;
            return;
        }
        Matcher matcher = REQUEST.matcher(line);
        if (!matcher.matches()) {
            return;
        }
        askedKind = matcher.group("cmd").toLowerCase(Locale.ROOT).startsWith("tpaher") ? Kind.TPAHERE : Kind.TPA;
        askedTarget = matcher.group("name");
        askedAt = System.currentTimeMillis();
        System.out.println("[FWM TPA] Befehl erkannt: /" + line + " - warte auf Bestaetigung vom Server");
    }

    public static void accept(Component message) {
        String low;
        if (message == null || !ServerLine.fromServer(message)) {
            return;
        }
        String line = ServerLine.plain(message);
        if (line.isEmpty() || INCOMING.matcher(line).find()) {
            return;
        }
        long now = System.currentTimeMillis();
        boolean asked = askedTarget != null && now - askedAt < 5000L;
        Matcher sent = TpaWatch.pattern(FwmConfig.INSTANCE.tpaSentPattern).matcher(line);
        if (sent.find()) {
            String name = TpaWatch.group(sent, "name");
            if (name == null) {
                String string = name = asked ? askedTarget : null;
            }
            if (name == null) {
                return;
            }
            kind = asked && askedTarget.equalsIgnoreCase(name) ? askedKind : (line.toLowerCase(Locale.ROOT).contains("gefragt") ? Kind.TPAHERE : Kind.TPA);
            target = asked && askedTarget.equalsIgnoreCase(name) ? askedTarget : name;
            sentAt = now;
            askedTarget = null;
            System.out.println("[FWM TPA] Bestaetigung erkannt: " + line);
            return;
        }
        if (asked && (low = line.toLowerCase(Locale.ROOT)).contains(askedTarget.toLowerCase(Locale.ROOT)) && (low.contains("anfrage") || low.contains("request")) && (low.contains("gesendet") || low.contains("geschickt") || low.contains("sent")) && !TpaWatch.pattern(FwmConfig.INSTANCE.tpaRefusedPattern).matcher(line).find()) {
            kind = askedKind;
            target = askedTarget;
            sentAt = now;
            askedTarget = null;
            System.out.println("[FWM TPA] Bestaetigung (frei erkannt): " + line);
            return;
        }
        if (TpaWatch.pattern(FwmConfig.INSTANCE.tpaAlreadyPattern).matcher(line).find()) {
            askedTarget = null;
            return;
        }
        if (asked && TpaWatch.pattern(FwmConfig.INSTANCE.tpaRefusedPattern).matcher(line).find()) {
            askedTarget = null;
            System.out.println("[FWM TPA] Abgelehnt vom Server: " + line);
            return;
        }
        if (asked) {
            System.out.println("[FWM TPA] Zeile nach /tpa nicht zugeordnet: " + line);
        }
        if (target == null) {
            return;
        }
        if (TpaWatch.pattern(FwmConfig.INSTANCE.tpaEndedPattern).matcher(line).find()) {
            TpaWatch.clear();
            return;
        }
        if (line.toLowerCase(Locale.ROOT).contains(target.toLowerCase(Locale.ROOT)) && TpaWatch.pattern(FwmConfig.INSTANCE.tpaDonePattern).matcher(line).find()) {
            TpaWatch.clear();
        }
    }

    private static String group(Matcher matcher, String name) {
        try {
            return matcher.group(name);
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
                System.err.println("[FWM] Bad TPA pattern '" + s + "': " + e.getMessage());
                return Pattern.compile("(?!)");
            }
        });
    }

    public static void tick(Minecraft client) {
        if (target == null) {
            return;
        }
        if (client.player == null || !Features.isOn("fwm.feature.tpawarn")) {
            TpaWatch.clear();
            return;
        }
        if (TpaWatch.remaining() <= 0) {
            TpaWatch.clear();
        }
    }

    static {
        PATTERNS = new HashMap<String, Pattern>();
    }

    public static enum Kind {
        TPA,
        TPAHERE;

    }
}

