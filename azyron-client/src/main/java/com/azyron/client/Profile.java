package com.azyron.client;

public enum Profile {
    POTATO("Potato"),
    BALANCED("Balanced"),
    SMOOTH("Smooth FPS");

    private final String displayName;

    Profile(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public Profile next() {
        Profile[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
