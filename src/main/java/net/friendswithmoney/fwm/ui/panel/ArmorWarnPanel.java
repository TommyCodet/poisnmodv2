/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package net.friendswithmoney.fwm.ui.panel;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.game.ArmorWarn;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.panel.RowsPanel;
import net.friendswithmoney.fwm.ui.widget.ButtonRow;
import net.friendswithmoney.fwm.ui.widget.TextRow;
import net.friendswithmoney.fwm.ui.widget.ToggleRow;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class ArmorWarnPanel
extends RowsPanel {
    @Override
    public Component title() {
        return Lang.of("fwm.tab.armorwarn", new Object[0]);
    }

    static Component t(String de, String en) {
        return Component.literal((String)Lang.t(de, en));
    }

    static ToggleRow toggle(String de, String en, BooleanSupplier get, Consumer<Boolean> set) {
        return new ToggleRow(ArmorWarnPanel.t(de, en), get, v -> {
            set.accept((Boolean)v);
            V2.save();
        });
    }

    static ToggleRow feature(String key) {
        return new ToggleRow(ArmorWarnPanel.t("An", "On"), () -> Features.isOn(key), v -> Features.set(key, v));
    }

    @Override
    protected void buildRows() {
        V2 c = V2.INSTANCE;
        this.rows.add(ArmorWarnPanel.feature("fwm.tab.armorwarn"));
        this.rows.add(new TextRow(ArmorWarnPanel.t("Teile", "Pieces")));
        this.rows.add(ArmorWarnPanel.toggle("Helm", "Helmet", () -> c.armorHelmet, v -> {
            c.armorHelmet = v;
        }).withIcon(new ItemStack((ItemLike)Items.NETHERITE_HELMET)));
        this.rows.add(ArmorWarnPanel.toggle("Brustpanzer", "Chestplate", () -> c.armorChest, v -> {
            c.armorChest = v;
        }).withIcon(new ItemStack((ItemLike)Items.NETHERITE_CHESTPLATE)));
        this.rows.add(ArmorWarnPanel.toggle("Hose", "Leggings", () -> c.armorLegs, v -> {
            c.armorLegs = v;
        }).withIcon(new ItemStack((ItemLike)Items.NETHERITE_LEGGINGS)));
        this.rows.add(ArmorWarnPanel.toggle("Schuhe", "Boots", () -> c.armorBoots, v -> {
            c.armorBoots = v;
        }).withIcon(new ItemStack((ItemLike)Items.NETHERITE_BOOTS)));
        this.rows.add(ArmorWarnPanel.toggle("Nebenhand", "Off hand", () -> c.armorOffhand, v -> {
            c.armorOffhand = v;
        }).withIcon(new ItemStack((ItemLike)Items.TOTEM_OF_UNDYING)));
        this.rows.add(new TextRow(ArmorWarnPanel.t("Stufen", "Steps")));
        this.rows.add(ArmorWarnPanel.toggle("20 %", "20 %", () -> c.armorAt20, v -> {
            c.armorAt20 = v;
        }));
        this.rows.add(ArmorWarnPanel.toggle("10 %", "10 %", () -> c.armorAt10, v -> {
            c.armorAt10 = v;
        }));
        this.rows.add(ArmorWarnPanel.toggle("5 %", "5 %", () -> c.armorAt5, v -> {
            c.armorAt5 = v;
        }));
        this.rows.add(ArmorWarnPanel.toggle("Ton", "Sound", () -> c.armorSound, v -> {
            c.armorSound = v;
        }));
        this.rows.add(new ButtonRow(ArmorWarnPanel.t("So sieht es aus", "How it looks"), () -> ArmorWarnPanel.t("Test-Warnung", "Test warning"), () -> ArmorWarn.test(Minecraft.getInstance())));
        this.rows.add(new TextRow(ArmorWarnPanel.t("Jedes Teil warnt pro Stufe einmal, bis es repariert ist.", "Each piece warns once per step until repaired."), null));
    }
}

