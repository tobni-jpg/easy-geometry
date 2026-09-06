package com.example.easygeometry;

import com.example.easygeometry.config.EasyConfig;
import com.example.easygeometry.render.ShapeOverlayRenderer;
import com.example.easygeometry.shape.Geometry;
import com.example.easygeometry.ui.GeometryScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

/**
 * Client entrypoint. Registers the overlay keybind, the world-render hook and
 * holds the current mod state (shape, radius, overlay enabled).
 */
public class EasyGeometryClient implements ClientModInitializer {

    private static EasyGeometryClient INSTANCE;

    private final KeyMapping openScreen = new KeyMapping(
            "key.easygeometry.open_screen",
            InputConstants.KEY_G,
            KeyMapping.Category.MISC);

    private Geometry.ShapeType shapeType = Geometry.ShapeType.SPHERE;
    private int radius = EasyConfig.DEFAULT_RADIUS;
    private boolean overlayEnabled = false;

    public static EasyGeometryClient getInstance() {
        return INSTANCE;
    }

    @Override
    public void onInitializeClient() {
        INSTANCE = this;

        // load persisted config
        EasyConfig cfg = EasyConfig.load();
        this.shapeType = cfg.shapeType;
        this.radius = cfg.radius;
        this.overlayEnabled = false; // preview starts off until user enables it

        // register keybind to open the settings screen
        KeyMappingHelper.registerKeyMapping(openScreen);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openScreen.consumeClick()) {
                client.setScreenAndShow(new GeometryScreen(null));
            }
        });

        // draw the ghost overlay in the GIZMOS phase of the level renderer
        LevelRenderEvents.BEFORE_GIZMOS.register(ctx -> ShapeOverlayRenderer.render());
    }

    public Geometry.ShapeType getShapeType() {
        return shapeType;
    }

    public void setShapeType(Geometry.ShapeType shapeType) {
        this.shapeType = shapeType;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public boolean isOverlayEnabled() {
        return overlayEnabled;
    }

    public void setOverlayEnabled(boolean overlayEnabled) {
        this.overlayEnabled = overlayEnabled;
    }
}
