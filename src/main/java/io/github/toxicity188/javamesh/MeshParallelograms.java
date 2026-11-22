/*
 * This source file is part of java-mesh.
 * Copyright (c) 2026 toxicity188
 * Licensed under the MIT License.
 * See LICENSE.md file for full license text.
 */

package io.github.toxicity188.javamesh;

import org.joml.Vector3f;
import org.joml.Matrix4f;

import java.util.*;

final class MeshParallelograms {
    private final MeshNamedImage images;
    private final MeshTriangleName triangles;
    private final List<Triangle> faces = new ArrayList<>();
    private final Map<Edge, Owners> edges = new HashMap<>();
    int shapeCount;

    MeshParallelograms(MeshNamedImage images, MeshTriangleName triangles) {
        this.images = images;
        this.triangles = triangles;
    }

    private record Edge(Vector3f a, Vector3f b) {
        static Edge of(Vector3f a, Vector3f b) {
            int order = Float.compare(a.x, b.x);
            if (order == 0) order = Float.compare(a.y, b.y);
            if (order == 0) order = Float.compare(a.z, b.z);
            return order <= 0 ? new Edge(a, b) : new Edge(b, a);
        }
    }

    private static final class Owners {
        int count;
        int first;
        int second;
    }

    private record Triangle(MeshPoint[] points, Owners[] edges, MeshMatrix matrix,
                            MeshFace face, String texture, String material) {}

    void add(MeshShape shape, MeshFace face, String material) {
        shapeCount++;
        var points = shape.points();
        var texture = images.get(shape.texture());
        if (points.size() == 3) {
            add(points.get(0), points.get(1), points.get(2), texture, shape.texture(), material, face);
            return;
        }
        var indices = shape.triangulateEarClipping();
        for (int i = 0; i < indices.size(); i += 3) {
            add(points.get(indices.getInt(i)), points.get(indices.getInt(i + 1)), points.get(indices.getInt(i + 2)),
                    texture, shape.texture(), material, face);
        }
    }

    private void add(MeshPoint a, MeshPoint b, MeshPoint c, MeshImage image, String texture, String material, MeshFace face) {
        var points = new MeshPoint[]{a, b, c};
        var owners = new Owners[3];
        for (int i = 0; i < 3; i++) {
            var key = Edge.of(points[i].position(), points[(i + 1) % 3].position());
            var entry = edges.computeIfAbsent(key, ignored -> new Owners());
            if (entry.count == 0) entry.first = faces.size();
            if (entry.count == 1) entry.second = faces.size();
            entry.count++;
            owners[i] = entry;
        }
        faces.add(new Triangle(points, owners, MeshMath.triangleMat(a, b, c, image), face, texture, material));
    }

    void emit(List<MeshModel> models) {
        var consumed = new boolean[faces.size()];
        for (int i = 0; i < faces.size(); i++) {
            var t = faces.get(i);
            if (consumed[i] || !t.matrix.transformation().isFinite()) continue;
            var parallelogram = merge(i, t, consumed);
            var model = t.face == MeshFace.SINGLE ? triangles.single() : triangles.duplex();
            if (parallelogram != null) models.add(new MeshModel(model, t.matrix.tint(), parallelogram));
            else MeshModel.create(model, t.matrix, models);
        }
    }

    private Matrix4f merge(int index, Triangle t, boolean[] consumed) {
        for (var edge : t.edges) if (edge.count > 2) return null;
        for (int i = 0; i < 3; i++) {
            var edge = t.edges[i];
            if (edge.count != 2 || edge.first != index || edge.second == index || consumed[edge.second]) continue;
            var other = faces.get(edge.second);
            if (other.face != t.face || !Objects.equals(t.texture, other.texture)
                    || !Objects.equals(t.material, other.material) || other.matrix.tint() != t.matrix.tint()
                    || !other.matrix.transformation().isFinite()) continue;
            var a = t.points[i];
            var c = t.points[(i + 1) % 3];
            var d = t.points[(i + 2) % 3];
            for (int j = 0; j < 3; j++) {
                if (other.edges[j] != edge || other.edges[(j + 1) % 3].count > 2 || other.edges[(j + 2) % 3].count > 2
                        || !c.equals(other.points[j]) || !a.equals(other.points[(j + 1) % 3])) continue;
                var b = other.points[(j + 2) % 3];
                var matrix = MeshMath.parallelogramMat(a.position(), b.position(), c.position(), d.position());
                if (matrix == null) continue;
                consumed[edge.second] = true;
                return matrix;
            }
        }
        return null;
    }
}
