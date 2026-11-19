/*
 * Decompiled with CFR 0.152.
 */
package net.friendswithmoney.fwm.ui.widget;

import java.util.HashMap;
import java.util.Map;

public final class Anim {
    private static final Map<String, State> VALUES = new HashMap<String, State>();

    private Anim() {
    }

    public static float toward(String key, boolean target, int millis) {
        long now = System.currentTimeMillis();
        float goal = target ? 1.0f : 0.0f;
        State state = VALUES.get(key);
        if (state == null) {
            VALUES.put(key, new State(goal, now));
            return goal;
        }
        float step = (float)(now - state.at()) / (float)Math.max(1, millis);
        float value = state.value();
        if (value < goal) {
            value = Math.min(goal, value + step);
        } else if (value > goal) {
            value = Math.max(goal, value - step);
        }
        VALUES.put(key, new State(value, now));
        return value;
    }

    public static float ease(float t) {
        float inv = 1.0f - Math.max(0.0f, Math.min(1.0f, t));
        return 1.0f - inv * inv * inv;
    }

    private record State(float value, long at) {
    }
}

