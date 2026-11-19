/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 */
package net.friendswithmoney.fwm.ui.panel;

import java.util.ArrayList;
import java.util.List;
import net.friendswithmoney.fwm.config.ConfigManager;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.game.FightKits;
import net.friendswithmoney.fwm.ui.ConfirmBox;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.LauncherGfx;
import net.friendswithmoney.fwm.ui.gfx.FwmFont;
import net.friendswithmoney.fwm.ui.panel.ItemPanel;
import net.friendswithmoney.fwm.ui.panel.Panel;
import net.friendswithmoney.fwm.ui.panel.PanelHost;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class FightKitPanel
extends Panel {
    private static final int CELL = 22;
    private final PanelHost host;
    private EditBox nameField;
    private boolean confirmDelete;
    private final List<int[]> boxes = new ArrayList<int[]>();
    private final List<Runnable> actions = new ArrayList<Runnable>();
    private final List<String> labels = new ArrayList<String>();
    private static final int TEXT = -1379602;
    private static final int SUB = -7362144;
    private static final int MINT = -8523578;
    private static final int CARD = 0x26000000;
    private static final int CARD_RING = 0x14FFFFFF;
    private static final int SIDE_W = 150;
    private static final int KIT_STEP = 25;
    private int kitScroll;
    private final ConfirmBox confirm = new ConfirmBox();
    private static final int NAME_W = 150;

    public FightKitPanel(PanelHost host) {
        this.host = host;
    }

    @Override
    public Component title() {
        return Lang.of("fwm.tab.fightkit", new Object[0]);
    }

    private static List<FwmConfig.FightKit> kits() {
        if (FwmConfig.INSTANCE.fightKits == null) {
            FwmConfig.INSTANCE.fightKits = new ArrayList<FwmConfig.FightKit>();
        }
        return FwmConfig.INSTANCE.fightKits;
    }

    private static FwmConfig.FightKit current() {
        return FightKits.selected();
    }

    private int mainX() {
        return this.x + 150 + 10;
    }

    private int mainW() {
        return this.width - 150 - 10;
    }

    private int gridX() {
        return this.mainX() + (this.mainW() - 230) / 2 + 22 + 10;
    }

    private int gridY() {
        return this.y + 50;
    }

    private int hotbarY() {
        return this.gridY() + 66 + 16;
    }

    private int[] cell(int index) {
        if (index == 40) {
            return new int[]{this.gridX() - 22 - 10, this.hotbarY()};
        }
        if (index < 9) {
            return new int[]{this.gridX() + index * 22, this.hotbarY()};
        }
        int row = (index - 9) / 9;
        int col = (index - 9) % 9;
        return new int[]{this.gridX() + col * 22, this.gridY() + row * 22};
    }

    private int cellAt(double mx, double my) {
        for (int i = 0; i < 36; ++i) {
            if (!this.inCell(i, mx, my)) continue;
            return i;
        }
        if (this.inCell(40, mx, my)) {
            return 40;
        }
        return -1;
    }

    private boolean inCell(int index, double mx, double my) {
        int[] c = this.cell(index);
        return mx >= (double)c[0] && mx < (double)(c[0] + 22) && my >= (double)c[1] && my < (double)(c[1] + 22);
    }

    private boolean onName(double mx, double my) {
        return mx >= (double)(this.mainX() + 10) && mx < (double)(this.mainX() + 10 + 150) && my >= (double)(this.y + 10) && my < (double)(this.y + 28);
    }

    @Override
    protected void onResize() {
        if (this.nameField != null) {
            this.host.detach((AbstractWidget)this.nameField);
        }
        Font font = FwmFont.ui();
        this.nameField = new EditBox(font, this.mainX() + 16, this.y + 15, 138, 10, (Component)Lang.of("fwm.fightkit.name", new Object[0]));
        this.nameField.setBordered(false);
        this.nameField.setMaxLength(24);
        this.nameField.setTextColor(-1379602);
        FwmConfig.FightKit kit = FightKitPanel.current();
        this.nameField.setValue(kit == null || kit.name == null ? "" : kit.name);
        this.host.attach((AbstractWidget)this.nameField);
    }

    private void storeName() {
        FwmConfig.FightKit kit = FightKitPanel.current();
        if (kit == null || this.nameField == null) {
            return;
        }
        String name = this.nameField.getValue().trim();
        kit.name = name.isEmpty() ? "Kit" : name;
    }

    private void select(int index) {
        this.storeName();
        FwmConfig.INSTANCE.fightKitSelected = index;
        this.confirmDelete = false;
        ConfigManager.save();
        this.onResize();
    }

    private void hit(int bx, int by, int w, int h, String key, Runnable action) {
        this.boxes.add(new int[]{bx, by, w, h});
        this.actions.add(action);
        this.labels.add(key);
    }

    private static boolean over(int mouseX, int mouseY, int bx, int by, int w, int h) {
        return mouseX >= bx && mouseX < bx + w && mouseY >= by && mouseY < by + h;
    }

    private int button(GuiGraphicsExtractor ctx, Font font, int bx, int by, String key, String label, boolean strong, boolean danger, int mouseX, int mouseY, Runnable action) {
        int w = font.width(label) + 16;
        int h = 16;
        boolean hover = FightKitPanel.over(mouseX, mouseY, bx, by, w, h);
        int fill = strong ? (hover ? -6555690 : -8523578) : (danger ? (hover ? 1727036018 : 871398002) : (hover ? 0x33FFFFFF : 0x1AFFFFFF));
        LauncherGfx.round(ctx, bx, by, w, h, 5, fill);
        if (!strong) {
            LauncherGfx.ring(ctx, bx, by, w, h, 5, danger ? -1997505934 : (hover ? -2005012282 : 0x22FFFFFF));
        }
        int color = strong ? -16045536 : (danger ? -15170 : (hover ? -1379602 : -4666172));
        ctx.text(font, label, bx + 8, by + 4, color, false);
        this.hit(bx, by, w, h, key, action);
        return w;
    }

    private void invseeSwitch(GuiGraphicsExtractor ctx, Font font, int mx0, int mw, int top) {
        boolean on = FwmConfig.INSTANCE.invseeCounts;
        String label = Lang.t("Items bei /invsee z\u00e4hlen", "Count items on /invsee");
        int sw = 22;
        int sx = mx0 + mw - 10 - sw;
        int lx = sx - 6 - font.width(label);
        ctx.text(font, label, lx, top + 2, on ? -1379602 : -7362144, false);
        LauncherGfx.toggle(ctx, sx, top, on, -11552618, -12957119);
        this.hit(lx - 4, top - 3, sx + sw - lx + 4, 16, "fwm.fightkit.invsee", () -> {
            FwmConfig.INSTANCE.invseeCounts = !FwmConfig.INSTANCE.invseeCounts;
            ConfigManager.save();
        });
    }

    private static int itemCount(FwmConfig.FightKit kit) {
        int n = 0;
        for (FwmConfig.KitSlot slot : kit.slots.values()) {
            if (slot == null || slot.item == null) continue;
            ++n;
        }
        return n;
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY, float delta) {
        this.boxes.clear();
        this.actions.clear();
        this.labels.clear();
        boolean asking = this.confirm.open();
        if (asking) {
            mouseX = -1000;
            mouseY = -1000;
        }
        this.drawPage(ctx, font, mouseX, mouseY, delta);
        this.confirm.render(ctx, font, this.x, this.y, this.width, this.height, mouseX, mouseY);
    }

    private void drawPage(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY, float delta) {
        this.sidebar(ctx, font, mouseX, mouseY);
        int mx0 = this.mainX();
        int mw = this.mainW();
        LauncherGfx.round(ctx, mx0, this.y, mw, this.height, 8, 0x26000000);
        LauncherGfx.ring(ctx, mx0, this.y, mw, this.height, 8, 0x14FFFFFF);
        FwmConfig.FightKit kit = FightKitPanel.current();
        this.invseeSwitch(ctx, font, mx0, mw, kit == null ? this.y + 13 : this.y + 31);
        if (kit == null) {
            this.nameField.visible = false;
            String a = Lang.t("Noch kein Kit", "No kit yet");
            String b = Lang.t("Leg links eins an oder \u00fcbernimm dein Inventar.", "Create one on the left or take over your inventory.");
            LauncherGfx.bold(ctx, font, a, mx0 + (mw - font.width(a)) / 2, this.y + this.height / 2 - 30, -1379602);
            ctx.text(font, b, mx0 + (mw - font.width(b)) / 2, this.y + this.height / 2 - 18, -7362144, false);
            String label = Lang.t("Inventar als Kit speichern", "Save inventory as a kit");
            int bw = font.width(label) + 16;
            this.button(ctx, font, mx0 + (mw - bw) / 2, this.y + this.height / 2, "fwm.fightkit.capture", label, true, false, mouseX, mouseY, () -> {
                Minecraft client = Minecraft.getInstance();
                if (client.player == null) {
                    return;
                }
                FwmConfig.FightKit made = this.newKit();
                FightKits.capture(made, client.player.getInventory());
                ConfigManager.save();
            });
            return;
        }
        this.nameField.visible = true;
        boolean focused = this.nameField.isFocused();
        LauncherGfx.round(ctx, mx0 + 10, this.y + 10, 150, 18, 5, 0x33000000);
        LauncherGfx.ring(ctx, mx0 + 10, this.y + 10, 150, 18, 5, focused ? -1434586938 : 0x22FFFFFF);
        this.nameField.extractWidgetRenderState(ctx, mouseX, mouseY, delta);
        boolean inInventory = FwmConfig.INSTANCE.fightKitShowInInventory;
        String showLabel = Lang.t("Im Inventar zeigen, was fehlt", "Show what is missing in the inventory");
        int sw = 22;
        int sx = mx0 + mw - 10 - sw;
        int lx = sx - 6 - font.width(showLabel);
        ctx.text(font, showLabel, lx, this.y + 15, inInventory ? -1379602 : -7362144, false);
        LauncherGfx.toggle(ctx, sx, this.y + 13, inInventory, -11552618, -12957119);
        this.hit(lx - 4, this.y + 10, sx + sw - lx + 4, 18, "fwm.fightkit.check", () -> {
            FwmConfig.INSTANCE.fightKitShowInInventory = !FwmConfig.INSTANCE.fightKitShowInInventory;
            ConfigManager.save();
        });
        ctx.text(font, Lang.t("Inventar", "Inventory"), this.gridX(), this.gridY() - 11, -7362144, false);
        ctx.text(font, Lang.t("Hotbar", "Hotbar"), this.gridX(), this.hotbarY() - 11, -7362144, false);
        int[] off = this.cell(40);
        String offLabel = Lang.t("Hand 2", "Offhand");
        ctx.text(font, offLabel, off[0] + (22 - font.width(offLabel)) / 2, off[1] + 22 + 2, -7362144, false);
        for (int i = 0; i < 36; ++i) {
            this.drawCell(ctx, font, kit, i, mouseX, mouseY);
        }
        this.drawCell(ctx, font, kit, 40, mouseX, mouseY);
        int by = this.hotbarY() + 22 + 12;
        String capture = Lang.t("Aus Inventar \u00fcbernehmen", "Take over inventory");
        String clear = Lang.t("Leeren", "Clear");
        String delete = this.confirmDelete ? Lang.t("Wirklich l\u00f6schen?", "Really delete?") : Lang.t("L\u00f6schen", "Delete");
        int total = font.width(capture) + font.width(clear) + font.width(delete) + 48 + 12;
        int bx = mx0 + (mw - total) / 2;
        bx += this.button(ctx, font, bx, by, "fwm.fightkit.capture", capture, true, false, mouseX, mouseY, () -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) {
                return;
            }
            FightKits.capture(kit, client.player.getInventory());
            ConfigManager.save();
        }) + 6;
        bx += this.button(ctx, font, bx, by, "fwm.fightkit.clear", clear, false, false, mouseX, mouseY, () -> {
            kit.slots.clear();
            ConfigManager.save();
        }) + 6;
        this.button(ctx, font, bx, by, "fwm.fightkit.delete", delete, false, true, mouseX, mouseY, () -> {
            if (!this.confirmDelete) {
                this.confirmDelete = true;
                return;
            }
            FightKitPanel.kits().remove(kit);
            FwmConfig.INSTANCE.fightKitSelected = Math.max(0, FightKitPanel.kits().size() - 1);
            this.confirmDelete = false;
            ConfigManager.save();
            this.onResize();
        });
        int hy = by + 28;
        if (hy + 44 > this.y + this.height) {
            return;
        }
        int hw = Math.min(mw - 20, 420);
        int hx = mx0 + (mw - hw) / 2;
        LauncherGfx.round(ctx, hx, hy, hw, 44, 6, 444461254);
        LauncherGfx.ring(ctx, hx, hy, hw, 44, 6, 578678982);
        FightKitPanel.step(ctx, font, hx + 8, hy + 5, "1", Lang.t("Slot anklicken und Item w\u00e4hlen \u00b7 Rechtsklick leert", "Click a slot, pick the item \u00b7 right click empties it"), hw - 16);
        FightKitPanel.step(ctx, font, hx + 8, hy + 18, "2", Lang.t("Mausrad \u00fcber einem Slot \u00e4ndert die Anzahl", "The wheel over a slot sets how many"), hw - 16);
        FightKitPanel.step(ctx, font, hx + 8, hy + 31, "3", Lang.t("Im Inventar steht rechts, was dir noch fehlt", "Your inventory shows on the right what is missing"), hw - 16);
        String note = Lang.t("Ohne R\u00fcstung \u00b7 Material egal: Netherite z\u00e4hlt wie Dia", "No armour \u00b7 material does not matter: netherite counts as diamond");
        if (hy + 56 <= this.y + this.height) {
            ctx.text(font, note, mx0 + (mw - font.width(note)) / 2, hy + 49, -7362144, false);
        }
    }

    private static void step(GuiGraphicsExtractor ctx, Font font, int sx, int sy, String n, String text, int w) {
        LauncherGfx.round(ctx, sx, sy - 1, 10, 10, 5, -8523578);
        ctx.text(font, n, sx + 5 - font.width(n) / 2 + 1, sy, -16045536, false);
        ctx.text(font, font.plainSubstrByWidth(text, w - 16), sx + 15, sy, -1379602, false);
    }

    private void sidebar(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY) {
        LauncherGfx.round(ctx, this.x, this.y, 150, this.height, 8, 0x26000000);
        LauncherGfx.ring(ctx, this.x, this.y, 150, this.height, 8, 0x14FFFFFF);
        LauncherGfx.bold(ctx, font, Lang.t("Deine Kits", "Your kits"), this.x + 10, this.y + 10, -1379602);
        List<FwmConfig.FightKit> kits = FightKitPanel.kits();
        int selected = Math.max(0, Math.min(kits.size() - 1, FwmConfig.INSTANCE.fightKitSelected));
        int newY = this.y + this.height - 26;
        int shown = Math.max(1, (newY - 4 - (this.y + 26)) / 25);
        this.kitScroll = Math.max(0, Math.min(this.kitScroll, Math.max(0, kits.size() - shown)));
        int ry = this.y + 26;
        for (int i = this.kitScroll; i < kits.size() && i < this.kitScroll + shown; ++i) {
            ItemStack icon;
            int index = i;
            FwmConfig.FightKit k = kits.get(i);
            boolean active = i == selected;
            boolean hover = FightKitPanel.over(mouseX, mouseY, this.x + 6, ry, 138, 22);
            LauncherGfx.round(ctx, this.x + 6, ry, 138, 22, 6, active ? 863891654 : (hover ? 0x22FFFFFF : 0x10FFFFFF));
            if (active || hover) {
                LauncherGfx.ring(ctx, this.x + 6, ry, 138, 22, 6, active ? -1434586938 : 0x33FFFFFF);
            }
            if (!(icon = FightKitPanel.firstItem(k)).isEmpty()) {
                LauncherGfx.item(ctx, icon, this.x + 10, ry + 3, 1.0f);
            }
            String name = k.name == null ? "Kit" : k.name;
            ctx.text(font, font.plainSubstrByWidth(name, 88), this.x + 30, ry + 3, active ? -1379602 : -4666172, false);
            int n = FightKitPanel.itemCount(k);
            ctx.text(font, n + Lang.t(n == 1 ? " Item" : " Items", n == 1 ? " item" : " items"), this.x + 30, ry + 12, -7362144, false);
            int bx = this.x + 150 - 22;
            int by = ry + 5;
            boolean overBin = FightKitPanel.over(mouseX, mouseY, bx - 2, by - 2, 14, 16);
            FightKitPanel.bin(ctx, bx, by, overBin ? -1017230 : -7362144);
            this.hit(this.x + 6, ry, 138, 22, "kit:" + name, () -> this.select(index));
            this.hit(bx - 2, by - 2, 14, 16, "kit:delete", () -> this.askDelete(index));
            ry += 25;
        }
        if (kits.size() > shown) {
            int trackY = this.y + 26;
            int trackH = shown * 25 - 3;
            int thumbH = Math.max(10, trackH * shown / kits.size());
            int thumbY = trackY + (trackH - thumbH) * this.kitScroll / Math.max(1, kits.size() - shown);
            ctx.fill(this.x + 150 - 4, trackY, this.x + 150 - 2, trackY + trackH, 0x22FFFFFF);
            ctx.fill(this.x + 150 - 4, thumbY, this.x + 150 - 2, thumbY + thumbH, -8523578);
        }
        String label = Lang.t("+ Neues Kit", "+ New kit");
        boolean hover = FightKitPanel.over(mouseX, mouseY, this.x + 6, newY, 138, 18);
        LauncherGfx.round(ctx, this.x + 6, newY, 138, 18, 5, hover ? 0x33FFFFFF : 0x1AFFFFFF);
        LauncherGfx.ring(ctx, this.x + 6, newY, 138, 18, 5, hover ? -2005012282 : 0x22FFFFFF);
        ctx.text(font, label, this.x + (150 - font.width(label)) / 2, newY + 5, hover ? -8523578 : -1379602, false);
        this.hit(this.x + 6, newY, 138, 18, "fwm.fightkit.new", () -> {
            this.newKit();
            ConfigManager.save();
        });
    }

    private static void bin(GuiGraphicsExtractor ctx, int bx, int by, int c) {
        ctx.fill(bx + 3, by, bx + 7, by + 1, c);
        ctx.fill(bx, by + 1, bx + 10, by + 2, c);
        ctx.fill(bx + 1, by + 3, bx + 9, by + 4, c);
        ctx.fill(bx + 1, by + 3, bx + 2, by + 12, c);
        ctx.fill(bx + 8, by + 3, bx + 9, by + 12, c);
        ctx.fill(bx + 1, by + 11, bx + 9, by + 12, c);
        ctx.fill(bx + 4, by + 5, bx + 5, by + 10, c);
        ctx.fill(bx + 6, by + 5, bx + 7, by + 10, c);
    }

    private void askDelete(int index) {
        List<FwmConfig.FightKit> kits = FightKitPanel.kits();
        if (index < 0 || index >= kits.size()) {
            return;
        }
        String name = kits.get((int)index).name == null ? "Kit" : kits.get((int)index).name;
        this.confirm.ask((Component)Component.literal((String)Lang.t("Kit \u201e" + name + "\u201c wirklich l\u00f6schen?", "Really delete kit \"" + name + "\"?")), () -> {
            this.storeName();
            kits.remove(index);
            FwmConfig.INSTANCE.fightKitSelected = Math.max(0, Math.min(kits.size() - 1, FwmConfig.INSTANCE.fightKitSelected));
            this.confirmDelete = false;
            ConfigManager.save();
            this.onResize();
        });
    }

    private FwmConfig.FightKit newKit() {
        this.storeName();
        List<FwmConfig.FightKit> kits = FightKitPanel.kits();
        FwmConfig.FightKit kit = new FwmConfig.FightKit();
        kit.name = "Kit " + (kits.size() + 1);
        kits.add(kit);
        this.select(kits.size() - 1);
        return kit;
    }

    private static ItemStack firstItem(FwmConfig.FightKit kit) {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack;
            FwmConfig.KitSlot slot = kit.slots.get(i);
            if (slot == null || slot.item == null || (stack = FightKits.stack(slot.item, 1)).isEmpty()) continue;
            return stack;
        }
        return ItemStack.EMPTY;
    }

    private void drawCell(GuiGraphicsExtractor ctx, Font font, FwmConfig.FightKit kit, int index, int mouseX, int mouseY) {
        int[] c = this.cell(index);
        boolean hover = FightKitPanel.over(mouseX, mouseY, c[0], c[1], 22, 22);
        LauncherGfx.round(ctx, c[0] + 1, c[1] + 1, 20, 20, 3, hover ? 0x40FFFFFF : 0x40000000);
        LauncherGfx.ring(ctx, c[0] + 1, c[1] + 1, 20, 20, 3, hover ? -864161594 : 0x1FFFFFFF);
        FwmConfig.KitSlot slot = kit.slots.get(index);
        if (slot == null || slot.item == null) {
            return;
        }
        ItemStack stack = FightKits.stack(slot.item, slot.count);
        if (stack.isEmpty()) {
            return;
        }
        int ix = c[0] + 3;
        int iy = c[1] + 3;
        ctx.item(stack, ix, iy);
        ctx.itemDecorations(font, stack, ix, iy);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.confirm.open()) {
            return this.confirm.mouseClicked(mouseX, mouseY);
        }
        if (!this.contains(mouseX, mouseY)) {
            return false;
        }
        if (this.nameField != null) {
            boolean onName = this.onName(mouseX, mouseY);
            if (!onName && this.nameField.isFocused()) {
                this.storeName();
                ConfigManager.save();
            }
            this.nameField.setFocused(onName && this.nameField.visible);
            if (onName) {
                return true;
            }
        }
        if (button == 0) {
            for (int i = this.boxes.size() - 1; i >= 0; --i) {
                int[] b = this.boxes.get(i);
                if (!(mouseX >= (double)b[0]) || !(mouseX < (double)(b[0] + b[2])) || !(mouseY >= (double)b[1]) || !(mouseY < (double)(b[1] + b[3]))) continue;
                this.actions.get(i).run();
                return true;
            }
        }
        FwmConfig.FightKit kit = FightKitPanel.current();
        int index = this.cellAt(mouseX, mouseY);
        if (kit == null || index < 0) {
            return true;
        }
        this.confirmDelete = false;
        if (button == 1) {
            kit.slots.remove(index);
            ConfigManager.save();
            return true;
        }
        if (button == 0) {
            this.host.openSub(new ItemPanel(this.host, id -> {
                FwmConfig.KitSlot slot = new FwmConfig.KitSlot();
                slot.item = id;
                FwmConfig.KitSlot old = kit.slots.get(index);
                slot.count = old != null && id.equals(old.item) ? old.count : 1;
                kit.slots.put(index, slot);
                ConfigManager.save();
            }));
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double vertical) {
        if (mouseX >= (double)this.x && mouseX < (double)(this.x + 150) && mouseY >= (double)this.y && mouseY < (double)(this.y + this.height)) {
            this.kitScroll = Math.max(0, this.kitScroll - (int)Math.signum(vertical));
            return true;
        }
        FwmConfig.FightKit kit = FightKitPanel.current();
        int index = this.cellAt(mouseX, mouseY);
        if (kit == null || index < 0) {
            return false;
        }
        FwmConfig.KitSlot slot = kit.slots.get(index);
        if (slot == null) {
            return true;
        }
        slot.count = Math.max(1, Math.min(FightKits.maxCount(slot.item), FightKits.count(slot) + (vertical > 0.0 ? 1 : -1)));
        ConfigManager.save();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.confirm.open()) {
            return this.confirm.keyPressed(keyCode);
        }
        if (this.nameField == null || !this.nameField.isFocused()) {
            return false;
        }
        if (keyCode == 257 || keyCode == 335) {
            this.nameField.setFocused(false);
            this.storeName();
            ConfigManager.save();
            return true;
        }
        return this.nameField.keyPressed(new KeyEvent(keyCode, scanCode, modifiers));
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        return this.nameField != null && this.nameField.isFocused() && this.nameField.charTyped(input);
    }

    @Override
    public boolean isCapturingKeys() {
        return this.nameField != null && this.nameField.isFocused();
    }

    @Override
    public void onHidden() {
        this.storeName();
        ConfigManager.save();
        if (this.nameField != null) {
            this.host.detach((AbstractWidget)this.nameField);
        }
    }
}

