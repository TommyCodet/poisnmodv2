/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package net.friendswithmoney.fwm.ui.panel;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.friendswithmoney.fwm.chat.AdBlock;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.panel.NameListPanel;
import net.friendswithmoney.fwm.ui.panel.PanelHost;
import net.friendswithmoney.fwm.ui.panel.RowsPanel;
import net.friendswithmoney.fwm.ui.widget.ButtonRow;
import net.friendswithmoney.fwm.ui.widget.TextRow;
import net.friendswithmoney.fwm.ui.widget.ToggleRow;
import net.minecraft.network.chat.Component;

public class AdBlockPanel
extends RowsPanel {
    private final PanelHost host;

    public AdBlockPanel(PanelHost host) {
        this.host = host;
    }

    @Override
    public Component title() {
        return Lang.of("fwm.tab.adblock", new Object[0]);
    }

    private static ToggleRow toggle(String de, String en, BooleanSupplier get, Consumer<Boolean> set) {
        return new ToggleRow((Component)Component.literal((String)Lang.t(de, en)), get, v -> {
            set.accept((Boolean)v);
            V2.save();
        });
    }

    @Override
    protected void buildRows() {
        V2 c = V2.INSTANCE;
        this.rows.add(new TextRow((Component)Component.literal((String)(Lang.t("Heute ausgeblendet: ", "Hidden today: ") + AdBlock.hiddenToday())), null));
        this.rows.add(new TextRow((Component)Component.literal((String)Lang.t("Was weg soll", "What goes"))));
        this.rows.add(AdBlockPanel.toggle("Stream- und Giveaway-Werbung", "Stream and giveaway adverts", () -> c.adStream, v -> {
            c.adStream = v;
        }));
        this.rows.add(AdBlockPanel.toggle("Kaufe / Verkaufe / WTS", "Buying / selling / WTS", () -> c.adTrade, v -> {
            c.adTrade = v;
        }));
        this.rows.add(AdBlockPanel.toggle("TP/TPA-Werbung (\"tp zu mir\")", "TP/TPA adverts (\"tp me\")", () -> c.adTpa, v -> {
            c.adTpa = v;
        }));
        this.rows.add(AdBlockPanel.toggle("Shop-Werbung des Servers", "The server's shop adverts", () -> c.adServer, v -> {
            c.adServer = v;
        }));
        this.rows.add(AdBlockPanel.toggle("Fakes ausblenden (falscher Clan-Tag)", "Hide fakes (copied clan tag)", () -> c.adFakes, v -> {
            c.adFakes = v;
        }));
        this.rows.add(new TextRow((Component)Component.literal((String)Lang.t("Ausnahmen", "Exceptions"))));
        this.rows.add(AdBlockPanel.toggle("Clan-Mitglieder nie ausblenden", "Never hide clan members", () -> c.adKeepMembers, v -> {
            c.adKeepMembers = v;
        }));
        this.rows.add(new TextRow((Component)Component.literal((String)Lang.t("Du selbst und echte TPA-Anfragen bleiben immer.", "You and real TPA requests always stay.")), null));
        this.rows.add(new TextRow((Component)Component.literal((String)Lang.t("Eigene W\u00f6rter", "Own words"))));
        this.rows.add(new ButtonRow((Component)Component.literal((String)Lang.t("W\u00f6rter bearbeiten (" + c.adWords.size() + ")", "Edit words (" + c.adWords.size() + ")")), () -> Component.literal((String)Lang.t("\u00d6ffnen", "Open")), () -> this.host.openSub(new NameListPanel(this.host, "fwm.tab.adblock", "fwm.adblock.none", "fwm.adblock.add", () -> c.adWords))));
    }
}

