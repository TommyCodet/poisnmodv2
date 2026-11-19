/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 */
package net.friendswithmoney.fwm.ui.panel;

import java.util.List;
import net.friendswithmoney.fwm.game.Todos;
import net.friendswithmoney.fwm.ui.FwmTheme;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.panel.PanelHost;
import net.friendswithmoney.fwm.ui.panel.RowsPanel;
import net.friendswithmoney.fwm.ui.widget.ButtonRow;
import net.friendswithmoney.fwm.ui.widget.ChipsRow;
import net.friendswithmoney.fwm.ui.widget.Row;
import net.friendswithmoney.fwm.ui.widget.SliderRow;
import net.friendswithmoney.fwm.ui.widget.TextInputRow;
import net.friendswithmoney.fwm.ui.widget.TextRow;
import net.friendswithmoney.fwm.ui.widget.ToggleRow;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

public class TodoEditPanel
extends RowsPanel {
    private final PanelHost host;
    private final Todos.Todo todo;

    public TodoEditPanel(PanelHost host, Todos.Todo todo) {
        this.host = host;
        this.todo = todo;
    }

    @Override
    public Component title() {
        return Component.literal((String)Lang.t("To-Do bearbeiten", "Edit to-do"));
    }

    private void changed(boolean rebuild) {
        Todos.sanitize(this.todo);
        Todos.save();
        if (rebuild) {
            this.setBounds(this.x, this.y, this.width, this.height);
        }
    }

    @Override
    protected void buildRows() {
        Todos.Todo t = this.todo;
        this.rows.add(new TextInputRow((Component)Component.literal((String)"Name"), () -> t.name, v -> {
            if (!v.isBlank()) {
                t.name = v;
            }
            this.changed(false);
        }, Lang.t("z. B. Voten", "e.g. Vote"), 32));
        this.rows.add(new IconRow((Component)Component.literal((String)Lang.t("Symbol", "Icon")), t, () -> this.changed(false)));
        this.rows.add(new ChipsRow((Component)Component.literal((String)Lang.t("Art", "Kind")), List.of(new ChipsRow.Chip(Lang.t("Einmal", "Once"), 0), new ChipsRow.Chip(Lang.t("T\u00e4glich", "Daily"), 1), new ChipsRow.Chip(Lang.t("Alle X h", "Every X h"), 2)), () -> "ONCE".equals(t.type) ? 0 : ("EVERY".equals(t.type) ? 2 : 1), v -> {
            t.type = v == 0 ? "ONCE" : (v == 2 ? "EVERY" : "DAILY");
            this.changed(true);
        }));
        if ("DAILY".equals(t.type)) {
            this.rows.add(new SliderRow((Component)Component.literal((String)Lang.t("Wieder offen um", "Opens again at")), 0, 95, () -> t.resetTime / 15, v -> {
                t.resetTime = v * 15;
                this.changed(false);
            }, v -> Component.literal((String)Todos.hhmm(v * 15)), TodoEditPanel::quarters));
        } else if ("EVERY".equals(t.type)) {
            this.rows.add(new SliderRow((Component)Component.literal((String)Lang.t("Wieder offen nach", "Opens again after")), 1, 168, () -> t.hours, v -> {
                t.hours = v;
                this.changed(false);
            }, v -> Component.literal((String)(v + " h")), TodoEditPanel::number));
        }
        this.rows.add(new ToggleRow((Component)Component.literal((String)Lang.t("Erinnern um eine Uhrzeit", "Remind at a time")), () -> t.remindAt >= 0, v -> {
            t.remindAt = v != false ? 1080 : -1;
            this.changed(true);
        }));
        if (t.remindAt >= 0) {
            this.rows.add(new SliderRow((Component)Component.literal((String)Lang.t("   Uhrzeit (wenn noch offen)", "   Time (if still open)")), 0, 95, () -> t.remindAt / 15, v -> {
                t.remindAt = v * 15;
                this.changed(false);
            }, v -> Component.literal((String)Todos.hhmm(v * 15)), TodoEditPanel::quarters));
        }
        if (!"ONCE".equals(t.type)) {
            this.rows.add(new ToggleRow((Component)Component.literal((String)Lang.t("Erinnern, sobald wieder offen", "Remind when open again")), () -> t.remindWhenOpen, v -> {
                t.remindWhenOpen = v;
                this.changed(false);
            }));
        }
        this.rows.add(new ChipsRow((Component)Component.literal((String)Lang.t("Gilt f\u00fcr", "Counts on")), List.of(new ChipsRow.Chip(Lang.t("Dieser Server", "This server"), 0), new ChipsRow.Chip(Lang.t("\u00dcberall", "Everywhere"), 1)), () -> "HERE".equals(t.server) ? 0 : 1, v -> {
            Todos.setScope(t, v == 0 ? "HERE" : "ALL");
            this.changed(false);
        }));
        if ("DAILY".equals(t.type)) {
            int streak = Todos.streakNow(t, Todos.now());
            this.rows.add(new TextRow((Component)Component.literal((String)Lang.t("Serie", "Streak")), (Component)Component.literal((String)(streak > 0 ? Lang.t(streak + " Tage am St\u00fcck", streak + " days in a row") : Lang.t("noch keine", "none yet")))));
        }
        this.rows.add(new ButtonRow((Component)Component.literal((String)Lang.t("Aufgabe l\u00f6schen", "Delete task")), () -> Component.literal((String)Lang.t("L\u00f6schen", "Delete")), () -> this.confirm.ask((Component)Component.literal((String)Lang.t("\"" + t.name + "\" l\u00f6schen?", "Delete \"" + t.name + "\"?")), () -> {
            Todos.delete(t);
            this.host.closeSub();
        })));
        this.rows.add(new ButtonRow((Component)Component.literal((String)""), () -> Component.literal((String)Lang.t("Fertig", "Done")), this.host::closeSub));
    }

    @Override
    public void onHidden() {
        for (Row r : this.rows) {
            r.commitTyped();
        }
        Todos.save();
    }

    static long quarters(String typed) {
        int h;
        String s = typed.trim().replace('.', ':');
        int m = 0;
        try {
            int colon = s.indexOf(58);
            if (colon >= 0) {
                h = Integer.parseInt(s.substring(0, colon).trim());
                String mm = s.substring(colon + 1).trim();
                if (!mm.isEmpty()) {
                    m = Integer.parseInt(mm);
                }
            } else if (s.length() >= 3) {
                int v = Integer.parseInt(s);
                h = v / 100;
                m = v % 100;
            } else {
                h = Integer.parseInt(s);
            }
        }
        catch (NumberFormatException e) {
            return 0L;
        }
        int minute = Math.max(0, Math.min(1439, h * 60 + m));
        return Math.round((float)minute / 15.0f) % 96;
    }

    static long number(String typed) {
        StringBuilder d = new StringBuilder();
        for (char c : typed.toCharArray()) {
            if (!Character.isDigit(c)) continue;
            d.append(c);
        }
        if (d.isEmpty()) {
            return 1L;
        }
        try {
            return Long.parseLong(d.toString());
        }
        catch (NumberFormatException e) {
            return 168L;
        }
    }

    static final class IconRow
    extends Row {
        private static final int CELL = 18;
        private final Todos.Todo todo;
        private final Runnable onChange;

        IconRow(Component label, Todos.Todo todo, Runnable onChange) {
            super(label);
            this.todo = todo;
            this.onChange = onChange;
        }

        private int startX() {
            return this.x + this.width - this.padding() - Todos.ICON_KEYS.length * 18;
        }

        @Override
        protected Component trimmedLabel(Font font) {
            int room = this.startX() - this.x - this.padding() - 4;
            return font.width((FormattedText)this.label) <= room ? this.label : Component.literal((String)font.plainSubstrByWidth(this.label.getString(), Math.max(8, room)));
        }

        @Override
        protected void renderControl(GuiGraphicsExtractor ctx, Font font, int mouseX, int mouseY) {
            int sx = this.startX();
            int chosen = Todos.iconIndex(this.todo.icon);
            for (int i = 0; i < Todos.ICON_KEYS.length; ++i) {
                boolean hovered;
                int cx = sx + i * 18;
                boolean on = i == chosen;
                boolean bl = hovered = mouseX >= cx && mouseX < cx + 18 && mouseY >= this.y && mouseY < this.y + 20;
                if (on || hovered) {
                    FwmTheme.box(ctx, cx, this.y + 1, 18, 18, on ? FwmTheme.ACCENT_DIM : FwmTheme.RAISED, on ? FwmTheme.ACCENT : FwmTheme.OFF);
                }
                ctx.item(Todos.iconStack(i), cx + 1, this.y + 2);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0 || !this.isHovered(mouseX, mouseY)) {
                return false;
            }
            int i = (int)Math.floor((mouseX - (double)this.startX()) / 18.0);
            if (i < 0 || i >= Todos.ICON_KEYS.length) {
                return false;
            }
            this.todo.icon = Todos.ICON_KEYS[i];
            this.onChange.run();
            return true;
        }
    }
}

