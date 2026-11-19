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
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 */
package net.friendswithmoney.fwm.ui.panel;

import java.util.ArrayList;
import java.util.List;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.controller.SellPresets;
import net.friendswithmoney.fwm.ui.ConfirmBox;
import net.friendswithmoney.fwm.ui.FwmTheme;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.LauncherGfx;
import net.friendswithmoney.fwm.ui.gfx.FwmFont;
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
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;

public class SellPresetsPanel
extends Panel {
    private static final int BTN_H = 13;
    private static final int ROW_H = 16;
    private static final int MINT = -8523578;
    private static final int GOLD = -673493;
    private final PanelHost host;
    private final List<Hit> hits = new ArrayList<Hit>();
    private final ConfirmBox confirm = new ConfirmBox();
    private EditBox name;
    private boolean withTimings;
    private int scroll;
    private String note = "";
    private int noteColor = -8523578;

    public SellPresetsPanel(PanelHost host) {
        this.host = host;
    }

    @Override
    public Component title() {
        return Component.literal((String)Lang.t("Sell-Presets", "Sell presets"));
    }

    @Override
    protected void onResize() {
        String before;
        Font font = FwmFont.ui();
        String string = before = this.name == null ? "" : this.name.getValue();
        if (this.name != null) {
            this.host.detach((AbstractWidget)this.name);
        }
        this.name = new EditBox(font, this.x + 4, this.y + 4, Math.max(40, this.width - 250), 10, (Component)Component.literal((String)""));
        this.name.setBordered(false);
        this.name.setMaxLength(32);
        this.name.setTextColor(FwmTheme.TEXT);
        this.name.setHint((Component)Component.literal((String)Lang.t("Name, z. B. Skelett-Farm", "Name, e.g. skeleton farm")));
        this.name.setValue(before);
        this.host.attach((AbstractWidget)this.name);
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY, float delta) {
        this.hits.clear();
        int mx = this.confirm.open() ? -1000 : mouseX;
        int my = this.confirm.open() ? -1000 : mouseY;
        int fw = Math.max(40, this.width - 250);
        FwmTheme.box(ctx, this.x, this.y, fw + 8, 15, FwmTheme.INSET, this.name.isFocused() ? FwmTheme.ACCENT : FwmTheme.EDGE);
        this.name.extractWidgetRenderState(ctx, mouseX, mouseY, delta);
        int bx = this.x + fw + 12;
        this.toggleChip(ctx, font, bx, this.y + 1, Lang.t("Timings mit", "With timings"), this.withTimings, mx, my, () -> {
            this.withTimings = !this.withTimings;
        });
        this.button(ctx, font, bx += font.width(Lang.t("Timings mit", "With timings")) + 18, this.y + 1, 92, Lang.t("Auswahl speichern", "Save selection"), mx, my, this::saveCurrent);
        this.button(ctx, font, bx += 96, this.y + 1, this.x + this.width - bx, Lang.t("Code einf\u00fcgen", "Paste code"), mx, my, this::importCode);
        String active = V2.INSTANCE.sellPresetActive;
        ctx.text(font, (Component)Component.literal((String)(Lang.t("Aktiv: ", "Active: ") + (active.isEmpty() ? "\u2013" : active))), this.x, this.y + 20, FwmTheme.TEXT_DIM, false);
        if (!this.note.isEmpty()) {
            ctx.text(font, (Component)Component.literal((String)font.plainSubstrByWidth(this.note, this.width - 120)), this.x + 110, this.y + 20, this.noteColor, false);
        }
        ArrayList<Object> rows = new ArrayList<Object>();
        rows.add(Lang.t("FWM-PRESETS", "FWM PRESETS"));
        rows.addAll(SellPresets.fwm());
        rows.add(Lang.t("EIGENE", "OWN"));
        rows.addAll(V2.INSTANCE.sellPresets);
        int top = this.y + 32;
        int visible = Math.max(1, (this.y + this.height - top) / 16);
        this.scroll = Math.max(0, Math.min(this.scroll, rows.size() - visible));
        int ry = top;
        int i = this.scroll;
        while (i < rows.size() && ry + 16 <= this.y + this.height) {
            Object row = rows.get(i);
            if (row instanceof String) {
                String heading = (String)row;
                ctx.text(font, (Component)Component.literal((String)heading), this.x + 2, ry + 5, FwmTheme.TEXT_DIM, false);
            } else {
                boolean over;
                boolean bl = over = mx >= this.x && mx < this.x + this.width && my >= ry && my < ry + 16;
                if (over) {
                    FwmTheme.hover(ctx, this.x, ry, this.width, 16);
                }
                if (row instanceof SellPresets.Remote) {
                    SellPresets.Remote r = (SellPresets.Remote)row;
                    ctx.text(font, (Component)Component.literal((String)r.name), this.x + 4, ry + 4, r.name.equals(active) ? -8523578 : -673493, false);
                    info = r.items.size() + " Items" + (String)(r.beschreibung == null || r.beschreibung.isBlank() ? "" : " \u00b7 " + r.beschreibung);
                    ctx.text(font, (Component)Component.literal((String)font.plainSubstrByWidth(info, this.width - 260)), this.x + 110, ry + 4, FwmTheme.TEXT_DIM, false);
                    rx = this.x + this.width - 150;
                    this.button(ctx, font, rx, ry + 1, 60, Lang.t("Laden", "Load"), mx, my, () -> {
                        SellPresets.load(r);
                        this.say(Lang.t("Geladen: ", "Loaded: ") + r.name, -8523578);
                    });
                    this.button(ctx, font, rx + 64, ry + 1, 86, Lang.t("Als eigenes", "Copy as own"), mx, my, () -> {
                        V2.SellPreset copy = SellPresets.copy(r);
                        this.say(Lang.t("Kopiert als ", "Copied as ") + copy.name, -8523578);
                    });
                } else if (row instanceof V2.SellPreset) {
                    V2.SellPreset p = (V2.SellPreset)row;
                    ctx.text(font, (Component)Component.literal((String)p.name), this.x + 4, ry + 4, p.name.equals(active) ? -8523578 : FwmTheme.TEXT, false);
                    info = p.items.size() + " Items" + (p.timings != null ? Lang.t(" \u00b7 mit Timings", " \u00b7 with timings") : "");
                    ctx.text(font, (Component)Component.literal((String)info), this.x + 110, ry + 4, FwmTheme.TEXT_DIM, false);
                    rx = this.x + this.width - 200;
                    this.button(ctx, font, rx, ry + 1, 44, Lang.t("Laden", "Load"), mx, my, () -> {
                        SellPresets.load(p);
                        this.say(Lang.t("Geladen: ", "Loaded: ") + p.name, -8523578);
                    });
                    this.button(ctx, font, rx + 48, ry + 1, 70, Lang.t("\u00dcbernehmen", "Take over"), mx, my, () -> this.confirm.ask((Component)Component.literal((String)Lang.t("\u201e" + p.name + "\u201c mit der aktuellen Auswahl \u00fcberschreiben?", "Overwrite \"" + p.name + "\" with the current selection?")), "fwm.confirm.confirm", () -> SellPresets.overwrite(p)));
                    this.button(ctx, font, rx + 122, ry + 1, 56, Lang.t("Teilen", "Share"), mx, my, () -> {
                        Minecraft.getInstance().keyboardHandler.setClipboard(SellPresets.share(p));
                        this.say(Lang.t("Code kopiert \u2013 in Discord einf\u00fcgen.", "Code copied \u2013 paste it into Discord."), -8523578);
                    });
                    this.button(ctx, font, rx + 182, ry + 1, 18, "\u00d7", mx, my, () -> this.confirm.ask((Component)Component.literal((String)Lang.t("Preset \u201e" + p.name + "\u201c l\u00f6schen?", "Delete preset \"" + p.name + "\"?")), () -> SellPresets.delete(p)));
                }
            }
            ++i;
            ry += 16;
        }
        if (V2.INSTANCE.sellPresets.isEmpty()) {
            String hint = Lang.t("Noch keine eigenen \u2013 Items w\u00e4hlen, Namen eingeben, speichern.", "None of your own yet \u2013 pick items, enter a name, save.");
            ctx.text(font, (Component)Component.literal((String)font.plainSubstrByWidth(hint, this.width - 8)), this.x + 4, Math.min(ry + 4, this.y + this.height - 10), FwmTheme.TEXT_DIM, false);
        }
        this.confirm.render(ctx, font, this.x, this.y, this.width, this.height, mouseX, mouseY);
    }

    private void saveCurrent() {
        String n = this.name.getValue().trim();
        if (n.isEmpty()) {
            this.say(Lang.t("Erst einen Namen eingeben.", "Enter a name first."), -43691);
            return;
        }
        if (!SellPresets.save(n, this.withTimings)) {
            this.say(Lang.t("Den Namen gibt es schon.", "That name is taken."), -43691);
            return;
        }
        this.name.setValue("");
        this.say(Lang.t("Gespeichert: ", "Saved: ") + n, -8523578);
    }

    private void importCode() {
        V2.SellPreset p = SellPresets.importCode(Minecraft.getInstance().keyboardHandler.getClipboard());
        this.say((String)(p == null ? Lang.t("In der Zwischenablage ist kein Preset-Code.", "No preset code on the clipboard.") : Lang.t("Importiert: ", "Imported: ") + p.name), p == null ? -43691 : -8523578);
    }

    private void say(String text, int color) {
        this.note = text;
        this.noteColor = color;
    }

    private void toggleChip(GuiGraphicsExtractor ctx, Font font, int cx, int cy, String label, boolean on, int mx, int my, Runnable action) {
        boolean over;
        int w = font.width(label) + 14;
        boolean bl = over = mx >= cx && mx < cx + w && my >= cy && my < cy + 13;
        LauncherGfx.round(ctx, cx, cy, w, 13, 4, on ? 1149104326 : (over ? 0x22FFFFFF : 0x11FFFFFF));
        ctx.fill(cx + 3, cy + 4, cx + 8, cy + 9, on ? -8523578 : 0x55FFFFFF);
        ctx.text(font, (Component)Component.literal((String)label), cx + 11, cy + 3, on ? -8523578 : FwmTheme.TEXT, false);
        this.hits.add(new Hit(cx, cy, w, 13, action));
    }

    private void button(GuiGraphicsExtractor ctx, Font font, int bx, int by, int bw, String label, int mx, int my, Runnable action) {
        boolean over = mx >= bx && mx < bx + bw && my >= by && my < by + 13;
        FwmTheme.box(ctx, bx, by, bw, 13, FwmTheme.RAISED, over ? FwmTheme.ACCENT : FwmTheme.OFF);
        MutableComponent t = Component.literal((String)font.plainSubstrByWidth(label, bw - 4));
        ctx.text(font, (Component)t, bx + (bw - font.width((FormattedText)t)) / 2, by + 3, over ? FwmTheme.ACCENT : FwmTheme.TEXT, false);
        this.hits.add(new Hit(bx, by, bw, 13, action));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.confirm.open()) {
            return this.confirm.mouseClicked(mouseX, mouseY);
        }
        if (!this.contains(mouseX, mouseY)) {
            return false;
        }
        boolean inField = mouseX >= (double)this.x && mouseX < (double)(this.x + Math.max(40, this.width - 250) + 8) && mouseY >= (double)this.y && mouseY < (double)(this.y + 15);
        this.name.setFocused(inField);
        if (inField || button != 0) {
            return true;
        }
        for (Hit h : this.hits) {
            if (!(mouseX >= (double)h.x()) || !(mouseX < (double)(h.x() + h.w())) || !(mouseY >= (double)h.y()) || !(mouseY < (double)(h.y() + h.h()))) continue;
            h.action().run();
            return true;
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double vertical) {
        this.scroll = Math.max(0, this.scroll - (int)Math.signum(vertical));
        return true;
    }

    @Override
    public boolean isCapturingKeys() {
        return this.confirm.open() || this.name != null && this.name.isFocused();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.confirm.open()) {
            return this.confirm.keyPressed(keyCode);
        }
        if (this.name == null || !this.name.isFocused()) {
            return false;
        }
        if (keyCode == 257 || keyCode == 335) {
            this.saveCurrent();
            return true;
        }
        if (keyCode == 256) {
            this.name.setFocused(false);
            return true;
        }
        return this.name.keyPressed(new KeyEvent(keyCode, scanCode, modifiers));
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        return this.name != null && this.name.isFocused() && this.name.charTyped(input);
    }

    @Override
    public void onHidden() {
        if (this.name != null) {
            this.host.detach((AbstractWidget)this.name);
        }
    }

    private record Hit(int x, int y, int w, int h, Runnable action) {
    }
}

