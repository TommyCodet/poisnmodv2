/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.client.multiplayer.ServerData
 */
package net.friendswithmoney.fwm.net;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;

public final class ServerContext {
    public static volatile Server test;
    private static Object memoWorld;
    private static Object memoConn;
    private static long memoAt;
    private static Server memo;

    private ServerContext() {
    }

    public static Server current() {
        if (test != null) {
            return test;
        }
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.level == null) {
            return Server.NONE;
        }
        ClientPacketListener conn = client.getConnection();
        long now = System.currentTimeMillis();
        Server m = memo;
        if (m != null && memoWorld == client.level && memoConn == conn && now - memoAt < 250L) {
            return m;
        }
        memo = m = ServerContext.compute(client);
        memoWorld = client.level;
        memoConn = conn;
        memoAt = now;
        return m;
    }

    private static Server compute(Minecraft client) {
        if (client.isLocalServer() || client.getSingleplayerServer() != null) {
            return Server.SINGLE;
        }
        ServerData server = client.getCurrentServer();
        if (server == null) {
            return Server.SINGLE;
        }
        if (server.isRealm()) {
            return Server.OTHER;
        }
        Server byAddress = ServerContext.of(server.ip);
        if (byAddress != Server.OTHER) {
            return byAddress;
        }
        ClientPacketListener connection = client.getConnection();
        return connection == null ? Server.OTHER : ServerContext.of(connection.serverBrand());
    }

    public static Server of(String ip) {
        if (ip == null) {
            return Server.OTHER;
        }
        String a = ip.toLowerCase(Locale.ROOT);
        if (a.contains("hugosmp") || a.contains("hugopvp")) {
            return Server.HUGO;
        }
        if (a.contains("donutsmp")) {
            return Server.DONUT;
        }
        if (a.contains("windsmp")) {
            return Server.WIND;
        }
        return Server.OTHER;
    }

    public static boolean hugo() {
        return ServerContext.current() == Server.HUGO;
    }

    public static boolean singleplayer() {
        return ServerContext.current() == Server.SINGLE;
    }

    public static enum Server {
        HUGO("HugoSMP"),
        DONUT("DonutSMP"),
        WIND("WindSMP"),
        OTHER(null),
        SINGLE(null),
        NONE(null);

        public final String label;

        private Server(String label) {
            this.label = label;
        }
    }
}

