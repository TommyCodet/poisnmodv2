/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 */
package net.friendswithmoney.fwm.ui.widget;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.friendswithmoney.fwm.ui.FwmTheme;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.LauncherGfx;
import net.friendswithmoney.fwm.ui.widget.Anim;
import net.friendswithmoney.fwm.ui.widget.Row;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;

public class ToggleRow
extends Row {
    private static final int MAX_PILL_W = 46;
    private static final int PILL_H = 14;
    private static final int KNOB = 10;
    private final BooleanSupplier getter;
    private final Consumer<Boolean> setter;

    public ToggleRow(Component label, BooleanSupplier getter, Consumer<Boolean> setter) {
        super(label);
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    protected void renderControl(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY) {
        boolean on = this.getter.getAsBoolean();
        int pillW = Math.min(46, this.controlWidth());
        int px = this.controlRight() - pillW;
        int py = this.y + 3;
        float t = Anim.ease(Anim.toward("tg" + System.identityHashCode(this), on, 110));
        float sx = this.controlRight() - 22;
        float sy = (float)this.y + 4.0f;
        LauncherGfx.toggle(ctx, sx, sy, t, -8523578, 0x40FFFFFF);
        MutableComponent state = Lang.of(on ? "fwm.state.on" : "fwm.state.off", new Object[0]);
        ctx.text(font, (Component)state, (int)sx - 5 - font.width((FormattedText)state), this.textY(font), on ? FwmTheme.TEXT : FwmTheme.TEXT_DIM, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !this.isHovered(mouseX, mouseY)) {
            return false;
        }
        this.setter.accept(!this.getter.getAsBoolean());
        return true;
    }
}

