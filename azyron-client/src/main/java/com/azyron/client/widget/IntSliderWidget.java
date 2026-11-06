package com.azyron.client.widget;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class IntSliderWidget extends SliderWidget {
    private final String label;
    private final int min;
    private final int max;
    private final Consumer<Integer> onChange;
    private int value;

    public IntSliderWidget(int x, int y, int width, int height, String label, int min, int max, int initial, Consumer<Integer> onChange) {
        super(x, y, width, height, Text.literal(label), (double) (initial - min) / (double) (max - min));
        this.label = label;
        this.min = min;
        this.max = max;
        this.onChange = onChange;
        this.value = initial;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(Text.literal(label + ": " + value));
    }

    @Override
    protected void applyValue() {
        value = min + (int) Math.round(this.value * (max - min));
        updateMessage();
        onChange.accept(value);
    }
}
