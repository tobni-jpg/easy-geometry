package com.example.easygeometry.render;

import com.example.easygeometry.EasyGeometryClient;
import com.example.easygeometry.gizmo.GizmoHelper;
import com.example.easygeometry.shape.Geometry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.SimpleGizmoCollector;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

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

        // Build all gizmos into a local collector, then hand them to the renderer
        // in one shot. This avoids ThreadLocal collector issues entirely.
        SimpleGizmoCollector collector = new SimpleGizmoCollector();

        // temporarily swap Gizmos' ThreadLocal to our collector so the static
        // helper methods (Gizmos.cuboid/line/circle) write into it
        net.minecraft.gizmos.Gizmos.TemporaryCollection tmp =
                net.minecraft.gizmos.Gizmos.withCollector(collector);
        try {
            switch (type) {
                case SPHERE -> renderSphere(center, radius);
                case CYLINDER, CONE -> renderCuboidShell(center, type, radius);
                case TORUS -> renderTorus(center, radius);
            }
        } finally {
            tmp.close(); // resets ThreadLocal, our gizmos are now in collector
        }

        // hand the finished gizmos to the LevelRenderer
        List<net.minecraft.gizmos.SimpleGizmoCollector.GizmoInstance> gizmos = collector.drainGizmos();
        if (!gizmos.isEmpty()) {
            ctx.levelRenderer().addMainThreadGizmos(gizmos);
        }
    }

    private static void renderCuboidShell(BlockPos center, Geometry.ShapeType type, int radius) {
        List<BlockPos.MutableBlockPos> positions = Geometry.shellPositions(type, center, radius);
        int limit = 60000;
        int n = Math.min(positions.size(), limit);
        for (int i = 0; i < n; i++) {
            GizmoHelper.block(positions.get(i));
        }
    }

    private static void renderSphere(BlockPos center, int radius) {
        AABB box = new AABB(
                new Vec3(center.getX() - radius, center.getY() - radius, center.getZ() - radius),
                new Vec3(center.getX() + radius, center.getY() + radius, center.getZ() + radius));
        GizmoHelper.cuboid(box);
    }

    private static void renderTorus(BlockPos center, int radius) {
        int segments = Math.max(24, radius * 4);
        double prevX = 0, prevZ = 0;
        boolean first = true;
        Vec3 c = new Vec3(center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5);
        for (int i = 0; i <= segments; i++) {
            double a = (Math.PI * 2 * i) / segments;
            double x = radius * Math.cos(a);
            double z = radius * Math.sin(a);
            if (!first) {
                GizmoHelper.line(c.add(prevX, 0, prevZ), c.add(x, 0, z),
                        GizmoHelper.FILL_GREEN, 0.03f);
            }
            prevX = x;
            prevZ = z;
            first = false;
        }
    }
}
