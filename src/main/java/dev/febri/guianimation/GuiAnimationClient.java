package dev.febri.guianimation;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.Screen;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Client entrypoint for Universal GUI Animation.
 *
 * Screen transitions are attached per Screen instance. Weak references avoid
 * retaining screens after Minecraft discards them, and the listener is only
 * installed once even when a screen is reinitialized after a resize.
 */
public final class GuiAnimationClient implements ClientModInitializer {
    public static final String MOD_ID = "universal_gui_animation";
    private static final long SCREEN_FADE_DURATION_MS = 220L;
    private static final int SCREEN_FADE_MAX_ALPHA = 112;

    private static final Map<Screen, Long> SCREEN_START_TIMES = new WeakHashMap<>();
    private static final Set<Screen> REGISTERED_SCREENS =
            Collections.newSetFromMap(new WeakHashMap<>());

    @Override
    public void onInitializeClient() {
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            SCREEN_START_TIMES.put(screen, nowMillis());

            if (REGISTERED_SCREENS.add(screen)) {
                ScreenEvents.afterExtract(screen).register(
                        (currentScreen, graphics, mouseX, mouseY, tickProgress) ->
                                renderScreenFade(currentScreen, graphics)
                );
            }
        });
    }

    private static long nowMillis() {
        return System.nanoTime() / 1_000_000L;
    }

    private static void renderScreenFade(
            Screen screen,
            net.minecraft.client.gui.GuiGraphicsExtractor graphics
    ) {
        Long startedAt = SCREEN_START_TIMES.get(screen);
        if (startedAt == null) {
            return;
        }

        long elapsed = Math.max(0L, Util.getMillis() - startedAt);
        if (elapsed >= SCREEN_FADE_DURATION_MS) {
            SCREEN_START_TIMES.remove(screen);
            return;
        }

        float linearProgress = (float) elapsed / SCREEN_FADE_DURATION_MS;
        // Ease-out cubic: fast initial reveal, gentle finish.
        float inverse = 1.0F - linearProgress;
        float easedRemaining = inverse * inverse * inverse;
        int alpha = Math.round(SCREEN_FADE_MAX_ALPHA * easedRemaining);

        if (alpha > 0) {
            int argb = (alpha << 24);
            graphics.fill(0, 0, screen.width, screen.height, argb);
        }
    }
}
