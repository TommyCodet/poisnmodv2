/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Holder$Reference
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.decoration.ArmorStand
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.equipment.trim.ArmorTrim
 *  net.minecraft.world.item.equipment.trim.TrimMaterial
 *  net.minecraft.world.item.equipment.trim.TrimMaterials
 *  net.minecraft.world.item.equipment.trim.TrimPattern
 *  net.minecraft.world.item.equipment.trim.TrimPatterns
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 */
package net.friendswithmoney.fwm.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.item.equipment.trim.TrimPatterns;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public final class ArmorTrims {
    public static final List<Base> BASES = List.of(new Base("Leder / Leather", Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS), new Base("Kette / Chain", Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS), new Base("Eisen / Iron", Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS), new Base("Gold / Golden", Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS), new Base("Diamant / Diamond", Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS), new Base("Netherite", Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS), new Base("Schildkr\u00f6te / Turtle", Items.TURTLE_HELMET, null, null, null));
    public static final List<Named<TrimPattern>> PATTERNS = List.of(new Named(TrimPatterns.SENTRY, "Sentry"), new Named(TrimPatterns.DUNE, "Dune"), new Named(TrimPatterns.COAST, "Coast"), new Named(TrimPatterns.WILD, "Wild"), new Named(TrimPatterns.WARD, "Ward"), new Named(TrimPatterns.EYE, "Eye"), new Named(TrimPatterns.VEX, "Vex"), new Named(TrimPatterns.TIDE, "Tide"), new Named(TrimPatterns.SNOUT, "Snout"), new Named(TrimPatterns.RIB, "Rib"), new Named(TrimPatterns.SPIRE, "Spire"), new Named(TrimPatterns.WAYFINDER, "Wayfinder"), new Named(TrimPatterns.SHAPER, "Shaper"), new Named(TrimPatterns.SILENCE, "Silence"), new Named(TrimPatterns.RAISER, "Raiser"), new Named(TrimPatterns.HOST, "Host"), new Named(TrimPatterns.FLOW, "Flow"), new Named(TrimPatterns.BOLT, "Bolt"));
    public static final List<Named<TrimMaterial>> MATERIALS = List.of(new Named(TrimMaterials.QUARTZ, "Quarz / Quartz"), new Named(TrimMaterials.IRON, "Eisen / Iron"), new Named(TrimMaterials.NETHERITE, "Netherite"), new Named(TrimMaterials.REDSTONE, "Redstone"), new Named(TrimMaterials.COPPER, "Kupfer / Copper"), new Named(TrimMaterials.GOLD, "Gold"), new Named(TrimMaterials.EMERALD, "Smaragd / Emerald"), new Named(TrimMaterials.DIAMOND, "Diamant / Diamond"), new Named(TrimMaterials.LAPIS, "Lapis"), new Named(TrimMaterials.AMETHYST, "Amethyst"), new Named(TrimMaterials.RESIN, "Harz / Resin"));
    private static final int[] base = new int[]{4, 4, 4, 4};
    private static final int[] pattern = new int[]{-1, -1, -1, -1};
    private static final int[] material = new int[]{5, 5, 5, 5};

    private ArmorTrims() {
    }

    public static int base(Slot s) {
        return base[s.ordinal()];
    }

    public static int pattern(Slot s) {
        return pattern[s.ordinal()];
    }

    public static int material(Slot s) {
        return material[s.ordinal()];
    }

    public static void setBase(Slot s, int v) {
        int b = ArmorTrims.clamp(v, BASES.size());
        ArmorTrims.base[s.ordinal()] = b >= 0 && !ArmorTrims.fits(b, s) ? -1 : b;
    }

    public static boolean fits(int b, Slot s) {
        return b >= 0 && b < BASES.size() && BASES.get(b).item(s) != null;
    }

    private static int randomBase(Random r, Slot s) {
        ArrayList<Integer> ok = new ArrayList<Integer>();
        for (int i = 0; i < BASES.size(); ++i) {
            if (!ArmorTrims.fits(i, s)) continue;
            ok.add(i);
        }
        return ok.isEmpty() ? -1 : (Integer)ok.get(r.nextInt(ok.size()));
    }

    public static void setPattern(Slot s, int v) {
        ArmorTrims.pattern[s.ordinal()] = v < 0 ? -1 : Math.min(v, PATTERNS.size() - 1);
    }

    public static void setMaterial(Slot s, int v) {
        ArmorTrims.material[s.ordinal()] = v < 0 ? -1 : Math.min(v, MATERIALS.size() - 1);
    }

    private static int clamp(int v, int size) {
        return v < 0 ? -1 : Math.min(v, size - 1);
    }

    public static void reset() {
        for (Slot s : Slot.values()) {
            ArmorTrims.base[s.ordinal()] = -1;
            ArmorTrims.pattern[s.ordinal()] = -1;
            ArmorTrims.material[s.ordinal()] = -1;
        }
    }

    public static void randomize() {
        Random r = new Random();
        for (Slot s : Slot.values()) {
            ArmorTrims.base[s.ordinal()] = ArmorTrims.randomBase(r, s);
            ArmorTrims.pattern[s.ordinal()] = r.nextInt(PATTERNS.size());
            ArmorTrims.material[s.ordinal()] = r.nextInt(MATERIALS.size());
        }
    }

    public static void randomize(Slot s) {
        Random r = new Random();
        ArmorTrims.base[s.ordinal()] = ArmorTrims.randomBase(r, s);
        ArmorTrims.pattern[s.ordinal()] = r.nextInt(PATTERNS.size());
        ArmorTrims.material[s.ordinal()] = r.nextInt(MATERIALS.size());
    }

    public static void clear(Slot s) {
        ArmorTrims.base[s.ordinal()] = -1;
        ArmorTrims.pattern[s.ordinal()] = -1;
        ArmorTrims.material[s.ordinal()] = -1;
    }

    public static ItemStack stack(Slot s) {
        int b = base[s.ordinal()];
        if (b < 0) {
            return ItemStack.EMPTY;
        }
        Item item = BASES.get(b).item(s);
        if (item == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack((ItemLike)item);
        int p = pattern[s.ordinal()];
        int m = material[s.ordinal()];
        if (p >= 0 && m >= 0) {
            Minecraft client = Minecraft.getInstance();
            if (client.level != null) {
                RegistryAccess drm = client.level.registryAccess();
                Holder.Reference pat = drm.lookupOrThrow(Registries.TRIM_PATTERN).getOrThrow(PATTERNS.get(p).key());
                Holder.Reference mat = drm.lookupOrThrow(Registries.TRIM_MATERIAL).getOrThrow(MATERIALS.get(m).key());
                stack.set(DataComponents.TRIM, (Object)new ArmorTrim((Holder)mat, (Holder)pat));
            }
        }
        return stack;
    }

    public static ArmorStand stand() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return null;
        }
        ArmorStand stand = new ArmorStand((Level)client.level, 0.0, 0.0, 0.0);
        stand.setInvisible(true);
        stand.setId(2000000001);
        for (Slot s : Slot.values()) {
            stand.setItemSlot(s.vanilla, ArmorTrims.stack(s));
        }
        return stand;
    }

    public static enum Slot {
        HEAD(EquipmentSlot.HEAD, "Helm", "Helmet"),
        CHEST(EquipmentSlot.CHEST, "Brust", "Chestplate"),
        LEGS(EquipmentSlot.LEGS, "Beine", "Leggings"),
        FEET(EquipmentSlot.FEET, "Schuhe", "Boots");

        public final EquipmentSlot vanilla;
        public final String de;
        public final String en;

        private Slot(EquipmentSlot vanilla, String de, String en) {
            this.vanilla = vanilla;
            this.de = de;
            this.en = en;
        }
    }

    public record Base(String name, Item head, Item chest, Item legs, Item feet) {
        Item item(Slot slot) {
            return switch (slot.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> this.head;
                case 1 -> this.chest;
                case 2 -> this.legs;
                case 3 -> this.feet;
            };
        }
    }

    public record Named<T>(ResourceKey<T> key, String label) {
    }
}

