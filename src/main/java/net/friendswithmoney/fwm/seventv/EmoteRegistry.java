/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 */
package net.friendswithmoney.fwm.seventv;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.friendswithmoney.fwm.seventv.Emote;
import net.friendswithmoney.fwm.seventv.EmoteApi;
import net.friendswithmoney.fwm.seventv.EmoteTexture;
import net.friendswithmoney.fwm.seventv.SevenTv;
import net.minecraft.client.Minecraft;

public final class EmoteRegistry {
    private static final Map<String, Emote> BY_KEY = new ConcurrentHashMap<String, Emote>();
    private static final Map<String, Emote> BY_NAME = new ConcurrentHashMap<String, Emote>();
    private static final Set<String> NOT_FOUND = ConcurrentHashMap.newKeySet();
    private static final Set<String> PENDING = ConcurrentHashMap.newKeySet();

    private EmoteRegistry() {
    }

    public static void register(Emote e) {
        BY_KEY.putIfAbsent(e.key(), e);
    }

    public static void bindName(Emote e) {
        EmoteRegistry.register(e);
        BY_NAME.put(e.name(), e);
        NOT_FOUND.remove(e.name());
    }

    public static Emote byKey(String key) {
        return BY_KEY.get(key);
    }

    public static Emote byName(String name) {
        return BY_NAME.get(name);
    }

    public static List<Emote> startingWith(String prefix, int limit) {
        String p = prefix.toLowerCase(Locale.ROOT);
        LinkedHashMap<String, Emote> byName = new LinkedHashMap<String, Emote>();
        for (Emote e2 : BY_KEY.values()) {
            Emote had;
            if (!e2.name().toLowerCase(Locale.ROOT).startsWith(p) || (had = (Emote)byName.get(e2.name())) != null && (had.provider() == Emote.Provider.SEVENTV || e2.provider() != Emote.Provider.SEVENTV)) continue;
            byName.put(e2.name(), e2);
        }
        ArrayList<Emote> out = new ArrayList<Emote>(byName.values());
        out.sort(Comparator.comparingInt(e -> e.name().length()).thenComparing(Emote::name));
        return out.size() > limit ? out.subList(0, limit) : out;
    }

    public static boolean isKnownMissing(String name) {
        return NOT_FOUND.contains(name);
    }

    public static void resolveAsync(String name) {
        if (BY_NAME.containsKey(name) || NOT_FOUND.contains(name) || !PENDING.add(name)) {
            return;
        }
        EmoteApi.lookupByName(name).whenComplete((emote, err) -> {
            PENDING.remove(name);
            if (err != null || emote == null) {
                NOT_FOUND.add(name);
                return;
            }
            EmoteRegistry.register(emote);
            BY_NAME.putIfAbsent(name, (Emote)emote);
            EmoteTexture.get(emote);
            Minecraft.getInstance().execute(SevenTv::requestChatRefresh);
        });
    }
}

