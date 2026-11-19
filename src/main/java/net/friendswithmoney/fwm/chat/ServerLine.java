/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.Style
 */
package net.friendswithmoney.fwm.chat;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.net.ServerContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public final class ServerLine {
    public static final Pattern DONUT_WHISPER_IN = Pattern.compile("^([A-Za-z0-9_.*]{2,17}) whispers to you: (.+)$");
    public static final Pattern DONUT_WHISPER_OUT = Pattern.compile("^You whisper to ([A-Za-z0-9_.*]{2,17}): (.+)$");
    public static final Pattern DONUT_PM = Pattern.compile("^\\[?\\s*(?:[A-Za-z]+\\s+)?([A-Za-z0-9_.*]{2,17})\\s*(?:->|\u2192|\u279c|\u00bb|\u27a1)\\s*([A-Za-z0-9_.*]{2,17})\\s*\\]?\\s*(?::|\u00bb|>|-)?\\s+(.+)$");
    public static final Pattern WIND_PM = Pattern.compile("^(?:[A-Z]+ ([A-Za-z0-9_.*]{2,17}) -> me|Me -> ([A-Za-z0-9_.*]{2,17})): (.*)$");
    private static final Set<String> SERVER_WORDS = Set.of("gedroppt", "geliefert", "achtung", "abgeholt", "verwendung", "sellmulti");
    private static final Pattern NAME_COLON = Pattern.compile("^\\s*([.*]?[A-Za-z0-9_]{2,16})\\s*:");

    private ServerLine() {
    }

    public static String plain(Component message) {
        return message == null ? "" : ServerLine.strip(message.getString());
    }

    public static String strip(String line) {
        if (line == null || line.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder(line.length());
        for (int i = 0; i < line.length(); ++i) {
            char c = line.charAt(i);
            if (c == '\u00a7' && i + 1 < line.length()) {
                ++i;
                continue;
            }
            if (ServerLine.invisible(c)) continue;
            out.append(c);
        }
        return out.toString();
    }

    private static boolean invisible(char c) {
        return c == '\u200b' || c == '\u200c' || c == '\u200d' || c == '\u2060' || c == '\ufeff';
    }

    public static boolean fromServer(Component message) {
        return switch (ServerContext.current()) {
            case ServerContext.Server.WIND -> {
                String p = ServerLine.plain(message);
                if (!(p.isBlank() || p.startsWith("<") || WIND_PM.matcher(p).find())) {
                    yield true;
                }
                yield false;
            }
            case ServerContext.Server.DONUT -> {
                if (!ServerLine.plain(message).isBlank()) {
                    yield true;
                }
                yield false;
            }
            default -> ServerLine.tagged(message, ServerLine.prefix());
        };
    }

    public static boolean tagged(Component message, String tag) {
        if (!ServerLine.startsColoured(message, tag)) {
            return false;
        }
        String rest = ServerLine.plain(message).substring(tag.length());
        Matcher m = NAME_COLON.matcher(rest);
        return !m.find() || SERVER_WORDS.contains(m.group(1).toLowerCase(Locale.ROOT));
    }

    public static boolean startsColoured(Component message, String prefix) {
        if (message == null || prefix == null || prefix.isEmpty()) {
            return false;
        }
        if (!ServerLine.plain(message).startsWith(prefix)) {
            return false;
        }
        int[] seen = new int[]{0};
        boolean[] legacy = new boolean[]{false};
        Optional coloured = message.visit((style, part) -> {
            for (int i = 0; i < part.length(); ++i) {
                char c = part.charAt(i);
                if (c == '\u00a7' && i + 1 < part.length()) {
                    char code;
                    if ((code = Character.toLowerCase(part.charAt(++i))) == 'r') {
                        legacy[0] = false;
                        continue;
                    }
                    if (!(code >= '0' && code <= '9' || code >= 'a' && code <= 'f') && code != 'x') continue;
                    legacy[0] = true;
                    continue;
                }
                if (ServerLine.invisible(c)) continue;
                if (seen[0] >= prefix.length()) {
                    return Optional.of(Boolean.TRUE);
                }
                if (Character.isLetter(c) && style.getColor() == null && !legacy[0]) {
                    return Optional.of(Boolean.FALSE);
                }
                seen[0] = seen[0] + 1;
            }
            return seen[0] >= prefix.length() ? Optional.of(Boolean.TRUE) : Optional.empty();
        }, Style.EMPTY);
        return coloured.orElse(Boolean.FALSE);
    }

    private static String prefix() {
        String prefix = FwmConfig.INSTANCE.serverPrefix;
        return prefix == null || prefix.isBlank() ? "[HugoSMP]" : prefix.trim();
    }
}

