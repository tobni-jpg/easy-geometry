package com.example.easygeometry.render;

import com.example.easygeometry.EasyGeometryClient;
import com.example.easygeometry.gizmo.GizmoHelper;
import com.example.easygeometry.shape.Geometry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Draws the currently configured shape around the player as translucent ghost
 * gizmos. Invoked from LevelRenderEvents.BEFORE_GIZMOS.
 */
public final class ShapeOverlayRenderer {

    private ShapeOverlayRenderer() {
    }

    public static void render() {
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

        // Draw into the per-frame render-thread gizmo collector. This sets the
        // ThreadLocal collector that Gizmos.* helpers write to, so the shapes
        // actually appear at their world coords. Without this wrapper the helpers
        // write into no collector and nothing is visible.
        try (net.minecraft.gizmos.Gizmos.TemporaryCollection ignored =
                     mc.levelRenderer.collectPerFrameRenderThreadGizmos()) {
            switch (type) {
                case SPHERE -> renderSphere(center, radius);
                case CYLINDER, CONE -> renderCuboidShell(center, type, radius);
                case TORUS -> renderTorus(center, radius);
            }
        }
    }

    private static void renderCuboidShell(BlockPos center, Geometry.ShapeType type, int radius) {
        // Cap the block count so huge radii don't flood the gizmo collector.
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
        // a small center marker so the midpoint is visible as requested
        GizmoHelper.circle(new Vec3(center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5),
                radius, GizmoHelper.COLOR_GREEN, 0.03f);
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
                        GizmoHelper.COLOR_GREEN, 0.03f);
            }
            prevX = x;
            prevZ = z;
            first = false;
        }
    }
}
