package com.example.easygeometry.render;

import com.example.easygeometry.EasyGeometryClient;
import com.example.easygeometry.gizmo.GizmoHelper;
import com.example.easygeometry.shape.Geometry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.SimpleGizmoCollector;

import java.util.List;

/**
 * Draws the currently configured shape around the player as translucent ghost
 * blocks. Invoked from LevelRenderEvents.BEFORE_GIZMOS via the LevelRenderer's
 * addMainThreadGizmos path — no ThreadLocal collector juggling required.
 */
public final class ShapeOverlayRenderer {

    private ShapeOverlayRenderer() {
    }

    /**
     * Called from BEFORE_GIZMOS. Builds a SimpleGizmoCollector filled with
     * the shape's ghost blocks and feeds it to the renderer.
     */
    public static void render(LevelRenderContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        EasyGeometryClient state = EasyGeometryClient.getInstance();
        if (!state.isOverlayEnabled()) {
            return;
        }

        // use the fixed anchor if set, otherwise fall back to the player position
        BlockPos center = state.getAnchor() != null ? state.getAnchor() : mc.player.blockPosition();
        Geometry.ShapeType type = state.getShapeType();
        int radius = state.getRadius();
        int height = state.getHeight();

        // Build all gizmos into a local collector, then hand them to the renderer
        // in one shot. This avoids ThreadLocal collector issues entirely.
        SimpleGizmoCollector collector = new SimpleGizmoCollector();

        // temporarily swap Gizmos' ThreadLocal to our collector so the static
        // helper methods (Gizmos.cuboid/line/circle) write into it
        net.minecraft.gizmos.Gizmos.TemporaryCollection tmp =
                net.minecraft.gizmos.Gizmos.withCollector(collector);
        try {
            renderShell(center, type, radius, height);
        } finally {
            tmp.close(); // resets ThreadLocal, our gizmos are now in collector
        }

        // hand the finished gizmos to the LevelRenderer
        List<net.minecraft.gizmos.SimpleGizmoCollector.GizmoInstance> gizmos = collector.drainGizmos();
        if (!gizmos.isEmpty()) {
            ctx.levelRenderer().addMainThreadGizmos(gizmos);
        }
    }

    /** Renders every shape as a shell of filled translucent ghost blocks. */
    private static void renderShell(BlockPos center, Geometry.ShapeType type, int radius, int height) {
        List<BlockPos.MutableBlockPos> positions = Geometry.shellPositions(type, center, radius, height);
        int limit = 60000;
        int n = Math.min(positions.size(), limit);
        for (int i = 0; i < n; i++) {
            GizmoHelper.block(positions.get(i));
        }
    }
}
