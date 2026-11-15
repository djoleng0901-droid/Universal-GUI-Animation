package dev.febri.guianimation.mixin;

import dev.febri.guianimation.config.AnimationConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies a restrained bottom-to-center entrance to screens.
 * Mouse input is briefly held until the visual transform has settled so
 * clickable widgets never receive clicks at mismatched visual coordinates.
 */
@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Unique private long universalGuiAnimation$entranceStartedAt = -1L;
    @Unique private boolean universalGuiAnimation$posePushed;

    @Inject(method = "added", at = @At("TAIL"))
    private void universalGuiAnimation$startEntrance(CallbackInfo ci) {
        this.universalGuiAnimation$entranceStartedAt = System.nanoTime() / 1_000_000L;
    }

    @Unique
    private boolean universalGuiAnimation$isAnimating() {
        if (this.universalGuiAnimation$entranceStartedAt < 0L) return false;
        long elapsed = Math.max(0L,
                System.nanoTime() / 1_000_000L - this.universalGuiAnimation$entranceStartedAt);
        return elapsed < AnimationConfig.getDurationMs();
    }

    @Unique
    private float universalGuiAnimation$progress() {
        if (this.universalGuiAnimation$entranceStartedAt < 0L) return 1.0F;
        long elapsed = Math.max(0L,
                System.nanoTime() / 1_000_000L - this.universalGuiAnimation$entranceStartedAt);
        return Math.min(1.0F, (float) elapsed / (float) AnimationConfig.getDurationMs());
    }

    @ModifyVariable(method = "extractRenderState", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int universalGuiAnimation$hideHoverX(int mouseX) {
        return this.universalGuiAnimation$isAnimating() ? -10_000 : mouseX;
    }

    @ModifyVariable(method = "extractRenderState", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int universalGuiAnimation$hideHoverY(int mouseY) {
        return this.universalGuiAnimation$isAnimating() ? -10_000 : mouseY;
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void universalGuiAnimation$applyEntrance(
            GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci
    ) {
        this.universalGuiAnimation$posePushed = false;
        long now = System.nanoTime() / 1_000_000L;
        if (this.universalGuiAnimation$entranceStartedAt < 0L) {
            this.universalGuiAnimation$entranceStartedAt = now;
        }

        float progress = this.universalGuiAnimation$progress();
        if (progress >= 1.0F) return;

        float remaining = 1.0F - progress;
        float eased = 1.0F - remaining * remaining * remaining;

        // A damped, endpoint-safe wave adds a gentle spring-like settle.
        float bounceStrength = AnimationConfig.getBounceStrength();
        float bounceWave = (float) Math.sin(progress * Math.PI * 2.0D)
                * remaining * bounceStrength * 0.55F;
        float movement = Math.max(-0.05F, Math.min(1.08F, eased + bounceWave));

        // A short squash/stretch pulse is synchronized with the landing.
        float squeezeStrength = AnimationConfig.getSqueezeStrength();
        float impact = (float) Math.exp(-Math.pow((progress - 0.76F) / 0.105F, 2.0D));
        float baseScale = 0.94F + 0.06F * eased;
        float squeeze = impact * squeezeStrength;
        float scaleX = baseScale * (1.0F + squeeze * 0.28F);
        float scaleY = baseScale * (1.0F - squeeze * 0.22F);

        Screen screen = (Screen) (Object) this;
        float centerX = screen.width * 0.5F;
        float centerY = screen.height * 0.5F;

        // At the first frame the entire screen begins below the visible area.
        float verticalOffset = (1.0F - movement) * screen.height * 0.95F;

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(centerX, centerY + verticalOffset);
        pose.scale(scaleX, scaleY);
        pose.translate(-centerX, -centerY);
        this.universalGuiAnimation$posePushed = true;
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void universalGuiAnimation$restorePose(
            GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci
    ) {
        if (this.universalGuiAnimation$posePushed) {
            graphics.pose().popMatrix();
            this.universalGuiAnimation$posePushed = false;
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void universalGuiAnimation$holdClickDuringEntrance(CallbackInfoReturnable<Boolean> cir) {
        if (this.universalGuiAnimation$isAnimating()) cir.setReturnValue(false);
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void universalGuiAnimation$holdReleaseDuringEntrance(CallbackInfoReturnable<Boolean> cir) {
        if (this.universalGuiAnimation$isAnimating()) cir.setReturnValue(false);
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void universalGuiAnimation$holdDragDuringEntrance(CallbackInfoReturnable<Boolean> cir) {
        if (this.universalGuiAnimation$isAnimating()) cir.setReturnValue(false);
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void universalGuiAnimation$holdScrollDuringEntrance(CallbackInfoReturnable<Boolean> cir) {
        if (this.universalGuiAnimation$isAnimating()) cir.setReturnValue(false);
    }
}
