package io.github.m4ssivee.mcpvpcrystal.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.m4ssivee.mcpvpcrystal.Log;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Tiny JSON config, stored as config/marlows-crystal-optimizer-for-mcpvp.json.
 * <ul>
 *   <li>{@code enabled}: master switch for the whole mod.</li>
 *   <li>{@code fastPlace}: also place crystals every tick while right click is held (Walksy-style).
 *       Off by default because it sends more packets than vanilla click speed allows.</li>
 * </ul>
 */
public final class ModConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private boolean enabled = true;

    private boolean fastPlace = false;

    public static ModConfig load(Path file) {
        ModConfig config = new ModConfig();
        if (Files.isRegularFile(file)) {
            try (Reader reader = Files.newBufferedReader(file)) {
                ModConfig read = GSON.fromJson(reader, ModConfig.class);
                if (read != null) {
                    config = read;
                }
            } catch (IOException | RuntimeException e) {
                Log.warn("Could not read " + file.getFileName() + ", using defaults: " + e.getMessage());
            }
        }

        // Rewritten on every start so new options show up in the file.
        config.save(file);
        return config;
    }

    private void save(Path file) {
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            Log.warn("Could not write " + file.getFileName() + ": " + e.getMessage());
        }
    }

    public boolean enabled() {
        return enabled;
    }

    public boolean fastPlace() {
        return fastPlace;
    }
}
