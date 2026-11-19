/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 */
package net.friendswithmoney.fwm.stream.fx;

import java.util.Arrays;
import net.friendswithmoney.fwm.stream.DonationTier;
import net.friendswithmoney.fwm.stream.fx.FxAnim;
import net.friendswithmoney.fwm.stream.fx.FxCanvas;
import net.friendswithmoney.fwm.util.Fmt;
import net.minecraft.world.item.ItemStack;

public final class Play {
    public final float[] r = new float[1024];
    public String donor = "TestSpender";
    public long amount;
    public long total;
    public int count = 1;
    public DonationTier tier = new DonationTier();
    public FxAnim anim;
    public ItemStack item = ItemStack.EMPTY;
    public boolean preview;
    public boolean sound = true;
    public boolean test;
    public int durMs;
    public final int[] palBurst = new int[3];
    public final int[] palSmall = new int[2];
    public final int[] palRocketA = new int[3];
    public final int[] palRocketB = new int[3];
    public final int[] palConfetti = new int[6];
    public String title = "";
    public String line = "";
    public String amountText = "";
    public String amountFull = "";
    public String tickerLine = "";
    public String[] titleChars = new String[0];
    public final int[] tones = new int[5];
    public String[] tickGlyphs = new String[0];
    public byte[] tickRole = new byte[0];
    private static final int STEPS = 90;
    private final String[] countCache = new String[91];
    private long countUnit = 1L;
    private long countTotal;
    private int countDecimals;
    private String countSuffix = "";

    public Play() {
        this.reseed(1L);
    }

    public void reseed(long seed) {
        long s = seed * -7046029254386353131L + 7146057691288625177L;
        for (int i = 0; i < this.r.length; ++i) {
            s ^= s << 13;
            s ^= s >>> 7;
            s ^= s << 17;
            this.r[i] = (float)(s >>> 40 & 0xFFFFFFL) / 1.6777216E7f;
        }
    }

    public float rnd(int i, float a, float b) {
        return a + this.r[i & this.r.length - 1] * (b - a);
    }

    public void prepare(FxAnim anim, DonationTier tier) {
        int col;
        this.anim = anim;
        this.tier = tier;
        this.title = tier.name == null ? "" : tier.name;
        this.titleChars = new String[this.title.length()];
        for (int i = 0; i < this.title.length(); ++i) {
            this.titleChars[i] = this.title.substring(i, i + 1);
        }
        this.amountText = Play.money(this.amount);
        this.amountFull = Fmt.fullMoney(this.amount);
        this.line = this.fill(tier.text);
        this.item = FxCanvas.itemOf(tier.item);
        int base = anim.defMs;
        this.durMs = tier.seconds > 0 ? Math.max(2000, tier.seconds * 1000) : base;
        String gave = " donates ";
        this.tickerLine = "\u2605 " + this.donor + gave + this.amountText + (String)(this.count > 1 ? " \u00b7 " + this.count + " Pays" : "") + " \u2605 " + this.line + "   ";
        this.prepareCount();
        this.prepareTicker(gave);
        this.palBurst[0] = col = tier.color | 0xFF000000;
        this.palBurst[1] = -1;
        this.palBurst[2] = -670398;
        this.palSmall[0] = col;
        this.palSmall[1] = -1;
        this.palRocketA[0] = col;
        this.palRocketA[1] = -1;
        this.palRocketA[2] = col;
        this.palRocketB[0] = col;
        this.palRocketB[1] = -1;
        this.palRocketB[2] = -670398;
        this.palConfetti[0] = col;
        this.palConfetti[1] = -1;
        this.palConfetti[2] = -670398;
        this.palConfetti[3] = -11884289;
        this.palConfetti[4] = -42406;
        this.palConfetti[5] = -11870565;
        this.tones[0] = col;
        this.tones[1] = FxCanvas.mix(col, -1, 0.42f);
        this.tones[2] = FxCanvas.mix(col, -1, 0.78f);
        this.tones[3] = FxCanvas.mix(col, -16777216, 0.28f);
        this.tones[4] = col == -670398 ? -8054 : -670398;
    }

    private void prepareCount() {
        Arrays.fill(this.countCache, null);
        long dollars = Math.max(0L, this.amount) / 100L;
        long[] steps = new long[]{1000000000000L, 1000000000L, 1000000L, 1000L};
        String[] units = new String[]{"T", "B", "M", "K"};
        this.countUnit = 1L;
        this.countSuffix = "";
        this.countDecimals = 0;
        for (int i = 0; i < steps.length; ++i) {
            int end;
            if (dollars < steps[i]) continue;
            this.countUnit = steps[i];
            this.countSuffix = units[i];
            String s = this.amountText;
            int comma = s.indexOf(44);
            if (comma < 0) break;
            for (end = comma + 1; end < s.length() && Character.isDigit(s.charAt(end)); ++end) {
            }
            this.countDecimals = end - comma - 1;
            break;
        }
        long scale = this.countDecimals == 2 ? 100L : (this.countDecimals == 1 ? 10L : 1L);
        this.countTotal = this.countUnit == 1L ? dollars : dollars * scale / this.countUnit;
    }

    private void prepareTicker(String gave) {
        String dot = "   \u00b7   ";
        String amt = this.amountText + (String)(this.count > 1 ? " \u00b7 " + this.count + " Pays" : "");
        if (!this.donor.isEmpty() && this.line.contains(this.donor) && this.line.contains(this.amountText) && this.line.codePointCount(0, this.line.length()) == this.line.length()) {
            this.prepareGlyphs(new String[]{this.line}, new byte[]{4});
            this.mark(this.line, this.donor, (byte)0);
            this.mark(this.line, (String)(this.line.contains(amt) ? amt : this.amountText), (byte)2);
            return;
        }
        this.prepareGlyphs(new String[]{this.donor, gave, amt, dot, this.line}, new byte[]{0, 1, 2, 3, 4});
    }

    private void mark(String line, String what, byte role) {
        int at = line.indexOf(what);
        if (at < 0) {
            return;
        }
        for (int i = at; i < at + what.length() && i < this.tickRole.length; ++i) {
            this.tickRole[i] = role;
        }
    }

    private void prepareGlyphs(String[] parts, byte[] partRoles) {
        int n = 0;
        for (String part : parts) {
            n += part.codePointCount(0, part.length());
        }
        this.tickGlyphs = new String[n];
        this.tickRole = new byte[n];
        int k = 0;
        for (int r = 0; r < parts.length; ++r) {
            int len;
            String part = parts[r];
            for (int i = 0; i < part.length(); i += len) {
                int cp = part.codePointAt(i);
                len = Character.charCount(cp);
                this.tickGlyphs[k] = part.substring(i, i + len);
                this.tickRole[k] = partRoles[r];
                ++k;
            }
        }
    }

    public String fill(String template) {
        if (template == null) {
            return "";
        }
        String amountWithCount = Play.money(this.amount) + (String)(this.count > 1 ? " \u00b7 " + this.count + " Pays" : "");
        String sum = Play.money(Math.max(this.total, this.amount));
        String n = Integer.toString(Math.max(1, this.count));
        return template.replace("{name}", this.donor).replace("{betrag}", amountWithCount).replace("{amount}", amountWithCount).replace("{gesamt}", sum).replace("{total}", sum).replace("{anzahl}", n).replace("{count}", n);
    }

    public static String money(long cents) {
        return "$" + Fmt.shortMoney(cents);
    }

    public String counted(float progress) {
        Object s;
        float p = Math.max(0.0f, Math.min(1.0f, progress));
        if (p >= 1.0f || this.countTotal <= 0L) {
            return this.amountText;
        }
        float q = 1.0f - p;
        float eased = 1.0f - q * q * q * q * q;
        int steps = (int)Math.min(90L, this.countTotal);
        int i = Math.min(steps - 1, (int)(eased * (float)steps));
        if (i < 0) {
            i = 0;
        }
        if ((s = this.countCache[i]) == null) {
            Object text;
            long v = this.countTotal * (long)i / (long)steps;
            if (this.countDecimals == 0) {
                text = Long.toString(v);
            } else {
                long div = this.countDecimals == 2 ? 100L : 10L;
                long frac = v % div;
                Object f = Long.toString(frac);
                if (this.countDecimals == 2 && frac < 10L) {
                    f = "0" + (String)f;
                }
                text = v / div + "," + (String)f;
            }
            this.countCache[i] = s = "$" + (String)text + this.countSuffix;
        }
        return s;
    }

    public float animTime(float elapsed) {
        float base = this.anim.defMs;
        if (this.durMs == (int)base) {
            return elapsed;
        }
        float intro = base * 0.3f;
        float outro = base * 0.15f;
        if ((float)this.durMs < intro + outro + 200.0f) {
            return elapsed * base / (float)Math.max(1, this.durMs);
        }
        if (elapsed <= intro) {
            return elapsed;
        }
        float outStart = (float)this.durMs - outro;
        if (elapsed >= outStart) {
            return base - outro + (elapsed - outStart);
        }
        float mid = Math.max(1.0f, outStart - intro);
        return intro + (elapsed - intro) / mid * (base - outro - intro);
    }
}

