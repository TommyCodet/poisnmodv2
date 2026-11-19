/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.core.Holder
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package net.friendswithmoney.fwm.game;

import java.util.EnumMap;
import java.util.Map;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.ui.FwmNotice;
import net.friendswithmoney.fwm.ui.Lang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public final class ArmorWarn {
    private static final EquipmentSlot[] SLOTS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.OFFHAND};
    private static final int[] STEPS = new int[]{20, 10, 5};
    private static final long SOUND_GAP_MS = 3000L;
    private static final Map<EquipmentSlot, Integer> WARNED = new EnumMap<EquipmentSlot, Integer>(EquipmentSlot.class);
    private static long soundAt;
    private static int ticks;

    private ArmorWarn() {
    }

    private static boolean watched(EquipmentSlot slot) {
        V2 c = V2.INSTANCE;
        return switch (slot) {
            case EquipmentSlot.HEAD -> c.armorHelmet;
            case EquipmentSlot.CHEST -> c.armorChest;
            case EquipmentSlot.LEGS -> c.armorLegs;
            case EquipmentSlot.FEET -> c.armorBoots;
            case EquipmentSlot.OFFHAND -> c.armorOffhand;
            default -> false;
        };
    }

    private static boolean stepOn(int step) {
        V2 c = V2.INSTANCE;
        return step == 20 ? c.armorAt20 : (step == 10 ? c.armorAt10 : c.armorAt5);
    }

    public static void tick(Minecraft client) {
        if (client.player == null || !Features.isOn("fwm.tab.armorwarn") || ++ticks % 10 != 0) {
            return;
        }
        block0: for (EquipmentSlot slot : SLOTS) {
            int warned;
            ItemStack stack = client.player.getItemBySlot(slot);
            if (!ArmorWarn.watched(slot) || stack.isEmpty() || !stack.isDamageableItem()) {
                WARNED.remove(slot);
                continue;
            }
            int max = stack.getMaxDamage();
            int percent = (int)Math.floor((double)(max - stack.getDamageValue()) * 100.0 / (double)max);
            if (percent > (warned = WARNED.getOrDefault(slot, 100).intValue())) {
                WARNED.put(slot, 100);
                warned = 100;
            }
            for (int step : STEPS) {
                if (percent > step || step >= warned || !ArmorWarn.stepOn(step)) continue;
                int lowest = step;
                for (int s : STEPS) {
                    if (percent > s || !ArmorWarn.stepOn(s)) continue;
                    lowest = Math.min(lowest, s);
                }
                WARNED.put(slot, lowest);
                ArmorWarn.warn(client, slot, stack, percent);
                continue block0;
            }
        }
    }

    private static String name(EquipmentSlot slot) {
        return switch (slot) {
            case EquipmentSlot.HEAD -> Lang.t("Helm", "Helmet");
            case EquipmentSlot.CHEST -> Lang.t("Brustpanzer", "Chestplate");
            case EquipmentSlot.LEGS -> Lang.t("Hose", "Leggings");
            case EquipmentSlot.FEET -> Lang.t("Schuhe", "Boots");
            default -> Lang.t("Nebenhand", "Off hand");
        };
    }

    private static void warn(Minecraft client, EquipmentSlot slot, ItemStack stack, int percent) {
        FwmNotice.show("FWM", Lang.t("Fast kaputt: ", "Almost broken: ") + ArmorWarn.name(slot), stack.getHoverName().getString() + " \u00b7 " + percent + " %", percent <= 5 ? 0xFF5555 : 0xFFAA00, 4500L);
        long now = System.currentTimeMillis();
        if (V2.INSTANCE.armorSound && now - soundAt > 3000L) {
            soundAt = now;
            client.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((Holder)SoundEvents.ITEM_BREAK, (float)0.8f));
        }
    }

    public static void test(Minecraft client) {
        if (client.player == null) {
            return;
        }
        ItemStack chest = client.player.getItemBySlot(EquipmentSlot.CHEST);
        ArmorWarn.warn(client, EquipmentSlot.CHEST, chest.isEmpty() ? new ItemStack((ItemLike)Items.NETHERITE_CHESTPLATE) : chest, 9);
    }
}

