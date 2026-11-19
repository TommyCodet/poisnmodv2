/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.Identifier
 */
package net.friendswithmoney.fwm.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.friendswithmoney.fwm.config.Features;
import net.friendswithmoney.fwm.config.V2;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class SoundMuffler {
    private SoundMuffler() {
    }

    public static float factor(SoundInstance sound) {
        if (sound == null || !Features.isOn("fwm.tab.sounds")) {
            return 1.0f;
        }
        Map<String, Integer> volumes = V2.INSTANCE.soundVolumes;
        if (volumes.isEmpty()) {
            return 1.0f;
        }
        Identifier id = sound.getIdentifier();
        if (id == null) {
            return 1.0f;
        }
        Integer v = volumes.get(id.toString());
        return v == null ? 1.0f : (float)Math.max(0, Math.min(100, v)) / 100.0f;
    }

    public static List<String> all(String query, boolean onlyChanged) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        ArrayList<String> out = new ArrayList<String>();
        for (Identifier id : BuiltInRegistries.SOUND_EVENT.keySet()) {
            String key = id.toString();
            if (onlyChanged && !V2.INSTANCE.soundVolumes.containsKey(key) || !q.isEmpty() && !key.contains(q) && !SoundMuffler.synonym(key).contains(q)) continue;
            out.add(key);
        }
        out.sort(String::compareTo);
        return out;
    }

    private static String synonym(String id) {
        StringBuilder b = new StringBuilder();
        if (id.contains("totem")) {
            b.append(" totem pop");
        }
        if (id.contains("generic.explode")) {
            b.append(" explosion kristall crystal tnt creeper bett anker");
        }
        if (id.contains("firework")) {
            b.append(" feuerwerk rakete rocket");
        }
        if (id.contains("villager")) {
            b.append(" dorfbewohner villager");
        }
        if (id.contains("chicken")) {
            b.append(" huhn h\u00fchner");
        }
        if (id.contains("piston")) {
            b.append(" kolben");
        }
        if (id.contains("spawner")) {
            b.append(" spawner");
        }
        if (id.contains("anvil")) {
            b.append(" amboss");
        }
        if (id.contains("ender_pearl")) {
            b.append(" perle pearl");
        }
        return b.toString();
    }

    public static int volume(String id) {
        Integer v = V2.INSTANCE.soundVolumes.get(id);
        return v == null ? 100 : v;
    }

    public static void set(String id, int percent) {
        if (percent >= 100) {
            V2.INSTANCE.soundVolumes.remove(id);
        } else {
            V2.INSTANCE.soundVolumes.put(id, Math.max(0, percent));
        }
        V2.save();
    }

    public static void preset(String which) {
        List<String> ids = switch (which) {
            case "farm" -> List.of("minecraft:block.piston.extend", "minecraft:block.piston.contract", "minecraft:entity.villager.ambient", "minecraft:entity.villager.trade", "minecraft:entity.villager.yes", "minecraft:entity.villager.no", "minecraft:entity.chicken.ambient", "minecraft:entity.chicken.egg", "minecraft:entity.item.pickup", "minecraft:block.dispenser.dispense", "minecraft:block.hopper.insert");
            case "pvp" -> List.of("minecraft:entity.firework_rocket.launch", "minecraft:entity.firework_rocket.blast", "minecraft:entity.firework_rocket.large_blast", "minecraft:entity.generic.explode", "minecraft:entity.player.attack.sweep", "minecraft:entity.player.attack.nodamage");
            default -> List.of();
        };
        for (String id : ids) {
            V2.INSTANCE.soundVolumes.put(id, 25);
        }
        V2.save();
    }

    public static void resetAll() {
        V2.INSTANCE.soundVolumes.clear();
        V2.save();
    }

    public static boolean shared(String id) {
        return id.equals("minecraft:entity.generic.explode");
    }
}

