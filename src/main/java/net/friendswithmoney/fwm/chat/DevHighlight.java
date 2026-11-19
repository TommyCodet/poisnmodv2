/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.chat.GuiMessageTag
 *  net.minecraft.network.chat.Component
 */
package net.friendswithmoney.fwm.chat;

import net.friendswithmoney.fwm.chat.RealNames;
import net.friendswithmoney.fwm.net.ServerContext;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;

public final class DevHighlight {
    public static final String NAME = "HydraVB";
    public static final int GOLD = -2841289;
    private static final int TINT_ALPHA = 32;
    public static final GuiMessageTag INDICATOR = new GuiMessageTag(13935927, null, (Component)Component.literal((String)"FWM Mod Developer"), "FWM");

    private DevHighlight() {
    }

    public static boolean fromDev(Component message) {
        if (message == null) {
            return false;
        }
        if (!ServerContext.hugo()) {
            return false;
        }
        return NAME.equals(RealNames.speaker(message.getString()));
    }

    public static int tint(float opacity) {
        int a = Math.round(32.0f * Math.max(0.0f, Math.min(1.0f, opacity)));
        return a << 24 | 0xD4A537;
    }
}

