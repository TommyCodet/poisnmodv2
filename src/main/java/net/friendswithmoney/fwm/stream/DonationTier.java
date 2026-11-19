/*
 * Decompiled with CFR 0.152.
 */
package net.friendswithmoney.fwm.stream;

import java.util.ArrayList;
import java.util.List;

public final class DonationTier {
    public static final int MINT = -11870565;
    public static final int GOLD = -670398;
    public static final int PURPLE = -4952848;
    public static final int RED = -42406;
    public static final int BLUE = -11884289;
    public static final int[] COLORS = new int[]{-11870565, -670398, -4952848, -42406, -11884289};
    public String name = "Donation";
    public long from;
    public long to = -1L;
    public String anim = "card";
    public String sound = "levelup";
    public float pitch = 1.0f;
    public float volume = 1.0f;
    public String sound2 = "none";
    public int seconds;
    public int color = -11870565;
    public String text = DonationTier.defaultText();
    public String item = "minecraft:diamond";
    public boolean on = true;
    public boolean animSounds = true;
    public long testAmount;
    private static final long M = 100000000L;

    public boolean matches(long cents) {
        return this.on && cents >= this.from && (this.to < 0L || cents < this.to);
    }

    public boolean overlaps(DonationTier o) {
        long aEnd = this.to < 0L ? Long.MAX_VALUE : this.to;
        long bEnd = o.to < 0L ? Long.MAX_VALUE : o.to;
        return this.from < bEnd && o.from < aEnd;
    }

    public long sampleAmount() {
        if (this.testAmount > 0L) {
            return this.testAmount;
        }
        if (this.to < 0L) {
            return Math.max(this.from, 100L) * 5L / 2L;
        }
        return Math.max(100L, this.from + (this.to - this.from) / 4L);
    }

    public DonationTier copy() {
        DonationTier t = new DonationTier();
        t.name = this.name;
        t.from = this.from;
        t.to = this.to;
        t.anim = this.anim;
        t.sound = this.sound;
        t.pitch = this.pitch;
        t.volume = this.volume;
        t.sound2 = this.sound2;
        t.seconds = this.seconds;
        t.color = this.color;
        t.text = this.text;
        t.item = this.item;
        t.on = this.on;
        t.animSounds = this.animSounds;
        t.testAmount = this.testAmount;
        return t;
    }

    void repair() {
        if (this.name == null) {
            this.name = "?";
        }
        if (this.anim == null) {
            this.anim = "card";
        }
        if (this.sound == null) {
            this.sound = "none";
        }
        if (this.sound2 == null) {
            this.sound2 = "none";
        }
        if (this.text == null) {
            this.text = DonationTier.defaultText();
        }
        if (this.item == null) {
            this.item = "minecraft:diamond";
        }
        if (this.from < 0L) {
            this.from = 0L;
        }
        if (!(this.pitch > 0.4f) || !(this.pitch <= 2.01f)) {
            this.pitch = 1.0f;
        }
        if (!(this.volume >= 0.0f) || !(this.volume <= 1.01f)) {
            this.volume = 1.0f;
        }
        this.seconds = Math.max(0, Math.min(20, this.seconds));
        if (this.color >>> 24 == 0) {
            this.color |= 0xFF000000;
        }
    }

    public static String defaultText() {
        return "{name} donated {amount}!";
    }

    public static List<DonationTier> defaults() {
        ArrayList<DonationTier> out = new ArrayList<DonationTier>();
        DonationTier small = new DonationTier();
        small.name = "Small Donation";
        small.from = 0L;
        small.to = 100000000L;
        small.anim = "chat";
        small.sound = "xp";
        small.color = -11870565;
        small.text = "+{amount} from {name}";
        small.item = "minecraft:gold_ingot";
        out.add(small);
        DonationTier normal = new DonationTier();
        normal.name = "Donation";
        normal.from = 100000000L;
        normal.to = 1000000000L;
        normal.anim = "card";
        normal.sound = "levelup";
        normal.pitch = 1.15f;
        normal.color = -11870565;
        normal.item = "minecraft:diamond";
        out.add(normal);
        DonationTier big = new DonationTier();
        big.name = "Big Donation";
        big.from = 1000000000L;
        big.to = 10000000000L;
        big.anim = "goldrain";
        big.sound = "firework_launch";
        big.color = -670398;
        big.item = "minecraft:gold_ingot";
        out.add(big);
        DonationTier crazy = new DonationTier();
        crazy.name = "Crazy Donation";
        crazy.from = 10000000000L;
        crazy.to = -1L;
        crazy.anim = "dragon";
        crazy.sound = "dragon_death";
        crazy.volume = 0.8f;
        crazy.sound2 = "dragon_growl";
        crazy.color = -4952848;
        crazy.item = "minecraft:dragon_head";
        out.add(crazy);
        return out;
    }
}

