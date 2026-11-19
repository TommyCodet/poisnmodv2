/*
 * Decompiled with CFR 0.152.
 */
package net.friendswithmoney.fwm.ui;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.friendswithmoney.fwm.stream.DonationFx;

public final class FwmNotice {
    private static final ConcurrentLinkedQueue<Notice> QUEUE = new ConcurrentLinkedQueue();
    private static Notice current;
    private static long since;
    private static long heldAt;
    private static final ArrayDeque<Entry> HISTORY;

    private FwmNotice() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void show(String head, String title, String line, int color, long ms) {
        Notice n = new Notice(head, title == null ? "" : title, line == null ? "" : line, 0xFF000000 | color, ms);
        QUEUE.add(n);
        ArrayDeque<Entry> arrayDeque = HISTORY;
        synchronized (arrayDeque) {
            HISTORY.addFirst(new Entry(System.currentTimeMillis(), n));
            while (HISTORY.size() > 60) {
                HISTORY.removeLast();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static List<Entry> history() {
        ArrayDeque<Entry> arrayDeque = HISTORY;
        synchronized (arrayDeque) {
            return new ArrayList<Entry>(HISTORY);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void clearHistory() {
        ArrayDeque<Entry> arrayDeque = HISTORY;
        synchronized (arrayDeque) {
            HISTORY.clear();
        }
    }

    static Notice current() {
        long now = System.currentTimeMillis();
        if (DonationFx.playing()) {
            if (heldAt == 0L) {
                heldAt = now;
            }
            return null;
        }
        if (heldAt != 0L) {
            since += now - heldAt;
            heldAt = 0L;
        }
        if (current != null && now - since >= current.ms()) {
            current = null;
        }
        if (current == null) {
            current = QUEUE.poll();
            since = now;
        }
        return current;
    }

    static float progress() {
        return current == null ? 1.0f : Math.min(1.0f, (float)(System.currentTimeMillis() - since) / (float)current.ms());
    }

    static {
        HISTORY = new ArrayDeque();
    }

    public record Notice(String head, String title, String line, int color, long ms) {
    }

    public record Entry(long at, Notice notice) {
    }
}

