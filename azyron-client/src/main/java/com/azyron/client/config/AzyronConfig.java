package com.azyron.client.config;

import com.azyron.client.Profile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class AzyronConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("azyron-client.json");

    private static AzyronConfig instance;

    public boolean settingsApplied = false;
    public Profile profile = Profile.POTATO;

    public boolean showFps = true;
    public boolean showCps = true;
    public boolean showPing = true;
    public boolean showCoords = true;
    public boolean showKeystrokes = true;
    public boolean showArmor = true;

    /** module key -> [x, y]; negative x anchors from the right edge, negative y from the bottom edge */
    public Map<String, int[]> hudPositions = defaultPositions();

    public static AzyronConfig get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static Map<String, int[]> defaultPositions() {
        Map<String, int[]> m = new HashMap<>();
        m.put("fps", new int[]{4, 4});
        m.put("cps", new int[]{4, 16});
        m.put("ping", new int[]{-4, 4});
        m.put("coords", new int[]{4, 28});
        m.put("keystrokes", new int[]{4, -58});
        m.put("armor", new int[]{4, -36});
        return m;
    }

    public static void load() {
        if (Files.exists(FILE)) {
            try (Reader reader = Files.newBufferedReader(FILE)) {
                instance = GSON.fromJson(reader, AzyronConfig.class);
            } catch (Exception e) {
                System.err.println("[AZYRON] Failed to read config, using defaults: " + e.getMessage());
            }
        }
        if (instance == null) {
            instance = new AzyronConfig();
        }
        if (instance.hudPositions == null) {
            instance.hudPositions = defaultPositions();
        }
        for (Map.Entry<String, int[]> entry : defaultPositions().entrySet()) {
            instance.hudPositions.putIfAbsent(entry.getKey(), entry.getValue());
        }
        if (instance.profile == null) {
            instance.profile = Profile.POTATO;
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE)) {
                GSON.toJson(get(), writer);
            }
        } catch (IOException e) {
            System.err.println("[AZYRON] Failed to save config: " + e.getMessage());
        }
    }
}
