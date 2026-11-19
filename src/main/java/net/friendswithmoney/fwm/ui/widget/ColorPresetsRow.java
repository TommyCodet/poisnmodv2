/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.network.chat.Component
 */
package net.friendswithmoney.fwm.ui.widget;

import net.friendswithmoney.fwm.config.ConfigManager;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.ui.FwmPalette;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.LauncherGfx;
import net.friendswithmoney.fwm.ui.widget.Row;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class ColorPresetsRow
extends Row {
    private static final Preset[] PRESETS = new Preset[]{new Preset("Gr\u00fcn-Wei\u00df", "Green-white", -1.0f, 0.0f, -1.0f, 1.0f), new Preset("Gold", "Gold", 0.13f, 0.45f, 0.12f, 1.0f), new Preset("Rot-Wei\u00df", "Red-white", 0.0f, 0.12f, 0.99f, 1.0f), new Preset("Blau-Lila", "Blue-purple", 0.6f, 0.45f, 0.77f, 1.0f), new Preset("T\u00fcrkis", "Turquoise", 0.5f, 0.35f, 0.48f, 1.0f), new Preset("Rosa", "Pink", 0.92f, 0.35f, 0.9f, 1.0f)};

    public ColorPresetsRow() {
        super((Component)Component.literal((String)""));
    }

    private int chipW() {
        return (this.width - 2 * this.padding() - 20) / 6;
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY) {
        this.renderControl(ctx, font, mouseX, mouseY);
    }

    @Override
    protected void renderControl(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY) {
        FwmConfig c = FwmConfig.INSTANCE;
        int cw = this.chipW();
        for (int i = 0; i < PRESETS.length; ++i) {
            boolean over;
            Preset p = PRESETS[i];
            int cx = this.x + this.padding() + i * (cw + 4);
            int cy = this.y + 2;
            int ch = 16;
            int from = FwmPalette.tintAt(p.tintHue(), p.tintSat());
            int to = FwmPalette.accentAt(p.menuHue(), p.menuSat());
            for (int k = 0; k < cw; ++k) {
                float t = (float)k / (float)Math.max(1, cw - 1);
                ctx.fill(cx + k, cy, cx + k + 1, cy + ch, ColorPresetsRow.mix(from, to, t));
            }
            boolean on = Math.abs(c.tintHue - p.tintHue()) < 0.01f && Math.abs(c.tintSat - p.tintSat()) < 0.01f && Math.abs(c.menuHue - p.menuHue()) < 0.01f && Math.abs(c.menuSat - p.menuSat()) < 0.01f;
            boolean bl = over = mouseX >= cx && mouseX < cx + cw && mouseY >= cy && mouseY < cy + ch;
            LauncherGfx.ring(ctx, cx - 1, cy - 1, cw + 2, ch + 2, 3, on ? -1 : (over ? -1426063361 : 0x33000000));
            String name = LauncherGfx.trim(font, Lang.t(p.de(), p.en()), cw - 4);
            ctx.text(font, name, cx + (cw - font.width(name)) / 2, cy + (ch - 8) / 2, -16049642, false);
        }
    }

    private static int mix(int a, int b, float t) {
        int r = Math.round((float)(a >> 16 & 0xFF) * (1.0f - t) + (float)(b >> 16 & 0xFF) * t);
        int g = Math.round((float)(a >> 8 & 0xFF) * (1.0f - t) + (float)(b >> 8 & 0xFF) * t);
        int bl = Math.round((float)(a & 0xFF) * (1.0f - t) + (float)(b & 0xFF) * t);
        return 0xFF000000 | r << 16 | g << 8 | bl;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || !this.isHovered(mouseX, mouseY)) {
            return false;
        }
        int cw = this.chipW();
        for (int i = 0; i < PRESETS.length; ++i) {
            int cx = this.x + this.padding() + i * (cw + 4);
            if (mouseX < (double)cx || mouseX >= (double)(cx + cw)) continue;
            Preset p = PRESETS[i];
            FwmConfig c = FwmConfig.INSTANCE;
            c.tintHue = p.tintHue();
            c.tintSat = p.tintSat();
            c.menuHue = p.menuHue();
            c.menuSat = p.menuSat();
            FwmPalette.refresh();
            ConfigManager.save();
            return true;
        }
        return false;
    }

    private record Preset(String de, String en, float tintHue, float tintSat, float menuHue, float menuSat) {
    }
}

