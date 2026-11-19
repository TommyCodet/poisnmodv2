/*
 * Decompiled with CFR 0.152.
 */
package net.friendswithmoney.fwm.stream.fx;

import net.friendswithmoney.fwm.stream.DonationSounds;
import net.friendswithmoney.fwm.stream.DonationTier;
import net.friendswithmoney.fwm.stream.fx.FxAnim;
import net.friendswithmoney.fwm.stream.fx.FxCanvas;
import net.friendswithmoney.fwm.stream.fx.Play;

public final class FxPlayer {
    public final Play play = new Play();
    private long start;
    private long startNanos;
    private float lastT = -1.0f;
    private boolean running;
    public int loopPause = -1;
    private final float[] shake = new float[2];
    private long seed = 1L;

    public void load(String donor, long amount, long total, int count, DonationTier tier, FxAnim anim) {
        Play p = this.play;
        p.donor = donor == null || donor.isEmpty() ? "?" : donor;
        p.amount = amount;
        p.total = total;
        p.count = Math.max(1, count);
        p.reseed(this.seed++);
        p.prepare(anim, tier);
    }

    public void start(long now) {
        this.start = now;
        this.startNanos = System.nanoTime();
        this.lastT = -1.0f;
        this.running = true;
    }

    public void stop() {
        this.running = false;
    }

    public boolean running() {
        return this.running;
    }

    private float elapsed(long now) {
        float period;
        float e = now - this.start;
        float fine = (float)(System.nanoTime() - this.startNanos) / 1000000.0f;
        if (Math.abs(fine - e) < 40.0f) {
            e = fine;
        }
        if (this.loopPause >= 0 && (e %= (period = (float)(this.play.durMs + this.loopPause))) > (float)this.play.durMs) {
            e = this.play.durMs;
        }
        return e;
    }

    public boolean update(long now) {
        if (!this.running) {
            return false;
        }
        if (this.loopPause < 0 && now - this.start >= (long)this.play.durMs) {
            this.running = false;
            return false;
        }
        return true;
    }

    public void sounds(long now) {
        float impact;
        if (!this.running || !this.play.sound || this.play.anim == null) {
            return;
        }
        float t = this.play.animTime(this.elapsed(now));
        DonationTier tier = this.play.tier;
        if (this.lastT < 0.0f) {
            DonationSounds.play(tier.sound, tier.pitch, tier.volume);
            this.lastT = 0.0f;
        }
        if (t < this.lastT) {
            this.lastT = -0.001f;
        }
        if ((impact = this.play.anim.impactMs()) > this.lastT && impact <= t) {
            DonationSounds.play(tier.sound2, tier.pitch, tier.volume);
        }
        this.play.anim.sounds(this.play, this.lastT, t);
        this.lastT = t;
    }

    public void draw(FxCanvas c, long now) {
        if (!this.running || this.play.anim == null) {
            return;
        }
        float t = this.play.animTime(this.elapsed(now));
        boolean moved = false;
        if (c.shake) {
            this.play.anim.shake(this.play, t, this.shake);
            if (this.shake[0] != 0.0f || this.shake[1] != 0.0f) {
                c.push();
                c.translate(this.shake[0], this.shake[1]);
                moved = true;
            }
        }
        this.play.anim.render(c, this.play, t);
        if (moved) {
            c.pop();
        }
    }

    public void drawAt(FxCanvas c, float t) {
        if (this.play.anim == null) {
            return;
        }
        this.play.anim.render(c, this.play, t);
    }
}

