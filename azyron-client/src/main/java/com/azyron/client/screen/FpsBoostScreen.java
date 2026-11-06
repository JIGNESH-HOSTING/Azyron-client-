package com.azyron.client.screen;

import com.azyron.client.config.AzyronConfig;
import com.azyron.client.widget.IntSliderWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;

public class FpsBoostScreen extends AzyronScreen {
    private final Screen parent;

    public FpsBoostScreen(Screen parent) {
        super(Text.literal("FPS Boost Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        GameOptions o = client.options;
        int cx = this.width / 2 - 110;
        int w = 220;
        int y = 40;

        addDrawableChild(new IntSliderWidget(cx, y, w, 20, "Render Distance", 2, 32,
                o.renderDistance.getValue(), v -> o.renderDistance.setValue(v)));
        y += 24;
        addDrawableChild(new IntSliderWidget(cx, y, w, 20, "Simulation Distance", 2, 32,
                o.simulationDistance.getValue(), v -> o.simulationDistance.setValue(v)));
        y += 24;
        addDrawableChild(new IntSliderWidget(cx, y, w, 20, "Max FPS", 30, 260,
                o.framerateLimit.getValue(), v -> o.framerateLimit.setValue(v)));
        y += 24;
        addDrawableChild(new IntSliderWidget(cx, y, w, 20, "Entity Distance %", 25, 100,
                (int) (o.entityDistanceScaling.getValue() * 100), v -> o.entityDistanceScaling.setValue(v / 100.0)));
        y += 24;
        addDrawableChild(new IntSliderWidget(cx, y, w, 20, "Mipmap Levels", 0, 4,
                o.mipmapLevels.getValue(), v -> o.mipmapLevels.setValue(v)));
        y += 28;

        int half = 108;
        addDrawableChild(ButtonWidget.builder(Text.literal("Clouds: " + o.cloudRenderMode.getValue().getName()),
                b -> {
                    o.cloudRenderMode.setValue(switch (o.cloudRenderMode.getValue()) {
                        case OFF -> net.minecraft.client.option.CloudRenderMode.FAST;
                        case FAST -> net.minecraft.client.option.CloudRenderMode.FANCY;
                        case FANCY -> net.minecraft.client.option.CloudRenderMode.OFF;
                    });
                    clearAndInit();
                }).dimensions(cx, y, half, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Particles: " + o.particles.getValue()),
                b -> {
                    o.particles.setValue(switch (o.particles.getValue()) {
                        case ALL -> net.minecraft.client.option.ParticlesMode.MINIMAL;
                        case MINIMAL -> net.minecraft.client.option.ParticlesMode.DECREASED;
                        case DECREASED -> net.minecraft.client.option.ParticlesMode.ALL;
                    });
                    clearAndInit();
                }).dimensions(cx + half + 4, y, half, 20).build());
        y += 24;
        addDrawableChild(ButtonWidget.builder(Text.literal("Smooth Lighting: " + (o.smoothLighting.getValue() ? "ON" : "OFF")),
                b -> {
                    o.smoothLighting.setValue(!o.smoothLighting.getValue());
                    clearAndInit();
                }).dimensions(cx, y, half, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("VSync: " + (o.useVsync.getValue() ? "ON" : "OFF")),
                b -> {
                    o.useVsync.setValue(!o.useVsync.getValue());
                    clearAndInit();
                }).dimensions(cx + half + 4, y, half, 20).build());
        y += 24;
        addDrawableChild(ButtonWidget.builder(Text.literal("Entity Shadows: " + (o.entityShadows.getValue() ? "ON" : "OFF")),
                b -> {
                    o.entityShadows.setValue(!o.entityShadows.getValue());
                    clearAndInit();
                }).dimensions(cx, y, half, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Biome Blend: " + o.biomeBlendRadius.getValue()),
                b -> {
                    int v = o.biomeBlendRadius.getValue();
                    o.biomeBlendRadius.setValue(v >= 7 ? 0 : v + 1);
                    clearAndInit();
                }).dimensions(cx + half + 4, y, half, 20).build());
        y += 30;

        addDrawableChild(ButtonWidget.builder(Text.literal("Save & Apply"),
                b -> {
                    client.options.write();
                    AzyronConfig.save();
                    client.setScreen(parent);
                }).dimensions(cx, y, half, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"),
                b -> client.setScreen(parent)).dimensions(cx + half + 4, y, half, 20).build());
    }
}
