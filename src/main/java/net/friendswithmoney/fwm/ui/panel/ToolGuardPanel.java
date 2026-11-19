/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package net.friendswithmoney.fwm.ui.panel;

import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.panel.RowsPanel;
import net.friendswithmoney.fwm.ui.widget.SliderRow;
import net.friendswithmoney.fwm.ui.widget.TextRow;
import net.friendswithmoney.fwm.ui.widget.ToggleRow;
import net.minecraft.network.chat.Component;

public class ToolGuardPanel
extends RowsPanel {
    @Override
    public Component title() {
        return Lang.of("fwm.tab.toolguard", new Object[0]);
    }

    @Override
    protected void buildRows() {
        FwmConfig config = FwmConfig.INSTANCE;
        this.rows.add(new ToggleRow((Component)Lang.of("fwm.toolguard.active", new Object[0]), () -> Features.isOn("fwm.tab.toolguard"), value -> Features.set("fwm.tab.toolguard", value)));
        this.rows.add(new SliderRow((Component)Lang.of("fwm.toolguard.threshold", new Object[0]), 1, 50, () -> config.toolGuardPercent, value -> {
            config.toolGuardPercent = value;
        }, percent -> Lang.of("fwm.value.percent", percent)));
        this.rows.add(new ToggleRow((Component)Lang.of("fwm.toolguard.warning", new Object[0]), () -> config.toolGuardWarning, value -> {
            config.toolGuardWarning = value;
        }));
        this.rows.add(new ToggleRow((Component)Lang.of("fwm.toolguard.block", new Object[0]), () -> config.toolGuardBlock, value -> {
            config.toolGuardBlock = value;
        }));
        this.rows.add(new TextRow((Component)Lang.of("fwm.toolguard.blockHint", new Object[0]), null));
        this.rows.add(new TextRow((Component)Lang.of("fwm.toolguard.blockHint2", new Object[0]), null));
    }
}

