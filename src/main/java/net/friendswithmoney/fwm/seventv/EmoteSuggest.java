/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.EditBox
 */
package net.friendswithmoney.fwm.seventv;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.friendswithmoney.fwm.seventv.Emote;
import net.friendswithmoney.fwm.seventv.EmoteApi;
import net.friendswithmoney.fwm.seventv.EmoteConfig;
import net.friendswithmoney.fwm.seventv.EmotePicker;
import net.friendswithmoney.fwm.seventv.EmoteRegistry;
import net.friendswithmoney.fwm.seventv.EmoteText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;

public final class EmoteSuggest {
    private static final Pattern TYPING = Pattern.compile("(?:^|\\s):([A-Za-z0-9_\\-!?.'()]{2,40})$");
    private static final Map<String, Integer> CHAT_COMMANDS = Map.ofEntries(Map.entry("msg", 2), Map.entry("tell", 2), Map.entry("w", 2), Map.entry("whisper", 2), Map.entry("m", 2), Map.entry("r", 1), Map.entry("reply", 1), Map.entry("cc", 1), Map.entry("clanchat", 1), Map.entry("ac", 1), Map.entry("teamchat", 1), Map.entry("me", 1));
    private static final int MAX = 8;
    private static final int ROW_H = 12;
    private static final long SEARCH_DELAY_MS = 250L;
    private static String prefix;
    private static String dismissed;
    private static int start;
    private static List<Emote> shown;
    private static int selected;
    private static String searched;
    private static String pending;
    private static long pendingSince;
    private static final Set<String> SEARCHED;
    private static int boxX;
    private static int boxY;
    private static int boxW;

    static boolean allowedAt(String before) {
        if (!before.startsWith("/")) {
            return true;
        }
        String[] parts = before.substring(1).split(" ", -1);
        Integer textFrom = CHAT_COMMANDS.get(parts[0].toLowerCase(Locale.ROOT));
        return textFrom != null && parts.length > textFrom + 0 && parts.length - 1 >= textFrom;
    }

    private EmoteSuggest() {
    }

    public static boolean active() {
        return !shown.isEmpty();
    }

    public static void update(EditBox field) {
        String text = field.getValue();
        int cursor = Math.min(field.getCursorPosition(), text.length());
        String before = text.substring(0, cursor);
        Matcher m = TYPING.matcher(before);
        if (!EmoteSuggest.allowedAt(before) || !m.find()) {
            EmoteSuggest.clear();
            return;
        }
        String now = m.group(1);
        if (now.equals(dismissed)) {
            shown = List.of();
            return;
        }
        dismissed = null;
        if (!now.equals(prefix)) {
            prefix = now;
            selected = 0;
            pending = now;
            pendingSince = System.currentTimeMillis();
        }
        start = m.start(1) - 1;
        String key = now.toLowerCase(Locale.ROOT);
        if (!key.equals(searched) && System.currentTimeMillis() - pendingSince > 250L && SEARCHED.add(key)) {
            searched = key;
            EmoteApi.searchSevenTv(now, 1, 25).whenComplete((list, err) -> {
                if (list != null) {
                    for (Emote e : list) {
                        EmoteRegistry.register(e);
                    }
                }
            });
        }
        LinkedHashSet<String> names = new LinkedHashSet<String>();
        ArrayList<Emote> out = new ArrayList<Emote>();
        for (Emote e : EmoteConfig.frequent(200)) {
            if (!e.name().toLowerCase(Locale.ROOT).startsWith(key) || !names.add(e.name().toLowerCase(Locale.ROOT))) continue;
            out.add(e);
        }
        for (Emote e : EmoteRegistry.startingWith(now, 16)) {
            if (out.size() >= 8) break;
            if (!names.add(e.name().toLowerCase(Locale.ROOT))) continue;
            out.add(e);
        }
        List<Emote> list2 = shown = out.size() > 8 ? out.subList(0, 8) : out;
        if (selected >= shown.size()) {
            selected = 0;
        }
    }

    public static void clear() {
        prefix = null;
        shown = List.of();
        selected = 0;
    }

    public static void render(GuiGraphicsExtractor g, EditBox field, int mouseX, int mouseY) {
        if (shown.isEmpty()) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        int w = 0;
        for (Emote e : shown) {
            w = Math.max(w, font.width(e.chatCode()));
        }
        boxW = w + 22;
        String before = field.getValue().substring(0, Math.min(start, field.getValue().length()));
        boxX = Math.min(field.getX() + font.width(before) - 1, g.guiWidth() - boxW - 2);
        boxY = field.getY() - 4 - shown.size() * 12;
        g.nextStratum();
        g.fill(boxX, boxY, boxX + boxW, boxY + shown.size() * 12, -805306368);
        for (int i = 0; i < shown.size(); ++i) {
            boolean over;
            Emote e = shown.get(i);
            int ry = boxY + i * 12;
            boolean bl = over = mouseX >= boxX && mouseX < boxX + boxW && mouseY >= ry && mouseY < ry + 12;
            if (over) {
                selected = i;
            }
            if (i == selected) {
                g.fill(boxX, ry, boxX + boxW, ry + 12, 0x40FFFFFF);
            }
            EmotePicker.drawEmote(g, e, boxX + 2, ry + 1, 10, 10);
            g.text(font, e.chatCode(), boxX + 16, ry + 2, i == selected ? -171 : -5592406, true);
        }
    }

    public static boolean keyPressed(EditBox field, int key) {
        if (shown.isEmpty()) {
            return false;
        }
        switch (key) {
            case 258: {
                EmoteSuggest.accept(field, shown.get(selected));
                return true;
            }
            case 265: {
                selected = (selected + shown.size() - 1) % shown.size();
                return true;
            }
            case 264: {
                selected = (selected + 1) % shown.size();
                return true;
            }
            case 256: {
                dismissed = prefix;
                shown = List.of();
                return true;
            }
        }
        return false;
    }

    public static boolean mouseClicked(EditBox field, double mx, double my) {
        if (shown.isEmpty() || mx < (double)boxX || mx >= (double)(boxX + boxW) || my < (double)boxY || my >= (double)(boxY + shown.size() * 12)) {
            return false;
        }
        EmoteSuggest.accept(field, shown.get((int)((my - (double)boxY) / 12.0)));
        return true;
    }

    private static void accept(EditBox field, Emote e) {
        if (!EmoteText.roomFor(field.getValue())) {
            EmoteSuggest.clear();
            return;
        }
        String text = field.getValue();
        int cursor = Math.min(field.getCursorPosition(), text.length());
        String code = e.chatCode() + " ";
        String next = text.substring(0, start) + code + text.substring(cursor);
        if (next.length() > 256) {
            return;
        }
        field.setValue(next);
        field.moveCursorTo(start + code.length(), false);
        EmoteRegistry.bindName(e);
        EmoteConfig.recordUse(e);
        EmoteSuggest.clear();
    }

    static {
        shown = List.of();
        searched = "";
        pending = "";
        SEARCHED = new HashSet<String>();
    }
}

