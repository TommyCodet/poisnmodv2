/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.network.chat.Component
 */
package net.friendswithmoney.fwm.ui.panel;

import java.util.List;
import net.friendswithmoney.fwm.game.Todos;
import net.friendswithmoney.fwm.ui.FwmTheme;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.LauncherGfx;
import net.friendswithmoney.fwm.ui.panel.PanelHost;
import net.friendswithmoney.fwm.ui.panel.RowsPanel;
import net.friendswithmoney.fwm.ui.panel.TodoEditPanel;
import net.friendswithmoney.fwm.ui.panel.TodoSettingsPanel;
import net.friendswithmoney.fwm.ui.widget.ButtonRow;
import net.friendswithmoney.fwm.ui.widget.ChipsRow;
import net.friendswithmoney.fwm.ui.widget.Row;
import net.friendswithmoney.fwm.ui.widget.TextRow;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class TodoPanel
extends RowsPanel {
    private final PanelHost host;
    private long shownSig = Long.MIN_VALUE;
    private long sigCheckedAt;
    private long rebuildAt;
    private TodoRow pressed;
    private double pressY;
    private double dragY;
    private boolean dragging;

    public TodoPanel(PanelHost host) {
        this.host = host;
    }

    @Override
    public Component title() {
        return Lang.of("fwm.tab.todos", new Object[0]);
    }

    @Override
    protected void buildRows() {
        long now = Todos.now();
        List<Todos.Todo> all = Todos.all();
        int open = 0;
        for (Todos.Todo t : all) {
            if (!Todos.isOpen(t, now)) continue;
            ++open;
        }
        int done = all.size() - open;
        if (all.isEmpty()) {
            this.rows.add(new TextRow((Component)Component.literal((String)Lang.t("Noch keine Aufgaben \u2013 leg unten eine an oder nimm eine Vorlage.", "No tasks yet \u2013 create one below or pick a template.")), null));
        } else {
            this.rows.add(new TextRow((Component)Component.literal((String)(Lang.t("Offen", "Open") + " (" + open + ")"))));
            for (Todos.Todo t : all) {
                if (!Todos.isOpen(t, now)) continue;
                this.rows.add(new TodoRow(t));
            }
            if (open == 0) {
                this.rows.add(new TextRow((Component)Component.literal((String)Lang.t("Alles erledigt!", "All done!")), null));
            }
            if (done > 0) {
                this.rows.add(new TextRow((Component)Component.literal((String)(Lang.t("Erledigt", "Done") + " (" + done + ")"))));
                for (Todos.Todo t : all) {
                    if (Todos.isOpen(t, now)) continue;
                    this.rows.add(new TodoRow(t));
                }
            }
        }
        this.rows.add(new TextRow((Component)Component.literal((String)Lang.t("Neu", "New"))));
        this.rows.add(new ButtonRow((Component)Component.literal((String)Lang.t("Eigene Aufgabe anlegen", "Create your own task")), () -> Component.literal((String)("+ " + Lang.t("Neu", "New"))), () -> {
            Todos.Todo t = Todos.create(Lang.t("Neue Aufgabe", "New task"), "DAILY", 24);
            this.host.openSub(new TodoEditPanel(this.host, t));
        }));
        this.rows.add(new ChipsRow((Component)Component.literal((String)Lang.t("Vorlagen", "Templates")), List.of(new ChipsRow.Chip(Lang.t("Voten", "Vote"), 0), new ChipsRow.Chip("Kit 24 h", 1), new ChipsRow.Chip(Lang.t("Farm 6 h", "Farm 6 h"), 2), new ChipsRow.Chip("Spawner", 3)), () -> -1, v -> {
            Todos.template(v);
            Todos.tickSound();
            this.rebuild();
        }));
        this.rows.add(new ButtonRow((Component)Component.literal((String)Lang.t("HUD-Karte & Erinnerungen", "HUD card & reminders")), () -> Component.literal((String)Lang.t("Einstellen", "Settings")), () -> this.host.openSub(new TodoSettingsPanel())));
        if (!all.isEmpty()) {
            this.rows.add(new TextRow((Component)Component.literal((String)Lang.t("Klick hakt ab \u00b7 Rechtsklick bearbeitet \u00b7 Ziehen sortiert", "Click ticks off \u00b7 right click edits \u00b7 drag sorts")), null));
        }
        this.shownSig = TodoPanel.signature(now);
    }

    private void rebuild() {
        this.setBounds(this.x, this.y, this.width, this.height);
    }

    private static long signature(long now) {
        List<Todos.Todo> all = Todos.all();
        long sig = all.size();
        for (int i = 0; i < all.size(); ++i) {
            Todos.Todo t = all.get(i);
            sig = sig * 31L + (long)t.id.hashCode() * 2L + (long)(Todos.isOpen(t, now) ? 1 : 0);
        }
        return sig;
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY, float delta) {
        long real = System.currentTimeMillis();
        if (this.rebuildAt > 0L && real >= this.rebuildAt && this.pressed == null) {
            this.rebuildAt = 0L;
            this.rebuild();
        } else if (this.pressed == null && this.rebuildAt == 0L && real - this.sigCheckedAt > 1000L) {
            this.sigCheckedAt = real;
            if (TodoPanel.signature(Todos.now()) != this.shownSig) {
                this.rebuild();
            }
        }
        super.render(ctx, font, mouseX, mouseY, delta);
        if (this.dragging && this.pressed != null) {
            LauncherGfx.ring(ctx, this.pressed.x(), this.pressed.y(), this.pressed.w(), 20, 4, FwmTheme.ACCENT);
            TodoRow target = this.rowAtY(this.dragY);
            if (target != null && target != this.pressed) {
                int ly = target.y() < this.pressed.y() ? target.y() : target.y() + 20 - 1;
                ctx.fill(target.x() + 4, ly, target.x() + target.w() - 4, ly + 1, FwmTheme.ACCENT);
            }
        }
    }

    private TodoRow rowAt(double mx, double my) {
        for (Row r : this.rows) {
            TodoRow tr;
            if (!(r instanceof TodoRow) || !(tr = (TodoRow)r).isHovered(mx, my)) continue;
            return tr;
        }
        return null;
    }

    private TodoRow rowAtY(double my) {
        for (Row r : this.rows) {
            TodoRow tr;
            if (!(r instanceof TodoRow) || !(my >= (double)(tr = (TodoRow)r).y()) || !(my < (double)(tr.y() + 20))) continue;
            return tr;
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.confirm.open()) {
            TodoRow r = this.rowAt(mouseX, mouseY);
            if (r != null && button == 0) {
                this.pressed = r;
                this.pressY = mouseY;
                this.dragY = mouseY;
                this.dragging = false;
                return true;
            }
            if (r != null && button == 1) {
                this.host.openSub(new TodoEditPanel(this.host, r.todo));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        if (this.pressed != null) {
            this.dragY = mouseY;
            if (Math.abs(mouseY - this.pressY) > 4.0) {
                this.dragging = true;
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased() {
        if (this.pressed != null) {
            TodoRow r = this.pressed;
            this.pressed = null;
            if (this.dragging) {
                this.dragging = false;
                TodoRow target = this.rowAtY(this.dragY);
                if (target != null && target != r) {
                    Todos.move(r.todo, Todos.all().indexOf(target.todo));
                    this.rebuild();
                }
            } else {
                this.toggle(r.todo);
            }
        }
        super.mouseReleased();
    }

    private void toggle(Todos.Todo t) {
        if (Todos.isOpen(t, Todos.now())) {
            Todos.markDone(t, true);
            Todos.tickSound();
        } else {
            Todos.undo(t);
        }
        this.rebuildAt = System.currentTimeMillis() + 450L;
    }

    @Override
    public void onHidden() {
        Todos.save();
    }

    static final class TodoRow
    extends Row {
        final Todos.Todo todo;

        TodoRow(Todos.Todo todo) {
            super((Component)Component.literal((String)todo.name));
            this.todo = todo;
        }

        int x() {
            return this.x;
        }

        int y() {
            return this.y;
        }

        int w() {
            return this.width;
        }

        @Override
        public void render(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY) {
            int infoColor;
            Object info;
            if (this.isHovered(mouseX, mouseY)) {
                FwmTheme.hover(ctx, this.x, this.y, this.width, 20);
            }
            long now = Todos.now();
            boolean open = Todos.isOpen(this.todo, now);
            boolean here = Todos.appliesHere(this.todo);
            int px = this.x + this.padding();
            int cy = this.y + 10;
            if (open) {
                LauncherGfx.ring(ctx, px, cy - 5, 10, 10, 5, here ? -578948922 : 1719529670);
            } else {
                LauncherGfx.round(ctx, px, cy - 5, 10, 10, 5, -12723068);
                ctx.text(font, "\u2714", px + 2, cy - 4, -16440561, false);
            }
            long fx = System.currentTimeMillis() - this.todo.fxAt;
            if (this.todo.fxAt > 0L && fx < 400L) {
                float p = (float)fx / 400.0f;
                int r = 5 + Math.round(p * 6.0f);
                int a = Math.round((1.0f - p) * 200.0f);
                LauncherGfx.ring(ctx, px + 5 - r, cy - r, r * 2, r * 2, r, a << 24 | 0x7DF0C6);
            }
            ctx.item(Todos.icon(this.todo), px + 14, this.y + 2);
            if (open) {
                int streak = Todos.streakNow(this.todo, now);
                info = Todos.kind(this.todo) + (String)(streak > 0 ? "  \u00b7  " + streak + Lang.t(" Tage am St\u00fcck", " days in a row") : "");
                infoColor = FwmTheme.TEXT_DIM;
            } else {
                long at = Todos.reopensAt(this.todo, now);
                info = at < 0L ? Lang.t("erledigt", "done") : Lang.t("wieder offen in ", "open again in ") + Todos.duration(at - now);
                infoColor = -8416120;
            }
            if (!here) {
                info = Lang.t("anderer Server", "other server") + "  \u00b7  " + (String)info;
            }
            int right = this.x + this.width - this.padding();
            int nameX = px + 34;
            int infoW = Math.min(font.width((String)info), Math.max(40, (right - nameX) / 2));
            Object infoShown = font.width((String)info) > infoW ? font.plainSubstrByWidth((String)info, infoW) : info;
            ctx.text(font, (String)infoShown, right - font.width((String)infoShown), this.textY(font), infoColor, false);
            int nameRoom = right - font.width((String)infoShown) - 8 - nameX;
            String name = this.todo.name;
            if (font.width(name) > nameRoom) {
                name = font.plainSubstrByWidth(name, Math.max(8, nameRoom));
            }
            int nameColor = !open ? -7693680 : (here ? FwmTheme.TEXT : FwmTheme.TEXT_DIM);
            ctx.text(font, name, nameX, this.textY(font), nameColor, false);
            if (!open) {
                int lineY = this.textY(font) + 3;
                ctx.fill(nameX, lineY, nameX + font.width(name), lineY + 1, -2138400112);
            }
        }

        @Override
        protected void renderControl(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY) {
        }
    }
}

