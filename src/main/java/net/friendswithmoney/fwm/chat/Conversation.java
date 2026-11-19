/*
 * Decompiled with CFR 0.152.
 */
package net.friendswithmoney.fwm.chat;

import java.util.ArrayList;
import java.util.List;
import net.friendswithmoney.fwm.chat.PrivateMessage;

public class Conversation {
    public String partner;
    public List<PrivateMessage> messages = new ArrayList<PrivateMessage>();
    public int unread;
    public long lastActivity;

    public Conversation() {
    }

    public Conversation(String partner) {
        this.partner = partner;
    }

    public void add(PrivateMessage message, int keep) {
        this.add(message, keep, true);
    }

    public void add(PrivateMessage message, int keep, boolean count) {
        if (this.messages == null) {
            this.messages = new ArrayList<PrivateMessage>();
        }
        this.messages.add(message);
        this.lastActivity = message.time;
        if (!message.outgoing && count) {
            ++this.unread;
        }
        while (this.messages.size() > keep) {
            this.messages.remove(0);
        }
    }

    public PrivateMessage newest() {
        if (this.messages == null || this.messages.isEmpty()) {
            return null;
        }
        return this.messages.get(this.messages.size() - 1);
    }
}

