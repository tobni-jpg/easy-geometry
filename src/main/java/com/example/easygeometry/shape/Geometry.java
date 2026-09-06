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
     * guaranteed unique (no duplicates) and sorted for stable output.
     */
    public static List<BlockPos.MutableBlockPos> shellPositions(
            ShapeType type, net.minecraft.core.BlockPos center, int radius) {

        List<BlockPos.MutableBlockPos> out = new ArrayList<>();
        int r = Math.max(1, radius);

        switch (type) {
            case SPHERE -> sphere(out, center, r);
            case CYLINDER -> cylinder(out, center, r);
            case CONE -> cone(out, center, r);
            case TORUS -> torus(out, center, r);
        }
        return out;
    }

    private static void sphere(List<BlockPos.MutableBlockPos> out,
                               net.minecraft.core.BlockPos c, int r) {
        int r2 = r * r;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    int d = dx * dx + dy * dy + dz * dz;
                    // shell: inside-or-equal and not fully inside
                    if (d <= r2 && d > (r - 1) * (r - 1)) {
                        out.add(new BlockPos.MutableBlockPos(c.getX() + dx, c.getY() + dy, c.getZ() + dz));
                    }
                }
            }
        }
    }

    private static void cylinder(List<BlockPos.MutableBlockPos> out,
                                 net.minecraft.core.BlockPos c, int r) {
        int r2 = r * r;
        int h = Math.max(1, r); // height = diameter for a balanced look
        for (int dy = 0; dy < h; dy++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    int d = dx * dx + dz * dz;
                    if (d <= r2 && d > (r - 1) * (r - 1)) {
                        out.add(new BlockPos.MutableBlockPos(c.getX() + dx, c.getY() + dy, c.getZ() + dz));
                    }
                }
            }
        }
    }

    private static void cone(List<BlockPos.MutableBlockPos> out,
                             net.minecraft.core.BlockPos c, int r) {
        int h = Math.max(1, r);
        for (int dy = 0; dy <= h; dy++) {
            // radius shrinks from base (r) at bottom to 0 at top
            double t = 1.0 - (double) dy / h;
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

    private static void torus(List<BlockPos.MutableBlockPos> out,
                              net.minecraft.core.BlockPos c, int r) {
        // two co-planar circles offset by the tube radius; drawn as ring of blocks
        int tube = Math.max(1, r / 3);
        int major = r;
        int segments = Math.max(12, r * 6);
        for (int i = 0; i < segments; i++) {
            double a = (Math.PI * 2 * i) / segments;
            int cx = (int) Math.round(major * Math.cos(a));
            int cz = (int) Math.round(major * Math.sin(a));
            int baseY = c.getY();
            for (int dy = -tube; dy <= tube; dy++) {
                for (int d = -tube; d <= tube; d++) {
                    int dx = cx + d;
                    int dz = cz;
                    if (dx * dx + (long) cz * cz <= (major + tube) * (major + tube)
                            && dx * dx + (long) cz * cz >= (major - tube) * (major - tube)) {
                        out.add(new BlockPos.MutableBlockPos(c.getX() + dx, baseY + dy, c.getZ() + dz));
                    }
                }
            }
        }
    }
}
