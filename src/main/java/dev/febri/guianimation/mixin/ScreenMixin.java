package dev.febri.guianimation.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Gives every screen a short, subtle entrance from below into its normal
 * centered position. The small ease-out overshoot creates a restrained bounce.
 */
@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Unique
    private static final long universalGuiAnimation$ENTRANCE_DURATION_MS = 360L;

    @Unique
    private static final float universalGuiAnimation$START_OFFSET_RATIO = 0.12F;

    @Unique
    private long universalGuiAnimation$entranceStartedAt = -1L;

    @Unique
    private boolean universalGuiAnimation$posePushed;

    @Inject(method = "added", at = @At("TAIL"))
    private void universalGuiAnimation$startEntrance(CallbackInfo ci) {
        this.universalGuiAnimation$entranceStartedAt = System.nanoTime() / 1_000_000L;
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void universalGuiAnimation$applyEntrance(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick,
            CallbackInfo ci
    ) {
        this.universalGuiAnimation$posePushed = false;

        long now = System.nanoTime() / 1_000_000L;
        if (this.universalGuiAnimation$entranceStartedAt < 0L) {
            this.universalGuiAnimation$entranceStartedAt = now;
        }

        long elapsed = Math.max(0L, now - this.universalGuiAnimation$entranceStartedAt);
        if (elapsed >= universalGuiAnimation$ENTRANCE_DURATION_MS) {
            return;
        }

        float progress = (float) elapsed / universalGuiAnimation$ENTRANCE_DURATION_MS;
        // Ease-out-back with a deliberately small overshoot.
        float inverse = progress - 1.0F;
        float eased = 1.0F
                + 2.2F * inverse * inverse * inverse
                + 1.2F * inverse * inverse;

        Screen screen = (Screen) (Object) this;
        float centerX = screen.width * 0.5F;
        float centerY = screen.height * 0.5F;
        float offsetY = (1.0F - eased) * screen.height
                * universalGuiAnimation$START_OFFSET_RATIO;
        float scale = 0.985F + 0.015F * Math.min(1.0F, eased);

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(centerX, centerY + offsetY);
        pose.scale(scale, scale);
        pose.translate(-centerX, -centerY);
        this.universalGuiAnimation$posePushed = true;
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void universalGuiAnimation$restorePose(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick,
            CallbackInfo ci
    ) {
        if (this.universalGuiAnimation$posePushed) {
            graphics.pose().popMatrix();
            this.universalGuiAnimation$posePushed = false;
        }
    }
}
