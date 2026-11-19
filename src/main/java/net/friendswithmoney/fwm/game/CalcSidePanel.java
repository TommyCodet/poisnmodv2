/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  org.joml.Matrix3x2fStack
 *  org.lwjgl.glfw.GLFW
 */
package net.friendswithmoney.fwm.game;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.game.Calc;
import net.friendswithmoney.fwm.mixin.HandledScreenAccessor;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.LauncherGfx;
import net.friendswithmoney.fwm.ui.ScreenScale;
import net.friendswithmoney.fwm.ui.gfx.FwmFont;
import net.friendswithmoney.fwm.ui.gfx.Shapes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

public final class CalcSidePanel {
    private static final int W = 96;
    private static final int H = 166;
    private static final int GAP = 7;
    private static final float KEY = 17.0f;
    private static final float PAD = 6.0f;
    private static final int MINT = -8523578;
    private static final int GOLD = -670398;
    private static final int DARK = -16045536;
    private static final int DIM = -7363684;
    private static final String[][] KEYS = new String[][]{{"\u232b", "C", "%", "\u00f7"}, {"7", "8", "9", "\u00d7"}, {"4", "5", "6", "\u2212"}, {"1", "2", "3", "+"}, {"\u221a", "0", ",", "="}};
    private static final String[] PILLS = new String[]{"k", "m", "b", "( )"};
    private static boolean focused;
    private static String pressed;
    private static long pressedAt;
    private static final float PILL_Y = 52.0f;
    private static final float PILL_H = 8.0f;
    private static final int WHITE = -1;

    private CalcSidePanel() {
    }

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (!(screen instanceof InventoryScreen)) {
                return;
            }
            InventoryScreen inv = (InventoryScreen)screen;
            ScreenEvents.afterExtract((Screen)screen).register((s, ctx, mx, my, delta) -> ScreenScale.run(s, ctx, mx, my, (ctx2, mx2, my2) -> CalcSidePanel.render(inv, ctx2, mx2, my2)));
            ScreenMouseEvents.allowMouseClick((Screen)screen).register((s, click) -> !CalcSidePanel.click(inv, click.x(), click.y(), click.button()));
            ScreenKeyboardEvents.allowKeyPress((Screen)screen).register((s, input) -> !CalcSidePanel.key(input.key(), input.scancode(), input.modifiers()));
            focused = false;
        });
    }

    private static float[] place(InventoryScreen inv) {
        int invX = ((HandledScreenAccessor)inv).fwm$x();
        int top = ((HandledScreenAccessor)inv).fwm$y();
        int sw = inv.width;
        boolean book = invX > (sw - 176) / 2 + 20;
        int right = invX - 7;
        if (book) {
            if (sw < 379) {
                return null;
            }
            int bookLeft = (sw - 147) / 2 - 86;
            right = bookLeft - 30 - 7;
        }
        float scale = 1.0f;
        if (right - 96 < 2) {
            scale = (float)(right - 2) / 96.0f;
        }
        if (scale < 0.55f) {
            return null;
        }
        return new float[]{(float)right - 96.0f * scale, (float)top + (166.0f - 166.0f * scale) / 2.0f, scale};
    }

    private static boolean shown(InventoryScreen inv) {
        return Features.isOn("fwm.tab.calc") && CalcSidePanel.place(inv) != null;
    }

    private static int[] tab(InventoryScreen inv) {
        return new int[]{((HandledScreenAccessor)inv).fwm$x() - 14, ((HandledScreenAccessor)inv).fwm$y() + 6, 12, 14};
    }

    private static boolean classic() {
        return V2.INSTANCE.designMinecraft || !FwmFont.enabled();
    }

    private static float keyX(int c) {
        return 6.0f + (float)c * 22.333334f;
    }

    private static float keyY(int r) {
        return 143.0f - (float)(KEYS.length - 1 - r) * 20.0f;
    }

    private static float pillW() {
        return 18.75f;
    }

    private static float pillX(int i) {
        return 6.0f + (float)i * (CalcSidePanel.pillW() + 3.0f);
    }

    private static float[] payBox() {
        float w = CalcSidePanel.width("/pay", 5.2f, true) + 9.0f;
        return new float[]{90.0f - w, 41.0f, w, 8.0f};
    }

    private static float[] headBox() {
        return new float[]{0.0f, 0.0f, 54.0f, 14.0f};
    }

    private static void render(InventoryScreen inv, GuiGraphicsExtractor ctx, int mx, int my) {
        float size;
        if (!Features.isOn("fwm.tab.calc")) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        Font font = client.font;
        float[] at = CalcSidePanel.place(inv);
        if (!V2.INSTANCE.calcOpen) {
            if (at == null) {
                return;
            }
            int[] t = CalcSidePanel.tab(inv);
            boolean overTab = CalcSidePanel.in((double)mx, (double)my, t);
            if (CalcSidePanel.classic()) {
                ctx.fill(t[0], t[1], t[0] + t[2], t[1] + t[3], overTab ? -299214260 : -870696398);
                ctx.text(font, (Component)Component.literal((String)"\u2211"), t[0] + 3, t[1] + 3, -2236963, false);
            } else {
                Shapes.round(ctx, t[0], t[1], t[2], t[3], 4.0f, overTab ? -266057150 : -434357708);
                Shapes.ring(ctx, t[0], t[1], t[2], t[3], 4.0f, 1434316998);
                CalcSidePanel.text(ctx, "\u2211", (float)t[0] + 3.2f, (float)(t[1] + 7) - 3.2f, -8523578, 6.6f, true);
            }
            return;
        }
        if (at == null) {
            return;
        }
        Matrix3x2fStack m = ctx.pose();
        m.pushMatrix();
        m.translate(at[0], at[1]);
        m.scale(at[2], at[2]);
        float lx = ((float)mx - at[0]) / at[2];
        float ly = ((float)my - at[1]) / at[2];
        boolean cl = CalcSidePanel.classic();
        if (cl) {
            ctx.fill(0, 0, 96, 166, -435152360);
            ctx.fill(0, 0, 96, 1, focused ? -8523578 : 0x55FFFFFF);
        } else {
            Shapes.shadow(ctx, 0.0f, 0.0f, 96.0f, 166.0f, 8.0f, 6.0f, 0x50000000);
            Shapes.roundGradient(ctx, 0.0f, 0.0f, 96.0f, 166.0f, 8.0f, -182699468, -183623651);
            Shapes.ring(ctx, 0.0f, 0.0f, 96.0f, 166.0f, 8.0f, focused ? -1719799610 : 1149104326);
        }
        CalcSidePanel.text(ctx, "\u2211 " + Lang.t("Rechner", "Calculator"), 6.0f, 4.0f, -1, 5.8f, true);
        if (!cl) {
            Shapes.circle(ctx, 86.5f, 7.0f, 3.5f, 0x22FFFFFF);
            Shapes.icon(ctx, "x", 84.0f, 4.5f, 5.0f, -3615023);
        }
        String input = V2.INSTANCE.calcInput;
        double v = Calc.eval(input);
        boolean bad = !input.isBlank() && Double.isNaN(v);
        String expr = input.isBlank() ? V2.INSTANCE.calcLast : input;
        expr = CalcSidePanel.fitLeft(expr + (focused && input.length() > 0 && System.currentTimeMillis() / 500L % 2L == 0L ? "_" : ""), 5.4f, 84.0f);
        CalcSidePanel.text(ctx, expr, 90.0f - CalcSidePanel.width(expr, 5.4f, false), 15.2f, -7363684, 5.4f, false);
        String big = input.isBlank() ? "0" : (bad ? Lang.t("Fehler", "Error") : Calc.plain(v));
        for (size = 12.5f; size > 6.0f && CalcSidePanel.width(big, size, false) > 84.0f; size -= 0.5f) {
        }
        CalcSidePanel.text(ctx, big, 90.0f - CalcSidePanel.width(big, size, false), 31.0f - size * 0.48f, bad ? -38037 : -1, size, false);
        float[] pay = CalcSidePanel.payBox();
        if (!input.isBlank() && !bad) {
            String sf = "\u2248 " + Calc.shortForm(v);
            float sw = CalcSidePanel.width(sf, 5.2f, true) + 9.0f;
            CalcSidePanel.pill(ctx, pay[0] - 3.0f - sw, pay[1], sw, pay[3], 780005574, sf, -8523578, cl);
        }
        boolean overPay = CalcSidePanel.in((double)lx, (double)ly, pay);
        CalcSidePanel.pill(ctx, pay[0], pay[1], pay[2], pay[3], overPay ? 0x3AFFFFFF : 0x1EFFFFFF, "/pay", -1, cl);
        for (int i = 0; i < PILLS.length; ++i) {
            float px = CalcSidePanel.pillX(i);
            boolean over = CalcSidePanel.in((double)lx, (double)ly, new float[]{px, 52.0f, CalcSidePanel.pillW(), 8.0f});
            boolean down = pressed.equals(PILLS[i]) && System.currentTimeMillis() - pressedAt < 130L;
            CalcSidePanel.pill(ctx, px, 52.0f, CalcSidePanel.pillW(), 8.0f, down ? 0x55FFFFFF : (over ? 0x2CFFFFFF : 0x16FFFFFF), PILLS[i], -3615023, cl);
        }
        String active = CalcSidePanel.activeOperator(input);
        for (int r = 0; r < KEYS.length; ++r) {
            for (int c = 0; c < KEYS[r].length; ++c) {
                int fg;
                int fill;
                boolean down;
                String k = KEYS[r][c];
                float kx = CalcSidePanel.keyX(c);
                float ky = CalcSidePanel.keyY(r);
                boolean over = lx >= kx && lx < kx + 17.0f && ly >= ky && ly < ky + 17.0f;
                boolean bl = down = pressed.equals(k) && System.currentTimeMillis() - pressedAt < 130L;
                if (k.equals("=")) {
                    fill = -670398;
                    fg = -16045536;
                } else if ("\u00f7\u00d7\u2212+".contains(k)) {
                    boolean on = k.equals(active);
                    fill = on ? -15851491 : -8523578;
                    fg = on ? -8523578 : -16045536;
                } else if ("\u232bC%\u221a".contains(k)) {
                    fill = 780005574;
                    fg = -8523578;
                } else {
                    fill = 0x24FFFFFF;
                    fg = -1;
                }
                if (over) {
                    fill = CalcSidePanel.lighten(fill);
                }
                if (down) {
                    fill = CalcSidePanel.darken(fill);
                }
                if (cl) {
                    ctx.fill(Math.round(kx), Math.round(ky), Math.round(kx + 17.0f), Math.round(ky + 17.0f), CalcSidePanel.opaque(fill));
                    ctx.text(font, (Component)Component.literal((String)k), Math.round(kx + (17.0f - (float)font.width(k)) / 2.0f), Math.round(ky + 5.0f), fg, false);
                    continue;
                }
                float inset = down ? 0.6f : 0.0f;
                Shapes.circle(ctx, kx + 8.5f, ky + 8.5f, 8.5f - inset, fill);
                if (k.equals(active)) {
                    Shapes.ring(ctx, kx, ky, 17.0f, 17.0f, 8.5f, -8523578);
                }
                float ts = Character.isDigit(k.charAt(0)) ? 8.0f : ("\u00f7\u00d7\u2212+=".contains(k) ? 9.0f : 7.6f);
                boolean bold = !Character.isDigit(k.charAt(0));
                CalcSidePanel.text(ctx, k, kx + (17.0f - CalcSidePanel.width(k, ts, bold)) / 2.0f, ky + 8.5f - ts * 0.48f, fg, ts, bold);
            }
        }
        m.popMatrix();
    }

    private static void pill(GuiGraphicsExtractor ctx, float x, float y, float w, float h, int fill, String label, int fg, boolean cl) {
        if (cl) {
            ctx.fill(Math.round(x), Math.round(y), Math.round(x + w), Math.round(y + h), CalcSidePanel.opaque(fill));
        } else {
            Shapes.round(ctx, x, y, w, h, h / 2.0f, fill);
        }
        float ts = 5.2f;
        CalcSidePanel.text(ctx, label, x + (w - CalcSidePanel.width(label, ts, true)) / 2.0f, y + h / 2.0f - ts * 0.48f, fg, ts, true);
    }

    private static String activeOperator(String input) {
        if (input.isEmpty()) {
            return "";
        }
        String last = input.substring(input.length() - 1);
        return "\u00f7\u00d7\u2212+".contains(last) ? last : "";
    }

    private static int lighten(int argb) {
        int a = argb >>> 24;
        if (a < 192) {
            return Math.min(255, a + 24) << 24 | argb & 0xFFFFFF;
        }
        return Shapes.mix(argb, -1, 0.18f);
    }

    private static int darken(int argb) {
        return Shapes.mix(argb, -16777216, 0.22f) | argb & 0xFF000000;
    }

    private static int opaque(int argb) {
        float a = (float)(argb >>> 24) / 255.0f;
        return Shapes.mix(-15721960, argb | 0xFF000000, a) | 0xFF000000;
    }

    private static void text(GuiGraphicsExtractor ctx, String s, float x, float y, int color, float size, boolean bold) {
        if (CalcSidePanel.classic()) {
            Font font = Minecraft.getInstance().font;
            Matrix3x2fStack m = ctx.pose();
            m.pushMatrix();
            m.translate(x, y + size * 0.48f - 4.0f);
            ctx.text(font, (Component)(bold ? Component.literal((String)s).withStyle(ChatFormatting.BOLD) : Component.literal((String)s)), 0, 0, color, false);
            m.popMatrix();
            return;
        }
        LauncherGfx.text(ctx, s, x, y, color, size, bold);
    }

    private static float width(String s, float size, boolean bold) {
        if (CalcSidePanel.classic()) {
            Font font = Minecraft.getInstance().font;
            return bold ? (float)font.width((FormattedText)Component.literal((String)s).withStyle(ChatFormatting.BOLD)) : (float)font.width(s);
        }
        return LauncherGfx.width(s, size, bold);
    }

    private static String fitLeft(String s, float size, float w) {
        if (CalcSidePanel.width(s, size, false) <= w) {
            return s;
        }
        String t = s;
        while (t.length() > 1 && CalcSidePanel.width("\u2026" + t, size, false) > w) {
            t = t.substring(1);
        }
        return "\u2026" + t;
    }

    private static boolean in(double mx, double my, int[] b) {
        return mx >= (double)b[0] && mx < (double)(b[0] + b[2]) && my >= (double)b[1] && my < (double)(b[1] + b[3]);
    }

    private static boolean in(double mx, double my, float[] b) {
        return mx >= (double)b[0] && mx < (double)(b[0] + b[2]) && my >= (double)b[1] && my < (double)(b[1] + b[3]);
    }

    private static boolean click(InventoryScreen inv, double mx, double my, int button) {
        if (!CalcSidePanel.shown(inv) || button != 0) {
            return false;
        }
        if (!V2.INSTANCE.calcOpen) {
            if (CalcSidePanel.in(mx, my, CalcSidePanel.tab(inv))) {
                V2.INSTANCE.calcOpen = true;
                focused = true;
                V2.save();
                return true;
            }
            return false;
        }
        float[] at = CalcSidePanel.place(inv);
        float lx = (float)((mx - (double)at[0]) / (double)at[2]);
        float ly = (float)((my - (double)at[1]) / (double)at[2]);
        if (lx < 0.0f || lx >= 96.0f || ly < 0.0f || ly >= 166.0f) {
            focused = false;
            return false;
        }
        focused = true;
        if (CalcSidePanel.in((double)lx, (double)ly, CalcSidePanel.headBox()) || lx >= 82.0f && ly < 13.0f) {
            V2.INSTANCE.calcOpen = false;
            focused = false;
            V2.save();
            return true;
        }
        if (CalcSidePanel.in((double)lx, (double)ly, CalcSidePanel.payBox())) {
            CalcSidePanel.press("\u2398");
            return true;
        }
        for (int i = 0; i < PILLS.length; ++i) {
            if (!CalcSidePanel.in((double)lx, (double)ly, new float[]{CalcSidePanel.pillX(i), 52.0f, CalcSidePanel.pillW(), 8.0f})) continue;
            CalcSidePanel.press(PILLS[i]);
            return true;
        }
        for (int r = 0; r < KEYS.length; ++r) {
            for (int c = 0; c < KEYS[r].length; ++c) {
                float kx = CalcSidePanel.keyX(c);
                float ky = CalcSidePanel.keyY(r);
                if (!(lx >= kx) || !(lx < kx + 17.0f) || !(ly >= ky) || !(ly < ky + 17.0f)) continue;
                CalcSidePanel.press(KEYS[r][c]);
                return true;
            }
        }
        return true;
    }

    private static void press(String k) {
        V2 c = V2.INSTANCE;
        pressed = k;
        pressedAt = System.currentTimeMillis();
        switch (k) {
            case "( )": {
                boolean close;
                int open = 0;
                for (char ch : c.calcInput.toCharArray()) {
                    open += ch == '(' ? 1 : (ch == ')' ? -1 : 0);
                }
                char last = c.calcInput.isEmpty() ? (char)' ' : c.calcInput.charAt(c.calcInput.length() - 1);
                boolean bl = close = open > 0 && (Character.isDigit(last) || last == ')' || "kmbt%".indexOf(last) >= 0);
                if (c.calcInput.length() >= 60) break;
                c.calcInput = c.calcInput + (close ? ")" : "(");
                break;
            }
            case "C": {
                c.calcInput = "";
                break;
            }
            case "\u232b": {
                c.calcInput = c.calcInput.isEmpty() ? "" : c.calcInput.substring(0, c.calcInput.length() - 1);
                break;
            }
            case "=": {
                double v = Calc.eval(c.calcInput);
                if (Double.isNaN(v)) break;
                c.calcLast = c.calcInput + " = " + Calc.plain(v);
                c.calcInput = Calc.plain(v).replace(".", "");
                break;
            }
            case "\u2398": {
                double v = Calc.eval(c.calcInput);
                if (Double.isNaN(v)) break;
                Minecraft.getInstance().keyboardHandler.setClipboard(String.valueOf(Math.round(v)));
                break;
            }
            case "\u221a": {
                c.calcInput = c.calcInput + "\u221a";
                break;
            }
            default: {
                if (c.calcInput.length() >= 60) break;
                c.calcInput = c.calcInput + k;
            }
        }
        V2.save();
    }

    private static boolean key(int key, int scancode, int modifiers) {
        boolean ctrl;
        if (!(focused && V2.INSTANCE.calcOpen && Features.isOn("fwm.tab.calc"))) {
            return false;
        }
        boolean shift = (modifiers & 1) != 0;
        boolean bl = ctrl = (modifiers & 2) != 0;
        if (ctrl) {
            String name = GLFW.glfwGetKeyName((int)key, (int)scancode);
            if (name != null && name.equalsIgnoreCase("c")) {
                CalcSidePanel.press("\u2398");
            }
            return true;
        }
        switch (key) {
            case 256: {
                focused = false;
                return true;
            }
            case 259: {
                CalcSidePanel.press("\u232b");
                return true;
            }
            case 261: {
                CalcSidePanel.press("C");
                return true;
            }
            case 257: 
            case 335: {
                CalcSidePanel.press("=");
                return true;
            }
            case 334: {
                CalcSidePanel.press("+");
                return true;
            }
            case 333: {
                CalcSidePanel.press("\u2212");
                return true;
            }
            case 332: {
                CalcSidePanel.press("\u00d7");
                return true;
            }
            case 331: {
                CalcSidePanel.press("\u00f7");
                return true;
            }
            case 330: {
                CalcSidePanel.press(",");
                return true;
            }
        }
        if (key >= 320 && key <= 329) {
            CalcSidePanel.press(String.valueOf(key - 320));
            return true;
        }
        String name = GLFW.glfwGetKeyName((int)key, (int)scancode);
        if (name == null || name.isEmpty()) {
            return true;
        }
        char ch = name.charAt(0);
        if (shift) {
            switch (ch) {
                case '+': {
                    ch = '*';
                    break;
                }
                case '=': {
                    ch = '+';
                    break;
                }
                case '8': {
                    ch = '(';
                    break;
                }
                case '9': {
                    ch = ')';
                    break;
                }
                case '7': {
                    ch = '/';
                    break;
                }
                case '5': {
                    ch = '%';
                    break;
                }
            }
        }
        if (Character.isDigit(ch) || "+-*/().,%kmbt^x".indexOf(Character.toLowerCase(ch)) >= 0) {
            String add;
            switch (Character.toLowerCase(ch)) {
                case '*': 
                case 'x': {
                    String string = "\u00d7";
                    break;
                }
                case '/': {
                    String string = "\u00f7";
                    break;
                }
                case '-': {
                    String string = "\u2212";
                    break;
                }
                case '.': {
                    String string = ",";
                    break;
                }
                default: {
                    String string = add = String.valueOf(Character.toLowerCase(ch));
                }
            }
            if (V2.INSTANCE.calcInput.length() < 60) {
                V2.INSTANCE.calcInput = V2.INSTANCE.calcInput + add;
            }
            pressed = add;
            pressedAt = System.currentTimeMillis();
            V2.save();
        }
        return true;
    }

    static {
        pressed = "";
    }
}

