package com.azyron.client;

import com.azyron.client.config.AzyronConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.ParticlesMode;

public final class VideoSettings {

    private VideoSettings() {
    }

    /** Applies one of the three AZYRON performance profiles to the vanilla video settings. */
    public static void applyProfile(Profile profile, MinecraftClient mc) {
        GameOptions o = mc.options;
        switch (profile) {
            case POTATO -> {
                o.graphicsMode.setValue(GraphicsMode.FAST);
                o.renderDistance.setValue(2);
                o.simulationDistance.setValue(2);
                o.cloudRenderMode.setValue(CloudRenderMode.OFF);
                o.particles.setValue(ParticlesMode.MINIMAL);
                o.smoothLighting.setValue(false);
                o.entityDistanceScaling.setValue(0.5);
                o.mipmapLevels.setValue(0);
                o.useVsync.setValue(false);
                o.framerateLimit.setValue(60);
                o.biomeBlendRadius.setValue(0);
                o.entityShadows.setValue(false);
            }
            case BALANCED -> {
                o.graphicsMode.setValue(GraphicsMode.FAST);
                o.renderDistance.setValue(6);
                o.simulationDistance.setValue(6);
                o.cloudRenderMode.setValue(CloudRenderMode.OFF);
                o.particles.setValue(ParticlesMode.MINIMAL);
                o.smoothLighting.setValue(false);
                o.entityDistanceScaling.setValue(0.75);
                o.mipmapLevels.setValue(2);
                o.useVsync.setValue(false);
                o.framerateLimit.setValue(120);
                o.biomeBlendRadius.setValue(1);
                o.entityShadows.setValue(false);
            }
            case SMOOTH -> {
                o.graphicsMode.setValue(GraphicsMode.FAST);
                o.renderDistance.setValue(10);
                o.simulationDistance.setValue(10);
                o.cloudRenderMode.setValue(CloudRenderMode.FAST);
                o.particles.setValue(ParticlesMode.ALL);
                o.smoothLighting.setValue(true);
                o.entityDistanceScaling.setValue(1.0);
                o.mipmapLevels.setValue(4);
                o.useVsync.setValue(false);
                o.framerateLimit.setValue(144);
                o.biomeBlendRadius.setValue(2);
                o.entityShadows.setValue(true);
            }
        }
        AzyronConfig.get().profile = profile;
        AzyronConfig.save();
        o.write();
    }

    /** One-click optimization: instantly switches to the lowest-latency Potato profile. */
    public static void oneClickOptimize(MinecraftClient mc) {
        applyProfile(Profile.POTATO, mc);
    }
}
