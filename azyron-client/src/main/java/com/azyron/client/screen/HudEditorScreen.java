package com.azyron.client.screen;

import com.azyron.client.config.AzyronConfig;
import com.azyron.client.hud.HudManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;

public class HudEditorScreen extends AzyronScreen {
    private final Screen parent;
    private final Map<String, int[]> bounds = new HashMap<>();
    private String dragging = null;

    public HudEditorScreen(Screen parent) {
        super(Text.literal("HUD Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int y = this.height - 24;
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"),
                b -> {
                    AzyronConfig.save();
                    client.setScreen(parent);
                }).dimensions(this.width / 2 - 100, y, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer,
                "Drag the HUD modules to move them — changes save automatically", this.width / 2, 30, ACCENT2);

        AzyronConfig cfg = AzyronConfig.get();
        int sw = context.getScaledWindowWidth();
        int sh = context.getScaledWindowHeight();
        bounds.clear();
        for (Map.Entry<String, String> module : HudManager.MODULES.entrySet()) {
            String key = module.getKey();
            boolean enabled = isEnabled(cfg, key);
            int w = Math.max(60, this.textRenderer.getWidth(module.getValue()) + 8);
            int h = 14;
            int[] pos = HudManager.resolve(key, w, h, sw, sh);
            bounds.put(key, new int[]{pos[0], pos[1], w, h});
            int color = key.equals(dragging) ? 0xFF33CCFF : (enabled ? ACCENT : 0xFF555566);
            context.fill(pos[0], pos[1], pos[0] + w, pos[1] + h, enabled ? 0x80000000 : 0x40000000);
            context.drawBorder(pos[0], pos[1], w, h, color);
            context.drawText(this.textRenderer, module.getValue() + (enabled ? "" : " (off)"),
                    pos[0] + 4, pos[1] + 3, enabled ? 0xFFFFFFFF : 0xFF999999, false);
        }
    }

    private static boolean isEnabled(AzyronConfig cfg, String key) {
        return switch (key) {
            case "fps" -> cfg.showFps;
            case "cps" -> cfg.showCps;
            case "ping" -> cfg.showPing;
            case "coords" -> cfg.showCoords;
            case "keystrokes" -> cfg.showKeystrokes;
            case "armor" -> cfg.showArmor;
            default -> false;
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (Map.Entry<String, int[]> e : bounds.entrySet()) {
                int[] b = e.getValue();
                if (mouseX >= b[0] && mouseX <= b[0] + b[2] && mouseY >= b[1] && mouseY <= b[1] + b[3]) {
                    dragging = e.getKey();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging != null && button == 0) {
            int[] b = bounds.get(dragging);
            if (b != null) {
                HudManager.setPosition(dragging, (int) mouseX - b[2] / 2, (int) mouseY - b[3] / 2);
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging != null) {
            dragging = null;
            AzyronConfig.save();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void removed() {
        AzyronConfig.save();
    }
}
