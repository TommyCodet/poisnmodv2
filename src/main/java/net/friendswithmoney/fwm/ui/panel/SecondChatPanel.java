/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 */
package net.friendswithmoney.fwm.ui.panel;

import net.friendswithmoney.fwm.config.ChatFilter;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.net.ServerContext;
import net.friendswithmoney.fwm.ui.HudEditScreen;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.panel.ChatFilterPanel;
import net.friendswithmoney.fwm.ui.panel.PanelHost;
import net.friendswithmoney.fwm.ui.panel.RowsPanel;
import net.friendswithmoney.fwm.ui.widget.ButtonRow;
import net.friendswithmoney.fwm.ui.widget.SliderRow;
import net.friendswithmoney.fwm.ui.widget.ToggleRow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SecondChatPanel
extends RowsPanel {
    private final PanelHost host;

    public SecondChatPanel(PanelHost host) {
        this.host = host;
    }

    @Override
    public Component title() {
        return Lang.of("fwm.tab.secondchat", new Object[0]);
    }

    @Override
    protected void buildRows() {
        FwmConfig config = FwmConfig.INSTANCE;
        this.rows.add(new ToggleRow((Component)Lang.of("fwm.secondchat.active", new Object[0]), () -> Features.isOn("fwm.tab.secondchat"), value -> Features.set("fwm.tab.secondchat", value)));
        this.rows.add(new ButtonRow((Component)Lang.of("fwm.secondchat.place", new Object[0]), () -> Lang.of("fwm.option.hudEditOpen", new Object[0]), () -> {
            Minecraft client = Minecraft.getInstance();
            client.gui.setScreen((Screen)new HudEditScreen(client.gui.screen()));
        }));
        this.rows.add(new ToggleRow((Component)Lang.of("fwm.secondchat.msg", new Object[0]), () -> config.secondChatMsg, value -> {
            config.secondChatMsg = value;
        }));
        this.rows.add(new ToggleRow((Component)Lang.of("fwm.secondchat.tpa", new Object[0]), () -> config.secondChatTpa, value -> {
            config.secondChatTpa = value;
        }));
        this.rows.add(new ToggleRow((Component)Lang.of("fwm.secondchat.pay", new Object[0]), () -> config.secondChatPay, value -> {
            config.secondChatPay = value;
        }));
        ServerContext.Server here = ServerContext.current();
        if (here != ServerContext.Server.OTHER) {
            this.rows.add(new ToggleRow((Component)Component.literal((String)Lang.t("Auktion/Order-Meldungen", "Auction/order messages")), () -> config.secondChatMarket, value -> {
                config.secondChatMarket = value;
            }));
        }
        this.rows.add(new ButtonRow((Component)Lang.of("fwm.secondchat.filters", new Object[0]), () -> Lang.of("fwm.secondchat.filterCount", SecondChatPanel.activeFilters()), () -> this.host.openSub(new ChatFilterPanel(this.host))));
        this.rows.add(new SliderRow((Component)Lang.of("fwm.secondchat.lines", new Object[0]), 3, 16, () -> config.secondChatLines, value -> {
            config.secondChatLines = value;
        }, lines -> Lang.of("fwm.secondchat.linesValue", lines)));
        this.rows.add(new SliderRow((Component)Component.literal((String)Lang.t("Deckkraft", "Opacity")), 0, 100, () -> config.secondChatBackgroundOpacity, value -> {
            config.secondChatBackgroundOpacity = value;
        }, value -> Component.literal((String)(value + " %"))));
    }

    private static int activeFilters() {
        int count = 0;
        for (ChatFilter filter : FwmConfig.INSTANCE.secondChatFilters) {
            if (filter == null || !filter.on || filter.text == null || filter.text.isBlank()) continue;
            ++count;
        }
        return count;
    }
}

