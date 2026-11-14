package dev.febri.guianimation.mixin;

import dev.febri.guianimation.config.AnimationConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Smooth bottom-to-center entrance for GUI screens only. */
@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Unique private long universalGuiAnimation$entranceStartedAt = -1L;
    @Unique private boolean universalGuiAnimation$posePushed;

    @Inject(method = "added", at = @At("TAIL"))
    private void universalGuiAnimation$startEntrance(CallbackInfo ci) {
        this.universalGuiAnimation$entranceStartedAt = System.nanoTime() / 1_000_000L;
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

        long duration = AnimationConfig.getDurationMs();
        long elapsed = Math.max(0L, now - this.universalGuiAnimation$entranceStartedAt);
        if (elapsed >= duration) return;

        float progress = Math.min(1.0F, (float) elapsed / (float) duration);
        float remaining = 1.0F - progress;
        float eased = 1.0F - remaining * remaining * remaining;

        float bounceStrength = AnimationConfig.getBounceStrength();
        float bounceWave = (float) Math.sin(progress * Math.PI * 2.2D)
                * remaining * remaining * bounceStrength * 1.5F;
        float movement = eased + bounceWave;

        float squeezeStrength = AnimationConfig.getSqueezeStrength();
        float impact = (float) Math.exp(-Math.pow((progress - 0.82F) / 0.13F, 2.0D));
        float stretch = (float) Math.sin(progress * Math.PI) * squeezeStrength * 0.15F;
        float baseScale = 0.94F + 0.06F * eased;
        float scaleX = baseScale * (1.0F + impact * squeezeStrength * 0.35F - stretch * 0.25F);
        float scaleY = baseScale * (1.0F - impact * squeezeStrength * 0.25F + stretch);

        Screen screen = (Screen) (Object) this;
        float centerX = screen.width * 0.5F;
        float centerY = screen.height * 0.5F;
        float verticalOffset = (1.0F - movement) * screen.height * 0.20F;

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
}
