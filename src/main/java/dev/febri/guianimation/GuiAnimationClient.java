package dev.febri.guianimation;

import com.mojang.blaze3d.platform.InputConstants;
import dev.febri.guianimation.config.AnimationConfig;
import dev.febri.guianimation.config.AnimationConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

/** Client entrypoint for Universal GUI Animation. */
public final class GuiAnimationClient implements ClientModInitializer {
    public static final String MOD_ID = "universal_gui_animation";
    private static KeyMapping openConfigKey;

    @Override
    public void onInitializeClient() {
        AnimationConfig.load();
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(MOD_ID, "main"));
        openConfigKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.universal_gui_animation.open_config",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F8,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openConfigKey.consumeClick()) {
                client.gui.setScreen(new AnimationConfigScreen(client.gui.screen()));
            }
        });
    }
}
