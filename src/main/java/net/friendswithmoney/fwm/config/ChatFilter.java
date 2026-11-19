/*
 * Decompiled with CFR 0.152.
 */
package net.friendswithmoney.fwm.config;

import java.util.Locale;

public class ChatFilter {
    public String text;
    public boolean on = true;

    public ChatFilter() {
    }

    public ChatFilter(String text) {
        this.text = text;
    }

    public boolean matches(String line) {
        if (!this.on || this.text == null || this.text.isBlank()) {
            return false;
        }
        return line.toLowerCase(Locale.ROOT).contains(this.text.toLowerCase(Locale.ROOT));
    }
}

