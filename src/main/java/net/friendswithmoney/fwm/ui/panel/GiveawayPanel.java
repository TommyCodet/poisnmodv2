/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 */
package net.friendswithmoney.fwm.ui.panel;

import java.util.List;
import net.friendswithmoney.fwm.auction.Giveaway;
import net.friendswithmoney.fwm.auction.Hologram;
import net.friendswithmoney.fwm.auction.StreamerStore;
import net.friendswithmoney.fwm.auction.Times;
import net.friendswithmoney.fwm.config.ConfigManager;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.ui.FwmNotice;
import net.friendswithmoney.fwm.ui.FwmTheme;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.StreamerScreen;
import net.friendswithmoney.fwm.ui.panel.AuctionItemPanel;
import net.friendswithmoney.fwm.ui.panel.AuctionPanel;
import net.friendswithmoney.fwm.ui.panel.PanelHost;
import net.friendswithmoney.fwm.ui.panel.RowsPanel;
import net.friendswithmoney.fwm.ui.widget.ButtonRow;
import net.friendswithmoney.fwm.ui.widget.ChipsRow;
import net.friendswithmoney.fwm.ui.widget.LiveRow;
import net.friendswithmoney.fwm.ui.widget.NumberRow;
import net.friendswithmoney.fwm.ui.widget.ReelRow;
import net.friendswithmoney.fwm.ui.widget.SliderRow;
import net.friendswithmoney.fwm.ui.widget.TextRow;
import net.friendswithmoney.fwm.ui.widget.ToggleRow;
import net.friendswithmoney.fwm.ui.widget.WordRow;
import net.friendswithmoney.fwm.util.Fmt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class GiveawayPanel
extends RowsPanel {
    private static final int MIN_SECONDS = 15;
    private static final int MAX_SECONDS = 3600;
    private final PanelHost host;
    private String builtFor = "";
    private long cancelArmedAt;

    @Override
    public boolean compact() {
        return false;
    }

    public GiveawayPanel(PanelHost host) {
        this.host = host;
    }

    @Override
    public Component title() {
        return Lang.of("fwm.tab.giveaway", new Object[0]);
    }

    private static String signature() {
        return String.valueOf((Object)Giveaway.status()) + ":" + Giveaway.participants().size() + ":" + Giveaway.drawing() + ":" + FwmConfig.INSTANCE.giveawayMoneyPrize;
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY, float delta) {
        if (!this.builtFor.equals(GiveawayPanel.signature()) && !this.isCapturingKeys()) {
            this.setBounds(this.x, this.y, this.width, this.height);
        }
        super.render(ctx, font, mouseX, mouseY, delta);
    }

    @Override
    protected void buildRows() {
        this.builtFor = GiveawayPanel.signature();
        switch (Giveaway.status()) {
            case RUNNING: 
            case PAUSED: {
                this.running();
                break;
            }
            case ENDED: {
                this.ended();
                break;
            }
            default: {
                this.setup();
            }
        }
        this.rows.add(new ToggleRow((Component)Component.literal((String)Lang.t("Hologramm in der Welt", "Hologram in the world")), () -> V2.INSTANCE.hologram, v -> {
            V2.INSTANCE.hologram = v;
            V2.save();
        }));
        this.rows.add(new ButtonRow((Component)Component.literal((String)Lang.t("Hologramm hierher holen", "Move hologram here")), () -> Component.literal((String)Lang.t("Hierher", "Here")), Hologram::moveHere));
    }

    private void setup() {
        FwmConfig config = FwmConfig.INSTANCE;
        this.rows.add(new ButtonRow((Component)Component.literal((String)"Giveaway"), () -> Component.literal((String)Lang.t("Starten", "Start")), this::start));
        this.rows.add(new ToggleRow((Component)Component.literal((String)Lang.t("Geld als Preis", "Money as the prize")), () -> config.giveawayMoneyPrize, on -> {
            config.giveawayMoneyPrize = on;
            ConfigManager.save();
        }));
        if (config.giveawayMoneyPrize) {
            this.rows.add(new NumberRow((Component)Component.literal((String)Lang.t("Betrag", "Amount")), () -> config.giveawayMoney, value -> {
                config.giveawayMoney = Math.max(0L, value);
                ConfigManager.save();
            }, value -> Component.literal((String)(value <= 0L ? Lang.t("Betrag eingeben", "Enter amount") : Fmt.dollars(value * 100L)))));
        } else {
            this.rows.add(new ButtonRow((Component)Lang.of("fwm.auction.item", new Object[0]), GiveawayPanel::itemCaption, () -> this.host.openSub(new AuctionItemPanel(this.host, Giveaway::item, Giveaway::hold))));
        }
        this.rows.add(new WordRow((Component)Component.literal((String)Lang.t("Teilnahme-Wort", "Join word")), () -> config.giveawayWord, value -> {
            config.giveawayWord = value;
            ConfigManager.save();
        }));
        this.rows.add(new SliderRow((Component)Lang.of("fwm.auction.time", new Object[0]), 15, 3600, () -> config.giveawaySeconds, value -> {
            config.giveawaySeconds = value;
            ConfigManager.save();
        }, seconds -> Component.literal((String)Times.clock(seconds)), Times::seconds));
        this.rows.add(new ChipsRow((Component)Component.literal((String)Lang.t("Schnellwahl", "Quick pick")), AuctionPanel.DURATIONS, () -> config.giveawaySeconds, value -> {
            config.giveawaySeconds = value;
            ConfigManager.save();
        }));
        this.rows.add(new TextRow((Component)Component.literal((String)Lang.t("Spieler schreiben dir per /msg das Wort.", "Players send you the word via /msg.")), null));
        this.rows.add(new TextRow((Component)Component.literal((String)(config.giveawayMoneyPrize ? Lang.t("Nach der Ziehung: Klick auf [Auszahlen] im Chat legt /pay bereit.", "After the draw: click [Pay out] in the chat to get the /pay ready.") : Lang.t("Das Item bleibt bei dir \u2013 \u00fcbergib es nach der Ziehung.", "The item stays with you \u2013 hand it over after the draw."))), null));
    }

    private void start() {
        FwmConfig config = FwmConfig.INSTANCE;
        if (config.giveawayMoneyPrize) {
            if (config.giveawayMoney <= 0L) {
                FwmNotice.show("Giveaway", Lang.t("Kein Betrag", "No amount"), Lang.t("Trag zuerst ein, wie viel Geld es gibt.", "Enter how much money first."), 16103723, 3500L);
                return;
            }
            Giveaway.start(ItemStack.EMPTY, config.giveawayMoney, config.giveawayWord, Integer.MAX_VALUE, config.giveawaySeconds);
            Minecraft.getInstance().gui.setScreen((Screen)new StreamerScreen(null, "giveaway"));
            return;
        }
        if (Giveaway.item().isEmpty()) {
            this.host.openSub(new AuctionItemPanel(this.host, Giveaway::item, Giveaway::hold));
            return;
        }
        Giveaway.start(Giveaway.item(), config.giveawayWord, Integer.MAX_VALUE, config.giveawaySeconds);
        Minecraft.getInstance().gui.setScreen((Screen)new StreamerScreen(null, "giveaway"));
    }

    private void running() {
        boolean paused = Giveaway.status() == Giveaway.Status.PAUSED;
        this.rows.add(new ButtonRow((Component)Component.literal((String)Lang.t("Streamer-Modus", "Streamer mode")), () -> Component.literal((String)Lang.t("\u00d6ffnen", "Open")), () -> Minecraft.getInstance().gui.setScreen((Screen)new StreamerScreen(null, "giveaway"))));
        this.rows.add(new LiveRow((Component)Component.literal((String)"Status"), () -> Component.literal((String)(paused ? Lang.t("PAUSIERT", "PAUSED") : "LIVE")), () -> paused ? FwmTheme.TEXT_DIM : FwmTheme.ACCENT));
        this.rows.add(new ButtonRow((Component)Component.literal((String)(paused ? Lang.t("Teilnahme ist gestoppt", "Entries are stopped") : Lang.t("Teilnahme l\u00e4uft", "Entries are open"))), () -> Component.literal((String)(paused ? Lang.t("Fortsetzen", "Resume") : Lang.t("Pausieren", "Pause"))), () -> {
            if (paused) {
                Giveaway.resume();
            } else {
                Giveaway.pause();
            }
        }));
        this.rows.add(new ButtonRow((Component)Component.literal((String)Lang.t("Gewinner sofort ziehen", "Draw the winner now")), () -> Component.literal((String)Lang.t("Jetzt ziehen", "Draw now")), Giveaway::draw));
        this.rows.add(new ButtonRow((Component)Component.literal((String)Lang.t("Giveaway abbrechen", "Cancel giveaway")), () -> Component.literal((String)(System.currentTimeMillis() - this.cancelArmedAt < 3000L ? Lang.t("Sicher? Klicken", "Sure? Click") : Lang.t("Abbrechen", "Cancel"))), () -> {
            if (System.currentTimeMillis() - this.cancelArmedAt < 3000L) {
                Giveaway.cancel();
            } else {
                this.cancelArmedAt = System.currentTimeMillis();
            }
        }));
        this.rows.add(new LiveRow((Component)(Giveaway.money() > 0L ? Component.literal((String)Lang.t("Preis", "Prize")) : Lang.of("fwm.auction.item", new Object[0])), () -> Component.literal((String)Giveaway.prizeName())));
        this.rows.add(new LiveRow((Component)Component.literal((String)Lang.t("Restzeit", "Time left")), () -> Component.literal((String)Times.clock((int)((Giveaway.remainingMs() + 999L) / 1000L)))));
        this.rows.add(new LiveRow((Component)Component.literal((String)Lang.t("Teilnehmer", "Participants")), () -> Component.literal((String)String.valueOf(Giveaway.participants().size()))));
        String join = Giveaway.joinCommand();
        this.rows.add(new ButtonRow((Component)Component.literal((String)(Lang.t("Teilnahme: ", "Join: ") + join)), () -> Component.literal((String)Lang.t("Kopieren", "Copy")), () -> StreamerStore.copy(join)));
        this.participantRows();
    }

    private void ended() {
        String winner = Giveaway.winner();
        if (winner != null) {
            this.rows.add(new ReelRow());
            this.rows.add(new ReelRow.Spacer());
        }
        if (Giveaway.drawing()) {
            this.participantRows();
            return;
        }
        this.rows.add(new TextRow((Component)Component.literal((String)(winner == null ? Lang.t("Niemand hat teilgenommen.", "Nobody took part.") : Lang.t("Gewinner", "Winner"))), (Component)(winner == null ? null : Component.literal((String)winner))));
        if (winner != null) {
            this.rows.add(new ButtonRow((Component)Component.literal((String)Lang.t("Gewinner", "Winner")), () -> Component.literal((String)Lang.t("Name kopieren", "Copy name")), () -> StreamerStore.copy(winner)));
            this.rows.add(new ButtonRow((Component)Component.literal((String)Lang.t("Zum Gewinner", "To the winner")), () -> Component.literal((String)("/tpa " + winner)), () -> StreamerStore.command("tpa " + winner)));
            this.rows.add(new ButtonRow((Component)Component.literal((String)Lang.t("Gewinner zu dir", "Winner to you")), () -> Component.literal((String)("/tpahere " + winner)), () -> this.confirm.ask((Component)Component.literal((String)Lang.t("Gewinner " + winner + " per /tpahere zu dir holen?", "Pull winner " + winner + " over with /tpahere?")), "fwm.confirm.confirm", () -> StreamerStore.command("tpahere " + winner))));
            this.rows.add(new TextRow((Component)Component.literal((String)Lang.t("\u00dcbergib das Item manuell (Handel oder Drop).", "Hand the item over yourself (trade or drop).")), null));
        }
        this.rows.add(new ButtonRow((Component)Component.literal((String)Lang.t("Neues Giveaway", "New giveaway")), () -> Component.literal((String)Lang.t("Schlie\u00dfen", "Close")), Giveaway::reset));
        this.participantRows();
    }

    private void participantRows() {
        List<Giveaway.Participant> all = Giveaway.participants();
        this.rows.add(new TextRow((Component)Component.literal((String)Lang.t("Teilnehmerliste", "Participant list")), (Component)Component.literal((String)(all.isEmpty() ? Lang.t("noch niemand", "nobody yet") : String.valueOf(all.size())))));
        for (int i = 0; i < all.size(); ++i) {
            Giveaway.Participant p = all.get(i);
            this.rows.add(new LiveRow((Component)Component.literal((String)(i + 1 + ". " + p.name())), () -> Component.literal((String)GiveawayPanel.ago(p.joinedAt())), () -> FwmTheme.TEXT_DIM));
        }
    }

    private static String ago(long time) {
        long seconds = Math.max(0L, (System.currentTimeMillis() - time) / 1000L);
        if (seconds < 60L) {
            return Lang.t("vor " + seconds + " s", seconds + " s ago");
        }
        if (seconds < 3600L) {
            return Lang.t("vor " + seconds / 60L + " Min", seconds / 60L + " min ago");
        }
        return Lang.t("vor " + seconds / 3600L + " Std", seconds / 3600L + " h ago");
    }

    private static Component itemCaption() {
        ItemStack stack = Giveaway.item();
        if (stack.isEmpty()) {
            return Lang.of("fwm.auction.pick", new Object[0]);
        }
        return Component.literal((String)((String)(stack.getCount() > 1 ? stack.getCount() + "x " : "") + stack.getHoverName().getString()));
    }
}

