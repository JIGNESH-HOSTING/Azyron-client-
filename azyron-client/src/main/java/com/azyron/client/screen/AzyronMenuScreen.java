package com.azyron.client.screen;

import com.azyron.client.AzyronClient;
import com.azyron.client.VideoSettings;
import com.azyron.client.config.AzyronConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class AzyronMenuScreen extends AzyronScreen {
    private final Screen parent;

    public AzyronMenuScreen(Screen parent) {
        super(Text.literal(AzyronClient.NAME + " " + AzyronClient.VERSION));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2 - 110;
        int w = 220;
        int h = 20;
        int y = 44;
        int gap = 24;

        addDrawableChild(ButtonWidget.builder(Text.literal("FPS Boost Settings"),
                b -> client.setScreen(new FpsBoostScreen(this))).dimensions(cx, y, w, h).build());
        y += gap;
        addDrawableChild(ButtonWidget.builder(Text.literal("HUD Editor"),
                b -> client.setScreen(new HudEditorScreen(this))).dimensions(cx, y, w, h).build());
        y += gap;
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Profile: " + AzyronConfig.get().profile.displayName()),
                b -> {
                    AzyronConfig cfg = AzyronConfig.get();
                    VideoSettings.applyProfile(cfg.profile.next(), client);
                    clearAndInit();
                }).dimensions(cx, y, w, h).build());
        y += gap;
        addDrawableChild(ButtonWidget.builder(Text.literal("One-Click Optimize"),
                b -> {
                    VideoSettings.oneClickOptimize(client);
                    clearAndInit();
                }).dimensions(cx, y, w, h).build());
        y += gap;
        addDrawableChild(ButtonWidget.builder(Text.literal("Mods"),
                b -> client.setScreen(new ModsScreen(this))).dimensions(cx, y, w, h).build());
        y += gap;
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"),
                b -> client.setScreen(parent)).dimensions(cx, y, w, h).build());
    }
}
