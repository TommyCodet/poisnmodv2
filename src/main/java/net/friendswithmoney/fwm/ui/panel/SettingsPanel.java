/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package net.friendswithmoney.fwm.ui.panel;

import net.friendswithmoney.fwm.config.ConfigManager;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.net.ServerContext;
import net.friendswithmoney.fwm.ui.FwmTutorial;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.panel.RowsPanel;
import net.friendswithmoney.fwm.ui.widget.ButtonRow;
import net.friendswithmoney.fwm.ui.widget.TextRow;
import net.friendswithmoney.fwm.ui.widget.ToggleRow;
import net.minecraft.network.chat.Component;

public class SettingsPanel
extends RowsPanel {
    @Override
    public Component title() {
        return Lang.of("fwm.tab.general", new Object[0]);
    }

    @Override
    protected void buildRows() {
        FwmConfig config = FwmConfig.INSTANCE;
        this.rows.add(new ToggleRow((Component)Lang.of("fwm.option.sounds", new Object[0]), () -> config.menuSounds, value -> {
            config.menuSounds = value;
            ConfigManager.save();
        }));
        this.rows.add(new ToggleRow((Component)Lang.of("fwm.option.hideDisabled", new Object[0]), () -> config.sidebarHideDisabled, value -> {
            config.sidebarHideDisabled = value;
            ConfigManager.save();
        }));
        this.rows.add(new TextRow((Component)Lang.of("fwm.option.hideDisabledHint", new Object[0]), null));
        if (ServerContext.hugo() || ServerContext.current() == ServerContext.Server.NONE) {
            this.rows.add(new ToggleRow((Component)Lang.of("fwm.option.presence", new Object[0]), () -> config.presenceShowOthers, value -> {
                config.presenceShowOthers = value;
                ConfigManager.save();
            }));
        }
        this.rows.add(new ButtonRow((Component)Lang.of("fwm.option.language", new Object[0]), () -> Component.literal((String)(Lang.isGerman() ? "Deutsch" : "English")), () -> {
            Lang.set(Lang.isGerman() ? "en" : "de");
            ConfigManager.save();
        }));
        this.rows.add(new TextRow((Component)Lang.of("fwm.option.languageHint", new Object[0]), null));
        this.rows.add(new ButtonRow((Component)Lang.of("fwm.option.replayTutorial", new Object[0]), () -> Lang.of("fwm.tutorial.start", new Object[0]), () -> {
            config.tutorialSeen = false;
            ConfigManager.save();
            FwmTutorial.start();
        }));
    }
}

