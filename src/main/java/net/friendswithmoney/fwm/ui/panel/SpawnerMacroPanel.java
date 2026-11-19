/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.chat.Component
 */
package net.friendswithmoney.fwm.ui.panel;

import java.util.List;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.game.SpawnerMacro;
import net.friendswithmoney.fwm.input.Keybinds;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.panel.PanelHost;
import net.friendswithmoney.fwm.ui.panel.RowsPanel;
import net.friendswithmoney.fwm.ui.panel.SpawnerGuidePanel;
import net.friendswithmoney.fwm.ui.widget.ButtonRow;
import net.friendswithmoney.fwm.ui.widget.ChipsRow;
import net.friendswithmoney.fwm.ui.widget.SliderRow;
import net.friendswithmoney.fwm.ui.widget.TextRow;
import net.friendswithmoney.fwm.ui.widget.ToggleRow;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class SpawnerMacroPanel
extends RowsPanel {
    private final PanelHost host;

    public SpawnerMacroPanel(PanelHost host) {
        this.host = host;
    }

    @Override
    public Component title() {
        return Lang.of("fwm.tab.spawnermacro", new Object[0]);
    }

    @Override
    protected void buildRows() {
        FwmConfig config = FwmConfig.INSTANCE;
        this.rows.add(new ToggleRow((Component)Lang.of("fwm.spawnermacro.active", new Object[0]), () -> Features.isOn("fwm.tab.spawnermacro"), value -> Features.set("fwm.tab.spawnermacro", value)));
        this.rows.add(new ButtonRow((Component)Lang.of("fwm.spawnermacro.key", Keybinds.spawnerKeyName()), () -> Component.literal((String)(SpawnerMacro.running() ? Lang.t("Stoppen", "Stop") : Lang.t("Starten", "Start"))), () -> {
            Minecraft client = Minecraft.getInstance();
            if (SpawnerMacro.running()) {
                SpawnerMacro.toggle(client);
            } else {
                client.gui.setScreen(null);
                SpawnerMacro.start(client);
            }
        }));
        this.rows.add(new ChipsRow((Component)Component.literal((String)Lang.t("Drops", "Drops")), List.of(new ChipsRow.Chip(Lang.t("Knochen", "Bones"), 0), new ChipsRow.Chip(Lang.t("Droppen", "Drop"), 1), new ChipsRow.Chip(Lang.t("Verkauf", "Sell"), 2)), () -> "drop".equals(config.spawnerMode) ? 1 : ("sell".equals(config.spawnerMode) ? 2 : 0), v -> {
            config.spawnerMode = v == 1 ? "drop" : (v == 2 ? "sell" : "bones");
        }));
        this.rows.add(new ButtonRow((Component)Component.literal((String)Lang.t("So baust du es am besten", "How to build it best")), () -> Component.literal((String)Lang.t("Bild ansehen", "View picture")), () -> this.host.openSub(new SpawnerGuidePanel())));
        this.rows.add(new ButtonRow((Component)Component.literal((String)Lang.t("Erweitert", "Advanced")), () -> Component.literal((String)Lang.t("\u00d6ffnen", "Open")), () -> this.host.openSub(new Advanced())));
        this.rows.add(new TextRow((Component)Component.literal((String)Lang.t("Auf den Spawner schauen, Taste dr\u00fccken \u2013 fertig.", "Look at the spawner, press the key - done.")), null));
        this.rows.add(new TextRow((Component)Lang.of("fwm.spawnermacro.hint3", new Object[0]), null));
    }

    private static final class Advanced
    extends RowsPanel {
        private Advanced() {
        }

        @Override
        public Component title() {
            return Component.literal((String)Lang.t("Spawner-Makro \u00b7 Erweitert", "Spawner macro \u00b7 advanced"));
        }

        @Override
        protected void buildRows() {
            FwmConfig config = FwmConfig.INSTANCE;
            this.rows.add(new ToggleRow((Component)Component.literal((String)Lang.t("Seitenfeld am Spawner-Men\u00fc", "Side box at the spawner menu")), () -> config.spawnerSidePanel, value -> {
                config.spawnerSidePanel = value;
            }));
            this.rows.add(new ToggleRow((Component)Component.literal((String)Lang.t("Infos \u00fcber ausgew\u00e4hlten Spawnern", "Info over picked spawners")), () -> config.spawnerLabels, value -> {
                config.spawnerLabels = value;
            }));
            this.rows.add(new SliderRow((Component)Component.literal((String)Lang.t("Verkaufen ab Auslastung", "Sell from fill")), 0, 100, () -> config.spawnerSellFill, value -> {
                config.spawnerSellFill = value > 0 && value < 10 ? 10 : value;
            }, v -> Component.literal((String)(v <= 0 ? Lang.t("nie", "never") : v + " %"))));
            this.rows.add(new ToggleRow((Component)Lang.of("fwm.spawnermacro.autoDrop", new Object[0]), () -> config.spawnerAutoDrop, value -> {
                config.spawnerAutoDrop = value;
            }));
            this.rows.add(new ToggleRow((Component)Lang.of("fwm.spawnermacro.autoStop", new Object[0]), () -> config.spawnerAutoStop, value -> {
                config.spawnerAutoStop = value;
            }));
            this.rows.add(new ToggleRow((Component)Lang.of("fwm.spawnermacro.autoSell", new Object[0]), () -> config.spawnerAutoSell, value -> {
                config.spawnerAutoSell = value;
            }));
            this.rows.add(new SliderRow((Component)Lang.of("fwm.spawnermacro.delay", new Object[0]), 2, 20, () -> config.spawnerClickDelayTicks, value -> {
                config.spawnerClickDelayTicks = value;
            }, ticks -> Component.literal((String)(ticks * 50 + " ms"))));
            this.rows.add(new SliderRow((Component)Lang.of("fwm.spawnermacro.repeat", new Object[0]), 10, 600, () -> config.spawnerRepeatSeconds, value -> {
                config.spawnerRepeatSeconds = value;
            }, seconds -> Component.literal((String)(seconds + " s"))));
            this.rows.add(new TextRow((Component)Lang.of("fwm.spawnermacro.hint", new Object[0]), null));
            this.rows.add(new TextRow((Component)Lang.of("fwm.spawnermacro.hint2", new Object[0]), null));
        }
    }
}

