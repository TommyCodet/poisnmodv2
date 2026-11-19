/*
 * Decompiled with CFR 0.152.
 */
package net.friendswithmoney.fwm.game;

import java.util.Locale;

public final class Calc {
    private final String s;
    private int i;

    private Calc(String s) {
        this.s = s;
    }

    public static double eval(String input) {
        if (input == null || input.isBlank()) {
            return Double.NaN;
        }
        String norm = input.toLowerCase(Locale.ROOT).replace('\u00d7', '*').replace('x', '*').replace('\u00f7', '/').replace(':', '/').replace('\u2212', '-').replace(',', '.').replace("\u221a", "r").replace(" ", "");
        try {
            Calc c = new Calc(norm);
            double v = c.sum();
            if (c.i != c.s.length()) {
                return Double.NaN;
            }
            return v;
        }
        catch (RuntimeException e) {
            return Double.NaN;
        }
    }

    private double sum() {
        char op;
        double v = this.product();
        if (this.i < this.s.length() && this.s.charAt(this.i) == '%' && (this.i + 1 >= this.s.length() || this.s.charAt(this.i + 1) == ')')) {
            ++this.i;
            v /= 100.0;
        }
        while (this.i < this.s.length() && ((op = this.s.charAt(this.i)) == '+' || op == '-')) {
            ++this.i;
            double rhs = this.product();
            if (this.i < this.s.length() && this.s.charAt(this.i) == '%') {
                ++this.i;
                rhs = v * rhs / 100.0;
            }
            v = op == '+' ? v + rhs : v - rhs;
        }
        return v;
    }

    private double product() {
        char op;
        double v = this.power();
        while (this.i < this.s.length() && ((op = this.s.charAt(this.i)) == '*' || op == '/')) {
            ++this.i;
            double rhs = this.power();
            v = op == '*' ? v * rhs : v / rhs;
        }
        return v;
    }

    private double power() {
        double v = this.unary();
        if (this.i < this.s.length() && this.s.charAt(this.i) == '^') {
            ++this.i;
            return Math.pow(v, this.power());
        }
        return v;
    }

    private double unary() {
        if (this.i < this.s.length() && this.s.charAt(this.i) == '-') {
            ++this.i;
            return -this.unary();
        }
        if (this.i < this.s.length() && this.s.charAt(this.i) == 'r') {
            ++this.i;
            return Math.sqrt(this.unary());
        }
        double v = this.atom();
        if (this.i < this.s.length() && this.s.charAt(this.i) == '%' && this.i + 1 < this.s.length() && "+-)".indexOf(this.s.charAt(this.i + 1)) < 0) {
            ++this.i;
            v /= 100.0;
        }
        return v;
    }

    private double atom() {
        if (this.i < this.s.length() && this.s.charAt(this.i) == '(') {
            ++this.i;
            double v = this.sum();
            if (this.i < this.s.length() && this.s.charAt(this.i) == ')') {
                ++this.i;
            }
            return v;
        }
        int start = this.i;
        while (this.i < this.s.length() && (Character.isDigit(this.s.charAt(this.i)) || this.s.charAt(this.i) == '.')) {
            ++this.i;
        }
        if (start == this.i) {
            throw new IllegalArgumentException("number expected");
        }
        double v = Double.parseDouble(this.s.substring(start, this.i));
        if (this.i < this.s.length()) {
            switch (this.s.charAt(this.i)) {
                case 'k': {
                    v *= 1000.0;
                    ++this.i;
                    break;
                }
                case 'm': {
                    v *= 1000000.0;
                    ++this.i;
                    break;
                }
                case 'b': {
                    v *= 1.0E9;
                    ++this.i;
                    break;
                }
                case 't': {
                    v *= 1.0E12;
                    ++this.i;
                    break;
                }
            }
        }
        return v;
    }

    public static String shortForm(double v) {
        String num;
        int u;
        double a = Math.abs(v);
        String[] units = new String[]{"", "K", "M", "B", "T"};
        for (u = 0; a >= 1000.0 && u < units.length - 1; a /= 1000.0, ++u) {
        }
        String string = num = a >= 100.0 || u == 0 ? String.format(Locale.ROOT, "%.0f", a) : String.format(Locale.ROOT, "%.1f", a);
        if (num.endsWith(".0")) {
            num = num.substring(0, num.length() - 2);
        }
        return (v < 0.0 ? "-" : "") + num + units[u];
    }

    public static String plain(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            return "?";
        }
        if (v == Math.rint(v) && Math.abs(v) < 1.0E15) {
            return String.format(Locale.ROOT, "%,.0f", v).replace(',', '.');
        }
        String t = String.format(Locale.ROOT, "%.4f", v).replaceAll("0+$", "").replaceAll("\\.$", "");
        return t.replace('.', ',');
    }
}

