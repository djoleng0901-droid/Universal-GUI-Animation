package dev.febri.guianimation.config;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** In-game settings screen for entrance animation controls. */
public final class AnimationConfigScreen extends Screen {
    private final Screen parent;

    public AnimationConfigScreen(Screen parent) {
        super(Component.translatable("screen.universal_gui_animation.config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int center = this.width / 2;
        int squeezeY = this.height / 2 - 42;
        int durationY = this.height / 2 - 2;
        int bounceY = this.height / 2 + 38;

        this.addRenderableWidget(Button.builder(Component.literal("-"), button ->
                AnimationConfig.adjustSqueeze(-0.02F))
                .bounds(center - 118, squeezeY, 24, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), button ->
                AnimationConfig.adjustSqueeze(0.02F))
                .bounds(center + 94, squeezeY, 24, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("-"), button ->
                AnimationConfig.adjustDuration(-50))
                .bounds(center - 118, durationY, 24, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), button ->
                AnimationConfig.adjustDuration(50))
                .bounds(center + 94, durationY, 24, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("-"), button ->
                AnimationConfig.adjustBounce(-0.02F))
                .bounds(center - 118, bounceY, 24, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), button ->
                AnimationConfig.adjustBounce(0.02F))
                .bounds(center + 94, bounceY, 24, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.onClose())
                .bounds(center - 100, this.height - 34, 200, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int center = this.width / 2;
        graphics.centeredText(this.font, this.title, center, this.height / 2 - 92, 0xFFFFFFFF);
        graphics.centeredText(this.font,
                Component.translatable("screen.universal_gui_animation.squeeze",
                        String.format(java.util.Locale.ROOT, "%.2f", AnimationConfig.getSqueezeStrength())),
                center, this.height / 2 - 36, 0xFFFFFFFF);
        graphics.centeredText(this.font,
                Component.translatable("screen.universal_gui_animation.duration", AnimationConfig.getDurationMs()),
                center, this.height / 2 + 4, 0xFFFFFFFF);
        graphics.centeredText(this.font,
                Component.translatable("screen.universal_gui_animation.bounce",
                        String.format(java.util.Locale.ROOT, "%.2f", AnimationConfig.getBounceStrength())),
                center, this.height / 2 + 44, 0xFFFFFFFF);
        graphics.centeredText(this.font, Component.translatable("screen.universal_gui_animation.hint"),
                center, this.height / 2 + 72, 0xFFB0B0B0);
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }
}
