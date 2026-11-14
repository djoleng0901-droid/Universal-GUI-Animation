package dev.febri.guianimation;

import net.fabricmc.api.ClientModInitializer;

/**
 * Client entrypoint for Universal GUI Animation.
 *
 * Screen entrance transforms are applied by ScreenMixin so they affect the
 * complete extracted screen, rather than drawing an overlay on top of it.
 */
public final class GuiAnimationClient implements ClientModInitializer {
    public static final String MOD_ID = "universal_gui_animation";

    @Override
    public void onInitializeClient() {
        // All screen entrance animation is handled by the Screen mixin.
    }
}
