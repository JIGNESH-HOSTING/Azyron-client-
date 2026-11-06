package com.azyron.client.mixin;

import com.azyron.client.AzyronClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashOverlay.class)
public class SplashOverlayMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void azyron$loadingBrand(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        String text = AzyronClient.NAME + " " + AzyronClient.VERSION + " — loading…";
        int x = context.getScaledWindowWidth() - mc.textRenderer.getWidth(text) - 4;
        int y = context.getScaledWindowHeight() - 12;
        context.drawText(mc.textRenderer, text, x, y, 0xFFAA66FF, true);
    }
}
