/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package net.friendswithmoney.fwm.ui.panel;

import net.friendswithmoney.fwm.config.ConfigManager;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.panel.RowsPanel;
import net.friendswithmoney.fwm.ui.widget.TextRow;
import net.friendswithmoney.fwm.ui.widget.ToggleRow;
import net.minecraft.network.chat.Component;

public class PrivacyPanel
extends RowsPanel {
    @Override
    public Component title() {
        return Lang.of("fwm.tab.privacy", new Object[0]);
    }

    @Override
    protected void buildRows() {
        this.rows.add(new TextRow((Component)Lang.of("fwm.privacy.sentHead", new Object[0])));
        this.rows.add(new TextRow((Component)Lang.of("fwm.privacy.presence", new Object[0]), (Component)Lang.of("fwm.privacy.presenceWhat", new Object[0])));
        this.rows.add(new TextRow((Component)Lang.of("fwm.privacy.lock", new Object[0]), (Component)Lang.of("fwm.privacy.lockWhat", new Object[0])));
        this.rows.add(new TextRow((Component)Lang.of("fwm.privacy.modId", new Object[0]), (Component)Lang.of("fwm.privacy.modIdWhat", new Object[0])));
        this.rows.add(new TextRow((Component)Lang.of("fwm.privacy.rtp", new Object[0]), (Component)Lang.of("fwm.privacy.rtpWhat", new Object[0])));
        this.rows.add(new TextRow((Component)Lang.of("fwm.privacy.switchHead", new Object[0])));
        this.rows.add(new ToggleRow((Component)Lang.of("fwm.privacy.presenceOn", new Object[0]), () -> FwmConfig.INSTANCE.presenceShowOthers, value -> {
            FwmConfig.INSTANCE.presenceShowOthers = value;
            ConfigManager.save();
        }));
        this.rows.add(new ToggleRow((Component)Lang.of("fwm.privacy.modIdOn", new Object[0]), () -> V2.INSTANCE.modIdConsent, value -> {
            V2.INSTANCE.modIdConsent = value;
            V2.save();
        }));
        this.rows.add(new TextRow((Component)Lang.of("fwm.privacy.whereHead", new Object[0])));
        this.rows.add(new ToggleRow((Component)Lang.of("fwm.privacy.singleplayer", new Object[0]), () -> V2.INSTANCE.singleplayerOn, value -> {
            V2.INSTANCE.singleplayerOn = value;
            V2.save();
        }));
        this.rows.add(new TextRow((Component)Lang.of("fwm.privacy.singleplayerHint", new Object[0]), null));
    }
}

