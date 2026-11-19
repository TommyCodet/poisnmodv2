/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package net.friendswithmoney.fwm.ui.panel;

import java.util.ArrayList;
import net.friendswithmoney.fwm.chat.Messages;
import net.friendswithmoney.fwm.config.ConfigManager;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.panel.RowsPanel;
import net.friendswithmoney.fwm.ui.widget.ButtonRow;
import net.friendswithmoney.fwm.ui.widget.TextRow;
import net.friendswithmoney.fwm.ui.widget.ToggleRow;
import net.minecraft.network.chat.Component;

public class MessageSettingsPanel
extends RowsPanel {
    @Override
    public Component title() {
        return Lang.of("fwm.messages.settings", new Object[0]);
    }

    @Override
    protected void buildRows() {
        FwmConfig config = FwmConfig.INSTANCE;
        this.rows.add(new ToggleRow((Component)Lang.of("fwm.messages.showEnvelope", new Object[0]), () -> config.msgShowEnvelope, value -> {
            config.msgShowEnvelope = value;
            ConfigManager.save();
        }));
        this.rows.add(new ToggleRow((Component)Lang.of("fwm.messages.showToast", new Object[0]), () -> config.msgShowToast, value -> {
            config.msgShowToast = value;
            ConfigManager.save();
        }));
        this.rows.add(new TextRow((Component)Lang.of("fwm.messages.showToastHint", new Object[0]), null));
        this.rows.add(new TextRow((Component)Component.literal((String)Lang.t("AUSGEBLENDETE PERSONEN", "HIDDEN PEOPLE")), null));
        if (config.msgHidden.isEmpty()) {
            this.rows.add(new TextRow((Component)Component.literal((String)Lang.t("Niemand ausgeblendet.", "Nobody hidden.")), null));
        }
        for (String name : new ArrayList<String>(config.msgHidden)) {
            this.rows.add(new ButtonRow((Component)Component.literal((String)name), () -> Component.literal((String)Lang.t("Einblenden", "Show again")), () -> {
                Messages.unhide(name);
                this.onResize();
            }));
        }
    }
}

