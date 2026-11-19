/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package net.friendswithmoney.fwm.ui.panel;

import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.input.Keybinds;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.panel.ItemPanel;
import net.friendswithmoney.fwm.ui.panel.PanelHost;
import net.friendswithmoney.fwm.ui.panel.RowsPanel;
import net.friendswithmoney.fwm.ui.panel.SellmacroTuningPanel;
import net.friendswithmoney.fwm.ui.widget.ButtonRow;
import net.friendswithmoney.fwm.ui.widget.KeybindRow;
import net.friendswithmoney.fwm.ui.widget.ToggleRow;
import net.minecraft.network.chat.Component;

public class SellmacroPanel
extends RowsPanel {
    private final PanelHost host;

    public SellmacroPanel(PanelHost host) {
        this.host = host;
    }

    @Override
    public Component title() {
        return Lang.of("fwm.menu.title", new Object[0]);
    }

    @Override
    protected void buildRows() {
        FwmConfig config = FwmConfig.INSTANCE;
        this.rows.add(new ToggleRow((Component)Lang.of("fwm.option.enabled", new Object[0]), () -> config.enabled, value -> {
            config.enabled = value;
        }));
        this.rows.add(new ButtonRow((Component)Lang.of("fwm.option.items", new Object[0]), () -> Lang.of("fwm.option.itemsCount", config.whitelistSize()), () -> this.host.openSub(new ItemPanel(this.host))));
        this.rows.add(new KeybindRow((Component)Lang.of("fwm.option.keyToggle", new Object[0]), Keybinds.TOGGLE));
        this.rows.add(new ButtonRow((Component)Lang.of("fwm.option.tuningOpen", new Object[0]), () -> Lang.of("fwm.option.open", new Object[0]), () -> this.host.openSub(new SellmacroTuningPanel())));
    }
}

