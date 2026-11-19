/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package net.friendswithmoney.fwm.ui.panel;

import java.util.List;
import net.friendswithmoney.fwm.config.ConfigManager;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.panel.RowsPanel;
import net.friendswithmoney.fwm.ui.widget.TextRow;
import net.friendswithmoney.fwm.ui.widget.ToggleRow;
import net.minecraft.network.chat.Component;

public class SidebarPanel
extends RowsPanel {
    private final List<String> keys;

    public SidebarPanel(List<String> keys) {
        this.keys = keys;
    }

    @Override
    public Component title() {
        return Lang.of("fwm.sidebar.title", new Object[0]);
    }

    @Override
    protected void buildRows() {
        this.rows.add(new TextRow((Component)Lang.of("fwm.sidebar.hint", new Object[0]), null));
        FwmConfig config = FwmConfig.INSTANCE;
        for (String key : this.keys) {
            this.rows.add(new ToggleRow((Component)Lang.of(key, new Object[0]), () -> !config.sidebarHidden.contains(key), value -> {
                if (value.booleanValue()) {
                    config.sidebarHidden.remove(key);
                } else if (!config.sidebarHidden.contains(key)) {
                    config.sidebarHidden.add(key);
                }
                ConfigManager.save();
            }));
        }
    }
}

