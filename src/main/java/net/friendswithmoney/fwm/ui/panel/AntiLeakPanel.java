/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.chat.Component
 */
package net.friendswithmoney.fwm.ui.panel;

import net.friendswithmoney.fwm.antileak.AntiLeak;
import net.friendswithmoney.fwm.config.ConfigManager;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.panel.RowsPanel;
import net.friendswithmoney.fwm.ui.widget.ButtonRow;
import net.friendswithmoney.fwm.ui.widget.TextRow;
import net.friendswithmoney.fwm.ui.widget.ToggleRow;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class AntiLeakPanel
extends RowsPanel {
    @Override
    public Component title() {
        return Lang.of("fwm.tab.antileak", new Object[0]);
    }

    @Override
    protected void buildRows() {
        this.rows.add(new TextRow((Component)Lang.of("fwm.antileak.intro", new Object[0]), null));
        this.rows.add(new ToggleRow((Component)Lang.of("fwm.extras.active", new Object[0]), () -> Features.isOn("fwm.feature.antileak"), value -> Features.set("fwm.feature.antileak", value)));
        this.rows.add(new ToggleRow((Component)Lang.of("fwm.antileak.coords", new Object[0]), () -> FwmConfig.INSTANCE.antiLeakCoords, value -> {
            FwmConfig.INSTANCE.antiLeakCoords = value;
            ConfigManager.save();
        }));
        this.rows.add(new TextRow((Component)Lang.of("fwm.antileak.hide", new Object[0])));
        this.rows.add(new ToggleRow((Component)Lang.of("fwm.antileak.bedrock", new Object[0]), () -> FwmConfig.INSTANCE.antiLeakBedrock, value -> {
            FwmConfig.INSTANCE.antiLeakBedrock = value;
        }));
        this.rows.add(new ToggleRow((Component)Lang.of("fwm.antileak.ores", new Object[0]), () -> FwmConfig.INSTANCE.antiLeakOres, value -> {
            FwmConfig.INSTANCE.antiLeakOres = value;
        }));
        this.rows.add(new ToggleRow((Component)Lang.of("fwm.antileak.rotation", new Object[0]), () -> FwmConfig.INSTANCE.antiLeakRotation, value -> {
            FwmConfig.INSTANCE.antiLeakRotation = value;
        }));
        this.rows.add(new ToggleRow((Component)Lang.of("fwm.antileak.stones", new Object[0]), () -> FwmConfig.INSTANCE.antiLeakStones, value -> {
            FwmConfig.INSTANCE.antiLeakStones = value;
        }));
        this.rows.add(new ToggleRow((Component)Lang.of("fwm.antileak.biome", new Object[0]), () -> FwmConfig.INSTANCE.antiLeakBiome, value -> {
            FwmConfig.INSTANCE.antiLeakBiome = value;
        }));
        this.rows.add(new ToggleRow((Component)Component.literal((String)Lang.t("Deepslate wie Stein zeigen", "Show deepslate as stone")), () -> FwmConfig.INSTANCE.antiLeakDeepslate, value -> {
            FwmConfig.INSTANCE.antiLeakDeepslate = value;
        }));
        this.rows.add(new ButtonRow((Component)Lang.of("fwm.antileak.pack", new Object[0]), () -> Lang.of(AntiLeak.dirty() ? "fwm.antileak.apply" : "fwm.antileak.current", new Object[0]), () -> AntiLeak.sync(Minecraft.getInstance())));
        this.rows.add(new TextRow((Component)Lang.of("fwm.antileak.hint", new Object[0]), null));
        this.rows.add(new TextRow((Component)Lang.of("fwm.antileak.hint2", new Object[0]), null));
    }

    @Override
    public void onHidden() {
        super.onHidden();
        AntiLeak.sync(Minecraft.getInstance());
    }
}

