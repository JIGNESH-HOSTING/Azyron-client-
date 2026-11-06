package com.azyron.client.mixin;

import com.azyron.client.hud.CpsTracker;
import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    // 1.21.11 signature: private void onMouseButton(long window, MouseInput input, int action)
    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void azyron$countClick(long window, MouseInput input, int action, CallbackInfo ci) {
        if (action == 1 && input.button() == 0) {
            CpsTracker.recordClick();
        }
    }
}
