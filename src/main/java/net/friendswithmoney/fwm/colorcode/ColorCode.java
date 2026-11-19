/*
 * Decompiled with CFR 0.152.
 */
package net.friendswithmoney.fwm.colorcode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.friendswithmoney.fwm.colorcode.CodeStyle;

public final class ColorCode {
    public static final String CODES = "0123456789abcdef";
    public static final int[] RGB = new int[]{0, 170, 43520, 43690, 0xAA0000, 0xAA00AA, 0xFFAA00, 0xAAAAAA, 0x555555, 0x5555FF, 0x55FF55, 0x55FFFF, 0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF};
    private static final Pattern PREFIX = Pattern.compile("^/?(rename|sign)\\s+", 2);
    private String text = "";
    private List<CodeStyle> styles = new ArrayList<CodeStyle>();
    public static final int CHAT_MAX = 256;

    public String text() {
        return this.text;
    }

    public List<CodeStyle> styles() {
        return this.styles;
    }

    public void set(String newText, List<CodeStyle> newStyles) {
        this.text = newText;
        this.styles = new ArrayList<CodeStyle>(newStyles);
    }

    public void set(ColorCode other) {
        this.set(other.text, other.styles);
    }

    public void applyColor(int start, int end, String color) {
        for (int i = this.clamp(start); i < this.clamp(end); ++i) {
            this.styles.set(i, this.styles.get(i).withColor(color));
        }
    }

    public void applyHex(int start, int end, String hex) {
        this.applyColor(start, end, hex.toUpperCase(Locale.ROOT));
    }

    public void toggleFormat(int start, int end, CodeStyle.Format format) {
        int i;
        boolean all = true;
        for (i = this.clamp(start); i < this.clamp(end); ++i) {
            if (this.styles.get(i).has(format)) continue;
            all = false;
            break;
        }
        for (i = this.clamp(start); i < this.clamp(end); ++i) {
            this.styles.set(i, this.styles.get(i).with(format, !all));
        }
    }

    public void reset(int start, int end) {
        for (int i = this.clamp(start); i < this.clamp(end); ++i) {
            this.styles.set(i, CodeStyle.DEFAULT);
        }
    }

    public boolean allHave(int start, int end, CodeStyle.Format format) {
        if (this.clamp(start) >= this.clamp(end)) {
            return false;
        }
        for (int i = this.clamp(start); i < this.clamp(end); ++i) {
            if (this.styles.get(i).has(format)) continue;
            return false;
        }
        return true;
    }

    private int clamp(int index) {
        return Math.max(0, Math.min(this.text.length(), index));
    }

    public void reconcile(String newText) {
        int newEnd;
        int start;
        String old = this.text;
        int min = Math.min(old.length(), newText.length());
        for (start = 0; start < min && old.charAt(start) == newText.charAt(start); ++start) {
        }
        int oldEnd = old.length();
        for (newEnd = newText.length(); oldEnd > start && newEnd > start && old.charAt(oldEnd - 1) == newText.charAt(newEnd - 1); --oldEnd, --newEnd) {
        }
        CodeStyle inherit = start > 0 ? this.styles.get(start - 1) : (this.styles.isEmpty() ? CodeStyle.DEFAULT : this.styles.get(0));
        ArrayList<CodeStyle> out = new ArrayList<CodeStyle>(this.styles.subList(0, start));
        for (int i = start; i < newEnd; ++i) {
            out.add(inherit);
        }
        out.addAll(this.styles.subList(oldEnd, this.styles.size()));
        this.text = newText;
        this.styles = out;
    }

    public List<Run> runs() {
        ArrayList<Run> out = new ArrayList<Run>();
        for (int i = 0; i < this.text.length(); ++i) {
            ColorCode.appendMerged(out, this.styles.get(i), String.valueOf(this.text.charAt(i)));
        }
        return out;
    }

    private static void appendMerged(List<Run> out, CodeStyle style, String part) {
        Run last;
        Run run = last = out.isEmpty() ? null : out.get(out.size() - 1);
        if (last != null && last.style.equals(style)) {
            out.set(out.size() - 1, new Run(style, last.text + part));
        } else {
            out.add(new Run(style, part));
        }
    }

    public List<Run> optimizedRuns() {
        List<Run> runs = this.runs();
        ArrayList<Run> changed = new ArrayList<Run>(runs);
        for (int i = 1; i < runs.size() - 1; ++i) {
            CodeStyle next;
            CodeStyle prev;
            Run run = runs.get(i);
            if (!ColorCode.isBlank(run.text) || !(prev = runs.get((int)(i - 1)).style).equals(next = runs.get((int)(i + 1)).style) || run.style.underline() != prev.underline() || run.style.strike() != prev.strike() || run.style.obf() != prev.obf()) continue;
            changed.set(i, new Run(prev, run.text));
        }
        ArrayList<Run> out = new ArrayList<Run>();
        for (Run run : changed) {
            ColorCode.appendMerged(out, run.style, run.text);
        }
        return out;
    }

    private static boolean isBlank(String s) {
        if (s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c) || Character.isSpaceChar(c)) continue;
            return false;
        }
        return true;
    }

    public String serialize() {
        StringBuilder out = new StringBuilder();
        CodeStyle cur = CodeStyle.DEFAULT;
        for (Run run : this.optimizedRuns()) {
            boolean formatDropped;
            CodeStyle s = run.style;
            boolean colorChanged = !ColorCode.equalColor(cur.color(), s.color());
            boolean bl = formatDropped = !s.bold() && cur.bold() || !s.underline() && cur.underline() || !s.italic() && cur.italic() || !s.strike() && cur.strike() || !s.obf() && cur.obf();
            if (colorChanged) {
                out.append(s.color() != null ? ColorCode.colorCode(s.color()) : "&r");
                cur = CodeStyle.DEFAULT.colorOnly(s.color());
            } else if (formatDropped) {
                out.append("&r");
                cur = CodeStyle.DEFAULT;
                if (s.color() != null) {
                    out.append(ColorCode.colorCode(s.color()));
                    cur = cur.withColor(s.color());
                }
            }
            for (CodeStyle.Format format : CodeStyle.Format.values()) {
                if (!s.has(format) || cur.has(format)) continue;
                out.append('&').append(format.code);
                cur = cur.with(format, true);
            }
            out.append(run.text);
        }
        return out.toString();
    }

    private static boolean equalColor(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    public static String colorCode(String color) {
        if (color == null) {
            return "";
        }
        if (color.length() == 1) {
            return "&" + color.toLowerCase(Locale.ROOT);
        }
        StringBuilder out = new StringBuilder("&x");
        for (char c : color.toLowerCase(Locale.ROOT).toCharArray()) {
            out.append('&').append(c);
        }
        return out.toString();
    }

    public int length() {
        return this.serialize().length();
    }

    public int length(Mode mode) {
        return mode.codesCount ? this.length() : this.text.length();
    }

    public static int limit(Mode mode) {
        return mode.limit;
    }

    public boolean tooLong(Mode mode) {
        return this.length(mode) > mode.limit || this.tooLongForChat(mode);
    }

    public boolean tooLongForChat(Mode mode) {
        return this.command(mode).length() > 256;
    }

    public int overflow(Mode mode) {
        return Math.max(0, this.length(mode) - mode.limit) + Math.max(0, this.command(mode).length() - 256);
    }

    public String command(Mode mode) {
        return mode.command + " " + this.serialize();
    }

    public static ColorCode parse(String code) {
        StringBuilder text = new StringBuilder();
        ArrayList<CodeStyle> styles = new ArrayList<CodeStyle>();
        CodeStyle cur = CodeStyle.DEFAULT;
        int i = 0;
        while (i < code.length()) {
            char c = code.charAt(i);
            if (ColorCode.isMarker(c) && i + 1 < code.length()) {
                char k = Character.toLowerCase(code.charAt(i + 1));
                if (k == 'x') {
                    StringBuilder hex = new StringBuilder();
                    int j = i + 2;
                    for (int n = 0; n < 6 && j + 1 < code.length() && ColorCode.isMarker(code.charAt(j)) && ColorCode.isHexDigit(code.charAt(j + 1)); ++n) {
                        hex.append(code.charAt(j + 1));
                        j += 2;
                    }
                    if (hex.length() == 6) {
                        cur = cur.colorOnly(hex.toString());
                        i = j;
                        continue;
                    }
                } else {
                    if (CODES.indexOf(k) >= 0) {
                        cur = cur.colorOnly(String.valueOf(k));
                        i += 2;
                        continue;
                    }
                    if (k == 'r') {
                        cur = CodeStyle.DEFAULT;
                        i += 2;
                        continue;
                    }
                    CodeStyle.Format format = ColorCode.formatFor(k);
                    if (format != null) {
                        cur = cur.with(format, true);
                        i += 2;
                        continue;
                    }
                }
            }
            text.append(c);
            styles.add(cur);
            ++i;
        }
        ColorCode out = new ColorCode();
        out.set(text.toString(), styles);
        return out;
    }

    private static boolean isMarker(char c) {
        return c == '&' || c == '\u00a7';
    }

    private static boolean isHexDigit(char c) {
        return Character.digit(c, 16) >= 0 && c < '\u0080';
    }

    private static CodeStyle.Format formatFor(char k) {
        for (CodeStyle.Format format : CodeStyle.Format.values()) {
            if (format.code != k) continue;
            return format;
        }
        return null;
    }

    public static Imported importCode(String raw) {
        String trimmed = raw.trim();
        Mode mode = null;
        Matcher matcher = PREFIX.matcher(trimmed);
        if (matcher.find()) {
            mode = matcher.group(1).equalsIgnoreCase("sign") ? Mode.SIGN : Mode.RENAME;
            trimmed = trimmed.substring(matcher.end());
        }
        return new Imported(ColorCode.parse(trimmed), mode);
    }

    public static int rgb(String color) {
        if (color == null) {
            return 0xFFFFFF;
        }
        if (color.length() == 1) {
            return RGB[CODES.indexOf(Character.toLowerCase(color.charAt(0)))];
        }
        return Integer.parseInt(color, 16);
    }

    public static char nearest16(int rgb) {
        int r = rgb >> 16 & 0xFF;
        int g = rgb >> 8 & 0xFF;
        int b = rgb & 0xFF;
        int best = 0;
        long bestDist = Long.MAX_VALUE;
        for (int i = 0; i < RGB.length; ++i) {
            int dr = r - (RGB[i] >> 16 & 0xFF);
            int dg = g - (RGB[i] >> 8 & 0xFF);
            int db = b - (RGB[i] & 0xFF);
            long dist = (long)dr * (long)dr + (long)dg * (long)dg + (long)db * (long)db;
            if (dist >= bestDist) continue;
            bestDist = dist;
            best = i;
        }
        return CODES.charAt(best);
    }

    public static String normalizeHex(String input) {
        String s = input.replace("#", "").trim();
        if (s.length() != 6) {
            return null;
        }
        for (int i = 0; i < 6; ++i) {
            if (ColorCode.isHexDigit(s.charAt(i))) continue;
            return null;
        }
        return s.toUpperCase(Locale.ROOT);
    }

    public record Run(CodeStyle style, String text) {
    }

    public static enum Mode {
        RENAME("/rename", 35, true),
        SIGN("/sign", 32, false);

        public final String command;
        public final int limit;
        public final boolean codesCount;

        private Mode(String command, int limit, boolean codesCount) {
            this.command = command;
            this.limit = limit;
            this.codesCount = codesCount;
        }
    }

    public record Imported(ColorCode code, Mode mode) {
    }
}

