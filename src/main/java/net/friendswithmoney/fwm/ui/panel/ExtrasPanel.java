/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package net.friendswithmoney.fwm.ui.panel;

import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.panel.RowsPanel;
import net.friendswithmoney.fwm.ui.widget.SliderRow;
import net.friendswithmoney.fwm.ui.widget.ToggleRow;
import net.minecraft.network.chat.Component;

public class ExtrasPanel
extends RowsPanel {
    private static final int MIN_TPA_SECONDS = 15;
    private static final int MAX_TPA_SECONDS = 300;

    @Override
    public Component title() {
        return Lang.of("fwm.tab.extras", new Object[0]);
    }

    @Override
    protected void buildRows() {
        this.toggle("fwm.feature.unitprice");
        this.toggle("fwm.feature.ahprice");
        this.toggle("fwm.feature.realname");
        this.toggle("fwm.feature.pricetrend");
        this.toggle("fwm.feature.tpawarn");
        if (Features.allowedHere("fwm.feature.tpawarn")) {
            this.rows.add(new SliderRow((Component)Lang.of("fwm.tpa.timeout", new Object[0]), 15, 300, () -> FwmConfig.INSTANCE.tpaTimeoutSeconds, value -> {
                FwmConfig.INSTANCE.tpaTimeoutSeconds = value;
            }, seconds -> Lang.of("fwm.value.seconds", seconds)));
        }
    }

    private void toggle(String feature) {
        if (!Features.allowedHere(feature)) {
            return;
        }
        this.rows.add(new ToggleRow((Component)Lang.of(feature, new Object[0]), () -> Features.isOn(feature), value -> Features.set(feature, value)));
        if (feature.equals("fwm.feature.ahprice") && Features.allowedHere("fwm.tab.spawner")) {
            this.rows.add(new ToggleRow((Component)Component.literal((String)Lang.t("Spawnerpreis im Tooltip", "Spawner price in the tooltip")), () -> V2.INSTANCE.spawnerTooltip, value -> {
                V2.INSTANCE.spawnerTooltip = value;
                V2.save();
            }));
        }
        V2 v = V2.INSTANCE;
        if (feature.equals("fwm.feature.pricetrend")) {
            this.rows.add(new ToggleRow((Component)Component.literal((String)Lang.t("Mit Shift: 7-Tage-Graph", "With Shift: 7-day graph")), () -> v.trendGraph, value -> {
                v.trendGraph = value;
                V2.save();
            }));
        }
    }
}

