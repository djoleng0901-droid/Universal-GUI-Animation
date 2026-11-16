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

@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Unique private long universalGuiAnimation$entranceStartedAt = -1L;
    @Unique private boolean universalGuiAnimation$posePushed;

    @Inject(method = "added", at = @At("TAIL"))
    private void universalGuiAnimation$startEntrance(CallbackInfo ci) {
        this.universalGuiAnimation$entranceStartedAt = System.nanoTime() / 1_000_000L;
    }

    @Unique
    private boolean universalGuiAnimation$isExcludedScreen() {
        String name = ((Screen) (Object) this).getClass().getSimpleName();
        return name.equals("ReceivingLevelScreen")
                || name.equals("LevelLoadingScreen")
                || name.equals("ProgressScreen")
                || name.equals("LoadingOverlay")
                || name.equals("SavingLevelScreen");
    }

    @Unique
    private boolean universalGuiAnimation$isAnimating() {
        if (this.universalGuiAnimation$isExcludedScreen()
                || this.universalGuiAnimation$entranceStartedAt < 0L) return false;
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

    @ModifyVariable(
            method = "extractRenderStateWithTooltipAndSubtitles",
            at = @At("HEAD"), argsOnly = true, ordinal = 0
    )
    private int universalGuiAnimation$hideHoverX(int mouseX) {
        return this.universalGuiAnimation$isAnimating() ? -10_000 : mouseX;
    }

    @ModifyVariable(
            method = "extractRenderStateWithTooltipAndSubtitles",
            at = @At("HEAD"), argsOnly = true, ordinal = 1
    )
    private int universalGuiAnimation$hideHoverY(int mouseY) {
        return this.universalGuiAnimation$isAnimating() ? -10_000 : mouseY;
    }

    @Inject(
            method = "extractRenderStateWithTooltipAndSubtitles",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/Screen;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
                    shift = At.Shift.BEFORE
            ),
            require = 1
    )
    private void universalGuiAnimation$applyEntrance(
            GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci
    ) {
        this.universalGuiAnimation$posePushed = false;
        if (this.universalGuiAnimation$isExcludedScreen()) return;

        long now = System.nanoTime() / 1_000_000L;
        if (this.universalGuiAnimation$entranceStartedAt < 0L) {
            this.universalGuiAnimation$entranceStartedAt = now;
        }
        float progress = this.universalGuiAnimation$progress();
        if (progress >= 1.0F) return;

        float remaining = 1.0F - progress;
        float eased = 1.0F - remaining * remaining * remaining;
        float bounceStrength = AnimationConfig.getBounceStrength();
        float bounceWave = (float) Math.sin(progress * Math.PI * 2.0D)
                * remaining * bounceStrength * 0.55F;
        float movement = Math.max(-0.05F, Math.min(1.08F, eased + bounceWave));
        float squeezeStrength = AnimationConfig.getSqueezeStrength();
        float impact = (float) Math.exp(-Math.pow((progress - 0.76F) / 0.105F, 2.0D));
        float squeeze = impact * squeezeStrength;

        Screen screen = (Screen) (Object) this;
        float centerX = screen.width * 0.5F;
        float centerY = screen.height * 0.5F;
        float pivotY = centerY;
        float offsetX = 0.0F;
        float offsetY = 0.0F;
        float rotation = 0.0F;
        float baseScaleX;
        float baseScaleY;

        String configuredStyle = AnimationConfig.getAnimationStyle();
        String style = configuredStyle.equals("AUTO")
                ? (screen.getClass().getSimpleName().equals("TitleScreen") ? "CLASSIC" : "ZOOM")
                : configuredStyle;

        switch (style) {
            case "ZOOM" -> {
                float zoomBounce = (float) Math.sin(progress * Math.PI)
                        * remaining * bounceStrength * 0.21F;
                float zoomScale = 0.92F + 0.08F * eased + zoomBounce;
                baseScaleX = zoomScale * (1.0F + squeeze * 0.10F);
                baseScaleY = zoomScale * (1.0F - squeeze * 0.08F);
            }
            case "POP" -> {
                float popScale = 0.72F + 0.28F * eased + bounceWave * 0.18F;
                baseScaleX = popScale;
                baseScaleY = popScale;
            }
            case "SLIDE_LEFT" -> {
                offsetX = -(1.0F - movement) * screen.width * 0.72F;
                offsetY = -bounceWave * screen.height * 0.06F;
                baseScaleX = 0.97F + 0.03F * eased;
                baseScaleY = baseScaleX;
            }
            case "SLIDE_RIGHT" -> {
                offsetX = (1.0F - movement) * screen.width * 0.72F;
                offsetY = -bounceWave * screen.height * 0.06F;
                baseScaleX = 0.97F + 0.03F * eased;
                baseScaleY = baseScaleX;
            }
            case "DROP" -> {
                offsetY = -(1.0F - movement) * screen.height * 0.88F;
                baseScaleX = 0.97F + 0.03F * eased;
                baseScaleY = baseScaleX;
            }
            case "TILT" -> {
                rotation = (1.0F - eased) * (float) Math.toRadians(-4.0D)
                        + bounceWave * 0.10F;
                float tiltScale = 0.96F + 0.04F * eased;
                baseScaleX = tiltScale;
                baseScaleY = tiltScale;
            }
            case "FLIP" -> {
                baseScaleX = 0.10F + 0.90F * eased;
                baseScaleY = 0.96F + 0.04F * eased;
                rotation = (1.0F - eased) * (float) Math.toRadians(2.0D);
            }
            case "SWING" -> {
                pivotY = screen.height * 0.10F;
                rotation = (1.0F - eased) * (float) Math.toRadians(-8.0D)
                        + (float) Math.sin(progress * Math.PI * 2.2D)
                        * remaining * (float) Math.toRadians(5.0D);
                offsetY = (1.0F - eased) * screen.height * 0.035F;
                baseScaleX = 0.98F + 0.02F * eased;
                baseScaleY = 0.98F + 0.02F * eased;
            }
            case "ELASTIC" -> {
                float spring = 1.0F - (float) Math.exp(-6.2F * progress)
                        * (float) Math.cos(progress * 10.5F);
                offsetY = (1.0F - spring) * screen.height * 0.95F;
                float elasticScale = 0.96F + 0.04F * eased;
                baseScaleX = elasticScale;
                baseScaleY = elasticScale;
            }
            case "CLASSIC" -> {
                offsetY = (1.0F - movement) * screen.height * 0.95F;
                float baseScale = 0.94F + 0.06F * eased;
                baseScaleX = baseScale * (1.0F + squeeze * 0.28F);
                baseScaleY = baseScale * (1.0F - squeeze * 0.22F);
            }
            default -> {
                baseScaleX = 0.92F + 0.08F * eased;
                baseScaleY = baseScaleX;
            }
        }

        if (!"CLASSIC".equals(style) && !"ZOOM".equals(style)) {
            baseScaleX *= 1.0F + squeeze * 0.28F;
            baseScaleY *= 1.0F - squeeze * 0.22F;
        }

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(centerX + offsetX, pivotY + offsetY);
        if (rotation != 0.0F) pose.rotate(rotation);
        pose.scale(baseScaleX, baseScaleY);
        pose.translate(-centerX, -pivotY);
        this.universalGuiAnimation$posePushed = true;
    }

    @Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("RETURN"), require = 1)
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
