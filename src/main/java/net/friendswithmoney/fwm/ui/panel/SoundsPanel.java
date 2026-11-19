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
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.Identifier
 *  net.minecraft.sounds.SoundEvent
 */
package net.friendswithmoney.fwm.ui.panel;

import java.util.ArrayList;
import java.util.List;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.game.SoundMuffler;
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
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class SoundsPanel
extends Panel {
    private static final int ROW_H = 14;
    private static final int BTN_H = 13;
    private static final int MINT = -8523578;
    private final PanelHost host;
    private final List<Hit> hits = new ArrayList<Hit>();
    private final ConfirmBox confirm = new ConfirmBox();
    private EditBox search;
    private boolean onlyChanged;
    private int scroll;
    private String dragging;
    private int dragX;
    private int dragW;

    public SoundsPanel(PanelHost host) {
        this.host = host;
    }

    @Override
    public Component title() {
        return Lang.of("fwm.tab.sounds", new Object[0]);
    }

    @Override
    protected void onResize() {
        String before;
        Font font = FwmFont.ui();
        String string = before = this.search == null ? "" : this.search.getValue();
        if (this.search != null) {
            this.host.detach((AbstractWidget)this.search);
        }
        this.search = new EditBox(font, this.x + 4, this.y + 4, this.searchWidth(font), 10, (Component)Component.literal((String)""));
        this.search.setBordered(false);
        this.search.setMaxLength(40);
        this.search.setTextColor(FwmTheme.TEXT);
        this.search.setHint((Component)Component.literal((String)Lang.t("Suchen: Totem, Kristall, villager \u2026", "Search: totem, crystal, villager \u2026")));
        this.search.setValue(before);
        this.host.attach((AbstractWidget)this.search);
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY, float delta) {
        this.hits.clear();
        int mx = this.confirm.open() ? -1000 : mouseX;
        int my = this.confirm.open() ? -1000 : mouseY;
        int fw = this.searchWidth(font);
        FwmTheme.box(ctx, this.x, this.y, fw + 8, 15, FwmTheme.INSET, this.search.isFocused() ? FwmTheme.ACCENT : FwmTheme.EDGE);
        this.search.extractWidgetRenderState(ctx, mouseX, mouseY, delta);
        int bx = this.x + fw + 12;
        this.chip(ctx, font, bx, this.y + 1, Lang.t("Nur ge\u00e4nderte", "Only changed"), this.onlyChanged, mx, my, () -> {
            this.onlyChanged = !this.onlyChanged;
            this.scroll = 0;
        });
        this.chip(ctx, font, bx += font.width(Lang.t("Nur ge\u00e4nderte", "Only changed")) + 14, this.y + 1, Lang.t("Farm leiser", "Farm quieter"), false, mx, my, () -> SoundMuffler.preset("farm"));
        this.chip(ctx, font, bx += font.width(Lang.t("Farm leiser", "Farm quieter")) + 14, this.y + 1, "PvP", false, mx, my, () -> SoundMuffler.preset("pvp"));
        this.chip(ctx, font, bx += font.width("PvP") + 14, this.y + 1, Lang.t("Alle zur\u00fcck", "Reset all"), false, mx, my, () -> this.confirm.ask((Component)Component.literal((String)Lang.t("Alle Sounds auf 100 % zur\u00fccksetzen?", "Reset every sound to 100 %?")), "fwm.confirm.confirm", SoundMuffler::resetAll));
        List<String> all = SoundMuffler.all(this.search.getValue(), this.onlyChanged);
        int changed = V2.INSTANCE.soundVolumes.size();
        ctx.text(font, (Component)Component.literal((String)(changed + Lang.t(" von ", " of ") + BuiltInRegistries.SOUND_EVENT.keySet().size() + Lang.t(" Sounds ge\u00e4ndert", " sounds changed"))), this.x, this.y + 19, FwmTheme.TEXT_DIM, false);
        int top = this.y + 31;
        int visible = Math.max(1, (this.y + this.height - top) / 14);
        this.scroll = Math.max(0, Math.min(this.scroll, all.size() - visible));
        int sliderX = this.x + this.width - 150;
        int sliderW = 100;
        for (int i = 0; i < visible && this.scroll + i < all.size(); ++i) {
            Object label;
            boolean over;
            String id = all.get(this.scroll + i);
            int ry = top + i * 14;
            int vol = SoundMuffler.volume(id);
            boolean bl = over = mx >= this.x && mx < this.x + this.width && my >= ry && my < ry + 14;
            if (over) {
                FwmTheme.hover(ctx, this.x, ry, this.width, 14);
            }
            Object object = label = id.startsWith("minecraft:") ? id.substring(10) : id;
            if (SoundMuffler.shared(id)) {
                label = (String)label + Lang.t("  (Kristall, TNT, Creeper, Bett, Anker)", "  (crystal, TNT, creeper, bed, anchor)");
            }
            ctx.text(font, (Component)Component.literal((String)font.plainSubstrByWidth((String)label, sliderX - this.x - 30)), this.x + 4, ry + 3, vol < 100 ? -8523578 : FwmTheme.TEXT, false);
            int px = sliderX - 18;
            this.button(ctx, font, px, ry + 1, 14, "\u266a", mx, my, () -> SoundsPanel.listen(id));
            ctx.fill(sliderX, ry + 6, sliderX + sliderW, ry + 8, 0x44FFFFFF);
            ctx.fill(sliderX, ry + 6, sliderX + sliderW * vol / 100, ry + 8, -8523578);
            int knob = sliderX + sliderW * vol / 100;
            ctx.fill(knob - 1, ry + 3, knob + 2, ry + 11, -1);
            String v = vol + " %";
            ctx.text(font, (Component)Component.literal((String)v), sliderX + sliderW + 6, ry + 3, vol < 100 ? -8523578 : FwmTheme.TEXT_DIM, false);
            int sx = sliderX;
            this.hits.add(new Hit(sliderX - 2, ry, sliderW + 4, 14, () -> {
                this.dragging = id;
                this.dragX = sx;
                this.dragW = sliderW;
            }));
        }
        if (all.isEmpty()) {
            String none = Lang.t("Kein Sound passt.", "No sound fits.");
            ctx.text(font, (Component)Component.literal((String)none), this.x + (this.width - font.width(none)) / 2, top + 20, FwmTheme.TEXT_DIM, false);
        }
        this.confirm.render(ctx, font, this.x, this.y, this.width, this.height, mouseX, mouseY);
    }

    private static void listen(String id) {
        Identifier ident = Identifier.tryParse((String)id);
        if (ident == null) {
            return;
        }
        SoundEvent event = (SoundEvent)BuiltInRegistries.SOUND_EVENT.getValue(ident);
        if (event != null) {
            Minecraft.getInstance().getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)event, (float)1.0f));
        }
    }

    private int searchWidth(Font font) {
        int chips = font.width(Lang.t("Nur ge\u00e4nderte", "Only changed")) + font.width(Lang.t("Farm leiser", "Farm quieter")) + font.width("PvP") + font.width(Lang.t("Alle zur\u00fcck", "Reset all")) + 56;
        return Math.max(40, this.width - chips - 12);
    }

    private void chip(GuiGraphicsExtractor ctx, Font font, int cx, int cy, String label, boolean on, int mx, int my, Runnable action) {
        boolean over;
        int w = font.width(label) + 10;
        boolean bl = over = mx >= cx && mx < cx + w && my >= cy && my < cy + 13;
        LauncherGfx.round(ctx, cx, cy, w, 13, 4, on ? 1149104326 : (over ? 0x22FFFFFF : 0x11FFFFFF));
        ctx.text(font, (Component)Component.literal((String)label), cx + 5, cy + 3, on ? -8523578 : FwmTheme.TEXT, false);
        this.hits.add(new Hit(cx, cy, w, 13, action));
    }

    private void button(GuiGraphicsExtractor ctx, Font font, int bx, int by, int bw, String label, int mx, int my, Runnable action) {
        boolean over = mx >= bx && mx < bx + bw && my >= by && my < by + 12;
        ctx.fill(bx, by, bx + bw, by + 12, over ? 0x44FFFFFF : 0x22FFFFFF);
        ctx.text(font, (Component)Component.literal((String)label), bx + (bw - font.width(label)) / 2, by + 2, over ? -8523578 : FwmTheme.TEXT, false);
        this.hits.add(new Hit(bx, by, bw, 12, action));
    }

    private void drag(double mouseX) {
        if (this.dragging == null) {
            return;
        }
        int pct = (int)Math.round((mouseX - (double)this.dragX) * 100.0 / (double)this.dragW / 5.0) * 5;
        SoundMuffler.set(this.dragging, Math.max(0, Math.min(100, pct)));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.confirm.open()) {
            return this.confirm.mouseClicked(mouseX, mouseY);
        }
        if (!this.contains(mouseX, mouseY)) {
            return false;
        }
        boolean inField = mouseX >= (double)this.x && mouseX < (double)(this.x + Math.max(40, this.width - 260) + 8) && mouseY >= (double)this.y && mouseY < (double)(this.y + 15);
        this.search.setFocused(inField);
        if (inField || button != 0) {
            return true;
        }
        for (Hit h : this.hits) {
            if (!(mouseX >= (double)h.x()) || !(mouseX < (double)(h.x() + h.w())) || !(mouseY >= (double)h.y()) || !(mouseY < (double)(h.y() + h.h()))) continue;
            h.action().run();
            this.drag(mouseX);
            return true;
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        if (this.dragging == null) {
            return false;
        }
        this.drag(mouseX);
        return true;
    }

    @Override
    public void mouseReleased() {
        this.dragging = null;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double vertical) {
        this.scroll = Math.max(0, this.scroll - (int)Math.signum(vertical) * 3);
        return true;
    }

    @Override
    public boolean isCapturingKeys() {
        return this.confirm.open() || this.search != null && this.search.isFocused();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.confirm.open()) {
            return this.confirm.keyPressed(keyCode);
        }
        if (this.search == null || !this.search.isFocused()) {
            return false;
        }
        if (keyCode == 256) {
            this.search.setFocused(false);
            return true;
        }
        this.scroll = 0;
        return this.search.keyPressed(new KeyEvent(keyCode, scanCode, modifiers));
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        if (this.search == null || !this.search.isFocused()) {
            return false;
        }
        this.scroll = 0;
        return this.search.charTyped(input);
    }

    @Override
    public void onHidden() {
        if (this.search != null) {
            this.host.detach((AbstractWidget)this.search);
        }
    }

    private record Hit(int x, int y, int w, int h, Runnable action) {
    }
}

