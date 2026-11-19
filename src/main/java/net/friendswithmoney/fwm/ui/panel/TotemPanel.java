/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package net.friendswithmoney.fwm.ui.panel;

import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.game.TotemCounter;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.panel.RowsPanel;
import net.friendswithmoney.fwm.ui.widget.ButtonRow;
import net.friendswithmoney.fwm.ui.widget.SliderRow;
import net.friendswithmoney.fwm.ui.widget.TextRow;
import net.friendswithmoney.fwm.ui.widget.ToggleRow;
import net.minecraft.network.chat.Component;

public class TotemPanel
extends RowsPanel {
    @Override
    public Component title() {
        return Lang.of("fwm.tab.totem", new Object[0]);
    }

    static Component t(String de, String en) {
        return Component.literal((String)Lang.t(de, en));
    }

    static ToggleRow toggle(String de, String en, BooleanSupplier get, Consumer<Boolean> set) {
        return new ToggleRow(TotemPanel.t(de, en), get, v -> {
            set.accept((Boolean)v);
            V2.save();
        });
    }

    static ToggleRow feature(String key) {
        return new ToggleRow(TotemPanel.t("An", "On"), () -> Features.isOn(key), v -> Features.set(key, v));
    }

    @Override
    protected void buildRows() {
        V2 c = V2.INSTANCE;
        this.rows.add(TotemPanel.feature("fwm.tab.totem"));
        this.rows.add(new TextRow(TotemPanel.t("Eigener Vorrat", "Own stock")));
        this.rows.add(TotemPanel.toggle("Karte im HUD", "Card in the HUD", () -> c.totemCard, v -> {
            c.totemCard = v;
        }));
        this.rows.add(TotemPanel.toggle("Nebenhand mitz\u00e4hlen", "Count the off hand", () -> c.totemOffhand, v -> {
            c.totemOffhand = v;
        }));
        this.rows.add(new SliderRow(TotemPanel.t("Gelb ab", "Yellow from"), 1, 10, () -> c.totemLow, v -> {
            c.totemLow = v;
            V2.save();
        }, v -> Component.literal((String)(v + Lang.t(" oder weniger", " or fewer")))));
        this.rows.add(new SliderRow(TotemPanel.t("Gr\u00f6\u00dfe der Karte", "Card size"), 50, 300, () -> Math.round(c.totemCardScale * 100.0f), v -> {
            c.totemCardScale = (float)v / 100.0f;
            V2.save();
        }, v -> Component.literal((String)(v + " %"))));
        this.rows.add(new TextRow(TotemPanel.t("Pops anderer Spieler", "Other players' pops")));
        this.rows.add(TotemPanel.toggle("Am Namensschild", "On the name tag", () -> c.totemNameTag, v -> {
            c.totemNameTag = v;
        }));
        this.rows.add(TotemPanel.toggle("In der Tab-Liste", "In the tab list", () -> c.totemTabList, v -> {
            c.totemTabList = v;
        }));
        this.rows.add(TotemPanel.toggle("Eigene Pops zeigen", "Show own pops", () -> c.totemOwnPops, v -> {
            c.totemOwnPops = v;
        }));
        this.rows.add(new ButtonRow(TotemPanel.t("Alle Z\u00e4hler zur\u00fccksetzen", "Reset all counters"), () -> TotemPanel.t("Zur\u00fccksetzen", "Reset"), TotemCounter::reset));
        this.rows.add(new TextRow(TotemPanel.t("Setzt sich bei Tod und Serverwechsel selbst zur\u00fcck. Auf DonutSMP nur der eigene Vorrat.", "Resets on death and server change. On DonutSMP only your own stock."), null));
        this.rows.add(new TextRow(TotemPanel.t("Top-Pops", "Top pops")));
        List<Map.Entry<String, Integer>> top = TotemCounter.top(8);
        if (top.isEmpty()) {
            this.rows.add(new TextRow(TotemPanel.t("Noch keine Pops gesehen.", "No pops seen yet."), null));
        }
        for (Map.Entry<String, Integer> e : top) {
            this.rows.add(new TextRow((Component)Component.literal((String)e.getKey()), (Component)Component.literal((String)(String.valueOf(e.getValue()) + " \u2726"))));
        }
    }
}

