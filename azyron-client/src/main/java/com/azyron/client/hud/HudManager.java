package com.azyron.client.hud;

import com.azyron.client.config.AzyronConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.util.LinkedHashMap;
import java.util.Map;

public final class HudManager {
    public static final int COLOR_ACCENT = 0xFFAA66FF;
    public static final int COLOR_ACCENT2 = 0xFF33CCFF;
    public static final int COLOR_TEXT = 0xFFFFFFFF;
    public static final int COLOR_BG = 0x80000000;

    /** Ordered list of draggable HUD modules: key -> display label. */
    public static final Map<String, String> MODULES = new LinkedHashMap<>();

    static {
        MODULES.put("fps", "FPS Counter");
        MODULES.put("cps", "CPS Counter");
        MODULES.put("ping", "Ping HUD");
        MODULES.put("coords", "Coordinates HUD");
        MODULES.put("keystrokes", "Keystrokes HUD");
        MODULES.put("armor", "Armor HUD");
    }

    private HudManager() {
    }

    public static void register() {
        HudRenderCallback.EVENT.register(HudManager::render);
    }

    /** Resolves an anchor-aware position: negative x counts from the right, negative y from the bottom. */
    public static int[] resolve(String key, int width, int height, int screenW, int screenH) {
        int[] pos = AzyronConfig.get().hudPositions.getOrDefault(key, new int[]{4, 4});
        int x = pos[0] < 0 ? screenW + pos[0] - width : pos[0];
        int y = pos[1] < 0 ? screenH + pos[1] - height : pos[1];
        return new int[]{MathHelper.clamp(x, 0, Math.max(0, screenW - width)), MathHelper.clamp(y, 0, Math.max(0, screenH - height))};
    }

    public static void setPosition(String key, int x, int y) {
        AzyronConfig.get().hudPositions.put(key, new int[]{x, y});
    }

    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        AzyronConfig cfg = AzyronConfig.get();
        if (mc.options.hudHidden) {
            return;
        }
        TextRenderer tr = mc.textRenderer;
        int sw = context.getScaledWindowWidth();
        int sh = context.getScaledWindowHeight();

        if (cfg.showFps) {
            drawTextModule(context, tr, "fps", sw, sh, "FPS: " + mc.getCurrentFps(), 0xFF55FF55);
        }
        if (cfg.showCps) {
            drawTextModule(context, tr, "cps", sw, sh, "CPS: " + CpsTracker.getCps(), COLOR_ACCENT2);
        }
        if (cfg.showPing) {
            drawTextModule(context, tr, "ping", sw, sh, "Ping: " + getPing(mc) + " ms", 0xFFFFAA00);
        }
        if (cfg.showCoords && mc.player != null) {
            String text = String.format("XYZ: %.1f / %.1f / %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
            drawTextModule(context, tr, "coords", sw, sh, text, COLOR_TEXT);
        }
        if (cfg.showKeystrokes) {
            drawKeystrokes(context, mc, sw, sh);
        }
        if (cfg.showArmor && mc.player != null) {
            drawArmor(context, mc, sw, sh);
        }
    }

    private static String getPing(MinecraftClient mc) {
        if (mc.getNetworkHandler() != null && mc.player != null) {
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (entry != null) {
                return String.valueOf(entry.getLatency());
            }
        }
        return "-";
    }

    private static void drawTextModule(DrawContext context, TextRenderer tr, String key, int sw, int sh, String text, int color) {
        int w = tr.getWidth(text);
        int h = tr.fontHeight + 4;
        int[] pos = resolve(key, w, h, sw, sh);
        context.fill(pos[0] - 2, pos[1] - 2, pos[0] + w + 2, pos[1] + h - 2, COLOR_BG);
        context.drawText(tr, text, pos[0], pos[1] + 2, color, true);
    }

    private static void drawKeystrokes(DrawContext context, MinecraftClient mc, int sw, int sh) {
        int size = 12;
        int gap = 2;
        int totalW = size * 3 + gap * 2;
        int totalH = size * 2 + gap;
        int[] pos = resolve("keystrokes", totalW, totalH, sw, sh);
        int x = pos[0];
        int y = pos[1];

        drawKey(context, tr(mc), x + size + gap, y, size, "W", mc.options.forwardKey.isPressed());
        drawKey(context, tr(mc), x, y + size + gap, size, "A", mc.options.leftKey.isPressed());
        drawKey(context, tr(mc), x + size + gap, y + size + gap, size, "S", mc.options.backKey.isPressed());
        drawKey(context, tr(mc), x + (size + gap) * 2, y + size + gap, size, "D", mc.options.rightKey.isPressed());
    }

    /** DrawContext.drawBorder was removed in 1.21.9+, so draw the outline with four fills. */
    public static void drawBorder(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + 1, color);
        context.fill(x, y + h - 1, x + w, y + h, color);
        context.fill(x, y, x + 1, y + h, color);
        context.fill(x + w - 1, y, x + w, y + h, color);
    }

    private static void drawKey(DrawContext context, TextRenderer tr, int x, int y, int size, String label, boolean pressed) {
        int bg = pressed ? 0xCCAA66FF : COLOR_BG;
        int border = pressed ? COLOR_TEXT : COLOR_ACCENT;
        context.fill(x, y, x + size, y + size, bg);
        drawBorder(context, x, y, size, size, border);
        int tx = x + (size - tr.getWidth(label)) / 2;
        int ty = y + (size - tr.fontHeight) / 2 + 1;
        context.drawText(tr, label, tx, ty, pressed ? 0xFF1B1030 : COLOR_TEXT, false);
    }

    private static void drawArmor(DrawContext context, MinecraftClient mc, int sw, int sh) {
        int slotW = 18;
        int[] pos = resolve("armor", slotW * 4, 16, sw, sh);
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (int i = 0; i < slots.length; i++) {
            ItemStack stack = mc.player.getEquippedStack(slots[i]);
            if (!stack.isEmpty()) {
                context.drawItem(stack, pos[0] + i * slotW, pos[1]);
            }
        }
    }

    private static TextRenderer tr(MinecraftClient mc) {
        return mc.textRenderer;
    }
}
