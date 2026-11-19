/*
 * Decompiled with CFR 0.152.
 */
package net.friendswithmoney.fwm.util;

import java.util.Locale;

public final class Fmt {
    private static final String[] UNITS = new String[]{"K", "M", "B", "T"};

    private Fmt() {
    }

    public static String shortMoney(long cents) {
        String sign;
        boolean negative = cents < 0L;
        long dollars = Math.abs(cents) / 100L;
        String string = sign = negative ? "-" : "";
        if (dollars < 1000L) {
            return sign + dollars;
        }
        long[] steps = new long[]{1000000000000L, 1000000000L, 1000000L, 1000L};
        String[] units = new String[]{"T", "B", "M", "K"};
        for (int i = 0; i < steps.length; ++i) {
            if (dollars < steps[i]) continue;
            long whole = dollars / steps[i];
            long hundredths = dollars % steps[i] * 100L / steps[i];
            Object text = Long.toString(whole);
            if (hundredths > 0L) {
                String frac = String.format(Locale.ROOT, "%02d", hundredths);
                if (frac.endsWith("0")) {
                    frac = frac.substring(0, 1);
                }
                text = (String)text + "," + frac;
            }
            return sign + (String)text + units[i];
        }
        return sign + dollars;
    }

    public static String fullMoney(long cents) {
        String de = Fmt.moneyDe(cents);
        return de.endsWith(",00") ? de.substring(0, de.length() - 3) : de;
    }

    public static String money(long cents) {
        boolean negative = cents < 0L;
        long abs = Math.abs(cents);
        String whole = Fmt.group(abs / 100L);
        String fraction = String.format(Locale.ROOT, "%02d", abs % 100L);
        return (negative ? "-$" : "$") + whole + "." + fraction;
    }

    public static String moneyDe(long cents) {
        boolean negative = cents < 0L;
        long abs = Math.abs(cents);
        String whole = Fmt.group(abs / 100L, '.');
        String fraction = String.format(Locale.ROOT, "%02d", abs % 100L);
        return (negative ? "-$" : "$") + whole + "," + fraction;
    }

    public static String compactMoney(long cents) {
        return "$" + Fmt.compact(cents / 100L);
    }

    public static String compactMoneyUp(long cents) {
        boolean negative = cents < 0L;
        long dollars = (Math.abs(cents) + 99L) / 100L;
        return (negative ? "-$" : "$") + Fmt.compactUp(dollars);
    }

    public static String compactUp(long value) {
        int unit;
        if (value < 1000L) {
            return String.valueOf(value);
        }
        double scaled = value;
        for (unit = -1; scaled >= 1000.0 && unit < UNITS.length - 1; scaled /= 1000.0, ++unit) {
        }
        double up = Math.ceil(scaled * 10.0) / 10.0;
        if (up >= 1000.0 && unit < UNITS.length - 1) {
            up /= 1000.0;
            ++unit;
        }
        String number = up == Math.floor(up) ? String.valueOf((long)up) : String.format(Locale.ROOT, "%.1f", up);
        return number + UNITS[unit];
    }

    public static String compact(long value) {
        long abs = Math.abs(value);
        if (abs < 10000L) {
            return String.valueOf(value);
        }
        if (abs < 1000000L) {
            return Fmt.trim((double)value / 1000.0) + "K";
        }
        if (abs < 1000000000L) {
            return Fmt.trim((double)value / 1000000.0) + "M";
        }
        return Fmt.trim((double)value / 1.0E9) + "B";
    }

    public static String playtime(long minutes) {
        long days = minutes / 1440L;
        long hours = minutes % 1440L / 60L;
        long mins = minutes % 60L;
        if (days > 0L) {
            return days + "d " + hours + "h";
        }
        if (hours > 0L) {
            return hours + "h " + mins + "m";
        }
        return mins + "m";
    }

    public static String duration(long millis) {
        long seconds = millis / 1000L;
        if (seconds < 60L) {
            return seconds + "s";
        }
        return Fmt.playtime(seconds / 60L);
    }

    public static String compactBoardValue(Double num, String kind) {
        if (num == null) {
            return "?";
        }
        long rounded = Math.round(num);
        return switch (kind == null ? "int" : kind) {
            case "money" -> "$" + Fmt.compact(rounded);
            case "playtime" -> Fmt.playtime(rounded);
            case "float" -> String.format(Locale.ROOT, "%.2f", num);
            default -> Fmt.compact(rounded);
        };
    }

    private static String trim(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    public static String dollars(long cents) {
        return "$" + Fmt.group(cents / 100L);
    }

    private static String group(long value) {
        return Fmt.group(value, ',');
    }

    private static String group(long value, char separator) {
        String digits = String.valueOf(value);
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < digits.length(); ++i) {
            if (i > 0 && (digits.length() - i) % 3 == 0) {
                out.append(separator);
            }
            out.append(digits.charAt(i));
        }
        return out.toString();
    }
}

