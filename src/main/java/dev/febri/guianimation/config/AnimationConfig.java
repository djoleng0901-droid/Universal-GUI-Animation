package dev.febri.guianimation.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/** Persistent client-side animation settings. */
public final class AnimationConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir()
            .resolve("universal-gui-animation.json");
    private static final String[] ANIMATION_STYLES = {
            "CLASSIC", "POP", "SLIDE_LEFT", "SLIDE_RIGHT", "DROP", "TILT",
            "FLIP", "SWING", "ELASTIC"
    };
    private static AnimationConfig current = new AnimationConfig();

    public float squeezeStrength = 0.12F;
    public float bounceStrength = 0.12F;
    public int durationMs = 480;
    /** CLASSIC preserves the original bottom-to-center entrance. */
    public String animationStyle = "CLASSIC";

    private AnimationConfig() {}

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            AnimationConfig loaded = GSON.fromJson(reader, AnimationConfig.class);
            if (loaded != null) current = loaded;
        } catch (IOException | RuntimeException exception) {
            System.err.println("[Universal GUI Animation] Could not load config: " + exception.getMessage());
        }
        current.squeezeStrength = clamp(current.squeezeStrength, 0.0F, 0.30F);
        current.bounceStrength = clamp(current.bounceStrength, 0.0F, 0.30F);
        current.durationMs = Math.max(250, Math.min(1000, current.durationMs));
        current.animationStyle = normalizeStyle(current.animationStyle);
    }

    public static float getSqueezeStrength() { return current.squeezeStrength; }
    public static float getBounceStrength() { return current.bounceStrength; }
    public static int getDurationMs() { return current.durationMs; }
    public static String getAnimationStyle() { return normalizeStyle(current.animationStyle); }

    public static String getAnimationStyleTranslationKey() {
        return "screen.universal_gui_animation.style." + getAnimationStyle().toLowerCase(java.util.Locale.ROOT);
    }

    public static void cycleAnimationStyle(int direction) {
        String style = getAnimationStyle();
        int index = 0;
        for (int i = 0; i < ANIMATION_STYLES.length; i++) {
            if (ANIMATION_STYLES[i].equals(style)) {
                index = i;
                break;
            }
        }
        int next = Math.floorMod(index + direction, ANIMATION_STYLES.length);
        current.animationStyle = ANIMATION_STYLES[next];
        save();
    }

    public static void adjustSqueeze(float amount) {
        current.squeezeStrength = clamp(current.squeezeStrength + amount, 0.0F, 0.30F);
        save();
    }
    public static void adjustBounce(float amount) {
        current.bounceStrength = clamp(current.bounceStrength + amount, 0.0F, 0.30F);
        save();
    }
    public static void adjustDuration(int amount) {
        current.durationMs = Math.max(250, Math.min(1000, current.durationMs + amount));
        save();
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(current, writer);
            }
        } catch (IOException exception) {
            System.err.println("[Universal GUI Animation] Could not save config: " + exception.getMessage());
        }
    }

    private static String normalizeStyle(String style) {
        if (style == null) return "CLASSIC";
        for (String candidate : ANIMATION_STYLES) {
            if (candidate.equalsIgnoreCase(style)) return candidate;
        }
        return "CLASSIC";
    }

    private static float clamp(float value, float min, float max) {
        if (!Float.isFinite(value)) return min;
        return Math.max(min, Math.min(max, value));
    }
}
