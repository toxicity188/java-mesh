/*
 * This source file is part of java-mesh.
 * Copyright (c) 2026 toxicity188
 * Licensed under the MIT License.
 * See LICENSE.md file for full license text.
 */

package io.github.toxicity188.javamesh;

import org.jetbrains.annotations.NotNull;

/**
 * Holds the Minecraft model ids used for generated triangle elements.
 * <pre>{@code
 * MeshTriangleName names = MeshTriangleName.of("item/mesh_triangle", "item/mesh_triangle_double");
 * }</pre>
 *
 * @param single model id for triangles that only need a single visible face
 * @param duplex model id for triangles that must be visible from both sides
 * @since 0.0.1
 */
public record MeshTriangleName(@NotNull String single, @NotNull String duplex) {
    /**
     * Creates a record holding the model ids for single-sided and double-sided triangles.
     *
     * @param single model id for single-sided triangles
     * @param duplex model id for double-sided triangles
     * @return a new triangle-name record
     * @since 0.0.1
     */
    public static @NotNull MeshTriangleName of(@NotNull String single, @NotNull String duplex) {
        return new MeshTriangleName(single, duplex);
    }
}

