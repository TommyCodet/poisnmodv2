/*
 * Decompiled with CFR 0.152.
 */
package net.friendswithmoney.fwm.auction;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Times {
    private static final Pattern CLOCK = Pattern.compile("^(\\d{1,4}):(\\d{1,2})(?::(\\d{1,2}))?$");
    private static final Pattern PART = Pattern.compile("(\\d{1,6})\\s*([a-z\u00e4\u00f6\u00fc]*)");

    private Times() {
    }

    public static long seconds(String typed) {
        if (typed == null) {
            return 0L;
        }
        String text = typed.trim().toLowerCase(Locale.ROOT);
        if (text.isEmpty()) {
            return 0L;
        }
        Matcher clock = CLOCK.matcher(text);
        if (clock.matches()) {
            long a = Long.parseLong(clock.group(1));
            long b = Long.parseLong(clock.group(2));
            if (clock.group(3) == null) {
                return a * 60L + b;
            }
            return a * 3600L + b * 60L + Long.parseLong(clock.group(3));
        }
        long total = 0L;
        Matcher part = PART.matcher(text);
        boolean any = false;
        while (part.find()) {
            long value = Long.parseLong(part.group(1));
            String unit = part.group(2);
            any = true;
            if (unit.isEmpty() || unit.startsWith("s")) {
                total += value;
            } else if (unit.startsWith("m")) {
                total += value * 60L;
            } else if (unit.startsWith("h") || unit.startsWith("st")) {
                total += value * 3600L;
            } else {
                return 0L;
            }
            if (total <= 10000000L) continue;
            return 10000000L;
        }
        return any ? total : 0L;
    }

    public static String clock(int seconds) {
        if (seconds < 60) {
            return seconds + "s";
        }
        int h = seconds / 3600;
        int m = seconds % 3600 / 60;
        int s = seconds % 60;
        if (h > 0) {
            return h + ":" + (m < 10 ? "0" : "") + m + ":" + (s < 10 ? "0" : "") + s;
        }
        return m + ":" + (s < 10 ? "0" : "") + s;
    }
}

