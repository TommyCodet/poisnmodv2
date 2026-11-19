/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package net.friendswithmoney.fwm.ui.panel;

import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.panel.RowsPanel;
import net.friendswithmoney.fwm.ui.widget.TextRow;
import net.friendswithmoney.fwm.ui.widget.ToggleRow;
import net.minecraft.network.chat.Component;

public class TpaGuardPanel
extends RowsPanel {
    @Override
    public Component title() {
        return Lang.of("fwm.tab.tpaguard", new Object[0]);
    }

    @Override
    protected void buildRows() {
        V2 c = V2.INSTANCE;
        this.rows.add(new TextRow((Component)Component.literal((String)Lang.t("Schickst du einem erkannten Faker eine Anfrage oder Geld, h\u00e4lt die Mod den Befehl an.", "Sending a request or money to a known fake is held back.")), null));
        this.rows.add(new ToggleRow((Component)Component.literal((String)"/tpa, /tpahere, /tpaccept"), () -> c.guardTpa, v -> {
            c.guardTpa = v;
            V2.save();
        }));
        this.rows.add(new ToggleRow((Component)Component.literal((String)"/pay"), () -> c.guardPay, v -> {
            c.guardPay = v;
            V2.save();
        }));
        this.rows.add(new ToggleRow((Component)Component.literal((String)Lang.t("/ah sell: Abfrage bei komischem Preis (unter 50 % / \u00fcber 5x)", "/ah sell: ask on an odd price (below 50 % / above 5x)")), () -> c.sellGuard, v -> {
            c.sellGuard = v;
            V2.save();
        }));
        this.rows.add(new ToggleRow((Component)Component.literal((String)Lang.t("Warnton", "Warning sound")), () -> c.guardSound, v -> {
            c.guardSound = v;
            V2.save();
        }));
        this.rows.add(new TextRow((Component)Component.literal((String)Lang.t("\"Trotzdem senden\" gilt einmal, 60 s, nur f\u00fcr genau diesen Befehl.", "\"Send anyway\" counts once, for 60 s, for exactly that command.")), null));
    }
}

