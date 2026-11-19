/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.InputConstants$Key
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
 *  net.minecraft.client.CameraType
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.KeyMapping$Category
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.Options
 *  net.minecraft.client.gui.screens.ChatScreen
 *  net.minecraft.client.gui.screens.PauseScreen
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.player.LocalPlayer
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.friendswithmoney.fwm.lyrics;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.friendswithmoney.fwm.lyrics.AfkConfig;
import net.friendswithmoney.fwm.lyrics.CinemaShots;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AfkCinema {
    public static final Logger LOG = LoggerFactory.getLogger((String)"FWM AFK Cinema");
    public static AfkConfig cfg;
    private static KeyMapping toggleKey;
    private static long lastInputMs;
    private static boolean active;
    private static boolean manual;
    private static long startMs;
    private static long afkSinceMs;
    private static float baseYaw;
    private static CameraType prevCamera;
    private static boolean prevHideGui;
    private static boolean pendingStart;

    private AfkCinema() {
    }

    public static void init() {
        cfg = AfkConfig.load();
        toggleKey = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("FWM: Kamerafahrt an/aus", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KeyMapping.Category.MISC));
        ClientTickEvents.END_CLIENT_TICK.register(AfkCinema::tick);
    }

    public static boolean isActive() {
        return active;
    }

    public static long startMs() {
        return startMs;
    }

    public static long afkSinceMs() {
        return afkSinceMs;
    }

    public static float baseYaw() {
        return baseYaw;
    }

    public static KeyMapping toggleKey() {
        return toggleKey;
    }

    public static void requestStart() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        pendingStart = true;
        mc.gui.setScreen(null);
    }

    public static String keyName() {
        if (toggleKey == null || toggleKey.isUnbound()) {
            return null;
        }
        return toggleKey.getTranslatedKeyMessage().getString();
    }

    public static boolean isAfkScreen(Screen screen) {
        return screen instanceof ChatScreen || screen instanceof PauseScreen;
    }

    private static boolean screenOk(Minecraft mc) {
        return mc.gui.screen() == null || AfkCinema.cfg.afkInChat && AfkCinema.isAfkScreen(mc.gui.screen());
    }

    private static boolean isToggle(InputConstants.Type type, int code) {
        if (toggleKey == null) {
            return false;
        }
        InputConstants.Key bound = KeyMappingHelper.getBoundKeyOf((KeyMapping)toggleKey);
        return bound.getType() == type && bound.getValue() == code;
    }

    public static void onKey(int key) {
        if (AfkCinema.isToggle(InputConstants.Type.KEYSYM, key)) {
            return;
        }
        AfkCinema.onInput(true);
    }

    public static void onButton(int button) {
        if (AfkCinema.isToggle(InputConstants.Type.MOUSE, button)) {
            return;
        }
        AfkCinema.onInput(true);
    }

    public static void onLook() {
        AfkCinema.onInput(false);
    }

    private static void onInput(boolean hard) {
        lastInputMs = System.currentTimeMillis();
        if (active && (hard || !manual)) {
            AfkCinema.stop();
        }
    }

    private static void tick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null) {
            if (active) {
                AfkCinema.stop();
            }
            lastInputMs = System.currentTimeMillis();
            while (toggleKey.consumeClick()) {
            }
            return;
        }
        while (toggleKey.consumeClick()) {
            if (active) {
                AfkCinema.stop();
                continue;
            }
            if (!AfkCinema.screenOk(mc) || mc.getCameraEntity() != player || !player.isAlive()) continue;
            AfkCinema.start(mc, player, true);
        }
        if (pendingStart) {
            pendingStart = false;
            if (!active && mc.gui.screen() == null && mc.getCameraEntity() == player && player.isAlive()) {
                AfkCinema.start(mc, player, true);
                lastInputMs = System.currentTimeMillis();
                return;
            }
        }
        if (active) {
            if (!AfkCinema.screenOk(mc) || player.hurtTime > 0 || !player.isAlive() || mc.getCameraEntity() != player) {
                AfkCinema.stop();
            }
        } else {
            Options o = mc.options;
            if (o.keyUp.isDown() || o.keyDown.isDown() || o.keyLeft.isDown() || o.keyRight.isDown() || o.keyJump.isDown() || o.keyShift.isDown() || o.keyAttack.isDown() || o.keyUse.isDown()) {
                lastInputMs = System.currentTimeMillis();
            }
            if (AfkCinema.cfg.enabled && AfkCinema.screenOk(mc) && mc.getCameraEntity() == player && System.currentTimeMillis() - lastInputMs >= (long)AfkCinema.cfg.delaySeconds * 1000L) {
                AfkCinema.start(mc, player, false);
            }
        }
    }

    private static void start(Minecraft mc, LocalPlayer player, boolean byHand) {
        active = true;
        manual = byHand;
        startMs = System.currentTimeMillis();
        afkSinceMs = byHand ? startMs : lastInputMs;
        baseYaw = player.yBodyRot;
        prevCamera = mc.options.getCameraType();
        prevHideGui = mc.gui.hud.isHidden;
        mc.gui.hud.isHidden = true;
        mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        CinemaShots.reset();
    }

    private static void stop() {
        active = false;
        manual = false;
        lastInputMs = System.currentTimeMillis();
        Minecraft mc = Minecraft.getInstance();
        if (prevCamera != null) {
            mc.options.setCameraType(prevCamera);
            mc.gui.hud.isHidden = prevHideGui;
        }
        prevCamera = null;
    }

    static {
        lastInputMs = System.currentTimeMillis();
    }
}

