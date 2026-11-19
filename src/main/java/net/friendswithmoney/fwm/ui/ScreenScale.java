/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 */
package net.friendswithmoney.fwm.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

public final class ScreenScale {
    private ScreenScale() {
    }

    public static float factor(Screen screen, GuiGraphicsExtractor ctx) {
        if (screen == null || screen.width <= 0) {
            return 1.0f;
        }
        float k = (float)ctx.guiWidth() / (float)screen.width;
        return Float.isFinite(k) && k > 0.05f && k < 20.0f ? k : 1.0f;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void run(Screen screen, GuiGraphicsExtractor ctx, int mouseX, int mouseY, Draw draw) {
        float k = ScreenScale.factor(screen, ctx);
        if (Math.abs(k - 1.0f) < 0.005f) {
            draw.draw(ctx, mouseX, mouseY);
            return;
        }
        ctx.pose().pushMatrix();
        ctx.pose().scale(k, k);
        try {
            draw.draw(ctx, Math.round((float)mouseX / k), Math.round((float)mouseY / k));
        }
        finally {
            ctx.pose().popMatrix();
        }
    }

    public static interface Draw {
        public void draw(GuiGraphicsExtractor var1, int var2, int var3);
    }
}

