package com.example.easygeometry.gizmo;

import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Thin wrapper around MC 26.2's native {@code Gizmos} helper API.
 * Static calls here render into the current frame's gizmo collector, which is
 * exactly what we want when invoked from LevelRenderEvents.BEFORE_GIZMOS.
 */
public final class GizmoHelper {

    public static final int COLOR_GREEN = 0xD000FF00;
    public static final int COLOR_RED = 0xD0FF3333;

    /** Fill color for ghost blocks (translucent green, low alpha so terrain shows through). */
    public static final int FILL_GREEN = 0x3D00FF00;
    public static final int FILL_RED = 0x3DFF3333;

    private GizmoHelper() {
    }

    /** Draws a single block-sized solid translucent ghost block at the given position. */
    public static void block(BlockPos pos) {
        Gizmos.cuboid(pos, GizmoStyle.fill(FILL_GREEN));
    }

    /** Draws an axis-aligned bounding box as a ghost block volume. */
    public static void cuboid(AABB box) {
        Gizmos.cuboid(box, GizmoStyle.fill(FILL_GREEN));
    }

    /** Draws an axis-aligned box with a custom style. */
    public static void cuboid(AABB box, int color, float width) {
        Gizmos.cuboid(box, GizmoStyle.strokeAndFill(color, width, color));
    }

    /** Draws a line segment. */
    public static void line(Vec3 start, Vec3 end, int color, float width) {
        Gizmos.line(start, end, color, width);
    }

    /** Draws a horizontal circle of the given radius at a position. */
    public static void circle(Vec3 center, float radius, int color, float width) {
        Gizmos.circle(center, radius, GizmoStyle.stroke(color, width));
    }
}
