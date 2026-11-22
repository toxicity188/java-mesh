/*
 * This source file is part of java-mesh.
 * Copyright (c) 2026 toxicity188
 * Licensed under the MIT License.
 * See LICENSE.md file for full license text.
 */

package io.github.toxicity188.javamesh;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Math.*;

/**
 * Polygon data extracted from a mesh source before conversion into Minecraft triangle models.
 * <p>
 * A shape contains at least three points and an optional texture reference. During conversion it is
 * triangulated and each triangle becomes one or more Minecraft model elements.
 * </p>
 * <pre>{@code
 * MeshShape quad = new MeshShape(List.of(p0, p1, p2, p3), "blade.png");
 * }</pre>
 *
 * @param points polygon points in winding order
 * @param texture optional texture name referenced by the source mesh material
 * @since 0.0.1
 */
public record MeshShape(@NotNull @Unmodifiable List<MeshPoint> points, @Nullable String texture) {

    private static final float EPS = 1.0e-6f;

    /**
     * Validates polygon data for mesh conversion.
     *
     * @throws IllegalArgumentException if fewer than three points are supplied
     * @since 0.0.1
     */
    public MeshShape {
        if (points.size() < 3) throw new IllegalArgumentException("points must be >= 3");
    }

    /**
     * Creates an untextured polygon from the provided points.
     *
     * @param points polygon points in winding order
     * @return a new shape without a texture reference
     * @throws NullPointerException if {@code points} is {@code null}
     * @throws IllegalArgumentException if fewer than three points are supplied
     * @since 0.0.1
     */
    public static @NotNull MeshShape of(@NotNull MeshPoint... points) {
        return new MeshShape(List.of(points), null);
    }

    @NotNull
    @Unmodifiable
    List<MeshMatrix> toTriangleMat(@NotNull MeshNamedImage images) {
        var image = images.get(texture);
        if (points.size() == 3) {
            return Collections.singletonList(MeshMath.triangleMat(
                points.get(0),
                points.get(1),
                points.get(2),
                image
            ));
        }

        var triangleIndices = triangulateEarClipping();
        var result = new ArrayList<MeshMatrix>(triangleIndices.size() / 3);

        for (int i = 0; i < triangleIndices.size(); i += 3) {
            result.add(MeshMath.triangleMat(
                points.get(triangleIndices.getInt(i)),
                points.get(triangleIndices.getInt(i + 1)),
                points.get(triangleIndices.getInt(i + 2)),
                image
            ));
        }

        return Collections.unmodifiableList(result);
    }

    @NotNull IntList triangulateEarClipping() {
        var projected = projectTo2D(points);
        var ccw = polygonArea2D(projected) > 0.0f;

        var polygon = new IntArrayList(points.size());
        for (int i = 0; i < points.size(); i++) {
            polygon.add(i);
        }

        var triangles = new IntArrayList((points.size() - 2) * 3);

        var guard = 0;
        var maxGuard = points.size() * points.size();

        while (polygon.size() > 3) {
            var earFound = false;

            for (int i = 0; i < polygon.size(); i++) {
                int prev = polygon.getInt((i - 1 + polygon.size()) % polygon.size());
                int curr = polygon.getInt(i);
                int next = polygon.getInt((i + 1) % polygon.size());

                if (isEar(prev, curr, next, polygon, projected, ccw)) {
                    triangles.add(prev);
                    triangles.add(curr);
                    triangles.add(next);

                    polygon.removeInt(i);
                    earFound = true;
                    break;
                }
            }

            if (!earFound) {
                throw new IllegalStateException("Failed to triangulate polygon: no ear found");
            }

            guard++;
            if (guard > maxGuard) {
                throw new IllegalStateException("Failed to triangulate polygon: guard exceeded");
            }
        }

        triangles.add(polygon.getInt(0));
        triangles.add(polygon.getInt(1));
        triangles.add(polygon.getInt(2));

        return triangles;
    }

    private static boolean isEar(
        int prev,
        int curr,
        int next,
        @NotNull IntList polygon,
        @NotNull List<Vector2f> projected,
        boolean ccw
    ) {
        var a = projected.get(prev);
        var b = projected.get(curr);
        var c = projected.get(next);

        var cross = cross(a, b, c);

        if (ccw) {
            if (cross <= EPS) return false;
        } else {
            if (cross >= -EPS) return false;
        }

        for (int idx : polygon) {
            if (idx == prev || idx == curr || idx == next) continue;

            Vector2f p = projected.get(idx);
            if (pointInTriangle(p, a, b, c, ccw)) {
                return false;
            }
        }

        return true;
    }

    private static boolean pointInTriangle(
        @NotNull Vector2f p,
        @NotNull Vector2f a,
        @NotNull Vector2f b,
        @NotNull Vector2f c,
        boolean ccw
    ) {
        var c1 = cross(a, b, p);
        var c2 = cross(b, c, p);
        var c3 = cross(c, a, p);

        if (ccw) {
            return c1 >= -EPS && c2 >= -EPS && c3 >= -EPS;
        } else {
            return c1 <= EPS && c2 <= EPS && c3 <= EPS;
        }
    }

    private static float cross(@NotNull Vector2f a, @NotNull Vector2f b, @NotNull Vector2f c) {
        var abx = (double) b.x - a.x;
        var aby = (double) b.y - a.y;
        var acx = (double) c.x - a.x;
        var acy = (double) c.y - a.y;

        return (float) fma(abx, acy, -aby * acx);
    }

    private static float polygonArea2D(@NotNull List<Vector2f> pts) {
        var sum = 0.0;

        for (int i = 0; i < pts.size(); i++) {
            var a = pts.get(i);
            var b = pts.get((i + 1) % pts.size());

            sum = fma(a.x, b.y, sum);
            sum = fma(-a.y, b.x, sum);
        }

        return (float) (sum * 0.5);
    }

    private static @NotNull List<Vector2f> projectTo2D(@NotNull List<MeshPoint> points) {
        var normal = computePolygonNormal(points);

        var ax = abs(normal.x);
        var ay = abs(normal.y);
        var az = abs(normal.z);

        var result = new ArrayList<Vector2f>(points.size());

        if (ax >= ay && ax >= az) {
            for (MeshPoint p : points) {
                result.add(new Vector2f(p.position().y, p.position().z));
            }
        } else if (ay >= ax && ay >= az) {
            for (MeshPoint p : points) {
                result.add(new Vector2f(p.position().x, p.position().z));
            }
        } else {
            for (MeshPoint p : points) {
                result.add(new Vector2f(p.position().x, p.position().y));
            }
        }

        return result;
    }

    private static @NotNull Vector3f computePolygonNormal(@NotNull List<MeshPoint> points) {
        var normal = new Vector3f();

        for (int i = 0; i < points.size(); i++) {
            var current = points.get(i).position();
            var next = points.get((i + 1) % points.size()).position();

            normal.x += (current.y - next.y) * (current.z + next.z);
            normal.y += (current.z - next.z) * (current.x + next.x);
            normal.z += (current.x - next.x) * (current.y + next.y);
        }

        if (normal.lengthSquared() < EPS) {
            throw new IllegalArgumentException("Degenerate polygon: cannot compute normal");
        }

        return normal.normalize();
    }
}

