/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package net.friendswithmoney.fwm.ui.panel;

import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.input.Keybinds;
import net.friendswithmoney.fwm.lyrics.AfkCinema;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.panel.RowsPanel;
import net.friendswithmoney.fwm.ui.widget.KeybindRow;
import net.minecraft.network.chat.Component;

public class KeysPanel
extends RowsPanel {
    @Override
    public Component title() {
        return Lang.of("fwm.tab.keys", new Object[0]);
    }

    @Override
    protected void buildRows() {
        if (Features.allowedHere("fwm.tab.sellmacro")) {
            this.rows.add(new KeybindRow((Component)Lang.of("fwm.option.keyToggle", new Object[0]), Keybinds.TOGGLE));
        }
        this.rows.add(new KeybindRow((Component)Lang.of("fwm.option.keyMenu", new Object[0]), Keybinds.OPEN_MENU, false));
        if (Features.allowedHere("fwm.tab.messages")) {
            this.rows.add(new KeybindRow((Component)Lang.of("fwm.option.keyMessages", new Object[0]), Keybinds.OPEN_MESSAGES));
        }
        if (Features.allowedHere("fwm.tab.auction")) {
            this.rows.add(new KeybindRow((Component)Lang.of("fwm.option.keyAuction", new Object[0]), Keybinds.OPEN_AUCTION));
        }
        if (Features.allowedHere("fwm.tab.giveaway")) {
            this.rows.add(new KeybindRow((Component)Lang.of("fwm.option.keyGiveaway", new Object[0]), Keybinds.OPEN_GIVEAWAY));
        }
        if (AfkCinema.toggleKey() != null) {
            this.rows.add(new KeybindRow((Component)Component.literal((String)Lang.t("Kamerafahrt an/aus", "Camera ride on/off")), AfkCinema.toggleKey()));
        }
    }
}

