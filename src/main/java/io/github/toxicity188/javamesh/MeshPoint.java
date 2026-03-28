/*
 * This source file is part of java-mesh.
 * Copyright (c) 2026 toxicity188
 * Licensed under the MIT License.
 * See LICENSE.md file for full license text.
 */

package io.github.toxicity188.javamesh;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * Vertex data for a mesh polygon, including model-space position and texture UV coordinates.
 * <pre>{@code
 * MeshPoint point = new MeshPoint(new Vector3f(0.0f, 1.0f, 0.0f), new Vector2f(0.5f, 1.0f));
 * }</pre>
 *
 * @param position mesh-space vertex position
 * @param uv texture coordinates used for tint sampling
 * @since 0.0.1
 */
public record MeshPoint(
    @NotNull Vector3f position,
    @NotNull Vector2f uv
) {
    /**
     * Creates a point with zero UV coordinates.
     *
     * @param position mesh-space vertex position
     * @throws NullPointerException if {@code position} is {@code null}
     * @since 0.0.1
     */
    public MeshPoint(@NotNull Vector3f position) {
        this(position, new Vector2f());
    }
}

