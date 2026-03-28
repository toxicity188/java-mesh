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

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

/**
 * Selects the triangle template used when converting mesh polygons into Minecraft models.
 * <p>
 * {@link #SINGLE} is used for closed surfaces whose neighboring polygons cover both sides,
 * while {@link #DOUBLE} is used for boundary surfaces that must remain visible from both directions.
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
        var hasBoundary = new boolean[size];

        class DisjointSet {
            int find(int i) {
                if (parent[i] == i) return i;
                return parent[i] = find(parent[i]);
            }

            void union(int i, int j) {
                var rootI = find(i);
                var rootJ = find(j);
                if (rootI != rootJ) {
                    parent[rootJ] = rootI;
                    hasBoundary[rootI] |= hasBoundary[rootJ];
                }
            }

            void markBoundary(int i) {
                hasBoundary[find(i)] = true;
            }

            boolean checkBoundary(int i) {
                return hasBoundary[find(i)];
            }
        }
        var ds = new DisjointSet();

        var edgeToPolygons = new Object2ObjectOpenHashMap<Edge, IntArrayList>();

        for (int i = 0; i < size; i++) {
            var points = polygons.get(i).points();
            var ptsSize = points.size();
            for (int j = 0; j < ptsSize; j++) {
                var p1 = points.get(j).position();
                var p2 = points.get((j + 1) % ptsSize).position();

                var edge = new Edge(p1, p2);
                edgeToPolygons.computeIfAbsent(edge, k -> new IntArrayList(2)).add(i);
            }
        }

        for (IntArrayList connectedIndices : edgeToPolygons.values()) {
            var connectedSize = connectedIndices.size();
            if (connectedSize == 1) {
                ds.markBoundary(connectedIndices.getInt(0));
            } else {
                int first = connectedIndices.getInt(0);
                for (int i = 1; i < connectedSize; i++) {
                    ds.union(first, connectedIndices.getInt(i));
                }
            }
        }

        for (int i = 0; i < size; i++) {
            var shape = polygons.get(i);
            consumer.accept(shape, ds.checkBoundary(i) ? MeshFace.DOUBLE : MeshFace.SINGLE);
        }
    }

    private record VectorKey(int x, int y, int z) implements Comparable<VectorKey> {
        private static final float MULTIPLIER = 10000.0f;

        static MeshFace.VectorKey from(Vector3f v) {
            return new MeshFace.VectorKey(
                Math.round(v.x * MULTIPLIER),
                Math.round(v.y * MULTIPLIER),
                Math.round(v.z * MULTIPLIER)
            );
        }

        @Override
        public int compareTo(@NotNull MeshFace.VectorKey o) {
            int c;
            if ((c = Integer.compare(x, o.x)) != 0) return c;
            if ((c = Integer.compare(y, o.y)) != 0) return c;
            return Integer.compare(z, o.z);
        }
    }

    private record Edge(@NotNull MeshFace.VectorKey v1, @NotNull MeshFace.VectorKey v2) {

        private Edge(@NotNull Vector3f a, @NotNull Vector3f b) {
            this(VectorKey.from(a), VectorKey.from(b));
        }

        private Edge {
            var a = v1;
            var b = v2;
            if (a.compareTo(b) > 0) {
                v2 = a;
                v1 = b;
            }
        }
    }
}

