package com.example.easygeometry.config;

import com.example.easygeometry.shape.Geometry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Persistent user settings: last chosen shape and radius, stored as JSON
 * in the Fabric config directory.
 */
public final class EasyConfig {

    public static final int MIN_RADIUS = 1;
    public static final int MAX_RADIUS = 64;
    public static final int DEFAULT_RADIUS = 8;

    public Geometry.ShapeType shapeType = Geometry.ShapeType.SPHERE;
    public int radius = DEFAULT_RADIUS;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("easy-geometry.json");
    }

    public static EasyConfig load() {
        Path path = configPath();
        if (Files.exists(path)) {
            try {
                EasyConfig cfg = GSON.fromJson(Files.readString(path), EasyConfig.class);
                if (cfg != null) {
                    if (cfg.shapeType == null) {
                        cfg.shapeType = Geometry.ShapeType.SPHERE;
                    }
                    cfg.radius = Math.max(MIN_RADIUS, Math.min(MAX_RADIUS, cfg.radius));
                    return cfg;
                }
            } catch (IOException e) {
                // fall through to defaults
            }
        }
        return new EasyConfig();
    }

    public void save() {
        try {
            Files.writeString(configPath(), GSON.toJson(this));
        } catch (IOException ignored) {
        }
    }
}
