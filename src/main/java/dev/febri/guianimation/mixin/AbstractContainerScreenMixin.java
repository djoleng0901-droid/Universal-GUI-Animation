package dev.febri.guianimation.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds a short spring-like scale animation to the item attached to the cursor
 * when a player picks an item up from a container slot.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
    @Shadow
    @Final
    protected AbstractContainerMenu menu;

    @Unique
    private static final long universalGuiAnimation$BOUNCE_DURATION_MS = 280L;

    @Unique
    private ItemStack universalGuiAnimation$lastCarried = ItemStack.EMPTY;

    @Unique
    private long universalGuiAnimation$bounceStartedAt = -1L;

    @Unique
    private boolean universalGuiAnimation$posePushed;

    @Inject(method = "extractCarriedItem", at = @At("HEAD"))
    private void universalGuiAnimation$beginCarriedItemBounce(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            CallbackInfo ci
    ) {
        ItemStack carried = this.menu.getCarried();

        // Ignore count-only changes (for example, splitting a stack while dragging).
        if (!carried.isEmpty()
                && !ItemStack.isSameItemSameComponents(carried, this.universalGuiAnimation$lastCarried)) {
            this.universalGuiAnimation$bounceStartedAt = System.nanoTime() / 1_000_000L;
        }

        this.universalGuiAnimation$lastCarried = carried.copy();
        this.universalGuiAnimation$posePushed = false;

        if (carried.isEmpty() || this.universalGuiAnimation$bounceStartedAt < 0L) {
            return;
        }

        long elapsed = Math.max(0L,
                System.nanoTime() / 1_000_000L - this.universalGuiAnimation$bounceStartedAt);
        if (elapsed >= universalGuiAnimation$BOUNCE_DURATION_MS) {
            return;
        }

        float progress = (float) elapsed / universalGuiAnimation$BOUNCE_DURATION_MS;
        float bounce = (float) Math.sin(progress * Math.PI * 3.0D) * (1.0F - progress);
        float scale = 1.0F + 0.24F * bounce;

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(mouseX, mouseY);
        pose.scale(scale, scale);
        pose.translate(-mouseX, -mouseY);
        this.universalGuiAnimation$posePushed = true;
    }

    @Inject(method = "extractCarriedItem", at = @At("RETURN"))
    private void universalGuiAnimation$endCarriedItemBounce(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            CallbackInfo ci
    ) {
        if (this.universalGuiAnimation$posePushed) {
            graphics.pose().popMatrix();
            this.universalGuiAnimation$posePushed = false;
        }
    }
}
