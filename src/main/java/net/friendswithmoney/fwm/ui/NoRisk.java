/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.phys.Vec3
 */
package net.friendswithmoney.fwm.ui;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.loader.api.FabricLoader;
import net.friendswithmoney.fwm.config.FwmConfig;
import net.friendswithmoney.fwm.net.MemberData;
import net.friendswithmoney.fwm.net.Members;
import net.friendswithmoney.fwm.net.ServerContext;
import net.friendswithmoney.fwm.net.TrustedClans;
import net.friendswithmoney.fwm.ui.MemberTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.Vec3;

public final class NoRisk {
    private static final String MOD_ID = "nrcclient";
    private static final String EVENTS = "gg.norisk.compat.nametag.NameTagRenderEventKt";
    private static final String EVENT = "gg.norisk.compat.nametag.NameTagRenderEvent";
    private static final String MANAGER = "gg.norisk.compat.nametag.NameTagManager";
    private static final String CONFIG = "gg.norisk.compat.nametag.NameTagConfig";
    private static final String PRIORITY = "gg.norisk.abstraction.event.EventPriority";
    private static final String FUNCTION1 = "kotlin.jvm.functions.Function1";
    private static final String UNIT = "kotlin.Unit";
    private static final String PING_CLASS = "gg.norisk.client.v2.modules.ping.PingHud";
    private static final double LINE_H = 0.3;
    private static final float SCALE = 1.0f;
    private static boolean attached;
    private static boolean attachFailed;
    private static Object manager;
    private static Method renderNameTag;
    private static Constructor<?> configCtor;
    private static Method getUuid;
    private static Method getNameTagPos;
    private static Method isSneaking;
    private static Method getDistance;
    private static Method getContext;
    private static final Map<UUID, Long> DRAWN;
    private static final long HANDLED_MS = 250L;
    private static long checkedAt;
    private static double lift;
    private static Object pingModule;
    private static Method position;
    private static Method scale;
    private static boolean pingUnavailable;

    private NoRisk() {
    }

    public static boolean handled(UUID uuid) {
        Long at = DRAWN.get(uuid);
        return at != null && System.currentTimeMillis() - at < 250L;
    }

    public static boolean attached() {
        return attached;
    }

    public static void attach() {
        if (attached || attachFailed) {
            return;
        }
        if (!FabricLoader.getInstance().isModLoaded(MOD_ID)) {
            attachFailed = true;
            return;
        }
        try {
            Object stream = Class.forName(EVENTS).getMethod("getAfterRenderNameTagEvent", new Class[0]).invoke(null, new Object[0]);
            Class<?> managerType = Class.forName(MANAGER);
            manager = managerType.getField("INSTANCE").get(null);
            renderNameTag = NoRisk.method(managerType, "renderNameTag", 6);
            configCtor = NoRisk.constructor(Class.forName(CONFIG), 4);
            Class<?> eventType = Class.forName(EVENT);
            getUuid = eventType.getMethod("getUuid", new Class[0]);
            getNameTagPos = eventType.getMethod("getNameTagPos", new Class[0]);
            isSneaking = eventType.getMethod("isSneaking", new Class[0]);
            getDistance = eventType.getMethod("getSquaredDistanceToCamera", new Class[0]);
            getContext = eventType.getMethod("getContext", new Class[0]);
            Object priority = NoRisk.priority();
            NoRisk.method(stream.getClass(), "listen", 2).invoke(stream, priority, NoRisk.listener());
            attached = true;
        }
        catch (Throwable ignored) {
            attachFailed = true;
            manager = null;
        }
    }

    private static Object priority() throws ClassNotFoundException {
        return Enum.valueOf(Class.forName(PRIORITY), "LAST");
    }

    private static Object listener() throws Exception {
        Class<?> function = Class.forName(FUNCTION1);
        Object unit = Class.forName(UNIT).getField("INSTANCE").get(null);
        return Proxy.newProxyInstance(function.getClassLoader(), new Class[]{function}, (proxy, method, args) -> {
            if ("invoke".equals(method.getName()) && args != null && args.length == 1) {
                NoRisk.onNameTag(args[0]);
                return unit;
            }
            if ("equals".equals(method.getName())) {
                return proxy == args[0];
            }
            if ("hashCode".equals(method.getName())) {
                return System.identityHashCode(proxy);
            }
            if ("toString".equals(method.getName())) {
                return "fwm-badge";
            }
            return null;
        });
    }

    private static void onNameTag(Object event) {
        try {
            MutableComponent label;
            UUID uuid = (UUID)getUuid.invoke(event, new Object[0]);
            if (uuid == null) {
                return;
            }
            Minecraft client = Minecraft.getInstance();
            if (client == null || client.level == null || client.player == null) {
                return;
            }
            if (!ServerContext.hugo()) {
                return;
            }
            if (uuid.equals(client.player.getUUID()) && client.options.getCameraType().isFirstPerson()) {
                return;
            }
            AbstractClientPlayer player = null;
            for (AbstractClientPlayer candidate : client.level.players()) {
                if (!uuid.equals(candidate.getUUID())) continue;
                player = candidate;
                break;
            }
            if (player == null || player.getGameProfile() == null) {
                return;
            }
            MemberData.Member member = Members.member(player.getGameProfile().name());
            if (MemberTags.wearsFake(player)) {
                return;
            }
            if (member != null) {
                if (MemberTags.wearsForeignTag(player)) {
                    return;
                }
                label = MemberTags.label(member);
            } else {
                label = MemberTags.wearsMarket(player) != null ? MemberTags.marketLabel(MemberTags.wearsMarket(player)) : null;
            }
            MutableComponent extra = TrustedClans.playerLabel(player.getUUID());
            if (MemberTags.isOwner(member) && label != null) {
                extra = MemberTags.ownerLogo();
            }
            if (label == null) {
                label = extra;
                extra = null;
            }
            if (label == null) {
                return;
            }
            double range = Math.max(1.0, FwmConfig.INSTANCE.memberTagRange);
            Object config = configCtor.newInstance(Float.valueOf(1.0f), false, false, range * range);
            Object pos = getNameTagPos.invoke(event, new Object[0]);
            double squared = (Double)getDistance.invoke(event, new Object[0]);
            double lift = MemberTags.belowNameLift(client.level, squared);
            if (lift > 0.0 && pos instanceof Vec3) {
                Vec3 at = (Vec3)pos;
                pos = at.add(0.0, lift, 0.0);
            }
            List<MemberTags.BadgeLine> badgeLines = MemberTags.badgeLines(player, member, (Component)label, (Component)extra);
            for (int lineIndex = 0; lineIndex < badgeLines.size(); ++lineIndex) {
                MemberTags.BadgeLine line = badgeLines.get(lineIndex);
                Object linePos = pos;
                if (lineIndex > 0) {
                    if (!(pos instanceof Vec3)) break;
                    Vec3 at = (Vec3)pos;
                    linePos = at.add(0.0, (double)lineIndex * 0.23, 0.0);
                }
                Object lineConfig = line.scale() == 1.0f ? config : configCtor.newInstance(Float.valueOf(1.0f * line.scale()), false, false, range * range);
                renderNameTag.invoke(manager, line.text(), lineConfig, linePos, isSneaking.invoke(event, new Object[0]), squared, getContext.invoke(event, new Object[0]));
            }
            if (DRAWN.size() > 200) {
                DRAWN.clear();
            }
            DRAWN.put(uuid, System.currentTimeMillis());
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static double extraLift() {
        if (attached) {
            return 0.0;
        }
        long now = System.currentTimeMillis();
        if (now - checkedAt < 1000L) {
            return lift;
        }
        checkedAt = now;
        lift = NoRisk.measure();
        return lift;
    }

    private static double measure() {
        if (pingUnavailable) {
            return 0.0;
        }
        try {
            double d;
            Enum value;
            Object where;
            if (pingModule == null) {
                if (!FabricLoader.getInstance().isModLoaded(MOD_ID)) {
                    pingUnavailable = true;
                    return 0.0;
                }
                Class<?> type = Class.forName(PING_CLASS);
                pingModule = type.getField("INSTANCE").get(null);
                position = type.getMethod("getPingNameTagPosition", new Class[0]);
                scale = type.getMethod("getPingNameTagScale", new Class[0]);
            }
            if (!((where = position.invoke(pingModule, new Object[0])) instanceof Enum) || !"ABOVE".equals((value = (Enum)where).name())) {
                return 0.0;
            }
            Object size = scale.invoke(pingModule, new Object[0]);
            if (size instanceof Number) {
                Number number = (Number)size;
                d = number.doubleValue();
            } else {
                d = 1.0;
            }
            double factor = d;
            return 0.3 * Math.max(0.1, Math.min(3.0, factor));
        }
        catch (Throwable ignored) {
            pingUnavailable = true;
            pingModule = null;
            return 0.0;
        }
    }

    private static Method method(Class<?> type, String name, int args) {
        for (Method candidate : type.getMethods()) {
            if (!candidate.getName().equals(name) || candidate.getParameterCount() != args) continue;
            return candidate;
        }
        throw new IllegalStateException(name);
    }

    private static Constructor<?> constructor(Class<?> type, int args) {
        for (Constructor<?> candidate : type.getConstructors()) {
            if (candidate.getParameterCount() != args) continue;
            return candidate;
        }
        throw new IllegalStateException(type.getName());
    }

    static {
        DRAWN = new HashMap<UUID, Long>();
    }
}

