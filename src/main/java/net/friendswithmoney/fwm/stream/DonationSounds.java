/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.client.sounds.SoundManager
 *  net.minecraft.core.Holder$Reference
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 */
package net.friendswithmoney.fwm.stream;

import java.util.ArrayList;
import java.util.List;
import net.friendswithmoney.fwm.ui.Lang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public final class DonationSounds {
    public static final List<Sound> ALL = new ArrayList<Sound>();

    private static void add(String id, String de, String en) {
        ALL.add(new Sound(id, de, en));
    }

    private DonationSounds() {
    }

    public static int indexOf(String id) {
        for (int i = 0; i < ALL.size(); ++i) {
            if (!ALL.get(i).id().equals(id)) continue;
            return i;
        }
        return ALL.size() - 1;
    }

    public static String label(String id) {
        return ALL.get(DonationSounds.indexOf(id)).label();
    }

    private static SoundEvent event(String id) {
        if (id == null) {
            return null;
        }
        if (id.startsWith("goat_horn_")) {
            try {
                int n = Integer.parseInt(id.substring(10));
                return (SoundEvent)((Holder.Reference)SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(Math.max(0, Math.min(SoundEvents.GOAT_HORN_SOUND_VARIANTS.size() - 1, n)))).value();
            }
            catch (RuntimeException e) {
                return null;
            }
        }
        return switch (id) {
            case "levelup" -> SoundEvents.PLAYER_LEVELUP;
            case "xp" -> SoundEvents.EXPERIENCE_ORB_PICKUP;
            case "bell" -> (SoundEvent)SoundEvents.NOTE_BLOCK_BELL.value();
            case "pling" -> (SoundEvent)SoundEvents.NOTE_BLOCK_PLING.value();
            case "harp" -> (SoundEvent)SoundEvents.NOTE_BLOCK_HARP.value();
            case "chime" -> (SoundEvent)SoundEvents.NOTE_BLOCK_CHIME.value();
            case "firework_launch" -> SoundEvents.FIREWORK_ROCKET_LAUNCH;
            case "firework_blast" -> SoundEvents.FIREWORK_ROCKET_LARGE_BLAST;
            case "firework_twinkle" -> SoundEvents.FIREWORK_ROCKET_TWINKLE;
            case "totem" -> SoundEvents.TOTEM_USE;
            case "thunder" -> SoundEvents.LIGHTNING_BOLT_THUNDER;
            case "wither" -> SoundEvents.WITHER_SPAWN;
            case "dragon_death" -> SoundEvents.ENDER_DRAGON_DEATH;
            case "dragon_growl" -> SoundEvents.ENDER_DRAGON_GROWL;
            case "anvil" -> SoundEvents.ANVIL_LAND;
            case "village_bell" -> SoundEvents.BELL_BLOCK;
            case "challenge" -> SoundEvents.UI_TOAST_CHALLENGE_COMPLETE;
            case "raid_horn" -> (SoundEvent)SoundEvents.RAID_HORN.value();
            default -> null;
        };
    }

    public static void play(String id, float pitch, float volume) {
        SoundEvent event = DonationSounds.event(id);
        if (event == null || volume <= 0.01f) {
            return;
        }
        try {
            Minecraft.getInstance().getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)event, (float)Math.max(0.5f, Math.min(2.0f, pitch)), (float)Math.max(0.0f, Math.min(1.0f, volume))));
        }
        catch (RuntimeException runtimeException) {
            // empty catch block
        }
    }

    public static void stopAll() {
        try {
            SoundManager manager = Minecraft.getInstance().getSoundManager();
            for (Sound s : ALL) {
                SoundEvent e = DonationSounds.event(s.id());
                if (e == null) continue;
                manager.stop(e.location(), null);
            }
        }
        catch (RuntimeException runtimeException) {
            // empty catch block
        }
    }

    static {
        DonationSounds.add("levelup", "Level-Up", "Level up");
        DonationSounds.add("xp", "Erfahrungs-Kugel", "XP orb");
        DonationSounds.add("bell", "Notenblock Glocke", "Note block bell");
        DonationSounds.add("pling", "Notenblock Pling", "Note block pling");
        DonationSounds.add("harp", "Notenblock Harfe", "Note block harp");
        DonationSounds.add("chime", "Notenblock Glockenspiel", "Note block chime");
        DonationSounds.add("firework_launch", "Feuerwerk Start", "Firework launch");
        DonationSounds.add("firework_blast", "Feuerwerk Explosion", "Firework blast");
        DonationSounds.add("totem", "Totem", "Totem");
        DonationSounds.add("thunder", "Donner", "Thunder");
        DonationSounds.add("wither", "Wither-Spawn", "Wither spawn");
        DonationSounds.add("dragon_death", "Enderdrache Tod", "Ender dragon death");
        DonationSounds.add("dragon_growl", "Enderdrache Br\u00fcllen", "Ender dragon growl");
        DonationSounds.add("anvil", "Amboss", "Anvil");
        DonationSounds.add("village_bell", "Glocke (Dorf)", "Village bell");
        DonationSounds.add("challenge", "Erfolg", "Challenge complete");
        DonationSounds.add("raid_horn", "Raid-Horn", "Raid horn");
        for (int i = 0; i < 8; ++i) {
            DonationSounds.add("goat_horn_" + i, "Goat-Horn " + (i + 1), "Goat horn " + (i + 1));
        }
        DonationSounds.add("none", "Kein Ton", "No sound");
    }

    public record Sound(String id, String de, String en) {
        public String label() {
            return Lang.t(this.de, this.en);
        }
    }
}

