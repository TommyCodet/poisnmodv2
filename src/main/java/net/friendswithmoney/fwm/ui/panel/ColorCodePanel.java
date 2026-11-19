/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.locale.Language
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.Style
 *  net.minecraft.network.chat.TextColor
 *  net.minecraft.util.FormattedCharSequence
 */
package net.friendswithmoney.fwm.ui.panel;

import java.util.List;
import java.util.Objects;
import net.friendswithmoney.fwm.colorcode.CodeStyle;
import net.friendswithmoney.fwm.colorcode.ColorCode;
import net.friendswithmoney.fwm.ui.FwmTheme;
import net.friendswithmoney.fwm.ui.gfx.Tooltip;
import net.friendswithmoney.fwm.ui.panel.ColorCodeEditorPanel;
import net.friendswithmoney.fwm.ui.panel.ColorCodeTemplatesPanel;
import net.friendswithmoney.fwm.ui.panel.PanelHost;
import net.friendswithmoney.fwm.ui.panel.TabbedPanel;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;

public class ColorCodePanel
extends TabbedPanel {
    private static final User SESSION = new User();

    public ColorCodePanel(PanelHost host) {
        super("fwm.tab.colorcodes", List.of(new TabbedPanel.Tab("fwm.colorcodes.editor", () -> new ColorCodeEditorPanel(host, SESSION)), new TabbedPanel.Tab("fwm.colorcodes.templates", () -> new ColorCodeTemplatesPanel(SESSION))));
        ColorCodePanel.SESSION.owner = this;
    }

    static Style style(CodeStyle s, boolean withBold) {
        return Style.EMPTY.withColor(TextColor.fromRgb((int)ColorCode.rgb(s.color()))).withBold(Boolean.valueOf(withBold && s.bold())).withUnderlined(Boolean.valueOf(s.underline())).withItalic(Boolean.valueOf(s.italic())).withStrikethrough(Boolean.valueOf(s.strike())).withObfuscated(Boolean.valueOf(s.obf()));
    }

    static MutableComponent text(ColorCode code) {
        MutableComponent out = Component.empty();
        for (ColorCode.Run run : code.runs()) {
            out.append((Component)Component.literal((String)run.text()).setStyle(ColorCodePanel.style(run.style(), true)));
        }
        return out;
    }

    static FormattedCharSequence trimmed(Font font, Component text, int width) {
        return Language.getInstance().getVisualOrder(font.substrByWidth((FormattedText)text, Math.max(0, width)));
    }

    static void button(GuiGraphicsExtractor ctx, Font font, int bx, int by, int bw, int bh, Component label, boolean hovered, boolean active, boolean enabled) {
        int fill;
        int n = !enabled ? FwmTheme.LOCKED : (fill = active ? FwmTheme.ACCENT_DIM : FwmTheme.RAISED);
        int edge = !enabled ? FwmTheme.OFF : (hovered || active ? FwmTheme.ACCENT : FwmTheme.OFF);
        FwmTheme.box(ctx, bx, by, bw, bh, fill, edge);
        FormattedCharSequence line = ColorCodePanel.trimmed(font, label, bw - 4);
        int color = !enabled ? FwmTheme.TEXT_DIM : (hovered ? FwmTheme.ACCENT : FwmTheme.TEXT);
        int n2 = bx + (bw - font.width(line)) / 2;
        Objects.requireNonNull(font);
        ctx.text(font, line, n2, by + (bh - 9) / 2 + 1, color, false);
    }

    static boolean over(double mx, double my, int bx, int by, int bw, int bh) {
        return mx >= (double)bx && mx < (double)(bx + bw) && my >= (double)by && my < (double)(by + bh);
    }

    static void tip(GuiGraphicsExtractor ctx, Font font, String text, int mouseX, int mouseY) {
        Tooltip.draw(ctx, font, text, mouseX, mouseY);
    }

    static void tooltipBox(GuiGraphicsExtractor ctx, int bx, int by, int bw, int bh) {
        Tooltip.box(ctx, bx, by, bw, bh);
    }

    static final class User {
        final ColorCode code = new ColorCode();
        ColorCode.Mode mode = ColorCode.Mode.RENAME;
        int gradientFrom = 16727471;
        int gradientTo = 0x55FFFF;
        int version;
        ColorCodePanel owner;

        User() {
        }

        void load(ColorCode content, ColorCode.Mode newMode) {
            this.code.set(content);
            if (newMode != null) {
                this.mode = newMode;
            }
            ++this.version;
        }
    }
}

