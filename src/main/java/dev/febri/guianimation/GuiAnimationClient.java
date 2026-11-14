package dev.febri.guianimation;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client entrypoint for Universal GUI Animation.
 *
 * <p>The initial foundation intentionally does not install global rendering
 * hooks. Animation systems will be added in isolated, testable stages.</p>
 */
public final class GuiAnimationClient implements ClientModInitializer {
    public static final String MOD_ID = "universal_gui_animation";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Universal GUI Animation initialized. Vanilla+ mode is ready for development.");
    }
}
