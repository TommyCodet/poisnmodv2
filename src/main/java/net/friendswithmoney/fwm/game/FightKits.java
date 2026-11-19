/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.InventoryMenu
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package net.friendswithmoney.fwm.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.friendswithmoney.fwm.config.ConfigManager;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.mixin.HandledScreenAccessor;
import net.friendswithmoney.fwm.ui.FwmTheme;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.ScreenScale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class FightKits {
    public static final int OFFHAND = 40;
    private static final Pattern TIERED = Pattern.compile("^(netherite|diamond|iron|golden|stone|wooden|copper|leather|chainmail)_(sword|axe|pickaxe|shovel|hoe|spear|helmet|chestplate|leggings|boots)$");
    private static int[] toggleBox;
    private static int[] kitBox;
    private static final Map<String, ItemStack> STACKS;

    public static boolean kitSlot(int index) {
        return index >= 0 && index < 36 || index == 40;
    }

    private FightKits() {
    }

    public static String kind(String id) {
        if (id == null) {
            return "";
        }
        String path = id.startsWith("minecraft:") ? id.substring(10) : id;
        Matcher m = TIERED.matcher(path);
        return m.matches() ? "*_" + m.group(2) : path;
    }

    public static List<Missing> missing(Map<Integer, FwmConfig.KitSlot> slots, Map<String, Integer> have) {
        HashMap haveByKind = new HashMap();
        have.forEach((id, n) -> haveByKind.merge(FightKits.kind(id), n, Integer::sum));
        LinkedHashMap<String, Integer> need = new LinkedHashMap<String, Integer>();
        HashMap sample = new HashMap();
        slots.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> {
            FwmConfig.KitSlot slot = (FwmConfig.KitSlot)e.getValue();
            if (slot == null || slot.item == null || slot.item.isBlank()) {
                return;
            }
            String k = FightKits.kind(slot.item);
            need.merge(k, FightKits.count(slot), Integer::sum);
            sample.putIfAbsent(k, slot.item);
        });
        ArrayList<Missing> out = new ArrayList<Missing>();
        need.forEach((k, n) -> {
            int lack = n - haveByKind.getOrDefault(k, 0);
            if (lack > 0) {
                out.add(new Missing((String)sample.get(k), lack));
            }
        });
        return out;
    }

    public static FwmConfig.FightKit selected() {
        List<FwmConfig.FightKit> kits = FwmConfig.INSTANCE.fightKits;
        if (kits == null || kits.isEmpty()) {
            return null;
        }
        int i = Math.max(0, Math.min(kits.size() - 1, FwmConfig.INSTANCE.fightKitSelected));
        return kits.get(i);
    }

    private static Map<String, Integer> have(Inventory inv) {
        HashMap<String, Integer> out = new HashMap<String, Integer>();
        for (int i = 0; i < 36; ++i) {
            FightKits.count(out, inv.getItem(i));
        }
        FightKits.count(out, inv.getItem(40));
        return out;
    }

    private static void count(Map<String, Integer> out, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        out.merge(BuiltInRegistries.ITEM.getKey((Object)stack.getItem()).toString(), stack.getCount(), Integer::sum);
    }

    public static void capture(FwmConfig.FightKit kit, Inventory inv) {
        kit.slots.clear();
        for (int i = 0; i < 36; ++i) {
            FightKits.put(kit, i, inv.getItem(i));
        }
        FightKits.put(kit, 40, inv.getItem(40));
    }

    private static void put(FwmConfig.FightKit kit, int index, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        FwmConfig.KitSlot slot = new FwmConfig.KitSlot();
        slot.item = BuiltInRegistries.ITEM.getKey((Object)stack.getItem()).toString();
        slot.count = stack.getCount();
        kit.slots.put(index, slot);
    }

    public static ItemStack stack(String id, int count) {
        ItemStack base = STACKS.computeIfAbsent(id == null ? "" : id, key -> {
            try {
                Identifier ident = Identifier.tryParse((String)key);
                if (ident == null || !BuiltInRegistries.ITEM.containsKey(ident)) {
                    return ItemStack.EMPTY;
                }
                Item item = (Item)BuiltInRegistries.ITEM.getValue(ident);
                return new ItemStack((ItemLike)item);
            }
            catch (Exception e) {
                return null;
            }
        });
        if (base == null) {
            STACKS.remove(id == null ? "" : id);
            return ItemStack.EMPTY;
        }
        return base.isEmpty() ? ItemStack.EMPTY : base.copyWithCount(Math.max(1, Math.min(base.getMaxStackSize(), count)));
    }

    public static int maxCount(String id) {
        ItemStack s = FightKits.stack(id, 1);
        return s.isEmpty() ? 64 : Math.max(1, s.getMaxStackSize());
    }

    public static int count(FwmConfig.KitSlot slot) {
        return Math.max(1, Math.min(FightKits.maxCount(slot.item), slot.count));
    }

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (!(screen instanceof InventoryScreen)) {
                return;
            }
            InventoryScreen inventory = (InventoryScreen)screen;
            ScreenEvents.afterExtract((Screen)screen).register((s, ctx, mouseX, mouseY, delta) -> ScreenScale.run(s, ctx, mouseX, mouseY, (ctx2, mouseX2, mouseY2) -> FightKits.render(client, inventory, ctx2, mouseX2, mouseY2)));
            ScreenMouseEvents.allowMouseClick((Screen)screen).register((s, click) -> !FightKits.click(click.x(), click.y(), click.button()));
        });
    }

    private static boolean active() {
        return Features.isOn("fwm.tab.fightkit") && FightKits.selected() != null;
    }

    private static boolean click(double x, double y, int button) {
        if (!FightKits.active() || button != 0) {
            return false;
        }
        if (FightKits.inside(toggleBox, x, y)) {
            FwmConfig.INSTANCE.fightKitShowInInventory = !FwmConfig.INSTANCE.fightKitShowInInventory;
            ConfigManager.save();
            return true;
        }
        if (FightKits.inside(kitBox, x, y)) {
            List<FwmConfig.FightKit> kits = FwmConfig.INSTANCE.fightKits;
            FwmConfig.INSTANCE.fightKitSelected = (FwmConfig.INSTANCE.fightKitSelected + 1) % kits.size();
            ConfigManager.save();
            return true;
        }
        return false;
    }

    private static boolean inside(int[] box, double x, double y) {
        return box != null && x >= (double)box[0] && x < (double)(box[0] + box[2]) && y >= (double)box[1] && y < (double)(box[1] + box[3]);
    }

    private static void render(Minecraft client, InventoryScreen screen, GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
        toggleBox = null;
        kitBox = null;
        if (!FightKits.active() || client.player == null) {
            return;
        }
        Font font = client.font;
        HandledScreenAccessor at = (HandledScreenAccessor)screen;
        FwmConfig.FightKit kit = FightKits.selected();
        int invX = at.fwm$x();
        int invY = at.fwm$y();
        int effects = client.player.getActiveEffects().size();
        int effectsH = effects == 0 ? 0 : (effects > 5 ? 166 : effects * 33);
        int left = invX + 176 + 4;
        int w = Math.max(90, Math.min(150, screen.width - left - 4));
        int top = invY + effectsH + (effects > 0 ? 4 : 0);
        int listX = left;
        int listY = top + 34;
        int listW = w;
        boolean showing = FwmConfig.INSTANCE.fightKitShowInInventory;
        String label = Lang.str(showing ? "fwm.fightkit.hide" : "fwm.fightkit.show", new Object[0]);
        toggleBox = FightKits.button(ctx, font, left, top, w, font.plainSubstrByWidth(label, w - 6), mouseX, mouseY, true);
        int kx = left;
        int ky = top + 16;
        String kitLabel = "< " + font.plainSubstrByWidth(kit.name == null ? "Kit" : kit.name, w - 24) + " >";
        kitBox = FightKits.button(ctx, font, kx, ky, w, kitLabel, mouseX, mouseY, FwmConfig.INSTANCE.fightKits.size() > 1);
        if (!showing) {
            return;
        }
        Inventory inv = client.player.getInventory();
        List<Missing> missing = FightKits.missing(kit.slots, FightKits.have(inv));
        if (listW > 0) {
            int ly = listY;
            if (missing.isEmpty()) {
                ctx.text(font, Lang.str("fwm.fightkit.complete", new Object[0]), listX, ly, -10688364);
            } else {
                ctx.text(font, Lang.str("fwm.fightkit.missing", new Object[0]), listX, ly, -1017230);
                ly += 11;
                for (Missing m : missing) {
                    ItemStack stack = FightKits.stack(m.item(), 1);
                    if (!stack.isEmpty()) {
                        ctx.item(stack, listX, ly - 2);
                    }
                    String name = stack.isEmpty() ? m.item() : stack.getHoverName().getString();
                    ctx.text(font, font.plainSubstrByWidth(m.count() + "x " + name, listW - 18), listX + 18, ly + 2, FwmTheme.TEXT);
                    if ((ly += 17) <= screen.height - 12) continue;
                    break;
                }
            }
        }
        for (Slot slot : ((InventoryMenu)screen.getMenu()).slots) {
            ItemStack ghost;
            int index;
            FwmConfig.KitSlot want;
            if (!(slot.container instanceof Inventory) || (want = kit.slots.get(index = slot.getContainerSlot())) == null || want.item == null || !FightKits.kitSlot(index)) continue;
            ItemStack has = inv.getItem(index);
            boolean right = !has.isEmpty() && FightKits.kind(BuiltInRegistries.ITEM.getKey((Object)has.getItem()).toString()).equals(FightKits.kind(want.item));
            int sx = at.fwm$x() + slot.x;
            int sy = at.fwm$y() + slot.y;
            if (right) {
                int need = FightKits.count(want);
                if (need <= 1 || has.getCount() >= need) continue;
                int fillW = Math.max(1, Math.round(16.0f * (float)has.getCount() / (float)need));
                ctx.fill(sx, sy + 15, sx + 16, sy + 16, Integer.MIN_VALUE);
                ctx.fill(sx, sy + 15, sx + fillW, sy + 16, -673493);
                continue;
            }
            if (has.isEmpty() && !(ghost = FightKits.stack(want.item, Math.max(1, FightKits.count(want)))).isEmpty()) {
                ctx.item(ghost, sx, sy);
                ctx.fill(sx, sy, sx + 16, sy + 16, -1601467509);
            }
            int red = has.isEmpty() ? -1713026995 : -1750963;
            ctx.fill(sx - 1, sy - 1, sx + 17, sy, red);
            ctx.fill(sx - 1, sy + 16, sx + 17, sy + 17, red);
            ctx.fill(sx - 1, sy, sx, sy + 16, red);
            ctx.fill(sx + 16, sy, sx + 17, sy + 16, red);
        }
    }

    private static int[] button(GuiGraphicsExtractor ctx, Font font, int x, int y, int w, String label, int mouseX, int mouseY, boolean enabled) {
        boolean over = enabled && mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + 14;
        ctx.fill(x, y, x + w, y + 14, over ? -534754765 : -804250602);
        int edge = over ? -10688364 : -13739454;
        ctx.fill(x, y, x + w, y + 1, edge);
        ctx.fill(x, y + 13, x + w, y + 14, edge);
        ctx.fill(x, y, x + 1, y + 14, edge);
        ctx.fill(x + w - 1, y, x + w, y + 14, edge);
        ctx.text(font, label, x + (w - font.width(label)) / 2, y + 3, enabled ? FwmTheme.TEXT : FwmTheme.TEXT_DIM);
        return new int[]{x, y, w, 14};
    }

    static {
        STACKS = new HashMap<String, ItemStack>();
    }

    public record Missing(String item, int count) {
    }
}

