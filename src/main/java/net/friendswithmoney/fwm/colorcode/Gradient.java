/*
 * Decompiled with CFR 0.152.
 */
package net.friendswithmoney.fwm.colorcode;

import java.util.ArrayList;
import java.util.List;
import net.friendswithmoney.fwm.colorcode.CodeStyle;
import net.friendswithmoney.fwm.colorcode.ColorCode;

public final class Gradient {
    public static final List<Preset> PRESETS = List.of(new Preset("Feuer", "Fire", 16722432, 0xFFAA00, 0xFFEE55), new Preset("Ozean", "Ocean", 17663, 46335, 0x55FFEE), new Preset("Gold", "Gold", 16749824, 0xFFD000, 16774048), new Preset("Lila", "Purple", 7995647, 12599551, 0xFF66FF), new Preset("Regenbogen", "Rainbow", 0xFF5555, 0xFFAA00, 0xFFFF55, 0x55FF55, 0x55FFFF, 0x5555FF, 0xFF55FF), new Preset("Wald", "Forest", 43520, 0x55FF55, 0xC8FF8C), new Preset("Eis", "Ice", 0xFFFFFF, 11071743, 5622015), new Preset("Sunset", "Sunset", 16731786, 16742981, 16763213));

    private Gradient() {
    }

    public static int at(int[] stops, double t) {
        if (stops.length == 1) {
            return stops[0];
        }
        double pos = Math.max(0.0, Math.min(1.0, t)) * (double)(stops.length - 1);
        int i = Math.min(stops.length - 2, (int)Math.floor(pos));
        double f = pos - (double)i;
        int a = stops[i];
        int b = stops[i + 1];
        int r = Gradient.mix(a >> 16 & 0xFF, b >> 16 & 0xFF, f);
        int g = Gradient.mix(a >> 8 & 0xFF, b >> 8 & 0xFF, f);
        int bl = Gradient.mix(a & 0xFF, b & 0xFF, f);
        return r << 16 | g << 8 | bl;
    }

    private static int mix(int a, int b, double f) {
        double la = Math.pow((double)a / 255.0, 2.2);
        double lb = Math.pow((double)b / 255.0, 2.2);
        return (int)Math.round(Math.pow(la + (lb - la) * f, 0.45454545454545453) * 255.0);
    }

    public static Result apply(ColorCode code, int start, int end, int[] stops, ColorCode.Mode mode) {
        String text = code.text();
        start = Math.max(0, start);
        end = Math.min(text.length(), end);
        ArrayList<Integer> letters = new ArrayList<Integer>();
        for (int i = start; i < end; ++i) {
            if (Character.isWhitespace(text.charAt(i)) || Character.isSpaceChar(text.charAt(i))) continue;
            letters.add(i);
        }
        int m = letters.size();
        if (m == 0) {
            return new Result(0, Kind.STANDARD, !code.tooLong(mode));
        }
        int[] target = new int[m];
        for (int j = 0; j < m; ++j) {
            target[j] = Gradient.at(stops, m == 1 ? 0.0 : (double)j / (double)(m - 1));
        }
        ArrayList<String[]> candidates = new ArrayList<String[]>();
        for (int k = 1; k <= m; ++k) {
            String[] hex = new String[m];
            String[] near = new String[m];
            for (int s = 0; s < k; ++s) {
                int from = s * m / k;
                int to = (s + 1) * m / k;
                int rgb = Gradient.at(stops, m == 1 ? 0.0 : (double)(from + to - 1) / 2.0 / (double)(m - 1));
                for (int j = from; j < to; ++j) {
                    hex[j] = Gradient.hex(rgb);
                    near[j] = String.valueOf(ColorCode.nearest16(rgb));
                }
            }
            candidates.add(hex);
            candidates.add(near);
        }
        String[] standard = new String[m];
        for (int j = 0; j < m; ++j) {
            standard[j] = String.valueOf(ColorCode.nearest16(target[j]));
        }
        candidates.add(standard);
        candidates.addAll(Gradient.upgrades(code, start, end, letters, target, standard, mode));
        boolean singleOnly = true;
        for (String[] c : candidates) {
            if (Gradient.steps(c) <= 1 || Gradient.paint(code, start, end, letters, c).tooLong(mode)) continue;
            singleOnly = false;
            break;
        }
        String[] best = null;
        double bestError = Double.MAX_VALUE;
        int bestLength = Integer.MAX_VALUE;
        int bestOver = Integer.MAX_VALUE;
        boolean bestFits = false;
        for (String[] c : candidates) {
            boolean better;
            boolean sameError;
            ColorCode trial = Gradient.paint(code, start, end, letters, c);
            int length = trial.length();
            int over = trial.overflow(mode);
            boolean fits = over == 0 && (m == 1 || Gradient.steps(c) > 1 || singleOnly);
            double error = Gradient.error(target, c);
            boolean bl = sameError = Math.abs(error - bestError) < 1.0E-9;
            boolean bl2 = fits != bestFits ? fits : (fits ? error < bestError - 1.0E-9 || sameError && length < bestLength : (better = over < bestOver || over == bestOver && error < bestError - 1.0E-9));
            if (best != null && !better) continue;
            best = c;
            bestError = error;
            bestLength = length;
            bestOver = over;
            bestFits = fits;
        }
        code.set(Gradient.paint(code, start, end, letters, best));
        return new Result(Gradient.steps(best), Gradient.kind(best), !code.tooLong(mode));
    }

    private static List<String[]> upgrades(ColorCode code, int start, int end, List<Integer> letters, int[] target, String[] standard, ColorCode.Mode mode) {
        ArrayList<String[]> out = new ArrayList<String[]>();
        String[] cur = (String[])standard.clone();
        while (true) {
            String[] bestNext = null;
            double bestGain = 0.0;
            for (int[] run : Gradient.runs(cur)) {
                double gain;
                if (cur[run[0]].length() == 6) continue;
                String[] next = (String[])cur.clone();
                for (int j = run[0]; j < run[1]; ++j) {
                    next[j] = Gradient.hex(run[1] - run[0] <= 3 ? target[j] : Gradient.average(target, run[0], run[1]));
                }
                if (Gradient.paint(code, start, end, letters, next).tooLong(mode) || !((gain = Gradient.error(target, cur) - Gradient.error(target, next)) > bestGain)) continue;
                bestGain = gain;
                bestNext = next;
            }
            if (bestNext == null) {
                return out;
            }
            cur = bestNext;
            out.add((String[])cur.clone());
        }
    }

    private static ColorCode paint(ColorCode code, int start, int end, List<Integer> letters, String[] colors) {
        ArrayList<CodeStyle> styles = new ArrayList<CodeStyle>(code.styles());
        int next = 0;
        String color = colors[0];
        for (int i = start; i < end; ++i) {
            if (next < letters.size() && letters.get(next) == i) {
                color = colors[next++];
            }
            styles.set(i, ((CodeStyle)styles.get(i)).withColor(color));
        }
        ColorCode out = new ColorCode();
        out.set(code.text(), styles);
        return out;
    }

    private static List<int[]> runs(String[] colors) {
        ArrayList<int[]> out = new ArrayList<int[]>();
        int from = 0;
        for (int j = 1; j <= colors.length; ++j) {
            if (j != colors.length && colors[j].equals(colors[from])) continue;
            out.add(new int[]{from, j});
            from = j;
        }
        return out;
    }

    private static int steps(String[] colors) {
        return Gradient.runs(colors).size();
    }

    private static Kind kind(String[] colors) {
        boolean hex = false;
        boolean standard = false;
        for (String c : colors) {
            if (c.length() == 6) {
                hex = true;
                continue;
            }
            standard = true;
        }
        return hex && standard ? Kind.MIXED : (hex ? Kind.HEX : Kind.STANDARD);
    }

    private static int average(int[] target, int from, int to) {
        long r = 0L;
        long g = 0L;
        long b = 0L;
        for (int j = from; j < to; ++j) {
            r += (long)(target[j] >> 16 & 0xFF);
            g += (long)(target[j] >> 8 & 0xFF);
            b += (long)(target[j] & 0xFF);
        }
        int n = to - from;
        return (int)(r / (long)n) << 16 | (int)(g / (long)n) << 8 | (int)(b / (long)n);
    }

    private static double error(int[] target, String[] colors) {
        double sum = 0.0;
        for (int j = 0; j < target.length; ++j) {
            double d = Gradient.distance(target[j], ColorCode.rgb(colors[j]));
            sum += j == 0 || j == target.length - 1 ? 3.0 * d : d;
        }
        return sum;
    }

    private static double distance(int a, int b) {
        int r1 = a >> 16 & 0xFF;
        int g1 = a >> 8 & 0xFF;
        int b1 = a & 0xFF;
        int r2 = b >> 16 & 0xFF;
        int g2 = b >> 8 & 0xFF;
        int b2 = b & 0xFF;
        double rm = (double)(r1 + r2) / 2.0;
        int dr = r1 - r2;
        int dg = g1 - g2;
        int db = b1 - b2;
        return Math.sqrt((2.0 + rm / 256.0) * (double)dr * (double)dr + (double)(4 * dg * dg) + (2.0 + (255.0 - rm) / 256.0) * (double)db * (double)db);
    }

    private static String hex(int rgb) {
        return String.format("%06X", rgb & 0xFFFFFF);
    }

    public record Result(int steps, Kind kind, boolean fits) {
    }

    public static enum Kind {
        HEX,
        STANDARD,
        MIXED;

    }

    public record Preset(String de, String en, int[] stops) {
    }
}

