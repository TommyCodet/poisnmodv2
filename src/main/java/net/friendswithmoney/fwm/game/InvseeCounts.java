/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package net.friendswithmoney.fwm.game;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.friendswithmoney.fwm.config.ConfigManager;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.mixin.HandledScreenAccessor;
import net.friendswithmoney.fwm.ui.FwmGfx;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.LauncherGfx;
import net.friendswithmoney.fwm.ui.ScreenScale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public final class InvseeCounts {
    private static final Pattern COMMAND = Pattern.compile("^(invsee|inv|openinv)\\s+([A-Za-z0-9_]{1,16}).*$", 2);
    private static final long WINDOW_MS = 3000L;
    private static final Item[] ITEMS = new Item[]{Items.TOTEM_OF_UNDYING, Items.END_CRYSTAL, Items.OBSIDIAN, Items.RESPAWN_ANCHOR, Items.GLOWSTONE, Items.COBWEB, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, Items.ENDER_PEARL, Items.EXPERIENCE_BOTTLE, Items.MACE, Items.WIND_CHARGE, Items.BREEZE_ROD, Items.TRIDENT, Items.SHIELD, Items.WATER_BUCKET, Items.LAVA_BUCKET, Items.ANCIENT_DEBRIS};
    private static final ItemStack[] STACKS = new ItemStack[ITEMS.length];
    private static long pendingUntil;
    private static String pendingName;
    private static AbstractContainerScreen<?> shown;
    private static String shownName;
    private static boolean editing;
    private static final List<int[]> hits;
    private static final int CLOSE = -1;
    private static final int GEAR = -2;
    private static final int REOPEN = -3;
    private static final int ZERO = -4;

    private InvseeCounts() {
    }

    public static void register() {
        ClientSendMessageEvents.COMMAND.register(command -> {
            if (command == null) {
                return;
            }
            Matcher m = COMMAND.matcher(command.trim());
            if (!m.matches()) {
                return;
            }
            pendingName = m.group(2);
            pendingUntil = System.currentTimeMillis() + 3000L;
        });
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (!(screen instanceof AbstractContainerScreen)) {
                return;
            }
            AbstractContainerScreen handled = (AbstractContainerScreen)screen;
            if (screen != shown) {
                if (System.currentTimeMillis() >= pendingUntil) {
                    return;
                }
                pendingUntil = 0L;
                shown = handled;
                shownName = pendingName;
            }
            if (!Features.allowedHere("fwm.tab.fightkit")) {
                return;
            }
            ScreenEvents.afterExtract((Screen)screen).register((s, ctx, mouseX, mouseY, delta) -> ScreenScale.run(s, ctx, mouseX, mouseY, (ctx2, mouseX2, mouseY2) -> InvseeCounts.render(client, handled, ctx2, mouseX2, mouseY2)));
            ScreenMouseEvents.allowMouseClick((Screen)screen).register((s, click) -> s != shown || !InvseeCounts.click(click.x(), click.y()));
            ScreenEvents.remove((Screen)screen).register(s -> {
                if (s == shown) {
                    shown = null;
                    editing = false;
                }
            });
        });
    }

    private static String id(int i) {
        return BuiltInRegistries.ITEM.getKey((Object)ITEMS[i]).toString();
    }

    private static boolean hidden(int i) {
        return V2.INSTANCE.invseeHiddenItems != null && V2.INSTANCE.invseeHiddenItems.contains(InvseeCounts.id(i));
    }

    private static boolean click(double mx, double my) {
        for (int[] h : hits) {
            if (mx < (double)h[0] || my < (double)h[1] || mx >= (double)(h[0] + h[2]) || my >= (double)(h[1] + h[3])) continue;
            int a = h[4];
            if (a == -1) {
                FwmConfig.INSTANCE.invseeCounts = false;
                editing = false;
                ConfigManager.save();
            } else if (a == -3) {
                FwmConfig.INSTANCE.invseeCounts = true;
                ConfigManager.save();
            } else if (a == -2) {
                editing = !editing;
            } else if (a == -4) {
                V2.INSTANCE.invseeShowZero = !V2.INSTANCE.invseeShowZero;
                V2.save();
            } else if (a >= 0) {
                String id;
                if (V2.INSTANCE.invseeHiddenItems == null) {
                    V2.INSTANCE.invseeHiddenItems = new ArrayList<String>();
                }
                if (!V2.INSTANCE.invseeHiddenItems.remove(id = InvseeCounts.id(a))) {
                    V2.INSTANCE.invseeHiddenItems.add(id);
                }
                V2.save();
            }
            return true;
        }
        return false;
    }

    private static void render(Minecraft client, AbstractContainerScreen<?> screen, GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
        boolean hg;
        hits.clear();
        if (screen != shown || client.player == null) {
            return;
        }
        Font font = client.font;
        HandledScreenAccessor at = (HandledScreenAccessor)screen;
        int x = at.fwm$x() + at.fwm$backgroundWidth() + 4;
        int y = at.fwm$y();
        if (!FwmConfig.INSTANCE.invseeCounts) {
            String label = Lang.t("Z\u00e4hler", "Counter");
            int pw = font.width(label) + 12;
            if (x + pw > screen.width - 2) {
                return;
            }
            boolean hover = mouseX >= x && mouseY >= y && mouseX < x + pw && mouseY < y + 14;
            LauncherGfx.round(ctx, x, y, pw, 14, 5, hover ? -535348688 : -1341185762);
            LauncherGfx.ring(ctx, x, y, pw, 14, 5, hover ? -1434586938 : 1149104326);
            ctx.text(font, label, x + 6, y + 3, hover ? -1379602 : -6310484, false);
            hits.add(new int[]{x, y, pw, 14, -3});
            return;
        }
        int[] counts = new int[ITEMS.length];
        for (Slot slot : screen.getMenu().slots) {
            ItemStack stack;
            if (slot.container == client.player.getInventory() || (stack = slot.getItem()).isEmpty()) continue;
            for (int i = 0; i < ITEMS.length; ++i) {
                if (!stack.is((Object)ITEMS[i])) continue;
                int n = i;
                counts[n] = counts[n] + stack.getCount();
            }
        }
        boolean[] row = new boolean[ITEMS.length];
        int rows = 0;
        for (int i = 0; i < ITEMS.length; ++i) {
            boolean bl = row[i] = editing || !InvseeCounts.hidden(i) && (i == 0 || counts[i] > 0 || V2.INSTANCE.invseeShowZero);
            if (!row[i]) continue;
            ++rows;
        }
        int w = Math.max(84, font.width(shownName) + 44);
        int head = editing ? 34 : 22;
        int cols = head + rows * 18 + 4 > screen.height - y - 2 ? 2 : 1;
        int colW = 84;
        if (cols == 2) {
            w = Math.max(w, colW * 2 + 4);
        }
        int perCol = Math.max(1, (rows + cols - 1) / cols);
        int h = head + perCol * 18 + 4;
        if (x + w > screen.width - 2) {
            return;
        }
        ctx.pose().pushMatrix();
        LauncherGfx.round(ctx, x, y, w, h, 6, -535879394);
        LauncherGfx.ring(ctx, x, y, w, h, 6, -2005012282);
        FwmGfx.playerHead(ctx, shownName, x + 6, y + 6, 10);
        ctx.text(font, shownName, x + 20, y + 7, -1379602, false);
        int bx = x + w - 13;
        boolean hx = mouseX >= bx && mouseY >= y + 4 && mouseX < bx + 10 && mouseY < y + 15;
        ctx.text(font, "\u00d7", bx + 2, y + 6, hx ? -1017230 : -6310484, false);
        hits.add(new int[]{bx, y + 4, 10, 11, -1});
        int gx = bx - 12;
        boolean bl = hg = mouseX >= gx && mouseY >= y + 4 && mouseX < gx + 10 && mouseY < y + 15;
        ctx.text(font, "\u2699", gx + 1, y + 6, editing ? -8523578 : (hg ? -1379602 : -6310484), false);
        hits.add(new int[]{gx, y + 4, 10, 11, -2});
        LauncherGfx.round(ctx, (float)(x + 6), (float)(y + 19), (float)(w - 12), 1.0f, 0.5f, 863891654);
        if (editing) {
            String zero = (V2.INSTANCE.invseeShowZero ? "\u2611 " : "\u2610 ") + Lang.t("auch 0 zeigen", "show 0 too");
            ctx.text(font, zero, x + 6, y + 23, -4666172, false);
            hits.add(new int[]{x + 4, y + 21, font.width(zero) + 4, 11, -4});
        }
        int drawn = 0;
        int cw = cols == 2 ? (w - 4) / 2 : w;
        for (int i = 0; i < ITEMS.length; ++i) {
            String n;
            if (!row[i]) continue;
            int col = drawn / perCol;
            int r = drawn % perCol;
            int cx = x + col * cw;
            int ry = y + head + 1 + r * 18;
            if (STACKS[i] == null) {
                InvseeCounts.STACKS[i] = new ItemStack((ItemLike)ITEMS[i]);
            }
            boolean off = editing && InvseeCounts.hidden(i);
            ctx.item(STACKS[i], cx + 6, ry);
            if (off) {
                ctx.fill(cx + 5, ry - 1, cx + cw - 4, ry + 17, -1341185762);
            }
            String string = editing ? (off ? Lang.t("aus", "off") : Lang.t("an", "on")) : (n = String.valueOf(counts[i]));
            int color = editing ? (off ? -9469831 : -8523578) : (counts[i] == 0 ? -1017230 : (i == 0 ? -673493 : -1379602));
            ctx.text(font, n, cx + cw - 8 - font.width(n), ry + 4, color, !editing);
            if (editing) {
                hits.add(new int[]{cx + 4, ry - 1, cw - 8, 18, i});
            }
            ++drawn;
        }
        ctx.pose().popMatrix();
    }

    static {
        pendingName = "";
        shownName = "";
        hits = new ArrayList<int[]>();
    }
}

