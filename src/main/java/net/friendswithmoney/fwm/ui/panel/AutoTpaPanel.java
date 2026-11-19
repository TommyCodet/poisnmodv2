/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package net.friendswithmoney.fwm.ui.panel;

import java.util.Locale;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.panel.RowsPanel;
import net.friendswithmoney.fwm.ui.widget.SliderRow;
import net.friendswithmoney.fwm.ui.widget.TextRow;
import net.friendswithmoney.fwm.ui.widget.ToggleRow;
import net.minecraft.network.chat.Component;

public class AutoTpaPanel
extends RowsPanel {
    @Override
    public Component title() {
        return Lang.of("fwm.tab.autotpa", new Object[0]);
    }

    @Override
    protected void buildRows() {
        FwmConfig config = FwmConfig.INSTANCE;
        this.rows.add(new ToggleRow((Component)Lang.of("fwm.autotpa.active", new Object[0]), () -> Features.isOn("fwm.tab.autotpa"), value -> Features.set("fwm.tab.autotpa", value)));
        this.rows.add(new ToggleRow((Component)Lang.of("fwm.autotpa.here", new Object[0]), () -> config.autoTpaHere, value -> {
            config.autoTpaHere = value;
        }));
        this.rows.add(new SliderRow((Component)Lang.of("fwm.autotpa.delay", new Object[0]), 0, 100, () -> config.autoTpaDelayTicks, value -> {
            config.autoTpaDelayTicks = value;
        }, ticks -> Component.literal((String)String.format(Locale.GERMANY, "%.1f s", (double)ticks / 20.0))));
        this.rows.add(new TextRow((Component)Lang.of("fwm.autotpa.count", config.autoTpaNames.size()), null));
        this.rows.add(new TextRow((Component)Lang.of("fwm.autotpa.hint", new Object[0]), null));
        this.rows.add(new TextRow((Component)Lang.of("fwm.autotpa.hint2", new Object[0]), null));
    }
}

