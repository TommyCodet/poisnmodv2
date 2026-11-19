/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.ClientModInitializer
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.client.gui.screens.Screen
 */
package net.friendswithmoney.fwm;

import java.nio.file.Files;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.friendswithmoney.fwm.antileak.AntiLeak;
import net.friendswithmoney.fwm.auction.Auction;
import net.friendswithmoney.fwm.auction.Giveaway;
import net.friendswithmoney.fwm.auction.Hologram;
import net.friendswithmoney.fwm.chat.ChatRouter;
import net.friendswithmoney.fwm.chat.Messages;
import net.friendswithmoney.fwm.chat.SecondChatClicks;
import net.friendswithmoney.fwm.config.ConfigManager;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.controller.SellController;
import net.friendswithmoney.fwm.game.AlertSidePanel;
import net.friendswithmoney.fwm.game.ArmorWarn;
import net.friendswithmoney.fwm.game.AutoTpa;
import net.friendswithmoney.fwm.game.CalcSidePanel;
import net.friendswithmoney.fwm.game.Crosshair;
import net.friendswithmoney.fwm.game.EcValue;
import net.friendswithmoney.fwm.game.FightKits;
import net.friendswithmoney.fwm.game.InvValue;
import net.friendswithmoney.fwm.game.InvseeCounts;
import net.friendswithmoney.fwm.game.PvpFx;
import net.friendswithmoney.fwm.game.SellGuard;
import net.friendswithmoney.fwm.game.SpawnerMacro;
import net.friendswithmoney.fwm.game.SpawnerSidePanel;
import net.friendswithmoney.fwm.game.SpawnerSpots;
import net.friendswithmoney.fwm.game.SpawnerTooltip;
import net.friendswithmoney.fwm.game.Todos;
import net.friendswithmoney.fwm.game.ToolGuard;
import net.friendswithmoney.fwm.game.TotemCounter;
import net.friendswithmoney.fwm.game.TpaGuard;
import net.friendswithmoney.fwm.game.TpaWatch;
import net.friendswithmoney.fwm.input.Keybinds;
import net.friendswithmoney.fwm.lyrics.SkyLyrics;
import net.friendswithmoney.fwm.money.History;
import net.friendswithmoney.fwm.money.MoneyEvents;
import net.friendswithmoney.fwm.money.MoneyStore;
import net.friendswithmoney.fwm.net.FakeList;
import net.friendswithmoney.fwm.net.Giveaways;
import net.friendswithmoney.fwm.net.Market;
import net.friendswithmoney.fwm.net.Members;
import net.friendswithmoney.fwm.net.ModAccount;
import net.friendswithmoney.fwm.net.ModBan;
import net.friendswithmoney.fwm.net.ModNews;
import net.friendswithmoney.fwm.net.Presence;
import net.friendswithmoney.fwm.net.PricePings;
import net.friendswithmoney.fwm.net.ServerContext;
import net.friendswithmoney.fwm.net.Spawners;
import net.friendswithmoney.fwm.net.Tickets;
import net.friendswithmoney.fwm.net.TrustedClans;
import net.friendswithmoney.fwm.net.Waypoints;
import net.friendswithmoney.fwm.seventv.SevenTv;
import net.friendswithmoney.fwm.shop.MarketTooltip;
import net.friendswithmoney.fwm.shop.RealName;
import net.friendswithmoney.fwm.shop.UnitPrice;
import net.friendswithmoney.fwm.stats.SellStats;
import net.friendswithmoney.fwm.stream.Donations;
import net.friendswithmoney.fwm.textures.TextureMixer;
import net.friendswithmoney.fwm.ui.Design;
import net.friendswithmoney.fwm.ui.FwmHud;
import net.friendswithmoney.fwm.ui.FwmPalette;
import net.friendswithmoney.fwm.ui.FwmTutorial;
import net.friendswithmoney.fwm.ui.IntroScreen;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.MemberTags;
import net.friendswithmoney.fwm.ui.panel.SkyPanel;
import net.minecraft.client.gui.screens.Screen;

public class FwmClient
implements ClientModInitializer {
    public static final String MOD_VERSION = FabricLoader.getInstance().getModContainer("fwm").map(mod -> mod.getMetadata().getVersion().getFriendlyString()).orElse("?");
    public static final SellController SELL_CONTROLLER = new SellController();

    public void onInitializeClient() {
        ConfigManager.load();
        V2.load();
        FwmPalette.applyFromConfig();
        Lang.load();
        Spawners.load();
        Market.load();
        try {
            Files.deleteIfExists(FabricLoader.getInstance().getConfigDir().resolve("fwm-reconnect-log.json"));
        }
        catch (Exception exception) {
            // empty catch block
        }
        TextureMixer.migrate();
        TextureMixer.registerGuard();
        SkyPanel.migrate();
        if (!FwmConfig.INSTANCE.envelopeOff66) {
            FwmConfig.INSTANCE.msgShowEnvelope = false;
            FwmConfig.INSTANCE.envelopeOff66 = true;
            ConfigManager.save();
        }
        TrustedClans.load();
        ModAccount.load();
        ModBan.register();
        Members.load();
        Messages.load();
        Keybinds.register();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null) {
                IntroScreen.joinCalm = 0;
                IntroScreen.joinAsked = false;
                return;
            }
            if (IntroScreen.joinAsked) {
                return;
            }
            if (client.gui.overlay() != null || IntroScreen.blocking(client.gui.screen())) {
                IntroScreen.joinCalm = 0;
                return;
            }
            if (++IntroScreen.joinCalm < 20) {
                return;
            }
            IntroScreen.joinAsked = true;
            String intro = IntroScreen.forServer(ServerContext.current());
            if (intro == null || !IntroScreen.available(intro) || IntroScreen.seen(intro)) {
                return;
            }
            IntroScreen.markSeen(intro);
            client.gui.setScreen((Screen)new IntroScreen(client.gui.screen(), intro));
        });
        FwmHud.register();
        MemberTags.register();
        ToolGuard.register();
        TpaWatch.register();
        TpaGuard.register();
        SellGuard.register();
        Todos.register();
        Donations.register();
        TotemCounter.register();
        Crosshair.register();
        CalcSidePanel.register();
        ChatRouter.register();
        SecondChatClicks.register();
        UnitPrice.register();
        FightKits.register();
        Waypoints.register();
        Hologram.register();
        PvpFx.register();
        SkyLyrics.init();
        RealName.register();
        MarketTooltip.register();
        SpawnerTooltip.register();
        InvValue.register();
        EcValue.register();
        InvseeCounts.register();
        SpawnerSpots.register();
        SpawnerSidePanel.register();
        AlertSidePanel.register();
        SevenTv.register();
        ClientLifecycleEvents.CLIENT_STOPPING.register(c -> {
            MoneyStore.left();
            MoneyStore.save();
            History.save();
            V2.save();
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean hugo;
            FwmConfig config = FwmConfig.INSTANCE;
            if (!Features.isOn("fwm.tab.sellmacro") && config.enabled) {
                config.enabled = false;
            }
            if (SELL_CONTROLLER.follow(config.enabled, client)) {
                FwmHud.showDeactivated();
                SellStats.cancelTest();
            }
            if (config.enabled && SELL_CONTROLLER.isRunning()) {
                SellStats.countActiveTick();
                if (SellStats.tickTest()) {
                    config.enabled = false;
                    SELL_CONTROLLER.disable(client);
                    ConfigManager.save();
                }
            }
            if (SellStats.tickGrace()) {
                client.gui.setScreen(Design.open(null, "fwm.tab.sellmacro", 1));
            }
            SELL_CONTROLLER.onClientTick(client);
            Keybinds.handle(client);
            boolean bl = hugo = ServerContext.hugo() || ServerContext.current() == ServerContext.Server.NONE;
            if (hugo) {
                Spawners.tick(client);
                Members.tick(client);
            }
            ModNews.tick(client);
            if (hugo) {
                FakeList.tick(client);
                Waypoints.tick(client);
            }
            Presence.tick(client);
            if (hugo) {
                Giveaways.tick(client);
                TrustedClans.tick(client);
                Tickets.tick(client);
            }
            FwmTutorial.tick(client);
            Messages.tick(client);
            Auction.tick(client);
            Giveaway.tick(client);
            TpaWatch.tick(client);
            if (hugo) {
                PricePings.tick(client);
            }
            AntiLeak.tick(client);
            AutoTpa.tick(client);
            SpawnerMacro.tick(client);
            MoneyEvents.tick(client);
            InvValue.tick(client);
            TotemCounter.tick(client);
            ArmorWarn.tick(client);
        });
    }
}

