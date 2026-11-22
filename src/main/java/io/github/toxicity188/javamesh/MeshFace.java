/*
 * This source file is part of java-mesh.
 * Copyright (c) 2026 toxicity188
 * Licensed under the MIT License.
 * See LICENSE.md file for full license text.
 */

package io.github.toxicity188.javamesh;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

/**
 * Selects the triangle template used when converting mesh polygons into Minecraft models.
 * <p>
 * An edge-connected component uses {@link #SINGLE} when each edge has two distinct
 * incident faces with opposite traversal directions. Open, inconsistent, or
 * degenerate components use {@link #DOUBLE}. Connectivity uses exact positions.
 * </p>
 * <pre>{@code
 * builder.load(shape, MeshFace.DOUBLE);
 * }</pre>
 *
 * @since 0.0.1
 */
public enum MeshFace {
    /**
     * Uses the single-sided Minecraft triangle model.
     *
     * @since 0.0.1
     */
    SINGLE,
    /**
     * Uses the double-sided Minecraft triangle model.
     *
     * @since 0.0.1
     */
    DOUBLE
    ;

    static void inferAndBuild(@NotNull List<MeshShape> polygons, @NotNull BiConsumer<MeshShape, MeshFace> consumer) {
        if (polygons.isEmpty()) return;
        int size = polygons.size();

        var parent = IntStream.range(0, size).toArray();
        var componentSize = new int[size];
        Arrays.fill(componentSize, 1);
        var defects = new int[size];
        class DisjointSet {
            int find(int i) {
                if (parent[i] == i) return i;
                return parent[i] = find(parent[i]);
            }

            void union(int i, int j) {
                int a = find(i), b = find(j);
                if (a == b) return;
                if (componentSize[a] < componentSize[b]) {
                    int swap = a;
                    a = b;
                    b = swap;
                }
                parent[b] = a;
                componentSize[a] += componentSize[b];
                defects[a] |= defects[b];
            }
        }
        var ds = new DisjointSet();
        var edgeToPolygons = new Object2ObjectOpenHashMap<Edge, IntArrayList>();
        for (int i = 0; i < size; i++) {
            var points = polygons.get(i).points();
            var previous = points.getLast().position();
            for (var point : points) {
                var next = point.position();
                if (!previous.isFinite() || !next.isFinite()) {
                    defects[i] |= DEGENERATE;
                } else {
                    var a = VectorKey.from(previous);
                    var b = VectorKey.from(next);
                    int direction = a.compareTo(b);
                    if (direction == 0) {
                        defects[i] |= DEGENERATE;
                    } else {
                        var edge = direction < 0 ? new Edge(a, b) : new Edge(b, a);
                        // Complement the face index for a traversal against canonical order.
                        edgeToPolygons.computeIfAbsent(edge, ignored -> new IntArrayList(2))
                                .add(direction < 0 ? i : ~i);
                    }
                }
                previous = next;
            }
        }
        for (var owners : edgeToPolygons.values()) {
            int first = owners.getInt(0);
            int face = faceIndex(first);
            int defect = 0;
            if (owners.size() == 1) {
                defect = BOUNDARY;
            } else if (owners.size() > 2 || faceIndex(owners.getInt(1)) == face) {
                defect = NON_MANIFOLD;
            } else if ((first < 0) == (owners.getInt(1) < 0)) {
                defect = WINDING;
            }
            for (int j = 1; j < owners.size(); j++) ds.union(face, faceIndex(owners.getInt(j)));
            defects[ds.find(face)] |= defect;
        }
        for (int i = 0; i < size; i++) {
            consumer.accept(polygons.get(i), defects[ds.find(i)] == 0 ? SINGLE : DOUBLE);
        }
    }

    private static final int BOUNDARY = 1;
    private static final int NON_MANIFOLD = 2;
    private static final int WINDING = 4;
    private static final int DEGENERATE = 8;

    private static int faceIndex(int owner) {
        return owner < 0 ? ~owner : owner;
    }

    private record VectorKey(int x, int y, int z) implements Comparable<VectorKey> {
        static VectorKey from(Vector3f v) {
            return new VectorKey(bits(v.x), bits(v.y), bits(v.z));
        }

        private static int bits(float value) {
            return value == 0f ? 0 : Float.floatToIntBits(value);
        }

        @Override
        public int compareTo(@NotNull VectorKey other) {
            int c = Integer.compare(x, other.x);
            if (c == 0) c = Integer.compare(y, other.y);
            return c == 0 ? Integer.compare(z, other.z) : c;
        }
    }

    private record Edge(VectorKey a, VectorKey b) {}
}
