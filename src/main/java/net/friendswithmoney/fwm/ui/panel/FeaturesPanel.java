/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package net.friendswithmoney.fwm.ui.panel;

import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.panel.RowsPanel;
import net.friendswithmoney.fwm.ui.widget.ToggleRow;
import net.minecraft.network.chat.Component;

public class FeaturesPanel
extends RowsPanel {
    @Override
    public Component title() {
        return Lang.of("fwm.tab.features", new Object[0]);
    }

    @Override
    protected void buildRows() {
        for (String key : Features.SWITCHABLE) {
            if (!Features.canSwitch(key)) continue;
            this.rows.add(new ToggleRow((Component)Lang.of(key, new Object[0]), () -> Features.isOn(key), value -> Features.set(key, value)));
        }
    }
}

