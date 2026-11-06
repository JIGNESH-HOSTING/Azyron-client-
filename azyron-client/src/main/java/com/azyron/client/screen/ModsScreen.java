package com.azyron.client.screen;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.Comparator;
import java.util.List;

public class ModsScreen extends AzyronScreen {
    private final Screen parent;
    private List<ModContainer> mods;

    public ModsScreen(Screen parent) {
        super(Text.literal("Installed Mods"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        mods = FabricLoader.getInstance().getAllMods().stream()
                .sorted(Comparator.comparing(m -> m.getMetadata().getName().toLowerCase()))
                .toList();
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"),
                b -> client.setScreen(parent)).dimensions(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int y = 40;
        int maxLines = (this.height - 70) / 12;
        for (int i = 0; i < mods.size() && i < maxLines; i++) {
            var meta = mods.get(i).getMetadata();
            String line = meta.getName() + "  v" + meta.getVersion().getFriendlyString();
            int color = meta.getId().equals("azyron-client") ? ACCENT : 0xFFCCCCDD;
            context.drawText(this.textRenderer, line, this.width / 2 - 110, y, color, false);
            y += 12;
        }
        context.drawCenteredTextWithShadow(this.textRenderer, mods.size() + " mods loaded", this.width / 2, this.height - 42, ACCENT2);
    }
}
