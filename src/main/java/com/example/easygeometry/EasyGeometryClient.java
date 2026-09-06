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
import net.minecraft.core.BlockPos;

/**
 * Client entrypoint. Registers the overlay keybind, the world-render hook and
 * holds the current mod state (shape, radius, overlay enabled) plus the fixed
 * anchor position the shape is drawn around.
 */
public class EasyGeometryClient implements ClientModInitializer {

    private static EasyGeometryClient INSTANCE;

    private final KeyMapping openScreen = new KeyMapping(
            "key.easygeometry.open_screen",
            InputConstants.KEY_G,
            KeyMapping.Category.MISC);

    private Geometry.ShapeType shapeType = Geometry.ShapeType.SPHERE;
    private int radius = EasyConfig.DEFAULT_RADIUS;
    private int height = EasyConfig.DEFAULT_HEIGHT;
    private boolean overlayEnabled = false;
    private BlockPos anchor = null;

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
        this.height = cfg.height;
        this.overlayEnabled = false; // preview starts off until user enables it

        // register keybind to open the settings screen
        KeyMappingHelper.registerKeyMapping(openScreen);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openScreen.consumeClick()) {
                client.setScreenAndShow(new GeometryScreen(null));
            }
        });

        // draw the ghost overlay in the GIZMOS phase of the level renderer
        LevelRenderEvents.BEFORE_GIZMOS.register(ctx -> ShapeOverlayRenderer.render(ctx));
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

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isOverlayEnabled() {
        return overlayEnabled;
    }

    public void setOverlayEnabled(boolean overlayEnabled) {
        this.overlayEnabled = overlayEnabled;
    }

    /** The fixed position the shape is drawn around (set once when preview is enabled). */
    public BlockPos getAnchor() {
        return anchor;
    }

    /** Snaps the anchor to the player's current position. */
    public void setAnchorToPlayer() {
        Minecraft mc = Minecraft.getInstance();
        this.anchor = (mc.player != null) ? mc.player.blockPosition() : null;
    }
}
