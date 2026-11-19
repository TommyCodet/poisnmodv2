/*
 * Decompiled with CFR 0.152.
 */
package net.friendswithmoney.fwm.lyrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Lyrics {
    private static final Pattern TIME = Pattern.compile("\\[(\\d{1,3}):(\\d{1,2})(?:[.:](\\d{1,3}))?]");
    private static final Pattern WORD_TIME = Pattern.compile("<(\\d{1,3}):(\\d{1,2})(?:[.:](\\d{1,3}))?>");
    private static final Pattern OFFSET = Pattern.compile("\\[offset:\\s*([+-]?\\d+)]", 2);
    public final List<Line> lines;
    public final boolean synced;
    public final boolean instrumental;
    public final boolean approximate;
    public long sourceDurationMs;
    private final Map<Integer, Moment> moments = new HashMap<Integer, Moment>();
    private final Set<Integer> hooks = new HashSet<Integer>();

    private Lyrics(List<Line> var1, boolean var2, boolean var3, boolean var4) {
        this.lines = var1;
        this.synced = var2;
        this.instrumental = var3;
        this.approximate = var4;
    }

    public static Lyrics instrumental() {
        return new Lyrics(List.of(), false, true, false);
    }

    private static long ms(Matcher var0) {
        long var1 = Long.parseLong(var0.group(1));
        long var3 = Long.parseLong(var0.group(2));
        long var5 = 0L;
        if (var0.group(3) != null) {
            String var7 = var0.group(3);
            var5 = Long.parseLong(var7);
            if (var7.length() == 1) {
                var5 *= 100L;
            } else if (var7.length() == 2) {
                var5 *= 10L;
            }
        }
        return var1 * 60000L + var3 * 1000L + var5;
    }

    public static Lyrics parseLrc(String var0) {
        ArrayList<Line> var1 = new ArrayList<Line>();
        long var2 = 0L;
        for (String var7 : var0.split("\\r?\\n")) {
            Matcher var8 = OFFSET.matcher(var7);
            if (var8.find()) {
                try {
                    var2 = Long.parseLong(var8.group(1));
                }
                catch (NumberFormatException numberFormatException) {}
                continue;
            }
            Matcher var9 = TIME.matcher(var7);
            ArrayList<Long> var10 = new ArrayList<Long>();
            int var11 = 0;
            while (var9.find() && var9.start() == var11) {
                var10.add(Lyrics.ms(var9));
                var11 = var9.end();
            }
            if (var10.isEmpty()) continue;
            String var12 = var7.substring(var11);
            ArrayList<long[]> var13 = null;
            StringBuilder var14 = new StringBuilder();
            Matcher var15 = WORD_TIME.matcher(var12);
            if (var15.find()) {
                var13 = new ArrayList<long[]>();
                var15.reset();
                int var16 = 0;
                long var17 = -1L;
                while (var15.find()) {
                    Lyrics.appendTimedWords(var14, var13, var12.substring(var16, var15.start()), var17);
                    var17 = Lyrics.ms(var15);
                    var16 = var15.end();
                }
                Lyrics.appendTimedWords(var14, var13, var12.substring(var16), var17);
            } else {
                var14.append(var12);
            }
            String var22 = var14.toString().trim().replaceAll("\\s+", " ");
            Iterator iterator = var10.iterator();
            while (iterator.hasNext()) {
                long var18 = (Long)iterator.next();
                var1.add(new Line(Math.max(0L, var18 - var2), var22, Lyrics.shift(var13, var2)));
            }
        }
        var1.sort((var0x, var1x) -> Long.compare(var0x.startMs, var1x.startMs));
        Lyrics var21 = new Lyrics(Collections.unmodifiableList(var1), true, false, false);
        var21.buildWords();
        var21.findMoments();
        return var21;
    }

    public static Lyrics fromPlain(String var0, long var1) {
        long var18;
        long var17;
        ArrayList<String> var3 = new ArrayList<String>();
        for (String var7 : var0.split("\\r?\\n")) {
            String var8 = var7.trim().replaceAll("\\s+", " ");
            if (var8.isEmpty() || var8.startsWith("[") && var8.endsWith("]")) continue;
            var3.add(var8);
        }
        if (var3.isEmpty()) {
            return null;
        }
        if (var1 > 30000L) {
            var17 = Math.min(15000L, var1 / 12L);
            var18 = var1 - Math.min(10000L, var1 / 15L);
        } else {
            var17 = 8000L;
            var18 = var17 + (long)var3.size() * 3500L;
        }
        double var19 = 0.0;
        double[] var10 = new double[var3.size()];
        for (int var11 = 0; var11 < var10.length; ++var11) {
            var10[var11] = 6.0 + (double)((String)var3.get(var11)).length();
            var19 += var10[var11];
        }
        ArrayList<Line> var20 = new ArrayList<Line>();
        double var12 = 0.0;
        for (int var14 = 0; var14 < var10.length; ++var14) {
            long var15 = var17 + Math.round((double)(var18 - var17) * var12 / var19);
            var20.add(new Line(var15, (String)var3.get(var14), null));
            var12 += var10[var14];
        }
        Lyrics var21 = new Lyrics(Collections.unmodifiableList(var20), true, false, true);
        var21.buildWords();
        var21.findMoments();
        return var21;
    }

    private static void appendTimedWords(StringBuilder var0, List<long[]> var1, String var2, long var3) {
        boolean var5 = true;
        for (String var9 : var2.trim().split("\\s+")) {
            if (var9.isEmpty()) continue;
            if (var0.length() > 0) {
                var0.append(' ');
            }
            var0.append(var9);
            var1.add(new long[]{var5 ? var3 : -1L});
            var5 = false;
        }
    }

    private static List<long[]> shift(List<long[]> var0, long var1) {
        if (var0 == null) {
            return null;
        }
        ArrayList<long[]> var3 = new ArrayList<long[]>();
        for (long[] var5 : var0) {
            var3.add(new long[]{var5[0] < 0L ? -1L : Math.max(0L, var5[0] - var1)});
        }
        return var3;
    }

    private void buildWords() {
        for (int var1 = 0; var1 < this.lines.size(); ++var1) {
            Line var2 = this.lines.get(var1);
            String[] var3 = var2.text.isEmpty() ? new String[]{} : var2.text.split(" ");
            ArrayList<Word> var4 = new ArrayList<Word>(var3.length);
            long var5 = this.endOf(var1, 0L);
            long var7 = var2.startMs + Math.max(300L, (long)((double)(var5 - var2.startMs) * 0.85));
            if (var2.exact != null && var2.exact.size() == var3.length) {
                long[] var23 = new long[var3.length];
                for (int var10 = 0; var10 < var3.length; ++var10) {
                    var23[var10] = var2.exact.get(var10)[0];
                }
                if (var23.length > 0 && var23[0] < 0L) {
                    var23[0] = var2.startMs;
                }
                for (int var24 = 1; var24 < var23.length; ++var24) {
                    int var26;
                    if (var23[var24] >= 0L) continue;
                    for (var26 = var24; var26 < var23.length && var23[var26] < 0L; ++var26) {
                    }
                    long var28 = var23[var24 - 1];
                    long var30 = var26 < var23.length ? var23[var26] : var7;
                    for (int var31 = var24; var31 < var26; ++var31) {
                        var23[var31] = var28 + (var30 - var28) * (long)(var31 - var24 + 1) / (long)(var26 - var24 + 1);
                    }
                    var24 = var26 - 1;
                }
                for (int var25 = 0; var25 < var3.length; ++var25) {
                    long var27 = var25 + 1 < var3.length ? var23[var25 + 1] : Math.max(var23[var25] + 250L, var7);
                    var4.add(new Word(var3[var25], var23[var25], Math.max(var23[var25] + 1L, var27)));
                }
            } else {
                long var9 = Math.max(300L, var7 - var2.startMs);
                double[] var11 = new double[var3.length];
                double var12 = 0.0;
                for (int var14 = 0; var14 < var3.length; ++var14) {
                    int var15 = var3[var14].replaceAll("[^\\p{L}\\p{N}]", "").length();
                    var11[var14] = 170.0 + 55.0 * (double)Math.min(var15, 12);
                    var12 += var11[var14];
                }
                double var29 = var12 > (double)var9 ? (double)var9 / var12 : 1.0;
                double var16 = 0.0;
                for (int var18 = 0; var18 < var3.length; ++var18) {
                    long var19 = var2.startMs + Math.round(var16 * var29);
                    long var21 = var18 == var3.length - 1 ? var7 : var2.startMs + Math.round((var16 += var11[var18]) * var29);
                    var4.add(new Word(var3[var18], var19, Math.max(var19 + 1L, var21)));
                }
            }
            var2.words = Collections.unmodifiableList(var4);
        }
    }

    private static String norm(String var0) {
        return var0.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N} ]", "").replaceAll("\\s+", " ").trim();
    }

    private void findMoments() {
        HashMap<String, Integer> var1 = new HashMap<String, Integer>();
        for (Line var3 : this.lines) {
            String var4 = Lyrics.norm(var3.text);
            if (var4.length() < 4) continue;
            var1.merge(var4, 1, Integer::sum);
        }
        int var11 = -1;
        int var12 = -1;
        long var13 = -1L;
        boolean var6 = false;
        for (int var7 = 0; var7 < this.lines.size(); ++var7) {
            boolean var10;
            boolean var9;
            Line var8 = this.lines.get(var7);
            if (var8.text.isBlank()) continue;
            if (var11 < 0) {
                var11 = var7;
            }
            var12 = var7;
            boolean bl = var9 = var1.getOrDefault(Lyrics.norm(var8.text), 0) >= 2;
            if (var9) {
                this.hooks.add(var7);
            }
            boolean bl2 = var10 = var13 >= 0L && var8.startMs - var13 >= 7000L;
            if (var9 && !var6) {
                this.moments.put(var7, Moment.CHORUS);
            } else if (var10) {
                this.moments.put(var7, Moment.DROP);
            }
            var6 = var9;
            var13 = this.endOf(var7, 0L);
        }
        if (var11 >= 0) {
            this.moments.putIfAbsent(var11, Moment.INTRO);
        }
        if (var12 > var11) {
            this.moments.put(var12, Moment.FINALE);
        }
    }

    public boolean isHook(int var1) {
        return this.hooks.contains(var1);
    }

    public Moment momentAt(int var1) {
        return this.moments.get(var1);
    }

    public int indexAt(long var1) {
        int var3 = 0;
        int var4 = this.lines.size() - 1;
        int var5 = -1;
        while (var3 <= var4) {
            int var6 = var3 + var4 >>> 1;
            if (this.lines.get((int)var6).startMs <= var1) {
                var5 = var6;
                var3 = var6 + 1;
                continue;
            }
            var4 = var6 - 1;
        }
        return var5;
    }

    public long endOf(int var1, long var2) {
        Line var4 = this.lines.get(var1);
        long var5 = var1 + 1 < this.lines.size() ? this.lines.get((int)(var1 + 1)).startMs : (var2 > 0L ? var2 : var4.startMs + 6000L);
        long var7 = 3500L + (long)var4.text.length() * 140L;
        return Math.min(var5, var4.startMs + Math.max(4000L, var7));
    }

    public static float wordPosition(Line var0, long var1) {
        List<Word> var3 = var0.words();
        if (var3 != null && !var3.isEmpty()) {
            for (int var4 = 0; var4 < var3.size(); ++var4) {
                Word var5 = var3.get(var4);
                if (var1 < var5.startMs()) {
                    return var4;
                }
                if (var1 >= var5.endMs()) continue;
                return (float)var4 + (float)(var1 - var5.startMs()) / (float)(var5.endMs() - var5.startMs());
            }
            return var3.size();
        }
        return 0.0f;
    }

    public static final class Line {
        private final long startMs;
        private final String text;
        private final List<long[]> exact;
        private List<Word> words;

        Line(long var1, String var3, List<long[]> var4) {
            this.startMs = var1;
            this.text = var3;
            this.exact = var4;
        }

        public long startMs() {
            return this.startMs;
        }

        public String text() {
            return this.text;
        }

        public List<Word> words() {
            return this.words;
        }
    }

    public record Word(String text, long startMs, long endMs) {
    }

    public static enum Moment {
        INTRO,
        CHORUS,
        DROP,
        FINALE;

    }
}

