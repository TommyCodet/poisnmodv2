/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
 *  net.fabricmc.fabric.api.event.player.UseBlockCallback
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.ContainerScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.level.block.BarrelBlock
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.ChestBlock
 *  net.minecraft.world.level.block.EnderChestBlock
 *  net.minecraft.world.level.block.ShulkerBoxBlock
 */
package net.friendswithmoney.fwm.shop;

import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.friendswithmoney.fwm.net.ServerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;

public final class MarketScreens {
    private static final Pattern ENDER_CHEST_COMMAND = Pattern.compile("^(ec|ecs|ecsee|echest|ender|enderchest|endersee|enderchestsee)(\\s.*)?$", 2);
    private static final long WINDOW_MS = 3000L;
    private static long pendingUntil;
    private static Screen allowed;

    private MarketScreens() {
    }

    public static void register() {
        ClientSendMessageEvents.COMMAND.register(command -> {
            if (command != null && ENDER_CHEST_COMMAND.matcher(command.trim()).matches()) {
                pendingUntil = System.currentTimeMillis() + 3000L;
            }
        });
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (world.isClientSide() && MarketScreens.opensStorage(world.getBlockState(hit.getBlockPos()).getBlock())) {
                pendingUntil = System.currentTimeMillis() + 3000L;
            }
            return InteractionResult.PASS;
        });
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen == allowed) {
                return;
            }
            if (!(screen instanceof ContainerScreen) && !(screen instanceof ShulkerBoxScreen)) {
                return;
            }
            if (System.currentTimeMillis() < pendingUntil) {
                pendingUntil = 0L;
                allowed = screen;
            }
        });
    }

    private static boolean opensStorage(Block block) {
        return block instanceof ChestBlock || block instanceof BarrelBlock || block instanceof EnderChestBlock || block instanceof ShulkerBoxBlock;
    }

    public static boolean allowed() {
        Minecraft client = Minecraft.getInstance();
        return MarketScreens.onHugoSmp(client) && MarketScreens.allowedHere(client.gui.screen());
    }

    private static boolean allowedHere(Screen screen) {
        return screen instanceof InventoryScreen || screen != null && screen == allowed;
    }

    private static boolean onHugoSmp(Minecraft client) {
        return ServerContext.hugo();
    }
}

