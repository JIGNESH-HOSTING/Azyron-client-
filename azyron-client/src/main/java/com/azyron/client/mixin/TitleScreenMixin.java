package com.azyron.client.mixin;

import com.azyron.client.AzyronClient;
import com.azyron.client.screen.AzyronMenuScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void azyron$customBackground(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        context.fillGradient(0, 0, this.width, this.height, 0xFF07070F, 0xFF1B1030);
        ci.cancel();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void azyron$branding(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int cx = this.width / 2;
        int logoY = this.height / 4 - 24;

        context.getMatrices().push();
        context.getMatrices().translate(cx, logoY, 0);
        context.getMatrices().scale(4.0F, 4.0F, 1.0F);
        context.drawText(this.textRenderer, AzyronClient.NAME, -this.textRenderer.getWidth(AzyronClient.NAME) / 2, 0, 0xFFAA66FF, true);
        context.getMatrices().pop();

        context.drawCenteredTextWithShadow(this.textRenderer,
                "CLIENT " + AzyronClient.VERSION + "  —  by " + AzyronClient.DEVELOPER, cx, logoY + 42, 0xFF33CCFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                "Minecraft 1.21.11  •  Fabric  •  Press RIGHT SHIFT in-game for the AZYRON menu",
                cx, logoY + 54, 0xFF8888AA);

        context.drawText(this.textRenderer,
                AzyronClient.NAME + " " + AzyronClient.VERSION, 4, this.height - 12, 0xFF8866CC, true);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void azyron$addMenuButton(CallbackInfo ci) {
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("AZYRON Menu"),
                button -> MinecraftClient.getInstance().setScreen(new AzyronMenuScreen(null))
        ).dimensions(this.width / 2 - 100, this.height - 46, 200, 20).build());
    }
}
