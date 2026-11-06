package com.azyron.client;

import com.azyron.client.config.AzyronConfig;
import com.azyron.client.hud.HudManager;
import com.azyron.client.screen.AzyronMenuScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class AzyronClient implements ClientModInitializer {
    public static final String MOD_ID = "azyron-client";
    public static final String NAME = "AZYRON CLIENT";
    public static final String VERSION = "1.0";
    public static final String DEVELOPER = "JIGNESH GAMING";

    private static KeyBinding openMenuKey;

    @Override
    public void onInitializeClient() {
        AzyronConfig.load();
        HudManager.register();

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.azyron.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.azyron.client"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new AzyronMenuScreen(null));
                } else {
                    client.setScreen(null);
                }
            }
        });

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            if (!AzyronConfig.get().settingsApplied) {
                VideoSettings.applyProfile(AzyronConfig.get().profile, client);
                AzyronConfig.get().settingsApplied = true;
                AzyronConfig.save();
            }
        });
    }
}
