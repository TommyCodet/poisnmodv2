/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.InputConstants$Key
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  com.mojang.blaze3d.platform.Window
 *  net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.KeyMapping$Category
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 */
package net.friendswithmoney.fwm.input;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.friendswithmoney.fwm.config.ConfigManager;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.controller.SellPresets;
import net.friendswithmoney.fwm.game.ItemInfo;
import net.friendswithmoney.fwm.game.SpawnerMacro;
import net.friendswithmoney.fwm.ui.Design;
import net.friendswithmoney.fwm.ui.FwmNotice;
import net.friendswithmoney.fwm.ui.StreamerScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public final class Keybinds {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.MISC;
    public static KeyMapping TOGGLE;
    public static KeyMapping OPEN_MENU;
    public static KeyMapping OPEN_MESSAGES;
    public static KeyMapping OPEN_AUCTION;
    public static KeyMapping OPEN_GIVEAWAY;
    public static KeyMapping SPAWNER_MACRO;
    public static KeyMapping ITEM_INFO;
    public static KeyMapping NEXT_PRESET;
    private static final Set<String> HELD;
    private static final int WATCH_INTERVAL = 40;
    private static int watch;
    private static boolean restored;

    private Keybinds() {
    }

    public static void register() {
        if (TOGGLE != null) {
            return;
        }
        TOGGLE = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("FWM: Sellmacro an/aus", InputConstants.Type.KEYSYM, 89, CATEGORY));
        OPEN_MENU = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("FWM: Men\u00fc \u00f6ffnen", InputConstants.Type.KEYSYM, 74, CATEGORY));
        OPEN_MESSAGES = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("FWM: Nachrichten \u00f6ffnen", InputConstants.Type.KEYSYM, 77, CATEGORY));
        OPEN_AUCTION = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("FWM: Auktionen \u00f6ffnen", InputConstants.Type.KEYSYM, -1, CATEGORY));
        OPEN_GIVEAWAY = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("FWM: Giveaways \u00f6ffnen", InputConstants.Type.KEYSYM, -1, CATEGORY));
        SPAWNER_MACRO = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("FWM: Spawner-Makro an/aus", InputConstants.Type.KEYSYM, -1, CATEGORY));
        ITEM_INFO = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("FWM: Item-Info kopieren", InputConstants.Type.KEYSYM, -1, CATEGORY));
        NEXT_PRESET = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("FWM: N\u00e4chstes Sell-Preset", InputConstants.Type.KEYSYM, -1, CATEGORY));
        Keybinds.registerContainerScreenHook();
    }

    private static void registerContainerScreenHook() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (!(screen instanceof AbstractContainerScreen)) {
                return;
            }
            ScreenKeyboardEvents.afterKeyPress((Screen)screen).register((s, input) -> {
                boolean stopKey;
                boolean bl = stopKey = SPAWNER_MACRO != null && !SPAWNER_MACRO.isUnbound() && SPAWNER_MACRO.matches(input) || OPEN_MENU != null && OPEN_MENU.matches(input);
                if (stopKey && SpawnerMacro.running()) {
                    SpawnerMacro.toggle(client);
                    return;
                }
                if (ITEM_INFO != null && !ITEM_INFO.isUnbound() && ITEM_INFO.matches(input) && ItemInfo.copy(client, (AbstractContainerScreen)s)) {
                    return;
                }
                if (TOGGLE == null) {
                    return;
                }
                if (!TOGGLE.matches(input)) {
                    return;
                }
                Keybinds.toggle();
            });
        });
    }

    private static void restore() {
        FwmConfig config = FwmConfig.INSTANCE;
        boolean changed = Keybinds.apply(OPEN_MENU, config.menuKeyBinding) | Keybinds.apply(TOGGLE, config.macroKeyBinding) | Keybinds.apply(OPEN_MESSAGES, config.messagesKeyBinding) | Keybinds.apply(OPEN_AUCTION, config.auctionKeyBinding) | Keybinds.apply(OPEN_GIVEAWAY, config.giveawayKeyBinding);
        for (KeyMapping binding : Minecraft.getInstance().options.keyMappings) {
            if (Keybinds.valid(Keybinds.key(binding.saveString()))) continue;
            binding.setKey(InputConstants.UNKNOWN);
            changed = true;
        }
        if (changed) {
            KeyMapping.resetMapping();
            Minecraft.getInstance().options.save();
        }
        Keybinds.remember();
    }

    private static boolean apply(KeyMapping binding, String wanted) {
        if (binding == null || wanted == null || wanted.isBlank()) {
            return false;
        }
        if (wanted.equals(binding.saveString())) {
            return false;
        }
        InputConstants.Key key = Keybinds.key(wanted);
        if (key == null || !Keybinds.valid(key)) {
            return false;
        }
        binding.setKey(key);
        return true;
    }

    public static InputConstants.Key key(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        try {
            return InputConstants.getKey((String)name);
        }
        catch (RuntimeException e) {
            return null;
        }
    }

    public static boolean valid(InputConstants.Key key) {
        if (key == null) {
            return false;
        }
        if (key.equals((Object)InputConstants.UNKNOWN)) {
            return true;
        }
        int code = key.getValue();
        return switch (key.getType()) {
            default -> throw new MatchException(null, null);
            case InputConstants.Type.KEYSYM -> {
                if (code >= 32 && code <= 348) {
                    yield true;
                }
                yield false;
            }
            case InputConstants.Type.MOUSE -> {
                if (code >= 0 && code <= 7) {
                    yield true;
                }
                yield false;
            }
            case InputConstants.Type.SCANCODE -> code >= 0;
        };
    }

    public static void remember() {
        String giveaway;
        FwmConfig config = FwmConfig.INSTANCE;
        String menu = OPEN_MENU == null ? "" : OPEN_MENU.saveString();
        String macro = TOGGLE == null ? "" : TOGGLE.saveString();
        String messages = OPEN_MESSAGES == null ? "" : OPEN_MESSAGES.saveString();
        String auction = OPEN_AUCTION == null ? "" : OPEN_AUCTION.saveString();
        String string = giveaway = OPEN_GIVEAWAY == null ? "" : OPEN_GIVEAWAY.saveString();
        if (menu.equals(config.menuKeyBinding) && macro.equals(config.macroKeyBinding) && messages.equals(config.messagesKeyBinding) && auction.equals(config.auctionKeyBinding) && giveaway.equals(config.giveawayKeyBinding)) {
            return;
        }
        config.menuKeyBinding = menu;
        config.macroKeyBinding = macro;
        config.messagesKeyBinding = messages;
        config.auctionKeyBinding = auction;
        config.giveawayKeyBinding = giveaway;
        ConfigManager.save();
    }

    public static boolean isTaken(KeyMapping self, InputConstants.Key key) {
        if (key == null || key.equals((Object)InputConstants.UNKNOWN)) {
            return false;
        }
        for (KeyMapping other : new KeyMapping[]{TOGGLE, OPEN_MENU, OPEN_MESSAGES, OPEN_AUCTION, OPEN_GIVEAWAY, SPAWNER_MACRO}) {
            if (other == null || other == self || !key.equals((Object)Keybinds.key(other.saveString()))) continue;
            return true;
        }
        return false;
    }

    private static void toggle() {
        if (!Features.isOn("fwm.tab.sellmacro")) {
            if (!FwmConfig.INSTANCE.enabled) {
                return;
            }
            FwmConfig.INSTANCE.enabled = false;
            ConfigManager.save();
            return;
        }
        FwmConfig.INSTANCE.enabled = !FwmConfig.INSTANCE.enabled;
        ConfigManager.save();
    }

    public static String toggleKeyName() {
        return TOGGLE == null ? "?" : TOGGLE.getTranslatedKeyMessage().getString();
    }

    public static String menuKeyName() {
        return OPEN_MENU == null ? "?" : OPEN_MENU.getTranslatedKeyMessage().getString();
    }

    public static String spawnerKeyName() {
        if (SPAWNER_MACRO == null || SPAWNER_MACRO.isUnbound()) {
            return "-";
        }
        return SPAWNER_MACRO.getTranslatedKeyMessage().getString();
    }

    public static String messagesKeyName() {
        return OPEN_MESSAGES == null ? "?" : OPEN_MESSAGES.getTranslatedKeyMessage().getString();
    }

    private static void pageKeys(Minecraft client) {
        Map<String, String> keys = V2.INSTANCE.pageKeys;
        if (keys.isEmpty()) {
            return;
        }
        if (client.gui.screen() != null) {
            HELD.clear();
            return;
        }
        for (Map.Entry<String, String> e : keys.entrySet()) {
            InputConstants.Key key = Keybinds.key(e.getValue());
            if (key == null || key.getType() != InputConstants.Type.KEYSYM || !Keybinds.valid(key) || key.equals((Object)InputConstants.UNKNOWN)) continue;
            boolean down = InputConstants.isKeyDown((Window)client.getWindow(), (int)key.getValue());
            if (!down) {
                HELD.remove(e.getKey());
                continue;
            }
            if (!HELD.add(e.getKey()) || !Features.allowedHere(e.getKey())) continue;
            client.gui.setScreen(Design.open(null, e.getKey(), -1));
            return;
        }
    }

    public static void handle(Minecraft client) {
        Screen live;
        if (TOGGLE == null) {
            return;
        }
        if (!restored) {
            restored = true;
            Keybinds.restore();
        }
        if (++watch >= 40) {
            watch = 0;
            Keybinds.remember();
        }
        while (TOGGLE.consumeClick()) {
            Keybinds.toggle();
        }
        while (OPEN_MENU.consumeClick()) {
            if (SpawnerMacro.running()) {
                SpawnerMacro.toggle(client);
                continue;
            }
            client.gui.setScreen(Design.open(client.gui.screen(), null, -1));
        }
        while (OPEN_MESSAGES.consumeClick()) {
            if (!Features.isOn("fwm.tab.messages")) continue;
            client.gui.setScreen(Design.open(client.gui.screen(), "fwm.tab.messages", -1));
        }
        while (SPAWNER_MACRO.consumeClick()) {
            SpawnerMacro.toggle(client);
        }
        Keybinds.pageKeys(client);
        while (NEXT_PRESET.consumeClick()) {
            String name;
            if (!Features.isOn("fwm.tab.sellmacro") || (name = SellPresets.next()) == null) continue;
            FwmNotice.show("FWM", "Sell-Preset", name, 8253638, 2500L);
        }
        while (OPEN_AUCTION.consumeClick()) {
            live = StreamerScreen.ifLive("fwm.tab.auction");
            client.gui.setScreen(live != null ? live : Design.open(client.gui.screen(), "fwm.tab.auction", 0));
        }
        while (OPEN_GIVEAWAY.consumeClick()) {
            live = StreamerScreen.ifLive("fwm.tab.giveaway");
            client.gui.setScreen(live != null ? live : Design.open(client.gui.screen(), "fwm.tab.auction", 1));
        }
    }

    static {
        HELD = new HashSet<String>();
    }
}

