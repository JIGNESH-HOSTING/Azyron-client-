package com.azyron.client.hud;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class CpsTracker {
    private static final Queue<Long> CLICKS = new ConcurrentLinkedQueue<>();

    private CpsTracker() {
    }

    public static void recordClick() {
        CLICKS.add(System.currentTimeMillis());
    }

    /** Clicks per second over the trailing 1000 ms window. */
    public static int getCps() {
        long now = System.currentTimeMillis();
        CLICKS.removeIf(t -> now - t > 1000L);
        return CLICKS.size();
    }
}
