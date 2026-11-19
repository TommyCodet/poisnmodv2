/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package net.friendswithmoney.fwm.ui.panel;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.panel.RowsPanel;
import net.friendswithmoney.fwm.ui.widget.TextRow;
import net.friendswithmoney.fwm.ui.widget.ToggleRow;
import net.minecraft.network.chat.Component;

public class CalcPanel
extends RowsPanel {
    @Override
    public Component title() {
        return Lang.of("fwm.tab.calc", new Object[0]);
    }

    static Component t(String de, String en) {
        return Component.literal((String)Lang.t(de, en));
    }

    static ToggleRow toggle(String de, String en, BooleanSupplier get, Consumer<Boolean> set) {
        return new ToggleRow(CalcPanel.t(de, en), get, v -> {
            set.accept((Boolean)v);
            V2.save();
        });
    }

    static ToggleRow feature(String key) {
        return new ToggleRow(CalcPanel.t("An", "On"), () -> Features.isOn(key), v -> Features.set(key, v));
    }

    @Override
    protected void buildRows() {
        this.rows.add(CalcPanel.feature("fwm.tab.calc"));
        this.rows.add(new TextRow(CalcPanel.t("Im Inventar links am Rand: das \u2211 klappt den Rechner aus.", "In the inventory, on its left edge: the \u2211 folds the calculator out."), null));
        this.rows.add(new TextRow(CalcPanel.t("Versteht 500k, 1.5m, 2b \u00b7 \"100 + 10 %\" = 110 \u00b7 Klammern, \u221a, ^", "Understands 500k, 1.5m, 2b \u00b7 \"100 + 10 %\" = 110 \u00b7 brackets, \u221a, ^"), null));
        this.rows.add(new TextRow(CalcPanel.t("Tippen geht, sobald du in den Rechner klickst; E schlie\u00dft dann nicht. \u29c9 kopiert das Ergebnis.", "Typing works once you click into it; E does not close then. \u29c9 copies the result."), null));
        this.rows.add(new TextRow(CalcPanel.t("Letzte Rechnung", "Last sum"), (Component)Component.literal((String)(V2.INSTANCE.calcLast.isEmpty() ? "\u2013" : V2.INSTANCE.calcLast))));
    }
}

