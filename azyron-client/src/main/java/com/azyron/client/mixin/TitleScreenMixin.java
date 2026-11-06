package com.azyron.client.mixin;

import com.azyron.client.AzyronClient;
import com.azyron.client.screen.AzyronMenuScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    @Unique
    private int azyron$buttonY = 0;

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    /** Bottom edge of the lowest vanilla menu button, so AZYRON UI never overlaps it. */
    @Unique
    private int azyron$menuBottom() {
        int bottom = this.height / 4 + 48 + 20;
        for (var element : this.children()) {
            if (element instanceof ClickableWidget widget) {
                bottom = Math.max(bottom, widget.getY() + widget.getHeight());
            }
        }
        return bottom;
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void azyron$addMenuButton(CallbackInfo ci) {
        int y = Math.min(azyron$menuBottom() + 12, this.height - 24);
        azyron$buttonY = y;
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("AZYRON Menu"),
                button -> MinecraftClient.getInstance().setScreen(new AzyronMenuScreen(null))
        ).dimensions(this.width / 2 - 100, y, 200, 20).build());
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void azyron$branding(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int cx = this.width / 2;
        // Three branding lines, centered, stacked above the AZYRON Menu button.
        context.drawCenteredTextWithShadow(this.textRenderer, AzyronClient.NAME, cx, azyron$buttonY - 38, 0xFFAA66FF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                "CLIENT " + AzyronClient.VERSION + "  |  by " + AzyronClient.DEVELOPER, cx, azyron$buttonY - 26, 0xFF33CCFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                "Minecraft Java 1.21.11  |  Fabric", cx, azyron$buttonY - 14, 0xFF8888AA);
    }
}
