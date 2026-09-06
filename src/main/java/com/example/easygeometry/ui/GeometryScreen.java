package com.example.easygeometry.ui;

import com.example.easygeometry.EasyGeometryClient;
import com.example.easygeometry.config.EasyConfig;
import com.example.easygeometry.shape.Geometry;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Semi-modern "Sodium-style" settings screen: dark panel, shape selector,
 * a radius slider and an overlay toggle, built on MC 26.2's extract-render API.
 */
public class GeometryScreen extends Screen {

    private final Screen parent;
    private EasyConfig config;
    private AbstractSliderButton radiusSlider;
    private boolean overlayEnabled;
    private Geometry.ShapeType shapeType;

    public GeometryScreen(Screen parent) {
        super(Component.literal("Easy Geometry"));
        this.parent = parent;
        this.config = EasyConfig.load();
        this.overlayEnabled = EasyGeometryClient.getInstance().isOverlayEnabled();
        this.shapeType = this.config.shapeType;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int panelW = Math.min(320, this.width - 40);
        int left = centerX - panelW / 2;
        int y = 40;

        // Shape selection: a plain button that cycles through the shapes
        Button shapeButton = Button.builder(
                        Component.literal("Form: " + shapeType.getDisplayName()),
                        btn -> {
                            Geometry.ShapeType[] values = Geometry.ShapeType.values();
                            int next = (shapeType.ordinal() + 1) % values.length;
                            shapeType = values[next];
                            btn.setMessage(Component.literal("Form: " + shapeType.getDisplayName()));
                        })
                .bounds(left, y, panelW, 20).build();

        // Radius slider wrapped as AbstractSliderButton (0..1 mapped to MIN..MAX)
        this.radiusSlider = new AbstractSliderButton(left, y + 30, panelW, 20,
                Component.literal("Radius: " + config.radius),
                toSliderValue(config.radius)) {
            @Override
            protected void updateMessage() {
                int r = toRadius(this.value);
                setMessage(Component.literal("Radius: " + r));
            }

            @Override
            protected void applyValue() {
                config.radius = toRadius(this.value);
            }
        };

        // Overlay toggle button
        Button toggleButton = Button.builder(overlayComponent(), btn -> {
            overlayEnabled = !overlayEnabled;
            btn.setMessage(overlayComponent());
        }).bounds(left, y + 60, panelW, 20).build();

        // Show preview button
        Button showButton = Button.builder(Component.literal("Show Preview"),
                        btn -> finish(true))
                .bounds(left, y + 90, panelW, 20).build();
        Button closeButton = Button.builder(Component.literal("Close"),
                        btn -> finish(false))
                .bounds(left, y + 115, panelW, 20).build();

        this.addRenderableWidget(shapeButton);
        this.addRenderableWidget(this.radiusSlider);
        this.addRenderableWidget(toggleButton);
        this.addRenderableWidget(showButton);
        this.addRenderableWidget(closeButton);
    }

    private Component overlayComponent() {
        return Component.literal("Preview: " + (overlayEnabled ? "Enabled" : "Disabled"));
    }

    private static int toRadius(double sliderValue) {
        double v = Math.max(0, Math.min(1, sliderValue));
        return (int) Math.round(EasyConfig.MIN_RADIUS
                + v * (EasyConfig.MAX_RADIUS - EasyConfig.MIN_RADIUS));
    }

    private static double toSliderValue(int radius) {
        int span = EasyConfig.MAX_RADIUS - EasyConfig.MIN_RADIUS;
        return (double) (radius - EasyConfig.MIN_RADIUS) / span;
    }

    private void finish(boolean showPreview) {
        if (showPreview) {
            overlayEnabled = true;
        }
        // persist + push state
        config.shapeType = shapeType;
        this.config.save();

        EasyGeometryClient state = EasyGeometryClient.getInstance();
        state.setShapeType(shapeType);
        state.setRadius(config.radius);
        state.setOverlayEnabled(overlayEnabled);

        // snap the ghost to the player's current position when enabling the preview
        if (overlayEnabled) {
            state.setAnchorToPlayer();
        }

        this.minecraft.setScreenAndShow(this.parent);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTick) {
        // draw a full-screen dark translucent backdrop instead of the vanilla blur image
        super.extractTransparentBackground(gui);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTick) {
        // dark translucent backdrop + panel
        int panelW = Math.min(320, this.width - 40);
        int centerX = this.width / 2;
        gui.fill(0, 0, this.width, this.height, 0xC8000000);
        gui.fill(centerX - panelW / 2 - 8, 28, centerX + panelW / 2 + 8, 150, 0xD0101010);

        // widgets
        super.extractRenderState(gui, mouseX, mouseY, partialTick);

        // title
        Font font = this.minecraft.font;
        gui.centeredText(font, "Easy Geometry", centerX, 14, 0xFFFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}