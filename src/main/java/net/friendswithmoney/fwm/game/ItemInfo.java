/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.ItemLore
 */
package net.friendswithmoney.fwm.game;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import net.friendswithmoney.fwm.mixin.HandledScreenAccessor;
import net.friendswithmoney.fwm.ui.Lang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

public final class ItemInfo {
    private ItemInfo() {
    }

    public static boolean copy(Minecraft client, AbstractContainerScreen<?> screen) {
        Slot slot = ((HandledScreenAccessor)screen).fwm$focusedSlot();
        if (slot == null || !slot.hasItem()) {
            return false;
        }
        String text = ItemInfo.describe(screen.getTitle(), slot.getItem());
        client.keyboardHandler.setClipboard(text);
        try {
            Path file = client.gameDirectory.toPath().resolve("config").resolve("fwm-iteminfo.txt");
            Files.writeString(file, (CharSequence)(text + "\n"), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (client.player != null) {
            client.player.sendSystemMessage((Component)Component.literal((String)("\u00a7a[FWM] \u00a7f" + Lang.t("Item-Info kopiert und in config/fwm-iteminfo.txt gespeichert.", "Item info copied and saved to config/fwm-iteminfo.txt."))));
        }
        return true;
    }

    static String describe(Component title, ItemStack stack) {
        StringBuilder out = new StringBuilder();
        out.append("=== ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append(" | screen: ").append(title == null ? "-" : title.getString()).append('\n');
        out.append("id: ").append(BuiltInRegistries.ITEM.getKey((Object)stack.getItem())).append(" x").append(stack.getCount()).append('\n');
        out.append("name: ").append(stack.getHoverName().getString()).append('\n');
        Component custom = (Component)stack.get(DataComponents.CUSTOM_NAME);
        out.append("custom_name: ").append(custom == null ? "-" : custom.getString()).append('\n');
        Component itemName = (Component)stack.get(DataComponents.ITEM_NAME);
        out.append("item_name: ").append(itemName == null ? "-" : itemName.getString()).append('\n');
        ItemLore lore = (ItemLore)stack.get(DataComponents.LORE);
        if (lore != null) {
            for (Component line : lore.lines()) {
                out.append("lore: ").append(line.getString()).append('\n');
            }
        }
        out.append("components: ").append(stack.getComponents()).append('\n');
        return out.toString();
    }
}

