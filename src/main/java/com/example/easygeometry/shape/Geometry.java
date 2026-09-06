package com.example.easygeometry.shape;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Computes the list of block positions for each supported geometric shape,
 * centered on the given center block position.
 */
public final class Geometry {

    private Geometry() {
    }

    public enum ShapeType {
        SPHERE("Sphere"),
        CYLINDER("Cylinder"),
        CONE("Cone"),
        TORUS("Torus");

        private final String displayName;

        ShapeType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Returns all block positions that make up the "shell" (outer surface) of
     * the given shape, centered at {@code center}. The returned positions are
     * unique. {@code height} is used for CYLINDER and CONE (sphere and torus
     * ignore it).
     */
    public static List<BlockPos.MutableBlockPos> shellPositions(
            ShapeType type, BlockPos center, int radius, int height) {

        List<BlockPos.MutableBlockPos> out = new ArrayList<>();
        int r = Math.max(1, radius);
        int h = Math.max(1, height);

        switch (type) {
            case SPHERE -> sphere(out, center, r);
            case CYLINDER -> cylinder(out, center, r, h);
            case CONE -> cone(out, center, r, h);
            case TORUS -> torus(out, center, r);
        }
        return out;
    }

    private static void sphere(List<BlockPos.MutableBlockPos> out, BlockPos c, int r) {
        int r2 = r * r;
        int inner = Math.max(0, (r - 1) * (r - 1));
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    int d = dx * dx + dy * dy + dz * dz;
                    if (d <= r2 && d > inner) {
                        out.add(new BlockPos.MutableBlockPos(c.getX() + dx, c.getY() + dy, c.getZ() + dz));
                    }
                }
            }
        }
    }

    private static void cylinder(List<BlockPos.MutableBlockPos> out, BlockPos c, int r, int h) {
        int r2 = r * r;
        int inner = Math.max(0, (r - 1) * (r - 1));
        for (int dy = 0; dy < h; dy++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    int d = dx * dx + dz * dz;
                    if (d <= r2 && d > inner) {
                        out.add(new BlockPos.MutableBlockPos(c.getX() + dx, c.getY() + dy, c.getZ() + dz));
                    }
                }
            }
        }
    }

    private static void cone(List<BlockPos.MutableBlockPos> out, BlockPos c, int r, int h) {
        for (int dy = 0; dy < h; dy++) {
            double t = 1.0 - (double) dy / Math.max(1, h - 1);
            double rr = r * t;
            int rrInt = (int) Math.round(rr);
            if (rrInt < 1) rrInt = 1;
            int rr2 = rrInt * rrInt;
            for (int dx = -rrInt; dx <= rrInt; dx++) {
                for (int dz = -rrInt; dz <= rrInt; dz++) {
                    int d = dx * dx + dz * dz;
                    if (d <= rr2) {
                        out.add(new BlockPos.MutableBlockPos(c.getX() + dx, c.getY() + dy, c.getZ() + dz));
                    }
                }
            }
        }
    }

    private static void torus(List<BlockPos.MutableBlockPos> out, BlockPos c, int r) {
        int tube = Math.max(1, r / 3);
        int major = r;
        int segments = Math.max(24, r * 6);
        int tubeR2 = tube * tube;
        for (int i = 0; i < segments; i++) {
            double a = (Math.PI * 2 * i) / segments;
            int cx = (int) Math.round(major * Math.cos(a));
            int cz = (int) Math.round(major * Math.sin(a));
            for (int dy = -tube; dy <= tube; dy++) {
                for (int ddx = -tube; ddx <= tube; ddx++) {
                    int x = cx + ddx;
                    if (ddx * ddx + dy * dy <= tubeR2) {
                        out.add(new BlockPos.MutableBlockPos(c.getX() + x, c.getY() + dy, c.getZ() + cz));
                    }
                }
            }
        }
    }
}