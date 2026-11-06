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
                o.getGraphicsMode().setValue(GraphicsMode.FAST);
                o.getViewDistance().setValue(2);
                o.getSimulationDistance().setValue(2);
                o.getCloudRenderMode().setValue(CloudRenderMode.OFF);
                o.getParticles().setValue(ParticlesMode.MINIMAL);
                o.getAo().setValue(false);
                o.getEntityDistanceScaling().setValue(0.5);
                o.getMipmapLevels().setValue(0);
                o.getEnableVsync().setValue(false);
                o.getMaxFps().setValue(60);
                o.getBiomeBlendRadius().setValue(0);
                o.getEntityShadows().setValue(false);
            }
            case BALANCED -> {
                o.getGraphicsMode().setValue(GraphicsMode.FAST);
                o.getViewDistance().setValue(6);
                o.getSimulationDistance().setValue(6);
                o.getCloudRenderMode().setValue(CloudRenderMode.OFF);
                o.getParticles().setValue(ParticlesMode.MINIMAL);
                o.getAo().setValue(false);
                o.getEntityDistanceScaling().setValue(0.75);
                o.getMipmapLevels().setValue(2);
                o.getEnableVsync().setValue(false);
                o.getMaxFps().setValue(120);
                o.getBiomeBlendRadius().setValue(1);
                o.getEntityShadows().setValue(false);
            }
            case SMOOTH -> {
                o.getGraphicsMode().setValue(GraphicsMode.FAST);
                o.getViewDistance().setValue(10);
                o.getSimulationDistance().setValue(10);
                o.getCloudRenderMode().setValue(CloudRenderMode.FAST);
                o.getParticles().setValue(ParticlesMode.ALL);
                o.getAo().setValue(true);
                o.getEntityDistanceScaling().setValue(1.0);
                o.getMipmapLevels().setValue(4);
                o.getEnableVsync().setValue(false);
                o.getMaxFps().setValue(144);
                o.getBiomeBlendRadius().setValue(2);
                o.getEntityShadows().setValue(true);
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
