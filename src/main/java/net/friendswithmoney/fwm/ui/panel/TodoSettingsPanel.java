/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package net.friendswithmoney.fwm.ui.panel;

import net.friendswithmoney.fwm.game.Todos;
import net.friendswithmoney.fwm.ui.Lang;
import net.friendswithmoney.fwm.ui.panel.RowsPanel;
import net.friendswithmoney.fwm.ui.widget.ButtonRow;
import net.friendswithmoney.fwm.ui.widget.SliderRow;
import net.friendswithmoney.fwm.ui.widget.TextRow;
import net.friendswithmoney.fwm.ui.widget.ToggleRow;
import net.minecraft.network.chat.Component;

public class TodoSettingsPanel
extends RowsPanel {
    @Override
    public Component title() {
        return Component.literal((String)Lang.t("To-Dos: HUD & Erinnerungen", "To-dos: HUD & reminders"));
    }

    @Override
    protected void buildRows() {
        Todos.Settings s = Todos.settings();
        this.rows.add(new TextRow((Component)Component.literal((String)Lang.t("HUD-Karte", "HUD card"))));
        this.rows.add(new ToggleRow((Component)Component.literal((String)Lang.t("HUD-Karte zeigen", "Show the HUD card")), () -> s.hud, v -> {
            s.hud = v;
            Todos.save();
        }));
        this.rows.add(new ToggleRow((Component)Component.literal((String)Lang.t("Nur offene zeigen", "Only open ones")), () -> s.hudOnlyOpen, v -> {
            s.hudOnlyOpen = v;
            Todos.save();
        }));
        this.rows.add(new SliderRow((Component)Component.literal((String)Lang.t("H\u00f6chstens Zeilen", "At most lines")), 1, 10, () -> s.hudMaxLines, v -> {
            s.hudMaxLines = v;
            Todos.save();
        }, v -> Component.literal((String)String.valueOf(v))));
        this.rows.add(new ToggleRow((Component)Component.literal((String)Lang.t("Ausblenden, wenn alles erledigt", "Hide when all done")), () -> s.hudHideWhenDone, v -> {
            s.hudHideWhenDone = v;
            Todos.save();
        }));
        this.rows.add(new TextRow((Component)Component.literal((String)Lang.t("Erinnerung", "Reminder"))));
        this.rows.add(new ToggleRow((Component)Component.literal((String)Lang.t("Erinnerungen", "Reminders")), () -> s.reminders, v -> {
            s.reminders = v;
            Todos.save();
        }));
        this.rows.add(new ToggleRow((Component)Component.literal((String)Lang.t("Chat-Zeile mit [Erledigt] [Sp\u00e4ter]", "Chat line with [Done] [Later]")), () -> s.chatLine, v -> {
            s.chatLine = v;
            Todos.save();
        }));
        this.rows.add(new ToggleRow((Component)Component.literal((String)Lang.t("Beim Joinen \"3 To-Dos offen\"", "On join \"3 to-dos open\"")), () -> s.joinNote, v -> {
            s.joinNote = v;
            Todos.save();
        }));
        this.rows.add(new SliderRow((Component)Component.literal((String)Lang.t("Lautst\u00e4rke", "Volume")), 0, 100, () -> s.volume, v -> {
            s.volume = v;
            Todos.save();
        }, v -> Component.literal((String)(v == 0 ? Lang.t("aus", "off") : v + " %"))));
        this.rows.add(new ButtonRow((Component)Component.literal((String)Lang.t("Ton testen", "Test the sound")), () -> Component.literal((String)Lang.t("Abspielen", "Play")), Todos::tickSound));
        this.rows.add(new TextRow((Component)Component.literal((String)Lang.t("Nie mehr als eine Erinnerung pro Aufgabe und Stunde.", "Never more than one reminder per task and hour.")), null));
    }
}

