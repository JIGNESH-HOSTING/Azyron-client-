package com.azyron.client.screen;

import com.azyron.client.config.AzyronConfig;
import com.azyron.client.widget.IntSliderWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.particle.ParticlesMode;
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
                o.getViewDistance().getValue(), v -> o.getViewDistance().setValue(v)));
        y += 24;
        addDrawableChild(new IntSliderWidget(cx, y, w, 20, "Simulation Distance", 2, 32,
                o.getSimulationDistance().getValue(), v -> o.getSimulationDistance().setValue(v)));
        y += 24;
        addDrawableChild(new IntSliderWidget(cx, y, w, 20, "Max FPS", 30, 260,
                o.getMaxFps().getValue(), v -> o.getMaxFps().setValue(v)));
        y += 24;
        addDrawableChild(new IntSliderWidget(cx, y, w, 20, "Entity Distance %", 25, 100,
                (int) (o.getEntityDistanceScaling().getValue() * 100), v -> o.getEntityDistanceScaling().setValue(v / 100.0)));
        y += 24;
        addDrawableChild(new IntSliderWidget(cx, y, w, 20, "Mipmap Levels", 0, 4,
                o.getMipmapLevels().getValue(), v -> o.getMipmapLevels().setValue(v)));
        y += 28;

        int half = 108;
        addDrawableChild(ButtonWidget.builder(Text.literal("Clouds: " + o.getCloudRenderModeValue().name()),
                b -> {
                    o.getCloudRenderMode().setValue(switch (o.getCloudRenderModeValue()) {
                        case OFF -> CloudRenderMode.FAST;
                        case FAST -> CloudRenderMode.FANCY;
                        case FANCY -> CloudRenderMode.OFF;
                    });
                    clearAndInit();
                }).dimensions(cx, y, half, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Particles: " + o.getParticles().getValue().name()),
                b -> {
                    o.getParticles().setValue(switch (o.getParticles().getValue()) {
                        case ALL -> ParticlesMode.MINIMAL;
                        case MINIMAL -> ParticlesMode.DECREASED;
                        case DECREASED -> ParticlesMode.ALL;
                    });
                    clearAndInit();
                }).dimensions(cx + half + 4, y, half, 20).build());
        y += 24;
        addDrawableChild(ButtonWidget.builder(Text.literal("Smooth Lighting: " + (o.getAo().getValue() ? "ON" : "OFF")),
                b -> {
                    o.getAo().setValue(!o.getAo().getValue());
                    clearAndInit();
                }).dimensions(cx, y, half, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("VSync: " + (o.getEnableVsync().getValue() ? "ON" : "OFF")),
                b -> {
                    o.getEnableVsync().setValue(!o.getEnableVsync().getValue());
                    clearAndInit();
                }).dimensions(cx + half + 4, y, half, 20).build());
        y += 24;
        addDrawableChild(ButtonWidget.builder(Text.literal("Entity Shadows: " + (o.getEntityShadows().getValue() ? "ON" : "OFF")),
                b -> {
                    o.getEntityShadows().setValue(!o.getEntityShadows().getValue());
                    clearAndInit();
                }).dimensions(cx, y, half, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Biome Blend: " + o.getBiomeBlendRadius().getValue()),
                b -> {
                    int v = o.getBiomeBlendRadius().getValue();
                    o.getBiomeBlendRadius().setValue(v >= 7 ? 0 : v + 1);
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
