/*
 * Decompiled with CFR 0.152.
 */
package net.friendswithmoney.fwm.auction;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.friendswithmoney.fwm.auction.Money;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.net.ServerContext;

public final class Payments {
    private static List<Pattern> compiled;
    private static List<String> compiledFrom;
    public static final Pattern WIND_IN;
    public static final Pattern WIND_OUT;
    public static final Pattern DONUT_IN;
    public static final Pattern DONUT_OUT;

    private Payments() {
    }

    public static Payment read(String line, String self) {
        if (line == null || line.isEmpty()) {
            return null;
        }
        ServerContext.Server server = ServerContext.current();
        if (server == ServerContext.Server.WIND || server == ServerContext.Server.DONUT) {
            Matcher m = (server == ServerContext.Server.WIND ? WIND_IN : DONUT_IN).matcher(line.trim());
            if (!m.find()) {
                return null;
            }
            String name = m.group("name");
            if (self != null && name.equalsIgnoreCase(self)) {
                return null;
            }
            long cents = Money.cents(m.group("amount"));
            return cents > 0L ? new Payment(name, cents) : null;
        }
        int spoken = Payments.chatPrefixEnd(line);
        if (spoken < 0) {
            return null;
        }
        for (Pattern pattern : Payments.patterns()) {
            long cents;
            Matcher matcher = pattern.matcher(line);
            if (!matcher.find()) continue;
            String name = Payments.group(matcher, "name");
            String amount = Payments.group(matcher, "amount");
            if (name == null || amount == null || self != null && name.equalsIgnoreCase(self) || (cents = Money.cents(amount + Payments.suffix(matcher))) <= 0L) continue;
            return new Payment(name, cents);
        }
        return null;
    }

    private static int chatPrefixEnd(String line) {
        int colon = line.indexOf(58);
        if (colon > 0 && Payments.isName(Payments.lastPlainWord(line.substring(0, colon)))) {
            return -1;
        }
        int open = line.indexOf(60);
        int close = line.indexOf(62);
        if (open >= 0 && close > open + 1 && Payments.isName(line.substring(open + 1, close).trim())) {
            return -1;
        }
        return 0;
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

    public static boolean isName(String word) {
        if (!(word == null || word.isEmpty() || word.charAt(0) != '.' && word.charAt(0) != '*')) {
            word = word.substring(1);
        }
        if (word == null || word.length() < 3 || word.length() > 16) {
            return false;
        }
        for (int i = 0; i < word.length(); ++i) {
            char c = word.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_') continue;
            return false;
        }
        return true;
    }

    private static String suffix(Matcher matcher) {
        String unit = Payments.group(matcher, "unit");
        return unit == null ? "" : unit;
    }

    private static String group(Matcher matcher, String name) {
        try {
            return matcher.group(name);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static List<Pattern> patterns() {
        List<String> source = FwmConfig.INSTANCE.payPatterns;
        if (source == null || source.isEmpty()) {
            source = FwmConfig.DEFAULT_PAY_PATTERNS;
        }
        if (compiled != null && source.equals(compiledFrom)) {
            return compiled;
        }
        ArrayList<Pattern> built = new ArrayList<Pattern>(source.size());
        for (String regex : source) {
            try {
                built.add(Pattern.compile(regex, 2));
            }
            catch (Exception e) {
                System.err.println("[FWM] Bad pay pattern: " + regex + " (" + e.getMessage() + ")");
            }
        }
        compiled = built;
        compiledFrom = new ArrayList<String>(source);
        return compiled;
    }

    static {
        WIND_IN = Pattern.compile("^You received \\$(?<amount>[0-9.,]+[KMBTkmbt]?) from (?<name>[A-Za-z0-9_.*]{2,17})\\.$");
        WIND_OUT = Pattern.compile("^You paid (?<name>[A-Za-z0-9_.*]{2,17}) \\$(?<amount>[0-9.,]+[KMBTkmbt]?)\\.$");
        DONUT_IN = Pattern.compile("^(?<name>[A-Za-z0-9_.*]{3,17}) paid you \\$ ?(?<amount>[0-9.,]+[kKmMbBtT]?)\\.?$");
        DONUT_OUT = Pattern.compile("^You paid (?<name>[A-Za-z0-9_.*]{2,17}) \\$ ?(?<amount>[0-9.,]+[kKmMbBtT]?)\\.?$");
    }

    public record Payment(String name, long amount) {
    }
}

