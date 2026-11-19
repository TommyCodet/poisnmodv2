/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.ComponentSerialization
 */
package net.friendswithmoney.fwm.chat;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.friendswithmoney.fwm.config.V2;
import net.friendswithmoney.fwm.net.ServerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public final class ChatLog {
    private ChatLog() {
    }

    public static void write(String kind, Component message) {
        if (!V2.INSTANCE.chatJsonLog || message == null) {
            return;
        }
        try {
            Minecraft client = Minecraft.getInstance();
            JsonOps ops = client.level == null ? JsonOps.INSTANCE : client.level.registryAccess().createSerializationContext((DynamicOps)JsonOps.INSTANCE);
            String json = ComponentSerialization.CODEC.encodeStart((DynamicOps)ops, (Object)message).result().map(Object::toString).orElse("?");
            System.out.println("[FWM-ChatJSON] " + String.valueOf((Object)ServerContext.current()) + " " + kind + " " + json);
        }
        catch (Exception e) {
            System.out.println("[FWM-ChatJSON] " + kind + " (unreadable) " + message.getString());
        }
    }
}

